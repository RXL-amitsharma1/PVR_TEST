package com.rxlogix.api


import com.rxlogix.ExecutionStatusService
import com.rxlogix.LibraryFilter
import com.rxlogix.PeriodicReportService
import com.rxlogix.UserService
import com.rxlogix.UtilService
import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.sql.DataSource

/*
@TestFor(PeriodicReportConfigurationRestController)
@Mock([ExecutedPeriodicReportConfiguration, User, Role, UserRole, Tenant,PeriodicReportConfiguration,ExecutionStatus,ExecutedPeriodicReportConfiguration,ExecutedReportUserState,WorkflowState])
@ConfineMetaClassChanges([ExecutedPeriodicReportConfiguration])
*/

@ConfineMetaClassChanges([User, ReportConfiguration, PeriodicReportConfiguration, ExecutedPeriodicReportConfiguration])
class PeriodicReportConfigurationRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<PeriodicReportConfigurationRestController> {

    def setup() {

    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains ExecutedPeriodicReportConfiguration, User, Role, UserRole, Tenant, PeriodicReportConfiguration, ExecutionStatus, ExecutedPeriodicReportConfiguration, ExecutedReportUserState, WorkflowState
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser(0..2) { -> user }
        securityMock.demand.isCurrentUserDev(0..2) { false }
        return securityMock.proxyInstance()
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
        return adminUser
    }

    void "showExecutedPeriodicReports"() {
        given:
        int max = 100
        int offset = 5
        String direction = 'asc'
        String sort = 'id'
        User adminUser = makeAdminUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        ExecutedReportUserState executedReportUserState = new ExecutedReportUserState(user: adminUser)
        executedReportUserState.save(failOnError:true, validate:false, flush:true)
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(owner: adminUser,
                executedReportUserStates: [executedReportUserState],
                periodicReportType: PeriodicReportTypeEnum.ADDENDUM,
                dueDate: new Date(),
                reportName: "report",
                numOfExecutions: 1,
                reportingDestinations: ["dest"],
                primaryReportingDestination: "destination",
                workflowState: new WorkflowState(name:"state"),
                status: ReportExecutionStatusEnum.BACKLOG,
                actionItems: [new ActionItem()])
        executedPeriodicReportConfiguration.save(failOnError:true, validate:false, flush:true)
        def mockUtilService = new MockFor(UtilService)
        mockUtilService.demand.getReportConnectionForPVR(1) { ->
            return Mock(DataSource)
        }
        controller.utilService = mockUtilService.proxyInstance()
        controller.userService = makeSecurityService(adminUser)

        ExecutedPeriodicReportConfiguration.metaClass.static.fetchAllBySearchStringAndStatusInList = { LibraryFilter filter,String sortBy = null, String sortDirection = "asc" ->
            new Object() {
                List<List> list(Map map) {
                    return [[executedPeriodicReportConfiguration.id]]
                }
            }
        }
        controller.metaClass.getDateRangeString = { Locale locale, Long exConfigId ->
            return "01-Jan-2024 to 31-Jan-2024"
        }
        ExecutedPeriodicReportConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        executedPeriodicReportConfiguration.workflowState.metaClass.getReportActionsAsList = { return [] }

        when:
        controller.invokeMethod('showExecutedPeriodicReports', [new LibraryFilter([search: params.searchString, user: adminUser]), max, offset, direction, sort] as Object[])

        then:
        response.status == 200
        response.json.aaData[0].size() == 33
        response.json.aaData.isAdmin != null
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
    }

