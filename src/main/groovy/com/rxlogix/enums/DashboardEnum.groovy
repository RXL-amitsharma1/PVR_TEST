package com.rxlogix.enums

enum DashboardEnum {
    PVR_MAIN,
    PVQ_MAIN,
    PVP_MAIN,
    PVC_MAIN,
    PVP_PUBLIC,
    PVR_PUBLIC,
    PVQ_PUBLIC,
    PVC_PUBLIC,
    PVR_USER,
    PVQ_USER,
    PVP_USER,
    PVC_USER


    public getI18nKey() {
        return "app.dashboard.DashboardEnum.${this.name()}"
    }

    static getItemsToSelect() {
        [PVR_PUBLIC,
         PVQ_PUBLIC,
         PVC_PUBLIC,
         PVP_PUBLIC,
         PVR_USER,
         PVQ_USER,
         PVC_USER,
         PVP_USER
        ]
    }
    static getPvcItemsToSelect() {
        [PVC_PUBLIC, PVC_USER]
    }
    static getRestrictedTypes() {
         [PVR_USER, PVQ_USER, PVC_USER, PVP_USER]
    }
}
