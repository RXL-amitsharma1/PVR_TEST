package com.rxlogix.dynamicReports.charts

import com.rxlogix.dynamicReports.charts.ooxml.Chart
import com.rxlogix.dynamicReports.charts.ooxml.ChartColorTheme
import com.rxlogix.dynamicReports.charts.ooxml.ChartDataSource
import com.rxlogix.dynamicReports.charts.ooxml.ChartUtil
import com.rxlogix.dynamicReports.charts.ooxml.data.CategoryDataSource
import com.rxlogix.dynamicReports.charts.ooxml.data.ChartData
import com.rxlogix.dynamicReports.charts.ooxml.data.ChartSeries
import com.rxlogix.dynamicReports.charts.ooxml.data.ValueDataSource
import com.rxlogix.util.MiscUtil
import net.sf.jasperreports.engine.export.ooxml.BaseHelper
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import org.apache.poi.ss.util.CellReference

/**
 * Created by gologuzov on 04.02.16.
 */
class XlsxChartHelper extends BaseHelper {
    private def options
    private int chartRowsCount
    private def totalRowIndices
    private JRXlsxExporter exporter

    private Chart chart

    XlsxChartHelper(JRXlsxExporter exporter, Writer writer, def options, int chartRowsCount, List totalRowIndicies) {
        super(exporter.jasperReportsContext, writer)
        this.exporter = exporter
        this.options = options
        this.chartRowsCount = chartRowsCount ? chartRowsCount : 1
        this.totalRowIndices = totalRowIndicies
        createChart()
    }

    private void createChart() {
        chart = new Chart(options)
        Collection<ChartData> chartDataCollection = chart.getChartDataFactory().createChartDataCollection(options)
        for (ChartData data : chartDataCollection) {
            chart.plot(data)
        }
    }

    //references from chart to table are not working properly, so using
    // chart.getChartDataFactory().createChartDataCollection(options)  instead of this method
    //leaving it here as probably we may need interactive chart in excel in future in some cases
    private Collection<ChartData> createChartDataCollection(options) {
        Map<String, ChartData> chartDataMap = new LinkedHashMap<>()

        def dataSheetName = getDataSheetName()
        char columnPrefix = 0
        char columnIndex = 'A'
        String firstCategoryColumn = (columnPrefix == 0 ? "" : String.valueOf(columnPrefix)) + String.valueOf(columnIndex)
        String lastCategoryColumn = (columnPrefix == 0 ? "" : String.valueOf(columnPrefix)) + String.valueOf((char) (columnIndex + chartRowsCount - 1))
        ChartColorTheme colorTheme = new ChartColorTheme(options.colors)

        for (int i =0; i < options.series.size(); i++) {
            def series = options.series[i]
            def chartType = series.type ?: options.chart.type
            int categoryAxisIndex = series.xAxis ?: 0
            int valueAxisIndex = series.yAxis ?: 0

            String key = "${chartType}_${categoryAxisIndex}_${valueAxisIndex}"
            ChartData data = chartDataMap.get(key)
            if (data == null) {
                data = chart.getChartDataFactory().createChartData(options, chartType, colorTheme)
                chartDataMap.put(key, data)
            }
            String valueColumn = (columnPrefix == 0 ? "" : String.valueOf(columnPrefix)) + String.valueOf((char) (columnIndex + chartRowsCount))
            ChartDataSource<Number> valueDS
            ChartDataSource<String> categoryDS
            valueDS = new ValueDataSource(series.data, dataSheetName, valueColumn, totalRowIndices)
            categoryDS = new CategoryDataSource(options.xAxis[0].categories, dataSheetName, firstCategoryColumn, lastCategoryColumn, totalRowIndices)
            ChartSeries chartSeries = data.addSeries(categoryDS, valueDS, i)
            chartSeries.setTitle(new CellReference(dataSheetName, 2, CellReference.convertColStringToIndex(valueColumn), true, true))

            if (columnIndex == 'Z') {
                if (columnPrefix == 0) {
                    columnPrefix = 'A'
                } else {
                    columnPrefix++
                }
                columnIndex = 'A'
            } else {
                columnIndex++
            }
            data.categoryAxisIndices.add(categoryAxisIndex)
            data.valueAxisIndices.add(valueAxisIndex)
        }
        return chartDataMap.values()
    }

    void exportChart() {
        this.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        this.write(chart.toString())
        this.flush()
    }

    private String getDataSheetName() {
        // A sheet with the same name like chart/report title
        return getSheetName(MiscUtil.escapeExcelSheetName(options.title?.text)).trim()
    }

    /**
     * A bit changed method from JRXlsAbstractExporter
     * @param sheetName Original sheet name
     * @return optimized sheet name with index if repeated, etc
     */
    private String getSheetName(String sheetName) {
        if (exporter.sheetNames != null && exporter.sheetNamesIndex < exporter.sheetNames.length) {
            sheetName = exporter.sheetNames[exporter.sheetNamesIndex];
        }

        if (sheetName == null) {
            return "Page " + (exporter.sheetIndex + 1);
        } else {
            int crtIndex = Integer.valueOf(1).intValue();
            String txtIndex = "";
            String validSheetName = sheetName.length() < 31 ? sheetName : sheetName.substring(0, 30);
            if (exporter.sheetNamesMap.containsKey(validSheetName)) {
                crtIndex = ((Integer) exporter.sheetNamesMap.get(validSheetName)).intValue() + 1;
                txtIndex = String.valueOf(crtIndex);
            }

            //this.sheetNamesMap.put(validSheetName, Integer.valueOf(crtIndex));
            String name = sheetName;
            if (txtIndex.length() > 0) {
                name = sheetName + " " + txtIndex;
            }

            if (name.length() > 30) {
                name = (sheetName + " ").substring(0, 30 - txtIndex.length()) + txtIndex;
            }

            return name;
        }
    }
}
