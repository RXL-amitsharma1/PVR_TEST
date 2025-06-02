package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class ActionPlanSummary {
    static auditable = true
    String textData
    String parentEntityKey
    Date from
    Date to
    boolean isDeleted = false
    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        textData blank: false
        textData(nullable: true)
    }

    static mapping = {
        table name: "ACTION_PLAN_SUMMARY"
        textData column: "NOTE"
        from column: "FROM_DATE"
        to column: "TO_DATE"
    }

    public String toString() {
        return textData
    }

    String getInstanceIdentifierForAuditLog() {
        return "Action Plan Summary (ID:${id})"
    }
}