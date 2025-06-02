package com.rxlogix.config

class PreventativeAction implements Serializable{

    String textDesc
    String ownerApp

    static mapping = {
        datasource "pva"
        table name: "VW_PVC_PREV_ACT_DSP"
        version false
        id column: "PREVENTATIVE_ACTION_ID"
        textDesc column: "TEXT_DESC"
        ownerApp column: "OWNER"
    }

    static constraints = {
        textDesc(nullable: false)
    }
}
