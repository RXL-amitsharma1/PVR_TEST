package com.rxlogix.enums
import com.rxlogix.util.ViewHelper

public enum UnitTypeEnum {
    SENDER("SENDER"),
    RECIPIENT("RECIPIENT"),
    BOTH("BOTH")

    private final String val

    UnitTypeEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.unitType.${this.name()}"
    }

    static List<UnitTypeEnum> searchBy(String search) {
        if (!search) {
            return []
        }
        search = search.toLowerCase()
        values().findAll { ViewHelper.getMessage(it.i18nKey).toLowerCase().contains(search) }
    }
}