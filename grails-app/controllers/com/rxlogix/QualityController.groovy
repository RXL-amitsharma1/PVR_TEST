package com.rxlogix

import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.config.*
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.enums.ReasonOfDelayAppEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FilterUtil
import com.rxlogix.util.ViewHelper
import grails.async.Promises
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException
import grails.web.mapping.LinkGenerator
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.grails.web.json.JSONObject
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile
import org.apache.commons.io.FileUtils

import javax.activation.MimetypesFileTypeMap
import java.text.SimpleDateFormat

@Secured(['ROLE_PVQ_VIEW'])
class QualityController implements SanitizePaginationAttributes {
    final static Integer BULK_UPDATE_MAX_ROWS = 500
    def userService
    def qualityService
    def dynamicReportService
    def utilService
    def emailService
    def CRUDService
    def sqlGenerationService
    def reportExecutorService
    def workflowService
    def importService
    def reportFieldService

    // Inject link generator
    LinkGenerator grailsLinkGenerator

    def index() {
        forward(controller: "dashboard", action: 'index')
    }

    def addReportWidget() {
        forward(controller: "dashboard", action: 'addReportWidget')
    }

    def removeReportWidgetAjax() {
        forward(controller: "dashboard", action: 'removeReportWidgetAjax')
    }

    def newDashboard() {
        forward(controller: "dashboard", action: 'newDashboard')
    }

    def removeDashboard() {
        forward(controller: "dashboard", action: 'removeDashboard')
    }

    def updateLabel() {
        forward(controller: "dashboard", action: 'updateLabel')
    }

    def updateReportWidgetsAjax() {
        forward(controller: "dashboard", action: 'updateReportWidgetsAjax')
    }

    def redirectFromWidget(String dataType){
        if(dataType && dataType == PvqTypeEnum.SUBMISSION_QUALITY.name()){
            redirect(action:  "submissionQuality")
        }else {
            redirect(action: "caseDataQuality")
        }
    }

    def ajaxCaseCount() {
        render status: HttpStatus.OK, contentType: 'application/json', text: getDataForErrorsCount(null, params.dataType) as JSON
    }

    def ajaxCaseDataCount() {
        render status: HttpStatus.OK, contentType: 'application/json', text: getCaseDataForErrorsCount() as JSON
    }

    def ajaxSubmissionCount() {
        render status: HttpStatus.OK, contentType: 'application/json', text: getSubmissionDataForErrorsCount() as JSON
    }

    def ajaxProductsCount() {
        Date from = (params.from ? Date.parse(DateUtil.ISO_DATE_FORMAT, params.from) : null)
        Date to = (params.to ? Date.parse(DateUtil.ISO_DATE_FORMAT, params.to) : null)
        render status: HttpStatus.OK, contentType: 'application/json', text: getBarDataForField("masterPrimProdName", from, false, to) as JSON
    }

    def ajaxCaseReportTypeCount() {
        Date from = (params.from ? Date.parse(DateUtil.ISO_DATE_FORMAT, params.from) : null)
        Date to = (params.to ? Date.parse(DateUtil.ISO_DATE_FORMAT, params.to) : null)
        render status: HttpStatus.OK, contentType: 'application/json', text: getBarDataForField("masterRptTypeId", from, true, to) as JSON
    }

    def ajaxEntrySiteCount() {
        Date from = (params.from ? Date.parse(DateUtil.ISO_DATE_FORMAT, params.from) : null)
        Date to = (params.to ? Date.parse(DateUtil.ISO_DATE_FORMAT, params.to) : null)
        render status: HttpStatus.OK, contentType: 'application/json', text: getBarDataForField("masterSiteId", from, false, to) as JSON
    }

    def ajaxTop20ErrorsCount() {
        Date from = (params.from ? Date.parse(DateUtil.ISO_DATE_FORMAT, params.from) : null)
        Date to = (params.to ? Date.parse(DateUtil.ISO_DATE_FORMAT, params.to) : null)
        render status: HttpStatus.OK, contentType: 'application/json', text: getDataForErrorsCount(from, params.dataType, to) as JSON
    }

    private getDataForErrorsCount(Date from = null, String dataType = null, Date to = null) {
        return qualityService.getCaseCountByError(Tenants.currentId() as Long,from,dataType, to)
    }

    private getSubmissionDataForErrorsCount() {
        return qualityService.getSubmissionCountByError(Tenants.currentId() as Long)
    }

    private getCaseDataForErrorsCount() {
        return qualityService.getCaseDataCountByError(Tenants.currentId() as Long)
    }

    private getBarDataForField(String fieldName, Date from = null, Boolean fromLast = false, Date to = null) {
        int errorTypeNumberLimit = 20
        String errorType = "errorType"
        def result = [fieldNameList: [], errors: []]
        List<Map> dataList = []
        Map externalMap = [:]
        Map dataMap = [:]
        Set<String> fieldNameSet = []
        if (from) {
            if (to) {
                externalMap = ["receiptDateFrom": from.format(DateUtil.DATEPICKER_FORMAT), "receiptDateTo": to.format(DateUtil.DATEPICKER_FORMAT)]
            }
            else
                externalMap = ["receiptDateFrom":from.format(DateUtil.DATEPICKER_FORMAT)]
            dataMap = getErrorsCountByFieldName(fieldName, params.dataType, externalMap, Tenants.currentId() as Long, errorTypeNumberLimit)
            fieldNameSet = dataMap.fieldNamesList
            dataList = dataMap.errorsList

        }else {
            externalMap = [:]
            dataMap = getErrorsCountByFieldName(fieldName, params.dataType, externalMap, Tenants.currentId() as Long, errorTypeNumberLimit)
            fieldNameSet = dataMap.fieldNamesList
            dataList = dataMap.errorsList
        }

        int i = 0
        Map fieldIndexMap = fieldNameSet.collectEntries {
            [(it): i++]
        }
        Set<String> errorTypeSet = []
        Map<String,Long> fieldNameCountMap = new LinkedHashMap<>()

        dataList.each {
            errorTypeSet << it.get(errorType)
            if(fieldNameCountMap.get(it.get(fieldName)) == null){
                fieldNameCountMap.put(it.get(fieldName), ((BigDecimal)it.get("count")).longValue())
            }else{
                fieldNameCountMap.put(it.get(fieldName),((BigDecimal)it.get("count")).longValue() + fieldNameCountMap.get(it.get(fieldName)))
            }
        }
        fieldNameCountMap = fieldNameCountMap.sort { a, b -> b.value <=> a.value }
        if(fieldNameSet.size() > 20){
            fieldNameSet = new LinkedHashSet<>(new ArrayList<>(fieldNameCountMap.keySet()).subList(0, 20))
        }

        //Now create errors Map with default count 0 for each fieldName in fieldNameSet
        def errors = errorTypeSet.collectEntries {
            def values = fieldNameSet.collect { 0 }
            [(it): values]
        }

        dataList.each {
            if(fieldNameSet.contains(it.get(fieldName))) {
                errors[it.get(errorType)][fieldIndexMap[it.get(fieldName)]] = ((BigDecimal) it.get("count")).longValue()
            }
        }
        result = [fieldNameList: fieldNameSet, errors: errors.collect { key, value -> [name: key, data: value] }]
        return result
    }

    private Map getErrorsCountByFieldName(String fieldName, String dataType,
                                  Map externalSearch, Long tenantId, int rowsCount) {
        String type = ""
        if (dataType == PvqTypeEnum.CASE_QUALITY.name()) {
            type = PvqTypeEnum.CASE_QUALITY.name()
        } else if (dataType == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
            type = PvqTypeEnum.SUBMISSION_QUALITY.name()
        }else if (dataType == "ALL") {
            type = null
        }
        Map dataMap = [:]
        if(!dataType || dataType == Constants.ALL){
            Map caseDataMap = qualityService.getErrorsCountByFieldName(fieldName, PvqTypeEnum.CASE_QUALITY.name(), externalSearch, tenantId, rowsCount)
            Set<String> fieldNameSet = caseDataMap.fieldNamesList
            List<Map> dataList = caseDataMap.errorsList
            Map submissionDataMap = qualityService.getErrorsCountByFieldName(fieldName, PvqTypeEnum.SUBMISSION_QUALITY.name(), externalSearch, tenantId, rowsCount)
            fieldNameSet.addAll(submissionDataMap['fieldNamesList'])
            dataList.addAll(submissionDataMap['errorsList'])
            dataMap['fieldNamesList'] = fieldNameSet
            dataMap['errorsList'] = dataList
        }else{
            if(dataType != Constants.NONE) {
                dataMap = qualityService.getErrorsCountByFieldName(fieldName, dataType, externalSearch, tenantId, rowsCount)
            }else{
                dataMap['fieldNamesList'] = []
                dataMap['errorsList'] = []
            }
        }
        return dataMap
    }

