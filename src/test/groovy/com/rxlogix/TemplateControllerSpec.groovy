package com.rxlogix


import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.SourceProfile
import com.rxlogix.config.Tenant
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ReportTemplate, User, SourceProfile])
class TemplateControllerSpec extends Specification implements DataTest, ControllerUnitTest<TemplateController> {

    def setupSpec() {
        mockDomains User, Tenant, Role, UserRole, ReportTemplate, SourceProfile
    }

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
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

    private User makeAdminUser1() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user").save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true,validate:false)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isAdmin = { -> return false }
        adminUser.metaClass.getUserTeamIds = { -> [] }
        adminUser.metaClass.static.isDev = { -> return true}
        return adminUser
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getUser(0..1) { -> user }
        securityMock.demand.isCurrentUserDev(0..2) { false }
        securityMock.demand.getCurrentUser(0..1) { -> user }
        securityMock.demand.isAnyGranted(0..1){ String role-> false}
        return securityMock.proxyInstance()
    }

    void "test view not found"(){
        given:
        ReportTemplate.metaClass.static.read={null}
        when:
        controller.view()
        then:
        response.status==302
        response.redirectedUrl=="/template/index"
    }

    void "test view found with no permission to view"(){
        given:
        ReportTemplate.metaClass.static.read={Long id -> new ReportTemplate()}
        def mockUserService=Mock(UserService)
        mockUserService.isAnyGranted(_)>>{}
        controller.userService=mockUserService
        when:
        controller.view(2L)
        then:
        response.status==302
        response.redirectedUrl=="/template/index"
    }

    void "test view found with permission to view and not viewable"(){
        given:
        User u = makeAdminUser1()
        ReportTemplate.metaClass.static.read={Long id -> new ReportTemplate(owner:u)}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return true}
        controller.userService = mockUserService
        ReportTemplate.metaClass.isViewableBy = {User currentUser -> true}
        when:
        controller.view(2L)
        then:
        response.status==200
    }

    void "test viewExecutedTemplate"(){
        given:
        ReportTemplate.metaClass.static.read={Long id -> reportTemplateInstance}
        ReportTemplate.metaClass.static.getReportTemplateInstance()>>{return reportTemplateInstance}
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return isAnyGranted}
        controller.userService=mockUserService
        when:
        params.templateType = templateTypeVal
        controller.viewExecutedTemplate(2L)
        then:
        response.status == statusVal
        response.redirectedUrl == urlVal

        where:
        templateTypeVal                      | reportTemplateInstance                                                                      | isAnyGranted | statusVal | urlVal
        TemplateTypeEnum.CASE_LINE.name()    | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.DATA_TAB.name()     | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.CUSTOM_SQL.name()   | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.NON_CASE.name()     | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.TEMPLATE_SET.name() | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.ICSR_XML.name()          | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.CASE_LINE.name()    | null                                                                                        | true         |  302      | '/template/index'
        TemplateTypeEnum.CASE_LINE.name()    | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | false        |  302      | '/template/index'
    }

    void "test create"(){
        given:
        ReportTemplate.metaClass.static.read={Long id -> reportTemplateInstance}
        ReportTemplate.metaClass.static.getReportTemplateInstance()>>{return reportTemplateInstance}
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return isAnyGranted}
        controller.userService=mockUserService
        SourceProfile.metaClass.static.sourceProfilesForUser = { User currentUser -> [new SourceProfile()]}
        when:
        params.templateType= templateTypeVal
        controller.create()
        then:
        response.status == statusVal
        response.redirectedUrl == urlVal
        where:
        templateTypeVal                      | reportTemplateInstance                                                                      | isAnyGranted | statusVal | urlVal
        null                                 | null                                                                                        | true         |  302      | '/template/index'
        TemplateTypeEnum.CASE_LINE.name()    | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.DATA_TAB.name()     | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.CUSTOM_SQL.name()   | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.NON_CASE.name()     | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.TEMPLATE_SET.name() | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.ICSR_XML.name()          | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true         |  200      |   null
        TemplateTypeEnum.CASE_LINE.name()    | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | false        |  302      |  '/template/index'
    }

    void "test save"(){
        given:
        ReportTemplate.metaClass.static.read={Long id -> reportTemplateInstance}
        ReportTemplate.metaClass.static.getReportTemplateInstance()>>{return reportTemplateInstance}
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return isAnyGranted}
        controller.userService=mockUserService
        when:
        params.templateType= templateTypeVal
        request.method="POST"
        controller.save()
        then:
        response.status == statusVal
        response.redirectedUrl == urlVal
        where:
        templateTypeVal                      | reportTemplateInstance                                                                      | isAnyGranted | statusVal | urlVal
        null                                 | null                                                                                        | true         |  302      | '/template/index'
        TemplateTypeEnum.CASE_LINE.name()    | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | false        |  302      |  '/template/index'
    }

    void "test edit"(){
        given:
        ReportTemplate.metaClass.static.read={Long id -> reportTemplateInstance}
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return isAnyGranted}
        controller.userService=mockUserService
        def mockTemplateService=Mock(TemplateService)
        mockTemplateService.getUsagesCountTemplateSet(_)>>{}
        controller.templateService=mockTemplateService
        SourceProfile.metaClass.static.sourceProfilesForUser = { User currentUser -> [new SourceProfile()]}
        when:
        params.templateType= templateTypeVal
        controller.edit(2L)
        then:
        response.status == statusVal
        response.redirectedUrl == urlVal
        where:
        templateTypeVal                      | reportTemplateInstance                                                                      | isAnyGranted   | editableVal       | statusVal | urlVal
        null                                 | null                                                                                        | true           | false             |  302      | '/template/index'
        TemplateTypeEnum.CASE_LINE.name()    | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | false          | false             |  302      |  '/template/index'
        TemplateTypeEnum.CASE_LINE.name()    | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | true           | false             |  302      |  '/template/index'
    }

    void "test edit editable true"(){
        given:
        ReportTemplate reportTemplate=new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))
        ReportTemplate.metaClass.static.read={Long id -> reportTemplate}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return true}
        controller.userService = mockUserService
        reportTemplate.metaClass.isEditableBy = {User currentUser -> true}
        def mockTemplateService=Mock(TemplateService)
        mockTemplateService.getUsagesCountTemplateSet(_)>>{}
        controller.templateService=mockTemplateService
        SourceProfile.metaClass.static.sourceProfilesForUser = { User currentUser -> [new SourceProfile()]}
        when:
        controller.edit(2L)
        then:
        response.status==200
    }

    void "test copy"(){
        given:
        ReportTemplate.metaClass.static.read={Long id -> reportTemplateInstance}
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return isAnyGranted}
        controller.userService=mockUserService
        when:
        controller.copy(2L)
        then:
        response.status == statusVal
        response.redirectedUrl == urlVal
        where:
        templateTypeVal                      | reportTemplateInstance                                                                      | isAnyGranted   | viewableVal       | statusVal | urlVal
        null                                 | null                                                                                        | true           | false             |  302      | '/template/index'
        TemplateTypeEnum.CASE_LINE.name()    | new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))  | false          | false             |  302      |  '/template/index'
    }

    void "test copy not viewable"(){
        given:
        ReportTemplate reportTemplate=new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))
        ReportTemplate.metaClass.static.read={Long id -> reportTemplate}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return true}
        controller.userService = mockUserService
        reportTemplate.metaClass.isViewableBy = {User currentUser -> false}
        when:
        controller.copy(2L)
        then:
        response.status==302
        response.redirectedUrl=="/template/index"
    }

    void "test copy viewable and try success"(){
        given:
        ReportTemplate reportTemplate=new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))
        ReportTemplate.metaClass.static.read={Long id -> reportTemplate}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return true}
        controller.userService = mockUserService
        reportTemplate.metaClass.isViewableBy = {User currentUser -> true}
        def mockTemplateService=Mock(TemplateService)
        mockTemplateService.copyTemplate(_,_)>>{}
        controller.templateService=mockTemplateService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.save(_)>>{return reportTemplate}
        controller.CRUDService=mockCRUDService
        def mockExecutedConfigurationService = Mock(ExecutedConfigurationService)
        mockExecutedConfigurationService.createReportTemplate(_) >> {return reportTemplate}
        controller.executedConfigurationService = mockExecutedConfigurationService
        when:
        controller.copy(2L)
        then:
        response.status==302
        response.redirectedUrl=="/template/view"
    }

    void "test copy viewable and try validation exception"(){
        given:
        ReportTemplate reportTemplate=new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))
        ReportTemplate.metaClass.static.read={Long id -> reportTemplate}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return true}
        controller.userService = mockUserService
        reportTemplate.metaClass.isViewableBy = {User currentUser -> true}
        def mockTemplateService=Mock(TemplateService)
        mockTemplateService.copyTemplate(_,_)>>{}
        controller.templateService=mockTemplateService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.save(_)>>{throw new ValidationException("ve",reportTemplate.errors)}
        controller.CRUDService=mockCRUDService
        when:
        controller.copy(2L)
        then:
        response.status==302
        response.redirectedUrl=="/template/index"
    }

    void "test delete"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return isGranted}
        controller.userService = mockUserService
        ReportTemplate.metaClass.static.get={Long id -> reportTemplate}
        when:
        params.id=paramsId
        request.method="POST"
        controller.delete()
        then:
        response.status==statusVal
        response.redirectedUrl==urlStatus
        where:
        reportTemplate                                                                                           |paramsId       |isGranted  | editVal    |statusVal          |urlStatus
        null                                                                                                     |0L             | false     |false      |302                |"/template/index"
        new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))               |1L             | false     |false      |302                |"/template/index"
    }

    void "test delete not editable"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return true}
        controller.userService = mockUserService
        ReportTemplate reportTemplate=new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))
        ReportTemplate.metaClass.static.get={Long id -> reportTemplate}
        reportTemplate.metaClass.isEditableBy = {User currentUser -> false}
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/template/index"
    }

    void "test delete editabl with usage count"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return true}
        controller.userService = mockUserService
        ReportTemplate reportTemplate=new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))
        ReportTemplate.metaClass.static.get={Long id -> reportTemplate}
        reportTemplate.metaClass.isEditableBy = {User currentUser ->true}
        def mockTemplateService=Mock(TemplateService)
        mockTemplateService.getUsagesCount(_)>>{return 1}
        controller.templateService=mockTemplateService
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/template/index"
    }

    void "test delete editable with template set usage count"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return true}
        controller.userService = mockUserService
        ReportTemplate reportTemplate=new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))
        ReportTemplate.metaClass.static.get={Long id -> reportTemplate}
        reportTemplate.metaClass.isEditableBy = {User currentUser ->true}
        def mockTemplateService=Mock(TemplateService)
        mockTemplateService.getUsagesCount(_)>>{return 0}
        mockTemplateService.getUsagesCountTemplateSet(_)>>{return 1}
        controller.templateService=mockTemplateService
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/template/index"
    }

    void "test delete try success"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return true}
        controller.userService = mockUserService
        ReportTemplate reportTemplate=new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))
        ReportTemplate.metaClass.static.get={Long id -> reportTemplate}
        reportTemplate.metaClass.isEditableBy = {User currentUser ->true}
        def mockTemplateService=Mock(TemplateService)
        mockTemplateService.getUsagesCount(_)>>{return 0}
        mockTemplateService.getUsagesCountTemplateSet(_)>>{return 0}
        controller.templateService=mockTemplateService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_)>>{}
        controller.CRUDService=mockCRUDService
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/template/index"
    }

    void "test delete validation exception"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return true}
        controller.userService = mockUserService
        ReportTemplate reportTemplate=new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))
        ReportTemplate.metaClass.static.get={Long id -> reportTemplate}
        reportTemplate.metaClass.isEditableBy = {User currentUser ->true}
        def mockTemplateService=Mock(TemplateService)
        mockTemplateService.getUsagesCount(_)>>{return 0}
        mockTemplateService.getUsagesCountTemplateSet(_)>>{return 0}
        controller.templateService=mockTemplateService
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_)>>{throw new ValidationException("ve",reportTemplate.errors)}
        controller.CRUDService=mockCRUDService
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/template/view/1"
    }

    void "test update not saved"(){
        when:
        request.method=="GET"
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/template/index"
    }

    void "test update saved"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        mockUserService.isAnyGranted(_)>>{return isGranted}
        controller.userService = mockUserService
        ReportTemplate.metaClass.static.get={Long id -> reportTemplate}
        when:
        params.id=paramsId
        request.method="POST"
        controller.update()
        then:
        response.status==statusVal
        response.redirectedUrl==urlStatus
        where:
        reportTemplate                                                                                           |paramsId       |isGranted  | editVal    |statusVal          |urlStatus
        null                                                                                                     |0L             | false     |false      |302                |"/template/index"
        new ReportTemplate(name:"test", owner: new User(preference: new Preference(locale: 'en')))               |1L             | false     |false      |302                |"/template/index"
    }
}
