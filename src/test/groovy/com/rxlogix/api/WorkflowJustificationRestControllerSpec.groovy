package com.rxlogix.api

import com.rxlogix.*
import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.user.*
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationErrors
import groovy.mock.interceptor.MockFor
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants, User, WorkflowState, DrilldownCLLMetadata])
class WorkflowJustificationRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<WorkflowJustificationRestController> {

    def setup() {
        def normalUser = new User(username: "testUser", fullName: "Test User", preference: [locale: "en_US", timeZone: "UTC"])
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        WorkflowJustification.userService = controller.userService
    }

    def cleanup() {
        WorkflowJustification.userService = null
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
        return adminUser
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

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, ExecutedReportConfiguration, QualityCaseData, WorkflowJustification, WorkflowRule, WorkflowState, ExecutedPeriodicReportConfiguration, ReportRequest, ExecutionStatus, ActionItem, DrilldownCLLMetadata
    }

    void "test index"() {
        given:
        // Create and save a normal user
        User normalUser = makeNormalUser("user", [])

        // Create and save an action item
        ActionItem actionItem = new ActionItem(status: StatusEnum.IN_PROGRESS)
        actionItem.save(failOnError:true, validate:false,flush:true)

        // Create and save a concrete ExecutedReportConfiguration
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(actionItems: [actionItem]) as ExecutedReportConfiguration
        executedReportConfiguration.save(failOnError:true , validate:false,flush:true)

        // Create and save workflow state
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)

        // Create and save a workflow justification
        WorkflowJustification workflowJustification = new WorkflowJustification(
                executedReportConfiguration: executedReportConfiguration,
                fromState: workflowState,
                toState: workflowState,
                description: "description",
                routedBy: normalUser
        )
        workflowJustification.save(failOnError:true , validate:false)

        // Mock PeriodicReportService to return an empty map for targetStatesAndApplications
        def mockPeriodicReportService = new MockFor(PeriodicReportService)
        mockPeriodicReportService.demand.targetStatesAndApplications(0..1) { Long executedReportConfigurationInstance, String initialState ->
            return [:]
        }
        controller.periodicReportService = mockPeriodicReportService.proxyInstance()

        // Mock toWorkflowJustificationMap to return a fixed map
        WorkflowJustification.metaClass.toWorkflowJustificationMap = {
            return [
                    fromState: "workstate",
                    toState: "workstate",
                    justification: "description",
                    routedBy: "user",
                    assignedToUser: "user",
                    assignedToUserGroup: "group",
                    dateCreated: "2023-04-22"
            ]
        }

        when:
        // When the controller's index method is called
        params.id = executedReportConfiguration.id
        controller.index(executedReportConfiguration.id, "workstate")

