package com.rxlogix.user

import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit

@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['user'])
class PVCEmailPreference {
    static auditable = [ignore:['preference']]
    boolean assignedToMe = false
    boolean assignedToMyGroup = false
    boolean workflowStateChange = false

    static belongsTo = [preference: Preference]

    static mapping = {
        table name: "PVC_EMAIL_PREF"
        preference column: "PREFERENCE_ID"
        assignedToMe column: "ASSIGNED_TO_ME"
        assignedToMyGroup column: "ASSIGNED_TO_MY_GROUP"
        workflowStateChange column: "WORKFLOW_STATE_CHANGES"
        version false
    }

    static constraints = {
        preference (nullable:false)
        assignedToMe (nullable:false)
        assignedToMyGroup(nullable:false)
        workflowStateChange(nullable: false)
    }

    static PVCEmailPreference getDefaultValues(Preference preferenceInstance) {
        new PVCEmailPreference(assignedToMe: true, assignedToMyGroup: true, workflowStateChange: true, preference: preferenceInstance)
    }
}