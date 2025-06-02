package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.TemplateFieldTypeEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.JSONAudit

@CollectionSnapshotAudit
class CaseLineListingTemplate extends ReportTemplate {
    transient def queryService

    static CLL_TEMPLATE_REPORT_FIELD_NAME = "masterCaseNum"
    static CLL_TEMPLATE_J_REPORT_FIELD_NAME = "masterCaseNumJ"
    static CLL_TEMPLATE_LAM_REPORT_FIELD_NAME = "masterCaseNumLam"
    static auditable = true

    boolean pageBreakByGroup = false
    boolean columnShowTotal = false
    boolean columnShowSubTotal = false
    boolean columnShowDistinct = false
    Boolean hideTotalRowCount = false
    String suppressRepeatingValuesColumnList

    String renamedGrouping
    String renamedRowCols
    String JSONQuery

    ReportFieldInfoList columnList
    ReportFieldInfoList groupingList
    ReportFieldInfoList rowColumnList
    ReportFieldInfoList serviceColumnList

    ReassessListednessEnum customExpressionListedness
    List<QueryExpressionValue> queryExpressionValues

    static hasMany = [queryExpressionValues: QueryExpressionValue]

    static mapping = {
        autoTimestamp false
        tablePerHierarchy false
        table name: "CLL_TEMPLT"
        id column: "ID"

        pageBreakByGroup column: "PAGE_BREAK_BY_GROUP"
        columnShowTotal column: "COL_SHOW_TOTAL"
        columnShowSubTotal column: "COL_SHOW_SUBTOTAL"
        hideTotalRowCount column: "COL_HIDE_ROW_COUNT"
        columnShowDistinct column: "COL_SHOW_DISTINCT"
        suppressRepeatingValuesColumnList column: "SUPPRESS_COLUMN_LIST"
        renamedGrouping column: "RENAME_GROUPING", sqlType: DbUtil.longStringType
        renamedRowCols column: "RENAME_ROW_COLS", sqlType: DbUtil.longStringType
        'JSONQuery' column: "QUERY", sqlType: DbUtil.longStringType
        customExpressionListedness column: "CUST_EXP_REASSESS_LISTEDNESS"

        columnList column: "COLUMNS_RF_INFO_LIST_ID", cascade: 'all-delete-orphan'
        groupingList column: "GROUPING_RF_INFO_LIST_ID", cascade: 'all-delete-orphan'
        rowColumnList column: "ROW_COLS_RF_INFO_LIST_ID", cascade: 'all-delete-orphan'
        serviceColumnList column: "SRV_COLS_RF_INFO_LIST_ID", cascade: 'all-delete-orphan'
        queryExpressionValues joinTable:[name: "CLL_TEMPLATES_QRS_EXP_VALUES", column: "QUERY_EXP_VALUE_ID", key:"CLL_TEMPLATE_ID"], cascade: 'all-delete-orphan'
    }

    static constraints = {
        suppressRepeatingValuesColumnList(nullable: true)
        renamedGrouping(nullable: true, maxSize: 4096)
        renamedRowCols(nullable: true, maxSize: 4096)
        JSONQuery(nullable: true, maxSize: 8388608, validator: { val, obj ->
            if (obj.JSONQuery) {
                def queryCriteria = MiscUtil.parseJsonText(obj?.JSONQuery)
                if (!queryCriteria?.all?.containerGroups?.expressions || queryCriteria?.all?.containerGroups?.expressions[0]?.size() == 0) {
                    return "com.rxlogix.config.CLL.JSONQuery.required"
                }
            }
        })

        customExpressionListedness(nullable: true)
        columnList(nullable: false)
        groupingList(nullable: true)
        rowColumnList(nullable: true)
        serviceColumnList(nullable: true)
        hideTotalRowCount(nullable: true)
    }

