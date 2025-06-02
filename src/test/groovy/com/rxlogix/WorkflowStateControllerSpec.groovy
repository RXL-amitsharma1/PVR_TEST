package com.rxlogix

import com.rxlogix.config.Tenant
import com.rxlogix.config.WorkflowState
import com.rxlogix.config.WorkflowStateReportAction
import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User,WorkflowState])
class WorkflowStateControllerSpec extends Specification implements DataTest, ControllerUnitTest<WorkflowStateController> {

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference, WorkflowStateReportAction, WorkflowState
    }

    def setup() {
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
    }

    def cleanup() {
    }
    private User makeNormalUser(name, team) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user")
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
        normalUser.metaClass.static.isDev = { -> return false}
        return normalUser
    }

    private Tenant getTenant() {
        def tenant = Tenant.get(1L)
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = 1L
        return tenant.save()
    }

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user").save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isAdmin = { -> return true }
        adminUser.metaClass.getUserTeamIds = { -> [] }
        adminUser.metaClass.static.isDev = { -> return true}
        return adminUser
    }

    void "test list action."() {
        when:"call list action"
        controller.list()

        then:"It gives JSON response"
        response.status==200
        response.json[0].name=='test'
    }
    void "test create action, When instance exist."(){
        given:
        WorkflowState workflowState = new WorkflowState(name:'test2',createdBy:'user',modifiedBy:'user')
        workflowState.save(failOnError:true)
        when:"call create action"
        controller.create(workflowState)

        then:"It renders create view"
        view=='/workflowState/create'
        model.workflowStateInstance.name=='test2'
    }
    void "test create action, When instance doesn't exist."(){
        given:
        WorkflowState workflowState = null
        when:"call create action"
        controller.create(workflowState)

        then:"It renders create view"
        view=='/workflowState/create'
        model.workflowStateInstance==null

    }

    void "test save try success"(){
        given:
        WorkflowState workflowState = new WorkflowState(name:'test2',createdBy:'user',modifiedBy:'user')
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.save(_)>>{}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        controller.save(workflowState)
        then:
        response.status==302
        response.redirectedUrl=="/workflowState/index"
    }

    void "test save validation exception"(){
        given:
        WorkflowState workflowState = new WorkflowState(name:'test2',createdBy:'user',modifiedBy:'user')
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.save(_)>>{throw new ValidationException("Validation Exception", workflowState.errors)}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        controller.save(workflowState)
        then:
        response.status==200
    }

    void "test update try success"(){
        given:
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowState = new WorkflowState(name: "state_1",reportActions: [new WorkflowStateReportAction(executors: [normalUser],reportAction: ReportActionEnum.GENERATE_CASES_DRAFT)])
        WorkflowState.metaClass.static.get={Long id->workflowState}
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        controller.update(2L)
        then:
        response.status==302
        response.redirectedUrl=="/workflowState/index"
    }

    void "test update validation exception"(){
        given:
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowState = new WorkflowState(name: "state_1",reportActions: [new WorkflowStateReportAction(executors: [normalUser],reportAction: ReportActionEnum.GENERATE_CASES_DRAFT)])
        WorkflowState.metaClass.static.get={Long id->workflowState}
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{throw new ValidationException("Validation Exception", workflowState.errors)}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        controller.update(2L)
        then:
        response.status==200
    }

    void "test show action, when instance exist."(){
        when:"call show action"
        controller.show(1L)

        then:"It renders show view"
        view=='/workflowState/show'
        model.workflowStateInstance.name=='test'
    }
    void "test show action, when instance doesn't exist."(){
        when:"call show action"
        controller.show(null)

        then:"It redirects to index action"
        response.redirectUrl=='/workflowState/index'
        flash.message!=null
    }

    void "test edit action, when instance exist."(){
        given:
        WorkflowState workflowState = new WorkflowState(name:'test2',createdBy:'user',modifiedBy:'user')
        workflowState.save(failOnError:true)

        when:"call edit action"
        controller.edit(workflowState)

        then:"It renders edit view"
        view=='/workflowState/edit'
        model.workflowStateInstance.name=='test2'
    }
    void "test edit action, when instance doesn't exist."(){
        when:"call edit action"
        controller.edit(null)

        then:"It redirects to index action"
        response.redirectUrl=='/workflowState/index'
        flash.message!=null
    }

    void "test delete not found"(){
        given:
        WorkflowState.metaClass.static.get={null}
        when:
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/workflowState/index"
    }

    void "test delete found with name"(){
        given:
        WorkflowState workflowState = new WorkflowState(name: "New")
        WorkflowState.metaClass.static.get={Long id -> workflowState}
        when:
        controller.delete(2L)
        then:
        response.status==302
        response.redirectedUrl=="/workflowState/index"
    }

    void "test delete found try success"(){
        given:
        WorkflowState workflowState = new WorkflowState(name: "test")
        WorkflowState.metaClass.static.get={Long id -> workflowState}
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_)>>{}
        controller.CRUDService=mockCRUDService
        when:
        controller.delete(2L)
        then:
        response.status==302
        response.redirectedUrl=="/workflowState/index"
    }

    void "test delete found validation exception"(){
        given:
        WorkflowState workflowState = new WorkflowState(name: "test")
        WorkflowState.metaClass.static.get={Long id -> workflowState}
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_)>>{throw new ValidationException("Validation Exception", workflowState.errors)}
        controller.CRUDService=mockCRUDService
        when:
        controller.delete(2L)
        then:
        response.status==302
        response.redirectedUrl=="/workflowState/index"
    }
}
