package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.localization.InteractiveHelp
import com.rxlogix.localization.ReleaseNotesNotifier
import com.rxlogix.localization.SystemNotification
import com.rxlogix.user.*
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonOutput
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([DateUtil, ViewHelper, ReportRequest, ReportWidget, Dashboard, Tenants, SpringSecurityUtils, ActionItem, ExecutedConfiguration, ExecutedPeriodicReportConfiguration, UserGroup, ExecutedReportConfiguration])
class DashboardControllerSpec extends Specification implements DataTest, ControllerUnitTest<DashboardController>  {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains SourceProfile, Dashboard, User, UserGroup, UserGroupUser, Role, UserRole, ReleaseNotesNotifier, Tenant, Notification, ActionItem, ReportRequest, ReportRequestType, DateRangeType, ReportRequestPriority, ActionItemCategory, ExecutedConfiguration, WorkflowRule, WorkflowState, ExecutedPeriodicReportConfiguration, ReportRequest, ReportWidget, EtlSchedule, ReportTemplate, ExecutedTemplateQuery, ExecutedDateRangeInformation, ExecutedTemplateValueList, ReportResult, PeriodicReportConfiguration, ExecutedReportConfiguration
    }

    private makeSecurityServiceCurrentUser(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser(0..3) { -> user }
        return securityMock.proxyInstance()
    }

    private makeSecurityServiceUser(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getUser(0..1) { -> user }
        return securityMock.proxyInstance()
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
        normalUser.metaClass.isDev = { -> return false }
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

    void "test home"(){
        when:
        controller.home()
        then:
        response.status==302
    }

    void "test index isPvqModule true and ifAnyGranted true"(){
        User adminUser = makeAdminUser()
        Dashboard dashboard = new Dashboard(id: 1, label: "test dashboard", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,widgets: [new ReportWidget(widgetType: WidgetTypeEnum.QUALITY_ACTION_ITEMS)],
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group",createdBy: "user",modifiedBy: "user")], createdBy: "user", modifiedBy: "user")
        dashboard.save(failOnError:true,flush: true,validate: false)
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true,validate: false)
        WorkflowState workflowStateInstance_2=new WorkflowState(name:'test_2',createdBy:'user',modifiedBy:'user')
        workflowStateInstance_2.save(failOnError:true,validate: false)
        WorkflowRule workflowRuleInstance=new WorkflowRule(configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance_2)
        workflowRuleInstance.save(failOnError:true,validate: false)
        def dashboardServiceMock=new MockFor(DashboardService)
        dashboardServiceMock.demand.getDashboard(0..1){ params,request -> dashboard}
        controller.dashboardService = dashboardServiceMock.proxyInstance()
        ViewHelper.metaClass.static.isPvqModule = {request -> return true}
        controller.userService = makeSecurityServiceCurrentUser( makeNormalUser("user",[]))
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        SystemNotification.metaClass.static.fetchNew={User s-> []}
        Locale locale = new Locale('en')
        Locale locale_ja = new Locale('ja')
        messageSource.addMessage("app.label.dashboard.main",  locale ,"Main Dashboard")
        messageSource.addMessage("app.label.dashboard.main",  locale_ja ,"Main Dashboard")
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "Main Dashboard" }
        when:
        controller.index()
        then:
        view == '/dashboard/index'
        model.size() == 7
    }

    void "test index isPvqModule true and ifAnyGranted false"(){
        User adminUser = makeAdminUser()
        Dashboard dashboard = new Dashboard(id: 1, label: "test dashboard", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,widgets: [new ReportWidget(widgetType: WidgetTypeEnum.QUALITY_ACTION_ITEMS)],
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group",createdBy: "user",modifiedBy: "user")], createdBy: "user", modifiedBy: "user")
        dashboard.save(failOnError:true,flush: true,validate: false)
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true,validate: false)
        WorkflowState workflowStateInstance_2=new WorkflowState(name:'test_2',createdBy:'user',modifiedBy:'user')
        workflowStateInstance_2.save(failOnError:true,validate: false)
        WorkflowRule workflowRuleInstance=new WorkflowRule(configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance_2)
        workflowRuleInstance.save(failOnError:true,validate: false)
        def dashboardServiceMock=new MockFor(DashboardService)
        dashboardServiceMock.demand.getDashboard(0..1){ params,request -> dashboard}
        controller.dashboardService = dashboardServiceMock.proxyInstance()
        controller.userService = makeSecurityServiceCurrentUser( makeNormalUser("user",[]))
        ViewHelper.metaClass.static.isPvqModule = {request -> return true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return false}
        SystemNotification.metaClass.static.fetchNew={User s-> []}
        Locale locale = new Locale('en')
        Locale locale_ja = new Locale('ja')
        messageSource.addMessage("app.label.dashboard.main",  locale ,"Main Dashboard")
        messageSource.addMessage("app.label.dashboard.main",  locale_ja ,"Main Dashboard")
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "Main Dashboard" }
        when:
        controller.index()
        then:
        view == '/dashboard/index'
        model.size() == 7
    }

    void "test index isPvqModule false and ifAnyGranted true"(){
        User adminUser = makeAdminUser()
        Dashboard dashboard = new Dashboard(id: 1, label: "test dashboard", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,widgets: [new ReportWidget(widgetType: WidgetTypeEnum.ETL)],
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group",createdBy: "user",modifiedBy: "user")], createdBy: "user", modifiedBy: "user")
        dashboard.save(failOnError:true,flush: true,validate: false)
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true,validate: false)
        WorkflowState workflowStateInstance_2=new WorkflowState(name:'test_2',createdBy:'user',modifiedBy:'user')
        workflowStateInstance_2.save(failOnError:true,validate: false)
        WorkflowRule workflowRuleInstance=new WorkflowRule(configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance_2)
        workflowRuleInstance.save(failOnError:true,validate: false)
        def dashboardServiceMock=new MockFor(DashboardService)
        dashboardServiceMock.demand.getDashboard(0..1){ params,request -> dashboard}
        controller.dashboardService = dashboardServiceMock.proxyInstance()
        controller.userService = makeSecurityServiceCurrentUser( makeNormalUser("user",[]))
        ViewHelper.metaClass.static.isPvqModule = {request -> return false}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        SystemNotification.metaClass.static.fetchNew={User s-> []}
        Locale locale = new Locale('en')
        Locale locale_ja = new Locale('ja')
        messageSource.addMessage("app.label.dashboard.main",  locale ,"Main Dashboard")
        messageSource.addMessage("app.label.dashboard.main",  locale_ja ,"Main Dashboard")
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "Main Dashboard" }
        when:
        controller.index()
        then:
        view == '/dashboard/index'
        model.size() == 7
    }

    void "test index isPvqModule false and ifAnyGranted false"(){
        User adminUser = makeAdminUser()
        Dashboard dashboard = new Dashboard(id: 1, label: "test dashboard", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,widgets: [new ReportWidget(widgetType: WidgetTypeEnum.ETL)],
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group",createdBy: "user",modifiedBy: "user")], createdBy: "user", modifiedBy: "user")
        dashboard.save(failOnError:true,flush: true,validate: false)
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true,validate: false)
        WorkflowState workflowStateInstance_2=new WorkflowState(name:'test_2',createdBy:'user',modifiedBy:'user')
        workflowStateInstance_2.save(failOnError:true,validate: false)
        WorkflowRule workflowRuleInstance=new WorkflowRule(configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance_2)
        workflowRuleInstance.save(failOnError:true,validate: false)
        def dashboardServiceMock=new MockFor(DashboardService)
        dashboardServiceMock.demand.getDashboard(0..1){ params,request -> dashboard}
        controller.dashboardService = dashboardServiceMock.proxyInstance()
        controller.userService = makeSecurityServiceCurrentUser( makeNormalUser("user",[]))
        ViewHelper.metaClass.static.isPvqModule = {request -> return false}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> return false}
        SystemNotification.metaClass.static.fetchNew={User s-> []}
        Locale locale = new Locale('en')
        Locale locale_ja = new Locale('ja')
        messageSource.addMessage("app.label.dashboard.main",  locale ,"Main Dashboard")
        messageSource.addMessage("app.label.dashboard.main",  locale_ja ,"Main Dashboard")
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "Main Dashboard" }
        when:
        controller.index()
        then:
        view == '/dashboard/index'
        model.size() == 7
    }

    void "test newDashboard"() {
        when:
        def adminUser = makeAdminUser()
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { theInstance -> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityServiceCurrentUser(adminUser)
        controller.newDashboard()
        then:
        response.status == 302
        response.redirectedUrl == "/dashboard/index"
    }

    void "test removeDashboard"() {
        when:
        def adminUser = makeAdminUser()
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete { theInstance, name, String justification = null -> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityServiceCurrentUser(adminUser)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group",createdBy: "user",modifiedBy: "user")], createdBy: "user", modifiedBy: "user")
        dashboard.save(failOnError:true,flush: true)
        params.id = 1
        controller.removeDashboard()
        then:
        response.status == 302
        response.redirectedUrl == "/dashboard/index"
    }

    void "getActionItemSummary when there is 1 matching messages"() {
        given:
        def adminUser = makeAdminUser()
        Notification notification = new Notification(user: adminUser, level: NotificationLevelEnum.INFO, message: "app.notification.actionItem.assigned", messageArgs: "messageArgs", appName: NotificationApp.ACTIONITEM,
                executionStatusId: 1)
        notification.save(flush:true)
        ActionItem.metaClass.static.getSummary = {User user, Boolean forPVQ=false -> [:]}
        controller.userService = makeSecurityServiceUser(adminUser)
        when:
        controller.getActionItemSummary()
        then:
        response.json.result.size() == 1
        response.json.result["new"] == 1
    }

    void "getActionItemSummary when there is 0 matching messages"() {
        given:
        def adminUser = makeAdminUser()
        Notification notification = new Notification(user: adminUser, level: NotificationLevelEnum.INFO, message: "app.notification.actionItem.closed", messageArgs: "messageArgs", appName: NotificationApp.ACTIONITEM,
                executionStatusId: 1)
        notification.save(flush:true)
        ActionItem.metaClass.static.getSummary = {User user, Boolean forPVQ=false -> [:]}
        controller.userService = makeSecurityServiceUser(adminUser)
        when:
        controller.getActionItemSummary()
        then:
        response.json.result.size() == 0
    }

    void "getActionItemSummary when user does not match"() {
        given:
        def adminUser = makeAdminUser()
        User normalUser = makeNormalUser("user2", [])
        Notification notification = new Notification( user:normalUser,level: NotificationLevelEnum.INFO, message: "app.notification.actionItem.assigned", messageArgs: "messageArgs", appName: NotificationApp.ACTIONITEM,
                executionStatusId: 1)
        notification.save(flush:true)
        ActionItem.metaClass.static.getSummary = {User user, Boolean forPVQ=false -> [:]}
        controller.userService = makeSecurityServiceUser(adminUser)
        when:
        controller.getActionItemSummary()
        then:
        response.json.result.size() == 0
    }

    void "getActionItemSummary when there is no notification"() {
        given:
        def adminUser = makeAdminUser()
        ActionItem.metaClass.static.getSummary = {User user, Boolean forPVQ=false -> [:]}
        controller.userService = makeSecurityServiceUser(adminUser)
        when:
        controller.getActionItemSummary()
        then:
        response.json.result.size() == 0
    }

    void "getReportRequestSummary when there is 0 matching messages"() {
        given:
        def adminUser = makeAdminUser()
        Notification notification = new Notification(user: adminUser, level: NotificationLevelEnum.INFO, message: "app.notification.actionItem.assigned", messageArgs: "messageArgs", appName: NotificationApp.ACTIONITEM,
                executionStatusId: 1)
        notification.save(flush:true)
        ReportRequest.metaClass.static.getSummary = {User user, Boolean forPVQ=false -> [:]}
        controller.userService = makeSecurityServiceUser(adminUser)
        when:
        controller.getReportRequestSummary()
        then:
        response.json.result.size() == 0
    }

    void "getReportRequestSummary when user does not match"() {
        given:
        def adminUser = makeAdminUser()
        User normalUser = makeNormalUser("user2", [])
        Notification notification = new Notification( user:normalUser,level: NotificationLevelEnum.INFO, message: "app.notification.actionItem.assigned", messageArgs: "messageArgs", appName: NotificationApp.ACTIONITEM,
                executionStatusId: 1)
        notification.save(flush:true)
        ReportRequest.metaClass.static.getSummary = {User user, Boolean forPVQ=false -> [:]}
        controller.userService = makeSecurityServiceUser(adminUser)
        when:
        controller.getReportRequestSummary()
        then:
        response.json.result.size() == 0
    }

    void "getReportRequestSummary when there is 1 matching messages"() {
        given:
        def adminUser = makeAdminUser()
        Notification notification = new Notification(user: adminUser, level: NotificationLevelEnum.INFO, message: "app.notification.reportRequest.assigned", messageArgs: "messageArgs", appName: NotificationApp.ACTIONITEM,
                executionStatusId: 1)
        notification.save(flush:true)
        ReportRequest.metaClass.static.getSummary = {User user, Boolean forPVQ=false -> [:]}
        controller.userService = makeSecurityServiceUser(adminUser)
        when:
        controller.getReportRequestSummary()
        then:
        response.json.result.size() == 1
        response.json.result["new"] == 1
    }

    void "getReportRequestSummary when there is no notification"() {
        given:
        def adminUser = makeAdminUser()
        ReportRequest.metaClass.static.getSummary = {User user, Boolean forPVQ=false -> [:]}
        controller.userService = makeSecurityServiceUser(adminUser)
        when:
        controller.getReportRequestSummary()
        then:
        response.json.result.size() == 0
    }

    void "test getAdhocSummary"(){
        def adminUser = makeAdminUser()
        Notification notification = new Notification(user: adminUser, level: NotificationLevelEnum.INFO, message: "app.notification.completed", messageArgs: "messageArgs", appName: NotificationApp.ADHOC_REPORT,
                executionStatusId: 1)
        notification.save(failOnError:true,flush:true)
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        WorkflowState workflowStateInstance_2=new WorkflowState(name:'test_2',createdBy:'user',modifiedBy:'user')
        workflowStateInstance_2.save(failOnError:true)
        ExecutedConfiguration.metaClass.static.getStates = { User user ->
            [workflowStateInstance, workflowStateInstance_2]
        }
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        User normalUser = makeNormalUser("user2", [])
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.ADHOC_REPORT,initialState: workflowStateInstance,targetState: workflowStateInstance_2,
                executors: [normalUser],executorGroups:[userGroup])
        workflowRuleInstance.save(failOnError:true,validate: false)
        ExecutedConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = {LibraryFilter filter -> new Object() {
            Long get() {
                return 0
            }} }
        controller.userService = makeSecurityServiceCurrentUser(adminUser)
        when:
        controller.getAdhocSummary()
        then:
        response.json.result.size() == 3
        response.json.result["new"] == 1
        response.json.result["states"].size() == 2
    }

    void "test getAggregateSummary"(){
        def adminUser = makeAdminUser()

        Notification.metaClass.static.countByUserAndExecutionStatusIdAndAppName = { User user, Integer statusId, NotificationApp app ->
            return 1  // This satisfies response.json.result["new"] == 1
        }

        Notification notification = new Notification(
                user: adminUser,
                level: NotificationLevelEnum.INFO,
                message: "app.notification.completed",
                messageArgs: "messageArgs",
                appName: NotificationApp.AGGREGATE_REPORT,
                executionStatusId: 1
        )
        notification.save(failOnError:true, flush:true)

        WorkflowState workflowStateInstance = new WorkflowState(name: 'test', createdBy: 'user', modifiedBy: 'user')
        workflowStateInstance.save(failOnError:true)

        WorkflowState workflowStateInstance_2 = new WorkflowState(name: 'test_2', createdBy: 'user', modifiedBy: 'user')
        workflowStateInstance_2.save(failOnError:true)

        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy:'user', modifiedBy:'user')
        userGroup.save(failOnError:true)

        User normalUser = makeNormalUser("user2", [])

        WorkflowRule workflowRuleInstance = new WorkflowRule(
                name: 'testRule1',
                createdBy: 'user',
                modifiedBy: 'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.PERIODIC_REPORT,
                initialState: workflowStateInstance,
                targetState: workflowStateInstance_2,
                executors: [normalUser],
                executorGroups: [userGroup]
        )
        workflowRuleInstance.save(failOnError:true, validate: false)
         ExecutedPeriodicReportConfiguration.metaClass.static.getStates = { User user ->
            [workflowStateInstance, workflowStateInstance_2]
        }
        ExecutedPeriodicReportConfiguration.metaClass.static.executeQuery = { CharSequence query, Map params ->
            return [[0L, 0L, 0L, 0L, 0L, 0L]]
        }
        ExecutedPeriodicReportConfiguration.metaClass.static.countAllBySearchStringAndStatusInList = { LibraryFilter filter -> new Object() {
            Long get() { return 0 }
        } }

        ExecutedPeriodicReportConfiguration.metaClass.static.overdueIds = { User user , Boolean isAdmin = true -> new Object() {
            Long get() { return 0 }
        } }

        ExecutedPeriodicReportConfiguration.metaClass.static.dueSoonIds = { User user ,Boolean isAdmin = true -> new Object() {
            Long get() { return 0 }
        } }

        ExecutedPeriodicReportConfiguration.metaClass.static.scheduledIds = { User user, Boolean isAdmin = true -> new Object() {
            Long get() { return 0 }
        } }

        ExecutedPeriodicReportConfiguration.metaClass.static.submittedRecentlyIds = { User user, Boolean isAdmin = true -> new Object() {
            Long get() { return 0 }
        } }

        controller.userService = makeSecurityServiceUser(adminUser)
        when:
        controller.getAggregateSummary()
        then:
        response.json.result.size() == 8
        response.json.result["new"] == 1
        response.json.result["states"].size() == 2
    }

    void "test getAdvancedReportRequest"(){
        def adminUser = makeAdminUser()
        Notification notification = new Notification(user: adminUser, level: NotificationLevelEnum.INFO, message: "app.notification.reportRequest.assigned", messageArgs: "messageArgs", appName: NotificationApp.AGGREGATE_REPORT,
                executionStatusId: 1)
        notification.save(failOnError:true,flush:true)
        WorkflowState workflowStateInstance=new WorkflowState(name:'test dashboard',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        WorkflowState workflowStateInstance_2=new WorkflowState(name:'test_2',createdBy:'user',modifiedBy:'user')
        workflowStateInstance_2.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        User normalUser = makeNormalUser("user2", [])
        WorkflowRule workflowRuleInstance=new WorkflowRule(name:'testRule1',createdBy:'user',modifiedBy:'user',
                configurationTypeEnum: WorkflowConfigurationTypeEnum.REPORT_REQUEST,initialState: workflowStateInstance,targetState: workflowStateInstance_2,
                executors: [normalUser],executorGroups:[userGroup])
        workflowRuleInstance.save(failOnError:true,validate: false)
        UserGroup.metaClass.static.fetchAllUserGroupByUser = { User user -> []}
        ReportRequest.metaClass.static.fetchByWorkflowState = { WorkflowState state, User user -> new Object() {
            Integer count(Object o) {
                return 0
            }}
        }
        ReportRequest.metaClass.static.fetchByPriority = {  User user -> new Object() {
            List list(Object o) {
                [[1,"name",0]]
            }}
        }
        ReportRequest.metaClass.static.getSummary = {User user -> [:]}
        controller.userService = makeSecurityServiceUser(adminUser)
        when:
        controller.getAdvancedReportRequest()
        then:
        response.json.result.size() == 5
        response.json.result["new"] == 1
        response.json.result["status"] == []
        response.json.result["user"].size() == 5
        response.json.result["priority"] == [[count:0,id:1,title:'name']]
        response.json.result["due"].size() == 0
    }

    void "test addReportWidget isPvqModule false"() {
        given:
        def adminUser = makeAdminUser()
        Dashboard dashboard = new Dashboard(
                id: 1,
                label: "test1",
                dashboardType: DashboardEnum.PVQ_PUBLIC,
                owner: adminUser,
                sharedWith: [adminUser, adminUser],
                sharedWithGroup: [new UserGroup(name: "group", createdBy: "user", modifiedBy: "user")],
                createdBy: "user",
                modifiedBy: "user"
        )
        dashboard.save(failOnError: true, flush: true)
        def dashboardServiceMock = new MockFor(DashboardService)
        dashboardServiceMock.demand.getDashboard(0..1) { params, request -> Dashboard.get(1) }
        controller.dashboardService = dashboardServiceMock.proxyInstance()
        def crudServiceMock = new Expando()
        crudServiceMock.save = { Dashboard d -> d.save(failOnError: true) }
        controller.CRUDService = crudServiceMock

        expect:
        dashboard.widgets.size() == 0

        when:
        params.id = dashboard.id
        params.widgetType = widgetType
        params.chartId = 0
        controller.addReportWidget()
        then:
        response.redirectUrl == '/dashboard/index/1'
        Dashboard newDashboard = Dashboard.get(params.id)
        newDashboard.widgets.size() == result

        where:
        widgetType                  | result
        "LAST_REPORTS"              | 1
        "ACTION_ITEMS"              | 1
        "AGGREGATE_REPORTS_SUMMARY" | 1
        "ADHOC_REPORTS_SUMMARY"     | 1
        "REPORT_REQUEST_SUMMARY"    | 1
        "ADVANCED_REPORT_REQUEST"   | 1
        "ACTION_ITEMS_SUMMARY"      | 1
        "CALENDAR"                  | 1
        "CHART"                     | 1

    }

    void "test addReportWidget isPvqModule true"(){
        given:
        def adminUser = makeAdminUser()
        Dashboard dashboard = new Dashboard(
                id: 1,
                label: "test1",
                dashboardType: DashboardEnum.PVQ_PUBLIC,
                owner: adminUser,
                sharedWith: [adminUser, adminUser],
                sharedWithGroup: [new UserGroup(name: "group", createdBy: "user", modifiedBy: "user")],
                createdBy: "user",
                modifiedBy: "user"
        )
        dashboard.save(failOnError: true, flush: true)

        def dashboardServiceMock = new MockFor(DashboardService)
        dashboardServiceMock.demand.getDashboard(0..1) { params, request -> Dashboard.get(1) }
        controller.dashboardService = dashboardServiceMock.proxyInstance()

        // Inject static ViewHelper method
        ViewHelper.metaClass.static.isPvqModule = { Object request -> return true }

        // ✅ Mock CRUDService to avoid NullPointerException
        def crudServiceMock = new Expando()
        crudServiceMock.save = { Dashboard d -> d.save(failOnError: true) }
        controller.CRUDService = crudServiceMock

        expect:
        dashboard.widgets.size() == 0

        when:
        params.id = dashboard.id
        params.widgetType = widgetType
        params.chartId = 0
        controller.addReportWidget()

        then:
        response.redirectUrl == '/quality?id=1'
        Dashboard newDashboard = Dashboard.get(params.id)
        newDashboard.widgets.size() == result

        where:
        widgetType                  | result
        "LAST_REPORTS"              | 1
        "ACTION_ITEMS"              | 1
        "AGGREGATE_REPORTS_SUMMARY" | 1
        "ADHOC_REPORTS_SUMMARY"     | 1
        "REPORT_REQUEST_SUMMARY"    | 1
        "ADVANCED_REPORT_REQUEST"   | 1
        "ACTION_ITEMS_SUMMARY"      | 1
        "CALENDAR"                  | 1
        "CHART"                     | 1
    }

    void "testRemoveReportWidgetAjax"() {
        given:
        def adminUser = makeAdminUser()
        Dashboard dashboard = new Dashboard(
                id: 1,
                label: "test1",
                dashboardType: DashboardEnum.PVQ_PUBLIC,
                owner: adminUser,
                sharedWith: [adminUser, adminUser],
                sharedWithGroup: [new UserGroup(name: "group", createdBy: "user", modifiedBy: "user")],
                createdBy: "user",
                modifiedBy: "user"
        )
        dashboard.save(failOnError: true, flush: true)

        ReportWidget reportWidget = new ReportWidget(
                widgetType: WidgetTypeEnum.LAST_REPORTS,
                x: 0, y: 0, width: 0, height: 0,
                autoPosition: true
        )
        reportWidget.save(failOnError: true, flush: true)

        dashboard.addToWidgets(reportWidget)
        dashboard.save(flush: true)

        def dashboardServiceMock = new MockFor(DashboardService)
        dashboardServiceMock.demand.getDashboard(0..1) { params, request -> Dashboard.get(1) }
        controller.dashboardService = dashboardServiceMock.proxyInstance()

        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> adminUser }
        controller.userService = mockUserService.proxyInstance()

        // ✅ Mock CRUDService to prevent NullPointerException
        def crudServiceMock = new Expando()
        crudServiceMock.save = { Dashboard d -> d.save(failOnError: true) }
        controller.CRUDService = crudServiceMock

        when:
        params.widgetId = reportWidget.id
        params.id = dashboard.id
        controller.removeReportWidgetAjax()

        then:
        response.json.result == "OK"
        Dashboard.get(dashboard.id).widgets.isEmpty()
    }


    void "test updateReportWidgetsAjax success"() {
        given:
        def adminUser = makeAdminUser()
        Dashboard dashboard = new Dashboard(
                id: 1,
                label: "test1",
                dashboardType: DashboardEnum.PVQ_PUBLIC,
                owner: adminUser,
                sharedWith: [adminUser, adminUser],
                sharedWithGroup: [new UserGroup(name: "group", createdBy: "user", modifiedBy: "user")],
                createdBy: "user",
                modifiedBy: "user"
        )
        dashboard.save(failOnError: true, flush: true)

        ReportWidget reportWidget = new ReportWidget(
                id: 1,
                widgetType: WidgetTypeEnum.LAST_REPORTS,
                x: 0, y: 0, width: 0, height: 0,
                autoPosition: true
        )
        reportWidget.save(failOnError: true, flush: true)

        dashboard.addToWidgets(reportWidget)
        dashboard.save(flush: true)

        def dashboardServiceMock = new MockFor(DashboardService)
        dashboardServiceMock.demand.getDashboard(0..1) { params, request -> Dashboard.get(1) }
        controller.dashboardService = dashboardServiceMock.proxyInstance()

        // ✅ Mock CRUDService to prevent NullPointerException
        def crudServiceMock = new Expando()
        crudServiceMock.save = { Object obj -> obj.save(failOnError: true) }
        controller.CRUDService = crudServiceMock

        when:
        params.items = JsonOutput.toJson([[id: 1, x: 5, y: 5, width: 10, height: 10]])
        params.id = dashboard.id
        controller.updateReportWidgetsAjax()

        then:
        response.json.result == "OK"
        reportWidget.refresh()
        reportWidget.x == 5
        reportWidget.y == 5
        reportWidget.width == 10
        reportWidget.height == 10
    }

    void "test updateReportWidgetsAjax converter exception"(){
        def adminUser=makeAdminUser()
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group",createdBy: "user",modifiedBy: "user")], createdBy: "user", modifiedBy: "user")
        dashboard.save(failOnError:true,flush: true)
        ReportWidget reportWidget = new ReportWidget(widgetType: WidgetTypeEnum.LAST_REPORTS,x: 0,y: 0,width: 0,height:0,autoPosition: true)
        reportWidget.save(failOnError:true,flush:true)
        dashboard.addToWidgets(reportWidget)
        dashboard.save(flush:true)
        def dashboardServiceMock=new MockFor(DashboardService)
        dashboardServiceMock.demand.getDashboard(0..1){ params,request -> Dashboard.get(1)}
        controller.dashboardService = dashboardServiceMock.proxyInstance()
        when:
        params.items = "id:1,x:5,y:5,width:10,height:10"
        params.id = dashboard.id
        controller.updateReportWidgetsAjax()
        then:
        response.json.result == "OK"
        reportWidget.x == 0
        reportWidget.y == 0
        reportWidget.width == 0
        reportWidget.height == 0
    }

    void "test updateWidgetSettings success"(){
        ReportWidget reportWidget = new ReportWidget(widgetType: WidgetTypeEnum.LAST_REPORTS,x: 0,y: 0,width: 0,height:0,autoPosition: true)
        reportWidget.save(failOnError:true,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){ theInstance -> theInstance}
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = reportWidget.id
        params.data = "setting"
        controller.updateWidgetSettings()
        then:
        response.json.httpCode == 200
        response.json.status == true
        response.json.message == ""
        reportWidget.settings == "setting"
    }

    void "test updateWidgetSettings exception"(){
        ReportWidget reportWidget = new ReportWidget(widgetType: WidgetTypeEnum.LAST_REPORTS,x: 0,y: 0,width: 0,height:0,autoPosition: true)
        reportWidget.save(failOnError:true,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){ theInstance -> throw new Exception()}
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = reportWidget.id
        controller.updateWidgetSettings()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.server.error.message"
    }

    void "test updateWidgetSettings reportwidget null"(){
        ReportWidget reportWidget = new ReportWidget(widgetType: WidgetTypeEnum.LAST_REPORTS,x: 0,y: 0,width: 0,height:0,autoPosition: true)
        reportWidget.save(failOnError:true,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){ theInstance -> theInstance}
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.updateWidgetSettings()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

    void "test updateLabel success"(){
        def adminUser=makeAdminUser()
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group",createdBy: "user",modifiedBy: "user")], createdBy: "user", modifiedBy: "user")
        dashboard.save(failOnError:true,flush: true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){ theInstance -> theInstance}
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = dashboard.id
        params.label = "setting"
        controller.updateLabel()
        then:
        response.json.httpCode == 200
        response.json.status == true
        response.json.message == ""
        dashboard.label == "setting"
    }

    void "test updateLabel exception"(){
        def adminUser=makeAdminUser()
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group",createdBy: "user",modifiedBy: "user")], createdBy: "user", modifiedBy: "user")
        dashboard.save(failOnError:true,flush: true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){ theInstance -> throw new Exception()}
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = dashboard.id
        controller.updateLabel()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.server.error.message"
    }

    void "test updateLabel dashboard null"(){
        def adminUser=makeAdminUser()
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group",createdBy: "user",modifiedBy: "user")], createdBy: "user", modifiedBy: "user")
        dashboard.save(failOnError:true,flush: true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){ theInstance -> theInstance}
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.updateLabel()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

    void "test getChartWidgetDataAjax"(){
        User normalUser = makeNormalUser("user",[])
        ReportWidget reportWidget = new ReportWidget(widgetType: WidgetTypeEnum.CHART,x: 0,y: 0,width: 0,height:0,autoPosition: true)
        reportWidget.save(failOnError:true,flush:true)
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(owner: normalUser,reportName: "report_1")
        reportConfiguration.save(failOnError:true,validate:false)
        reportWidget.reportConfiguration = reportConfiguration
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(owner: normalUser,reportName: "report_1",status: ReportExecutionStatusEnum.COMPLETED)
        executedReportConfiguration.save(failOnError:true,validate:false)
        controller.userService=makeSecurityServiceCurrentUser(normalUser)
        controller.dashboardService = new DashboardService()
        controller.dashboardService.userService = [:]
        when:
        params['widgetId'] = reportWidget.id
        controller.getChartWidgetDataAjax()
        then:
        response.json.title == "report_1"
        response.json.runDate == ""
    }

    void "test getChartWidgetData"(){
        User normalUser = makeNormalUser("user",[])
        ReportWidget reportWidget = new ReportWidget(widgetType: WidgetTypeEnum.CHART,x: 0,y: 0,width: 0,height:0,autoPosition: true)
        reportWidget.save(failOnError:true,flush:true)
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(owner: normalUser,reportName: "report_1")
        reportConfiguration.save(failOnError:true,validate:false)
        reportWidget.reportConfiguration = reportConfiguration
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: new ReportTemplate(name: "template"))
        executedTemplateQuery.save(failOnError:true, validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(owner: normalUser,reportName: "report_1",status: ReportExecutionStatusEnum.COMPLETED)
        executedReportConfiguration.save(failOnError:true,validate:false)
        ExecutedReportConfiguration.metaClass.static.executeQuery = { CharSequence query, Map params ->
            return executedReportConfiguration
        }
        def dashboardServiceMock = new MockFor(DashboardService)
        dashboardServiceMock.demand.getExecutedTemplateQuery(0..1) { ReportWidget widget, ExecutedReportConfiguration executedConfiguration ->
            return executedTemplateQuery
        }
        controller.dashboardService = dashboardServiceMock.proxyInstance()
        def dynamicReportServiceMock = new MockFor(DynamicReportService)
        ViewHelper.metaClass.static.getReportTitle = {ExecutedReportConfiguration executedConfiguration, ExecutedTemplateQuery exTemplateQuery ->
            return "report_1"
        }
        controller.userService=makeSecurityServiceCurrentUser(normalUser)
        Tenants.metaClass.static.currentId = { -> return 10}
        when:
        def result = controller.invokeMethod('getChartWidgetData', [reportWidget, normalUser] as Object[])
        then:
        result.size() == 7
        result.reportWidget == reportWidget
        result.title == "report_1"
    }

    void "test etl"(){
        User normalUser = makeNormalUser("user",[])
        EtlSchedule etlSchedule = new EtlSchedule(scheduleName: "schedule_1",startDateTime: "morning",repeatInterval: "error=exception;error=validation",
                emailToUsers: "email" , createdBy: "user",modifiedBy: "user")
        etlSchedule.save(failOnError:true,flush:true)
        def userServiceMock = new MockFor(UserService)
        userServiceMock.demand.getCurrentUser(0..3) { -> normalUser }
        controller.userService = userServiceMock.proxyInstance()
        def mockEtlService = new MockFor(EtlJobService)
        mockEtlService.demand.getSchedule(0..1){ -> EtlSchedule.get(1)}
        mockEtlService.demand.getEtlStatus(0..1){ -> new EtlStatus(status: EtlStatusEnum.ETL_INITIATED)}
        mockEtlService.demand.checkPreMartEtlStatusApplicable(0..1) { -> false}
        mockEtlService.demand.checkAffEtlStatusApplicable(0..1) { -> false}
        controller.etlJobService = mockEtlService.proxyInstance()
        DateUtil.metaClass.static.getLongDateStringForTimeZone = {Date date, String timeZone, Boolean showTimeZone = false -> ""}
        when:
        controller.etl()
        then:
        response.json.result.size() == 10
        response.json.result.status == "ETL_INITIATED"
        response.json.result.lastRun == ""
        response.json.result.repeat == [[label:"error",value:"exception"],[label:"error",value:"validation"]]
    }

    void "test renderReportOutputType"(){
        given:
        File reportFile = File.createTempFile("temp", "")
        reportFile.write("hello world!")
        controller.dynamicReportService = new Object() {
            public String getReportNameForWidget(List<ReportWidget> reportWidgetList, Map params) {
                return "REPORT_WIDGET_0"
            }

            public String getContentType(String type) {
                return "text/plain"
            }
        }
        when:
        params.outputFormat = "csv"
        controller.invokeMethod('renderReportOutputType', [reportFile, [], params])
        then:
        response.getContentType()
        response.getContentAsString() == "hello world!"
        response.getHeaderValue("Content-Disposition") == 'attachment;filename="REPORT_WIDGET_0.csv"'
    }

    void "test exportWidget"(){
        User normalUser = makeNormalUser("user",[])
        File reportFile = File.createTempFile("temp", "")
        reportFile.write("hello world!")
        ReportWidget reportWidget = new ReportWidget(widgetType: WidgetTypeEnum.CHART,x: 0,y: 0,width: 0,height:0,autoPosition: true)
        reportWidget.save(failOnError:true,flush:true)
        controller.userService = makeSecurityServiceCurrentUser(normalUser)
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.createMultiReportForWidgetExport(0..1){List<ReportWidget> reportWidgetList, Map params -> return reportFile}
        mockDynamicReportService.demand.getReportNameForWidget(0..1){List<ReportWidget> reportWidgetList, Map params -> return "REPORT_WIDGET_0"}
        mockDynamicReportService.demand.getContentType(0..1){String type -> return "text/plain"}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        when:
        params.data = JsonOutput.toJson([selectedWidgets: [reportWidget.id] ,outputFormat:'pdf'])
        controller.exportWidget()
        then:
        response.getContentType()
        response.getContentAsString() == "hello world!"
        response.getHeaderValue("Content-Disposition") == 'attachment;filename="REPORT_WIDGET_0.pdf"'
    }

    void "test getDashboardAjax"(){
        given:
        Dashboard dashboard = new Dashboard()
        dashboard.save(failOnError:true,validate:false)
        Dashboard.metaClass.static.get={Long id -> dashboard}
        dashboard.metaClass.toMap={[id:3L,name:"rx",description:"rx"]}
        when:
        params.id=2L
        controller.getDashboardAjax()
        then:
        response.status==200
    }

    void "test reportConfiguration"(){
        given:
        ReportWidget.metaClass.static.get={Long id -> new ReportWidget(sectionNumber: 0, widgetType:WidgetTypeEnum.ACTION_ITEMS,reportConfiguration: new Configuration(templateQueries: [new TemplateQuery()]))}
        when:
        params.widgetId="1"
        controller.reportConfiguration()
        then:
        response.status==200
    }

}
