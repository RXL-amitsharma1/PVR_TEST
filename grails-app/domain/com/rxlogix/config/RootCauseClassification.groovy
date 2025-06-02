package com.rxlogix.config

class RootCauseClassification extends RcaEntity{

    String ownerApp
    Date hiddenDate
    Set<Long> linkIds = []

    static mapping = {
        datasource "pva"
        table name: "VW_PVC_RC_CLASS_DSP"
        version false
        id column: "ROOT_CAUSE_CLASS_ID"
        textDesc column: "TEXT_DESC"
        ownerApp column: "OWNER"
        hiddenDate column: "HIDDEN_DATE"
        linkIds joinTable: [name: "VW_PVC_LINK_LATE_RC_CLASS", column: "LATE_ID", key: "ROOT_CAUSE_CLASS_ID"], lazy: false, fetch: 'join'
    }

}