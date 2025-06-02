package com.rxlogix.user

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class UserSpec extends Specification implements DomainUnitTest<User> {

    def setup() {
    }

    def cleanup() {
    }

    void "test tenantId can not be null"() {
        when:
        domain.tenants = []

        then:
        !domain.validate(['tenants'])
        domain.errors['tenants'].code == "minSize.notmet"
    }
}