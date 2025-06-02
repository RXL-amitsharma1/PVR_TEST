package com.rxlogix.config

import com.rxlogix.reportTemplate.CountTypeEnum

class DataTabulationColumnMeasure {

    ReportFieldInfoList columnList
    List<DataTabulationMeasure> measures
    boolean showTotalIntervalCases = false
    boolean showTotalCumulativeCases = false

    static hasMany = [measures: DataTabulationMeasure]

    static mapping = {
        tablePerHierarchy false
        table name: "DTAB_COLUMN_MEASURE"

        columnList column: "COLUMNS_RFI_LIST_ID", cascade: 'all-delete-orphan'
        measures joinTable: [name: "DTAB_COL_MEAS_MEASURES", column: "MEASURE_ID", key: "DTAB_COL_MEAS_ID"], indexColumn: [name:"MEASURES_IDX"]
        showTotalIntervalCases column: "SHOW_TOTAL_INTERVAL_CASES"
        showTotalCumulativeCases column: "SHOW_TOTAL_CUMULATIVE_CASES"
    }

    static constraints = {
        // Add custom validation in DataTabulationTemplate
        measures(nullable: true)
        columnList(nullable: true)
    }

    boolean hasCumulative() {
        boolean hasCumulative = false
        measures.each {
            if (it.dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT) {
                hasCumulative = true
            }
        }
        return hasCumulative
    }

}
