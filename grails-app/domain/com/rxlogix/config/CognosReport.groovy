package com.rxlogix.config

import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class CognosReport {

    static auditable = true
    @AuditEntityIdentifier
    String name
    String url
    String description
    boolean isDeleted = false

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapping = {
        table name: "COGNOS_REPORT"

        name column: "NAME"
        url column: "URL"
        description column: "DESCRIPTION"
        isDeleted column: "IS_DELETED"
    }

    static constraints = {
        description (nullable: true, maxSize: 1000)
        url (maxSize: 1000, url: true)
        createdBy(nullable: false, maxSize: 255)
        modifiedBy(nullable: false, maxSize: 255)
    }

    public String toString() {
        return name
    }
}
