package com.rxlogix.enums

public enum LateEnum {
    LATE("Late"),
    NOT_LATE("Not Late"),
    EXCLUDED("Excluded"),
    FALSE_LATE("False Late")

    private final String val

    LateEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.late.${this.name()}"
    }
}