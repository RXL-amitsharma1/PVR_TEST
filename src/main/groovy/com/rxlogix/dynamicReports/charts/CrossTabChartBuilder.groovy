package com.rxlogix.dynamicReports.charts

import com.rxlogix.ChartOptionsUtils
import com.rxlogix.TemplateService
import com.rxlogix.config.*
import com.rxlogix.dynamicReports.FooterBuilder
import com.rxlogix.dynamicReports.HeaderBuilder
import com.rxlogix.dynamicReports.JasperReportBuilderEntry
import com.rxlogix.dynamicReports.ReportBuilder
import com.rxlogix.dynamicReports.reportTypes.CrosstabReportBuilder
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.FieldBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.jasperreports.engine.design.JRDesignField
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.util.regex.Matcher
import java.util.zip.GZIPInputStream

import static com.rxlogix.dynamicReports.charts.ChartBuilder.TotalFilterExpression
import static net.sf.dynamicreports.report.builder.DynamicReports.field
import static net.sf.dynamicreports.report.builder.DynamicReports.field
import static net.sf.dynamicreports.report.builder.DynamicReports.field
import static net.sf.dynamicreports.report.builder.DynamicReports.type

class CrossTabChartBuilder implements ChartBuilder {

    def commentService = Holders.applicationContext.getBean("commentService")

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

