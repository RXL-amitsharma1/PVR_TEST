package com.rxlogix.commandObjects

import grails.validation.Validateable
import org.springframework.web.multipart.MultipartFile

class BulkCaseSubmissionCO implements Validateable {

    Integer tenantId
    String username
    String password
    String approvalDate
    Boolean bulk
    String recipient
    String submissionDate
    String time
    String timeZone
    String status
    String dueDate
    MultipartFile file
    String comment
    String caseData
    String currentStatus
    Long justificationId
}
