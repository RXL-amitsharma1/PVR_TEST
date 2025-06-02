package com.rxlogix.mapping

class LmIngredientProductSectorType implements Serializable  {

    BigDecimal ingredientId
    BigDecimal productSectorTypeId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL2_COL1"
        cache: "read-only"
        id composite: ['ingredientId', 'productSectorTypeId']
        version false
        ingredientId column: "COL1_ID"
        productSectorTypeId column: "COL2_ID"
    }
}
