package com.rxlogix.enums

enum FrequencyEnum {

    MINUTELY('Minutes'),
    HOURLY('Hourly'),
    DAILY ('Daily'),
    WEEKLY('Weekly'),
    WEEKDAYS('Weekdays'),
    MONTHLY('Monthly'),
    YEARLY('Yearly'),
    RUN_ONCE('Run Once')

    final String val
    FrequencyEnum(String val){
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.frequency.${this.name()}"
    }

    String getKey() {
        name()
    }

}