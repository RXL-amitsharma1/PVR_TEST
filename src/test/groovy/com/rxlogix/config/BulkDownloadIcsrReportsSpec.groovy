package com.rxlogix.config


import com.rxlogix.user.User
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class BulkDownloadIcsrReportsSpec extends Specification implements DomainUnitTest<BulkDownloadIcsrReports> {


    def setupSpec() {
        mockDomain User
    }

    void "test create bulkDownloadIcsrReports"() {
        given:
        def downloadData = '{exIcsrTemplateQueryId: 124, caseNumber: 23456789, versionNumber: 1}'
        User  user = new User(username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        def bulkDownloadIcsrReports = new BulkDownloadIcsrReports(id: 1, downloadBy: user, downloadData: downloadData)
        bulkDownloadIcsrReports.save(validate: false);

        when:
        def bulkDownloadIcsrReportsInstance = BulkDownloadIcsrReports.findById(1)

        then:
        bulkDownloadIcsrReportsInstance.id==1
        bulkDownloadIcsrReportsInstance.downloadBy==user
        bulkDownloadIcsrReportsInstance.downloadData==downloadData
    }
}