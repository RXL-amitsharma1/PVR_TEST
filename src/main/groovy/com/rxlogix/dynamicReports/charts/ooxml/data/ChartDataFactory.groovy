package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.ChartColorTheme
import com.rxlogix.dynamicReports.charts.ooxml.ChartUtil

class ChartDataFactory {

    private static ChartDataFactory instance;

    private ChartDataFactory() {}

    Collection<ChartData> createChartDataCollection(options) {
        Map<String, ChartData> chartDataMap = new LinkedHashMap<>()
        ChartColorTheme colorTheme = new ChartColorTheme(options.colors)
        for (int i = 0; i < options.series.size(); i++) {
            def series = options.series[i]
            def chartType = series.type ?: options.chart.type
            int categoryAxisIndex = series.xAxis ?: 0
            int valueAxisIndex = series.yAxis ?: 0

            String key = "${chartType}_${categoryAxisIndex}_${valueAxisIndex}"
            ChartData data = chartDataMap.get(key)
            if (data == null) {
                data = createChartData(options, chartType, colorTheme)
                chartDataMap.put(key, data)
            }
            ChartSeries chartSeries
            if (ChartUtil.ifChartInverted(options)) {
                chartSeries = data.addSeries(new CategoryDataSource(ChartUtil.reverseCategories(options.xAxis[0].categories)), new ValueDataSource(series.data.reverse()), i)
            } else {
                chartSeries = data.addSeries(new CategoryDataSource(options.xAxis[0].categories), new ValueDataSource(series.data), i)
            }
            chartSeries.setTitle(series.name)

            data.categoryAxisIndices.add(categoryAxisIndex)
            data.valueAxisIndices.add(valueAxisIndex)
        }
        return chartDataMap.values()
    }

    ChartData createChartData(def options, String chartType, ChartColorTheme colorTheme) {
        switch (chartType) {
        // Supported chart types
            case "area":
            case "arearange":
            case "areaspline":
                return new AreaChartData(options, colorTheme)
            case "bar":
            case "column":
            case "columnrange":
                if (options.chart.options3d?.enabled) {
                    return new Bar3DChartData(options, colorTheme)
                } else {
                    return new BarChartData(options, colorTheme)
                }
            case "line":
            case "spline":
                return new LineChartData(options, colorTheme)
            case "pie":
                if (options.plotOptions.pie?.innerSize) {
                    return new DoughnutChartData(options, colorTheme)
                } else if (options.chart.options3d?.enabled) {
                    return new Pie3DChartData(options, colorTheme)
                } else {
                    return new PieChartData(options, colorTheme)
                }
            case "bubble":
                return new BubbleChartData(options, colorTheme)
            case "scatter":
                return new ScatterChartData(options, colorTheme)
        // Not supported chart types
            case "boxplot":
            case "errorbar":
            case "funnel":
            case "gauge":
            case "heatmap":
            case "polygon":
            case "pyramid":
            case "series":
            case "solidgauge":
            case "treemap":
            case "waterfall":
                break
        }
        return new BarChartData(options, colorTheme)
    }

    /**
     * @return factory instance
     */
    static ChartDataFactory getInstance() {
        if (instance == null) {
            instance = new ChartDataFactory();
        }
        return instance;
    }
}
