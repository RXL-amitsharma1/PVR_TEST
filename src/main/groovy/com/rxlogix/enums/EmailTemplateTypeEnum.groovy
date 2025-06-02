package com.rxlogix.enums

enum EmailTemplateTypeEnum {
    PUBLIC,
    USER

    public getI18nKey() {
        return "com.rxlogix.enums.EmailTemplateTypeEnum.${this.name()}"
    }


}
