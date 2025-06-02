package com.rxlogix.mapping

class LmProductFamilyCompanyUnit implements Serializable  {

    BigDecimal productFamilyId
    BigDecimal companyUnitId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL4_COL2"

        cache: "read-only"
        id composite: ['productFamilyId', 'companyUnitId']
        version false
        productFamilyId column: "COL1_ID"
        companyUnitId column: "COL2_ID"
    }
}
