package com.rxlogix


import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class CapaControllerSpec extends Specification implements ControllerUnitTest<CapaController> {

    void "test capaList"(){
        when:
        controller.capaList()
        then:
        response.status == 200
    }

    void "test getCorrectiveMapping"(){
        given:
        def mockReportExecutorService=Mock(ReportExecutorService)
        mockReportExecutorService.getCorrectiveActionList()>>{}
        controller.reportExecutorService=mockReportExecutorService
        when:
        controller.getCorrectiveMapping()
        then:
        response.status == 200
    }

    void "test getPreventativeMapping"(){
        given:
        def mockReportExecutorService=Mock(ReportExecutorService)
        mockReportExecutorService.getPreventativeActionList()>>{}
        controller.reportExecutorService=mockReportExecutorService
        when:
        controller.getPreventativeMapping()
        then:
        response.status == 200
    }

    void "test saveCAPA------- if success"(){
        given:
        def mockCapaService= Mock(CapaService)
        mockCapaService.editCAPA(_,_,_,_)>>{}
        controller.capaService=mockCapaService
        when:
        params.id=1L
        params.textDesc = "capaValidTextDesc"
        controller.saveCAPA()
        then:
        response.text=="Ok"
    }

    void "test saveCAPA------- if fails"(){
        given:
        def mockCapaService=Mock(CapaService)
        mockCapaService.createCAPA(_,_,_)>>{}
        controller.capaService=mockCapaService
        when:
        params.id=null
        params.textDesc = "capaValidTextDesc"
        controller.saveCAPA()
        then:
        response.text=="Ok"
    }


    void "test deleteCorrective"(){
        given:
        def mockCapaService= Mock(CapaService)
        mockCapaService.deleteCAPA(_,_)>>{}
        controller.capaService=mockCapaService
        when:
        controller.deleteCorrective(1L,'PVC')
        then:
        response.status==302
        response.redirectedUrl=="/capa/capaList"
    }

    void "test deletePreventative"(){
        given:
        def mockCapaService=Mock(CapaService)
        mockCapaService.deleteCAPA(_,_)>>{}
        controller.capaService=mockCapaService
        when:
        controller.deletePreventative(1L,'PVC')
        then:
        response.status==302
        response.redirectedUrl=="/capa/capaList"
    }

}
