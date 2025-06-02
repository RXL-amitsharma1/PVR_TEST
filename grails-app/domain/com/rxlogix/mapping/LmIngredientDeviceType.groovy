package com.rxlogix.mapping

class LmIngredientDeviceType implements Serializable  {

    BigDecimal ingredientId
    BigDecimal deviceTypeId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL4_COL1"
        cache: "read-only"
        id composite: ['ingredientId', 'deviceTypeId']
        version false
        ingredientId column: "COL1_ID"
        deviceTypeId column: "COL2_ID"
    }
}
