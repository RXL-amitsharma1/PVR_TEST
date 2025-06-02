package com.rxlogix.config

import com.rxlogix.enums.AssignmentRuleEnum
import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification

/**
 * Created by Chetan on 2/18/2016.
 */
@CollectionSnapshotAudit
class WorkflowRule {

    public static final NEW_TO_UNDER_REVIEW = "New to Under Review"
    public static final UNDER_REVIEW_TO_REVIEWED = "Under Review to Reviewed"

    public static final NEW_TO_INPROGRESS = "New To In Progress"
    public static final NEW_TO_NEEDCLARIFICATION = "New To Need Clarification"
    public static final INPROGRESS_TO_NEEDCLARIFICATION = "In Progress To Need Clarification"
    public static final INPROGRESS_TO_COMPLETE = "In Progress To Complete"
    public static final NEEDCLARIFICATION_TO_INPROGRESS = "Need Clarification To In Progress"
    public static final COMPLETE_TO_CLOSE = "Complete To Close"
    public static final COMPLETE_TO_REOPEN = "Complete To Reopen"
    public static final REOPEN_TO_INPROGRESS = "Reopen To In Progress"
    public static final REOPEN_TO_NEEDCLARIFICATION = "Reopen To Need Clarification"
    public static final CASE_DATA_TRIGGER_TO_NEW = "Quality Case Data : Trigger to New"
    public static final SAMPLING_TRIGGER_TO_NEW = "Other Quality Type #1 : Trigger to New"
    public static final SAMPLING2_TRIGGER_TO_NEW = "Other Quality Type #2 : Trigger to New"
    public static final SAMPLING3_TRIGGER_TO_NEW = "Other Quality Type #3 : Trigger to New"
    public static final SUBMISSION_TRIGGER_TO_NEW = "Quality Submission : Trigger to New"
    public static final PVC_TRIGGER_TO_NEW = "PVC : Trigger to New"
    public static final PVC_INBOUND_TRIGGER_TO_NEW = "PVC Inbound : Trigger to New"

    static final int NAME_MAX_LENGTH = 255

    static auditable =  true
    @AuditEntityIdentifier
    String name
    String description
    WorkflowConfigurationTypeEnum configurationTypeEnum

    WorkflowState initialState
    WorkflowState targetState
    ReportActionEnum defaultReportAction
    List<User> executors = []
    List<UserGroup> executorGroups = []
    List<User> assignedToUser = []
    List<UserGroup> assignedToUserGroup = []
    Boolean needApproval = false
    Integer autoExecuteInDays
    User owner

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    boolean isDeleted=false
    String assignmentRule
    boolean assignToUserGroup = false
    boolean autoAssignToUsers = false
    AdvancedAssignment advancedAssignment
    Integer dueInDays
    Boolean excludeWeekends = false
    Boolean autoExecuteExcludeWeekends = false



    static hasMany = [executors: User, executorGroups: UserGroup, assignedToUser: User, assignedToUserGroup: UserGroup]

    static constraints = {
        needApproval nullable: true
        autoExecuteInDays nullable: true, max: 999999999
        owner nullable: true
        description nullable: true, maxSize: 255
        name(validator: { val, obj ->
            if (val && val.length() > NAME_MAX_LENGTH)
                return ['maxSize.exceeded', obj.name, "${NAME_MAX_LENGTH}"]
            //Name is unique
            if (!obj.id || obj.isDirty("name")) {
                long count = WorkflowRule.createCriteria().count{
                    eq('name', val, [ignoreCase : true])
                    eq('configurationTypeEnum', obj.configurationTypeEnum)
                    eq('isDeleted', false)
                    if (obj.id){ne('id', obj.id)}
                }
                if (count) {
                    return "com.rxlogix.config.workflowRule.name.unique"
                }
            }
        })
        defaultReportAction nullable: true
        assignmentRule nullable: true
        advancedAssignment nullable: true, validator: { val, obj ->
            if (!val && obj.assignmentRule && obj.assignmentRule.equals(AssignmentRuleEnum.ADVANCED_RULE.name())) {
                return "com.rxlogix.config.WorkFlowRule.advancedAssignment.nullable"
            }
        }

        assignedToUserGroup nullable: true, validator: { val, obj ->
            if (!val && (obj.assignToUserGroup || obj.autoAssignToUsers)) {
                return "com.rxlogix.config.WorkFlowRule.assignTo.nullable"
            }
        }
        dueInDays nullable: true, max: 999999999
        excludeWeekends nullable: true
        autoExecuteExcludeWeekends nullable: true
    }

