package com.rxlogix.enums

enum TopColumnTypeEnum {
    FIRST,
    LAST,
    TOTAL


    public getI18nKey() {
        return "app.topColumnType.${this.name()}"
    }

    static getItemsToSelect() {
        [FIRST,
         LAST,
         TOTAL]
    }
}
