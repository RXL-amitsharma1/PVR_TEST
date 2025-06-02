package com.rxlogix.config

import com.rxlogix.util.DbUtil

/**
 * Contains application settings configured via Control Panel
 */

class ApplicationSettings {

    Boolean showJapaneseReportFields = false        //show or hide Japanese ReportFields in Template/Query CRUD form
    String dmsIntegration //enables integration with Document Management System

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy = "Application"
    String modifiedBy = "Application"
    String defaultUiSettings
    boolean runPriorityOnly = false

    static mapping = {
        table name: "APPLICATION_SETTINGS"

        showJapaneseReportFields column: "SHOW_JAPANESE_REPORT_FIELDS"
        dmsIntegration column: "DMS_INTEGRATION"
        defaultUiSettings  column: "DEFAULT_UI_SETTINGS", sqlType: DbUtil.longStringType
        runPriorityOnly column: "RUN_PRIORITY_ONLY"
    }

    static constraints = {
        createdBy(nullable: false, maxSize: 255)
        dmsIntegration(nullable: true, maxSize:4000)
        modifiedBy(nullable: false, maxSize: 255)
        defaultUiSettings(nullable: true)
    }

    @Override
    public String toString() {
        return "Default App Setting"
    }

}
