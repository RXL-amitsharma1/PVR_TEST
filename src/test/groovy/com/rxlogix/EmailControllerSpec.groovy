package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.config.Email
import com.rxlogix.config.PeriodicReportConfiguration
import com.rxlogix.config.Tenant
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants, User, Email, PeriodicReportConfiguration, Configuration])
class EmailControllerSpec extends Specification implements DataTest, ControllerUnitTest<EmailController> {

    def setupSpec() {
        mockDomains User, Role, UserRole, Tenant, Email, Configuration, PeriodicReportConfiguration
    }

    private Tenant fetchTenant(Long id) {
        def tenant = Tenant.get(id)
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = id
        return tenant.save()
    }

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user").save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(fetchTenant(1L))
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    private Email makeEmailForTenant(Long tenantId, User user) {
        def email = new Email(email: 'test@rxlogix.com', description: 'Test Email', createdBy: user.username, modifiedBy: user.username, tenantId: fetchTenant(tenantId).id)
        email.save(failOnError: true)
        return email
    }

    void "test list"() {
        given:
        def adminUser = makeAdminUser()
        def email = makeEmailForTenant(tenantId, adminUser)
        Tenants.metaClass.static.currentId = { tenantId }

        when:
        controller.list()

        then:
        response.status == 200
        response.json[0].email == emailId

        where:
        tenantId | emailId
        1L       | "test@rxlogix.com"

    }

    void "test index"(){
        when:
        controller.index()

        then:
        response.status==200
    }

    void "test create"(){
        given:
        def adminUser = makeAdminUser()
        def email = makeEmailForTenant(1L, adminUser)
        Tenants.metaClass.static.currentId = { 1L }

        when:
        controller.create(email)

        then:
        response.status == 200
    }

    void "test Save -- Success"(){
        given:
        def adminUser = makeAdminUser()
        def email = makeEmailForTenant(1L, adminUser)
        Tenants.metaClass.static.currentId = { 1L }
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {}
        controller.CRUDService = mockCRUDService

        when:
        request.method = 'POST'
        controller.save(email)

        then:
        response.status == 302
        response.redirectedUrl == '/email/index'
    }

    void "test Save -- Failure"(){
        given:
        def adminUser = makeAdminUser()
        def email = makeEmailForTenant(1L, adminUser)
        Tenants.metaClass.static.currentId = { 1L }
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception", email.errors)}
        controller.CRUDService = mockCRUDService

        when:
        request.method = 'POST'
        controller.save(email)

        then:
        response.status == 200
    }

    void "test Edit"(){
        given:
        def adminUser = makeAdminUser()
        def email = makeEmailForTenant(1L, adminUser)
        Tenants.metaClass.static.currentId = { 1L }
        Email.metaClass.static.read = {email}

        when:
        controller.edit(1L)

        then:
        response.status == 200
    }

    void "test Edit -- Not Found"(){
        given:
        Email.metaClass.static.read = {null}

        when:
        controller.edit(1L)

        then:
        response.status == 302
        response.redirectedUrl == '/email/index'
    }

    void "test Update -- Success"(){
        given:
        def adminUser = makeAdminUser()
        def email = makeEmailForTenant(1L, adminUser)
        Tenants.metaClass.static.currentId = { 1L }
        Email.metaClass.static.get = {email}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {}
        controller.CRUDService = mockCRUDService

        when:
        request.method = 'POST'
        params.email = "testNew@rxlogix.com"
        params.description = "New Description"
        controller.update(1L)

        then:
        response.status == 302
        response.redirectedUrl == '/email/index'
    }

    void "test Update -- Failure"(){
        given:
        def adminUser = makeAdminUser()
        def email = makeEmailForTenant(1L, adminUser)
        Tenants.metaClass.static.currentId = { 1L }
        Email.metaClass.static.get = {email}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception", email.errors)}
        controller.CRUDService = mockCRUDService

        when:
        request.method = 'POST'
        params.id = 1L
        params.email = "testNew@rxlogix.com"
        params.description = "New Description"
        controller.update(1L)

        then:
        response.status == 200
    }

    void "test Show"(){
        given:
        def adminUser = makeAdminUser()
        def email = makeEmailForTenant(1L, adminUser)
        Tenants.metaClass.static.currentId = { 1L }
        Email.metaClass.static.read = {email}

        when:
        controller.show(1L)

        then:
        response.status == 200
    }

    void "test Delete -- success"(){
        given:
        def adminUser = makeAdminUser()
        def email = makeEmailForTenant(1L, adminUser)
        Tenants.metaClass.static.currentId = { 1L }
        Email.metaClass.static.read = {email}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_) >> {}
        controller.CRUDService = mockCRUDService

        when:
        request.method = 'PUT'
        controller.delete(1L)

        then:
        response.status == 302
        response.redirectedUrl == '/email/index'
    }

    void "test Delete -- failure"(){
        given:
        def adminUser = makeAdminUser()
        def email = makeEmailForTenant(1L, adminUser)
        Tenants.metaClass.static.currentId = { 1L }
        Email.metaClass.static.read = {email}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_) >> {throw new ValidationException("Validation Exception", email.errors)}
        controller.CRUDService = mockCRUDService

        when:
        request.method = 'PUT'
        controller.delete(1L)

        then:
        response.status == 302
        response.redirectedUrl == '/email/index'
    }

    void "test Ajax Add -- Success"(){
        given:
        Tenants.metaClass.static.currentId = { 1L }
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {}
        controller.CRUDService = mockCRUDService

        when:
        params.id = 1L
        params.email = "testNew@rxlogix.com"
        params.description = "New Description"
        controller.axajAdd()

        then:
        response.status == 200
        response.json.message == "ok"
        response.json.status == true
    }

    void "test Ajax Add -- Failure"(){
        given:
        def adminUser = makeAdminUser()
        def email = makeEmailForTenant(1L, adminUser)
        Tenants.metaClass.static.currentId = { 1L }
        Email.metaClass.static.read = {email}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception", email.errors)}
        controller.CRUDService = mockCRUDService

        when:
        params.id = 1L
        params.email = "testNew@rxlogix.com"
        params.description = "New Description"
        controller.axajAdd()

        then:
        response.status == 200
        response.json.status == false
        response.json.message == ""
        println response.status
    }

    void "test allEmails"(){
        given:
        def emailList = ["test@rxlogix.com", "test2@rxlogix.com", "test3@rxlogix.com"]
        Configuration.metaClass.static.read = {new Configuration()}
        PeriodicReportConfiguration.metaClass.static.read = {new PeriodicReportConfiguration()}
        def mockUserService = Mock(UserService)
        mockUserService.getAllEmails(_) >> {emailList}
        mockUserService.getAllEmails() >> {emailList}
        controller.userService = mockUserService

        when:
        controller.allEmails(id)

        then:
        response.status == 200
        response.json == emailList

        where:
        id << [1L, null]
    }

    void "test allEmailsForCC"(){
        given:
        def emailList = ["test@rxlogix.com", "test2@rxlogix.com", "test3@rxlogix.com"]
        def mockUserService = Mock(UserService)
        mockUserService.getAllEmailsForCC(_) >> {emailList}
        controller.userService = mockUserService

        when:
        controller.allEmailsForCC("")

        then:
        response.status == 200
    }
}