package com.rxlogix.user

import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
import org.joda.time.DateTimeZone
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['user'])
class Preference {
    static auditable = [ignore:['lastUpdated','createdBy','dateCreated','user','modifiedBy']]

    Locale locale
    String timeZone
    String theme = 'gradient_blue'
    boolean checkSimilarCases = false
    String userPreferences

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static hasOne = [actionItemEmail: AIEmailPreference, reportRequestEmail: ReportRequestEmailPreference , pvcEmail: PVCEmailPreference, pvqEmail: PVQEmailPreference]

    static belongsTo = [user:User]

    static mapping = {
        table name: "PREFERENCE"
        locale column: "LOCALE"
        timeZone column: "TIME_ZONE"
        theme column: "THEME"
        userPreferences column: "user_preferences"
        checkSimilarCases column: "SIM_CASES"
        actionItemEmail lazy: true, cascade: 'all'
        reportRequestEmail lazy: true, cascade: 'all'
        pvcEmail lazy: true, cascade: 'all'
        pvqEmail lazy: true, cascade: 'all'
    }

    static constraints = {
        locale (nullable:false)
        timeZone (nullable:true)
        userPreferences (nullable:true)
        theme(nullable:false)
        checkSimilarCases (nullable: false)
        createdBy(nullable: false, maxSize: 255)
        modifiedBy(nullable: false, maxSize: 255)
        actionItemEmail(nullable: true)
        reportRequestEmail(nullable: true)
        pvcEmail(nullable: true)
        pvqEmail(nullable: true)
    }

    String getTimeZone() {
        if (timeZone) {
            return timeZone
        } else {
            return DateTimeZone.UTC.ID
        }
    }

    def getInstanceIdentifierForAuditLog() {
        return user.getInstanceIdentifierForAuditLog()
    }

    public String toString() {
        return "Preference for - $user"
    }

}
