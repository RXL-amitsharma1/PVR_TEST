package com.rxlogix.mapping

import com.rxlogix.SelectableList


class LmProductFamily implements SelectableList, Serializable {

    BigDecimal productFamilyId
    String name
    String lang

    static mapping = {
        datasource "pva"
        table "VW_FAMILY_NAME_ALL"

        cache: "read-only"
        version false
        id composite: ['productFamilyId', 'lang']
        productFamilyId column: "PROD_FAMILY_ID", type: "big_decimal"
        name column: "FAMILY_NAME"
        lang column: "lang_id", sqlType: 'char'
    }

    static constraints = {
        name(blank:false, maxSize:40)
    }

    @Override
    List<Object> getSelectableList(String lang) {
        return this.executeQuery("select distinct lmp.name from LmProductFamily lmp where lmp.lang = :lang order by lmp.name asc", [lang:lang])
       // return LmProductFamily.findAll().unique().collect { it.name }.sort()
    }


//    TODO need to fix with Lang
    static List<String> getAllNamesForIds(List<String> productIds, String currentLang){
        return LmProductFamily.createCriteria().list {
            projections {
                distinct("name")
            }
            inList("productFamilyId", productIds*.toBigDecimal())
            order("name","asc")
            eq("lang", currentLang)
        }
    }

    static String createJoinQuery(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId){
        String joinQuery = ""

        if (productSectorId) {
            joinQuery += ", LmProductFamilyProductSector productSectors "
        }

        if (productSectorTypeId) {
            joinQuery += ", LmProductFamilyProductSectorType productSectorTypes "
        }

        if (deviceTypeId) {
            joinQuery += ", LmProductFamilyDeviceType deviceTypes "
        }

        if (companyUnitId) {
            joinQuery += ", LmProductFamilyCompanyUnit companyUnits "
        }

        return joinQuery
    }

    static String createWhereQuery(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId, Map namedParameters){
        String whereQuery = ""

        if (productSectorId) {
            whereQuery += "lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId"
            namedParameters.put("productSectorId", productSectorId as BigDecimal)
        }

        if (productSectorTypeId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
            namedParameters.put("productSectorTypeId", productSectorTypeId as BigDecimal)
        }

        if (deviceTypeId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId"
            namedParameters.put("deviceTypeId", deviceTypeId as BigDecimal)
        }

        if (companyUnitId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
            namedParameters.put("companyUnitId", companyUnitId as BigDecimal)
        }

        return whereQuery
    }

    static List<BigDecimal> fetchByProductDictionaryFilters(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId){
        Map namedParameters = [:]
        String selectQuery = "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies "
        String joinQuery = createJoinQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)
        String whereClause = createWhereQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId, namedParameters)
        String mainQuery = selectQuery + joinQuery + (whereClause ? " where ${whereClause}" : "")
        return executeQuery(mainQuery, namedParameters)
    }
}
