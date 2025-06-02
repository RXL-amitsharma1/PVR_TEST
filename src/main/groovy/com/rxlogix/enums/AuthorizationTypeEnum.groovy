package com.rxlogix.enums

public enum AuthorizationTypeEnum {
    MARKETED_DRUG("Marketed Drug"),
    INVESTIGATIONAL_DRUG("Investigational Drug"),
    PARTIAL_CHANGE_DRUG("Partial Change Drug")

    private final String val

    AuthorizationTypeEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.authorizationType.${this.name()}"
    }
}