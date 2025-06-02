package com.rxlogix.mapping

import com.rxlogix.SelectableList
import groovy.sql.Sql

class LmProduct implements SelectableList, Serializable {

    BigDecimal productId
    BigDecimal productFamilyId
    String name
    String lang

    static mapping = {
        datasource "pva"
        table "VW_PRODUCT_ALL"

        cache: "read-only"
        version false

        id composite: ['productId', 'lang']
        productId column: "PRODUCT_ID", type: "big_decimal"
        name column: "PRODUCT_NAME"
        productFamilyId column: "PROD_FAMILY_ID"
        lang column: "lang_id", sqlType: 'char'
    }

    static constraints = {
        name(blank:false, maxSize:70)
    }

    static String createJoinQuery(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId){
        String joinQuery = ""

        if (productSectorId) {
            joinQuery += ", LmProductProductSector productSectors "
        }

        if (productSectorTypeId) {
            joinQuery += ", LmProductProductSectorType productSectorTypes "
        }

        if (deviceTypeId) {
            joinQuery += ", LmProductDeviceType deviceTypes "
        }

        if (companyUnitId) {
            joinQuery += ", LmProductCompanyUnit companyUnits "
        }

        return joinQuery
    }

    static String createWhereQuery(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId, Map namedParameters){
        String whereQuery = ""

        if (productSectorId) {
            whereQuery += "lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId"
            namedParameters.put("productSectorId", productSectorId as BigDecimal)
        }

        if (productSectorTypeId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
            namedParameters.put("productSectorTypeId", productSectorTypeId as BigDecimal)
        }

        if (deviceTypeId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId"
            namedParameters.put("deviceTypeId", deviceTypeId as BigDecimal)
        }

        if (companyUnitId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
            namedParameters.put("companyUnitId", companyUnitId as BigDecimal)
        }

        return whereQuery
    }

    static List<BigDecimal> fetchByProductDictionaryFilters(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId){
        Map namedParameters = [:]
        String selectQuery = "select distinct lmProducts.productId from LmProduct lmProducts "
        String joinQuery = createJoinQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)
        String whereClause = createWhereQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId, namedParameters)
        String mainQuery = selectQuery + joinQuery + (whereClause ? " where ${whereClause}" : "")
        return executeQuery(mainQuery, namedParameters)
    }

    static List<LmLicense> fetchLicensesByProduct(BigDecimal productId, String currentLang){
        List<LmLicense> licenses = []
        List licenseIds = LmProductLmLicenseMapping.createCriteria().listDistinct {
            projections {
                property('licenseId')
            }
            eq('productId', productId)
        }
        if (licenseIds) {
            licenseIds.collate(999).each { list ->
                licenses += LmLicense.createCriteria().list {
                    inList("licenseId", list)
                    eq("lang", currentLang)
                }
            }
        }
        return licenses
    }

    static List<LmIngredient> fetchIngredientsByProduct(BigDecimal productId, String currentLang){
        List<LmIngredient> ingredients = []
        List ingredientIds = LmProductLmIngredientMapping.createCriteria().listDistinct {
            projections {
                property('ingredientId')
            }
            eq('productId', productId)
        }
        if (ingredientIds) {
            ingredientIds.collate(999).each { list ->
                ingredients += LmIngredient.createCriteria().list {
                    inList("ingredientId", list)
                    eq("lang", currentLang)
                }
            }
        }
        return ingredients
    }
    
    @Override
    List<Object> getSelectableList(String lang) {
        return this.executeQuery("select distinct lmp.name from LmProduct lmp where lmp.lang = :lang order by lmp.name asc",[lang:lang])
       // return LmProduct.findAll().unique().collect { it.name }.sort()
    }

    static List<String> getAllProductNamesForIds(List<String> productNameIds, String currentLang){
        return LmProduct.createCriteria().list {
            projections {
                distinct("name")
            }
            inList("productId", productNameIds*.toBigDecimal())
            order("name","asc")
            eq("lang", currentLang)
        }
    }
}
