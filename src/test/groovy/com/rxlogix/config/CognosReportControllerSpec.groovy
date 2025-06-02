package com.rxlogix.config

import com.rxlogix.CRUDService
import com.rxlogix.CognosReportController
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([CognosReport])
class CognosReportControllerSpec extends Specification implements DataTest, ControllerUnitTest<CognosReportController> {

    def setup() {
        mockCognosReport()
    }

    def setupSpec() {
        mockDomain CognosReport
    }

    void "test update not found"(){
        given:
        CognosReport.metaClass.static.lock={null}
        when:
        request.method="POST"
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/cognosReport/index"
    }

    void "test update success"(){
        given:
        CognosReport cognosReport = new CognosReport(id:2,name:"rx")
        cognosReport.save(failOnError:true,validate:false)
        CognosReport.metaClass.static.lock={Serializable serializable -> return cognosReport}
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{return cognosReport}
        controller.CRUDService=mockCRUDService
        when:
        params.id=2L
        request.method="POST"
        params.name="anish"
        params.url="gmail.com"
        params.description="al"
        params.createdBy="user"
        params.modifiedBy="admin"
        controller.update()
        then:
        response.status==302
    }

    void "test update failure"(){
        given:
        CognosReport cognosReport = new CognosReport(name:"rx",createdBy: "user",modifiedBy: "admin")
        CognosReport.metaClass.static.lock={Serializable serializable -> return CognosReport.get(serializable)}
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{throw new ValidationException("Validation Exception",cognosReport.errors)}
        controller.CRUDService=mockCRUDService
        when:
        params.id=2
        request.method="POST"
        params.name="anish"
        params.url="gmail.com"
        params.description="al"
        params.createdBy="user"
        params.modifiedBy="admin"
        controller.update()
        then:
        response.status==200
    }

    void "Test index action, it renders the index page"() {

        when: "Call the index action."
        params.max = 2
        controller.index()

        then: "It renders the index page"
        view == "/cognosReport/index"
        model.cognosReportInstanceList == CognosReport.findAllByIsDeleted(false, params)
        model.cognosReportInstanceTotal == CognosReport.countByIsDeleted(false, params)
    }

    void "Test show action, when cognos report instance exists."() {

        given: "Cognos report instance id"
        params.id = 2

        when: "Call the show action."
        controller.show()

        then: "It renders the show page with id=2"
        view == "/cognosReport/show"
        model.cognosReportInstance == CognosReport.findByIdAndIsDeleted(params.id, false)
    }

    void "Test show action, when cognos report instance does not exists."() {

        given: "Cognos report id=null"
        params.id = null

        when: "Call the show action."
        controller.show()

        then: "It redirects to index action"
        response.redirectedUrl == "/cognosReport/index"
        response.status == 302
    }

    void "Test create action, it renders the create page."() {

        given: "A param name set to cognosReportTest"
        params.name = "cognosReportTest"

        when: "Call the create action."
        controller.create()

        then: "It renders the create page."
        view == "/cognosReport/create"
        model.cognosReportInstance instanceof CognosReport && params.name == model.cognosReportInstance.name
    }

    void "Test edit action, when cognos report instance exists."() {

        given: "Cognos report instance id"
        params.id = 2

        when: "Call the edit action."
        controller.edit()

        then: "It renders the show page"
        view == "/cognosReport/edit"
        model.cognosReportInstance == CognosReport.findByIdAndIsDeleted(params.id, false)
    }

    void "Cognos Report is saved."() {

        given: "params for cognos report instance "
        params.name = "cognosReportTest"
        params.url = "https://www.google.co.in"
        params.modifiedBy = "Application"
        params.createdBy = "Application"
        params.dateCreated = new Date()
        params.lastUpdated = new Date()
        params.isDeleted = false

        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save() { cognosReport -> new CognosReport(params).save() }
        controller.CRUDService = crudServiceMock.proxyInstance()

        when: "Call the save action."
        request.method = 'POST'
        controller.save()

        then: "It redirects to the show page."
        response.status == 302
        response.redirectedUrl == '/cognosReport/show/4'
    }

    void "Cognos Report with error cannot be saved."() {

        given: "params for cognos report instance "
        params.url = "https://www.google.co.in"
        params.lastUpdated = new Date()
        params.isDeleted = false
        CognosReport cognosReport = new CognosReport(params)
        cognosReport.validate()

        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save() { throw new ValidationException("Validation Exception", cognosReport.errors) }
        controller.CRUDService = crudServiceMock.proxyInstance()

        when: "Call the save action."
        request.method = 'POST'
        controller.save()

        then: "It render the create page and instance is not created."
        !cognosReport?.id
        view == '/cognosReport/create'
    }

    // Not working because of locking used in getting the instance of the cognos report instance.
    // Not finding a way to mock the lock.
    /*void "Cognos Report is updated."() {

        given: "params for cognos report instance "
        params.id = 1
        params.modifiedBy = "Test User"
        params.createdBy = "Test User"

        CognosReport.metaClass.static.lock = { id ->
            CognosReport cognosReport1 = CognosReport.get(1)
            return cognosReport1
        }

        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update() { originalCognosReport -> originalCognosReport }
        controller.CRUDService = crudServiceMock.proxyInstance()

        when: "Call the update action."
        controller.update()

        then: "It redirects to the show page."
        response.redirectedUrl == '/cognosReport/show/1'
    }*/

    void "Cognos Report is deleted."() {

        given: "A cognos report instance "
        CognosReport cognosReport = CognosReport.get(1)

        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete() { instance, name, justification -> cognosReport }
        controller.CRUDService = crudServiceMock.proxyInstance()

        when: "Call the delete action."
        request.method = 'POST'
        controller.delete(cognosReport)

        then: "It redirects to the index page."
        response.status == 302
        response.redirectedUrl == '/cognosReport/index'
    }

    private static mockCognosReport() {
        3.times {
            new CognosReport(name: "cognosReportTest", url: "https://www.google.co.in", modifiedBy: "Application", createdBy: "Application", dateCreated: new Date(), lastUpdated: new Date()).save(failOnError: true)
        }
    }
}
