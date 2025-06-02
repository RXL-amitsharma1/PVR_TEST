package com.rxlogix.enums

import com.rxlogix.util.ViewHelper

enum ReasonOfDelayFieldEnum {
    Issue_Type,
    Root_Cause,
    Root_Cause_Class,
    Root_Cause_Sub_Cat,
    Resp_Party,
    Corrective_Action,
    Preventive_Action,
    Corrective_Date,
    Preventive_Date,
    Investigation,
    Summary,
    Actions

    public getI18nKey() {
        return "rod.fieldType.${this.name()}"
    }

    String getMessage() {
        ViewHelper.getMessage(getI18nKey())
    }
}