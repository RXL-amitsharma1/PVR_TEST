package com.rxlogix.enums

public enum QueryLevelEnum {
    CASE("Case"),
    EVENT("Event"),
    PRODUCT("Product"),
    PRODUCT_EVENT("Product & Event"),
    SUBMISSION("Submission")

    private final String val

    QueryLevelEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.queryLevel.${this.name()}"
    }
}