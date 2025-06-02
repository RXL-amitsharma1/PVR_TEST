package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.dynamicReports.FooterBuilder
import com.rxlogix.dynamicReports.HeaderBuilder
import com.rxlogix.dynamicReports.ReportBuilder
import com.rxlogix.dynamicReports.charts.ChartElement
import com.rxlogix.dynamicReports.charts.CrossTabChartBuilder
import com.rxlogix.dynamicReports.charts.NonCaseSQLChartBuilder
import com.rxlogix.dynamicReports.reportTypes.*
import com.rxlogix.enums.AssignedToFilterEnum
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.GranularityEnum
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.mapping.Country
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum
import grails.plugin.springsecurity.SpringSecurityUtils
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.CsvUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.RelativeDateConverter
import com.rxlogix.util.ViewHelper
import grails.async.Promises
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import grails.gorm.transactions.NotTransactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.design.JRDesignField
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.context.i18n.LocaleContextHolder
import org.hibernate.StatelessSession
import org.hibernate.Transaction

import java.sql.Connection
import java.text.ParseException
import java.text.SimpleDateFormat
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.zip.GZIPInputStream

import static com.rxlogix.Constants.CUSTOM_SQL_VALUE_REGEX_CONSTANT
import static net.sf.dynamicreports.report.builder.DynamicReports.type
import static net.sf.dynamicreports.report.builder.DynamicReports.type
import static net.sf.dynamicreports.report.builder.DynamicReports.type
import static net.sf.dynamicreports.report.builder.DynamicReports.type

@Transactional
class TemplateService {
    def userService
    def customMessageService
    def CRUDService
    def seedDataService
    def messageSource
    def actionItemService
    def commentService
    def workflowService
    GrailsApplication grailsApplication
    def dataSource_pva
    def sqlService
    def executedConfigurationService

    String chartDefaultOptions
    def utilService
    def sessionFactory
    def notificationService

    public static final String DATE_FMT = "dd-MM-yyyy"

    private static final List<String> REDUNDANT_JSON_KEYS = ["pvcLcpLate", "pvcLcpRootCause", "pvcLcpRcClass", "pvcLcpRcSubCat",
                                                             "pvcLcpRespParty", "pvcLcpCorrAct", "pvcLcpPrevAct",  "pvcLcpCorrDate", "pvcLcpPrevDate", "pvcLcpActions", "pvcLcpSummary" , "pvcLcpInvestigation"]

    private static final Map<String, String> JSON_KEY_TO_MART_COLUMN_MAP = ["pvcLcpLate"     :"LATE", "pvcLcpRootCause":"ROOT_CAUSE",
                                                                            "pvcLcpRespParty":"REPONSIBLE_PARTY", "pvcLcpRcSubCat":"ROOT_CAUSE_SUB_CATEGORY",
                                                                            "pvcLcpRcClass"  :"ROOT_CAUSE_CLASSIFICATION", "pvcLcpCorrAct":"CORRECTIVE_ACTION",
                                                                            "pvcLcpPrevAct"  :"PREVENTATIVE_ACTION", "pvcLcpCorrDate":"CORRECTIVE_DATE", "pvcLcpPrevDate":"PREVENTATIVE_DATE","pvcLcpInvestigation":"INVESTIGATION","pvcLcpActions":"ACTIONS", "pvcLcpSummary":"SUMMARY"]

    private static final List<String> INBOUND_REDUNDANT_JSON_KEYS = ["pvcIcFlagLate", "pvcIcRootCause", "pvcIcRcClass", "pvcIcRcSubCat", "pvcIcRespParty",
                                                                     "pvcIcCorrAct", "pvcIcPrevAct", "pvcIcCorrDate", "pvcIcPrevDate"]

    private static final Map<String, String> INBOUND_JSON_KEY_TO_MART_COLUMN_MAP = ["pvcIcFlagLate"     :"LATE", "pvcIcRootCause":"ROOT_CAUSE",
                                                                                    "pvcIcRcClass":"REPONSIBLE_PARTY", "pvcIcRcSubCat":"ROOT_CAUSE_SUB_CATEGORY",
                                                                                    "pvcIcRespParty"  :"ROOT_CAUSE_CLASSIFICATION", "pvcIcCorrAct":"CORRECTIVE_ACTION",
                                                                                    "pvcIcPrevAct"  :"PREVENTATIVE_ACTION", "pvcIcCorrDate":"CORRECTIVE_DATE", "pvcIcPrevDate":"PREVENTATIVE_DATE"]

    private static final List<String> ADMINISTRATIVE_DATA_KEYS = ["assignedTo"]
    private static final String DUE_DATE = "dueInDays"
    private static final String ASSIGNED_TO = "assignedToUser"
    private static final String ASSIGNED_TO_GROUP = "assignedToGroup"
    private static final String WORKFLOW_STATE = "workFlowState"
    private static final String PROCESSING_TIME = "pvcIcDtp"

    private Connection getReportConnectionForPVR() {
        return utilService.getReportConnectionForPVR()
    }


    def copyTemplate(ReportTemplate originalTemplate, User owner) {
        ReportTemplate newTemplate = null
        if (originalTemplate instanceof NonCaseSQLTemplate) {
            newTemplate = new NonCaseSQLTemplate(originalTemplate.properties)
            newTemplate.customSQLValues = []
            originalTemplate.customSQLValues.each {
                newTemplate.addToCustomSQLValues(new CustomSQLValue(key: it.key, value: ""))
            }
        } else if (originalTemplate instanceof CustomSQLTemplate) {
            newTemplate = new CustomSQLTemplate(originalTemplate.properties)
            newTemplate.customSQLValues = []
            originalTemplate.customSQLValues.each {
                newTemplate.addToCustomSQLValues(new CustomSQLValue(key: it.key, value: ""))
            }
        } else if (originalTemplate instanceof DataTabulationTemplate) {
            newTemplate = copyDataTabulation(originalTemplate)
        } else if (originalTemplate instanceof CaseLineListingTemplate) {
            newTemplate = copyCLL(originalTemplate)
        } else if (originalTemplate instanceof TemplateSet) {
            TemplateSet templateSet = (TemplateSet) originalTemplate
            newTemplate = new TemplateSet(templateSet.properties)
            newTemplate.nestedTemplates = []
            templateSet.nestedTemplates.each {
                newTemplate.addToNestedTemplates(it)
            }
        } else if (originalTemplate instanceof XMLTemplate) {
            XMLTemplate xmlTemplate = (XMLTemplate) originalTemplate
            newTemplate = new XMLTemplate(xmlTemplate.properties)
            xmlTemplate.refresh()
            newTemplate.rootNode = copyXmlTemplateNode(xmlTemplate.rootNode)
            newTemplate.nestedTemplates = []
            xmlTemplate.nestedTemplates.each {
                newTemplate.addToNestedTemplates(it)
            }
        }
        newTemplate.lastExecuted = null
        newTemplate.userTemplates = null
        newTemplate.userGroupTemplates = null
        newTemplate.templateUserStates = null
        newTemplate.name = generateUniqueName(originalTemplate, owner)
        newTemplate.qualityChecked = false
        newTemplate.createdBy = owner.username
        newTemplate.owner = owner
        newTemplate.useFixedTemplate = originalTemplate.useFixedTemplate
        newTemplate.originalTemplateId = 0L
        return newTemplate
    }

    private XMLTemplateNode copyXmlTemplateNode(XMLTemplateNode original) {
        Map properties = new HashMap<>(original.properties)
        properties['children'] = null
        properties['parent'] = null
        XMLTemplateNode copy = new XMLTemplateNode(properties)
        for (XMLTemplateNode child : original.children) {
            copy.addToChildren(copyXmlTemplateNode(child))
        }
        return copy
    }

    CaseLineListingTemplate copyCLL(CaseLineListingTemplate template) {
        Map properties = new HashMap<>(template.properties)
        properties['columnList'] = null
        properties['groupingList'] = null
        properties['rowColumnList'] = null
        properties['serviceColumnList'] = null
        CaseLineListingTemplate newCLL = new CaseLineListingTemplate(properties)

        if (template.columnList) { // columnList cannot be null
            ReportFieldInfoList parsedColumns = new ReportFieldInfoList()
            template.columnList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedColumns.addToReportFieldInfoList(reportFieldInfo)
            }
            newCLL.columnList = parsedColumns
        }

