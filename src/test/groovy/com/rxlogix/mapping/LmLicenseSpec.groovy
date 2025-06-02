package com.rxlogix.mapping

import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

class LmLicenseSpec extends Specification {

    @Unroll
    def "testing CreateJoinQuery"() {
        given:
        String productSectorId = productSectorIdValue
        String productSectorTypeId = productSectorTypeIdValue
        String deviceTypeId = deviceTypeIdValue
        String companyUnitId = companyUnitIdValue

        when:
        String joinQuery = LmLicense.createJoinQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)

        then:
        joinQuery == result

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | result
        ""                   | ""                       | ""                | ""                 | ""
        ""                   | ""                       | ""                | "1"                | ", LmLicenseCompanyUnit companyUnits "
        ""                   | ""                       | "1"               | ""                 | ", LmLicenseDeviceType deviceTypes "
        ""                   | ""                       | "1"               | "1"                | ", LmLicenseDeviceType deviceTypes , LmLicenseCompanyUnit companyUnits "
        ""                   | "1"                      | ""                | ""                 | ", LmLicenseProductSectorType productSectorTypes "
        ""                   | "1"                      | ""                | "1"                | ", LmLicenseProductSectorType productSectorTypes , LmLicenseCompanyUnit companyUnits "
        ""                   | "1"                      | "1"               | ""                 | ", LmLicenseProductSectorType productSectorTypes , LmLicenseDeviceType deviceTypes "
        ""                   | "1"                      | "1"               | "1"                | ", LmLicenseProductSectorType productSectorTypes , LmLicenseDeviceType deviceTypes , LmLicenseCompanyUnit companyUnits "
        "1"                  | ""                       | ""                | ""                 | ", LmLicenseProductSector productSectors "
        "1"                  | ""                       | ""                | "1"                | ", LmLicenseProductSector productSectors , LmLicenseCompanyUnit companyUnits "
        "1"                  | ""                       | "1"               | ""                 | ", LmLicenseProductSector productSectors , LmLicenseDeviceType deviceTypes "
        "1"                  | ""                       | "1"               | "1"                | ", LmLicenseProductSector productSectors , LmLicenseDeviceType deviceTypes , LmLicenseCompanyUnit companyUnits "
        "1"                  | "1"                      | ""                | ""                 | ", LmLicenseProductSector productSectors , LmLicenseProductSectorType productSectorTypes "
        "1"                  | "1"                      | ""                | "1"                | ", LmLicenseProductSector productSectors , LmLicenseProductSectorType productSectorTypes , LmLicenseCompanyUnit companyUnits "
        "1"                  | "1"                      | "1"               | ""                 | ", LmLicenseProductSector productSectors , LmLicenseProductSectorType productSectorTypes , LmLicenseDeviceType deviceTypes "
        "1"                  | "1"                      | "1"               | "1"                | ", LmLicenseProductSector productSectors , LmLicenseProductSectorType productSectorTypes , LmLicenseDeviceType deviceTypes , LmLicenseCompanyUnit companyUnits "
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
        String whereQuery = LmLicense.createWhereQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId, namedParameters)

        then:
        whereQuery == result

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | result
        ""                   | ""                       | ""                | ""                 | ""
        ""                   | ""                       | ""                | "1"                | "lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | ""                       | "1"               | ""                 | "lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | ""                       | "1"               | "1"                | "lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | ""                | ""                 | "lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        ""                   | "1"                      | ""                | "1"                | "lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | "1"               | ""                 | "lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | "1"                      | "1"               | "1"                | "lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | ""                | ""                 | "lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId"
        "1"                  | ""                       | ""                | "1"                | "lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | "1"               | ""                 | "lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | ""                       | "1"               | "1"                | "lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | ""                | ""                 | "lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        "1"                  | "1"                      | ""                | "1"                | "lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | "1"               | ""                 | "lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | "1"                      | "1"               | "1"                | "lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
    }

    @Unroll
    @ConfineMetaClassChanges(LmLicense)
    def "testing FetchByProductDictionaryFilters"() {
        given:
        String query = mainQuery
        Map params = namedParameters
        String productSectorId = productSectorIdValue
        String productSectorTypeId = productSectorTypeIdValue
        String deviceTypeId = deviceTypeIdValue
        String companyUnitId = companyUnitIdValue
        LmLicense.metaClass.static.executeQuery = { String q, Map p ->
            assert q == query
            assert p == params
            return [1]
        }

        expect:
        LmLicense.fetchByProductDictionaryFilters(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | namedParameters                                                                 | mainQuery
        ""                   | ""                       | ""                | ""                 | [:]                                                                             | "select distinct lmLicenses.licenseId from LmLicense lmLicenses "
        ""                   | ""                       | ""                | "1"                | [companyUnitId: 1]                                                              | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseCompanyUnit companyUnits  where lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | ""                       | "1"               | ""                 | [deviceTypeId: 1]                                                               | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseDeviceType deviceTypes  where lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | ""                       | "1"               | "1"                | [deviceTypeId: 1, companyUnitId: 1]                                             | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseDeviceType deviceTypes , LmLicenseCompanyUnit companyUnits  where lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | ""                | ""                 | [productSectorTypeId: 1]                                                        | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSectorType productSectorTypes  where lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        ""                   | "1"                      | ""                | "1"                | [productSectorTypeId: 1, companyUnitId: 1]                                      | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSectorType productSectorTypes , LmLicenseCompanyUnit companyUnits  where lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | "1"               | ""                 | [productSectorTypeId: 1, deviceTypeId: 1]                                       | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSectorType productSectorTypes , LmLicenseDeviceType deviceTypes  where lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | "1"                      | "1"               | "1"                | [productSectorTypeId: 1, deviceTypeId: 1, companyUnitId: 1]                     | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSectorType productSectorTypes , LmLicenseDeviceType deviceTypes , LmLicenseCompanyUnit companyUnits  where lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | ""                | ""                 | ["productSectorId": 1]                                                          | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSector productSectors  where lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId"
        "1"                  | ""                       | ""                | "1"                | [productSectorId: 1, companyUnitId: 1]                                          | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSector productSectors , LmLicenseCompanyUnit companyUnits  where lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId AND lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | "1"               | ""                 | [productSectorId: 1, deviceTypeId: 1]                                           | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSector productSectors , LmLicenseDeviceType deviceTypes  where lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId AND lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | ""                       | "1"               | "1"                | [productSectorId: 1, deviceTypeId: 1, companyUnitId: 1]                         | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSector productSectors , LmLicenseDeviceType deviceTypes , LmLicenseCompanyUnit companyUnits  where lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId AND lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | ""                | ""                 | [productSectorId: 1, productSectorTypeId: 1]                                    | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSector productSectors , LmLicenseProductSectorType productSectorTypes  where lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId AND lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        "1"                  | "1"                      | ""                | "1"                | [productSectorId: 1, productSectorTypeId: 1, companyUnitId: 1]                  | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSector productSectors , LmLicenseProductSectorType productSectorTypes , LmLicenseCompanyUnit companyUnits  where lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId AND lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | "1"               | ""                 | [productSectorId: 1, productSectorTypeId: 1, deviceTypeId: 1]                   | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSector productSectors , LmLicenseProductSectorType productSectorTypes , LmLicenseDeviceType deviceTypes  where lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId AND lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | "1"                      | "1"               | "1"                | [productSectorId: 1, productSectorTypeId: 1, deviceTypeId: 1, companyUnitId: 1] | "select distinct lmLicenses.licenseId from LmLicense lmLicenses , LmLicenseProductSector productSectors , LmLicenseProductSectorType productSectorTypes , LmLicenseDeviceType deviceTypes , LmLicenseCompanyUnit companyUnits  where lmLicenses.licenseId = productSectors.licenseId AND productSectors.productSectorId = :productSectorId AND lmLicenses.licenseId = productSectorTypes.licenseId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmLicenses.licenseId = deviceTypes.licenseId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmLicenses.licenseId = companyUnits.licenseId AND companyUnits.companyUnitId = :companyUnitId"
    }
}
