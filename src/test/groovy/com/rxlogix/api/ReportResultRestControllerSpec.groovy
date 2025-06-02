package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.UserService
import com.rxlogix.config.*
import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.user.*
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

/*
@TestFor(ReportResultRestController)
@Mock([ReportResult, User, Role, UserRole,Tenant, Preference, Configuration,SharedWith, TemplateQuery, ExecutedConfiguration, ExecutedTemplateQuery, CaseLineListingTemplate, Query, ReportTemplate, ExecutedQuery,ExecutedReportUserState,WorkflowState,ActionItem,WorkflowStateReportAction,ExecutedDeliveryOption,UserGroup])
@Build([Query,CaseLineListingTemplate])
*/
@ConfineMetaClassChanges([User, ReportResultRestController, ExecutedConfiguration, ExecutedReportConfiguration])
class ReportResultRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportResultRestController> {

    def setup() {
//        def normalUser = makeNormalUser()
//        def resultData = new ReportResultData(value: "unit test".bytes, versionSQL: "", querySQL: "", reportSQL: "")
//
//        controller.springSecurityService = makeSecurityService(normalUser)
//        def templateQuery = new TemplateQuery()
//        def config1 = new Configuration(reportName: "test config" ,deliveryOption: new DeliveryOption(),owner:normalUser, createdBy: normalUser.username, modifiedBy: normalUser.username).addToTemplateQueries(templateQuery)
//
//        def ec1 = new ExecutedConfiguration (config1.properties)
//
//        List<ReportField> selectedFields = new ArrayList<ReportField>()
//        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
//                executedTemplate: new ReportTemplate(name:"test", createdBy:normalUser, clumnList: new ReportFieldInfoList()),
//                executedConfiguration: ec1, createdBy: normalUser.username, modifiedBy: normalUser.username)
//        executedTemplateQuery.executedDateRangeInformationForTemplateQuery = new ExecutedDateRangeInformation()
//        ec1.executedDeliveryOption = new ExecutedDeliveryOption(config1.deliveryOption.properties)
//        def newResult = new ReportResult( report: config1, data: resultData, executionStatus: ReportExecutionStatusEnum.COMPLETED, status: ReportResultStatusEnum.NEW, scheduledBy: normalUser, templateQuery:templateQuery,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)
//
//
//        def archivedResult = new ReportResult( report: config1, data: resultData, executionStatus: ReportExecutionStatusEnum.COMPLETED, status: ReportResultStatusEnum.REVIEWED, scheduledBy: normalUser, templateQuery:templateQuery,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)
//
//        def analysisResult = new ReportResult( report: config1, data: resultData, executionStatus: ReportExecutionStatusEnum.COMPLETED, status: ReportResultStatusEnum.NON_REVIEWED, scheduledBy: normalUser, templateQuery:templateQuery,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)
//        request.addHeader("Accept", "application/json")
    }

    def cleanup() {
    }

    def cleanupSpec() {
        User.metaClass.encodePassword = null
    }

