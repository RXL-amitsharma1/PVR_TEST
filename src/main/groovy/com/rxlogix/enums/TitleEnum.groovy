package com.rxlogix.enums

public enum TitleEnum {
    MR("Mr"),
    MRS("Mrs"),
    MISS("Miss"),
    MS("Ms"),
    DR("Dr"),
    DR_PH("DrPh"),
    DRA("Dra"),
    INFORMATION_MANAGER("InformationManager"),
    M_EN_C("MENC"),
    MA("Ma"),
    MAG("Mag"),
    MANAGER_PHARMACOVIGILANCE_UNIT("ManagerPharmacovigilanceUnit"),
    MD("Md"),
    MME("Mme"),
    MPH("Mph"),
    PHARM_D("PharmaD"),
    PROF("Prof"),
    Q_MA_DEL("QMaDel"),
    RN("Rn"),
    RPH("Rph"),
    SUBDIRECCION_GENERAL_DE_PRODUCTOS_SANITA("SubdireccionGeneralDeProductosSanita"),
    代表取締役社長("代表取締役社長"),
    理事長("理事長"),
    代表取締役("代表取締役")

    private final String val

    TitleEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.title.${this.name()}"
    }
}