package com.rxlogix.enums

/**
 * Created for query type dropdown for PVR-44232
 */
public enum QueryDropdownEnum {
    NON_VALID_CASES("Non-Valid Cases"),
    DELETED_CASES("Deleted Cases"),
    ICSR_PADER_AGENCY_CASES("ICSR PADER Case")

    private final String val

    QueryDropdownEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public String getI18nKey() {
        return "app.queryDropdown.${this.name()}"
    }
}