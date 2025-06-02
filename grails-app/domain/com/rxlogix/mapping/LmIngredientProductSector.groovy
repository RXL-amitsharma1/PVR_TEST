package com.rxlogix.mapping

class LmIngredientProductSector implements Serializable  {

    BigDecimal ingredientId
    BigDecimal productSectorId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL1_COL1"

        cache: "read-only"
        id composite: ['ingredientId', 'productSectorId']
        version false
        ingredientId column: "COL1_ID"
        productSectorId column: "COL2_ID"
    }
}
