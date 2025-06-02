package com.rxlogix

import com.reports.PvReportsTagLib
import com.rxlogix.config.*
import com.rxlogix.enums.AppTypeEnum
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.StatusEnum
import com.rxlogix.user.*
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([AuditLogConfigUtil, DateUtil, Tenants])
class ActionItemControllerSpec extends Specification implements DataTest, ControllerUnitTest<ActionItemController>{

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference, ActionItem, ActionItemCategory, ExecutedPeriodicReportConfiguration, ExecutedReportConfiguration, QualitySampling, Capa8D, ReportRequest
        mockTagLib(PvReportsTagLib)
        AuditLogConfigUtil.metaClass.static.logChanges = { domain, Map newMap, Map oldMap, String eventName, String extraValue -> }
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

    void "test bindAssignedTo userGroup"(){
        UserGroup userGroup = new UserGroup(name: "userGroup_1")
        userGroup.save(failOnError:true,validate:false)
        ActionItem actionItem = new ActionItem()
        actionItem.save(failOnError:true,validate:false)
        when:
        params.assignedTo = "UserGroup_${userGroup.id}"
        controller.bindAssignedTo(actionItem)
        then:
        actionItem.assignedGroupTo == userGroup
        actionItem.assignedTo == null
    }

    void "test bindAssignedTo user"(){
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem()
        actionItem.save(failOnError:true,validate:false)
        when:
        params.assignedTo = "User_${normalUser.id}"
        controller.bindAssignedTo(actionItem)
        then:
        actionItem.assignedGroupTo == null
        actionItem.assignedTo == normalUser
    }

    void "test bindAssignedTo null"(){
        ActionItem actionItem = new ActionItem()
        actionItem.save(failOnError:true,validate:false)
        when:
        params.assignedTo = ""
        controller.bindAssignedTo(actionItem)
        then:
        actionItem.assignedGroupTo == null
        actionItem.assignedTo == null
    }

