package com.rxlogix.enums

import com.rxlogix.util.ViewHelper

enum ReportRequestFrequencyEnum {

    RUN_ONCE('Run Once'),
    HOURLY('Hours'),
    DAILY('Days'),
    WEEKLY('Weeks'),
    MONTHLY('Months'),
    YEARLY('Years')


    final String val

    ReportRequestFrequencyEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.reportRequestFrequency.${this.name()}"
    }

    String getKey() {
        name()
    }

    static List forSelect() {
        ReportRequestFrequencyEnum.values().collect {
            [name: it.name(), display: ViewHelper.getMessage(it?.getI18nKey())]
        }
    }

}