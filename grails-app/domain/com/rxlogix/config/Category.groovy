package com.rxlogix.config

class Category {
    String name
    boolean defaultName = false

    static mapping = {
        table name: "CATEGORY"

        name column: "NAME"
        defaultName column: "DEFAULT_NAME"
    }

    static constraints = {
        name(unique: true, blank: false)
    }

    def getInstanceIdentifierForAuditLog() {
        return name
    }

    public String toString() {
        return name
    }

}
