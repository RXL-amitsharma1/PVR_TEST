package com.reports


import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

class ShowXMLOptionTagLibSpec extends Specification implements TagLibUnitTest<ShowXMLOptionTagLib> {

    void "Test for showXMLOption"() {
        given:
        grailsApplication.config.show.xml.option = true
        expect:
        tagLib.showXMLOption([:], 'XML Option Enabled').toString() == 'XML Option Enabled'
    }
}