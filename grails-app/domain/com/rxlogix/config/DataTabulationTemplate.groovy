package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.JSONAudit
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.text.SimpleDateFormat

@CollectionSnapshotAudit
class DataTabulationTemplate extends ReportTemplate {
    transient def queryService

    static auditable = true
    boolean supressHeaders = false
    boolean supressRepeatingExcel = false
    boolean drillDownToCaseList = false
    boolean pageBreakByGroup = false
    boolean transposeOutput = false
    boolean positiveCountOnly = false
    boolean allTimeframes = false
    List<DataTabulationColumnMeasure> columnMeasureList
    ReportFieldInfoList groupingList
    ReportFieldInfoList rowList
    String chartCustomOptions
    String JSONQuery
    Boolean chartExportAsImage
    Boolean worldMap
    String worldMapConfig

    static hasMany = [columnMeasureList: DataTabulationColumnMeasure]

    static mapping = {
        autoTimestamp false
        tablePerHierarchy false
        table name: "DTAB_TEMPLT"

        pageBreakByGroup column: "PAGE_BREAK_BY_GROUP"
        supressHeaders column: "SUPRESS_HEADERS"
        supressRepeatingExcel column: "SUPRESS_REPEATING_EXCEL"
        drillDownToCaseList column: "DRILL_DOWN_TO_CASE_LIST"
        transposeOutput column: "TRANSPOSE_OUTPUT"
        positiveCountOnly column: "POSITIVE_COUNT_ONLY"
        allTimeframes column: "ALL_TIMEFRAMES"

        columnMeasureList joinTable: [name: "DTAB_TEMPLTS_COL_MEAS", column: "COLUMN_MEASURE_ID", key: "DTAB_TEMPLT_ID"], indexColumn: [name: "COLUMN_MEASURE_IDX"]
        groupingList column: "GROUPING_RF_INFO_LIST_ID", cascade: 'all-delete-orphan'
        rowList column: "ROWS_RF_INFO_LIST_ID", cascade: 'all-delete-orphan'
        chartCustomOptions column: "CHART_CUSTOM_OPTIONS", sqlType: DbUtil.longStringType
        'JSONQuery' column: "QUERY", sqlType: DbUtil.longStringType
        chartExportAsImage column: "EXPORT_AS_IMAGE"
        worldMap column: "WORLD_MAP"
        worldMapConfig column: "WORLD_MAP_CONFIG"
    }

    static constraints = {
        columnMeasureList(minSize: 1, validator: { val ->
            boolean hasMeasure = true
            boolean columns = true
            for (DataTabulationColumnMeasure colMeas : val) {
                if (!colMeas.measures || colMeas.measures.size() == 0) {
                    hasMeasure = false
                } else {
                    Set uniqMeasures = []
                    for (DataTabulationMeasure measure : colMeas.measures) {
                        String key = "${measure.type}_${measure.dateRangeCount}"
                        if (uniqMeasures.contains(key)) return "com.rxlogix.config.DataTabulationTemplate.measures.duplicates"
                        uniqMeasures.add(key)
                    }
                }
                if (colMeas.columnList?.reportFieldInfoList?.size() > 5) {
                    columns = false
                }
            }
            if (!hasMeasure && columns) {
                return "com.rxlogix.config.DataTabulationTemplate.measures.null"
            } else if (hasMeasure && !columns) {
                return "com.rxlogix.config.DataTabulationTemplate.columnList.exceedMax"
            } else if (!hasMeasure && !columns) {
                return "com.rxlogix.config.DataTabulationTemplate.columnMeasure"
            }
        })

        groupingList(nullable: true)
        chartExportAsImage(nullable: true)
        worldMap(nullable: true)
        worldMapConfig(nullable: true)
        rowList(nullable: true, validator: { val ->
            if (val?.reportFieldInfoList?.size() > 5) {
                return "com.rxlogix.config.DataTabulationTemplate.rowList.exceedMax"
            }
        })
        chartCustomOptions(nullable: true)
        JSONQuery(nullable: true, maxSize: 8388608, validator: { val, obj ->
            if (obj.JSONQuery) {
                def queryCriteria = MiscUtil.parseJsonText(obj?.JSONQuery)
                if (!queryCriteria?.all?.containerGroups?.expressions || queryCriteria?.all?.containerGroups?.expressions[0]?.size() == 0) {
                    return "com.rxlogix.config.CLL.JSONQuery.required"
                }
            }
        })
    }

    transient List<ReportFieldInfo> getAllSelectedFieldsInfo() {
        List allColumns = []
        columnMeasureList.each {
            if (it.columnList) {
                allColumns.add(it.columnList.reportFieldInfoList)
            }
        }
        groupingList?.reportFieldInfoList && allColumns.addAll(groupingList?.reportFieldInfoList)

        List allFields = []
        allColumns && allFields.addAll(allColumns.flatten())
        rowList?.reportFieldInfoList && allFields.addAll(rowList?.reportFieldInfoList)
        return allFields
    }

