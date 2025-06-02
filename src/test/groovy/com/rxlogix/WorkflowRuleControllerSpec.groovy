package com.rxlogix

import com.rxlogix.config.AdvancedAssignment
import com.rxlogix.config.Tenant
import com.rxlogix.config.WorkflowRule
import com.rxlogix.config.WorkflowState
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.*
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants, User, SpringSecurityUtils])
class WorkflowRuleControllerSpec extends Specification implements DataTest, ControllerUnitTest<WorkflowRuleController>  {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains WorkflowState, WorkflowRule, User, Role, UserRole, Tenant, UserGroup, AdvancedAssignment
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser(0..1) { -> user }
        return securityMock.proxyInstance()
    }

    private User makeNormalUser(name, team) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_SYSTEM_CONFIGURATION', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user")
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
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

    void "test list action."() {
        given:
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        User normalUser = makeNormalUser("user2", [])
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser],executorGroups:[userGroup])
        workflowRuleInstance.save(failOnError:true,validate: false)
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> true}
        when:"call list action"
        controller.list()
        then:"It gives JSON response"
        response.status==200
        response.json[0].name=='testRule1'
    }

    void "test create"(){
        given:
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        User normalUser = makeNormalUser("user2", [])
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser],executorGroups:[userGroup])
        Tenants.metaClass.static.currentId = { -> return 1}
        when:
        controller.create()
        then:
        response.status==200
    }
    void "test show action, when instance exist."(){
        given:
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        User normalUser = makeNormalUser("user2", [])
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser],executorGroups:[userGroup])
        workflowRuleInstance.save(failOnError:true,validate: false)
        Tenants.metaClass.static.currentId = { -> return 1}
        when:"call show action"
        controller.show(1L)
        then:"It renders show view"
        view=='/workflowRule/show'
        model.workflowRuleInstance.name=='testRule1'
        model.initialStates.size()==1
        model.targetStates.size()==1
    }

    void "test show action, when instance doesn't exist."(){
        when:"call show action"
        controller.show(null)
        then:"It redirects to index action"
        response.redirectUrl=='/workflowRule/index'
        flash.error!=null
    }

    void "test edit action, when instance exist."(){
        given:
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        User normalUser = makeNormalUser("user2", [])
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser],executorGroups:[userGroup])
        workflowRuleInstance.save(failOnError:true,validate: false)
        Tenants.metaClass.static.currentId = { -> return 1}
        when:"call edit action"
        controller.edit(workflowRuleInstance)
        then:"It renders edit view"
        view=='/workflowRule/edit'
        model.workflowRuleInstance.name=='testRule1'
        model.initialStates.size()==1
        model.targetStates.size()==1
    }

    void "test edit action, when instance doesn't exist."(){
        when:"call edit action"
        controller.edit(null)
        then:"It redirects to index action"
        response.redirectUrl=='/workflowRule/index'
        flash.error!=null
    }

    void "test delete "(){
        given:
        User normalUser = makeNormalUser("user2", [])
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser],executorGroups:[userGroup])
        workflowRuleInstance.save(failOnError:true,validate: false)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete { theInstance,name, String justification = null -> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        when:
        request.method = 'POST'
        controller.delete(workflowRuleInstance.id)
        then:
        response.redirectUrl=='/workflowRule/index'
        flash.message!=null
    }

    void "test delete with validation error"(){
        given:
        User normalUser = makeNormalUser("user2", [])
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy: 'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser],executorGroups:[userGroup])
        workflowRuleInstance.save(failOnError:true , validate: false)
        ValidationErrors errors =new ValidationErrors(workflowRuleInstance)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete { theInstance,name, String justification = null -> throw new ValidationException("message", errors) }
        controller.CRUDService = crudServiceMock.proxyInstance()
        when:
        request.method = 'POST'
        controller.delete(workflowRuleInstance.id)
        then:
        response.redirectUrl=='/workflowRule/index'
        flash.error=="Unable to delete the Workflow Rule"
    }

    void "test delete - no instance"(){
        given:
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete { theInstance,name, String justification = null -> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        when:
        request.method = 'POST'
        controller.delete(9)
        then:
        response.redirectUrl=='/workflowRule/index'
        flash.error!=null
    }

    void "test bind executors is update true"(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser_1],executorGroups:[userGroup_1])
        workflowRuleInstance.save(failOnError:true,validate: false)
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
       List<String> executors=["User_${id1}","UserGroup_${id2}"]
        when:
        controller.bindExecutors(workflowRuleInstance,executors,true)
        then:
        workflowRuleInstance.executorGroups==[userGroup_2]
        workflowRuleInstance.executors==[normalUser_2]
    }

    void "test bind executors is update false"(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser_1],executorGroups:[userGroup_1])
        workflowRuleInstance.save(failOnError:true,validate: false)
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        List<String> executors=["User_${id1}","UserGroup_${id2}"]
        when:
        controller.bindExecutors(workflowRuleInstance,executors)
        then:
        workflowRuleInstance.executorGroups==[userGroup_1,userGroup_2]
        workflowRuleInstance.executors==[normalUser_1,normalUser_2]
    }

    void "test update "(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser_1],executorGroups:[userGroup_1])
        workflowRuleInstance.save(failOnError:true,validate: false)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { theInstance-> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityService(normalUser_1)
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        List<String> executors=["User_${id1}","UserGroup_${id2}"]
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params.name='testRule12'
        params.configurationTypeEnum=WorkflowConfigurationTypeEnum.ADHOC_REPORT
        params.initialState=workflowStateInstance
        params.targetState=workflowStateInstance
        params.canExecute=executors
        params.id=workflowRuleInstance.id
        controller.update(workflowRuleInstance.id)
        then:
        response.status == 302
        response.redirectUrl=='/workflowRule/index'
        flash.message!=null
        workflowRuleInstance.name=='testRule12'
    }

    void "test update with exception"(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser_1],executorGroups:[userGroup_1])
        workflowRuleInstance.save(failOnError:true,validate: false)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { theInstance-> throw new Exception() }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityService(normalUser_1)
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        List<String> executors=["User_${id1}","UserGroup_${id2}"]
        Tenants.metaClass.static.currentId = { -> return 1}
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params.name='testRule12'
        params.configurationTypeEnum=WorkflowConfigurationTypeEnum.ADHOC_REPORT
        params.initialState=workflowStateInstance
        params.targetState=workflowStateInstance
        params.canExecute=executors
        params.id=workflowRuleInstance.id
        controller.update(workflowRuleInstance.id)
        then:
        view=='/workflowRule/edit'
        flash.message==null
    }

    void "test update with validation error"(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser_1],executorGroups:[userGroup_1])
        workflowRuleInstance.save(failOnError:true,validate: false)
        ValidationErrors errors =new ValidationErrors(workflowRuleInstance)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { theInstance-> throw new ValidationException("message", errors) }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityService(normalUser_1)
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        List<String> executors=["User_${id1}","UserGroup_${id2}"]
        Tenants.metaClass.static.currentId = { -> return 1}
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params.name='testRule12'
        params.configurationTypeEnum=WorkflowConfigurationTypeEnum.ADHOC_REPORT
        params.initialState=workflowStateInstance
        params.targetState=workflowStateInstance
        params.canExecute=executors
        params.id=workflowRuleInstance.id
        controller.update(workflowRuleInstance.id)
        then:
        view=='/workflowRule/edit'
        workflowRuleInstance.name=='testRule12'
        flash.message==null
    }

    void "test save success"(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser_1],executorGroups:[userGroup_1])
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { theInstance -> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityService(normalUser_1)
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        String executors="User_${id1};UserGroup_${id2}"
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params['canExecute']=executors
        controller.save(workflowRuleInstance)
        then:
        response.status == 200
    }

    void "test save with validation error"(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser_1],executorGroups:[userGroup_1])
        ValidationErrors errors =new ValidationErrors(workflowRuleInstance)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { theInstance -> throw new ValidationException("message", errors) }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityService(normalUser_1)
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        String executors="User_${id1};UserGroup_${id2}"
        String assignedTo="User_${id1}"
        Tenants.metaClass.static.currentId = { -> return 1}
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params['canExecute']=executors
        params['assignedTo']=assignedTo
        controller.save(workflowRuleInstance)
        then:
        view=='/workflowRule/create'
        model.workflowRuleInstance.name=='testRule1'
        model.initialStates.size()==1
        model.targetStates.size()==1
        flash.message==null
    }

    void "test save exception error"(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance,
                executors: [normalUser_1],executorGroups:[userGroup_1])
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { theInstance -> throw new Exception() }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityService(normalUser_1)
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        String executors="User_${id1};UserGroup_${id2}"
        Tenants.metaClass.static.currentId = { -> return 1}
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params['canExecute']=executors
        controller.save(workflowRuleInstance)
        then:
        view=='/workflowRule/create'
        flash.message == null
        model.workflowRuleInstance.name=='testRule1'
        model.initialStates.size()==1
        model.targetStates.size()==1
    }
}
