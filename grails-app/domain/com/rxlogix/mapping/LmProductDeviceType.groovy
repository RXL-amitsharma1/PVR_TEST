package com.rxlogix.mapping

class LmProductDeviceType implements Serializable  {

    BigDecimal productId
    BigDecimal deviceTypeId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL3_COL3"

        cache: "read-only"
        id composite: ['productId', 'deviceTypeId']
        version false
        productId column: "COL1_ID", type: "big_decimal"
        deviceTypeId column: "COL2_ID", type: "big_decimal"
    }
}
