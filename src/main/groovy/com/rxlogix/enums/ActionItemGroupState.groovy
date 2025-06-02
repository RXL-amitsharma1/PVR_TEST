package com.rxlogix.enums

public enum ActionItemGroupState {

    WAITING("Waiting"),
    OVERDUE("Overdue"),
    CLOSED("All Closed");

    final String value

    ActionItemGroupState(String value){
        this.value = value
    }

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.actionItemGroupState.${this.name()}"
    }

}