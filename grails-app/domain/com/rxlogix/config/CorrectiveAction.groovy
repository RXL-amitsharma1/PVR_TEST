package com.rxlogix.config

class CorrectiveAction implements Serializable{

    String textDesc
    String ownerApp

    static mapping = {
        datasource "pva"
        table name: "VW_PVC_CORR_ACT_DSP"
        version false
        id column: "CORRECTIVE_ACTION_ID"
        textDesc column: "TEXT_DESC"
        ownerApp column: "OWNER"
    }

    static constraints = {
        textDesc(nullable: false, maxSize: 255)
    }

}
