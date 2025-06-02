package com.rxlogix.user

import com.rxlogix.config.Tenant
import grails.testing.gorm.DataTest
import spock.lang.Specification

class ReportRequestEmailPreferenceSpec extends Specification implements DataTest {

    ReportRequestEmailPreference reportRequestEmailPreference

    def setupSpec() {
        mockDomains ReportRequestEmailPreference, Preference, User, Tenant
    }

    def createNewReportRequestEmailPreference() {
        reportRequestEmailPreference = new ReportRequestEmailPreference()
        reportRequestEmailPreference.id = 1L
        reportRequestEmailPreference.version = 2L
        reportRequestEmailPreference.creationEmails = true
        reportRequestEmailPreference.updateEmails = true
        reportRequestEmailPreference.deleteEmails = true
        reportRequestEmailPreference.workflowUpdate = true
        Preference preference = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user", user: new User(username: 'testUser', createdBy: "user", modifiedBy: "user", tenants: [getTenant()]))
        reportRequestEmailPreference.preference = preference
    }

    private Tenant getTenant() {
        def tenant = Tenant.get(1L)
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = 1L
        return tenant.save()
    }

    def setup() {
    }

    def cleanup() {
    }

    void "test creationEmails can be boolean"() {
        given:
        createNewReportRequestEmailPreference()
        when: "creationEmails equals value"
        reportRequestEmailPreference.creationEmails = value
        then:
        reportRequestEmailPreference.validate() == result
        where:
        value | result
        true  | true
        false | true
    }

    void "test updateEmails can be boolean"() {
        given:
        createNewReportRequestEmailPreference()
        when: "updateEmails equals value"
        reportRequestEmailPreference.updateEmails = value
        then:
        reportRequestEmailPreference.validate() == result
        where:
        value | result
        true  | true
        false | true
    }

    void "test jobEmails can be boolean"() {
        given:
        createNewReportRequestEmailPreference()
        when: "jobEmails equals value"
        reportRequestEmailPreference.deleteEmails = value
        then:
        reportRequestEmailPreference.validate() == result
        where:
        value | result
        true  | true
        false | true
    }

    void "test workflowUpdate can be boolean"() {
        given:
        createNewReportRequestEmailPreference()
        when: "workflowUpdate equals value"
        reportRequestEmailPreference.workflowUpdate = value
        then:
        reportRequestEmailPreference.validate() == result
        where:
        value | result
        true  | true
        false | true
    }

    void "test preference cannot be null"() {
        given:
        createNewReportRequestEmailPreference()
        when: "preference equals value"
        reportRequestEmailPreference.preference = value
        then:
        reportRequestEmailPreference.validate() == result
        where:
        value | result
        null  | false
    }

    void "test get Default Values"() {
        when:
        def result = ReportRequestEmailPreference.getDefaultValues(new Preference())

        then:
        result.creationEmails
        result.updateEmails
        result.deleteEmails
        result.workflowUpdate
    }
}