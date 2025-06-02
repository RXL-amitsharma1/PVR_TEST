package com.rxlogix.dynamicReports.charts

import com.rxlogix.dynamicReports.charts.ooxml.Chart
import com.rxlogix.dynamicReports.charts.ooxml.data.ChartData
import net.sf.jasperreports.engine.export.ooxml.BaseHelper
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter

/**
 * Created by gologuzov on 04.02.16.
 */
class PptxChartHelper extends BaseHelper {
    private def options
    private int chartRowsCount
    private JRPptxExporter exporter

    private Chart chart

    PptxChartHelper(JRPptxExporter exporter, Writer writer, def options, int chartRowsCount) {
        super(exporter.jasperReportsContext, writer)
        this.exporter = exporter
        this.options = options
        this.chartRowsCount = chartRowsCount ? chartRowsCount : 1
        createChart()
    }

    private void createChart() {
        chart = new Chart(options)
        Collection<ChartData> chartDataCollection = chart.getChartDataFactory().createChartDataCollection(options)
        for (ChartData data : chartDataCollection) {
            chart.plot(data)
        }
    }

    void exportChart() {
        this.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        this.write(chart.toString())
        this.flush()
    }
}
