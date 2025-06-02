package com.rxlogix.user

import com.rxlogix.enums.AuditLogCategoryEnum
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class AccessControlGroup {
    static auditable = false
    @AuditEntityIdentifier
    String name
    String ldapGroupName
    String description

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapping = {
        table name: "ACCESS_CONTROL_GROUP"
        name column: "NAME"
        ldapGroupName column: "LDAP_GROUP_NAME"
        description column: "DESCRIPTION"
    }

    static constraints = {
        name(nullable: false, maxSize: 30, unique: true)
        ldapGroupName(nullable: false, maxSize: 30, unique: true)
        description(nullable: true, maxSize: 1000)

        createdBy(nullable: false, maxSize: 255)
        modifiedBy(nullable: false, maxSize: 255)
    }

    String toString() {
        "[AccessControlGroup = " +
                " id->${id}" +
                " name->${name}" +
                " ldapGroupName->${ldapGroupName}" +
                "]";
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof AccessControlGroup)) return false

        AccessControlGroup accessControlGroup = (AccessControlGroup) o

        if (ldapGroupName != accessControlGroup.ldapGroupName) return false
        if (name != accessControlGroup.name) return false

        return true
    }

    @Override
    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (ldapGroupName != null ? ldapGroupName.hashCode() : 0)
        return result
    }
}
