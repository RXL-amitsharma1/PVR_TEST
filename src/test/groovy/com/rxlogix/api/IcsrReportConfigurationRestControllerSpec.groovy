package com.rxlogix.api

import com.rxlogix.ExecutionStatusService
import com.rxlogix.LibraryFilter
import com.rxlogix.PeriodicReportService
import com.rxlogix.UserService
import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.config.ExecutedIcsrReportConfiguration
import com.rxlogix.config.ExecutedIcsrTemplateQuery
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.IcsrReportConfiguration
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.config.ReportConfiguration
import com.rxlogix.config.ReportSubmission
import com.rxlogix.config.Tag
import com.rxlogix.config.Tenant
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.enums.ReportActionEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ReportSubmissionStatusEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([PeriodicReportConfiguration, ExecutedIcsrReportConfiguration, ExecutedIcsrTemplateQuery, ExecutedIcsrProfileConfiguration])
class IcsrReportConfigurationRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<IcsrReportConfigurationRestController> {

    def setupSpec() {
        mockDomain User
        mockDomain Role
        mockDomain UserRole
        mockDomain UserGroup
        mockDomain UserGroupUser
        mockDomain Tenant
        mockDomain ExecutedIcsrReportConfiguration
        mockDomain IcsrReportConfiguration
        mockDomain ReportConfiguration
        mockDomain PeriodicReportConfiguration
        mockDomain ExecutionStatus
    }

