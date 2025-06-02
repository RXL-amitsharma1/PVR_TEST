package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.util.Holders
@CollectionSnapshotAudit
class ReportRequestLinkType {
    static auditable =  true
    @AuditEntityIdentifier
    String name
    String description
    boolean isDeleted = false
    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        name blank: false, validator: { val, obj ->
            if (val) {
                long count = ReportRequestLinkType.createCriteria().count {
                    ilike('name', "${val}")
                    eq('isDeleted', false)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "unique"
                }
            }
        }
        description nullable: true
    }

    static mapping = {
        table('REPORT_REQUEST_LINK_TYPE')
        name column: 'NAME'
        description column: 'DESCRIPTION', sqlType: DbUtil.stringType
        isDeleted column: 'IS_DELETED'
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

    public String toString() {
        return name
    }

}