    static mapping = {
        table('WORKFLOW_RULE')
        needApproval column: "NEED_APPROVAL"
        autoExecuteInDays column: "EXECUTE_IN_DAYS"
        owner column: "OWNER"
        isDeleted column: "IS_DELETED"
        executors joinTable: [name: "WORKFlOW_EXECUTORS", column: "EXECUTOR_ID", key: "WORKFLOW_ID"], indexColumn: [name: "EXECUTORS_IDX"]
        executorGroups joinTable: [name: "WORKFlOW_EXECUTORS_GROUP", column: "EXECUTOR_GROUP_ID", key: "WORKFLOW_ID"], indexColumn: [name: "EXECUTORS_GROUP_IDX"]
        assignedToUser joinTable: [name: "WORKFlOW_ASSIGNED_TO_USER", column: "ASSIGNED_TO_USER_ID", key: "WORKFLOW_ID"], indexColumn: [name: "ASSIGNED_TO_USER_IDX"]
        assignedToUserGroup joinTable: [name: "WORKFlOW_ASSIGNED_TO_USERGROUP", column: "ASSIGNED_TO_USERGROUP_ID", key: "WORKFLOW_ID"], indexColumn: [name: "ASSIGNED_TO_USERGROUP_IDX"]
        assignmentRule column: "ASSIGNMENT_RULE"
        assignToUserGroup column: "ASSIGN_TO_USER_GROUP"
        autoAssignToUsers column: "AUTO_ASSIGN_TO_USERS"
        advancedAssignment column: "ADVANCED_ASSIGNMENT_ID"
        dueInDays column: 'DUE_IN_DAYS'
        excludeWeekends column: "EXCLUDE_WEEKENDS"
        autoExecuteExcludeWeekends column: "AE_EXCLUDE_WEEKENDS"
    }

    static namedQueries = {
        getAllByConfigurationTypeAndInitialState{WorkflowConfigurationTypeEnum typeEnum, WorkflowState initialState ->
            createAlias('targetState', 'target', CriteriaSpecification.INNER_JOIN)
            and {
                eq 'configurationTypeEnum', typeEnum
                eq 'isDeleted', false
                eq 'initialState',initialState
                eq 'target.isDeleted',false
            }
        }
    }

    def toWorkflowRuleMap() {
        [
            workflowRuleId: id,
            name: name,
            reportType: configurationTypeEnum.name(),
            description: description,
            initialState: initialState.name,
            targetState: targetState.name
        ]
    }

    Boolean canExecute(User user) {
        return (!executors && !executorGroups) ||
                executors?.find{it.id==user.id} ||
                executorGroups?.find {gr -> gr.getUsers()?.find{it.id==user.id} }
    }

    static boolean isDefaultWorkRuleExists() {
        return WorkflowRule.findAllByNameInList([WorkflowRule.NEW_TO_UNDER_REVIEW,WorkflowRule.UNDER_REVIEW_TO_REVIEWED])?.size() > 0
    }

    static def getDefaultWorkFlowRuleByType(com.rxlogix.enums.WorkflowConfigurationTypeEnum type) {
            return WorkflowRule.findByConfigurationTypeEnumAndInitialStateAndTargetState(type, WorkflowState.triggerWorkState,  WorkflowState.defaultWorkState)
    }

    public String toString() {
        return name
    }

}
