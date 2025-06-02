package com.rxlogix

import com.rxlogix.config.ReportRequest
import com.rxlogix.user.AccessControlGroup
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

class AccessControlGroupControllerSpec extends Specification implements DataTest, ControllerUnitTest<AccessControlGroupController> {


    def setupSpec() {
        mockDomain AccessControlGroup
    }

    def setup() {

        AccessControlGroup accessControlGroup=new AccessControlGroup(id:1,name:'test',ldapGroupName:'testGroup',createdBy:'user',modifiedBy:'user')
        accessControlGroup.save(flush:true,failOnError:true)
    }

    def cleanup() {
    }

    void "Index action renders index view."() {

        when:"call the index Action"
        controller.index()
        then:"It renders index view"
        view == '/accessControlGroup/index'
        model.acgInstanceList != null
        model.acgInstanceTotal != null
    }

    void "test Show action, When Instance exists. "(){
        given:"AccessControlGroup instance"
        AccessControlGroup accessControlGroup=AccessControlGroup.get(id)
        when:"call show action"
        controller.show(accessControlGroup)
        then:"It render show view"
        view=='/accessControlGroup/show'
        model.acgInstance==accessControlGroup
        where:
        id << [1]

    }

    void "test Show Action, When Instance does not exists. "(){
        given: "A null AccessControlGroup instance"
        AccessControlGroup accessControlGroup=null

        when: "Call the show action"
        controller.show(accessControlGroup)

        then: "It redirects to the index action."
        response.redirectUrl == '/accessControlGroup/index'
        response.status == 302
    }
    void "test create Action, with params "(){

        when: "Call the create method"
        params.name='test1'
        params.ldapGroupName='testGroup1'
        params.createdBy='user'
        params.modifiedBy='user'
        controller.create()

        then: "It renders Create View."
        view=='/accessControlGroup/create'
        model.acgInstance.name=='test1'
        model.acgInstance.ldapGroupName=='testGroup1'
        model.acgInstance.createdBy=='user'
        model.acgInstance.modifiedBy=='user'
    }
    void "test create Action, without params. "(){

        when: "Call the create method"
        controller.create()

        then: "It renders Create View."
        view=='/accessControlGroup/create'
        model.acgInstance!=null
        model.acgInstance.name==null
        model.acgInstance.ldapGroupName==null
        model.acgInstance.createdBy==null
        model.acgInstance.modifiedBy==null
    }

    void "test edit Action, When Instance Exist."(){
        given:"AccessControlGroup instance"
        AccessControlGroup accessControlGroup=AccessControlGroup.get(id)
        when:"call edit action"
        controller.show(accessControlGroup)
        then:"It render edit view"
        view=='/accessControlGroup/show'
        model.acgInstance==accessControlGroup
        where:
        id << [1]

    }
    void "test edit Action, When Instance doesn't Exist."(){
        given:"AccessControlGroup instance"
        AccessControlGroup accessControlGroup=null
        when:"call edit action"
        controller.show(accessControlGroup)
        then: "It redirects to the index action."
        response.redirectUrl == '/accessControlGroup/index'
        response.status == 302

    }

    void "test Save "(){
        given:

        AccessControlGroup instance=new AccessControlGroup(id:2,name:'test2',ldapGroupName:'testGroup2',createdBy:'user',modifiedBy:'user')
        instance.save(failOnError: true)
        when:
        def mockCRUDService=new MockFor(CRUDService)
        mockCRUDService.demand.save{theInstance->return instance }
        controller.CRUDService=mockCRUDService.proxyInstance()
        request.method='POST'
        controller.save(instance)
        then:
        response.status == 302
        flash.message != null
        response.redirectUrl == '/accessControlGroup/show/2'

    }
    void "test Update, When instance doesn't exist. "(){
        given: "A null AccessControlGroup instance"
        AccessControlGroup accessControlGroup=null

        when: "Call the update action"
        request.method='PUT'
        controller.update(accessControlGroup)

        then: "It redirects to the index action."
        response.status == 302
        response.redirectUrl == '/accessControlGroup/index'

    }
    void "test Update, When instance exist. "(){
        given: "A null AccessControlGroup instance"
        AccessControlGroup instance=new AccessControlGroup(id:2,name:'test2',ldapGroupName:'testGroup2',createdBy:'user',modifiedBy:'user')
        instance.save(failOnError: true)

        when: "Call the update action"
        def mockCRUDService=new MockFor(CRUDService)
        mockCRUDService.demand.update{acgInstance->return instance}
        controller.CRUDService=mockCRUDService.proxyInstance()
        request.method='PUT'
        controller.update(instance)

        then: "It redirects to the index action."
        response.status == 302
        flash.message != null
        response.redirectUrl == '/accessControlGroup/show/2'

    }
    void "test Delete, When instance doesn't exist. "(){
        given: "A null AccessControlGroup instance"
        AccessControlGroup accessControlGroup=null

        when: "Call the delete action"
        request.method='DELETE'
        controller.delete(accessControlGroup)

        then: "It redirects to the index action."
        response.status == 302
        response.redirectUrl == '/accessControlGroup/index'

    }
    void "test Delete, When instance exist -- Success "(){
        given: "A AccessControlGroup instance"
        AccessControlGroup instance=new AccessControlGroup(id:2,name:'test2',ldapGroupName:'testGroup2',createdBy:'user',modifiedBy:'user')
        instance.save(failOnError: true)

        when: "Call the Delete action"
        def mockCRUDService=new MockFor(CRUDService)
        mockCRUDService.demand.delete{acgInstance->}
        controller.CRUDService=mockCRUDService.proxyInstance()
        request.method='DELETE'
        controller.delete(instance)

        then: "It redirects to the index action."
        response.status == 302
        flash.message != null
        response.redirectUrl == '/accessControlGroup/index'

    }
    void "test Delete, When instance exist -- Failed "(){
        given: "A AccessControlGroup instance"
        AccessControlGroup instance=new AccessControlGroup(id:2,name:'test2',ldapGroupName:'testGroup2',createdBy:'user',modifiedBy:'user')

        when: "Call the Delete action"
        def mockCRUDService=new MockFor(CRUDService)
        mockCRUDService.demand.delete{acgInstance->acgInstance.delete()
            throw new ValidationException("Validation Exception",acgInstance.errors) }
        controller.CRUDService=mockCRUDService.proxyInstance()
        request.method='DELETE'
        controller.delete(instance)

        then: "It redirects to the index action."
        response.status == 302
        flash.error != null
        response.redirectUrl == '/accessControlGroup/show'

    }


}
