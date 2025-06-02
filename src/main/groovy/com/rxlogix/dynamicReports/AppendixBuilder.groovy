package com.rxlogix.dynamicReports

import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.QueryService
import com.rxlogix.ReportExecutorService
import com.rxlogix.UserService
import com.rxlogix.config.Query
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.util.DateUtil
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.QueryExpressionValue
import com.rxlogix.config.ReportResult
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.util.MiscUtil
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource

import com.rxlogix.ConfigurationService
import org.springframework.context.NoSuchMessageException

import static net.sf.dynamicreports.report.builder.DynamicReports.col
import static net.sf.dynamicreports.report.builder.DynamicReports.type
import com.rxlogix.Constants

class AppendixBuilder {
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    QueryService queryService = Holders.applicationContext.getBean("queryService")
    ConfigurationService configurationService = Holders.applicationContext.getBean("configurationService")
    ReportExecutorService reportExecutorService = Holders.applicationContext.getBean("reportExecutorService")
    UserService userService = Holders.applicationContext.getBean("userService")

    private static final String SECTION_TITLE_FIELD = "sectionTitle"
    private static final String TEMPLATE_NAME_FIELD = "templateName"
    private static final String QUERY_NAME_FIELD = "queryName"
    private static final String CRITERIA_FIELD = "criteria"

