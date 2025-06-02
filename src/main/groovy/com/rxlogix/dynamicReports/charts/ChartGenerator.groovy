package com.rxlogix.dynamicReports.charts

interface ChartGenerator {
    static final String PARAMETER_CHART_GENERATOR = "CHART_GENERATOR"
    static final String PARAMETER_CHART_ROWS_COUNT = "CHART_ROWS_COUNT"
    static final String PARAMETER_TOTAL_ROW_INDICES = "TOTAL_ROW_INDICES"
    static final String PARAMETER_EXPORT_AS_IMAGE = "EXPORT_AS_IMAGE"

    def generateChart(Boolean forPdf)
    def getLatestComment()
}
