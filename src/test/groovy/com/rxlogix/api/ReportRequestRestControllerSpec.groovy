package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.ReportRequestService
import com.rxlogix.ReportsJsonUtil
import com.rxlogix.UserService
import com.rxlogix.config.ActionItem
import com.rxlogix.config.ActionItemCategory
import com.rxlogix.config.DrilldownCLLData
import com.rxlogix.config.ReportRequest
import com.rxlogix.config.ReportRequestField
import com.rxlogix.config.ReportRequestPriority
import com.rxlogix.config.ReportRequestType
import com.rxlogix.config.Tenant
import com.rxlogix.config.WorkflowState
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

@ConfineMetaClassChanges([User, ReportsJsonUtil, ReportRequest])
class ReportRequestRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportRequestRestController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant,Preference,ReportRequest, ReportRequestField
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

    void "test reportRequestDropdownList"(){
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest(reportName: "report",id: 1)
        reportRequest.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ReportRequest.metaClass.static.fetchByTerm = { User user, String term -> new Object(){
                List list(Object o){
                    return [[reportRequest]]
                }
                int count(){
                    return 1
                }
            }
        }
        when:
        controller.reportRequestDropdownList()
        then:
        response.json.total_count == 1
        response.json.items.size() == 1
    }

    void "test index sort is assignedTo"(){
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest(reportName: "report",assignedTo: normalUser,description: "description",requesters: [normalUser],requesterGroups: [new UserGroup()],dueDate: new Date(),priority: new ReportRequestPriority(name: "priority"),workflowState: new WorkflowState(name: "status"),createdBy: "user",reportRequestType: new ReportRequestType(name: "reportRequestType"),productSelection: "selection",eventSelection: "selection")
        reportRequest.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ReportRequest.metaClass.static.fetchByFilter = { LibraryFilter filter, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){
                    return [[reportRequest.id]]
                }
            }
        }
        ReportRequest.metaClass.static.countByFilter = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        ReportsJsonUtil.metaClass.static.getNameFieldFromSelectionJson = {String jsonString -> "selection"}
        ReportRequest.metaClass.static.getAll = { List<Long> ids ->
            [reportRequest]
        }
        def mockReportRequestService = Mock(ReportRequestService)
        mockReportRequestService.getReportEndDate(_) >> {return new Date()}
        reportRequest.reportRequestService = mockReportRequestService
        when:
        params.sort = "assignedTo"
        params.max = 10
        params.offset = 0
        params.direction = ""
        controller.index()
        then:
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
        response.json.aaData[0].size() == 26
    }

    void "test index sort is requestName"(){
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest(reportName: "report",assignedTo: normalUser,description: "description",requesters: [normalUser],requesterGroups: [new UserGroup()],dueDate: new Date(),priority: new ReportRequestPriority(name: "priority"),workflowState: new WorkflowState(name: "status"),createdBy: "user",reportRequestType: new ReportRequestType(name: "reportRequestType"),productSelection: "selection",eventSelection: "selection")
        reportRequest.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ReportRequest.metaClass.static.fetchByFilter = { LibraryFilter filter, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){
                    return [[reportRequest.id]]
                }
            }
        }
        ReportsJsonUtil.metaClass.static.getNameFieldFromSelectionJson = {String jsonString -> "selection"}
        when:
        params.sort = "requestName"
        params.max = 10
        params.offset = 0
        params.direction = ""
        controller.index()
        then:
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
        response.json.aaData[0].size() == 26
    }

    void "test index sort is reportRequestId"(){
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest(reportName: "report",assignedTo: normalUser,description: "description",requesters: [normalUser],requesterGroups: [new UserGroup()],dueDate: new Date(),priority: new ReportRequestPriority(name: "priority"),workflowState: new WorkflowState(name: "status"),createdBy: "user",reportRequestType: new ReportRequestType(name: "reportRequestType"),productSelection: "selection",eventSelection: "selection")
        reportRequest.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ReportRequest.metaClass.static.fetchByFilter = { LibraryFilter filter, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){
                    return [[reportRequest.id]]
                }
            }
        }
        ReportsJsonUtil.metaClass.static.getNameFieldFromSelectionJson = {String jsonString -> "selection"}
        when:
        params.sort = "reportRequestId"
        params.max = 10
        params.offset = 0
        params.direction = ""
        controller.index()
        then:
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
        response.json.aaData[0].size() == 26
    }

    void "test widgetSearch sort is reportRequestId"(){
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest(reportName: "report",assignedTo: normalUser,description: "description",requesters: [normalUser],requesterGroups: [new UserGroup()],dueDate: new Date(),priority: new ReportRequestPriority(name: "priority"),workflowState: new WorkflowState(name: "status"),createdBy: "user",reportRequestType: new ReportRequestType(name: "reportRequestType"),productSelection: "selection",eventSelection: "selection")
        reportRequest.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ReportRequest.metaClass.static.fetchByWidgetFilter = { widgetFilter, User user-> new Object(){
                List list(Object o){
                    return [[reportRequest.id]]
                }
            }
        }
        ReportRequest.metaClass.static.countByWidgetFilter = { widgetFilter, User user->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        ReportsJsonUtil.metaClass.static.getNameFieldFromSelectionJson = {String jsonString -> "selection"}
        when:
        params.sort = "reportRequestId"
        params.max = 10
        params.offset = 0
        params.direction = ""
        controller.widgetSearch()
        then:
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
        response.json.aaData[0].size() == 26
    }

    void "test widgetSearch sort is requestName"(){
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest(reportName: "report",assignedTo: normalUser,description: "description",requesters: [normalUser],requesterGroups: [new UserGroup()],dueDate: new Date(),priority: new ReportRequestPriority(name: "priority"),workflowState: new WorkflowState(name: "status"),createdBy: "user",reportRequestType: new ReportRequestType(name: "reportRequestType"),productSelection: "selection",eventSelection: "selection")
        reportRequest.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ReportRequest.metaClass.static.fetchByWidgetFilter = { widgetFilter, User user ->
            new Object() {
                List list(Object o) {
                    return [[reportRequest.id]]
                }
            }
        }
        ReportRequest.metaClass.static.countByWidgetFilter = { widgetFilter, User user ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        ReportsJsonUtil.metaClass.static.getNameFieldFromSelectionJson = {String jsonString -> "selection"}
        when:
        params.sort = "requestName"
        params.max = 10
        params.offset = 0
        params.direction = ""
        controller.widgetSearch()
        then:
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
        response.json.aaData[0].size() == 26
    }

    void "test widgetSearch sort is assignedTo"(){
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest(reportName: "report",assignedTo: normalUser,description: "description",requesters: [normalUser],requesterGroups: [new UserGroup()],dueDate: new Date(),priority: new ReportRequestPriority(name: "priority"),workflowState: new WorkflowState(name: "status"),createdBy: "user",reportRequestType: new ReportRequestType(name: "reportRequestType"),productSelection: "selection",eventSelection: "selection")
        reportRequest.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ReportRequest.metaClass.static.fetchByWidgetFilter = { widgetFilter, User user ->
            new Object() {
                List list(Object o) {
                    return [[reportRequest.id]]
                }
            }
        }
        ReportRequest.metaClass.static.countByWidgetFilter = { widgetFilter, User user ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        ReportsJsonUtil.metaClass.static.getNameFieldFromSelectionJson = {String jsonString -> "selection"}
        when:
        params.sort = "assignedTo"
        params.max = 10
        params.offset = 0
        params.direction = ""
        controller.widgetSearch()
        then:
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
        response.json.aaData[0].size() == 26
    }
}
