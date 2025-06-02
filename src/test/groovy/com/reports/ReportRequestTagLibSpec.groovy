package com.reports

import com.rxlogix.config.ReportRequest
import grails.testing.gorm.DataTest
import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification

class ReportRequestTagLibSpec extends Specification implements DataTest, TagLibUnitTest<ReportRequestTagLib> {

    void setup() {
    }

    def setupSpec() {
        mockDomain ReportRequest
    }

    void "Test for renderRRSettingsEntityName" () {
        given:
        String reportRequest = 'Report Request Priority'
        String reportRequetMsgCode = 'app.label.UserDictioname.appName'

        expect:
        tagLib.renderRRSettingsEntityName([type:reportRequest]).toString() == reportRequetMsgCode
    }
}
