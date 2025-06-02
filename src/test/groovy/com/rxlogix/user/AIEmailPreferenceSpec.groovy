package com.rxlogix.user

import com.rxlogix.config.Tenant
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class AIEmailPreferenceSpec extends Specification implements DomainUnitTest<AIEmailPreference> {

    AIEmailPreference actionItemEmailPreference

    def setupSpec() {
        mockDomains Preference, User, Tenant
    }

    def createNewAIEmailPreference(){
        actionItemEmailPreference=new AIEmailPreference()
        actionItemEmailPreference.id = 1L
        actionItemEmailPreference.version = 2L
        actionItemEmailPreference.creationEmails = true
        actionItemEmailPreference.updateEmails = true
        actionItemEmailPreference.jobEmails = true
        Preference preference = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user", user: new User(username: 'testUser', createdBy: "user", modifiedBy: "user", tenants: [getTenant()]))
        actionItemEmailPreference.preference = preference
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
        createNewAIEmailPreference()
        when:"creationEmails equals value"
        actionItemEmailPreference.creationEmails = value
        then:
        actionItemEmailPreference.validate() == result
        where:
        value | result
        true  | true
        false | true
    }

    void "test updateEmails can be boolean"() {
        given:
        createNewAIEmailPreference()
        when:"updateEmails equals value"
        actionItemEmailPreference.updateEmails = value
        then:
        actionItemEmailPreference.validate() == result
        where:
        value | result
        true  | true
        false | true
    }

    void "test jobEmails can be boolean"() {
        given:
        createNewAIEmailPreference()
        when:"jobEmails equals value"
        actionItemEmailPreference.jobEmails = value
        then:
        actionItemEmailPreference.validate() == result
        where:
        value | result
        true  | true
        false | true
    }

    void "test preference cannot be null"() {
        given:
        createNewAIEmailPreference()
        when:"preference equals value"
        actionItemEmailPreference.preference = value
        then:
        actionItemEmailPreference.validate() == result
        where:
        value | result
        null  | false
    }

    void "test get Default Values"() {
        when:
        def result = AIEmailPreference.getDefaultValues(new Preference())
        then:
        result.creationEmails
        result.updateEmails
        result.jobEmails
    }
}