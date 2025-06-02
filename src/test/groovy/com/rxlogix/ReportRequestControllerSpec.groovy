package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.user.*
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([AuditLogConfigUtil,ReportRequest, WorkflowState, DateUtil])
class ReportRequestControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportRequestController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains ReportRequest, WorkflowState, User, UserGroup, Tenant, Role, Preference, ReportRequestType, ReportRequestPriority, DateRangeType, UserRole, ActionItem, ActionItemCategory, ReportRequest, Configuration, PeriodicReportConfiguration, DeliveryOption, TaskTemplate, ReportRequestLinkType, ReportRequestComment, WorkflowState, ReportRequestLink, ReportRequestAttachment, Task, UserGroupUser
        AuditLogConfigUtil.metaClass.static.logChanges = { domain, Map newMap, Map oldMap, String eventName, String extraValue -> }
    }

    private makeSecurityServiceCurrentUser(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser(0..1) { -> user }
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

    void "test index"(){
        given:
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        WorkflowState.metaClass.static.getFinalStatesForType = {WorkflowConfigurationTypeEnum type -> null}
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test createAdhocReport"(){
        when:
        controller.createAdhocReport()
        then:
        response.status == 200
        response.forwardedUrl == '/reportRequest/update?configurationType=ADHOC_REPORT'
    }

    void "test createAggregateReport"(){
        when:
        controller.createAggregateReport()
        then:
        response.status == 200
        response.forwardedUrl == '/reportRequest/update?configurationType=PERIODIC_REPORT'
    }

    void "test copy"() {
        when:
        def serviceMock = new MockFor(ReportRequestService)
        serviceMock.demand.copy { ReportRequest reportRequest -> reportRequest }
        controller.reportRequestService = serviceMock.proxyInstance()
        ReportRequest reportRequest = new ReportRequest(id: 1, reportName: "test", description: "test")
        reportRequest.metaClass.hasErrors = { -> false }
        controller.copy(reportRequest)
        then:
        response.status == 302
        response.redirectedUrl == "/reportRequest/show"
    }

    void "test copy not found"(){
        when:
        controller.copy(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/reportRequest/index'
    }

    void "test createReport configuration"(){
        User normalUser = makeNormalUser("user",[])
        def adminUser = makeAdminUser()
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        Date now=new Date()
        ActionItemCategory actionItemCategory=new ActionItemCategory(name: "abc",key: "xyz")
        actionItemCategory.save(failOnError:true,flush:true)
        ActionItem actionItem=new ActionItem(actionCategory:actionItemCategory ,description: "dsafadfs",modifiedBy: "admin",createdBy: "admin",assignedTo: adminUser
                ,dueDate: now ,completionDate: now+30,priority: "priority",status: StatusEnum.OPEN ,appType: AppTypeEnum.QUALITY_MODULE)
        actionItem.dueDate=new Date()
        actionItem.save(failOnError:true,flush:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser, frequency : ReportRequestFrequencyEnum.RUN_ONCE,
                reportingPeriodStart: new Date(), reportingPeriodEnd: new Date())
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup]
        reportRequest.save(failOnError:true)
        when:
        params.id = reportRequest.id
        params['actionItems'] = [actionItem]
        params['requesters'] = [adminUser]
        params['assignedTo'] = normalUser
        params['configurationType'] = "Adhoc Report"
        controller.createReport()
        then:
        response.redirectUrl == '/periodicReport/create?continueEditing=true'
    }

    void "test createReport periodicReport"(){
        User normalUser = makeNormalUser("user",[])
        def adminUser = makeAdminUser()
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        Date now=new Date()
        ActionItemCategory actionItemCategory=new ActionItemCategory(name: "abc",key: "xyz")
        actionItemCategory.save(failOnError:true,flush:true)
        ActionItem actionItem=new ActionItem(actionCategory:actionItemCategory ,description: "dsafadfs",modifiedBy: "admin",createdBy: "admin",assignedTo: adminUser
                ,dueDate: now ,completionDate: now+30,priority: "priority",status: StatusEnum.OPEN ,appType: AppTypeEnum.QUALITY_MODULE)
        actionItem.dueDate=new Date()
        actionItem.save(failOnError:true,flush:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser, frequency : ReportRequestFrequencyEnum.RUN_ONCE,
                reportingPeriodStart: new Date(), reportingPeriodEnd: new Date())
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup]
        reportRequest.save(failOnError:true)
        when:
        params.id = reportRequest.id
        params['actionItems'] = [actionItem]
        params['requesters'] = [adminUser]
        params['assignedTo'] = normalUser
        params['configurationType'] = "Periodic Report"
        controller.createReport()
        then:
        response.redirectUrl == '/periodicReport/create?continueEditing=true'
    }

    void "test getModelMap"(){
        User normalUser = makeNormalUser("user",[])
        def adminUser = makeAdminUser()
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        TaskTemplate taskTemplate = new TaskTemplate(name: "template",createdBy: "user",modifiedBy: "user",type: TaskTemplateTypeEnum.REPORT_REQUEST)
        taskTemplate.save(failOnError:true)
        ReportRequestLinkType reportRequestLinkType = new ReportRequestLinkType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestLinkType.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        Date now=new Date()
        ActionItemCategory actionItemCategory=new ActionItemCategory(name: "abc",key: "xyz")
        actionItemCategory.save(failOnError:true,flush:true)
        ActionItem actionItem=new ActionItem(actionCategory:actionItemCategory ,description: "actionitem",modifiedBy: "admin",createdBy: "admin",assignedTo: adminUser
                ,dueDate: now ,completionDate: now+30,priority: "priority",status: StatusEnum.OPEN ,appType: AppTypeEnum.QUALITY_MODULE)
        actionItem.dueDate=new Date()
        actionItem.save(failOnError:true,flush:true)
        WorkflowState workflowStateInstance=new WorkflowState(name:'test',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup]
        reportRequest.actionItems = [actionItem]
        reportRequest.requesters=[adminUser]
        reportRequest.workflowState = workflowStateInstance
        reportRequest.save(failOnError:true)
        ReportRequestComment reportRequestComment = new ReportRequestComment(reportComment: "comment",createdBy: "user",modifiedBy: "user",
                isDeleted: false,newObj: true)
        reportRequestComment.reportRequest = reportRequest
        reportRequestComment.save(failOnError:true)
        reportRequest.addToComments(reportRequestComment)
        def mockUserService = Mock(UserService)
        mockUserService.getCurrentUser() >> { return normalUser}
        mockUserService.isCurrentUserAdmin() >> { return true}
        controller.userService = mockUserService
        WorkflowState.metaClass.static.getFinalStatesForType = { WorkflowConfigurationTypeEnum type ->
            workflowStateInstance
        }
        when:
        def result = controller.invokeMethod('getModelMap', [reportRequest] as Object[])
        then:
        result.size() == 12
    }

    void "test create"(){
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        WorkflowState.metaClass.static.getFinalStatesForType = { WorkflowConfigurationTypeEnum type ->
            workflowStateInstance
        }
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user',defaultRRAssignTo: true)
        userGroup.save(failOnError:true)
        def mockUserService = Mock(UserService)
        mockUserService.isAnyGranted(_) >> { return true}
        mockUserService.getCurrentUser() >> { return normalUser}
        mockUserService.isCurrentUserAdmin() >> { return true}
        controller.userService = mockUserService
        DateUtil.metaClass.static.getEndDate = {String dateToDatePicker, Locale locale -> null}
        when:
        params.dueDate = "3/20/2016 11:59:59"
        controller.create()
        then:
        view == '/reportRequest/create'
        model.size() == 12
    }

    void "test bindAssignedToAndRequestor isUpdate false"(){
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.save(failOnError:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.isAnyGranted(0..1){ String role-> true}
        controller.userService = mockUserService.proxyInstance()
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        List requesters=["User_${id1}","UserGroup_${id2}"]
        String executors = "User_${id1}"
        String requestorNames="${userGroup_1.name} ,${userGroup_2.name} ,${normalUser_1.fullName} ,${normalUser_2.fullName}"
        when:
        params['assignedTo'] = executors
        params['requesters'] = requesters
        controller.invokeMethod('bindAssignedToAndRequestor', [reportRequest] as Object[])
        then:
        reportRequest.requesters.size() == 2
        reportRequest.requesterGroups.size() == 2
        reportRequest.assignedTo == normalUser_2
        reportRequest.assignedGroupTo == userGroup_1
        reportRequest.requestorsNames == requestorNames
    }

    void "test bindAssignedToAndRequestor isUpdate true"(){
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user',defaultRRAssignTo: true)
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.save(failOnError:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.isAnyGranted(0..2){ String role-> true}
        controller.userService = mockUserService.proxyInstance()
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        List requesters=["User_${id1}","UserGroup_${id2}"]
        String executors = "User_${id1}"
        String requestorNames="${userGroup_2.name} ,${normalUser_2.fullName}"
        when:
        params['assignedTo'] = executors
        params['requesters'] = requesters
        controller.invokeMethod('bindAssignedToAndRequestor', [reportRequest, true] as Object[])
        then:
        reportRequest.requesters.size() == 1
        reportRequest.requesterGroups.size() == 1
        reportRequest.assignedTo == normalUser_2
        reportRequest.assignedGroupTo == null
        reportRequest.requestorsNames == requestorNames
    }

    void "test getActionItemMap"(){
        ActionItemCategory actionItemCategory=new ActionItemCategory(name: "abc",key: "xyz")
        actionItemCategory.save(failOnError:true,flush:true)
        User normalUser_2 = makeNormalUser("user3", [])
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user',defaultRRAssignTo: true)
        userGroup_2.save(failOnError:true)
        def id1=normalUser_2.id as String
        String executors = "User_${id1}"
        when:
        params.("actionItems[0].assignedTo") = executors
        params.("actionItems[0].actionCategory") = "xyz"
        params.("actionItems[0].priority") = "priority"
        params.("actionItems[0].status") = StatusEnum.OPEN
        params.("actionItems[0].appType") = AppTypeEnum.QUALITY_MODULE
        params.("actionItems[0].description") = "actionitem"
        def result = controller.getActionItemMap(0)
        then:
        result.size() == 8
        result.actionCategory == actionItemCategory
        result.assignedGroupTo == null
        result.assignedTo == normalUser_2
        result.status == StatusEnum.OPEN
        result.priority == 'priority'
        result.description == "actionitem"
    }

    void "test bindActionItems"(){
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user',defaultRRAssignTo: true)
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        ActionItemCategory actionItemCategory=new ActionItemCategory(name: "abc",key: "xyz")
        actionItemCategory.save(failOnError:true,flush:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.save(failOnError:true)
        def id1=normalUser_2.id as String
        String executors = "User_${id1}"
        when:
        params.("actionItems[0]") = "true"
        params.("actionItems[0].assignedTo") = executors
        params.("actionItems[0].actionCategory") = "xyz"
        params.("actionItems[0].priority") = "priority"
        params.("actionItems[0].status") = StatusEnum.OPEN
        params.("actionItems[0].appType") = AppTypeEnum.QUALITY_MODULE
        params.("actionItems[0].description") = "newactionitem"
        params.("actionItems[0].newObj") = "true"
        params.("actionItems[0].completionDateObj") = ""
        params.("actionItems[0].dateCreatedObj") = ""
        params.("actionItems[0].id") = "1"
        params.("actionItems[0].dueDateObj") = "20-Mar-/2016"
        params.("actionItems[0].deleted") = "false"
        controller.bindActionItems(reportRequest,new Locale("en"),"UTC")
        then:
        reportRequest.actionItems.size() == 1
        reportRequest.actionItems[0].description == "newactionitem"
        reportRequest.actionItems[0].status == StatusEnum.OPEN
    }

    void "test bindActionItems newObj false"(){
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user',defaultRRAssignTo: true)
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        Date now = new Date()
        ActionItemCategory actionItemCategory=new ActionItemCategory(name: "abc",key: "xyz")
        actionItemCategory.save(failOnError:true,flush:true)
        ActionItem actionItem=new ActionItem(actionCategory:actionItemCategory ,description: "actionitem",modifiedBy: "admin",createdBy: "admin",assignedTo: normalUser_1
                ,dueDate: now ,completionDate: now+30,priority: "priority",status: StatusEnum.OPEN ,appType: AppTypeEnum.QUALITY_MODULE)
        actionItem.dueDate=new Date()
        actionItem.save(failOnError:true,flush:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.actionItems = [actionItem]
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.save(failOnError:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name -> theInstance}
        controller.CRUDService = mockCRUDService.proxyInstance()
        def id1=normalUser_2.id as String
        String executors = "User_${id1}"
        when:
        params.("actionItems[0]") = "true"
        params.("actionItems[0].assignedTo") = executors
        params.("actionItems[0].actionCategory") = "xyz"
        params.("actionItems[0].priority") = "priority"
        params.("actionItems[0].status") = StatusEnum.OPEN
        params.("actionItems[0].appType") = AppTypeEnum.QUALITY_MODULE
        params.("actionItems[0].description") = "newactionitem"
        params.("actionItems[0].newObj") = "false"
        params.("actionItems[0].completionDateObj") = ""
        params.("actionItems[0].dateCreatedObj") = ""
        params.("actionItems[0].id") = actionItem.id
        params.("actionItems[0].dueDateObj") = "20-Mar-/2016"
        params.("actionItems[0].deleted") = "true"
        controller.invokeMethod('bindActionItems', [reportRequest, new Locale("en"), "UTC"] as Object[])
        then:
        reportRequest.actionItems.size() == 0
    }

    void "test bindComments"(){
        User normalUser_1 = makeNormalUser("user2", [])
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.save(failOnError:true)
        when:
        params.("comments[0]") = "true"
        params.("comments[0].newObj") = "true"
        params.("comments[0].deleted") ="false"
        params.("comments[0].reportComment") = "comment"
        params.("comments[0].dateCreated") = ""
        params.("comments[0].id") = "1"
        controller.bindComments(reportRequest,"UTC")
        then:
        reportRequest.comments.size() == 1
        reportRequest.comments[0].reportComment == "comment"
    }

    void "test bindAsOfVersionDate evaluate date as Latest Version"(){
        User normalUser_1 = makeNormalUser("user2", [])
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.save(failOnError:true)
        controller.userService = makeSecurityServiceCurrentUser(normalUser_1)
        when:
        params['includeLockedVersion'] = true
        controller.bindAsOfVersionDate(reportRequest,"20-Mar-/2016")
        then:
        reportRequest.includeLockedVersion == true
        reportRequest.asOfVersionDate == null
    }

    void "test bindAsOfVersionDate evaluate date as Version ASOF"(){
        User normalUser_1 = makeNormalUser("user2", [])
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1,evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.save(failOnError:true)
        controller.userService = makeSecurityServiceCurrentUser(normalUser_1)
        DateUtil.metaClass.static.getEndDate = {String dateToDatePicker, Locale locale -> return  new Date()}
        DateUtil.metaClass.static.getEndDate = {String dateToDatePicker, def timezone, Locale locale -> return  new Date()}
        when:
        params['includeLockedVersion'] = true
        controller.invokeMethod('bindAsOfVersionDate', [reportRequest, "20-Mar-/2016"] as Object[])
        then:
        reportRequest.includeLockedVersion == true
        reportRequest.asOfVersionDate != null
    }

    void "test save success"(){
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user',defaultRRAssignTo: true)
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        Date now=new Date()
        ActionItemCategory actionItemCategory=new ActionItemCategory(name: "abc",key: "xyz")
        actionItemCategory.save(failOnError:true,flush:true)
        ActionItem actionItem=new ActionItem(actionCategory:actionItemCategory ,description: "dsafadfs",modifiedBy: "admin",createdBy: "admin",assignedTo: normalUser_1
                ,dueDate: now ,completionDate: now+30,priority: "priority",status: StatusEnum.OPEN ,appType: AppTypeEnum.QUALITY_MODULE)
        actionItem.dueDate=new Date()
        actionItem.save(failOnError:true,flush:true)
        def mockUserService = Mock(UserService)
        mockUserService.isAnyGranted(_) >> { return true}
        mockUserService.getCurrentUser() >> {return normalUser_1}
        controller.userService = mockUserService
        def mockReportRequestService = new MockFor(ReportRequestService)
        mockReportRequestService.demand.validateDatesBeforeSave(0..1) {ReportRequest reportRequestInstance -> return ""}
        mockReportRequestService.demand.save(0..1){reportRequestInstance->reportRequestInstance}
        mockReportRequestService.demand.sendCreationNotification(0..1){ReportRequest reportRequestInstance -> null}
        controller.reportRequestService = mockReportRequestService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance -> theInstance}
        controller.CRUDService = mockCRUDService.proxyInstance()
        DateUtil.metaClass.static.getEndDate = {String dateToDatePicker, Locale locale -> return  new Date()}
        DateUtil.metaClass.static.getEndDate = {String dateToDatePicker, def timezone, Locale locale -> return  new Date()}
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        String requesters=["User_${id1}","UserGroup_${id2}"]
        String executors = "User_${id1}"
        when:
        request.method = 'POST'
        request.contentType = FORM_CONTENT_TYPE
        params['linksToDelete'] = ""
        params['linksToAdd'] = ""
        params['attachmentsToDelete'] = ""
        params['reportName']= "report_1"
        params['reportRequestType']= reportRequestType
        params['priority']= reportRequestPriority
        params['description']= "description"
        params['dueDate'] = "10-Mar-2016"
        params['asOfVersionDate']= ""
        params['endDate']= ""
        params['completionDate']= ""
        params['startDate']= ""
        params['dateRangeType'] =  dateRangeType
        params['createdBy'] = "user"
        params['modifiedBy'] = "user"
        params['productSelection'] = "normalstring"
        params['productGroupSelection'] = "normalstring"
        params['studySelection'] = "normalstring"
        params['eventGroupSelection'] = "normalstring"
        params['eventSelection'] = "normalstring"
        params['evaluateDateAs'] = EvaluateCaseDateEnum.VERSION_ASOF
        params['includeLockedVersion'] = true
        params['assignedTo'] = executors
        params['requesters'] = requesters
        params.("comments[0]") = "true"
        params.("comments[0].newObj") = "true"
        params.("comments[0].deleted") ="false"
        params.("comments[0].reportComment") = "comment"
        params.("comments[0].dateCreated") = ""
        params.("comments[0].id") = "1"
        params.("actionItems[0]") = "true"
        params.("actionItems[0].assignedTo") = executors
        params.("actionItems[0].actionCategory") = "xyz"
        params.("actionItems[0].priority") = "priority"
        params.("actionItems[0].status") = StatusEnum.OPEN
        params.("actionItems[0].appType") = AppTypeEnum.QUALITY_MODULE
        params.("actionItems[0].description") = "newactionitem"
        params.("actionItems[0].newObj") = "true"
        params.("actionItems[0].completionDateObj") = ""
        params.("actionItems[0].dateCreatedObj") = ""
        params.("actionItems[0].id") = "1"
        params.("actionItems[0].dueDateObj") = "20-Mar-/2016"
        params.("actionItems[0].deleted") = "false"
        controller.save()
        then:
        flash.message == 'default.created.message'
        response.redirectUrl == '/reportRequest/index'
    }

    void "test save validation exception"(){
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user',defaultRRAssignTo: true)
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        WorkflowState.metaClass.static.getFinalStatesForType = {WorkflowConfigurationTypeEnum type -> workflowStateInstance}
        Date now=new Date()
        ActionItemCategory actionItemCategory=new ActionItemCategory(name: "abc",key: "xyz")
        actionItemCategory.save(failOnError:true,flush:true)
        ActionItem actionItem=new ActionItem(actionCategory:actionItemCategory ,description: "dsafadfs",modifiedBy: "admin",createdBy: "admin",assignedTo: normalUser_1
                ,dueDate: now ,completionDate: now+30,priority: "priority",status: StatusEnum.OPEN ,appType: AppTypeEnum.QUALITY_MODULE)
        actionItem.dueDate=new Date()
        actionItem.save(failOnError:true,flush:true)
        def mockUserService = Mock(UserService)
        mockUserService.isAnyGranted(_) >> { return true}
        mockUserService.getCurrentUser() >> { return normalUser_1}
        mockUserService.isCurrentUserAdmin() >> { return true}
        controller.userService = mockUserService
        def mockReportRequestService = new MockFor(ReportRequestService)
        mockReportRequestService.demand.validateDatesBeforeSave(0..1) {ReportRequest reportRequestInstance -> return ""}
        mockReportRequestService.demand.save(0..1){reportRequestInstance-> throw new ValidationException("message",new ValidationErrors(new Object()))}
        mockReportRequestService.demand.sendCreationNotification(0..1){ReportRequest reportRequestInstance -> null}
        controller.reportRequestService = mockReportRequestService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance -> theInstance}
        controller.CRUDService = mockCRUDService.proxyInstance()
        DateUtil.metaClass.static.getEndDate = {String dateToDatePicker, Locale locale -> return  new Date()}
        DateUtil.metaClass.static.getEndDate = {String dateToDatePicker, def timezone, Locale locale -> return  new Date()}
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        String requesters=["User_${id1}","UserGroup_${id2}"]
        String executors = "User_${id1}"
        when:
        request.method = 'POST'
        request.contentType = FORM_CONTENT_TYPE
        params['linksToDelete'] = ""
        params['linksToAdd'] = ""
        params['attachmentsToDelete'] = ""
        params['reportName']= "report_1"
        params['reportRequestType']= reportRequestType
        params['priority']= reportRequestPriority
        params['description']= "description"
        params['dueDate'] = "10-Mar-2016"
        params['asOfVersionDate']= ""
        params['endDate']= ""
        params['completionDate']= ""
        params['startDate']= ""
        params['dateRangeType'] =  dateRangeType
        params['createdBy'] = "user"
        params['modifiedBy'] = "user"
        params['productSelection'] = "normalstring"
        params['productGroupSelection'] = "normalstring"
        params['studySelection'] = "normalstring"
        params['eventGroupSelection'] = "normalstring"
        params['eventSelection'] = "normalstring"
        params['evaluateDateAs'] = EvaluateCaseDateEnum.VERSION_ASOF
        params['includeLockedVersion'] = true
        params['assignedTo'] = executors
        params['requesters'] = requesters
        params.("comments[0]") = "true"
        params.("comments[0].newObj") = "true"
        params.("comments[0].deleted") ="false"
        params.("comments[0].reportComment") = "comment"
        params.("comments[0].dateCreated") = ""
        params.("comments[0].id") = "1"
        params.("actionItems[0]") = "true"
        params.("actionItems[0].assignedTo") = executors
        params.("actionItems[0].actionCategory") = "xyz"
        params.("actionItems[0].priority") = "priority"
        params.("actionItems[0].status") = StatusEnum.OPEN
        params.("actionItems[0].appType") = AppTypeEnum.QUALITY_MODULE
        params.("actionItems[0].description") = "newactionitem"
        params.("actionItems[0].newObj") = "true"
        params.("actionItems[0].completionDateObj") = ""
        params.("actionItems[0].dateCreatedObj") = ""
        params.("actionItems[0].id") = "1"
        params.("actionItems[0].dueDateObj") = "20-Mar-/2016"
        params.("actionItems[0].deleted") = "false"
        controller.save()
        then:
        flash.message == null
        view == '/reportRequest/create'
        model.size() == 14
    }

    void "test save GET"(){
        when:
        controller.save()
        then:
        flash.error == 'default.not.saved.message'
        response.redirectUrl == '/reportRequest/index'
    }

    void "test update success"() {
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2", createdBy: 'user', modifiedBy: 'user', defaultRRAssignTo: true)
        userGroup_1.save(failOnError: true)
        userGroup_2.save(failOnError: true)

        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority", createdBy: "user", modifiedBy: "user")
        reportRequestPriority.save(failOnError: true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type", createdBy: "user", modifiedBy: "user")
        reportRequestType.save(failOnError: true)
        DateRangeType dateRangeType = new DateRangeType(name: "date")
        dateRangeType.save(failOnError: true)

        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)

        WorkflowState workflowStateInstance = new WorkflowState(name: 'New', createdBy: 'user', modifiedBy: 'user')
        workflowStateInstance.save(failOnError: true)

        ReportRequest reportRequest = new ReportRequest(reportName: "report_1", reportRequestType: reportRequestType, priority: reportRequestPriority,
                description: "description", dueDate: new Date(), asOfVersionDate: new Date(), dateRangeType: dateRangeType,
                owner: normalUser_1, createdBy: "user", modifiedBy: "user", productSelection: "normalstring",
                productGroupSelection: "normalstring", studySelection: "normalstring", eventGroupSelection: "normalstring",
                eventSelection: "normalstring", assignedTo: normalUser_1, assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters = [normalUser_1]
        reportRequest.workflowState = workflowStateInstance
        reportRequest.save(failOnError: true)

        Date now = new Date()
        ActionItemCategory actionItemCategory = new ActionItemCategory(name: "abc", key: "xyz")
        actionItemCategory.save(failOnError: true, flush: true)

        ActionItem actionItem = new ActionItem(actionCategory: actionItemCategory, description: "dsafadfs", modifiedBy: "admin", createdBy: "admin", assignedTo: normalUser_1,
                dueDate: now, completionDate: now + 30, priority: "priority", status: StatusEnum.OPEN, appType: AppTypeEnum.QUALITY_MODULE)
        actionItem.dueDate = new Date()
        actionItem.save(failOnError: true, flush: true)

        def mockUserService = Mock(UserService)
        mockUserService.isAnyGranted(_) >> { return true }
        mockUserService.getCurrentUser() >> { return normalUser_1 }
        controller.userService = mockUserService

        def mockReportRequestService = new MockFor(ReportRequestService)
        mockReportRequestService.demand.getReportRequestMap(0..1){reportRequestInstance -> [:]}
        mockReportRequestService.demand.getReportActionItems(0..1){reportRequestInstance -> []}
        mockReportRequestService.demand.getReportComments(0..1) { reportRequestInstance -> [] }
        mockReportRequestService.demand.getAttachments(0..1){reportRequestInstance -> []}
        mockReportRequestService.demand.getReportRequestMap(0..1){reportRequestInstance -> [:]}
        mockReportRequestService.demand.update(0..1){reportRequestInstance->reportRequestInstance}
        mockReportRequestService.demand.sendUpdateModeNotification(0..1){ReportRequest reportRequestInstance,def newReportRequestRef,
                                                                         def oldReportRequestRef, def oldComments, def oldActionItems
            -> null}
        controller.reportRequestService = mockReportRequestService.proxyInstance()

        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1) { theInstance -> theInstance }
        controller.CRUDService = mockCRUDService.proxyInstance()

        DateUtil.metaClass.static.getEndDate = { String dateToDatePicker, Locale locale -> return new Date() }
        DateUtil.metaClass.static.getEndDate = { String dateToDatePicker, def timezone, Locale locale -> return new Date() }

        def id1 = normalUser_2.id as String
        def id2 = userGroup_2.id as String
        String requesters = "User_1" // Semicolon-separated values for requesters
        String executors = "User_${id1}" // Executors parameter

        when:
        request.method = 'POST'
        request.contentType = FORM_CONTENT_TYPE
        params['id'] = reportRequest.id
        params['linksToDelete'] = ""
        params['linksToAdd'] = ""
        params['attachmentsToDelete'] = ""
        params['reportName'] = "report_1"
        params['reportRequestType'] = reportRequestType
        params['priority'] = reportRequestPriority
        params['description'] = "description"
        params['dueDate'] = "10-Mar-2016"
        params['asOfVersionDate'] = ""
        params['endDate'] = ""
        params['completionDate'] = ""
        params['startDate'] = ""
        params['dateRangeType'] = dateRangeType
        params['createdBy'] = "user"
        params['modifiedBy'] = "user"
        params['productSelection'] = "normalstring"
        params['productGroupSelection'] = "normalstring"
        params['studySelection'] = "normalstring"
        params['eventGroupSelection'] = "normalstring"
        params['eventSelection'] = "normalstring"
        params['evaluateDateAs'] = EvaluateCaseDateEnum.VERSION_ASOF
        params['includeLockedVersion'] = true
        params['assignedTo'] = executors
        params['requesters'] = requesters // Updated with semicolon-separated values
        params["comments[0]"] = "true"
        params["comments[0].newObj"] = "true"
        params["comments[0].deleted"] = "false"
        params["comments[0].reportComment"] = "comment"
        params["comments[0].dateCreated"] = ""
        params["comments[0].id"] = "1"
        params["actionItems[0]"] = "true"
        params["actionItems[0].assignedTo"] = executors
        params["actionItems[0].actionCategory"] = "xyz"
        params["actionItems[0].priority"] = "priority"
        params["actionItems[0].status"] = StatusEnum.OPEN
        params["actionItems[0].appType"] = AppTypeEnum.QUALITY_MODULE
        params["actionItems[0].description"] = "newactionitem"
        params["actionItems[0].newObj"] = "true"
        params["actionItems[0].completionDateObj"] = ""
        params["actionItems[0].dateCreatedObj"] = ""
        params["actionItems[0].id"] = "1"
        params["actionItems[0].dueDateObj"] = "20-Mar-/2016"
        params["actionItems[0].deleted"] = "false"
        controller.update()
        then:
        flash.message == 'default.updated.message'
        response.redirectUrl == '/reportRequest/index'
    }

    void "test update Validation error"(){
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user',defaultRRAssignTo: true)
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        WorkflowState.metaClass.static.getFinalStatesForType = {WorkflowConfigurationTypeEnum type -> workflowStateInstance}
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.workflowState = workflowStateInstance
        reportRequest.save(failOnError:true)
        Date now=new Date()
        ActionItemCategory actionItemCategory=new ActionItemCategory(name: "abc",key: "xyz")
        actionItemCategory.save(failOnError:true,flush:true)
        ActionItem actionItem=new ActionItem(actionCategory:actionItemCategory ,description: "dsafadfs",modifiedBy: "admin",createdBy: "admin",assignedTo: normalUser_1
                ,dueDate: now ,completionDate: now+30,priority: "priority",status: StatusEnum.OPEN ,appType: AppTypeEnum.QUALITY_MODULE)
        actionItem.dueDate=new Date()
        actionItem.save(failOnError:true,flush:true)
        def mockUserService = Mock(UserService)
        mockUserService.isAnyGranted(_) >> { return true}
        mockUserService.getCurrentUser() >> { return normalUser_1}
        mockUserService.isCurrentUserAdmin() >> { return true}
        controller.userService = mockUserService
        def mockReportRequestService = Mock(ReportRequestService)
        mockReportRequestService.getReportRequestMap(_) >> [:]
        mockReportRequestService.getReportComments(_) >> []
        mockReportRequestService.getAttachments(_) >> []
        mockReportRequestService.getReportActionItems(_) >> []
        mockReportRequestService.update(_) >> { it[0] }
        mockReportRequestService.sendUpdateModeNotification(_, _, _, _, _) >> null
        controller.reportRequestService = mockReportRequestService
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance -> theInstance}
        controller.CRUDService = mockCRUDService.proxyInstance()
        DateUtil.metaClass.static.getEndDate = {String dateToDatePicker, Locale locale -> return  new Date()}
        DateUtil.metaClass.static.getEndDate = {String dateToDatePicker, def timezone, Locale locale -> return  new Date()}
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        String requesters="User_${id1}"
        String executors = "User_${id1}"
        when:
        request.method = 'POST'
        request.contentType = FORM_CONTENT_TYPE
        params['id'] = reportRequest.id
        params['linksToDelete'] = ""
        params['linksToAdd'] = ""
        params['attachmentsToDelete'] = ""
        params['reportName']= "report_1"
        params['reportRequestType']= reportRequestType
        params['priority']= reportRequestPriority
        params['description']= "description"
        params['dueDate'] = "10-Mar-2016"
        params['asOfVersionDate']= ""
        params['endDate']= ""
        params['completionDate']= ""
        params['startDate']= ""
        params['dateRangeType'] =  dateRangeType
        params['createdBy'] = "user"
        params['modifiedBy'] = "user"
        params['productSelection'] = "normalstring"
        params['productGroupSelection'] = "normalstring"
        params['studySelection'] = "normalstring"
        params['eventGroupSelection'] = "normalstring"
        params['eventSelection'] = "normalstring"
        params['evaluateDateAs'] = EvaluateCaseDateEnum.VERSION_ASOF
        params['includeLockedVersion'] = true
        params['assignedTo'] = executors
        params['requesters'] = requesters
        params.("comments[0]") = "true"
        params.("comments[0].newObj") = "true"
        params.("comments[0].deleted") ="false"
        params.("comments[0].reportComment") = "comment"
        params.("comments[0].dateCreated") = ""
        params.("comments[0].id") = "1"
        params.("actionItems[0]") = "true"
        params.("actionItems[0].assignedTo") = executors
        params.("actionItems[0].actionCategory") = "xyz"
        params.("actionItems[0].priority") = "priority"
        params.("actionItems[0].status") = StatusEnum.OPEN
        params.("actionItems[0].appType") = AppTypeEnum.QUALITY_MODULE
        params.("actionItems[0].description") = "newactionitem"
        params.("actionItems[0].newObj") = "true"
        params.("actionItems[0].completionDateObj") = ""
        params.("actionItems[0].dateCreatedObj") = ""
        params.("actionItems[0].id") = "1"
        params.("actionItems[0].dueDateObj") = "20-Mar-/2016"
        params.("actionItems[0].deleted") = "false"
        controller.update()
        then:
        flash.message == "default.updated.message"
        view == '/reportRequest/update.gsp'
    }

    void "test update Exception error"(){
        User normalUser_1 = makeNormalUser("user2", [])
        User normalUser_2 = makeNormalUser("user3", [])
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user',defaultRRAssignTo: true)
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup.save(failOnError:true)
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        WorkflowState.metaClass.static.getFinalStatesForType = {WorkflowConfigurationTypeEnum type -> workflowStateInstance}
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.workflowState = workflowStateInstance
        reportRequest.save(failOnError:true)
        Date now=new Date()
        ActionItemCategory actionItemCategory=new ActionItemCategory(name: "abc",key: "xyz")
        actionItemCategory.save(failOnError:true,flush:true)
        ActionItem actionItem=new ActionItem(actionCategory:actionItemCategory ,description: "dsafadfs",modifiedBy: "admin",createdBy: "admin",assignedTo: normalUser_1
                ,dueDate: now ,completionDate: now+30,priority: "priority",status: StatusEnum.OPEN ,appType: AppTypeEnum.QUALITY_MODULE)
        actionItem.dueDate=new Date()
        actionItem.save(failOnError:true,flush:true)
        def mockUserService = Mock(UserService)
        mockUserService.isAnyGranted(_) >> { return true}
        mockUserService.getCurrentUser() >> { return normalUser_1}
        mockUserService.isCurrentUserAdmin() >> { return true}
        controller.userService = mockUserService
        def mockReportRequestService = Mock(ReportRequestService)
        mockReportRequestService.getReportRequestMap(_) >> [:]
        mockReportRequestService.getReportComments(_) >> []
        mockReportRequestService.getAttachments(_) >> []
        mockReportRequestService.getReportActionItems(_) >> []
        mockReportRequestService.update(_) >> { it[0] }
        mockReportRequestService.sendUpdateModeNotification(_, _, _, _, _) >> null
        controller.reportRequestService = mockReportRequestService
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance -> theInstance}
        controller.CRUDService = mockCRUDService.proxyInstance()
        DateUtil.metaClass.static.getEndDate = {String dateToDatePicker, Locale locale -> return  new Date()}
        DateUtil.metaClass.static.getEndDate = {String dateToDatePicker, def timezone, Locale locale -> return  new Date()}
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        String requesters="User_${id1}"
        String executors = "User_${id1}"
        when:
        request.method = 'POST'
        request.contentType = FORM_CONTENT_TYPE
        params['id'] = reportRequest.id
        params['linksToDelete'] = ""
        params['linksToAdd'] = ""
        params['attachmentsToDelete'] = ""
        params['reportName']= "report_1"
        params['reportRequestType']= reportRequestType
        params['priority']= reportRequestPriority
        params['description']= "description"
        params['dueDate'] = "10-Mar-2016"
        params['asOfVersionDate']= ""
        params['endDate']= ""
        params['completionDate']= ""
        params['startDate']= ""
        params['dateRangeType'] =  dateRangeType
        params['createdBy'] = "user"
        params['modifiedBy'] = "user"
        params['productSelection'] = "normalstring"
        params['productGroupSelection'] = "normalstring"
        params['studySelection'] = "normalstring"
        params['eventGroupSelection'] = "normalstring"
        params['eventSelection'] = "normalstring"
        params['evaluateDateAs'] = EvaluateCaseDateEnum.VERSION_ASOF
        params['includeLockedVersion'] = true
        params['assignedTo'] = executors
        params['requesters'] = requesters
        params.("comments[0]") = "true"
        params.("comments[0].newObj") = "true"
        params.("comments[0].deleted") ="false"
        params.("comments[0].reportComment") = "comment"
        params.("comments[0].dateCreated") = ""
        params.("comments[0].id") = "1"
        params.("actionItems[0]") = "true"
        params.("actionItems[0].assignedTo") = executors
        params.("actionItems[0].actionCategory") = "xyz"
        params.("actionItems[0].priority") = "priority"
        params.("actionItems[0].status") = StatusEnum.OPEN
        params.("actionItems[0].appType") = AppTypeEnum.QUALITY_MODULE
        params.("actionItems[0].description") = "newactionitem"
        params.("actionItems[0].newObj") = "true"
        params.("actionItems[0].completionDateObj") = ""
        params.("actionItems[0].dateCreatedObj") = ""
        params.("actionItems[0].id") = "1"
        params.("actionItems[0].dueDateObj") = "20-Mar-/2016"
        params.("actionItems[0].deleted") = "false"
        controller.update()
        then:
        flash.message == "default.updated.message"
        view == '/reportRequest/update.gsp'
    }

    void "test update null instance"(){
        def mockReportRequestService = new MockFor(ReportRequestService)
        mockReportRequestService.demand.getReportRequestMap(0..1){reportRequestInstance -> [:]}
        mockReportRequestService.demand.getReportActionItems(0..1){reportRequestInstance -> []}
        controller.reportRequestService = mockReportRequestService.proxyInstance()
        when:
        request.method = 'POST'
        request.contentType = FORM_CONTENT_TYPE
        params['id'] = 3
        controller.update()
        then:
        flash.message == null
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/reportRequest/index'
    }

    void "test update GET"(){
        when:
        controller.update()
        then:
        flash.error == 'default.not.saved.message'
        response.redirectUrl == '/reportRequest/index'
    }

    void "test edit"(){
        User normalUser_1 = makeNormalUser("user2", [])
        def adminUser = makeAdminUser()
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        WorkflowState.metaClass.static.getFinalStatesForType = {WorkflowConfigurationTypeEnum type -> workflowStateInstance}
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.workflowState = workflowStateInstance
        reportRequest.save(failOnError:true)
        WorkflowState.metaClass.static.getFinalStatesForType = {WorkflowConfigurationTypeEnum type -> null}
        def mockUserService = Mock(UserService)
        mockUserService.isAnyGranted(_) >> { return true}
        mockUserService.getCurrentUser() >> { return normalUser_1}
        mockUserService.isCurrentUserAdmin() >> { return true}
        controller.userService = mockUserService
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteNotificationByExecutionStatusId(0..1){ User user, Long executionStatusId, NotificationApp appName
            ->  null }
        controller.notificationService = mockNotificationService.proxyInstance()
        when:
        controller.edit(reportRequest)
        then:
        view == '/reportRequest/edit'
        model.size() == 12
    }

    void "test edit not found"(){
        when:
        controller.edit(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/reportRequest/index'
    }

    void "test show"(){
        User normalUser_1 = makeNormalUser("user2", [])
        def adminUser = makeAdminUser()
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.workflowState = workflowStateInstance
        reportRequest.save(failOnError:true)
        WorkflowState.metaClass.static.getFinalStatesForType = {WorkflowConfigurationTypeEnum type -> null}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){ -> adminUser }
        mockUserService.demand.isCurrentUserAdmin(0..1){ -> true}
        controller.userService = mockUserService.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteNotificationByExecutionStatusId(0..1){ User user, Long executionStatusId, NotificationApp appName
            ->  null }
        controller.notificationService = mockNotificationService.proxyInstance()
        when:
        controller.show(reportRequest)
        then:
        view == '/reportRequest/show'
        model.size() == 7
    }

    void "test show not found"(){
        when:
        controller.show(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/reportRequest/index'
    }

    void "test delete"(){
        User normalUser_1 = makeNormalUser("user2", [])
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.save(failOnError:true)
        def mockReportRequestService = new MockFor(ReportRequestService)
        mockReportRequestService.demand.delete(0..1){reportRequestInstance, String justification -> reportRequestInstance}
        mockReportRequestService.demand.sendDeleteNotification(0..1){ReportRequest reportRequestInstance -> null}
        mockReportRequestService.demand.sendDeleteEmailNotification(0..1){ReportRequest reportRequestInstance -> null}
        def mockUserService = new MockFor(UserService)
        def adminUser = makeAdminUser()
        mockUserService.demand.getCurrentUser(0..1){ -> adminUser }
        mockUserService.demand.isCurrentUserAdmin(0..1){ -> true}
        controller.userService = mockUserService.proxyInstance()
        controller.reportRequestService = mockReportRequestService.proxyInstance()
        when:
        request.method = 'POST'
        params['deleteJustification'] = ""
        controller.delete(reportRequest)
        then:
        flash.message == "default.deleted.message"
        response.redirectUrl == '/reportRequest/index'
    }

    void "test delete validation error"(){
        User normalUser_1 = makeNormalUser("user2", [])
        ReportRequestPriority reportRequestPriority = new ReportRequestPriority(name: "priority",createdBy: "user",modifiedBy: "user")
        reportRequestPriority.save(failOnError:true)
        ReportRequestType reportRequestType = new ReportRequestType(name: "type",createdBy: "user",modifiedBy: "user")
        reportRequestType.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name:"date")
        dateRangeType.save(failOnError:true)
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        ReportRequest reportRequest = new ReportRequest(reportName: "report_1",reportRequestType: reportRequestType,priority: reportRequestPriority
                ,description: "description",dueDate: new Date(),asOfVersionDate: new Date(),dateRangeType: dateRangeType,
                owner:normalUser_1,createdBy: "user",modifiedBy: "user",productSelection: "normalstring",
                productGroupSelection: "normalstring",studySelection: "normalstring",eventGroupSelection: "normalstring",
                eventSelection: "normalstring",assignedTo: normalUser_1,assignedGroupTo: userGroup_1)
        reportRequest.dueDate = new Date()
        reportRequest.requesterGroups = [userGroup_1]
        reportRequest.requesters=[normalUser_1]
        reportRequest.save(failOnError:true)
        def mockReportRequestService = new MockFor(ReportRequestService)
        mockReportRequestService.demand.delete(0..1){reportRequestInstance, String justification -> throw new grails.validation.ValidationException("message",new ValidationErrors(new Object()))}
        mockReportRequestService.demand.sendDeleteNotification(0..1){ReportRequest reportRequestInstance -> null}
        mockReportRequestService.demand.sendDeleteEmailNotification(0..1){ReportRequest reportRequestInstance -> null}
        def mockUserService = new MockFor(UserService)
        def adminUser = makeAdminUser()
        mockUserService.demand.getCurrentUser(0..1){ -> adminUser }
        mockUserService.demand.isCurrentUserAdmin(0..1){ -> true}
        controller.userService = mockUserService.proxyInstance()
        controller.reportRequestService = mockReportRequestService.proxyInstance()
        when:
        request.method = 'POST'
        params['deleteJustification'] = ""
        controller.delete(reportRequest)
        then:
        flash.message == null
        flash.error == "Unable to delete the report request"
        response.redirectUrl == '/reportRequest/index'
    }

    void "test delete not found"(){
        when:
        request.method = 'POST'
        controller.delete(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/reportRequest/index'
    }

    void "test findTasks"(){
        TaskTemplate taskTemplate = new TaskTemplate(name: "template",createdBy: "user",modifiedBy: "user",type: TaskTemplateTypeEnum.REPORT_REQUEST)
        Task task = new Task(taskName: "task_1",createdBy: "user",modifiedBy: "user",dueDate: 10,priority: "priority",taskTemplate: taskTemplate)
        task.save(failOnError:true)
        taskTemplate.tasks = [task]
        taskTemplate.save(failOnError:true)
        def mockReportRequestService = new MockFor(ReportRequestService)
        mockReportRequestService.demand.findTasks(0..1){taskTemplateId ->
            def tasks = {}
            if (taskTemplateId) {
                def newTaskTemplate = TaskTemplate.get(taskTemplateId)
                if (newTaskTemplate) {
                    tasks = Task.findAllByTaskTemplate(newTaskTemplate)?.collect {
                        it.toTaskDto()
                    }
                }
            }
            tasks
        }
        controller.reportRequestService = mockReportRequestService.proxyInstance()
        when:
        params['taskTemplateId'] = taskTemplate.id
        controller.findTasks()
        then:
        response.json.taskName == ["task_1"]
    }

    void "test bindLinks"(){
        int run = 0
        ReportRequestLink reportRequestLink = new ReportRequestLink()
        reportRequestLink.save(failOnError:true,validate: false)
        ReportRequest reportRequest = new ReportRequest()
        reportRequest.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null->
            run++
            theInstance
        }
        mockCRUDService.demand.save(0..1){theInstance ->
            run++
            theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params['linksToDelete'] = "1"
        params['linksToAdd'] = '[{"createdBy":"John","modifiedBy":"user"}]'
        controller.bindLinks(reportRequest)
        then:
        run == 2
    }
    void "test exportToExcel"() {
        given:
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest(reportName: "report",assignedTo: normalUser,description: "description",requesters: [normalUser],requesterGroups: [new UserGroup()],dueDate: new Date(),priority: new ReportRequestPriority(name: "priority"),workflowState: new WorkflowState(name: "status"),createdBy: "user",reportRequestType: new ReportRequestType(name: "reportRequestType"),productSelection: "selection",eventSelection: "selection")
        reportRequest.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        ReportRequest.metaClass.static.fetchByFilter = { LibraryFilter filter, String sortBy = null, String sortDirection = "asc" -> new Object(){
            List list(Object o){
                return [[reportRequest.id]]
            }
        }
        }
        controller.qualityService = new QualityService()
        def resultData
        controller.qualityService.metaClass.exportToExcel = { data, metadata ->
            resultData = data
            new byte[0]
        }
        when:
        controller.exportToExcel()
        then:
        resultData.size() == 1
    }
}