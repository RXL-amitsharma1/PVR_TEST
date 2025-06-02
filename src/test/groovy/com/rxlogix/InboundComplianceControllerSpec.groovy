package com.rxlogix

import com.rxlogix.config.InboundCompliance
import com.rxlogix.config.SourceProfile
import com.rxlogix.config.SuperQuery
import com.rxlogix.config.Tenant
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User, SuperQuery, SourceProfile])
class InboundComplianceControllerSpec extends Specification implements DataTest, ControllerUnitTest<InboundComplianceController> {

    def setupSpec() {
        mockDomains SourceProfile, Tenant, User, Preference, Role, UserRole, SuperQuery, InboundCompliance
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

    void "test create query not found"(){
        boolean run =false
        User normalUser = makeNormalUser("user",[])
        def mockInboundComplianceService = new MockFor(InboundComplianceService)
        mockInboundComplianceService.demand.fetchConfigurationMapFromSession(0..1){ params, session->
            run = true
            return [configurationParams:[:],queryComplianceIndex:[:]]
        }
        controller.inboundComplianceService = mockInboundComplianceService.proxyInstance()

        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){-> normalUser}
        controller.userService = mockUserService.proxyInstance()

        SuperQuery.metaClass.static.get = { Serializable serializable -> return null}

        SourceProfile.metaClass.static.sourceProfilesForUser = { User currentUser -> [new SourceProfile()]}
        when:
        params.selectedQuery = 10
        controller.create()
        then:
        run == true
        flash.error == 'app.configuration.query.notFound'
        view == '/inboundCompliance/create'
    }

    void "test - executionStatus"(){
        def mockUserService = Mock(UserService)
        mockUserService.isCurrentUserAdmin()
        controller.userService = mockUserService
        when:
        controller.executionStatus()
        then:
        response.status == 200
    }

}
