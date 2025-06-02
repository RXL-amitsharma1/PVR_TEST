package com.rxlogix.enums

public enum JobExecutionHistoryStatusEnum {

    SUCCESS("Success"),
    FAILURE("Failure"),
    IN_PROGRESS("In Progress")

    private final String val

    JobExecutionHistoryStatusEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.jobExecutionHistoryStatus.${this.name()}"
    }
}
