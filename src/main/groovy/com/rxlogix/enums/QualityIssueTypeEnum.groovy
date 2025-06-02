package com.rxlogix.enums

enum QualityIssueTypeEnum {
    MISSING_DATA, DATA_ENTRY_ERROR, INCORRECT_DATA

    public getI18nKey() {
        return "app.QualityIssueTypeEnum.${this.name()}"
    }

    public static List<QualityIssueTypeEnum> list() {
        [MISSING_DATA, DATA_ENTRY_ERROR, INCORRECT_DATA]
    }
}
