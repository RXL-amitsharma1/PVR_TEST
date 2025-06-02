package com.rxlogix.mapping

class LmProductLmIngredientMapping implements Serializable{

    BigDecimal ingredientId
    BigDecimal productId

    static mapping = {
        datasource "pva"
        table "VW_PROD_INGRED_LINK"

        cache: "read-only"
        version false

        id composite: ['productId', 'ingredientId']
        productId column: "PRODUCT_ID"
        ingredientId column: "INGREDIENT_ID"
    }

}