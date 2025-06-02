package com.rxlogix.enums

public enum ResponsiblePartyEnum {
    GPV_DISTRIBUTION_TEAM("GPV Distribution Team"),
    LOCAL_REGULATORY_AFFAIRS("Local Regulatory Affairs"),
    OTHER("Other"),
    SH_COUNTRY_SAFETY_HEAD("SH - Country Safety Head"),
    GPV_PLANNING_TEAM("GPV Planning Team"),
    GLOBAL_REGULATORY_AFFAIRS("Global Regulatory Affairs")

    private final String val

    ResponsiblePartyEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.responsibleParty.${this.name()}"
    }
}