package com.rxlogix.mapping

class OrganizationCountry implements Serializable {
    
    Long id
    String name
    String langDesc
    
    static constraints = {
        name nullable: true
        langDesc nullable: true
    }
    
    static mapping = {
        datasource "pva"
        table "VW_LCO_COUNTRY"
    
        cache: "read-only"
        version false
    
        id column: "COUNTRY_ID", type: "long", generator: "assigned"
        name column: "COUNTRY"
        langDesc column: "LANG_DESC"
    }
}
