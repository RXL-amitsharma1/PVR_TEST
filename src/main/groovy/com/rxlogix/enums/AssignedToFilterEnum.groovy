package com.rxlogix.enums

public enum AssignedToFilterEnum {

    ME,
    MY_GROUPS,
    INDIVIDUAL_USER,
    INDIVIDUAL_USER_GROUPS

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    public String getI18nKey() {
        return "app.pvc.assignedTo.${this.name()}"
    }

}