    @Override
    JasperReportBuilder buildChartSheet(ReportResult reportResult, String chartTitle, Map params) {
        if (!reportResult.data || !reportResult.data.value) {
            return null
        }
        ReportBuilder reportBuilder = new ReportBuilder()
        JasperReportBuilder report = reportBuilder.initializeNewReport(reportResult, true)
        JSONArray topHeaders = reportBuilder.getHeaderForTopNColumns(reportResult, report.getDataSource())

        List<FieldBuilder> rowFields
        List<FieldBuilder> columnFields
        List<TextColumnBuilder> rows
        List<TextColumnBuilder> columns
        DataTabulationTemplate executedTemplate = (DataTabulationTemplate) (reportResult.template ?: reportResult.executedTemplateQuery.executedTemplate)
        String chartCustomOptions = executedTemplate.chartCustomOptions
        Map specialSettings = setSpecialChartSettings(executedTemplate)
        (rowFields, columnFields, rows, columns) = parseFields(reportResult, specialSettings, topHeaders)
        report.addField(*rowFields)
        report.addField(*columnFields)
        report.addField(field(CrosstabReportBuilder.ROW_ID_FIELD_NAME, type.longType()))
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
        if (chartCustomOptions) {
            chartOptions = ChartOptionsUtils.deserialize(chartCustomOptions, chartOptions)
            // Clean chart series from template. They may be not empty if user chosen Combination charts
            chartOptions.series = []
        } else {
            chartOptions = ChartOptionsUtils.deserialize(templateService.getChartDefaultOptions(), chartOptions)
        }
        def yAxisTitle = getMeasuresList(reportResult).findAll { it }.join(", ")
        def yAxisPercentageTitle = getPercentageMeasuresList(reportResult).findAll { it }.join(", ")
        String latestComment = commentService.getReportResultChartAnnotation(reportResult.getId())
        chart = addChart(chartOptions, chartTitle, yAxisTitle, yAxisPercentageTitle, rows, columns, report, latestComment, specialSettings)
        if (chartOptions.chart.type == "pie") {
            chart.setShowPercentages(true)
            chartOptions.legend.borderWidth = 0
        }
        if (executedTemplate.chartExportAsImage) chart.setExportAsImage()
        chart.setTotalRowIndices(filter.totalRowIndices)
        chart.setFixedHeight(415)
        if (reportResult.executedTemplateQuery.usedTemplate.drillDownToCaseList
                && (!params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name())) {
            String url = grailsLinkGenerator.link(
                    absolute: true,
                    controller: 'caseSeries',
                    action: 'previewCrosstabCases',
                    id: reportResult.id)
            chart.setDrillDownUrlTemplate(url)
        }
        if ((GrailsHibernateUtil.unwrapIfProxy(reportResult.executedTemplateQuery.executedTemplate) instanceof ExecutedDataTabulationTemplate) && reportResult.executedTemplateQuery.executedTemplate.worldMap) {
            chart.setMapType(true)
            String contLabel = TemplateService.getMapCountLabel(reportResult)
            def data = TemplateService.fetchDataForMap(reportResult?.data?.getDecryptedValue())
            // chartOptions.clear()
            // chartOptions.plotOptions=null
            chartOptions.chart = [map: 'custom/world']
            //  title: [text: null],
            //  subtitle: [ text: ''],
            def colorAxis = [min: 0]
            if (reportResult.executedTemplateQuery?.executedTemplate?.worldMapConfig) {
                try {
                    colorAxis = JSON.parse(reportResult.executedTemplateQuery?.executedTemplate?.worldMapConfig)
                } catch (Exception e) {
                    log.error("Error parsing worldMapConfig", e)
                }
            }
            chartOptions.colorAxis = colorAxis
            chartOptions.series = [[
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

        }
        report.addSummary(chart)
        return report
    }

    static Map setSpecialChartSettings(DataTabulationTemplate executedTemplate) {
        Map specialSettings = [:]
        executedTemplate.columnMeasureList.eachWithIndex { col, ind ->
            col.measures.each { measure ->
                if (measure.valuesChartType) {
                    specialSettings.put(measure.type.code + "1" + (ind + 1), measure.valuesChartType)
                }
                if (measure.percentageChartType) {
                    specialSettings.put("P" + measure.type.code[1] + "1" + (ind + 1), measure.percentageChartType)
                    specialSettings.put("P" + measure.type.code[1] + "1" + (ind + 1) + "_label", measure.percentageAxisLabel)
                }
                if (measure.type == MeasureTypeEnum.COMPLIANCE_RATE) {
                    specialSettings.put("P" + measure.type.code[1] + "1" + (ind + 1) + "_label", measure.percentageAxisLabel)
                }
            }
        }
        return specialSettings
    }

    @Override
    List parseFields(ReportResult reportResult, Map specialSettings, JSONArray topHeaders) {
        JSONArray tabHeaders = topHeaders?:(JSONArray) JSON.parse(reportResult.data.crossTabHeader)

        List<FieldBuilder> rowFields = []
        List<FieldBuilder> columnFields = []
        List<TextColumnBuilder> rows = []
        List<TextColumnBuilder> columns = []

        for (JSONObject header : tabHeaders) {
            String name = header.entrySet().getAt(0).key
            String label = header.entrySet().getAt(0).value
            // Remove line breaks from column header
            label = label.replaceAll("[\\r\\n]+", "")
            boolean isCaseListColumn = name.startsWith("CASE_LIST")
            boolean isTotalCaseCount = name.startsWith("CASE_COUNT")
            boolean isIntervalCaseCount = name.startsWith("INTERVAL_CASE_COUNT")
            Matcher matcher = (name =~ /(\w+)_(\d+)_(\w+)/)
            String specialSetting = matcher.matches() ? (specialSettings?.get(name.split("_")[2])) : null
            //identify percentage values to link them to percentage axis
            boolean isPercentageColumn = matcher.matches() && matcher.groupCount() > 2 && matcher.group(3).startsWith("P") && !(matcher.group(3).startsWith("PA") && !specialSetting)

            if (name.substring(0, 3).equalsIgnoreCase("ROW")) {
                FieldBuilder field = field(name, type.stringType())
                rowFields.add(field)
                rows.add(Columns.column(label, field))
            } else if (!isCaseListColumn && !isTotalCaseCount && !isIntervalCaseCount && !isPercentageColumn && !specialSetting) {
                FieldBuilder field = field(name, type.integerType())
                columnFields.add(field)
                columns.add(Columns.column(label, field))
            } else if (specialSetting) {
                if (specialSetting != "hide") {
                    FieldBuilder field = field(name, type.doubleType())
                    columnFields.add(field)
                    columns.add(Columns.column(label, field))
                }
            }
        }
        [rowFields, columnFields, rows, columns]
    }

    static List getMeasuresList(ReportResult reportResult) {
        def result = []
        ExecutedDataTabulationTemplate template = reportResult.executedTemplateQuery.executedTemplate
        template.columnMeasureList.each {columnMeasure ->
            columnMeasure.measures.each {measure ->
                if (!(measure.type == com.rxlogix.reportTemplate.MeasureTypeEnum.COMPLIANCE_RATE && measure.valuesChartType) && (measure.valuesChartType!="hide"))
                    result.add(measure.valueAxisLabel == null ? measure.name : measure.valueAxisLabel.trim())
            }
        }
        return result.unique()
    }

    static List getPercentageMeasuresList(ReportResult reportResult) {
        List percentageLabels = []
        ExecutedDataTabulationTemplate template = reportResult.executedTemplateQuery.executedTemplate
        template.columnMeasureList.each { columnMeasure ->
            columnMeasure.measures.each { measure ->
                if ((measure.percentageOption != com.rxlogix.reportTemplate.PercentageOptionEnum.NO_PERCENTAGE) && measure.percentageChartType && (measure.percentageChartType != "hide"))
                    percentageLabels << (measure.percentageAxisLabel ?: (measure.name + " Percentage"))
                if ((measure.type == com.rxlogix.reportTemplate.MeasureTypeEnum.COMPLIANCE_RATE) && measure.valuesChartType && (measure.valuesChartType != "hide")) {
                    percentageLabels << (measure.percentageAxisLabel ?: measure.name)
                }
            }
        }

        return percentageLabels.unique()
    }
}