package com.rxlogix.mapping

class LmDrugWithStudy implements Serializable {

    BigDecimal productId
    BigDecimal productFamilyId
    String name
    String lang

    static mapping = {
        datasource "pva"
        table "VW_STUDY_DRUG_ALL"

        cache: "read-only"
        version false

        id composite: ['productId', 'lang']
        productId column: "PRODUCT_ID", type: "big_decimal"
        name column: "PRODUCT_NAME"
        productFamilyId column: "PROD_FAMILY_ID"
        lang column: "lang_id", sqlType: 'char'
    }

    static constraints = {
        name(blank:false, maxSize:70)
    }
}
