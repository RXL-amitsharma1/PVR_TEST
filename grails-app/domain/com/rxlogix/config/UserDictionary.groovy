package com.rxlogix.config

import com.rxlogix.util.DateUtil
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class UserDictionary {
    static auditable =  true
    @AuditEntityIdentifier
    String name
    String description
    UserDictionaryType type
    Boolean isDeleted = false
    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy

    static constraints = {
        name(blank: false)
        description nullable: true, maxSize: 200
        dateCreated nullable: true
        lastUpdated nullable: true
        createdBy nullable: true
        modifiedBy nullable: true
        isDeleted nullable: true
    }

    static mapping = {
        table name: "USER_DICT"
        name column: "NAME"
    }

    Map toReportRequestTypeMap() {
        [
                id         : id,
                name       : name,
                description: description,
                lastUpdated: lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                modifiedBy : modifiedBy
        ]
    }

    static enum UserDictionaryType {
        INN,
        DRUG,
        PSR_TYPE_FILE,
    }

}
