package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.FileDTO
import com.rxlogix.enums.*
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.mapping.AgencyName
import com.rxlogix.mapping.CaseInfo
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupRole
import com.rxlogix.util.CsvUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FilterUtil
import com.rxlogix.util.RelativeDateConverter
import com.rxlogix.util.ViewHelper
import com.rxlogix.user.Preference
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
import org.apache.commons.csv.CSVRecord
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.*
import org.joda.time.Period
import org.jsoup.Jsoup
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ResourceLoader

import javax.sql.DataSource
import java.sql.Connection
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Timestamp
import java.text.DateFormat
import java.text.Format
import java.text.SimpleDateFormat
import java.util.zip.GZIPInputStream
import org.apache.poi.ss.usermodel.*
import org.apache.poi.common.usermodel.HyperlinkType

@Transactional
class QualityService implements ResourceLoaderAware {
    public static final String DATE_FMT = "dd-MMM-yyyy hh:mm a"
    GrailsApplication grailsApplication
    def userService
    def CRUDService
    def utilService
    def configurationService
    def emailService
    def groovyPageRenderer
    ResourceLoader resourceLoader
    DataSource dataSource_pva
    DataSource dataSource_safetySource
    def fileAttachmentLocator
    public static final String EMPTY_STRING = "";
    private Connection getReportConnectionForPVR() {
        return utilService.getReportConnectionForPVR()
    }

    def retrieveQualityData(List<ExecutedReportConfiguration> executedConfigurationInstances, String type) {
        List result = []
        executedConfigurationInstances.each { executedConfigurationInstance ->
            result.addAll(retrieveQualityData(executedConfigurationInstance, type))
        }
        return result
    }

    def retrieveQualityData(ExecutedReportConfiguration executedConfigurationInstance, String type) {
        //TODO Implement widget
        List result = []
        return result
    }

    def retrieveManualAddedQualityData(String type) {
        //TODO Implementation for widgets
        List result = []
        return result
    }

    List<CSVRecord> retrieveSamplingData(List<ExecutedReportConfiguration> executedConfigurationInstances) {
        List result = []
        executedConfigurationInstances.each { executedConfigurationInstance ->
            result.addAll(retrieveSamplingData(executedConfigurationInstance))
        }
        return result
    }

    List<CSVRecord> retrieveSamplingData(ExecutedReportConfiguration executedConfigurationInstance) {
        List data = []
        if (executedConfigurationInstance) {
            List<ExecutedTemplateQuery> executedTemplateQueries = executedConfigurationInstance.executedTemplateQueries

            for (ExecutedTemplateQuery executedTemplateQuery : executedTemplateQueries) {
                ReportResult reportResult = executedTemplateQuery.reportResult
                if (reportResult?.data?.value) {
                    CaseLineListingTemplate template = executedTemplateQuery.executedTemplate

                    // Read header
                    def fieldNameIndexTupleList = template.getFieldNameIndexTuple()
                    String[] headers = fieldNameIndexTupleList.collect { it.getFirst() }

                    def result = new GZIPInputStream(new ByteArrayInputStream(reportResult?.data?.getDecryptedValue()))
                    def scanningResult = new Scanner(result).useDelimiter("\\A")
                    def theString = scanningResult.next()
                    data.addAll(CsvUtil.parseCsv(headers, new StringReader(theString)).collect {
                        def m = it.toMap();
                        m << [report: executedConfigurationInstance.id]; m
                    })
                }
            }
        }
        data
    }

