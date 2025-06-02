package com.rxlogix.mapping

import com.rxlogix.SelectableList
import groovy.sql.Sql


class LmIngredient implements SelectableList, Serializable {

    BigDecimal ingredientId
    String ingredient
    String lang

    static mapping = {
        datasource "pva"
        table "VW_INGREDIENT_ALL"

        cache: "read-only"
        version false

        id composite: ['ingredientId', 'lang']
        ingredientId column: "INGREDIENT_ID", type: "big_decimal"
        ingredient column: "INGREDIENT"
        lang column: "lang_id", sqlType: 'char'
    }

    static constraints = {
        ingredient(blank:false, maxSize:120)
    }

    @Override
    List<Object> getSelectableList(String lang) {
        return this.executeQuery("select distinct c.ingredient from LmIngredient c where c.lang = :lang order by c.ingredient asc",[lang:lang])
//        return LmIngredient.findAll().unique().collect { it.ingredient }.sort()
    }

    static String createJoinQuery(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId){
        String joinQuery = ""

        if (productSectorId) {
            joinQuery += ", LmIngredientProductSector productSectors "
        }

        if (productSectorTypeId) {
            joinQuery += ", LmIngredientProductSectorType productSectorTypes "
        }

        if (deviceTypeId) {
            joinQuery += ", LmIngredientDeviceType deviceTypes "
        }

        if (companyUnitId) {
            joinQuery += ", LmIngredientCompanyUnit companyUnits "
        }

        return joinQuery
    }

    static String createWhereQuery(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId, Map namedParameters){
        String whereQuery = ""

        if (productSectorId) {
            whereQuery += "lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId"
            namedParameters.put("productSectorId", productSectorId as BigDecimal)
        }

        if (productSectorTypeId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
            namedParameters.put("productSectorTypeId", productSectorTypeId as BigDecimal)
        }

        if (deviceTypeId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId"
            namedParameters.put("deviceTypeId", deviceTypeId as BigDecimal)
        }

        if (companyUnitId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
            namedParameters.put("companyUnitId", companyUnitId as BigDecimal)
        }

        return whereQuery
    }

    static List<BigDecimal> fetchByProductDictionaryFilters(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId){
        Map namedParameters = [:]
        String selectQuery = "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients "
        String joinQuery = createJoinQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)
        String whereClause = createWhereQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId, namedParameters)
        String mainQuery = selectQuery + joinQuery + (whereClause ? " where ${whereClause}" : "")
        return executeQuery(mainQuery, namedParameters)
    }

    static List<LmProduct> fetchProductsByIngredient(BigDecimal ingredientId, String currentLang){
        List<LmProduct> products = []
        List productIds = LmProductLmIngredientMapping.createCriteria().listDistinct {
            projections {
                property('productId')
            }
            eq('ingredientId', ingredientId)
        }
        if (productIds) {
            productIds.collate(999).each { list ->
                products += LmProduct.createCriteria().list {
                    inList("productId", list)
                    eq("lang", currentLang)
                }
            }
        }
        return products
    }
}
