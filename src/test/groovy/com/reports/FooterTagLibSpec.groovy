package com.reports


import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

class FooterTagLibSpec extends Specification implements TagLibUnitTest<FooterTagLib> {

    void "Test for SecurityPolicyLink by default" () {
        expect:
        tagLib.renderSecurityPolicyLink().toString() == ''
    }

    void "Test for SecurityPolicyLink if disabled" () {
        given:
        config.pvreports.privacy.policy.link = ''
        expect:
        tagLib.renderSecurityPolicyLink().toString() == ''
    }

    void "Test for buildDate" () {
        expect:
        tagLib.buildDate().toString() == new Date().toString()
    }
}