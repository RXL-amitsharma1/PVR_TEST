package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.ReportRequestTypeEnum
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import spock.lang.Specification

class ReportRequestTypeControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportRequestTypeController> {

    public static final user = "unitTest"

    def setupSpec() {
        mockDomains ReportRequestType, ReportRequestPriority, ReportRequestLinkType, UserDictionary, ReportRequest
    }

    void "Unique constraint check"() {
        when: "Two object with same name value"
        ReportRequestType reportRequestType1 = new ReportRequestType(name: ReportRequestTypeEnum.AD_HOC_REPORT.value, description: "desc", modifiedBy: "admin", createdBy: "admin")
        ReportRequestType reportRequestType2 = new ReportRequestType(name: ReportRequestTypeEnum.AD_HOC_REPORT.value, description: "desc1", modifiedBy: "admin1", createdBy: "admin1")

        then: "Then duplicate one should fail"
        reportRequestType1.save(flush: true)
        !reportRequestType2.save(flush: true)
        reportRequestType2.errors.getFieldError('name').code != null
    }



    void "object to map conversion"() {
        when: "Object is saved properly"
//        https://github.com/grails/grails-core/issues/676
        ReportRequestType reportRequestType = new ReportRequestType(name: ReportRequestTypeEnum.DSUR.value, description: "desc", modifiedBy: "admin",  createdBy: "admin", lastUpdated: new Date()).save(flush: true)

        then: "Map data should contains keys"
        reportRequestType.toReportRequestTypeMap().keySet().containsAll(['id','name','description','lastUpdated','modifiedBy'])
    }

    void "test index"(){
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test list"(){
        given:
        ReportRequestType reportRequestType = new ReportRequestType(name: ReportRequestTypeEnum.DSUR.value, description: "desc", modifiedBy: "admin",  createdBy: "admin", lastUpdated: new Date()).save(flush: true)
        when:
        controller.list()
        then:
        response.status == 200
    }

    void "test listPriority"(){
        given:
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        when:
        controller.listPriority()
        then:
        response.status == 200
    }

    void "test listLink"(){
        given:
        ReportRequestLinkType reportRequestLinkType = new ReportRequestLinkType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestLinkType.save(failOnError:true)
        when:
        controller.listLink()
        then:
        response.status == 200
    }

    void "test create"(){
        given:
        def reportRequest = instance
        reportRequest.save(failOnError:true)
        when:
        params.type = types
        controller.create()
        then:
        response.status == 200
        where:
        types                 |instance
        "priority"            |new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        "link"                |new ReportRequestLinkType(name: "link",createdBy: "user",modifiedBy: "user")
        "type"                |new ReportRequestType(name: "test",createdBy: "user",modifiedBy: "user")

    }

