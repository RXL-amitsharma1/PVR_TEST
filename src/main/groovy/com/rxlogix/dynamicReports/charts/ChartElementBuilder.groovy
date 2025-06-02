package com.rxlogix.dynamicReports.charts

import com.rxlogix.dynamicReports.reportTypes.CustomSQLReportBuilder
import net.sf.dynamicreports.report.base.AbstractScriptlet
import net.sf.dynamicreports.report.base.chart.dataset.DRCategoryChartSerie
import net.sf.dynamicreports.report.builder.chart.CategoryChartSerieBuilder
import net.sf.dynamicreports.report.builder.component.GenericElementBuilder
import net.sf.dynamicreports.report.builder.expression.Expressions
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.dynamicreports.report.definition.chart.dataset.DRIChartSerie
import net.sf.dynamicreports.report.definition.expression.DRIExpression
import net.sf.dynamicreports.report.definition.expression.DRISimpleExpression
import org.apache.commons.lang3.Validate
import java.util.regex.Matcher
/**
 * Created by gologuzov on 25.11.15.
 */
class ChartElementBuilder extends GenericElementBuilder {
    private static final String NAMESPACE = "http://www.rxlogix.com/customElements"
    private static final String NAME = "highcharts"

    private ChartElement chart

    private DRISimpleExpression<String> titleExpression
    private DRISimpleExpression<String> yAxisTitleExpression
    private DRISimpleExpression<String> categoryExpression
    private List<DRCategoryChartSerie> series
    private ReportScriptlet scriptlet
    private Map specialSettings

    ChartElementBuilder(def options, String latestComment,Map specialSettings) {
        super(NAMESPACE, NAME)
        setHeight(200)
        scriptlet = new ReportScriptlet()
        series = new ArrayList<DRIChartSerie>()
        chart = new ChartElement(options, latestComment)
        addParameter(ChartGenerator.PARAMETER_CHART_GENERATOR, chart)
        this.specialSettings=specialSettings
    }

    ChartElementBuilder setExportAsImage() {
        addParameter(ChartGenerator.PARAMETER_EXPORT_AS_IMAGE, true)
        return this;
    }

    ChartElementBuilder setTitle(String title) {
        Validate.notNull(title, "Chart title must not be null")
        this.titleExpression = Expressions.text(title)
        return this;
    }

    ChartElementBuilder setTitle(DRIExpression<String> title) {
        Validate.notNull(title, "Chart title must not be null")
        this.titleExpression = title
        return this
    }

    ChartElementBuilder setYAxisTitle(String title) {
        Validate.notNull(title, "Y axis title must not be null")
        this.yAxisTitleExpression = Expressions.text(title)
        return this
    }

    ChartElementBuilder setYAxisPercentageTitle(String setYAxisPercentageTitle) {
        chart.yAxisPercentageTitle = setYAxisPercentageTitle
        return this
    }

    ChartElementBuilder setYAxisTitle(DRIExpression<String> title) {
        Validate.notNull(title, "Y axis title must not be null")
        this.yAxisTitleExpression = title
        return this
    }

    ChartElementBuilder setShowPercentages(Boolean showPercentages) {
        chart.setShowPercentages(showPercentages)
        return this
    }

    ChartElementBuilder setMapType(Boolean isMap) {
        chart.isMap = isMap
        return this
    }

    ChartElementBuilder setCategory(DRIExpression<String> expression) {
        Validate.notNull(expression, "expression must not be null")
        this.categoryExpression = expression
        return this
    }

    ChartElementBuilder addSerie(CategoryChartSerieBuilder serie) {
        Validate.notNull(serie, "serie must not be null")
        this.series.add(serie.build())
        return this
    }

    ChartElementBuilder setShowLegend(Boolean showLegend) {
        chart.setShowLegend(showLegend)
        return this
    }

    ChartElementBuilder setChartRowsCount(int rowsCount) {
        addParameter(ChartGenerator.PARAMETER_CHART_ROWS_COUNT, rowsCount)
        return this
    }

    ChartElementBuilder setTotalRowIndices(List indices) {
        addParameter(ChartGenerator.PARAMETER_TOTAL_ROW_INDICES, indices)
        return this
    }

    ChartElementBuilder setDrillDownUrlTemplate(String urlTemplate) {
        chart.drillDownUrlTemplate = urlTemplate
        return this
    }

    ReportScriptlet getScriptlet() {
        return scriptlet
    }

    private class ReportScriptlet extends AbstractScriptlet {
        @Override
        void afterReportInit(ReportParameters reportParameters) {
            chart.setTitle(titleExpression.evaluate(reportParameters))
            chart.setYAxisTitle(yAxisTitleExpression.evaluate(reportParameters))
            chart.setYAxisPercentageTitle(chart.yAxisPercentageTitle)
            series.each {
                if (specialSettings) {
                    String code = it.valueExpression.component.valueExpression.name
                    if (code.contains(CustomSQLReportBuilder.CHART_COLUMN_PREFIX)) { //Custom SQL
                        String type = (specialSettings?.get(code))
                        String label = (specialSettings?.get(code + "_label"))
                        boolean isPercentageColumn = code.contains(CustomSQLReportBuilder.CHART_COLUMN_P_PREFIX)
                        if (type || isPercentageColumn) {
                            chart.addSerie(CustomSQLReportBuilder.getColumnLabel(code), type, isPercentageColumn, label)
                            return
                        }
                    } else {//data tabulation
                        Matcher matcher = (code =~ /(\w+)_(\d+)_(\w+)/)
                        String key = matcher.matches() ? code.split("_")[2] : null
                        String type = key ? (specialSettings?.get(key)) : null
                        String label = key ? (specialSettings?.get(key + "_label")) : null
                        //identify percentage values to link them to percentage axis
                        boolean isPercentageColumn = matcher.matches() && matcher.groupCount() > 2 && matcher.group(3).startsWith("P") && !(matcher.group(3).startsWith("PA") && !type)
                        if ((type || isPercentageColumn) && (type != "hide")) {
                            chart.addSerie(it.labelExpression.value, type, isPercentageColumn, label)
                            return
                        }
                    }
                }
                chart.addSerie(it.labelExpression.value)
            }
        }

        @Override
        void afterDetailEval(ReportParameters reportParameters) {
            super.afterDetailEval(reportParameters)
            List<String> keys = categoryExpression.evaluate(reportParameters)
            series.each {
                Long rowId
                try {
                    rowId = reportParameters.getValue("ID")
                } catch (Exception e) {
                    // Do nothing. Report does not have the ID field
                }
                String columnName = it.valueExpression.name
                Number value = reportParameters.getValue(columnName)
                chart.addValue(it.labelExpression.value, keys, value, columnName, rowId)
            }
        }
    }
}
