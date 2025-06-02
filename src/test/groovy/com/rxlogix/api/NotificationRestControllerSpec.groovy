package com.rxlogix.api

import com.rxlogix.config.ActionItem
import com.rxlogix.config.ActionItemCategory
import com.rxlogix.config.DrilldownCLLData
import com.rxlogix.config.Notification
import com.rxlogix.config.Tenant
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.NotificationLevelEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.user.UserRole
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import org.springframework.context.MessageSource
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User, Notification])
class NotificationRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<NotificationRestController> {


    def setupSpec() {
        mockDomains Notification, User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference
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

    private makeSecurityService(User user) {
        def securityMock = new MockFor(SpringSecurityService)
        securityMock.demand.getPrincipal() { -> user }
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
//  Works fine but fails while calculating coverage report
//    void "test forUser success"(){
//        User normalUser = makeNormalUser("user",[])
//        Notification notification = new Notification(user: normalUser,appName: NotificationApp.ADHOC_REPORT,message: "app.notification.completed",level: NotificationLevelEnum.INFO,executedConfigId: 1,executionStatusId: 1)
//        notification.save(failOnError:true,validate:false,flush:true)
//        MessageSource.metaClass.getMessage = { String code, Object[] args, Locale locale -> return "xyz"}
//        when:
//        controller.forUser(normalUser)
//        then:
//        response.json.size() == 1
//        response.json[0].size() == 8
//        response.json == []
//    }

    void "test forUser ids of userObj and user do not match"(){
        User normalUser = makeNormalUser("user",[])
        Notification notification = new Notification(user: normalUser,appName: NotificationApp.ADHOC_REPORT,message: "app.notification.completed",level: NotificationLevelEnum.INFO,executedConfigId: 1,executionStatusId: 1)
        notification.save(failOnError:true,validate:false,flush:true)
        controller.springSecurityService = makeSecurityService(normalUser)
        when:
        def result = controller.forUser(new User())
        then:
        result == null
    }

    void "test deleteNotificationById success"(){
        Notification notification = new Notification()
        notification.save(failOnError:true,validate:false,flush:true)
        Notification.metaClass.static.executeUpdate = { CharSequence charSequence,Map map -> return 1}
        when:
        controller.deleteNotificationById(notification.id)
        then:
        response.text == 'true'
    }

    void "test deleteNotificationById failure"(){
        Notification notification = new Notification()
        notification.save(failOnError:true,validate:false,flush:true)
        Notification.metaClass.static.executeUpdate = { CharSequence charSequence,Map map -> throw new Exception()}
        when:
        controller.deleteNotificationById(10)
        then:
        response.text == 'false'
    }

    void "test deleteNotificationsForUserId success"(){
        User normalUser = makeNormalUser("user",[])
        Notification notification = new Notification(user: normalUser)
        notification.save(failOnError:true,validate:false,flush:true)
        Notification.metaClass.static.executeUpdate = { CharSequence charSequence,Map map -> return 1}
        when:
        controller.deleteNotificationsForUserId(normalUser.id)
        then:
        response.text == 'true'
    }

    void "test deleteNotificationsForUserId failure"(){
        User normalUser = makeNormalUser("user",[])
        Notification notification = new Notification(user: normalUser)
        notification.save(failOnError:true,validate:false,flush:true)
        Notification.metaClass.static.executeUpdate = { CharSequence charSequence,Map map -> throw new Exception()}
        when:
        controller.deleteNotificationsForUserId(10)
        then:
        response.text == 'false'
    }
}
