package com.rxlogix

import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.Notification
import com.rxlogix.config.Tenant
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User])
class ExecutionStatusControllerSpec extends Specification implements DataTest, ControllerUnitTest<ExecutionStatusController>  {

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference,ExecutionStatus
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
        normalUser.metaClass.static.isDev = { -> return false}
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
        adminUser.metaClass.static.isDev = { -> return true}
        return adminUser
    }

    void "test list, When isICSRProfile is false"(){
        given:
        def mockuserService=Mock(UserService)
        mockuserService.isCurrentUserAdmin()
        controller.userService=mockuserService
        when:
        controller.list(false)
        then:
        response.status == 200
    }


    void "test list, When isICSRProfile is true"(){
        given:
        def mockuserService=Mock(UserService)
        mockuserService.isCurrentUserAdmin()
        controller.userService=mockuserService
        when:
        controller.list(true)
        then:
        response.status == 200
    }
    void "test listAllResults"(){
        given:
        def mockuserService=Mock(UserService)
        mockuserService.isCurrentUserAdmin()
        controller.userService=mockuserService
        when:
        controller.listAllResults()
        then:
        response.status == 200
    }

    void "test reportExecutionError action, When Instance exists. "(){
        given:
        ExecutionStatus executionStatus=new ExecutionStatus()
        executionStatus.save(failOnError:true,validate:false)
        when:
        controller.reportExecutionError(1L)
        then:
        response.status == 200
    }

    void "test reportExecutionError Action, When Instance does not exists. "(){
        given:
        ExecutionStatus executionStatus =null
        when:
        controller.reportExecutionError(1L)
        then:
        response.status == 302
        response.redirectedUrl == '/executionStatus/list'

    }

    void "test viewNotificationError-when instance not exists"(){
        when:
        params.id=1L
        controller.viewNotificationError()
        then:
        response.status==302
        response.redirectedUrl=="/executionStatus/list"
    }

    void "test viewNotificationError-when instance exists"(){
        given:
        Notification notification=new Notification(message: "test",executionStatusId: 2L)
        def mockNotificationService=Mock(NotificationService)
        mockNotificationService.deleteNotification(_)>>{}
        controller.notificationService=mockNotificationService
        when:
        params.id=1L
        controller.viewNotificationError(notification)
        then:
        response.status==200
    }
}
