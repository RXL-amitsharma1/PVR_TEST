package com.rxlogix.user

import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit

@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['user'])
class AIEmailPreference {
    static auditable = [ignore:['preference']]
    boolean creationEmails = false
    boolean updateEmails = false
    boolean jobEmails = false

    static belongsTo = [preference: Preference]

    static mapping = {
        table name: "ACTION_ITEM_EMAIL_PREF"
        preference column: "PREFERENCE_ID"
        creationEmails column: "CREATE_EMAILS"
        updateEmails column: "UPDATE_EMAILS"
        jobEmails column: "JOB_EMAILS"
        version false
    }

    static constraints = {
        preference (nullable:false)
        creationEmails (nullable:false)
        updateEmails(nullable:false)
        jobEmails(nullable: false)
    }

    static AIEmailPreference getDefaultValues(Preference preferenceInstance) {
        new AIEmailPreference(creationEmails: true, updateEmails: true, jobEmails: true, preference: preferenceInstance)
    }
}