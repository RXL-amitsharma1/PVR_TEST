package com.rxlogix


import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class SpotfireControllerSpec extends Specification implements ControllerUnitTest<SpotfireController> {

    void "test index"(){
        when:
        controller.index()
        then:
        response.text == "This is a great software"
        response.status == 200
    }
}
