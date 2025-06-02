package com.rxlogix


import com.rxlogix.config.AdvancedAssignment
import com.rxlogix.config.Tenant
import com.rxlogix.user.*
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants])
class AdvancedAssignmentControllerSpec extends Specification implements DataTest, ControllerUnitTest<AdvancedAssignmentController>{
    private makeUserService() {
        def userMock = new MockFor(UserService)
        userMock.demand.isCurrentUserDev(1..2) { false }
        return userMock.createMock()
    }
    public static final user = "unitTest"

    def qualityService
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
        def userRole = new Role(authority: 'ROLE_SYSTEM_CONFIGURATION', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user")
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
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

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, Tenant, AdvancedAssignment, Role, UserRole, UserGroup
    }

    @Unroll
    void "test list"() {
        given:
        User normalUser = makeNormalUser("user2", [])
        AdvancedAssignment advancedAssignmentInstance=new AdvancedAssignment(name:'testRule1', category:'pvcCentral',description:'Testcases', qualityChecked:'false',
                assignmentQuery:'update c set b',isDeleted:'true',dateCreated:'user',lastUpdated:'user', assignedUser: normalUser
        )
        advancedAssignmentInstance.save(failOnError:true,validate: false)
        Tenants.metaClass.static.currentId = { return 1L }
        when:"call list action"
        controller.list()
        then:"It gives JSON response"
        response.status==200
    }
    @Unroll
    void "test save"(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        AdvancedAssignment advancedAssignmentInstance=new AdvancedAssignment(name:'testRule1', category:'pvcCentral',description:'Testcases', qualityChecked:'false',
                assignmentQuery:'update c set b',isDeleted:'true',dateCreated:'user',lastUpdated:'user', assignedUser: normalUser_1
                )
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { theInstance -> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityService(normalUser_1)
        Tenants.metaClass.static.currentId = { return 1L }
        when:
        request.method = 'POST'
        controller.save()
        then:
        response.status == 302
    }
    @Unroll
    void "edit"(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        AdvancedAssignment advancedAssignmentInstance=new AdvancedAssignment(name:'testRule1', category:'pvcCentral',description:'Testcases', qualityChecked:'false',
                assignmentQuery:'update c set b',isDeleted:'true',dateCreated:'user',lastUpdated:'user', assignedUser: normalUser_1
        )
        advancedAssignmentInstance.save(failOnError:true,validate: false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        mockUserService.demand.getUser(0..1){-> adminUser}
        Tenants.metaClass.static.currentId = { -> return 1}
        Long id = 1L;
        when:
        controller.edit(id)
        then:
        response.status == 200

    }

    void "test create"(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        AdvancedAssignment advancedAssignmentInstance=new AdvancedAssignment(name:'testRule1', category:'pvcCentral',description:'Testcases', qualityChecked:'false',
                assignmentQuery:'update c set b',isDeleted:'true',dateCreated:'user',lastUpdated:'user', assignedUser: normalUser_1
        ).save(flush:true)
        def mockuserservice= Mock(UserService)
        mockuserservice.getCurrentUser()>>{normalUser_1}
        controller.userService=mockuserservice
        when:
        controller.create(advancedAssignmentInstance)
        then:
        response.status==200
    }
    void "test update "(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        AdvancedAssignment advancedAssignmentInstance=new AdvancedAssignment(name:'testRule1', category:'pvcCentral',description:'Testcases', qualityChecked:'false',
                assignmentQuery:'update c set b',isDeleted:'true',dateCreated:'user',lastUpdated:'user', assignedUser: normalUser_1
        )

        advancedAssignmentInstance.save(failOnError:true,validate: false)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { theInstance-> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityService(normalUser_1)
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params.name='testRule12'
        params.category=advancedAssignmentInstance.category
        params.description=advancedAssignmentInstance
        params.qualityChecked=advancedAssignmentInstance
        params.assignmentQuery=advancedAssignmentInstance
        Long id = 1L
        controller.update(id)
        then:
        response.status == 302
    }

    void "test show"(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        AdvancedAssignment advancedAssignmentInstance=new AdvancedAssignment(name:'testRule1', category:'pvcCentral',description:'Testcases', qualityChecked:'false',
                assignmentQuery:'update c set b',isDeleted:'true',dateCreated:'user',lastUpdated:'user', assignedUser: normalUser_1
        )
        advancedAssignmentInstance.save(failOnError:true,validate: false)
        Tenants.metaClass.static.currentId = { -> return 1}
        when:
        controller.show(1L)
        then:
        response.status == 200
    }

    void "test delete "(){
        given:
        User normalUser_1 = makeNormalUser("user2", [])
        AdvancedAssignment advancedAssignmentInstance=new AdvancedAssignment(name:'testRule1', category:'pvcCentral',description:'Testcases', qualityChecked:'false',
                assignmentQuery:'update c set b',isDeleted:'true',dateCreated:'user',lastUpdated:'user', assignedUser: normalUser_1
        )
        advancedAssignmentInstance.save(failOnError:true,validate: false)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete { theInstance,name, String justification = null -> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        when:
        request.method = 'POST'
        controller.delete(1L)
        then:
        response.status == 302
        flash.message!=null
    }


}