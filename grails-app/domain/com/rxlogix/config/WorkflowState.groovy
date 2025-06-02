package com.rxlogix.config

import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class WorkflowState {

    public static final NEW_NAME = "New"
    public static final UNDER_REVIEW_NAME = "Under Review"
    public static final REVIEWED_NAME = "Reviewed"
    public static final CLOSED_NAME = "Closed"
    public static final INPROGRESS_NAME = "In Progress"
    public static final NEEDCLARIFICATION_NAME = "Need Clarification"
    public static final REOPEN_NAME = "Reopen"
    public static final COMPLETE_NAME = "Complete"
    public static final TRIGGER = "Trigger"

    static final int NAME_MAX_LENGTH = 255

    transient def userService

    static auditable =  true
    @AuditEntityIdentifier
    String name
    String description
    boolean display = true
    boolean finalState = false
    boolean isDeleted = false

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    static hasMany = [reportActions:WorkflowStateReportAction]

    static constraints = {
        description nullable: true, maxSize: 255
        reportActions nullable: true
        name(validator: { val, obj ->
            if (val && val.length() > NAME_MAX_LENGTH)
                return ['maxSize.exceeded', obj.name, "${NAME_MAX_LENGTH}"]
            //Name is unique
            if (!obj.id || obj.isDirty("name")) {
                long count = WorkflowState.createCriteria().count{
                    ilike('name', "${val}")
                    eq('isDeleted', false)
                    if (obj.id){ne('id', obj.id)}
                }
                if (count) {
                    return "com.rxlogix.config.workflowState.name.unique"
                }
            }
        })
    }

    static mapping = {
        table('WORKFLOW_STATE')
        isDeleted column: "IS_DELETED"
        reportActions cascade: 'all'
    }

    Map toWorkflowStateMap() {
        [
           workflowStateId: id,
           name: name,
           description: description,
           display: display,
           finalState: finalState
        ]
    }

    List<ReportActionEnum> getReportActionsAsList() {
        Long userId = (userService?.currentUser)?.id ?: 0

        Set<WorkflowStateReportAction> reportActionsList = reportActions
        if (userId > 0 && reportActions) {
            reportActionsList = reportActions?.findAll { ra ->
                (!ra.executors && !ra.executorGroups) ||
                        ra.executors?.find { it.id == userId } ||
                        ra.executorGroups?.find { gr -> gr.getUsers()?.find { it.id == userId } }
            }
        }
        return reportActionsList?.collect { it.reportAction }?.sort { it.ordinal() }
    }

    static WorkflowState getDefaultWorkState() {
        return WorkflowState.findByNameAndIsDeleted(NEW_NAME,false)
    }

    static WorkflowState getUnderReviewWorkState() {
        return WorkflowState.findByNameAndIsDeleted(UNDER_REVIEW_NAME,false)
    }

    static WorkflowState getReviewedWorkState() {
        return WorkflowState.findByNameAndIsDeleted(REVIEWED_NAME,false)
    }

    static boolean isDefaultWorkStateExists() {
        return WorkflowState.countByNameAndIsDeleted(NEW_NAME,false) > 0
    }

    boolean isFinalState(WorkflowConfigurationTypeEnum type) {
        return (id in getFinalStatesForType(type)?.collect{it.id})
    }

    static WorkflowState getTriggerWorkState() {
        return WorkflowState.findByNameAndIsDeleted(TRIGGER,false)
    }

    static List<WorkflowState> getFinalStatesForType(WorkflowConfigurationTypeEnum type) {
        return WorkflowState.executeQuery("from WorkflowState st where " +
                "(st.id not in (select wr.initialState.id from WorkflowRule wr where wr.isDeleted=false and wr.configurationTypeEnum='${type.name()}' ))" +
                " and " +
                "(st.id in (select wr.targetState.id from WorkflowRule wr where wr.isDeleted=false and wr.configurationTypeEnum='${type.name()}' )) ")

    }

    static List<WorkflowState> getAllWorkFlowStatesForAdhoc() {
        Set<WorkflowState> workflowStates = []
        WorkflowRule.findAllByConfigurationTypeEnumAndIsDeleted(WorkflowConfigurationTypeEnum.ADHOC_REPORT, false).each {
            workflowStates.add(it.initialState)
            workflowStates.add(it.targetState)
        }
        return workflowStates.toList().findAll { it }.sort { it.name }
    }

    static List<WorkflowState> getAllWorkFlowStatesForAggregate() {
        return getAllWorkFlowStatesForType(WorkflowConfigurationTypeEnum.PERIODIC_REPORT)
    }

    static List<WorkflowState> getAllWorkFlowStatesForType(WorkflowConfigurationTypeEnum type) {
        Set<WorkflowState> workflowStates = []
        WorkflowRule.findAllByConfigurationTypeEnumAndIsDeleted(type, false).each {
            workflowStates.add(it.initialState)
            workflowStates.add(it.targetState)
        }
        return workflowStates.toList().findAll { it }.sort { it.name }
    }

    public String toString() {
        return name
    }

}
