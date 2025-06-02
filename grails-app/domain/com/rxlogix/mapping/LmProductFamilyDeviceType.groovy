package com.rxlogix.mapping

class LmProductFamilyDeviceType implements Serializable  {

    BigDecimal productFamilyId
    BigDecimal deviceTypeId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL3_COL2"

        cache: "read-only"
        id composite: ['productFamilyId', 'deviceTypeId']
        version false
        productFamilyId column: "COL1_ID"
        deviceTypeId column: "COL2_ID"
    }
}
