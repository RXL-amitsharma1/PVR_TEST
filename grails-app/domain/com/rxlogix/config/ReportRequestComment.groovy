package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit

/**
 * Created by Chetan on 2/28/2016.
 */
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['reportRequest'])
class ReportRequestComment {
    static auditable =  true
    @AuditEntityIdentifier
    String reportComment

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy

    //Transient fields
    boolean isDeleted
    boolean newObj

    static constraints = {
        reportComment blank: false
        isDeleted bindable:true
    }

    static belongsTo = [reportRequest: ReportRequest]

    static transients = ['isDeleted', 'newObj']

    static mapping = {
        table name: "REPORT_REQUEST_COMMENT"
        autoTimestamp false // required to maintain ordering of comments when creating report request

        reportComment column: 'REPORT_COMMENT', sqlType: "VARCHAR(8000)"
    }

    static beforeInsert = {
        lastUpdated = new Date() // required to update lastUpdated field when comment is created.
    }

    static beforeUpdate = {
        lastUpdated = new Date() // required to update lastUpdated field when comment is updated.
    }

    public String toString() {
        return reportComment
    }

}
