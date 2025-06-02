package com.rxlogix

import com.rxlogix.config.ReportRequest
import com.rxlogix.gateway.AxwayMessage
import com.rxlogix.gateway.AxwayService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor

class AxwayServiceSpec implements DataTest, ServiceUnitTest<AxwayService>{


    def setupSpec() {
        mockDomain AxwayMessage
    }

    void "test setTransmitDateForFile"(){
        given:
        AxwayMessage axwayMessage = new AxwayMessage(fileName: "file1.xml", linkedMessageId:1, status: "TRANSMITTED");
        axwayMessage.save(flush:true, saveOnError:false, validate:false);
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update(0..2) { theInstance -> theInstance }
        service.CRUDService = crudServiceMock.proxyInstance()

        when:
        service.setTransmitDateForFile("file1.xml", "TRANSMITTED")
        then: "Object was created successfully"
        AxwayMessage.count() ==1
    }

    void "test setAckReceiveDateForFile"(){
        given:
        AxwayMessage axwayMessage = new AxwayMessage(fileName: "file1.ack", direction: "Inbound", status: "ACK_RECIEVED")
        axwayMessage.save(flush:true, saveOnError:false, validate:false);
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update(0..1) { theInstance -> theInstance }
        service.CRUDService = crudServiceMock.proxyInstance()
        when:
        service.setAckReceiveDateForFile("file1.ack", "ACK_RECIEVED")
        then: "Object was created successfully"
        AxwayMessage.count() ==1
    }
}
