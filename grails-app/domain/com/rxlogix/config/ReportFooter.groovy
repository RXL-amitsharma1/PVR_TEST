package com.rxlogix.config

import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class ReportFooter {
    static auditable =  true
    @AuditEntityIdentifier
    String footer
    String description
    boolean isDeleted = false
    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        footer blank: false, maxSize: 1000, validator: {val, obj ->
            //Name is unique to user
            if (!obj.id || obj.isDirty("footer") || obj.isDirty("createdBy")) {
                long count = ReportFooter.createCriteria().count{
                    ilike('footer', "${val}")
                    eq('createdBy', obj.createdBy)
                    eq('isDeleted', false)
                    if (obj.id){ne('id', obj.id)}
                }
                if (count) {
                    return "com.rxlogix.config.reportFooter.footer.unique.per.user"
                }
            }
        }
        description nullable: true, maxSize: 4000
    }

    static mapping = {
        table('REPORT_FOOTER')
        footer column: 'FOOTER'
        description column: 'DESCRIPTION', sqlType: DbUtil.stringType
        isDeleted column: 'IS_DELETED'
    }

    Map toReportFooterMap() {
        [
                reportFooterId: id,
                footer               : footer,
                description        : description,
                lastUpdated        : lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                modifiedBy         : modifiedBy


        ]
    }

    public String toString() {
        return footer
    }

}
