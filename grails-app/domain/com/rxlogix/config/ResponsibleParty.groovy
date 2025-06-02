package com.rxlogix.config

class ResponsibleParty extends RcaEntity{

    String ownerApp
    Date hiddenDate
    Set<Long> linkIds = []

    static hasMany = [linkIds: Long]

    static mapping = {
        datasource "pva"
        table name: "VW_PVC_RESP_PARTY_DSP"
        version false
        id  column: "RESPONSIBLE_PARTY_ID"
        textDesc column: "TEXT_DESC"
        ownerApp column: "OWNER"
        hiddenDate column: "HIDDEN_DATE"
        linkIds joinTable: [name: "VW_PVC_LINK_ROOTCAU_RESP", column: "ROOT_CAUSE_ID", key: "ID"], lazy: false, fetch: 'join'
    }

}