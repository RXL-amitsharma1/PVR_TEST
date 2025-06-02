package com.rxlogix.config

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ExecutionStatusSpec extends Specification implements DomainUnitTest<ExecutionStatus> {

    def setup() {
    }

    def cleanup() {
    }

    void "test tenantId Can not be null"() {
        when:
        domain.tenantId = null

        then:
        !domain.validate(['tenantId'])
        domain.errors['tenantId'].code == "nullable"
    }
}