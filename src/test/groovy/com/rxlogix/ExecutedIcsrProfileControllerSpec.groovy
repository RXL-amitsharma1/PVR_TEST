package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants, User, ExecutedIcsrProfileConfiguration])
class ExecutedIcsrProfileControllerSpec extends Specification implements DataTest, ControllerUnitTest<ExecutedIcsrProfileController> {

    def setupSpec() {
        mockDomains ExecutedIcsrProfileConfiguration, ExecutedTemplateQuery, ReportResult, Role, User, Preference, Tenant, UserRole
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

    void "test index"(){
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test view action, When Instance exists. "(){
        given:
        ExecutedIcsrProfileConfiguration executedConfiguration=new ExecutedIcsrProfileConfiguration()
        executedConfiguration.save(failOnError:true,validate:false)
        when:
        controller.view(executedConfiguration)
        then:
        response.status == 200
    }

    void "test view Action, When Instance does not exists. "(){
        given:
        ExecutedIcsrProfileConfiguration executedConfiguration=null
        when:
        controller.view(executedConfiguration)
        then:
        response.status == 302
        response.redirectUrl == '/executedIcsrProfile/index'
    }

    void "test showResult,When Instance exists"() {
        given:
        Tenants.metaClass.static.currentId = { -> return 1}
        User currentUser = makeNormalUser('user', [])
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(id: 2L)
        ExecutedIcsrProfileConfiguration executedConfiguration = new ExecutedIcsrProfileConfiguration(id: 1L, executedTemplateQueries: [executedTemplateQuery], owner: currentUser, tenantId: 1L)
        ExecutedIcsrProfileConfiguration.metaClass.static.read = { Long id -> executedConfiguration }
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { currentUser }
        controller.userService = mockUserService
        def mockDynamicReportService = Mock(DynamicReportService)
        controller.dynamicReportService = mockDynamicReportService
        mockDynamicReportService.getReportName(_, _, _) >> { return "testReportName" }
        when:
        controller.showResult(1L)
        then:
        response.status == 200
        response.forwardedUrl == "/icsrProfileConfiguration/viewCases?exIcsrProfileId="
    }

    void "test showResult,When Instance does not exists"(){
        given:
        ExecutedIcsrProfileConfiguration.metaClass.static.read ={Long id -> null}
        when:
        controller.showResult(999L)
        then:
        response.status==302
        response.redirectUrl == "/executedIcsrProfile/index"
    }

    void "test show"(){
        given:
        ReportTemplate executedTemplate=new ReportTemplate(id: 1L)
        ExecutedTemplateQuery executedTemplateQuery=new ExecutedTemplateQuery(id: 1L, executedTemplateQuery:executedTemplate )
        ReportResult reportResult= new ReportResult(id: 1L, drillDownSource:executedTemplateQuery)
        def mockDynamicReportService = Mock(DynamicReportService)
        mockDynamicReportService.getReportName(_,_,_) >> { return "testReportName"}
        controller.dynamicReportService=mockDynamicReportService
        def mockUserService= Mock(UserService)
        User currentUser = makeAdminUser()
        mockUserService.getUser() >> {currentUser}
        controller.userService=mockUserService
        reportResult.metaClass.isViewableBy = { ->
            return false}
        when:
        controller.show(reportResult)
        then:
        response.status==200
    }

    void "test show, When Instance does not exists "(){
        given:
        ReportResult reportResult=null
        when:
        controller.show(reportResult)
        then:
        response.status==302
        response.redirectUrl == "/executedIcsrProfile/index"
    }
}
