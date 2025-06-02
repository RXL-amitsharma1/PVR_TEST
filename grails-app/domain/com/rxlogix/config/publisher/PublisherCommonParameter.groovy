package com.rxlogix.config.publisher

import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class PublisherCommonParameter {
    static auditable =  true
    @AuditEntityIdentifier
    String name
    String description
    String value
    boolean isDeleted = false

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "Application"
    String modifiedBy = "Application"

    static mapping = {
        table('publisher_cmn_prm')
    }

    static constraints = {
        name(maxSize: 255, validator: { val, obj ->
            //Name is unique to user
            if (!obj.id || obj.isDirty("name")) {
                long count = PublisherCommonParameter.createCriteria().count {
                    ilike('name', "${val}")
                    eq('isDeleted', false)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "com.rxlogix.config.PublisherCommonParameter.name.unique"
                }
            }
            if(val){
                String regEx = "^[\\w_]+\$";
                if(!val.matches(regEx)){
                    return "com.rxlogix.config.publisher.PublisherTemplateParameter.name.invalid.special.characters"
                }
            }
        })
        description(nullable: true, maxSize: 4000)
        value(nullable: true, maxSize: 4000)

    }

    Map toMap() {
        [name       : name, id: id,
         description: description,
         value      : value,
         //  type:type,
         dateCreated: dateCreated,
         lastUpdated: lastUpdated,
         createdBy  : createdBy,
         modifiedBy : modifiedBy,
        ]
    }
}