        then:
        // Assert that the response contains the expected values
        response.status == 200
        response.json.workflowJustificationList.size() == 1
        response.json.workflowJustificationList[0].size() == 7 // Check that the map has 7 entries
        response.json.reportId == executedReportConfiguration.id
        response.json.actionItems == 1
    }



    void "test reportRequest"() {
        given:
        User normalUser = makeNormalUser("user", [])

        // Mock toWorkflowJustificationMap to avoid accessing currentUser
        WorkflowJustification.metaClass.toWorkflowJustificationMap = {
            return [
                    fromState: "",
                    toState: "",
                    justification: "",
                    routedBy: "",
                    assignedToUser: "",
                    assignedToUserGroup: "",
                    dateCreated: ""
            ]
        }

        ReportRequest reportRequest = new ReportRequest()
        reportRequest.save(failOnError: true, validate: false)

        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError: true, validate: false)

        WorkflowRule workflowRule = new WorkflowRule(
                configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST,
                initialState: workflowState,
                targetState: workflowState
        )
        workflowRule.save(failOnError: true, validate: false)

        workflowRule.targetState.metaClass.getReportActionsAsList = { -> [] }

        WorkflowJustification workflowJustification = new WorkflowJustification(
                reportRequest: reportRequest,
                fromState: workflowState,
                toState: workflowState,
                description: "description",
                routedBy: normalUser
        )
        workflowJustification.save(failOnError: true, validate: false)

        when:
        controller.reportRequest(reportRequest.id, "workstate")

        then:
        response.status == 200
        response.json instanceof Map
        response.json.size() == 5
    }

    void "test sendResponse"(){
        when:
        controller.sendResponse(200,"found")
        then:
        response.json == [errorRows:null, worflowName:null, action:null, message:"found", status:200]
    }

    void "test handleReportAction GENERATE_CASES_FINAL"(){
        boolean run = false
        LinkedHashMap<String,String> responseObject = [:]
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true)
        executedReportConfiguration.save(failOnError:true , validate:false,flush:true)
        def mockExecutionStatusService = new MockFor(ExecutionStatusService)
        mockExecutionStatusService.demand.generateDraft(0..1){ ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction = com.rxlogix.enums.ReportActionEnum.GENERATE_DRAFT ->
            run = true
        }
        controller.executionStatusService = mockExecutionStatusService.proxyInstance()
        when:
        controller.invokeMethod('handleReportAction', [executedReportConfiguration, ReportActionEnum.GENERATE_CASES_FINAL, responseObject] as Object[])
        then:
        responseObject == [code:"app.periodicReportConfiguration.state.update.GENERATE_CASES_FINAL"]
        run == true
    }

    void "test handleReportAction MARK_AS_SUBMITTED"(){
        boolean run = false
        LinkedHashMap<String,String> responseObject = [:]
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true)
        executedReportConfiguration.save(failOnError:true , validate:false,flush:true)
        def mockExecutionStatusService = new MockFor(ExecutionStatusService)
        mockExecutionStatusService.demand.generateDraft(0..1){ ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction = com.rxlogix.enums.ReportActionEnum.GENERATE_DRAFT ->
            run = true
        }
        controller.executionStatusService = mockExecutionStatusService.proxyInstance()
        when:
        controller.invokeMethod('handleReportAction', [executedReportConfiguration, ReportActionEnum.MARK_AS_SUBMITTED, responseObject] as Object[])
        then:
        responseObject == [code:"app.periodicReportConfiguration.state.update.MARK_AS_SUBMITTED",action: "markAsSubmitted"]
        run == false
    }

    void "test handleReportAction SEND_TO_DMS"(){
        boolean run = false
        LinkedHashMap<String,String> responseObject = [:]
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true)
        executedReportConfiguration.save(failOnError:true , validate:false,flush:true)
        def mockExecutionStatusService = new MockFor(ExecutionStatusService)
        mockExecutionStatusService.demand.generateDraft(0..1){ ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction = com.rxlogix.enums.ReportActionEnum.GENERATE_DRAFT ->
            run = true
        }
        controller.executionStatusService = mockExecutionStatusService.proxyInstance()
        when:
        controller.invokeMethod('handleReportAction', [executedReportConfiguration, ReportActionEnum.SEND_TO_DMS, responseObject] as Object[])
        then:
        responseObject == [code:"app.periodicReportConfiguration.state.update.SEND_TO_DMS",action: "sendToDms"]
        run == false
    }

    void "test handleReportAction ARCHIVE"(){
        boolean run = false
        LinkedHashMap<String,String> responseObject = [:]
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true)
        executedReportConfiguration.save(failOnError:true , validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){ theInstance ->
            run = true
            executedReportConfiguration.archived = true
            theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.invokeMethod('handleReportAction', [executedReportConfiguration, ReportActionEnum.ARCHIVE, responseObject] as Object[])
        then:
        responseObject == [:]
        run == true
        executedReportConfiguration.archived == true
    }

    void "test handleReportAction GENERATE_DRAFT"(){
        boolean run = false
        LinkedHashMap<String,String> responseObject = [:]
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true)
        executedReportConfiguration.save(failOnError:true , validate:false,flush:true)
        def mockExecutionStatusService = new MockFor(ExecutionStatusService)
        mockExecutionStatusService.demand.generateDraft(0..1){ ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction = com.rxlogix.enums.ReportActionEnum.GENERATE_DRAFT ->
            run = true
        }
        controller.executionStatusService = mockExecutionStatusService.proxyInstance()
        when:
        controller.invokeMethod('handleReportAction', [executedReportConfiguration, ReportActionEnum.GENERATE_DRAFT, responseObject] as Object[])
        then:
        responseObject == [code:"app.periodicReportConfiguration.state.update.GENERATE_DRAFT"]
        run == true
    }

    void "test handleReportAction GENERATE_CASES"(){
        boolean run = false
        LinkedHashMap<String,String> responseObject = [:]
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true)
        executedReportConfiguration.save(failOnError:true , validate:false,flush:true)
        def mockExecutionStatusService = new MockFor(ExecutionStatusService)
        mockExecutionStatusService.demand.generateDraft(0..1){ ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction = com.rxlogix.enums.ReportActionEnum.GENERATE_DRAFT ->
            run = true
        }
        controller.executionStatusService = mockExecutionStatusService.proxyInstance()
        when:
        controller.invokeMethod('handleReportAction', [executedReportConfiguration, ReportActionEnum.GENERATE_CASES, responseObject] as Object[])
        then:
        responseObject == [code:"app.periodicReportConfiguration.state.update.GENERATE_CASES"]
        run == true
    }

    void "test handleReportAction GENERATE_CASES_DRAFT"(){
        boolean run = false
        LinkedHashMap<String,String> responseObject = [:]
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true)
        executedReportConfiguration.save(failOnError:true , validate:false,flush:true)
        def mockExecutionStatusService = new MockFor(ExecutionStatusService)
        mockExecutionStatusService.demand.generateDraft(0..1){ ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction = com.rxlogix.enums.ReportActionEnum.GENERATE_DRAFT ->
            run = true
        }
        controller.executionStatusService = mockExecutionStatusService.proxyInstance()
        when:
        controller.invokeMethod('handleReportAction', [executedReportConfiguration, ReportActionEnum.GENERATE_CASES_DRAFT, responseObject] as Object[])
        then:
        responseObject == [code:"app.periodicReportConfiguration.state.update.GENERATE_CASES_DRAFT"]
        run == true
    }

    void "test handleReportAction GENERATE_FINAL"(){
        boolean run = false
        LinkedHashMap<String,String> responseObject = [:]
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true)
        executedReportConfiguration.save(failOnError:true , validate:false,flush:true)
        def mockExecutionStatusService = new MockFor(ExecutionStatusService)
        mockExecutionStatusService.demand.generateDraft(0..1){ ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction = com.rxlogix.enums.ReportActionEnum.GENERATE_DRAFT ->
            run = true
        }
        controller.executionStatusService = mockExecutionStatusService.proxyInstance()
        when:
        controller.invokeMethod('handleReportAction', [executedReportConfiguration, ReportActionEnum.GENERATE_FINAL, responseObject] as Object[])
        then:
        responseObject == [code:"app.periodicReportConfiguration.state.update.GENERATE_FINAL"]
        run == true
    }

    void "test save success"(){
        User normalUser = makeNormalUser("user",[])
        int run = 0
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){ theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        mockCRUDService.demand.update(0..1){  theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.toState = workflowState
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:"workstate", action:null, message:"app.periodicReportConfiguration.state.update.success", status:200]
        run == 2
    }

    void "test save needApproval true and password"(){
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState,needApproval: true)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        when:
        params.password = ""
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:null, action:null, message:"app.label.workflow.rule.fillLogon", status:500]
    }

    void "test save needApproval true and ldapService"(){
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState,needApproval: true)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockLdapService = new MockFor(LdapService)
        mockLdapService.demand.isLoginPasswordValid(0..1){String login, String password -> return false}
        controller.ldapService = mockLdapService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.password = "password"
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:null, action:null, message:"app.label.workflow.rule.approvl.fail", status:500]
    }

    void "test save success already running"(){
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState,needApproval: true)
        workflowRule.save(failOnError:true , validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true , validate:false,flush:true)
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: executedReportConfiguration.id,entityType: ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION,executionStatus: ReportExecutionStatusEnum.GENERATING)
        executionStatus.save(failOnError:true , validate:false,flush:true)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: executedReportConfiguration, fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockLdapService = new MockFor(LdapService)
        mockLdapService.demand.isLoginPasswordValid(0..1){String login, String password -> return true}
        controller.ldapService = mockLdapService.proxyInstance()
        when:
        params.password = "password"
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName: null, action:null, message:"workflowJustification.executedReportConfiguration.executing", status:500]
    }

    void "test save workflowRule forbidden"(){
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState,needApproval: true,executors: [makeAdminUser()])
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockLdapService = new MockFor(LdapService)
        mockLdapService.demand.isLoginPasswordValid(0..1){String login, String password -> return true}
        controller.ldapService = mockLdapService.proxyInstance()
        when:
        params.password = "password"
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:null, action:null, message:"app.label.workflow.rule.forbidden", status:500]
    }

    void "test save workflowRule change fail"(){
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowState = new WorkflowState(name: "workstate",isDeleted: true)
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState,needApproval: true)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockLdapService = new MockFor(LdapService)
        mockLdapService.demand.isLoginPasswordValid(0..1){String login, String password -> return true}
        controller.ldapService = mockLdapService.proxyInstance()
        when:
        params.password = "password"
        params.toState = workflowState
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:null, action:null, message:"app.label.workflow.rule.change.fail", status:500]
    }

    void "test save success with default message and clearing all conditions"(){
        User normalUser = makeNormalUser("user",[])
        int run = 0
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState,needApproval: true)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockLdapService = new MockFor(LdapService)
        mockLdapService.demand.isLoginPasswordValid(0..1){String login, String password -> return true}
        controller.ldapService = mockLdapService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){ theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        mockCRUDService.demand.update(0..1){  theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.password = "password"
        params.toState = workflowState
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:"workstate", action:null, message:"app.periodicReportConfiguration.state.update.success", status:200]
        run == 2
    }

    void "test save success with defaultReportAction as GENERATE_CASES_FINAL"(){
        User normalUser = makeNormalUser("user",[])
        int run = 0
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState,needApproval: true,defaultReportAction: ReportActionEnum.GENERATE_CASES_FINAL)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockLdapService = new MockFor(LdapService)
        mockLdapService.demand.isLoginPasswordValid(0..1){String login, String password -> return true}
        controller.ldapService = mockLdapService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){ theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        mockCRUDService.demand.update(0..1){  theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockExecutionStatusService = new MockFor(ExecutionStatusService)
        mockExecutionStatusService.demand.generateDraft(0..1){ ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction = com.rxlogix.enums.ReportActionEnum.GENERATE_DRAFT ->
            run++
        }
        controller.executionStatusService = mockExecutionStatusService.proxyInstance()
        when:
        params.password = "password"
        params.toState = workflowState
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:"workstate", action:null, message:"app.periodicReportConfiguration.state.update.GENERATE_CASES_FINAL", status:200]
        run == 3
    }

    void "test save success with defaultReportAction as MARK_AS_SUBMITTED"(){
        User normalUser = makeNormalUser("user",[])
        int run = 0
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState,needApproval: true,defaultReportAction: ReportActionEnum.MARK_AS_SUBMITTED)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockLdapService = new MockFor(LdapService)
        mockLdapService.demand.isLoginPasswordValid(0..1){String login, String password -> return true}
        controller.ldapService = mockLdapService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){ theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        mockCRUDService.demand.update(0..1){  theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.password = "password"
        params.toState = workflowState
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:"workstate", action:"markAsSubmitted", message:"app.periodicReportConfiguration.state.update.MARK_AS_SUBMITTED", status:200]
        run == 2
    }

    void "test save success with defaultReportAction as SEND_TO_DMS"(){
        User normalUser = makeNormalUser("user",[])
        int run = 0
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState,needApproval: true,defaultReportAction: ReportActionEnum.SEND_TO_DMS)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockLdapService = new MockFor(LdapService)
        mockLdapService.demand.isLoginPasswordValid(0..1){String login, String password -> return true}
        controller.ldapService = mockLdapService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){ theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        mockCRUDService.demand.update(0..1){  theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.password = "password"
        params.toState = workflowState
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:"workstate", action:"sendToDms", message:"app.periodicReportConfiguration.state.update.SEND_TO_DMS", status:200]
        run == 2
    }

    void "test save success with defaultReportAction as ARCHIVE"(){
        User normalUser = makeNormalUser("user",[])
        int run = 0
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState,needApproval: true,defaultReportAction: ReportActionEnum.ARCHIVE)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockLdapService = new MockFor(LdapService)
        mockLdapService.demand.isLoginPasswordValid(0..1){String login, String password -> return true}
        controller.ldapService = mockLdapService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){ theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        mockCRUDService.demand.update(0..2){  theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.password = "password"
        params.toState = workflowState
        controller.save(workflowJustification)
        then:
        run == 3
    }

    void "test save validation error"(){
        User normalUser = makeNormalUser("user",[])
        int run = 0
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){ theInstance, Map saveParams = null ->
            throw new grails.validation.ValidationException("message",new ValidationErrors(new Object()))
        }
        mockCRUDService.demand.update(0..1){  theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.toState = workflowState
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:null, action:null, message:"", status:404]
        run == 0
    }

    void "test save exception"(){
        User normalUser = makeNormalUser("user",[])
        int run = 0
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){ theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        mockCRUDService.demand.update(0..1){  theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.save(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:null, action:null, message:"app.periodicReportConfiguration.state.update.failure", status:500]
        run == 0
    }

    void "test saveReportRequest success"(){
        User normalUser = makeNormalUser("user",[])
        int run = 0
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(reportRequest: new ReportRequest(), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){ theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        mockCRUDService.demand.update(0..1){  theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockReportRequestService = new MockFor(ReportRequestService)
        mockReportRequestService.demand.getReportRequestMap(0..1){reportRequest ->
            run++
            return [:]
        }
        mockReportRequestService.demand.getNotificationRecipients(0..1){ReportRequest reportRequestInstance, String mode ->
            run++
            return [] as Set<String>
        }
        mockReportRequestService.demand.sendReportRequestNotification(0..1){ReportRequest reportRequestInstance, def recipients, def mode, def oldReportRequestRef, emailSubject ->
            run++
        }
        controller.reportRequestService = mockReportRequestService.proxyInstance()
        when:
        controller.saveReportRequest(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:null, action:null, message:"app.periodicReportConfiguration.state.update.success", status:200]
        run == 5
    }

    void "test saveReportRequest validation error"(){
        User normalUser = makeNormalUser("user",[])
        int run = 0
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(reportRequest: new ReportRequest(), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){ theInstance, Map saveParams = null ->
            throw new grails.validation.ValidationException("message",new ValidationErrors(new Object()))
        }
        mockCRUDService.demand.update(0..1){  theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockReportRequestService = new MockFor(ReportRequestService)
        mockReportRequestService.demand.getReportRequestMap(0..1){reportRequest ->
            run++
            return [:]
        }
        mockReportRequestService.demand.getNotificationRecipients(0..1){ReportRequest reportRequestInstance ->
            run++
            return [] as Set<String>
        }
        mockReportRequestService.demand.sendReportRequestNotification(0..1){ReportRequest reportRequestInstance, def recipients, def mode, def oldReportRequestRef, emailSubject ->
            run++
        }
        controller.reportRequestService = mockReportRequestService.proxyInstance()
        when:
        controller.saveReportRequest(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:null, action:null, message:"", status:404]
        run == 0
    }

    void "test saveReportRequest exception"(){
        User normalUser = makeNormalUser("user",[])
        int run = 0
        WorkflowState workflowState = new WorkflowState(name: "workstate")
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState)
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(reportRequest: new ReportRequest(), fromState: workflowState,toState: workflowState,description: "description",workflowRule: workflowRule)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){ theInstance, Map saveParams = null ->
            throw new Exception()
        }
        mockCRUDService.demand.update(0..1){  theInstance, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockReportRequestService = new MockFor(ReportRequestService)
        mockReportRequestService.demand.getReportRequestMap(0..1){reportRequest ->
            run++
            return [:]
        }
        mockReportRequestService.demand.getNotificationRecipients(0..1){ReportRequest reportRequestInstance ->
            run++
            return [] as Set<String>
        }
        mockReportRequestService.demand.sendReportRequestNotification(0..1){ReportRequest reportRequestInstance, def recipients, def mode, def oldReportRequestRef, emailSubject ->
            run++
        }
        controller.reportRequestService = mockReportRequestService.proxyInstance()
        when:
        controller.saveReportRequest(workflowJustification)
        then:
        response.json == [errorRows:null, worflowName:null, action:null, message:"app.periodicReportConfiguration.state.update.failure", status:500]
        run == 0
    }

    @Ignore
    void "save Quality WorkFlow - success"(){
        given:
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowState = new WorkflowState(name: "workstate", id: 1L)
        WorkflowState targetWorkflowState = new WorkflowState(name: "targetWorkstate", id: 2L)
        workflowState.save(failOnError:true , validate:false)
        QualityCaseData qualityCaseData = new QualityCaseData(id:1, reportId: 1L, assignedToUser: normalUser, qualityData: "Test", type: "Aman").save(validate: false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState, targetState: targetWorkflowState, needApproval: true, executors: [normalUser], assignmentRule: 'BASIC_RULE', assignedToUserGroup: [new UserGroup()], assignedToUser: [new User()])
        workflowRule.save(failOnError:true , validate:false)
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(), fromState: workflowState,toState: targetWorkflowState,description: "description",workflowRule: workflowRule,qualityCaseData: qualityCaseData,qualityData: "abc", moduleName: "PVQ")
        def mockUserService = Mock(UserService)
        mockUserService.currentUser >> { normalUser}
        controller.userService = mockUserService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.save(_) >> {true}
        mockCRUDService.saveOrUpdate(_)>>{true}
        controller.CRUDService=mockCRUDService
        def mockQualityService = Mock(QualityService)
        mockQualityService.getIdToUpdateWorkflow(_,_,_) >> { [goodIds: [1L], badIds: new HashSet()]}
        mockQualityService.CRUDService = mockCRUDService
        controller.qualityService = mockQualityService
        WorkflowState.metaClass.static.isAssigned = {return normalUser}
        WorkflowState.metaClass.static.findByIdAndIsDeleted = { def id, boolean b ->
            null
        }
        controller.workflowService = new WorkflowService()
        controller.workflowService.CRUDService = mockCRUDService
        controller.workflowService.qualityService = mockQualityService
        controller.workflowService.userService = mockUserService
        Tenants.metaClass.static.currentId={1L}
        when:
        params.toState = targetWorkflowState
        controller.saveQualityWorkFlow(workflowJustification)

        then:
        response.json == [errorRows:null, action:null, message:"app.periodicReportConfiguration.state.update.success", status:200]

    }

    void "save PVC WorkFlow - success"(){
        given:
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowState = new WorkflowState(name: "workstate", id: 1L)
        WorkflowState targetWorkflowState = new WorkflowState(name: "targetWorkstate", id: 2L)
        workflowState.save(failOnError:true , validate:false)
        WorkflowRule workflowRule = new WorkflowRule(initialState: workflowState, targetState: targetWorkflowState, needApproval: true, executors: [normalUser], assignmentRule: 'BASIC_RULE', assignedToUserGroup: [new UserGroup()], assignToUser: true )
        workflowRule.save(failOnError:true , validate:false)
        DrilldownCLLMetadata drilldownCLLMetadata = new DrilldownCLLMetadata(caseId: 1L, processedReportId: 3L, tenantId: 2L)
        drilldownCLLMetadata.save(failOnError:true , validate:false)
        InboundDrilldownMetadata inboundMetadata = new InboundDrilldownMetadata(senderId: '-1')
        WorkflowJustification workflowJustification = new WorkflowJustification(executedReportConfiguration: new ExecutedPeriodicReportConfiguration(), drilldownCLLMetadata: drilldownCLLMetadata, fromState: workflowState,toState: targetWorkflowState,description: "description",workflowRule: workflowRule, inboundMetadata: inboundMetadata)
        def mockUserService = Mock(UserService)
        mockUserService.currentUser >> { normalUser}
        controller.userService = mockUserService
        WorkflowState.metaClass.static.findByIdAndIsDeleted = { def id, boolean b ->
            null
        }
        DrilldownCLLMetadata.metaClass.static.getMetadataRecord = { def metadataParams ->
            new DrilldownCLLMetadata(caseId: 1L, processedReportId: 3L, tenantId: 2L, assignedToUser: normalUser)
        }

        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.save(_) >> {true}
        mockCRUDService.saveOrUpdate(_)>>{true}
        controller.CRUDService=mockCRUDService
        def mockWorkflowService = Mock(WorkflowService)
        mockWorkflowService.assignPvcWorkflow(_,_,_) >> {return [success: true, message: null, rowInfo: null]}
        controller.workflowService = mockWorkflowService

        when:
        params.toState = targetWorkflowState
        controller.savePVCWorkFlow(workflowJustification)

        then:
        response.json == [errorRows:null, worflowName:null, action:null, message:"app.periodicReportConfiguration.state.update.success", status:200]
    }

}
