package com.rxlogix.config

import com.rxlogix.enums.IcsrCaseStateEnum

class MessageType {

    Integer id
    boolean display
    boolean isActive
    Integer tenantId
    String description
    Integer langId

    static mapping = {
        datasource "pva"
        table name: "VW_E2B_MESSAGE_TYPE"
        version false
        id column: 'CODE_ID'
        display column: 'DISPLAY'
        isActive column: 'IS_ACTIVE'
        tenantId column: 'TENANT_ID'
        description column: 'DESCRIPTION'
        langId column: 'LANG_ID'
    }

    static constraints = {

    }

}