    def ajaxLatestQualityIssuesUrl(String dataType) {
        Integer totalCount = 0
        Map dataMap = [:]
        List<Map> data = []
        int max = params.int('length')
        if(!dataType || dataType == Constants.ALL){
            dataMap = qualityService.getLatestQualityIssues(PvqTypeEnum.CASE_QUALITY.name(), Tenants.currentId() as Long, max, params.int('start'))
            data = dataMap.aaData
            totalCount = dataMap.recordsTotal
            if(data.size() < max){
                int remaining = max - data.size()
                int start = params.int('start') - totalCount
                dataMap = qualityService.getLatestQualityIssues(PvqTypeEnum.SUBMISSION_QUALITY.name(), Tenants.currentId() as Long, remaining, start)
                List submissionDataList = dataMap.aaData
                data.addAll(submissionDataList)
                totalCount = totalCount + dataMap.recordsTotal
            }else{
                //For getting only total record
                Map submissionMap = qualityService.getLatestQualityIssues(PvqTypeEnum.SUBMISSION_QUALITY.name(), Tenants.currentId() as Long, max, 0)
                totalCount = totalCount + submissionMap.recordsTotal
            }
        }else{
            if(dataType != Constants.NONE) {
                dataMap = qualityService.getLatestQualityIssues(dataType, Tenants.currentId() as Long, max, params.int('start'))
                data = dataMap.aaData
                totalCount = dataMap.recordsTotal
            }
        }
        render status: HttpStatus.OK, contentType: 'application/json', text: [aaData: data , recordsTotal: totalCount, recordsFiltered: totalCount]as JSON
    }

    def caseDataQuality() {
        if(!qualityService.isPermitted(PvqTypeEnum.CASE_QUALITY.name())) {
            redirect(controller: 'errors',action: 'forbidden')
            return
        }
        List reportFields=qualityService.getQualityReportAllFields(PvqTypeEnum.CASE_QUALITY.name())
        List<String> selFieldsName = grailsApplication.config.qualityModule.qualityColumnList
        List<List> columnUiStackMapping = grailsApplication.config.qualityModule.qualityColumnUiStackMapping
        return caseQuality(reportFields,selFieldsName, columnUiStackMapping, PvqTypeEnum.CASE_QUALITY.name(), params.viewOption)
    }

    def submissionQuality() {
        if(!qualityService.isPermitted(PvqTypeEnum.SUBMISSION_QUALITY.name())) {
            redirect(controller: 'errors',action: 'forbidden')
            return
        }
        List reportFields=qualityService.getQualityReportAllFields(PvqTypeEnum.SUBMISSION_QUALITY.name())
        List<String> selFieldsName = grailsApplication.config.qualityModule.submissionColumnList
        List<List> columnUiStackMapping = grailsApplication.config.qualityModule.submissionColumnUiStackMapping
        return caseQuality(reportFields,selFieldsName, columnUiStackMapping, PvqTypeEnum.SUBMISSION_QUALITY.name(), params.viewOption)
    }

    def sampling() {
    }

    def checkCaseNum(String caseNumber) {
        def dataList = qualityService.checkCaseNumber(caseNumber)
        if(!dataList){
            dataList = [:]
        }
        render(dataList as JSON)
    }

    def renderImage() {}

    @Secured(['ROLE_PVQ_EDIT'])
    def saveAdHocAlert() {
        qualityService.saveAdhocQualityRecord(params, Tenants.currentId() as Long)
        flash.message = message(code: 'qualityAlert.observationAdded')
        if(params.dataType == PvqTypeEnum.CASE_QUALITY.name())
            redirect(action: 'caseDataQuality')
        else
            redirect(action: 'submissionQuality')
    }
    @Secured(['ROLE_PVQ_EDIT'])
    def addToManual() {
        AjaxResponseDTO resp = new AjaxResponseDTO()
        try {
            qualityService.saveAdhocQualityRecord(params, Tenants.currentId() as Long)
        } catch (Exception e) {
            resp.setFailureResponse(e, message(code: 'default.server.error.message') as String)
        }
        render(resp.toAjaxResponse())
    }

    def sendEmail() {
        Locale locale = userService.getCurrentUser()?.preference?.locale
        String emailBodyType = qualityService.getLabelForType(params.dataType)
        def d = exportToExcel(params,params.dataType, Tenants.currentId() as Long)
        byte[] data = qualityService.exportToExcel(d.data, d.metadata)
        List files=[]
        files.add([type: grailsApplication.config.grails.mime.types.excel,
                   name: d.metadata.sheetName + ".xlsx",
                   data: data
        ])
        //render(file: data, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: d.metadata.sheetName + ".xlsx")

        List<String> emailList = []
        if(params.emailToUsers){
            if (params.emailToUsers instanceof String) {
                emailList.add(params.emailToUsers)
            } else {
                emailList = params.emailToUsers
            }
            String[] emailIds = emailList.toArray();
            String body = (params.body ?: g.message(code: "app.label.hi") + "<br><br>" + g.message(code: "app.label.quality.email.body", args: [emailBodyType])) + "<br><br>"
            if (params?.body) {
                body = body
            } else {
                if (locale?.language == 'ja') {
                    body += ViewHelper.getMessage("app.label.pv.reports")
                } else {
                    body += ViewHelper.getMessage("app.label.thanks") + "," + "<br>" + ViewHelper.getMessage("app.label.pv.reports")
                }
            }
            String subject = params.subject ?: g.message(code: "app.label.case.data.quality")
            //body += params.casesData
            //emailService.sendEmail(emailList, body, true, subject)
            emailService.sendEmailWithFiles(emailIds, null, subject, body, true, files)
            flash.message = message(code: 'qualityModule.email.success.message')
        }
        if(params.dataType == PvqTypeEnum.CASE_QUALITY.name())
        redirect(action:  "caseDataQuality")
        else if(params.dataType == PvqTypeEnum.SUBMISSION_QUALITY.name())
        redirect(action:  "submissionQuality")
        else
            redirect(action:  "caseSampling", params:[dataType: params.dataType])
    }

    def caseSampling() {
        if(!qualityService.isPermitted(params.dataType)) {
            redirect(controller: 'errors',action: 'forbidden')
            return
        }
        List reportFields=qualityService.getQualityReportAllFields(params.dataType)
        List<String> selFieldsName = qualityService.getColumnList(params.dataType)
        List<List> columnUiStackMapping =  Holders.config.qualityModule.additional.find { it.name == params.dataType }?.columnUiStackMapping?:[]
        Map model = caseQuality(reportFields,selFieldsName, columnUiStackMapping, params.dataType, params.viewOption)
        return model;
    }

    private Map caseQuality(List reportFields,List selFieldsName,List  columnUiStackMapping, pvqType, String viewType) {
        List selFields = []
        WorkflowConfigurationTypeEnum workFlowType = null;
        if(pvqType.equals(PvqTypeEnum.CASE_QUALITY.name())){
            workFlowType = WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA
        }else if(pvqType.equals(PvqTypeEnum.SUBMISSION_QUALITY.name())){
            workFlowType = WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION
        }else {
            workFlowType = WorkflowConfigurationTypeEnum.getAdditional(qualityService.getAdditional(pvqType).workflow)
        }
        selFieldsName.each { String fname ->
            def mappedReportField = reportFields.find { it.fieldName == fname }
            if (mappedReportField) reportFields.remove(mappedReportField)
            boolean isSelectable = ReportField.findByName(fname)?.lmSQL ? true : false
            selFields << [fieldName : fname,
                          selectable: mappedReportField?.selectable,
                          fieldType : mappedReportField?.fieldType ?: "VARCHAR2",
                          fieldLabel: (mappedReportField?.fieldLabel ?: ViewHelper.getMessage('app.reportField.' + fname)),
                          selectable: mappedReportField?.isSelectable ?: isSelectable]
        }
        reportFields= reportFields.sort {a,b -> a.fieldLabel <=> b.fieldLabel};
        Map model = [moduleColumnList : selFields,columnUiStackMapping:columnUiStackMapping,
                     reportOtherColumnList : reportFields, workFlowStates : WorkflowState.getAllWorkFlowStatesForType(workFlowType), viewType : viewType]
        setOtherDetails(model)
        return model
    }

