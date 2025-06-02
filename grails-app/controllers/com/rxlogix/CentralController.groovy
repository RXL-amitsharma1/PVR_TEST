package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.config.*
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.enums.ReasonOfDelayAppEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.async.Promises
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.sql.Sql
import org.apache.commons.io.FileUtils
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile

import java.text.SimpleDateFormat

@Secured(['ROLE_PVC_VIEW'])
class CentralController {
    def reportExecutorService
    def qualityService
    def userService
    def utilService
    def dataSource_pva
    def index() {
        forward(controller: "dashboard", action: 'index')
    }

    def dashboard() {
        forward(controller: "dashboard", action: 'index')
    }

    def newDashboard() {
        forward(controller: "dashboard", action: 'newDashboard')
    }

    def removeDashboard() {
        forward(controller: "dashboard", action: 'removeDashboard')
    }
    def importRcaForm(){
        Map model = [:]
        AuditTrail auditTrail
        if(params.boolean("submit")) {
            auditTrail = new AuditTrail(category: AuditTrail.Category.INSERT.toString(), username: userService.currentUser.username,
                    fullname: userService.currentUser.fullName, applicationName: Holders.config.getProperty('grails.plugin.auditLog.applicationName'), description: "Import successful",
                    entityName: AuditTrail.Category.INSERT.displayName, moduleName: "Import RCA", entityValue: "Imported data from Import RCA successfully", transactionId: "" + System.currentTimeMillis())
            auditTrail.save(flush: true, failOnError: true)
        }
        model.rows = getPreValidate(getDataListFromParams(params, Constants.Central.RCA_COLUMN_NUMBER), params.boolean("submit"), params.replace != "append", auditTrail)
        render(model: model, view: "importRca")
    }

    def importSubmissionsForm() {
        Map model = [:]
        AuditTrail auditTrail
        if(params.boolean("submit")) {
            auditTrail = new AuditTrail(category: AuditTrail.Category.INSERT.toString(), username: userService.currentUser.username,
                    fullname: userService.currentUser.fullName, applicationName: Holders.config.getProperty('grails.plugin.auditLog.applicationName'), description: "Import successful",
                    entityName: AuditTrail.Category.INSERT.displayName, moduleName: "Import Submissions", entityValue: "Imported data from Import Submissions successfully", transactionId: "" + System.currentTimeMillis())
            auditTrail.save(flush: true, failOnError: true)
        }
        model.rows = getPreValidateSubmission(getDataListFromParams(params, Constants.Central.SUBMISSION_COLUMN_NUMBER), params.boolean("submit"), auditTrail)
        render(model: model, view: "importSubmissions")
    }

    private static List getDataListFromParams(params, int columnNumber) {
        List list = []
        for (int i = 0; i < params.int("total"); i++) {
            List r = []
            for (int j = 0; j < columnNumber; j++) {
                r << params["cell_" + i + "_" + j]
            }
            if (r.find { it }) list << r
        }
        return list
    }

    def importRca() {
        Map model = [:]
        if (request.method == 'POST') {
            List list = parseFile(getExcelWorkbook(request), Constants.Central.RCA_COLUMN_NUMBER)
            if (list.isEmpty()) {
                flash.error = message(code: 'app.import.file.empty')
                return;
            }
            model.rows = getPreValidate(list, false, true)
        }
        model
    }

    private static Workbook getExcelWorkbook(request) {
        MultipartFile file = request.getFile('file')
        Workbook workbook = null

        if (file.originalFilename?.toLowerCase()?.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(file.inputStream)
        } else if (file.originalFilename.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(file.inputStream)
        }
        workbook
    }

    def importSubmissions() {
        Map model = [:]
        if (request.method == 'POST') {
            List list = parseFile(getExcelWorkbook(request), Constants.Central.SUBMISSION_COLUMN_NUMBER)
            if (list.isEmpty()) {
                flash.error = message(code: 'app.import.file.empty')
                return;
            }
            model.rows = getPreValidateSubmission(list, false)
        }
        model
    }

    private List parseFile(Workbook workbook,int columnNumber){
        List list = []
        Sheet sheet = workbook?.getSheetAt(0);
        Row row;
        if (sheet)
            for (int i = 1; i <= sheet?.getLastRowNum(); i++) {
                if ((row = sheet.getRow(i)) != null) {
                    List r = []
                    for (int j = 0; j < columnNumber; j++) {
                        r << getExcelCell(row, j, (!(columnNumber==Constants.Central.SUBMISSION_COLUMN_NUMBER && j==6) && j!=15))
                    }
                    list << r
                }
            }
        return list
    }

