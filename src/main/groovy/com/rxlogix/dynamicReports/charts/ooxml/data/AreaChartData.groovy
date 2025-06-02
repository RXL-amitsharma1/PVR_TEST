package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.*
import org.openxmlformats.schemas.drawingml.x2006.chart.*
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties

/**
 * Holds data for a Area Chart
 */
class AreaChartData implements ChartData {
    AreaChartData(def options, ChartColorTheme colorTheme) {
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

        protected void addToChart(CTAreaChart ctAreaChart) {
            CTAreaSer ctAreaSer = ctAreaChart.addNewSer()
            ctAreaSer.addNewIdx().setVal(id)
            ctAreaSer.addNewOrder().setVal(order)

            CTAxDataSource catDS = ctAreaSer.addNewCat()
            ChartUtil.buildAxDataSource(catDS, categories)
            CTNumDataSource valueDS = ctAreaSer.addNewVal()
            ChartUtil.buildNumDataSource(valueDS, values)

            if (isTitleSet()) {
                addCTSerTx(ctAreaSer.addNewTx())
            }
            CTShapeProperties shapeProperties = ctAreaSer.addNewSpPr()
            CTSRgbColor fillColor = shapeProperties.addNewSolidFill().addNewSrgbClr()
            fillColor.setVal(colorTheme.nextColorBytes())
            fillColor.addNewAlpha().setVal(ChartColorTheme.DEFAULT_FILL_ALPHA)
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
        CTAreaChart areaChart = plotArea.addNewAreaChart()

        def stacking = options.plotOptions.series.stacking
        CTGrouping areaGrouping = areaChart.addNewGrouping()
        switch (stacking) {
            case "percent":
                areaGrouping.setVal(STGrouping.PERCENT_STACKED)
                break
            case "normal":
                areaGrouping.setVal(STGrouping.STACKED)
                break
            default:
                areaGrouping.setVal(STGrouping.STANDARD)
                break
        }

        areaChart.addNewVaryColors().setVal(false)

        for (Series s : series) {
            s.addToChart(areaChart)
        }

        CTDLbls dataLabels = areaChart.addNewDLbls()
        setPercentageLabel(dataLabels)
        dataLabels.addNewShowLegendKey().setVal(false)
        dataLabels.addNewShowVal().setVal(options.plotOptions?.series?.dataLabels?.enabled as boolean)
        dataLabels.addNewShowCatName().setVal(false)
        dataLabels.addNewShowSerName().setVal(false)
        dataLabels.addNewShowPercent().setVal(false)
        dataLabels.addNewShowBubbleSize().setVal(false)

        for (int index : categoryAxisIndices) {
            areaChart.addNewAxId().setVal(chart.categoryAxes[index].getId())
        }
        for (int index : valueAxisIndices) {
            areaChart.addNewAxId().setVal(chart.valueAxes[index].getId())
        }
    }
}