    List<ReportFieldInfo> getSelectedFieldsRows() {
        def result = []
        groupingList?.reportFieldInfoList && result.addAll(groupingList?.reportFieldInfoList)
        rowList?.reportFieldInfoList && result.addAll(rowList?.reportFieldInfoList)
        return result
    }

    boolean hasCumulative() {
        boolean hasCumulative = false
        columnMeasureList.each {
            it.measures.each {
                if (it.dateRangeCount == CountTypeEnum.CUMULATIVE_COUNT ) {
                    hasCumulative = true
                }
            }
        }
        return hasCumulative
    }

    String getJSONStringMeasures() {
        JSONArray measureList = new JSONArray()
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DateFormat.WITH_TZ)

        columnMeasureList.each { colMeas ->
            JSONArray JSONMeasures = new JSONArray()
            if (colMeas.measures) {
                colMeas.measures.each {
                    JSONObject measure = new JSONObject([
                            name                  : it.name,
                            type                  : it.type.name(),
                            count                 : it.dateRangeCount.name(),
                            percentage            : it.percentageOption.name(),
                            showTotal             : it.showTotal,
                            relativeDateRangeValue: it.relativeDateRangeValue,
                            sort                  : it.sort?.value(),
                            sortLevel             : it.sortLevel])
                    // add date range info
                    measure.customPeriodFrom = it.customPeriodFrom ? sdf.format(it.customPeriodFrom) : null
                    measure.customPeriodTo = it.customPeriodTo ? sdf.format(it.customPeriodTo) : null
                    JSONMeasures.add(measure)
                }
                measureList.add(JSONMeasures)
            }
        }

        return measureList.toString()
    }

    def createMeasureView(columnMeasureListValue) {
        columnMeasureListValue?.collect {
            ViewHelper.getReadableMap((it.columnList?.getInstanceIdentifierForAuditLog() ?: "")?.toString()) +
                    "Measures:\n" + (it.measures?.collect {ViewHelper.getReadableMeasures(getValueForMeasure(it))})?.toString() +
                    ("\nShow Total Interval Cases :  ${(it.showTotalIntervalCases == "true") ? "Yes" : "No"}") +
                    ("\nShow Total Cumulative Cases : ${(it.showTotalCumulativeCases == "true") ? "Yes" : "No"}\n")
        }?.join("\n")
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && (oldValues == null)) {
            newValues.put("columnMeasureList", createMeasureView(columnMeasureList))
            newValues.put("JSONQuery", queryService.generateReadableQueryForAuditLog([], JSONQuery, 0))
        }
        if (newValues && oldValues && this.dirtyPropertyNames?.contains("columnMeasureList")) {
            newValues.put("columnMeasureList", createMeasureView(columnMeasureList))
            oldValues.put("columnMeasureList", createMeasureView(this.getPersistentValue("columnMeasureList")))
        }
        if (newValues && oldValues && this.dirtyPropertyNames?.contains("JSONQuery")) {
            newValues.put("JSONQuery", queryService.generateReadableQueryForAuditLog([], JSONQuery, 0))
            oldValues.put("JSONQuery", queryService.generateReadableQueryForAuditLog([], this.getPersistentValue("JSONQuery"), 0))
        }

        return [newValues: newValues, oldValues: oldValues]
    }

    String getValueForMeasure(DataTabulationMeasure measure) {
        String dataTabMesaureValue = ""
        if (!measure) {
            return dataTabMesaureValue
        }
        MiscUtil.getObjectProperties(measure).sort { it.key }.each { key, value ->
            if (value != null) {
                if (key == "colorConditions") {
                    dataTabMesaureValue += "$key :" + ViewHelper.getReadableConditionalFormatting(value)
                } else {
                    dataTabMesaureValue += "$key : $value, "
                }
            }
        }
        return dataTabMesaureValue
    }

    boolean isGranularity(){
        getAllSelectedFieldsInfo()?.find {
            it && !it.customExpression && (it.reportField?.dataType?.simpleName == "Date")
        }
    }

    @Override
    Set<String> getPOIInputsKeys() {
        Set<String> poiInputsSet = []
        groupingList?.reportFieldInfoList*.customExpression?.each {
            it?.findAll(Constants.POI_INPUT_PATTERN_REGEX)?.each {
                poiInputsSet.add(it)
            }
        }
        rowList?.reportFieldInfoList*.customExpression?.each {
            it?.findAll(Constants.POI_INPUT_PATTERN_REGEX)?.each {
                poiInputsSet.add(it)
            }
        }
        return poiInputsSet
    }

    @Override
    public String toString() {
        super.toString()
    }
}
