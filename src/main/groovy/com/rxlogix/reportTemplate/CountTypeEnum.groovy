package com.rxlogix.reportTemplate

public enum CountTypeEnum {
    PERIOD_COUNT("PERIOD_COUNT","PERIOD_COUNT"),
    CUMULATIVE_COUNT("CUMULATIVE_COUNT","CUMULATIVE_COUNT"),
    CUSTOM_PERIOD_COUNT("CUSTOM_PERIOD_COUNT","CUSTOM_PERIOD_COUNT"),
    PREVIOUS_PERIOD_COUNT("PREVIOUS_PERIOD_COUNT","CUSTOM_PERIOD_COUNT"),
    LAST_X_DAYS_PERIOD_COUNT("LAST_X_DAYS_PERIOD_COUNT","CUSTOM_PERIOD_COUNT"),
    LAST_X_WEEKS_PERIOD_COUNT("LAST_X_WEEKS_PERIOD_COUNT","CUSTOM_PERIOD_COUNT"),
    LAST_X_MONTHS_PERIOD_COUNT("LAST_X_MONTHS_PERIOD_COUNT","CUSTOM_PERIOD_COUNT"),
    LAST_X_YEARS_PERIOD_COUNT("LAST_X_YEARS_PERIOD_COUNT","CUSTOM_PERIOD_COUNT"),
    NEXT_X_DAYS_PERIOD_COUNT("NEXT_X_DAYS_PERIOD_COUNT","CUSTOM_PERIOD_COUNT"),
    NEXT_X_WEEKS_PERIOD_COUNT("NEXT_X_WEEKS_PERIOD_COUNT","CUSTOM_PERIOD_COUNT"),
    NEXT_X_MONTHS_PERIOD_COUNT("NEXT_X_MONTHS_PERIOD_COUNT","CUSTOM_PERIOD_COUNT"),
    NEXT_X_YEARS_PERIOD_COUNT("NEXT_X_YEARS_PERIOD_COUNT","CUSTOM_PERIOD_COUNT"),

    final String val
    final String type

    CountTypeEnum(String val, String type) {
        this.val = val
        this.type = type
    }

    String value() { return val }

    String type() { return type }

    public getI18nKey() {
        return "app.countTypeEnum.${this.name()}"
    }

    static String getTypeByCode(Character code) {
        if (code == '1') return "PERIOD_COUNT"
        else if (code == '2') return "CUMULATIVE_COUNT"
        else return "CUSTOM_PERIOD_COUNT"
    }
}