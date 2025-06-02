package com.rxlogix.mapping

class LmLicenseProductSector implements Serializable  {

    BigDecimal licenseId
    BigDecimal productSectorId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL1_COL4"

        cache: "read-only"
        id composite: ['licenseId', 'productSectorId']
        version false
        licenseId column: "COL1_ID"
        productSectorId column: "COL2_ID"
    }
}
