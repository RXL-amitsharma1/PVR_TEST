package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.Chart
import com.rxlogix.dynamicReports.charts.ooxml.ChartAxis
import com.rxlogix.dynamicReports.charts.ooxml.ChartColorTheme
import com.rxlogix.dynamicReports.charts.ooxml.ChartDataSource

/**
 * A base for all charts data types.
 */
trait ChartData {
    /**
     * Chart data color theme
     */
    ChartColorTheme colorTheme

    /**
     * Chart options
     */
    def options

    /**
     * Category axis indices
     */
    Set<Integer> categoryAxisIndices = new HashSet<>()

    /**
     * Value axis indices
     */
    Set<Integer> valueAxisIndices = new HashSet<>()

    /**
     * List of all data series.
     */
    List<? extends ChartSeries> series

    abstract ChartSeries addSeries(ChartDataSource<?> categoryAxisData, ChartDataSource<? extends Number> values, int seriesId)

    /**
     * Fills a charts with data specified by implementation.
     *
     * @param chart a charts to fill in
     * @param categoryAxes chart category axes to use
     * @param valueAxes chart value axes to use
     */
    abstract void fillChart(Chart chart)

    void setPercentageLabel(dataLabels) {
        if (options?.series?.get(series[0].id)?.isPercentageColumn) {
            dataLabels.addNewNumFmt();
            dataLabels.getNumFmt().setSourceLinked(false);
            dataLabels.getNumFmt().setFormatCode("#0.00\\%");
        }
    }
}
