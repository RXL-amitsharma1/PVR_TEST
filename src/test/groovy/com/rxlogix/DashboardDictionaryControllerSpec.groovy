package com.rxlogix

import com.rxlogix.config.Dashboard
import com.rxlogix.config.Tenant
import com.rxlogix.enums.DashboardEnum
import com.rxlogix.user.*
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants, Dashboard, User])
class DashboardDictionaryControllerSpec extends Specification implements DataTest, ControllerUnitTest<DashboardDictionaryController>  {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains Dashboard, User, Role, UserRole,Tenant
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser(0..1) { -> user }
        securityMock.demand.getAllowedSharedWithUsersForCurrentUser { -> [user] }
        securityMock.demand.getAllowedSharedWithGroupsForCurrentUser { -> [] }
        securityMock.demand.isCurrentUserAdmin(0..2) { false }
        securityMock.demand.isAnyGranted(0..1) { true }
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

    void "test index"(){
        when:
        controller.index()
        then:
        response.status==200
    }

    void "test save success"(){
        given:
        Dashboard dashboard= new Dashboard()
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        controller.userService=mockUserService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {}
        controller.CRUDService=mockCRUDService
        when:
        request.method='POST'
        controller.save()
        then:
        response.status==302
        response.redirectedUrl=="/dashboardDictionary/index"
    }

    void "test save validation exception"(){
        given:
        Dashboard dashboard = new Dashboard()
        User adminUser = makeAdminUser()
        def mockUserService= Mock(UserService)
        mockUserService.getAllowedSharedWithUsersForCurrentUser()>>{}
        mockUserService.getAllowedSharedWithGroupsForCurrentUser()>>{}
        controller.userService = mockUserService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception", dashboard.errors)}
        controller.CRUDService=mockCRUDService
        when:
        request.method='POST'
        controller.save()
        then:
        response.status==200
    }

    void "test edit not found"(){
        given:
        Dashboard dashboard = new Dashboard()
        Dashboard.metaClass.static.read={null}
        when:
        controller.edit(1L)
        then:
        response.status==302
        response.redirectedUrl=="/dashboardDictionary/index"
    }

    void "test edit found"(){
        given:
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)
        Dashboard dashboard = new Dashboard(id: 1, label: "test dashboard", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group")], createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        Dashboard.metaClass.static.read={Long id -> dashboard}
        when:
        controller.edit(1L)
        then:
        response.status==200
    }

    void "test updateModal not found"(){
            given:
            Dashboard updateDashboard = new Dashboard()
            Dashboard.metaClass.static.get={null}
            when:
            controller.updateModal(1L)
            then:
            response.status==302
            response.redirectedUrl=="/dashboardDictionary/index"
    }

    void "test updateModal found"(){
        given:
        Dashboard updateDashboard = new Dashboard()
        Dashboard.metaClass.static.get={Long id -> updateDashboard}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {}
        controller.CRUDService=mockCRUDService
        when:
        controller.updateModal(1L)
        then:
        response.status==200
    }

    void "test importJson"(){
        given:
        def mockimportService=Mock(ImportService)
        mockimportService.importDashboards(_)>>{}
        controller.importService=mockimportService
        when:
        params?.json=true
        controller.importJson()
        then:
        response.status==302
        response.redirectedUrl=="/dashboardDictionary/index"
    }

    void "Dashboard can be created"() {
        given:
        def adminUser = makeAdminUser()
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { dashboard -> dashboard }
        controller.CRUDService = crudServiceMock.proxyInstance()

        controller.userService = makeSecurityService(adminUser)


        when: "The dashboard with no errors created"
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params.id = null
        params.label = "test dashboard"
        params.dashboardType = DashboardEnum.PVR_PUBLIC
        String shareUsers = ["User_" + adminUser.id]
        params.sharedWith = shareUsers
        request.method = 'POST'
        controller.save()
        then: "The dashboard is created and page redirected"
        response.status == 302
        response.redirectedUrl == "/dashboardDictionary/index"
    }

