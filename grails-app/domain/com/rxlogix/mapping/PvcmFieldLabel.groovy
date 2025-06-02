package com.rxlogix.mapping

class PvcmFieldLabel implements Serializable {

    BigDecimal id
    String displayText
    String section

    static mapping = {
        datasource "pva"
        table "VW_PVCM_FIELD_LABELS"

        cache: "read-only"
        version false

        id column: "ID", type: "big_decimal", generator: "assigned"
        displayText column: "DISPLAY_TEXT"
        section column: 'SECTION'
    }
}
