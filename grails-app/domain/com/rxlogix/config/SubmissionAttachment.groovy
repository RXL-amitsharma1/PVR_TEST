package com.rxlogix.config

import com.rxlogix.config.ReportSubmission
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['reportSubmission'])
class SubmissionAttachment {
    static auditable =  [ignore:['data']]
    String name
    byte[] data
    String ext

    //todo:fix
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "-"
    String modifiedBy = "-"

    static belongsTo = [submission: ReportSubmission]

    static mapping = {
        table name: "SUBMISSION_ATTACH"

        name column: "NAME"
        data column: "DATA", lazy: true
    }

    static constraints = {

        ext(nullable: true)
        name(maxSize: 255)
        data(nullable: true, maxSize: 20971520)
    }

    static beforeInsert = {
        lastUpdated = new Date() // required to update lastUpdated field when attachment is created.
    }

    static beforeUpdate = {
        lastUpdated = new Date() // required to update lastUpdated field when attachment is updated.
    }

    String toString() {
        return name
    }
}

