package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import grails.util.Holders

class ReportRequestLink {

    ReportRequestLinkType linkType
    String description
    ReportRequest from
    ReportRequest to
    //   static hasOne = [from:ReportRequest, to:ReportRequest]

    boolean isDeleted = false
    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        description nullable: true, maxSize: 8000
    }

    static mapping = {
        table('REPORT_REQUEST_LINK')
        linkType column: 'LINK_TYPE_ID'
        description column: 'DESCRIPTION'
        from column: 'FROM_ID'
        to column: 'TO_ID'
        isDeleted column: 'IS_DELETED'
    }

    Map toReportRequestTypeMap() {
        [
                id         : id,
                from       : from.reportName,
                to         : to.reportName,
                linkType   : linkType.name,
                description: description,


        ]
    }

    public String toString() {
        return "[$from] - [$to]"
    }

}
