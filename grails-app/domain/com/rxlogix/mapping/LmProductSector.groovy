package com.rxlogix.mapping


class LmProductSector implements Serializable{

    BigDecimal id
    String name
    String lang

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_SELECT_LIST1"
        cache: "read-only"
        version false
        id column: "COL_ID", type: "big_decimal", generator: "assigned"
        name column: "COL_DESC", sqlType: 'char'
        lang column: "lang_id", sqlType: 'char'
    }

    static constraints = {
        id(nullable: false, unique: false)
        name(blank: false, maxSize: 200)
    }
}
