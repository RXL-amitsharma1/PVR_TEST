package com.reports


import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

class CsrfTagLibSpec extends Specification implements TagLibUnitTest<CsrfTagLib> {

    void "test isCsrfProtectionEnabled"() {
        when:
        grailsApplication.config.csrfProtection.enabled = true
        then:
        tagLib.isCsrfProtectionEnabled([:], 'CSRF Enabled').toString() == 'CSRF Enabled'
    }
}