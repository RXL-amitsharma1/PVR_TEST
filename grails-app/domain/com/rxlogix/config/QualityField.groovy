

package com.rxlogix.config

class QualityField {
    String qualityModule
    String fieldName;
    String fieldType;
    String label;
    Boolean isSelectable;
    Set<Long> reportIds = []
    Long execReportId

    static hasMany = [reportIds: Long]
    static constraints = {
        fieldName(nullable: false)
        fieldType(nullable: false)
        execReportId(nullable: true)
        label(nullable: true)
        isSelectable(nullable: true)
    }
    static mapping = {
        table("QUALITY_FIELD")
        id column: 'ID'
        qualityModule column: 'QUALITY_MODULE'
        fieldName column: "FIELD_NAME"
        fieldType column: "FIELD_TYPE"
        label column: "LABEL"
        isSelectable column: "IS_SELECTABLE"
        execReportId column: "EXEC_REPORT_ID"
        reportIds joinTable: [name: "QUALITY_FIELD_REPORT", column: "REPORT_ID", key: "QUALITY_FIELD_ID"]
        version false
    }

    boolean isReportIdExists(Long reportId) {
        if(reportIds?.find {it == reportId}) {
            return true
        }
        return false
    }
}
