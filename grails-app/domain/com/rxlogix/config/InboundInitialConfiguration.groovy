package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class InboundInitialConfiguration implements Serializable{

    static auditable =  true

    ReportField reportField //This sender name is same as the report field name
    Date startDate
    boolean isICInitialize = false
    boolean caseDateLogicValue = false

    static mapping = {
        table name: "INBOUND_INITIAL_CONF"
        reportField column: "RPT_FIELD_ID"
        startDate column: "START_DATE"
        isICInitialize column: "IS_IC_INITIALIZE"
        caseDateLogicValue column: "CASE_DATE_LOGIC"
    }
    static constraints = {
        reportField(nullable: false)
        startDate(nullable: true, validator: { val, obj ->
            if (!val && obj.reportField && obj.isICInitialize) {
                return "com.rxlogix.inbound.initial.configuration.date.error.msg"
            }
        })
        isICInitialize(nullable: false)
        caseDateLogicValue(nullable: false)
    }

    String getInstanceIdentifierForAuditLog() {
        return reportField.name
    }

    public String toString() {
        return reportField.name
    }

}
