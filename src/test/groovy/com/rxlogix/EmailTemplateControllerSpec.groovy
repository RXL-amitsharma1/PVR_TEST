package com.rxlogix

import com.rxlogix.config.EmailTemplate
import com.rxlogix.config.Tenant
import com.rxlogix.enums.EmailTemplateTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ViewHelper, EmailTemplate, User])
class EmailTemplateControllerSpec extends Specification implements DataTest, ControllerUnitTest<EmailTemplateController> {

    def setupSpec() {
        mockDomains EmailTemplate, User, Role, UserRole, Tenant
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser(0..1) { -> user }
        securityMock.demand.getAllowedSharedWithUsersForCurrentUser { -> [user] }
        securityMock.demand.getAllowedSharedWithGroupsForCurrentUser { -> [] }
        securityMock.demand.isCurrentUserAdmin(0..2) { false }
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

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user").save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    private User makeUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def user = new User(username: 'user', password: 'user', fullName: "User", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        user.addToTenants(tenant)
        user.save(failOnError: true)
        return user
    }

    def setup() {
    }

    def cleanup() {
    }

    void "test EmailTemplate can be created"() {
        given:
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)

        EmailTemplate resultInstance
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { theInstance -> resultInstance = theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()

        when:
        params.id = null
        params.name = "test"
        params.description = "test description"
        params.body = "test body"
        params.type = EmailTemplateTypeEnum.PUBLIC.name()
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        controller.save()
        then: "The Aggregate TaskTemplate is created and page redirected"
        response.status == 302
        response.redirectedUrl == "/emailTemplate/index"
        resultInstance.type == EmailTemplateTypeEnum.PUBLIC
        resultInstance.name == "test"
        resultInstance.description == "test description"
        resultInstance.body == "test body"
        resultInstance.owner == adminUser
    }


    void "test EmailTemplate  can be updated"() {
        given:
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)

        EmailTemplate resultInstance
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { theInstance -> resultInstance = theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()

        EmailTemplate template = new EmailTemplate(id: 1, name: "test", description: "test description", body: "test body", type: EmailTemplateTypeEnum.PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)

        when:
        params.id = 1
        params.name = "test1"
        params.description = "test description1"
        params.body = "test body1"
        params.type = EmailTemplateTypeEnum.USER.name()
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        controller.update()
        then:
        response.status == 302
        response.redirectedUrl == "/emailTemplate/index"
        resultInstance.type == EmailTemplateTypeEnum.USER
        resultInstance.name == "test1"
        resultInstance.description == "test description1"
        resultInstance.body == "test body1"
        resultInstance.owner == adminUser
    }

    void "test create action, it renders the create page"() {
        given:

        when: "Call create action."

        controller.create()

        then: "Renders the view page."
        view == '/emailTemplate/create'
        model.emailTemplateInstance != null
    }

    void "test show action, it renders the show page"() {
        given:
        def adminUser = makeAdminUser()
        EmailTemplate template = new EmailTemplate(id: 1L, name: "test", description: "test description", body: "test body", type: EmailTemplateTypeEnum.PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)

        when: "Call show action."
        controller.show(1L)

        then: "Renders the show page."
        view == '/emailTemplate/show'
        model.emailTemplate.id == 1
    }

    void "test delete action, it deletes the show page"() {
        given:
        def adminUser = makeAdminUser()
        EmailTemplate template = new EmailTemplate(id: 1, name: "test", description: "test description", body: "test body", type: EmailTemplateTypeEnum.PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)
        def crudServiceMock = new MockFor(CRUDService)
        EmailTemplate resultInstance
        crudServiceMock.demand.softDelete { theInstance, name, String justification = null -> resultInstance = theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        when: "Call delete action."
        controller.delete(1)

        then: "redirect to index page"
        response.status == 302
        response.redirectedUrl == "/emailTemplate/index"
        resultInstance.id == 1
        flash.message != null
    }

    void "test list method"() {
        given:
        Holders.grailsApplication.mainContext.beanFactory.registerSingleton("dynamicReportService", new DynamicReportService())
        Holders.grailsApplication.mainContext.beanFactory.registerSingleton("applicationSettingsService", new ApplicationSettingsService())
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "message" }
        def adminUser = makeAdminUser()
        EmailTemplate template = new EmailTemplate(id: 1, name: "test", description: "test description", body: "test body", type: EmailTemplateTypeEnum.PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)
        template = new EmailTemplate(id: 2, name: "test2", description: "test description", body: "test body", type: EmailTemplateTypeEnum.PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)

        when:
        controller.list()
        def result = JSON.parse(response.text)
        then:
        response.status == 200
        result.size() == 2
        result[0].name == "test"
        result[1].name == "test2"

    }

