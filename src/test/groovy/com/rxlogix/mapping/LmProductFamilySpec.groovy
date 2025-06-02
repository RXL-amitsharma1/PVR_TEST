package com.rxlogix.mapping

import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

class LmProductFamilySpec extends Specification {

    @Unroll
    def "testing CreateJoinQuery"() {
        given:
        String productSectorId = productSectorIdValue
        String productSectorTypeId = productSectorTypeIdValue
        String deviceTypeId = deviceTypeIdValue
        String companyUnitId = companyUnitIdValue

        when:
        String joinQuery = LmProductFamily.createJoinQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)

        then:
        joinQuery == result

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | result
        ""                   | ""                       | ""                | ""                 | ""
        ""                   | ""                       | ""                | "1"                | ", LmProductFamilyCompanyUnit companyUnits "
        ""                   | ""                       | "1"               | ""                 | ", LmProductFamilyDeviceType deviceTypes "
        ""                   | ""                       | "1"               | "1"                | ", LmProductFamilyDeviceType deviceTypes , LmProductFamilyCompanyUnit companyUnits "
        ""                   | "1"                      | ""                | ""                 | ", LmProductFamilyProductSectorType productSectorTypes "
        ""                   | "1"                      | ""                | "1"                | ", LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyCompanyUnit companyUnits "
        ""                   | "1"                      | "1"               | ""                 | ", LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyDeviceType deviceTypes "
        ""                   | "1"                      | "1"               | "1"                | ", LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyDeviceType deviceTypes , LmProductFamilyCompanyUnit companyUnits "
        "1"                  | ""                       | ""                | ""                 | ", LmProductFamilyProductSector productSectors "
        "1"                  | ""                       | ""                | "1"                | ", LmProductFamilyProductSector productSectors , LmProductFamilyCompanyUnit companyUnits "
        "1"                  | ""                       | "1"               | ""                 | ", LmProductFamilyProductSector productSectors , LmProductFamilyDeviceType deviceTypes "
        "1"                  | ""                       | "1"               | "1"                | ", LmProductFamilyProductSector productSectors , LmProductFamilyDeviceType deviceTypes , LmProductFamilyCompanyUnit companyUnits "
        "1"                  | "1"                      | ""                | ""                 | ", LmProductFamilyProductSector productSectors , LmProductFamilyProductSectorType productSectorTypes "
        "1"                  | "1"                      | ""                | "1"                | ", LmProductFamilyProductSector productSectors , LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyCompanyUnit companyUnits "
        "1"                  | "1"                      | "1"               | ""                 | ", LmProductFamilyProductSector productSectors , LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyDeviceType deviceTypes "
        "1"                  | "1"                      | "1"               | "1"                | ", LmProductFamilyProductSector productSectors , LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyDeviceType deviceTypes , LmProductFamilyCompanyUnit companyUnits "
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
        String whereQuery = LmProductFamily.createWhereQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId, namedParameters)

        then:
        whereQuery == result

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | result
        ""                   | ""                       | ""                | ""                 | ""
        ""                   | ""                       | ""                | "1"                | "lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | ""                       | "1"               | ""                 | "lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | ""                       | "1"               | "1"                | "lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | ""                | ""                 | "lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        ""                   | "1"                      | ""                | "1"                | "lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | "1"               | ""                 | "lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | "1"                      | "1"               | "1"                | "lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | ""                | ""                 | "lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId"
        "1"                  | ""                       | ""                | "1"                | "lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | "1"               | ""                 | "lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | ""                       | "1"               | "1"                | "lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | ""                | ""                 | "lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        "1"                  | "1"                      | ""                | "1"                | "lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | "1"               | ""                 | "lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | "1"                      | "1"               | "1"                | "lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
    }

    @Unroll
    @ConfineMetaClassChanges(LmProductFamily)
    def "testing FetchByProductDictionaryFilters"() {
        given:
        String query = mainQuery
        Map params = namedParameters
        String productSectorId = productSectorIdValue
        String productSectorTypeId = productSectorTypeIdValue
        String deviceTypeId = deviceTypeIdValue
        String companyUnitId = companyUnitIdValue
        LmProductFamily.metaClass.static.executeQuery = { String q, Map p ->
            assert q == query
            assert p == params
            return [1]
        }

        expect:
        LmProductFamily.fetchByProductDictionaryFilters(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | namedParameters                                                                 | mainQuery
        ""                   | ""                       | ""                | ""                 | [:]                                                                             | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies "
        ""                   | ""                       | ""                | "1"                | [companyUnitId: 1]                                                              | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyCompanyUnit companyUnits  where lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | ""                       | "1"               | ""                 | [deviceTypeId: 1]                                                               | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyDeviceType deviceTypes  where lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | ""                       | "1"               | "1"                | [deviceTypeId: 1, companyUnitId: 1]                                             | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyDeviceType deviceTypes , LmProductFamilyCompanyUnit companyUnits  where lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | ""                | ""                 | [productSectorTypeId: 1]                                                        | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSectorType productSectorTypes  where lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        ""                   | "1"                      | ""                | "1"                | [productSectorTypeId: 1, companyUnitId: 1]                                      | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyCompanyUnit companyUnits  where lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | "1"               | ""                 | [productSectorTypeId: 1, deviceTypeId: 1]                                       | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyDeviceType deviceTypes  where lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | "1"                      | "1"               | "1"                | [productSectorTypeId: 1, deviceTypeId: 1, companyUnitId: 1]                     | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyDeviceType deviceTypes , LmProductFamilyCompanyUnit companyUnits  where lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | ""                | ""                 | ["productSectorId": 1]                                                          | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSector productSectors  where lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId"
        "1"                  | ""                       | ""                | "1"                | [productSectorId: 1, companyUnitId: 1]                                          | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSector productSectors , LmProductFamilyCompanyUnit companyUnits  where lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId AND lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | "1"               | ""                 | [productSectorId: 1, deviceTypeId: 1]                                           | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSector productSectors , LmProductFamilyDeviceType deviceTypes  where lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId AND lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | ""                       | "1"               | "1"                | [productSectorId: 1, deviceTypeId: 1, companyUnitId: 1]                         | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSector productSectors , LmProductFamilyDeviceType deviceTypes , LmProductFamilyCompanyUnit companyUnits  where lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId AND lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | ""                | ""                 | [productSectorId: 1, productSectorTypeId: 1]                                    | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSector productSectors , LmProductFamilyProductSectorType productSectorTypes  where lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId AND lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        "1"                  | "1"                      | ""                | "1"                | [productSectorId: 1, productSectorTypeId: 1, companyUnitId: 1]                  | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSector productSectors , LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyCompanyUnit companyUnits  where lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId AND lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | "1"               | ""                 | [productSectorId: 1, productSectorTypeId: 1, deviceTypeId: 1]                   | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSector productSectors , LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyDeviceType deviceTypes  where lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId AND lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | "1"                      | "1"               | "1"                | [productSectorId: 1, productSectorTypeId: 1, deviceTypeId: 1, companyUnitId: 1] | "select distinct lmProductFamilies.productFamilyId from LmProductFamily lmProductFamilies , LmProductFamilyProductSector productSectors , LmProductFamilyProductSectorType productSectorTypes , LmProductFamilyDeviceType deviceTypes , LmProductFamilyCompanyUnit companyUnits  where lmProductFamilies.productFamilyId = productSectors.productFamilyId AND productSectors.productSectorId = :productSectorId AND lmProductFamilies.productFamilyId = productSectorTypes.productFamilyId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmProductFamilies.productFamilyId = deviceTypes.productFamilyId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmProductFamilies.productFamilyId = companyUnits.productFamilyId AND companyUnits.companyUnitId = :companyUnitId"
    }
}