    void "test save success"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "userGroup_1")
        userGroup.save(failOnError:true,validate:false)
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "actionItemCategory")
        actionItemCategory.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            run++
            theInstance.save(validate:false,flush:true)
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockActionItemService = new MockFor(ActionItemService)
        mockActionItemService.demand.sendActionItemNotification(0..1){ActionItem actionItem, String mode,Object oldActionItemRef,Object emailSubject,String[] ccRecipients  ->
            run++
        }
        controller.actionItemService = mockActionItemService.proxyInstance()
        DateUtil.metaClass.static.parseDateWithLocaleAndTimeZone = {String date, String format, Locale locale, String timeZone ->
            run++
            return new Date()
        }
        when:
        request.method = 'POST'
        params.dueDate = "20-Mar-2021"
        params.completionDate = "20-Mar-2021"
        params.dateCreated = ""
        params.actionCategory = "actionItemCategory"
        params.assignedTo = "UserGroup_${userGroup.id}"
        params.status = StatusEnum.IN_PROGRESS
        params.appType = AppTypeEnum.ADHOC_REPORT
        controller.save()
        then:
        ActionItem.count() == 1
        run == 3
        response.json.status == 200
    }

    void "test save success with executedReportConfiguration"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "userGroup_1")
        userGroup.save(failOnError:true,validate:false)
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "actionItemCategory")
        actionItemCategory.save(failOnError:true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            run++
            theInstance.save(validate:false,flush:true)
            return theInstance
        }
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..2){theInstance ->
            run++
            theInstance.save(validate:false,flush:true)
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockActionItemService = new MockFor(ActionItemService)
        mockActionItemService.demand.sendActionItemNotification(0..1){ActionItem actionItem, String mode,Object oldActionItemRef,Object emailSubject,String[] ccRecipients  ->
            run++
        }
        controller.actionItemService = mockActionItemService.proxyInstance()
        DateUtil.metaClass.static.parseDateWithLocaleAndTimeZone = {String date, String format, Locale locale, String timeZone ->
            run++
            return new Date()
        }
        when:
        request.method = 'POST'
        params.dueDate = "20-Mar-2021"
        params.completionDate = "20-Mar-2021"
        params.dateCreated = ""
        params.actionCategory = "actionItemCategory"
        params.assignedTo = "UserGroup_${userGroup.id}"
        params.status = StatusEnum.CLOSED
        params.appType = AppTypeEnum.ADHOC_REPORT
        params.executedReportId = executedReportConfiguration.id
        controller.save()
        then:
        ActionItem.count() == 1
        run == 4
        response.json.status == 200
    }
    
    void "test save success with QUALITY_MODULE_CAPA key QUALITY_MODULE_CORRECTIVE"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "userGroup_1")
        userGroup.save(failOnError:true,validate:false)
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "QUALITY_MODULE_CORRECTIVE")
        actionItemCategory.save(failOnError:true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        Capa8D capa8D = new Capa8D()
        capa8D.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            run++
            theInstance.save(validate:false,flush:true)
            return theInstance
        }
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..2){theInstance ->
            run++
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockActionItemService = new MockFor(ActionItemService)
        mockActionItemService.demand.sendActionItemNotification(0..1){ActionItem actionItem, String mode,Object oldActionItemRef,Object emailSubject,String[] ccRecipients  ->
            run++
        }
        controller.actionItemService = mockActionItemService.proxyInstance()
        DateUtil.metaClass.static.parseDateWithLocaleAndTimeZone = {String date, String format, Locale locale, String timeZone ->
            run++
            return new Date()
        }
        Tenants.metaClass.static.currentId = { -> return 1}
        when:
        request.method = 'POST'
        params.dueDate = "20-Mar-2021"
        params.completionDate = "20-Mar-2021"
        params.dateCreated = ""
        params.actionCategory = "QUALITY_MODULE_CORRECTIVE"
        params.assignedTo = "UserGroup_${userGroup.id}"
        params.status = StatusEnum.CLOSED
        params.appType = AppTypeEnum.QUALITY_MODULE_CAPA
        params.capaId = capa8D.id
        controller.save()
        then:
        ActionItem.count() == 1
        run == 4
        response.json.status == 200
        capa8D.correctiveActions.size() == 1
    }

    void "test save success with QUALITY_MODULE_CAPA key QUALITY_MODULE_PREVENTIVE"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "userGroup_1")
        userGroup.save(failOnError:true,validate:false)
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "QUALITY_MODULE_PREVENTIVE")
        actionItemCategory.save(failOnError:true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        Capa8D capa8D = new Capa8D()
        capa8D.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            run++
            theInstance.save(validate:false,flush:true)
            return theInstance
        }
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..2){theInstance ->
            run++
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockActionItemService = new MockFor(ActionItemService)
        mockActionItemService.demand.sendActionItemNotification(0..1){ActionItem actionItem, String mode,Object oldActionItemRef,Object emailSubject ->
            run++
        }
        controller.actionItemService = mockActionItemService.proxyInstance()
        DateUtil.metaClass.static.parseDateWithLocaleAndTimeZone = {String date, String format, Locale locale, String timeZone ->
            run++
            return new Date()
        }
        Tenants.metaClass.static.currentId = { -> return 1}
        when:
        request.method = 'POST'
        params.dueDate = "20-Mar-2021"
        params.completionDate = "20-Mar-2021"
        params.dateCreated = ""
        params.actionCategory = "QUALITY_MODULE_PREVENTIVE"
        params.assignedTo = "UserGroup_${userGroup.id}"
        params.status = StatusEnum.CLOSED
        params.appType = AppTypeEnum.QUALITY_MODULE_CAPA
        params.capaId = capa8D.id
        controller.save()
        then:
        ActionItem.count() == 1
        run == 4
        response.json.status == 200
        capa8D.preventiveActions.size() == 1
    }

    void "test save validation error"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "userGroup_1")
        userGroup.save(failOnError:true,validate:false)
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "actionItemCategory")
        actionItemCategory.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockActionItemService = new MockFor(ActionItemService)
        mockActionItemService.demand.sendActionItemNotification(0..1){ActionItem actionItem, String mode,Object oldActionItemRef,Object emailSubject,String[] ccRecipients  ->
            run++
        }
        controller.actionItemService = mockActionItemService.proxyInstance()
        DateUtil.metaClass.static.parseDateWithLocaleAndTimeZone = {String date, String format, Locale locale, String timeZone ->
            run++
            return new Date()
        }
        when:
        request.method = 'POST'
        params.dueDate = "20-Mar-2021"
        params.completionDate = "20-Mar-2021"
        params.dateCreated = ""
        params.actionCategory = "actionItemCategory"
        params.assignedTo = "UserGroup_${userGroup.id}"
        params.status = StatusEnum.IN_PROGRESS
        params.appType = AppTypeEnum.ADHOC_REPORT
        controller.save()
        then:
        ActionItem.count() == 0
        run == 1
    }

    void "test view"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(description   : "description",
                actionCategory: new ActionItemCategory(key: "actionItemCategory",name: "actionItem"),
                assignedTo    : normalUser,
                dateCreated   : new Date(),
                priority      : "priority",
                status        : StatusEnum.IN_PROGRESS,
                createdBy     : "user",
                appType       : AppTypeEnum.ADHOC_REPORT,
                configuration : new PeriodicReportConfiguration(reportName: "report_3"))
        actionItem.dueDate = new Date()
        actionItem.completionDate = new Date()
        actionItem.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(actionItems: [actionItem],reportName: "report_1")
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportRequest reportRequest = new ReportRequest(actionItems: [actionItem],reportName: "report_2")
        reportRequest.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteNotificationByExecutionStatusId(0..1){User userObj, Long executionStatusIdVal, NotificationApp appNameVal->
            run = true
        }
        controller.notificationService = mockNotificationService.proxyInstance()
        ViewHelper.metaClass.static.getMessage = {String code, Object[] params = null, String defaultLabel='' -> code}
        when:
        params.actionItemId = actionItem.id
        controller.view()
        then:
        run == true
        response.status == 200
        response.json.size() == 26
        response.json.associatedCaseNumber == ""
    }

    void "test view QUALITY_MODULE"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(description   : "description",
                actionCategory: new ActionItemCategory(key: "actionItemCategory",name: "actionItem"),
                assignedTo    : normalUser,
                dateCreated   : new Date(),
                priority      : "priority",
                status        : StatusEnum.IN_PROGRESS,
                createdBy     : "user",
                appType       : AppTypeEnum.QUALITY_MODULE,
                configuration : new PeriodicReportConfiguration(reportName: "report_3"))
        actionItem.dueDate = new Date()
        actionItem.completionDate = new Date()
        actionItem.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(actionItems: [actionItem],reportName: "report_1")
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportRequest reportRequest = new ReportRequest(actionItems: [actionItem],reportName: "report_2")
        reportRequest.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteNotificationByExecutionStatusId(0..1){User userObj, Long executionStatusIdVal, NotificationApp appNameVal->
            run++
        }
        controller.notificationService = mockNotificationService.proxyInstance()
        def mockQualityService = new MockFor(QualityService)
        mockQualityService.demand.getCaseNoByActionItemId(0..1){Long actionItemId ->
            run++
            return ["masterCaseNum":"1234"]
        }
        controller.qualityService = mockQualityService.proxyInstance()
        ViewHelper.metaClass.static.getMessage = {String code, Object[] params = null, String defaultLabel='' -> code}
        Tenants.metaClass.static.currentId = { -> return 1}
        when:
        params.actionItemId = actionItem.id
        controller.view()
        then:
        run == 2
        response.status == 200
        response.json.size() == 26
        response.json.associatedCaseNumber == "1234"
    }

    void "test view QUALITY_MODULE_CAPA"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(description   : "description",
                actionCategory: new ActionItemCategory(key: "actionItemCategory",name: "actionItem"),
                assignedTo    : normalUser,
                dateCreated   : new Date(),
                priority      : "priority",
                status        : StatusEnum.IN_PROGRESS,
                createdBy     : "user",
                appType       : AppTypeEnum.QUALITY_MODULE_CAPA,
                configuration : new PeriodicReportConfiguration(reportName: "report_3"))
        actionItem.dueDate = new Date()
        actionItem.completionDate = new Date()
        actionItem.save(failOnError:true,validate:false)
        Capa8D capa8D = new Capa8D(correctiveActions: [actionItem],preventiveActions: [actionItem])
        capa8D.save(failOnError:true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(actionItems: [actionItem],reportName: "report_1")
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportRequest reportRequest = new ReportRequest(actionItems: [actionItem],reportName: "report_2")
        reportRequest.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteNotificationByExecutionStatusId(0..1){User userObj, Long executionStatusIdVal, NotificationApp appNameVal->
            run++
        }
        controller.notificationService = mockNotificationService.proxyInstance()
        def mockQualityService = new MockFor(QualityService)
        mockQualityService.demand.getCapa(0..1){Long actionItemId ->
            run++
            return ["associatedIssueNumber":"1234","associatedIssueId":45]
        }
        controller.qualityService = mockQualityService.proxyInstance()
        ViewHelper.metaClass.static.getMessage = {String code, Object[] params = null, String defaultLabel='' -> code}
        Tenants.metaClass.static.currentId = { -> return 1}
        Capa8D.metaClass.static.capasByActionItem = { actionItemId ->
            new Object() {
                List list(Object o) {
                    return [capa8D.id]
                }
            }
        }
        when:
        params.actionItemId = actionItem.id
        controller.view()
        then:
        run == 2
        response.status == 200
        response.json.size() == 26
        response.json.associatedCaseNumber == ""
    }

    void "test update success"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(description   : "description",
                actionCategory: new ActionItemCategory(key: "actionItemCategory_1",name: "actionItem"),
                assignedTo    : normalUser,
                dateCreated   : new Date(),
                priority      : "priority",
                status        : StatusEnum.IN_PROGRESS,
                createdBy     : "user",
                appType       : AppTypeEnum.QUALITY_MODULE_CAPA,
                configuration : new PeriodicReportConfiguration(reportName: "report_3"))
        actionItem.dueDate = new Date()
        actionItem.completionDate = new Date()
        actionItem.save(failOnError:true,validate:false)
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "actionItemCategory")
        actionItemCategory.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockReportRequestService=new MockFor(ReportRequestService)
        mockReportRequestService.demand.getNotificationRecipients(0..1){ReportRequest rptRequest ->
            run++
            return [] as Set<String>
        }
        controller.reportRequestService = mockReportRequestService.proxyInstance()
        def mockActionItemService = new MockFor(ActionItemService)
        mockActionItemService.demand.sendActionItemNotification(0..1){ActionItem actionItemInstance, String mode,Object oldActionItemRef,Object emailSubject,String[] ccRecipients  ->
            run++
        }
        controller.actionItemService = mockActionItemService.proxyInstance()
        PvReportsTagLib.metaClass.actionItemUpdate = { attrs -> return "Action item Updated"}
        when:
        request.method = 'POST'
        params.actionItemId = actionItem.id
        params.description = "new_action_item"
        params.dueDate = "20-Mar-2021"
        params.completionDate = "20-Mar-2021"
        params.status = StatusEnum.CLOSED
        params.actionCategory = "actionItemCategory"
        params.assignedTo = "User_${normalUser.id}"
        params.rptRequestId = 1L
        controller.update()
        then:
        run == 3
        actionItem.description == "new_action_item"
        actionItem.actionCategory.key == "actionItemCategory"
        response.json.status == 200
    }

    void "test update validation exception"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(description   : "description",
                actionCategory: new ActionItemCategory(key: "actionItemCategory_1",name: "actionItem"),
                assignedTo    : normalUser,
                dateCreated   : new Date(),
                priority      : "priority",
                status        : StatusEnum.IN_PROGRESS,
                createdBy     : "user",
                appType       : AppTypeEnum.QUALITY_MODULE_CAPA,
                configuration : new PeriodicReportConfiguration(reportName: "report_3"))
        actionItem.dueDate = new Date()
        actionItem.completionDate = new Date()
        actionItem.save(failOnError:true,validate:false)
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "actionItemCategory")
        actionItemCategory.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockActionItemService = new MockFor(ActionItemService)
        mockActionItemService.demand.sendActionItemNotification(0..1){ActionItem actionItemInstance, String mode,Object oldActionItemRef,Object emailSubject ->
            run++
        }
        controller.actionItemService = mockActionItemService.proxyInstance()
        PvReportsTagLib.metaClass.actionItemUpdate = { attrs -> return "Action item Updated"}
        when:
        request.method = 'POST'
        params.actionItemId = actionItem.id
        params.description = "new_action_item"
        params.dueDate = "20-Mar-2021"
        params.completionDate = "20-Mar-2021"
        params.status = StatusEnum.CLOSED
        params.actionCategory = "actionItemCategory"
        params.assignedTo = "User_${normalUser.id}"
        controller.update()
        then:
        run == 0
    }

    void "test update null actionItem"(){
        ActionItem actionItem = new ActionItem()
        actionItem.save(failOnError:true,validate:false)
        when:
        request.method = 'POST'
        params.actionItemId = 10
        controller.update()
        then:
        response.json.status == 404
        response.json.message == "default.not.found.message"
    }

    void "test update null actionItemId"(){
        ActionItem actionItem = new ActionItem()
        actionItem.save(failOnError:true,validate:false)
        when:
        request.method = 'POST'
        controller.update()
        then:
        response.json.status == 404
        response.json.message == "actionItemId not found."
    }

    void "test update null aiVersion"(){
        ActionItem actionItem = new ActionItem()
        actionItem.save(failOnError:true,validate:false)
        actionItem.save(failOnError:true,validate:false)
        actionItem.save(failOnError:true,validate:false)
        when:
        request.method = 'POST'
        params.actionItemId = actionItem.id
        params.aiVersion = 1
        controller.update()
        then:
        response.json.status == 409
        response.json.message == "app.configuration.update.lock.permission"
    }

    void "test delete success"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(description   : "description",
                actionCategory: new ActionItemCategory(key: "actionItemCategory_1",name: "actionItem"),
                assignedTo    : normalUser,
                dateCreated   : new Date(),
                priority      : "priority",
                status        : StatusEnum.IN_PROGRESS,
                createdBy     : "user",
                appType       : AppTypeEnum.ADHOC_REPORT,
                configuration : new PeriodicReportConfiguration(reportName: "report_3"))
        actionItem.dueDate = new Date()
        actionItem.completionDate = new Date()
        actionItem.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.actionItemId = actionItem.id
        controller.delete()
        then:
        run == true
        response.json.status == 200
        response.json.message == "success"
    }

    void "test delete success QUALITY_MODULE_CAPA and key QUALITY_MODULE_CORRECTIVE"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "QUALITY_MODULE_CORRECTIVE")
        actionItemCategory.save(failOnError:true,validate:false,flush:true)
        ActionItem actionItem = new ActionItem(description   : "description",
                actionCategory: actionItemCategory,
                assignedTo    : normalUser,
                dateCreated   : new Date(),
                priority      : "priority",
                status        : StatusEnum.IN_PROGRESS,
                createdBy     : "user",
                appType       : AppTypeEnum.QUALITY_MODULE_CAPA,
                configuration : new PeriodicReportConfiguration(reportName: "report_3"))
        actionItem.dueDate = new Date()
        actionItem.completionDate = new Date()
        actionItem.save(failOnError:true,validate:false)
        Capa8D capa8D = new Capa8D(correctiveActions: [actionItem],preventiveActions: [actionItem])
        capa8D.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveOrUpdate(0..1){theInstance, Map saveParams = null ->
            run++
        }
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        Capa8D.metaClass.static.capasByActionItem = {actionItemId -> new Object(){
                List list(Object o){
                    return [capa8D.id]
                }

                def get(Object o){
                    return capa8D.id
                }
            }
        }
        Capa8D.metaClass.static.get = {Long id -> capa8D}
        when:
        request.method = 'POST'
        params.actionItemId = actionItem.id
        controller.delete()
        then:
        run == 2
        response.json.status == 200
        response.json.message == "success"
        capa8D.correctiveActions.size() == 0
        capa8D.preventiveActions.size() == 1
    }

    void "test delete success QUALITY_MODULE_CAPA and key QUALITY_MODULE_PREVENTIVE"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "QUALITY_MODULE_PREVENTIVE")
        actionItemCategory.save(failOnError:true,validate:false,flush:true)
        ActionItem actionItem = new ActionItem(description   : "description",
                actionCategory: actionItemCategory,
                assignedTo    : normalUser,
                dateCreated   : new Date(),
                priority      : "priority",
                status        : StatusEnum.IN_PROGRESS,
                createdBy     : "user",
                appType       : AppTypeEnum.QUALITY_MODULE_CAPA,
                configuration : new PeriodicReportConfiguration(reportName: "report_3"))
        actionItem.dueDate = new Date()
        actionItem.completionDate = new Date()
        actionItem.save(failOnError:true,validate:false)
        Capa8D capa8D = new Capa8D(preventiveActions: [actionItem],correctiveActions: [actionItem])
        capa8D.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveOrUpdate(0..1){theInstance, Map saveParams = null ->
            run++
        }
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        Capa8D.metaClass.static.capasByActionItem = {actionItemId -> new Object(){
                List list(Object o){
                    return [capa8D.id]
                }

                def get(Object o){
                    return capa8D.id
                }
            }
        }
        when:
        request.method = 'POST'
        params.actionItemId = actionItem.id
        controller.delete()
        then:
        run == 2
        response.json.status == 200
        response.json.message == "success"
        capa8D.preventiveActions.size() == 1
        capa8D.correctiveActions.size() == 1
    }

    void "test delete validation exception"(){
        User normalUser = makeNormalUser("user",[])
        ActionItemCategory actionItemCategory = new ActionItemCategory(key: "QUALITY_MODULE_PREVENTIVE")
        actionItemCategory.save(failOnError:true,validate:false,flush:true)
        ActionItem actionItem = new ActionItem(description   : "description",
                actionCategory: actionItemCategory,
                assignedTo    : normalUser,
                dateCreated   : new Date(),
                priority      : "priority",
                status        : StatusEnum.IN_PROGRESS,
                createdBy     : "user",
                appType       : AppTypeEnum.ADHOC_REPORT,
                configuration : new PeriodicReportConfiguration(reportName: "report_3"))
        actionItem.dueDate = new Date()
        actionItem.completionDate = new Date()
        actionItem.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.actionItemId = actionItem.id
        controller.delete()
        then:
        response.json.status == 404
        response.json.message == "app.notification.action.item.delete.unable"
    }

    void "test delete null actionItem"(){
        ActionItem actionItem = new ActionItem()
        actionItem.save(failOnError:true,validate:false)
        when:
        request.method = 'POST'
        params.actionItemId = 10
        controller.delete()
        then:
        response.json.status == 404
        response.json.message == "default.not.found.message"
    }

    void "test delete null actionItemId"(){
        ActionItem actionItem = new ActionItem()
        actionItem.save(failOnError:true,validate:false)
        when:
        request.method = 'POST'
        controller.delete()
        then:
        response.json.status == 404
        response.json.message == "actionItemId not found."
    }
    void "test exportToExcel"(){
        given:
        User normalUser = makeNormalUser("user",[])
        def actionCategory = new ActionItemCategory(key: "actionItemCategory_1",name: "actionItem1")
        ActionItem actionItem = new ActionItem(description   : "description",
                actionCategory: actionCategory,
                assignedTo    : normalUser,
                dateCreated   : new Date(),
                priority      : "priority",
                status        : StatusEnum.IN_PROGRESS,
                createdBy     : "user",
                appType       : AppTypeEnum.ADHOC_REPORT,
                configuration : new PeriodicReportConfiguration(reportName: "report_3"))
        actionItem.dueDate = new Date()
        actionItem.completionDate = new Date()
        actionItem.save(failOnError:true,validate:false)


        actionItem.save(failOnError: true, validate: false,flush:true)
        def mockUserService = Mock(UserService)
        mockUserService.getCurrentUser() >> {return normalUser}
        controller.userService = mockUserService
        ActionItem.metaClass.static.fetchActionItemsBySearchString = { LibraryFilter filter, String filterType, User user, Long executedReportId, Long sectionId, Long publisherId, Boolean pvq = false -> new Object() {
            List list(Object o){
                return [[actionItem.id]]
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
        params.searchString = ""
        params.max = 10
        params.offset = 0
        params.order = ""
        params.executedReportId = 1
        params.pvq = "false"
        params.sort = "assignedTo"
        params.singleActionItemId = "1"
        controller.exportToExcelForAI()
        then:
        response.status == 200
        resultData.size() == 1
        resultData[0][0] == actionCategory
        resultData[0][1] == normalUser
        resultData[0][2] == 'description'
    }
}
