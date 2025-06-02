package com.rxlogix.config

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ConfigurationSpec extends Specification implements DomainUnitTest<Configuration> {

    def setup() {
    }

    def cleanup() {
    }

    void "test name can not be null"() {
        when:
        domain.reportName = null

        then:
        !domain.validate(['reportName'])
        domain.errors['reportName'].code == 'nullable'
    }

    void "test tenantId can not be null"() {
        when:
        domain.tenantId = null

        then:
        !domain.validate(['tenantId'])
        domain.errors['tenantId'].code == 'nullable'
    }
}