    Map checkCaseNumber(String caseNumber) {
        if (caseNumber) {
            Sql sql = new Sql(dataSource_pva)
            log.info("Checking case number from PV Mart: ${caseNumber}")

            String searchQuery = ''
            if (grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)) {
                searchQuery = "SELECT CASE_NUM, CI.VERSION_NUM, CF.PRIM_PROD_NAME, CI.DATE_RECEIPT_MOST_RECENT, \n" +
                        " CI.OCCURED_COUNTRY_DESC, CI.SOURCE_TYPE_DESC, NULL AS SITE_DESC \n" +
                        " FROM  C_IDENTIFICATION CI, C_IDENTIFICATION_FU CF \n" +
                        " WHERE  TRUNC (NVL(CI.DATE_VERSION_END,  TO_DATE('31/12/9999', 'DD/MM/YYYY') )) = TO_DATE('31/12/9999', 'DD/MM/YYYY') \n" +
                        " AND CI.CASE_ID = CF.CASE_ID \n" +
                        " AND CI.VERSION_NUM = CF.VERSION_NUM \n" +
                        " AND CI.TENANT_ID = CF.TENANT_ID \n" +
                        " AND CF.WORKFLOW_STATE_ID != 12 \n" +
                        " AND CASE_NUM = :caseNumberToSearch \n" +
                        " AND EXISTS ( SELECT 1\n" +
                        "                FROM CDR_DATES_LATEST_LOCKED CLL\n" +
                        "               WHERE CI.CASE_ID = CLL.CASE_ID\n" +
                        "                  AND CI.VERSION_NUM = CLL.VERSION_NUM\n" +
                        "                  AND CI.TENANT_ID = CLL.TENANT_ID) order by ci.version_num desc"
            }
            else {
                searchQuery = "  SELECT case_num, ci.version_num, cf.prim_prod_name, ci.date_receipt_most_recent,"+
                        "  ci.occured_country_desc, ci.source_type_desc, ls.site_desc "+
                        "  FROM  c_identification ci, c_identification_fu cf, c_master_addl cif,"+
                        "  vw_lsi_site_desc ls "+
                        "  WHERE "+
                        " trunc (ci.date_version_end) = TO_DATE('31/12/9999', 'DD/MM/YYYY') "+
                        "  AND ci.case_id = cif.case_id "+
                        "  AND ci.version_num = cif.version_num "+
                        "  AND ci.tenant_id = cif.tenant_id "+
                        "  AND ls.site_id (+) = cif.site_id "+
                        "  AND ls.tenant_id (+) = cif.tenant_id "+
                        "  AND ci.case_id = cf.case_id "+
                        "  AND ci.version_num = cf.version_num "+
                        "  AND ci.tenant_id = cf.tenant_id "+
                        "  AND cf.workflow_state_id != 1 "+
                        "  AND case_num = :caseNumberToSearch  "+
                        "  AND EXISTS ( SELECT 1\n" +
                        "                FROM CDR_DATES_LATEST_LOCKED CLL\n" +
                        "               WHERE CI.CASE_ID = CLL.CASE_ID\n" +
                        "                  AND CI.VERSION_NUM = CLL.VERSION_NUM\n" +
                        "                  AND CI.TENANT_ID = CLL.TENANT_ID) order by ci.version_num desc"
            }

            try {
                def dataList = sql.rows(searchQuery,[caseNumberToSearch:caseNumber]).collect {
                    String occuredCountry = ""
                    String receiptDate = ""
                    if (it.date_receipt_most_recent) {
                        receiptDate = it.date_receipt_most_recent.format(DateUtil.DATEPICKER_FORMAT)
                    }
                    if (it.occured_country_desc) {
                        occuredCountry = it.occured_country_desc
                    }
                    [caseNumber     : it.CASE_NUM, masterVersionNum: it.version_num, country: occuredCountry, reportType: it.source_type_desc,
                     caseReceiptDate: receiptDate,
                     masterSiteId   : it.site_desc, masterPrimProdName: it.PRIM_PROD_NAME]
                }
                dataList ? dataList.first() : [:]

            } catch (SQLException e) {
                log.error("Exception caught in checking case number from PV Mart: ${caseNumber}, exception: ${e.message}")
            } finally {
                sql?.close()
            }
        }
    }

    Map saveAdhocQualityRecord(Map params, Long tenantId, Long selectedId = null) {
        Map model = ['status': 'failure']
        if (params) {
            List<String> minColumnNameList = getColumnList(params.dataType)
            List<String> columnNameList = minColumnNameList + grailsApplication.config.qualityModule.extraColumnList
            Map requiredMap = params.findAll { (it.key in columnNameList) }
            if (requiredMap.containsKey("masterCaseReceiptDate")){
                String masterCaseReceiptDateValue = requiredMap.getOrDefault("masterCaseReceiptDate", null)
                Format dateFormatter = new SimpleDateFormat(Constants.DateFormat.REGULAR_DATE)
                Date date = (Date) dateFormatter.parseObject(masterCaseReceiptDateValue)
                DateFormat dateFormat = new SimpleDateFormat(Constants.DateFormat.CSV_JASPER)
                requiredMap.put("masterCaseReceiptDate", dateFormat.format(date))
            }
            String mapStr = new JsonBuilder(requiredMap).toString()
            def selectedQualityObj = null
            def qualityObj = null
            switch (params.dataType) {
                case PvqTypeEnum.CASE_QUALITY.name():
                    qualityObj = params.id ? QualityCaseData.get(params.get("id") as Long) : new QualityCaseData()
                    if(selectedId) {
                        selectedQualityObj = QualityCaseData.get(selectedId)
                    }
                    break
                case PvqTypeEnum.SUBMISSION_QUALITY.name():
                    qualityObj = params.id ? QualitySubmission.get(params.get("id") as Long) : new QualitySubmission()
                    if(selectedId) {
                        selectedQualityObj = QualitySubmission.get(selectedId)
                    }
                    break
                default:
                    qualityObj = params.id ? QualitySampling.get(params.get("id") as Long) : new QualitySampling([type: params.dataType])
                    if(selectedId) {
                        selectedQualityObj = QualitySampling.get(selectedId)
                    }
                    break
            }

            qualityObj.caseNumber = params.masterCaseNum ? params.masterCaseNum.trim() : params.masterCaseNum
            qualityObj.errorType = params.errorType

            if (!params.id) {//do not update metadata
                if(selectedQualityObj){
                    mapStr = selectedQualityObj.metadata
                }
                qualityObj.metadata = mapStr
                if(params.dataType==PvqTypeEnum.SUBMISSION_QUALITY.name()) qualityObj.submissionIdentifier=params.submissionIdentifier
                qualityObj.entryType = params.entryType ?: QualityEntryTypeEnum.MANUAL.getValue()
                qualityObj.triageAction = 0L
                qualityObj.workflowStateUpdatedDate = new Date()
                qualityObj.tenantId = tenantId
                Map record = new JsonSlurper().parseText(mapStr)
                qualityObj.versionNumber = record["masterVersionNum"] ? Long.parseLong(record["masterVersionNum"]) : record["masterVersionNum"]
                qualityObj.workflowState = WorkflowState.defaultWorkState
                List similarCases = getSimilarQualityObj(params.dataType, qualityObj.caseNumber, qualityObj.versionNumber, tenantId,  params.submissionIdentifier)
                def similarCase = similarCases?.size() > 0 ? similarCases.get(0) : null
                if (similarCase) {
                    qualityObj.workflowState = similarCase.workflowState
                    qualityObj.assignedToUser = similarCase.assignedToUser
                    qualityObj.assignedToUserGroup = similarCase.assignedToUserGroup
                }
                qualityObj.updateDueDate(null)
            }

            if (params.mandatoryType) {
                qualityObj.mandatoryType = params.mandatoryType as QualityTypeEnum
            }
            qualityObj.fieldName = params.fieldName
            qualityObj.value = params.value
            qualityObj.fieldLocation = params.fieldLocation
            if (params.priority && params.priority.length() > 0) {
                qualityObj.priority = params.priority
            }
            try{
                if(params.comment?.length() > 0){
                    Comment comment = new Comment(textData:params.comment)
                    CRUDService.save(comment)
                    qualityObj.comments<<comment
                }
                editQualityIssueType(qualityObj, params.qualityIssueTypeId)
                CRUDService.save(qualityObj)
                model.put('status', 'success')
            }catch(Exception e){
                log.error("Error in saving manual quality record", e)
            }
        }
        model
    }

    def editQualityIssueType(def qualityObj, String qualityIssueTypeId){
        if(!qualityIssueTypeId){
            if(qualityObj.qualityIssueDetails)
                qualityObj.qualityIssueDetails.clear()
        } else {
            Long updatedIssueTypeId = Long.parseLong(qualityIssueTypeId)
            if (qualityObj.qualityIssueTypeId != updatedIssueTypeId && qualityObj.qualityIssueDetails) // Issue type changed
                qualityObj.qualityIssueDetails.clear()
            qualityObj.qualityIssueTypeId = updatedIssueTypeId
        }
    }

    def exportToExcel(data, metadata) {
        XSSFWorkbook workbook = new XSSFWorkbook()
        workbook = writeDataToWorkbook(data, metadata, workbook, null)
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        workbook.write(out)
        out.toByteArray()
    }

    def exportToExcel(List sheets) {
        XSSFWorkbook workbook = new XSSFWorkbook()
        CreationHelper creationHelper = workbook.getCreationHelper()
        sheets.each {
            workbook = writeDataToWorkbook(it.data, it.metadata, workbook, null)
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        workbook.write(out)
        out.toByteArray()
    }

    XSSFWorkbook writeDataToWorkbook(data, metadata, XSSFWorkbook workbook, String moduleType) {
        XSSFSheet worksheet = workbook.createSheet(metadata?.sheetName ?: "Data");

        XSSFFont defaultFont = workbook.createFont();
        defaultFont.setFontHeightInPoints((short) 10);
        defaultFont.setFontName("Arial");
        defaultFont.setColor(IndexedColors.BLACK.getIndex());
        defaultFont.setBold(false);
        defaultFont.setItalic(false);

        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setItalic(false);

        XSSFRow row1
        XSSFRow row
        XSSFCell cell
        if(moduleType == "BQMQ") {
            row = worksheet.createRow((short) 0)
        }else {
            row1 = worksheet.createRow((short) 0)
            XSSFCell cellA1 = row1.createCell((short) 0)
            User user = userService.user
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getLongDateFormatForLocale(user?.preference?.locale, true))
            sdf.setTimeZone(TimeZone.getTimeZone(ViewHelper.getTimeZone(user)))
            cellA1.setCellValue(sdf.format(new Date()))

            row = worksheet.createRow((short) 1)
            row = worksheet.createRow((short) 2)

        }

        XSSFColor color = new XSSFColor(new java.awt.Color(0, 113, 156), workbook.getStylesSource().getIndexedColors());
        int indexOfReportLink = 0;
        int indexOfCaseLink = 0;
        metadata.columns.eachWithIndex { it, i ->
            cell = row.createCell((short) i)
            worksheet.setColumnWidth(i, 256 * (it.width as Integer))
            cell.setCellValue(it.title as String)
            XSSFCellStyle style = workbook.createCellStyle()

            style.setAlignment(HorizontalAlignment.CENTER);
            style.setFont(font);
            style.setFillForegroundColor(color)
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setWrapText(true)
            cell.setCellStyle(style)
            if (metadata.sheetName.equals("Action Item")) {
                indexOfReportLink = i
                indexOfCaseLink = i-1
                int freezeColumnCount = 2;  // Adjust this value to freeze more columns if needed
                worksheet.createFreezePane(freezeColumnCount, 0, freezeColumnCount, 0)

            }
        }

        data.eachWithIndex { dataRow, j ->
            if(moduleType == "BQMQ") {
                row = worksheet.createRow((short) 1 + j)
            }else {
                row = worksheet.createRow((short) 3 + j)
            }
            dataRow.eachWithIndex { it, i ->
                cell = row.createCell((short) i)
                if (metadata.sheetName.equals("Action Item") && (i == indexOfReportLink || i == indexOfCaseLink)) {
                    XSSFFont hyperlinkFont = workbook.createFont()
                    hyperlinkFont.setUnderline(Font.U_SINGLE)
                    hyperlinkFont.setColor(IndexedColors.BLUE.getIndex())
                    XSSFCellStyle hyperlinkStyle = workbook.createCellStyle()
                    hyperlinkStyle.setFont(hyperlinkFont)
                    String linkUrl = extractLinkUrl(it)
                    String linkText = extractLinkText(it)

                    CreationHelper creationHelper = workbook.getCreationHelper()
                    Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL)
                    hyperlink.setAddress(linkUrl)
                    cell.setHyperlink(hyperlink)
                    cell.setCellValue(linkText)
                    cell.setCellStyle(hyperlinkStyle)
                    // Set the font color explicitly to blue
                    XSSFRichTextString richString = new XSSFRichTextString(cell.getStringCellValue())
                    richString.applyFont(hyperlinkFont)
                    cell.setCellValue(richString)
                } else {
                    cell.setCellValue(sanitize(it as String))
                }
            }
        }

        return workbook
    }
    def extractLinkUrl(linkHtml) {
        def doc = Jsoup.parse(linkHtml)
        doc.select("a").attr("href")
    }

    def extractLinkText(linkHtml) {
        def doc = Jsoup.parse(linkHtml)
        doc.text()
    }

    String sanitize(String cellValue){
        if(cellValue?.size() > Constants.ExcelConstants.MAX_CELL_LENGTH_XLSX){
            cellValue = cellValue.replace(cellValue.substring(Constants.ExcelConstants.MAX_CELL_LENGTH_XLSX - Constants.ExcelConstants.TRUNCATE_TEXT_XLSX.size()), Constants.ExcelConstants.TRUNCATE_TEXT_XLSX)
        }
        return cellValue
    }

    private List<String> getQualityFields(List<String> reportFields, String type) {
        List<String> qualityFields = [];
        Map dbFieldToQualityFieldMap = [:]
        if (type in [PvqTypeEnum.CASE_QUALITY.name(), PvqTypeEnum.SUBMISSION_QUALITY.name()]) {
            dbFieldToQualityFieldMap = grailsApplication.config.qualityModule.dbFieldToQualityFieldMap?:[:];
        } else {
            dbFieldToQualityFieldMap = grailsApplication.config.qualityModule.dbFieldToSamplingFieldMap?:[:];

        }
        int size = reportFields.size()

        for (int i = 0; i < size; i++) {
            String fieldname = reportFields[i]
            fieldname = dbFieldToQualityFieldMap[fieldname] ? dbFieldToQualityFieldMap[fieldname] : fieldname

            qualityFields<<fieldname
        }
        return qualityFields;
    }

    List<String> getPVQFields(Long reportId, Long executedReportId, List<String> reportFields, String type, ResultSetMetaData rsmd, List<String> renameValues, List<Boolean> selectableValues) {
        List<String> qualityFields = getQualityFields(reportFields, type)
        List<QualityField> qualityFieldList = []

        for (int i = 0; i < qualityFields.size(); i++) {
            qualityFieldList <<  new QualityField(["fieldName": qualityFields[i], "fieldType": rsmd.getColumnTypeName(i + 1), label: renameValues[i], isSelectable: selectableValues[i]])
        }
        qualityFieldsSave(reportId, executedReportId, qualityFieldList,type);
        return qualityFields
    }

    boolean isAdditional(String type) {
        getAdditional(type)
    }

    Map getAdditional(String type) {
        Holders.config.qualityModule.additional.find { it.name == type }
    }

    List listTypes() {
        [
                [label: Holders.config.getProperty('qualityModule.qualityLabel'), value: PvqTypeEnum.CASE_QUALITY.name()],
                [label: Holders.config.getProperty('qualityModule.submissionLabel'), value: PvqTypeEnum.SUBMISSION_QUALITY.name()]
        ] + Holders.config.qualityModule.additional.collect { [label: it.label, value: it.name] }
    }
    List listWorkflowStates(ReasonOfDelayAppEnum type = ReasonOfDelayAppEnum.PVQ) {
        Set<WorkflowState> workflowStates = []
        List types = (type == ReasonOfDelayAppEnum.PVQ ? WorkflowConfigurationTypeEnum.getAllQuality():[WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY])
        WorkflowRule.findAllByConfigurationTypeEnumInListAndIsDeleted(types , false).each {
            workflowStates.add(it.initialState)
            workflowStates.add(it.targetState)
        }
        List result = workflowStates.toList().findAll { it }.sort { it.name }.collect{
            [label:it.name,value: it.id]
        }
        return [
                [label: ViewHelper.getMessage('app.actionPlan.notFinal' ) , value: "notFinal"],
                [label: ViewHelper.getMessage('app.actionPlan.final' ) , value: "final"],
                [label: ViewHelper.getMessage('app.actionPlan.any') , value: ""]
        ]+result

    }

    List getColumnList(String type) {
        if (type == PvqTypeEnum.CASE_QUALITY.name()) {
            return grailsApplication.config.getProperty('qualityModule.qualityColumnList', List)
        } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
            return grailsApplication.config.getProperty('qualityModule.submissionColumnList', List)
        } else
            return Holders.config.qualityModule.additional.find { it.name == type }.columnList
    }


    String getLabelForType(String type) {
        if (type == PvqTypeEnum.CASE_QUALITY.name()) {
            return Holders.config.getProperty('qualityModule.qualityLabel')
        } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
            return Holders.config.getProperty('qualityModule.submissionLabel')
        } else {
            return Holders.config.qualityModule.additional.find { it.name == type }?.label
        }
    }

    List<String> getTypes(ExecutedTemplateQuery executedTemplateQuery) {
        return executedTemplateQuery.executedConfiguration.pvqType?.split(";")
    }

    void sortColumnsAndUpdateMapping() {
        if (!grailsApplication.config.qualityModule.qualityColumnUiStackMapping) grailsApplication.config.qualityModule.qualityColumnUiStackMapping = []
        if (!grailsApplication.config.qualityModule.submissionColumnUiStackMapping) grailsApplication.config.qualityModule.submissionColumnUiStackMapping = []
        sortColumnsAndUpdateMapping(grailsApplication.config.qualityModule.qualityColumnList, grailsApplication.config.qualityModule.qualityColumnUiStackMapping)
        sortColumnsAndUpdateMapping(grailsApplication.config.qualityModule.submissionColumnList, grailsApplication.config.qualityModule.submissionColumnUiStackMapping)
        Holders.config.qualityModule.additional.each {
            if (!it.columnUiStackMapping) it.columnUiStackMapping = []
            sortColumnsAndUpdateMapping(it.columnList, it.columnUiStackMapping)
        }
    }

    void sortColumnsAndUpdateMapping(def columnList, def columnListUiMapping) {

        if (columnList && columnListUiMapping) {
            List<String> resultColumnList = []
            for (int i = 0; i < columnListUiMapping.size(); i++) {
                for (int j = 0; j < columnListUiMapping[i].size(); j++) {
                    int indx = columnList.indexOf(columnListUiMapping[i][j])
                    if (indx > -1) {
                        resultColumnList << columnListUiMapping[i][j]
                        columnList.remove(indx);
                    } else {
                        columnListUiMapping[i].remove(j)
                        j--
                    }
                }
            }
            columnList.each {
                resultColumnList << it
                columnListUiMapping.add([it])
            }
            for (int i = 0; i < columnListUiMapping.size(); i++) {
                if (!columnListUiMapping[i]) {
                    columnListUiMapping.remove(i)
                    i--
                }
            }
            columnList.clear()
            columnList.addAll(resultColumnList)
        }

    }

    void qualityDataAddExtraSave(Map metadata, Long reportId, Long executedTemplateId, Long executedReportId, String caseNum, String queryName, Map rcaData, String type, Sql pvrsql, Sql pvaSql, Long tenantId) {
        Map qualityRecord = [:]
        qualityRecord["metadata"] = metadata as JSON
        qualityRecord["reportId"] = reportId
        qualityRecord["caseNumber"] = caseNum
        qualityRecord["errorType"] = queryName
        qualityRecord["executedReportId"] = executedReportId
        qualityRecord["tenantId"] = tenantId
        qualityRecord["workflowState"] = WorkflowState.defaultWorkState.id
        qualityRecord["executedTemplateId"] = executedTemplateId
        qualityRecord["submissionIdentifier"] = metadata["vcsProcessedReportId"]
        qualityRecord.putAll(rcaData)
        saveQualityRecord(qualityRecord, type, pvrsql, pvaSql, metadata["masterVersionNum"])
    }

    private void saveQualityRecord(Map qualityRecord, String type, Sql pvrsql, Sql pvaSql, String version) {
        String tableName = ""
        String linkDetailSql = ""
        String typeCondition = ""
        String typeInsert = ""
        String moduleName = ""
        String submissionIdentifier = ""
        String submissionCondition = ""
        Date workflowStateUpdatedDate = new Date()
        WorkflowRule workFlowRule = new WorkflowRule()
        if (type == PvqTypeEnum.CASE_QUALITY.name()) {
            tableName = "QUALITY_CASE_DATA"
            linkDetailSql = "insert into QUALITY_CASE_ISSUE_DETAILS(QUALITY_CASE_ID,QUALITY_ISSUE_DETAIL_ID) values (:id,:detail_id)"
            moduleName = WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA.getKey()
            workFlowRule = WorkflowRule.getDefaultWorkFlowRuleByType(WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA)

        } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
            tableName = "Quality_SUBMISSION"
            linkDetailSql = "insert into QUALITY_SUB_ISSUE_DETAILS(QUALITY_SUBMISSION_ID,QUALITY_ISSUE_DETAIL_ID) values (:id,:detail_id)"
            moduleName = WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION.getKey()
            workFlowRule = WorkflowRule.getDefaultWorkFlowRuleByType(WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION)
            submissionIdentifier = qualityRecord["submissionIdentifier"]
            submissionCondition = " and SUBMISSION_IDENTIFIER = '"+qualityRecord["submissionIdentifier"]+"'"
        } else {
            typeCondition = " and TYPE = '${type}'"
            typeInsert = type
            tableName = "QUALITY_SAMPLING"
            linkDetailSql = "insert into QUALITY_SAMPL_ISSUE_DETAILS(QUALITY_SAMPLING_ID, QUALITY_ISSUE_DETAIL_ID) values (:id,:detail_id)"
            Map additionalType = getAdditional(type)
            moduleName = additionalType.name
            workFlowRule= WorkflowRule.getDefaultWorkFlowRuleByType(WorkflowConfigurationTypeEnum.getAdditional(additionalType.workflow))
        }
        Date dueDate = ((workFlowRule?.excludeWeekends) ? DateUtil.addDaysSkippingWeekends(workflowStateUpdatedDate, workFlowRule?.dueInDays ?: 0) : workflowStateUpdatedDate.plus(workFlowRule?.dueInDays ?: 0))
        String querycnt = "SELECT ID, ENTRY_TYPE FROM ${tableName} A WHERE CASE_NUM = :caseNumber AND ERROR_TYPE = :errorType AND TENANT_ID = :tenantId AND ENTRY_TYPE = 'A' AND ISDELETED=0 AND A.VERSION_NUM = :version " + typeCondition + submissionCondition
        def cntres = pvrsql.firstRow(querycnt, ["caseNumber": qualityRecord["caseNumber"], "errorType": qualityRecord["errorType"], "version": version, "tenantId": qualityRecord["tenantId"]])
        boolean isExistsRecord = false
        if(cntres) {
            if(cntres["ID"] && cntres["ID"]>=1) {
                isExistsRecord=true
            }
        }

        Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        String statement = ""
        if (isExistsRecord) {
            /*
                There can be cases where some records can be manual,
                modified query to update all those records for given case no and errorType , which are not manual
             */
            statement = "UPDATE ${tableName} A SET REPORT_ID = :reportId,EXEC_REPORT_ID = :executedReportId,METADATA=:metadata,LAST_UPDATED=:currDate,MODIFIED_BY='Application',PRIORITY=:priority,EXECUTED_TEMPLATE_ID = :executedTemplateId WHERE CASE_NUM=:caseNumber AND A.metadata.masterVersionNum = :version AND ERROR_TYPE=:errorType AND ISDELETED=0 AND TENANT_ID = :tenantId AND ENTRY_TYPE = 'A' " + (typeInsert ? " and TYPE = :type" : "") + submissionCondition
            pvrsql.execute(statement, ["reportId": qualityRecord["reportId"], "priority": qualityRecord["priority"], "caseNumber": qualityRecord["caseNumber"], "version": version, "errorType": qualityRecord["errorType"], "metadata": qualityRecord["metadata"].toString(), "currDate": currentTimestamp, "executedReportId": qualityRecord["executedReportId"], "tenantId": qualityRecord["tenantId"],"executedTemplateId":qualityRecord["executedTemplateId"]] + (typeInsert ? [type: typeInsert] : [:]))

        } else {
            List similarCases = getSimilarQualityObj(type, qualityRecord["caseNumber"], version as Long, qualityRecord["tenantId"], submissionIdentifier)
            if (similarCases) {
                qualityRecord.workflowState = similarCases?.get(0)?.workflowStateId ?: WorkflowState.defaultWorkState.id
                qualityRecord.assignedToUser = similarCases?.get(0)?.assignedToUserId
                qualityRecord.assignedToUserGroup = similarCases?.get(0)?.assignedToUserGroupId
            }
            if (qualityRecord.actionsSql) qualityRecord.actions = trimSqlValue("actions", pvaSql.firstRow(qualityRecord.actionsSql, [caseNumber: qualityRecord["caseNumber"], "version": version as Long, tenantId: qualityRecord["tenantId"]])?.entrySet()?.first()?.value?.toString())
            if (qualityRecord.investigationSql) qualityRecord.investigation = trimSqlValue("investigation", pvaSql.firstRow(qualityRecord.investigationSql, [caseNumber: qualityRecord["caseNumber"], "version": version as Long, tenantId: qualityRecord["tenantId"]])?.entrySet()?.first()?.value?.toString())
            if (qualityRecord.summarySql) qualityRecord.summary = trimSqlValue("summary", pvaSql.firstRow(qualityRecord.summarySql, [caseNumber: qualityRecord["caseNumber"], "version": version as Long, tenantId: qualityRecord["tenantId"]])?.entrySet()?.first()?.value?.toString())

            Long id = pvrsql.firstRow("select ${tableName}_ID.NEXTVAL as nextValue from dual", [])["nextValue"]
            statement = "INSERT INTO ${tableName}(ID,REPORT_ID,EXEC_REPORT_ID,CASE_NUM,VERSION_NUM,ERROR_TYPE,PRIORITY,METADATA,ENTRY_TYPE,ISDELETED,DATE_CREATED,CREATED_BY,LAST_UPDATED,MODIFIED_BY,TRIAGE_ACTION,TENANT_ID, WORKFLOW_STATE_ID,ASSIGNED_TO_USER, ASSIGNED_TO_USERGROUP, DUE_DATE, WORKFLOW_STATE_UPDATED_DATE ${(typeInsert ? ",TYPE" : "")}, EXECUTED_TEMPLATE_ID, QUALITY_ISSUE_TYPE_ID ${submissionIdentifier? ", SUBMISSION_IDENTIFIER":""}) " +
                    "VALUES(:id,:reportId,:execReportId,:caseNumber,:version,:errorType,:priority,:metadata,'A',0,:currDate,'Application',:currDate,'Application',0,:tenantId,:workflowState,:assignedToUser,:assignedToUserGroup,:dueDate,:workflowStateUpdatedDate ${(typeInsert ? ",:type" : "")},:executedTemplateId, :issueType ${(submissionIdentifier ?  ", :submissionIdentifier" : "")})"
            pvrsql.execute(statement, [id        : id, "reportId": qualityRecord["reportId"], "execReportId": qualityRecord["executedReportId"], "caseNumber": qualityRecord["caseNumber"], "version": version as Long, "errorType": qualityRecord["errorType"],
                                       "priority": qualityRecord["priority"], "metadata": qualityRecord["metadata"].toString(), "currDate": currentTimestamp, "tenantId": qualityRecord["tenantId"], "workflowState": qualityRecord["workflowState"], assignedToUser: qualityRecord["assignedToUser"], assignedToUserGroup: qualityRecord["assignedToUserGroup"], issueType: qualityRecord.issueType, dueDate: new Timestamp(dueDate.getTime()), "workflowStateUpdatedDate": currentTimestamp] + (typeInsert ? [type: typeInsert] : [:]) + ["executedTemplateId": qualityRecord['executedTemplateId']]+ (submissionIdentifier ? [submissionIdentifier: submissionIdentifier] : [:]));
            if (qualityRecord.rootCause || qualityRecord.actions || qualityRecord.investigation || qualityRecord.summary) {
                Long detail_id = pvrsql.firstRow("select HIBERNATE_SEQUENCE.NEXTVAL as nextValue from dual", [])["nextValue"]
                pvrsql.execute("insert into QUALITY_ISSUE_DETAIL (id,version,ROOT_CAUSE_ID,RESPONSIBLE_PARTY_ID,IS_PRIMARY,DATE_CREATED,LAST_UPDATED,CREATED_BY,MODIFIED_BY,INVESTIGATION,SUMMARY,ACTIONS)values(:id,:version,:ROOT_CAUSE_ID,:RESPONSIBLE_PARTY_ID,:IS_PRIMARY,:DATE_CREATED,:LAST_UPDATED,:CREATED_BY,:MODIFIED_BY,:INVESTIGATION,:SUMMARY, :ACTIONS) ",
                        [id: detail_id, version: 1L, ROOT_CAUSE_ID: qualityRecord.rootCause, RESPONSIBLE_PARTY_ID: qualityRecord.responsibleParty, IS_PRIMARY: 1, DATE_CREATED: currentTimestamp, LAST_UPDATED: currentTimestamp, CREATED_BY: "Application", MODIFIED_BY: "Application", INVESTIGATION: qualityRecord.investigation, SUMMARY: qualityRecord.summary, ACTIONS: qualityRecord.actions])
                pvrsql.execute(linkDetailSql, [id: id, detail_id: detail_id])
            }
            if (!similarCases) {
                boolean isBasicRule = (workFlowRule?.assignmentRule == com.rxlogix.enums.AssignmentRuleEnum.BASIC_RULE.name()) && workFlowRule?.assignedToUserGroup && (workFlowRule?.assignToUserGroup || workFlowRule?.autoAssignToUsers)
                boolean isAdvanceRule = workFlowRule?.assignmentRule == com.rxlogix.enums.AssignmentRuleEnum.ADVANCED_RULE.name()
                boolean correctProcessedReportId = (moduleName!=PvqTypeEnum.SUBMISSION_QUALITY.name()) || (submissionIdentifier && submissionIdentifier.isNumber())
                if (workFlowRule && (isBasicRule || isAdvanceRule) && correctProcessedReportId) {

                    AutoAssignment autoAssignment = new AutoAssignment()
                    autoAssignment.caseNumber = qualityRecord["caseNumber"]
                    autoAssignment.moduleName = moduleName
                    autoAssignment.workflowRule = workFlowRule
                    autoAssignment.processedReportId = submissionIdentifier
                    autoAssignment.tenantId = qualityRecord["tenantId"]
                    autoAssignment.versionNumber = (version != null ? Long.parseLong(version) : null)
                    autoAssignment.type = typeInsert
                    CRUDService.save(autoAssignment)
                }
            }
        }

    }

    private trimSqlValue(String field, String value) {
        int max = QualityIssueDetail.constrainedProperties[field].maxSize
        if (value?.length() >= max) return value.substring(0, max - 1)
        return value
    }


    void qualityRecordDeleteByReport(Long reportId, String type, Sql pvrsql, String errorType, Long tenantId) {
        String tableName = "QUALITY_CASE_DATA"
        String typeCondition = ""
        if (type == PvqTypeEnum.CASE_QUALITY.name()) {
            tableName = "QUALITY_CASE_DATA"
        } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
            tableName = "Quality_SUBMISSION"
        } else {
            tableName = "QUALITY_SAMPLING"
            typeCondition = " and TYPE = '${type}'"
        }
        String strSql = "UPDATE ${tableName} SET ISDELETED=1 WHERE ISDELETED=0 AND REPORT_ID=${reportId} AND ERROR_TYPE='${errorType}' AND TENANT_ID = ${tenantId}" + typeCondition
        pvrsql.execute(strSql)
    }

    List<String> getQualityPriorityList() {
        return Holders.config.qualityModule.pvqPriorityList?.collect{it.name}
    }

    private String getQualityDataExternalWhereStatement(Map externalSearch) {
        List<String> whereConditions=[]
        if(externalSearch["receiptDateFrom"]) {
            whereConditions.add("A.DATE_CREATED >= '" + externalSearch["receiptDateFrom"] + "'" )
        }
        if(externalSearch["receiptDateTo"]) {
            whereConditions.add("A.DATE_CREATED <= '" + externalSearch["receiptDateTo"] + "'" )
        }
        String whereCondition = whereConditions.join(" AND ")
        return whereCondition
    }

    private String getQualityErrorTypeCondition(Map externalSearch) {
        String strWhereErrTypeCondition = ""
        String[] errorTypes = null
        if(externalSearch["errorType"]) {
            if (externalSearch["errorType"] instanceof String) {
                errorTypes = externalSearch["errorType"].split("/,")
                for (int i = 0; i < errorTypes.size(); i++) {
                    errorTypes[i] = "'" + errorTypes[i] + "'"
                }
                strWhereErrTypeCondition = "A.ERROR_TYPE IN (${errorTypes.join(",")}) "
            } else if (externalSearch["errorType"] instanceof ArrayList && !externalSearch["errorType"][0].equals("")) {
                errorTypes = externalSearch["errorType"]
                for (int i = 0; i < errorTypes.size(); i++) {
                    errorTypes[i] = "'" + errorTypes[i] + "'"
                }
                strWhereErrTypeCondition = "A.ERROR_TYPE IN (${errorTypes.join(",")}) "
            }
        }
        return strWhereErrTypeCondition
    }

    private String getAdvanceSearchQry(Map mapAdvanceSearch, String type) {
        List<String> advSearch = []
        if(mapAdvanceSearch !=null) {
            mapAdvanceSearch.each { key,val ->
                String searchValue = null;
                if (val["value"] instanceof ArrayList) {
                    val["value"] = val["value"].findAll {it!=''}
                    searchValue = val["value"].join("', '")
                } else {
                    searchValue = val["value"]?.toString()?.trim()
                }
                if(searchValue && searchValue != "") {
                    if(key == "priority") {
                        advSearch << "A.PRIORITY in ('${searchValue}')"
                    } else if(key == "assignedTo") {
                        List groupIds = []
                        List userIds = []
                        searchValue.split("', '").each {
                            if (it.startsWith(Constants.USER_GROUP_TOKEN)) {
                                groupIds.add(it.replaceAll(Constants.USER_GROUP_TOKEN, '').toString())
                            }
                            else if (it.startsWith(Constants.USER_TOKEN)) {
                                userIds.add(it.replaceAll(Constants.USER_TOKEN, '').toString())
                            }
                        }
                        String userString = " ( "
                        if (groupIds.size() > 0)
                            userString += "A.ASSIGNED_TO_USERGROUP in (${groupIds.join(', ')})"
                        if (userIds.size() > 0) {
                            if (groupIds.size() > 0) userString += " OR "
                            userString += " A.ASSIGNED_TO_USER in (${userIds.join(', ')})"
                        }
                        userString += " ) "
                        advSearch << userString
                    } else if(key == "workflowState") {
                        advSearch << "A.WORKFLOW_STATE_ID in ('${searchValue}')"
                    } else if(key == "qualityIssueType") {
                        advSearch << "A.QUALITY_ISSUE_TYPE_ID in ('${searchValue}')"
                    } else if(key == "masterCaseNum") {
                        if (searchValue.contains(';') || searchValue.contains("'"))
                            advSearch << "A.CASE_NUM in ('${searchValue.replaceAll("'", "''").split(';').join("','")}')"
                        else
                            advSearch << "A.CASE_NUM LIKE '%${searchValue}%'"
                    }  else if(key == "masterVersionNum") {
                        advSearch << "A.VERSION_NUM = ${searchValue}"
                    } else if (val["dType"] == "NUMBER") {
                        advSearch << "A.METADATA.${key} = ${searchValue}"
                    } else if (val["dType"] == "TIMESTAMP") {
                        advSearch << "TO_DATE(SUBSTR(A.METADATA.${key},0,10),'YYYY-MM-DD') = '${searchValue}'"
                    } else if (key == "errorType"){
                        advSearch << "A.ERROR_TYPE IN ('${searchValue}') "
                    }
                    else if (key == "rootCause"){
                        if (type == PvqTypeEnum.CASE_QUALITY.name())
                            advSearch << "A.ID IN (select quality_case_id from quality_case_issue_details where quality_issue_detail_id in (select id from quality_issue_detail where root_cause_id in ('${searchValue}')))"
                        else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name())
                            advSearch << "A.ID IN (select quality_submission_id from quality_sub_issue_details where quality_issue_detail_id in (select id from quality_issue_detail where root_cause_id in ('${searchValue}')))"
                        else
                            advSearch << "A.ID IN (select quality_sampling_id from quality_sampl_issue_details where quality_issue_detail_id in (select id from quality_issue_detail where root_cause_id in ('${searchValue}')))"
                    } else if (key == "responsibleParty"){
                        if (type == PvqTypeEnum.CASE_QUALITY.name())
                            advSearch << "A.ID IN (select quality_case_id from quality_case_issue_details where quality_issue_detail_id in (select id from quality_issue_detail where responsible_party_id in ('${searchValue}')))"
                        else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name())
                            advSearch << "A.ID IN (select quality_submission_id from quality_sub_issue_details where quality_issue_detail_id in (select id from quality_issue_detail where responsible_party_id in ('${searchValue}')))"
                        else
                            advSearch << "A.ID IN (select quality_sampling_id from quality_sampl_issue_details where quality_issue_detail_id in (select id from quality_issue_detail where responsible_party_id in ('${searchValue}')))"
                    } else if (key == "workflowGroup") {
                        if (searchValue == 'final')
                            advSearch << "WFS.ID = A.WORKFLOW_STATE_ID AND WFS.FINAL_STATE = 1"
                        else if (searchValue == 'all')
                            advSearch << "WFS.ID = A.WORKFLOW_STATE_ID"
                    } else if (val["type"] == "rptField-select2") {
                        advSearch << "A.METADATA.${key} IN ('${searchValue}') "
                    } else {
                        List<String> searchConditions = []
                        searchValue.split(";").findAll { !it.trim().isEmpty()}.each {
                            searchConditions << "(UPPER(A.METADATA.${key}) like '%${it.trim().toUpperCase()}%')"
                        }
                        if (!searchConditions.isEmpty()) {
                            advSearch << "(".concat(searchConditions.join(" OR ")).concat(")")
                        }
                    }
                }

                if (key == "dateCreated") {
                    String fromDate = mapAdvanceSearch["dateCreated"]?.value1?.toString()?.trim()
                    String toDate = mapAdvanceSearch["dateCreated"]?.value2?.toString()?.trim()
                    if (fromDate && toDate) {
                        advSearch << "A.DATE_CREATED BETWEEN TO_DATE('${fromDate}', 'DD-Mon-YYYY') AND TO_DATE('${toDate} 23:59:59', 'DD-Mon-YYYY HH24:MI:SS')"
                    } else if (fromDate) {
                        advSearch << "A.DATE_CREATED >= TO_DATE('${fromDate}', 'DD-Mon-YYYY')"
                    } else if (toDate) {
                        advSearch << "A.DATE_CREATED <= TO_DATE('${toDate} 23:59:59', 'DD-Mon-YYYY HH24:MI:SS')"
                    }
                }
                if (key == "dueDate") {
                    String fromDate = mapAdvanceSearch["dueDate"]?.value1?.toString()?.trim()
                    String toDate = mapAdvanceSearch["dueDate"]?.value2?.toString()?.trim()
                    if (fromDate && toDate) {
                        advSearch << "DUE_DATE BETWEEN TO_DATE('${fromDate}', 'DD-Mon-YYYY') AND TO_DATE('${toDate} 23:59:59', 'DD-Mon-YYYY HH24:MI:SS')"
                    } else if (fromDate) {
                        advSearch << "DUE_DATE >= TO_DATE('${fromDate}', 'DD-Mon-YYYY')"
                    } else if (toDate) {
                        advSearch << "DUE_DATE <= TO_DATE('${toDate} 23:59:59', 'DD-Mon-YYYY HH24:MI:SS')"
                    }
                }
                if (val["dType"] == "DATE") {
                    // Handle the from and to date range for other date fields
                    String fromDate = mapAdvanceSearch[key]?.value1?.toString()?.trim()
                    String toDate = mapAdvanceSearch[key]?.value2?.toString()?.trim()
                    if (fromDate && toDate) {
                        advSearch << "TO_DATE(SUBSTR(A.METADATA.${key}, 0, 10), 'YYYY-MM-DD') BETWEEN TO_DATE('${fromDate}', 'DD-Mon-YYYY') AND TO_DATE('${toDate} 23:59:59', 'DD-Mon-YYYY HH24:MI:SS')"
                    } else if (fromDate) {
                        advSearch << "TO_DATE(SUBSTR(A.METADATA.${key}, 0, 10), 'YYYY-MM-DD') >= TO_DATE('${fromDate}', 'DD-Mon-YYYY')"
                    } else if (toDate) {
                        advSearch << "TO_DATE(SUBSTR(A.METADATA.${key}, 0, 10), 'YYYY-MM-DD') <= TO_DATE('${toDate} 23:59:59', 'DD-Mon-YYYY HH24:MI:SS')"
                    }
                }
            }
        }
        if(mapAdvanceSearch ==null || !mapAdvanceSearch?.containsKey("workflowGroup"))
            advSearch << "WFS.ID = A.WORKFLOW_STATE_ID AND WFS.FINAL_STATE = 0"
        String advSearchText = advSearch.join(" AND ")
        return advSearchText
    }

    Map getQualityDataList(Integer offset, Integer max, String sort,
                           String direction, String type,
                           boolean isChartDataRequired, Map externalSearch,
                           Map advanceFilter, Long tenantId, String viewType = "ICV",String assignedToFilter = null,def linkFilter=null) {
        Sql pvrsql
        try {
            pvrsql = new Sql(getReportConnectionForPVR())
            String tableName
            Class entityClass
            String typeCondition = ""
            String pvqTypeEnum = type
            String submissionExtraSearch = ""
            List<String> minColumnNameList = []
            if (type == PvqTypeEnum.CASE_QUALITY.name()) {
                tableName = "QUALITY_CASE_DATA"
                entityClass = QualityCaseData
                grailsApplication.config.qualityModule.qualityColumnList.each { minColumnNameList.add(it) }
            } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                entityClass = QualitySubmission
                tableName = "QUALITY_SUBMISSION"
                submissionExtraSearch = " A.SUBMISSION_IDENTIFIER, "
                grailsApplication.config.qualityModule.submissionColumnList.each { minColumnNameList.add(it) }
            } else {
                entityClass = QualitySampling
                tableName = "QUALITY_SAMPLING"
                typeCondition = " and TYPE = '${type}'"
                grailsApplication.config.qualityModule.additional.find { it.name == type }.columnList.each { minColumnNameList.add(it) }
            }
            minColumnNameList.add("errorType")
            minColumnNameList.add("priority")
            minColumnNameList.add("dateCreated")
            minColumnNameList.add("id")

            String strWhereCondition = " WHERE A.ISDELETED = 0 AND A.FIELD_NAME IS NULL AND A.TENANT_ID = ${tenantId} " + typeCondition
            String strExternalWhereCondition = getQualityDataExternalWhereStatement(externalSearch)
            String strTotalWhereCondition = "AND WFS.ID = A.WORKFLOW_STATE_ID AND WFS.FINAL_STATE = 0"

            if(advanceFilter !=null) {
                advanceFilter.each { key, val ->
                    String searchValue = val["value"]?.toString()?.trim();
                    if (key == "workflowGroup" && searchValue && searchValue != "") {
                        strTotalWhereCondition = "AND WFS.ID = A.WORKFLOW_STATE_ID"
                    }
                }
            }
            String linkWhereCondition = ""
            if (linkFilter) {
                linkFilter = new JsonSlurper().parseText(linkFilter)
                linkWhereCondition = " AND A.ID IN (" + linkFilter.join(",") + ")"
            }

            if(strExternalWhereCondition !="") {
                strWhereCondition = "${strWhereCondition} AND ${strExternalWhereCondition} "
            }
            String strAdvanceSearchCondition = getAdvanceSearchQry(advanceFilter, type);
            if(strAdvanceSearchCondition !="") {
                strWhereCondition += "AND ${strAdvanceSearchCondition}"
            }
            String strWhereConditionChart = strWhereCondition

            String errorTypeCondition = getQualityErrorTypeCondition(externalSearch)

            if (errorTypeCondition != "") {
                strWhereCondition = "${strWhereCondition} AND ${errorTypeCondition}"
            }
            User currentUser = userService.currentUser
            if (!assignedToFilter && currentUser && SpringSecurityUtils.ifAnyGranted("ROLE_USER_GROUP_RCA_PVQ")) {
                assignedToFilter = AssignedToFilterEnum.MY_GROUPS.name()
            }

            if(assignedToFilter && assignedToFilter!='null'){
                String assignedToFilterClause = addAssignedToFilterWhereClause(assignedToFilter)
                strWhereCondition += "${assignedToFilterClause}"
            }
            /*
                By default sorted by these 3 columns as we have to show cosolidated view which is
                always shown in group of case_num,version_num and within that group error_type in asc order
             */
            String orderBy = "ORDER BY A.CASE_NUM ASC, A.VERSION_NUM ASC, ${submissionExtraSearch} A.ERROR_TYPE ASC "
            if (sort && sort != "" && sort != "errorType") {
                if(sort == "state") {
                    orderBy = "ORDER BY WFS.NAME ${direction}, A.CASE_NUM ASC, A.VERSION_NUM ASC, A.ERROR_TYPE ASC "
                }else if(sort == "masterCaseNum") {
                    orderBy = "ORDER BY A.CASE_NUM ${direction}, A.VERSION_NUM ASC, ${submissionExtraSearch} A.ERROR_TYPE ASC "
                }else if(sort == "masterVersionNum") {
                    orderBy = "ORDER BY A.VERSION_NUM ${direction}, A.CASE_NUM ASC, A.VERSION_NUM ASC, ${submissionExtraSearch} A.ERROR_TYPE ASC "
                }else if(sort == "dueIn") {
                    orderBy = "ORDER BY TRUNC(A.DUE_DATE) ${direction}, A.CASE_NUM ASC, A.VERSION_NUM ASC, ${submissionExtraSearch} A.ERROR_TYPE ASC "
                }else if(sort == "dateCreated"){
                    orderBy = "ORDER BY TRUNC(A.DATE_CREATED) ${direction}, A.CASE_NUM ASC, A.VERSION_NUM ASC, ${submissionExtraSearch} A.ERROR_TYPE ASC"
                }
                else {
                    orderBy = "ORDER BY A.METADATA.${sort} ${direction}, A.CASE_NUM ASC, A.VERSION_NUM ASC, ${submissionExtraSearch} A.ERROR_TYPE ASC "
                }
            }

            List list = []
            Map<String, Map<Long, String>> allRcaDataMap = getAllRcaDataMap()
            String querystring = "SELECT A.ID, count(1) over(order by 1) as recordCount FROM ${tableName} A ,WORKFLOW_STATE WFS ${strWhereCondition} ${linkWhereCondition} ${orderBy} " +
                    "OFFSET ${offset} ROWS FETCH NEXT ${max} ROWS ONLY"

            List dbrecords = pvrsql.rows(querystring)
            int total = 0L
            if (dbrecords.size() > 0) {
                total = Long.parseLong(dbrecords[0]["recordCount"].toString());
            }
            List idList = dbrecords.collect { it["ID"] as Long }
            List records = entityClass.getAll(idList)
            Map<String, String> caseVersionMap =  ['caseNumData': '', 'versionData': '', 'submissionIdentifier': '']
            Map fieldTypeMap = getQualityReportAllFields(type).collectEntries { [(it.fieldName), it.fieldType] }
            String dateFormat = DateUtil.getLongDateFormatForLocale(userService.currentUser?.preference?.locale, true)
            records.each { qualityRow ->
                def record = formQualityRecordRow(qualityRow, minColumnNameList, pvqTypeEnum, allRcaDataMap, fieldTypeMap, dateFormat)
                caseVersionMap = consolidate(record, caseVersionMap)
                list.add(record)
            }
            if (isChartDataRequired) {
                List<Map> chartData = []
                String qryChart = "SELECT A.ERROR_TYPE,COUNT(1) as COUNT FROM ${tableName} A ,WORKFLOW_STATE WFS ${strWhereConditionChart} ${linkWhereCondition} GROUP BY A.ERROR_TYPE "
                pvrsql.rows(qryChart).each {
                    chartData << ["errorType": it['ERROR_TYPE'], "count": it['COUNT']]
                }
                chartData.sort { a, b -> b.count <=> a.count }
                return [aaData: list, recordsTotal: total, recordsFiltered: total, chartData: chartData]
            } else {
                return [aaData: list, recordsTotal: total, recordsFiltered: total]
            }
        } catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        } finally {
            pvrsql?.close()
        }
    }

    //Assigned to filter handling for ROLE_USER_GROUP_RCA_PVQ role
    String addAssignedToFilterWhereClause(String assignedToFilter){
        if (assignedToFilter == null || assignedToFilter.isEmpty()) {
            return null
        }
        StringBuilder whereClause = new StringBuilder()
        whereClause.append(" AND ")
        switch(assignedToFilter) {
            case AssignedToFilterEnum.ME.name():
                Long userId = userService.currentUser.id
                whereClause.append("A.ASSIGNED_TO_USER = " + userId + " ")
                break
            case AssignedToFilterEnum.MY_GROUPS.name():
                Long userId = userService.currentUser.id
                List<UserGroup> userGroups = UserGroup.fetchAllUserGroupByUser(userService.currentUser)
                StringBuilder assignedToUserGroupClause = new StringBuilder()
                StringBuilder assignedToUserClause = new StringBuilder()
                assignedToUserGroupClause.append("(A.ASSIGNED_TO_USERGROUP IN (0")
                assignedToUserClause.append(") OR (A.ASSIGNED_TO_USERGROUP is Null and A.ASSIGNED_TO_USER IN (")
                Set<Long> ids = [0L]
                boolean userHasRole = userService.currentUser.authorities.any { it.authority == 'ROLE_USER_GROUP_RCA_PVQ' }
                userGroups.eachWithIndex { val, idx ->
                    if(!userHasRole || val.authorities.authority.contains("ROLE_USER_GROUP_RCA_PVQ")) {
                        val.getUsers().collect {
                            ids.add(it.id)
                        }
                        assignedToUserGroupClause.append("," + val.id + "")
                    }
                }
                List<List<Long>> batches = ids.collate(999)
                batches.eachWithIndex { it, index ->
                    assignedToUserClause.append(String.join(",", it.collect{String.valueOf(it)}))
                    if(index != batches.size() - 1) {
                        assignedToUserClause.append(") OR A.ASSIGNED_TO_USER IN (")
                    }
                }
                whereClause.append(assignedToUserGroupClause.toString())
                whereClause.append(assignedToUserClause.toString())
                whereClause.append("))) ")
                break
        }
        if(assignedToFilter.contains(Constants.USER_TOKEN)) {
            whereClause.append("A.ASSIGNED_TO_USER = " + assignedToFilter.split("_")[1] + " ")
        }else if(assignedToFilter.contains(Constants.USER_GROUP_TOKEN)) {
            String userGroupId = assignedToFilter.split("_")[1]
            String assignedToUserClause = String.join(",", UserGroup.get(Long.valueOf(userGroupId)).getUsers().collect{String.valueOf(it.id)})
            whereClause.append("(A.ASSIGNED_TO_USERGROUP = " + userGroupId + " ")
            if(assignedToUserClause) whereClause.append("OR (A.ASSIGNED_TO_USERGROUP is Null and A.ASSIGNED_TO_USER IN ("+assignedToUserClause+")))") else whereClause.append(")")
        }
        whereClause.toString()
    }


    Map formQualityRecordRow(def qualityRow, List minColumnNameList, String qualityRecordType, Map allRcaDataMap, Map typeMap, String dateFormat) {

        Map record = new JsonSlurper().parseText(qualityRow.metadata)
        Map row = minColumnNameList.collectEntries { [(it): ''] }
        def executedTemplateId = qualityRow.executedTemplateId
        List renames = getRenameValueOfReportField(null, executedTemplateId as Long)
        String additionalData = ""
        record.eachWithIndex { entry, ind ->
            String key = entry.getKey(), value = entry.getValue()
            key = (key.contains("__")) ? key.replaceAll("__\\d+", "") : key
            // if multiple report fields are used multiple times in template and that report field is present in minColumnNameList -> it should be added in additional data
            if (row.get(key) != null && row.get(key) == '') {
                row << [(key): value]
            } else {
                if (additionalData != "") {
                    additionalData += "!@@\n##!"
                }
                String val = (value ?: "")

                if (val && typeMap[key] == "DATE") val = DateUtil.parseDate(val, DateUtil.ISO_DATE_TIME_FORMAT)?.format(dateFormat ?: DateUtil.DATEPICKER_FORMAT_AM_PM)
                additionalData += renames.getAt(ind) + ":" + (val ?: "")

            }
        }

        Map<String, String> rcaDataMap = new HashMap<>()
        rcaDataMap.put("LATE", ViewHelper.getEmptyLabel())
        rcaDataMap.put("LATE_TYPE", ViewHelper.getEmptyLabel())
        rcaDataMap.put("ROOT_CAUSE", ViewHelper.getEmptyLabel())
        rcaDataMap.put("RESPONSIBLE_PARTY", ViewHelper.getEmptyLabel())
        if (allRcaDataMap) {

            if (qualityRow.qualityIssueTypeId && allRcaDataMap['LATE_MAP'][qualityRow.qualityIssueTypeId]) {
                rcaDataMap.put("LATE", allRcaDataMap['LATE_MAP'][qualityRow.qualityIssueTypeId])
                rcaDataMap.put("LATE_TYPE", allRcaDataMap['LATE_TYPE_MAP'][qualityRow.qualityIssueTypeId])
                Set<QualityIssueDetail> qualityIssuesDetails = qualityRow?.qualityIssueDetails?.findAll { it.isPrimary }
                if (qualityIssuesDetails?.size() > 0) {
                    for (QualityIssueDetail qualityIssuesDetail : qualityIssuesDetails) {
                        if (qualityIssuesDetail.isPrimary) {
                            if (allRcaDataMap['ROOT_CAUSE_MAP'][qualityIssuesDetail.rootCauseId]) {
                                rcaDataMap.put("ROOT_CAUSE", allRcaDataMap['ROOT_CAUSE_MAP'][qualityIssuesDetail.rootCauseId])
                                if (allRcaDataMap['RESPONSIBLE_PARTY_MAP'][qualityIssuesDetail.responsiblePartyId]) {
                                    rcaDataMap.put("RESPONSIBLE_PARTY", allRcaDataMap['RESPONSIBLE_PARTY_MAP'][qualityIssuesDetail.responsiblePartyId])
                                }
                            }
                        }
                    }
                }
            }
        }

        boolean waiting = false
        String status = null
        Collection<ActionItem> aiList = qualityRow.actionItems.findAll{!it.isDeleted }
        if (aiList?.size() > 0) {
            aiList?.each {
                if (it.status != StatusEnum.CLOSED) {
                    waiting = true
                    if (it.dueDate < new Date()) {
                        status = ActionItemGroupState.OVERDUE.toString()
                    }
                }
            }
            status = status ? status : (waiting ? ActionItemGroupState.WAITING.toString() : ActionItemGroupState.CLOSED.toString())
        }
        row << ["caseNumber": qualityRow.caseNumber]
        row << ["errorType": qualityRow.errorType]
        row << ["priority": qualityRow.priority ?: '-1']
        row << ["submissionIdentifier": qualityRow.hasProperty("submissionIdentifier")? qualityRow.submissionIdentifier : '-']
        row << ["additionalDetails": additionalData]
        row << ["assignedToUser": qualityRow.assignedToUser?.fullName ?: qualityRow.assignedToUser?.username]
        row << ["assignedToUserId": qualityRow.assignedToUser ? Constants.USER_TOKEN + qualityRow.assignedToUser?.id : ""]
        row << ["assignedToUserGroup": qualityRow.assignedToUserGroup?.name]
        row << ["assignedToUserGroupId": qualityRow.assignedToUserGroup ? Constants.USER_GROUP_TOKEN + qualityRow.assignedToUserGroup?.id : ""]
        row << ["reportId": qualityRow.reportId]
        row << ["actionItemStatus": status]
        row << ["id": qualityRow.id]
        row << ["entryType": qualityRow.entryType]
        row << ["executedReportId": qualityRow.executedReportId]
        row << ["dateCreated": qualityRow.dateCreated?.format(DateUtil.DATEPICKER_FORMAT)]
        row << ["state": qualityRow.workflowState?.name]
        row << ["isFinalState": qualityRow.workflowState?.finalState]
        row << ["qualityIssueType": rcaDataMap['LATE']]
        row << ["rootCause": rcaDataMap['ROOT_CAUSE']]
        row << ["responsibleParty": rcaDataMap['RESPONSIBLE_PARTY']]
        row << ["dataType": qualityRecordType]
        row << ["dueIn": qualityRow.dueDate]
        row << ["indicator": getIndicator(qualityRow.dueDate)]
        row << ["comment": qualityRow.comments?.max { it.id }?.textData]
        row << ["qualityIssueTypeId": qualityRow.qualityIssueTypeId]
        row << ["hasIssues": qualityRow.issues?.find { !it.isDeleted }? "true": "false"]
        Map issueTypeDetailMap = new HashMap<>()
        issueTypeDetailMap['qualityIssueType'] = rcaDataMap['LATE']
        issueTypeDetailMap['rootCause'] = rcaDataMap['ROOT_CAUSE']
        issueTypeDetailMap['responsibleParty'] = rcaDataMap['RESPONSIBLE_PARTY']
        row << ['issueTypeDetail': issueTypeDetailMap]
        row << ['qualityMonitoringType': getLabelForType(qualityRecordType)]
        row << ['qualityIssueNotError': (rcaDataMap['LATE_TYPE'] == ReasonOfDelayLateTypeEnum.NOT_ERROR.name())]
        return row
    }

    List <User> getUserList(List assignedToUserList, Sql sql){
        String stringIds = assignedToUserList.collect {"${it}"}.join(",")
        List <User> userList = []
        try {
            sql.rows("SELECT ID, FULLNAME, USERNAME FROM PVUSER WHERE ID IN (${stringIds})", []).collect {
                User user = new User(username: it.USERNAME, fullName: it.FULLNAME)
                user.setId(Long.parseLong(it.ID.toString()))
                userList.add(user)
            }
        } catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        }
        return userList
    }


    List <UserGroup> getUserGroupList(List assignedToUserGroupList, Sql sql){
        List <UserGroup> userGroupList = []
        String stringIds = assignedToUserGroupList.collect {"${it}"}.join(",")
        try {
            sql.rows("SELECT ID, NAME FROM USER_GROUP A WHERE ID IN (${stringIds})",[]).collect {
                UserGroup userGroup = new UserGroup(name: it.NAME)
                userGroup.setId(Long.parseLong(it.ID.toString()))
                userGroupList.add(userGroup)
            }
        } catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        }
        return userGroupList
    }

    Map<Long,String> getCommentsByQualityIds(List idsArray, String dataType, Sql sql ){
        Map idCommentsMap = [:]
        try {
            String tableName
            String idColumnName
            if (dataType == PvqTypeEnum.CASE_QUALITY.name()) {
                tableName = "QUALITY_CASE_COMMENTS"
                idColumnName = "QUALITY_CASE_ID"
            } else if (dataType == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                tableName = "QUALITY_SUBMISSION_COMMENTS"
                idColumnName = "QUALITY_SUBMISSION_ID"
            } else {
                tableName = "QUALITY_SAMPLING_COMMENTS"
                idColumnName = "QUALITY_SAMPLING_ID"
            }
            String stringIds = idsArray.collect {"${it}"}.join(",")
            sql.rows(
                    "SELECT ${idColumnName} as id, comment_id, note from ${tableName} " +
                            "left JOIN comment_table on comment_id = comment_table.id " +
                            "WHERE ${tableName}.${idColumnName} IN (${stringIds}) order by ${idColumnName}, comment_id asc",[]).collect {
                idCommentsMap.put(it.id,it.note)
            }

        } catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        }
        return idCommentsMap
    }

    Map<Long,List> getActionItemByQualityIds(List idsArray, String dataType, Sql sql ){
        Map idActionItemMap = [:]
        Long id = null
        List actionItemList = []
        try {
            String tableName
            String idColumnName
            if (dataType == PvqTypeEnum.CASE_QUALITY.name()) {
                tableName = "QUALITY_CASE_ACTION_ITEMS"
                idColumnName = "QUALITY_CASE_ID"
            } else if (dataType == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                tableName = "QUALITY_SUBMISSION_ACTION_ITEM"
                idColumnName = "QUALITY_SUBMISSION_ID"
            } else {
                tableName = "QUALITY_SAMPLING_ACTION_ITEMS"
                idColumnName = "QUALITY_SAMPLING_ID"
            }
            String stringIds = idsArray.collect {"${it}"}.join(",")
            sql.rows(
                    "SELECT ${idColumnName} as id, action_item_id, status, CAST(due_date AS DATE) as due_date from ${tableName} " +
                            "left JOIN action_item on ${tableName}.action_item_id = action_item.id " +
                            "WHERE ${tableName}.${idColumnName} IN (${stringIds}) and action_item.is_deleted= 0 order by ${idColumnName}, action_item_id asc",[]).collect {

                List actionItem = [it.action_item_id, it.status, it.due_date]
                if (id != it.id)
                    actionItemList = [actionItem]
                else if (id == it.id)
                    actionItemList.add(actionItem)
                idActionItemMap.put(it.id,actionItemList)
                id = it.id
            }

        } catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        }
        return idActionItemMap
    }

    Map<Long,List> getStateByQualityIds(List idsArray, String dataType, Sql sql ){
        Map idStatesMap = [:]
        try {
            String tableName
            String idColumnName
            if (dataType == PvqTypeEnum.CASE_QUALITY.name()) {
                tableName = "QUALITY_CASE_DATA"
            } else if (dataType == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                tableName = "QUALITY_SUBMISSION"
            } else {
                tableName = "QUALITY_SAMPLING"
            }
            String stringIds = idsArray.collect {"${it}"}.join(",")
            sql.rows(
                    "SELECT ${tableName}.id as id, name, final_state from ${tableName} " +
                            "left JOIN workflow_state on ${tableName}.workflow_state_id = workflow_state.id " +
                            "WHERE ${tableName}.id IN (${stringIds}) order by ${tableName}.id",[]).collect {
                List stateNameMap = [it.name, it.final_state]
                idStatesMap.put(it.id,stateNameMap)
            }

        } catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        }
        return idStatesMap
    }

    List getCommentStateActionItemMapList(List idList, String type){
        List <Map> commentStateActionItemMapList = []
        Sql pvrsql
        try {
            pvrsql = new Sql(getReportConnectionForPVR())
                commentStateActionItemMapList.add(getCommentsByQualityIds(idList, type, pvrsql))
                commentStateActionItemMapList.add(getStateByQualityIds(idList, type, pvrsql))
                commentStateActionItemMapList.add(getActionItemByQualityIds(idList, type, pvrsql))
        }
        catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        } finally {
            pvrsql?.close()
        }
        return commentStateActionItemMapList
    }

    private Map<String,String> consolidate(def record, Map caseVersionMap){
        String caseNumData = caseVersionMap['caseNumData']
        String versionData = caseVersionMap['versionData']
        String submissionIdentifierData = caseVersionMap['submissionIdentifier']
        String caseNum = record['caseNumber']
        String version = record['masterVersionNum']
        String submissionIdentifier = record['submissionIdentifier']
        if(!caseNum) {
            caseNum = ''
        }
        if(!version) {
            version = ''
        }
        if(!submissionIdentifier) {
            submissionIdentifier = ''
        }
        String dataToShow = caseNum.trim()
        String versionToShow = version.trim()
        String submissionIdentifierToShow = submissionIdentifier.trim()
        //Except First row , case No and version in other rows for every group of same case num and same version will be blank
        if(caseNumData != ''){
            if(caseNum.trim() == caseNumData && version.trim() == versionData && submissionIdentifier.trim() == submissionIdentifierData) {
                dataToShow = ''
                versionToShow = ''
                submissionIdentifierToShow = ''
            }
        }
        caseVersionMap['caseNumData'] = caseNum.trim()
        caseVersionMap['versionData'] = version.trim()
        caseVersionMap['submissionIdentifier'] = submissionIdentifier.trim()
        record['consolidatedCaseNum'] = dataToShow
        record['consolidatedVersion'] = versionToShow
        record['consolidatedSubmissionIdentifier'] = submissionIdentifierToShow
        return caseVersionMap
    }

    def getRenameValueOfReportField(String reportFieldName, Long executedTemplateId){
        def renames = []
        if(executedTemplateId!=null) {
            def columnLists = ReportTemplate.get(executedTemplateId)?.columnList?.reportFieldInfoList
            def groupingList = ReportTemplate.get(executedTemplateId)?.groupingList?.reportFieldInfoList
            def rowColumnList = ReportTemplate.get(executedTemplateId)?.rowColumnList?.reportFieldInfoList
            def serviceColumnList = ReportTemplate.get(executedTemplateId)?.serviceColumnList?.reportFieldInfoList
            def commonlist = columnLists + groupingList + rowColumnList + serviceColumnList
            for (rptfield in commonlist) {
                if (rptfield != null) {
                    def name = rptfield.renameValue ?: ViewHelper.getMessage('app.reportField.' + rptfield.reportField.name)
                    if (reportFieldName == null) renames.add(name)
                    else if (reportFieldName == rptfield.reportField.name) return name
                }
            }
        }
        return (reportFieldName == null) ? renames : ViewHelper.getMessage('app.reportField.' + reportFieldName)
    }


    Map parseQualityRecord(def dbrow, List minColumnNameList, String qualityRecordType, Map<String,Map<Long,String>> allRcaDataMap, Map idCommentsMap=[:], Map idStateMap=[:], Map idActionItemMap=[:], List <User> userList, List <UserGroup> userGroupList) {
        def metadata=dbrow['METADATA']
        User assignToUser = userList.find{it.id == dbrow['ASSIGNED_TO_USER']}
        UserGroup assignToUserGroup = userGroupList.find{it.id == dbrow['ASSIGNED_TO_USERGROUP']}
        def rtext = metadata instanceof  String ? metadata : metadata?.characterStream?.text
        Map record = new JsonSlurper().parseText(rtext)
        Map row = minColumnNameList.collectEntries { [(it): ''] }
        def executedTemplateId = dbrow['EXECUTED_TEMPLATE_ID']

        String additionalData = ""
        WorkflowRule workflowRule = null
        List<Map> workflowJustificationList = []
        def qualityDataObj = null
        if (qualityRecordType.equals(PvqTypeEnum.CASE_QUALITY.toString())) {
            qualityDataObj = QualityCaseData.get(Long.valueOf(dbrow['ID'].toString()))
            workflowJustificationList= WorkflowJustification.findAllByQualityCaseData(qualityDataObj, [sort: 'dateCreated', order: 'desc'])
            if(workflowJustificationList){
                workflowRule = workflowJustificationList.get(0).workflowRule
            }else{
                workflowRule = WorkflowRule.getDefaultWorkFlowRuleByType(com.rxlogix.enums.WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA)
            }
        } else if (qualityRecordType.equals(PvqTypeEnum.SUBMISSION_QUALITY.toString())) {
            qualityDataObj = QualitySubmission.get(Long.valueOf(dbrow['ID'].toString()))
            workflowJustificationList= WorkflowJustification.findAllByQualitySubmission(qualityDataObj, [sort: 'dateCreated', order: 'desc'])
            if(workflowJustificationList){
                workflowRule = workflowJustificationList.get(0).workflowRule
            }else{
                workflowRule = WorkflowRule.getDefaultWorkFlowRuleByType(com.rxlogix.enums.WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION)
            }
        } else {
            qualityDataObj = QualitySampling.get(Long.valueOf(dbrow['ID'].toString()))
            workflowJustificationList= WorkflowJustification.findAllByQualitySampling(qualityDataObj, [sort: 'dateCreated', order: 'desc'])
            if(workflowJustificationList){
                workflowRule = workflowJustificationList.get(0).workflowRule
            }else{
                workflowRule = WorkflowRule.getDefaultWorkFlowRuleByType(com.rxlogix.enums.WorkflowConfigurationTypeEnum.QUALITY_SAMPLING)
            }
        }
        String workflowUpdatedDate = new SimpleDateFormat("dd-MMM-yyyy").format(dbrow['WORKFLOW_STATE_UPDATED_DATE'])
        Date dueInDate = new Date()
        if(workflowRule?.excludeWeekends){
            dueInDate = com.rxlogix.util.DateUtil.addDaysSkippingWeekends(new Date(workflowUpdatedDate), workflowRule?.dueInDays ?: 0)
        }else{
            dueInDate = new Date(workflowUpdatedDate).plus(workflowRule?.dueInDays ?: 0)
        }
        record.each { key, value ->
            if (row.get(key) != null) {
                row << [(key): value]
            } else {
                if (additionalData != "") {
                    additionalData += "!@@\n##!"
                }
                key = key.toString()
                def reportFieldName = getRenameValueOfReportField(key, executedTemplateId as Long)
                additionalData += reportFieldName + ":" + (value?:"")
            }
        }



        String hasIssues = "false"
        if(qualityDataObj.issues != null && qualityDataObj.issues.size()> 0){
            qualityDataObj.issues.each{
                if(it.isDeleted == false)
                    hasIssues = "true"
            }
        }
        Map rcaMap = getRCADataForQualityRecord(Long.valueOf(dbrow['ID'].toString()), qualityRecordType, allRcaDataMap)
        String workflowStateName = idStateMap.find {it.key == Long.valueOf(dbrow['ID'].toString())}?.getValue()[0]
        Boolean isFinalState = idStateMap.find {it.key == Long.valueOf(dbrow['ID'].toString())}?.getValue()[1]
        List actionItems = idActionItemMap.find {it.key == Long.valueOf(dbrow['ID'].toString())}?.getValue()
        boolean waiting = false
        String status = null
        if (actionItems?.size() > 0) {
            for(int i=0; actionItems[i]; i++){
                if (actionItems[i][1] != StatusEnum.CLOSED.toString()) {
                    waiting = true
                    if (actionItems[i][2] < new Date()) {
                        status = ActionItemGroupState.OVERDUE.toString()
                    }
                }
            }
            status = status ? status : (waiting ? ActionItemGroupState.WAITING.toString() : ActionItemGroupState.CLOSED.toString())
        }
        row << ["caseNumber" : dbrow['CASE_NUM']]
        row << ["errorType" : dbrow['ERROR_TYPE']]
        row << ["priority" : (dbrow['PRIORITY'] != null) ? dbrow['PRIORITY'] : '-1']
        row << ["additionalDetails": additionalData]
        if(assignToUser){
            row << ["assignedTo" : assignToUser?.fullName ?: assignToUser?.username]
            row << ["assignedToId" : Constants.USER_TOKEN + dbrow['ASSIGNED_TO_USER']]
        }else if(assignToUserGroup){
            row << ["assignedTo" : assignToUserGroup?.name]
            row << ["assignedToId" : Constants.USER_GROUP_TOKEN + dbrow['ASSIGNED_TO_USERGROUP']]
        }
        row << ["reportId": dbrow['REPORT_ID']]
        row << ["actionItemStatus": status]
        row << ["id" : dbrow['ID']]
        row << ["entryType" : dbrow['ENTRY_TYPE']]
        row << ["executedReportId" : dbrow['EXEC_REPORT_ID']]
        row << ["dateCreated" : dbrow['DATE_CREATED'].toString()]
        row << ["state": workflowStateName]
        row << ["isFinalState" : isFinalState]
        row << ["qualityIssueType" : rcaMap['LATE']]
        row << ["rootCause" : rcaMap['ROOT_CAUSE']]
        row << ["responsibleParty" : rcaMap['RESPONSIBLE_PARTY']]
        row << ["dataType" : qualityRecordType]
        row << ["dueIn" : dueInDate]
        row << ["indicator" : getIndicator(dueInDate)]
        row << ["comment" : idCommentsMap.find {it.key == Long.valueOf(dbrow['ID'].toString())}?.getValue()]
        row << ["qualityIssueTypeId" : (dbrow['QUALITY_ISSUE_TYPE_ID'] != null) ? Long.parseLong(dbrow['QUALITY_ISSUE_TYPE_ID'].toString()) : dbrow['QUALITY_ISSUE_TYPE_ID']]
        row << ["hasIssues": hasIssues]
        Map issueTypeDetailMap = new HashMap<>()
        issueTypeDetailMap['qualityIssueType'] = rcaMap['LATE']
        issueTypeDetailMap['rootCause'] = rcaMap['ROOT_CAUSE']
        issueTypeDetailMap['responsibleParty'] = rcaMap['RESPONSIBLE_PARTY']
        row << ['issueTypeDetail':issueTypeDetailMap]
        row << ['qualityMonitoringType': getLabelForType(qualityRecordType)]
        row << ['qualityIssueNotError': (rcaMap['LATE_TYPE'] == ReasonOfDelayLateTypeEnum.NOT_ERROR.name())]     //Will be true if issueType is Not_Error
        return row
    }

    private String getIndicator(Date dueInDate) {
        Date now = new Date();
        Date soon = now + 2;
        if (dueInDate > now && dueInDate < soon) return "yellow"
        if (dueInDate < now) return "red"
        return ""
    }

    Map parseQualityRecord(def dbrow, String qualityRecordType, Map<String,Map<Long,String>> allRcaDataMap) {
        def metadata=dbrow['METADATA']
        User assignToUser = User.findById(dbrow['ASSIGNED_TO_USER'])
        UserGroup assignToUserGroup = UserGroup.findById(dbrow['ASSIGNED_TO_USERGROUP'])
        def executedTemplateId = dbrow['EXECUTED_TEMPLATE_ID']
        def rtext = metadata?.characterStream?.text
        Map record = new JsonSlurper().parseText(rtext)
        WorkflowState workflowState = WorkflowState.findById(dbrow['WORKFLOW_STATE_ID'])
        Map rcaMap = getRCADataForQualityRecord(Long.valueOf(dbrow['ID'].toString()), qualityRecordType, allRcaDataMap)
        record << ["errorType" : dbrow['ERROR_TYPE']]
        record << ["priority": (dbrow['PRIORITY'] != null) ? dbrow['PRIORITY'] : '']
        record << ["assignedToUser": assignToUser?.fullName ?: assignToUser?.username]
        record << ["assignedToUserId": dbrow['ASSIGNED_TO_USER'] ? Constants.USER_TOKEN + dbrow['ASSIGNED_TO_USER'] : ""]
        record << ["assignedToUserGroup": assignToUserGroup?.name]
        record << ["assignedToUserGroupId": dbrow['ASSIGNED_TO_USERGROUP'] ? Constants.USER_GROUP_TOKEN + dbrow['ASSIGNED_TO_USERGROUP'] : ""]
        record << ["qualityIssueType": rcaMap['LATE']]
        record << ["rootCause" : rcaMap['ROOT_CAUSE']]
        record << ["responsibleParty" : rcaMap['RESPONSIBLE_PARTY']]
        record << ["state" : workflowState?.name]
        record << ["dueIn": dbrow['DUE_DATE']?.format(DateUtil.DATEPICKER_FORMAT) ?: ""]
        record << ["dateCreated": (new SimpleDateFormat("dd-MMM-yyyy").format(dbrow['DATE_CREATED']))]
        record << ["latestComment" : getCommentsForQualityRecord(Long.valueOf(dbrow['ID'].toString()), qualityRecordType)]
        record << ["qualityIssueTypeId" : dbrow['QUALITY_ISSUE_TYPE_ID']]
        record << ['qualityMonitoringType': getLabelForType(qualityRecordType)]
        record << ['executedTemplateId' : executedTemplateId]
        return record
    }

    List getQualityDataEntityListSearch(String type, Map externalSearch, Map advanceFilter, Long tenantId, Boolean idsOnly = false) {
        Sql pvrsql
        try {
            pvrsql=new Sql(getReportConnectionForPVR())
            String tableName
            String pvqTypeEnum = type.toString()
            String typeCondition = ""
            if (type == PvqTypeEnum.CASE_QUALITY.name()) {
                tableName = "QUALITY_CASE_DATA"
            } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                tableName = "QUALITY_SUBMISSION"
            } else {
                tableName = "QUALITY_SAMPLING"
                typeCondition = " AND TYPE ='"+type+"' "
            }

            String strWhereCondition = " WHERE ISDELETED = 0 AND A.FIELD_NAME IS NULL AND TENANT_ID = ${tenantId} " +typeCondition
            String strExternalWhereCondition = getQualityDataExternalWhereStatement(externalSearch)
            if(strExternalWhereCondition !="") {
                strWhereCondition = "${strWhereCondition} AND ${strExternalWhereCondition} "
            }
            String strAdvanceSearchCondition = getAdvanceSearchQry(advanceFilter, type);
            if(strAdvanceSearchCondition !="") {
                strWhereCondition += "AND ${strAdvanceSearchCondition}"
            }

            String errorTypeCondition = getQualityErrorTypeCondition(externalSearch)
            //errorTypeCondition =""
            if (errorTypeCondition != "") {
                strWhereCondition = "${strWhereCondition} AND ${errorTypeCondition}"
            }

            String querystring = "SELECT A.CASE_NUM,A.ERROR_TYPE,A.PRIORITY,A.METADATA,A.REPORT_ID, CAST(A.DATE_CREATED AS DATE) as DATE_CREATED,CAST(A.WORKFLOW_STATE_UPDATED_DATE AS DATE) as WORKFLOW_STATE_UPDATED_DATE, CAST(A.DUE_DATE AS DATE) DUE_DATE,A.ASSIGNED_TO_USER,A.ASSIGNED_TO_USERGROUP,A.ID,A.ENTRY_TYPE,A.EXEC_REPORT_ID, A.WORKFLOW_STATE_ID, QUALITY_ISSUE_TYPE_ID,A.EXECUTED_TEMPLATE_ID FROM ${tableName} A ,WORKFLOW_STATE WFS ${strWhereCondition} ORDER BY A.CASE_NUM ASC, A.VERSION_NUM ASC, A.ERROR_TYPE ASC "

            List list = []
            Map<String,Map<Long,String>> allRcaDataMap = getAllRcaDataMap()
            List dbrecords = pvrsql.rows(querystring)
            dbrecords.each {
                if (idsOnly) {
                    list.add(it["ID"] as Long)
                } else {
                    def record = parseQualityRecord(it, pvqTypeEnum, allRcaDataMap)
                    list.add(record)
                }
            }
            return list;
        } catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        } finally {
            pvrsql?.close()
        }
    }

    List getQualityDataByIds(List ids, String type, Long tenantId) {
        List<List<String>> splitIds = getIdsListSplit(ids)
        List<Map> list =[];
        Sql pvrsql
        try {
            pvrsql=new Sql(getReportConnectionForPVR())
            String tableName = ""
            if (type == PvqTypeEnum.CASE_QUALITY.name()) {
                tableName = "QUALITY_CASE_DATA"
            } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                tableName = "QUALITY_SUBMISSION"
            } else {
                tableName = "QUALITY_SAMPLING"
           }
            Map<String,Map<Long,String>> allRcaDataMap = getAllRcaDataMap()
            String querystringbase = "SELECT A.CASE_NUM,A.ERROR_TYPE,A.PRIORITY,CAST(A.DATE_CREATED AS DATE) as DATE_CREATED, CAST(A.WORKFLOW_STATE_UPDATED_DATE AS DATE) as WORKFLOW_STATE_UPDATED_DATE, CAST(A.DUE_DATE AS DATE) DUE_DATE , A.METADATA,A.REPORT_ID,A.ASSIGNED_TO_USER,A.ASSIGNED_TO_USERGROUP,A.ID,A.ENTRY_TYPE,A.EXEC_REPORT_ID, A.WORKFLOW_STATE_ID, A.EXECUTED_TEMPLATE_ID, QUALITY_ISSUE_TYPE_ID  FROM ${tableName} A WHERE ISDELETED = 0 AND TENANT_ID = ${tenantId} "
            for (int i = 0; i < splitIds.size(); i++) {
                String recIdIn = splitIds[i].join(",")
                String querystring  = "${querystringbase} AND ID IN (${recIdIn}) ORDER BY A.CASE_NUM ASC, A.VERSION_NUM ASC, A.ERROR_TYPE ASC "
                List dbrecords = pvrsql.rows(querystring)
                dbrecords.each {
                    def record = parseQualityRecord(it, type, allRcaDataMap)
                    list.add(record)
                }
            }

            return list
        } catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        } finally {
            pvrsql?.close()
        }
    }

    private List getIdsListSplit(List ids) {
        List<List<String>> idslists =[[]];
        if(ids.size() >0) {
            int length = 1000
            int bocksize = Math.ceil(ids.size()/length)
            for(int i = 0; i< bocksize ; i++ ) {
                idslists[i] = []
                int itmlength = ((i+1) * length) < ids.size() ? ((i+1) * length) : ids.size()
                for(int j=i*length; j< itmlength; j++) {
                    idslists[i] << ids[j];
                }
            }
        }
        return idslists
    }

    List<Map> getQualityReportAllFields(String type) {
        List fields = []
        List<QualityField> dbFields = QualityField.findAllByQualityModule(type)
        dbFields.each {
            Map fieldMap = [fieldName: it.fieldName, fieldType: it.fieldType, fieldLabel: it.label ?: ViewHelper.getMessage('app.reportField.' + it.fieldName), selectable: (it.isSelectable && ReportField.findByName(it.fieldName)?.lmSQL) ? true : false]
            if (!fields.contains(fieldMap)) {
                fields << fieldMap
            }
        }
        return fields
    }

    List<String> getQualityErrorTypes(String type, Long tenantId) {
        Sql pvrsql
        List<String> list = []
        try {
            String typeCondition = ""
            pvrsql = new Sql(getReportConnectionForPVR())
            String tableName = ""
            if (type == PvqTypeEnum.CASE_QUALITY.name()) {
                tableName = "QUALITY_CASE_DATA"
            } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                tableName = "QUALITY_SUBMISSION"
            } else {
                tableName = "QUALITY_SAMPLING"
                typeCondition = " and TYPE = '${type}'"
            }
            String querystring = "SELECT DISTINCT A.ERROR_TYPE FROM ${tableName} A WHERE ISDELETED = 0 AND TENANT_ID = ${tenantId}" + typeCondition
            List dbrecords = pvrsql.rows(querystring)
            dbrecords.each {
                list << it[0]
            }
            return list
        } catch (Exception e) {
            log.error("Error Occured During Fetching Errortype list -- ${e.getMessage()}")
            throw e
        } finally {
            pvrsql?.close()
        }
    }

    void qualityFieldsSave(Long reportId, Long executedReportId, List<QualityField> rptfields, String type) {
        List<QualityField> dbfields = QualityField.findAllByQualityModule(type)


        rptfields.each {QualityField qf ->
            qf.qualityModule=type
            QualityField existingQF = dbfields?.find {it.fieldName == qf.fieldName}
            if(!existingQF) {
                qf.addToReportIds(reportId)
                qf.execReportId= executedReportId
                CRUDService.saveWithoutAuditLog(qf)
            } else {
                Sql pvrSql = new Sql(getReportConnectionForPVR())
                boolean reportIdExists = false
                try {
                    def qualityFieldId = existingQF.id
                    def query = "SELECT 1 FROM QUALITY_FIELD_REPORT WHERE QUALITY_FIELD_ID = :qualityFieldId AND REPORT_ID = :reportId"
                    def result = pvrSql.firstRow(query, [qualityFieldId: qualityFieldId, reportId: reportId])

                    reportIdExists = (result != null)
                } catch (SQLException ex) {
                    log.error("Error while checking for reportId(${reportId}) in QUALITY_FIELD_REPORT table for field: ${existingQF.fieldName} - ${existingQF.id} with executedReportId: ${executedReportId}", ex)
                } finally {
                    pvrSql.close()
                }

                if (!reportIdExists) {
                    existingQF.addToReportIds(reportId)
                }
                existingQF.label = qf.label
                existingQF.isSelectable = qf.isSelectable
                existingQF.execReportId = executedReportId
                CRUDService.updateWithoutAuditLog(existingQF)
                dbfields.remove(existingQF)
            }
        }


        dbfields.each {
            if(executedReportId != it.execReportId) {
                int index = it.reportIds.findIndexOf { it == reportId }
                if (index > -1) {
                    it.reportIds.remove(index)
                    if (it.reportIds.size() == 0) {
                        CRUDService.delete(it)
                    } else {
                        CRUDService.updateWithoutAuditLog(it)
                    }
                }
            }
        }
   }

    String updateAssignedOwner(Map params, Long tenantId){
        List cases = [[caseNumber: params.caseNumber, caseVersion: params.version, submissionIdentifier: params.submissionIdentifier]]
        Set allowedAssignedToIds = []
        if (params.field == Constants.USER_GROUP_TOKEN)
            allowedAssignedToIds = userService.allowedAssignedToUserListPvcPvq(null, 0, Integer.MAX_VALUE, params.value, Constants.ALLOWED_ASSIGNED_TO_ROLES_PVQ)?.items?.collect { it.id } ?: []
        else
            allowedAssignedToIds = userService.allowedAssignedToGroupListPvcPvq(null, 0, Integer.MAX_VALUE, params.value, Constants.ALLOWED_ASSIGNED_TO_ROLES_PVQ)?.items?.collect { it.id } ?: []

        if (params.selectedCases) {
            def json = JSON.parse(params.selectedCases)
            if (json && json.size() > 0) cases.addAll(json)
            cases = cases.unique { a, b -> ((a.caseNumber == b.caseNumber) && (a.caseVersion == b.caseVersion) && (a.submissionIdentifier == b.submissionIdentifier)) ? 0 : -1 }
        }
        cases.each {
            updateAssignedToForCase(params.dataType, it.caseNumber, it.caseVersion, tenantId, params.value, params.field, allowedAssignedToIds, it.submissionIdentifier)
        }
    }


    def updateAssignedToForCase(String dataType, String caseNumber, String caseVersion, tenantId, value, String field, Set allowedAssignedToIds, String submissionIdentifier) {
        def qualityModuleObjArray =  getQualityDataByCaseNumAndVersion(dataType, caseNumber, caseVersion, tenantId,submissionIdentifier)
        qualityModuleObjArray.each { qualityModuleObj ->
            String assignedTo = value
            User previousUser = qualityModuleObj.assignedToUser
            UserGroup previousUserGroup = qualityModuleObj.assignedToUserGroup
            if (assignedTo) {
                if (assignedTo.startsWith(Constants.USER_GROUP_TOKEN)) {
                    if(allowedAssignedToIds && qualityModuleObj.assignedToUser && !allowedAssignedToIds.contains(Constants.USER_TOKEN+qualityModuleObj.assignedToUser.id)) throw new IllegalArgumentException(ViewHelper.getMessage("app.label.assignedTo.incompatibleError"))
                    qualityModuleObj.assignedToUserGroup = UserGroup.read(Long.valueOf(assignedTo.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    if(previousUserGroup?.id!=qualityModuleObj.assignedToUserGroup?.id){
                        if(!previousUser) {
                            qualityModuleObj.assigner = userService.currentUser
                            qualityModuleObj.assigneeUpdatedDate = new Date()
                        }
                    }
                } else if (assignedTo.startsWith(Constants.USER_TOKEN)) {
                    if(allowedAssignedToIds && qualityModuleObj.assignedToUserGroup && !allowedAssignedToIds.contains(Constants.USER_GROUP_TOKEN+qualityModuleObj.assignedToUserGroup.id)) throw new IllegalArgumentException(ViewHelper.getMessage("app.label.assignedTo.incompatibleError"))
                    qualityModuleObj.assignedToUser = User.read(Long.valueOf(assignedTo.replaceAll(Constants.USER_TOKEN, '')))
                    if(previousUser?.id!=qualityModuleObj.assignedToUser?.id){
                       qualityModuleObj.assigner = userService.currentUser
                        qualityModuleObj.assigneeUpdatedDate = new Date()
                    }
                }

            } else {
                if (field == Constants.USER_GROUP_TOKEN) qualityModuleObj.assignedToUserGroup = null
                if (field == Constants.USER_TOKEN) qualityModuleObj.assignedToUser = null
            }
            CRUDService.saveWithoutAuditLog(qualityModuleObj)
        }
    }

    void checkAllowedAssignedTo(){

    }

    void updatePriority(Map params, Long tenantId) {
        Set idstoUpdate = [params.id]
        if(params.selectedIds) idstoUpdate.addAll(params.selectedIds.split(";"))
        idstoUpdate.each {
            def qualityModuleObj = initializeQualityObjByIdAndTenantId(it, params.dataType, tenantId)
            if (params.value == "-1") {
                qualityModuleObj.priority = null
            } else {
                qualityModuleObj.priority = params.value
            }
            CRUDService.saveOrUpdate(qualityModuleObj)
        }
    }

    String updateQualityIssueType(Map params, Long tenantId){
        def qualityModuleObj = initializeQualityObjByIdAndTenantId(params.id, params.dataType, tenantId)
        if(params.value == '-1'){
            qualityModuleObj.qualityIssueType = null
        }else {
            qualityModuleObj.qualityIssueType = params.value
        }
        qualityModuleObj.rootCause = null
        qualityModuleObj.responsibleParty = null
        try {
            CRUDService.saveOrUpdate(qualityModuleObj)
            return "Ok"
        } catch (Exception e) {
            log.error("Error occurred in updating priority", e)
            return "Error occurred in updating priority"
        }
    }

    String updateRootCauses(Map params, Long tenantId){
        def qualityModuleObj = initializeQualityObjByIdAndTenantId(params.id, params.dataType, tenantId)
        if(params.value == '-1'){
            qualityModuleObj.rootCause = null
        }else {
            qualityModuleObj.rootCause = params.value
        }
        qualityModuleObj.responsibleParty = null
        try {
            CRUDService.saveOrUpdate(qualityModuleObj)
            return "Ok"
        } catch (Exception e) {
            log.error("Error occurred in updating root cause", e)
            return "Error occurred in updating root cause"
        }
    }

    String updateResponsibleParty(Map params, Long tenantId){
        def qualityModuleObj = initializeQualityObjByIdAndTenantId(params.id, params.dataType, tenantId)
        if(params.value == '-1'){
            qualityModuleObj.responsibleParty = null
        }else {
            qualityModuleObj.responsibleParty = params.value
        }
        try {
            CRUDService.saveOrUpdate(qualityModuleObj)
            return "Ok"
        } catch (Exception e) {
            log.error("Error occurred in updating responsible party", e)
            return "Error occurred in updating responsible party"
        }
    }

    Map getIdToUpdateWorkflow(String dataType, List<Long> casesId, Long fromStateId) {
        Map fullResult = [goodIds: new HashSet(), badIds: new HashSet()]
        casesId.each { id ->
            def quality = initializeQualityObjById(dataType, id)
            List groupList = getSimilarQualityObj(dataType, quality.caseNumber, quality.versionNumber, quality.tenantId, quality.hasProperty("submissionIdentifier")? quality.submissionIdentifier:null)
            if (groupList.find { it.workflowStateId == fromStateId })
                fullResult.goodIds.addAll(groupList*.id)
            else
                fullResult.badIds.addAll(groupList*.id)
        }
        fullResult
    }

    public List getSimilarQualityObj(String dataType, String caseNumber, Long caseVersion, Long tenantId, String submissionIdentifier) {
        def qualityObj
        switch (dataType) {
            case PvqTypeEnum.CASE_QUALITY.toString():
                qualityObj = QualityCaseData.findAllByCaseNumberAndVersionNumberAndTenantIdAndIsDeleted(caseNumber, caseVersion, tenantId, false)
                break
            case PvqTypeEnum.SUBMISSION_QUALITY.toString():
                qualityObj = QualitySubmission.findAllByCaseNumberAndVersionNumberAndTenantIdAndIsDeletedAndSubmissionIdentifier(caseNumber, caseVersion, tenantId, false, submissionIdentifier)
                break
            default:
                qualityObj = QualitySampling.findAllByCaseNumberAndVersionNumberAndTenantIdAndTypeAndIsDeleted(caseNumber,caseVersion, tenantId, dataType, false)
                break
        }
        return qualityObj
    }

    public def initializeQualityObjByIdAndTenantId(String idString, String dataType, Long tenantId){
        def qualityObj
        Long id = Long.parseLong(idString)
        switch(dataType){
            case PvqTypeEnum.CASE_QUALITY.toString():
                qualityObj = QualityCaseData.findByIdAndTenantId(id, tenantId)
                break
            case PvqTypeEnum.SUBMISSION_QUALITY.toString():
                qualityObj = QualitySubmission.findByIdAndTenantId(id, tenantId)
                break
            default:
                qualityObj = QualitySampling.findByIdAndTenantId(id, tenantId)
                break
        }
        return qualityObj
    }

    String getActionItemStatusForQualityRecord(Long id, String qualityRecordType){
        Set<ActionItem> qualityActionItems = null
        boolean waiting = false
        if (qualityRecordType.equals(PvqTypeEnum.CASE_QUALITY.toString())) {
            qualityActionItems = QualityCaseData.get(id)?.actionItems?.findAll { !it.deleted }
        } else if (qualityRecordType.equals(PvqTypeEnum.SUBMISSION_QUALITY.toString())) {
            qualityActionItems = QualitySubmission.get(id)?.actionItems?.findAll { !it.deleted }
        } else {
            qualityActionItems = QualitySampling.get(id)?.actionItems?.findAll { !it.deleted }
        }
        if (qualityActionItems?.size() > 0) {
            for(ActionItem qualityActionItem : qualityActionItems){
                if ((qualityActionItem.status != StatusEnum.CLOSED)) {
                    waiting = true
                    if (qualityActionItem.dueDate < new Date()) {
                        return ActionItemGroupState.OVERDUE.toString()
                    }
                }
            }
            return waiting ? ActionItemGroupState.WAITING.toString() : ActionItemGroupState.CLOSED.toString()
        }else{
            return null
        }
    }

    List<String> fetchQualityModuleErrorTypes(String errorType, Long tenantId) {
        List<String> errorTypeList = null
        switch(errorType){
            case PvqTypeEnum.CASE_QUALITY.name():
                errorTypeList = QualityCaseData.getErrorTypes(tenantId)
                break
            case PvqTypeEnum.SUBMISSION_QUALITY.name():
                errorTypeList = QualitySubmission.getErrorTypes(tenantId)
                break
            default:
                errorTypeList = QualitySampling.getErrorTypes(tenantId, errorType)
                break
        }
        errorTypeList
    }

    List<String> fetchQualityModuleErrorsList(String errorType, Long tenantId) {
        List<String> errorTypeList = []
        String errorTypeListQuery
        switch(errorType){
            case PvqTypeEnum.CASE_QUALITY.name():
                errorTypeListQuery = "SELECT DISTINCT ERROR_TYPE FROM QUALITY_CASE_DATA WHERE ISDELETED=0 AND TENANT_ID=${tenantId} ORDER BY ERROR_TYPE ASC"
                break
            case PvqTypeEnum.SUBMISSION_QUALITY.name():
                errorTypeListQuery = "SELECT DISTINCT ERROR_TYPE FROM QUALITY_SUBMISSION WHERE ISDELETED=0 AND TENANT_ID=${tenantId} ORDER BY ERROR_TYPE ASC"
                break
            default:
                errorTypeListQuery = "SELECT DISTINCT ERROR_TYPE FROM QUALITY_SAMPLING WHERE ISDELETED=0 AND TENANT_ID=${tenantId} ORDER BY ERROR_TYPE ASC"
                break
        }
        Sql pvrsql
        try {
            pvrsql = new Sql(getReportConnectionForPVR())
            List dbrecords = pvrsql.rows(errorTypeListQuery)
            dbrecords.each {
                errorTypeList << it['ERROR_TYPE'].toString()
            }
        }
        catch (Exception e) {
            log.error("Error Occured During Fetching Errortype list -- ${e.getMessage()}")
            throw e
        } finally {
            pvrsql?.close()
        }
        errorTypeList
    }

    String updateErrorType(Map params, Long tenantId) {
        Set idstoUpdate = [params.id]
        if (params.selectedIds) idstoUpdate.addAll(params.selectedIds.split(";"))
        idstoUpdate.each {
            def qualityModuleObj = initializeQualityObjByIdAndTenantId(it,params.dataType,  tenantId)
            if (qualityModuleObj != null) {
                if ((qualityModuleObj.entryType == "M") && (!qualityModuleObj.workflowState || !qualityModuleObj.workflowState?.finalState)) {
                    qualityModuleObj.errorType = params.value
                    CRUDService.save(qualityModuleObj)
                }
            }
        }
    }

    Map getCaseCountByError(Long tenantId, Date from = null, String dataType = null, Date to = null){
        LinkedHashMap<String, Long> errorTypeCaseCountMap = new LinkedHashMap<String, Long>()
        String fromDate = null
        String toDate = null
        String type = ""
        if (from) {
            fromDate =  from.format(DateUtil.DATEPICKER_FORMAT)
        }
        if (to) {
            toDate =  to.format(DateUtil.DATEPICKER_FORMAT)
        }

        if(!dataType || dataType == Constants.ALL){
            getCaseCountByErrorType(fromDate, tenantId, PvqTypeEnum.CASE_QUALITY.name(), errorTypeCaseCountMap, toDate)
            getCaseCountByErrorType(fromDate, tenantId, PvqTypeEnum.SUBMISSION_QUALITY.name(), errorTypeCaseCountMap, toDate)
        }else{
            if(dataType != Constants.NONE) {
                getCaseCountByErrorType(fromDate, tenantId, dataType, errorTypeCaseCountMap, toDate)
            }else{
                errorTypeCaseCountMap = [:]
                return [errorNameList: [], errorTotalCountList: errorTypeCaseCountMap.values()]
            }
        }
        errorTypeCaseCountMap = errorTypeCaseCountMap.sort { a, b -> b.value <=> a.value }
        return [errorNameList: errorTypeCaseCountMap.keySet(), errorTotalCountList: errorTypeCaseCountMap.values()]
    }

    void getCaseCountByErrorType(String from, Long tenantId, String type, LinkedHashMap<String, Long> errorTypeCaseCountMap, String to){
        Sql pvrsql
        List<Map> dataList = []
        String errorType
        Long caseCount
        try {
            pvrsql=new Sql(getReportConnectionForPVR())
            String tableName = "QUALITY_SUBMISSION"
            if (type == PvqTypeEnum.CASE_QUALITY.name()) {
                tableName = "QUALITY_CASE_DATA"
            }
            String whereClause = ""
            if(from){
                whereClause = " AND A.DATE_CREATED>= '"+from+"'"
            }
            if(to){
                whereClause += " AND A.DATE_CREATED <= '"+to+"'"
            }
            String queryString = "SELECT count(A.CASE_NUM), A.ERROR_TYPE FROM ${tableName} A  WHERE ISDELETED = 0  AND TENANT_ID = ${tenantId} "+
                                 " ${whereClause} GROUP BY A.ERROR_TYPE"
            dataList = pvrsql.rows(queryString)

            dataList.each {
                errorType = it[1].toString()
                caseCount = Long.valueOf(it[0].toString())
                if (errorTypeCaseCountMap.get(errorType) == null) {
                    errorTypeCaseCountMap.put(errorType, caseCount)
                } else {
                    errorTypeCaseCountMap.put(errorType, errorTypeCaseCountMap.get(errorType) + caseCount)
                }
            }
        } catch (Exception e) {
            log.error("Error Occured During Fetching Errortype list -- ${e.getMessage()}")
            throw e
        } finally {
            pvrsql?.close()
        }
    }

    Map getCaseDataCountByError(Long tenantId){
        LinkedHashMap<String, Long> errorTypeCaseCountMap = new LinkedHashMap<String, Long>()
        String errorType
        Long caseCount
        QualityCaseData.getErrorTypeCaseCount(tenantId).each{
            errorType = it[1].toString()
            caseCount = Long.valueOf(it[0].toString())
            if(errorTypeCaseCountMap.get(errorType)==null){
                errorTypeCaseCountMap.put(errorType, caseCount)
            }else{
                errorTypeCaseCountMap.put(errorType, errorTypeCaseCountMap.get(errorType) + caseCount)
            }
        }
        return [errorNames: errorTypeCaseCountMap.keySet(), caseCountValues: errorTypeCaseCountMap.values()]
    }

    Map getSubmissionCountByError(Long tenantId){
        LinkedHashMap<String, Long> errorTypeSubmissionCountMap = new LinkedHashMap<String, Long>()
        String errorType
        Long caseCount
        QualitySubmission.getErrorTypeCaseCount(tenantId).each{
            errorType = it[1].toString()
            caseCount = Long.valueOf(it[0].toString())
            if(errorTypeSubmissionCountMap.get(errorType)==null){
                errorTypeSubmissionCountMap.put(errorType, caseCount)
            }else{
                errorTypeSubmissionCountMap.put(errorType, errorTypeSubmissionCountMap.get(errorType) + caseCount)
            }
        }
        return [errorNames: errorTypeSubmissionCountMap.keySet(), caseCountValues: errorTypeSubmissionCountMap.values()]
    }

    Map getCapa(Long actionItemId) {
        Map result
        List<Capa8D> capaList = Capa8D.capasByActionItem(actionItemId)?.list()
        Capa8D capa = null
        if (capaList && capaList.size() > 0) {
            capa = Capa8D.get(capaList.first())
            if (capa) {
                result = [:]
                result.associatedIssueNumber = capa.issueNumber
                result.associatedIssueId = capa.id
            }
        }
        return result
    }

    Map getCaseNoByActionItemId(Long actionItemId) {
        Sql pvrsql
        List<Map> list = []
        try {
            pvrsql=new Sql(getReportConnectionForPVR())
            String queryString = getQualityCaseQuery(actionItemId) + " UNION ALL" + getQualitySubmissionQuery(actionItemId) + " UNION ALL" + getQualitySamplingQuery(actionItemId)
            List dbrecords = pvrsql.rows(queryString)
            dbrecords.each {
                Map caseNoDetailMap = [:]
                caseNoDetailMap['masterCaseNum'] = it[0]
                caseNoDetailMap['dataType'] = it[2]
                caseNoDetailMap['id'] = it[3]
                setVersion(it, caseNoDetailMap)
                list << caseNoDetailMap
            }
            return list.size()>0?list[0]:[:]
        } catch (Exception e) {
            log.error("Error Occured During Fetching Action Item -- ${e.getMessage()}")
            throw e
        } finally {
            pvrsql?.close()
        }
    }

    void setVersion(def dbrow, Map caseNoDetailMap){
        def metadata=dbrow['METADATA']
        def rtext = metadata instanceof  String ? metadata : metadata?.characterStream?.text
        Map record = new JsonSlurper().parseText(rtext)
        caseNoDetailMap['masterVersionNum'] = 1
        if(record['masterVersionNum']){
            caseNoDetailMap['masterVersionNum'] = record['masterVersionNum']
        }
    }

    String getQualityCaseQuery(Long actionItemId){
        String dataType = PvqTypeEnum.CASE_QUALITY.toString()
        return " select quality.CASE_NUM, quality.METADATA , '"+dataType+"',quality.id  from quality_case_data quality inner join quality_case_action_items qualityItems " +
                "ON quality.id = qualityItems.quality_case_id   " +
                "WHERE qualityItems.action_item_id = ${actionItemId} "
    }

    String getQualitySubmissionQuery(Long actionItemId){
        String dataType = PvqTypeEnum.SUBMISSION_QUALITY.toString()
        return " select quality.CASE_NUM, quality.METADATA , '"+dataType+"',quality.id  from quality_submission quality inner join quality_submission_action_item qualityItems " +
                "ON quality.id = qualityItems.quality_submission_id  " +
                "WHERE qualityItems.action_item_id =  ${actionItemId} "
    }

    String getQualitySamplingQuery(Long actionItemId){
        return " select quality.CASE_NUM, quality.METADATA ,quality.type,quality.id  from quality_sampling quality inner join quality_sampling_action_items qualityItems "+
                " ON quality.id = qualityItems.quality_sampling_id "+
                "  WHERE qualityItems.action_item_id = ${actionItemId} "
    }

    String getCaseNoByIssueId(Long issueId, Long tenantId){
        Sql pvrsql
        List<String> list=[]
        try {
            pvrsql=new Sql(getReportConnectionForPVR())
            String queryString = getQualityCaseNoByIssueId(issueId, tenantId) + " UNION " + getSubmissionCaseNoByIssueId(issueId, tenantId) + " UNION " + getSamplingCaseNoByIssueId(issueId, tenantId)
            List dbrecords = pvrsql.rows(queryString)
            dbrecords.each {
                list << it[0]
            }
            return list.size()>0?list[0]:""
        } catch (Exception e) {
            log.error("Error Occured During Fetching Errortype list -- ${e.getMessage()}")
            throw e
        } finally {
            pvrsql?.close()
        }
    }

    String getQualityCaseNoByIssueId(Long issueId, Long tenantId){
        return "select CASE_NUM from quality_case_data quality inner join quality_case_issues qualityItems " +
                "ON quality.id = qualityItems.quality_case_id   " +
                "WHERE qualityItems.issue_id = ${issueId} and quality.tenant_id = ${tenantId} "
    }

    String getSubmissionCaseNoByIssueId(Long issueId, Long tenantId){
        return "select CASE_NUM from quality_submission quality inner join quality_submission_issues qualityItems " +
                "ON quality.id = qualityItems.quality_submission_id  " +
                "WHERE qualityItems.issue_id =  ${issueId} and quality.tenant_id = ${tenantId} "
    }

    String getSamplingCaseNoByIssueId(Long issueId, Long tenantId){
        return "select CASE_NUM from quality_sampling quality inner join quality_sampling_issues qualityItems "+
                " ON quality.id = qualityItems.quality_sampling_id "+
                "  WHERE qualityItems.issue_id = ${issueId} and quality.tenant_id = ${tenantId} "
    }

    Map fetchPriorityAndComments(String qualityRecordType,Long id) {
        String priority = Constants.BLANK_STRING
        String comment = Constants.BLANK_STRING
        def dataList = []
        if(qualityRecordType.equals(PvqTypeEnum.CASE_QUALITY.toString())){
            QualityCaseData qualityData = QualityCaseData.get(id)
            dataList = new JsonSlurper().parseText(qualityData.metadata)
            priority = qualityData.priority
            if(qualityData.comments && qualityData.comments.size() > 0) {
                comment = qualityData.comments[0].textData
            }
        } else if(qualityRecordType.equals(PvqTypeEnum.SUBMISSION_QUALITY.toString())){
            QualitySubmission qualityData = QualitySubmission.get(id)
            dataList = new JsonSlurper().parseText(qualityData.metadata)
            priority = qualityData.priority
            if(qualityData.comments && qualityData.comments.size() > 0) {
                comment = qualityData.comments[0].textData
            }
        } else {
            QualitySampling qualityData = QualitySampling.get(id)
            dataList = new JsonSlurper().parseText(qualityData.metadata)
            priority = qualityData.priority
            if(qualityData.comments && qualityData.comments.size() > 0) {
                comment = qualityData.comments[0].textData
            }
        }
        dataList.put("priority",priority)
        dataList.put("comment", comment)
        return dataList
    }

    public def initializeQualityObjById(String dataType, Long id){
        def qualityObj
        switch(dataType){
            case PvqTypeEnum.CASE_QUALITY.toString():
                qualityObj = QualityCaseData.get(id)
                break
            case PvqTypeEnum.SUBMISSION_QUALITY.toString():
                qualityObj = QualitySubmission.get(id)
                break
            default:
                qualityObj = QualitySampling.get(id)
                break
        }
        return qualityObj
    }

    def initializeQualityObjListByIds(String dataType, Collection ids){
        List qualityObjList
        switch (dataType) {
            case PvqTypeEnum.CASE_QUALITY.toString():
                qualityObjList = QualityCaseData.getAll(ids)
                break
            case PvqTypeEnum.SUBMISSION_QUALITY.toString():
                qualityObjList = QualitySubmission.getAll(ids)
                break
            default:
                qualityObjList = QualitySampling.getAll(ids)
                break
        }
        return qualityObjList
    }

    String getCommentsForQualityRecord(Long id, String qualityRecordType){
        String latestComment = null
        Set<Comment> comments = null
        if(qualityRecordType.equals(PvqTypeEnum.CASE_QUALITY.toString())){
            comments = QualityCaseData.get(id)?.comments
            if(comments && comments.size()>0){
                comments = comments.sort {it.lastUpdated}
                latestComment = comments.last().textData
            }
        } else if(qualityRecordType.equals(PvqTypeEnum.SUBMISSION_QUALITY.toString())){
            comments = QualitySubmission.get(id)?.comments
            if(comments && comments.size()>0){
                comments = comments.sort {it.lastUpdated}
                latestComment = comments.last().textData
            }
        } else {
            comments = QualitySampling.get(id)?.comments
            if (comments && comments.size() > 0) {
                comments = comments.sort {it.lastUpdated}
                latestComment = comments.last().textData
            }
        }
        return latestComment
    }

    List<WorkflowRule> qualityTargetStates(Long qualityId, WorkflowState initialState, String dataType) {
        WorkflowConfigurationTypeEnum configurationTypeEnum
        if (dataType.equals(PvqTypeEnum.CASE_QUALITY.name())) {
            configurationTypeEnum = WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA
        } else if (dataType.equals(PvqTypeEnum.SUBMISSION_QUALITY.name())) {
            configurationTypeEnum = WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION
        } else {
            configurationTypeEnum = WorkflowConfigurationTypeEnum.getAdditional(getAdditional(dataType).workflow)
        }
        return WorkflowRule.getAllByConfigurationTypeAndInitialState(configurationTypeEnum,initialState).list()
    }
    Map qualityTargetStatesAndApplications(Long qualityId, String initialState, String dataType) {

        List states = []
        Map actions = [:]
        Map rules = [:]
        Map needApproval = [:]
        WorkflowState initialStateObj = WorkflowState.findByNameAndIsDeleted(initialState, false)
        WorkflowConfigurationTypeEnum configurationTypeEnum
        if (dataType.equals(PvqTypeEnum.CASE_QUALITY.name())) {
            configurationTypeEnum = WorkflowConfigurationTypeEnum.QUALITY_CASE_DATA
        } else if (dataType.equals(PvqTypeEnum.SUBMISSION_QUALITY.name())) {
            configurationTypeEnum = WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION
        } else {
            configurationTypeEnum = WorkflowConfigurationTypeEnum.getAdditional(getAdditional(dataType).workflow)
        }
        List<WorkflowRule> workflowRules = WorkflowRule.getAllByConfigurationTypeAndInitialState(configurationTypeEnum,initialStateObj).list()
        if(workflowRules){
            workflowRules?.each {
                actions.put(it.targetState?.name, it.targetState?.reportActionsAsList)
                states.add(it.targetState)
                rules.put(it.id, it.targetState)
                needApproval.put(it.id, it.needApproval)
            }
        }
        [actions: actions, states: states, rules: rules, needApproval: needApproval]
    }

    List fetchAttachments(String caseNumber) {
        Sql sql
        if(grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)) {
            sql = new Sql(dataSource_pva)
        }else {
            sql = new Sql(dataSource_safetySource)
        }
        try {
            CaseInfo.withNewSession {
                CaseInfo c = CaseInfo.findByCaseNumber(caseNumber)
                String attachmentListQuery = ""
                if(grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)) {
                    attachmentListQuery = Holders.config.getProperty('pvcm_attachments.list')
                }else {
                    attachmentListQuery = Holders.config.getProperty('argus_attachments.list')
                }
                List dbrecords = sql.rows(attachmentListQuery, [c.caseId, c.caseId])
                if (!dbrecords || dbrecords.size() == 0) return []

                Set<List> uniqueCaseVersionPairs = dbrecords.collect { [c.caseId, it.VERSION_NUM] }.toSet()
                if (uniqueCaseVersionPairs.isEmpty()) return []

                String whereClause = uniqueCaseVersionPairs.collect { " CASE_ID = ${it[0]} AND VERSION_NUM = ${it[1]}" }.join(" OR")
                if (!whereClause || whereClause.isEmpty()) return []

                String excFrmPrivacyLocQuery = """
    SELECT CASE_ID, VERSION_NUM, 
           CASE WHEN EXISTS (SELECT 1 FROM C_IDENTIFICATION 
                             WHERE C_IDENTIFICATION.CASE_ID = MAIN.CASE_ID 
                             AND C_IDENTIFICATION.VERSION_NUM = MAIN.VERSION_NUM 
                             AND EXC_FRM_PRIVACY_LOCATION_ID = 1) 
                THEN 1 ELSE 0 
           END AS EXC_FRM_PRIVACY_CHECK 
    FROM C_IDENTIFICATION MAIN
    WHERE${whereClause}"""

                Map<String, Boolean> privacyCheckMap = [:] // Key: "caseId-versionNum"

                sql.rows(excFrmPrivacyLocQuery).each { row ->
                    privacyCheckMap["${row.CASE_ID}-${row.VERSION_NUM}"] = (row.EXC_FRM_PRIVACY_CHECK == 1)
                }
                dbrecords.removeAll {
                    Boolean excFrmprivacyLoc = privacyCheckMap["${c.caseId}-${it.VERSION_NUM}"] ?: false
                    excFrmprivacyLoc && (it.size() > 5 && it.FLAG_PROTECTED == 1 && it.FLAG_FILE_REDACTED == 0)
                }

                dbrecords.collect {
                    Boolean excFrmprivacyLoc = privacyCheckMap["${c.caseId}-${it.VERSION_NUM}"] ?: false
                    [
                            id      : it.REPORT_DATA_ID.toString(),
                            fileName: it.FLAG_INCLUDED_DOCUMENTS,
                            notes   : it.NOTES,
                            version : it.VERSION_NUM,
                            isRedacted: excFrmprivacyLoc ? it.FLAG_FILE_REDACTED == 1 : false
                    ]
                }
            }
        }catch(Exception e){
            e.printStackTrace()
        }finally {
            sql?.close()
        }
    }

    Map fetchAttachmentContent(id, caseNumber, versionNumber, Boolean isRedacted = false) {
        Sql sql
        try {
            List dataList
            if(grailsApplication.config.getProperty('safety.source').equals(Constants.PVCM)) {
                String uniqueId = caseNumber + "_" + versionNumber + "_" + id
                FileDTO fileDTO = fileAttachmentLocator.getServiceFor(grailsApplication.config.getProperty('safety.source')).getFile(uniqueId, isRedacted)
                dataList = fileDTO.collect {
                    [id: id, fileName: fileDTO?.name, data: fileDTO?.data]
                }
            }else{
                sql = new Sql(dataSource_safetySource)
                String attachmentListQuery = Holders.config.argus_attachments.content
                dataList = sql.rows(attachmentListQuery, [id]).collect {
                    byte[] data = it[3].getBytes(1L, (int) it[3].length());
                    it[3].free();
                    [id: it[0], fileName: it[1], data: data]
                }
            }
            return dataList ? dataList.first() : [:]
        }catch(Exception e){
            e.printStackTrace()
        }finally {
            sql?.close()
        }
        return null
    }

    boolean isPermitted(String type) {
        User user = userService.currentUser
        if (user.isAdmin()) return true
        String tableName
        String strWhereCondition = "where ISDELETED=0 AND TENANT_ID = ${Tenants.currentId() as Long} and REPORT_ID is not null "
        if (type == PvqTypeEnum.CASE_QUALITY.name()) {
            tableName = "QUALITY_CASE_DATA"
        } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
            tableName = "QUALITY_SUBMISSION"
        } else {
            tableName = "QUALITY_SAMPLING"
            strWhereCondition += " and TYPE = '${type}' "
        }

        String querystring = "SELECT DISTINCT A.REPORT_ID FROM ${tableName} A ${strWhereCondition}"

        Sql pvrsql
        try {
            pvrsql = new Sql(getReportConnectionForPVR())
            List dbrecords = pvrsql.rows(querystring)
            List dbrecordsParts = dbrecords.collate(999)
            for (List part : dbrecordsParts) {
                List<ReportConfiguration> reportConfigList = ReportConfiguration.getAll(part.collect { it[0] as Long })
                for (ReportConfiguration reportConfig : reportConfigList) {
                    if (!reportConfig.isDeleted && !reportConfig.isViewableBy(user)) return false
                }
            }
            return true
        } finally {
            pvrsql?.close()
        }
    }

    void appendQualityTypesToLeftMenu() {
        List leftMenu = grailsApplication.config.getProperty('pv.app.settings.PVQuality', List, [])
        if(!leftMenu){
            return;
        }
        int pos = leftMenu[0].children.size()
        Holders.config.qualityModule.additional.eachWithIndex { it, i ->
            leftMenu[0].children.add(
                    [
                            name    : it.label,
                            icon    : '',
                            link    : '/quality/caseSampling?dataType=' + it.name,
                            position: pos + i,
                            role    : 'ROLE_PVQ_VIEW',
                    ]
            )
        }
    }

    Map getErrorsCountByFieldName(String fieldName, String type,
                           Map externalSearch, Long tenantId, int rowsCount) {
        Sql pvrsql
        try {
            pvrsql = new Sql(getReportConnectionForPVR())
            String tableName = ""
            String pvqTypeEnum = type
            if (type == PvqTypeEnum.CASE_QUALITY.name()) {
                tableName = "QUALITY_CASE_DATA"
            } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                tableName = "QUALITY_SUBMISSION"
            }

            List list = []
            String strWhereCondition = " WHERE ISDELETED = 0  AND TENANT_ID = ${tenantId} and A.MetaData.${fieldName} is not null "
            String strExternalWhereCondition = getQualityDataExternalWhereStatement(externalSearch)
            if(strExternalWhereCondition !="") {
                strWhereCondition = "${strWhereCondition} AND ${strExternalWhereCondition} "
            }

            String topByFieldNameQuery = " SELECT A.MetaData.${fieldName} FROM ${tableName} A "+
                                    " ${strWhereCondition} GROUP BY (A.MetaData.${fieldName}) "+
                                    " ORDER BY count(*) DESC OFFSET 0 ROWS FETCH NEXT ${rowsCount} ROWS ONLY "

            List topRecords = pvrsql.rows(topByFieldNameQuery)
            Set fieldsNames = []
            topRecords.each {
                fieldsNames.add(it[0])
            }

            String queryString = " SELECT COUNT(*),A.error_type, A.MetaData.${fieldName} FROM ${tableName} A " +
                                 " ${strWhereCondition}  AND A.MetaData.${fieldName} IN (${topByFieldNameQuery}) " +
                                 " GROUP BY (A.error_type, A.MetaData.${fieldName}) ORDER BY A.error_type "

            List dbRecords = pvrsql.rows(queryString)
            dbRecords.each {
                Map record = [:]
                record['count'] = it[0]
                record['errorType'] = it[1]
                record[fieldName] = it[2]
                list.add(record)
            }
            return ["fieldNamesList":fieldsNames, "errorsList": list]
        } catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        } finally {
            pvrsql?.close()
        }
        return list
    }

    Map getLatestQualityIssues(String type, Long tenantId, int max, int offset) {
        Sql pvrsql
        List list = []
        Class entityClass
        int total = 0L
        try {
            pvrsql = new Sql(getReportConnectionForPVR())
            String tableName = ""
            String pvqTypeEnum = ""
            List<String> minColumnNameList = ["masterCaseNum","masterVersionNum","masterCaseReceiptDate","masterRptTypeId","masterCountryId","masterPrimProdName"]
            if (type == PvqTypeEnum.CASE_QUALITY.name()) {
                tableName = "QUALITY_CASE_DATA"
                entityClass = QualityCaseData
                pvqTypeEnum = PvqTypeEnum.CASE_QUALITY.toString()
            } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                tableName = "QUALITY_SUBMISSION"
                entityClass = QualitySubmission
                pvqTypeEnum = PvqTypeEnum.SUBMISSION_QUALITY.toString()
            }
            String limitClause = ""
            if(max > -1){
                limitClause = " OFFSET ${offset} ROWS FETCH NEXT ${max} ROWS ONLY"
            }
            String maxExecReportIdSql = "SELECT max(EXEC_REPORT_ID) from ${tableName} ex where ISDELETED = 0  "

            String strWhereCondition = " WHERE ISDELETED = 0  AND TENANT_ID = ${tenantId} AND A.EXEC_REPORT_ID = ($maxExecReportIdSql) "

            String queryString = " SELECT ID, count(1) over(order by 1) as recordCount FROM ${tableName} A ${strWhereCondition} " +
                    " ORDER BY A.DATE_CREATED desc " +
                    " ${limitClause}"
            List dbRecords = pvrsql.rows(queryString)
            List idList = dbRecords.collect { it["ID"] as Long }
            if (dbRecords.size() > 0) {
                total = Long.parseLong(dbRecords[0][1].toString());
            } else {
                if(offset>0){
                    pvrsql.rows("select count(1) as c from "+tableName+" A "+strWhereCondition).each{
                        total=it["c"] as Long
                    }
                }
            }
            List records = entityClass.getAll(idList)
            Map fieldTypeMap = getQualityReportAllFields(type).collectEntries { [(it.fieldName), it.fieldType] }
            String dateFormat = DateUtil.getLongDateFormatForLocale(userService.currentUser?.preference?.locale, true)
            records.each { qualityRow ->
                def record = formQualityRecordRow(qualityRow, minColumnNameList, pvqTypeEnum, [:], fieldTypeMap, dateFormat)
                list.add(record)
            }
        } catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        } finally {
            pvrsql?.close()
        }
        return [aaData: list, recordsTotal: total, recordsFiltered: total]
    }

    def getQualityIssues(){
        List<Late> qualityIssueTypes = []
        Late.withNewSession {
            qualityIssueTypes = Late.findAllByOwnerApp("PVQ").findAll{it}.sort { a, b ->
                // Sort based on hiddenDate presence
                def hiddenDateA = a.hiddenDate != null
                def hiddenDateB = b.hiddenDate != null
                hiddenDateA.compareTo(hiddenDateB)
            }
        }
        return qualityIssueTypes
    }

    def getRootCauses(){
        List<RootCause> rootCauses = []
        RootCause.withNewSession {
            rootCauses = RootCause.findAllByOwnerApp("PVQ").findAll{it}
        }
        return rootCauses
    }

    def getResponsibleParties(){
        List<ResponsibleParty> responsibleParties = []
        ResponsibleParty.withNewSession {
            responsibleParties = ResponsibleParty.findAllByOwnerApp("PVQ").findAll{it}
        }
        return responsibleParties
    }

    def saveIssueDetailData(String dataType, List rcList, Set ids, Long selectedIssueTypId) {
        User user = userService.currentUser
        Preference preference = user?.preference
        String timeZone = preference?.timeZone
        Locale locale = preference?.locale
        Map issueDetailMap = new HashMap<>()
        String errorMessage = ""

        List qualityObjList = initializeQualityObjListByIds(dataType, ids)

        List mandatoryRCAFields = RCAMandatory.getMandatoryRCAFieldsForPVQ(qualityObjList.collect {it.workflowStateId}?.unique()).list()

        qualityObjList.each { qualityObj ->
            List<String> rcaMandatoryFields = mandatoryRCAFields.findAll { it[1] == qualityObjList.workflowStateId }?.collect { it[0] as String }
            List<String> errorFields = []
            if (rcList) {
                rcList.each { rc ->
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Issue_Type.toString()) && !rc.late) {
                        errorFields.add(ReasonOfDelayFieldEnum.Issue_Type.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Root_Cause.toString()) && !rc.rootCause) {
                        errorFields.add(ReasonOfDelayFieldEnum.Root_Cause.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Resp_Party.toString()) && !rc.responsibleParty) {
                        errorFields.add(ReasonOfDelayFieldEnum.Resp_Party.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Corrective_Action.toString()) && !rc.correctiveAction) {
                        errorFields.add(ReasonOfDelayFieldEnum.Corrective_Action.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Preventive_Action.toString()) && !rc.preventativeAction) {
                        errorFields.add(ReasonOfDelayFieldEnum.Preventive_Action.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Corrective_Date.toString()) && !rc.correctiveDate) {
                        errorFields.add(ReasonOfDelayFieldEnum.Corrective_Date.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Preventive_Date.toString()) && !rc.preventiveDate) {
                        errorFields.add(ReasonOfDelayFieldEnum.Preventive_Date.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Investigation.toString()) && !rc.investigation) {
                        errorFields.add(ReasonOfDelayFieldEnum.Investigation.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Summary.toString()) && !rc.summary) {
                        errorFields.add(ReasonOfDelayFieldEnum.Summary.getMessage())
                    }
                    if (rcaMandatoryFields.contains(ReasonOfDelayFieldEnum.Actions.toString()) && !rc.actions) {
                        errorFields.add(ReasonOfDelayFieldEnum.Actions.getMessage())
                    }
                    errorFields = errorFields.unique()
                    if(errorFields.size() ==0 ) {
                        qualityObj.qualityIssueDetails.clear()
                        if (rc.late) {
                            //PVR-59293 PVQ | Null entries for RCA fields are getting stored in edited RCA
                            if ((!Boolean.parseBoolean(rc.flagPrimary)) && !rc.rootCause && !rc.responsibleParty && !rc.correctiveAction && !rc.preventativeAction && !rc.correctiveDate && !rc.preventativeDate && rc.investigation.trim().equals(EMPTY_STRING) && rc.actions.trim().equals(EMPTY_STRING) && rc.summary.trim().equals(EMPTY_STRING)) {
                                return
                            }
                            Date correctiveDate = rc.correctiveDate ? DateUtil.getDateByLocaleAndTimezone(rc.correctiveDate, timeZone, locale) : null
                            Date preventativeDate = rc.preventiveDate ? DateUtil.getDateByLocaleAndTimezone(rc.preventiveDate, timeZone, locale) : null
                            Date dateCreated = DateUtil.getDateByLocaleAndTimezone(DateUtil.getShortDateStringForLocaleAndTimeZone(new Date(), locale, timeZone), timeZone, locale)
                            Date lastUpdated = DateUtil.getDateByLocaleAndTimezone(DateUtil.getShortDateStringForLocaleAndTimeZone(new Date(), locale, timeZone), timeZone, locale)
                            def issueDetail = new QualityIssueDetail()
                            if (rc.rootCause == '') {
                                issueDetail.rootCauseId = null
                            } else {
                                issueDetail.rootCauseId = rc.rootCause ? Long.parseLong(rc.rootCause) : rc.rootCause
                            }
                            if (rc.responsibleParty == '') {
                                issueDetail.responsiblePartyId = null
                            } else {
                                issueDetail.responsiblePartyId = rc.responsibleParty ? Long.parseLong(rc.responsibleParty) : rc.responsibleParty
                            }
                            issueDetail.correctiveActionId = rc.correctiveAction ? Long.parseLong(rc.correctiveAction) : null
                            issueDetail.correctiveDate = correctiveDate ?: null
                            issueDetail.preventativeActionId = rc.preventativeAction ? Long.parseLong(rc.preventativeAction) : null
                            issueDetail.preventativeDate = preventativeDate ?: null
                            issueDetail.investigation = rc.investigation ? rc.investigation : ""
                            issueDetail.summary = rc.summary ? rc.summary : ""
                            issueDetail.actions = rc.actions ? rc.actions : ""
                            issueDetail.isPrimary = rc.flagPrimary ? Boolean.parseBoolean(rc.flagPrimary) : false
                            if (issueDetail.isPrimary) {
                                issueDetailMap['rootCauseId'] = issueDetail.rootCauseId
                                issueDetailMap['responsiblePartyId'] = issueDetail.responsiblePartyId
                            }
                            issueDetail.id = rc.pvcLcpId ? Long.parseLong(rc.pvcLcpId) : rc.pvcLcpId
                            issueDetail.dateCreated = dateCreated
                            issueDetail.lastUpdated = lastUpdated
                            issueDetail.modifiedBy = user.username
                            issueDetail.createdBy = user.username
                            qualityObj.addToQualityIssueDetails(issueDetail)
                        }
                    }
                }
            }
            if (errorFields.size() ==0) {
                qualityObj.qualityIssueTypeId = selectedIssueTypId
                issueDetailMap['issueTypeId'] = selectedIssueTypId
                CRUDService.saveOrUpdate(qualityObj)
            }
            else if (errorFields.size() >0) {
                errorMessage += errorFields.join(', ') + " can't be empty for Case Number: " + qualityObj.caseNumber + " Version Number: " + qualityObj.versionNumber +
                        " Error Type: " + qualityObj.errorType + " in Workflow State : " + qualityObj.workflowState + ", please fill the required field(s).<br>"
            }
        }
        issueDetailMap['errorMessage'] = errorMessage
        return issueDetailMap
    }

    def deleteCases(List<Long> ids, String type, Long tenantId, String justification) {
        List row = []
        ids?.collate(999)?.each {
            if (type == PvqTypeEnum.CASE_QUALITY.name()) {
                row.addAll(QualityCaseData.findAllByIdInListAndTenantId(it, tenantId))
            } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                row.addAll(QualitySubmission.findAllByIdInListAndTenantId(it, tenantId))
            } else {
                row.addAll(QualitySampling.findAllByIdInListAndTenantId(it, tenantId))
            }
        }
        row.each {
            it.actionItems.each {
                CRUDService.softDelete(it, justification)
            }
            it.issues.each {
                CRUDService.softDelete(it, justification)
            }
            CRUDService.softDelete(it, justification)
        }

        def caseNumbers = row.collect { it.caseNumber } as Set

        def caseEntries = [:]
        caseNumbers.collate(999).each {
            if (type == PvqTypeEnum.CASE_QUALITY.name()) {
                caseEntries.putAll(QualityCaseData.findAllByCaseNumberInListAndTenantId(it, tenantId).groupBy { it.caseNumber })
            } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                caseEntries.putAll(QualitySubmission.findAllByCaseNumberInListAndTenantId(it, tenantId).groupBy { it.caseNumber })
            } else {
                caseEntries.putAll(QualitySampling.findAllByCaseNumberInListAndTenantId(it, tenantId).groupBy { it.caseNumber })
            }
        }

        def onlyCaseLevelCases = [] as Set
        caseEntries.each { _, values -> onlyCaseLevelCases.addAll(values.findAll { it.reportId != null && !it.isDeleted }.collect{_})}
        onlyCaseLevelCases.each { caseEntries.remove(it) }

        def fieldLevelCases = []
        caseEntries.each { _, values -> fieldLevelCases.addAll(values.findAll { it.reportId == null && !it.isDeleted }) }

        fieldLevelCases.each {
            it.actionItems.each {
                CRUDService.softDelete(it, justification)
            }
            it.issues.each {
                CRUDService.softDelete(it, justification)
            }
            CRUDService.softDelete(it, justification)
        }
    }

    def displayFieldLevelMsg(List<Long> ids, String type, Long tenantId) {
        List row = []
        ids?.collate(999)?.each {
            if (type == PvqTypeEnum.CASE_QUALITY.name()) {
                row.addAll(QualityCaseData.findAllByIdInListAndTenantIdAndReportIdIsNotNullAndIsDeleted(it, tenantId, false))
            } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                row.addAll(QualitySubmission.findAllByIdInListAndTenantIdAndReportIdIsNotNullAndIsDeleted(it, tenantId, false))
            } else {
                row.addAll(QualitySampling.findAllByIdInListAndTenantIdAndReportIdIsNotNullAndIsDeleted(it, tenantId, false))
            }
        }
        def caseNumbers = row.collect { it.caseNumber } as Set
        def casesEntries = [:]
        caseNumbers.collate(999).each {
            if (type == PvqTypeEnum.CASE_QUALITY.name()) {
                casesEntries.putAll(QualityCaseData.findAllByCaseNumberInListAndTenantId(it, tenantId).groupBy { it.caseNumber })
            } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                casesEntries.putAll(QualitySubmission.findAllByCaseNumberInListAndTenantId(it, tenantId).groupBy { it.caseNumber })
            } else {
                casesEntries.putAll(QualitySampling.findAllByCaseNumberInListAndTenantId(it, tenantId).groupBy { it.caseNumber })
            }
        }
        def receivedCaseEntries = row.groupBy { it.caseNumber }
        boolean flag = false
        receivedCaseEntries.each { k, v ->
            int receivedEntriesSize = casesEntries.get(k).findAll { it.reportId != null && !it.isDeleted }.size()
            if (v.size() == receivedEntriesSize) {
                int count = casesEntries.get(k).findAll { it.reportId == null && !it.isDeleted }.size()
                if (count > 0) {
                    flag = true
                    return true
                }
            }
        }
        return flag
    }

    def getAllIssueRcaForId(Map params, Long tenantId){
        String dataType = params.dataType
        Long id = params["id"] as Long
        User user = userService.currentUser
        Preference preference = user?.preference
        String timeZone = preference?.timeZone
        Locale locale = preference?.locale
        Sql sql = new Sql(getReportConnectionForPVR())
        List data = []
        Map dataMap = [:]
        WorkflowState workflowState
        try {
            String tableName
            String idColumnName
            if (dataType == PvqTypeEnum.CASE_QUALITY.name()) {
                tableName = "QUALITY_CASE_ISSUE_DETAILS"
                idColumnName = "QUALITY_CASE_ID"
            } else if (dataType == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
                tableName = "QUALITY_SUB_ISSUE_DETAILS"
                idColumnName = "QUALITY_SUBMISSION_ID"
            } else {
                tableName = "QUALITY_SAMPL_ISSUE_DETAILS"
                idColumnName = "QUALITY_SAMPLING_ID"
            }
            def qualityModuleObj = initializeQualityObjByIdAndTenantId(id.toString(), dataType, tenantId)
            Map workflow = qualityModuleObj.workflowState.toWorkflowStateMap()
            workflowState = qualityModuleObj.workflowState
            dataMap.workflowList = qualityTargetStates(id, qualityModuleObj.workflowState, dataType)?.collect { [id: it.id, name: it.targetState.name] }

            boolean differentAssigned = false
            boolean differentWorkflow = false
            boolean containsFinalState = workflow.finalState
            if (params.selectedIds) {
                for (String it in params.selectedIds.split(";")) {
                    def qualityObject = initializeQualityObjByIdAndTenantId(it, dataType, tenantId)
                    if ((qualityObject.assignedToUser != qualityModuleObj.assignedToUser)||(qualityObject.assignedToUserGroup != qualityModuleObj.assignedToUserGroup))differentAssigned = true
                    if (((qualityObject.caseNumber != qualityModuleObj.caseNumber) || (qualityObject.versionNumber != qualityModuleObj.versionNumber))
                            && (qualityObject.workflowState?.id != workflow.workflowStateId)) differentWorkflow = true
                    if(!containsFinalState) containsFinalState = qualityObject.workflowState.finalState
                    if (differentAssigned && differentWorkflow && containsFinalState) break
                }
            }

            dataMap.assignedToUser= (differentAssigned ? "" : qualityModuleObj.assignedToUser? Constants.USER_TOKEN + qualityModuleObj.assignedToUser.id:"")
            dataMap.assignedToUserGroup= (differentAssigned ? "" : qualityModuleObj.assignedToUserGroup?Constants.USER_GROUP_TOKEN + qualityModuleObj.assignedToUserGroup.id:"")
            dataMap.workflow= (differentWorkflow ? "" : workflow)
            dataMap.containsFinalState = containsFinalState
            List editableList = RCAMandatory.getEditableRCAFields(ReasonOfDelayAppEnum.PVQ, workflowState, userService.currentUser).list()
            dataMap.nonEditableList= ReasonOfDelayFieldEnum.findAll {!editableList*.toString().contains(it.toString())}
            dataMap.mandatoryList= RCAMandatory.getMandatoryRCAFields(ReasonOfDelayAppEnum.PVQ, workflowState).list()


            Long lateId = qualityModuleObj.qualityIssueTypeId
            if(lateId && allRcaDataMap['LATE_MAP'][lateId]) {
                dataMap['qualityIssueTypeId'] = lateId
                data = sql.rows(
                        "Select ID,VERSION,IS_PRIMARY,ROOT_CAUSE_ID,RESPONSIBLE_PARTY_ID," +
                                " CORRECTIVE_ACTION_ID,CAST(CORRECTIVE_DATE AS DATE) as CORRECTIVE_DATE,PREVENTATIVE_ACTION_ID,CAST(PREVENTATIVE_DATE AS DATE) as PREVENTATIVE_DATE, INVESTIGATION," +
                                " SUMMARY, ACTIONS from QUALITY_ISSUE_DETAIL qdr, ${tableName} q " +
                                " where q.${idColumnName} =:id and qdr.id = q.QUALITY_ISSUE_DETAIL_ID order by IS_PRIMARY desc", [id: id]).collect {
                    String correctiveDate = ""
                    String preventiveDate = ""
                    if (it.CORRECTIVE_DATE) {
                        correctiveDate = DateUtil.getShortDateStringForLocaleAndTimeZone(it.CORRECTIVE_DATE, locale, timeZone)
                    }
                    if (it.PREVENTATIVE_DATE) {
                        preventiveDate = DateUtil.getShortDateStringForLocaleAndTimeZone(it.PREVENTATIVE_DATE, locale, timeZone)
                    }
                    [
                            id                   : it.id, primaryFlag: it.IS_PRIMARY + "", lateValue: lateId,
                            rootCauseValue       : it.ROOT_CAUSE_ID, responsiblePartyValue: it.RESPONSIBLE_PARTY_ID,
                            correctiveActionValue: it.CORRECTIVE_ACTION_ID, preventiveActionValue: it.PREVENTATIVE_ACTION_ID,
                            investigation        : it.INVESTIGATION, summary: it.SUMMARY, actions: it.ACTIONS,
                            correctiveDate       : correctiveDate, preventiveDate: preventiveDate
                    ]
                }
            }
        } catch (Exception e) {
            log.error("Error Occured During Fetching Quality data --${e.getMessage()}")
            throw e
        } finally {
            sql?.close()
        }
        dataMap['data'] = data

        return dataMap
    }

    Map getRCADataForQualityRecord(Long id, String qualityRecordType, Map<String,Map<Long,String>> allRcaDataMap) {
        Map<String,String> rcaDataMap = new HashMap<>()
        rcaDataMap.put("LATE",ViewHelper.getEmptyLabel())
        rcaDataMap.put("LATE_TYPE",ViewHelper.getEmptyLabel())
        rcaDataMap.put("ROOT_CAUSE",ViewHelper.getEmptyLabel())
        rcaDataMap.put("RESPONSIBLE_PARTY",ViewHelper.getEmptyLabel())
        def qualityRecord
        if(allRcaDataMap) {
            if (qualityRecordType.equals(PvqTypeEnum.CASE_QUALITY.toString())) {
                qualityRecord = QualityCaseData.get(id)
            } else if (qualityRecordType.equals(PvqTypeEnum.SUBMISSION_QUALITY.toString())) {
                qualityRecord = QualitySubmission.get(id)
            } else {
                qualityRecord = QualitySampling.get(id)
            }
            if (qualityRecord.qualityIssueTypeId && allRcaDataMap['LATE_MAP'][qualityRecord.qualityIssueTypeId]) {
                rcaDataMap.put("LATE", allRcaDataMap['LATE_MAP'][qualityRecord.qualityIssueTypeId])
                rcaDataMap.put("LATE_TYPE", allRcaDataMap['LATE_TYPE_MAP'][qualityRecord.qualityIssueTypeId])
                Set<QualityIssueDetail> qualityIssuesDetails = qualityRecord?.qualityIssueDetails?.findAll { it.isPrimary }
                if (qualityIssuesDetails?.size() > 0) {
                    for (QualityIssueDetail qualityIssuesDetail : qualityIssuesDetails) {
                        if (qualityIssuesDetail.isPrimary) {
                            if (allRcaDataMap['ROOT_CAUSE_MAP'][qualityIssuesDetail.rootCauseId]) {
                                rcaDataMap.put("ROOT_CAUSE", allRcaDataMap['ROOT_CAUSE_MAP'][qualityIssuesDetail.rootCauseId])
                                if (allRcaDataMap['RESPONSIBLE_PARTY_MAP'][qualityIssuesDetail.responsiblePartyId]) {
                                    rcaDataMap.put("RESPONSIBLE_PARTY", allRcaDataMap['RESPONSIBLE_PARTY_MAP'][qualityIssuesDetail.responsiblePartyId])
                                }
                            }
                        }
                    }
                }
            }
        }
        return rcaDataMap
    }

    Map<String,Map<Long,String>> getAllRcaDataMap(){
        Map<String,Map<Long,String>> allRcaDataMap = new HashMap<>()
        Map<Long,String> lateDataMap = new HashMap<>()
        Map<Long,String> lateTypeDataMap = new HashMap<>()
        Map<Long,String> rootCauseDataMap = new HashMap<>()
        Map<Long,String> responsiblePartyDataMap = new HashMap<>()
        getQualityIssues().each { late ->
            lateDataMap.put(late.id, late.textDesc)
            lateTypeDataMap.put(late.id, ReasonOfDelayLateTypeEnum.lateTypeIdKeyMap.get(late.lateType)?.name()) //Fetch and put name of lateType in the map
        }
        allRcaDataMap.put("LATE_MAP", lateDataMap)
        allRcaDataMap.put("LATE_TYPE_MAP", lateTypeDataMap)

        getRootCauses().each { rootCause ->
            rootCauseDataMap.put(rootCause.id, rootCause.textDesc)
        }
        allRcaDataMap.put("ROOT_CAUSE_MAP", rootCauseDataMap)

        getResponsibleParties().each { responsibleParty ->
            responsiblePartyDataMap.put(responsibleParty.id, responsibleParty.textDesc)
        }
        allRcaDataMap.put("RESPONSIBLE_PARTY_MAP", responsiblePartyDataMap)
        return allRcaDataMap
    }

    def getQualityDataByCaseNumAndVersion(String type, String caseNum, String version, Long tenantId, String submissionIdentifier) {
        if (type == PvqTypeEnum.CASE_QUALITY.name()) {
            return QualityCaseData.findAllByCaseNumberAndVersionNumberAndTenantIdAndIsDeleted(caseNum, version as Long, tenantId, false)
        } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
            return QualitySubmission.findAllByCaseNumberAndVersionNumberAndTenantIdAndIsDeletedAndSubmissionIdentifier(caseNum, version as Long, tenantId, false, submissionIdentifier)
        } else {
            return QualitySampling.findAllByCaseNumberAndVersionNumberAndTenantIdAndTypeAndIsDeleted(caseNum, version as Long, tenantId, type, false)
        }
    }


    @ReadOnly(connection = 'pva')
    List<ResponsibleParty> getResponsiblePartyList(ReasonOfDelayAppEnum app) {
        ResponsibleParty.findAllByOwnerAppAndHiddenDateIsNull(app.name()).findAll{it}.sort { it.textDesc }
    }

    Map<String, Date> getRanges(params) {
        int periodsNumber = (params["periodsNumber"] as Integer ?: 2) + 1
        int lastX = params["lastX"] as Integer ?: 1
        String timeZone = (userService.currentUser?.preference?.timeZone ?: params.timeZone) ?: "UTC"
        Map<String, Date> result = [:]
        Date previous = new Date();
        if (params.dateRangeType == DateRangeEnum.CUSTOM.name()) {
            SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT)
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
            Date from = sdf.parse(params.dateRangeFrom)
            Date to = sdf.parse(params.dateRangeTo)
            int delta = 1 + (to.getTime() - from.getTime()) / 1000 / 60 / 60 / 24 as Integer//days
            previous = to + 1
            for (int i = 0; i < periodsNumber; i++) {
                def range = RelativeDateConverter.lastXDays(previous, delta, timeZone)
                result["to" + i] = range[1]
                result["from" + i] = range[0]
                previous = range[0]
            }
        } else {

            for (int i = 0; i < periodsNumber; i++) {
                def range = []
                if (params.dateRangeType == DateRangeEnum.LAST_X_DAYS.name()) range = RelativeDateConverter.lastXDays(previous, lastX, timeZone)
                if (params.dateRangeType == DateRangeEnum.LAST_X_WEEKS.name()) range = RelativeDateConverter.lastXWeekActionPlan(previous, lastX, timeZone)
                if (params.dateRangeType == DateRangeEnum.LAST_X_MONTHS.name()) range = RelativeDateConverter.lastXMonthsActionPlan(previous, lastX, timeZone)
                result["to" + i] = range[1]
                result["from" + i] = range[0]
                previous = range[0]
            }
        }
        return result
    }

    Map<String, Map> fetchActionItems(Set rows, Date from, Date to, index, Map<String, Map> result) {
        rows.collate(999).each { list ->
            //date intervals intersection  <=> (StartA <= EndB) and (EndA >= StartB)
            ActionItem.findAllByParentEntityKeyInListAndDateRangeFromLessThanEqualsAndDateRangeToGreaterThanEqualsAndIsDeleted(list, to, from, false).each { ActionItem ai ->
                Map stat = result.get(ai.parentEntityKey)
                if (!stat) {
                    stat = [("completed" + index): 0,
                            ("overdue" + index)  : 0,
                            ("total" + index)    : 0]
                    result.put(ai.parentEntityKey, stat)
                }
                if (stat["total" + index] == null) {
                    stat.putAll([("completed" + index): 0,
                                 ("overdue" + index)  : 0,
                                 ("total" + index)    : 0])
                }
                stat["total" + index]++
                if (ai.status == StatusEnum.CLOSED) {
                    stat["completed" + index]++
                } else {
                    if (ai.dueDate < new Date()) {
                        stat["overdue" + index]++
                    }
                }

            }
        }
        return result
    }

    private Integer toPercent(num, total) {
        if (!num) return 0
        if (!total) return 1001
        return Math.round((num as Double) / (total as Double) * 100)
    }

    @ReadOnly('pva')
    List getAgenciesNames() {
        return AgencyName.pva.findAllByTenantId(Tenants.currentId() as Long)
    }

    List errorTypesForQuality(){
        List<String> allPvqEnums = PvqTypeEnum.toQualityList()
        List resultList = []
        allPvqEnums.each{
            resultList.add(fetchQualityModuleErrorsList(it , Tenants.currentId() as Long))
        }
        return resultList?.flatten()?.unique{a,b ->  a<=>b}
    }

    void deleteAlltheDirectoriesForAttachment(File file) {
        for (File subfile : file.listFiles()) {
            if (subfile.isDirectory()) {
                deleteAlltheDirectoriesForAttachment(subfile)
            }
            subfile.delete()
        }
    }

    //Method to get recipient email according to preference
    public String getPVQRecipientsByEmailPreference(User user, String mode){
        String recipient = null
        User currentUser = userService.getCurrentUser()
        if(user!=currentUser){
            if (mode == Constants.ASSIGNED_TO) {
                if (user?.preference?.pvqEmail?.assignedToMe)
                    recipient = user.email
            }
            if (mode == Constants.ASSIGNED_TO_GROUP) {
                if (user?.preference?.pvqEmail?.assignedToMyGroup)
                    recipient = user.email
            }
            if (mode == Constants.WORKFLOW_CHANGES) {
                if (user?.preference?.pvqEmail?.workflowStateChange)
                    recipient = user.email
            }
        }
        return recipient
    }

    def fetchLastRunDate(jobName) {
        def job = JobRunTrackerPVCPVQ.findByJobName(jobName)
        return job?.lastRunDate ?: new Date(0)
    }

    def updateLastRunDate(jobName, currentRun) {
        def job = JobRunTrackerPVCPVQ.findByJobName(jobName)
        if (!job) {
            job = new JobRunTrackerPVCPVQ(jobName: jobName)
        }
        job.lastRunDate = currentRun
        job.lastUpdated = new Date()  // Correct mapping
        job.save(flush: true, failOnError: true)
    }


    def sendPVQEmailNotifications() {
        def domains = ["QualityCaseData", "QualitySampling", "QualitySubmission"]
        def jobName = 'PVQEmailNotificationJob'
        def currentRun = new Date()
        domains.each { domain ->
            def userCaseMap = fetchPVQData(domain)
            Map<User, List<Map>> userCaseInfoMap = [:]
            Map<UserGroup, List<Map>> groupCaseInfoMap = [:]

            userCaseMap.each { userId, uniqueCaseInfo ->
                User user = User.findById(userId)
                UserGroup userGroup = UserGroup.findById(userId)

                if (userGroup) {
                    if (!groupCaseInfoMap.containsKey(userGroup)) {
                        groupCaseInfoMap[userGroup] = []
                    }
                    uniqueCaseInfo.each { caseInfo ->
                        caseInfo.userGroupName = userGroup.name
                    }
                    groupCaseInfoMap[userGroup] += uniqueCaseInfo
                } else if (user) {
                    if (!userCaseInfoMap.containsKey(user)) {
                        userCaseInfoMap[user] = []
                    }
                    uniqueCaseInfo.each { caseInfo ->
                        caseInfo.userGroupName = null
                    }
                    userCaseInfoMap[user] += uniqueCaseInfo
                }
            }

            // Send emails to individual users
            userCaseInfoMap.each { user, combinedCaseInfoList ->
                def allRecordIds = combinedCaseInfoList*.qualityId
                def qualityIds = allRecordIds.take(500)
                def hasLargeIds = allRecordIds.size() > 500
                def linkFilterParam = qualityIds ? URLEncoder.encode(qualityIds.toString(), 'UTF-8') : ''
                sendEmailToUser(user, combinedCaseInfoList, domain, linkFilterParam, hasLargeIds)
            }

            // Consolidate and send single email for group users
            sendConsolidatedEmailForGroups(groupCaseInfoMap, domain)

            // ================= Handle Workflow State Changes =================
            def workflowChanges = fetchQualityWorkflowStateChanges(domain)
            Map<User, List<Map>> userWorkflowChangesMap = [:]
            Map<UserGroup, List<Map>> groupWorkflowChangesMap = [:]

            // Process workflow state changes for users and user groups
            workflowChanges.each { userId, uniqueWorkflowChanges ->
                User user = User.findById(userId)
                UserGroup userGroup = UserGroup.findById(userId)

                if (userGroup) {
                    if (!groupWorkflowChangesMap.containsKey(userGroup)) {
                        groupWorkflowChangesMap[userGroup] = []
                    }
                    uniqueWorkflowChanges.each { caseInfo ->
                        caseInfo.userGroupName = userGroup.name
                    }
                    groupWorkflowChangesMap[userGroup] += uniqueWorkflowChanges
                } else if (user) {
                    if (!userWorkflowChangesMap.containsKey(user)) {
                        userWorkflowChangesMap[user] = []
                    }
                    uniqueWorkflowChanges.each { caseInfo ->
                        caseInfo.userGroupName = null
                    }
                    userWorkflowChangesMap[user] += uniqueWorkflowChanges
                }

            }

            // Send Workflow Emails to Users
            userWorkflowChangesMap.each { user, workflowCaseList ->
                def allRecordIds = workflowCaseList*.qualityId
                def qualityIds = allRecordIds.take(500)
                def hasLargeIds = allRecordIds.size() > 500
                def linkFilterParam = qualityIds ? URLEncoder.encode(qualityIds.toString(), 'UTF-8') : ''
                sendWorkflowChangeEmailToUser(user, workflowCaseList, domain, linkFilterParam, hasLargeIds)
            }
            // Send Consolidated Workflow Emails to Groups
            sendConsolidatedWorkflowEmailForGroups(groupWorkflowChangesMap, domain)
        }
        updateLastRunDate(jobName, currentRun)

    }


    private void  sendEmailToUser(User user, List<Map> caseInfoList, String domain, def linkFilterParam, def hasLargeIds) {
        if (domain == "QualitySampling") {
            def casesByType = caseInfoList.groupBy { it.type }
            casesByType.each { samplingType, cases ->
                def samplingConfig = Holders.config.qualityModule.additional.find { it.name == samplingType }
                def samplingLabel = samplingConfig?.label
                String content = groovyPageRenderer.render(
                        template: '/mail/pvquality/pvqNotification',
                        model: [
                                'userCaseInfo': cases,
                                'domain': domain,
                                'samplingType':samplingLabel,
                                'samplingLink':samplingType,
                                'timeframe': new Date().format(DATE_FMT),
                                'linkFilter':linkFilterParam,
                                'hasLargeIds'  : hasLargeIds
                        ]
                )

                String recipientsEmail = getPVQRecipientsByEmailPreference(user, Constants.ASSIGNED_TO)
                recipientsEmail = emailService.getValidEmailId(recipientsEmail)
                String emailSubject = fetchEmailSubject(domain, "user", samplingLabel)
                sendEmailPVQ(content, recipientsEmail, emailSubject)
            }
        } else {
            String content = groovyPageRenderer.render(
                    template: '/mail/pvquality/pvqNotification',
                    model: [
                            'userCaseInfo': caseInfoList,
                            'domain': domain,
                            'timeframe': new Date().format(DATE_FMT),
                            'linkFilter':linkFilterParam,
                            'hasLargeIds'  : hasLargeIds
                    ]
            )

            String recipientsEmail = getPVQRecipientsByEmailPreference(user, Constants.ASSIGNED_TO)
            recipientsEmail = emailService.getValidEmailId(recipientsEmail)
            String emailSubject = fetchEmailSubject(domain, "user")
            sendEmailPVQ(content, recipientsEmail, emailSubject)
        }
    }

    private void sendConsolidatedEmailForGroups(Map<UserGroup, List<Map>> groupCaseInfoMap, String domain) {
        Map<User, List<Map>> consolidatedCaseMap = [:]

        groupCaseInfoMap.each { userGroup, caseInfoList ->
            userGroup.users.each { user ->
                consolidatedCaseMap[user] = (consolidatedCaseMap[user] ?: []) + caseInfoList
            }
        }

        consolidatedCaseMap.each { user, allCases ->
            if (!allCases) return
            def allRecordIds = allCases*.qualityId
            def qualityIds = allRecordIds.take(500)
            def hasLargeIds = allRecordIds.size() > 500
            def linkFilterParam = qualityIds ? URLEncoder.encode(qualityIds.toString(), 'UTF-8') : ''

            if (domain == "QualitySampling") {
                // Group by sampling type only
                def samplingTypeGrouped = allCases.groupBy { it.type }

                samplingTypeGrouped.each { samplingType, casesByType ->
                    def samplingConfig = Holders.config.qualityModule.additional.find { it.name == samplingType }
                    def samplingLabel = samplingConfig?.label

                    String content = groovyPageRenderer.render(
                            template: '/mail/pvquality/pvqNotification',
                            model: [
                                    groupedCases : casesByType,
                                    domain       : domain,
                                    samplingType : samplingLabel,
                                    samplingLink : samplingType,
                                    timeframe: new Date().format(DATE_FMT),
                                    linkFilter   : linkFilterParam,
                                    hasLargeIds  : hasLargeIds
                            ]
                    )

                    String recipientsEmail = getPVQRecipientsByEmailPreference(user, Constants.ASSIGNED_TO_GROUP)
                    recipientsEmail = emailService.getValidEmailId(recipientsEmail)
                    String emailSubject = fetchEmailSubject(domain, "userGroup", samplingLabel)
                    sendEmailPVQ(content, recipientsEmail, emailSubject)
                }

            } else {
                String content = groovyPageRenderer.render(
                        template: '/mail/pvquality/pvqNotification',
                        model: [
                                groupedCases : allCases,
                                domain       : domain,
                                timeframe    : new Date().format(DATE_FMT),
                                linkFilter   : linkFilterParam,
                                hasLargeIds  : hasLargeIds
                        ]
                )

                String recipientsEmail = getPVQRecipientsByEmailPreference(user, Constants.ASSIGNED_TO_GROUP)
                recipientsEmail = emailService.getValidEmailId(recipientsEmail)
                String emailSubject = fetchEmailSubject(domain, "userGroup")
                sendEmailPVQ(content, recipientsEmail, emailSubject)
            }
        }
    }

    private void sendWorkflowChangeEmailToUser(User user, List<Map> workflowCaseList, String domain, def linkFilterParam, def hasLargeIds) {

        if (domain == "QualitySampling") {
            def casesByType = workflowCaseList.groupBy { it.type }
            casesByType.each { samplingType, cases ->
                def samplingConfig = Holders.config.qualityModule.additional.find { it.name == samplingType }
                def samplingLabel = samplingConfig?.label
                String content = groovyPageRenderer.render(
                        template: '/mail/pvquality/pvqWorkflow',
                        model: [
                                'workflowStateChanges': cases,
                                'domain': domain,
                                'samplingType':samplingLabel,
                                'samplingLink':samplingType,
                                'timeframe':new Date().format(DATE_FMT),
                                'linkFilter':linkFilterParam,
                                'hasLargeIds':hasLargeIds
                        ]
                )

                String recipientsEmail = getPVQRecipientsByEmailPreference(user, Constants.WORKFLOW_CHANGES)
                recipientsEmail = emailService.getValidEmailId(recipientsEmail)
                String emailSubject = fetchEmailSubject(domain, "workflow", samplingLabel)
                sendEmailPVQ(content, recipientsEmail, emailSubject)
            }
        } else {
            String content = groovyPageRenderer.render(
                    template: '/mail/pvquality/pvqWorkflow',
                    model: [
                            'workflowStateChanges': workflowCaseList,
                            'domain': domain,
                            'timeframe':new Date().format(DATE_FMT),
                            'linkFilter':linkFilterParam
                    ]
            )

            String recipientsEmail = getPVQRecipientsByEmailPreference(user, Constants.WORKFLOW_CHANGES)
            recipientsEmail = emailService.getValidEmailId(recipientsEmail)
            String emailSubject = fetchEmailSubject(domain, "workflow")
            sendEmailPVQ(content, recipientsEmail, emailSubject)
        }
    }

    private void sendConsolidatedWorkflowEmailForGroups(Map<UserGroup, List<Map>> groupWorkflowChangesMap, String domain) {
        Map<User, List<Map>> consolidatedCaseMap = [:]

        groupWorkflowChangesMap.each { userGroup, caseInfoList ->
            userGroup.users.each { user ->
                consolidatedCaseMap[user] = (consolidatedCaseMap[user] ?: []) + caseInfoList
            }
        }

        consolidatedCaseMap.each { user, allCases ->
            if (!allCases) return
            def allRecordIds = allCases*.qualityId
            def qualityIds = allRecordIds.take(500)
            def hasLargeIds = allRecordIds.size() > 500
            def linkFilterParam = qualityIds ? URLEncoder.encode(qualityIds.toString(), 'UTF-8') : ''


            if (domain == "QualitySampling") {
                // Group by sampling type only
                def samplingTypeGrouped = allCases.groupBy { it.type }

                samplingTypeGrouped.each { samplingType, casesByType ->
                    def samplingConfig = Holders.config.qualityModule.additional.find { it.name == samplingType }
                    def samplingLabel = samplingConfig?.label

                    String content = groovyPageRenderer.render(
                            template: '/mail/pvquality/pvqWorkflow',
                            model: [
                                    groupedWorkflowStateChanges : casesByType,
                                    domain       : domain,
                                    samplingType : samplingLabel,
                                    samplingLink : samplingType,
                                    timeframe    : new Date().format(DATE_FMT),
                                    linkFilter   : linkFilterParam,
                                    hasLargeIds  : hasLargeIds
                            ]
                    )

                    String recipientsEmail = getPVQRecipientsByEmailPreference(user, Constants.WORKFLOW_CHANGES)
                    recipientsEmail = emailService.getValidEmailId(recipientsEmail)
                    String emailSubject = fetchEmailSubject(domain, "workflow", samplingLabel)
                    sendEmailPVQ(content, recipientsEmail, emailSubject)
                }
            } else {
                String content = groovyPageRenderer.render(
                        template: '/mail/pvquality/pvqWorkflow',
                        model: [
                                groupedWorkflowStateChanges: allCases,
                                domain                     : domain,
                                timeframe                  : new Date().format(DATE_FMT),
                                linkFilter                 : linkFilterParam,
                                hasLargeIds                : hasLargeIds
                        ]
                )

                String recipientsEmail = getPVQRecipientsByEmailPreference(user, Constants.WORKFLOW_CHANGES)
                recipientsEmail = emailService.getValidEmailId(recipientsEmail)
                String emailSubject = fetchEmailSubject(domain, "workflow")
                sendEmailPVQ(content, recipientsEmail, emailSubject)
            }
        }
    }

