package com.rxlogix.mapping

class LmLicenseCompanyUnit implements Serializable {

    BigDecimal licenseId
    BigDecimal companyUnitId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL4_COL4"

        cache: "read-only"
        id composite: ['licenseId', 'companyUnitId']
        version false
        licenseId column: "COL1_ID"
        companyUnitId column: "COL2_ID"
    }
}
