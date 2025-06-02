package com.rxlogix.enums

enum IcsrCaseStateEnum {

    ALL,
    ERROR,
    GENERATED,
    GENERATION_ERROR,
    PARSER_REJECTED,
    COMMIT_REJECTED,
    TRANS_SUCCESS,
    //    OVERDUE,
    OVERDUE_AND_DUE_SOON,
    //    PENDING, // Not getting used
    PENDING_ACTIONS,
    COMMIT_ACCEPTED,
    COMMIT_RECEIVED,
    READY_FOR_LOCAL_CP,
    SCHEDULED,
    SUBMISSION_NOT_REQUIRED,
    SUBMISSION_NOT_REQUIRED_FINAL,
    SUBMITTED,
    TRANSMITTED,
    TRANSMITTED_ATTACHMENT,
    TRANSMISSION_ERROR,
    TRANSMITTING,
    TRANSMITTING_ATTACHMENT

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.icsrCaseState.${this.name()}"
    }

    static List<IcsrCaseStateEnum> getStatusesOfAck(){
        return [COMMIT_REJECTED, PARSER_REJECTED, COMMIT_ACCEPTED, TRANS_SUCCESS]
    }

}