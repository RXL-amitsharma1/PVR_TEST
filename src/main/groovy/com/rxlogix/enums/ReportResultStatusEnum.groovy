package com.rxlogix.enums

public enum ReportResultStatusEnum {
    NEW("NEW"), NON_REVIEWED("NON_REVIEWED"), REVIEWED("REVIEWED")

    private final String val

    ReportResultStatusEnum(String val) {
        this.val = val
    }

    String value() { return val }
}