    def downloadTemplate() {
        render(file: this.class.classLoader.getResourceAsStream("export/Reason_of_Delay.xlsx"), fileName: "template.xlsx", contentType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
    }

    def downloadSubmissionsTemplate() {
        render(file: this.class.classLoader.getResourceAsStream("export/Submissions.xlsx"), fileName: "submission_tpl.xlsx", contentType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
    }

    private String getExcelCell(Row row, int i, Boolean toDate = true) {
        Cell cell = row?.getCell(i)
        // For Case Number - Temporary Fix
        if (i == 0){
            cell?.setCellType(CellType.STRING);
        }
        if ((cell?.getCellType() == CellType.NUMERIC) && toDate) {
            return cell.getDateCellValue().format("dd-MMM-yyyy")
        } else if ((cell?.getCellType() == CellType.NUMERIC)) { // For RCA Import - Version Number Column
            cell?.setCellType(CellType.NUMERIC);
            boolean isInteger = isInteger(cell?.getNumericCellValue())
            if(isInteger) {
                int value = (int)cell?.getNumericCellValue()
                return value
            }
            return cell?.getNumericCellValue()
        } else {
            cell?.setCellType(CellType.STRING);
            return cell?.getStringCellValue()?.trim()
        }
    }

    boolean isInteger(double number) {
        return number % 1 == 0;// if the modulus(remainder of the division) of the argument(number) with 1 is 0 then return true otherwise false.
    }

    private List getPreValidate(List list, Boolean submit, Boolean replace, AuditTrail auditTrail = null){
        def labels=[
                message(code:"app.pvc.import.CaseNumber"),
                message(code:"app.pvc.import.ReportAgencyName"),
                message(code:"app.pvc.import.CaseReceiptDate"),
                message(code:"app.pvc.import.ReportDueDate"),
                message(code:"app.pvc.import.SubmissionDate"),
                message(code:"app.pvc.late"),
                message(code:"app.pvc.rootcause"),
                message(code:"app.pvc.RootCauseClass"),
                message(code:"app.pvc.RootCauseSubCategory"),
                message(code:"app.pvc.ResponsibleParty"),
                message(code:"app.pvc.CorrectiveAction"),
                message(code:"app.pvc.PreventiveAction"),
                message(code:"app.pvc.CorrectiveDate"),
                message(code:"app.pvc.PreventiveDate"),
                message(code:"app.pvc.import.Primary"),
                message(code:"app.pvc.version"),
                message(code:"app.pvc.investigation"),
                message(code:"app.pvc.summary"),
                message(code:"app.pvc.actions")
        ]
        list.each{ row->
            StringBuilder errors=new StringBuilder()
            [0,1,2,3,4,5,6,9,14].each { i ->
                if (!row[i]) errors.append(message(code: "app.pvc.import.notEmpty", args: [labels[i]]))
            }
            [2, 3, 4, 12, 13].each { i ->
                if (row[i] && !DateUtil.checkDate(row[i], DateUtil.DATEPICKER_FORMAT)) errors.append(message(code: "app.pvc.import.notDate", args: [labels[i]]))
            }
            if (row[14] && !(row[14] in ["yes", "no"])) errors.append(message(code: "app.pvc.import.yesno", args: [labels[14]]))
            if ((replace && (row[14] == "no")) && !list.find { (row[0] == it[0]) && (row[1] == it[1]) && (row[2] == it[2]) && (row[3] == it[3]) && (row[4] == it[4]) && (it[14] == "yes") })
                errors.append(message(code: "app.pvc.import.noprimary", args: [labels[14]]))
            if ((row[14] == "yes") && (list.findAll { (row[0] == it[0]) && (row[1] == it[1]) && (row[2] == it[2]) && (row[3] == it[3]) && (row[4] == it[4]) && (it[14] == "yes") }?.size() > 1))
                errors.append(message(code: "app.pvc.import.oneprimary", args: [labels[14]]))
            row << errors.toString()
        }
        boolean submitted = reportExecutorService.importRcas(list, submit, replace)
        if (list.find { it[Constants.Central.RCA_COLUMN_NUMBER] })
            flash.error = message(code: "app.pvc.import.errorDetected")
        if(submit && submitted){
            flash.message = message(code: "app.pvc.import.importSuccess")
            list.each{ row ->
                if(auditTrail != null) {
                    AuditTrailChild auditTrailChild = new AuditTrailChild()
                    auditTrailChild.newValue = toRowMapString(labels, row)
                    auditTrailChild.oldValue = ""
                    auditTrailChild.propertyName = "Row " + (list.indexOf(row) + 1)
                    auditTrailChild.auditTrail = auditTrail
                    auditTrailChild.save(flush: true, failOnError: true)
                }
            }
            list=[]
        }
        list
    }

    private List getPreValidateSubmission(List list, Boolean submit, AuditTrail auditTrail = null) {
        def labels = [
                message(code: "app.pvc.import.CaseNumber"),
                message(code: "app.pvc.import.CaseReceiptDate"),
                message(code: "app.pvc.import.destination"),
                message(code: "app.pvc.import.ReportDueDate"),
                message(code: "app.pvc.import.SubmissionDate"),
                message(code: "app.pvc.import.ExpPeriodicSubmission"),
                message(code: "app.pvc.import.Timeframe"),
                message(code: "app.pvc.import.ReportForm"),
        ]
        list.each { row ->
            StringBuilder errors = new StringBuilder()
            [0, 2, 3, 4].each { i ->
                if (!row[i]) errors.append(message(code: "app.pvc.import.notEmpty", args: [labels[i]]))
            }
            [1, 3, 4].each { i ->
                if (row[i] && !DateUtil.checkDate(row[i], DateUtil.DATEPICKER_FORMAT)) errors.append(message(code: "app.pvc.import.notDate", args: [labels[i]]))
            }
            if (row[5] != null && !row[5].toString().isEmpty() && !(row[5] in ReportExecutorService.ALLOWED_SUBMISSION_TYPES)) {
                errors.append(message(code: "app.pvc.import.error.expperiodic"))
            }
            if (row[6] != null && !row[6].toString().isEmpty() && !row[6].toString().isInteger()) {
                errors.append(message(code: "app.pvc.import.error.timeframe"))
            }
            row << errors.toString()
        }
        boolean submitted = reportExecutorService.importSubmissions(list, submit)
        if (list.find { it[8] })
            flash.error = message(code: "app.pvc.import.errorDetected")
        if (submit && submitted) {
            flash.message = message(code: "app.pvc.import.importSuccess")
            list.each{ row ->
                if(auditTrail != null) {
                    AuditTrailChild auditTrailChild = new AuditTrailChild()
                    auditTrailChild.newValue = toRowMapString(labels, row)
                    auditTrailChild.oldValue = ""
                    auditTrailChild.propertyName = "Row " + (list.indexOf(row) + 1)
                    auditTrailChild.auditTrail = auditTrail
                    auditTrailChild.save(flush: true, failOnError: true)
                }
            }
            list = []
        }
        list
    }

    def actionPlan() {
    }

    def actionPlanList() {
        String timeZone = userService.currentUser.preference.timeZone
        Map<String, Date> ranges = qualityService.getRanges(params);
        Map json = [aaData: getActionPlanData(params, ranges)]
        json.periods = ranges.collectEntries { k, v -> [(k): v.format(DateUtil.DATEPICKER_FORMAT, TimeZone.getTimeZone(timeZone))] }
        render json as JSON
    }

    private static List<String> parseParamList(par) {
        return par instanceof String[] ? par : par?.split(";")?.findAll { it }
    }

    static List getActionPlanData(params, Map<String, Date> ranges) {
        QualityService qualityService = Holders.getApplicationContext().getBean("qualityService")
        Map responsiblePartyName
        Map rcName
        ResponsibleParty.withNewTransaction {
            responsiblePartyName = ResponsibleParty.findAllByOwnerApp(ReasonOfDelayAppEnum.PVC.name()).findAll{it}.collectEntries { [("" + it.id): it.textDesc] }
            rcName = RootCause.findAllByOwnerApp(ReasonOfDelayAppEnum.PVC.name()).findAll{it}.collectEntries { [("" + it.id): it.textDesc] }
        }
        rcName.put("0", ViewHelper.getEmptyLabel())
        responsiblePartyName.put("0", ViewHelper.getEmptyLabel())
        Sql sql
        List result = []
        try {
            TreeSet rows = new TreeSet()
            sql = new Sql( Holders.getApplicationContext().getBean("dataSource_pva"))
            List<Long> responsiblePartyFilter = parseParamList(params.responsiblePartyFilter)?.collect { it as Long }
            List<String> lateFilter = parseParamList(params.lateFilter)
            List<String> destinationFilter = parseParamList(params.destinationFilter)
            List<String> rcFilter = parseParamList(params.rcFilter)
            List<String> classFilter = parseParamList(params.classFilter)
            List<String> subFilter = parseParamList(params.subFilter)

            List periods = []
            for (int i = 0; ; i++) {
                if (!ranges["from" + i]) break;
                periods << fetchActonPlanData(sql, ranges["from" + i], ranges["to" + i], rows, responsiblePartyFilter, lateFilter, destinationFilter, params.workflowFilter, params.primaryOnly as Boolean,rcFilter,classFilter,subFilter)
            }
            Map<String, Map> actionItems = [:]
            for (int i = 0; ; i++) {
                if (!ranges["from" + i]) break;
                qualityService.fetchActionItems(rows, ranges["from" + i], ranges["to" + i], i, actionItems)
            }

            rows.each {
                Map lastRow = getActionPlanNotNullRow(periods[0], it, responsiblePartyName, rcName)
                Map actionItemStat = actionItems.get(it)
                Map fullRow = [
                        responsibleParty  : lastRow.responsibleParty,
                        responsiblePartyId: lastRow.responsiblePartyId,
                        rc              : lastRow.rc,
                        rcCode          : lastRow.rcCode,
                        destination       : lastRow.destination
                ]

                Integer previousNumber = null
                periods.eachWithIndex { periodRow, int i ->
                    Map periodRowNotNull = getActionPlanNotNullRow(periods[i], it, responsiblePartyName, rcName)
                    fullRow["lastNumber" + i] = periodRowNotNull.number
                    fullRow["lastVendor" + i] = periodRowNotNull.ofVendor
                    fullRow["lastRc" + i] = periodRowNotNull.ofRc
                    fullRow["lastDestination" + i] = periodRowNotNull.ofDestination
                    fullRow["completed" + i] = qualityService.toPercent(actionItemStat?.get("completed" + i), actionItemStat?.get("total" + i))
                    fullRow["overdue" + i] = qualityService.toPercent(actionItemStat?.get("overdue" + i), actionItemStat?.get("total" + i))
                    fullRow["total" + i] = actionItemStat?.get("total" + i) ?: 0
                    if (previousNumber != null) {
                        fullRow["lastToPrevious" + (i - 1)] = qualityService.toPercent(previousNumber - periodRowNotNull.number, periodRowNotNull.number)
                    }
                    previousNumber = periodRowNotNull.number
                }
                result << fullRow
            }

        } finally {
            sql?.close()
        }
        return result
    }

    private static Map getActionPlanNotNullRow(Map data, String key, Map responsiblePartyName, Map rcName) {
        Map row = data[key]
        if (row) return row
        String[] keyParts = key.split("_@_")
        return [
                responsibleParty  : responsiblePartyName["" + keyParts[0]] ?: ("" + keyParts[0]),
                responsiblePartyId: keyParts[0],
                rc              : rcName["" + keyParts[1]] ?: ("" + keyParts[1]),
                rcCode          : keyParts[1],
                destination       : keyParts[2],
                number            : 0,
                ofRc            : 0,
                ofVendor          : 0,
                ofDestination     : 0
        ]
    }

    private static String getWhereClause(List responsiblePartyFilter, List lateFilter, List destinationFilter, String workflowFilter, Boolean primaryOnly, List rcFilter,List classFilter,List subFilter) {
        String whereClause = ""
        if (responsiblePartyFilter && (responsiblePartyFilter[0] != 0L)) {
            whereClause += " AND RESPONSIBLE_PARTY_ID in (" + responsiblePartyFilter.join(",") + ") "
        }
        if (lateFilter) {
            whereClause += " AND LATE_ID in (" + lateFilter.join(",") + ") "
        }
        if (destinationFilter && (destinationFilter[0] != "null")) {
            whereClause += " AND DESTINATION_DESC in ('" + destinationFilter.join("','") + "') "
        }
        if (rcFilter && (rcFilter[0] != "0")) {
            whereClause += " AND ROOT_CAUSE_ID in ('" + rcFilter.join("','") + "') "
        }
        if (classFilter) {
            whereClause += " AND ROOT_CAUSE_CLASS_ID in ('" + classFilter.join("','") + "') "
        }
        if (subFilter) {
            whereClause += " AND ROOT_CAUSE_SUB_CAT_ID in ('" + subFilter.join("','") + "') "
        }
        if (primaryOnly) {
            whereClause += " AND FLAG_PRIMARY=1 "
        }

        if (workflowFilter) {
            Set<WorkflowState> workflowFinalStates = []
            Set<WorkflowState> workflowNotFinalStates = []
            if (workflowFilter in ["final", "notFinal"]) {
                WorkflowRule.findAllByConfigurationTypeEnumAndIsDeletedAnd(WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY, false).each {
                    if (it.targetState?.finalState)
                        workflowFinalStates.add(it.targetState)
                    else
                        workflowNotFinalStates.add(it.targetState)
                }
                if (workflowFilter == "notFinal")
                    whereClause += " AND WORKFLOW_STATE_ID in (${(workflowNotFinalStates) ? workflowNotFinalStates*.id.join(",") : null})"
                else if (workflowFilter == "final")
                    whereClause += " AND WORKFLOW_STATE_ID in (${(workflowFinalStates) ? workflowFinalStates*.id.join(",") : null})"
            } else {
                whereClause += " AND WORKFLOW_STATE_ID in (${workflowFilter.replaceAll(";", ",")}) "
            }

        }
        return whereClause
    }

    private static Map fetchActonPlanData(Sql sql, Date from, Date to, Set rows, List responsiblePartyFilter, List lateFilter, List destinationFilter, String workflowFilter, Boolean primaryOnly,List rcFilter,List classFilter,List subFilter) {
        QualityService qualityService = Holders.getApplicationContext().getBean("qualityService")
        Map queryParams = [from: new java.sql.Date(from.getTime()), to: new java.sql.Date(to.getTime())]
        String whereClause = getWhereClause(responsiblePartyFilter, lateFilter, destinationFilter, workflowFilter, primaryOnly,rcFilter,classFilter,subFilter)
        String caseQuery = """select nvl(RESPONSIBLE_PARTY_ID,0) RESPONSIBLE_PARTY_ID, REPONSIBLE_PARTY, nvl(ROOT_CAUSE_ID,0) ROOT_CAUSE_ID,ROOT_CAUSE, DESTINATION_DESC,  COUNT(DISTINCT PROCESSED_REPORT_ID)  Num  from VW_PVR_LATE_CASE_PROCESSING 
                where DETECTION_DATE>:from and DETECTION_DATE<:to and tenant_id=${Tenants.currentId()} ${whereClause}
                group by  RESPONSIBLE_PARTY_ID, ROOT_CAUSE_ID, DESTINATION_DESC,REPONSIBLE_PARTY,ROOT_CAUSE"""
        List currentPeriodDataList = sql.rows(caseQuery, queryParams)
        Map destinationTotal = [:]
        Map responsiblePartyTotal = [:]
        Map rcTotal = [:]
        currentPeriodDataList.each {
            int num = (it['NUM'] as Integer)
            if (responsiblePartyTotal[it['RESPONSIBLE_PARTY_ID']] == null) responsiblePartyTotal[it['RESPONSIBLE_PARTY_ID']] = 0
            responsiblePartyTotal[it['RESPONSIBLE_PARTY_ID']] += num
            if (rcTotal[it['ROOT_CAUSE_ID']] == null) rcTotal[it['ROOT_CAUSE_ID']] = 0
            rcTotal[it['ROOT_CAUSE_ID']] += num
            if (destinationTotal[it['DESTINATION_DESC']] == null) destinationTotal[it['DESTINATION_DESC']] = 0
            destinationTotal[it['DESTINATION_DESC']] += num
        }
        Map result = [:]
        currentPeriodDataList.each {
            String key = it['RESPONSIBLE_PARTY_ID'] + "_@_" + it['ROOT_CAUSE_ID'] + "_@_" + it['DESTINATION_DESC']
            rows.add(key)
            result.put(key,
                    [
                            responsibleParty  : it['REPONSIBLE_PARTY'] ?: ViewHelper.emptyLabel,
                            responsiblePartyId: it['RESPONSIBLE_PARTY_ID'],
                            rc              : it['ROOT_CAUSE'] ?: ViewHelper.emptyLabel,
                            rcCode          : it['ROOT_CAUSE_ID'],
                            destination       : it['DESTINATION_DESC'],
                            number            : it['NUM'] as Integer,
                            ofVendor          : qualityService.toPercent(it['NUM'], responsiblePartyTotal[it['RESPONSIBLE_PARTY_ID']]),
                            ofRc            : qualityService.toPercent(it['NUM'], rcTotal[it['ROOT_CAUSE_ID']]),
                            ofDestination     : qualityService.toPercent(it['NUM'], destinationTotal[it['DESTINATION_DESC']]),
                    ])
        }
        return result
    }

    def actionPlanCaseList() {
        Map json = [aaData: []]
        List<Long> responsiblePartyFilter = [params.responsibleParty as Long]
        List<String> destinationFilter = [params.destination]
        List<String> rcFilter = [params.rcCode]
        List<String> lateFilter = parseParamList(params.lateFilter)
        List<String> classFilter = parseParamList(params.classFilter)
        List<String> subFilter = parseParamList(params.subFilter)
        Preference preference = userService.currentUser?.preference
        String timeZone = preference?.timeZone
        Locale locale = preference?.locale
        if (params.period) {
            Map<String, Date> ranges = qualityService.getRanges(params);
            json.aaData = fetchActonPlanCasesData(ranges["from" + params.period], ranges["to" + params.period], responsiblePartyFilter, lateFilter, destinationFilter, params.workflowFilter, params.boolean("primaryOnly"), rcFilter, classFilter, subFilter, true, timeZone, locale)
        }
        render json as JSON
    }

    static List fetchActonPlanCasesData(Date from, Date to, List responsiblePartyFilter, List lateFilter, List destinationFilter, String workflowFilter, Boolean primaryOnly, List rcFilter, List classFilter, List subFilter, boolean exactMatch, String timeZone, Locale locale) {
        Sql sql
        List result = []

        Map dectionaryLabels = [:]
        try {

            sql = new Sql(Holders.getApplicationContext().getBean("dataSource_pva"))
            Map queryParams = [from: new java.sql.Date(from.getTime()), to: new java.sql.Date(to.getTime())]
            String whereClause = getWhereClause(responsiblePartyFilter, lateFilter, destinationFilter, workflowFilter, primaryOnly, rcFilter, classFilter, subFilter)

            if (exactMatch) {
                if (!destinationFilter || destinationFilter[0] == "null") {
                    whereClause += " AND DESTINATION_DESC is null "
                }
                if (!responsiblePartyFilter || (responsiblePartyFilter[0] == 0L)) {
                    whereClause += " AND RESPONSIBLE_PARTY_ID is null"
                }
                if (!rcFilter || (rcFilter[0] == "0")) {
                    whereClause += " AND ROOT_CAUSE_ID is null"
                }
            }
            String caseQuery = """select CASE_ID, PROCESSED_REPORT_ID, CASE_NUM,VERSION_NUM, DESTINATION_DESC, nvl(RESPONSIBLE_PARTY_ID,0) RESPONSIBLE_PARTY_ID,nvl(ROOT_CAUSE_ID,0) ROOT_CAUSE_ID, PROCESSED_REPORT_ID,
                ROOT_CAUSE,ROOT_CAUSE, REPONSIBLE_PARTY,ROOT_CAUSE_CLASSIFICATION,ROOT_CAUSE_SUB_CATEGORY, CORRECTIVE_ACTION,PREVENTATIVE_ACTION,CORRECTIVE_DATE,PREVENTATIVE_DATE,FLAG_PRIMARY,INVESTIGATION,SUMMARY,ACTIONS
                from VW_PVR_LATE_CASE_PROCESSING 
                where DETECTION_DATE>:from and DETECTION_DATE<:to and tenant_id=${Tenants.currentId()} ${whereClause}"""

            List caseList = sql.rows(caseQuery, queryParams)

            caseList.each {
                DrilldownCLLMetadata cllMetadata = DrilldownCLLMetadata.findByCaseIdAndProcessedReportIdAndTenantId(it['CASE_ID'] as Long, it["PROCESSED_REPORT_ID"].toString(), Tenants.currentId())
                String assignedTo = cllMetadata?.assignedToUser?.fullName ?: cllMetadata?.assignedToUserGroup?.name
                String workflow = cllMetadata?.workflowState?.name

                result.add(
                        [
                                id                : it['CASE_ID'],
                                caseNumber        : it['CASE_NUM'],
                                caseVersion       : it['VERSION_NUM'],
                                observation       : "",
                                assignedTo        : assignedTo,
                                rootCauseClass    : it['ROOT_CAUSE_CLASSIFICATION'] ?: "",
                                rootCauseSub      : it['ROOT_CAUSE_SUB_CATEGORY'] ?: "",
                                rootCause         : it['ROOT_CAUSE'] ?: "",
                                correctiveAction  : it['CORRECTIVE_ACTION'] ?: "",
                                responsibleParty  : it['REPONSIBLE_PARTY'] ?: "",
                                preventativeAction: it['PREVENTATIVE_ACTION'] ?: "",
                                correctiveDate    : it.CORRECTIVE_DATE?DateUtil.getShortDateStringForLocaleAndTimeZone(new Date(it.CORRECTIVE_DATE?.getTime()), locale, timeZone):"",
                                preventativeDate  : it.PREVENTATIVE_DATE?DateUtil.getShortDateStringForLocaleAndTimeZone(new Date(it.PREVENTATIVE_DATE?.getTime()), locale, timeZone):"",
                                investigation     : it['INVESTIGATION'],
                                summary           : it['SUMMARY'],
                                actions           : it['ACTIONS'],
                                primary           : it['FLAG_PRIMARY'],
                                workFlowState     : workflow,
                                reportId          : it['PROCESSED_REPORT_ID'] ?: ""
                        ])
            }
            return result
        } finally {
            sql?.close()
        }
    }

    def exportActionPlanToExcel() {
        Map m = [:]
        Long userId = userService.currentUser.id
        String timeZone = userService.currentUser.preference.timeZone
        Locale locale = userService.currentUser.preference.locale
        String tempdir = grailsApplication.config.tempDirectory
        m.putAll(JSON.parse(params.data))
        m.timeZone = timeZone
        Promises.task {
            QualitySampling.withNewSession {
                String fileName = "Action Plan.xlsx"
                byte[] data = prepareActionPlanExcel(m, fileName, timeZone, locale)
                File reportFile = new File(tempdir + "/" + System.currentTimeMillis() + ".xlsx")
                FileUtils.writeByteArrayToFile(reportFile, data)
                User user = User.get(userId)
                Notification notification = new Notification(user: user,
                        level: NotificationLevelEnum.INFO,
                        message: "app.notification.export",
                        messageArgs: fileName,
                        appName: NotificationApp.EXPORT,
                        executionStatusId: 0,
                        notificationParameters: """{"sourceFileName":"${reportFile.getName()}","userFileName":"${fileName}"}"""
                )
                notification.save(flush: true)
                userService.pushNotificationToBrowser(notification, user)
            }
        }.onError { Throwable err ->
            QualitySampling.withNewSession {
                User user = User.get(userId)
                Notification notification = new Notification(user: user,
                        level: NotificationLevelEnum.ERROR,
                        message: "app.notification.error",
                        messageArgs: err.getMessage(),
                        appName: NotificationApp.ERROR,
                        executionStatusId: 0
                )
                notification.save(flush: true)
                userService.pushNotificationToBrowser(notification, user)
                log.error("Error occurred preparing export file!", err)
                err.printStackTrace()
            }

        }
        render "ok"
        return
    }

    static byte[] prepareActionPlanExcel(Map params, String fileName, String timeZone, Locale locale) {
        QualityService qualityService = Holders.getApplicationContext().getBean("qualityService")
        Map<String, Date> ranges = qualityService.getRanges(params);
        List sheets = []
        def header = [[title: ViewHelper.getMessage("quality.capa.responsibleParty.label"), width: 25],
                      [title: ViewHelper.getMessage("app.pvc.rootcause"), width: 25],
                      [title: ViewHelper.getMessage("app.pvc.import.destination"), width: 25],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.NumberSubmissionReports"), width: 15],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pod"), width: 15],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.porc"), width: 15],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pcv"), width: 15],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.CorectPreventActions"), width: 15],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.cpp"), width: 15]]
        for (int i = 1; ; i++) {
            if (!ranges["from" + i]) break;
            header.addAll([
                    [title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.NumberSubmissionReports"), width: 15],
                    [title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pod"), width: 15],
                    [title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.porc"), width: 15],
                    [title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pcv"), width: 15],
                    [title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.CorectPreventActions"), width: 15]
            ])
            if (ranges["from" + (i + 1)]) header.add([title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.cpp"), width: 15])
        }

        def d = [data: getActionPlanData(params, ranges).collect { it ->
            List row = [it['responsibleParty'], it['rc'], it['destination'],]
            for (int i = 0; ; i++) {
                if (!ranges["from" + i]) break;
                row.addAll([it['lastNumber' + i], it['lastDestination' + i], it['lastRc' + i], it['lastVendor' + i], it['completed' + i]])
                if (ranges["from" + (i + 1)]) row.add(it['lastToPrevious' + i] > 1000 ? ">1000" : it['lastToPrevious' + i])
            }
            row
        }, metadata  : [sheetName: "Action Plan",
                        columns: header]]
        sheets << d
        List<Long> responsiblePartyFilter = parseParamList(params.responsiblePartyFilter)?.collect { it as Long }
        List<String> lateFilter = parseParamList(params.lateFilter)
        List<String> destinationFilter = parseParamList(params.destinationFilter)
        List<String> rcFilter = parseParamList(params.rcFilter)
        List<String> classFilter = parseParamList(params.classFilter)
        List<String> subFilter = parseParamList(params.subFilter)
        List caseHeader = [
                [title: ViewHelper.getMessage("app.case.id.label"), width: 25],
                [title: ViewHelper.getMessage("app.caseNumber.label"), width: 25],
                [title: ViewHelper.getMessage("app.label.quality.caseVersion"), width: 25],
                [title: ViewHelper.getMessage("app.label.processedReportId"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.rootcause"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.RootCauseClass"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.RootCauseSubCategory"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.ResponsibleParty"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.CorrectiveAction"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.PreventiveAction"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.CorrectiveDate"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.PreventiveDate"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.investigation"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.summary"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.actions"), width: 25],
                [title: ViewHelper.getMessage("app.pvc.import.Primary"), width: 25],
                [title: ViewHelper.getMessage("app.label.workflow.appName"), width: 25],
                [title: ViewHelper.getMessage("app.label.workflow.rule.assignedTo"), width: 25]
        ]

        for (int i = 0; ; i++) {
            if (!ranges["from" + i]) break;
            List data = fetchActonPlanCasesData(ranges["from" + i], ranges["to" + i], responsiblePartyFilter, lateFilter, destinationFilter, params.workflowFilter, params.primaryOnly as Boolean, rcFilter, classFilter, subFilter, false,timeZone, locale )
                    .collect { it ->
                        [it.id, it.caseNumber, it.caseVersion, it.reportId, it.rootCause, it.rootCauseClass, it.rootCauseSub, it.responsibleParty,
                         it.correctiveAction, it.preventativeAction, it.correctiveDate, it.preventativeDate, it.investigation, it.summary,
                         it.actions, it.primary, it.workFlowState, it.assignedTo]
                    }
            Map sheet = [
                    data    : data,
                    metadata: [sheetName: "Cases ${ranges["from" + i].format(DateUtil.ISO_DATE_FORMAT)} ${ranges["to" + i].format(DateUtil.ISO_DATE_FORMAT)}",//ISO_DATE_FORMAT berause sheet name has 30 char limit
                               columns  : caseHeader]
            ]
            sheets << sheet
        }
        byte[] data = qualityService.exportToExcel(sheets)
        AuditLogConfigUtil.logChanges(sheets, [outputFormat: ReportFormatEnum.XLSX.name(), fileName: fileName, exportedDate: new Date()],
                [:], Constants.AUDIT_LOG_EXPORT, ViewHelper.getMessage("auditLog.entityValue.action.plan.export", "PV Central"))
        return data
    }

    String toRowMapString(def labels, def row) {
        String result = ""
        for(int index = 0; index < labels.size(); index++) {
            if(row[index] != "")
                result += labels[index] + " : " + row[index] + "\n"
        }
        return result
    }
}