    private Map setOtherDetails(Map model){
        model.qualityIssues = (getQualityIssues() as JSON).toString()
        model.rootCauses = (getRootCauses() as JSON).toString()
        model.responsibleParties = (getResponsibleParties() as JSON).toString()
        model.correctiveActions = (getCorrectiveActionList() as JSON).toString()
        model.preventativeActions = (getPreventativeActionList() as JSON).toString()
        return model
    }

    private String convertDatetimeString(String isoDateTimeString) {
        if (isoDateTimeString) {
            DateTimeZone timeZone = DateTimeZone.forID("America/Los_Angeles")
            DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ssZ").withZone(timeZone)
            def dt = formatter.parseLocalDate(isoDateTimeString)
            dt.toString()
        } else
            ""
    }

    def exportToExcelCaseDatQuality() {
        if (params.async) {
            getExcelExportFileAsync(params, PvqTypeEnum.CASE_QUALITY.name(), userService.currentUser.id)
            render "ok"
            return
        }
        def d = exportToExcel(params, PvqTypeEnum.CASE_QUALITY.name(), Tenants.currentId() as Long)
        byte[] data = qualityService.exportToExcel(d.data, d.metadata)
        String fileName = d.metadata.sheetName + ".xlsx"

        AuditLogConfigUtil.logChanges(new QualityCaseData(), [outputFormat: ReportFormatEnum.XLSX.name(), fileName: fileName, exportedDate: new Date()],
                [:], Constants.AUDIT_LOG_EXPORT, ViewHelper.getMessage("auditLog.entityValue.bulk.export", "Case Data Quality", ReportFormatEnum.XLSX.displayName))
        render(file: data, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: fileName)
    }

    def exportToExcelQualitySubmission() {
        if (params.async) {
            getExcelExportFileAsync(params, PvqTypeEnum.SUBMISSION_QUALITY.name(), userService.currentUser.id)
            render "ok"
            return
        }
        def d = exportToExcel(params, PvqTypeEnum.SUBMISSION_QUALITY.name(), Tenants.currentId() as Long)
        byte[] data = qualityService.exportToExcel(d.data, d.metadata)
        String fileName = d.metadata.sheetName + ".xlsx"
        AuditLogConfigUtil.logChanges(new QualitySubmission(), [outputFormat: ReportFormatEnum.XLSX.name(), fileName: fileName, exportedDate: new Date()],
                [:], Constants.AUDIT_LOG_EXPORT, ViewHelper.getMessage("auditLog.entityValue.bulk.export", "Submission Quality", ReportFormatEnum.XLSX.displayName))
        render(file: data, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: fileName)
    }

    def exportToExcelQualitySampling() {
        if (params.async) {
            getExcelExportFileAsync(params, params.dataType, userService.currentUser.id)
            render "ok"
            return
        }
        def d = exportToExcel(params, params.dataType, Tenants.currentId() as Long)
        byte[] data = qualityService.exportToExcel(d.data, d.metadata)
        String fileName = d.metadata.sheetName + ".xlsx"
        AuditLogConfigUtil.logChanges(new QualitySampling(), [outputFormat: ReportFormatEnum.XLSX.name(), fileName: fileName, exportedDate: new Date()],
                [:], Constants.AUDIT_LOG_EXPORT, ViewHelper.getMessage("auditLog.entityValue.bulk.export", "Case Sampling", ReportFormatEnum.XLSX.displayName))
        render(file: data, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: fileName)
    }

    void getExcelExportFileAsync(Map params, String type, Long userId) {
        Map m = [:]
        m.putAll(params)
        Promises.task {
            QualitySampling.withNewSession {
                def d = exportToExcel(m, type, Tenants.currentId() as Long)
                byte[] data = qualityService.exportToExcel(d.data, d.metadata)
                String fileName = d.metadata.sheetName + ".xlsx"
                File reportFile = new File(grailsApplication.config.tempDirectory + "/" + System.currentTimeMillis() + ".xlsx")
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

    }

    def exportToExcelCaseForm() {
        def paramData = new JsonSlurper().parseText(params.data.toString())
        boolean selectAll = Boolean.parseBoolean(paramData.selectAll)
        String caseNumber = params.caseNumber
        List list = []
        String dataType = params.type
        def data = []
        if (selectAll){
            Map<String,Map<Long,String>> allRcaDataMap = qualityService.getAllRcaDataMap()
            if (paramData.table == 'issuesTable') {
                if(dataType.equals(PvqTypeEnum.CASE_QUALITY.name()))
                    list.addAll(formListForType(QualityCaseData.findAllByIsDeletedAndCaseNumberAndTenantId(false, caseNumber, Tenants.currentId() as Long), PvqTypeEnum.CASE_QUALITY.name(), allRcaDataMap, params.versionNumber))
                else if(dataType.equals(PvqTypeEnum.SUBMISSION_QUALITY.name()))
                    list.addAll(formListForType(QualitySubmission.findAllByIsDeletedAndCaseNumberAndTenantId(false, caseNumber, Tenants.currentId() as Long), PvqTypeEnum.SUBMISSION_QUALITY.name(), allRcaDataMap, params.versionNumber))
                else
                    list.addAll(formListForType(QualitySampling.findAllByIsDeletedAndCaseNumberAndTenantIdAndType(false, caseNumber, Tenants.currentId() as Long, dataType), null, allRcaDataMap, params.versionNumber))
            }
            else if (paramData.table == 'allIssuesTable') {
                list.addAll(formListForType(QualityCaseData.findAllByIsDeletedAndCaseNumberAndTenantId(false, caseNumber, Tenants.currentId() as Long), PvqTypeEnum.CASE_QUALITY.name(), allRcaDataMap, params.versionNumber))
                list.addAll(formListForType(QualitySubmission.findAllByIsDeletedAndCaseNumberAndTenantId(false, caseNumber, Tenants.currentId() as Long), PvqTypeEnum.SUBMISSION_QUALITY.name(), allRcaDataMap, params.versionNumber))
                list.addAll(formListForType(QualitySampling.findAllByIsDeletedAndCaseNumberAndTenantId(false, caseNumber, Tenants.currentId() as Long), null, allRcaDataMap, params.versionNumber))
            }
            if (paramData.advanceFilter) {
                def advanceFilter = null;
                if (paramData.tableFilter) {
                    advanceFilter = new JsonSlurper().parseText(paramData.tableFilter)
                }
                if (advanceFilter != null) {
                    list = applyCaseFormFilter(list, advanceFilter)
                }
            }
        }
        else {
            Map<String,Map<Long,String>> allRcaDataMap = qualityService.getAllRcaDataMap()
            List idTypes= paramData.selectedIds
            idTypes.each {
                String  idString = null
                if (paramData.table == 'issuesTable') {
                    idString = it.toString()
                }
                else if (paramData.table == 'allIssuesTable'){
                    idString = it.toString().split(":")[0]
                    dataType = it.toString().split(":")[1]
                }
                Long id = Long.parseLong(idString)
                List qualityObjectsList = []
                if(dataType.equals(PvqTypeEnum.CASE_QUALITY.name())){
                    qualityObjectsList = formListForType(QualityCaseData.findAllByIsDeletedAndCaseNumberAndTenantId(false, caseNumber, Tenants.currentId() as Long), PvqTypeEnum.CASE_QUALITY.name(), allRcaDataMap, params.versionNumber)
                    list.add(qualityObjectsList.find{ it.id == id})
                }else if(dataType.equals(PvqTypeEnum.SUBMISSION_QUALITY.name())){
                    qualityObjectsList = formListForType(QualitySubmission.findAllByIsDeletedAndCaseNumberAndTenantId(false, caseNumber, Tenants.currentId() as Long), PvqTypeEnum.SUBMISSION_QUALITY.name(), allRcaDataMap, params.versionNumber)
                    list.add(qualityObjectsList.find{ it.id == id})
                }else {
                    qualityObjectsList = formListForType(QualitySampling.findAllByIsDeletedAndCaseNumberAndTenantId(false, caseNumber, Tenants.currentId() as Long), null, allRcaDataMap, params.versionNumber)
                    list.add(qualityObjectsList.find{ it.id == id})
                }
            }

        }
        list.each {
            data.add([it.qualityMonitoringType,
                      it.errorType ?: "",
                      it.fieldName ?: "",
                      it.value ?: "",
                      it.fieldLocation ?: "",
                      it.entryType ?: "",
                      it.dueIn ?: "",
                      (it.priority != "-1") ? it.priority : "",
                      it.qualityIssueType ?: "",
                      it.rootCause ?: "",
                      it.responsibleParty ?: "",
                      it.comment ?: ""
            ])
        }

        def metadata = [sheetName: caseNumber,
                        columns  : [
                                [title: ViewHelper.getMessage("app.label.quality.monitoringType"), width: 25],
                                [title: ViewHelper.getMessage("app.label.quality.issueDescription"), width: 25],
                                [title: ViewHelper.getMessage("app.label.quality.fieldName"), width: 25],
                                [title: ViewHelper.getMessage("app.label.quality.value"), width: 25],
                                [title: ViewHelper.getMessage("app.label.quality.fieldLocation"), width: 25],
                                [title: ViewHelper.getMessage("app.label.quality.source"), width: 25],
                                [title: ViewHelper.getMessage("app.label.icsr.profile.conf.dueInDays"), width: 25],
                                [title: ViewHelper.getMessage("app.label.action.item.priority"), width: 25],
                                [title: ViewHelper.getMessage("quality.capa.issueType.label"), width: 25],
                                [title: ViewHelper.getMessage("quality.capa.rootCause.label"), width: 25],
                                [title: ViewHelper.getMessage("quality.capa.responsibleParty.label"), width: 25],
                                [title: ViewHelper.getMessage('quality.latestComment.label'), width: 25]
                        ]]
        byte[] file = qualityService.exportToExcel(data, metadata)
        String fileName = caseNumber + ".xlsx"
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_DATE_TIME_FORMAT)
        AuditLogConfigUtil.logChanges(list, [outputFormat: ReportFormatEnum.XLSX.name(), fileName: fileName, exportedDate: new Date()],
                [:], Constants.AUDIT_LOG_EXPORT, ViewHelper.getMessage("auditLog.entityValue.bulk.export", dataType + "Case Form", ReportFormatEnum.XLSX.displayName))
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: fileName)
    }

