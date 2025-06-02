package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.*
import org.openxmlformats.schemas.drawingml.x2006.chart.*
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties

/**
 * Holds data for a Bar Chart
 */
class Bar3DChartData implements ChartData {
    Bar3DChartData(def options, ChartColorTheme colorTheme) {
        this.options = options
        this.colorTheme = colorTheme
        this.series = new ArrayList<Series>()
    }

    static class Series extends AbstractChartSeries {

        protected Series(int id, int order,
                         ChartDataSource<?> categories,
                         ChartDataSource<? extends Number> values,
                         ChartColorTheme colorTheme) {
            super(id, order, categories, values, colorTheme)
        }

        protected void addToChart(CTBar3DChart ctBarChart) {
            CTBarSer ctBarSer = ctBarChart.addNewSer()
            ctBarSer.addNewIdx().setVal(id)
            ctBarSer.addNewOrder().setVal(order)

            CTAxDataSource catDS = ctBarSer.addNewCat()
            ChartUtil.buildAxDataSource(catDS, categories)
            CTNumDataSource valueDS = ctBarSer.addNewVal()
            ChartUtil.buildNumDataSource(valueDS, values)

            if (isTitleSet()) {
                addCTSerTx(ctBarSer.addNewTx())
            }
            CTShapeProperties shapeProperties = ctBarSer.addNewSpPr()
            shapeProperties.addNewSolidFill().addNewSrgbClr().setVal(colorTheme.nextColorBytes())
            CTLineProperties lineProperties = shapeProperties.addNewLn()
            lineProperties.addNewNoFill()
        }
    }

    ChartSeries addSeries(ChartDataSource<?> categoryAxisData, ChartDataSource<? extends Number> values, int seriesId) {
        if (!values.isNumeric()) {
            throw new IllegalArgumentException("Value data source must be numeric.")
        }
        Series newSeries = new Series(seriesId, seriesId, categoryAxisData, values, colorTheme)
        series.add(newSeries)
        return newSeries
    }

    void fillChart(Chart chart) {
        CTPlotArea plotArea = chart.getCTChart().getPlotArea()
        CTBar3DChart barChart = plotArea.addNewBar3DChart()
        CTBarDir barDir = barChart.addNewBarDir()
        if (options.chart.type == "column") {
            barDir.setVal(options.chart.inverted ? STBarDir.BAR : STBarDir.COL)
        } else {
            barDir.setVal(options.chart.inverted ? STBarDir.COL : STBarDir.BAR)
        }

        def stacking = options.plotOptions.series.stacking
        CTBarGrouping barGrouping = barChart.addNewGrouping()
        switch (stacking) {
            case "percent":
                barGrouping.setVal(STBarGrouping.PERCENT_STACKED)
                break
            case "normal":
                barGrouping.setVal(STBarGrouping.STACKED)
                break
            default:
                barGrouping.setVal(STBarGrouping.STANDARD)
                break
        }

        barChart.addNewVaryColors().setVal(false)

        for (Series s : series) {
            s.addToChart(barChart)
        }

        CTDLbls dataLabels = barChart.addNewDLbls()
        setPercentageLabel(dataLabels)
        dataLabels.addNewShowLegendKey().setVal(false)
        dataLabels.addNewShowVal().setVal(options.plotOptions?.series?.dataLabels?.enabled as boolean)
        dataLabels.addNewShowCatName().setVal(false)
        dataLabels.addNewShowSerName().setVal(false)
        dataLabels.addNewShowPercent().setVal(false)
        dataLabels.addNewShowBubbleSize().setVal(false)

        barChart.addNewGapWidth().setVal(150)

        for (int index : categoryAxisIndices) {
            barChart.addNewAxId().setVal(chart.categoryAxes[index].getId())
        }
        for (int index : valueAxisIndices) {
            barChart.addNewAxId().setVal(chart.valueAxes[index].getId())
        }
    }
}
