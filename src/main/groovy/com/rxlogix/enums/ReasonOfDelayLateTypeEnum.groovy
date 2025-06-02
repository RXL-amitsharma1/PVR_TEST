package com.rxlogix.enums

import com.rxlogix.util.ViewHelper

enum ReasonOfDelayLateTypeEnum {
    LATE(1L, "Late", "PVC"), NOT_LATE(2L, "Not Late", "PVC"),
    EXCLUDE(3L, "Exclude", "PVC"), ERROR(4L, "Error", "PVQ"),
    NOT_ERROR(5L, "No Error", "PVQ"), LATE_INB(6L, "Late", "PVC_Inbound"),
    NOT_LATE_INB(7L, "Not Late", "PVC_Inbound"), NOT_QUALIFIED(8L, "Not Qualified", "PVC_Inbound")
    public getI18nKey() {
        return "rod.lateType.${this.name()}"
    }

    String getInstanceIdentifierForAuditLog() {
        ViewHelper.getMessage(getI18nKey())
    }

    private final Long val
    private final String text
    public final String ownerApp

    ReasonOfDelayLateTypeEnum(Long val, String text, String ownerApp) {
        this.val = val
        this.text = text
        this.ownerApp = ownerApp
    }

    Long value() {
        return val
    }

    String text(){
        return text
    }

    String ownerApp(){
        return ownerApp
    }

    static HashMap<Long, ReasonOfDelayLateTypeEnum> lateTypeIdKeyMap = [1L: LATE, 2L: NOT_LATE, 3L: EXCLUDE, 4L: ERROR, 5L: NOT_ERROR, 6L: LATE_INB, 7L: NOT_LATE_INB, 8L: NOT_QUALIFIED]
}