    void "test save success"(){
        given:
        User adminUser = makeAdminUser()
        EmailTemplate emailTemplate=new EmailTemplate()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        controller.userService=mockUserService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        controller.save()
        then:
        response.status==302
        response.redirectedUrl=="/emailTemplate/index"
    }

    void "test save validation exception"(){
        given:
        User adminUser = makeAdminUser()
        EmailTemplate emailTemplate=new EmailTemplate()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        controller.userService=mockUserService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception", emailTemplate.errors)}
        controller.CRUDService=mockCRUDService
        when:
        request.method="POST"
        controller.save()
        then:
        response.status==200
    }

    void "test edit not found"(){
        given:
        EmailTemplate.metaClass.static.read={null}
        when:
        controller.edit()
        then:
        response.status==302
        response.redirectedUrl=="/emailTemplate/index"
    }

    void "test edit found"(){
        given:
        EmailTemplate emailTemplate=new EmailTemplate()
        EmailTemplate.metaClass.static.read={Long id->emailTemplate}
        when:
        controller.edit(2L)
        then:
        response.status==200
    }

    void "test axajList method for user"() {
        given:
        def adminUser = makeAdminUser()
        def normalUser = makeUser()

        // Save templates
        new EmailTemplate(
                id: 1,
                name: "test1",
                description: "test description",
                body: "test body",
                type: EmailTemplateTypeEnum.USER,
                owner: adminUser,
                createdBy: "user",
                modifiedBy: "user"
        ).save(flush: true, failOnError: true)

        new EmailTemplate(
                id: 2,
                name: "test2",
                description: "test description",
                body: "test body",
                type: EmailTemplateTypeEnum.PUBLIC,
                owner: normalUser,
                createdBy: "user",
                modifiedBy: "user"
        ).save(flush: true, failOnError: true)

        new EmailTemplate(
                id: 3,
                name: "test3",
                description: "test description",
                body: "test body",
                type: EmailTemplateTypeEnum.USER,
                owner: normalUser,
                createdBy: "user",
                modifiedBy: "user"
        ).save(flush: true, failOnError: true)

        // Mock userService to return adminUser
        controller.userService = [
                getCurrentUser: { -> adminUser },
                getUser: { -> adminUser },
                getUserByUsername: { String username -> adminUser }
        ] as UserService

        when:
        params.user = param.toString()
        controller.axajList()
        def result = grails.converters.JSON.parse(response.text)

        then:
        response.status == 200
        result.data.size() == 1
        result.data[0].name == expectedName

        where:
        param | expectedName
        true  | "test2"
    }


    void "test axajSave  method"() {
        given:
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)

        EmailTemplate resultInstance
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { theInstance -> resultInstance = theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()

        EmailTemplate template = new EmailTemplate(id: 1, name: "test", description: "test description", body: "test body", type: EmailTemplateTypeEnum.PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)

        when:
        params.id = 1
        params.body = "test body1"
        controller.axajSave()
        def result = JSON.parse(response.text)
        then:
        response.status == 200
        result.data == 1
        resultInstance.body == "test body1"
    }

    void "test axajDelete  method"() {
        given:
        def adminUser = makeAdminUser()

        EmailTemplate resultInstance
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.delete { theInstance -> resultInstance = theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()

        EmailTemplate template = new EmailTemplate(id: 1, name: "test", description: "test description", body: "test body", type: EmailTemplateTypeEnum.PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        template.save(flush: true)

        when:
        params.id = 1
        controller.axajDelete()
        def result = JSON.parse(response.text)
        then:
        response.status == 200
        resultInstance.id == 1
    }

}
