package com.rxlogix.enums

import com.rxlogix.util.ViewHelper

enum EvaluateCaseDateEnum {
    LATEST_VERSION('LATEST_VERSION'), ALL_VERSIONS("ALL_VERSIONS") , VERSION_PER_REPORTING_PERIOD('VERSION_PER_REPORTING_PERIOD'), VERSION_ASOF('VERSION_ASOF'), VERSION_ASOF_GENERATION_DATE('VERSION_ASOF_GENERATION_DATE')
    private final String val

    EvaluateCaseDateEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.dateRangeType.${this.name()}"
    }

    String getInstanceIdentifierForAuditLog() {
        ViewHelper.getMessage(getI18nKey())
    }
}
