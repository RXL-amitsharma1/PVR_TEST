package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.*
import org.openxmlformats.schemas.drawingml.x2006.chart.*
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties

/**
 * Holds data for a Line Chart
 */
class LineChartData implements ChartData {
    LineChartData(def options, ChartColorTheme colorTheme) {
        this.options = options
        this.colorTheme = colorTheme
        this.series = new ArrayList<Series>()
    }

    class Series extends AbstractChartSeries {
        protected Series(int id, int order,
                         ChartDataSource<?> categories,
                         ChartDataSource<? extends Number> values,
                         ChartColorTheme colorTheme) {
            super(id, order, categories, values, colorTheme)
        }

        protected void addToChart(CTLineChart ctLineChart) {
            CTLineSer ctLineSer = ctLineChart.addNewSer()
            ctLineSer.addNewIdx().setVal(id)
            ctLineSer.addNewOrder().setVal(order)

            CTAxDataSource catDS = ctLineSer.addNewCat()
            ChartUtil.buildAxDataSource(catDS, categories)
            CTNumDataSource valueDS = ctLineSer.addNewVal()
            ChartUtil.buildNumDataSource(valueDS, values)

            if (isTitleSet()) {
                addCTSerTx(ctLineSer.addNewTx())
            }
            CTShapeProperties shapeProperties = ctLineSer.addNewSpPr()
            CTLineProperties lineProperties = shapeProperties.addNewLn()
            lineProperties.addNewSolidFill().addNewSrgbClr().setVal(colorTheme.nextColorBytes())

            ctLineSer.addNewSmooth().setVal(options.chart.type == "spline")
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
        CTLineChart lineChart = plotArea.addNewLineChart()

        def stacking = options.plotOptions.series.stacking
        CTGrouping barGrouping = lineChart.addNewGrouping()
        switch (stacking) {
            case "percent":
                barGrouping.setVal(STGrouping.PERCENT_STACKED)
                break
            case "normal":
                barGrouping.setVal(STGrouping.STACKED)
                break
            default:
                barGrouping.setVal(STGrouping.STANDARD)
                break
        }

        lineChart.addNewVaryColors().setVal(false)

        for (Series s : series) {
            s.addToChart(lineChart)
        }

        CTDLbls dataLabels = lineChart.addNewDLbls()
        setPercentageLabel(dataLabels)
        dataLabels.addNewShowLegendKey().setVal(false)
        dataLabels.addNewShowVal().setVal(options.plotOptions?.series?.dataLabels?.enabled as boolean)
        dataLabels.addNewShowCatName().setVal(false)
        dataLabels.addNewShowSerName().setVal(false)
        dataLabels.addNewShowPercent().setVal(false)
        dataLabels.addNewShowBubbleSize().setVal(false)

        for (int index : categoryAxisIndices) {
            lineChart.addNewAxId().setVal(chart.categoryAxes[index].getId())
        }
        for (int index : valueAxisIndices) {
            lineChart.addNewAxId().setVal(chart.valueAxes[index].getId())
        }

        lineChart.addNewSmooth().setVal(options.chart.type == "spline")
        CTMarker marker = lineChart.addNewMarker().setVal(true)
    }
}
