package com.rxlogix.enums


enum TransferTypeEnum {
    OWNERSHIP("OWNERSHIP"),
    SHAREWITH("SHAREWITH"),
    SHAREOWNED("SHAREOWNED")

    final String value

    TransferTypeEnum(String value) {
        this.value = value
    }

    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.ownership.TransferTypeEnum.${this.name()}"
    }
}