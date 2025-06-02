package com.rxlogix

import com.rxlogix.config.Tenant
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.web.WebAttributes
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SpringSecurityUtils,User])
class LoginControllerSpec extends Specification implements DataTest, ControllerUnitTest<LoginController> {

    def setupSpec() {
        mockDomains Role, User, Preference, Tenant, UserRole
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
    def mockSpringSecurityService = Mock( SpringSecurityService )

    void setup() {
        controller.springSecurityService = mockSpringSecurityService
    }

    void "test index"() {
        when:
        controller.index()
        then:
        1 * mockSpringSecurityService.isLoggedIn() >> true
        response.redirectedUrl == '/'
        when:
        controller.response.reset()
        controller.index()
        then:
        1 * mockSpringSecurityService.isLoggedIn() >> false
        response.redirectedUrl == '/login/auth'
    }

    void "test auth, when isLoggedIn is true"(){
        given:
        mockSpringSecurityService.isLoggedIn()>>true
        when:
        controller.auth()
        then:
        response.redirectedUrl == '/'
    }

    void "test auth, when isLoggedIn is false"(){
        mockSpringSecurityService.isLoggedIn()>>false
        SpringSecurityUtils.metaClass.static.securityConfig = {-> "Test Auth" }
        when:
        controller.auth()
        then:
        response.status==200
    }

    void "test authAjax"(){
        given:
        SpringSecurityUtils.metaClass.static.securityConfig = { -> "Test Auth"}
        when:
        request.method = 'POST'
        controller.authAjax()
        then:
        response.status == 401
        response.getHeader('Location') == '/login/authAjax'
    }

    void "test denied"() {
        given:
        def mockAuthenticationTrustResolver=Mock(AuthenticationTrustResolver)
        mockAuthenticationTrustResolver.isRememberMe(_) >>true
        mockSpringSecurityService.isLoggedIn() >> true
        controller.authenticationTrustResolver= mockAuthenticationTrustResolver
        when:
        controller.denied()
        then:
        response.redirectedUrl == '/login/full'
    }

    void "test full"(){
        given:
        def mockAuthenticationTrustResolver=Mock(AuthenticationTrustResolver)
        mockAuthenticationTrustResolver.isRememberMe(_) >>true
        controller.authenticationTrustResolver= mockAuthenticationTrustResolver
        SpringSecurityUtils.metaClass.static.securityConfig = { -> "Test Full"}
        when:
        controller.full()
        then:
        response.status==200
    }

    void "test authfail,--Failure"(){
        given:
        Holders.config.grails?.plugin?.springsecurity?.saml?.active=true
        when:
        controller.authfail()
        then:
        response.status==200
        response.forwardedUrl=="/login/ssoAuthFail"
    }

    void "test authfail, --Success and When isAjax(request) is equal to true"(){
        given:
        mockSpringSecurityService.isAjax(_)>>true
        when:
        controller.authfail()
        then:
        response.status==200
    }

    void "test authfail, --Success and When isAjax(request) is equal to false"(){
        given:
        Holders.config.grails?.plugin?.springsecurity?.saml?.active=false
        mockSpringSecurityService.isAjax(_)>> false
        when:
        controller.authfail()
        then:
        response.status == 302
        response.redirectedUrl.startsWith('/login/auth')
    }

    void "test ajaxDenied"(){
    when:
    controller.ajaxDenied()
    then:
    response.status == 200
    }

    void "test ssoAuthFail, When isAjax(request) is true"(){
     given:
     mockSpringSecurityService.isAjax(request)>>true
     when:
     controller.ssoAuthFail()
     then:
     response.status==200
    }

    void "test ssoAuthFail, When isAjax(request) is false"(){
        given:
        mockSpringSecurityService.isAjax(request)>>false
        when:
        controller.ssoAuthFail()
        then:
        response.status==200
    }

    void "test ssoAuthFail, SAMLException and isAjax(request) is false"(){
        given:
        request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, new AuthenticationServiceException("Test for AuthenticationServiceException where message contains Error validating SAML message."))
        mockSpringSecurityService.isAjax(request)>>false
        when:
        controller.ssoAuthFail()
        then:
        response.status==302
        response.redirectUrl=="/"
    }

    void "test securityAndPrivacyPolicy"(){
        when:
        controller.securityAndPrivacyPolicy()
        then:
        response.status==200
    }
}
