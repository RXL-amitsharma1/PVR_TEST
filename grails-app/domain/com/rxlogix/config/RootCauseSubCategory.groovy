package com.rxlogix.config

class RootCauseSubCategory extends RcaEntity {

    String ownerApp
    Date hiddenDate

    Set<Long> linkIds = []

    static hasMany = [linkIds: Long]

    static mapping = {
        datasource "pva"
        table name: "VW_PVC_RC_SUB_CAT_DSP"
        version false
        id column: "ROOT_CAUSE_SUB_CAT_ID"
        textDesc column: "TEXT_DESC"
        ownerApp column: "OWNER"
        hiddenDate column: "hIDDEN_DATE"
        linkIds joinTable: [name: "VW_PVC_LINK_ROOTCAU_RCSUB", column: "ROOT_CAUSE_ID", key: "ROOT_CAUSE_SUB_CAT_ID"], lazy: false, fetch: 'join'
    }
}