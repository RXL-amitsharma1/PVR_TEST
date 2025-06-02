package com.rxlogix.dto


import com.rxlogix.config.ExecutedIcsrTemplateQuery
import com.rxlogix.enums.IcsrCaseStateEnum
import grails.validation.Validateable

import java.sql.Timestamp

class CaseStateUpdateDTO implements Validateable {

    ExecutedIcsrTemplateQuery executedIcsrTemplateQuery
    String caseNumber
    Long versionNumber
    String status
    String previousStatus
    Boolean lateFlag
    String reportingDestination
    Date dueDate
    String comment
    String commentJ
    Long justificationId
    String error
    Byte[] attachment
    String attachmentFilename
    Date submissionDate
    Date transmissionDate
    Date transmittedDate
    String updatedBy
    String ackFileName
    Date ackReceiveDate
    List<LateReasonDTO> lateReasons
    Long processedReportId
    Date localSubmissionDate
    String timeZoneId
    Date dateTransmissionAttach
    Date dateTransmittedAttach
    String attachmentAckFileName
    Date dateAckRecievedAttach

    static constraints = {
        executedIcsrTemplateQuery nullable: false
        caseNumber nullable: false, blank: false
        versionNumber nullable: false
        status nullable: false
        lateFlag nullable: true
        reportingDestination nullable: true, validator: { val, obj ->
            if (obj.status == IcsrCaseStateEnum.SUBMITTED.toString() && !val) {
                return 'reportingDestination.required'
            }
            return true
        }
        dueDate nullable: true
        comment nullable: true
        commentJ nullable: true
        justificationId nullable: true
        error nullable: true
        attachment nullable: true
        attachmentFilename nullable: true
        submissionDate nullable: true, validator: { val, obj ->
            if (obj.status == IcsrCaseStateEnum.SUBMITTED.toString() && !val) {
                return 'submissionDate.required'
            }
            return true
        }
        transmissionDate nullable: true, validator: { val, obj ->
            if (obj.status == IcsrCaseStateEnum.TRANSMITTING.toString() && !val) {
                return 'transmissionDate.required'
            }
            return true
        }
        transmittedDate nullable: true, validator: { val, obj ->
            if (obj.status == IcsrCaseStateEnum.TRANSMITTED.toString() && !val) {
                return 'transmittedDate.required'
            }
            return true
        }
        updatedBy nullable: true
        lateReasons nullable: true
        errorText nullable: true
        ackFileName nullable: true, validator: { val, obj ->
            if (obj.previousStatus == IcsrCaseStateEnum.TRANSMITTED.toString() && (obj.status in IcsrCaseStateEnum.statusesOfAck*.toString()) && !val) {
                return 'ackFileName.required'
            }
            return true
        }
        attachmentAckFileName nullable: true
        ackReceiveDate nullable: true, validator: { val, obj ->
            if (obj.previousStatus == IcsrCaseStateEnum.TRANSMITTED.toString() && (obj.status in IcsrCaseStateEnum.statusesOfAck*.toString()) && !val) {
                return 'ackReceiveDate.required'
            }
            return true
        }
        //In Grails 3.x getters of DTO/Command are part of constrain properties.
        // https://stackoverflow.com/questions/47274278/command-object-fails-validation-with-getter-not-associated-to-property-grails-3
        transmissionDateTime nullable: true
        submissionDateTime nullable: true
        transmittedDateTime nullable: true
        ackReceiveDateTime nullable: true
        dueDateTime nullable: true
        processedReportId nullable: true
        localSubmissionDate nullable: true
        timeZoneId nullable: true
        dateTransmissionAttach nullable: true
        dateTransmittedAttach nullable: true
        dateAckRecievedAttach nullable: true
        dateTimeTransmissionAttach nullable: true
        dateTimeTransmittedAttach nullable: true
        dateTimeAckRecievedAttach nullable: true
        previousStatus nullable: true
    }


    String getProfileName() {
        return executedIcsrTemplateQuery?.usedConfiguration?.reportName
    }

    String getErrorText() {
        error?.replaceAll("'", "''")
    }

    byte[] getSubmissionDocument() {
        if (attachment) {
            return attachment
        }
        return new byte[0]
    }


    Timestamp getSubmissionDateTime() {
        // In case of SUBMISSION_NOT_REQUIRED Date should be blank.
        if (this.status in ([IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED.toString(), IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL.toString()])) {
            return null
        }
        submissionDate ? new Timestamp(submissionDate.time) : null
    }

    Timestamp getTransmissionDateTime() {
        transmissionDate ? new Timestamp(transmissionDate.time) : null
    }

    Timestamp getTransmittedDateTime() {
        transmittedDate ? new Timestamp(transmittedDate.time) : null
    }

    Timestamp getDateTimeTransmissionAttach() {
        dateTransmissionAttach ? new Timestamp(dateTransmissionAttach.time) : null
    }

    Timestamp getDateTimeTransmittedAttach() {
        dateTransmittedAttach ? new Timestamp(dateTransmittedAttach.time) : null
    }

    Timestamp getDateTimeAckRecievedAttach() {
        dateAckRecievedAttach ? new Timestamp(dateAckRecievedAttach.time) : null
    }

    Timestamp getDueDateTime() {
        dueDate ? new Timestamp(dueDate.time) : null
    }

    Timestamp getAckReceiveDateTime() {
        ackReceiveDate ? new Timestamp(ackReceiveDate.time) : null
    }

    Timestamp getLocalSubmissionDate() {
        // In case of SUBMISSION_NOT_REQUIRED Date should be blank.
        if (this.status in ([IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED.toString(), IcsrCaseStateEnum.SUBMISSION_NOT_REQUIRED_FINAL.toString()])) {
            return null
        }
        localSubmissionDate ? new Timestamp(localSubmissionDate.time) : null
    }

}
