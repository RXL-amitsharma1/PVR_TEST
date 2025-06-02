package com.rxlogix.mapping

import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

class LmIngredientSpec extends Specification {
    @Unroll
    def "testing CreateJoinQuery"() {
        given:
        String productSectorId = productSectorIdValue
        String productSectorTypeId = productSectorTypeIdValue
        String deviceTypeId = deviceTypeIdValue
        String companyUnitId = companyUnitIdValue

        when:
        String joinQuery = LmIngredient.createJoinQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)

        then:
        joinQuery == result

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | result
        ""                   | ""                       | ""                | ""                 | ""
        ""                   | ""                       | ""                | "1"                | ", LmIngredientCompanyUnit companyUnits "
        ""                   | ""                       | "1"               | ""                 | ", LmIngredientDeviceType deviceTypes "
        ""                   | ""                       | "1"               | "1"                | ", LmIngredientDeviceType deviceTypes , LmIngredientCompanyUnit companyUnits "
        ""                   | "1"                      | ""                | ""                 | ", LmIngredientProductSectorType productSectorTypes "
        ""                   | "1"                      | ""                | "1"                | ", LmIngredientProductSectorType productSectorTypes , LmIngredientCompanyUnit companyUnits "
        ""                   | "1"                      | "1"               | ""                 | ", LmIngredientProductSectorType productSectorTypes , LmIngredientDeviceType deviceTypes "
        ""                   | "1"                      | "1"               | "1"                | ", LmIngredientProductSectorType productSectorTypes , LmIngredientDeviceType deviceTypes , LmIngredientCompanyUnit companyUnits "
        "1"                  | ""                       | ""                | ""                 | ", LmIngredientProductSector productSectors "
        "1"                  | ""                       | ""                | "1"                | ", LmIngredientProductSector productSectors , LmIngredientCompanyUnit companyUnits "
        "1"                  | ""                       | "1"               | ""                 | ", LmIngredientProductSector productSectors , LmIngredientDeviceType deviceTypes "
        "1"                  | ""                       | "1"               | "1"                | ", LmIngredientProductSector productSectors , LmIngredientDeviceType deviceTypes , LmIngredientCompanyUnit companyUnits "
        "1"                  | "1"                      | ""                | ""                 | ", LmIngredientProductSector productSectors , LmIngredientProductSectorType productSectorTypes "
        "1"                  | "1"                      | ""                | "1"                | ", LmIngredientProductSector productSectors , LmIngredientProductSectorType productSectorTypes , LmIngredientCompanyUnit companyUnits "
        "1"                  | "1"                      | "1"               | ""                 | ", LmIngredientProductSector productSectors , LmIngredientProductSectorType productSectorTypes , LmIngredientDeviceType deviceTypes "
        "1"                  | "1"                      | "1"               | "1"                | ", LmIngredientProductSector productSectors , LmIngredientProductSectorType productSectorTypes , LmIngredientDeviceType deviceTypes , LmIngredientCompanyUnit companyUnits "
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
        String whereQuery = LmIngredient.createWhereQuery(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId, namedParameters)

