package com.rxlogix.mapping

class LmProductFamilyProductSector implements Serializable  {

    BigDecimal productFamilyId
    BigDecimal productSectorId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL1_COL2"

        cache: "read-only"
        id composite: ['productFamilyId', 'productSectorId']
        version false
        productFamilyId column: "COL1_ID"
        productSectorId column: "COL2_ID"
    }
}
