package com.rxlogix.mapping

class LmLicenseDeviceType implements Serializable  {

    BigDecimal licenseId
    BigDecimal deviceTypeId

    static mapping = {
        datasource "pva"
        table "VW_PVR_PROD_BRWS_MAP_SL3_COL4"

        cache: "read-only"
        id composite: ['licenseId', 'deviceTypeId']
        version false
        licenseId column: "COL1_ID"
        deviceTypeId column: "COL2_ID"
    }
}
