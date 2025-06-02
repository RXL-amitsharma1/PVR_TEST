package com.rxlogix.dynamicReports

import com.rxlogix.*
import com.rxlogix.config.*
import com.rxlogix.enums.AssignedToFilterEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter
import net.sf.dynamicreports.report.builder.DynamicReports
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment
import net.sf.dynamicreports.report.constant.Markup
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JREmptyDataSource
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import grails.converters.JSON
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class CriteriaSheetBuilder {

    ConfigurationService configurationService = Holders.applicationContext.getBean("configurationService")
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    ReportExecutorService reportExecutorService = Holders.applicationContext.getBean("reportExecutorService")
    EtlJobService etlJobService = Holders.applicationContext.getBean("etlJobService")
    UserService userService = Holders.applicationContext.getBean("userService")
    TemplateService templateService = Holders.applicationContext.getBean("templateService")

    private static final String SECTION_TITLE_FIELD = "sectionTitle"
    private static final String TEMPLATE_NAME_FIELD = "templateName"
    private static final String QUERY_FIELD = "query"
    private static final String PARAMETERS_FIELD = "parameters"
    private static final String DATE_RANGE_FIELD = "dateRange"
    private static final String VERSION_ASOF_FIELD = "evaluateCaseDateOn"
    private static final String QUERY_LEVEL_FIELD = "queryLevel"
    private static final String CASE_COUNT_FIELD = "caseCount"
    private static final String NON_SUBMITTABLE_FIELD = "nonSubmittable"
    private static final String DRILL_DOWN_FIELD = "drillDown"
    private static final String ETL_RUN_FIELD = "etlStarttime"

    protected Logger log = LoggerFactory.getLogger(getClass())

    void createCriteriaSheet(ExecutedReportConfiguration executedConfigurationInstance, ReportResult reportResult, Map params,
                                     ExecutedTemplateQuery executedTemplateQuery,
                                     ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            ReportBuilder reportBuilder = new ReportBuilder()
            JasperReportBuilder criteriaSheet = reportBuilder.initializeNewReport()

            //todo:  get the header from setHeaderAndFooter return value?
            String header = customMessageService.getMessage("jasperReports.criteriaSheet")
            HeaderBuilder headerBuilder = new HeaderBuilder()
            FooterBuilder footerBuilder = new FooterBuilder()

            headerBuilder.setHeader(executedConfigurationInstance, params, criteriaSheet, executedTemplateQuery, header, true)
            buildCriteriaSheet(criteriaSheet, executedConfigurationInstance, reportResult, params)
            footerBuilder.setFooter(params, criteriaSheet, null, true)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = criteriaSheet
            jasperReportBuilderEntry.excelSheetName = header
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    /**
     * Builds a criteria sheet for the given report.
     * @param report               The JasperReportBuilder to which the criteria sheet will be added.
     * @param executedConfiguration The executed report configuration.
     * @param reportResult         The report result.
     * @param params               A map of parameters.
     */
    private buildCriteriaSheet(JasperReportBuilder report, ExecutedReportConfiguration executedConfiguration,
                               ReportResult reportResult, Map params) {
        StyleBuilder parametersStyle = stl.style(Templates.columnStyle).setMarkup(Markup.HTML)
        ComponentBuilder<?, ?> criteriaSheetHeader = createCriteriaSheetHeader(executedConfiguration, params)
        criteriaSheetHeader.setPrintWhenExpression(exp.printInFirstPage())
        criteriaSheetHeader.setRemoveLineWhenBlank(true)
        report.addDetail(criteriaSheetHeader)

        JasperReportBuilder subreport = DynamicReports.report()
        if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
            subreport.setIgnorePageWidth(true)
        }
        subreport.setTemplate(Templates.reportTemplate)
        subreport.setDefaultFont(Templates.defaultFontStyle)
        subreport.setColumnTitleStyle(Templates.columnTitleStyle)
        subreport.setColumnStyle(Templates.columnStyle.setTopPadding(1))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.sectionTitle"), SECTION_TITLE_FIELD, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.templateName"), TEMPLATE_NAME_FIELD, type.stringType()))
        if (params.filter || params.searchData || params.rowIdFilter || params.globalSearch || params.assignedToFilter)
            subreport.addColumn(col.column(customMessageService.getMessage("app.label.filters"), DRILL_DOWN_FIELD, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.query"), QUERY_FIELD, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.parameters"), PARAMETERS_FIELD, type.stringType()).setStyle(parametersStyle))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.DateRange"), DATE_RANGE_FIELD, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.EvaluateCaseDateOn"), VERSION_ASOF_FIELD, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.queryLevel"), QUERY_LEVEL_FIELD, type.stringType()))
        subreport.addColumn(col.column(customMessageService.getMessage("app.label.caseCount"), CASE_COUNT_FIELD, type.longType()).setValueFormatter(new CaseCountFormatter()))
        if (executedConfiguration.instanceOf(ExecutedPeriodicReportConfiguration))
            subreport.addColumn(col.column(customMessageService.getMessage("app.label.draftOnly"), NON_SUBMITTABLE_FIELD, type.stringType()))

        subreport.setDataSource(createCriteriaSheetDataSource(executedConfiguration, reportResult, params.sectionsToExport ?: [], params, Boolean.valueOf(params.isInDraftMode ?: false)))
        report.setDataSource(new JREmptyDataSource())
        report.addDetail(cmp.subreport(subreport))
    }

    /**
     * Creates a header for a criteria sheet based on the provided executed report configuration.
     *
     * @param executedConfigurationInstance The executed report configuration instance.
     * @param params Optional parameters map.
     * @return The ComponentBuilder for the criteria sheet header.
     */
    private ComponentBuilder<?, ?> createCriteriaSheetHeader(ExecutedReportConfiguration executedConfigurationInstance, Map params = null) {
        Boolean isExecutedPeriodicReport = executedConfigurationInstance instanceof ExecutedPeriodicReportConfiguration

        List<HorizontalListBuilder> reportCriteriaList =[]
        //Add list item 1
        HorizontalListBuilder reportCriterialListItem = cmp.horizontalList()
        //Top Section
        addCriteriaSheetSectionTitle(reportCriterialListItem, "app.label.reportCriteria")
        addCriteriaSheetAttribute(reportCriterialListItem, "app.label.reportName", executedConfigurationInstance.reportName)
        addCriteriaSheetAttribute(reportCriterialListItem, "app.label.reportVersion", String.valueOf(executedConfigurationInstance.numOfExecutions))
        addCriteriaSheetAttribute(reportCriterialListItem, "app.label.description", executedConfigurationInstance.description ?: "")
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null // After adding to list make sure to nullify reference.
        //Add list item 2
        reportCriterialListItem = cmp.horizontalList()
        addDictionaryValuesToCriteriaSheet(reportCriterialListItem, "app.label.productSelection", getProductSelectionValue(executedConfigurationInstance), ",")
        addDictionaryValuesToCriteriaSheet(reportCriterialListItem, "app.label.productDictionary.include.who.drugs", executedConfigurationInstance.includeWHODrugs?"Yes":"No", ",")
        if(Holders.config.getProperty('safety.source') == Constants.PVCM)
            addDictionaryValuesToCriteriaSheet(reportCriterialListItem, "app.label.productDictionary.multi.substance", executedConfigurationInstance.isMultiIngredient?"Yes":"No", ",")
        else
            addDictionaryValuesToCriteriaSheet(reportCriterialListItem, "app.label.productDictionary.multi.ingredient", executedConfigurationInstance.isMultiIngredient?"Yes":"No", ",")
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null // After adding to list make sure to nullify reference.
        //Add list item 3
        reportCriterialListItem = cmp.horizontalList()
        addDictionaryValuesToCriteriaSheet(reportCriterialListItem, "app.label.studySelection", getStudySelectionValue(executedConfigurationInstance), ",")
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null // After adding to list make sure to nullify reference.
        if (!isExecutedPeriodicReport) {
            //Add list item 4 if applicable
            reportCriterialListItem = cmp.horizontalList()
            addDictionaryValuesToCriteriaSheet(reportCriterialListItem, "app.label.eventSelection", getEventSelectionValue(executedConfigurationInstance), ",")
            reportCriteriaList.add(reportCriterialListItem)
            reportCriterialListItem = null
        }

        //Add list item 5 if applicable
        reportCriterialListItem = cmp.horizontalList()
        addCriteriaSheetAttribute(reportCriterialListItem, "app.label.globalQueryName", executedConfigurationInstance.executedGlobalQuery?.name ?: customMessageService.getMessage("app.label.no.query"))
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null
        reportCriterialListItem = cmp.horizontalList()
        addDictionaryValuesToCriteriaSheet(reportCriterialListItem, "app.label.globalQuery.parameter", formatParameters(executedConfigurationInstance.executedGlobalQueryValueLists), ";", true)
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null
        reportCriterialListItem = cmp.horizontalList()
        addCriteriaSheetAttribute(reportCriterialListItem, "app.label.globalDateRangInformation", configurationService.getDateRangeValue(executedConfigurationInstance.executedGlobalDateRangeInformation, executedConfigurationInstance.owner?.preference?.locale))
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null

        //Add list item 6
        reportCriterialListItem = cmp.horizontalList()
        addCriteriaSheetAttribute(reportCriterialListItem, "userGroup.source.profiles.label", executedConfigurationInstance.sourceProfile.sourceName)
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null
        reportCriterialListItem = cmp.horizontalList()
        addCriteriaSheetAttribute(reportCriterialListItem, "app.label.EvaluateCaseDateOn", executedConfigurationInstance?.evaluateDateAs.getInstanceIdentifierForAuditLog())
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null
        reportCriterialListItem = cmp.horizontalList()
        addCriteriaSheetAttribute(reportCriterialListItem, "app.label.DateRangeType", customMessageService.getMessage(executedConfigurationInstance.dateRangeType?.getI18nKey()))
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null
        reportCriterialListItem = cmp.horizontalList()
        addCriteriaSheetAttribute(reportCriterialListItem, "reportCriteria.exclude.non.valid.cases",
                executedConfigurationInstance.excludeNonValidCases ? customMessageService.getMessage("app.label.yes") :
                        customMessageService.getMessage("app.label.no"))
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null
        reportCriterialListItem = cmp.horizontalList()
        addCriteriaSheetAttribute(reportCriterialListItem, "reportCriteria.exclude.deleted.cases",
                executedConfigurationInstance.excludeDeletedCases ? customMessageService.getMessage("app.label.yes") :
                        customMessageService.getMessage("app.label.no"))
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null
        reportCriterialListItem = cmp.horizontalList()
        addCriteriaSheetAttribute(reportCriterialListItem, "reportCriteria.exclude.follow.up",
                executedConfigurationInstance.excludeFollowUp ? customMessageService.getMessage("app.label.yes") :
                        customMessageService.getMessage("app.label.no"))
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null
        reportCriterialListItem = cmp.horizontalList()
        addCriteriaSheetAttribute(reportCriterialListItem, "app.label.SuspectProduct",
                executedConfigurationInstance.suspectProduct ? customMessageService.getMessage("app.label.yes") :
                        customMessageService.getMessage("app.label.no"))
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null

        if (!isExecutedPeriodicReport) {
            reportCriterialListItem = cmp.horizontalList()
            addCriteriaSheetAttribute(reportCriterialListItem, "app.label.eventSelection.limit.primary.path",
                    executedConfigurationInstance.limitPrimaryPath ? customMessageService.getMessage("app.label.yes") :
                            customMessageService.getMessage("app.label.no"))
            reportCriteriaList.add(reportCriterialListItem)
            reportCriterialListItem = null
            reportCriterialListItem = cmp.horizontalList()
            addCriteriaSheetAttribute(reportCriterialListItem, "reportCriteria.include.medically.confirm.cases",
                    executedConfigurationInstance.includeMedicallyConfirmedCases ? customMessageService.getMessage("app.label.yes") :
                            customMessageService.getMessage("app.label.no"))
            reportCriteriaList.add(reportCriterialListItem)
            reportCriterialListItem = null
            reportCriterialListItem = cmp.horizontalList()
            addCriteriaSheetAttribute(reportCriterialListItem, "reportCriteria.include.locked.versions.only",
                    executedConfigurationInstance.includeLockedVersion ? customMessageService.getMessage("app.label.yes") :
                            customMessageService.getMessage("app.label.no"))
            reportCriteriaList.add(reportCriterialListItem)
            reportCriterialListItem = null
            if (executedConfigurationInstance instanceof ExecutedConfiguration) {
                reportCriterialListItem = cmp.horizontalList()
                addCriteriaSheetAttribute(reportCriterialListItem, "app.label.useCaseSeries",
                        executedConfigurationInstance.usedCaseSeries?.seriesName ?: customMessageService.getMessage("app.label.none"))
                reportCriteriaList.add(reportCriterialListItem)
                reportCriterialListItem = null
            }
            reportCriterialListItem = cmp.horizontalList()
            addCriteriaSheetAttribute(reportCriterialListItem, "reportCriteria.include.non.significant.followup.cases",
                    executedConfigurationInstance.includeNonSignificantFollowUp ? customMessageService.getMessage("app.label.yes") :
                            customMessageService.getMessage("app.label.no"))
            reportCriteriaList.add(reportCriterialListItem)
            reportCriterialListItem = null
            reportCriterialListItem = cmp.horizontalList()
            addCriteriaSheetAttribute(reportCriterialListItem, "app.label.removeOldVersion",
                    executedConfigurationInstance.removeOldVersion ? customMessageService.getMessage("app.label.yes") :
                            customMessageService.getMessage("app.label.no"))
            reportCriteriaList.add(reportCriterialListItem)
            reportCriterialListItem = null
        }
        if (isExecutedPeriodicReport) {
            reportCriterialListItem = cmp.horizontalList()
            addCriteriaSheetAttribute(reportCriterialListItem, "app.label.includePreviousMissingCases",
                    executedConfigurationInstance.includePreviousMissingCases ? customMessageService.getMessage("app.label.yes") :
                            customMessageService.getMessage("app.label.no"))
            reportCriteriaList.add(reportCriterialListItem)
            reportCriterialListItem = null
            reportCriterialListItem = cmp.horizontalList()
            addCriteriaSheetAttribute(reportCriterialListItem, "app.label.includeOpenCasesInDraft",
                    executedConfigurationInstance.includeOpenCasesInDraft ? customMessageService.getMessage("app.label.yes") :
                            customMessageService.getMessage("app.label.no"))
            reportCriteriaList.add(reportCriterialListItem)
            reportCriterialListItem = null
        }
        reportCriterialListItem = cmp.horizontalList()
        addCriteriaSheetAttribute(reportCriterialListItem, "app.label.includeAllStudyDrugsCases",
                executedConfigurationInstance.includeAllStudyDrugsCases ? executedConfigurationInstance.studyDrugs :
                        customMessageService.getMessage("app.label.no"))
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null
        reportCriterialListItem = cmp.horizontalList()
        addCriteriaSheetAttribute(reportCriterialListItem, "app.label.reportOwner", executedConfigurationInstance.owner.fullNameAndUserName)
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null
        reportCriterialListItem = cmp.horizontalList()
        boolean isFinal = ViewHelper.isFinalReport(executedConfigurationInstance, Boolean.valueOf(params.isInDraftMode ?: false))
        addCriteriaSheetAttribute(reportCriterialListItem, "app.label.runDateAndTime", ViewHelper.formatRunDateAndTime(executedConfigurationInstance, isFinal))
        reportCriteriaList.add(reportCriterialListItem)
        reportCriterialListItem = null
        if(Holders.config.safety.source == Constants.PVCM) {
            reportCriterialListItem = cmp.horizontalList()
            addCriteriaSheetAttribute(reportCriterialListItem, "app.last.successful.etl.start.time", Constants.NOT_APPLICABLE)
            reportCriteriaList.add(reportCriterialListItem)
            reportCriterialListItem = null
        } else {
            reportCriterialListItem = cmp.horizontalList()
            addCriteriaSheetAttribute(reportCriterialListItem, "app.last.successful.etl.start.time", DateUtil.getFormattedDateForLastSuccessfulEtlRun(ViewHelper.getTimeZone(userService.getCurrentUser()), (isFinal ? executedConfigurationInstance?.finalExecutedEtlDate : executedConfigurationInstance?.executedETLDate)))
            reportCriteriaList.add(reportCriterialListItem)
            reportCriterialListItem = null
        }

        HorizontalListBuilder reportSectionsList = cmp.horizontalList().setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT).setTopPadding(30))
        addCriteriaSheetSectionTitle(reportSectionsList, "report.sections.criteria")
        reportSectionsList.add(reportSectionsList)
        return cmp.multiPageList(*reportCriteriaList)
    }

    private JRDataSource createCriteriaSheetDataSource(ExecutedReportConfiguration executedConfigurationInstance, ReportResult reportResult, List<Long> sectionsToExport, Map params, Boolean isInDraftMode) {
        Collection<Map<java.lang.String,?>> result = new LinkedList<>()
        List<ExecutedTemplateQuery> executedTemplateQueries = []
        String templateName = ""

        if (reportResult) {
            executedTemplateQueries << reportResult.executedTemplateQuery
        } else {
            if(executedConfigurationInstance.containsTemplateSet()) {
                executedTemplateQueries = new ArrayList<ExecutedTemplateQuery>(executedConfigurationInstance.getSectionExTempQueriesMap().keySet())
            }else {
                executedTemplateQueries = executedConfigurationInstance.getExecutedTemplateQueriesForProcessing()
            }
        }
        if (sectionsToExport) {
            executedTemplateQueries = executedTemplateQueries.findAll { it.id in sectionsToExport }
        }
        boolean isAggregate = executedConfigurationInstance.instanceOf(ExecutedPeriodicReportConfiguration)
        Locale locale = userService.getCurrentUser()?.preference?.locale ?: executedConfigurationInstance.owner.preference?.locale
        Date executedAsOfVersionDate = executedConfigurationInstance.getExecutedAsOfVersionDate()
        executedTemplateQueries.each {
            String parameterData = formatParameters(it)
            List<String> parameterDataList = splitParameterValue(parameterData, ";")
            templateName = it.executedTemplate.name

            if (it.executedTemplate.ciomsI) {
                if (it?.blindProtected && it?.privacyProtected) {
                    templateName = templateName + " (" + customMessageService.getMessage("templateQuery.blindedPrivacy.label") + ")"
                } else if (it?.blindProtected) {
                    templateName = templateName + " (" + customMessageService.getMessage("templateQuery.blinded.label") + ")"
                } else if (it?.privacyProtected) {
                    templateName = templateName + " (" + customMessageService.getMessage("templateQuery.privacyProtected.label") + ")"
                }
            }
            parameterDataList.eachWithIndex{ entry, int iterator ->
                Map item = [:]
                item.put(SECTION_TITLE_FIELD, iterator == 0 ? dynamicReportService.getReportNameAsTitle(executedConfigurationInstance, it) : '')
                item.put(TEMPLATE_NAME_FIELD, iterator == 0 ? (params.filter ? MiscUtil.matchCSVPattern(templateName) : templateName) : '')
                if ((params.filter || params.searchData || params.globalSearch || params.rowIdFilter || params.assignedToFilter) && iterator == 0) {
                    String filter = ""
                    ReportTemplate template = reportResult.template ? GrailsHibernateUtil.unwrapIfProxy(reportResult.template) : reportResult.executedTemplateQuery.getUsedTemplate()

                    Map metadata = (template instanceof CaseLineListingTemplate) ? templateService.getResultTable((CaseLineListingTemplate) template) : null
                    if (params.searchData || params.rowIdFilter || params.assignedToFilter || params.globalSearch) {

                        if (params.rowIdFilter) {
                            filter = customMessageService.getMessage("app.label.selectedRowIds") + ": " + params.rowIdFilter + "; "
                        } else {
                            if (params.searchData) {
                                filter = customMessageService.getMessage("app.label.pageFilter") + ": " + JSON.parse(params.searchData).collect { k, v ->

                                    if (v.type == "range") return (metadata?.fieldsCodeNameMap[v.name] ?: v.name) + ":" + (v.value1 ?: ViewHelper.getEmptyLabel()) + " - " + (v.value2 ?: ViewHelper.getEmptyLabel())
                                    return (metadata?.fieldsCodeNameMap[v.name] ?: v.name) + ":" + (v.value ?: ViewHelper.getEmptyLabel())
                                }.join(";") + ";"
                            }
                            if (params.globalSearch) {
                                filter += customMessageService.getMessage("app.label.globalSearch") + ": " + params.globalSearch + "; "
                            }
                            if (params.assignedToFilter) {
                                String val = ""
                                if (params.assignedToFilter == AssignedToFilterEnum.ME.name()) {
                                    val = customMessageService.getMessage("app.pvc.assignedTo.ME")
                                } else if (params.assignedToFilter == AssignedToFilterEnum.MY_GROUPS.name()) {
                                    val = customMessageService.getMessage("app.pvc.assignedTo.MY_GROUPS")
                                } else if (params.assignedToFilter.startsWith(Constants.USER_GROUP_TOKEN)) {
                                    UserGroup.withNewSession {
                                        UserGroup group = UserGroup.get(params.assignedToFilter.split("_")[1] as Long)
                                        val = group.name
                                    }
                                } else if (params.assignedToFilter.startsWith(Constants.USER_TOKEN)) {
                                    User.withNewSession {
                                        User user = User.get(params.assignedToFilter.split("_")[1] as Long)
                                        val = user.fullName ?: user.username
                                    }
                                }
                                filter += customMessageService.getMessage("app.label.assignedToId") + ": " + val + "; "
                            }
                        }
                    }
                    String drilldownFilter = params.filter ? params.list("filter").collect {
                        (metadata?.fieldsCodeNameMap?.get(it.field) ?: (customMessageService.getMessage("app.reportField." + it.field) ?: it.field)) + ":" + (it.value ?: ViewHelper.getEmptyLabel())
                    }.join(";") : ""
                    String label = drilldownFilter ? (customMessageService.getMessage("app.label.drillDownParameters") + ": ") : ""
                    label += (drilldownFilter ?: "") + (drilldownFilter ? "; " : "") + filter

                    item.put(DRILL_DOWN_FIELD, label)
                }
                item.put(QUERY_FIELD, iterator == 0 ? it?.executedQuery?.name ?: customMessageService.getMessage("app.label.none") : '')
                item.put(PARAMETERS_FIELD, configurationService.replaceStringWithDate(parameterDataList.get(iterator), it as ExecutedTemplateQuery, true, locale))
                item.put(DATE_RANGE_FIELD, iterator == 0 ? configurationService.getDateRangeValueForCriteria(it, locale) : '')
                item.put(VERSION_ASOF_FIELD, iterator == 0 ? formatDateForCriteriaWithoutTZ(executedAsOfVersionDate ?: it.executedDateRangeInformationForTemplateQuery.executedAsOfVersionDate, locale) : '')
                item.put(QUERY_LEVEL_FIELD, iterator == 0 ? customMessageService.getMessage("${it.queryLevel.i18nKey}") : '')
                item.put(CASE_COUNT_FIELD, iterator == 0 ? it.getReportResult(isInDraftMode).caseCount : 0L)
                if (isAggregate)
                    item.put(NON_SUBMITTABLE_FIELD, iterator == 0 ? it.getReportResult(isInDraftMode).executedTemplateQuery.isDraftOnly() ? customMessageService.getMessage("app.label.yes") : customMessageService.getMessage("app.label.no") : '')
                result.add(item)
            }
        }
        return new JRMapCollectionDataSource(result)
    }

    protected void addCriteriaSheetSectionTitle(HorizontalListBuilder list, String labelId) {
        list.add(cmp.text(customMessageService.getMessage(labelId)).setStyle(Templates.criteriaSectionTitleStyle)).newRow()
    }

    protected void addCriteriaSheetAttribute(HorizontalListBuilder list, String labelId, String value, boolean isHtmlValue = false) {
        if (value) {
            value = MiscUtil.matchCSVPattern(value)
            def valueField = cmp.text(value).setStyle(Templates.criteriaValueStyle)
            if (isHtmlValue) {
                valueField.setMarkup(Markup.HTML)
            }
            list.add(cmp.text(customMessageService.getMessage(labelId) + (labelId ? ":" : "")).setFixedColumns(25).setStyle(Templates.criteriaNameStyle),
                    valueField).newRow()
        }
    }

    private String getProductSelectionValue(ExecutedReportConfiguration executedConfigurationInstance) {
        if (executedConfigurationInstance.productSelection || executedConfigurationInstance.validProductGroupSelection) {
            return ViewHelper.getDictionaryValues(executedConfigurationInstance, DictionaryTypeEnum.PRODUCT)
        }
        return customMessageService.getMessage("app.label.none")
    }

    private String getStudySelectionValue(ExecutedReportConfiguration executedConfigurationInstance) {
        if (executedConfigurationInstance.studySelection) {
            return ViewHelper.getDictionaryValues(executedConfigurationInstance, DictionaryTypeEnum.STUDY)
        }
        return customMessageService.getMessage("app.label.none")
    }

    private String getEventSelectionValue(ExecutedReportConfiguration executedConfigurationInstance) {
        if (executedConfigurationInstance.usedEventSelection || executedConfigurationInstance.usedValidEventGroupSelection) {
            return ViewHelper.getDictionaryValues(executedConfigurationInstance, DictionaryTypeEnum.EVENT)
        }
        return customMessageService.getMessage("app.label.none")
    }

    protected formatDateForCriteriaWithoutTZ(Date date, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getShortDateFormatForLocale(locale))
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        return sdf.format(date)
    }

    protected formatParameters(ExecutedTemplateQuery executedTemplateQuery) {
        StringBuilder sb = new StringBuilder()
        List<ExecutedTemplateValueList> executedTemplateValueLists = executedTemplateQuery.executedTemplateValueLists
        if (executedTemplateValueLists || executedTemplateQuery.showReassessDateDiv()) {
            sb.append("<i>" + customMessageService.getMessage("app.label.template") + "</i>").append("<br/>")
            executedTemplateValueLists.each {
                sb.append(it.template.name).append(";<br/>")
                it.parameterValues.each {
                    sb.append("${it.key} = ${it.value}").append(";<br/>")
                }
            }
            if (executedTemplateQuery.showReassessDateDiv())
                sb.append(customMessageService.getMessage("app.label.reassessListedness") + " " + customMessageService.getMessage("app.reassessListednessEnum.CUSTOM_START_DATE") + " " + (executedTemplateQuery.templtReassessDate? DateUtil.dateRangeString(executedTemplateQuery.templtReassessDate, Constants.DEFAULT_SELECTED_TIMEZONE) : "")+ "<br/>")
        }
        List<ExecutedQueryValueList> executedQueryValueLists = executedTemplateQuery.executedQueryValueLists
        if (executedQueryValueLists || executedTemplateQuery.showQueryReassessDateDiv()) {
            sb.append("<i>" + customMessageService.getMessage("app.label.query") + "</i>").append("<br/>")
            executedQueryValueLists.each {
                sb.append(it.query.name).append("<br/>")
                it.parameterValues.each {
                    if (it.hasProperty('reportField')) {
                        sb.append(customMessageService.getMessage("app.reportField.${it.reportField.name}")).append(" ")
                        sb.append(customMessageService.getMessage(it.operator.getI18nKey())).append(" ")
                        sb.append(it.value).append("<br/>")
                    } else {
                        sb.append("${it.key} = ${it.value}")
                    }
                }
            }
            if (executedTemplateQuery.showQueryReassessDateDiv())
                sb.append(customMessageService.getMessage("app.label.reassessListedness") + " " + customMessageService.getMessage("app.reassessListednessEnum.CUSTOM_START_DATE") + " " + (executedTemplateQuery.reassessListednessDate? DateUtil.dateRangeString(executedTemplateQuery.reassessListednessDate, Constants.DEFAULT_SELECTED_TIMEZONE) : "") + "<br/>")
        }
        if(executedTemplateQuery?.onDemandSectionParams) {
            sb.append("<br/><i>" + customMessageService.getMessage("app.label.drilldownTo") + "</i>").append("<br/>")
            sb.append(reportExecutorService.getDrilldownOnDemandSection(executedTemplateQuery.onDemandSectionParams))
        }
        if (!executedTemplateQuery.executedTemplateValueLists && !executedTemplateQuery.executedQueryValueLists && !executedTemplateQuery.onDemandSectionParams && !executedTemplateQuery.showReassessDateDiv() && !executedTemplateQuery?.showQueryReassessDateDiv()) {
            sb.append(customMessageService.getMessage("app.label.none"))
        }
        return sb.toString()
    }

    protected formatParameters(List<ExecutedQueryValueList> executedQueryValueLists) {
        StringBuilder sb = new StringBuilder()
        if (executedQueryValueLists) {
            executedQueryValueLists.each {
                sb.append(it.query.name).append("<br/>")
                it.parameterValues.eachWithIndex { pv, idx ->
                    if (pv.hasProperty('reportField')) {
                        sb.append(customMessageService.getMessage("app.reportField.${pv.reportField.name}")).append(" ")
                        sb.append(customMessageService.getMessage(pv.operator.getI18nKey())).append(" ")
                        sb.append(pv.value).append("<br/>")
                    } else {
                        sb.append("${pv.key} = ${pv.value}")
                    }
                }
            }
        }
        if (!executedQueryValueLists) {
            sb.append(customMessageService.getMessage("app.label.none"))
        }
        return sb.toString()
    }

    /**
    *Split the dictionary values by comma and then add those values from list in one single cell until it reaches the limit of cell.
    *Once the limit is reached, create new cell and keep iterating the list until this cell reaches its limit.
    **/
    private void addDictionaryValuesToCriteriaSheet(HorizontalListBuilder reportCriteriaList, String label, String text, String delimeter, boolean isHtmlValue = false) {
        int limitOfCell = Holders.config.getProperty('excel.limit.cell.characters', Integer)

        if (text != "None" && text.length() > limitOfCell) {
            List<String> valueList = text.split(delimeter)
            String textInACell = ""
            valueList.each {
                if (it.length() + textInACell.length() < limitOfCell - 2) {
                    textInACell += it + delimeter + " "
                } else if (textInACell.length() <= limitOfCell) {
                    addCriteriaSheetAttribute(reportCriteriaList, label, textInACell.trim(), isHtmlValue)
                    label = null
                    textInACell = it + delimeter + " "
                }
            }
            if (textInACell.length() <= limitOfCell) addCriteriaSheetAttribute(reportCriteriaList, null, textInACell.trim(), isHtmlValue)
        } else {
            addCriteriaSheetAttribute(reportCriteriaList, label, text, isHtmlValue)
        }
    }

    /**
     *Split the Parameter values by delimeter and then add those values from list in one single cell until it reaches the limit of cell.
     *Once the limit is reached, create new cell and keep iterating the list until this cell reaches its limit.
     **/

    public static List<String> splitParameterValue(String text, String delimeter) {
        List<String> parameterValuesList = []
        int limitOfCell = Holders.config.getProperty('excel.limit.cell.characters', Integer)
        String textToProcess = text.trim()
        if (textToProcess != "None" && textToProcess.length() > limitOfCell) {
            List<String> valueList = textToProcess.split(delimeter)
            String textInACell
            valueList.each {
                if (textInACell && it.length() + textInACell.length() < limitOfCell - 2) {
                    textInACell += it + delimeter + " "
                } else {
                    parameterValuesList.add(textInACell)
                    textInACell = it + delimeter + " "
                }
            }
            parameterValuesList.add(textInACell)
        } else {
            parameterValuesList.add(textToProcess)
        }
        return parameterValuesList  - [null]
    }


    private class CaseCountFormatter extends AbstractValueFormatter<String, Long> {
        @Override
        String format(Long value, ReportParameters reportParameters) {
            return value == null ? customMessageService.getMessage("app.label.notAvailable") : value == 0L ? '' : String.valueOf(value)
        }
    }
}
