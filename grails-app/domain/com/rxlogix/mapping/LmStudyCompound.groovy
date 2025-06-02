package com.rxlogix.mapping

class LmStudyCompound implements Serializable {

    BigDecimal studyId
    String compoundId
    String lang

    static mapping = {
        datasource "pva"
        table "VW_STUDY_CLIN_REF_LINK"
        cache: "read-only"
        version false
        id composite: ['studyId', 'compoundId', 'lang']
        studyId column: "STUDY_KEY", type: "big_decimal"
        compoundId column: "CLIN_REF_NUM", sqlType: 'VARCHAR2(8003 CHAR)'
        lang column: 'LANG_ID'
    }

    static constraints = {
    }
}