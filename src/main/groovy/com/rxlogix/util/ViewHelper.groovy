package com.rxlogix.util

import com.rxlogix.ApplicationSettingsService
import com.rxlogix.Constants
import com.rxlogix.DynamicReportService
import com.rxlogix.ReportSubmissionService
import com.rxlogix.SqlGenerationService
import com.rxlogix.UserService
import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.mapping.AuthorizationType
import com.rxlogix.mapping.ClDatasheetReassess
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.mapping.OrganizationCountry
import com.rxlogix.mapping.PreferredLanguage
import com.rxlogix.mapping.SafetyCalendar
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.FieldProfile
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.view.AuditLogViewHelper
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.context.i18n.LocaleContextHolder as LCH

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.Locale

@Slf4j
class ViewHelper {
    static DynamicReportService dynamicReportService
    static UserService userService
    static ApplicationSettingsService applicationSettingsService
    static def messageSource
    static SqlGenerationService sqlGenerationService
    static ReportSubmissionService reportSubmissionService

//    public static dateFullFormat = getMessage("dateFullFormat")
    static String getMessage(String code, Object[] params = null, String defaultLabel='', Locale locale = null) {
        if (!messageSource) messageSource = Holders.applicationContext.getBean("messageSource")
        return messageSource.getMessage(
                code,
                params,
                defaultLabel,
                locale?:LCH.getLocale())
    }

    static String getEmptyLabel(){
        return Holders.config.getProperty('report.empty.label')
    }

    //To remove 'Event receipt Date' for release_5.3
    static getDateRangeTypeI18n() {
        return DateRangeType.findAllByIsDeleted(false,[sort:'sortOrder', order:'asc']).findAll{!(it?.name == Constants.EVENT_RECEIPT_DATE)}.collect {
            [name: it.id, display: getMessage(it?.getI18nKey())]
        }
    }

    // For handling Multiple Event Receipt Date (PVR and PVS)
    static getDateRangeTypeKeyDescriptionI18n() {
        return DateRangeType.findAllByIsDeletedAndNameNotEqual(false, Constants.EVENT_RECEIPT_DATE, [sort:'sortOrder', order:'asc']).collect {
            [name: it.id, display: getMessage(it?.getI18nKey()), description: getMessage(it?.getI18nDescriptionKey())]
        }
    }

    static getDateRangeTypeI18nInList(List<Long> dateRangeIds){
        if (!dateRangeIds) {
            return [:]
        }
        return DateRangeType.findAllByIsDeletedAndIdInList(false, dateRangeIds, [sort:'sortOrder', order:'asc']).collect {
            [name: it.id, display: getMessage(it?.getI18nKey())]
        }
    }

