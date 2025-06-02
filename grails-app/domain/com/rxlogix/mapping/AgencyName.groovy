package com.rxlogix.mapping

import grails.gorm.MultiTenant

class AgencyName implements MultiTenant<AgencyName>, Serializable {

    BigDecimal id
    String name
    Integer tenantId
    Integer lang

    static constraints = {
        name nullable: true
    }

    static mapping = {
        datasource "pva"
        table "VW_LRC_AGENCY_NAME_DSP"

        cache: "read-only"
        version false

        id column: "AGENCY_ID", type: "big_decimal", generator: "assigned"
        name column: "AGENCY_NAME"
        tenantId column: 'TENANT_ID', name: 'tenantId'
        lang column: "LANG_ID"
    }
}
