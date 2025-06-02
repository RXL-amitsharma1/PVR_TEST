package com.rxlogix.config

import com.rxlogix.enums.E2BReportFormatEnum
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['icsrProfileConfiguration'])
class DistributionChannel {
    static auditable =  [ignore:['deliveryReceipt']]
    String outgoingFolder
    E2BReportFormatEnum reportFormat
    String incomingFolder
    String markSubmittedAfter
    boolean deliveryReceipt
    String maxReportPerMsg
    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy

    static transients = ['auditLogPropertiesList']

    static constraints = {
        outgoingFolder nullable: true
        reportFormat nullable: true
        incomingFolder nullable: true
        markSubmittedAfter nullable: true
        deliveryReceipt nullable: true
        maxReportPerMsg nullable: true
    }

    static mapping = {
        table name: "DISTRIBUTION_CHANNEL"
        outgoingFolder column: "OUTGOING_FOLDER"
        reportFormat column: "REPORT_FORMAT"
        incomingFolder column: "INCOMING_FOLDER"
        markSubmittedAfter column: "MARK_SUBMITTED_AFTER"
        deliveryReceipt column: "DELIVERY_RECIPIENT"
        maxReportPerMsg column: "MAX_REPORT_PER_MSG"
    }

    List<String> getAuditLogPropertiesList() {
        return ['maxReportPerMsg','deliveryReceipt','markSubmittedAfter','incomingFolder','outgoingFolder','reportFormat']
    }

    String toString() {
        return reportFormat ?: ''
    }

}