        then:
        whereQuery == result

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | result
        ""                   | ""                       | ""                | ""                 | ""
        ""                   | ""                       | ""                | "1"                | "lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | ""                       | "1"               | ""                 | "lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | ""                       | "1"               | "1"                | "lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | ""                | ""                 | "lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        ""                   | "1"                      | ""                | "1"                | "lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | "1"               | ""                 | "lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | "1"                      | "1"               | "1"                | "lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | ""                | ""                 | "lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId"
        "1"                  | ""                       | ""                | "1"                | "lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | "1"               | ""                 | "lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | ""                       | "1"               | "1"                | "lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | ""                | ""                 | "lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        "1"                  | "1"                      | ""                | "1"                | "lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | "1"               | ""                 | "lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | "1"                      | "1"               | "1"                | "lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId" + " AND " + "lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId" + " AND " + "lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId" + " AND " + "lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
    }

    @Unroll
    @ConfineMetaClassChanges(LmIngredient)
    def "testing FetchByProductDictionaryFilters"() {
        given:
        String query = mainQuery
        Map params = namedParameters
        String productSectorId = productSectorIdValue
        String productSectorTypeId = productSectorTypeIdValue
        String deviceTypeId = deviceTypeIdValue
        String companyUnitId = companyUnitIdValue
        LmIngredient.metaClass.static.executeQuery = { String q, Map p ->
            assert q == query
            assert p == params
            return [1]
        }

        expect:
        LmIngredient.fetchByProductDictionaryFilters(productSectorId, productSectorTypeId, deviceTypeId, companyUnitId)

        where:
        productSectorIdValue | productSectorTypeIdValue | deviceTypeIdValue | companyUnitIdValue | namedParameters                                                                 | mainQuery
        ""                   | ""                       | ""                | ""                 | [:]                                                                             | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients "
        ""                   | ""                       | ""                | "1"                | [companyUnitId: 1]                                                              | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientCompanyUnit companyUnits  where lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | ""                       | "1"               | ""                 | [deviceTypeId: 1]                                                               | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientDeviceType deviceTypes  where lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | ""                       | "1"               | "1"                | [deviceTypeId: 1, companyUnitId: 1]                                             | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientDeviceType deviceTypes , LmIngredientCompanyUnit companyUnits  where lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | ""                | ""                 | [productSectorTypeId: 1]                                                        | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSectorType productSectorTypes  where lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        ""                   | "1"                      | ""                | "1"                | [productSectorTypeId: 1, companyUnitId: 1]                                      | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSectorType productSectorTypes , LmIngredientCompanyUnit companyUnits  where lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        ""                   | "1"                      | "1"               | ""                 | [productSectorTypeId: 1, deviceTypeId: 1]                                       | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSectorType productSectorTypes , LmIngredientDeviceType deviceTypes  where lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId"
        ""                   | "1"                      | "1"               | "1"                | [productSectorTypeId: 1, deviceTypeId: 1, companyUnitId: 1]                     | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSectorType productSectorTypes , LmIngredientDeviceType deviceTypes , LmIngredientCompanyUnit companyUnits  where lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | ""                | ""                 | ["productSectorId": 1]                                                          | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSector productSectors  where lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId"
        "1"                  | ""                       | ""                | "1"                | [productSectorId: 1, companyUnitId: 1]                                          | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSector productSectors , LmIngredientCompanyUnit companyUnits  where lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId AND lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | ""                       | "1"               | ""                 | [productSectorId: 1, deviceTypeId: 1]                                           | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSector productSectors , LmIngredientDeviceType deviceTypes  where lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId AND lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | ""                       | "1"               | "1"                | [productSectorId: 1, deviceTypeId: 1, companyUnitId: 1]                         | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSector productSectors , LmIngredientDeviceType deviceTypes , LmIngredientCompanyUnit companyUnits  where lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId AND lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | ""                | ""                 | [productSectorId: 1, productSectorTypeId: 1]                                    | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSector productSectors , LmIngredientProductSectorType productSectorTypes  where lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId AND lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId"
        "1"                  | "1"                      | ""                | "1"                | [productSectorId: 1, productSectorTypeId: 1, companyUnitId: 1]                  | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSector productSectors , LmIngredientProductSectorType productSectorTypes , LmIngredientCompanyUnit companyUnits  where lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId AND lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
        "1"                  | "1"                      | "1"               | ""                 | [productSectorId: 1, productSectorTypeId: 1, deviceTypeId: 1]                   | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSector productSectors , LmIngredientProductSectorType productSectorTypes , LmIngredientDeviceType deviceTypes  where lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId AND lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId"
        "1"                  | "1"                      | "1"               | "1"                | [productSectorId: 1, productSectorTypeId: 1, deviceTypeId: 1, companyUnitId: 1] | "select distinct lmIngredients.ingredientId from LmIngredient lmIngredients , LmIngredientProductSector productSectors , LmIngredientProductSectorType productSectorTypes , LmIngredientDeviceType deviceTypes , LmIngredientCompanyUnit companyUnits  where lmIngredients.ingredientId = productSectors.ingredientId AND productSectors.productSectorId = :productSectorId AND lmIngredients.ingredientId = productSectorTypes.ingredientId AND productSectorTypes.productSectorTypeId = :productSectorTypeId AND lmIngredients.ingredientId = deviceTypes.ingredientId AND deviceTypes.deviceTypeId = :deviceTypeId AND lmIngredients.ingredientId = companyUnits.ingredientId AND companyUnits.companyUnitId = :companyUnitId"
    }
}
