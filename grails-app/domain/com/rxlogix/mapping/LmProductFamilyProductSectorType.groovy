package com.rxlogix.mapping

class LmProductFamilyProductSectorType implements Serializable  {

    BigDecimal productFamilyId
    BigDecimal productSectorTypeId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL2_COL2"

        cache: "read-only"
        id composite: ['productFamilyId', 'productSectorTypeId']
        version false
        productFamilyId column: "COL1_ID"
        productSectorTypeId column: "COL2_ID"
    }
}
