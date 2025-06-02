package com.rxlogix.mapping

class LmIngredientCompanyUnit implements Serializable {

    BigDecimal ingredientId
    BigDecimal companyUnitId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL3_COL1"

        cache: "read-only"
        id composite: ['ingredientId', 'companyUnitId']
        version false
        ingredientId column: "COL1_ID"
        companyUnitId column: "COL2_ID"

    }
}