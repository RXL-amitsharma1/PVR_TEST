package com.rxlogix.enums

public enum ActionItemFilterEnum {
    MY_OPEN("My Open Action Items"),
    MY_ALL("My All Action Items"),
    EXECUTED_REPORT_ALL("Executed Report Related Action Items"),
    ALL("All Action Items")

    final String value

    ActionItemFilterEnum(String value) {
        this.value = value
    }
}