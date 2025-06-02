package com.rxlogix.mapping

class LmProductProductSector implements Serializable  {

    BigDecimal productId
    BigDecimal productSectorId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL1_COL3"

        cache: "read-only"
        id composite: ['productId', 'productSectorId']
        version false
        productId column: "COL1_ID"
        productSectorId column: "COL2_ID"
    }
}
