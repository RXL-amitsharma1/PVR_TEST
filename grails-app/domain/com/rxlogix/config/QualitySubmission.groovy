package com.rxlogix.config


import com.rxlogix.enums.QualityTypeEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.User
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class QualitySubmission implements Serializable {

    static auditable =  true
    Long reportId
    Long executedReportId
    String caseNumber
    Long versionNumber
    String submissionIdentifier
    String errorType
    String metadata
    Long triageAction
    boolean isDeleted = false
    String priority
    String justification
    String entryType
    User assignedToUser
    UserGroup assignedToUserGroup
    Set<Comment> comments = []

    QualityTypeEnum mandatoryType
    String fieldName
    String value
    String fieldLocation

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    WorkflowState workflowState
    Date workflowStateUpdatedDate
    Date dueDate
    Long qualityIssueTypeId

    Long tenantId
    Long executedTemplateId
    User assigner
    Date assigneeUpdatedDate
    Long caseId

    static hasMany = [actionItems: ActionItem, comments: Comment, issues: Capa8D, qualityIssueDetails: QualityIssueDetail]

    static constraints = {
        reportId nullable: true
        dueDate nullable: true
        justification nullable: true, maxSize: 4000
        priority nullable: true
        assignedToUser nullable: true
        assignedToUserGroup nullable: true
        triageAction nullable: true
        executedReportId nullable: true
        mandatoryType nullable:true
        fieldName nullable:true
        value nullable:true
        fieldLocation nullable:true
        qualityIssueTypeId nullable:true
        executedTemplateId nullable:true
        assigner nullable: true
        assigneeUpdatedDate nullable: true
        caseId nullable: true
    }

    static mapping = {
        table('QUALITY_SUBMISSION')
        id column: 'ID', generator: "sequence", params: [sequence: "QUALITY_SUBMISSION_ID"]
        reportId column: 'REPORT_ID'
        executedReportId column: 'EXEC_REPORT_ID'
        dueDate column: 'DUE_DATE'
        caseNumber column: 'CASE_NUM'
        versionNumber column: 'VERSION_NUM'
        errorType column: 'ERROR_TYPE'
        metadata column: 'METADATA'
        submissionIdentifier column: 'SUBMISSION_IDENTIFIER'
        triageAction column: 'TRIAGE_ACTION'
        isDeleted column: 'ISDELETED'
        priority column: 'PRIORITY'
        justification column: 'JUSTIFICATION'
        entryType column: 'ENTRY_TYPE'
        assignedToUser column: 'ASSIGNED_TO_USER'
        assignedToUserGroup column: 'ASSIGNED_TO_USERGROUP'
        actionItems joinTable: [name: "QUALITY_SUBMISSION_ACTION_ITEM", column: "ACTION_ITEM_ID", key: "QUALITY_SUBMISSION_ID"]
        comments joinTable: [name: "QUALITY_SUBMISSION_COMMENTS", column: "COMMENT_ID", key: "QUALITY_SUBMISSION_ID"]
        issues joinTable: [name: "QUALITY_SUBMISSION_ISSUES", column: "ISSUE_ID", key: "QUALITY_SUBMISSION_ID"]
        qualityIssueDetails joinTable: [name: "QUALITY_SUB_ISSUE_DETAILS", column: "QUALITY_ISSUE_DETAIL_ID", key: "QUALITY_SUBMISSION_ID"]

        dateCreated column: 'DATE_CREATED'
        lastUpdated column: 'LAST_UPDATED'
        createdBy column: 'CREATED_BY'
        modifiedBy column: 'MODIFIED_BY'
        tenantId column: 'TENANT_ID'
        workflowState column: "WORKFLOW_STATE_ID"
        workflowStateUpdatedDate column: "WORKFLOW_STATE_UPDATED_DATE"
        qualityIssueTypeId column: "QUALITY_ISSUE_TYPE_ID"
        executedTemplateId column: "EXECUTED_TEMPLATE_ID"
        version false
        assigner column: 'ASSIGNER'
        assigneeUpdatedDate column: 'ASSIGNEE_UPDATED_DATE'
        caseId column: 'CASE_ID'
    }

    String toString() {
        if (!caseNumber && !errorType) return ""
        return "$caseNumber - $errorType"
    }

    static def getErrorTypes(Long tenantId){
        createCriteria().list{
            eq("isDeleted", false)
            eq("tenantId", tenantId)
            projections{
                distinct("errorType")
            }
            order("errorType", "asc")
        }

    }

    static def getActionItemIds(Map params, Long tenantId){
        createCriteria().list{
            eq("tenantId", tenantId)
            eq("caseNumber", params.caseNumber)
            eq("errorType", params.errorType)
            eq("isDeleted", false)
            createAlias("actionItems", "actionItems")
            order("actionItems." + "${params.sort}", params.direction)
            firstResult(Integer.valueOf(params.start))
            maxResults(Integer.valueOf(params.length))
            projections{
                property("actionItems.id")
            }
            eq("actionItems.isDeleted", false)
        }
    }
  
    static def getErrorTypeCaseCount(Long tenantId){
        createCriteria().list{
            eq("tenantId", tenantId)
            eq("isDeleted", false)
            projections{
                rowCount("rowCount")
                groupProperty("errorType")
            }
            order("rowCount", "desc")
        }
    }

    Boolean isAssigned(User user) {
        return assignedToUser?.id==user.id || assignedToUserGroup?.getUsers()?.find{it.id==user.id}
    }

    String getInstanceIdentifierForAuditLog() {
        if (!caseNumber && !errorType) return ""
        return "$caseNumber - $errorType"
    }

    void updateDueDate(WorkflowRule _workflowRule) {
        if (!workflowStateUpdatedDate) workflowStateUpdatedDate = new Date()
        WorkflowRule workflowRule = _workflowRule ?: WorkflowRule.getDefaultWorkFlowRuleByType(WorkflowConfigurationTypeEnum.QUALITY_SUBMISSION)
        if (workflowRule?.excludeWeekends) {
            dueDate = DateUtil.addDaysSkippingWeekends(workflowStateUpdatedDate, workflowRule?.dueInDays ?: 0)
        } else {
            dueDate = workflowStateUpdatedDate.plus(workflowRule?.dueInDays ?: 0)
        }
        this.markDirty()
    }
}