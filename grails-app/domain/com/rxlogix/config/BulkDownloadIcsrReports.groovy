package com.rxlogix.config

import com.rxlogix.user.User
import org.joda.time.DateTimeZone

class BulkDownloadIcsrReports {

    String downloadData
    User downloadBy

    //standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapping = {
        table name: "BULK_DOWNLOAD_ICSR_REPORTS"
        downloadData column: "DOWNLOAD_DATA"
        downloadBy column: "DOWNLOAD_PVUSER_ID"
        dateCreated column: "DATE_CREATED"
        lastUpdated column: "LAST_UPDATED"
        createdBy column: "CREATED_BY"
        modifiedBy column: "MODIFIED_BY"
        version false
    }

    static constraints = {
        downloadData nullable: false
        downloadBy nullable: false
        createdBy nullable: false
        modifiedBy nullable: false
    }

}