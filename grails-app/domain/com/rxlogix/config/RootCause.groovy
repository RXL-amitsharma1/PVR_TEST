package com.rxlogix.config

class RootCause extends RcaEntity{

    String ownerApp

    Set<Long> linkIds = []
    Set<Long> responsiblePartyIds = []
    Set<Long> rootCauseSubCategoryIds = []
    Date hiddenDate

    static hasMany = [linkIds: Long, responsiblePartyIds: Long, rootCauseSubCategoryIds: Long]

    static mapping = {
        datasource "pva"
        table name: "VW_PVC_ROOT_CAUSE_DSP"
        version false
        id column: "ROOT_CAUSE_ID"
        textDesc column: "TEXT_DESC"
        ownerApp column: "OWNER"
        hiddenDate column: "HIDDEN_DATE"
        linkIds joinTable: [name: "VW_PVC_LINK_LATE_ROOTCAUSE", column: "LATE_ID", key: "ID"], lazy: false, fetch: 'join'
        responsiblePartyIds joinTable: [name: "VW_PVC_LINK_ROOTCAU_RESP", column: "ID", key: "ROOT_CAUSE_ID"], lazy:false, fetch: 'join'
        rootCauseSubCategoryIds joinTable: [name: "VW_PVC_LINK_ROOTCAU_RCSUB", column: "ROOT_CAUSE_SUB_CAT_ID", key: "ROOT_CAUSE_ID"], lazy: false, fetch: 'join'
    }

}