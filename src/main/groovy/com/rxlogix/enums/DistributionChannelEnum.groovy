package com.rxlogix.enums

public enum DistributionChannelEnum {
    PV_GATEWAY("PV_GATEWAY"),
    EXTERNAL_FOLDER("EXTERNAL_FOLDER"),
    EMAIL("Email"),
    PAPER_MAIL("Paper Mail")

    private final String val

    DistributionChannelEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.distributionChannel.${this.name()}"
    }
}