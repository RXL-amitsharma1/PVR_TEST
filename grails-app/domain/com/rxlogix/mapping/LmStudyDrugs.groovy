package com.rxlogix.mapping


class LmStudyDrugs implements Serializable {

    BigDecimal studyId
    BigDecimal productId
    boolean isImp

    static mapping = {
        datasource "pva"
        table "VW_STUDY_PRODUCT_ALL"

        cache: "read-only"
        version false

        id composite: ['studyId', 'productId']
        studyId column: "STUDY_KEY", type: "big_decimal"
        productId column: "PRODUCT_ID", type: "big_decimal"
        isImp formula: 'CASE WHEN NVL(TGT_PROD_TYPE_ID,2)=2 THEN 1 ELSE 0 END' //PVR-8816 Logic is like for NUll and 2 its IMP rest not

    }

    static constraints = {
    }
}
