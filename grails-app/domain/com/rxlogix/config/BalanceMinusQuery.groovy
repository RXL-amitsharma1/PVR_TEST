package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.joda.time.DateTimeZone
@CollectionSnapshotAudit
class BalanceMinusQuery {
    static auditable =  true

    transient String sourceProfile
    List<BmQuerySection> bmQuerySections = []
    String startDateTime
    String repeatInterval
    String configSelectedTimeZone = "UTC"
    boolean isDisabled = false

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy

    static hasMany = [bmQuerySections: BmQuerySection]
    static mappedBy = [bmQuerySections: 'balanceMinusQuery']

    static mapping = {
        table name: "BALANCE_MINUS_QUERY"
        bmQuerySections joinTable: [name: "BQMQ_SECTION", column: "ID", key: "BMQUERY_ID"], indexColumn: [name: "BQMQ_SECTION_IDX"], cascade: "all-delete-orphan"
        startDateTime column: "START_DATETIME"
        repeatInterval column: "REPEAT_INTERVAL"
        configSelectedTimeZone column: "SELECTED_TIME_ZONE"
        isDisabled column: "DISABLED"
        dateCreated column: "DATE_CREATED"
        lastUpdated column: "LAST_UPDATED"
        createdBy column: "CREATED_BY"
        modifiedBy column: "MODIFIED_BY"
        version false
    }

    static constraints = {
        startDateTime nullable: false
        repeatInterval nullable: false
        configSelectedTimeZone nullable:false
        createdBy nullable: false
        modifiedBy nullable: false
        bmQuerySections nullable: false
    }

    String getTimezone() {
        if (configSelectedTimeZone) {
            return configSelectedTimeZone
        } else {
            return DateTimeZone.UTC.ID
        }
    }

    String getInstanceIdentifierForAuditLog() {
        return "BM Query"
    }

    @Override
    public String toString() {
        return "BM Query"
    }
}
