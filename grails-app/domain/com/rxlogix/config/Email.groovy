package com.rxlogix.config

import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class Email {
    static auditable =  true
    @AuditEntityIdentifier
    String email
    String description
    boolean isDeleted = false
    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    Long tenantId

    static constraints = {
        email blank: false, maxSize: 200, email: true, validator: { val, obj ->
            if (!obj.id || obj.isDirty("email")) {
                Boolean exists = false
                exists = User.countByEnabledAndEmail(true, obj.email) ||
                        (obj.id ? Email.countByEmailAndIsDeletedAndIdNotEqual(obj.email, false, obj.id) :
                                Email.countByEmailAndIsDeleted(obj.email, false))
                if (exists) return "com.rxlogix.config.Email.email.unique"
            }
        }
        description blank: false, maxSize: 1000
    }

    static mapping = {
        table('EMAIL')
        email column: 'EMAIL'
        description column: 'DESCRIPTION'
        isDeleted column: 'IS_DELETED'
        tenantId column: 'TENANT_ID'
    }

    Map toEmailMap() {
        [
                id         : id,
                email      : email,
                description: description,
                lastUpdated: lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                modifiedBy : modifiedBy
        ]
    }

    public String toString() {
        return email
    }

}