    private List applyCaseFormFilter(List qualityIssuesList, def advanceFilter){
        advanceFilter.each { key, val ->
            String searchValue = val["value"]?.toString()?.trim()
            if (searchValue && searchValue != "") {
                if (key == "errorType") {
                    qualityIssuesList =qualityIssuesList.findAll { it.errorType == searchValue}
                }
                else if (key == "fieldName") {
                    qualityIssuesList =qualityIssuesList.findAll { it.fieldName == searchValue}
                }
                else if (key == "value") {
                    qualityIssuesList =qualityIssuesList.findAll { it.value == searchValue}
                }
                else if (key == "fieldLocation") {
                    qualityIssuesList =qualityIssuesList.findAll { it.fieldLocation == searchValue}
                }
                else if (key == "entryType") {
                    qualityIssuesList =qualityIssuesList.findAll { it.entryType == searchValue}
                }
                else if (key == "priority") {
                    qualityIssuesList = qualityIssuesList.findAll { it.priority == searchValue }
                }
                else if (key == "qualityIssueType") {
                    qualityIssuesList =qualityIssuesList.findAll { it.qualityIssueTypeId == Long.valueOf(searchValue.toString())}
                }
                else if (key == "rootCause") {
                    qualityIssuesList =qualityIssuesList.findAll { it.rootCause == searchValue}
                }
                else if (key == "responsibleParty") {
                    qualityIssuesList = qualityIssuesList.findAll { it.responsibleParty == searchValue }
                }
            }
        }
        return qualityIssuesList
    }

    protected List getQualityDataByFilter(Map paramData, String type, Boolean idsOnly) {
        Map externalSearch = [:]
        List search = paramData.search ? new JsonSlurper().parseText(paramData.search) : null
        if (search) {
            for (int i = 0; i < search.size(); i++) {
                externalSearch[search[i].name] = search[i].value
            }
        }
        Map advanceFilter = null;
        if (paramData.advanceFilter) {
            advanceFilter = FilterUtil.convertToJsonFilter(new JsonSlurper().parseText(paramData.advanceFilter))
        }
        qualityService.getQualityDataEntityListSearch(type, externalSearch, advanceFilter, Tenants.currentId() as Long, idsOnly)
    }

    private Map exportToExcel(params, String type, Long tenantId) {
        def paramData = new JsonSlurper().parseText(params.data.toString())
        boolean selectAll = !paramData.selectedIds
        List list = []
        List data = []
        if (selectAll) {
            list = getQualityDataByFilter(paramData, type, false)
        } else {
            List ids = paramData.selectedIds
            if (ids && ids.size() > 0) {
                list = qualityService.getQualityDataByIds(ids, type, tenantId)
            }
        }
        Map cols = getHeaderColumnList(type)
        List skippedFields=["assignedToUserGroupId","assignedToUserId","qualityIssueTypeId", "issueTypeDetail", "hasIssues", "executedTemplateId"]
        Map renamesMap=[:]
        for(int i=0;i<list.size();i++) {
            Map row = cols.collectEntries { [(it.key): ''] }
            Map record  = list.get(i)
            def executedTemplateId = record.executedTemplateId as Long
            skippedFields.each {
                if (record.containsKey(it)){
                    record.remove(it)
                }
            }
            List renames = renamesMap[executedTemplateId as Long]
            if (renames == null) {
                renames = qualityService.getRenameValueOfReportField(null, executedTemplateId as Long)
                renamesMap.put(executedTemplateId as Long, renames)
            }
            record.eachWithIndex { entry, ind ->
                String key = entry.getKey(), value = entry.getValue()
                String origKey = (key.contains("__")) ? key.replaceAll("__\\d+", "") : key
                if (cols.get(key) == null) {
                    cols[key] = renames.getAt(ind) ?: ViewHelper.getMessage('app.reportField.' + origKey)
                    row << [(key): value]
                }  else row << [(origKey): value]
            }
            List rowdata=[]
            row.each { key, value ->
                rowdata.add(value)
            }
            data.add(rowdata)
        }
        List columns = []
        cols.each { key, value ->
            columns << ["title": value, width: "20"]
        }
        String sheetName = qualityService.getLabelForType(type)
        Map metadata = [
            "sheetName": sheetName,
            "columns": columns
        ];
        return [metadata : metadata, data : data]

    }

