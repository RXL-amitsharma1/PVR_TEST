package com.rxlogix.mapping

import grails.util.Holders

class ArgusFile implements Serializable {
    Long caseId
    Long seqNum
    Long blobSize
    String fileName
    String notes
    byte[] data

    static constraints = {
        blobSize nullable: true
        fileName nullable: true
        data nullable: true
        notes nullable: true
    }

    static mapping = {
        if(Holders.config.dataSources.safetySource) {
            datasource "safetySource"
        }
        table "case_notes_attach"
        id composite: ['caseId', 'seqNum']
        version false
        caseId column: 'CASE_ID'
        seqNum column: 'SEQ_NUM'
        blobSize column: "BLOBSIZE"
        fileName column: "FILETYPE"
        notes column: "NOTES"
        data column: "DATA", lazy: true
    }

    @Override
    String toString(){
        return "${fileName}-${caseId}-${seqNum}"
    }
}