        if (template.groupingList) { // groupingList can be null
            ReportFieldInfoList parsedGroupings = new ReportFieldInfoList()
            template.groupingList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedGroupings.addToReportFieldInfoList(reportFieldInfo)
            }
            newCLL.groupingList = parsedGroupings
        }

        if (template.rowColumnList) { // rowColumnList can be null
            ReportFieldInfoList parsedRowCols = new ReportFieldInfoList()
            template.rowColumnList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedRowCols.addToReportFieldInfoList(reportFieldInfo)
            }
            newCLL.rowColumnList = parsedRowCols
        }

        if (template.serviceColumnList) { // serviceColumnList can be null
            ReportFieldInfoList parsedServiceCols = new ReportFieldInfoList()
            template.serviceColumnList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedServiceCols.addToReportFieldInfoList(reportFieldInfo)
            }
            newCLL.serviceColumnList = parsedServiceCols
        }

        return newCLL
    }

    private DataTabulationTemplate copyDataTabulation(DataTabulationTemplate template) {
        Map properties = new HashMap<>(template.properties)
        properties['columnMeasureList'] = null
        properties['groupingList'] = null
        properties['rowList'] = null
        DataTabulationTemplate newDT = new DataTabulationTemplate(properties)

        if (template.columnMeasureList) {
            List<DataTabulationColumnMeasure> savedColumnMeasureList = []

            template.columnMeasureList.each { columnMeasure ->
                DataTabulationColumnMeasure dtColumnMeasure = new DataTabulationColumnMeasure(columnMeasure.properties)
                dtColumnMeasure.columnList = null
                dtColumnMeasure.measures = []

                columnMeasure.measures.each { measure ->
                    dtColumnMeasure.addToMeasures(new DataTabulationMeasure(measure.properties))
                }

                ReportFieldInfoList dtColumns = new ReportFieldInfoList()
                columnMeasure.columnList?.reportFieldInfoList?.each { rfi ->
                    ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                    dtColumns.addToReportFieldInfoList(reportFieldInfo)
                }
                dtColumnMeasure.columnList = dtColumns

                savedColumnMeasureList.add(dtColumnMeasure)
            }
            savedColumnMeasureList.each {
                newDT.addToColumnMeasureList(it)
            }
        }

        if (template.groupingList) { // groupingList may be null
            ReportFieldInfoList parsedRows = new ReportFieldInfoList()
            template.groupingList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedRows.addToReportFieldInfoList(reportFieldInfo)
            }
            newDT.groupingList = parsedRows
        }

        if (template.rowList) { // rowList cannot be null
            ReportFieldInfoList parsedRows = new ReportFieldInfoList()
            template.rowList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedRows.addToReportFieldInfoList(reportFieldInfo)
            }
            newDT.rowList = parsedRows
        }

        return newDT
    }

    String generateUniqueName(ReportTemplate template, User owner) {
        String templateName = template.name
        String newName = "Copy of ${templateName}"
        int maxSize = ReportTemplate.constrainedProperties.name.maxSize

        if (newName.size() >= maxSize) {
            int templateNameSize = templateName.size()
            templateName = templateName.replace(templateName.substring(templateNameSize - 20, templateNameSize), "...")
            newName = "Copy of ${templateName}"
        }

        if (ReportTemplate.countByNameAndOwnerAndIsDeleted(newName, owner, false)) {
            int count = 1
            newName = "Copy of ${templateName} ($count)"
            while (ReportTemplate.countByNameAndOwnerAndIsDeleted(newName, owner, false)) {
                newName = "Copy of ${templateName} (${count++})"
            }
        }

        return newName
    }

    List getTemplateList(String search, int offset, int max, Boolean showXMLSpecific) {
        ReportTemplate.ownedByUserWithSearch(userService.getUser(), search, (showXMLSpecific ? TemplateTypeEnum.ICSR_XML : null)).list([max: max, offset: offset]).collect {
            [id       : it[0], text: it[1] + " " + (it[2] ? "(" + it[2] + ")" : "") + " - Owner: " + it[6],
             hasBlanks: it[4], qced: it[3], isFavorite: it[5], configureAttachments: it[2] ? "${it[1]} (${it[2]})" : "${it[1]}"]
        }
    }

    int getTemplateListCount(String search, Boolean showXMLSpecific) {
        return ReportTemplate.countOwnedByUserWithSearch(userService.getUser(), search, (showXMLSpecific ? TemplateTypeEnum.ICSR_XML : null)).get()
    }

    Map getTemplateListForTemplateSet(Long oldSelectedId, String search, int offset, int max, TemplateTypeEnum templateTypeEnum, boolean includeCllWithCustomSql) {
        List<ReportTemplate> list = ReportTemplate.ownedByUserWithSearchNoBlank(userService.getUser(), search, templateTypeEnum, includeCllWithCustomSql)
                .list(order: 'asc', sort: 'name', fetch: [owner: 'join'], max: max, offset: offset)

        List<ReportTemplate> list2 = list.collect {
            Map result = [
                id: it[0],
                nameWithDescription: it[1] + " " + (it[2] ? "(" + it[2] + ")" : "") + " - Owner: " + it[6],
                qualityChecked: it[3],
                isFavorite: it[5]
            ]
            ReportTemplate reportTemplate = ReportTemplate.read(it[0])
            if ((reportTemplate instanceof CaseLineListingTemplate) && reportTemplate.groupingList) {
                ReportFieldInfoList reportFieldInfoList = reportTemplate.groupingList
                result["groupingColumns"] = reportFieldInfoList.reportFieldInfoList.collect { reportFieldInfo ->
                    reportFieldInfo.reportField.name
                }
            }
            return result
        }
        return [list: list2, totalCount: ReportTemplate.ownedByUserWithSearchNoBlank(userService.getUser(), search, templateTypeEnum, includeCllWithCustomSql).count()]
    }

    @Transactional(readOnly = true)
    def createMeasureListFromJson(DataTabulationTemplate template, String measureJSON) {
        def measures = null
        if (measureJSON) {
            measures = []
            JSON.parse(measureJSON).each {
                DataTabulationMeasure measure = new DataTabulationMeasure(name: it.name)
                measure.type = MeasureTypeEnum.valueOf(it.type)
                measure.dateRangeCount = CountTypeEnum.valueOf(it.count)
                measure.percentageOption = PercentageOptionEnum.valueOf(it.percentage)
                measure.relativeDateRangeValue = it.relativeDateRangeValue as Integer
                if (measure.dateRangeCount == CountTypeEnum.CUSTOM_PERIOD_COUNT) {
                    // save date range with timezone
                    measure.customPeriodFrom = DateUtil.parseDate(it.customPeriodFrom, Constants.DateFormat.WITH_TZ)
                    measure.customPeriodTo = DateUtil.parseDate(it.customPeriodTo, Constants.DateFormat.WITH_TZ)
                }
                measure.showTotal = it.showTotal
                measures.add(measure)
            }
        }
        return measures
    }

    // For data tabulation rows
    List<String> getAllSelectedFieldNames(ReportTemplate reportTemplate) {
        List<String> rowNames = getSelectedFieldNames(reportTemplate.selectedFieldsRows)
        return rowNames // Don't show column names for now.
    }

    private List<String> getSelectedFieldNames(List<ReportFieldInfo> reportFieldInfoList) {
        List<String> names = []
        String reportFieldName
        Locale locale

        reportFieldInfoList.each {
            if (it.renameValue) {
                names.add(it.renameValue)
            } else {
                reportFieldName = customMessageService.getMessage("app.reportField." + it.reportField.name)
                locale = reportFieldName.endsWith('(J)') ? Locale.JAPANESE : Locale.ENGLISH
                reportFieldName = customMessageService.getMessage("app.reportField." + it.reportField.name, null, "app.reportField." + it.reportField.name, locale)
                names.add(reportFieldName)
            }
        }
        return names
    }

    @Transactional(readOnly = true)
    List<TemplateQuery> getUsages(ReportTemplate template) {
        return TemplateQuery.usuageByTemplate(template).listDistinct()
    }

    @Transactional(readOnly = true)
    int getUsagesCount(ReportTemplate template) {
        return TemplateQuery.countUsuageByTemplate(template).get()
    }

    @Transactional(readOnly = true)
    List<ReportTemplate> getUsagesTemplateSet(ReportTemplate template) {
        return TemplateSet.usuageByTemplate(template).listDistinct()
    }

    @Transactional(readOnly = true)
    int getUsagesCountTemplateSet(ReportTemplate template) {
        return TemplateSet.countUsuageByTemplate(template).get()
    }

    @Transactional(readOnly = true)
    boolean isTemplateUpdateable(ReportTemplate template) {
        return !(getUsagesCount(template) > 0 || TemplateSet.usuageByTemplate(template).list([readOnly: true]).unique {
            it.id
        }.find {
            if (getUsagesCount(it)) {
                return true
            }
        })
    }

    // For view page; dev user only
    @NotTransactional
    JSON getTemplateAsJSON(ReportTemplate reportTemplate) {
        HashMap templateMap = MiscUtil.getObjectProperties(reportTemplate)
        templateMap.remove("templateService")
        templateMap.remove("sqlService")
        templateMap.remove("userService")

        if (reportTemplate.fixedTemplate?.data) {
            templateMap['fixedTemplate'] = [
                    name: reportTemplate.fixedTemplate.name,
                    data: Base64.encoder.encodeToString(reportTemplate.fixedTemplate?.data)
            ]
        }
        templateMap['userGroupTemplates'] = reportTemplate.userGroupTemplates?.collect { it.userGroup.name } ?: []
        templateMap['userTemplates'] = reportTemplate.userTemplates?.collect { it.user.username } ?: []
        if (reportTemplate.templateType == TemplateTypeEnum.CASE_LINE) {
            templateMap.columnList = reportTemplate.columnList.reportFieldInfoList.collect {
                return getRFIPropertiesMap(it)
            }
            templateMap.groupingList = reportTemplate.groupingList?.reportFieldInfoList?.collect {
                return getRFIPropertiesMap(it)
            }
            templateMap.rowColumnList = reportTemplate.rowColumnList?.reportFieldInfoList?.collect {
                return getRFIPropertiesMap(it)
            }
            templateMap.serviceColumnList = reportTemplate.serviceColumnList?.reportFieldInfoList?.collect {
                return getRFIPropertiesMap(it)
            }
        } else if (reportTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
            templateMap.groupingList = reportTemplate.groupingList?.reportFieldInfoList.collect {
                return getRFIPropertiesMap(it)
            }
            templateMap.rowList = reportTemplate.rowList?.reportFieldInfoList?.collect {
                return getRFIPropertiesMap(it)
            }
            templateMap.columnMeasureList = reportTemplate.columnMeasureList.collect {
                return getColMeasMap(it)
            }
        }

        return templateMap as JSON
    }

    @NotTransactional
    HashMap getRFIPropertiesMap(ReportFieldInfo reportFieldInfo) {
        HashMap rfiListMap = new HashMap(reportFieldInfo.properties)
        rfiListMap.reportFieldName = reportFieldInfo.reportField.name
        rfiListMap.customFieldGroupName = reportFieldInfo.customField?.fieldGroup?.name
        rfiListMap.remove("reportFieldInfoList")
        rfiListMap.sortEnumValue = reportFieldInfo.sort?.value()
        rfiListMap.remove("sort")
        return rfiListMap
    }

    @NotTransactional
    HashMap getColMeasMap(DataTabulationColumnMeasure columnMeasure) {
        HashMap columnMeasureMap = new HashMap(columnMeasure.properties)

        columnMeasureMap.columnList = columnMeasure.columnList?.reportFieldInfoList?.collect {
            return getRFIPropertiesMap(it)
        }
        columnMeasureMap.measures = columnMeasure.measures?.collect {
            HashMap rfiListMap = new HashMap(it.properties)
            return rfiListMap
        }
        return columnMeasureMap
    }

    JSONArray getJSONRF(ReportFieldInfoList list, Locale locale) {
        JSONArray JSONColumns = new JSONArray()
        User user = userService.currentUser
        Set<ReportField> blindedFieldsForUser = user ? User.getBlindedFieldsForUser(user) : []
        Set<ReportField> protectedFieldsForUser = user ? User.getProtectedFieldsForUser(user) : []
        if (list) {
            list.reportFieldInfoList.each {
                ReportField rf = it.reportField
                JSONObject column = new JSONObject([id                     : rf.id, stackId: it.stackId, renameValue: it.renameValue,drillDownFilerColumns:it.drillDownFilerColumns, drillDownTemplate:it.drillDownTemplate?.id,
                                                    text                   : it.customField ? it.customField.customName : customMessageService.getMessage("app.reportField." + rf.name),
                                                    customExpression       : it.customExpression ?: '', datasheet: it.datasheet, onPrimaryDatasheet: it.onPrimaryDatasheet,
                                                    sortLevel              : it.sortLevel, sort: it.sort?.value(), argusName: it.argusName,
                                                    commaSeparatedValue    : it.commaSeparatedValue, advancedSorting: it.advancedSorting,
                                                    suppressRepeatingValues: it.suppressRepeatingValues, customFieldId: it.customField?.id,
                                                    suppressLabel          : it.suppressLabel, hideSubtotal:it.hideSubtotal?:false, redactedValue: it.redactedValue ?: false, legend: customMessageService.getMessage("app.reportField.${rf.name}.label.legend"),
                                                    description            : it.customField ? it.customField.customDescription : customMessageService.getMessage("app.reportField.${rf.name}.label.description"),
                                                    blindedValue           : it.blindedValue, reportFieldName: it.reportField.name, newLegendValue: it.newLegendValue,
                                                    clobType               : it.reportField.isClobField(locale), setId: it.setId, columnWidth: it.columnWidth,
                                                    colorConditions        : it.colorConditions,
                                                    dateType               : it.reportField?.dataType?.equals(Date)?"1":"0",
                                                    fromBlindedList        : !!blindedFieldsForUser.find { it.id == rf.id},
                                                    fromProtectedList      : !!protectedFieldsForUser.find { it.id == rf.id},
                                                    fieldLabelJ            : it.customField ? it.customField.customName : messageSource.getMessage("app.reportField." + rf.name, null, rf.reportFieldName ?: "app.reportField." + rf.name, Locale.JAPANESE),
                                                    fieldLabelE            : it.customField ? it.customField.customName : messageSource.getMessage("app.reportField." + rf.name, null, rf.reportFieldName ?: "app.reportField." + rf.name, Locale.ENGLISH)])
                JSONColumns.add(column)
            }
        }
        return JSONColumns
    }

    // For gsp
    String getJSONStringRF(ReportFieldInfoList list, Locale locale) {
        return getJSONRF(list, locale).toString()
    }

    ReportTemplate createExecutedReportTemplate(TemplateQuery templateQuery) throws Exception {
        return createExecutedReportTemplate(templateQuery.template)
    }

    ReportTemplate createExecutedReportTemplate(ReportTemplate template) throws Exception {
        ReportTemplate executedTemplate = null
        if (template instanceof NonCaseSQLTemplate) {
            executedTemplate = new ExecutedNonCaseSQLTemplate(template.properties)
            executedTemplate.originalTemplateId = template.id
            executedTemplate.customSQLValues = []
            template.customSQLValues.each {
                executedTemplate.addToCustomSQLValues(new CustomSQLValue(key: it.key, value: ""))
            }
        } else if (template instanceof CustomSQLTemplate) {
            executedTemplate = new ExecutedCustomSQLTemplate(template.properties)
            executedTemplate.originalTemplateId = template.id
            executedTemplate.customSQLValues = []
            template.customSQLValues.each {
                executedTemplate.addToCustomSQLValues(new CustomSQLValue(key: it.key, value: ""))
            }
        } else if (template instanceof DataTabulationTemplate) {
            executedTemplate = createExecutedDataTabulationTemplate(template)
        } else if (template instanceof CaseLineListingTemplate) {
            executedTemplate = createExecutedCLL(template)
//            executedTemplate = new ExecutedCaseLineListingTemplate(template.properties)
//            executedTemplate.originalTemplateId = template.id
        } else if (template instanceof TemplateSet) {
            TemplateSet templateSet = (TemplateSet) template
            executedTemplate = new ExecutedTemplateSet(templateSet.properties)
            executedTemplate.originalTemplateId = templateSet.id
            executedTemplate.nestedTemplates = []
            templateSet.nestedTemplates.each {
                ReportTemplate executedNestedTemplate = createExecutedReportTemplate(it)
                executedTemplate.addToNestedTemplates(executedNestedTemplate)
            }
        }

        if(template.userTemplates){
            template.userTemplates.each { userTemplate ->
                executedTemplate.addToUserTemplates(new UserTemplate(user: User.get(userTemplate.user.id)))
            }
        }
        if(template.userGroupTemplates){
            template.userGroupTemplates.each { userGroupTemplate ->
                executedTemplate.addToUserGroupTemplates(new UserGroupTemplate(userGroup: UserGroup.get(userGroupTemplate.userGroup.id)))
            }
        }

        CRUDService.saveWithoutAuditLog(executedTemplate)

        return executedTemplate
    }

    private createExecutedCLL(CaseLineListingTemplate template) {
        Map properties = new HashMap<>(template.properties)
        properties['columnList'] = null
        properties['groupingList'] = null
        properties['rowColumnList'] = null
        ExecutedCaseLineListingTemplate executedCLL = new ExecutedCaseLineListingTemplate(properties)
        executedCLL.originalTemplateId = template.id

        if (template.columnList) { // columnList cannot be null
            ReportFieldInfoList parsedColumns = new ReportFieldInfoList()
            template.columnList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedColumns.addToReportFieldInfoList(reportFieldInfo)
            }
            executedCLL.columnList = parsedColumns
        }

        if (template.groupingList) { // groupingList can be null
            ReportFieldInfoList parsedGroupings = new ReportFieldInfoList()
            template.groupingList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedGroupings.addToReportFieldInfoList(reportFieldInfo)
            }
            executedCLL.groupingList = parsedGroupings
        }

        if (template.rowColumnList) { // rowColumnList can be null
            ReportFieldInfoList parsedRowCols = new ReportFieldInfoList()
            template.rowColumnList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedRowCols.addToReportFieldInfoList(reportFieldInfo)
            }
            executedCLL.rowColumnList = parsedRowCols
        }

        if (template.serviceColumnList) { // rowColumnList can be null
            ReportFieldInfoList parsedServiceCols = new ReportFieldInfoList()
            template.serviceColumnList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedServiceCols.addToReportFieldInfoList(reportFieldInfo)
            }
            executedCLL.serviceColumnList = parsedServiceCols
        }

        return executedCLL
    }

    String getChartDefaultOptions() {
        if (!chartDefaultOptions) {
            chartDefaultOptions = seedDataService.getInputStreamForMetadata("chart_default_options.json").text
        }
        return chartDefaultOptions
    }

    static def fetchDataForMap(byte[] decryptedValue) {
        Map<String, String> countries = Country.getNameIso2Map()
        Map<String, String> countries2 = new HashMap<>();
        for (String iso : Locale.getISOCountries()) {
            Locale l = new Locale("", iso);
            countries2.put(l.getDisplayCountry().toLowerCase(), iso.toLowerCase());
        }
        def result = new GZIPInputStream(new ByteArrayInputStream(decryptedValue))
        def scanningResult = new Scanner(result).useDelimiter("\\A")
        def theString = scanningResult.next()
        def data = JSON.parse(theString).collect { row ->
            String countryCode = row["ROW_1"]?.toLowerCase()
            if (countryCode) {
                if (countryCode.size() != 2) {
                    countryCode = countries.get(countryCode) ?: countries2.get(countryCode);
                }
            }
            def value = row.find { k, v -> k.startsWith("GP") }.value ?: 0
            countryCode ? [countryCode, value] : null
        }.findAll()
        data
    }

    static String getMapCountLabel(ReportResult reportResult) {
        JSONArray tabHeaders = (JSONArray) JSON.parse(reportResult.data.crossTabHeader)
        return tabHeaders[((JSONArray) tabHeaders).collect { it.keySet()[0] }?.find { it.startsWith("GP") }].findAll { it }?.collect { it.trim() }?.join(" ")

    }

    Map getMapOptions(ReportResult reportResult) {
        def data = fetchDataForMap(reportResult?.data?.getDecryptedValue())
        JSONArray tabHeaders = (JSONArray) JSON.parse(reportResult.data.crossTabHeader)
        String contLabel = getMapCountLabel(reportResult)
        def colorAxis = [min: 0]
        if (reportResult.executedTemplateQuery?.executedTemplate?.worldMapConfig) {
            try {
                colorAxis = JSON.parse(reportResult.executedTemplateQuery?.executedTemplate?.worldMapConfig)
            } catch (Exception e) {
                log.error("Error parsing worldMapConfig", e)
            }
        }
        return [
                chart        : [map: 'custom/world'],
                title        : [text: null],
                colorAxis    : colorAxis,
                credits      : [enabled: false],
                series       : [[
                                        data      : data,
                                        name      : contLabel,
                                        states    : [hover: [color: '#BADA55']],
                                        dataLabels: [
                                                enabled: true,
                                                formatter: """function() {
                                                    if(this.point.value ==null) return null;
                                                    return this.point.name +" - "+ this.point.value;
                                                
                                                }"""
                                        ]
                                ]]
        ]
    }
    def getChartOptions(ReportResult reportResult, String click) {
        def chartOptions = getEmptyChartOptions()
        ReportTemplate executedTemplate = GrailsHibernateUtil.unwrapIfProxy(reportResult.executedTemplateQuery.executedTemplate)
        String chartCustomOptions = executedTemplate.chartCustomOptions
        Map specialSettings = [:]
        if (chartCustomOptions) {
            chartOptions = ChartOptionsUtils.deserialize(chartCustomOptions, chartOptions)
            // Clean chart series from template. They may be not empty if user chosen Combination charts
            chartOptions.series = []
        } else {
            chartOptions = ChartOptionsUtils.deserialize(getChartDefaultOptions(), chartOptions)
        }
        ChartElement chart = new ChartElement(chartOptions, null)
        chart.setTitle(null)
        if (click) chart.setOnClick("function (e) { ${click}(e);}");
        JRDataSource dataSource
        Map cols
        if (executedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
            specialSettings = CrossTabChartBuilder.setSpecialChartSettings(executedTemplate)
            chart.setYAxisTitle(CrossTabChartBuilder.getMeasuresList(reportResult).findAll { it }.join(", "))
            chart.setYAxisPercentageTitle(CrossTabChartBuilder.getPercentageMeasuresList(reportResult).findAll { it }.join(", "))
            dataSource = ReportBuilder.createDataSource(reportResult, true)
            JSONArray topHeaders = ReportBuilder.getHeaderForTopNColumns(reportResult, dataSource)
            cols = getDataTabColumnsAndRows(topHeaders, specialSettings)
        } else if (executedTemplate.templateType == TemplateTypeEnum.NON_CASE) {
            specialSettings = NonCaseSQLChartBuilder.setSpecialChartSettings((NonCaseSQLTemplate) executedTemplate)
            chart.setYAxisTitle(NonCaseSQLChartBuilder.getMeasuresList(reportResult, specialSettings).join(", "))
            chart.setYAxisPercentageTitle(NonCaseSQLChartBuilder.getPercentageMeasuresList(reportResult, specialSettings).join(", "))
            dataSource = ReportBuilder.createDataSourceCSV(reportResult, executedTemplate, [chart: true])
            cols = getNonCaseColumnsAndRows(reportResult, specialSettings)
        }
        chart.setShowPercentages(chart.options.chart.type == "pie")
        cols.columns.each {
            chart.addSerie(it.columnLabel, it.type, it.isPercentageColumn, it.axisLabel)
        }
        def rowId = executedTemplate.templateType == TemplateTypeEnum.DATA_TAB ? "" : -1L
        while (dataSource.next()) {
            JRDesignField idField = new JRDesignField()
            idField.setName("ID")
            if (executedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
                rowId = dataSource.getFieldValue(idField)
            } else {
                rowId++
            }
            def labels = cols?.rows?.collect {
                String value = dataSource.getFieldValue(it.field)
                value == null ? ViewHelper.getEmptyLabel() : value
            }
            if (labels.contains("Total") || labels.contains("Subtotal") || labels.contains("Sub Total") || labels.contains("小計") || labels.contains("総計")) {
                continue;
            }
            cols.columns.each {
                if (!it.isRowColumn) {
                    def value = dataSource.getFieldValue(it.field)
                    chart.addValue(it.columnLabel, labels, value, null, rowId, it.columnValue)
                }
            }
        }
        return chart.generateChart(false)
    }

    private Map<String, List<Map<String, ?>>> getDataTabColumnsAndRows(JSONArray tabHeaders, Map specialSettings ) {
        List<Map<String, ?>> rows = []
        List<Map<String, ?>> columns = []

        for (JSONObject header : tabHeaders) {
            String name = header.entrySet().getAt(0).key
            String label = header.entrySet().getAt(0).value
            // Remove line breaks from column header
            label = label.trim().replaceAll("[\\r\\n]+", ";")
            boolean isCaseListColumn = name.startsWith("CASE_LIST")
            boolean isTotalCaseCount = name.startsWith("CASE_COUNT")
            boolean isIntervalCaseCount = name.startsWith("INTERVAL_CASE_COUNT")
            Matcher matcher = (name =~ /(\w+)_(\d+)_(\w+)/)
            String key = matcher.matches() ? name.split("_")[2] : null
            String specialSetting = key ? (specialSettings?.get(key)) : null
            String axisLabel = key ? (specialSettings?.get(key + "_label")) : null
            //identify percentage values to link them to percentage axis
            boolean isPercentageColumn = matcher.matches() && matcher.groupCount() > 2 && matcher.group(3).startsWith("P") && !(matcher.group(3).startsWith("PA") && !specialSetting)
            JRDesignField jrField = new JRDesignField()
            jrField.setName(name)
            Map<String, ?> item = [
                    columnLabel: label,
                    columnValue: name,
                    field      : jrField
            ]
            if (name.substring(0, 3).equalsIgnoreCase("ROW")) {
                jrField.setValueClass(type.stringType().getValueClass())
                rows.add(item)
            } else if (!isCaseListColumn && !isTotalCaseCount && !isIntervalCaseCount && !isPercentageColumn && !specialSetting) {
                jrField.setValueClass(type.integerType().getValueClass())
                columns.add(item)
            } else if (specialSetting) {
                if (specialSetting != "hide") {
                    jrField.setValueClass(type.doubleType().getValueClass())
                    item.isPercentageColumn = isPercentageColumn
                    item.type = specialSetting
                    item.axisLabel = axisLabel
                    columns.add(item)
                }
            }
        }
        return [columns: columns, rows: rows]
    }

    void createExecutedTemplate(ReportTemplate reportTemplateInstance) {
        if (!(reportTemplateInstance instanceof ITemplateSet)) {
            executedConfigurationService.createReportTemplate(reportTemplateInstance)
        }
    }

    List<XMLTemplateNode> findAllInvalidTags(XMLTemplate xmlTemplate, List<String> reportFieldInfos, XMLTemplateNode node) {
        List<XMLTemplateNode> xmlTemplateNodeList = []
        if (node.children) {
            node.children.each {
                xmlTemplateNodeList = xmlTemplateNodeList + findAllInvalidTags(xmlTemplate, reportFieldInfos, it)
            }
        }
        //Check both template should be of CLL only and files should belong to cllTemplates fields only.
        if ((node.templateId && !(node.templateId in xmlTemplate.nestedTemplates*.id)) || (node.reportFieldInfo && !(node.reportFieldInfo.uniqueIdentifierXmlTag() in reportFieldInfos))) {
            xmlTemplateNodeList.add(node)
        }
        return xmlTemplateNodeList
    }

    //todo: there is a near duplicate method in queryService; combine and parameterize - morett
    private ReportTemplate saveCustomSQLValues(ReportTemplate template) {
        template.customSQLValues?.clear()
        String base = null
        if (template.templateType == TemplateTypeEnum.CUSTOM_SQL) {
            base = template.customSQLTemplateSelectFrom + " " + template.customSQLTemplateWhere
        } else if (template.templateType == TemplateTypeEnum.NON_CASE) {
            base = template.nonCaseSql
        }

        List<String> keys = base?.findAll(CUSTOM_SQL_VALUE_REGEX_CONSTANT)

        keys?.unique()?.each {
            CustomSQLValue toAdd = new CustomSQLValue(key: it, value: "")
            template.addToCustomSQLValues(toAdd)
        }

        template.hasBlanks = keys?.size() > 0
        return template
    }

    // Added all sqlQuery validation here rather than in domain class..
    private ReportTemplate preValidateTemplate(ReportTemplate template, def params) {
        if (template.templateType == TemplateTypeEnum.CUSTOM_SQL) {
            template = (CustomSQLTemplate) template
            template.customSQLTemplateSelectFrom = params.customSQLTemplateSelectFrom
            template.customSQLTemplateWhere = params.customSQLTemplateWhere
            template = saveCustomSQLValues(template)
            if (template.validateExcluding(['columnNamesList'])) {
                if (template.hasBlanks) {
                    template.columnNamesList = "[]"
                    if (sqlService.validateTemplateQuerySQL(template.customSQLTemplateSelectFrom)) {
                        template.errors.rejectValue('customSQLTemplateSelectFrom', 'com.rxlogix.invalid.custom.template.sql.table.name')
                    }
                } else {
                    String toValidate = CustomSQLTemplate.getSqlQueryToValidate(template)
                    if (!sqlService.validateCustomSQL(toValidate, false)) {
                        template.errors.rejectValue('customSQLTemplateSelectFrom', 'com.rxlogix.config.CustomSQLTemplate.sql.incorrect')
                    } else if (!sqlService.validateColumnName(toValidate, false)) {
                        template.errors.rejectValue('customSQLTemplateSelectFrom', 'com.rxlogix.config.template.columnNameInvalid')
                    } else {
                        template.columnNamesList = sqlService.getColumnsFromSqlQuery(toValidate, false, false).toListString()
                    }

                }
            }
        } else if (template.templateType == TemplateTypeEnum.NON_CASE) {
            template = (NonCaseSQLTemplate) template
            template.nonCaseSql = params.nonCaseSql
            template = saveCustomSQLValues(template)
            if (template.validateExcluding(['columnNamesList'])) {
                if (template.hasBlanks) {
                    template.columnNamesList = "[]"
                    if (sqlService.validateTemplateQuerySQL(template.nonCaseSql, template?.usePvrDB)) {
                        template.errors.rejectValue('nonCaseSql', 'com.rxlogix.invalid.non.case.template.sql.table.name')
                    }
                } else {
                    String toValidate = NonCaseSQLTemplate.getSqlQueryToValidate(template)
                    if (!sqlService?.validateCustomSQL(toValidate, template?.usePvrDB)) {
                        template.errors.rejectValue('nonCaseSql', 'com.rxlogix.config.NonCaseSQLTemplate.sql.incorrect')
                    } else if (!sqlService.validateColumnName(toValidate, template?.usePvrDB)) {
                        template.errors.rejectValue('nonCaseSql', 'com.rxlogix.config.template.columnNameInvalid')
                    } else {
                        template.columnNamesList = sqlService.getColumnsFromSqlQuery(toValidate, template.usePvrDB, false).toListString()
                    }
                }
            }
        } else if (template.templateType == TemplateTypeEnum.ICSR_XML) {
            template = (XMLTemplate) template
            if(template.validateExcluding()){
                List<String> reportFieldInfos = template.nestedTemplates.collect {
                    it.allSelectedFieldsInfo.collect { it.uniqueIdentifierXmlTag() }
                }.flatten().unique()
                List<XMLTemplateNode> xmlTemplateNodeList = findAllInvalidTags(template, reportFieldInfos, template.rootNode)
                if (xmlTemplateNodeList) {
                    log.debug("Invalid tags for ${template.name}, ${xmlTemplateNodeList*.tagName}")
                    template.errors.rejectValue('rootNode', 'com.rxlogix.config.XMLTemplate.rootNode.template.invalid', [xmlTemplateNodeList*.tagName] as Object[], "Invalid tags for ${template.name}, ${xmlTemplateNodeList*.tagName}")
                }
            }
        }
        return template
    }

    def updateTemplate(ReportTemplate reportTemplateInstance, params){

        //todo:  Until we move this to the domain object constraints, we'll go ahead and attempt the update regardless of outcome. - morett
        //todo:  This will collect all validation errors at once vs. piecemeal. - morett
        if(reportTemplateInstance.isDirty('customSQLTemplateSelectFrom')||reportTemplateInstance.isDirty('nonCaseSql')||reportTemplateInstance.isDirty('customSQLTemplateWhere')){
            reportTemplateInstance = preValidateTemplate(reportTemplateInstance, params)
        }

        if (reportTemplateInstance.hasErrors()) {  //To handle pre validation conditions done in preValidateTemplate as moving in Domain could have expensive.
            throw new ValidationException("Template preValidate has added validation issues", reportTemplateInstance.errors)
        }
        if(!userService.isAnyGranted("ROLE_QUALITY_CHECK")){
            reportTemplateInstance.qualityChecked = false
        }
        reportTemplateInstance = (ReportTemplate) CRUDService.update(reportTemplateInstance, [flush: true])
        createExecutedTemplate(reportTemplateInstance)
        return reportTemplateInstance
    }

    private def getNonCaseColumnsAndRows(ReportResult reportResult, Map specialSettings) {
        def rows = []
        def columns = []
        ReportTemplate executedTemplate = reportResult.executedTemplateQuery.executedTemplate
        JSONArray columnNamesList = JSON.parse(executedTemplate.columnNamesList)
        for (String columnName : columnNamesList) {
            String columnValue = CustomSQLReportBuilder.getColumnLabel(columnName)
            def isRowColumn = !columnName.contains(CustomSQLReportBuilder.CHART_COLUMN_PREFIX)
            boolean isPercentageColumn = columnName.contains(CustomSQLReportBuilder.CHART_COLUMN_P_PREFIX)
            JRDesignField jrField = new JRDesignField()
            jrField.setName(columnName)
            String chartType = specialSettings?.get(columnName)
            String label = specialSettings?.get(columnName + "_label")
            def item = [
                    columnLabel: CustomSQLReportBuilder.getColumnLabel(columnName),
                    columnValue: columnValue,
                    field      : jrField
            ]
            if (!isRowColumn) {
                if (isPercentageColumn) {
                    item.isPercentageColumn = isPercentageColumn
                    item.type = chartType
                    item.axisLabel = label
                    jrField.setValueClass(type.doubleType().getValueClass())
                } else {
                    jrField.setValueClass(type.integerType().getValueClass())
                }
                columns.add(item)
            } else {
                jrField.setValueClass(type.stringType().getValueClass())
                rows.add(item)
            }
        }
        return [columns: columns, rows: rows]
    }

    def getEmptyChartOptions() {
        def chartOptions = [
                chart      : [:],
                credits    : [
                        enabled: false
                ],
                legend     : [:],
                plotOptions: [
                        series: [
                                dataLabels: [:]
                        ]
                ],
                lang       : [
                        noData: "${ViewHelper.getMessage("app.label.widget.noData")}"
                ],
                title      : [
                        text: ""
                ]
        ]
        return chartOptions
    }

    private createExecutedDataTabulationTemplate(DataTabulationTemplate template) {
        Map properties = new HashMap<>(template.properties)
        properties['columnMeasureList'] = null
        properties['groupingList'] = null
        properties['rowList'] = null
        ExecutedDataTabulationTemplate executedDataTabulationTemplate = new ExecutedDataTabulationTemplate(properties)
        executedDataTabulationTemplate.originalTemplateId = template.id

        if (template.columnMeasureList) {
            List<DataTabulationColumnMeasure> savedColumnMeasureList = []

            template.columnMeasureList.each { columnMeasure ->
                DataTabulationColumnMeasure dtColumnMeasure = new DataTabulationColumnMeasure(columnMeasure.properties)
                dtColumnMeasure.columnList = null
                dtColumnMeasure.measures = []

                columnMeasure.measures.each { measure ->
                    dtColumnMeasure.addToMeasures(new DataTabulationMeasure(measure.properties))
                }

                ReportFieldInfoList dtColumns = new ReportFieldInfoList()
                columnMeasure.columnList?.reportFieldInfoList?.each { rfi ->
                    ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                    dtColumns.addToReportFieldInfoList(reportFieldInfo)
                }
                dtColumnMeasure.columnList = dtColumns

                savedColumnMeasureList.add(dtColumnMeasure)
            }
            savedColumnMeasureList.each {
                executedDataTabulationTemplate.addToColumnMeasureList(it)
            }
        }

        if (template.groupingList) { // groupingList may be null
            ReportFieldInfoList parsedRows = new ReportFieldInfoList()
            template.groupingList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedRows.addToReportFieldInfoList(reportFieldInfo)
            }
            executedDataTabulationTemplate.groupingList = parsedRows
        }

        if (template.rowList) { // rowList cannot be null
            ReportFieldInfoList parsedRows = new ReportFieldInfoList()
            template.rowList.reportFieldInfoList.each { rfi ->
                ReportFieldInfo reportFieldInfo = new ReportFieldInfo(rfi.properties)
                parsedRows.addToReportFieldInfoList(reportFieldInfo)
            }
            executedDataTabulationTemplate.rowList = parsedRows
        }

        return executedDataTabulationTemplate
    }

    void updateLastExecutionDate(ReportTemplate template) {
        if (!template) {
            return
        }
        try {
            ReportTemplate.executeUpdate("update ReportTemplate set lastExecuted=:lastExecuted where id=:id ", [lastExecuted: new Date(), id: template.id])
            //As multiple updates happens at the same time.
        } catch (Exception ex) {
            log.error("Error while updating last executed of template: ${template?.id} ", ex)
        }
    }

    void fillCLLTemplateServiceFields(CaseLineListingTemplate template) {
        // Add version number service field if report contains the case number field
        template.serviceColumnList = null
        if (template.allSelectedFieldsInfo.find { it.reportField.name.startsWith("masterCaseNum") }) {
            addServiceField(template, "masterVersionNum")
            template.serviceColumnList?.save(failOnError: true)
        }
        if (template.name == Holders.config.getProperty('pvcModule.late_processing_template')) {
            addServiceField(template, "masterCaseId")
            addServiceField(template, "vcsProcessedReportId")
            addServiceField(template, "masterEnterpriseId")
            addServiceField(template, "pvcLcpFlagPrimary")
            template.serviceColumnList.save(failOnError: true)
        }else if(template.name == Holders.config.getProperty('pvcModule.inbound_processing_template')) {
            addServiceField(template, "masterCaseId")
            addServiceField(template, "masterEnterpriseId")
            addServiceField(template, "pvcIcSenderId")
            addServiceField(template, "masterVersionNum")
            template.serviceColumnList.save(failOnError: true)
        }
    }

    void addServiceField(CaseLineListingTemplate template, String name) {
        if (!template.allSelectedFieldsInfo.find { name.equals(it.reportField.name) }) {
            if (!template.serviceColumnList) {
                template.serviceColumnList = new ReportFieldInfoList()
            }
            ReportField reportField = ReportField.findByName(name)
            SourceColumnMaster sourceColumn = reportField.getSourceColumn(template.owner.preference.locale)
            ReportFieldInfo reportFieldInfo = new ReportFieldInfo(
                    reportField: reportField,
                    argusName: "${sourceColumn?.tableName?.tableAlias}.${sourceColumn?.columnName}",
                    stackId: -1,
                    sortLevel: -1,
                    setId: 0,
                    columnWidth: 0
            )
            template.serviceColumnList.addToReportFieldInfoList(reportFieldInfo)
        }
    }

    def toJasperDesign(ReportTemplate template) {
        ReportBuilder reportBuilder = new ReportBuilder()
        JasperReportBuilder report = reportBuilder.initializeNewReport()
        report.setQuery("", "csv")
        SpecificTemplateTypeBuilder specificTemplateTypeBuilder
        if (template.templateType == TemplateTypeEnum.CUSTOM_SQL ||
                template.templateType == TemplateTypeEnum.NON_CASE) {
            //todo: refactor:  custom SQL is not an output format, it is a process that describes how the columns were obtained; - morett
            //todo: refactor:  this presumes a case line listing but that is not correct; custom sql should work for any output type - morett
            specificTemplateTypeBuilder = new CustomSQLReportBuilder()
        } else if (template.templateType == TemplateTypeEnum.DATA_TAB) {
            specificTemplateTypeBuilder = new CrosstabReportBuilder()
        } else if (template.templateType == TemplateTypeEnum.TEMPLATE_SET) {
            specificTemplateTypeBuilder = new TemplateSetReportBuilder()
        } else if (template.templateType == TemplateTypeEnum.CASE_LINE) {
            specificTemplateTypeBuilder = new CaseLineListingReportBuilder()
        } else {
            specificTemplateTypeBuilder = new UnknownReportBuilder()
        }

        // Report footer
        HeaderBuilder headerBuilder = new HeaderBuilder()
        FooterBuilder footerBuilder = new FooterBuilder()
        if (!template.ciomsI) {
            headerBuilder.setHeaderTemplate(report)
            footerBuilder.setFooterTemplate(report, template)
        }

        def params = [:]
        return specificTemplateTypeBuilder.buildTemplate(template, report, params, LocaleContextHolder.getLocale().toString())
    }

    def setFavorite(ReportTemplate reportTemplate, Boolean isFavorite) {
        User user = userService.getUser()
        if (reportTemplate) {
            TemplateUserState state = TemplateUserState.findByUserAndTemplate(user, reportTemplate)
            if (!state) {
                state = new TemplateUserState(user: user, template: reportTemplate)
            }
            state.isFavorite = isFavorite ? true : null
            state.save()
        }
    }

    List<Map> getTemplatesByUser(User user, String search = '', Integer max = 30, Integer offset = 0) {
        List<Map> templateList = ReportTemplate.createCriteria().list([max: max, offset: offset, fetch: [owner: 'join']]) {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("name", "name")
                property("description", "description")
                'owner'{
                    property("fullName", "owner")
                }
            }
            createAlias('userTemplates', 'ut', CriteriaSpecification.LEFT_JOIN)
            createAlias('userGroupTemplates', 'ugt', CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
            eq("originalTemplateId", 0L)
            if (!user.isAnyAdmin()) {
                or {
                    eq('owner.id', user.id)
                    eq('ut.user.id', user.id)
                    if (UserGroup.countAllUserGroupByUser(user)) {
                        'in'('ugt.userGroup', UserGroup.fetchAllUserGroupByUser(user))
                    }
                }
            }
            if (search) {
                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
                    }
                }
            }
            and {
                order('name','asc')
                'owner' {
                    order('fullName','desc')
                }
            }

        }
        return templateList
    }

    Integer countTemplatesByUser(User user, String search) {
        Integer templateCount = ReportTemplate.createCriteria().count {
            createAlias('userTemplates', 'ut', CriteriaSpecification.LEFT_JOIN)
            createAlias('userGroupTemplates', 'ugt', CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
            eq("originalTemplateId", 0L)
            if (!user.isAnyAdmin()) {
                or {
                    'owner' {
                        eq('id', user.id)
                    }
                    eq('ut.user.id', user.id)
                    if (UserGroup.countAllUserGroupByUser(user)) {
                        'in'('ugt.userGroup', UserGroup.fetchAllUserGroupByUser(user))
                    }
                }
            }
            if (search) {
                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
                    }
                }
            }
        }
        return templateCount
    }

    List<Map> generateTemplateListByIDs(List<String> idList) {
        List<Long> templateIdList = idList.collect {
            it as Long
        }
        List<Map> templateList = ReportTemplate.createCriteria().list([sort: "name"]) {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("name", "name")
                property("description", "description")
            }
            'in'('id', templateIdList)
        }
        templateList
    }

    Map getTemplatesDetailByUser(User user, Integer max=0, Integer offset=0, String searchString='', String sort='lastUpdated', String dir='desc') {
        Map result = [:]
        result['recordsTotal'] = ReportTemplate.fetchAllByUser(user).count()
        result['aaData'] = ReportTemplate.fetchAllByUserFiltered(user, searchString, sort, dir).listDistinct([fetch: [owner: 'join'], max: max, offset: offset])
        result['recordsFiltered'] =  ReportTemplate.fetchCountByUserFiltered(user, searchString, sort, dir).get()
        return result
    }

    List<Map> generateNotDeletedTemplateList() {
        List<Map> templateList = ReportTemplate.createCriteria().list([sort: "name"]) {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            eq('isDeleted', false)
            eq("originalTemplateId", 0L)
            projections {
                property("id", "id")
                property("name", "name")
                property("description", "description")
            }
        }
        templateList
    }

    Map getResultTable(CaseLineListingTemplate template) {
        Map result = [:]

        List<ReportFieldInfo> columnList = template.columnList?.reportFieldInfoList
        List<ReportFieldInfo> groupingList = template.groupingList?.reportFieldInfoList
        List<ReportFieldInfo> rowColumnList = template.rowColumnList?.reportFieldInfoList
        List<ReportFieldInfo> serviceColumnList = template.serviceColumnList?.reportFieldInfoList

        result.rowColumns = rowColumnList.collect { it.reportField.name } ?: []
        result.groupColumns = groupingList.collect { it.reportField.name } ?: []
        result.serviceColumns = serviceColumnList.collect { it.reportField.name } ?: []
        List<String> headerList = []
        result.fieldTypeMap = [:]
        result.fieldsCodeNameMap = [:]
        template.allSelectedFieldsInfo.each { fieldInfo ->
            String uniqueName = addAsUniqueFieldName(headerList, fieldInfo)
            result.fieldTypeMap.put(uniqueName, (fieldInfo.customExpression ? "Custom" : fieldInfo.reportField.dataType.simpleName))
            result.fieldsCodeNameMap.put(uniqueName, fieldInfo.renameValue ?: (ViewHelper.getMessage('app.reportField.' + fieldInfo.reportField.name) ?: fieldInfo.reportField.name))
        }
        result.templateHeader=headerList
        result.header = headerList + ['cllRowId','actionItemStatus','latestComment','workFlowState','finalState','assignedToUser','assignedToUserId','assignedToGroup','assignedToGroupId','dueInDays','indicator','hasAttachments','hasIssues']
        Map stacked = [:]
        columnList.eachWithIndex { ReportFieldInfo column, int i ->
            if (column.stackId > -1) {
                String uniqueName = headerList[i];
                stacked[uniqueName] = column.stackId
            }
        }
        result.stacked = stacked ?: [:]
        return result
    }

    private String getDateConverter(GranularityEnum granularity) {
        switch (granularity) {
            case GranularityEnum.WEEKLY:
                return '"Week"WW-YYYY'
            case GranularityEnum.MONTHLY:
                return 'YYYY-MM'
            case GranularityEnum.QUARTERLY:
                return 'YYYY-"Q"Q'
            case GranularityEnum.ANNUALLY:
                return 'YYYY'
            default:
                return 'DD-MM-YYYY'

        }
    }

    private def getJavaDateConverter(GranularityEnum granularity) {
        switch (granularity) {
            case GranularityEnum.WEEKLY:
                return { String date ->
                    Date d = Date.parse("yyyy-MM-dd", date)
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    int week = cal.get(Calendar.WEEK_OF_YEAR);
                    return "Week" + (week < 10 ? "0" : "") + week + "-" + d.format("yyyy")
                }
            case GranularityEnum.MONTHLY:
                return { String date ->
                    Date d = Date.parse("yyyy-MM-dd", date)
                    return d.format("yyyy-MM")
                }
            case GranularityEnum.QUARTERLY:
                return { String date ->
                    Date d = Date.parse("yyyy-MM-dd", date)
                    String Q = "1"
                    if (d.getMonth() > 2 && d.getMonth() < 6) Q = "2"
                    if (d.getMonth() > 5 && d.getMonth() < 9) Q = "3"
                    if (d.getMonth() > 8) Q = "4"
                    return d.format("yyyy") + "-Q" + Q
                }
                return 'YYYY-"Q"Q'
            case GranularityEnum.ANNUALLY:
                return { String date ->
                    Date d = Date.parse("yyyy-MM-dd", date)
                    return d.format("yyyy")
                }
            case GranularityEnum.DAILY:
                return { String date ->
                    Date d = Date.parse("yyyy-MM-dd", date)
                    return d.format("dd-MM-yyyy")
                }
            default:
                return null
        }
    }

    List<String> getColumnNamesList(String columnNames) {
        return columnNames.replace("[", "").replace("]", "")
                .replace(CustomSQLReportBuilder.CHART_COLUMN_P_PREFIX, "")
                .replace(CustomSQLReportBuilder.CHART_COLUMN_PREFIX, "")
                .replace("\"", "").tokenize(',').collect { it.trim().toUpperCase() }
    }

    Map getDataFromReportResultData(ReportResult reportResult, List<String> columnNamesList, Map columnTypeMap, def filterCodesList, Long offset, Long max, String sortDir,
                                    String sortField, def searchData, String globalSearchData, String rowIdFilter = null, Boolean withCllRowIds=false, String chartFilter = null) {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd")
        dataFormat.setLenient(false)
        dataFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
        SimpleDateFormat inputFormat = new SimpleDateFormat(DateUtil.getShortDateFormatForLocale(userService.currentUser.preference.locale))
        inputFormat.setLenient(false)
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
        if (reportResult?.data?.value) {
            Locale locale = userService.currentUser.preference.locale
            ExecutedTemplateQuery ex = reportResult.getExecutedTemplateQuery()
            def granularityDateConverter = getJavaDateConverter(ex.granularity)
            List fieldTypeMap = []
            ReportTemplate template = GrailsHibernateUtil.unwrapIfProxy(reportResult.template ?: ex.executedTemplate)
            if (granularityDateConverter && (template instanceof ExecutedCaseLineListingTemplate)) {
                fieldTypeMap = getResultTable(template)?.fieldTypeMap?.collect { k, v -> v }
            }

            def reportResultInputStream = new GZIPInputStream(new ByteArrayInputStream(reportResult?.data?.getDecryptedValue()))
            def scanningResult = new Scanner(reportResultInputStream).useDelimiter("\\A")
            def theString = scanningResult.next()
            List rows = CsvUtil.parseCsv(new StringReader(theString))
            if (withCllRowIds) {
                List tempRows = []
                rows.eachWithIndex { List entry, int i ->
                    List l = new LinkedList(entry)
                    l.add(i)
                    tempRows << l
                }
                rows = tempRows
            }
            if (rowIdFilter) {
                rows = rowIdFilter.split(",").collect {
                    rows[it as Integer]
                }
            }
            if (chartFilter) {
                //this is using for click on chart only, sorting is not using while chart generating, so row number in chart equals to row number in initial csv
                int rowNumber = Integer.parseInt(chartFilter)
                Integer maxChartPoints = reportResult.executedTemplateQuery.usedTemplate.maxChartPoints
                if (maxChartPoints && (maxChartPoints <= rowNumber)) {
                    rows = rows.subList(maxChartPoints, rows.size())
                } else {
                    rows = [rows[rowNumber]]
                }
            }
            List resultRows = []
            if (filterCodesList || searchData || globalSearchData) {
                filterCodesList?.each {
                    it.field = columnNamesList.indexOf(it.field)
                }
                searchData?.each {
                    it.field = columnNamesList.indexOf(it.field)
                    if (DateUtil.checkDate(it.value, DateUtil.getShortDateFormatForLocale(locale))) {
                        SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.ISO_DATE_FORMAT)
                        it.value = formatter.format(new SimpleDateFormat(DateUtil.getShortDateFormatForLocale(locale)).parse(it.value))
                    } else {
                        it.value = it.value?.toLowerCase()
                    }
                }
                globalSearchData = globalSearchData?.toLowerCase()
                r:
                for (int i = 0; i < rows.size(); i++) {
                    for (int j = 0; j < filterCodesList?.size() ?: 0; j++) {
                        if (filterCodesList[j].field > -1) {
                            if (filterCodesList[j].value instanceof List) {
                                if (!filterCodesList[j].value.find { (it ?: "") == rows[i][filterCodesList[j].field] }) continue r
                            } else {
                                if (granularityDateConverter && fieldTypeMap[filterCodesList[j].field] == "Date") {
                                    String convertedSourceValue = granularityDateConverter(rows[i][filterCodesList[j].field])
                                    if (convertedSourceValue != (filterCodesList[j].value ?: "")) continue r
                                } else if (rows[i][filterCodesList[j].field] != (filterCodesList[j].value ?: "")) continue r
                            }
                        }
                    }
                    for (int j = 0; j < searchData?.size() ?: 0; j++) {
                        if ((searchData[j].field > -1) && ((searchData[j].value) || (searchData[j].value1) || (searchData[j].value2))) {
                            if (rows[i][searchData[j].field] == null) continue r;
                            if (searchData[j].type == "number") {
                                if (rows[i][searchData[j].field].toLowerCase() != searchData[j].value) continue r
                            } else if (searchData[j].type == "range") {
                                try {
                                    Date date = dataFormat.parse(rows[i][searchData[j].field])
                                    if (searchData[j].value1) {
                                        Date from = inputFormat.parse(searchData[j].value1)
                                        if (date.getTime() < from.getTime()) continue r;
                                    }
                                    if (searchData[j].value2) {
                                        Date to = inputFormat.parse(searchData[j].value2)
                                        if (date.getTime() > to.getTime()) continue r;
                                    }
                                } catch (Exception e) {
                                    continue r;
                                }
                            } else {
                                if (rows[i][searchData[j].field].toLowerCase().indexOf(searchData[j].value) == -1) continue r
                            }
                        }
                    }
                    if (globalSearchData) {
                        boolean found = false
                        for (int j = 0; j < rows[i].size()-(withCllRowIds?1:0); j++) {
                            String val = rows[i][j]
                            if (val.toLowerCase()?.contains(globalSearchData)) {
                                found = true; break;
                            }
                            if (val && fieldTypeMap[j] == "Date") {
                                if (inputFormat.format(dataFormat.parse(val)).toLowerCase()?.contains(globalSearchData)) {
                                    found = true; break;
                                }
                            }
                        }
                        if (!found) continue r
                    }
                    resultRows << rows[i]
                }
            } else {
                resultRows = rows
            }
            if (sortField) {
                int index = columnNamesList.findIndexOf { it == sortField }
                boolean isCustom = true;
                boolean isString = false;
                boolean isNumber = false;
                boolean isDate = false;
                if (columnTypeMap != null && columnTypeMap[sortField] != null) {
                    isCustom = (columnTypeMap[sortField]?.toString() == "Custom")
                    isString = (columnTypeMap[sortField]?.toString() == "String")
                    isDate = (columnTypeMap[sortField]?.toString() == "Date")
                    isNumber = columnTypeMap[sortField]?.toString()?.toLowerCase() == "number"
                }
                if (isCustom) {
                    try {//trying to sort as date
                        resultRows = resultRows.sort { parseDateForSort(it[index]?.toString()) }
                    } catch (ParseException e) {
                        try {//trying to sort as integer
                            resultRows = resultRows.sort {
                                String numberString = it[index]?.toString().trim();
                                return (numberString == null || numberString.isEmpty()) ? 0 : Double.parseDouble(numberString)
                            }
                        } catch (Exception ee) { //sorting as string
                            resultRows = resultRows.sort { it[index]?.toString().trim().toUpperCase() }
                        }
                    }
                } else if (isDate) {
                    try {
                        resultRows = resultRows.sort { parseDateForSort(it[index]?.toString()) }
                    } catch (ParseException e) {
                        resultRows = resultRows.sort { it[index] }
                    }
                } else if (isString) {
                    resultRows = resultRows.sort { it[index]?.toString().trim().toUpperCase() }
                } else if (isNumber) {
                    resultRows = resultRows.sort { parseNumberForSorting(it[index]?.toString()) }
                } else {
                    resultRows = resultRows.sort { it[index] }
                }
                if (sortDir == "desc") resultRows = resultRows.reverse();
            }

            List page = ((offset != null) && (max != null)) ? resultRows.subList(offset as int, Math.min(offset + max, resultRows.size()) as int) : resultRows
            return [aaData: page, recordsTotal: rows.size(), recordsFiltered: resultRows.size()]
        }
        return [aaData: [], recordsTotal: 0, recordsFiltered: 0]
    }
    public static Date parseDateForSort(String dateString) throws ParseException {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        for (String format : DateUtil.DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return sdf.parse(dateString);
            } catch (ParseException e) {
                // Try next format
            }
        }

        throw new ParseException("Unparseable date: \"" + dateString + "\". Supported formats: " + DateUtil.DATE_FORMATS, 0);
    }

    Double parseNumberForSorting(String numberString) {
        Double value
        if (numberString == null || numberString.trim().isEmpty()) {
            return 0
        }
        try {
            value = Double.parseDouble(numberString.trim())
        } catch (Exception e) {
            log.warn(numberString + " value can not be parsed as number in TemplateService")
            value = 0
        }
        return value
    }



    def getFilterDataForInteractiveOutput(String paramsFilterData){
        def filterCodesList = paramsFilterData ? new JsonSlurper().parseText(paramsFilterData) : []

        if (filterCodesList){
            // For Subtotal hyperlinks
            filterCodesList = removeSubtotalFilter(filterCodesList)
        }
        return filterCodesList
    }

    List getSearchDataForInteractiveOutput(String paramsSearchData) {
        def searchData = []
        def searchDataItr = paramsSearchData ? new JsonSlurper().parseText(paramsSearchData) : []
        searchDataItr.each { k, v ->
            searchData.add(["field": k, "value": v.value, "value1": v.value1, "value2": v.value2, type: v.type])
        }
        return searchData
    }

    Map getCllDrilldownDataAjax(def filterData, ReportResult reportResult, Long offset, Long max, String sortDir,
                                    String sortField, def searchData, String globalSearchData, Map fieldTypeMap, List<String> templateHeader, Map additionalFilterMap=null, String assignedToFilter = null, String rowIdFilters = null, def linkFilter = null) {

        ExecutedTemplateQuery ex = reportResult.getExecutedTemplateQuery()
        String granularityDateConverter = getDateConverter(ex.granularity)
        StringBuilder stringBuilder = new StringBuilder()
        String ddwnMetadataTable = null
        String viewPvrCaseTable = null
        boolean reasonOfDelay = false
        boolean inboundCompliance = false
        if((reportResult.template?:reportResult.executedTemplateQuery.executedTemplate).name == Holders.config.getProperty('pvcModule.inbound_processing_template')) {
            inboundCompliance = true
            ddwnMetadataTable = "IN_DRILLDOWN_METADATA"
        }else if((reportResult.template?:reportResult.executedTemplateQuery.executedTemplate).name == Holders.config.getProperty('pvcModule.late_processing_template')) {
            reasonOfDelay = true
            ddwnMetadataTable = "DRILLDOWN_METADATA"
        }
        stringBuilder.append("WITH TAB1 AS (SELECT DD.CLL_ROW_DATA, DD.ID, count(1) over(order by 1) as recordCount ")
        if (reasonOfDelay || inboundCompliance) {
            Boolean fieldExists = searchData.flatten().any { it.field == "pvcCapaIssueNumber" }
            if(fieldExists){
                stringBuilder.append(",DM.LAST_UPDATED_ISSUE")
            }
            if (globalSearchData || (reasonOfDelay && searchData.find { it.field in REDUNDANT_JSON_KEYS } || REDUNDANT_JSON_KEYS.contains(sortField)) || (inboundCompliance && searchData.find { it.field in INBOUND_REDUNDANT_JSON_KEYS } || INBOUND_REDUNDANT_JSON_KEYS.contains(sortField))) {
                stringBuilder.append(", DM.WORKFLOW_STATE_ID, DM.ASSIGNED_TO_USER, DM.ASSIGNED_TO_USERGROUP, " +
                        "VWPVR.VERSION_NUM, VWPVR.LATE, VWPVR.REPONSIBLE_PARTY, VWPVR.CORRECTIVE_ACTION, VWPVR.CORRECTIVE_DATE, VWPVR.PREVENTATIVE_ACTION, VWPVR.PREVENTATIVE_DATE, VWPVR.INVESTIGATION, VWPVR.SUMMARY, VWPVR.ACTIONS, VWPVR.ROOT_CAUSE_SUB_CATEGORY, VWPVR.ROOT_CAUSE_CLASSIFICATION " +
                        "FROM DRILLDOWN_DATA DD LEFT JOIN ${ddwnMetadataTable} DM ")
                if(inboundCompliance) {
                    stringBuilder.append(" ON JSON_VALUE(dd.cll_row_data, '\$.masterCaseId')=DM.CASE_ID and JSON_VALUE(dd.cll_row_data, '\$.masterEnterpriseId')=DM.TENANT_ID and JSON_VALUE(dd.cll_row_data, '\$.pvcIcSenderId')=DM.SENDER_ID and JSON_VALUE(dd.cll_row_data, '\$.masterVersionNum')=DM.VERSION_NUM " +
                            "LEFT JOIN vw_c_inbound_compliance_rca VWPVR ON JSON_VALUE(dd.cll_row_data, '\$.masterCaseId')=VWPVR.CASE_ID and JSON_VALUE(dd.cll_row_data, '\$.masterEnterpriseId')=VWPVR.TENANT_ID and JSON_VALUE(dd.cll_row_data, '\$.pvcIcSenderId')=VWPVR.SENDER_ID and JSON_VALUE(dd.cll_row_data, '\$.masterVersionNum')=VWPVR.VERSION_NUM ")
                }else {
                    stringBuilder.append(" ON JSON_VALUE(dd.cll_row_data, '\$.masterCaseId')=DM.CASE_ID and JSON_VALUE(dd.cll_row_data, '\$.masterEnterpriseId')=DM.TENANT_ID and JSON_VALUE(dd.cll_row_data, '\$.vcsProcessedReportId')=DM.PROCESSED_REPORT_ID " +
                            "LEFT JOIN VW_PVR_LATE_CASE_PROCESSING VWPVR ON JSON_VALUE(dd.cll_row_data, '\$.masterCaseId')=VWPVR.CASE_ID and JSON_VALUE(dd.cll_row_data, '\$.masterEnterpriseId')=VWPVR.TENANT_ID and JSON_VALUE(dd.cll_row_data, '\$.vcsProcessedReportId')=VWPVR.PROCESSED_REPORT_ID ")
                }
                stringBuilder.append(" and VWPVR.FLAG_PRIMARY=1  LEFT JOIN PVUSER U on u.id=DM.ASSIGNED_TO_USER  LEFT JOIN USER_GROUP ug on ug.id=DM.ASSIGNED_TO_USERGROUP")
            }else {
                if (inboundCompliance) {
                    stringBuilder.append(", DM.WORKFLOW_STATE_ID, DM.ASSIGNED_TO_USER, DM.ASSIGNED_TO_USERGROUP " +
                            "FROM DRILLDOWN_DATA DD LEFT JOIN ${ddwnMetadataTable} DM ON JSON_VALUE(dd.cll_row_data, '\$.masterCaseId')=DM.CASE_ID and JSON_VALUE(dd.cll_row_data, '\$.masterEnterpriseId')=DM.TENANT_ID and JSON_VALUE(dd.cll_row_data, '\$.pvcIcSenderId')=DM.SENDER_ID and JSON_VALUE(dd.cll_row_data, '\$.masterVersionNum')=DM.VERSION_NUM ")
                } else {
                    stringBuilder.append(", DM.WORKFLOW_STATE_ID, DM.ASSIGNED_TO_USER, DM.ASSIGNED_TO_USERGROUP " +
                            "FROM DRILLDOWN_DATA DD LEFT JOIN ${ddwnMetadataTable} DM ON JSON_VALUE(dd.cll_row_data, '\$.masterCaseId')=DM.CASE_ID and JSON_VALUE(dd.cll_row_data, '\$.masterEnterpriseId')=DM.TENANT_ID and JSON_VALUE(dd.cll_row_data, '\$.vcsProcessedReportId')=DM.PROCESSED_REPORT_ID ")
                }
                stringBuilder.append("  LEFT JOIN USER_GROUP ug on ug.id=DM.ASSIGNED_TO_USERGROUP")
            }
            if(globalSearchData || searchData.find { it.field in WORKFLOW_STATE } || (sortField==WORKFLOW_STATE)){
                stringBuilder.append(" LEFT JOIN WORKFLOW_STATE WF on WF.ID=DM.WORKFLOW_STATE_ID ")
            }
            stringBuilder.append(" WHERE 1=1 ")
            User currentUser = userService.currentUser
            if (!assignedToFilter && currentUser && SpringSecurityUtils.ifAnyGranted("ROLE_USER_GROUP_RCA")) {
                assignedToFilter = AssignedToFilterEnum.MY_GROUPS.name()
            }
            if(assignedToFilter){
                String assignedToFilterClause = addAssignedToFilterWhereClause(assignedToFilter)
                stringBuilder.append(assignedToFilterClause)
            }

        }else{
            stringBuilder.append("FROM DRILLDOWN_DATA DD WHERE 1=1 ")
        }

        if(additionalFilterMap != null && additionalFilterMap.containsKey('cllRecordId')){
            if(linkFilter){
                linkFilter = new JsonSlurper().parseText(linkFilter)
                stringBuilder.append(" AND DD.ID IN (" + linkFilter.join(",") + ")")
            }else{
                stringBuilder.append(" AND DD.ID = "+additionalFilterMap.get('cllRecordId')+"")
            }

        }else if(linkFilter && linkFilter != "null"){
            linkFilter = new JsonSlurper().parseText(linkFilter)
            stringBuilder.append(" AND DD.ID IN (" + linkFilter.join(",") + ")")

        }else {
            int index = 0
            filterData?.each {
                if (it.field) {
                    def value = (it.value.equals('') || (it.value == null) || (it.value == 'null')) ? null : it.value
                    if (value != null) {
                        if (value instanceof Collection) {
                            Set values = value.toSet()
                            if (values.any { (it == "" || it == null) }) {
                                stringBuilder.append(" AND (JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "')  is null OR JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "') IN ('" + values.join("','") + "'))")
                            } else {
                                stringBuilder.append(" AND JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "') IN ('" + values*.replaceAll("'", "''").join("','") + "')")
                            }
                        } else {
                            if (granularityDateConverter && fieldTypeMap[it.field] == "Date") {
                                stringBuilder.append(" AND to_char(to_timestamp_tz(''||JSON_VALUE(DD.CLL_ROW_DATA, '\$.${it.field}'),'YYYY-MM-DD\"T\"HH24:MI:SSTZHTZM'),'${granularityDateConverter}') like '" + value + "'")
                            } else {
                                stringBuilder.append(" AND (JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "') = '" + value.replaceAll("'", "''") + "'")
                                stringBuilder.append(" OR JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "') = '\"" + value.replaceAll("'", "''") + "\"')")
                            }
                        }
                    } else {
                        stringBuilder.append(" AND JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "') is null")
                    }
                    index++
                }
            }

            if(globalSearchData){
                stringBuilder.append(addGlobalSearchWhereClause(globalSearchData, fieldTypeMap, reasonOfDelay, inboundCompliance))
            }

            searchData?.eachWithIndex{it, count ->
                if (it.field) {
                    def value = (it.value.equals('') || (it.value == null)) ? null : it.value
                    if ((value != null) || (it.value1)||(it.value2)) {
                        if (it.field == ASSIGNED_TO) {
                            if ((it.value.charAt(0) == "\"") && (it.value.charAt(it.value.length() - 1) == "\"")) {
                                it.value = it.value.substring(1, it.value.length() - 1)
                                stringBuilder.append(" AND (UPPER(DM.ASSIGNED_TO_NAME) LIKE UPPER('${it.value}') ) ")
                            }
                            else{
                            stringBuilder.append(" AND (UPPER(DM.ASSIGNED_TO_NAME) LIKE UPPER('%${it.value}%') ) ")
                            }
                        } else if (it.field == ASSIGNED_TO_GROUP) {
                            if ((it.value.charAt(0) == "\"") && (it.value.charAt(it.value.length() - 1) == "\"")) {
                                it.value = it.value.substring(1, it.value.length() - 1)
                                stringBuilder.append(" AND (UPPER(ug.NAME) LIKE UPPER('${it.value}') ) ")
                            } else {
                                stringBuilder.append(" AND (UPPER(ug.NAME) LIKE UPPER('%${it.value}%') ) ")
                            }
                        } else if (it.field == DUE_DATE) {
                            if (it.value1)
                                stringBuilder.append(" AND TRUNC(DM.DUE_DATE) >= TO_TIMESTAMP_TZ('${it.value1}', 'DD-Mon-YYYY')");
                            if (it.value2)
                                stringBuilder.append(" AND TRUNC(DM.DUE_DATE) <= TO_TIMESTAMP_TZ('${it.value2}', 'DD-Mon-YYYY')");
                        }else if (it.field == WORKFLOW_STATE) {
                            if ((it.value.charAt(0) == "\"") && (it.value.charAt(it.value.length() - 1) == "\"")) {
                                it.value = it.value.substring(1, it.value.length() - 1)
                                stringBuilder.append(" AND (UPPER(WF.NAME) LIKE  UPPER('${it.value}')) ")
                            }
                            else{
                            stringBuilder.append(" AND (UPPER(WF.NAME) LIKE  UPPER('%${it.value}%')) ")
                            }
                        } else if ((reasonOfDelay && REDUNDANT_JSON_KEYS.contains(it.field)) || (inboundCompliance && INBOUND_REDUNDANT_JSON_KEYS.contains(it.field))) {
                            String martColumn = inboundCompliance ? INBOUND_JSON_KEY_TO_MART_COLUMN_MAP.get(it.field) : JSON_KEY_TO_MART_COLUMN_MAP.get(it.field)
                            if(martColumn.toLowerCase().contains("date")){
                                stringBuilder.append(" AND UPPER(TO_CHAR(TO_TIMESTAMP_TZ((VWPVR." + martColumn + "), '${martColumn=="CORRECTIVE_DATE" || martColumn=="PREVENTATIVE_DATE"? "DD-MM-YY" : "YYYY-MM-DD"}\"T\"HH24:MI:SSTZHTZM'), 'DD-MON-YYYY')) LIKE UPPER('%" + value + "%') ")
                            }
                            else {
                                if ((it.value.charAt(0) == "\"") && (it.value.charAt(it.value.length() - 1) == "\"")) {
                                    it.value = it.value.substring(1, it.value.length() - 1)
                                    stringBuilder.append(" AND UPPER(VWPVR." + martColumn + ") LIKE UPPER('"+it.value+"') ")
                                }
                                else{
                                stringBuilder.append(" AND UPPER(VWPVR." + martColumn + ") LIKE UPPER('%" + value + "%') ")
                                }
                            }
                        } else {
                            if (it.type == "number") {
                                stringBuilder.append(" AND JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "')='" + it.value.replaceAll("'", "''") + "' ")
                            } else if (it.type == "range") {
                                if (it.value1)
                                    stringBuilder.append(" AND TRUNC(TO_TIMESTAMP_TZ(JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "'), 'YYYY-MM-DD\"T\"HH24:MI:SSTZHTZM')) >= TO_TIMESTAMP_TZ('${it.value1}', 'DD-Mon-YYYY')");
                                if (it.value2)
                                    stringBuilder.append(" AND TRUNC(TO_TIMESTAMP_TZ(JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "'), 'YYYY-MM-DD\"T\"HH24:MI:SSTZHTZM')) <= TO_TIMESTAMP_TZ('${it.value2}', 'DD-Mon-YYYY')");

                            } else {
                                if (it.field == "masterCaseNum" && it.value?.contains(";")) {
                                    stringBuilder.append(" AND UPPER(CASE_NUM) in (''")
                                    it.value?.split(";")?.collect { it.trim() }?.findAll { it }?.each { c ->
                                        stringBuilder.append(",'" + c.toUpperCase() + "'")
                                    }
                                    stringBuilder.append(") ")
                                } else {
                                    if (fieldTypeMap[it.field].equals("Date")) {
                                        stringBuilder.append(" AND UPPER(TO_CHAR(TO_TIMESTAMP_TZ(JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "'), 'YYYY-MM-DD\"T\"HH24:MI:SSTZHTZM'), 'DD-MON-YYYY')) LIKE UPPER('%" + value + "%') ")
                                    } else {
                                        if ((it.value.charAt(0) == "\"") && (it.value.charAt(it.value.length() - 1) == "\"")) {
                                            it.value = it.value.substring(1, it.value.length() - 1)
                                            stringBuilder.append(" AND UPPER(JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "')) = UPPER('" + it.value.replaceAll("'", "''") + "') ")
                                        } else {
                                            if(it.field=='pvcCapaIssueNumber'){
                                                stringBuilder.append(" AND UPPER(DM.LAST_UPDATED_ISSUE) LIKE UPPER('%" + value.replaceAll("'", "''") + "%')")
                                            }
                                            else {
                                                stringBuilder.append(" AND UPPER(JSON_VALUE(DD.CLL_ROW_DATA, '\$." + it.field + "')) LIKE UPPER('%" + value.replaceAll("'", "''") + "%')")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        log.debug("additionalFilterMap : "+additionalFilterMap)
        if (additionalFilterMap?.containsKey('pvcLcpFlagPrimary')) {
            stringBuilder.append(" AND (JSON_VALUE(DD.CLL_ROW_DATA, '\$.pvcLcpFlagPrimary') IS NULL OR JSON_VALUE(DD.CLL_ROW_DATA, '\$.pvcLcpFlagPrimary') = 'Yes')  ")
        }
        stringBuilder.append(" AND DD.REPORT_RESULT_ID = " + reportResult.id + "")

        if (sortDir && sortField) {
            stringBuilder.append(sortColumn(sortDir,sortField,reasonOfDelay, inboundCompliance))
        } else {
            stringBuilder.append(" ORDER BY DD.ID ")
        }
        stringBuilder.append(") SELECT * FROM TAB1")
        if(offset != null){
            stringBuilder.append(" OFFSET ${offset}");
        }
        if(max != null) {
            stringBuilder.append(" ROWS FETCH NEXT ${max} ROWS ONLY ")
        }
        Sql pvrsql
        Long recordCount = 0L
        List csvList = []
        try {
            pvrsql = new Sql(getReportConnectionForPVR())

            String queryString = stringBuilder.toString()
            log.debug("QUERY " + queryString)
            List dbrecords = pvrsql.rows(queryString)
            //todo: update code for inbound
            JsonSlurper jsonSlurper = new JsonSlurper()
            List metadataRecordIds = []
            def startTime = System.currentTimeMillis()
            Set<String> rowIds = new HashSet<>();
            if(rowIdFilters && !rowIdFilters.isEmpty()) {
                rowIds = new HashSet<>(Arrays.asList(rowIdFilters.split(",")));
            }
            dbrecords.each { GroovyRowResult dbrecord ->
                Map dataMap = jsonSlurper.parseText(dbrecord[0]?.characterStream?.text)
                def metadataRecord = null
                Map metadataParams = [:]
                if (reasonOfDelay){
                    metadataParams.masterCaseId = dataMap['masterCaseId']
                    metadataParams.processedReportId = dataMap['vcsProcessedReportId']
                    metadataParams.tenantId = dataMap['masterEnterpriseId']
                    metadataRecord = DrilldownCLLMetadata.getMetadataRecord(metadataParams).get()
                }

                if(inboundCompliance) {
                    metadataParams.masterCaseId = dataMap['masterCaseId']
                    metadataParams.tenantId = dataMap['masterEnterpriseId']
                    metadataParams.masterVersionNum = dataMap['masterVersionNum']
                    metadataParams.senderId = dataMap['pvcIcSenderId']
                    metadataRecord = InboundDrilldownMetadata.getMetadataRecord(metadataParams).get()

                }
                metadataRecordIds.add(metadataRecord?.id)

                //this is used for the export feature only to the checked rows
                if(rowIdFilters && !rowIdFilters.isEmpty()) {
                    if (rowIds.contains(dbrecord[1].toString())) {
                        def record = parseCllRecord(dataMap, dbrecord[1], reasonOfDelay,inboundCompliance, templateHeader, metadataRecord)
                        csvList.add(record)
                    }
                } else {
                    def record = parseCllRecord(dataMap, dbrecord[1], reasonOfDelay,inboundCompliance, templateHeader, metadataRecord)
                    csvList.add(record)
                }
            }

            if (reasonOfDelay || inboundCompliance) {
                def attachmentsResult = []
                def issuesResult = []

                metadataRecordIds.collate(999).each {
                    List recordsIds = it.findAll { it != null }
                    String placeholder = (1..recordsIds.size()).collect { '?' }.join(',')
                    String attachmentsQuery  = ""
                    if(inboundCompliance){
                        attachmentsQuery = "SELECT IN_METADATA_ID FROM PVC_ATTACH WHERE IN_METADATA_ID IN (${placeholder})"
                    }else{
                        attachmentsQuery = "SELECT METADATA_ID FROM PVC_ATTACH WHERE METADATA_ID IN (${placeholder})"
                    }
                    String issuesQuery  = ""
                    if(inboundCompliance){
                        issuesQuery = "SELECT d.CLL_ROW_ID FROM CAPA_8D c INNER JOIN IN_DDWN_MDATA_ISSUES d ON c.ID = d.ISSUE_ID WHERE d.CLL_ROW_ID IN (${placeholder}) AND c.IS_DELETED = 0"
                    }else{
                        issuesQuery = "SELECT d.CLL_ROW_ID FROM CAPA_8D c INNER JOIN DDWN_MDATA_ISSUES d ON c.ID = d.ISSUE_ID WHERE d.CLL_ROW_ID IN (${placeholder}) AND c.IS_DELETED = 0"
                    }

                    attachmentsResult += pvrsql.rows(attachmentsQuery, recordsIds)
                    issuesResult += pvrsql.rows(issuesQuery, recordsIds)
                }

                csvList.eachWithIndex { List record, ind ->
                    long metadataRecordId = metadataRecordIds.size() > 0 ? metadataRecordIds[ind] : null
                    if (metadataRecordId) {
                        def attachmentExists = attachmentsResult.isEmpty() ? false : attachmentsResult.find { (inboundCompliance ? it.IN_METADATA_ID : it.METADATA_ID) == metadataRecordId }
                        def issuesExist = issuesResult.isEmpty() ? false : issuesResult.find { it.CLL_ROW_ID == metadataRecordId }
                        record.add(attachmentExists ? "true" : "false")
                        record.add(issuesExist ? "true" : "false")
                    } else {
                        record.add("false")
                        record.add("false")
                    }
                }
            }

            def totalTime = System.currentTimeMillis() - startTime
            log.info("Parsing of CLL record for dbrecords took: " + totalTime/60 + " secs")

            if(dbrecords.size() > 0){
                recordCount = Long.parseLong(dbrecords[0][2].toString());
            }
        } catch (Exception e) {
            log.error("Exception in fetching CLL Records", e)
        } finally {
            pvrsql?.close()
        }
        return [aaData: csvList, recordsTotal: recordCount, recordsFiltered: recordCount]
    }

    def removeSubtotalFilter(def filterCodesList){
        int subtotalInd = -1;
        for(int i = 0; i < filterCodesList.size(); i++){
            if (filterCodesList[i]?.get("value") in ["Subtotal","小計"]){
                subtotalInd = i
                break;
            }
        }
        if (subtotalInd != -1) filterCodesList.remove(subtotalInd)
        return filterCodesList
    }
    List<String> parseCllRecord(Map dataMap, def drilldownCllDataId, boolean reasonOfDelay, boolean inboundCompliance,List<String> templateHeader, def metadataRecord) {
        List<String> cllRecordData = []
        int index=-1
        templateHeader.eachWithIndex(){it,i->
            cllRecordData.add(dataMap[it])
            if(it == "pvcCapaIssueNumber"){
                index=i;
            }
        }
        cllRecordData.add(drilldownCllDataId.toString())
        if(reasonOfDelay|| inboundCompliance) {
            String assignedToUser = null
            String assignedToUserId = null
            String assignedToGroup = null
            String assignedToGroupId = null
            String actionItemStatus = null
            String latestComment = null
            WorkflowState workflow = WorkflowState.defaultWorkState
            if (metadataRecord != null){
                if(index!=-1 && metadataRecord.issues.size()>0) {
                    String issueNumber=metadataRecord.lastUpdatedIssue
                    if(issueNumber!=null) {
                        issueNumber = issueNumber.replaceAll(",", "\n")
                    }
                    cllRecordData[index] = issueNumber
                }
                actionItemStatus = actionItemService.getActionItemStatusForDrilldownRecord(metadataRecord)
                latestComment = commentService.getLatestCommentForDrilldownRecord(metadataRecord)
                workflow = getLatestWorkFlowForDrilldownRecord(metadataRecord)

                assignedToUser = metadataRecord.assignedToUser?.fullName ?: metadataRecord.assignedToUser?.username
                assignedToUserId = metadataRecord?.assignedToUser ? Constants.USER_TOKEN + metadataRecord.assignedToUser.id : ""

                assignedToGroup = metadataRecord.assignedToUserGroup?.name
                assignedToGroupId = metadataRecord?.assignedToUserGroup ? Constants.USER_GROUP_TOKEN + metadataRecord.assignedToUserGroup.id : ""

            }
            cllRecordData.add(actionItemStatus)
            cllRecordData.add(latestComment)
            cllRecordData.add(workflow.name)
            cllRecordData.add(workflow.finalState.toString())
            cllRecordData.add(assignedToUser)
            cllRecordData.add(assignedToUserId)
            cllRecordData.add(assignedToGroup)
            cllRecordData.add(assignedToGroupId)
            cllRecordData.add(metadataRecord.dueDate?.format(DateUtil.DATEPICKER_FORMAT))
            cllRecordData.add(getIndicator(metadataRecord.dueDate))
        }
        cllRecordData
    }

    private String getIndicator(Date dueInDate) {
        Date now = new Date();
        Date soon = now + 2;
        if (dueInDate > now && dueInDate < soon) return "yellow"
        if (dueInDate < now) return "red"
        return ""
    }


    WorkflowState getLatestWorkFlowForDrilldownRecord(def cllRecord){
        //DrilldownMetadata cllRecord = DrilldownMetadata.get(id)
        return cllRecord.workflowState?:WorkflowState.defaultWorkState
    }

    String addAsUniqueFieldName(List<String> drilldownReportFields, def fieldInfo) {
        String name
        name = fieldInfo instanceof ReportFieldInfo ? fieldInfo.reportField.name : fieldInfo
        Integer index = drilldownReportFields.findAll { (it == name) || it.startsWith(name + "__") }?.size()
        name = name + (index ? ("__" + index) : "")
        drilldownReportFields.add(name)
        return name
    }

    List<String> getPrevPeriodStartDateAndEndDate (BaseTemplateQuery templateQuery) {
        String startDate = ""
        String endDate = ""
        String defaultTimeZone = Constants.DEFAULT_SELECTED_TIMEZONE
        BaseConfiguration config = templateQuery.usedConfiguration
        Date templateStartDate = templateQuery?.startDate
        DateRangeEnum dateRangeEnum = templateQuery?.usedDateRangeInformationForTemplateQuery.dateRangeEnum
        Integer relativeDateRangeValue = templateQuery?.usedDateRangeInformationForTemplateQuery?.relativeDateRangeValue
        if (dateRangeEnum == DateRangeEnum.PR_DATE_RANGE) {
            if(config instanceof ExecutedReportConfiguration){
                dateRangeEnum = config?.executedGlobalDateRangeInformation?.dateRangeEnum
                relativeDateRangeValue = config?.executedGlobalDateRangeInformation?.relativeDateRangeValue
            }else{
                dateRangeEnum = config?.globalDateRangeInformation?.dateRangeEnum
                relativeDateRangeValue = config?.globalDateRangeInformation?.relativeDateRangeValue
            }
        }

        switch (dateRangeEnum) {
            case DateRangeEnum.CUMULATIVE:
                startDate = templateQuery?.startDate?.format(DATE_FMT)?.toString()
                endDate = templateQuery?.endDate?.format(DATE_FMT)?.toString()
                break
            case DateRangeEnum.YESTERDAY:
                (startDate, endDate) = RelativeDateConverter.lastXDays(templateStartDate, 1, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.LAST_WEEK:
                (startDate, endDate) = RelativeDateConverter.lastXWeeks(templateStartDate, 1, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.LAST_MONTH:
                (startDate, endDate) = RelativeDateConverter.lastXMonths(templateStartDate, 1, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.LAST_YEAR:
                (startDate, endDate) = RelativeDateConverter.lastXYears(templateStartDate, 1, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.LAST_X_DAYS:
                (startDate, endDate) = RelativeDateConverter.lastXDays(templateStartDate, relativeDateRangeValue, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.LAST_X_WEEKS:
                (startDate, endDate) = RelativeDateConverter.lastXWeeks(templateStartDate, relativeDateRangeValue, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.LAST_X_MONTHS:
                (startDate, endDate) = RelativeDateConverter.lastXMonths(templateStartDate, relativeDateRangeValue, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.LAST_X_YEARS:
                (startDate, endDate) = RelativeDateConverter.lastXYears(templateStartDate, relativeDateRangeValue, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.TOMORROW:
                (startDate, endDate) = RelativeDateConverter.lastXDays(templateStartDate, relativeDateRangeValue, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.NEXT_WEEK:
                (startDate, endDate) = RelativeDateConverter.lastXWeeks(templateStartDate, 1, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.NEXT_MONTH:
                (startDate, endDate) = RelativeDateConverter.lastXMonths(templateStartDate, 1, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.NEXT_YEAR:
                (startDate, endDate) = RelativeDateConverter.lastXYears(templateStartDate, 1, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.NEXT_X_DAYS:
                (startDate, endDate) = RelativeDateConverter.lastXDays(templateStartDate, relativeDateRangeValue, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.NEXT_X_WEEKS:
                (startDate, endDate) = RelativeDateConverter.lastXWeeks(templateStartDate, relativeDateRangeValue, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.NEXT_X_MONTHS:
                (startDate, endDate) = RelativeDateConverter.lastXMonths(templateStartDate, relativeDateRangeValue, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            case DateRangeEnum.NEXT_X_YEARS:
                (startDate, endDate) = RelativeDateConverter.lastXYears(templateStartDate, relativeDateRangeValue, defaultTimeZone).collect { it.format(DATE_FMT)?.toString() }
                break
            default:
                startDate = (templateQuery?.startDate - 1.second -(templateQuery?.endDate - templateQuery?.startDate))?.format(DATE_FMT)?.toString()
                endDate = (templateQuery?.startDate - 1.second)?.format(DATE_FMT)?.toString()
        }
        [startDate, endDate]
    }

    String addGlobalSearchWhereClause(String searchString, Map fieldMap, boolean reasonOfDelay, boolean inboundCompliance){
        Set jsonKeys = fieldMap.keySet()
        searchString = searchString.replaceAll("'", "''")
        StringBuilder globalSearchWhereClause = new StringBuilder()
        globalSearchWhereClause.append(" AND (")
        jsonKeys.eachWithIndex{ field, idx ->
            if((!reasonOfDelay || !REDUNDANT_JSON_KEYS.contains(field)) && (!inboundCompliance || !INBOUND_REDUNDANT_JSON_KEYS.contains(field))) {
                if (fieldMap[field].equals("Date")) {
                    globalSearchWhereClause.append(" UPPER(TO_CHAR(TO_TIMESTAMP_TZ(JSON_VALUE(DD.CLL_ROW_DATA, '\$." + field + "'), 'YYYY-MM-DD\"T\"HH24:MI:SSTZHTZM'), 'DD-MON-YYYY')) LIKE UPPER('%" + searchString + "%') ")
                } else {
                    globalSearchWhereClause.append(" UPPER(JSON_VALUE(DD.CLL_ROW_DATA, '\$." + field + "')) LIKE UPPER('%" + searchString + "%') ")
                }
                if(idx != jsonKeys.size()-1){
                    globalSearchWhereClause.append(" OR");
                }
            }
        }
        if(reasonOfDelay || inboundCompliance) {
            globalSearchWhereClause.append(" OR TO_CHAR(DM.DUE_DATE,'DD-MON-YYYY') LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(WF.NAME) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(ASSIGNED_TO_NAME) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(ug.NAME) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(VWPVR.LATE) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(VWPVR.REPONSIBLE_PARTY) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(VWPVR.ROOT_CAUSE) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(VWPVR.ROOT_CAUSE_SUB_CATEGORY) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(VWPVR.ROOT_CAUSE_CLASSIFICATION) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(VWPVR.CORRECTIVE_ACTION) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(VWPVR.PREVENTATIVE_ACTION) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(VWPVR.INVESTIGATION) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(VWPVR.SUMMARY) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(VWPVR.ACTIONS) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(U.FULLNAME) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(UG.NAME) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(TO_CHAR(TO_TIMESTAMP_TZ(VWPVR.CORRECTIVE_DATE, 'YYYY-MM-DD\"T\"HH24:MI:SSTZHTZM'), 'DD-MON-YYYY')) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(TO_CHAR(TO_TIMESTAMP_TZ(VWPVR.PREVENTATIVE_DATE, 'YYYY-MM-DD\"T\"HH24:MI:SSTZHTZM'), 'DD-MON-YYYY')) LIKE UPPER('%" + searchString + "%') OR ")
            globalSearchWhereClause.append(" UPPER(DM.LAST_UPDATED_ISSUE) LIKE UPPER('%" + searchString.replaceAll("'", "''") + "%') ")
        }
        globalSearchWhereClause.append(" )")
        return globalSearchWhereClause.toString()
    }

    String sortColumn(String sortDir, String sortField, boolean reasonOfDelay, boolean inboundCompliance){
        StringBuilder sortColumnString = new StringBuilder()
        String martColumn = inboundCompliance ? INBOUND_JSON_KEY_TO_MART_COLUMN_MAP.get(sortField) : JSON_KEY_TO_MART_COLUMN_MAP.get(sortField)
        if(reasonOfDelay || inboundCompliance) {
            if((reasonOfDelay && REDUNDANT_JSON_KEYS.contains(sortField)) || (inboundCompliance && INBOUND_REDUNDANT_JSON_KEYS.contains(sortField))){
                if(sortField.contains("Date")){
                    sortColumnString.append(" ORDER BY (VWPVR." + martColumn + ")")
                } else {
                    sortColumnString.append(" ORDER BY UPPER(VWPVR." + martColumn + ")")
                }

            } else if (sortField == DUE_DATE) {
                sortColumnString.append(" ORDER BY DUE_DATE")
            } else if (sortField == ASSIGNED_TO) {
                sortColumnString.append(" ORDER BY  UPPER(ASSIGNED_TO_NAME)")
            } else if (sortField == ASSIGNED_TO_GROUP) {
                sortColumnString.append(" ORDER BY  UPPER(ug.NAME)")
            } else if (sortField == WORKFLOW_STATE) {
                sortColumnString.append(" ORDER BY UPPER(WF.NAME)")
            } else if (sortField == PROCESSING_TIME) {
                sortColumnString.append(" ORDER BY TO_NUMBER(JSON_VALUE(DD.CLL_ROW_DATA, '\$.${sortField}'))")
            } else if (sortField == "pvcCapaIssueNumber"){
                sortColumnString.append(" ORDER BY UPPER(DM.LAST_UPDATED_ISSUE)")
            } else {
                sortColumnString.append(" ORDER BY UPPER(JSON_VALUE(DD.CLL_ROW_DATA, '\$.${sortField}'))")
            }
        }
        else{
            sortColumnString.append(" ORDER BY UPPER(JSON_VALUE(DD.CLL_ROW_DATA, '\$.${sortField}'))")
        }
        sortColumnString.append(" ${sortDir}")
        return sortColumnString.toString()
    }

    String addAssignedToFilterWhereClause(String assignedToFilter){
        StringBuilder whereClause = new StringBuilder()
        whereClause.append(" AND ")
        User currentUser = userService.currentUser
        if (!assignedToFilter && currentUser && SpringSecurityUtils.ifAnyGranted("ROLE_USER_GROUP_RCA")) {
            assignedToFilter = AssignedToFilterEnum.MY_GROUPS.name()
        }
        switch(assignedToFilter) {
            case AssignedToFilterEnum.ME.name():
                Long userId = userService.currentUser.id
                whereClause.append("DM.ASSIGNED_TO_USER = " + userId + " ")
                break
            case AssignedToFilterEnum.MY_GROUPS.name():
                Long userId = userService.currentUser.id
                List<UserGroup> userGroups = UserGroup.fetchAllUserGroupByUser(userService.currentUser)
                StringBuilder assignedToUserGroupClause = new StringBuilder()
                StringBuilder assignedToUserClause = new StringBuilder()
                assignedToUserGroupClause.append("(DM.ASSIGNED_TO_USERGROUP IN (0")
                assignedToUserClause.append(") OR (DM.ASSIGNED_TO_USERGROUP is Null and DM.ASSIGNED_TO_USER IN (")
                Set<Long> ids = [0L]
                boolean userHasRole = userService.currentUser.authorities.any { it.authority == 'ROLE_USER_GROUP_RCA' }
                userGroups.eachWithIndex { val, idx ->
                    if(!userHasRole || val.authorities.authority.contains("ROLE_USER_GROUP_RCA")) {
                        val.getUsers().collect {
                            ids.add(it.id)
                        }
                        assignedToUserGroupClause.append("," + val.id + "")
                    }
                }
                def batches = ids.collate(999)
                batches.eachWithIndex { it, index ->
                    assignedToUserClause.append(String.join(",", it.collect{String.valueOf(it)}))
                    if(index != batches.size() - 1) {
                        assignedToUserClause.append(") OR DM.ASSIGNED_TO_USER IN (")
                    }
                }
                whereClause.append(assignedToUserGroupClause.toString())
                whereClause.append(assignedToUserClause.toString())

                whereClause.append("))) ")
                break
        }
        if(assignedToFilter.contains(Constants.USER_TOKEN)) {
            whereClause.append("DM.ASSIGNED_TO_USER = " + assignedToFilter.split("_")[1] + " ")
        }else if(assignedToFilter.contains(Constants.USER_GROUP_TOKEN)) {
            String userGroupId = assignedToFilter.split("_")[1]
            String assignedToUserClause = String.join(",", UserGroup.get(Long.valueOf(userGroupId)).getUsers().collect{String.valueOf(it.id)})
            whereClause.append("(DM.ASSIGNED_TO_USERGROUP = " + userGroupId + " ")
            if(assignedToUserClause) whereClause.append("OR (DM.ASSIGNED_TO_USERGROUP is Null and DM.ASSIGNED_TO_USER IN ("+assignedToUserClause+")))") else whereClause.append(")")
        }
        whereClause.toString()
    }

    List<String> getDrilldownReportFields(ExecutedCaseLineListingTemplate executedTemplate) {
        List<String> drilldownReportFields = []
        executedTemplate.allSelectedFieldsInfo.each {
            addAsUniqueFieldName(drilldownReportFields, it)
        }
        return drilldownReportFields
    }

    List<String> getUrlFields(ExecutedCaseLineListingTemplate executedTemplate) {
        List<String> urlFields = []
        executedTemplate.allSelectedFieldsInfo.each {
            if (it.reportField.isUrlField) {
                urlFields.add(it.reportField.name)
            }
        }
        return urlFields
    }

    def fetchFromRptRsltAndInserttoDDCLL(ReportResult reportResult) {
        Promises.task { task ->
            try {
                ExecutedTemplateQuery.withNewSession { sess ->
                    ExecutedTemplateQuery executedTemplateQuery = reportResult.getExecutedTemplateQuery()
                    List<String> drilldownReportFields = getDrilldownReportFields(reportResult.template)
                    List<String> urlFields = getUrlFields(reportResult.template)

                    Long executedReportId = executedTemplateQuery.executedConfiguration.id
                    Long reportResultId = reportResult ? reportResult.id : executedTemplateQuery.getReportResult().id
                    StatelessSession session = sessionFactory.openStatelessSession()
                    Transaction tx = session.beginTransaction()
                    List dataList = fetchDataFromRptRsltData(ReportResult.get(reportResult.id), drilldownReportFields)
                    if (dataList) {
                        dataList.each { row ->
                            Map dataJson = [:]
                            row?.eachWithIndex { it, int i ->
                                if (urlFields.contains(drilldownReportFields.get(i)) && row[i]) {
                                    row[i] = row[i].replaceAll(ReportResultService.urlPattern) { m -> "<a href='${m[0]}' target='_blank'>${m[0]}</a>" }
                                }
                                dataJson[drilldownReportFields.get(i)] = row[i] ?: ""
                            }

                            DrilldownCLLData drilldownCLLData = new DrilldownCLLData()
                            drilldownCLLData.executedReportId = executedReportId
                            drilldownCLLData.reportResultId = reportResultId
                            drilldownCLLData.cllRowData = (dataJson as JSON)
                            session.insert(drilldownCLLData)
                        }
                    }
                    tx.commit()
                    session.close()
                    notificationService.notifyListeners(NotificationApp.PVC_REPORT, executedReportId)
                    DrilldownAccessTracker accessTracker = DrilldownAccessTracker.findByReportResultId(reportResultId)
                    accessTracker.setState(DrilldownAccessTracker.State.ACTIVE)
                    accessTracker.save(flush: true, failOnError: true)
                }
            } catch (Exception e) {
                log.error("Unexpected error occurred loading pvc report data", e)
            }
        }
    }

    List fetchDataFromRptRsltData(ReportResult reportResult, List<String> columnNamesList) {
        if (reportResult?.data?.value) {
            def reportResultInputStream = new GZIPInputStream(new ByteArrayInputStream(reportResult?.data?.getDecryptedValue()))
            def scanningResult = new Scanner(reportResultInputStream).useDelimiter("\\A")
            def theString = scanningResult.next()
            List rows = CsvUtil.parseCsv(new StringReader(theString))
            return rows
        }
        return []
    }

    void clearReasonOfDelayData() {
        int days = Holders.config.pvcModule.dataExpireDays ?: 30
        Date expireDate = new Date().minus(days)
        DrilldownAccessTracker.findAllByStateAndLastAccessLessThan(DrilldownAccessTracker.State.ACTIVE, expireDate)?.collect { it }?.each { DrilldownAccessTracker it ->
            try {
                it.setState(DrilldownAccessTracker.State.ARCHIVING)
                it.save(flush: true, failOnError: true)
                DrilldownCLLData.executeUpdate("delete from DrilldownCLLData d where d.reportResultId=${it.reportResultId}")
                it.setState(DrilldownAccessTracker.State.ARCHIVED)
                it.save(flush: true, failOnError: true)
            } catch (Exception e) {
                log.error("Unexpected error in clearReasonOfDelayData", e)
            }

        }

    }
}

