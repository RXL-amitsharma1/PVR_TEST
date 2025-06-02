package com.rxlogix.config


import com.rxlogix.enums.PageSizeEnum
import com.rxlogix.enums.SensitivityLabelEnum
import com.rxlogix.util.DbUtil
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import grails.plugins.orm.auditable.SectionModuleAudit
import net.sf.dynamicreports.report.constant.PageOrientation
@CollectionSnapshotAudit
@SectionModuleAudit(parentClassName = ['caseSeries','etlSchedule','configuration', 'periodicReportConfiguration','executedReportConfiguration','icsrProfileConfiguration','icsrReportConfiguration','executedIcsrProfileConfiguration','executedIcsrReportConfiguration', 'executedPeriodicReportConfiguration', 'executedConfiguration'])
class EmailConfiguration {
    static auditable =  true
    String subject
    String body
    String to
    String cc
    Boolean noEmailOnNoData = false
    Boolean isDeleted = false
    PageOrientation pageOrientation
    Boolean showPageNumbering
    Boolean excludeCriteriaSheet
    Boolean excludeAppendix
    Boolean excludeComments
    Boolean excludeLegend
    Boolean showCompanyLogo
    PageSizeEnum paperSize
    SensitivityLabelEnum sensitivityLabel
    Boolean deliveryReceipt = false

    static constraints = {
        subject nullable: false, maxSize: 2000
        body nullable: false
        to nullable: true
        cc nullable: true, maxSize: 8192
        pageOrientation nullable:true
        showPageNumbering nullable:true
        excludeCriteriaSheet nullable:true
        excludeAppendix nullable:true
        excludeComments nullable:true
        excludeLegend nullable: true
        showCompanyLogo nullable:true
        paperSize nullable:true
        sensitivityLabel nullable:true
    }

    static mapping = {
        table name: "EMAIL_CONFIGURATION"

        to column: "EMAIL_TO"
        subject column: "EMAIL_SUBJECT"
        body column: "EMAIL_BODY", sqlType: DbUtil.longStringType
        noEmailOnNoData column: "NO_EMAIL_ON_NO_DATA"
        isDeleted column: "IS_DELETED"
        deliveryReceipt column: "DELIVERY_RECEIPT"
    }

    @Override
    public String toString() {
        return subject
    }
}