    def setup() {
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

    void "test briefProperties"() {
        User normalUser = makeNormalUser("user",[])
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration(reportName: "report",description: "description",numOfExecutions: 1,tags: [new Tag()],owner: normalUser,primaryReportingDestination: "destination")
        executedIcsrReportConfiguration.save(failOnError:true , validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        def list = controller.briefProperties([executedIcsrReportConfiguration])
        then:
        list.size() == 1
        list[0].size() == 11
    }

    void "test index"(){
        User normalUser = makeNormalUser("user",[])
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(reportName: "report",description: "description",numOfExecutions: 1,tags: [new Tag()],owner: normalUser,primaryReportingDestination: "destination")
        icsrReportConfiguration.save(failOnError:true , validate:false)
        ReportConfiguration.metaClass.static.getAllIdsByFilter = {LibraryFilter filter, Class clazz, boolean showXMLOption = false, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){
                    return [icsrReportConfiguration.id]
                }
            }
        }
        ReportConfiguration.metaClass.static.countRecordsBySearchString = {LibraryFilter filter, boolean showXMLOption = false -> new Object(){
                Integer get(){
                    return 1
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.index()
        then:
        response.json.size() == 3
    }

    void "test bulkSchedulingList"(){
        User normalUser = makeNormalUser("user",[])
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration()
        periodicReportConfiguration.save(failOnError:true , validate:false)
        PeriodicReportConfiguration.metaClass.static.fetchAllIdsForBulkUpdate = {LibraryFilter filter -> new Object(){
                List list(Object o){
                    return [periodicReportConfiguration.id]
                }
            }
        }
        PeriodicReportConfiguration.metaClass.static.countAllForBulkUpdate = {LibraryFilter filter -> new Object(){
                Integer get(){
                    return 1
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockPeriodicReportService = new MockFor(PeriodicReportService)
        mockPeriodicReportService.demand.toBulkTableMap(0..1){PeriodicReportConfiguration conf -> [:]}
        controller.periodicReportService = mockPeriodicReportService.proxyInstance()
        when:
        controller.bulkSchedulingList()
        then:
        response.json.size() == 3
    }

    void "test getIndicator yellow"(){
        Date now = new Date();
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration(dueDate: now+2,)
        executedIcsrReportConfiguration.save(failOnError:true , validate:false)
        when:
        def result = controller.getIndicator(executedIcsrReportConfiguration)
        then:
        result == "yellow"
    }

    void "test getIndicator red"(){
        Date now = new Date();
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration(dueDate: now-2,reportSubmissions: [new ReportSubmission(reportSubmissionStatus: ReportSubmissionStatusEnum.PENDING)])
        executedIcsrReportConfiguration.save(failOnError:true , validate:false)
        when:
        def result = controller.getIndicator(executedIcsrReportConfiguration)
        then:
        result == "red"
    }

    void "test getIndicator null"(){
        Date now = new Date();
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration(dueDate: now-2,reportSubmissions: [new ReportSubmission()])
        executedIcsrReportConfiguration.save(failOnError:true , validate:false)
        when:
        def result = controller.getIndicator(executedIcsrReportConfiguration)
        then:
        result == ""
    }

    void "test showExecutedPeriodicReports"(){
        User normalUser = makeNormalUser("user",[])
        Date now = new Date()
        LibraryFilter libraryFilter = new LibraryFilter(user: normalUser)
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration(reportName: "report",description: "description",numOfExecutions: 1,tags: [new Tag()],owner: normalUser,primaryReportingDestination: "destination",periodicReportType: PeriodicReportTypeEnum.ACO,dueDate: now+2)
        executedIcsrReportConfiguration.dateCreated = now
        executedIcsrReportConfiguration.lastUpdated = now
        executedIcsrReportConfiguration.save(failOnError:true , validate:false)
        ExecutedIcsrReportConfiguration.metaClass.static.fetchAllBySearchStringAndStatusInList = {LibraryFilter filter -> new Object(){
                List list(Object o){
                    return [[executedIcsrReportConfiguration.id]]
                }
            }
        }
        controller.metaClass.getDateRangeString = { Locale locale, Long exConfigId ->
            return "01-Jan-2024 to 31-Jan-2024"
        }
        ExecutedIcsrReportConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('showExecutedPeriodicReports', [libraryFilter,10,0 ,"",""] as Object[])
        then:
        response.json.size() == 3
    }

    void "test reportsList"(){
        User normalUser = makeNormalUser("user",[])
        Date now = new Date()
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration(reportName: "report",description: "description",numOfExecutions: 1,tags: [new Tag()],owner: normalUser,primaryReportingDestination: "destination",periodicReportType: PeriodicReportTypeEnum.ACO,dueDate: now+2)
        executedIcsrReportConfiguration.dateCreated = now
        executedIcsrReportConfiguration.lastUpdated = now
        executedIcsrReportConfiguration.save(failOnError:true , validate:false)
        ExecutedIcsrReportConfiguration.metaClass.static.fetchAllBySearchStringAndStatusInList = {LibraryFilter filter -> new Object(){
                List list(Object o){
                    return [[executedIcsrReportConfiguration.id]]
                }
            }
        }
        controller.metaClass.getDateRangeString = { Locale locale, Long exConfigId ->
            return "01-Jan-2024 to 31-Jan-2024"
        }
        ExecutedIcsrReportConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.reportsList()
        then:
        response.json.size() == 3
    }

    void "test latestPeriodicReport"(){
        User normalUser = makeNormalUser("user",[])
        Date now = new Date()
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration(reportName: "report",description: "description",numOfExecutions: 1,tags: [new Tag()],owner: normalUser,primaryReportingDestination: "destination",periodicReportType: PeriodicReportTypeEnum.ACO,dueDate: now+2)
        executedIcsrReportConfiguration.dateCreated = now
        executedIcsrReportConfiguration.lastUpdated = now
        executedIcsrReportConfiguration.save(failOnError:true , validate:false)
        ExecutedIcsrReportConfiguration.metaClass.static.fetchAllBySearchStringAndStatusInList = { LibraryFilter filter ->
            new Object() {
                List list(Object o) {
                    return [[executedIcsrReportConfiguration.id]]
                }
            }
        }
        controller.metaClass.getDateRangeString = { Locale locale, Long exConfigId ->
            return "01-Jan-2024 to 31-Jan-2024"
        }
        ExecutedIcsrReportConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.latestPeriodicReport()
        then:
        response.json.size() == 3
    }

    void "test generateDraft"(){
        boolean run = false
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration(hasGeneratedCasesData: true)
        executedIcsrReportConfiguration.save(failOnError:true , validate:false)
        def mockExecutionStatusService = new MockFor(ExecutionStatusService)
        mockExecutionStatusService.demand.generateDraft(0..1){ ExecutedReportConfiguration executedPeriodicReportConfiguration, ReportActionEnum reportAction = com.rxlogix.enums.ReportActionEnum.GENERATE_DRAFT ->
            run = true
        }
        controller.executionStatusService = mockExecutionStatusService.proxyInstance()
        when:
        controller.generateDraft(executedIcsrReportConfiguration)
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
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration()
        executedIcsrReportConfiguration.save(failOnError:true , validate:false)
        when:
        controller.generateDraft(executedIcsrReportConfiguration)
        then:
        response.status == 400
        response.json.error == 'Not found'
    }

    void "test generateDraft already running"(){
        ExecutedIcsrReportConfiguration executedIcsrReportConfiguration = new ExecutedIcsrReportConfiguration(hasGeneratedCasesData: true)
        executedIcsrReportConfiguration.save(failOnError:true , validate:false)
        ExecutionStatus executionStatus = new ExecutionStatus(entityId: executedIcsrReportConfiguration.id,entityType: ExecutingEntityTypeEnum.EXECUTED_CONFIGURATION,executionStatus: ReportExecutionStatusEnum.GENERATING)
        executionStatus.save(failOnError:true , validate:false)
        when:
        controller.generateDraft(executedIcsrReportConfiguration)
        then:
        response.json.warning == "app.periodicReportConfiguration.draft.already.executing"
    }
}