    transient List<ReportFieldInfo> getAllSelectedFieldsInfo() {
        def result = []
        columnList?.reportFieldInfoList && result.addAll(columnList?.reportFieldInfoList)
        groupingList?.reportFieldInfoList && result.addAll(groupingList?.reportFieldInfoList)
        rowColumnList?.reportFieldInfoList && result.addAll(rowColumnList?.reportFieldInfoList)
        serviceColumnList?.reportFieldInfoList && result.addAll(serviceColumnList?.reportFieldInfoList)
        return result.sort { it.setId }
    }

    transient List<ReportFieldInfo> getAllSelectedFieldsInfoForXML() {
        def result = []
        columnList?.reportFieldInfoList && result.addAll(columnList?.reportFieldInfoList)
        groupingList?.reportFieldInfoList && result.addAll(groupingList?.reportFieldInfoList)
        rowColumnList?.reportFieldInfoList && result.addAll(rowColumnList?.reportFieldInfoList)
        return result.sort { it.setId }
    }

    transient List<String> getFieldNameWithIndex() {
        List<String> list = []
        List<ReportField> allFields = getAllSelectedFieldsInfo().reportField
        allFields.eachWithIndex{ it, index  ->
            list.add(it.name + "_" + index)
        }
        return list
    }

    def getFieldNameIndexTuple() {
        List<Tuple2<String, Integer>> list = []
        List<ReportField> allFields = getAllSelectedFieldsInfo().reportField
        allFields.eachWithIndex{ it, index  ->
            list.add(new Tuple2<String, Integer>(it.name,index))
        }
        list
    }

    List getSelectedFieldsFullInfo() {
        List list = []
        columnList?.reportFieldInfoList?.each { list.add([nameWithIndex: it.reportField.name, type: TemplateFieldTypeEnum.COLUMN_FIELD, reportFieldInfo: it]); }
        groupingList?.reportFieldInfoList?.each { list.add([nameWithIndex: it.reportField.name, type: TemplateFieldTypeEnum.GROUPING_FIELD, reportFieldInfo: it]); }
        rowColumnList?.reportFieldInfoList?.each { list.add([nameWithIndex: it.reportField.name, type: TemplateFieldTypeEnum.ROW_COLUMN_FIELD, reportFieldInfo: it]); }
        serviceColumnList?.reportFieldInfoList?.each { list.add([nameWithIndex: it.reportField.name, type: TemplateFieldTypeEnum.SERVICE_COLUMN_FIELD, reportFieldInfo: it]); }
        list = list.sort { it.reportFieldInfo.setId }
        list.eachWithIndex { it, index ->
            it.nameWithIndex = it.nameWithIndex + "_" + index
        }
        return list
    }

    boolean hasStackedColumns() {
        List <ReportFieldInfo> selectedColumns = columnList.reportFieldInfoList
        for (ReportFieldInfo reportFieldInfo : selectedColumns) {
            if (reportFieldInfo.stackId > 0) {
                return true
            }
        }
        return false
    }

    @Override
    transient Set<String> getPOIInputsKeys() {
        Set<String> poiInputsSet = []
        columnList?.reportFieldInfoList*.customExpression?.each {
            it?.findAll(Constants.POI_INPUT_PATTERN_REGEX)?.each {
                poiInputsSet.add(it)
            }
        }

        groupingList?.reportFieldInfoList*.customExpression?.each {
            it?.findAll(Constants.POI_INPUT_PATTERN_REGEX)?.each {
                poiInputsSet.add(it)
            }
        }

        serviceColumnList?.reportFieldInfoList*.customExpression?.each {
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

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && (oldValues == null)) {
            newValues.put("JSONQuery", queryService.generateReadableQueryForAuditLog(queryExpressionValues, JSONQuery, 0))
        }
        if (newValues && oldValues && this.dirtyPropertyNames?.contains("JSONQuery")) {
            newValues.put("JSONQuery", queryService.generateReadableQueryForAuditLog(queryExpressionValues, JSONQuery, 0))
            oldValues.put("JSONQuery", queryService.generateReadableQueryForAuditLog(this.getPersistentValue("queryExpressionValues"), this.getPersistentValue("JSONQuery"), 0))
        }

        return [newValues: newValues, oldValues: oldValues]
    }

}
