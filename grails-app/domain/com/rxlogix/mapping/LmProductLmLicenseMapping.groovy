package com.rxlogix.mapping

class LmProductLmLicenseMapping implements Serializable{

    BigDecimal licenseId
    BigDecimal productId

    static mapping = {
        datasource "pva"
        table "VW_PROD_LICENSE_LINK_DSP"

        cache: "read-only"
        version false

        id composite: ['productId', 'licenseId']
        productId column: "PRODUCT_ID"
        licenseId column: "LICENSE_ID"
    }
}