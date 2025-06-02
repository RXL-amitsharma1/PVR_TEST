package com.rxlogix.mapping

class DataSourceInfo {

    BigDecimal id
    String sourceName
    String sourceAbbrev
    String caseNumberFieldName

    static mapping = {
        datasource "pva"
        table "pvr_app_source_info"

        cache: "read-only"
        version false

        id column: "SOURCE_ID", type: "big_decimal", generator: "assigned"
        sourceName column: "SOURCE_NAME"
        sourceAbbrev column: "SOURCE_ABBRV"
        caseNumberFieldName column: "JAVA_VARIABLE"
    }
}
