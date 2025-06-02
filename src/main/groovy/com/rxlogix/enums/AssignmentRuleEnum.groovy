package com.rxlogix.enums

public enum AssignmentRuleEnum {

    BASIC_RULE("Basic Rule"),
    ADVANCED_RULE("Advanced Rule")


    private final String val

    AssignmentRuleEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.assignmentRule.${this.name()}"
    }
}