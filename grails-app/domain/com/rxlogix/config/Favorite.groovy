package com.rxlogix.config

import com.rxlogix.user.User

class Favorite {
    Integer priority
    String entity
    Long entityId
    User user
    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy

    static constraints = {
        priority(nullable:false)
        entity(nullable:false)
        entityId(nullable:true)
        user(nullable:false)
    }

    static mapping = {
        table name: "FAVORITE"
        id column: "ID"
        priority column: "PRIORITY"
        entity column: "ENTITY"
        entityId column: "ENTITY_ID"
        user column: "USER_ID"
    }

}
