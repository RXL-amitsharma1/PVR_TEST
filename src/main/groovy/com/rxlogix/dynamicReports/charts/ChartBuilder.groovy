package com.rxlogix.dynamicReports.charts

import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.TemplateService
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ReportResult
import com.rxlogix.dynamicReports.JasperReportBuilderEntry
import com.rxlogix.util.ViewHelper
import grails.util.Holders
import grails.web.mapping.LinkGenerator
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.builder.FieldBuilder
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.definition.ReportParameters
import org.grails.web.json.JSONArray

import static net.sf.dynamicreports.report.builder.DynamicReports.cht

/**
 * Created by gologuzov on 10.11.17.
 */
trait ChartBuilder {
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    TemplateService templateService = Holders.applicationContext.getBean("templateService")
    LinkGenerator grailsLinkGenerator = Holders.applicationContext.getBean("grailsLinkGenerator")

    abstract void createChart(ExecutedReportConfiguration executedConfiguration,ReportResult reportResult,
                                   Map params, ExecutedTemplateQuery executedTemplateQuery,
                                   ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList)

    abstract List parseFields(ReportResult reportResult, Map specialSettings, JSONArray topHeaders)

    abstract JasperReportBuilder buildChartSheet(ReportResult reportResult, String chartTitle, Map params)

    ChartElementBuilder addChart(
            def options, String chartTitle, String yAxisTitle, String yAxisPercentageTitle, List<TextColumnBuilder> rows,
            List<TextColumnBuilder> columns, JasperReportBuilder report, String latestComment, Map specialSettings) {
        ChartElementBuilder chart = new ChartElementBuilder(options, latestComment, specialSettings)
                .setTitle(chartTitle)
                .setYAxisTitle(yAxisTitle)
                .setYAxisPercentageTitle(yAxisPercentageTitle)
                .setShowLegend(true)
                .setCategory(new CategoryExpression(rows))
                .setChartRowsCount(rows.size())
        columns.each {
            chart.addSerie(cht.serie(it))
        }
        report.scriptlets(chart.scriptlet)
        return chart
    }

    static class TotalFilterExpression extends AbstractSimpleExpression<Boolean> {
        private List<FieldBuilder> rowFields

        def totalRowIndices = []

        TotalFilterExpression(List<FieldBuilder> rowFields) {
            this.rowFields = rowFields
        }

        @Override
        Boolean evaluate(ReportParameters reportParameters) {
            def isTotalRow = rowFields.find {
                Object value = reportParameters.getValue(it.name)
                value instanceof String && ("Total".equals(value) || "Subtotal".equals(value) || "Sub Total".equals(value) || "総計".equals(value)|| "小計".equals(value))
            }
            if (isTotalRow) {
                totalRowIndices.add(reportParameters.columnRowNumber + totalRowIndices.size())
            }
            return !isTotalRow
        }
    }

    static class CategoryExpression extends AbstractSimpleExpression <List<String>> {
        private List<TextColumnBuilder> rows

        CategoryExpression(List<TextColumnBuilder> rows) {
            this.rows = rows
        }

        @Override
        List<String> evaluate(ReportParameters reportParameters) {
            return rows.collect {
                String value = reportParameters.getFieldValue(it.name)
                value == null ? ViewHelper.getEmptyLabel() : value
            }
        }
    }
}