package com.rxlogix.config

import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class SchedulerConfigParams {
    static auditable = true

    ReportConfiguration configuration
    Date runDate
    Set<Comment> comments = []
    User primaryPublisherContributor

    boolean isDeleted = false

    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy


    static belongsTo = [ReportConfiguration]
    static hasMany = [comments: Comment, publisherContributors: User]

    static mapping = {
        table name: "SCHEDULER_CFG_PARAMS"
        runDate column: "RUN_DATE"
        configuration column: "CONFIG_ID"
        comments joinTable: [name: "SCHED_CFG_PAR_COMMENTS", column: "COMMENT_ID", key: "SCHED_CFG_PAR_ID"]
        publisherContributors joinTable: [name: "SCHED_CFG_PARA_P_C_USERS", column: "USER_ID", key: "SCHED_CFG_PAR_ID"], indexColumn: [name: "SHARED_WITH_IDX"]
        primaryPublisherContributor column: "PRIMARY_P_CONTRIBUTOR"
    }
    static constraints = {
        primaryPublisherContributor nullable: true
        publisherContributors nullable: true
    }

    public String toString() {
        return configuration.reportName + " : " + runDate.format(DateUtil.DATEPICKER_DATE_TIME_FORMAT)
    }

    transient def getContributorsAsMap() {
        Map result = [configParamsId: id]
        Set contributors = []
        if (publisherContributors) contributors.addAll(publisherContributors)
        if (primaryPublisherContributor) contributors.add(primaryPublisherContributor)
        if (contributors) {
            result.contributors = contributors.collect { it.fullName }.join(", ")
            result.primaryId = primaryPublisherContributor?.id
            result.contributorsId = publisherContributors?.collect { it.id }?.join(",")
        } else {
            result.contributors = ""
            result.primaryId = ""
            result.contributorsId = ""
        }
        return result
    }
}