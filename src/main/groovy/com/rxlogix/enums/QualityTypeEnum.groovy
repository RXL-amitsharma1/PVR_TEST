package com.rxlogix.enums

enum QualityTypeEnum {
    MANDATORY, OPTIONAL

    public getI18nKey() {
        return "app.QualityType.${this.name()}"
    }

}