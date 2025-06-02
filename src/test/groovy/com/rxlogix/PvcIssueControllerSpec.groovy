package com.rxlogix

import com.rxlogix.central.PvcIssueController
import com.rxlogix.config.ActionItem
import com.rxlogix.config.Capa8D
import com.rxlogix.config.Tenant
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User])
class PvcIssueControllerSpec extends Specification implements DataTest, ControllerUnitTest<PvcIssueController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, Role, Tenant, UserRole, Preference, Capa8D, ActionItem
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

    void "test create"() {
        when:
        controller.create()
        then:
        response.status == 200
        response.forwardedUrl == '/issue/create?type=PVC'
    }

    void "test index"(){
        when:
        controller.index()
        then:
        response.status == 200

    }

    void "test view"(){
        when:
        params.id = 1L
        controller.view()

        then:
        response.status == 200
        response.forwardedUrl == '/issue/view/1?type=PVC'
    }

    def "test delete success"(){
        when:
        controller.delete()
        then:
        response.status == 200
        response.forwardedUrl == '/issue/delete?type=PVC'
    }

    def "test edit"(){
        given:
        User normalUser = makeNormalUser("user", [])
        def mockUserService = Mock( UserService )
        mockUserService.getActiveUsers()>>{return [normalUser]}
        controller.userService = mockUserService

        when:
        controller.edit()
        then:
        response.status == 200
    }

    def "test save --Success"(){
        when:
        controller.validateAndCreate()
        then:
        response.status == 200
        response.forwardedUrl == '/issue/validateAndCreate'
    }

    def "test update"(){
        when:
        controller.update()
        then:
        response.status == 200
        response.forwardedUrl == '/issue/update'
    }


    def "test share"(){
        when:
        controller.share()
        then:
        response.status == 200
        response.forwardedUrl == '/issue/share?type=PVC'
    }

    def "test email"(){
        when:
        controller.email()
        then:
        response.status == 200
        response.forwardedUrl == '/issue/email?type=PVC'
    }

}
