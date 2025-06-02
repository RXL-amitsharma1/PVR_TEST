package com.rxlogix.enums

public enum SortEnum {
    ASCENDING("asc"),
    DESCENDING("desc")

    private final String val

    SortEnum(String val) {
        this.val = val
    }

    String value() { return val }

    static SortEnum valueOfName( String name ) {
        values().find { it.val == name }
    }
}
