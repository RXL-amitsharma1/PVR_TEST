package com.rxlogix.enums

enum GranularityEnum {
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    QUARTERLY("QUARTERLY"),
    ANNUALLY("ANNUALLY")

    final String code

    GranularityEnum(String val) {
        this.code = val
    }

    String code() { return code }

    public getI18nKey() {
        return "app.granularity.${this.name()}"
    }

    String getKey() {
        name()
    }


}