    private Map getHeaderColumnList(String type) {
        List<String> selFieldsName = qualityService.getColumnList(type)
        Map cols = [:]
        for(int i=0; i<selFieldsName.size(); i++) {
            cols[selFieldsName[i]] = ViewHelper.getMessage('app.reportField.' + selFieldsName[i])
        }
        cols["errorType"] = ViewHelper.getMessage('app.label.errorType')
        cols["priority"] = ViewHelper.getMessage('app.label.action.item.priority')
        cols["assignedToUserGroup"] = ViewHelper.getMessage('app.label.assignedToGroup')
        cols["assignedToUser"] = ViewHelper.getMessage('app.label.assignedToUser')
        cols["qualityIssueType"] = ViewHelper.getMessage('quality.capa.issueType.label')
        cols["rootCause"] = ViewHelper.getMessage('quality.capa.rootCause.label')
        cols["responsibleParty"] = ViewHelper.getMessage('quality.capa.responsibleParty.label')
        cols["qualityMonitoringType"] = ViewHelper.getMessage('app.label.quality.monitoringType')
        cols["dueIn"] = ViewHelper.getMessage('app.pcv.dueIn')
        cols["state"] = ViewHelper.getMessage('app.label.state')
        cols["latestComment"] = ViewHelper.getMessage('quality.latestComment.label')
        cols["dateCreated"] = ViewHelper.getMessage('app.label.dateCreated')
        return cols
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def updatePriority() {
        try{
            qualityService.updatePriority(params, Tenants.currentId() as Long)
            render "Ok"
        } catch (Exception e) {
            log.error("Error occurred in updating priority", e)
            render "Error occurred in updating priority"
        }
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def updateQualityIssueType() {
        try {
            qualityService.updateQualityIssueType(params, Tenants.currentId() as Long)
        } catch (Exception e) {
            log.error("Error occurred in updating QualityIssueType", e)
            render "Error occurred in updating QualityIssueType"
        }
        render "Ok"
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def updateRootCause() {
        render qualityService.updateRootCauses(params, Tenants.currentId() as Long)
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def updateResponsibleParty(){
        render qualityService.updateResponsibleParty(params, Tenants.currentId() as Long)
    }

    def actionItems() {
        render model: [qualityModule: true], view: 'actionItems'
    }

    def getQualityPriorityList() {
        render qualityService.getQualityPriorityList() as JSON
    }

    def qualityDataAjax() {
        qualityAjax(params,PvqTypeEnum.CASE_QUALITY.name())
    }
    def qualitySubmissionAjax() {
        qualityAjax(params,PvqTypeEnum.SUBMISSION_QUALITY.name())
    }
    def qualitySamplingAjax() {
        qualityAjax(params, params.dataType)
    }

    def getSelectAll() {
        params.length = BULK_UPDATE_MAX_ROWS + 1
        Map resultQry = qualityAjaxData(params, params.dataType)
        List out = resultQry.aaData.collect { [id: it.id, caseNumber: it.caseNumber, caseVersion: it.masterVersionNum, errorType: it.errorType, submissionIdentifier: it.dataType == PvqTypeEnum.SUBMISSION_QUALITY.name()?it.submissionIdentifier:"-"] }
        render(contentType: "application/json", out as JSON)
    }

    private qualityAjax(params, String type) {
        Map resultQry = qualityAjaxData(params, type)
        render(resultQry as JSON)
    }

    private Map qualityAjaxData(params, String type) {
        sanitize(params)
        //List dbcolumns = []
        Map externalSearch=[:]
        params.eachWithIndex { entry, index ->
            String keySearchName="search[${index}][name]"
            String keySearchValue="search[${index}][value]"

            if(params[keySearchName] && params[keySearchValue]) {
                externalSearch[params[keySearchName]]=params[keySearchValue]
            }
        }
        /*
          * Set it in externalSearch to be used later to decide whether to consider
          * error types while filtering or not even if errorType is present in externalSearch
         */
        externalSearch["filterByErrorType"] = params.boolean('filterByErrorType')
        def advanceFilter = null;
        if(params.advanceFilter) {
            advanceFilter = FilterUtil.convertToJsonFilter(params.advanceFilter)
        }

        String sort = params.sort ?  params.sort : 'CaseNumber'
        boolean refreshChart = Boolean.parseBoolean(params.refreshChart)
        Long tenantId = Tenants.currentId() as Long
        String assignedToFilter = (params.assignedToFilter?.length() == 0) ? null : params.assignedToFilter
        return qualityService.getQualityDataList(params.offset, params.max, sort, params.order, type, refreshChart, externalSearch, advanceFilter, tenantId, params.viewType,assignedToFilter,params.linkFilter)
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def updateAssignedOwner() {
        try {
            qualityService.updateAssignedOwner(params, Tenants.currentId() as Long)
            render(status: 200, text: "Ok")
        } catch (Exception e) {
            log.error("Error occurred during updating Assigned To", e)
            render(status: 500, text: "Error occurred during updating Assigned To - ${e.message}")
        }
    }

    def fetchUsers(){
        List userMap = userService.getActiveUsers().collect{
            [userId: it.id, fullName: it.fullName]
        }
        render userMap as JSON
    }

    def fetchUsersAndGroups(){
        List userMap = userService.getActiveGroups().collect{
            [userId: Constants.USER_GROUP_TOKEN + it.id, fullName: it.name]
        }
        userService.getActiveUsers().collect{
            userMap << [userId: Constants.USER_TOKEN + it.id, fullName: it.fullName]
        }
        render userMap as JSON
    }

    def getQualityIssues(){
        return qualityService.getQualityIssues()
    }

    def getQualityIssuesMap(){
        List issueMap = qualityService.getQualityIssues().collect{
            [id: it.id, textDesc: it.textDesc]
        }
        render issueMap as JSON;
    }

    def getRootCauses(){
        return qualityService.getRootCauses()
    }

    def getRootCausesMap(){
        List rootCauseMap = qualityService.getRootCauses().collect{
            [id: it.id, textDesc: it.textDesc]
        }
        render rootCauseMap as JSON;
    }

    def getResponsibleParties(){
        return qualityService.getResponsibleParties()
    }

    def getResponsiblePartiesMap(){
        List responsiblePartiesMap = qualityService.getResponsibleParties().collect{
            [id: it.id, textDesc: it.textDesc]
        }
        render responsiblePartiesMap as JSON;
    }

    def listofWorkflowQuality(List<WorkflowConfigurationTypeEnum> workflowConfigurationType){
        def RuleForPVQuality  = WorkflowRule.findAllByConfigurationTypeEnumInListAndIsDeleted(workflowConfigurationType , false)
        def listOfQualityStates = RuleForPVQuality.initialState + RuleForPVQuality.targetState
        return listOfQualityStates?.findAll{!it.isDeleted}?.unique { a, b -> a.name <=> b.name}
    }

    def getFilterData() {
        render collectFilterPanelData() as JSON
    }

    private collectFilterPanelData() {
        Map filterPanelData = [:]
        filterPanelData.put("responsiblePartyList", qualityService.getResponsibleParties().collect{
            [id: it.id, textDesc: it.textDesc]
        })
        filterPanelData.put("rootCauseList", qualityService.getRootCauses().collect{
            [id: it.id, textDesc: it.textDesc]
        })
        filterPanelData.put("issueList", qualityService.getQualityIssues().collect{
            [id: it.id, textDesc: it.textDesc]
        })
        filterPanelData.put("userGroupList", userService.getActiveGroups().collect{
            [id: Constants.USER_GROUP_TOKEN + it.id, fullName: it.name]
        })
        filterPanelData.put("userList", userService.getActiveUsers().collect{
            [id: Constants.USER_TOKEN + it.id, fullName: it.fullName]
        })
        filterPanelData.put("workflowStateList",(listofWorkflowQuality(WorkflowConfigurationTypeEnum.getAllQuality()))?.collect {
            [id: it.id, name: it.name]
        })
        filterPanelData.put("qualityPriorityList", qualityService.getQualityPriorityList())
        filterPanelData.put("errorTypeList", qualityService.getQualityErrorTypes(params.dataType, Tenants.currentId() as Long))
        return filterPanelData;
    }

    def getCorrectiveActionList(){
        return reportExecutorService.getCorrectiveActionList(ReasonOfDelayAppEnum.PVQ)
    }

    def getPreventativeActionList(){
        return reportExecutorService.getPreventativeActionList(ReasonOfDelayAppEnum.PVQ)
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def fetchQualityModuleErrorTypes(){
        render qualityService.fetchQualityModuleErrorTypes(params.dataType, Tenants.currentId() as Long) as JSON
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def fetchQualityModuleErrorsList(){
        render qualityService.fetchQualityModuleErrorsList(params.dataType, Tenants.currentId() as Long) as JSON
    }

    @Secured(['ROLE_PVQ_EDIT'])
    def updateErrorType(){
        try {
            qualityService.updateErrorType(params, Tenants.currentId() as Long)
        } catch (Exception e) {
            log.error("Error occurred in updating updateErrorType", e)
            render "Error occurred in updating updateErrorType"
        }
        render "Ok"
    }

    def fetchCriteriaForManualError(Long id,String caseNumber) {
        def dataList = qualityService.fetchPriorityAndComments(params.dataType,id)
        render(dataList as JSON)
    }

    def downloadSourceDocuments(String id, String caseNumber, String caseVersion) {
        Map attachment = qualityService.fetchAttachmentContent(id, caseNumber, caseVersion)
        render(file: attachment.data, fileName: attachment.fileName, contentType: getContentType(attachment.fileName))
    }

    private String getContentType(String fileName) {
        if(!fileName) return "application/octet-stream"
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        String contentType = mimeTypesMap.getContentType(fileName.toLowerCase());
        if (contentType == "application/xml") contentType = "text/plain"
        return contentType?:"application/octet-stream"
    }

    def viewSourceDocument() {
        Map attachment = qualityService.fetchAttachmentContent(params.id, params.caseNumber, params.versionNumber, params.isRedacted?.toBoolean())
        String contentType = params.isRedacted?.toBoolean() ? "application/pdf": getContentType(attachment?.fileName)
        if (contentType == "text/xml") contentType = "text/plain"
        response.setContentType(contentType?:"application/octet-stream")
        response.addHeader("Content-Disposition", "inline")
        OutputStream responseOutputStream = response.getOutputStream()
        try {
            responseOutputStream.write(attachment?.data)
            responseOutputStream.flush()
            response.flushBuffer()
        } finally {
            responseOutputStream.close()
        }
        return
    }

    def fetchSourceDocuments(String caseNumber) {
        def dataList = qualityService.fetchAttachments(caseNumber)
        render(dataList as JSON)
    }

    def caseForm() {
        String caseNumber = params.caseNumber
        Integer versionNumber = params.int('versionNumber')
        Long id = params.long('id')
        String type= params.type
        WorkflowState workflowState
        def qualityModuleObj
        String assignedToId=null
        if (!sqlGenerationService.isCaseNumberExistsForTenant(caseNumber, versionNumber)) {
            response.sendError(403, "You are not authorized to view or data doesn't exist")
            return
        }
        if (type.equals(PvqTypeEnum.CASE_QUALITY.name()))
            qualityModuleObj = QualityCaseData.findById(id)
        else if (type.equals(PvqTypeEnum.SUBMISSION_QUALITY.name()))
            qualityModuleObj = QualitySubmission.findById(id)
        else
            qualityModuleObj = QualitySampling.findById(id)
        workflowState = qualityModuleObj.workflowState
        String state = workflowState.name
        String    assignedToUser = qualityModuleObj.assignedToUser?Constants.USER_TOKEN + qualityModuleObj.assignedToUser.id:""
        String    assignedToUserGroup = qualityModuleObj.assignedToUserGroup?Constants.USER_GROUP_TOKEN + qualityModuleObj.assignedToUserGroup.id:""
        Map caseInfo = sqlGenerationService.getCaseMetadataDetails(caseNumber, versionNumber)
        def qualityIssues = getQualityIssues()
        String agency = JSON.parse(qualityModuleObj.metadata)["reportsAgencyId"]
        setOtherDetails([caseInfo: caseInfo, type: type, caseNumber: caseNumber, id: id, versionNumber: versionNumber, agency: agency, submissionIdentifier: params.submissionIdentifier, state: state, isFinalState: workflowState.finalState,
                         attachmentsList: [], assignedToUser: assignedToUser, assignedToUserGroup: assignedToUserGroup, qualityIssueTypes: qualityIssues])
    }

    def getAttachmentsByCaseNo(){
        String caseNumber = params.caseNumber
        def attachmentsList = qualityService.fetchAttachments(caseNumber)
        render ((attachmentsList ?: []) as JSON)
    }

    def issueList() {
        def list = []
        String type = params.type;
        Map<String,Map<Long,String>> allRcaDataMap = qualityService.getAllRcaDataMap()
        if(type.equals(PvqTypeEnum.CASE_QUALITY.name())){
            list = formListForType(QualityCaseData.findAllByIsDeletedAndCaseNumberAndTenantId(false, params.caseNumber, Tenants.currentId() as Long), PvqTypeEnum.CASE_QUALITY.name(), allRcaDataMap, params.versionNumber)
        } else if (type.equals(PvqTypeEnum.SUBMISSION_QUALITY.name())) {
            list = formListForType(QualitySubmission.findAllByIsDeletedAndCaseNumberAndTenantIdAndSubmissionIdentifier(false, params.caseNumber, Tenants.currentId() as Long, params.submissionIdentifier), PvqTypeEnum.SUBMISSION_QUALITY.name(), allRcaDataMap, params.versionNumber)
        } else {
            list = formListForType(QualitySampling.findAllByIsDeletedAndCaseNumberAndTenantIdAndType(false, params.caseNumber, Tenants.currentId() as Long, params.type), null, allRcaDataMap, params.versionNumber)
        }
        if (params.sort && params.direction) {
            sortIssueList(list, false)
        }
        if (!params.advancedFilter || params.advancedFilter == "false") {
            if (params.errorList)
                render list?.errorType as JSON
            if (params.fieldName)
                render list?.fieldName as JSON
            if (params.fieldLocation)
                render list?.fieldLocation as JSON
            else
                render list as JSON
        } else if (params.advancedFilter == "true") {
            def advanceFilter = null;
            if (params.tableFilter) {
                advanceFilter = FilterUtil.convertToJsonFilter(params.tableFilter)
            }
            if (advanceFilter != null) {
                list = applyCaseFormFilter(list, advanceFilter)
                render list as JSON
            }
        }
    }

    private List formListForType(List list, String type = null, Map<String,Map<Long,String>> allRcaDataMap, String versionNum) {
        List dataList = []
        List<String> minColumnNameList = []
        if(type.equals(PvqTypeEnum.CASE_QUALITY.name())){
            grailsApplication.config.qualityModule.qualityColumnList.each { minColumnNameList.add(it) }
        }else if(type.equals(PvqTypeEnum.SUBMISSION_QUALITY.name())){
            grailsApplication.config.qualityModule.qualityColumnList.each { minColumnNameList.add(it) }
        }else {
            grailsApplication.config.qualityModule.qualityColumnList.each { minColumnNameList.add(it) }
        }
        Map fieldTypeMap = qualityService.getQualityReportAllFields(type).collectEntries { [(it.fieldName), it.fieldType] }
        String dateFormat = DateUtil.getLongDateFormatForLocale(userService.currentUser?.preference?.locale, true)
        list.each { q ->
            Map row = qualityService.formQualityRecordRow(q, minColumnNameList, type ?: q.type, allRcaDataMap, fieldTypeMap, dateFormat)
            row.putAll(
                    [mandatoryType   : q.mandatoryType ? message(code: q.mandatoryType.i18nKey) : "",
                     typeCode        : q.mandatoryType?.name() ?: "",
                     fieldName       : q.fieldName ?: "",
                     value           : q.value ?: "",
                     fieldLocation   : q.fieldLocation ?: "",
                     executedReportId: q.executedReportId,
                     caseVersion: versionNum
                    ])
            if (row['state'] && String.valueOf(row['masterVersionNum']) == versionNum) {
                dataList.add(row)
            }
        }
        dataList.sort{it.id}
        dataList = dataList.reverse()
        return dataList
    }

    def getAllQualityIssues() {
        Map<String,Map<Long,String>> allRcaDataMap = qualityService.getAllRcaDataMap()
        def list = formListForType(QualityCaseData.findAllByIsDeletedAndCaseNumberAndTenantId(false, params.caseNumber, Tenants.currentId() as Long), PvqTypeEnum.CASE_QUALITY.name(), allRcaDataMap, params.versionNumber)
        list.addAll(formListForType(QualitySubmission.findAllByIsDeletedAndCaseNumberAndTenantId(false, params.caseNumber, Tenants.currentId() as Long), PvqTypeEnum.SUBMISSION_QUALITY.name(), allRcaDataMap, params.versionNumber))
        QualitySampling.findAllByIsDeletedAndCaseNumberAndTenantId(false, params.caseNumber, Tenants.currentId() as Long).groupBy {it.type}.each {list.addAll(formListForType(it.value, null, allRcaDataMap, params.versionNumber)) }
        if (params.sort && params.direction) {
            sortIssueList(list)
        }
        render list as JSON
    }

    def updateFieldError() {
        params.masterCaseNum = params.caseNumber
        params.putAll(qualityService.checkCaseNumber(params.caseNumber))
        qualityService.saveAdhocQualityRecord(params, Tenants.currentId() as Long, params.long("selectedId"))
        render "ok"
    }

    def saveAllRcasForCase() {
        List rcList = []
        def qualityObj = qualityService.initializeQualityObjById(params.dataType, Long.parseLong(params.currentRcaId))
        Set ids = [params.currentRcaId]
        Long selectedIssueType
        String errorMessage = null
        List errorRows = null
        List cases = [[caseNumber: qualityObj.caseNumber, caseVersion: "" + qualityObj.versionNumber, id: params.currentRcaId, submissionIdentifier: qualityObj.hasProperty("submissionIdentifier") ? qualityObj.submissionIdentifier : "-"]]
        if (params.selectedCases) {
            def json = JSON.parse(params.selectedCases)
            if (json && json.size() > 0) cases.addAll(json)
            cases = cases.unique { a, b -> ((a.caseNumber == b.caseNumber) && (a.caseVersion == b.caseVersion) && (a.submissionIdentifier == b.submissionIdentifier)) ? 0 : -1 }
        }
        try {
            if (params.baseLate) {
                selectedIssueType = Long.parseLong(params.baseLate)
            }
            if (params?.selectedIds) {
                if (params?.selectedIds instanceof String) {
                    ids.addAll(params?.selectedIds?.split(","))
                } else if (params?.selectedIds instanceof String[]) {
                    params?.selectedIds.each {
                        ids.add(it)
                    }
                }
            }

            if (!params?.late) {
                rcList << [late            : params.baseLate, rootCause: null, responsibleParty: null,
                           correctiveAction: null, preventativeAction: null, flagPrimary: "true",
                           pvcLcpId        : null, actions: "", summary: "", investigation: "",
                           correctiveDate  : null, preventiveDate: null]
            } else if (params?.late instanceof String) {
                rcList << [late            : params.baseLate, rootCause: params.rootCause, responsibleParty: params.responsibleParty,
                           correctiveAction: params.correctiveAction, preventativeAction: params.preventativeAction, flagPrimary: params.flagPrimary,
                           pvcLcpId        : params.pvcLcpId, actions: params.actions.trim(), summary: params.summary.trim(), investigation: params.investigation.trim(),
                           correctiveDate  : params.correctiveDate, preventiveDate: params.preventiveDate]
            } else if (params?.late instanceof String[]) {
                if(params.correctiveAction && params.preventativeAction) {
                    params?.late.eachWithIndex { it, i ->
                        rcList << [late            : params.baseLate, rootCause: params.rootCause[i], responsibleParty: params.responsibleParty[i],
                                   correctiveAction: params.correctiveAction[i], preventativeAction: params.preventativeAction[i], flagPrimary: params.flagPrimary[i],
                                   pvcLcpId        : params.pvcLcpId[i], actions: params.actions[i].trim(), summary: params.summary[i].trim(), investigation: params.investigation[i].trim(),
                                   correctiveDate  : params.correctiveDate[i], preventiveDate: params.preventiveDate[i]]
                    }
                }else{
                    response.status = 400
                    return render([message: message(code: "app.error.fill.all.required").toString()] as JSON)
                }
            }

            Map primaryIssueDetailMap = qualityService.saveIssueDetailData(params.dataType, rcList, ids, selectedIssueType)
            if(primaryIssueDetailMap.errorMessage!=''){
                response.status = 500
                Map responseMap = [message:primaryIssueDetailMap.errorMessage]
                render(contentType: "application/json", responseMap as JSON)
                return
            }

            WorkflowJustification.withTransaction { status ->

                List results = []
                WorkflowRule rule = params.workflowRule ? WorkflowRule.get(params.workflowRule as Long) : null
                cases?.each { kase ->
                    qualityService.updateAssignedToForCase(params.dataType, kase.caseNumber, "" + kase.caseVersion, Tenants.currentId() as Long, params.assignedToUserGroup, Constants.USER_GROUP_TOKEN, null, kase.submissionIdentifier)
                    qualityService.updateAssignedToForCase(params.dataType, kase.caseNumber, "" + kase.caseVersion, Tenants.currentId() as Long, params.assignedToUser, Constants.USER_TOKEN, null, kase.submissionIdentifier)
                }


                if (rule) {
                    Map casesToUpdate = qualityService.getIdToUpdateWorkflow(params.dataType, cases.collect { it.id as Long }, qualityObj.workflowState.id)
                    if (casesToUpdate.badIds) {
                        status.setRollbackOnly()
                        response.status = 500
                        Map responseMap = [message: message(code: "app.periodicReportConfiguration.state.update.warn"), errorRows: casesToUpdate.badIds]
                        render(contentType: "application/json", responseMap as JSON)
                        return

                    } else {
                        casesToUpdate.goodIds.each { id ->
                            WorkflowJustification currentWorkflowJustificationInstance = new WorkflowJustification(
                                    fromState: rule.initialState,
                                    toState: rule.targetState,
                                    routedBy: userService.currentUser,
                                    description: params.justification,
                                    workflowRule: rule,
                                    qualityCaseData: (params.dataType == PvqTypeEnum.CASE_QUALITY.name() ? qualityService.initializeQualityObjById(params.dataType, id) : null),
                                    qualitySubmission: (params.dataType == PvqTypeEnum.SUBMISSION_QUALITY.name() ? qualityService.initializeQualityObjById(params.dataType, id) : null),
                                    qualitySampling: (params.dataType == PvqTypeEnum.SAMPLING.name() ? qualityService.initializeQualityObjById(params.dataType, id) : null)
                            )
                            results << workflowService.assignPvqWorkflow(currentWorkflowJustificationInstance, true, false)
                        }
                        if (results.find { !it.success }) {
                            errorRows = results.findAll { !it.success }?.collect { it.rowInfo }
                            status.setRollbackOnly()
                            response.status = 500
                            Map responseMap = [message: message(code: "app.periodicReportConfiguration.state.update.warn"), errorRows: errorRows]
                            render(contentType: "application/json", responseMap as JSON)
                            return
                        }
                    }
                }
            }
            render primaryIssueDetailMap as JSON
        } catch (ValidationException e) {
            log.error("Validation Error Occured During saving quality issue type and root causes data --${e.getMessage()}",e)
            response.status = 400
            render([message: message(code: "app.error.fill.all.required").toString()] as JSON)
        } catch (Exception e) {
            log.error("Server Error Occured During saving quality issue type and root causes data --${e.getMessage()}",e)
            response.status = 500
            render([message: message(code: "error.500.title").toString()] as JSON)
        }
    }

    def getAllRcasForCase() {
        Map dataMap = qualityService.getAllIssueRcaForId(params, Tenants.currentId() as Long)
        render dataMap as JSON
    }

    String getAssignedToFromMetadata(drilldownCLLMetadata) {
        String assignedTo = null
        if (drilldownCLLMetadata.assignedToUser)
            assignedTo = Constants.USER_TOKEN + drilldownCLLMetadata.assignedToUser.id
        if (drilldownCLLMetadata.assignedToUserGroup)
            assignedTo = Constants.USER_GROUP_TOKEN + drilldownCLLMetadata.assignedToUserGroup.id
        return assignedTo
    }


    def deleteCases() {
        if (params.boolean("selectAll")) {
            qualityService.deleteCases(getQualityDataByFilter(params, params.type, true), params.type, Tenants.currentId() as Long, params.justification)
        } else {
            qualityService.deleteCases((params."selectedIds[]" instanceof String? params."selectedIds[]"?.split(";"):params."selectedIds[]")?.collect { it as Long }, params.type, Tenants.currentId() as Long, params.justification)
        }
        render(AjaxResponseDTO.success().withSuccessAlert(message(code: 'quality.case.deleted.success')).toJsonAjaxResponse())
    }

    def displayFieldLevelMsg() {
        List<Long> ids
        if (params.selectAll == "true") {
            ids = getQualityDataByFilter(params, params.type, true)?.collect { it as Long }
        } else {
            ids = params."selectedIds[]" ? (params."selectedIds[]" instanceof String ? params."selectedIds[]"?.split(";") : params."selectedIds[]")?.collect { it as Long } : []
        }
        def displayMsg = qualityService.displayFieldLevelMsg(ids, params.type, Tenants.currentId() as Long)
        render displayMsg
    }

    def actionPlan() {

    }

    def actionPlanList() {
        String timeZone = userService.currentUser.preference.timeZone
        Map<String, Date> ranges = qualityService.getRanges(params);
        Map json = [aaData: qualityService.getActionPlanData(params, ranges)]
        json.periods = ranges.collectEntries { k, v -> [(k): v.format(DateUtil.DATEPICKER_FORMAT, TimeZone.getTimeZone(timeZone))] }
        render json as JSON
    }

    def actionPlanCaseList() {
        Map json = [aaData: []]
        String timeZone = userService.currentUser.preference.timeZone
        Locale locale = userService.currentUser.preference.locale
        if (params.period) {
            Map<String, Date> ranges = qualityService.getRanges(params);
            List<String> issueTypeFilter = params.issueTypeFilter?.split(";")?.findAll { it }
            json.aaData = qualityService.fetchActonPlanCasesData(ranges["from" + params.period], ranges["to" + params.period], [params.responsibleParty], [params.errorType], [params.observation], params.workflowFilter, issueTypeFilter, params.priority, params["primaryOnly"] as Boolean, timeZone, locale)
        }
        render json as JSON
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

    private byte[] prepareActionPlanExcel(Map params, String fileName, String timeZone, Locale locale) {
        Map<String, Date> ranges = qualityService.getRanges(params);
        List sheets = []
        def header = [[title: ViewHelper.getMessage("quality.capa.responsibleParty.label"), width: 25],
                      [title: ViewHelper.getMessage("app.actionPlan.observation"), width: 25],
                      [title: ViewHelper.getMessage("app.label.errorType"), width: 25],
                      [title: ViewHelper.getMessage("app.label.quality.priority"), width: 25],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.Number"), width: 15],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pcv"), width: 15],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pcit"), width: 15],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pco"), width: 15],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pop"), width: 15],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.CorectPreventActions"), width: 15],
                      [title: ViewHelper.getMessage("app.actionPlan.LastPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.cpp"), width: 15]]
        for (int i = 1; ; i++) {
            if (!ranges["from" + i]) break;
            header.addAll([
                    [title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.Number"), width: 15],
                    [title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pcv"), width: 15],
                    [title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pcit"), width: 15],
                    [title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pco"), width: 15],
                    [title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.pop"), width: 15],
                    [title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.CorectPreventActions"), width: 15]
            ])
            if (ranges["from" + (i + 1)]) header.add([title: ViewHelper.getMessage("app.actionPlan.PreviousPeriod") + "\n" + ViewHelper.getMessage("app.actionPlan.cpp"), width: 15])
        }

        def d = [data: qualityService.getActionPlanData(params, ranges).collect { it ->
            List row = [it['responsibleParty'], it['observation'], it['errorType'], it['priority']]
            for (int i = 0; ; i++) {
                if (!ranges["from" + i]) break;
                row.addAll([it['lastNumber' + i], it['lastVendor' + i], it['lastIssue' + i], it['lastObservation' + i], it['lastPriority' + i], it['completed' + i]])
                if (ranges["from" + (i + 1)]) row.add(it['lastToPrevious' + i] > 1000 ? ">1000" : it['lastToPrevious' + i])
            }
            row
        }, metadata  : [sheetName: "Action Plan",
                        columns  : header]]

        sheets << d
        List<Long> responsiblePartyFilter = FilterUtil.parseParamList(params.responsiblePartyFilter)?.collect { it as Long }
        List<String> observationFilter = FilterUtil.parseParamList(params.observationFilter)
        List<String> issueTypeFilter = FilterUtil.parseParamList(params.issueTypeFilter)
        List<String> errorTypeFilter = FilterUtil.parseParamList(params.errorType)
        List caseHeader = [
                [title: ViewHelper.getMessage("app.case.id.label"), width: 25],
                [title: ViewHelper.getMessage("app.caseNumber.label"), width: 25],
                [title: ViewHelper.getMessage("app.label.quality.caseVersion"), width: 25],
                [title: ViewHelper.getMessage("app.actionPlan.observation"), width: 25],
                [title: ViewHelper.getMessage("app.label.errorType"), width: 25],
                [title: ViewHelper.getMessage("quality.capa.rootCause.label"), width: 25],
                [title: ViewHelper.getMessage("quality.capa.responsibleParty.label"), width: 25],
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
            List data = qualityService.fetchActonPlanCasesData(ranges["from" + i], ranges["to" + i], responsiblePartyFilter, errorTypeFilter, observationFilter, params.workflowFilter, issueTypeFilter, params.priorityFilter,params["primaryOnly"] as Boolean, timeZone ,locale)
                    .collect { it -> [it.id, it.caseNumber, it.caseVersion, it.observation, it.errorType, it.rootCause, it.responsibleParty, it.correctiveAction, it.preventativeAction, it.correctiveDate, it.preventativeDate, it.investigation, it.summary, it.actions, it.primary, it.workFlowState, it.assignedTo] }
            Map sheet = [
                    data    : data,
                    metadata: [sheetName: "Cases ${ranges["from" + i].format(DateUtil.ISO_DATE_FORMAT)} ${ranges["to" + i].format(DateUtil.ISO_DATE_FORMAT)}",//ISO_DATE_FORMAT berause sheet name has 30 char limit
                               columns  : caseHeader]
            ]
            sheets << sheet
        }
        byte[] data = qualityService.exportToExcel(sheets)
        AuditLogConfigUtil.logChanges(sheets, [outputFormat: ReportFormatEnum.XLSX.name(), fileName: fileName, exportedDate: new Date()],
                [:], Constants.AUDIT_LOG_EXPORT, ViewHelper.getMessage("auditLog.entityValue.action.plan.export", "PV Quality"))
        return data
    }

    def importExcel() {
        Map map = [uploadedValues: "", message: "", success: false]
        MultipartFile file = request.getFile('file')

        List list = importService.readFromExcel(file)
        if (list) {
            map.uploadedValues = list.join(';')
            map.success = true
        } else {
            map.message = "${message(code: 'app.label.no.data.excel.error')}"
        }
        render map as JSON
    }

    def fieldPossibleValues(String lang, String field, String term, Integer max, Integer page) {           //fetching dropdown values for the field
        if (field != null) {
            if(ReportField.findByNameAndIsDeletedAndListDomainClass(field,false,NonCacheSelectableList.class)){
                redirect(controller: 'query', action: 'possiblePaginatedValues', params: [lang: lang, field: field, term: term, max: max, page: page])
            } else {
                render((reportFieldService.getSelectableValuesForFields(lang).get(field).findAll { it.toString().toLowerCase().contains(term.toLowerCase()) }.collect { new JSONObject(id: it, text: it) }) as JSON)
            }
            return
        }
        render reportFieldService.getSelectableValuesForFields(lang).findAll {it.toString().toLowerCase().contains(term.toLowerCase())}.collect{new JSONObject(id: it, text: it)} as JSON
    }

    private void sortIssueList(List issueList, boolean allIssues = true) {
        String priorityEmptyLabel = null
        if (!allIssues) {
            priorityEmptyLabel = message(code: 'qualityModule.assign.priority')
        }
        issueList.sort { a, b ->
            def valueA = a[params.sort]
            def valueB = b[params.sort]
            if (params.sort == 'actionItemStatus') {
                valueA = params.get(String.format("sortMap[%s]", valueA))
                valueB = params.get(String.format("sortMap[%s]", valueB))
            } else if (params.sort == 'issueTypeDetail') {
                valueA = String.format("[%s]-[%s]-[%s]", a["qualityIssueType"], a["rootCause"], a["responsibleParty"])
                valueB = String.format("[%s]-[%s]-[%s]", b["qualityIssueType"], b["rootCause"], b["responsibleParty"])
            } else if (params.sort == 'priority') {
                valueA = valueA == "-1" ? priorityEmptyLabel : valueA
                valueB = valueB == "-1" ? priorityEmptyLabel : valueB
            } else if (params.sort == 'value') {
                valueA = valueA ? valueA.toString().trim() : valueA
                valueB = valueB ? valueB.toString().trim() : valueB
            }
            def comparison = valueA <=> valueB
            if (comparison != 0 && (!valueA || !valueB)) {
                comparison = !valueA ? 1 : -1
            }
            params.direction == 'asc' ? comparison : -comparison
        }
    }
}
