package com.rxlogix.commandObjects

import com.rxlogix.dto.LateReasonDTO
import com.rxlogix.enums.IcsrCaseStateEnum
import com.rxlogix.enums.LateEnum
import grails.validation.Validateable

class CaseSubmissionCO implements Validateable{

    Long id
    String icsrCaseId
    String profileName
    String currentState
    IcsrCaseStateEnum icsrCaseState
    String comment
    String commentJ
    Long justificationId
    String reportingDestinations
    Date dueDate
    LateEnum late
    Long profileId
    String queryId
    String caseNumber
    Long versionNumber
    Date submissionDate
    List<LateReasonDTO> lateReasons
    byte[] submissionDocument
    String submissionFilename
    Long processedReportId
    String updatedBy
    Date localSubmissionDate
    String timeZoneId
    String scheduleDateJSON

    static constraints = {
        id(nullable:true)
        icsrCaseId(nullable:false)
        profileName(nullable:false)
        currentState(nullable:true)
        icsrCaseState(nullable:false,validator: { val ->
            if (val && !(val in [IcsrCaseStateEnum.SUBMITTED, IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED,IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL])) {
                return false
            }
            return true
        })
        comment(nullable:true)
        commentJ(nullable:true)
        justificationId(nullable:true)
        reportingDestinations(nullable:false)
        dueDate(nullable:true)
        late(nullable:true)
        profileId(nullable:true)
        queryId(nullable:true)
        caseNumber(nullable: true)
        versionNumber(nullable:true)
        submissionDate(nullable:true)
        lateReasons(nullable:true)
        submissionDocument(nullable:true)
        submissionFilename(nullable:true)
        processedReportId(nullable: true)
        updatedBy(nullable: true)
        localSubmissionDate(nullable: true)
        timeZoneId(nullable: true)
        scheduleDateJSON(nullable: true)
    }
}
