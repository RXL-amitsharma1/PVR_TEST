package com.rxlogix.config.publisher

import com.rxlogix.config.*
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.converters.JSON
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
import grails.util.Holders
import groovy.json.JsonBuilder
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['configuration', 'periodicReportConfiguration','executedReportConfiguration', 'executedPeriodicReportConfiguration'])
class PublisherConfigurationSection {
    static auditable =  [ignore:['templateFileData']]
    String name
    Integer sortNumber
    byte[] templateFileData
    String filename
    UserGroup assignedToGroup
    User reviewer
    User approver
    User author
    PublisherTemplate publisherTemplate
    String parameterValuesJson
    WorkflowState workflowState
    User lockedBy
    String lockCode
    String destination
    Date dueDate
    Integer dueInDays
    Integer pendingVariable
    Integer pendingManual
    Integer pendingComment
    TaskTemplate taskTemplate

    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "-"
    String modifiedBy = "-"

    static hasMany = [actionItems: ActionItem, publisherExecutedTemplates: PublisherExecutedTemplate, comments: Comment]
    static belongsTo = [configuration: ReportConfiguration, executedConfiguration: ExecutedReportConfiguration]

    static mapping = {
        table name: "PUBLISHER_CFG_SECT"
        name column: "NAME"
        lockedBy column: "LOCKED_USER_ID"
        taskTemplate column: "TASK_TEMPLATE_ID"
        workflowState column: "final_workflow_state_id"
        parameterValuesJson column: "parameter_values_json"
    }

    static constraints = {
        templateFileData(nullable: true, maxSize: 20971520)
        lockCode(nullable: true, maxSize: 1050)
        name(maxSize: 400)
        filename nullable: true
        dueInDays nullable: true
        reviewer nullable: true
        approver nullable: true
        author nullable: true
        configuration nullable: true
        executedConfiguration nullable: true
        assignedToGroup nullable: true
        workflowState nullable: true
        lockedBy nullable: true
        destination nullable: true
        dueDate nullable: true
        publisherTemplate nullable: true
        parameterValuesJson nullable: true
        comments(nullable: true)
        pendingComment(nullable: true)
        pendingManual(nullable: true)
        pendingVariable(nullable: true)
        taskTemplate(nullable: true)
        comments cascade: 'all-delete-orphan'
        actionItems cascade: 'all-delete-orphan'
        publisherExecutedTemplates cascade: 'all-delete-orphan'

    }

    transient Map getParameterValues() {
        return parameterValuesJson ? JSON.parse(parameterValuesJson) : [:]
    }

    transient Map putParameterValues(String key, value) {
        Map map = getParameterValues()
        map.put(key, value)
        setParameterValues(map)
    }

    transient void setParameterValues(Map map) {
        if (map) {
            Map cleanMap = map.findAll { it.key }
            if (cleanMap) {
                parameterValuesJson = new JsonBuilder(cleanMap).toPrettyString()
                return
            }
        }
        parameterValuesJson = null
    }

    transient PublisherExecutedTemplate getLastPublisherExecutedTemplates() {
        publisherExecutedTemplates?.find { it.status == PublisherExecutedTemplate.Status.FINAL } ?: publisherExecutedTemplates?.find { it.status == PublisherExecutedTemplate.Status.DRAFT }
    }

    transient PublisherExecutedTemplate getDraftPublisherExecutedTemplates() {
        publisherExecutedTemplates?.find { it.status == PublisherExecutedTemplate.Status.DRAFT }
    }

    public boolean isVisible(User user=null) {
        User currenUser = user ?: Holders.applicationContext.getBean("userService").currentUser
        if (currenUser.isAdmin() || (currenUser.id == this.author?.id) || (currenUser.id == this.reviewer?.id) || (currenUser.id == this.approver?.id) || (executedConfiguration?.ownerId == currenUser.id) ||
                (executedConfiguration?.primaryPublisherContributorId == currenUser.id) || (executedConfiguration?.publisherContributors?.find { it.id == currenUser.id })) return true
        UserGroup userGroup = this.assignedToGroup
        if (userGroup) {
            return userGroup.users.find { it.id == currenUser.id }
        } else {
            return this.executedConfiguration.isVisible(currenUser)
        }
    }

    transient PublisherExecutedTemplate.Status getState() {
        if (!publisherExecutedTemplates || publisherExecutedTemplates.size() == 0) return PublisherExecutedTemplate.Status.EMPTY
        if (publisherExecutedTemplates.find { it.status == PublisherExecutedTemplate.Status.FINAL }) return PublisherExecutedTemplate.Status.FINAL
        if (publisherExecutedTemplates.find { it.status == PublisherExecutedTemplate.Status.DRAFT }) return PublisherExecutedTemplate.Status.DRAFT
        return PublisherExecutedTemplate.Status.EMPTY
    }

    String toString() {
        return name
    }
}
