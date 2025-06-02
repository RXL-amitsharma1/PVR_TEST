package com.rxlogix.enums

public enum DateRangeTypeCaseEnum {
    CASE_RECEIPT_DATE('CM_INIT_REPT_DATE'), CASE_LOCKED_DATE('CM_DATE_LOCKED'),  SAFTEY_RECEIPT_DATE('CM_SAFETY_DATE'), CREATION_DATE('CM_CREATE_TIME'), SUBMISSION_DATE('CMR_DATE_SUBMITTED'), CASE_RECEIPT_DATE_J('CM_INIT_REPT_DATE_J'), REPORT_DUE_DATE('REPORT_DUE_DATE'), REPORT_AWARE_DATE('REPORT_AWARE_DATE'), REPORT_SAFETY_DATE('REPORT_SAFETY_DATE')

    private final String val

    DateRangeTypeCaseEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.dateRangeType.${this.name()}"
    }
}