    void "test save"(){
        given:
        def reportRequest = instance
        reportRequest.save(failOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {return reportRequest}
        controller.CRUDService = mockCRUDService
        when:
        request.method = 'POST'
        params.type = type
        controller.save()
        then:
        flash.message == 'default.created.message'
        response.status == 302
        where:
        type                  |instance
        "priority"            |new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        "link"                |new ReportRequestLinkType(name: "link",createdBy: "user",modifiedBy: "user")
        "type"                |new ReportRequestType(name: "test",createdBy: "user",modifiedBy: "user")
    }

    void "test save validation exception"(){
        given:
        def reportRequest = instance
        reportRequest.save(failOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception", reportRequest.errors)}
        controller.CRUDService = mockCRUDService
        when:
        request.method = 'POST'
        params.type = type
        controller.save()
        then:
        response.status == 200
        where:
        type                  |instance
        "priority"            |new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        "link"                |new ReportRequestLinkType(name: "link",createdBy: "user",modifiedBy: "user")
        "type"                |new ReportRequestType(name: "test",createdBy: "user",modifiedBy: "user")
    }

    void "test edit when instance exists"(){
        given:
        def reportRequest = instance
        reportRequest.save(failOnError:true)
        when:
        params.id = 1L
        params.type = type
        controller.edit()
        then:
        response.status == 200
        where:
        type                  |instance
        "priority"            |new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        "link"                |new ReportRequestLinkType(name: "link",createdBy: "user",modifiedBy: "user")
        "type"                |new ReportRequestType(name: "test",createdBy: "user",modifiedBy: "user")
    }

    void "test edit when instance does not exist"(){
        when:
        controller.edit()
        then:
        response.status == 302
        response.redirectedUrl == "/reportRequestType/index?type="
    }

    void "test update when instance exists"(){
        given:
        def reportRequest = instance
        reportRequest.save(failOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {return reportRequest}
        controller.CRUDService = mockCRUDService
        when:
        request.method = 'POST'
        request.method = 'PUT'
        params.id = 1L
        params.type = type
        controller.update()
        then:
        flash.message == 'default.updated.message'
        response.status == 302
        where:
        type                  |instance
        "priority"            |new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        "link"                |new ReportRequestLinkType(name: "link",createdBy: "user",modifiedBy: "user")
        "type"                |new ReportRequestType(name: "test",createdBy: "user",modifiedBy: "user")
    }

    void "test update when instance exists and validation exception"(){
        given:
        def reportRequest = instance
        reportRequest.save(failOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception", reportRequest.errors)}
        controller.CRUDService = mockCRUDService
        when:
        request.method = 'POST'
        request.method = 'PUT'
        params.id = 1L
        params.type = type
        controller.update()
        then:
        response.status == 200
        where:
        type                  |instance
        "priority"            |new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        "link"                |new ReportRequestLinkType(name: "link",createdBy: "user",modifiedBy: "user")
        "type"                |new ReportRequestType(name: "test",createdBy: "user",modifiedBy: "user")
    }

    void "test show when instance exists"(){
        given:
        def reportRequest = instance
        reportRequest.save(failOnError:true)
        when:
        params.id = 1L
        params.type = type
        controller.show()
        then:
        response.status == 200
        where:
        type                  |instance
        "priority"            |new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        "link"                |new ReportRequestLinkType(name: "link",createdBy: "user",modifiedBy: "user")
        "type"                |new ReportRequestType(name: "test",createdBy: "user",modifiedBy: "user")
    }

    void "test show when instance does not exist"(){
        when:
        controller.show()
        then:
        response.status == 302
        response.redirectedUrl == "/reportRequestType/index?type="
    }

    void "test delete when instance does not exist"(){
        when:
        request.method = 'POST'
        request.method = 'DELETE'
        controller.delete()
        then:
        response.status == 302
        response.redirectedUrl == "/reportRequestType/index?type="
    }

    void "test delete when instance exists"(){
        given:
        def reportRequest = instance
        reportRequest.save(failOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_, _, _) >> {true}
        controller.CRUDService = mockCRUDService
        when:
        request.method = 'POST'
        request.method = 'DELETE'
        params.id = 1L
        params.type = type
        params.deleteJustification = "delete"
        controller.delete()
        then:
        flash.message == 'default.deleted.message'
        response.status == 302
        where:
        type                  |instance
        "priority"            |new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        "link"                |new ReportRequestLinkType(name: "link",createdBy: "user",modifiedBy: "user")
        "type"                |new ReportRequestType(name: "test",createdBy: "user",modifiedBy: "user")
    }

    void "test delete validation exception"(){
        given:
        def reportRequest = instance
        reportRequest.save(failOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_, _, _) >> {throw new ValidationException("Validation Exception", reportRequest.errors)}
        controller.CRUDService = mockCRUDService
        when:
        request.method = 'POST'
        request.method = 'DELETE'
        params.id = 1L
        params.type = type
        controller.delete()
        then:
        flash.error == "default.unable.deleted.message"
        response.status == 302
        where:
        type                  |instance
        "priority"            |new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        "link"                |new ReportRequestLinkType(name: "link",createdBy: "user",modifiedBy: "user")
        "type"                |new ReportRequestType(name: "test",createdBy: "user",modifiedBy: "user")
    }
}