    void "test briefProperties"(){
        User adminUser = makeAdminUser()
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName:"report", description: "description", numOfExecutions: 1,owner: adminUser, primaryReportingDestination: "Destination")
        periodicReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        def result = controller.briefProperties([periodicReportConfiguration])
        then:
        result[0].size() == 12
    }

    void "test index"(){
        User adminUser = makeAdminUser()
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName:"report", description: "description", numOfExecutions: 1,owner: adminUser, primaryReportingDestination: "Destination")
        periodicReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> adminUser}
        mockUserService.demand.getCurrentUser(0..1){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        ReportConfiguration.metaClass.static.getAllIdsByFilter = {LibraryFilter filter, Class clazz, boolean showXMLOption = false, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){
                    return [periodicReportConfiguration.id]
                }
            }
        }
        ReportConfiguration.metaClass.static.countRecordsBySearchString = { LibraryFilter filter, boolean showXMLOption = false-> new Object(){
                Integer get(Object o){
                    return 1
                }
            }
        }
        when:
        controller.index()
        then:
        response.json.size() == 3
        response.json.aaData[0].size() == 12
    }

    void "test bulkSchedulingList"(){
        User adminUser = makeAdminUser()
        boolean run = false
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName:"report", description: "description", numOfExecutions: 1,owner: adminUser, primaryReportingDestination: "Destination")
        periodicReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        PeriodicReportConfiguration.metaClass.static.fetchAllIdsForBulkUpdate = {LibraryFilter filter -> new Object(){
                List list(Object o){
                    return [periodicReportConfiguration.id]
                }
            }
        }
        PeriodicReportConfiguration.metaClass.static.countAllForBulkUpdate = { LibraryFilter filter-> new Object(){
                Integer get(Object o){
                    return 1
                }
            }
        }
        def mockPeriodicReportService = new MockFor(PeriodicReportService)
        mockPeriodicReportService.demand.toBulkTableMap(0..1){PeriodicReportConfiguration conf->
            run = true
            return [:]
        }
        controller.periodicReportService = mockPeriodicReportService.proxyInstance()
        when:
        controller.bulkSchedulingList()
        then:
        run == true
        response.json.size() == 3
        response.json.aaData == [[:]]
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
    }

    void "test bulkSchedulingList recordsFilteredCount null"(){
        User adminUser = makeAdminUser()
        boolean run = false
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName:"report", description: "description", numOfExecutions: 1,owner: adminUser, primaryReportingDestination: "Destination")
        periodicReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        PeriodicReportConfiguration.metaClass.static.fetchAllIdsForBulkUpdate = {LibraryFilter filter -> new Object(){
                List list(Object o){
                    return []
                }
            }
        }
        PeriodicReportConfiguration.metaClass.static.countAllForBulkUpdate = { LibraryFilter filter-> new Object(){
                Integer get(Object o){
                    return 0
                }
            }
        }
        def mockPeriodicReportService = new MockFor(PeriodicReportService)
        mockPeriodicReportService.demand.toBulkTableMap(0..1){PeriodicReportConfiguration conf->
            run = true
            return [:]
        }
        controller.periodicReportService = mockPeriodicReportService.proxyInstance()
        when:
        controller.bulkSchedulingList()
        then:
        run == false
        response.json.size() == 3
        response.json.aaData == []
        response.json.recordsFiltered == 0
        response.json.recordsTotal == 0
    }

    void "test reportsList"(){
        int max = 100
        int offset = 5
        String direction = 'asc'
        String sort = 'id'
        User adminUser = makeAdminUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        ExecutedReportUserState executedReportUserState = new ExecutedReportUserState(user: adminUser)
        executedReportUserState.save(failOnError:true,validate:false,flush:true)
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(owner: adminUser,executedReportUserStates: [executedReportUserState],periodicReportType: PeriodicReportTypeEnum.ADDENDUM,dueDate: new Date(),reportName: "report",numOfExecutions: 1,reportingDestinations: ["dest"],primaryReportingDestination: "destination",workflowState: new WorkflowState(name:"state"),status: ReportExecutionStatusEnum.BACKLOG,actionItems: [new ActionItem()])
        executedPeriodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedPeriodicReportConfiguration.metaClass.static.fetchAllBySearchStringAndStatusInList = { LibraryFilter filter,String sortBy = null, String sortDirection = "asc" ->
            new Object() {
                List<List> list(Map map) {
                    return [[executedPeriodicReportConfiguration.id]]
                }
            }
        }
        controller.metaClass.getDateRangeString = { Locale locale, Long exConfigId ->
            return "01-Jan-2024 to 31-Jan-2024"
        }
        ExecutedPeriodicReportConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        executedPeriodicReportConfiguration.workflowState.metaClass.getReportActionsAsList = { return []}

        when:
        params.max = max
        params.offset = offset
        params.sort = sort
        params.direction = direction
        controller.reportsList()
        then:
        response.status == 200
        response.json.aaData[0].size() == 33
        response.json.aaData.isAdmin != null
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
    }

    void "test latestPeriodicReport"(){
        int max = 100
        int offset = 5
        String direction = 'asc'
        String sort = 'id'
        User adminUser = makeAdminUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        ExecutedReportUserState executedReportUserState = new ExecutedReportUserState(user: adminUser)
        executedReportUserState.save(failOnError:true,validate:false,flush:true)
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(owner: adminUser,executedReportUserStates: [executedReportUserState],periodicReportType: PeriodicReportTypeEnum.ADDENDUM,dueDate: new Date(),reportName: "report",numOfExecutions: 1,reportingDestinations: ["dest"],primaryReportingDestination: "destination",workflowState: new WorkflowState(name:"state"),status: ReportExecutionStatusEnum.BACKLOG,actionItems: [new ActionItem()])
        executedPeriodicReportConfiguration.save(failOnError:true,validate:false,flush:true)

        ExecutedPeriodicReportConfiguration.metaClass.static.fetchAllBySearchStringAndStatusInList = { LibraryFilter filter,String sortBy = null, String sortDirection = "asc" ->
            new Object() {
                List<List> list(Map map) {
                    return [[executedPeriodicReportConfiguration.id]]
                }
            }
        }
        controller.metaClass.getDateRangeString = { Locale locale, Long exConfigId ->
            return "01-Jan-2024 to 31-Jan-2024"
        }
        executedPeriodicReportConfiguration.workflowState.metaClass.getReportActionsAsList = { return []}
        when:
        params.max = max
        params.offset = offset
        params.sort = sort
        params.direction = direction
        controller.latestPeriodicReport()
        then:
        response.status == 200
        response.json.aaData[0].size() == 33
        response.json.aaData.isAdmin != null
    }

    void "test getIndicator "(){
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(dueDate: date,reportSubmissions: [new ReportSubmission(reportSubmissionStatus: ReportEnum)])
        executedPeriodicReportConfiguration.save(failOnError:true , validate:false)
        when:
        result = controller.getIndicator(executedPeriodicReportConfiguration)
        then:
        result.class == String
        where:
              date        |                   ReportEnum                             |       result
            new Date()    |     ReportSubmissionStatusEnum.SUBMITTED                 |         ""
            new Date()    |     ReportSubmissionStatusEnum.SUBMISSION_NOT_REQUIRED   |         ""
            new Date()    |     ReportSubmissionStatusEnum.PENDING                   |         ""
         new Date()+10    |     ReportSubmissionStatusEnum.PENDING                   |       "yellow"
         new Date()+10    |     ReportSubmissionStatusEnum.SUBMISSION_NOT_REQUIRED   |       "yellow"
         new Date()+10    |     ReportSubmissionStatusEnum.SUBMITTED                 |       "yellow"
         new Date()+30    |     ReportSubmissionStatusEnum.PENDING                   |         ""
         new Date()+30    |     ReportSubmissionStatusEnum.SUBMISSION_NOT_REQUIRED   |         ""
         new Date()+30    |     ReportSubmissionStatusEnum.SUBMITTED                 |         ""
         new Date()-10    |     ReportSubmissionStatusEnum.PENDING                   |         "red"
         new Date()-10    |     ReportSubmissionStatusEnum.SUBMISSION_NOT_REQUIRED   |         ""
         new Date()-10    |     ReportSubmissionStatusEnum.SUBMITTED                 |         ""
    }

    void "test getIndicator yellow"(){
        Date now = new Date();
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(dueDate: now+2,)
        executedPeriodicReportConfiguration.save(failOnError:true , validate:false)
        when:
        def result = controller.getIndicator(executedPeriodicReportConfiguration)
        then:
        result == "yellow"
    }

    void "test getIndicator red"(){
        Date now = new Date();
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(dueDate: now-2,reportSubmissions: [new ReportSubmission(reportSubmissionStatus: ReportSubmissionStatusEnum.PENDING)])
        executedPeriodicReportConfiguration.save(failOnError:true , validate:false)
        when:
        def result = controller.getIndicator(executedPeriodicReportConfiguration)
        then:
        result == "red"
    }

    void "test getIndicator null"(){
        Date now = new Date();
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(dueDate: now-2,reportSubmissions: [new ReportSubmission()])
        executedPeriodicReportConfiguration.save(failOnError:true , validate:false)
        when:
        def result = controller.getIndicator(executedPeriodicReportConfiguration)
        then:
        result == ""
    }

    void "test generateDraft"(){
        User adminUser = makeAdminUser()
        boolean run = false
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true,numOfExecutions: 1,owner: adminUser,reportName: "report",tenantId: 1,executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.DOCX],sharedWith:[adminUser] ))
        executedPeriodicReportConfiguration.save(failOnError:true , validate:false)
        def mockExecutionStatusService = new MockFor(ExecutionStatusService)
        mockExecutionStatusService.demand.generateDraft(0..1){ ExecutedReportConfiguration executedPeriodicReportConfigurationInstance, ReportActionEnum reportAction = com.rxlogix.enums.ReportActionEnum.GENERATE_DRAFT ->
            run = true
        }
        controller.executionStatusService = mockExecutionStatusService.proxyInstance()
        when:
        params.reportAction = "GENERATE_CASES_DRAFT"
        controller.generateDraft(executedPeriodicReportConfiguration)
        then:
        response.json.success == true
        response.json.message == 'app.Configuration.RunningMessage'
        run == true
    }

    void "test generateDraft null instance"(){
        when:
        controller.generateDraft(null)
        then:
        response.status == 400
        response.json.error == 'Not found'
    }

    void "test generateDraft hasGeneratedCasesData false"(){
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedPeriodicReportConfiguration.save(failOnError:true , validate:false)
        when:
        controller.generateDraft(executedPeriodicReportConfiguration)
        then:
        response.status == 400
        response.json.error == 'Not found'
    }

    void "test generateDraft already running"(){
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(hasGeneratedCasesData: true)
        executedPeriodicReportConfiguration.save(failOnError:true , validate:false)
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: executedPeriodicReportConfiguration.id,entityType: ExecutingEntityTypeEnum.EXECUTED_PERIODIC_CONFIGURATION,executionStatus: ReportExecutionStatusEnum.GENERATING)
        executionStatus.save(failOnError:true , validate:false)
        when:
        controller.generateDraft(executedPeriodicReportConfiguration)
        then:
        response.json.warning =="app.periodicReportConfiguration.draft.already.executing"
    }
}