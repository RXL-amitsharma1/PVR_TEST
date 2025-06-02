package com.rxlogix


import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class ErrorsControllerSpec extends Specification implements ControllerUnitTest<ErrorsController> {
    void "test forbidden"(){
        when:
        controller.forbidden()
        then:
        response.status==302
    }

    void "test notFound"(){
        when:
        controller.notFound()
        then:
        response.status==200
    }

    void "test notAllowed"(){
        when:
        controller.notAllowed()
        then:
        response.status==200
    }

    void "test serverError"(){
        when:
        controller.serverError()
        then:
        response.status==200
    }
}
