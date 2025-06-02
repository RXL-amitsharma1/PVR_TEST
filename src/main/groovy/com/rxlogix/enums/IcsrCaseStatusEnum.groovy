package com.rxlogix.enums

import groovy.transform.CompileStatic

@CompileStatic
enum IcsrCaseStatusEnum {
    DELETED,
    SCHEDULED,
    READY_FOR_GENERATION,
    START,
    DB_INPROGRESS,
    DB_IN_PROGRESS,
    NOT_QUALIFIED,
    QUALIFIED,
    GENERATION_ERROR,
    ERROR,
    DB_IN_PROGRESS_LOCKED,
    GENERATED,
    MANUAL,
    DB_FIRST_CHECKPOINT_LOCKED,
    NO_DEVICE,
    DB_BATCH_ERROR,
    PRE_QUALIFIED,
    NOT_MULTI_REPORTABLE

    public static List<IcsrCaseStatusEnum> getSuccessCaseStatusList() {
        return [PRE_QUALIFIED, QUALIFIED, SCHEDULED, DB_FIRST_CHECKPOINT_LOCKED, DB_IN_PROGRESS_LOCKED, GENERATED, GENERATION_ERROR, ERROR]
    }
}
