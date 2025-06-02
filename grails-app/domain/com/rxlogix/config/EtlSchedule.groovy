package com.rxlogix.config


import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class EtlSchedule {
    static auditable =  true
    @AuditEntityIdentifier
    String scheduleName
    String startDateTime
    String repeatInterval
    boolean isDisabled = false
    boolean isInitial = true

    String emailToUsers
    EmailConfiguration emailConfiguration

    boolean sendSuccessEmail = false
    boolean pauseLongRunningETL = false
    Integer sendEmailETLInterval = 0
    boolean emailTrigger = true
    boolean emailTriggerForLongRunning = true


    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapping = {
        table name: "ETL_SCHEDULE"
        scheduleName column: "SCHEDULE_NAME"
        startDateTime column: "START_DATETIME"
        repeatInterval column: "REPEAT_INTERVAL"
        isDisabled column: "DISABLED"
        isInitial column: "IS_INITIAL"
        emailToUsers column: "EMAIL_TO_USERS"
        emailConfiguration column: "EMAIL_CONFIGURATION_ID"
        emailConfiguration cascade: 'all'
        sendSuccessEmail column: "SEND_SUCCESS_EMAIL"
        pauseLongRunningETL column: "PAUSE_LONG_RUNNING_ETL"
        sendEmailETLInterval column: "SEND_EMAIL_ETL_INTERVAL"
        emailTrigger column: "EMAIL_TRIGGER"
        emailTriggerForLongRunning column: "EMAIL_FOR_LONG_RUNNING"

    }

    static constraints = {
        scheduleName blank: false, unique: true, maxSize: 20
        repeatInterval nullable: false
        startDateTime nullable: false
        emailToUsers nullable: false, maxSize: 4000
        emailConfiguration nullable: true
        sendSuccessEmail nullable: false
        pauseLongRunningETL nullable: false
        sendEmailETLInterval nullable: false
        emailTrigger nullable: false
        emailTriggerForLongRunning nullable: false
    
    }

    public String toString() {
        return scheduleName
    }
}
