package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.user.*
import com.rxlogix.util.ViewHelper
import grails.gsp.PageRenderer
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import org.grails.web.util.WebUtils
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.servlet.http.HttpServletRequest

@ConfineMetaClassChanges([User, WebUtils])
class ReportRequestServiceSpec extends Specification implements DataTest, ServiceUnitTest<ReportRequestService> {

    def setupSpec() {
        mockDomains ReportRequest, ReportRequestPriority, WorkflowState, ReportRequestAttachment, ReportRequestComment, ActionItem, User, UserGroup, UserRole, Preference, Role, Tenant, ActionItemCategory, AIEmailPreference, ReportRequestEmailPreference
    }

    private User makeNormalUser(name, team) {
        User.metaClass.encodePassword = { "password" }
        AIEmailPreference aiEmailPreference = new AIEmailPreference(creationEmails: true, updateEmails: true, jobEmails: true)
        ReportRequestEmailPreference reportRequestEmailPreference = new ReportRequestEmailPreference(creationEmails: true, updateEmails: true, deleteEmails: true, workflowUpdate: true)
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user",timeZone:"UK", actionItemEmail: aiEmailPreference, reportRequestEmail: reportRequestEmailPreference )
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user",email: "abc@gmail.com")
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

    def setup() {
    }

    def cleanup() {
    }

    void "test copy"() {
        when:
        Date d = new Date()
        def crudServiceMock = new MockFor(CRUDService)

        crudServiceMock.demand.save { theInstance -> theInstance }
        service.CRUDService = crudServiceMock.proxyInstance()

        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(1..1) { -> return new User(username: "owner") }
        service.userService = userMock.proxyInstance()

        ReportRequest reportRequest = new ReportRequest(reportName: "test",
                description: "test",
                priority: new ReportRequestPriority(name:"test"),
                assignedTo: new User(username: "test"),
                assignedGroupTo: new UserGroup(name: "test"),
                dueDate: d,
                asOfVersionDate: d,
                owner: new User(username: "test"),
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                excludeFollowUp: true,
                includeLockedVersion: true,
                includeAllStudyDrugsCases: true,
                excludeNonValidCases: true,
                excludeDeletedCases: true,
                suspectProduct: true,
                productSelection: "test",
                studySelection: "test",
                eventSelection: "test",
                requesters: [],
                requesterGroups: [],
                comments: [],
                actionItems: [],
                attachments: []
        )
        reportRequest.startDate = d
        reportRequest.endDate = d
        def result = service.copy(reportRequest)

        then:
        result.reportName.endsWith("test")
        result.description == "test"
        result.priority.name == "test"
        result.asOfVersionDate == d
        result.startDate == d
        result.endDate == d
        result.owner.username == "owner"
        result.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION
        result.excludeFollowUp == true
        result.includeLockedVersion == true
        result.includeAllStudyDrugsCases == true
        result.excludeNonValidCases == true
        result.excludeDeletedCases == true
        result.suspectProduct == true
        result.productSelection == "test"
        result.studySelection == "test"
        result.eventSelection == "test"
        result.assignedTo.username == "test"
        result.assignedGroupTo.name == "test"
    }

    void "test save"(){
        User normalUser = new User(username: "owner")
        normalUser.save(failOnError:true,validate:false)
        ReportRequest reportRequest = new ReportRequest()
        reportRequest.comments = [new ReportRequestComment()]
        reportRequest.actionItems = [new ActionItem()]
        reportRequest.attachments = [new ReportRequestAttachment()]
        reportRequest.save(failOnError:true , validate:false)
        def userMock = new MockFor(UserService)
        userMock.demand.getUser(0..1) { -> return normalUser }
        service.userService = userMock.proxyInstance()
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save(0..1){ theInstance -> theInstance }
        service.CRUDService = crudServiceMock.proxyInstance()
        when:
        service.save(reportRequest)
        then:
        reportRequest.actionItems[0].createdBy == "owner"
        reportRequest.actionItems[0].modifiedBy == "owner"
        reportRequest.comments[0].createdBy == "owner"
        reportRequest.comments[0].modifiedBy == "owner"
        reportRequest.attachments[0].createdBy == "owner"
        reportRequest.attachments[0].modifiedBy == "owner"
    }

    void "test update"(){
        User normalUser = new User(username: "owner")
        normalUser.save(failOnError:true,validate:false)
        ReportRequest reportRequest = new ReportRequest()
        reportRequest.comments = [new ReportRequestComment(createdBy: "user",modifiedBy:"user")]
        reportRequest.actionItems = [new ActionItem(createdBy:  "user",modifiedBy:"user")]
        reportRequest.attachments = [new ReportRequestAttachment(createdBy: "user",modifiedBy:"user")]
        reportRequest.save(failOnError:true , validate:false)
        def userMock = new MockFor(UserService)
        userMock.demand.getUser(0..1) { -> return normalUser }
        service.userService = userMock.proxyInstance()
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update(0..1){ theInstance -> theInstance }
        service.CRUDService = crudServiceMock.proxyInstance()
        when:
        service.update(reportRequest)
        then:
        reportRequest.actionItems[0].createdBy == "user"
        reportRequest.actionItems[0].modifiedBy == "owner"
        reportRequest.comments[0].createdBy == "user"
        reportRequest.comments[0].modifiedBy == "owner"
        reportRequest.attachments[0].createdBy == "user"
        reportRequest.attachments[0].modifiedBy == "owner"
    }

    void "test sendActionItemNotification"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        normalUser.save(failOnError:true,validate:false)
        ReportRequest reportRequest = new ReportRequest(assignedTo: normalUser)
        ActionItem actionItem = new ActionItem(id: 1L,assignedTo: normalUser)
        actionItem.save(failOnError:true , validate:false)
        reportRequest.actionItems = [actionItem]
        reportRequest.save(failOnError:true , validate:false)
        HttpServletRequest request = new MockHttpServletRequest()
        request.setRequestURI("/reports/actionItem/index")
        WebUtils.metaClass.static.retrieveGrailsWebRequest = { -> new Object(){
                HttpServletRequest getCurrentRequest(){
                    return request
                }
            }
        }
        Set<String> recipient = ["abc@gmail.com"] as Set
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..1){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName -> run++}
        service.notificationService = mockNotificationService.proxyInstance()
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> return normalUser }
        service.userService = userMock.proxyInstance()
        def mockGroovyPageRenderer = new MockFor(PageRenderer)
        mockGroovyPageRenderer.demand.render(0..1){Map args -> "actionItemContent"}
        service.groovyPageRenderer = mockGroovyPageRenderer.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..1){def recipients, def messageBody, boolean asyVal, String emailSubject, String[] ccRecipients-> run++}
        service.emailService = mockEmailService.proxyInstance()
        when:
        service.sendActionItemNotification(reportRequest,reportRequest.actionItems,'create',recipient,"app.notification.actionItem.email.created")
        then:
        run == 2
    }

    void "test sendCommentNotification"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        normalUser.save(failOnError:true,validate:false)
        ReportRequest reportRequest = new ReportRequest(assignedTo: normalUser,reportName: "report_1")
        reportRequest.comments = [new ReportRequestComment(createdBy: "user",modifiedBy:"user")]
        reportRequest.save(failOnError:true , validate:false)
        HttpServletRequest request = new MockHttpServletRequest()
        request.setRequestURI("/reports/actionItem/index")
        WebUtils.metaClass.static.retrieveGrailsWebRequest = { -> new Object(){
                HttpServletRequest getCurrentRequest(){
                    return request
                }
            }
        }
        Set<String> recipient = ["abc@gmail.com"] as Set
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..1){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName -> run++}
        service.notificationService = mockNotificationService.proxyInstance()
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> return normalUser }
        service.userService = userMock.proxyInstance()
        def mockGroovyPageRenderer = new MockFor(PageRenderer)
        mockGroovyPageRenderer.demand.render(0..1){Map args -> "comment"}
        service.groovyPageRenderer = mockGroovyPageRenderer.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..1){def recipients, def messageBody, boolean asyVal, String emailSubject -> run++}
        service.emailService = mockEmailService.proxyInstance()
        when:
        service.sendCommentNotification(reportRequest,reportRequest.comments,'create',recipient,"app.notification.comment.email.created")
        then:
        run == 2
    }

    void "test sendReportRequestNotification mode create"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        normalUser.save(failOnError:true,validate:false)
        ReportRequest reportRequest = new ReportRequest(assignedTo: normalUser,reportName: "report_1")
        reportRequest.save(failOnError:true , validate:false)
        HttpServletRequest request = new MockHttpServletRequest()
        request.setRequestURI("/reports/actionItem/index")
        WebUtils.metaClass.static.retrieveGrailsWebRequest = { -> new Object(){
                HttpServletRequest getCurrentRequest(){
                    return request
                }
            }
        }
        Set<String> recipient = ["abc@gmail.com"] as Set
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..1){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName -> run++}
        service.notificationService = mockNotificationService.proxyInstance()
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> return normalUser }
        service.userService = userMock.proxyInstance()
        def mockGroovyPageRenderer = new MockFor(PageRenderer)
        mockGroovyPageRenderer.demand.render(0..1){Map args -> "comment"}
        service.groovyPageRenderer = mockGroovyPageRenderer.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..1){def recipients, def messageBody, boolean asyVal, String emailSubject -> run++}
        service.emailService = mockEmailService.proxyInstance()
        when:
        service.sendReportRequestNotification(reportRequest,recipient,'create',null,"app.notification.reportRequest.email.created")
        then:
        run == 2
    }

    void "test sendCreationNotification"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        normalUser.save(failOnError:true,validate:false)
        ReportRequest reportRequest = new ReportRequest(assignedTo: normalUser,reportName: "report_1")
        reportRequest.comments = [new ReportRequestComment(createdBy: "user",modifiedBy:"user")]
        reportRequest.actionItems = [new ActionItem(createdBy:  "user",modifiedBy:"user", assignedTo: normalUser)]
        reportRequest.save(failOnError:true , validate:false)
        HttpServletRequest request = new MockHttpServletRequest()
        request.setRequestURI("/reports/actionItem/index")
        WebUtils.metaClass.static.retrieveGrailsWebRequest = { -> new Object(){
                HttpServletRequest getCurrentRequest(){
                    return request
                }
            }
        }
        def mockActionItemService = Mock(ActionItemService)
        mockActionItemService.getRecipientsByEmailPreference(_,_) >> {User user, String mode -> ['abc@gmail.com']}
        service.actionItemService = mockActionItemService
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..3){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName -> run++}
        service.notificationService = mockNotificationService.proxyInstance()
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..3) { -> return normalUser }
        service.userService = userMock.proxyInstance()
        def mockGroovyPageRenderer = new MockFor(PageRenderer)
        mockGroovyPageRenderer.demand.render(0..3){Map args -> "comment"}
        service.groovyPageRenderer = mockGroovyPageRenderer.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..3){def recipients, def messageBody, boolean asyVal, String emailSubject, String [] ccRecipients -> run++}
        service.emailService = mockEmailService.proxyInstance()
        when:
        service.sendCreationNotification(reportRequest)
        then:
        run == 6
    }

    void "test sendDeleteEmailNotification"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        normalUser.save(failOnError:true,validate:false)
        ReportRequest reportRequest = new ReportRequest(owner: normalUser,assignedTo: normalUser,requesters: [normalUser])
        reportRequest.save(failOnError:true , validate:false)
        def mockGroovyPageRenderer = new MockFor(PageRenderer)
        mockGroovyPageRenderer.demand.render(0..1){Map args -> "content"}
        service.groovyPageRenderer = mockGroovyPageRenderer.proxyInstance()
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> return normalUser }
        service.userService = userMock.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..1){def recipients, def messageBody, boolean asyVal, String emailSubject -> run++}
        service.emailService = mockEmailService.proxyInstance()
        when:
        service.sendDeleteEmailNotification(reportRequest)
        then:
        run == 1
    }

    void "test getReportComments"(){
        ReportRequest reportRequest = new ReportRequest()
        reportRequest.comments = [new ReportRequestComment(reportComment: "comment_1")]
        reportRequest.save(failOnError:true , validate:false)
        when:
        def result = service.getReportComments(reportRequest)
        then:
        result == [[id:1,reportComment: "comment_1"]]
    }

    void "test sendCommentUpdateNotification"(){
        String message = ""
        int run = 0
        ReportRequest reportRequest = new ReportRequest()
        reportRequest.comments = [new ReportRequestComment(reportComment: "comment_new")]
        reportRequest.save(failOnError:true , validate:false)
        service.metaClass.sendCommentNotification = {ReportRequest reportRequestInstance, def comments, def mode, def recipients, def subject ->
            message = "app.notification.comment.email.updated"
            run++
        }
        when:
        service.sendCommentUpdateNotification(reportRequest,[[id:1,reportComment: "comment_old"]],["abc@gmail.com"])
        then:
        message == "app.notification.comment.email.updated"
        run == 1
    }

    void "test getReportActionItems"(){
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest()
        ActionItemCategory actionItemCategory = new ActionItemCategory()
        actionItemCategory.save(failOnError:true , validate:false,flush :true)
        reportRequest.actionItems = [new ActionItem(description: "description",actionCategory:actionItemCategory,assignedTo:normalUser,priority: "priority",
                                    status: StatusEnum.OPEN)]
        reportRequest.save(failOnError:true , validate:false)
        when:
        def result = service.getReportActionItems(reportRequest)
        then:
        result == [[description: "description",actionCategory:actionItemCategory,assignedTo:"user",completionDate:null,dueDate: null,priority: "priority",status: StatusEnum.OPEN,id:1]]
    }

    void "test showActionItemUpdateNotification"(){
        String message = ""
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest()
        ActionItemCategory actionItemCategory = new ActionItemCategory()
        actionItemCategory.save(failOnError:true , validate:false,flush :true)
        reportRequest.actionItems = [new ActionItem(description: "description",actionCategory:actionItemCategory,assignedTo:normalUser,priority: "priority",
                status: StatusEnum.OPEN)]
        reportRequest.comments = [new ReportRequestComment(reportComment: "comment_new")]

        reportRequest.save(failOnError:true , validate:false)
        service.metaClass.sendActionItemNotification = {ReportRequest reportRequestInstance,def actionItems, def mode, def recipients, def actionItemSubject ->
            message = "app.notification.actionItem.email.created"
            run++
        }
        when:
        service.showActionItemUpdateNotification(reportRequest,[],["abc@gmail.com"])
        then:
        message == "app.notification.actionItem.email.created"
        run == 1
    }

    void "test sendUpdateModeNotification"(){
        String message_comment = ""
        String message_actionItem = ""
        String message_reportRequest = ""
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest(assignedTo: normalUser,owner: normalUser)
        ActionItemCategory actionItemCategory = new ActionItemCategory()
        actionItemCategory.save(failOnError:true , validate:false,flush :true)
        reportRequest.actionItems = [new ActionItem(description: "description",actionCategory:actionItemCategory,assignedTo:normalUser,priority: "priority",
                status: StatusEnum.OPEN)]
        reportRequest.save(failOnError:true , validate:false)
        service.metaClass.sendReportRequestNotification = {ReportRequest reportRequestInstance, def recipients, def mode, def oldReportRequestRef, emailSubject ->
            message_reportRequest = "app.notification.reportRequest.created"
            run++
        }
        service.metaClass.sendActionItemNotification = {ReportRequest reportRequestInstance,def actionItems, def mode, def recipients, def actionItemSubject ->
            message_actionItem = "app.notification.actionItem.email.created"
            run++
        }
        service.metaClass.sendCommentNotification = {ReportRequest reportRequestInstance, def comments, def mode, def recipients, def subject ->
            message_comment = "app.notification.comment.email.updated"
            run++
        }
        when:
        service.sendUpdateModeNotification(reportRequest,null,reportRequest,[[id:1,reportComment: "comment_old"]],[])
        then:
        message_reportRequest == "app.notification.reportRequest.created"
        message_comment == "app.notification.comment.email.updated"
        message_actionItem == "app.notification.actionItem.email.created"
        run == 3
    }

    void "test getAttachments"(){
        ReportRequest reportRequest = new ReportRequest()
        reportRequest.attachments = [new ReportRequestAttachment(name: "attachment")]
        reportRequest.save(failOnError:true , validate:false)
        when:
        def result = service.getAttachments(reportRequest)
        then:
        result == [[id:1,name: "attachment"]]
    }

    void "test actionItemStatusForReportRequest false"(){
        ReportRequest reportRequest = new ReportRequest()
        reportRequest.actionItems = [new ActionItem(status: StatusEnum.CLOSED)]
        reportRequest.save(failOnError:true , validate:false)
        when:
        def result = service.actionItemStatusForReportRequest("1")
        then:
        result['actionItemStatus'] == "false"
    }

    void "test actionItemStatusForReportRequest true"(){
        ReportRequest reportRequest = new ReportRequest()
        reportRequest.actionItems = [new ActionItem(status: StatusEnum.OPEN)]
        reportRequest.save(failOnError:true , validate:false)
        when:
        def result = service.actionItemStatusForReportRequest("1")
        then:
        result['actionItemStatus'] == "true"
    }

    void "test validateDatesBeforeSave empty"() {
        ReportRequest reportRequest = new ReportRequest()
        reportRequest.dueDate = new Date()
        reportRequest.completionDate = new Date() + 1
        when:
        String result = service.validateDatesBeforeSave(reportRequest)
        then:
        result.isEmpty()
    }

    void "test validateDatesBeforeSave dueDate"() {
        ViewHelper.metaClass.static.getMessage = { String messageKey ->
            return messageKey
        }
        ReportRequest reportRequest = new ReportRequest()
        reportRequest.dueDate = new Date() - 1
        when:
        String result = service.validateDatesBeforeSave(reportRequest)
        then:
        result.contains('app.report.request.dueDate.before.now')
    }

    void "test validateDatesBeforeSave completionDate"() {
        ViewHelper.metaClass.static.getMessage = { String messageKey ->
            return messageKey
        }
        ReportRequest reportRequest = new ReportRequest()
        reportRequest.completionDate = new Date() - 1
        when:
        String result = service.validateDatesBeforeSave(reportRequest)
        then:
        result.contains('app.report.request.completionDate.before.now')
    }

    void "test getNotificationRecipients"(){
        User normalUser = makeNormalUser("user",[])
        ReportRequest reportRequest = new ReportRequest(assignedTo: normalUser,owner: normalUser,requesters: [normalUser])
        reportRequest.save(failOnError:true , validate:false)
        when:
        def result = service.getNotificationRecipients(reportRequest)
        then:
        result.size() == 1
    }

    void "test sendCreationNotification without Email"(){
        User user = makeNormalUser("user",[])
        user.preference.timeZone = "new TimeZone"
        user.preference.reportRequestEmail = new ReportRequestEmailPreference(creationEmails: false)
        ReportRequest reportRequest = new ReportRequest(assignedTo: user,reportName: "report_1")
        reportRequest.comments = [new ReportRequestComment(createdBy: "user",modifiedBy:"user")]
        reportRequest.actionItems = [new ActionItem(id: 1L,createdBy:  "user",modifiedBy:"user", assignedTo: user)]
        reportRequest.save(failOnError:true , validate:false)
        HttpServletRequest request = new MockHttpServletRequest()
        request.setRequestURI("/reports/actionItem/index")
        WebUtils.metaClass.static.retrieveGrailsWebRequest = { -> new Object(){
            HttpServletRequest getCurrentRequest(){
                return request
            }
        }
        }
        def mockActionItemService = Mock(ActionItemService)
        mockActionItemService.getRecipientsByEmailPreference(_,_) >> {User u, String mode -> []}
        service.actionItemService = mockActionItemService
        def mockNotificationService = Mock(NotificationService)
        mockNotificationService.addNotification(_,_,_,_,_) >> { List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName -> true}
        service.notificationService = mockNotificationService
        def userMock = Mock(UserService)
        userMock.getCurrentUser() >> {return user }
        service.userService = userMock
        def mockGroovyPageRenderer = Mock(PageRenderer)
        mockGroovyPageRenderer.render(_,_) >> {Map args -> "comment"}
        service.groovyPageRenderer = mockGroovyPageRenderer
        def mockEmailService = Mock(EmailService)
        mockEmailService.sendNotificationEmail(_,_,_,_,_) >> {def recipients, def messageBody, boolean asyVal, String emailSubject, String[] ccRecipients -> true}
        service.emailService = mockEmailService
        when:
        def model = service.sendCreationNotification(reportRequest)
        then:
        model == null
    }


    void "test sendUpdateModeNotification without Email"(){
        String message_comment = ""
        String message_actionItem = ""
        String message_reportRequest = ""
        User user = makeNormalUser("user",[])
        user.preference.timeZone = "new TimeZone"
        user.preference.reportRequestEmail = new ReportRequestEmailPreference(updateEmails: false)
        ReportRequest reportRequest = new ReportRequest(assignedTo: user,owner: user)
        ActionItemCategory actionItemCategory = new ActionItemCategory()
        reportRequest.actionItems = [new ActionItem(description: "description",actionCategory:actionItemCategory,assignedTo:user,priority: "priority",
                status: StatusEnum.OPEN)]
        service.metaClass.sendReportRequestNotification = {ReportRequest reportRequestInstance, def recipients, def mode, def oldReportRequestRef, emailSubject ->
            message_reportRequest = "app.notification.reportRequest.created"
            true
        }
        service.metaClass.sendActionItemNotification = {ReportRequest reportRequestInstance,def actionItems, def mode, def recipients, def actionItemSubject ->
            message_actionItem = "app.notification.actionItem.email.created"
            true
        }
        service.metaClass.sendCommentNotification = {ReportRequest reportRequestInstance, def comments, def mode, def recipients, def subject ->
            message_comment = "app.notification.comment.email.updated"
            true
        }
        when:
        service.sendUpdateModeNotification(reportRequest,null,reportRequest,[[id:1,reportComment: "comment_old"]],[])
        then:
        message_reportRequest == "app.notification.reportRequest.created"
        message_comment == "app.notification.comment.email.updated"
        message_actionItem == "app.notification.actionItem.email.created"
    }

    void "test sendDeleteEmailNotification without Email"(){
        User user = makeNormalUser("user",[])
        user.preference.timeZone = "new TimeZone"
        user.preference.reportRequestEmail = new ReportRequestEmailPreference(deleteEmails: false)
        ReportRequest reportRequest = new ReportRequest(owner: user,assignedTo: user,requesters: [user])
        def mockGroovyPageRenderer = Mock(PageRenderer)
        mockGroovyPageRenderer.render(_,_) >> {Map args -> "content"}
        service.groovyPageRenderer = mockGroovyPageRenderer
        def userMock = Mock(UserService)
        userMock.getCurrentUser() >> { return user }
        service.userService = userMock
        def mockEmailService = Mock(EmailService)
        mockEmailService.sendNotificationEmail(_,_,_,_) >> {def recipients, def messageBody, boolean asyVal, String emailSubject -> true}
        service.emailService = mockEmailService
        when:
        def model = service.sendDeleteEmailNotification(reportRequest)
        then:
        model == null
    }
}
