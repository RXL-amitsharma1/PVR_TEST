package com.rxlogix

import com.rxlogix.config.ActionItem
import com.rxlogix.config.Tenant
import com.rxlogix.enums.AppTypeEnum
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.user.*
import grails.gorm.multitenancy.Tenants
import grails.gsp.PageRenderer
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants])
class ActionItemServiceSpec extends Specification implements DataTest, ServiceUnitTest<ActionItemService>{

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference,ActionItem, AIEmailPreference, ReportRequestEmailPreference
    }

    private User makeNormalUser(name, team, String email = null) {
        User.metaClass.encodePassword = { "password" }
        AIEmailPreference aiEmailPreference = new AIEmailPreference(creationEmails: true, updateEmails: true, jobEmails: true)
        ReportRequestEmailPreference reportRequestEmailPreference = new ReportRequestEmailPreference(creationEmails: true, updateEmails: true, deleteEmails: true, workflowUpdate: true)
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user", actionItemEmail: aiEmailPreference, reportRequestEmail: reportRequestEmailPreference)
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user",email: email?:"abc@gmail.com")
        normalUser.addToTenants(tenant)
        normalUser.save(validate: false)
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

    void "test sendNotificationToOwnerWhenClosed"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(assignedTo: normalUser,appType: AppTypeEnum.ADHOC_REPORT,createdBy: "user",status: StatusEnum.CLOSED)
        actionItem.save(failOnError:true,validate:false)
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..1){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName->
            run = true
        }
        service.notificationService = mockNotificationService.proxyInstance()
        Set<String> recipients = [] as Set<String>
        when:
        service.sendNotificationToOwnerWhenClosed(actionItem,recipients)
        then:
        run == true
    }

    void "test sendNotificationToOwnerWhenClosed status QUALITY_MODULE_CAPA"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(assignedTo: normalUser,appType: AppTypeEnum.QUALITY_MODULE_CAPA,createdBy: "user",status: StatusEnum.CLOSED)
        actionItem.save(failOnError:true,validate:false)
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..1){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName->
            run = true
        }
        service.notificationService = mockNotificationService.proxyInstance()
        Set<String> recipients = [] as Set<String>
        when:
        service.sendNotificationToOwnerWhenClosed(actionItem,recipients)
        then:
        run == true
    }

    void "test sendNotificationToOwnerWhenClosed status QUALITY_MODULE"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(assignedTo: normalUser,appType: AppTypeEnum.QUALITY_MODULE,createdBy: "user",status: StatusEnum.CLOSED)
        actionItem.save(failOnError:true,validate:false)
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..1){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName->
            run = true
        }
        service.notificationService = mockNotificationService.proxyInstance()
        Set<String> recipients = [] as Set<String>
        when:
        service.sendNotificationToOwnerWhenClosed(actionItem,recipients)
        then:
        run == true
    }

    void "test sendActionItemNotification mode create"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(assignedTo: normalUser,appType: AppTypeEnum.ADHOC_REPORT,createdBy: "user",status: StatusEnum.CLOSED)
        actionItem.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        service.userService = mockUserService.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..2){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName->
            run++
        }
        service.notificationService = mockNotificationService.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..1){def recipients, def messageBody, boolean asyVal, String emailSubject, String [] ccRecipients ->
            run++
        }
        service.emailService = mockEmailService.proxyInstance()
        def mockGroovyPageRenderer = new MockFor(PageRenderer)
        mockGroovyPageRenderer.demand.render(0..1){Map args -> return "content"}
        service.groovyPageRenderer = mockGroovyPageRenderer.proxyInstance()
        Tenants.metaClass.static.currentId = { return 1L }
        when:
        service.sendActionItemNotification(actionItem,"create",null,"")
        then:
        run == 3
    }

    void "test sendActionItemNotification mode create apptype QUALITY_MODULE"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(assignedTo: normalUser,appType: AppTypeEnum.QUALITY_MODULE,createdBy: "user",status: StatusEnum.IN_PROGRESS)
        actionItem.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        service.userService = mockUserService.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..2){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName->
            run++
        }
        service.notificationService = mockNotificationService.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..1){def recipients, def messageBody, boolean asyVal, String emailSubject,String [] ccRecipients ->
            run++
        }
        service.emailService = mockEmailService.proxyInstance()
        def mockGroovyPageRenderer = new MockFor(PageRenderer)
        mockGroovyPageRenderer.demand.render(0..1){Map args -> return "content"}
        service.groovyPageRenderer = mockGroovyPageRenderer.proxyInstance()
        Tenants.metaClass.static.currentId = { return 1L }
        when:
        service.sendActionItemNotification(actionItem,"create",null,"")
        then:
        run == 2
    }

    void "test sendActionItemNotification mode create apptype QUALITY_MODULE_CAPA"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ActionItem actionItem = new ActionItem(assignedTo: normalUser,appType: AppTypeEnum.QUALITY_MODULE_CAPA,createdBy: "user",status: StatusEnum.CLOSED)
        actionItem.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        service.userService = mockUserService.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..2){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName->
            run++
        }
        service.notificationService = mockNotificationService.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..1){def recipients, def messageBody, boolean asyVal, String emailSubject, String [] ccRecipients ->
            run++
        }
        service.emailService = mockEmailService.proxyInstance()
        def mockGroovyPageRenderer = new MockFor(PageRenderer)
        mockGroovyPageRenderer.demand.render(0..1){Map args -> return "content"}
        service.groovyPageRenderer = mockGroovyPageRenderer.proxyInstance()
        Tenants.metaClass.static.currentId = { return 1L }
        when:
        service.sendActionItemNotification(actionItem,"create",null,"")
        then:
        run == 3
    }

    void "test sendActionItemNotification mode update"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        User user = makeNormalUser("normalUser",[], "abc1@gmail.com")
        ActionItem actionItem = new ActionItem(assignedTo: normalUser,appType: AppTypeEnum.ADHOC_REPORT,createdBy: "user",status: StatusEnum.CLOSED)
        actionItem.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> user}
        service.userService = mockUserService.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..1){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName->
            run++
        }
        mockNotificationService.demand.addNotification(0..2){ User userInstance, String message, long id, NotificationLevelEnum level, NotificationApp appName->
            run++
        }
        mockNotificationService.demand.addNotification(0..1){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName->
            run++
        }
        service.notificationService = mockNotificationService.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..1){def recipients, def messageBody, boolean asyVal, String emailSubject, String [] ccRecipients->
            run++
        }
        service.emailService = mockEmailService.proxyInstance()
        def mockGroovyPageRenderer = new MockFor(PageRenderer)
        mockGroovyPageRenderer.demand.render(0..1){Map args -> return "content"}
        service.groovyPageRenderer = mockGroovyPageRenderer.proxyInstance()
        Tenants.metaClass.static.currentId = { return 1L }
        when:
        service.sendActionItemNotification(actionItem,"update",[assignedToId:user.id],"")
        then:
        run == 5
    }

    void "test sendActionItemNotification mode update same user"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        UserGroup userGroupInstance = new UserGroup()
        userGroupInstance.save(failOnError:true,validate:false)
        UserGroupUser userGroupUser = new UserGroupUser(user: normalUser,userGroup:userGroupInstance)
        userGroupUser.save(failOnError:true,validate:false)
        ActionItem actionItem = new ActionItem(assignedTo: normalUser,appType: AppTypeEnum.ADHOC_REPORT,createdBy: "user",status: StatusEnum.CLOSED,assignedGroupTo: userGroup)
        actionItem.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> normalUser}
        service.userService = mockUserService.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.addNotification(0..3){ List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName->
            run++
        }
        service.notificationService = mockNotificationService.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..1){def recipients, def messageBody, boolean asyVal, String emailSubject, String [] ccRecipients ->
            run++
        }
        service.emailService = mockEmailService.proxyInstance()
        def mockGroovyPageRenderer = new MockFor(PageRenderer)
        mockGroovyPageRenderer.demand.render(0..1){Map args -> return "content"}
        service.groovyPageRenderer = mockGroovyPageRenderer.proxyInstance()
        when:
        service.sendActionItemNotification(actionItem,"update",[assignedGroupToId:userGroupInstance.id],"")
        then:
        run == 4
    }


    void "test sendActionItemNotification mode create without Email"(){
        User normalUser = makeNormalUser("user",[])
        normalUser.preference.timeZone = "new TimeZone"
        normalUser.preference.actionItemEmail = new AIEmailPreference(creationEmails: false)
        ActionItem actionItem = new ActionItem(assignedTo: normalUser,appType: AppTypeEnum.QUALITY_MODULE_CAPA,createdBy: "user",status: StatusEnum.CLOSED)
        actionItem.save(failOnError:true,validate:false)
        def mockUserService = Mock(UserService)
        mockUserService.getCurrentUser() >> {return normalUser}
        service.userService = mockUserService
        def mockNotificationService = Mock(NotificationService)
        mockNotificationService.addNotification(_,_,_,_,_) >> { List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName->
            true
        }
        service.notificationService = mockNotificationService
        def mockEmailService = Mock(EmailService)
        mockEmailService.sendNotificationEmail(_,_,_,_,_) >> {def recipients, def messageBody, boolean asyVal, String emailSubject, String [] ccRecipients ->
            true
        }
        service.emailService = mockEmailService
        def mockGroovyPageRenderer = Mock(PageRenderer)
        mockGroovyPageRenderer.render(_,_) >> {Map args -> return "content"}
        service.groovyPageRenderer = mockGroovyPageRenderer
        when:
        def model = service.sendActionItemNotification(actionItem,"create",null,"")
        then:
        model == null
    }

    void "test sendActionItemNotification mode update without Email"(){
        User normalUser = makeNormalUser("user",[])
        normalUser.preference.timeZone = "new TimeZone"
        normalUser.preference.actionItemEmail = new AIEmailPreference(updateEmails: false)
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        UserGroup userGroupInstance = new UserGroup()
        userGroupInstance.save(failOnError:true,validate:false)
        UserGroupUser userGroupUser = new UserGroupUser(user: normalUser,userGroup:userGroupInstance)
        userGroupUser.save(failOnError:true,validate:false)
        ActionItem actionItem = new ActionItem(assignedTo: normalUser,appType: AppTypeEnum.ADHOC_REPORT,createdBy: "user",status: StatusEnum.CLOSED,assignedGroupTo: userGroup)
        actionItem.save(failOnError:true,validate:false)
        def mockUserService = Mock(UserService)
        mockUserService.getCurrentUser() >> {return normalUser}
        service.userService = mockUserService
        def mockNotificationService = Mock(NotificationService)
        mockNotificationService.addNotification(_,_,_,_,_) >> { List<User> userList, String message, Long id, NotificationLevelEnum level, NotificationApp appName->
            true
        }
        service.notificationService = mockNotificationService
        def mockEmailService = Mock(EmailService)
        mockEmailService.sendNotificationEmail(_,_,_,_,_) >> {def recipients, def messageBody, boolean asyVal, String emailSubject, String [] ccRecipients ->
            true
        }
        service.emailService = mockEmailService
        def mockGroovyPageRenderer = Mock(PageRenderer)
        mockGroovyPageRenderer.render(_,_) >> {Map args -> return "content"}
        service.groovyPageRenderer = mockGroovyPageRenderer
        when:
        def model = service.sendActionItemNotification(actionItem,"update",[assignedGroupToId:userGroupInstance.id],"")
        then:
        model == null
    }
}
