package com.rxlogix

import com.rxlogix.config.Tenant
import com.rxlogix.user.*
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants, User])
class PreferenceControllerSpec extends Specification implements DataTest, ControllerUnitTest<PreferenceController> {

    public static final user = "unitTest"

    def setupSpec() {
        mockDomains User, UserGroup, Role, UserRole,UserGroupUser,Tenant, Preference
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

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user, actionItemEmail: new AIEmailPreference(), reportRequestEmail: new ReportRequestEmailPreference())
        def userRole = new Role(authority: 'ROLE_PVQ_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getUser(0..1) { -> user }
        securityMock.demand.isCurrentUserDev(0..2) { false }
        return securityMock.proxyInstance()
    }

    private makeUserService() {
        def userMock = mockFor(UserService)
        userMock.demand.isCurrentUserDev(1..2) { false }
        return userMock.createMock()
    }
    void "test index, When User exists"(){
        given:
        def normalUser = makeNormalUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {normalUser}
        controller.userService=mockUserService
        when:
        controller.index()
        then:
        response.status==200
    }

    void "test index, When User does not exist"(){
        given:
        def mockUserService=Mock(UserService)
        controller.userService=mockUserService
        when:
        controller.index()
        then:
        response.status==200
    }

    void "test update with instance null"(){
        given:
        def mockUserService = Mock( UserService )
        controller.userService = mockUserService
        when:
        request.method = 'POST'
        controller.update()
        then:
        response.redirectedUrl == '/preference/index'
    }

    void "test update success"(){
        given:
        def normalUser = makeNormalUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {normalUser}
        controller.userService=mockUserService
        def preferenceNormal = new Preference(locale: new Locale("ja"), createdBy: user, modifiedBy: user, actionItemEmail: new AIEmailPreference(), reportRequestEmail: new ReportRequestEmailPreference())
        preferenceNormal.save(failOnError:true,validate:false)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_)>>{return preferenceNormal}
        controller.CRUDService=mockCRUDService
        when:
        request.method = 'POST'
        params.timeZone = 'UTC'
        params.language = 'ja'
        params.theme = 'Theme'
        controller.update()
        then:
        response.status == 302
        response.redirectedUrl=="/preference/index"

    }

    void "test update validation exception"(){
        given:
        def normalUser = makeNormalUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {normalUser}
        controller.userService=mockUserService
        def preferenceNormal = new Preference(locale: new Locale("ja"), createdBy: user, modifiedBy: user, actionItemEmail: new AIEmailPreference(), reportRequestEmail: new ReportRequestEmailPreference())
        preferenceNormal.save(failOnError:true,validate:false)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception", preferenceNormal.errors)}
        controller.CRUDService=mockCRUDService
        when:
        request.method = 'POST'
        params.timeZone = 'UTC'
        params.language = 'ja'
        params.theme = 'Theme'
        controller.update()
        then:
        response.status == 302
    }

    void "test updateCurrentTenant, When request.getHeader contains updateCurrentTenant and changed Success"(){
        given:
        def normalUser = makeNormalUser()
        Tenants.metaClass.static.currentId = { -> return 1}
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser() >> {normalUser}
        controller.userService=mockUserService
        request.addHeader('referer',"updateCurrentTenant")
        when:
        controller.updateCurrentTenant(1L)
        then:
        flash.message == 'tenant.current.changed.success'
        response.status == 302
        response.redirectedUrl == 'updateCurrentTenant'
    }

    void "test updateCurrentTenant, When request.getHeader does not contain updateCurrentTenant and changed Success"(){
        given:
        def normalUser = makeNormalUser()
        Tenants.metaClass.static.currentId = { -> return 1}
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser() >> {normalUser}
        controller.userService=mockUserService
        when:
        controller.updateCurrentTenant(1L)
        then:
        flash.message == 'tenant.current.changed.success'
        response.status == 302
        response.redirectedUrl == '/dashboard'
    }

    void "test updateCurrentTenant, When request.getHeader does not contain updateCurrentTenant and Selected tenant not found"(){
        given:
        def normalUser = makeNormalUser()
        Tenants.metaClass.static.currentId = { -> return 1}
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser() >> {normalUser}
        controller.userService=mockUserService
        when:
        controller.updateCurrentTenant(4L)
        then:
        flash.error == 'tenant.current.changed.error'
        response.status == 302
        response.redirectedUrl == '/dashboard'
    }

    void "test updateCurrentTenant, When request.getHeader contains updateCurrentTenant and Selected tenant not found"() {
        given:
        def normalUser = makeNormalUser()
        Tenants.metaClass.static.currentId = { -> return 1 }
        def mockUserService = Mock(UserService)
        mockUserService.getCurrentUser() >> { normalUser }
        controller.userService = mockUserService
        request.addHeader('referer', "updateCurrentTenant")
        when:
        controller.updateCurrentTenant(4L)
        then:
        flash.error == 'tenant.current.changed.error'
        response.status == 302
        response.redirectedUrl == 'updateCurrentTenant'
    }

    void "test loadTheme --Success"(){
        given:
        def normalUser = makeNormalUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {normalUser}
        controller.userService=mockUserService
        when:
        controller.loadTheme()
        then:
        response.status == 200
    }

    void "test loadTheme --Failure"(){
        given:
        def userRole = new Role(authority: 'ROLE_PVQ_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: null, createdBy: user, modifiedBy: user)
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {normalUser}
        controller.userService=mockUserService
        when:
        controller.loadTheme()
        then:
        response.status == 200
    }
}
