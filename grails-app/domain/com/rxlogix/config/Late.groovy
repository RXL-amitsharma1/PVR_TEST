package com.rxlogix.config

class Late extends RcaEntity{
    String ownerApp
    Long lateType
    Date hiddenDate

    Set<Long> rootCauseIds = []

    static hasMany = [rootCauseIds: Long, rootCauseClassIds: Long]

    static mapping = {
        datasource "pva"
        table name: "VW_PVC_LATE_DSP"
        version false
        id column: "LATE_ID"
        textDesc column: "TEXT_DESC"
        ownerApp column: "OWNER"
        lateType column: "IS_LATE"
        hiddenDate column: "HIDDEN_DATE"
        rootCauseIds joinTable: [name: "VW_PVC_LINK_LATE_ROOTCAUSE", column: "ID", key: "LATE_ID"], lazy: false, fetch: 'join'
        rootCauseClassIds joinTable: [name: "VW_PVC_LINK_LATE_RC_CLASS", column: "ROOT_CAUSE_CLASS_ID", key: "LATE_ID"], lazy: false, fetch: 'join'
    }

}