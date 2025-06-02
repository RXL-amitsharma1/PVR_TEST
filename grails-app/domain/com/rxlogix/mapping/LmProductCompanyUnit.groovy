package com.rxlogix.mapping

class LmProductCompanyUnit implements Serializable  {

    BigDecimal productId
    BigDecimal companyUnitId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL4_COL3"

        cache: "read-only"
        id composite: ['productId', 'companyUnitId']
        version false
        productId column: "COL1_ID"
        companyUnitId column: "COL2_ID"
    }
}