    void "Dashboard can be updated"() {
        given:
        def adminUser = makeAdminUser()

        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { dashboard -> dashboard }
        controller.CRUDService = crudServiceMock.proxyInstance()

        controller.userService = makeSecurityService(adminUser)
        Dashboard dashboard = new Dashboard(id: 1, label: "test dashboard", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)

        when: "The dashboard with no errors created"
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params.id = 1
        params.label = "test1"
        params.dashboardType = DashboardEnum.PVR_PUBLIC
        request.method = 'POST'
        controller.update()
        then: "The dashboard is created and page redirected"
        response.status == 302
        response.redirectedUrl == "/dashboardDictionary/index"
    }

    void "test list action"() {
        given:
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "type" }
        def adminUser = makeAdminUser()

        String label = "test1"
        Dashboard dashboard = new Dashboard(id: 1, label: label, dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group")], createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        when: "Call list action."
        controller.list()

        then: "Renders JSON."
        response.json != null
        response.json[0].owner == "Peter Fletcher"
        response.json[0].sharedWith == "Peter Fletcher, Peter Fletcher"
        response.json[0].sharedWithGroup == "group"
        response.json[0].label == label
        response.json[0].dashboardType == "type"
        response.json[0].id == 1
    }

    void "test create action, it renders the create page"() {
        given:
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)

        when: "Call index action."
        controller.create()

        then: "Renders the view page."
        view == '/dashboardDictionary/create'
        model.dashboard != null
        model.users.size() == 1
        model.userGroups.size() == 0
    }

    void "test show action, it renders the show page"() {
        given:
        def adminUser = makeAdminUser()
        Tenants.metaClass.static.currentId = { -> return 1}
        def importServiceMock = new MockFor(ImportService)
        importServiceMock.demand.getDashboardAsJSON(0..1) { Dashboard dashboardInstance ->
            return [:] as JSON
        }
        controller.importService = importServiceMock.proxyInstance()
        controller.userService = makeSecurityService(adminUser)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group")], createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        Dashboard.metaClass.static.read = {Long id -> return dashboard}
        when: "Call show action."
        params.id = 1
        controller.show()

        then: "Renders the show page."
        model.dashboard.id == 1
    }

    void "test delete action, it deletes the show page"() {
        given:
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)
        Dashboard dashboard = new Dashboard(id: 1, label: "test dashboard", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser,
                sharedWith: [adminUser, adminUser], sharedWithGroup: [new UserGroup(name: "group")], createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete { theInstance, name, String justification = null -> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        when: "Call delete action."
        request.method = 'POST'
        params.id = 1
        controller.delete()

        then: "redirect to index page"
        response.status == 302
        response.redirectedUrl == "/dashboardDictionary/index"
        flash.message != null
    }

    void "test edit modal"() {
        given:
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)
        Dashboard dashboard = new Dashboard(id: 1, label: "test dashboard", dashboardType: DashboardEnum.PVC_PUBLIC, owner: adminUser,
                sharedWith: [adminUser, adminUser], sharedWithGroup: [],
                createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)

        when: "Call edit modal action"
        controller.editModal(1L)

        then: "Renders JSON"
        response.json != null
    }

    void "test update modal"(){
        given:
        def adminUser = makeAdminUser()

        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { dashboard -> dashboard }
        controller.CRUDService = crudServiceMock.proxyInstance()

        controller.userService = makeSecurityService(adminUser)
        Dashboard dashboard = new Dashboard(id: 1, label: "test dashboard", dashboardType: DashboardEnum.PVC_PUBLIC, owner: adminUser,
                createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)

        when:
        params.label = "New Label"
        params.dashboardType = DashboardEnum.PVC_USER
        params.icon = "md md-settings"
        request.method = 'POST'
        controller.updateModal(1L)

        then:
        response.status == 200
    }

}
