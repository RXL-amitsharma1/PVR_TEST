package com.rxlogix.enums

public enum ReasonEnum {
    DISTRIBUTION_ERROR("distribution error"),
    NOT_LATE("not late – a. no due date, b. submission upon HA request, c. permissable delaylate distribution"),
    OVERSIGHT("oversight"),
    PLANNING_ERROR("planning error"),
    TECHNICAL_ERROR("technical error"),
    NEW_REQUIREMENT("new requirements"),
    RESOURCE_ERROR("resource error"),
    OTHER("other")

    private final String val

    ReasonEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.reason.${this.name()}"
    }
}