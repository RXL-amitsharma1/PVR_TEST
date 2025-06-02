package com.rxlogix.config

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PeriodicReportConfigurationSpec extends Specification implements DomainUnitTest<PeriodicReportConfiguration> {

    void "test isTemplate"() {
        given:
        domain.isTemplate = false
        domain.productSelection = null
        domain.studySelection = null

        when: "test productSelection"

        then:
        domain.validate(['productSelection'])
    }
}