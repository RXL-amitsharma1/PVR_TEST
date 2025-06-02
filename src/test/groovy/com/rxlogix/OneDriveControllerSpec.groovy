package com.rxlogix

import com.rxlogix.e2b.OneDriveService
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class OneDriveControllerSpec extends Specification implements ControllerUnitTest<OneDriveController> {

    def mockIcsrDriveService = Mock( OneDriveService )

    void setup() {

        controller.icsrDriveService = mockIcsrDriveService
    }

    void "test index"(){
        given:
        mockIcsrDriveService.checkOnDriveLogin()>>true
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test callback"(){
        given:
        mockIcsrDriveService.setAuthCode(_) >> {}
        when:
        controller.callback("test", null)
        then:
        response.status==302
        response.redirectedUrl=="/controlPanel/index"
    }

    void "test loadOneDriveClient"(){
        given:
        mockIcsrDriveService.loadOneDriveClient() >> {}
        when:
        controller.loadOneDriveClient()
        then:
        response.status==302
        response.redirectedUrl=="/controlPanel/index"
    }

    void "test loginOnOnDrive"(){
        given:
        mockIcsrDriveService.getAuthCodeUrl >> {}
        when:
        controller.loginOnOnDrive()
        then:
        response.status==302
        response.redirectedUrl=="/oneDrive/index"
    }
}
