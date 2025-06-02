package com.rxlogix.dynamicReports.charts

import com.rxlogix.ChartOptionsUtils
import com.rxlogix.config.*
import com.rxlogix.dynamicReports.FooterBuilder
import com.rxlogix.dynamicReports.HeaderBuilder
import com.rxlogix.dynamicReports.JasperReportBuilderEntry
import com.rxlogix.dynamicReports.ReportBuilder
import com.rxlogix.dynamicReports.reportTypes.CustomSQLReportBuilder
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.FieldBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import org.grails.web.json.JSONArray

import static net.sf.dynamicreports.report.builder.DynamicReports.*
import static com.rxlogix.dynamicReports.charts.ChartBuilder.TotalFilterExpression

class NonCaseSQLChartBuilder implements ChartBuilder {

    @Override
    void createChart(ExecutedReportConfiguration executedConfiguration, ReportResult reportResult, Map params,
                                    ExecutedTemplateQuery executedTemplateQuery,
                                    ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        String chartTitle = ViewHelper.getReportTitle(executedConfiguration, executedTemplateQuery)
        JasperReportBuilder chartSheet = buildChartSheet(reportResult, chartTitle, params)

        if (chartSheet) {
            String header = customMessageService.getMessage("jasperReports.chartSheet")
            if (dynamicReportService.isInPrintMode(params)) {
                HeaderBuilder headerBuilder = new HeaderBuilder()
                FooterBuilder footerBuilder = new FooterBuilder()

                headerBuilder.setHeader(executedConfiguration, params, chartSheet, executedTemplateQuery, header, true)
                footerBuilder.setFooter(params, chartSheet, executedTemplateQuery, true)
            }

            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = chartSheet
            jasperReportBuilderEntry.excelSheetName = header
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    static Map setSpecialChartSettings(NonCaseSQLTemplate executedTemplate) {
        Map specialSettings = [:]
        if (executedTemplate.specialChartSettings) {
            JSONArray columnNamesList = JSON.parse(executedTemplate.columnNamesList)
            Map specialSettingsJson = [:]
            JSON.parse(executedTemplate.specialChartSettings).each {
                specialSettingsJson.put(it.key.trim().toUpperCase(), it.value)
            }
            for (String columnName : columnNamesList) {
                String key = columnName.toUpperCase()
                def isChartColumn = columnName.contains(CustomSQLReportBuilder.CHART_COLUMN_PREFIX)
                if (isChartColumn) {
                    if (specialSettingsJson.get(key).type)
                        specialSettings.put(columnName, specialSettingsJson.get(key).type)
                    specialSettings.put(columnName + "_label", specialSettingsJson.get(key).label)
                }
            }
        }
        return specialSettings
    }

    @Override
    JasperReportBuilder buildChartSheet(ReportResult reportResult, String chartTitle, Map params) {
        if (!reportResult.data || !reportResult.data.value) {
            return null
        }
        ReportBuilder reportBuilder = new ReportBuilder()
        JasperReportBuilder report = reportBuilder.initializeNewReportCSV(reportResult, reportResult.executedTemplateQuery.executedTemplate, [chart: true])

        List<FieldBuilder> rowFields
        List<FieldBuilder> columnFields
        List<TextColumnBuilder> rows
        List<TextColumnBuilder> columns
        (rowFields, columnFields, rows, columns) = parseFields(reportResult, setSpecialChartSettings(reportResult.executedTemplateQuery.executedTemplate), null)
        report.addField(*rowFields)
        report.addField(*columnFields)
        TotalFilterExpression filter = new TotalFilterExpression(rowFields)
        report.setFilterExpression(filter)

        ComponentBuilder chart
        def chartOptions = [
                chart      : [:],
                legend     : [:],
                plotOptions: [
                        series: [
                                dataLabels:[:]
                        ]
                ]
        ]
        NonCaseSQLTemplate template = (NonCaseSQLTemplate) reportResult.executedTemplateQuery.executedTemplate
        String chartCustomOptions = template.chartCustomOptions

        if (chartCustomOptions) {
            chartOptions = ChartOptionsUtils.deserialize(chartCustomOptions, chartOptions)
            // Clean chart series from template. They may be not empty if user chosen Combination charts
            chartOptions.series = []
        } else {
            chartOptions = ChartOptionsUtils.deserialize(templateService.getChartDefaultOptions(), chartOptions)
        }
        Map specialChartSettings = setSpecialChartSettings(template)
        def yAxisTitle = getMeasuresList(reportResult, specialChartSettings).join(", ")
        def yAxisPercentageTitle = getPercentageMeasuresList(reportResult, specialChartSettings).findAll { it }.join(", ")
        chart = addChart(chartOptions, chartTitle, yAxisTitle, yAxisPercentageTitle, rows, columns, report, null, specialChartSettings)
        if (template.chartExportAsImage) chart.setExportAsImage()
        if (chartOptions.chart.type == "pie") {
            chart.setShowPercentages(true)
            chartOptions.legend.borderWidth = 0
        }
        chart.setTotalRowIndices(filter.totalRowIndices)
        chart.setFixedHeight(415)
        report.addSummary(chart)
        return report
    }

    @Override
    List parseFields(ReportResult reportResult, Map specialSettings, JSONArray topHeaders) {
        ExecutedNonCaseSQLTemplate template = reportResult.executedTemplateQuery.executedTemplate
        JSONArray columnNamesList = JSON.parse(template.columnNamesList)

        List<FieldBuilder> rowFields = []
        List<FieldBuilder> columnFields = []
        List<TextColumnBuilder> rows = []
        List<TextColumnBuilder> columns = []

        for (String columnName : columnNamesList) {
            String name = columnName
            String label = CustomSQLReportBuilder.getColumnLabel(columnName)
            def isCaseListColumn = name.startsWith("CASE_LIST")
            def isTotalCaseCount = name.startsWith("CASE_COUNT")
            def isIntervalCaseCount = name.startsWith("INTERVAL_CASE_COUNT")

            if (!name.contains(CustomSQLReportBuilder.CHART_COLUMN_PREFIX)) {
                FieldBuilder field = field(name, type.stringType())
                rowFields.add(field)
                rows.add(Columns.column(label, field))
            } else if (!isCaseListColumn && !isTotalCaseCount && !isIntervalCaseCount) {
                FieldBuilder field = (name.contains(CustomSQLReportBuilder.CHART_COLUMN_P_PREFIX) ? field(name, type.doubleType()) : field(name, type.integerType()))
                columnFields.add(field)
                columns.add(Columns.column(label, field))
            }
        }
        [rowFields, columnFields, rows, columns]
    }

    static List getMeasuresList(ReportResult reportResult, Map specialSettings) {
        def result = []
        ExecutedNonCaseSQLTemplate template = reportResult.executedTemplateQuery.executedTemplate
        JSONArray columnNamesList = JSON.parse(template.columnNamesList)
        for (String columnName : columnNamesList) {
            if (columnName.contains(CustomSQLReportBuilder.CHART_COLUMN_PREFIX) && !columnName.contains(CustomSQLReportBuilder.CHART_COLUMN_P_PREFIX)) {
                String label = specialSettings?.get(columnName + "_label")
                if (label != null)
                    result.add(label)
                else
                    result.add(CustomSQLReportBuilder.getColumnLabel(columnName))
            }
        }
        return result.unique().findAll { it }
    }

    static List getPercentageMeasuresList(ReportResult reportResult, Map specialSettings) {
        def result = []
        ExecutedNonCaseSQLTemplate template = reportResult.executedTemplateQuery.executedTemplate
        JSONArray columnNamesList = JSON.parse(template.columnNamesList)
        for (String columnName : columnNamesList) {
            if (columnName.contains(CustomSQLReportBuilder.CHART_COLUMN_P_PREFIX)) {
                String label = specialSettings?.get(columnName + "_label")
                if (label != null)
                    result.add(label)
                else
                    result.add(CustomSQLReportBuilder.getColumnLabel(columnName))
            }
        }
        return result.unique().findAll { it }
    }
}