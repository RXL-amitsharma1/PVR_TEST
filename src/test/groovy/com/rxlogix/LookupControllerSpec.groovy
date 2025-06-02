package com.rxlogix

import com.rxlogix.config.ActionItem
import com.rxlogix.config.ReportRequest
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class LookupControllerSpec extends Specification implements DataTest, ControllerUnitTest<LookupController> {

    def setupSpec() {
        mockDomain ActionItem
    }

    void "Test lookup, When class exists"(){
        when:
        params.name = "ActionItem"
        controller.lookup()
        then:
        response.status == 200
        response.json == []
    }

    void "Test lookup, When class not found"(){
        when:
        params.name = " ClassNotFound "
        controller.lookup()
        then:
        response.status == 200
        response.json == []
    }
}
