package com.rxlogix.config

import com.rxlogix.user.User

class LockedObjects {
    Long identifier
    Date lockTime = new Date()
    User user

    static mapping = {
        table name: "LOCKED_OBJECTS"
        identifier column: "IDENTIFIER"
        lockTime column: "LOCK_TIME"
        user column: "USER_ID"
    }

    static constraints = {
        identifier(unique: true)
    }
}
