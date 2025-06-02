package com.rxlogix.mapping

class LmLicenseProductSectorType implements Serializable  {

    BigDecimal licenseId
    BigDecimal productSectorTypeId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL2_COL4"

        cache: "read-only"
        id composite: ['licenseId', 'productSectorTypeId']
        version false
        licenseId column: "COL1_ID"
        productSectorTypeId column: "COL2_ID"
    }
}
