package com.rxlogix.config

import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.ViewHelper

class ReportFieldInfoList {
    List<ReportFieldInfo> reportFieldInfoList

    static hasMany = [reportFieldInfoList: ReportFieldInfo]
//    static belongsTo = [reportTemplate: ReportTemplate]

    static constraints = {
        reportFieldInfoList(nullable: true)
    }

    static mapping = {
        table name: "RPT_FIELD_INFO_LIST"
        reportFieldInfoList joinTable: [name:"RF_INFO_LISTS_RF_INFO", column:"RF_INFO_ID", key: "RF_INFO_LIST_ID"],
                indexColumn: [name:"RF_INFO_IDX"], cascade: 'all-delete-orphan'
    }

    String getInstanceIdentifierForAuditLog() {
        AuditLogConfigUtil.convertMapToString(getValueAsMap())
    }

    public String toString() {
        reportFieldInfoList.reportField.collect {
            it.getDisplayName().join(",")
        }
    }

    Map<String, Map> getValueAsMap(){
        Map<String, Map> tempMap = new HashMap<Long, Map>()
        int count = 1
        int position = 0
        reportFieldInfoList.each {
            Map reportFieldInfoMap = it.instanceAsMap()
            String renameValue = reportFieldInfoMap.getOrDefault("renameValue", null)
            String key = it.nameForAudit(renameValue)
            reportFieldInfoMap.put("position", position)
            if (reportFieldInfoMap.containsKey("colorConditions") && reportFieldInfoMap.get("colorConditions")) {
                reportFieldInfoMap.put("Conditional Formatting", ViewHelper.getReadableConditionalFormatting(reportFieldInfoMap.get("colorConditions")))
                reportFieldInfoMap.remove("colorConditions")
            }
            position++
            if (tempMap.containsKey(key)){
                key = "${key}_${count}"
                tempMap.put(key, reportFieldInfoMap)
                count++
            } else {
                tempMap.put(key, reportFieldInfoMap)
            }
        }
        tempMap
    }
}