    def setupSpec() {
        mockDomains ReportResult, User, Role, UserRole, Tenant, Preference, Configuration, SharedWith, TemplateQuery, ExecutedConfiguration, ExecutedTemplateQuery, CaseLineListingTemplate, Query, ReportTemplate, ExecutedQuery, ExecutedReportUserState, WorkflowState, ActionItem, WorkflowStateReportAction, ExecutedDeliveryOption, UserGroup
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

    private makeSecurityService(User user) {
        def securityMock = new MockFor(SpringSecurityService)
        securityMock.demand.getCurrentUser { -> user }
        return securityMock.proxyInstance()
    }
//we cannot test criteria query in unit test so we just test method call and response.

    void "PVR-151: By default do not show Archived Reports in My Inbox"() {
        given: "Report results with json format"
        User normalUser = makeNormalUser("user",[])
        ExecutedReportUserState executedReportUserState = new ExecutedReportUserState(user:normalUser,isFavorite: true,isArchived: false )
        executedReportUserState.save(failOnError:true,validate:false,flush:true)
        WorkflowState workflowState = new WorkflowState(name: "state_1",reportActions: [new WorkflowStateReportAction(executors: [normalUser],reportAction: ReportActionEnum.GENERATE_CASES_DRAFT)])
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(workflowState: workflowState,actionItems: [new ActionItem(status: StatusEnum.IN_PROGRESS)],reportName: "report",owner: normalUser,description: "description")
        executedConfiguration.dateCreated = new Date()
        executedConfiguration.save(failOnError:true,validate:false,flush:true)
        executedConfiguration.workflowState.metaClass.getReportActionsAsList = { return []}

        def mockControllerUserService = new MockFor(UserService)
        mockControllerUserService.demand.getCurrentUser(0..2){-> normalUser}
        controller.userService = mockControllerUserService.proxyInstance()

        ReportResultRestController.metaClass.static.getCompletedReport = { List<ExecutedConfiguration> executedConfigurations -> [ExecutedConfiguration.get(1)] }
        ReportResultRestController.metaClass.static.getExecutedConfigMaps = { List<ExecutedConfiguration> executedConfigurations -> ["please": 'work'] }

        ExecutedConfiguration.metaClass.static.fetchAllBySearchStringAndStatusInList = { LibraryFilter filter ->
            new Object() {
                List list(Object o) {
                    return [[executedConfiguration.id]]
                }
            }
        }
        ExecutedConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        when: "Call index method to show My Inbox"
        controller.index()

        then: "Report status should not be archived"
        response.status == 200
    }

    private mockCriteriaForAll() {
        def myCriteria = [
                list : {Closure  cls -> [ExecutedConfiguration.findAll()]}
        ]
        ExecutedConfiguration.metaClass.static.createCriteria = { myCriteria }

        def c = ExecutedConfiguration.createCriteria()
        return c
    }

    def mockGetReportCompleted() {
        def statusMock = new MockFor(ExecutedConfiguration)
        statusMock.demand.getCompletedReport { [] }
        return statusMock.proxyInstance()
    }

    void "test getExecutedConfigMaps"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportUserState executedReportUserState = new ExecutedReportUserState(user:normalUser,isFavorite: true,isArchived: false )
        executedReportUserState.save(failOnError:true,validate:false,flush:true)
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(workflowState: new WorkflowState(name: "state_1",reportActions: [new WorkflowStateReportAction(executors: [normalUser],reportAction: ReportActionEnum.GENERATE_CASES_DRAFT)]),actionItems: [new ActionItem(status: StatusEnum.IN_PROGRESS)],reportName: "report",owner: normalUser,description: "description")
        executedConfiguration.dateCreated = new Date()
        executedConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        executedConfiguration.workflowState.userService = mockUserService.proxyInstance()
        when:
        def result = controller.getExecutedConfigMaps([executedConfiguration],1,1,normalUser,true)
        then:
        result.recordsFiltered == 1
        result.recordsTotal == 1
        result.aaData[0].size() == 15
    }

