package com.rxlogix.mapping

class LmStudiesLmProtocolsMapping implements Serializable{

        BigDecimal protocolId
        BigDecimal studyId

        static mapping = {
            datasource "pva"
            table "VW_STUDY_PROJECT_LINK"

            cache: "read-only"
            version false

            id composite: ['protocolId', 'studyId']
            studyId column: "STUDY_KEY"
            protocolId column: "PROJECT_ID"
        }

    }

