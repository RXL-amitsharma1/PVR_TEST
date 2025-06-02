package com.rxlogix.config

import com.rxlogix.user.User
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.util.ViewHelper
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@DirtyCheck
@CollectionSnapshotAudit
class AutoReasonOfDelay implements Serializable{
    static auditable =  true

    DateRangeType dateRangeType
    EvaluateCaseDateEnum evaluateDateAs = EvaluateCaseDateEnum.LATEST_VERSION
    GlobalDateRangeInformationAutoROD globalDateRangeInformationAutoROD
    List<QueryRCA> queriesRCA = []
    SourceProfile sourceProfile

    User owner
    Date nextRunDate
    //Date and time the report was run; the start of the run; does not take into account the duration of the run.
    Date lastRunDate
    String scheduleDateJSON
    String configSelectedTimeZone = "UTC"

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    boolean executing = false
    boolean isEnabled = true
    boolean isDeleted=false
    String blankValuesJSON
    List<String> emailToUsers = []

    static hasMany = [queriesRCA: QueryRCA, emailToUsers: String]
    static mappedBy = [queriesRCA: 'autoReasonOfDelay']


    static constraints = {
        dateRangeType nullable: true
        evaluateDateAs nullable:true
        globalDateRangeInformationAutoROD nullable: true
        queriesRCA nullable: true
        nextRunDate(nullable: true)
        lastRunDate(nullable: true)
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
        scheduleDateJSON(nullable: true, maxSize: 1024, validator: { val, obj ->
            if (val && !MiscUtil.validateScheduleDateJSON(val)) {
                return "com.rxlogix.config.date.time.weekly.required"
            }
        })
        blankValuesJSON(nullable: true, maxSize: 8192)
    }

    static mapping = {
        table name: "AUTO_REASON_OF_DELAY"
        dateRangeType column: "DATE_RANGE_TYPE"
        evaluateDateAs column: "EVALUATE_DATE_AS"
        globalDateRangeInformationAutoROD column: "GLOBAL_DATE_RANGE_AUTOROD_ID"
        queriesRCA joinTable: [name: "QUERY_RCA", column: "ID", key: "AUTO_REASON_OF_DELAY_ID"], indexColumn: [name: "QUERY_RCA_IDX"], cascade: "all-delete-orphan"
        sourceProfile column: "SOURCE_PROFILE"
        executing column: "EXECUTING"
        isEnabled column: "IS_ENABLED"
        isDeleted column: "IS_DELETED"
        owner column: "PVUSER_ID"
        scheduleDateJSON column: "SCHEDULE_DATE"
        configSelectedTimeZone column: "SELECTED_TIME_ZONE"
        nextRunDate column: "NEXT_RUN_DATE"
        lastRunDate column: "LAST_RUN_DATE"
        blankValuesJSON column: "BLANK_VALUES", sqlType: DbUtil.longStringType
        emailToUsers joinTable: [name: "AUTO_ROD_PVUSER", column: "EMAIL_USER", key: "AUTO_REASON_OF_DELAY_ID"], indexColumn: [name: "EMAIL_USERX"], lazy: false
        version false
    }

    public String toString() {
        return "${emailToUsers ? emailToUsers.join(",") : ''}"
    }
    String getInstanceIdentifierForAuditLog() {
        return ViewHelper.getMessage("app.label.auto.rod.appName")
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && oldValues && oldValues?.keySet()?.contains("globalDateRangeInformationAutoROD")) {
            withNewSession {
                AutoReasonOfDelay autoROD = AutoReasonOfDelay.read(id);
                if (oldValues?.keySet()?.contains("globalDateRangeInformationAutoROD"))
                    oldValues.put("globalDateRangeInformationAutoROD", autoROD.globalDateRangeInformationAutoROD?.toString())
            }
        }
        return [newValues: newValues, oldValues: oldValues]
    }
}