    void "test index sort is owner"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportUserState executedReportUserState = new ExecutedReportUserState(user:normalUser,isFavorite: true,isArchived: false )
        executedReportUserState.save(failOnError:true,validate:false,flush:true)
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(workflowState: new WorkflowState(name: "state_1",reportActions: [new WorkflowStateReportAction(executors: [normalUser],reportAction: ReportActionEnum.GENERATE_CASES_DRAFT)]),actionItems: [new ActionItem(status: StatusEnum.IN_PROGRESS)],reportName: "report",owner: normalUser,description: "description")
        executedConfiguration.dateCreated = new Date()
        executedConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        executedConfiguration.workflowState.userService = mockUserService.proxyInstance()
        def mockControllerUserService = new MockFor(UserService)
        mockControllerUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockControllerUserService.proxyInstance()
        ExecutedConfiguration.metaClass.static.fetchAllBySearchStringAndStatusInList = { LibraryFilter filter -> new Object(){
                List list(Object o){
                    return [[executedConfiguration.id]]
                }
            }
        }
        ExecutedConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = { LibraryFilter filter -> new Object(){
                Long get() {
                    return 1
                }
            }
        }
        when:
        params.sort = "owner"
        controller.index()
        then:
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
        response.json.aaData[0].size() == 15
    }

    void "test index sort is version"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportUserState executedReportUserState = new ExecutedReportUserState(user:normalUser,isFavorite: true,isArchived: false )
        executedReportUserState.save(failOnError:true,validate:false,flush:true)
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(workflowState: new WorkflowState(name: "state_1",reportActions: [new WorkflowStateReportAction(executors: [normalUser],reportAction: ReportActionEnum.GENERATE_CASES_DRAFT)]),actionItems: [new ActionItem(status: StatusEnum.IN_PROGRESS)],reportName: "report",owner: normalUser,description: "description")
        executedConfiguration.dateCreated = new Date()
        executedConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        executedConfiguration.workflowState.userService = mockUserService.proxyInstance()
        def mockControllerUserService = new MockFor(UserService)
        mockControllerUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockControllerUserService.proxyInstance()
        ExecutedConfiguration.metaClass.static.fetchAllBySearchStringAndStatusInList = { LibraryFilter filter -> new Object(){
                List list(Object o){
                    return [[executedConfiguration.id]]
                }
            }
        }
        ExecutedConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        when:
        params.sort = "version"
        controller.index()
        then:
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
        response.json.aaData[0].size() == 15
    }

    void "test showExecutedICSRReports sort is owner"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportUserState executedReportUserState = new ExecutedReportUserState(user:normalUser,isFavorite: true,isArchived: false )
        executedReportUserState.save(failOnError:true,validate:false,flush:true)
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(workflowState: new WorkflowState(name: "state_1",reportActions: [new WorkflowStateReportAction(executors: [normalUser],reportAction: ReportActionEnum.GENERATE_CASES_DRAFT)]),actionItems: [new ActionItem(status: StatusEnum.IN_PROGRESS)],reportName: "report",owner: normalUser,description: "description")
        executedConfiguration.dateCreated = new Date()
        executedConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        executedConfiguration.workflowState.userService = mockUserService.proxyInstance()
        def mockControllerUserService = new MockFor(UserService)
        mockControllerUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockControllerUserService.proxyInstance()
        ExecutedConfiguration.metaClass.static.fetchAllBySearchStringAndStatusInList = { LibraryFilter filter -> new Object(){
                List list(Object o){
                    return [[executedConfiguration.id]]
                }
            }
        }
        ExecutedConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        when:
        params.max = 10
        params.offset = 0
        params.direction = ""
        params.sort = "owner"
        controller.showExecutedICSRReports()
        then:
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
        response.json.aaData[0].size() == 15
    }

    void "test showExecutedICSRReports sort is version"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportUserState executedReportUserState = new ExecutedReportUserState(user:normalUser,isFavorite: true,isArchived: false )
        executedReportUserState.save(failOnError:true,validate:false,flush:true)
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(workflowState: new WorkflowState(name: "state_1",reportActions: [new WorkflowStateReportAction(executors: [normalUser],reportAction: ReportActionEnum.GENERATE_CASES_DRAFT)]),actionItems: [new ActionItem(status: StatusEnum.IN_PROGRESS)],reportName: "report",owner: normalUser,description: "description")
        executedConfiguration.dateCreated = new Date()
        executedConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        executedConfiguration.workflowState.userService = mockUserService.proxyInstance()
        def mockControllerUserService = new MockFor(UserService)
        mockControllerUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockControllerUserService.proxyInstance()
        ExecutedConfiguration.metaClass.static.fetchAllBySearchStringAndStatusInList = { LibraryFilter filter -> new Object(){
                List list(Object o){
                    return [[executedConfiguration.id]]
                }
            }
        }
        ExecutedConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        when:
        params.max = 10
        params.offset = 0
        params.direction = ""
        params.sort = "version"
        controller.showExecutedICSRReports()
        then:
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
        response.json.aaData[0].size() == 15
    }

    void "test getSharedWithUsers"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedDeliveryOption executedDeliveryOption = new ExecutedDeliveryOption(sharedWith: [normalUser],sharedWithGroup: [new UserGroup()])
        executedDeliveryOption.save(failOnError:true,validate:false,flush:true)
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(executedDeliveryOption: executedDeliveryOption)
        executedConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        params.id = executedConfiguration.id
        controller.getSharedWithUsers()
        then:
        response.json.groups.size() == 1
        response.json.users.size() == 1
    }

    void "test getEmailToUsers"(){
        ExecutedDeliveryOption executedDeliveryOption = new ExecutedDeliveryOption(emailToUsers: ["abc@gmail.com"])
        executedDeliveryOption.save(failOnError:true,validate:false,flush:true)
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(executedDeliveryOption: executedDeliveryOption)
        executedConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        params.id = executedConfiguration.id
        controller.getEmailToUsers()
        then:
        response.json == ["abc@gmail.com"]
    }

    void "test getReportsList"() {
        given:
        User normalUser = makeNormalUser("admin", [])
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration().save(failOnError: true, validate: false, flush: true)
        ExecutedReportConfiguration.metaClass.static.fetchAllByReportName = { User currentUser, String test ->
            new Object() {
                List list(Object o) {
                    return [[executedConfiguration.id]]
                }
            }
        }
        ExecutedReportConfiguration.metaClass.static.countAllByReportName = { User currentUser, String test ->
            new Object() {
                Integer get() {
                    return 1
                }
            }
        }

        when:
        params.max = 30
        params.offset = 0
        controller.getReportsList()
        then:
        response.json.total_count == 1
    }
}
