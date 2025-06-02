package com.rxlogix.config

import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['workflowState'])
class WorkflowStateReportAction {
    static auditable =  true
    List<User> executors = []
    List<UserGroup> executorGroups = []
    ReportActionEnum reportAction

    static belongsTo = [workflowState: WorkflowState]

    static hasMany = [executors: User, executorGroups: UserGroup]

    static constraints = {
        executors nullable: true
        executorGroups nullable: true
    }

    static mapping = {
        table('WORKFLOW_STATE_ACTION')
        reportAction column: "REPORT_ACTION"
        workflowState column: "WORKFlOW_STATE_ID"
        executors joinTable: [name: "WORKFlOWSTATE_ACT_EXTRS", column: "EXECUTOR_ID", key: "WORKFLOW_ID"], indexColumn: [name: "EXECUTORS_IDX"]
        executorGroups joinTable: [name: "WORKFlOWSTATE_ACT_EXTRS_GR", column: "EXECUTOR_GROUP_ID", key: "WORKFLOW_ID"], indexColumn: [name: "EXECUTORS_GROUP_IDX"]
    }

    public String toString() {
        return reportAction
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if(newValues && oldValues == null && newValues.executors == [])
            newValues.put("executors", "Any User")
        if(newValues && oldValues) {
            if(newValues.executors == [])
                newValues.put("executors", "Any User")
            if(oldValues.executors == [])
                oldValues.put("executors", "Any User")
        }
        return [newValues: newValues, oldValues: oldValues]
    }
}
