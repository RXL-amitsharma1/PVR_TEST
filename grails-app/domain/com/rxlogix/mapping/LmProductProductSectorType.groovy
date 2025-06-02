package com.rxlogix.mapping

class LmProductProductSectorType implements Serializable  {

    BigDecimal productId
    BigDecimal productSectorTypeId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL2_COL3"

        cache: "read-only"
        id composite: ['productId', 'productSectorTypeId']
        version false
        productId column: "COL1_ID"
        productSectorTypeId column: "COL2_ID"
    }
}
