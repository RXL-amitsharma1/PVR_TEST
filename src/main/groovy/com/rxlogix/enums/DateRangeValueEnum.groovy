package com.rxlogix.enums

enum DateRangeValueEnum {
RELATIVE('relative'),CUSTOM('custom'),CUMULATIVE('cumulative'),PR_DATE_RANGE('interval')
    private final String val

    DateRangeValueEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.dateRangeType.${this.name()}"
    }
}