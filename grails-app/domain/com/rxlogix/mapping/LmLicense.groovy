package com.rxlogix.mapping

import com.rxlogix.SelectableList
import groovy.sql.Sql

class LmLicense implements SelectableList, Serializable {

    BigDecimal licenseId
    String tradeName
    String lang

    static mapping = {
        datasource "pva"
        table "VW_TRADE_NAME_APPROVAL_NUM_ALL"

        cache: "read-only"
        version false

        id composite: ['licenseId', 'lang']
        licenseId column: "LICENSE_ID", type: "big_decimal"
        tradeName column: "TRADE_NAME_APPROVAL_NUMBER"
        lang column: "lang_id", sqlType: 'char'
    }

    static constraints = {
        tradeName(blank:false, maxSize:70)
    }

    @Override
    List<Object> getSelectableList(String lang) {
        return this.executeQuery("select distinct c.tradeName from LmLicense c where c.lang = :lang order by c.tradeName asc", [lang: lang])
//        return LmLicense.findAll().unique().collect{ it.tradeName }.sort()
    }

    static String createJoinQuery(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId){
        String joinQuery = ""

        if (productSectorId) {
            joinQuery += ", LmLicenseProductSector productSectors "
        }

        if (productSectorTypeId) {
            joinQuery += ", LmLicenseProductSectorType productSectorTypes "
        }

        if (deviceTypeId) {
            joinQuery += ", LmLicenseDeviceType deviceTypes "
        }

        if (companyUnitId) {
            joinQuery += ", LmLicenseCompanyUnit companyUnits "
        }

        return joinQuery
    }

    static String createWhereQuery(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId, Map namedParameters){
        String whereQuery = ""

        if (productSectorId) {
            whereQuery += "lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId"
            namedParameters.put("productSectorId", productSectorId as BigDecimal)
        }

        if (productSectorTypeId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
            namedParameters.put("productSectorTypeId", productSectorTypeId as BigDecimal)
        }

        if (deviceTypeId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId"
            namedParameters.put("deviceTypeId", deviceTypeId as BigDecimal)
        }

        if (companyUnitId) {
            whereQuery += (whereQuery) ? " AND " : ""
            whereQuery += "lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
            namedParameters.put("companyUnitId", companyUnitId as BigDecimal)
        }

        return whereQuery
    }

    static List<BigDecimal> fetchByProductDictionaryFilters(String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId){
        Map namedParameters = [:]
        String selectQuery = "select distinct lmLicenses.licenseId from LmLicense lmLicenses "
        String joinQuery = createJoinQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)
        String whereClause = createWhereQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId, namedParameters)
        String mainQuery = selectQuery + joinQuery + (whereClause ? " where ${whereClause}" : "")
        return executeQuery(mainQuery, namedParameters)
    }

    static List<LmProduct> fetchProductsByLicense(BigDecimal licenseId, String currentLang){
        List<LmProduct> products = []
        List productIds = LmProductLmLicenseMapping.createCriteria().listDistinct {
            projections {
                property('productId')
            }
            eq('licenseId', licenseId)
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
