package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['reportRequest'])
class ReportRequestAttachment {
    static auditable =  true
    String name

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    FileAttachment fileAttachment

    static belongsTo = [reportRequest: ReportRequest]

    static mapping = {
        table name: "REPORT_REQUEST_ATTACH"

        name column: "NAME"
        fileAttachment lazy: true, cascade: 'all'
    }

    static constraints = {
        name(maxSize: 255)
        fileAttachment(nullable: true)
    }

    static beforeInsert = {
        lastUpdated = new Date() // required to update lastUpdated field when attachment is created.
    }

    static beforeUpdate = {
        lastUpdated = new Date() // required to update lastUpdated field when attachment is updated.
    }

    public String toString() {
        return name
    }
}
