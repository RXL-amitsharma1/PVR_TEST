package com.rxlogix.config

import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class PvcAttachment {
    @AuditEntityIdentifier
    String name
    byte[] data

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "-"
    String modifiedBy = "-"

    static auditable =  [ignore:['data']]

    static belongsTo = [drilldownCLLMetadata: DrilldownCLLMetadata, inboundMetadata: InboundDrilldownMetadata]

    static mapping = {
        table name: "PVC_ATTACH"
        name column: "NAME"
        data column: "DATA", lazy: true
        drilldownCLLMetadata column: "METADATA_ID"
        inboundMetadata column: "IN_METADATA_ID"
    }

    static constraints = {
        name(maxSize: 255)
        data(nullable: true, maxSize: 20971520)
        drilldownCLLMetadata(nullable: true)
        inboundMetadata(nullable: true)
    }

    String toString() {
        return name
    }
}
