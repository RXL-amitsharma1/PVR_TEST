package com.rxlogix.mapping

/*
This is a mapping domain for Authorization Types in PVR-DB
 */
class AuthorizationType {

    Integer id
    String name
    boolean isDisplay
    boolean isActive
    Integer tenantId
    boolean isSelectable
    String langId

    static mapping = {
        datasource "pva"
        table name: "VW_LLTYP_LICENSE_TYPE"
        version false
        id column: 'LICENSE_TYPE_ID'
        name column: 'LICENSE_TYPE'
        isDisplay column: 'DISPLAY'
        isActive column: 'IS_ACTIVE'
        tenantId column: 'TENANT_ID'
        isSelectable column: 'SELECTABLE'
        langId column: 'LANG_ID'
    }

    static constraints = {
    }
}
