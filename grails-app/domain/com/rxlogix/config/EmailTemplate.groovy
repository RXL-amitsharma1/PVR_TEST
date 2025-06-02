package com.rxlogix.config

import com.rxlogix.enums.EmailTemplateTypeEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import com.rxlogix.util.ViewHelper
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class EmailTemplate {
    static auditable =  true
    @AuditEntityIdentifier
    String name
    String description
    String body
    User owner
    EmailTemplateTypeEnum type
    String to
    String cc

    boolean isDeleted = false
    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        name blank: false, maxSize: 255, unique: true
        description nullable: true, maxSize: 1000
        to nullable: true, maxSize: 1000
        cc nullable: true, maxSize: 1000
    }

    static mapping = {
        table('EMAIL_TEMPLATE')
        name column: 'NAME'
        description column: 'DESCRIPTION'
        body column: 'BODY', sqlType: DbUtil.longStringType
        owner column: 'OWNER_ID'
        type column: 'TYPE'
        to column: 'TO_ID'
        cc column: 'CC_ID'
        isDeleted column: 'IS_DELETED'
    }

    Map toMap() {
        [
                id         : id,
                name       : name,
                description: description,
                owner      : owner.fullName,
                type       : ViewHelper.getMessage(type.getI18nKey()),
                lastUpdated: lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                modifiedBy : modifiedBy
        ]
    }

    Map toContentMap() {
        [
                id         : id,
                name       : name,
                description: description,
                type       : type,
                body       : body,
                to       : to,
                cc       : cc
        ]
    }

    public String toString() {
        return name
    }

}
