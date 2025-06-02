package com.rxlogix.config

import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.enums.AuditLogCategoryEnum
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class Comment {

    String textData
    PublisherConfigurationSection publisherConfigurationSection
    PublisherReport publisherReport
    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        textData blank: false, maxSize: 8000
        publisherConfigurationSection nullable: true
        publisherReport nullable: true
    }

    static belongsTo = [ExecutedReportConfiguration, ReportResult,  PublisherConfigurationSection, PublisherReport,QualityCaseData, QualitySubmission, DrilldownCLLData,SchedulerConfigParams]

    static mapping = {
        table name: "COMMENT_TABLE"
        textData column: "NOTE"
        publisherConfigurationSection column: "PUBLISHER_SECTION_ID"
        publisherReport column: "PUBLISHER_REPORT_ID"

    }

    public String toString() {
        return textData
    }

    String getInstanceIdentifierForAuditLog() {
        return (textData.length() < 50 ? textData : (textData.substring(0, 49) + "..."))
    }
}