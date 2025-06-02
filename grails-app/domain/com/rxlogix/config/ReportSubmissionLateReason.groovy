package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['reportSubmission'])
class ReportSubmissionLateReason {
    static auditable = true
    String responsible
    String reason
    boolean isPrimary = false
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static belongsTo = [reportSubmission: ReportSubmission]

    static constraints = {

    }
}
