package com.rxlogix.user


import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit

@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['user'])
class ReportRequestEmailPreference {
    static auditable = [ignore:['preference']]
    boolean creationEmails = false
    boolean updateEmails = false
    boolean deleteEmails = false
    boolean workflowUpdate = false

    static belongsTo = [preference: Preference]

    static mapping = {
        table name: "REPORT_REQUEST_EMAIL_PREF"
        preference column: "PREFERENCE_ID"
        creationEmails column: "CREATE_EMAILS"
        updateEmails column: "UPDATE_EMAILS"
        deleteEmails column: "DELETE_EMAILS"
        workflowUpdate column: "WORKFLOW_UPDATE_EMAILS"
        version false
    }

    static constraints = {
        preference (nullable:false)
        creationEmails (nullable:false)
        updateEmails(nullable:false)
        deleteEmails(nullable: false)
        workflowUpdate(nullable: false)
    }

    static ReportRequestEmailPreference getDefaultValues(Preference preferenceInstance) {
        new ReportRequestEmailPreference(creationEmails: true, updateEmails: true, deleteEmails: true, workflowUpdate: true, preference: preferenceInstance)
    }
}