// Common email sender
    private void sendEmailPVQ(String content, String recipientsEmail, String emailSubject) {
        if (recipientsEmail && recipientsEmail.trim().length() != 0) {
            log.info("Sending PVQ Notification email to ${recipientsEmail}")
            sendMail {
                multipart true
                async true
                to recipientsEmail
                subject emailSubject
                html(content)
                inline 'pvreportsMailBackground', 'image/jpg', resourceLoader.getResource("/images/background.jpg")?.getFile()
                inline 'pvreportslogo', 'image/jpg', resourceLoader.getResource("/images/pv_reports_logo.png")?.getFile()
            }
        }
    }
    def fetchEmailSubject(String domain, String type, String samplingType = null) {
        String emailSubject
        if (type == "user") {
            if (domain == "QualityCaseData") {
                emailSubject = ViewHelper.getMessage("pvq.caseDataQuality.email.subject.user.label")
            } else if (domain == "QualitySubmission") {
                emailSubject = ViewHelper.getMessage("pvq.caseSubmission.email.subject.user.label")
            } else if (domain == "QualitySampling") {
                emailSubject = ViewHelper.getMessage("pvq.sampling.email.subject.user.label",samplingType)
            }
            } else if (type == "userGroup") {
                if (domain == "QualityCaseData") {
                    emailSubject = ViewHelper.getMessage("pvq.caseDataQuality.email.subject.group.label")
                } else if (domain == "QualitySubmission") {
                    emailSubject = ViewHelper.getMessage("pvq.caseSubmission.email.subject.group.label")
                } else if (domain == "QualitySampling") {
                    emailSubject = ViewHelper.getMessage("pvq.sampling.email.subject.group.label",samplingType)
                }
            } else if (type == "workflow") {
                if (domain == "QualityCaseData") {
                    emailSubject = ViewHelper.getMessage("pvq.caseDataQuality.email.subject.workflow.label")
                } else if (domain == "QualitySubmission") {
                    emailSubject = ViewHelper.getMessage("pvq.caseSubmission.email.subject.workflow.label")
                } else if (domain == "QualitySampling") {
                    emailSubject = ViewHelper.getMessage("pvq.sampling.email.subject.workflow.label",samplingType)
                }
            }
            if (Holders.config.getProperty('reports.email.subject.prepend')) {
                emailSubject = Holders.config.getProperty('reports.email.subject.prepend') + emailSubject
            }
            return emailSubject
    }


    def fetchPVQData(String domain) {
        def jobName = 'PVQEmailNotificationJob'
        def domainClass = domain == 'QualitySubmission' ? QualitySubmission :
                (domain == 'QualityCaseData' ? QualityCaseData : QualitySampling)

        def lastRunDate = fetchLastRunDate(jobName)
        def currentRun = new Date()

// Dynamic dataType assignment
        def dataType = domain == 'QualitySubmission' ? 'SUBMISSION_QUALITY' :
                (domain == 'QualityCaseData' ? 'CASE_QUALITY' : 'SAMPLING')
        def includeType = domain == 'QualitySampling'
        def results = domainClass.createCriteria().list {
            projections {
                property("caseNumber")  // CASE_NUM
                property("reportId")
                property("versionNumber")    // METADATA
                property("id")          // quality.id
                property("assignedToUser.id")      // assigned to user
                property("assignedToUserGroup.id") // assigned to group
                property("assigner")
                if (includeType) {
                    property("type")
                }
            }
            and {
                ge("assigneeUpdatedDate", lastRunDate)
                lt("assigneeUpdatedDate", currentRun)
                eq("isDeleted", false)
            }
            or {
                isNotNull("assignedToUser")
                isNotNull("assignedToUserGroup")
            }
            order("assignedToUser", "asc")
        }

        def userCaseMap = [:]
        results.each { row ->
            def caseNum = row[0]
            def reportId = row[1]
            def versionNumber = row[2]
            def qualityId = row[3]
            def assignedUserId = row[4]
            def assignedGroupId = row[5]
            def assigner = row[6]
            def type = includeType ? row[7] : null

            def assignedEntityId = assignedUserId ?: assignedGroupId
            if (assignedEntityId!=assigner?.id) {
                def caseInfo = [
                        caseNum         : caseNum,
                        reportId        : reportId,
                        qualityId       : qualityId,
                        masterVersionNum: versionNumber,
                        dataType        : dataType
                ]
                if (includeType) {
                    caseInfo.type = type
                }
                userCaseMap.computeIfAbsent(assignedEntityId) { [] }.add(caseInfo)
            }
        }

        //updateLastRunDate(jobName, currentRun)
        return userCaseMap
    }



    def fetchQualityWorkflowStateChanges(String domain) {
        def jobName = 'PVQEmailNotificationJob'
        def lastRunDate = fetchLastRunDate(jobName)
        def currentRun = new Date()

        // Dynamic association based on domain
        def association = domain == 'QualitySubmission' ? 'qualitySubmission' :
                (domain == 'QualityCaseData' ? 'qualityCaseData' : 'qualitySampling')

        // Dynamic alias mapping
        def domainAlias = domain == 'QualitySubmission' ? 'qs' :
                (domain == 'QualityCaseData' ? 'qcd' : 'qsmp')

        def dataType = domain == 'QualitySubmission' ? 'SUBMISSION_QUALITY' :
                (domain == 'QualityCaseData' ? 'CASE_QUALITY' : 'SAMPLING')

        def includeType = domain == 'QualitySampling'
        def results = WorkflowJustification.createCriteria().list {
            createAlias(association, domainAlias)  // Dynamic association
            projections {
                distinct("${domainAlias}.caseNumber")
                property('assignedToUser.id')
                property('assignedToUserGroup.id')
                property('fromState.name')
                property('toState.name')
                property("${domainAlias}.reportId")
                property("${domainAlias}.id")
                property("${domainAlias}.versionNumber")
                property("${domainAlias}.assigner")
                if (includeType) {
                    property("${domainAlias}.type")
                }
            }
            createAlias('fromState', 'fromState')
            createAlias('toState', 'toState')

            and {
                ge("lastUpdated", lastRunDate)
                lt("lastUpdated", currentRun)
                eq("${domainAlias}.isDeleted", false)
            }
            or {
                isNotNull("assignedToUser")
                isNotNull("assignedToUserGroup")
            }
            order("assignedToUser", "asc")
        }

        def userCaseMap = [:]

        results.each { row ->
            def caseNum = row[0]
            def assignedUserId = row[1]
            def assignedGroupId = row[2]
            def fromStateName = row[3]
            def toStateName = row[4]
            def reportId = row[5]
            def qualityId = row[6]
            def versionNumber = row[7]
            def assigner = row[8]
            def type = includeType ? row[9] : null

            def assignedEntityId = assignedUserId ?: assignedGroupId
            if (assignedEntityId != assigner?.id) {
                def caseInfo = [
                        caseNum         : caseNum,
                        reportId        : reportId,
                        qualityId       : qualityId,
                        fromState       : fromStateName,
                        toState         : toStateName,
                        masterVersionNum: versionNumber,
                        dataType        : dataType
                ]
                if (includeType) {
                    caseInfo.type = type
                }
                userCaseMap.computeIfAbsent(assignedEntityId) { [] }.add(caseInfo)
            }
        }
        //updateLastRunDate(jobName, currentRun)
        return userCaseMap
    }

    List getActionPlanData(Map params, Map<String, Date> ranges) {
        Map responsiblePartyName
        ResponsibleParty.withNewTransaction {
            responsiblePartyName = ResponsibleParty.findAllByOwnerApp(ReasonOfDelayAppEnum.PVQ.name()).findAll { it }.collectEntries { [("" + it?.id): it?.textDesc] }
        }
        responsiblePartyName.put("0", ViewHelper.getEmptyLabel())
        Sql sql
        List result = []
        try {
            TreeSet rows = new TreeSet()
            sql = new Sql(utilService.getReportConnectionForPVR())
            List<Long> responsiblePartyFilter = FilterUtil.parseParamList(params.responsiblePartyFilter)?.collect { it as Long }
            List<String> observationFilter = FilterUtil.parseParamList(params.observationFilter)
            List<String> issueTypeFilter = FilterUtil.parseParamList(params.issueTypeFilter)
            List<String> errorTypeFilter = FilterUtil.parseParamList(params.errorType)
            List periods = []
            for (int i = 0; ; i++) {
                if (!ranges["from" + i]) break;
                periods << fetchActonPlanData(sql, ranges["from" + i], ranges["to" + i], responsiblePartyName, rows, responsiblePartyFilter, errorTypeFilter, observationFilter, params.workflowFilter, issueTypeFilter, params.priorityFilter, params)
            }
            Map<String, Map> actionItems = [:]
            for (int i = 0; ; i++) {
                if (!ranges["from" + i]) break;
                fetchActionItems(rows, ranges["from" + i], ranges["to" + i], i, actionItems)
            }
            rows.each {
                Map lastRow = getActionPlanNotNullRow(periods[0], it, responsiblePartyName)
                Map actionItemStat = actionItems.get(it)
                Map fullRow = [
                        responsibleParty  : lastRow.responsibleParty,
                        responsiblePartyId: lastRow.responsiblePartyId,
                        observation       : lastRow.observation,
                        observationCode   : lastRow.observationCode,
                        errorType         : lastRow.errorType,
                        priority          : lastRow.priority
                ]
                Integer previousNumber = null
                periods.eachWithIndex { periodRow, int i ->
                    Map periodRowNotNull = getActionPlanNotNullRow(periods[i], it, responsiblePartyName)
                    fullRow["lastNumber" + i] = periodRowNotNull.number
                    fullRow["lastVendor" + i] = periodRowNotNull.ofVendor
                    fullRow["lastIssue" + i] = periodRowNotNull.ofIssue
                    fullRow["lastObservation" + i] = periodRowNotNull.ofObservation
                    fullRow["lastPriority" + i] = periodRowNotNull.ofPriority
                    fullRow["completed" + i] = toPercent(actionItemStat?.get("completed" + i), actionItemStat?.get("total" + i))
                    fullRow["overdue" + i] = toPercent(actionItemStat?.get("overdue" + i), actionItemStat?.get("total" + i))
                    fullRow["total" + i] = actionItemStat?.get("total" + i) ?: 0
                    if (previousNumber != null) {
                        fullRow["lastToPrevious" + (i - 1)] = toPercent(previousNumber - periodRowNotNull.number, periodRowNotNull.number)
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

    private String getWhereClause(List responsiblePartyFilter, List<String> errorTypeFilter, String workflowFilter, List<String> issueTypeFilter, Boolean primaryOnly, Map queryParams, String priorityFilter) {
        String whereClause = ""
        if (responsiblePartyFilter) {
            whereClause += "and ("
            if (responsiblePartyFilter.contains(0L) || responsiblePartyFilter.contains("0")) whereClause += " rca.RESPONSIBLE_PARTY_ID is null or "
            whereClause += "  rca.RESPONSIBLE_PARTY_ID in (" + responsiblePartyFilter.join(",") + ") "
            whereClause += ")"
        }
        if (errorTypeFilter) {
            whereClause += " AND ERROR_TYPE in (${errorTypeFilter?.collect { "'" + it + "'" }.join(",")})"
        }
        if (issueTypeFilter) {
            whereClause += " AND QUALITY_ISSUE_TYPE_ID in (" + issueTypeFilter.join(',') + ") "
        }
        if (priorityFilter) {
            whereClause += "and ("
            if (priorityFilter.contains(ViewHelper.getEmptyLabel())) whereClause += " PRIORITY is null or "
            whereClause += " PRIORITY in ('" + priorityFilter.replaceAll(";", "','") + "') "
            whereClause += ")"
        }
        if (primaryOnly) {
            whereClause += " AND IS_PRIMARY=1 "
        }
        if (workflowFilter) {
            Set<WorkflowState> workflowStates = []
            if (workflowFilter in ["final", "notFinal"]) {
                WorkflowRule.findAllByConfigurationTypeEnumInListAndIsDeletedAnd(WorkflowConfigurationTypeEnum.getAllQuality(), false).each {
                    if (it.targetState?.finalState)
                        workflowStates.add(it.targetState)
                }
                if (workflowStates) {
                    whereClause += " AND data.WORKFLOW_STATE_ID ${workflowFilter == "notFinal" ? "NOT" : ""} in (${workflowStates*.id.join(",")})"
                } else if (workflowFilter == "final") {
                    WorkflowRule.findAllByConfigurationTypeEnumInListAndIsDeletedAnd(WorkflowConfigurationTypeEnum.getAllQuality(), false).each {
                        if (!it.targetState?.finalState)
                            workflowStates.add(it.targetState)
                    }
                    whereClause += " AND data.WORKFLOW_STATE_ID NOT in (${workflowStates*.id.join(",")})"
                }
            } else {
                whereClause += " AND data.WORKFLOW_STATE_ID in (${workflowFilter.replaceAll(";", ",")}) "
            }

        }
        return whereClause
    }

    private String getSamplingWhereClause(List<String> observationFilter) {
        String samplingWhere = ""
        if (observationFilter) {
            samplingWhere = " AND  data.TYPE in (${observationFilter.collect { "'" + it + "'" }.join(",")})"
        }
        return samplingWhere;
    }

    static def fetchDictionaryLabels(Map dectionaryLabels) {
        ResponsibleParty.withNewTransaction {
            dectionaryLabels.RootCause = RootCause.findAllByOwnerApp(ReasonOfDelayAppEnum.PVQ.name()).findAll { it }.collectEntries { [("" + it.id): it.textDesc] }
            dectionaryLabels.ResponsibleParty = ResponsibleParty.findAllByOwnerApp(ReasonOfDelayAppEnum.PVQ.name()).findAll { it }.collectEntries { [("" + it.id): it.textDesc] }
            dectionaryLabels.CorrectiveAction = CorrectiveAction.findAllByOwnerApp(ReasonOfDelayAppEnum.PVQ.name()).findAll { it }.collectEntries { [("" + it.id): it.textDesc] }
            dectionaryLabels.PreventativeAction = PreventativeAction.findAllByOwnerApp(ReasonOfDelayAppEnum.PVQ.name()).findAll { it }.collectEntries { [("" + it.id): it.textDesc] }

        }
    }


    List fetchActonPlanCasesData(Date from, Date to, List responsiblePartyFilter, List<String> errorTypeFilter, List<String> observationFilter, String workflowFilter, List<String> issueTypeFilter, String priorityFilter, Boolean primaryOnly, String timeZone, Locale locale) {
        Sql sql
        List result = []

        Map dectionaryLabels = [:]
        fetchDictionaryLabels(dectionaryLabels)
        try {
            sql = new Sql(utilService.getReportConnectionForPVR())
            Map queryParams = [from: new java.sql.Date(from.getTime()), to: new java.sql.Date(to.getTime())]
            String whereClause = getWhereClause(responsiblePartyFilter, errorTypeFilter, workflowFilter, issueTypeFilter, primaryOnly, queryParams, priorityFilter)
            String samplingWhere = getSamplingWhereClause(observationFilter)

            String caseQuery = """select data.ID, CASE_NUM, ASSIGNED_TO_USER, ASSIGNED_TO_USERGROUP, WORKFLOW_STATE_ID, JSON_VALUE(data.METADATA, '\$.masterVersionNum') CASE_VER,'${PvqTypeEnum.CASE_QUALITY.name()}' TYPE,ERROR_TYPE,
                ROOT_CAUSE_ID,CORRECTIVE_ACTION_ID,RESPONSIBLE_PARTY_ID,PREVENTATIVE_ACTION_ID,CORRECTIVE_DATE,PREVENTATIVE_DATE,IS_PRIMARY,INVESTIGATION,SUMMARY,ACTIONS
                from QUALITY_CASE_DATA data left join QUALITY_CASE_ISSUE_DETAILS data_rca on(data.id=data_rca.QUALITY_CASE_ID) left join QUALITY_ISSUE_DETAIL rca on (rca.ID = data_rca.QUALITY_ISSUE_DETAIL_ID)
                where data.ISDELETED=0 and data.DATE_CREATED>:from and data.DATE_CREATED<:to and tenant_id=${Tenants.currentId()} ${whereClause}"""
            String submissionQuery = """select data.ID, CASE_NUM, ASSIGNED_TO_USER, ASSIGNED_TO_USERGROUP, WORKFLOW_STATE_ID, JSON_VALUE(data.METADATA, '\$.masterVersionNum') CASE_VER,'${PvqTypeEnum.SUBMISSION_QUALITY.name()}' TYPE, ERROR_TYPE,
                ROOT_CAUSE_ID,CORRECTIVE_ACTION_ID,RESPONSIBLE_PARTY_ID,PREVENTATIVE_ACTION_ID,CORRECTIVE_DATE,PREVENTATIVE_DATE,IS_PRIMARY,INVESTIGATION,SUMMARY,ACTIONS
                from QUALITY_SUBMISSION data left join QUALITY_SUB_ISSUE_DETAILS data_rca on(data.id=data_rca.QUALITY_SUBMISSION_ID) left join QUALITY_ISSUE_DETAIL rca on (rca.ID = data_rca.QUALITY_ISSUE_DETAIL_ID)
                where data.ISDELETED=0 and data.DATE_CREATED>:from and data.DATE_CREATED<:to and tenant_id=${Tenants.currentId()} ${whereClause}"""
            String samplingQuery = """select data.ID, CASE_NUM, ASSIGNED_TO_USER, ASSIGNED_TO_USERGROUP, WORKFLOW_STATE_ID, JSON_VALUE(data.METADATA, '\$.masterVersionNum') CASE_VER,TYPE, ERROR_TYPE,
                ROOT_CAUSE_ID,CORRECTIVE_ACTION_ID,RESPONSIBLE_PARTY_ID,PREVENTATIVE_ACTION_ID,CORRECTIVE_DATE,PREVENTATIVE_DATE,IS_PRIMARY,INVESTIGATION,SUMMARY,ACTIONS
                from QUALITY_SAMPLING data left join QUALITY_SAMPL_ISSUE_DETAILS data_rca on(data.id=data_rca.QUALITY_SAMPLING_ID) left join QUALITY_ISSUE_DETAIL rca on (rca.ID = data_rca.QUALITY_ISSUE_DETAIL_ID) 
                where data.ISDELETED=0 and data.DATE_CREATED>:from and data.DATE_CREATED<:to and tenant_id=${Tenants.currentId()} ${whereClause} ${samplingWhere}"""

            List caseList = []
            if (!observationFilter || observationFilter.contains(PvqTypeEnum.CASE_QUALITY.name())) caseList.addAll(sql.rows(caseQuery, queryParams))
            if (!observationFilter || observationFilter.contains(PvqTypeEnum.SUBMISSION_QUALITY.name())) caseList.addAll(sql.rows(submissionQuery, queryParams))
            caseList.addAll(sql.rows(samplingQuery, queryParams))

            caseList.each {
                String assignedTo = ""
                if (it['ASSIGNED_TO_USER']) assignedTo = User.get(it['ASSIGNED_TO_USER'] as Long)?.fullName
                if (it['ASSIGNED_TO_USERGROUP']) assignedTo = UserGroup.get(it['ASSIGNED_TO_USERGROUP'] as Long)?.name

                result.add(
                        [
                                id                : it['ID'],
                                caseNumber        : it['CASE_NUM'],
                                caseVersion       : it['CASE_VER'],
                                observation       : it['TYPE'],
                                assignedTo        : assignedTo,
                                rootCause         : dectionaryLabels.RootCause["" + it['ROOT_CAUSE_ID']] ?: "",
                                correctiveAction  : dectionaryLabels.CorrectiveAction["" + it['CORRECTIVE_ACTION_ID']] ?: "",
                                responsibleParty  : dectionaryLabels.ResponsibleParty["" + it['RESPONSIBLE_PARTY_ID']] ?: "",
                                preventativeAction: dectionaryLabels.PreventativeAction["" + it['PREVENTATIVE_ACTION_ID']] ?: "",
                                correctiveDate    : DateUtil.getShortDateStringForLocaleAndTimeZone(it.CORRECTIVE_DATE?.timestampValue(), locale, timeZone),
                                preventativeDate  : DateUtil.getShortDateStringForLocaleAndTimeZone(it.PREVENTATIVE_DATE?.timestampValue(), locale, timeZone),
                                investigation     : it['INVESTIGATION'],
                                summary           : it['SUMMARY'],
                                actions           : it['ACTIONS'],
                                primary           : it['IS_PRIMARY'],
                                errorType         : it['ERROR_TYPE'],
                                workFlowState     : it['WORKFLOW_STATE_ID'] ? WorkflowState.get(it['WORKFLOW_STATE_ID'] as Long)?.name : ""
                        ])
            }
            return result
        } finally {
            sql?.close()
        }
    }

    private Map fetchActonPlanData(Sql sql, Date from, Date to, Map responsiblePartyName, Set rows, List responsiblePartyFilter, List<String> errorTypeFilter, List<String> observationFilter, String workflowFilter, List<String> issueTypeFilter, String priorityFilter, Map params) {
        Map queryParams = [from: new java.sql.Date(from.getTime()), to: new java.sql.Date(to.getTime())]
        String whereClause = getWhereClause(responsiblePartyFilter, errorTypeFilter, workflowFilter, issueTypeFilter, params["primaryOnly"] as Boolean, queryParams, priorityFilter)
        String samplingWhere = getSamplingWhereClause(observationFilter)

        String caseQuery = """select nvl(RESPONSIBLE_PARTY_ID,0) RESPONSIBLE_PARTY_ID, nvl(PRIORITY,'${ViewHelper.getEmptyLabel()}') PRIORITY,'${PvqTypeEnum.CASE_QUALITY.name()}' TYPE, ERROR_TYPE, count( distinct data.id)  as Num  from QUALITY_CASE_DATA data left join QUALITY_CASE_ISSUE_DETAILS data_rca on(data.id=data_rca.QUALITY_CASE_ID) left join QUALITY_ISSUE_DETAIL rca on (rca.ID = data_rca.QUALITY_ISSUE_DETAIL_ID)
                where data.ISDELETED=0 and data.DATE_CREATED>:from and data.DATE_CREATED<:to and tenant_id=${Tenants.currentId()} ${whereClause}
                group by  ERROR_TYPE, PRIORITY, RESPONSIBLE_PARTY_ID"""
        String submissionQuery = """select nvl(RESPONSIBLE_PARTY_ID,0) RESPONSIBLE_PARTY_ID, nvl(PRIORITY,'${ViewHelper.getEmptyLabel()}') PRIORITY,'${PvqTypeEnum.SUBMISSION_QUALITY.name()}' TYPE, ERROR_TYPE, count( distinct data.id) as Num
                from QUALITY_SUBMISSION data left join QUALITY_SUB_ISSUE_DETAILS data_rca on(data.id=data_rca.QUALITY_SUBMISSION_ID) left join QUALITY_ISSUE_DETAIL rca on (rca.ID = data_rca.QUALITY_ISSUE_DETAIL_ID)
                where data.ISDELETED=0 and data.DATE_CREATED>:from and data.DATE_CREATED<:to and tenant_id=${Tenants.currentId()} ${whereClause}
                group by  ERROR_TYPE, PRIORITY, RESPONSIBLE_PARTY_ID"""
        String samplingQuery = """select nvl(RESPONSIBLE_PARTY_ID,0) RESPONSIBLE_PARTY_ID,nvl(PRIORITY,'${ViewHelper.getEmptyLabel()}') PRIORITY, TYPE, ERROR_TYPE, count( distinct data.id)  as Num
                from QUALITY_SAMPLING data left join QUALITY_SAMPL_ISSUE_DETAILS data_rca on(data.id=data_rca.QUALITY_SAMPLING_ID) left join QUALITY_ISSUE_DETAIL rca on (rca.ID = data_rca.QUALITY_ISSUE_DETAIL_ID) 
                where data.ISDELETED=0 and data.DATE_CREATED>:from and data.DATE_CREATED<:to and tenant_id=${Tenants.currentId()} ${whereClause} ${samplingWhere}
                group by ERROR_TYPE, RESPONSIBLE_PARTY_ID, PRIORITY, TYPE"""

        List caseList = (!observationFilter || observationFilter.contains(PvqTypeEnum.CASE_QUALITY.name())) ? sql.rows(caseQuery, queryParams) : []
        List submissionList = (!observationFilter || observationFilter.contains(PvqTypeEnum.SUBMISSION_QUALITY.name())) ? sql.rows(submissionQuery, queryParams) : []
        List samplingList = sql.rows(samplingQuery, queryParams)
        List currentPeriodDataList = caseList + submissionList + samplingList
        Map errorTypeTotal = [:]
        Map responsiblePartyTotal = [:]
        Map observationTotal = [:]
        Map priorityTotal = [:]
        currentPeriodDataList.each {
            int num = (it['NUM'] as Integer)
            if (responsiblePartyTotal[it['RESPONSIBLE_PARTY_ID']] == null) responsiblePartyTotal[it['RESPONSIBLE_PARTY_ID']] = 0
            responsiblePartyTotal[it['RESPONSIBLE_PARTY_ID']] += num
            if (observationTotal[it['TYPE']] == null) observationTotal[it['TYPE']] = 0
            observationTotal[it['TYPE']] += num
            if (errorTypeTotal[it['ERROR_TYPE']] == null) errorTypeTotal[it['ERROR_TYPE']] = 0
            errorTypeTotal[it['ERROR_TYPE']] += num
            if (priorityTotal[it['PRIORITY']] == null) priorityTotal[it['PRIORITY']] = 0
            priorityTotal[it['PRIORITY']] += num
        }
        Map result = [:]
        currentPeriodDataList.each {
            String type = getLabelForType(it['TYPE']) ?: it['TYPE']
            String responsibleParty = responsiblePartyName["" + it['RESPONSIBLE_PARTY_ID']] ?: ("" + it['RESPONSIBLE_PARTY_ID'])
            String key = it['RESPONSIBLE_PARTY_ID'] + "_@_" + it['TYPE'] + "_@_" + it['ERROR_TYPE'] + "_@_" + it['PRIORITY']
            rows.add(key)
            result.put(key,
                    [
                            responsibleParty  : responsibleParty,
                            responsiblePartyId: it['RESPONSIBLE_PARTY_ID'],
                            observation       : type,
                            observationCode   : it['TYPE'],
                            errorType         : it['ERROR_TYPE'],
                            priority          : it['PRIORITY'],
                            number            : it['NUM'] as Integer,
                            ofVendor          : toPercent(it['NUM'], responsiblePartyTotal[it['RESPONSIBLE_PARTY_ID']]),
                            ofIssue           : toPercent(it['NUM'], errorTypeTotal[it['ERROR_TYPE']]),
                            ofObservation     : toPercent(it['NUM'], observationTotal[it['TYPE']]),
                            ofPriority        : toPercent(it['NUM'], priorityTotal[it['PRIORITY']]),
                    ])
        }
        return result
    }

    private Map getActionPlanNotNullRow(Map data, String key, Map responsiblePartyName) {
        Map row = data[key]
        if (row) return row
        String[] keyParts = key.split("_@_")
        return [
                responsibleParty  : responsiblePartyName["" + keyParts[0]] ?: ("" + keyParts[0]),
                responsiblePartyId: keyParts[0],
                observation       : getLabelForType(keyParts[1]) ?: keyParts[1],
                observationCode   : keyParts[1],
                errorType         : keyParts[2],
                priority          : keyParts[3],
                number            : 0,
                ofVendor          : 0,
                ofIssue           : 0,
                ofObservation     : 0,
                ofPriority        : 0
        ]
    }
}