    static getEvaluateCaseDateI18n(boolean showAllversions = true) {
        List<EvaluateCaseDateEnum> evaluateCaseDateEnumList = EvaluateCaseDateEnum.values() - EvaluateCaseDateEnum.VERSION_ASOF_GENERATION_DATE
        if(!showAllversions){
            evaluateCaseDateEnumList.remove(EvaluateCaseDateEnum.ALL_VERSIONS)
        }
        return evaluateCaseDateEnumList.collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static getEvaluateCaseDateSubmissionI18n() {
        return (EvaluateCaseDateEnum.values() - EvaluateCaseDateEnum.VERSION_ASOF).collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static getCaseDateI18nForLatestVersion() {
        return [EvaluateCaseDateEnum.LATEST_VERSION].collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static getEvaluateCaseDateForSub() {
        return [EvaluateCaseDateEnum.LATEST_VERSION, EvaluateCaseDateEnum.VERSION_ASOF_GENERATION_DATE].collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static getConfigurationTypeI18n() {
        return ConfigurationTypeEnum.values().collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static String getWorkflowConfigurationTypeI18nAsMap() {
        (getWorkflowConfigurationTypeI18n().collectEntries { [it.name, it.display] } as JSON).toString(false)
    }

    static getWorkflowConfigurationTypeI18n() {
        List<WorkflowConfigurationTypeEnum>  list = WorkflowConfigurationTypeEnum.getAllPVReports()
        if(SpringSecurityUtils.ifAnyGranted("ROLE_PVC_EDIT, ROLE_ADMIN, ROLE_DEV")) {
            list += WorkflowConfigurationTypeEnum.getAllPVCentral()
        }
        if(SpringSecurityUtils.ifAnyGranted("ROLE_PVQ_EDIT, ROLE_ADMIN, ROLE_DEV")) {
            list += WorkflowConfigurationTypeEnum.getAllPVQuality()
        }
        if(SpringSecurityUtils.ifAnyGranted("ROLE_PUBLISHER_TEMPLATE_EDITOR, ROLE_PUBLISHER_SECTION_EDITOR, ROLE_ADMIN, ROLE_DEV")) {
            list +=WorkflowConfigurationTypeEnum.getAllPVPublisher()
        }

        List result = list.collect {
            if (it.name().startsWith("QUALITY_SAMPLING")) {
                return null
            } else {
                [name: it.name(), display: getMessage(it?.getI18nKey())]
            }
        }
        if(SpringSecurityUtils.ifAnyGranted("ROLE_PVQ_EDIT, ROLE_ADMIN, ROLE_DEV")) {
            Holders.config.qualityModule.additional?.each {
                result.add([name: WorkflowConfigurationTypeEnum.getAdditional(it.workflow).name(), display: it.label])
            }
        }
        result?.findAll { it }
    }

    static getAuditLogCategoryI18n() {
        return AuditLogCategoryEnum.values().collect {
            [name: it.name(), display: getMessage(it.getI18nKey())]
        }
    }

    static getSetOperatorI18n() {
        return SetOperatorEnum.values().collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getDateRangeFoActionPlan() {
        List<DateRangeEnum> dateRangeEnumList = [DateRangeEnum.LAST_X_DAYS,DateRangeEnum.LAST_X_WEEKS,DateRangeEnum.LAST_X_MONTHS,DateRangeEnum.CUSTOM]
        return dateRangeEnumList.collect {
            [id: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static getDateRange(isForPeriodicReport = false, isFull = false) {
        List<DateRangeEnum> dateRangeEnumList = DateRangeEnum.getReportTemplateDateRangeOptions(isFull)
        if (isForPeriodicReport) {
            dateRangeEnumList = DateRangeEnum.periodicReportTemplateDateRangeOptions
        }
        return dateRangeEnumList.collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getEtlDateRange() {
        List<DateRangeEnum> dateRangeEnumList = DateRangeEnum.relativeDateOperatorsWithX
        return dateRangeEnumList.collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static getTimezoneValues() {
        return TimeZoneEnum.values().collect {
            [name: it?.timezoneId, display: getMessage(it?.getI18nKey(), it?.getGmtOffset())]
        }
    }

    static getQueryLevels() {
        return QueryLevelEnum.values()
    }

    static getStatuses() {
        return StatusEnum.values().collect {
            [name: it.key, display: getMessage(it.getI18nKey())]
        }
    }

    static getAttachmentFormat(List<ReportFormatEnum> reportFormats) {
        def formats = []
        reportFormats.each {
            formats.add(getMessage(it.getI18nKey()))
        }
        return formats
    }

    static String getTimeZone(User user) {
       return user?.preference?.timeZone?:TimeZone.getDefault()?.getID()
    }

    static getDataTabulationMeasures() {
        return MeasureTypeEnum.values().collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getDataTabulationCounts() {
        return CountTypeEnum.values().collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static getDatasheet() {
        ClDatasheetReassess.withTransaction {
            return ClDatasheetReassess.findAllByTenantId(Tenants.currentId() as Long)?.sort { it.sheetName }
        }
    }

    static getReassessListedness() {
        return ReassessListednessEnum.values().collect {
            [name: it, display: getMessage(it?.getI18nKey())]
        }
    }

    static actionItemCategoryEnumPvr() {
        return ActionItemCategory.findAll().collect {
            [name: it.key, display: getMessage(it.getI18nKey())] // use id and i18n later
        }
    }
    static actionItemCategoryEnumPvq() {
        return ActionItemCategory.findAllWhere(forPvq: true).collect {
            [name: it.key, display: getMessage(it.getI18nKey())] // use id and i18n later
        }
    }

    static topColumnTypeEnum() {
        return TopColumnTypeEnum.itemsToSelect.collect {
            [name: it.name(), display: getMessage(it.getI18nKey())] // use id and i18n later
        }
    }

    static actionItemCategoryForActionPlan() {
        return ActionItemCategory.findAllByKeyInList(["QUALITY_MODULE_PREVENTIVE", "QUALITY_MODULE_CORRECTIVE"]).collect {
            [name: it.key, display: getMessage(it.getI18nKey())] // use id and i18n later
        }
    }

    static getTransferTypeEnum() {
        return TransferTypeEnum.values().collect {
            [name: it, display: getMessage(it.getI18nKey())] // use id and i18n later
        }
    }

    static getGranularity() {
        return GranularityEnum.values().collect {
            [name: it, display: getMessage(it.getI18nKey())] // use id and i18n later
        }
    }

    static getDashboardEnum() {
        return DashboardEnum.getItemsToSelect().collect {
            [name: it, display: getMessage(it.getI18nKey())]
        }
    }

    static getPvcDashboardEnum() {
        return DashboardEnum.getPvcItemsToSelect().collect {
            [name: it, display: getMessage(it.getI18nKey())]
        }
    }


    static getPriorityEnum() {
        return PriorityEnum.values().collect {
            [name: it, display: getMessage(it.getI18nKey())] // use id and i18n later
        }
    }

    static getPageSizeEnum() {
        return PageSizeEnum.values().collect {
            [name: it, display: getMessage(it.getI18nKey())] // use id and i18n later
        }
    }

    static getSensitivityLabelEnum() {
        return SensitivityLabelEnum.values().collect {
            [name: it.getKey(), display: getMessage(it.getI18nKey())] // use id and i18n later
        }
    }

    static getReportActionEnum() {
        if (!applicationSettingsService) applicationSettingsService = Holders.applicationContext.getBean("applicationSettingsService")
        return (applicationSettingsService.hasDmsIntegration() ? ReportActionEnum.values().collect {
            [name: it.getKey(), display: getMessage(it.getI18nKey())]
        } : (ReportActionEnum.values() - ReportActionEnum.SEND_TO_DMS).collect {
            [name: it.getKey(), display: getMessage(it.getI18nKey())]
        })
    }

    static getDataTabulationPercentageOptions() {
        return PercentageOptionEnum.values()
    }

    static getAllProductFamilies() {
        return LmProductFamily.findAll().unique().sort({it.name})
    }

    static getDictionaryValues(ReportRequest obj, DictionaryTypeEnum dictionaryType) {
        if(!obj){
            return ''
        }
        List data = []
        if (dictionaryType == DictionaryTypeEnum.PRODUCT && obj.validProductGroupSelection) {
            data.add(getDictionaryGroupValues(obj.validProductGroupSelection) + " (Product Group)")
        }
        if (dictionaryType == DictionaryTypeEnum.EVENT && obj.validEventGroupSelection) {
            data.add(getDictionaryGroupValues(obj.validEventGroupSelection) + " (Event Group)")
        }
        if (obj.(dictionaryType.value())) {
            data.add(getDictionaryValues(obj.(dictionaryType.value()) as String, dictionaryType))
        }
        return data.join(', ')
    }

    static getDictionaryValues(BaseConfiguration obj, DictionaryTypeEnum dictionaryType) {
        if(!obj){
            return ''
        }
        List data = []
        if (dictionaryType == DictionaryTypeEnum.PRODUCT && obj.validProductGroupSelection) {
            data.add(getDictionaryGroupValues(obj.validProductGroupSelection) + " (Product Group)")
        }
        if (dictionaryType == DictionaryTypeEnum.EVENT && obj.usedValidEventGroupSelection) {
            data.add(getDictionaryGroupValues(obj.usedValidEventGroupSelection) + " (Event Group)")
        }
        if (obj.(dictionaryType.value())) {
            data.add(getDictionaryValues(obj.(dictionaryType.value()) as String, dictionaryType))
        }
        return data.join(', ')
    }

    static getDictionaryValues(BaseCaseSeries obj, DictionaryTypeEnum dictionaryType) {
        if (!obj) {
            return ''
        }
        List data = []
        if (dictionaryType == DictionaryTypeEnum.PRODUCT && obj.validProductGroupSelection) {
            data.add(getDictionaryGroupValues(obj.validProductGroupSelection) + " (Product Group)")
        }
        if (dictionaryType == DictionaryTypeEnum.EVENT && obj.validEventGroupSelection) {
            data.add(getDictionaryGroupValues(obj.validEventGroupSelection) + " (Event Group)")
        }
        if (obj.(dictionaryType.value())) {
            data.add(getDictionaryValues(obj.(dictionaryType.value()) as String, dictionaryType))
        }
        return data.join(', ')
    }

    static getDictionaryValues(BaseInboundCompliance obj, DictionaryTypeEnum dictionaryType) {
        if (!obj) {
            return ''
        }
        List data = []
        if (dictionaryType == DictionaryTypeEnum.PRODUCT && obj.validProductGroupSelection) {
            data.add(getDictionaryGroupValues(obj.validProductGroupSelection) + " (Product Group)")
        }
        if (obj.(dictionaryType.value())) {
            data.add(getDictionaryValues(obj.(dictionaryType.value()) as String, dictionaryType))
        }
        return data.join(', ')
    }

    static String getDueDateCssClass(ExecutedPeriodicReportConfiguration report) {
        Date now = new Date();
        boolean complete = report.reportSubmissions.find {
            it.reportSubmissionStatus == ReportSubmissionStatusEnum.SUBMITTED || it.reportSubmissionStatus == ReportSubmissionStatusEnum.SUBMISSION_NOT_REQUIRED
        }
        if (complete) return ""
        Date soon = now + 30;
        if (report.dueDate > now && report.dueDate < soon) return "label-primary";
        if (report.dueDate < now) return "label-danger text-white";
        return ""
    }

    static List<String> getProductsAsList(String jsonString, String filter = null) {
        if (!jsonString) return []
        String lowFilter = filter?.toLowerCase()
        def productSelection = PVDictionaryConfig.ProductConfig.levels.withIndex().collectEntries { element, index ->
            [(index + 1), element]
        }
        List<String> data = []
        MiscUtil.parseJsonText(jsonString).each { level ->
            level.value?.each {
                if (!filter || it.name?.toLowerCase()?.contains(lowFilter))
                    data << [id: (level.key + "_" + it.name), text: (it.name + " (" + productSelection.get(level.key as Integer) + ")")]
            }
        }
        return data
    }

    static getDictionaryValues(String jsonString, DictionaryTypeEnum dictionaryType) {
        if (!jsonString) {
            return ""
        }

        Map selectionMap
        switch (dictionaryType) {
            case DictionaryTypeEnum.EVENT:
                selectionMap = PVDictionaryConfig.EventConfig.levels.withIndex().collectEntries { element, index ->
                    [(index + 1), element]
                }
                break
            case DictionaryTypeEnum.PRODUCT:
                selectionMap = PVDictionaryConfig.ProductConfig.views.withIndex().collectEntries { element, index ->
                    [(index + 1), getMessage(element.code)]
                }
                break
            case DictionaryTypeEnum.STUDY:
                selectionMap = PVDictionaryConfig.StudyConfig.levels.withIndex().collectEntries { element, index ->
                    [(index + 1), element]
                }
                break
            default:
                selectionMap = [:]
        }

        List data = []

        def dictionaryData = MiscUtil.parseJsonText(jsonString)
        if (dictionaryType == DictionaryTypeEnum.PRODUCT && dictionaryData["100"]) {
            selectionMap[100] = "Drug Record Number"
        }
        dictionaryData.collect {
            if (it.value && it.value instanceof Collection) {
                Integer key = it.key as Integer
                String parameterSelectionLabel = selectionMap[key]
                String delimiter = dictionaryType == DictionaryTypeEnum.EVENT ? " (" + parameterSelectionLabel + ")" + ", " : ", "
                boolean productWhoDrugsFlag = dictionaryType == DictionaryTypeEnum.PRODUCT && key == 100
                if (productWhoDrugsFlag) {
                    data << ((it.value.id.join(delimiter)) + " (" + parameterSelectionLabel + ")")
                } else {
                    data << ((it.value.name.join(delimiter)) + " (" + parameterSelectionLabel + ")")
                }
            }
        }

        return data.join(", ")
    }

    static getDictionaryGroupValues(String jsonString) {
        if(!jsonString) {
            return ""
        }

        def data = []
        def object = MiscUtil.parseJsonText(jsonString)

        object.each {
            if (it.name) {
                data << it.name.replace(it.id?.toString(), '').replace('()', '').trim()
            }
        }

        return data.join(", ")

    }

    static getDictionaryGroupValuesForAuditLog(String jsonString, DictionaryTypeEnum dictionaryType) {
        if(!jsonString) {
            return ""
        }

        List<String> data = []
        def object = MiscUtil.parseJsonText(jsonString)

        object.each {
            String groupData = ""
            if (it.name) {
                groupData = it.name.trim()
            }
            if (dictionaryType == DictionaryTypeEnum.PRODUCT) {
                groupData += "\n--Is Multi Ingredient: " + (it.isMultiIngredient ? "Yes" : "No")
                groupData += "\n--Include WHO Drugs: " + (it.includeWHODrugs ? "Yes" : "No")
            }
            data << groupData
        }

        return data.join("\n\n")

    }

    static getMetaDataValues(String jsonString) {
        if(!jsonString && jsonString != "") {
            return ""
        }
        def data = [:]
        def object = MiscUtil.parseJsonText(jsonString)
        object.each {
            String replaceText = getMessage("app.reportField." + it.key)
            if (replaceText) {
                data.put(replaceText, it.value)
            } else {
                data.put(it.key,it.value)
            }
        }
        return data
    }

    static getUserTimeZoneForConfig(BaseConfiguration configurationInstance, User user) {
        if(configurationInstance?.nextRunDate && configurationInstance.isEnabled) {
            return configurationInstance?.configSelectedTimeZone
        } else {
            return user?.preference?.timeZone
        }

    }

    static getUserTimeZoneForConfig(BaseCaseSeries seriesInstance, User user) {
        if(seriesInstance?.nextRunDate && seriesInstance.isEnabled) {
            return seriesInstance?.configSelectedTimeZone
        } else {
            return user?.preference?.timeZone
        }

    }

    static getCommaSeperatedFromList(def tagsList) {
        if (tagsList) {
            tagsList.inject( '' ) { s, v ->
                s + ( s ? ', ' : '' ) + v.name
            }
        }
    }

    static boolean checkForSpecialCharacters(String stringValue) {
        stringValue.find(/[@#$%^&+=]/);
    }

    static String getHexValues(char ch) {
       return String.format("%04x", (int) ch)
    }

    static String getI18nMessageForString(String i18nKey) {
        return getMessage(i18nKey)
    }

    static String getReportHeader(ExecutedTemplateQuery executedTemplateQuery, String customReportHeader) {
        String reportHeader
        if (customReportHeader) {
            reportHeader = customReportHeader
        } else {
            reportHeader = executedTemplateQuery.header
        }
        reportHeader
    }

    static String getReportTitle(ExecutedReportConfiguration executedConfiguration, ExecutedTemplateQuery executedTemplateQuery) {
        if (!dynamicReportService) dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
        String title = getReportHeader(executedTemplateQuery, null)
        if(executedConfiguration.signalConfiguration){
            title = dynamicReportService.getReportNameAsTitle(executedConfiguration, executedTemplateQuery,false)
        }
        if (!title) {
            title = dynamicReportService.getReportNameAsTitle(executedConfiguration, executedTemplateQuery)
        }
        return title
    }

    static boolean isNotExportable(ExecutedReportConfiguration executedConfiguration, ReportTemplate executedTemplate, ReportFormatEnum format, ReportTypeEnum reportType) {
        switch (reportType) {
            case ReportTypeEnum.MULTI_REPORT:
                for (ExecutedTemplateQuery query:executedConfiguration.executedTemplateQueries) {
                    if (query.executedTemplate.isNotExportable(format)) {
                        return true;
                    }
                }
                return false;
            default:
                return executedTemplate.isNotExportable(format)
        }
    }

    static String getLicenses(BaseConfiguration obj) {
        if (!obj.productSelection) {
            return ""
        }

        def json = MiscUtil.parseJsonText(obj.productSelection)
        return (json.get("4")?.collect { it.name }?.join(",")) ?: ""
    }

    static getReportSubmissionStatusEnumI18n(){
        return ReportSubmissionStatusEnum.values().collect {
            [name: it.key, display: getMessage(it.getI18nKey())]
        }
    }

    static getReportSubmissionStatusEnumWoPendingI18n(){
        return ReportSubmissionStatusEnum.values().findAll{it!=ReportSubmissionStatusEnum.PENDING}.collect {
            [name: it.key, display: getMessage(it.getI18nKey())]
        }
    }
    static getCaseSubmissionStatusEnumFinalI18n() {
        List<IcsrCaseStateEnum> data = [IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL]
        return data.collect {
            [name: it.key, display: getMessage(it.getI18nKey())]
        }
    }

    static List<Map> getGeneratedStateOptions() {
        return [IcsrCaseStateEnum.SUBMITTED, IcsrCaseStateEnum.GENERATED].collect { state ->
            [name: state.key, display: getMessage(state.getI18nKey())]
        }
    }

    static List<Map> getPreviousStateOptions(String icsrCaseId, String profileName) {
        if (!reportSubmissionService) reportSubmissionService = Holders.applicationContext.getBean("reportSubmissionService")
        String previousState = reportSubmissionService.getPreviousState(icsrCaseId, profileName)
        List<IcsrCaseStateEnum> data = []
        data.add(IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED)
        if(previousState){
            data.add(IcsrCaseStateEnum."${previousState}")
        }
        return data.collect {
            [name: it.key, display: getMessage(it.getI18nKey())]
        }
    }

    static getCaseSubmissionStatusEnumI18n(Boolean noSubmisson = false) {
        List<IcsrCaseStateEnum> data = []
        if (!noSubmisson) {
            data.add(IcsrCaseStateEnum.SUBMITTED)
        }
        data.add(IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED)
        return data.collect {
            [name: it.key, display: getMessage(it.getI18nKey())]
        }
    }

    static getSubmissionDropDown(String action, Locale requestLocale = null) {
        List<IcsrCaseStateEnum> data = []
        if (action == "submissionNotRequired") {
            data.add(IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL)
        } else if (action == "submit") {
            data.add(IcsrCaseStateEnum.SUBMITTED)
            data.add(IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED)
        }
        return data.collect {
            [id: it.key, text: getMessage(it.getI18nKey(), null, null, requestLocale)]
        }
    }

    static getIcsrStateDropDown(String action, Locale requestLocale = null, String icsrCaseId = null, String profileName = null) {
        List<IcsrCaseStateEnum> data = []
        if (action == "SUBMITTED") {
            data.add(IcsrCaseStateEnum.SUBMITTED)
            data.add(IcsrCaseStateEnum.GENERATED)
        } else if (action == "SUBMISSION_NOT_REQUIRED" || action == "SUBMISSION_NOT_REQUIRED_FINAL") {
            if (!reportSubmissionService) reportSubmissionService = Holders.applicationContext.getBean("reportSubmissionService")
            String previousState = reportSubmissionService.getPreviousState(icsrCaseId, profileName)
            data.add(IcsrCaseStateEnum."${action}")
            if(previousState){
                data.add(IcsrCaseStateEnum."${previousState}")
            }
        } else {
            data.add(IcsrCaseStateEnum."${action}")
        }
        return data.collect {
            [id: it.key, text: getMessage(it.getI18nKey(), null, null, requestLocale)]
        }
    }

    /**
     * Formats the run date and time of an executed configuration instance.
     *
     * @param executedConfigurationInstance The executed configuration instance to format the run date and time for.
     * @param isFinal Indicates if the report is a final report.
     * @return The formatted run date and time as a string.
     */
    static String formatRunDateAndTime(def executedConfigurationInstance, boolean isFinal = false) { // updated method to handle draft and final report's run date
        executedConfigurationInstance = GrailsHibernateUtil.unwrapIfProxy(executedConfigurationInstance)
        if (!userService) userService = Holders.applicationContext.getBean("userService")
        User user = userService.user ?: executedConfigurationInstance.owner
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getLongDateFormatForLocale(user?.preference?.locale, true))
        sdf.setTimeZone(TimeZone.getTimeZone(getTimeZone(user)))

        if (executedConfigurationInstance.getClass() == ExecutedCaseSeries) {
            return sdf.format(executedConfigurationInstance.dateCreated)
        } else {
            if (isFinal && executedConfigurationInstance.finalLastRunDate) {
                return sdf.format(executedConfigurationInstance.finalLastRunDate)
            } else {
                return sdf.format(executedConfigurationInstance.lastRunDate)
            }
        }
    }

    /**
     * Checks if the given executed configuration instance represents a final report.
     * @param executedConfigurationInstance The executed configuration instance to check.
     * @param isInDraftMode Indicates if the report is in draft mode.
     * @return True if the executed configuration instance represents a final report, false otherwise.
     */
    static boolean isFinalReport(def executedConfigurationInstance, boolean isInDraftMode){
        return executedConfigurationInstance.hasGeneratedCasesData && !isInDraftMode &&
                executedConfigurationInstance.status in [ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT, ReportExecutionStatusEnum.SUBMITTED]
    }

    static getXMLNodeDateFormatI18n() {
        return XMLNodeDateFormat.values().collect {
            [name: it.name(), display: it.name()]
        }
    }

    static List getReportExecutionStatusEnumI18n(){
        return ReportExecutionStatusEnum.getExeuctionStatusList().collect{
            [name: it.key, display: getMessage(it.getI18nValueForExecutionStatusDropDown())]
        }
    }

    static List getReportExecutionStatusEnumForICI18n(){
        return ReportExecutionStatusEnum.getExecutionStatusForICList().collect{
            [name: it.key, display: getMessage(it.getI18nValueForExecutionStatusDropDown())]
        }
    }

    static List getIcsrCaseStateEnumI18n(Locale locale = null) {
        def values  = IcsrCaseStateEnum.values().collect {
            if(!(it.toString() in ['SUBMISSION_NOT_REQUIRED_FINAL', 'ERROR' ,'TRANS_SUCCESS'])){
                [id: it.toString(), name: getMessage(it.getI18nKey(),null, '',locale)]
            }
        }
        values = values - null
        return values
    }

    static String getCacheLastModified(User user, Locale locale) {
        Long max = 0
        max = Math.max(Holders.applicationContext.getBean('reportFieldService').getCachedFileLastModified(locale), user?.lastUpdated?.time)
        UserGroup.withNewSession {
            Set<FieldProfile> fieldProfiles = UserGroup.fetchAllFieldProfileByUser(user)
            max = Math.max(max, fieldProfiles?.max { it.lastUpdated }?.lastUpdated?.time ?: 0);
            def lastUpdate = CustomReportField.executeQuery("select max(lastUpdated) from CustomReportField")

            max = Math.max(max, lastUpdate?.get(0)?.time ?: 0)
        }
        return max + "" + locale?.toString()
    }

    static boolean isPvqModule(request) {
        for(String controller : Holders.config.qualityModule.qualityControllers){
            if(request?.forwardURI?.indexOf(controller)>-1) return true
        }
        return false
    }

    static boolean isPvPModule(request) {
        if (request.getParameter("pvp") == "true") return true
        for (String controller : Holders.config.getProperty('publisherControllers', List)) {
            if (request?.forwardURI?.indexOf(controller) > -1) return true
        }
        return false
    }

    static getUnitTypeEnumI18n() {
        return UnitTypeEnum.values().collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static List getQueryDropdownEnumI18n() {
        //To get all the values to populate query type dropdown
        return QueryDropdownEnum.values().collect {
            [name: it?.name(), display: getMessage(it?.getI18nKey())]
        }
    }


    static getSelectedQueryType(query) {
        //To show selected query Type value
        if (query?.nonValidCases) {
            return QueryDropdownEnum.NON_VALID_CASES.name()
        } else if (query?.deletedCases) {
            return QueryDropdownEnum.DELETED_CASES.name()
        } else if (query?.icsrPadderAgencyCases) {
            return QueryDropdownEnum.ICSR_PADER_AGENCY_CASES.name()
        } else {
            return null
        }
    }

    static getE2BReportFormatEnumI18n() {
        return E2BReportFormatEnum.values().collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static getDistributionChannelEnumI18n() {
        List<DistributionChannelEnum> list = []
        DistributionChannelEnum.values().each{
            if ((it.name() == "PV_GATEWAY" && (Holders.config.getProperty('pvgateway.integrated', Boolean))) || (it.name() == "EXTERNAL_FOLDER" && (Holders.config.getProperty('icr.gateway.axway.active', Boolean)))){
                list.add(it)
            } else if (it.name() != "PV_GATEWAY" && it.name() != "EXTERNAL_FOLDER"){
                list.add(it)
            }
        }
        return list.collect{
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }


    static getAuthorizationTypeEnumI18n() {
        return AuthorizationTypeEnum.values().collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static getAuthorizationTypeNames(String authIdsString) {
        User currentUser = Holders.applicationContext.getBean("userService").currentUser
        Integer langId = Holders.applicationContext.getBean("sqlGenerationService").getPVALanguageId(currentUser?.preference?.locale?.toString() ?: 'en')
        authIdsString = authIdsString?.replaceAll(/\[|\]/, "")
        List authIds = authIdsString.split(", ")
        List result = []
        AuthorizationType.'pva'.withNewSession {
            result.add(AuthorizationType.findAllByIdInListAndLangId(authIds, langId).name)
        }
        return result.join(", ")
    }

    static getTitleEnumI18n(String userLocale) {
            return TitleEnum.values().collect {
                [name: it.name(), display: getMessage(it.getI18nKey())]
            }
    }

    static getResponsiblePartyEnumI18n() {
        return ResponsiblePartyEnum.values().collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static getReasonEnumI18n() {
        return ReasonEnum.values().collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static getLateEnumI18n() {
        return LateEnum.values().collect {
            [name: it.name(), display: getMessage(it?.getI18nKey())]
        }
    }

    static boolean isPvcModule(request) {
        for(String controller : Holders.config.getProperty('pvcModule.pvcControllers', List)){
            if(request?.forwardURI?.indexOf(controller)>-1) {
                return true
            }
        }
        return false
    }

    static DashboardEnum checkPVModule(request) {
        if (isPvqModule(request)) {
            return DashboardEnum.PVQ_USER
        } else if (isPvcModule(request)) {
            return DashboardEnum.PVC_USER
        } else {
            return DashboardEnum.PVR_USER
        }
    }

    static getDateRangeTypeI18nMessage(Long id){
        return getMessage(DateRangeType.findById(id).getI18nKey())
    }

    static getAdvancedAssignmentEnumI18n(){
        return AdvancedAssignmentCategoryEnum.getItemsToSelect().collect{
            [name: it.name(), display: getMessage(it.getI18nKey())]
        }
    }

    static getRODAppTypeEnum() {
        def includePVQ = Holders.config.pv.app.pvquality.enabled
        def includePVC = Holders.config.pv.app.pvcentral.enabled
        def allowedValues = ReasonOfDelayAppEnum.values().findAll {
            (includePVC && (it == ReasonOfDelayAppEnum.PVC || it == ReasonOfDelayAppEnum.PVC_Inbound)) ||
            (includePVQ && it == ReasonOfDelayAppEnum.PVQ)
        }
        return allowedValues.collect {
            [name: it, display: getMessage(it.getI18nKey())]
        }
    }

    static getRODLateTypeEnum(String ownerApp) {
        List<ReasonOfDelayLateTypeEnum> list = []
        ReasonOfDelayLateTypeEnum.values().each{
            if(it.ownerApp == ownerApp){
                list.add(it)
            }
        }
        return list.collect{
            [name: it, display: getMessage(it.getI18nKey())]
        }
    }

    static getUserTimeZoneForConfig(AutoReasonOfDelay autoReasonOfDelayInstance, User user) {
        if(autoReasonOfDelayInstance?.nextRunDate && autoReasonOfDelayInstance.isEnabled) {
            return autoReasonOfDelayInstance?.configSelectedTimeZone
        } else {
            return user?.preference?.timeZone
        }

    }


    static getBalanceQueryPeriodEnum() {
        return BalanceQueryPeriodEnum.values().collect {
            [name: it.name(), display: it.value]
        }
    }


    static getExecutionStatusConfigTypeEnumI18n(){
        def list = ExecutionStatusConfigTypeEnum.values().collect {
                [name: it.getKey(), display: getMessage(it.getI18nKey())?:it.getKey()]
        }
        list
    }

    static getTimeZoneValues() {
        return TimeZoneEnum.values().collect {
            [name: it.timezoneId, display: getMessage(it.getI18nKey(), it.getGmtOffset())]
        }
    }

    static getReportAction(String reportAction) {
        return ReportActionEnum.(ReportActionEnum.valueOf(reportAction)).getDisplayName()
    }

    static getReportActionsList(String reportActions) {
        reportActions = reportActions.substring(1,reportActions.length() - 1)
        List<String> list = reportActions.split(", ")
        def reportActionList = []
        list.each {
                reportActionList.add(ReportActionEnum.(ReportActionEnum.valueOf(it)).getDisplayName())
        }
        return reportActionList.join(", ")
    }

    static String getReadableConditionalFormatting(String mapString) {
        if (mapString == null) return null
        try {
            def colorSettings = new JsonSlurper().parseText(mapString)

            return colorSettings.collect { coditionSet ->
                return "\n( " + coditionSet.conditions?.collect {
                    return "(" + it.fieldLabel + " " + it.operatorLabel + " " + it.value + ")"
                }?.join(" AND ") + ") " +
                        (coditionSet.color ? (" Then Color" + coditionSet.color) : "") +
                        (coditionSet.icon ? " formatting:" + coditionSet.icon : "")

            }.join("\n Else ") + "\n\n"
        } catch (Exception e) {
            e.printStackTrace()
        }
        return mapString
    }

    static getReadableMap(String mapString) {
        try {
            Matcher mapMatcher = (mapString =~ /\[([^\]]+)\]/)
            List<String> mapValues = []
            List<String> nameList = []

            mapMatcher.each { match ->
                String result = match[1]
                String previousKey
                String previousValue
                List<String> splitResult = result.split(", ")
                Map resultMap = [:]
                splitResult.each {
                    if(!it.contains(":")){
                        resultMap[previousKey] += "," + it
                        return
                    }
                    if(it.count(":") > 1){
                        List fieldData = it.split(":")
                        String key = fieldData[0]
                        String value = fieldData.drop(1).join(":")
                        key.trim()
                        value.trim()
                        resultMap[key] = value
                        return
                    }
                    def (key, value) = it.split(":")
                    key = key.trim()
                    value = value.trim()
                    key = AuditLogViewHelper.splitCamelCase(key)
                    value = (value == "null") ? null : ((value == "true") ? "Yes" : ((value == "false") ? "No" : value))
                    previousKey = key
                    previousValue = value
                    resultMap[key] = value
                    if (key == "Report Field") {
                        nameList.add(resultMap[key])
                    }
                }
                resultMap = resultMap.findAll { key, value ->
                    value != null
                }
                resultMap.put("Source Field", resultMap.remove("Argus Name"))
                resultMap.remove("Report Field")
                mapValues.add(resultMap.toMapString())
            }
            String newMapString = ""
            for (int i = 0; i < nameList.size(); i++) {
                newMapString += nameList[i] + " = " + mapValues[i]
            }
            newMapString = newMapString.replaceAll("= \\[", "= [\n- ")
            newMapString = newMapString.replaceAll(", ", ",\n- ")
            newMapString = newMapString.replaceAll(":", ": ")
            newMapString = newMapString.replaceAll("]", "\n]\n")
            return newMapString
        } catch (Exception e) {
            log.error("Error occurred while getting readable fields",e)
            return mapString
        }
    }
    static getReassessListedness(String reassessListedness) {
        return getMessage("app.reassessListednessEnum.$reassessListedness")
    }

    static getTaskTemplateType(String type) {
        return getMessage("app.TaskTemplateTypeEnum.$type")
    }

    static getCalendarNames(String calendarString) {
        SafetyCalendar.'pva'.withNewSession {
            calendarString = calendarString.substring(1, calendarString.length() - 1)
            List calendarIds = calendarString.split(", ")
            List result = []
            calendarIds.each { id ->
                result.add(SafetyCalendar.findById(Long.parseLong(id)).name)
            }
            return result.join(", ")
        }
    }

    static getReadableMeasures(String measures) {
        try {
            List<String> mapValues = []
            List<String> nameList = []
            String previousKey
            String previousValue

            List<String> splitResult = measures.split(", ")
            Map resultMap = [:]
            splitResult.each {
                if(!it.contains(" : ")){
                    resultMap[previousKey] = previousValue + "," + it
                    return
                }
                if(it.count(" : ") > 1){
                    List fieldData = it.split(" : ")
                    String key = fieldData[0]
                    String value = fieldData.drop(1).join(":")
                    key.trim()
                    value.trim()
                    resultMap[key] = value
                    return
                }
                def (key, value) = it.split(" : ")
                key = key.trim()
                value = value.trim()
                key = AuditLogViewHelper.splitCamelCase(key)
                value = (value == "null") ? null : ((value == "true") ? "Yes" : ((value == "false") ? "No" : value))
                previousKey = key
                previousValue = value
                resultMap[key] = value
                if (key == "Name") {
                    nameList.add(resultMap[key])
                }
            }
            resultMap = resultMap.findAll { key, value ->
                value != null
            }
            resultMap.remove("Name")
            mapValues.add(resultMap.toMapString())
            String newMapString = ""
            for (int i = 0; i < nameList.size(); i++) {
                newMapString += nameList[i] + " = " + mapValues[i]
            }
            newMapString = newMapString.replaceAll("= \\[", "= [\n- ")
            newMapString = newMapString.replaceAll(", ", ",\n- ")
            newMapString = newMapString.replaceAll(":", ": ")
            newMapString = newMapString.replaceAll("]", "\n]\n")
            return newMapString
        } catch (Exception e) {
            log.error("Error occurred while getting readable measures",e)
            return measures
        }
    }

    static getCustomChartOptions(String chartOptions) {
        def chartJson = MiscUtil.parseJsonText(chartOptions)
        try {
            return formatJson(chartJson, 2)
        } catch (Exception e) {
            log.error("Error while generating readable chart options",e)
            return chartOptions
        }
    }

    static formatJson(obj, currentIndent) {
        String result = ''
        String currentIndentStr = '-' * currentIndent

        if (obj instanceof Map) {
            result += '{\n'
            obj.each { key, value ->
                key = AuditLogViewHelper.splitCamelCase(key)
                result += "${currentIndentStr}  $key: ${formatJson(value, currentIndent + 2)}"
            }
            result += "${currentIndentStr.substring(0,currentIndentStr.size() - 2)} }"
        } else if (obj instanceof List) {
            result += '[\n'
            obj.each { item ->
                result += "${currentIndentStr}  ${formatJson(item, currentIndent + 2)}"
            }
            result += "${currentIndentStr.substring(0,currentIndentStr.size() - 2)} ]"
        } else {
            result += "$obj"
        }

        return result + (currentIndent > 2 ? ',\n' : '\n')
    }

    static getCorrectTimeZone(String timezone){
        String offset = TimeZoneEnum.find{
            it.timezoneId == timezone
        }?.getGmtOffset()
        return "(GMT ${offset}) ${timezone}"
    }

    static String getCorrectPreferredLanguage(String langCode){
        User currenUser = Holders.applicationContext.getBean("userService").currentUser
        Integer langId = Holders.applicationContext.getBean("sqlGenerationService").getPVALanguageId(currenUser?.preference?.locale?.toString() ?: 'en')
        PreferredLanguage preferredLanguage = new PreferredLanguage()
        PreferredLanguage.'pva'.withNewSession {
            preferredLanguage = PreferredLanguage.'pva'.findByLangCodeAndLangId(langCode, langId)
        }
        return preferredLanguage?.name
    }

    static getWidgetNames(String widgetList) {
        List<String> widgets = []
        if (widgetList == "[]")
            return widgetList
        widgetList = widgetList.substring(1, widgetList.size() - 1)
        widgetList.split(", ").each {
            if(it.contains("CHART (") || it.contains("SPOTFIRE ("))
                widgets.add(it)
            else {
                WidgetTypeEnum widgetType = WidgetTypeEnum.valueOf(it)
                switch (widgetType) {
                    case WidgetTypeEnum.LAST_REPORTS:
                        widgets.add(getMessage("default.button.addLastReportsWidget.label"))
                        break
                    case WidgetTypeEnum.ACTION_ITEMS:
                    case WidgetTypeEnum.QUALITY_ACTION_ITEMS:
                        widgets.add(getMessage("default.button.addactionItemsWidget.label"))
                        break
                    case WidgetTypeEnum.CALENDAR:
                        widgets.add(getMessage("default.button.addCalendarWidget.label"))
                        break
                    case WidgetTypeEnum.REPORT_REQUEST_SUMMARY:
                        widgets.add(getMessage("app.widget.button.reportRequest.label"))
                        break
                    case WidgetTypeEnum.ACTION_ITEMS_SUMMARY:
                    case WidgetTypeEnum.QUALITY_ACTION_ITEMS_SUMMARY:
                        widgets.add(getMessage("app.widget.button.actionItem.label"))
                        break
                    case WidgetTypeEnum.ADHOC_REPORTS_SUMMARY:
                        widgets.add(getMessage("app.widget.button.adhoc.label"))
                        break
                    case WidgetTypeEnum.AGGREGATE_REPORTS_SUMMARY:
                        widgets.add(getMessage("app.widget.button.aggregate.label"))
                        break
                    case WidgetTypeEnum.ETL:
                        widgets.add(getMessage("app.widget.button.etl.label"))
                        break
                    case WidgetTypeEnum.QUALITY_CASE_COUNT:
                        widgets.add(getMessage("app.widget.button.quality.caseCount.label"))
                        break
                    case WidgetTypeEnum.QUALITY_CASE_REPORT_TYPE:
                        widgets.add(getMessage("app.widget.button.quality.caseReportTypeCount.label"))
                        break
                    case WidgetTypeEnum.QUALITY_ENTRYSITE_COUNT:
                        widgets.add(getMessage("app.widget.button.quality.entrySiteCount.label"))
                        break
                    case WidgetTypeEnum.QUALITY_ERROR_COUNT:
                        widgets.add(getMessage("app.widget.button.quality.top20ErrorsCount.label"))
                        break
                    case WidgetTypeEnum.QUALITY_LATEST_ISSUES:
                        widgets.add(getMessage("app.widget.button.quality.latestIssues.label"))
                        break
                    case WidgetTypeEnum.QUALITY_PRODUCT_COUNT:
                        widgets.add(getMessage("app.widget.button.quality.productCount.label"))
                        break
                    case WidgetTypeEnum.ADVANCED_REPORT_REQUEST:
                        widgets.add(getMessage("app.widget.button.advancedReportRequest.label"))
                        break
                    case WidgetTypeEnum.CASE_COUNT_BY_ERROR:
                        widgets.add(getMessage("default.button.addCaseCountByErrorWidget.label"))
                        break
                    case WidgetTypeEnum.ACTION_PLAN_PVC:
                    case WidgetTypeEnum.ACTION_PLAN_PVQ:
                        widgets.add(getMessage("app.actionPlan.actionPlan"))
                        break
                    case WidgetTypeEnum.SUBMISSION_COUNT_BY_ERROR:
                        widgets.add(getMessage("default.button.addSubmissionCountByErrorWidget.label"))
                        break
                    case WidgetTypeEnum.ADVANCED_PUBLISHER:
                        widgets.add(getMessage("app.widget.button.advancedPublisher.label"))
                        break
                    case WidgetTypeEnum.COMPLIANCE_PUBLISHER:
                        widgets.add(getMessage("app.widget.button.compliancePublisher.label"))
                        break
                    case WidgetTypeEnum.ICSR_TRACKING:
                        widgets.add(getMessage("app.label.icsr.case.tracking"))
                        break
                }
            }
        }
        return widgets.join(", ")
    }

    static getCorrectFieldGroupName(String fieldGroupName) {
        return getMessage("app.reportFieldGroup.${fieldGroupName}")
    }

    static List preferredLanguageList() {
        User currenUser = Holders.applicationContext.getBean("userService").currentUser
        Integer langId = Holders.applicationContext.getBean("sqlGenerationService").getPVALanguageId(currenUser?.preference?.locale?.toString() ?: 'en')
        List<PreferredLanguage> preferredLanguageList = []
        PreferredLanguage.'pva'.withNewSession {
            preferredLanguageList = PreferredLanguage.'pva'.findAllByLangIdAndLangCodeIsNotNull(langId)
        }
        return preferredLanguageList.collect {
            [name: it.langCode, display: it.name]
        }
    }

    static getOrganizationTypeByPreference(def name){
        IcsrOrganizationType orgType = IcsrOrganizationType.findByName(name)
        User currenUser = Holders.applicationContext.getBean("userService").currentUser
        Integer langId = Holders.applicationContext.getBean("sqlGenerationService").getPVALanguageId(currenUser?.preference?.locale?.toString() ?: 'en')
        if(orgType == null) return null
        IcsrOrganizationType orgName = IcsrOrganizationType.findByOrg_name_idAndLangId(orgType?.org_name_id, langId)
        if (!orgName) {
            Integer defaultLangId = Holders.applicationContext.getBean("sqlGenerationService").getPVALanguageId('en')
            orgName = IcsrOrganizationType.findByOrg_name_idAndLangId(orgType?.org_name_id, defaultLangId)
        }
        return orgName
    }

    static getOrganizationTypeIdByPreference(def id){
        IcsrOrganizationType orgType = IcsrOrganizationType.get(id)
        if(orgType == null) return null
        IcsrOrganizationType orgName = getOrganizationTypeByPreference(orgType)
        return orgName?.id
    }

    static getOrganizationCountryNameByPreference(String countryName){
        OrganizationCountry OrgCountry = null
        User currenUser = Holders.applicationContext.getBean("userService").currentUser
        String userLang = currenUser?.preference?.locale?.toString() ?: 'en'
        OrganizationCountry.'pva'.withNewSession {
            OrgCountry = OrganizationCountry.findByName(countryName)
        }
        if(OrgCountry == null) return null
        OrgCountry = OrganizationCountry.'pva'.withNewSession {
            return OrganizationCountry.findByIdAndLangDesc(OrgCountry.id, userLang)
        }
        return OrgCountry?.name
    }

    static getIcsrRuleEvaluationList(boolean isJapanProfile) {
        List<IcsrRuleEvaluationEnum> values = []
        if (isJapanProfile) {
            values = IcsrRuleEvaluationEnum.values() - IcsrRuleEvaluationEnum.DEVICE_REPORTING
        } else {
            values = IcsrRuleEvaluationEnum.values() - IcsrRuleEvaluationEnum.CLINICAL_RESEARCH_MEASURE_REPORT
        }
        return values.collect { [id: it.key, text: getMessage(it.getI18nKey())] }
    }

    static getIcsrRuleEvaluationValue(IcsrRuleEvaluationEnum icsrRuleEvaluationEnum) {
        return [id: icsrRuleEvaluationEnum?.key, text: getMessage(icsrRuleEvaluationEnum?.getI18nKey())]
    }

    static getDictionaryGroupType(String type) {
        switch(type) {
            case PVDictionaryConfig.PRODUCT_GRP_TYPE.toString() : return getMessage("app.productDictionary.product.group")
            case PVDictionaryConfig.EVENT_GRP_TYPE.toString() : return getMessage("app.eventDictionary.eventGroup")
        }
    }

    static getDictionaryGroupDataSources(String dataSources) {
        List dataSourceNames = dataSources.substring(1, dataSources.length() - 1).split(", ").collect {
            Holders.config.product.dictionary.datasources.additionalInfo."$it"?.displayName ?: it
        }

        return dataSourceNames.join(", ")
    }

}