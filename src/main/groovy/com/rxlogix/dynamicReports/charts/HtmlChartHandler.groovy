package com.rxlogix.dynamicReports.charts

import com.rxlogix.ChartOptionsUtils
import net.sf.jasperreports.engine.JRGenericPrintElement
import net.sf.jasperreports.engine.export.GenericElementHtmlHandler
import net.sf.jasperreports.engine.export.JRHtmlExporterContext
import org.apache.commons.lang.StringEscapeUtils

class HtmlChartHandler implements GenericElementHtmlHandler {

    @Override
    boolean toExport(JRGenericPrintElement element) {
        return true;
    }

    @Override
    String getHtmlFragment(JRHtmlExporterContext exporterContext, JRGenericPrintElement element) {
        ChartElement chart = (ChartElement) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)
        Object chartData = chart.generateChart(false)
        if (chart.isMap) {
            ((Map) chartData).remove("plotOptions")
            ((Map) chartData).remove("legend")
            ((Map) chartData).remove("subtitle")
            ((Map) chartData).remove("xAxis")
            ((Map) chartData).remove("yAxis")
            ((Map) chartData).series.remove(((Map) chartData).series.findIndexOf {it.isPercentageColumn!=null})
        }
        StringBuffer result = new StringBuffer()
        result.append("<div id=\"${element.hashCode()}\" style=\"width:100%; min-height:400px;\" ></div>\n")
        result.append("<script>")
        result.append("var staticHtmlChartOptions = " + ChartOptionsUtils.serializeToHtml(chartData) + ";")
        result.append("\$(function () { \n    var container = \$('#${element.hashCode()}'); \n")
        result.append((chart.isMap ? "    container.highcharts('Map',staticHtmlChartOptions);" : "    container.highcharts(staticHtmlChartOptions);") +
                "});</script>")
        return result.toString()
    }
}
