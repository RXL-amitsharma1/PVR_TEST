package com.rxlogix.dto

import grails.validation.Validateable

class CaseAckSubmissionDTO implements Validateable{

    Long tenantId
    Long caseId
    Long versionNumber
    Long processedReportId
    String icsrMessageNumber
    String transmissionAckCode
    String safetyReportId
    String localReportNumber
    String localMessageNumber
    String reportAckCode
    String ackMessageComment

    static constraints = {
        tenantId nullable: false
        caseId nullable: false
        versionNumber nullable: false
        processedReportId nullable: false
        icsrMessageNumber nullable: true
        transmissionAckCode nullable: true
        safetyReportId nullable: true
        localReportNumber nullable: true
        localMessageNumber nullable: true
        reportAckCode nullable: true
        ackMessageComment nullable: true
    }
}
