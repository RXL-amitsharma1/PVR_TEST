package com.rxlogix.enums

import com.rxlogix.util.ViewHelper

enum ReasonOfDelayAppEnum {
    PVC, PVQ, PVC_Inbound
    public getI18nKey() {
        return "rod.appType.${this.name()}"
    }

    String getInstanceIdentifierForAuditLog() {
        ViewHelper.getMessage(getI18nKey())
    }
}