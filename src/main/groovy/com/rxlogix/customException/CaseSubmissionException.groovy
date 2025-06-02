package com.rxlogix.customException

import com.rxlogix.commandObjects.CaseSubmissionCO

class CaseSubmissionException extends RuntimeException {

    final String caseNumber
    final Long versionNumber
    final String errorCode

    public CaseSubmissionException(String caseNumber, Long versionNumber, String errorCode, String defaultMessage) {
        super(defaultMessage);
        this.caseNumber = caseNumber
        this.versionNumber = versionNumber
        this.errorCode = errorCode
    }

    public CaseSubmissionException(CaseSubmissionCO caseSubmissionCO, String errorCode, String defaultMessage) {
        super(defaultMessage);
        this.caseNumber = caseSubmissionCO.caseNumber
        this.versionNumber = caseSubmissionCO.versionNumber
        this.errorCode = errorCode
    }

}
