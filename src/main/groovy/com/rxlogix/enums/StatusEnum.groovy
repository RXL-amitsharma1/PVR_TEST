package com.rxlogix.enums

public enum StatusEnum {

    OPEN,
    IN_PROGRESS,
    NEED_CLARIFICATION,
    CLOSED;

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    public String getI18nKey() {
        return "app.statusenum.${this.name()}"
    }

}