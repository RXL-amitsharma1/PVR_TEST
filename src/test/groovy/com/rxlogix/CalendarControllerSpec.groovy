package com.rxlogix


import grails.testing.web.controllers.ControllerUnitTest

class CalendarControllerSpec implements ControllerUnitTest<CalendarController> {

    void "test index"(){
        when:
        controller.index()
        then:
        response.status == 200
    }
}
