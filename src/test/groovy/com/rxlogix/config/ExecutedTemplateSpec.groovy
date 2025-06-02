package com.rxlogix.config


import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ExecutedTemplateSpec extends Specification implements DomainUnitTest<ReportTemplate> {

    def setupSpec() {
        mockDomain ReportField
    }

    def setup() {
    }

    def cleanup() {
    }

}
