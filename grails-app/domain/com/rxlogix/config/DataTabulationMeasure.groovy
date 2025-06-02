package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.SortEnum
import com.rxlogix.enums.TopColumnTypeEnum
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum

import java.text.SimpleDateFormat

class DataTabulationMeasure {
    MeasureTypeEnum type
    String name
    CountTypeEnum dateRangeCount
    Date customPeriodFrom
    Date customPeriodTo
    Integer relativeDateRangeValue = 1
    PercentageOptionEnum percentageOption
    CaseLineListingTemplate drillDownTemplate

    boolean showTotal = false
    boolean showTopX = false
    Integer topXCount
    Integer sortLevel
    SortEnum sort
    TopColumnTypeEnum topColumnType
    Integer topColumnX

    // Grouping options
    boolean showSubtotalRowAfterGroups = false
    boolean showTotalRowOnly = false
    boolean showTotalAsColumn = false

    //chart
    String percentageChartType
    String valuesChartType
    String percentageAxisLabel
    String valueAxisLabel
    String colorConditions

    static mapping = {
        table name: "DTAB_MEASURE"

        type column: "MEASURE_TYPE"
        name column: "NAME"
        dateRangeCount columnn: "COUNT_TYPE" // for some reason its not getting renamed in MySQL
        customPeriodFrom column: "FROM_DATE"
        customPeriodTo column: "TO_DATE"
        relativeDateRangeValue column: "RELATIVE_DATE_RNG_VALUE"
        percentageOption column: "PERCENTAGE"
        showTotal column: "SHOW_TOTAL"
        showTopX column: "SHOW_TOP_X"
        topXCount column: "TOP_X_COUNT"
        sortLevel column: "SORT_LEVEL"
        sort column: "SORT"
        showSubtotalRowAfterGroups column: "SHOW_SUBTOTALS"
        showTotalRowOnly column: "SHOW_TOTAL_ROWS"
        showTotalAsColumn column: "SHOW_TOTAL_AS_COLS"
        drillDownTemplate column: "DRILL_TEMPLATE_ID"
        valuesChartType column: "VALUES_CHART_TYPE"
        percentageChartType column: "PERC_CHART_TYPE"
        valueAxisLabel column: "VALUES_CHART_LABEL"
        colorConditions column: "COLOR_CONDITIONS"
        percentageAxisLabel column: "PERC_CHART_LABEL"
        topColumnType column: "TOP_TYPE"
        topColumnX column: "TOP_N"
    }

    static constraints = {
        customPeriodFrom(nullable: true)
        customPeriodTo(nullable: true)
        relativeDateRangeValue(min:1)
        topXCount(nullable: true)
        sortLevel(nullable: true)
        sort(nullable: true)
        drillDownTemplate(nullable: true)
        valuesChartType(nullable: true)
        percentageChartType(nullable: true)
        valueAxisLabel(nullable: true)
        percentageAxisLabel(nullable: true)
        topColumnX(nullable: true)
        topColumnType(nullable: true)
        colorConditions(nullable: true)
    }

    String getNameI18nKey() {
        if (name) {
            return name
        } else {
            return type.getI18nKey()
        }
    }

    String getCustomPeriodFromWithTZ() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DateFormat.WITH_TZ)
        return customPeriodFrom ? sdf.format(customPeriodFrom) : null
    }

    String getCustomPeriodToWithTZ() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DateFormat.WITH_TZ)
        return customPeriodTo ? sdf.format(customPeriodTo) : null
    }
}
