package com.rxlogix.reportTemplate

public enum PercentageOptionEnum {

    NO_PERCENTAGE,
    BY_TOTAL,
    BY_SUBTOTAL,
    INTERVAL_TO_CUMULATIVE

    public getI18nKey() {
        return "app.percentageOptionEnum.${this.name()}"    }
}