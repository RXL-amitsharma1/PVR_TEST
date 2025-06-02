package com.rxlogix.config.publisher

import com.rxlogix.config.ActionItem
import com.rxlogix.config.Comment
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.WorkflowState
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.util.Holders
@CollectionSnapshotAudit
class PublisherReport {
    static auditable =  [ignore:['data']]
    @AuditEntityIdentifier
    String name
    Integer numOfExecution = 0
    String comment
    User owner
    byte[] data
    boolean isDeleted
    Boolean published = false

    Date dueDate
    User lockedBy
    String lockCode
    String destination
    UserGroup assignedToGroup
    User reviewer
    User approver
    User author
    WorkflowState workflowState
    WorkflowState qcWorkflowState

    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "-"
    String modifiedBy = "-"

    static belongsTo = [executedReportConfiguration: ExecutedReportConfiguration]
    static hasMany = [actionItems: ActionItem, comments: Comment]

    static mapping = {
        table name: "PUBLISHER_REPORT"
        name column: "NAME"
        comment column: "COMMNT"
        executedReportConfiguration column: "ex_config_id"
        data column: "DATA", lazy: true
        lockedBy column: "LOCKED_USER_ID"
    }

    static constraints = {
        data(maxSize: 20971520)
        comment(length: 4000, nullable: true)
        comments(nullable: true)
        comments cascade: 'all-delete-orphan'

        dueDate(nullable: true)
        lockedBy(nullable: true)
        lockCode(nullable: true)
        destination(nullable: true)
        assignedToGroup(nullable: true)
        reviewer(nullable: true)
        approver(nullable: true)
        author(nullable: true)
        workflowState(nullable: true)
        qcWorkflowState(nullable: true)
    }

    public boolean isVisible(User user = null) {
        User currenUser = user ?: Holders.applicationContext.getBean("userService").currentUser
        if (currenUser.isAdmin() || (currenUser.id == this.owner?.id) || (currenUser.id == this.author?.id) || (currenUser.id == this.reviewer?.id) || (currenUser.id == this.approver?.id) || (executedReportConfiguration?.ownerId == currenUser.id) ||
                (executedReportConfiguration?.primaryPublisherContributorId == currenUser.id) || (executedReportConfiguration?.publisherContributors?.find { it.id == currenUser.id })) return true
        UserGroup userGroup = this.assignedToGroup
        if (userGroup) {
            return userGroup.users.find { it.id == currenUser.id }
        } else {
            return this.executedReportConfiguration.isVisible(currenUser)
        }
    }
    String toString() {
        return name
    }
}
