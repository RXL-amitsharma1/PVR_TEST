package com.rxlogix.config

import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class ReportRequestType {
    static auditable =  true
    @AuditEntityIdentifier
    String name
    String description
    boolean isDeleted = false
    Boolean aggregate = false
    PeriodicReportConfiguration configuration
    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        name maxSize: 255, blank: false, validator: { val, obj ->
            //Name is unique
            if (!obj.id || obj.isDirty("name")) {
                if (val){
                    long count = ReportRequestType.createCriteria().count{
                        ilike('name', "${val}")
                        eq('isDeleted', false)
                        if (obj.id){ne('id', obj.id)}
                    }
                    if(count) {
                        return "unique"
                    }
                }
            }
        }
        description nullable: true, maxSize: 32000
        aggregate nullable: true
        configuration nullable: true
    }

    static mapping = {
        table('REPORT_REQUEST_TYPE')
        name column: 'NAME'
        description column: 'DESCRIPTION', sqlType: DbUtil.stringType
        isDeleted column: 'IS_DELETED'
        configuration column: 'RCONFIG_ID'
    }

    Map toReportRequestTypeMap() {
        [
                id         : id,
                name       : name,
                aggregate  : aggregate,
                configuration  : configuration?.reportName?:"",
                description: description,
                lastUpdated: lastUpdated.format(DateUtil.DATEPICKER_UTC_FORMAT),
                modifiedBy : modifiedBy


        ]
    }

    public String toString() {
        return name
    }

}
