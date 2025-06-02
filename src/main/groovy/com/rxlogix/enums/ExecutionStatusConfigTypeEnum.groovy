package com.rxlogix.enums

import com.rxlogix.util.ViewHelper

enum ExecutionStatusConfigTypeEnum{
    REPORTS,
    INBOUND_COMPLIANCE,
    ICSR_PROFILE

    String getKey(){
        name()
    }

    public getI18nKey() {
        return "app.executionStatus.configType.${this.name()}"
    }

    String getInstanceIdentifierForAuditLog(){
        ViewHelper.getMessage(getI18nKey())
    }

}



