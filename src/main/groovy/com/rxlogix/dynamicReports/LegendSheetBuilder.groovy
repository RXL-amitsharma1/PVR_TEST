package com.rxlogix.dynamicReports

import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ReportTemplate
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.constant.WhenNoDataType
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import static net.sf.dynamicreports.report.builder.DynamicReports.col
import static net.sf.dynamicreports.report.builder.DynamicReports.type

/**
 * Created by gologuzov on 16.11.17.
 */
class LegendSheetBuilder {
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    private static final String COLUMN_NAME_FIELD = "columnName"
    private static final String COLUMN_LEGEND_FIELD = "columnLegend"

    void createReportLegendSheet(ExecutedReportConfiguration executedConfigurationInstance, Map params,
                                 ExecutedTemplateQuery executedTemplateQuery,
                                 ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            JRMapCollectionDataSource dataSource = createDataSource(executedConfigurationInstance, executedTemplateQuery, params.sectionsToExport)
            // If at least one legend value is not empty
            if (dataSource.getData().find {it.get(COLUMN_LEGEND_FIELD)?.trim()}) {
                ReportBuilder reportBuilder = new ReportBuilder()
                JasperReportBuilder legendSheet = reportBuilder.initializeNewReport()

                String header = customMessageService.getMessage("jasperReports.reportLegendSheet")
                HeaderBuilder headerBuilder = new HeaderBuilder()
                FooterBuilder footerBuilder = new FooterBuilder()

                headerBuilder.setHeader(executedConfigurationInstance, params, legendSheet, null, header, true)
                buildLegendSheet(legendSheet, dataSource)
                footerBuilder.setFooter(params, legendSheet, executedTemplateQuery, true)
                JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
                jasperReportBuilderEntry.jasperReportBuilder = legendSheet
                jasperReportBuilderEntry.excelSheetName = header
                jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
            }
        }
    }

    private JRMapCollectionDataSource createDataSource(ExecutedReportConfiguration executedConfigurationInstance, ExecutedTemplateQuery executedTemplateQuery, List<Long> sectionsToExport = null) {
        List<Map<String, String>> data = []
        if (executedTemplateQuery != null) {
            data.addAll(getTemplateQueryLegend(executedTemplateQuery))
        } else {
            List<ExecutedTemplateQuery> executedTemplateQueries = executedConfigurationInstance.executedTemplateQueries
            if (sectionsToExport) {
                executedTemplateQueries = executedTemplateQueries.findAll { it.id in sectionsToExport }
            }
            executedTemplateQueries.each { it ->
                data.addAll(getTemplateQueryLegend(it))
            }
        }
        return new JRMapCollectionDataSource(data.unique())
    }

    private List<Map<String, String>> getTemplateQueryLegend(ExecutedTemplateQuery executedTemplateQuery) {
        ReportTemplate executedTemplate = GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery.executedTemplate)
        List<Map<String, String>> templateQueryLegendList =  executedTemplate.getAllSelectedFieldsInfo().collect {
            [
                    (COLUMN_NAME_FIELD)  : it?.renameValue ?: customMessageService.getMessage("app.reportField.${it?.reportField?.name}"),
                    (COLUMN_LEGEND_FIELD): it?.newLegendValue ?: customMessageService.getMessage("app.reportField.${it?.reportField?.name}.label.legend")
            ]
        }
        templateQueryLegendList = excludeVersionNumber(templateQueryLegendList)

        return templateQueryLegendList
    }

    List<Map<String, String>> excludeVersionNumber(List<Map<String, String>> templateQueryLegendList) {
        templateQueryLegendList.each {
            if((it.columnName == "Version Number" && it.columnLegend == "") || it.columnLegend == "") {
                templateQueryLegendList = templateQueryLegendList - it
            }
        }
        return templateQueryLegendList
    }

    private JasperReportBuilder buildLegendSheet(JasperReportBuilder report, JRDataSource dataSource) {
        report.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
        TextColumnBuilder columnNameColumn = col.column(customMessageService.getMessage("app.label.columnName"), COLUMN_NAME_FIELD, type.stringType())
        TextColumnBuilder columnLegendColumn = col.column(customMessageService.getMessage("app.label.columnLegend"), COLUMN_LEGEND_FIELD, type.stringType())
        report.addColumn(columnNameColumn, columnLegendColumn)
        report.setDataSource(dataSource)
    }
}
