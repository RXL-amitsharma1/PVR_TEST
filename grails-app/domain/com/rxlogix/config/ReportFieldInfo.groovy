package com.rxlogix.config

import com.rxlogix.enums.SortEnum
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil

class ReportFieldInfo {
    public static final int AUTO_COLUMN_WIDTH = 0

    ReportField reportField

    String argusName
    String renameValue
    String customExpression
    String datasheet
    Boolean onPrimaryDatasheet = false
    String advancedSorting
    CustomReportField customField
    String newLegendValue

    int stackId
    int sortLevel
    SortEnum sort

    // For CLL
    boolean commaSeparatedValue = false
    boolean suppressRepeatingValues = false
    boolean suppressLabel = false
    boolean blindedValue = false
    boolean redactedValue = false
    Boolean hideSubtotal = false
    int setId = 0
    int columnWidth = AUTO_COLUMN_WIDTH
    CaseLineListingTemplate drillDownTemplate
    String drillDownFilerColumns
    String colorConditions

    static belongsTo = [reportFieldInfoList: ReportFieldInfoList]

    static constraints = {
        customField(nullable: true)
        reportField(nullable: false)
        argusName(nullable: false)
        renameValue(nullable: true, maxSize: 255)
        hideSubtotal(nullable: true)
        customExpression(nullable: true, maxSize: 32000)
        datasheet(nullable: true)
        onPrimaryDatasheet(nullable: true)
        advancedSorting(nullable: true, maxSize: 2000)
        newLegendValue(nullable: true, maxSize: 2000)

        stackId(nullable: true)
        sortLevel(nullable: true)
        sort(nullable: true)

        commaSeparatedValue(nullable: false)
        suppressRepeatingValues(nullable: false)
        suppressLabel(nullable: false)
        blindedValue(nullable: false)
        redactedValue(nullable: false)
        setId(nullable: false)
        columnWidth(nullable: false)
        drillDownTemplate(nullable: true)
        drillDownFilerColumns(nullable: true)
        colorConditions(nullable: true)
    }

    static mapping = {
        table name: "RPT_FIELD_INFO"

        customField column: "CUSTOM_FIELD_ID"
        reportFieldInfoList column: "RF_INFO_LIST_ID"
        reportField column: "RPT_FIELD_ID", cascade: "none"
        argusName column: "ARGUS_NAME"
        renameValue column: "RENAME_VALUE"
        hideSubtotal column: "HIDE_SUBTOTAL"
        customExpression column: "CUSTOM_EXPRESSION", sqlType: DbUtil.longStringType
        datasheet column: "DATASHEET"
        onPrimaryDatasheet column: "ON_PRIMARY_DATASHEET"
        advancedSorting column: "ADVANCED_SORTING", length: 2000
        newLegendValue column: "NEW_LEGEND_VALUE", length: 2000

        stackId column: "STACK_ID"
        sortLevel column: "SORT_LEVEL"
        sort column: "SORT"

        commaSeparatedValue column: "COMMA_SEPARATED"
        suppressRepeatingValues column: "SUPPRESS_REPEATING"
        suppressLabel column: "SUPPRESS_LABEL"
        blindedValue column: "BLINDED"
        redactedValue column: "REDACTED"
        redactedValue column: "REDACTED"
        setId column: "SET_ID"
        columnWidth column: "COLUMN_WIDTH", defaultValue: AUTO_COLUMN_WIDTH
        drillDownTemplate column: "DRILL_DOWN_ID"
        drillDownFilerColumns column: "DRILL_DOWN_FIELDS"
        colorConditions column: "COLOR_CONDITIONS"
    }

    public String toString() {
        return "$reportField : $argusName"
    }

// XML Unique Field Identifier
    String uniqueIdentifierXmlTag() {
        return reportField.name + "-" + (renameValue ? (renameValue.bytes.encodeAsBase64().toString()) : '')
    }

    String nameForAudit(String renameValue){
        return renameValue ? "$reportField.name($renameValue)" : "$reportField.name"
    }


    Map instanceAsMap(){
        Map propertiesMap = [:]
        MiscUtil.getObjectProperties(this).sort{it.key}.each { key, value ->
            if (key != "reportFieldInfoList"){
                value = (key == "stackId" && value == -1) ? "NON-STACKED" : value
                propertiesMap.put(key, value)
            }
        }
       propertiesMap
    }
}
