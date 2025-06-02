package com.rxlogix.dynamicReports.reportTypes.crosstab

import com.rxlogix.reportTemplate.MeasureTypeEnum
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import org.grails.web.json.JSONObject

import java.util.regex.Pattern

class HeaderTabDTO {
    private static final Pattern COLUMN_NAME_PATTERN = ~/(\w+)_(\d+)_(\w+)(\d{1})(\d+)/
    private static final Pattern COLUMN_LABEL_PATTERN = ~/(?s)^(.*)\((.*)\)$/
    private static final String PERCENTAGE_ALIAS_PREFIX = "P"
    private static final String ROW_COLUMN_NAME_PREFIX = "ROW"
    private static final String CASE_LIST_COLUMN_NAME_PREFIX = "CASE_LIST"
    private static final String CASE_COUNT_COLUMN_NAME_PREFIX = "CASE_COUNT"
    private static final String INTERVAL_CASE_COUNT_COLUMN_NAME_PREFIX = "INTERVAL_CASE_COUNT"
    private static final Map<MeasureTypeEnum, List<String>> MEASURE_TYPE_TO_ALIASES = [
            (MeasureTypeEnum.CASE_COUNT)         : ["CC", "PC", "IC"],
            (MeasureTypeEnum.EVENT_COUNT)        : ["CE", "PE", "EC"],
            (MeasureTypeEnum.PRODUCT_EVENT_COUNT): ["CP", "PP", "PC"],
            (MeasureTypeEnum.VERSION_COUNT)      : ["CV", "PV", "VC"],
            (MeasureTypeEnum.REPORT_COUNT)       : ["CR", "PR", "RR"],
            (MeasureTypeEnum.ROW_COUNT)          : ["CB", "PB", "IRC"],
            (MeasureTypeEnum.COMPLIANCE_RATE)    : ["PA", "??", "??"]
    ]

    String columnName
    String columnLabel
    String simpleColumnLabel
    String measureName

    String prefix
    Integer loopCounter
    String alias
    Integer dateRangeType
    Integer columnIndex

    List<String> labels
    TextColumnBuilder columnBuilder

    HeaderTabDTO(JSONObject header) {
        this.columnName = header.entrySet().getAt(0).key
        this.columnLabel = header.entrySet().getAt(0).value
        parseColumnName()
        parseColumnLabel()
    }

    boolean isPercentageColumn() {
        return alias != null && alias.startsWith(PERCENTAGE_ALIAS_PREFIX)
    }

    boolean isRowColumn() {
        return columnName.substring(0, 3).equalsIgnoreCase(ROW_COLUMN_NAME_PREFIX)
    }

    boolean isCaseListColumn() {
        return columnName.startsWith(CASE_LIST_COLUMN_NAME_PREFIX)
    }

    boolean isTotalCaseCount() {
        return columnName.startsWith(CASE_COUNT_COLUMN_NAME_PREFIX)
    }

    boolean isIntervalCaseCount() {
        return columnName.startsWith(INTERVAL_CASE_COUNT_COLUMN_NAME_PREFIX)
    }

    boolean isMeasureTotal() {
        columnLabel.endsWith("\n\rTotal")
    }

    boolean isRelatedWith(MeasureTypeEnum measureTypeEnum) {
        MEASURE_TYPE_TO_ALIASES[measureTypeEnum]?.contains(alias)
    }

    private void parseColumnName() {
        def matcher = COLUMN_NAME_PATTERN.matcher(columnName)
        if (matcher.matches()) {
            int groupIndex = 0
            prefix = matcher.group(++groupIndex)
            loopCounter = Integer.parseInt(matcher.group(++groupIndex))
            alias = matcher.group(++groupIndex)
            dateRangeType = Integer.parseInt(matcher.group(++groupIndex))
            columnIndex = Integer.parseInt(matcher.group(++groupIndex))
        }
    }

    private void parseColumnLabel() {
        this.labels = columnLabel.split("\n").findAll { it }
        def matcher = COLUMN_LABEL_PATTERN.matcher(columnLabel)
        if (matcher.matches()) {
            int groupIndex = 0
            simpleColumnLabel = matcher.group(++groupIndex)
            measureName = matcher.group(++groupIndex)
        }
    }
}
