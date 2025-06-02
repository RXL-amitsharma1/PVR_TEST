package com.rxlogix


import com.rxlogix.config.ReportFooter
import com.rxlogix.config.Tenant
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ReportFooter, User])
class ReportFooterControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportFooterController> {

    def setupSpec() {
        mockDomains User, Tenant, Role, UserRole, ReportFooter
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
        normalUser.metaClass.static.isDev = { -> return false }
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
        adminUser.metaClass.static.isDev = { -> return true }
        return adminUser
    }

    void "test index"() {
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test list"() {
        given:
        ReportFooter reportFooter = new ReportFooter(footer: "test1", description: "test2", isDeleted: false)
        when:
        controller.list()
        then:
        response.status == 200
    }

    void "test create"() {
        given:
        ReportFooter reportFooter = new ReportFooter(footer: "test1", description: "test2", isDeleted: false)
        when:
        controller.create(reportFooter)
        then:
        response.status == 200
    }

    void "test save try success"() {
        given:
        ReportFooter reportFooter = new ReportFooter(footer: "test1", description: "test2", isDeleted: false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {}
        controller.CRUDService = mockCRUDService
        when:
        request.method = "POST"
        controller.save(reportFooter)
        then:
        response.status == 302
        response.redirectedUrl == "/reportFooter/index"
    }

    void "test save try validation Exception"() {
        given:
        ReportFooter reportFooter = new ReportFooter(footer: "test1", description: "test2", isDeleted: false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> { throw new ValidationException("Validation Exception", reportFooter.errors) }
        controller.CRUDService = mockCRUDService
        when:
        request.method = "POST"
        controller.save(reportFooter)
        then:
        response.status == 200
    }

    void "test edit"() {
        given:
        ReportFooter.metaClass.static.read = { Long id -> reportFooter }
        when:
        controller.edit(2L)
        then:
        response.status == statusVal
        response.redirectedUrl == urlStatus
        where:
        reportFooter                                                              | statusVal | urlStatus
        null                                                                      | 302       | "/reportFooter/index"
        new ReportFooter(footer: "test1", description: "test2", isDeleted: false) | 200       | null
    }

    void "test update found try success"() {
        given:
        ReportFooter reportFooter = new ReportFooter(footer: "test")
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {}
        controller.CRUDService = mockCRUDService
        when:
        request.method = "POST"
        controller.update(reportFooter)
        then:
        response.status == 302
        response.redirectedUrl == "/reportFooter/index"
    }

    void "test update found try validation exception"() {
        given:
        ReportFooter reportFooter = new ReportFooter(footer: "test")
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception",reportFooter.errors)}
        controller.CRUDService = mockCRUDService
        when:
        request.method = "POST"
        controller.update(reportFooter)
        then:
        response.status == 200
    }

    void "test show"() {
        given:
        ReportFooter.metaClass.static.read = { Long id -> reportFooter }
        when:
        controller.show(2L)
        then:
        response.status == statusVal
        response.redirectedUrl == urlStatus
        where:
        reportFooter                                                              | statusVal | urlStatus
        null                                                                      | 302       | "/reportFooter/index"
        new ReportFooter(footer: "test1", description: "test2", isDeleted: false) | 200       | null
    }

    void "test delete"() {
        given:
        ReportFooter.metaClass.static.read = { Long id -> reportFooter }
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_) >> checkVal
        controller.CRUDService = mockCRUDService
        when:
        request.method="POST"
        controller.delete(2L)
        then:
        response.status == statusVal
        response.redirectedUrl == urlStatus
        where:
        reportFooter                                                              | checkVal                                                                      | statusVal | urlStatus
        null                                                                      |  {}                                                                           |302        | "/reportFooter/index"
        new ReportFooter(footer: "test1", description: "test2", isDeleted: false) |  {}                                                                           |302        | "/reportFooter/index"
        new ReportFooter(footer: "test1", description: "test2", isDeleted: false) |  {throw new ValidationException("Validation Exception", reportFooter.errors)} |302        | "/reportFooter/index"
    }
}