package com.rxlogix.dynamicReports.charts.ooxml.data

import com.rxlogix.dynamicReports.charts.ooxml.*
import org.openxmlformats.schemas.drawingml.x2006.chart.*
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties

/**
 * Holds data for a Pie Chart
 */
class Pie3DChartData implements ChartData {
    Pie3DChartData(def options, ChartColorTheme colorTheme) {
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

        protected void addToChart(CTPie3DChart ctPieChart) {
            CTPieSer ctPieSer = ctPieChart.addNewSer()
            ctPieSer.addNewIdx().setVal(id)
            ctPieSer.addNewOrder().setVal(order)

            CTAxDataSource catDS = ctPieSer.addNewCat()
            ChartUtil.buildAxDataSource(catDS, categories)
            CTNumDataSource valueDS = ctPieSer.addNewVal()
            ChartUtil.buildNumDataSource(valueDS, values)

            if (isTitleSet()) {
                addCTSerTx(ctPieSer.addNewTx())
            }

            int numOfPoints = values.getPointCount()
            for (int i = 0; i < numOfPoints; ++i) {
                Object value = values.getPointAt(i)
                if (value != null) {
                    CTDPt dataPoint = ctPieSer.addNewDPt()
                    dataPoint.addNewIdx().setVal(i)
                    dataPoint.addNewBubble3D().setVal(false)
                    CTShapeProperties dpProperties = dataPoint.addNewSpPr()
                    dpProperties.addNewSolidFill().addNewSrgbClr().setVal(colorTheme.nextColorBytes())
                    CTLineProperties lineProperties = dpProperties.addNewLn()
                    lineProperties.addNewNoFill()
                }
            }
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
        CTPie3DChart pieChart = plotArea.addNewPie3DChart()
        pieChart.addNewVaryColors().setVal(false)

        for (Series s : series) {
            s.addToChart(pieChart)
        }
        def showLegend = options.plotOptions?.pie?.showInLegend
        if (showLegend == null) {
            showLegend = options.plotOptions?.series?.showInLegend  as boolean
        }
        if(!showLegend) {
            chart.deleteLegend()
        }

        CTDLbls dataLabels = pieChart.addNewDLbls()
        def showDataLabels = options.plotOptions?.pie?.dataLabels?.enabled
        if (showDataLabels == null) {
            showDataLabels = options.plotOptions?.series?.dataLabels?.enabled
        }
        if (showDataLabels == null) {
            showDataLabels = true
        }
        dataLabels.addNewDLblPos().setVal(STDLblPos.BEST_FIT)
        dataLabels.addNewShowLegendKey().setVal(false)
        dataLabels.addNewShowVal().setVal(false)
        dataLabels.addNewShowCatName().setVal(showDataLabels)
        dataLabels.addNewShowSerName().setVal(false)
        dataLabels.addNewShowPercent().setVal(showDataLabels)
        dataLabels.addNewShowBubbleSize().setVal(false)
        dataLabels.addNewShowLeaderLines().setVal(showDataLabels)
    }
}
