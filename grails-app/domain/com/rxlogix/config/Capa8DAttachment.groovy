package com.rxlogix.config

import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['capa8D'])
class Capa8DAttachment {
    static auditable =  [ignore:['data']]

    byte[] data
    String filename
    String ownerType
    Boolean isDeleted = false

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "-"
    String modifiedBy = "-"

    static belongsTo = [issues : Capa8D]

    static mapping = {
        table name : "CAPA_8D_ATTACHMENT"
        filename column : "FILENAME"
        data column: "DATA", lazy: true
        ownerType column: "OWNER_TYPE"
        isDeleted column: "ISDELETED"
        issues column: "ISSUE_ID"
    }

    static constraints = {
        filename(nullable: true, maxSize: 255)
        ownerType nullable: true
        data(nullable: true, maxSize: 20971520)
    }

    String toString() {
        return filename
    }

}
