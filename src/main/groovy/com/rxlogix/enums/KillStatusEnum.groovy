package com.rxlogix.enums

enum KillStatusEnum {

    NEW("New"),
    IN_PROGRESS("In-Progress"),
    KILLED("Killed"),
    NOT_KILLED("Not Killed")

    private String val

    KillStatusEnum(String val) {
        this.val = val
    }

    String value() { return val }
}