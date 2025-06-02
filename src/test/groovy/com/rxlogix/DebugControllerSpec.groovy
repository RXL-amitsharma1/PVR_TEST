package com.rxlogix

import grails.converters.JSON
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.plugins.console.ConsoleService
import org.grails.plugins.console.Evaluation
import spock.lang.Specification

class DebugControllerSpec extends Specification implements ControllerUnitTest<DebugController> {

    void "test index"(){
        when:
        controller.index()
        then:
        response.text=="Welcome to debugger only accessible via server"
    }

    void "test execute"(){
        given:
        def mockConsoleService = Mock(ConsoleService)
        mockConsoleService.eval(_,_,_) >> {return new Evaluation()}
        controller.consoleService = mockConsoleService
        JSON.metaClass.static.use = {String configName, Closure<?> callable -> JSON}
        when:
        request.method="POST"
        controller.execute()
        then:
        response.status==200
    }

   /* void "test monitoring"(){
        given:
        when:
        controller.monitoring()
        then:
        response.status==200
    }
*/
}
