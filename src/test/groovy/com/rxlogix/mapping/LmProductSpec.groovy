package com.rxlogix.mapping

import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

class LmProductSpec extends Specification {

    @Unroll
    def "testing CreateJoinQuery"() {
        given:
        String productSectorId = productSectorIdValue
        String productSectorTypeId = productSectorTypeIdValue
        String deviceTypeId = deviceTypeIdValue
        String companyUnitId = companyUnitIdValue

        when:
        String joinQuery = LmProduct.createJoinQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)

        then:
        joinQuery == result

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | result
        ""                   | ""                       | ""                | ""                 | ""
        ""                   | ""                       | ""                | "1"                | ", LmProductCompanyUnit companyUnits "
        ""                   | ""                       | "1"               | ""                 | ", LmProductDeviceType deviceTypes "
        ""                   | ""                       | "1"               | "1"                | ", LmProductDeviceType deviceTypes , LmProductCompanyUnit companyUnits "
        ""                   | "1"                      | ""                | ""                 | ", LmProductProductSectorType productSectorTypes "
        ""                   | "1"                      | ""                | "1"                | ", LmProductProductSectorType productSectorTypes , LmProductCompanyUnit companyUnits "
        ""                   | "1"                      | "1"               | ""                 | ", LmProductProductSectorType productSectorTypes , LmProductDeviceType deviceTypes "
        ""                   | "1"                      | "1"               | "1"                | ", LmProductProductSectorType productSectorTypes , LmProductDeviceType deviceTypes , LmProductCompanyUnit companyUnits "
        "1"                  | ""                       | ""                | ""                 | ", LmProductProductSector productSectors "
        "1"                  | ""                       | ""                | "1"                | ", LmProductProductSector productSectors , LmProductCompanyUnit companyUnits "
        "1"                  | ""                       | "1"               | ""                 | ", LmProductProductSector productSectors , LmProductDeviceType deviceTypes "
        "1"                  | ""                       | "1"               | "1"                | ", LmProductProductSector productSectors , LmProductDeviceType deviceTypes , LmProductCompanyUnit companyUnits "
        "1"                  | "1"                      | ""                | ""                 | ", LmProductProductSector productSectors , LmProductProductSectorType productSectorTypes "
        "1"                  | "1"                      | ""                | "1"                | ", LmProductProductSector productSectors , LmProductProductSectorType productSectorTypes , LmProductCompanyUnit companyUnits "
        "1"                  | "1"                      | "1"               | ""                 | ", LmProductProductSector productSectors , LmProductProductSectorType productSectorTypes , LmProductDeviceType deviceTypes "
        "1"                  | "1"                      | "1"               | "1"                | ", LmProductProductSector productSectors , LmProductProductSectorType productSectorTypes , LmProductDeviceType deviceTypes , LmProductCompanyUnit companyUnits "
    }

    @Unroll
    def "testing CreateWhereQuery"() {
        given:
        String productSectorId = productSectorIdValue
        String productSectorTypeId = productSectorTypeIdValue
        String deviceTypeId = deviceTypeIdValue
        String companyUnitId = companyUnitIdValue
        Map namedParameters = [:]

        when:
        String whereQuery = LmProduct.createWhereQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId, namedParameters)

        then:
        whereQuery == result

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | result
        ""                   | ""                       | ""                | ""                 | ""
        ""                   | ""                       | ""                | "1"                | "lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | ""                       | "1"               | ""                 | "lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | ""                       | "1"               | "1"                | "lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | ""                | ""                 | "lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        ""                   | "1"                      | ""                | "1"                | "lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | "1"               | ""                 | "lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | "1"                      | "1"               | "1"                | "lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | ""                | ""                 | "lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId"
        "1"                  | ""                       | ""                | "1"                | "lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | "1"               | ""                 | "lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | ""                       | "1"               | "1"                | "lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | ""                | ""                 | "lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        "1"                  | "1"                      | ""                | "1"                | "lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | "1"               | ""                 | "lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | "1"                      | "1"               | "1"                | "lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
    }

    @Unroll
    @ConfineMetaClassChanges(LmProduct)
    def "testing FetchByProductDictionaryFilters"() {
        given:
        String query = mainQuery
        Map params = namedParameters
        String productSectorId = productSectorIdValue
        String productSectorTypeId = productSectorTypeIdValue
        String deviceTypeId = deviceTypeIdValue
        String companyUnitId = companyUnitIdValue
        LmProduct.metaClass.static.executeQuery = { String q, Map p ->
            assert q == query
            assert p == params
            return [1]
        }

        expect:
        LmProduct.fetchByProductDictionaryFilters(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | namedParameters                                                                 | mainQuery
        ""                   | ""                       | ""                | ""                 | [:]                                                                             | "select distinct lmProducts.productId from LmProduct lmProducts "
        ""                   | ""                       | ""                | "1"                | [companyUnitId: 1]                                                              | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductCompanyUnit companyUnits  where lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | ""                       | "1"               | ""                 | [deviceTypeId: 1]                                                               | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductDeviceType deviceTypes  where lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | ""                       | "1"               | "1"                | [deviceTypeId: 1, companyUnitId: 1]                                             | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductDeviceType deviceTypes , LmProductCompanyUnit companyUnits  where lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | ""                | ""                 | [productSectorTypeId: 1]                                                        | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSectorType productSectorTypes  where lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        ""                   | "1"                      | ""                | "1"                | [productSectorTypeId: 1, companyUnitId: 1]                                      | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSectorType productSectorTypes , LmProductCompanyUnit companyUnits  where lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | "1"               | ""                 | [productSectorTypeId: 1, deviceTypeId: 1]                                       | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSectorType productSectorTypes , LmProductDeviceType deviceTypes  where lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | "1"                      | "1"               | "1"                | [productSectorTypeId: 1, deviceTypeId: 1, companyUnitId: 1]                     | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSectorType productSectorTypes , LmProductDeviceType deviceTypes , LmProductCompanyUnit companyUnits  where lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | ""                | ""                 | ["productSectorId": 1]                                                          | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSector productSectors  where lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId"
        "1"                  | ""                       | ""                | "1"                | [productSectorId: 1, companyUnitId: 1]                                          | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSector productSectors , LmProductCompanyUnit companyUnits  where lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId AND lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | "1"               | ""                 | [productSectorId: 1, deviceTypeId: 1]                                           | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSector productSectors , LmProductDeviceType deviceTypes  where lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId AND lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | ""                       | "1"               | "1"                | [productSectorId: 1, deviceTypeId: 1, companyUnitId: 1]                         | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSector productSectors , LmProductDeviceType deviceTypes , LmProductCompanyUnit companyUnits  where lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId AND lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | ""                | ""                 | [productSectorId: 1, productSectorTypeId: 1]                                    | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSector productSectors , LmProductProductSectorType productSectorTypes  where lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId AND lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        "1"                  | "1"                      | ""                | "1"                | [productSectorId: 1, productSectorTypeId: 1, companyUnitId: 1]                  | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSector productSectors , LmProductProductSectorType productSectorTypes , LmProductCompanyUnit companyUnits  where lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId AND lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | "1"               | ""                 | [productSectorId: 1, productSectorTypeId: 1, deviceTypeId: 1]                   | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSector productSectors , LmProductProductSectorType productSectorTypes , LmProductDeviceType deviceTypes  where lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId AND lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | "1"                      | "1"               | "1"                | [productSectorId: 1, productSectorTypeId: 1, deviceTypeId: 1, companyUnitId: 1] | "select distinct lmProducts.productId from LmProduct lmProducts , LmProductProductSector productSectors , LmProductProductSectorType productSectorTypes , LmProductDeviceType deviceTypes , LmProductCompanyUnit companyUnits  where lmProducts.productId = productSectors.productId AND productSectors.productSectorId = :productSectorId AND lmProducts.productId = productSectorTypes.productId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProducts.productId = deviceTypes.productId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmProducts.productId = companyUnits.productId AND companyUnits.companyUnitId = :companyUnitId"
    }
}
