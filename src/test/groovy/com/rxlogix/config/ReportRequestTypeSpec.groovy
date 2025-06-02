package com.rxlogix.config

import com.rxlogix.enums.ReportRequestTypeEnum
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ReportRequestTypeSpec extends Specification implements DomainUnitTest<ReportRequestType> {

    void "Unique constraint check"() {
        when: "Two object with same name value"
        ReportRequestType reportRequestType1 = new ReportRequestType(name: ReportRequestTypeEnum.AD_HOC_REPORT.value, description: "desc", modifiedBy: "admin", createdBy: "admin")
        ReportRequestType reportRequestType2 = new ReportRequestType(name: ReportRequestTypeEnum.AD_HOC_REPORT.value, description: "desc1", modifiedBy: "admin1", createdBy: "admin1")

        then: "Then duplicate one should fail"
        reportRequestType1.save(flush: true)
        !reportRequestType2.save(flush: true)
        reportRequestType2.errors.getFieldError('name').code != null
    }



    void "object to map conversion"() {
        when: "Object is saved properly"
//        https://github.com/grails/grails-core/issues/676
        ReportRequestType reportRequestType = new ReportRequestType(name: ReportRequestTypeEnum.DSUR.value, description: "desc", modifiedBy: "admin",  createdBy: "admin", lastUpdated: new Date()).save(flush: true)

        then: "Map data should contains keys"
        reportRequestType.toReportRequestTypeMap().keySet().containsAll(['id','name','description','lastUpdated','modifiedBy'])
    }

}
