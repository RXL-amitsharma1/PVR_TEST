package com.rxlogix.config

class ActionItemCategory {

    String name
    String key
    String description
    Boolean forPvq = false

    static constraints = {
        name(unique: true, blank: false)
        description nullable: true
    }

    static mapping = {
        cache: true
        table name: "ACTION_ITEM_CATEGORY"
        name column: "NAME"
        key column: "KEY"
        description column: "DESCRIPTION"
        forPvq column: "FOR_PVQ"
    }

    def getInstanceIdentifierForAuditLog() {
        return name + "(${key})"
    }

    public String getI18nKey() {
        return "app.actionItemCategory.${key}"
    }

    @Override
    public String toString() {
        return name
    }
}
