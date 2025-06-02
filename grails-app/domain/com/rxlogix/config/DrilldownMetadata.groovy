package com.rxlogix.config

import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import java.util.Date

abstract class DrilldownMetadata implements Serializable {

    WorkflowState workflowState
    Date dueDate
    String assignToName
    Date workflowStateUpdatedDate
    User assignedToUser
    UserGroup assignedToUserGroup
    Set<ActionItem> actionItems = []
    Set<Comment> comments = []
    Set<Capa8D> issues = []
    User assigner
    Date assigneeUpdatedDate
    String lastUpdatedIssue

    Date detectionDate = new Date()

    static mapWith = "none"

    static hasMany = [actionItems: ActionItem, comments: Comment, issues: Capa8D]

    static constraints = {
        workflowState nullable: true
        assignedToUser nullable: true
        assignedToUserGroup nullable: true
        dueDate nullable: true
        assignToName nullable: true
        assigner nullable: true
        assigneeUpdatedDate nullable: true
        lastUpdatedIssue nullable:true, maxSize: 8000
    }

    static mapping = {
        workflowState column: "WORKFLOW_STATE_ID"
        workflowStateUpdatedDate column: "WORKFLOW_STATE_UPDATED_DATE"
        assignedToUser column: 'ASSIGNED_TO_USER'
        assignedToUserGroup column: 'ASSIGNED_TO_USERGROUP'
        dueDate column: 'DUE_DATE'
        assignToName column: 'ASSIGNED_TO_NAME'
        assigner column: 'ASSIGNER'
        assigneeUpdatedDate column: 'ASSIGNEE_UPDATED_DATE'
        lastUpdatedIssue column: 'LAST_UPDATED_ISSUE'
    }

    void updateAssignedToName() {
        this.setAssignToName(assignedToUser?.fullName ?: assignedToUser?.username)
        this.markDirty()
    }

    void updateDueDate(WorkflowRule _workflowRule) {
        if(!workflowStateUpdatedDate) workflowStateUpdatedDate = new Date()
        WorkflowRule workflowRule = _workflowRule ?: WorkflowRule.getDefaultWorkFlowRuleByType(WorkflowConfigurationTypeEnum.PVC_REASON_OF_DELAY)
        if (workflowRule?.excludeWeekends) {
            dueDate = DateUtil.addDaysSkippingWeekends(workflowStateUpdatedDate, workflowRule?.dueInDays ?: 0)
        } else {
            dueDate = workflowStateUpdatedDate.plus(workflowRule?.dueInDays ?: 0)
        }
        this.markDirty()
    }
}