    void createAppendix(ExecutedReportConfiguration executedConfigurationInstance, ReportResult reportResult, Map params,
                               ExecutedTemplateQuery executedTemplateQuery,
                               ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            ReportBuilder reportBuilder = new ReportBuilder()
            JasperReportBuilder appendixSheet = reportBuilder.initializeNewReport()

            //todo:  get the header from setHeaderAndFooter return value?
            String header = customMessageService.getMessage("jasperReports.appendix")
            HeaderBuilder headerBuilder = new HeaderBuilder()
            FooterBuilder footerBuilder = new FooterBuilder()

            headerBuilder.setHeader(executedConfigurationInstance, params, appendixSheet, executedTemplateQuery, header, true)
            buildAppendixSheet(appendixSheet, executedConfigurationInstance, reportResult, params.sectionsToExport)
            executedTemplateQuery?.executedTemplate?.showTemplateFooter = false
            footerBuilder.setFooter(params, appendixSheet, executedTemplateQuery, true)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = appendixSheet
            jasperReportBuilderEntry.excelSheetName = header
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private buildAppendixSheet(JasperReportBuilder report, ExecutedReportConfiguration executedConfiguration, ReportResult reportResult, List<Long> sectionsToExport = null) {
        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(Templates.columnTitleStyle)
        report.setColumnStyle(Templates.columnStyle)
        report.addColumn(col.column(customMessageService.getMessage("app.label.sectionTitle"), SECTION_TITLE_FIELD, type.stringType()))
        report.addColumn(col.column(customMessageService.getMessage("app.label.templateName"), TEMPLATE_NAME_FIELD, type.stringType()))
        report.addColumn(col.column(customMessageService.getMessage("app.label.queryName"), QUERY_NAME_FIELD, type.stringType()))
        report.addColumn(col.column(customMessageService.getMessage("app.label.criteria"), CRITERIA_FIELD, type.stringType()))
        report.setDataSource(createAppendixSheetDataSource(executedConfiguration, reportResult, sectionsToExport))

    }

    String generateReadableGlobalQuery(ExecutedReportConfiguration executedConfiguration, int reassessIndex) {
        String result = ""

        Set<QueryExpressionValue> blanks = []
        executedConfiguration.executedGlobalQueryValueLists?.each {
            it.parameterValues.each {
                if (it.hasProperty('reportField')) {
                    blanks.add(it)
                }
            }
        }

        Map dataMap = MiscUtil.parseJsonText(executedConfiguration.executedGlobalQuery.JSONQuery)
        Map allMap = dataMap.all
        List containerGroupsList = allMap.containerGroups
        Locale locale = executedConfiguration.locale
        for (int i = 0; i < containerGroupsList.size(); i++) {
            if ((allMap.keyword) && (i > 0)) {
                String keyword
                try {
                    keyword = messageSource.getMessage("app.keyword.${allMap.keyword.toUpperCase()}", null, locale)
                } catch (NoSuchMessageException exception) {
                    log.warn("No app.queryOperator found for locale: $locale. Defaulting to English locale.")
                    keyword = messageSource.getMessage("app.keyword.${allMap.keyword.toUpperCase()}", null, Locale.ENGLISH)
                }
                result += " $keyword "
            }
            def executed = queryService.buildCriteriaFromGroup(true, containerGroupsList[i],
                    executedConfiguration.nextRunDate,
                    "UTC", blanks, locale, reassessIndex)
            result += "(${executed})"
        }
        return result
    }

    private JRDataSource createAppendixSheetDataSource(ExecutedReportConfiguration executedConfigurationInstance, ReportResult reportResult, List<Long> sectionsToExport = null) {
        Collection<Map<java.lang.String,?>> result = new LinkedList<>()
        List<ExecutedTemplateQuery> executedTemplateQueries = []
        Locale locale = userService.getCurrentUser()?.preference?.locale ?: executedConfigurationInstance?.owner?.preference?.locale

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
        executedTemplateQueries.each {
            String criteriaData = ''
            switch (it?.executedQuery?.queryType) {
                case QueryTypeEnum.QUERY_BUILDER:
                    criteriaData = queryService.generateReadableQueryFromExTemplateQuery(it, 0) + ' '
                    break;
                case QueryTypeEnum.CUSTOM_SQL:
                    criteriaData = it.executedQuery.customSQLQuery + ' '
                    break;
                case QueryTypeEnum.SET_BUILDER:
                    List<Tuple2> values = it.executedQueryValueLists?.findAll {
                        it.parameterValues?.any { it?.hasProperty('reportField') }
                    }?.collectMany {
                        it.parameterValues?.findAll { it?.hasProperty('reportField') }?.collect {
                            new Tuple2(it.reportField?.name, it.value)
                        }
                    }
                    criteriaData = queryService.buildSetSQLFromJSON(it.executedQuery.JSONQuery, values) + ' '
                    break;
                default:
                    criteriaData = ' '
            }
            List<String> listOfCriteriaData = CriteriaSheetBuilder.splitParameterValue(criteriaData, " ")
            listOfCriteriaData.eachWithIndex{ String entry, int iterator ->
                Map<java.lang.String, ?> item = new HashMap<>()
                item.put(SECTION_TITLE_FIELD, iterator == 0 ? dynamicReportService.getReportNameAsTitle(executedConfigurationInstance, it) : '')
                item.put(TEMPLATE_NAME_FIELD, iterator == 0 ? it.executedTemplate.name : '')
                item.put(QUERY_NAME_FIELD, iterator == 0 ? it?.executedQuery?.name ?: customMessageService.getMessage("app.label.none") : '')
                String queryParamters = null
                switch (it?.executedQuery?.queryType) {
                    case QueryTypeEnum.QUERY_BUILDER:
                    case QueryTypeEnum.CUSTOM_SQL:
                    case QueryTypeEnum.SET_BUILDER:
                        queryParamters = configurationService.replaceStringWithDate(listOfCriteriaData.get(iterator), it as ExecutedTemplateQuery, false, locale)
                        break
                    default:
                        queryParamters = null
                }
                if(it?.executedQuery instanceof Query && it?.executedQuery?.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE && !it?.executedQuery?.reassessListednessDate) {
                    queryParamters = queryParamters + "\n" +customMessageService.getMessage("app.label.reassessListedness") + " " + customMessageService.getMessage("app.reassessListednessEnum.CUSTOM_START_DATE") + " = " + DateUtil.dateRangeString(it.reassessListednessDate, Constants.DEFAULT_SELECTED_TIMEZONE)
                }
                if(it?.onDemandSectionParams) {
                    queryParamters = queryParamters + "\n" +customMessageService.getMessage("app.label.drilldownTo") + " " +reportExecutorService.getDrilldownOnDemandSection(it.onDemandSectionParams)
                }
                item.put(CRITERIA_FIELD, queryParamters)
                result.add(item)
            }
        }
        if(executedConfigurationInstance.executedGlobalQuery) {
            String criteriaData = ''
            switch (executedConfigurationInstance?.executedGlobalQuery?.queryType) {
                case QueryTypeEnum.QUERY_BUILDER:
                    criteriaData = generateReadableGlobalQuery(executedConfigurationInstance, 0) + ' '
                    break;
                case QueryTypeEnum.CUSTOM_SQL:
                    criteriaData = executedConfigurationInstance?.executedGlobalQuery?.customSQLQuery + ' '
                    break;
                case QueryTypeEnum.SET_BUILDER:
                    List<Tuple2> values = executedConfigurationInstance.executedGlobalQueryValueLists?.findAll {
                        it.parameterValues?.any { it?.hasProperty('reportField') }
                    }?.collectMany {
                        it.parameterValues?.findAll { it?.hasProperty('reportField') }?.collect {
                            new Tuple2(it.reportField?.name, it.value)
                        }
                    }
                    criteriaData = queryService.buildSetSQLFromJSON(executedConfigurationInstance?.executedGlobalQuery.JSONQuery, values) + ' '
                    break;
                default:
                    criteriaData = ' '
            }
            List<String> listOfCriteriaData = CriteriaSheetBuilder.splitParameterValue(criteriaData, " ")
            listOfCriteriaData.eachWithIndex { String entry, int iterator ->
                Map<java.lang.String, ?> item = new HashMap<>()
                item.put(SECTION_TITLE_FIELD, executedConfigurationInstance.reportName + '\n' + "(global query)")
                item.put(TEMPLATE_NAME_FIELD, '')
                item.put(QUERY_NAME_FIELD, executedConfigurationInstance.executedGlobalQuery.name)
                switch (executedConfigurationInstance?.executedGlobalQuery?.queryType) {
                    case QueryTypeEnum.QUERY_BUILDER:
                    case QueryTypeEnum.CUSTOM_SQL:
                    case QueryTypeEnum.SET_BUILDER:
                        item.put(CRITERIA_FIELD, configurationService.replaceGlobalQueryStringWithDate(listOfCriteriaData.get(iterator),executedConfigurationInstance, false, locale))
                        break
                    default:
                        item.put(CRITERIA_FIELD, null)
                }
                result.add(item)
            }
        }
        return new JRMapCollectionDataSource(result)
    }

}