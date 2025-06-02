package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['qualitySampling','qualitySubmission','qualityCaseData'])
class QualityIssueDetail implements Serializable {

    static auditable =  true
    Long rootCauseId
    Long responsiblePartyId
    Long correctiveActionId
    Long preventativeActionId

    Date correctiveDate
    Date preventativeDate
    String investigation
    String summary
    String actions
    boolean isPrimary = false

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy

    static constraints = {
        investigation nullable: true, maxSize: 32000
        summary nullable: true, maxSize: 32000
        actions nullable: true, maxSize: 32000
        correctiveDate nullable: true
        preventativeDate nullable: true
        preventativeActionId nullable: true
        correctiveActionId nullable: true
        rootCauseId nullable: true
        responsiblePartyId nullable: true
    }

    static mapping = {
        table('QUALITY_ISSUE_DETAIL')
        rootCauseId column: 'ROOT_CAUSE_ID'
        responsiblePartyId column: 'RESPONSIBLE_PARTY_ID'
        correctiveActionId column: 'CORRECTIVE_ACTION_ID'
        correctiveDate column: 'CORRECTIVE_DATE'
        preventativeActionId column: 'PREVENTATIVE_ACTION_ID'
        preventativeDate column: 'PREVENTATIVE_DATE'
        investigation column: 'INVESTIGATION'
        summary column: 'SUMMARY'
        actions column: 'ACTIONS'
        isPrimary column: 'IS_PRIMARY'

        dateCreated column: 'DATE_CREATED'
        lastUpdated column: 'LAST_UPDATED'
        createdBy column: 'CREATED_BY'
        modifiedBy column: 'MODIFIED_BY'
    }

    static beforeInsert = {
        dateCreated = dateCreated ?: new Date()
        lastUpdated = new Date()
    }

    static beforeUpdate = {
        lastUpdated = new Date()
    }

    static belongsTo = [QualityCaseData, QualitySampling, QualitySubmission]

    String getInstanceIdentifierForAuditLog() {
        return "$rootCauseId - $responsiblePartyId"
    }
}