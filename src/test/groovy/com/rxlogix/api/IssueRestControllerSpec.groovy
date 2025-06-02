package com.rxlogix.api

import com.rxlogix.CRUDService
import com.rxlogix.config.Capa8D
import com.rxlogix.config.Capa8DAttachment
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Capa8D])
class IssueRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<IssueRestController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomain Capa8D
    }


    def "test updateCapaAttachment"(){
        given:
        Capa8D.metaClass.static.findByIssueNumber = { String issueNumber->
            return new Capa8D()
        }
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {}
        controller.CRUDService = mockCRUDService

        when:
        params.issueNumber = '20CWPK2MAK'
        params.ownerType = "PVQ"
        controller.updateCapaAttachment()

        then:
        response.status == 200
    }

    def "test bindfile"(){
        given:
        Capa8DAttachment attachment = new Capa8DAttachment()

        when:
        controller.bindFile(new Capa8D(), request.getFiles('file'), "ABC.txt")

        then:
        response.status == 200
    }
}
