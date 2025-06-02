package com.rxlogix.user

import com.rxlogix.CRUDService
import com.rxlogix.UserService
import com.rxlogix.UtilService
import com.rxlogix.config.Dashboard
import com.rxlogix.config.DateRangeType
import com.rxlogix.config.SourceProfile
import com.rxlogix.config.Tenant
import com.rxlogix.enums.DashboardEnum
import com.rxlogix.enums.SourceProfileTypeEnum
import com.rxlogix.signal.SignalIntegrationService
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import groovy.sql.Sql
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ViewHelper, Dashboard, User])
class UserGroupControllerSpec extends Specification implements DataTest, ControllerUnitTest<UserGroupController> {

    private SimpleDriverDataSource reportDataSourcePVR

    def setup() {
        ViewHelper.metaClass.static.getMessage = { String code -> return "" }
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains Dashboard, User, Role, UserRole, UserGroup, Tenant, SourceProfile, UserGroupRole, UserGroupUser, DateRangeType, FieldProfile
    }

    private makeSecurityServiceUser(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getUser(0..1) { -> user }
        securityMock.demand.updateBlindedFlagForUsersAndGroups(0..1) {}
        securityMock.demand.getAllEnabledUsers(0..1) { Long id -> [user]}
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
        adminUser.metaClass.isAdmin = { -> return true }
        return adminUser
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

    def newConn(){
        reportDataSourcePVR = new SimpleDriverDataSource()
        Properties properties = new Properties()
        properties.put("defaultRowPrefetch", grailsApplication.config.jdbcProperties.fetch_size ?: 50)
        properties.put("defaultBatchValue", grailsApplication.config.jdbcProperties.batch_size ?: 5)
        properties.put("dbdriver", "com.mysql.jdbc.Driver")
        reportDataSourcePVR.setConnectionProperties(properties)
        reportDataSourcePVR.setDriverClass(org.h2.Driver)
        reportDataSourcePVR.setUsername('sa')
        reportDataSourcePVR.setPassword('sa')
        reportDataSourcePVR.setUrl('jdbc:h2:mem:testDb')

        return reportDataSourcePVR.getConnection('sa','sa')
    }

    void "test populateDashboards"() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, name: "group")
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update(2) { def obj -> obj.save(flush: true) }
        controller.CRUDService = crudServiceMock.proxyInstance()
        when: "The dashboard with no errors created"
        params.dashboardId = [1, 2]
        controller.populateDashboards(userGroup, false)
        def result = Dashboard.findAll().findAll { gr -> gr.sharedWithGroup.find { it.name == "group" } }
        then:
        result.size() == 2
    }

    void "test create defaultRRAssignedTO false"() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.findAllUsersHavingFullName(0..2) { -> [] }
        controller.userService = mockUserService.proxyInstance()
        when:
        def result = controller.create()
        then:
        result.userGroupInstance != null
        result.availableDashboards == [[id: 1, name: 'test1()'], [id: 2, name: 'test2()']]
        result.canUpdateDefaultRRAssignedTo == true
    }

    void "test create defaultRRAssignedTO true"() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user', defaultRRAssignTo: true)
        userGroup.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.findAllUsersHavingFullName(0..2) { -> [] }
        controller.userService = mockUserService.proxyInstance()
        when:
        def result = controller.create()
        then:
        result.userGroupInstance != null
        result.availableDashboards == [[id: 1, name: 'test1()'], [id: 2, name: 'test2()']]
        result.canUpdateDefaultRRAssignedTo == false
    }

    void "test create no available dashboards"() {
        given:
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user', defaultRRAssignTo: true)
        userGroup.save(failOnError: true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.findAllUsersHavingFullName(0..2) { -> [] }
        controller.userService = mockUserService.proxyInstance()
        when:
        def result = controller.create()
        then:
        result.userGroupInstance != null
        result.availableDashboards == []
        result.canUpdateDefaultRRAssignedTo == false
    }

    void "test create with different dashboard enums"() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVQ_MAIN, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.findAllUsersHavingFullName(0..2) { -> [] }
        controller.userService = mockUserService.proxyInstance()
        when:
        def result = controller.create()
        then:
        result.userGroupInstance != null
        result.availableDashboards == [[id: 1, name: 'test1()']]
        result.canUpdateDefaultRRAssignedTo == true
    }

    void "test sorted roles"() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Role role = new Role(authority: 'ROLE_SYSTEM_CONFIGURATION', createdBy: "user", modifiedBy: "user")
        role.save(failOnError: true)
        UserGroupRole userGroupRole = new UserGroupRole(userGroup: userGroup, role: role)
        userGroupRole.save(failOnError: true)
        UserGroupUser userGroupUser = new UserGroupUser(user: adminUser, userGroup: userGroup, manager: true)
        userGroupUser.save(failOnError: true)
        when:
        def result = controller.sortedRoles()
        then:
        result.size() == 2
        result[1] == role
    }

    void "test buildUserModel"() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Role role = new Role(authority: 'ROLE_SYSTEM_CONFIGURATION', createdBy: "user", modifiedBy: "user")
        role.save(failOnError: true)
        UserGroupRole userGroupRole = new UserGroupRole(userGroup: userGroup, role: role)
        userGroupRole.save(failOnError: true)
        UserGroupUser userGroupUser = new UserGroupUser(user: adminUser, userGroup: userGroup, manager: true)
        userGroupUser.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        when:
        def result = controller.buildUserModel(userGroup)
        then:
        result.userGroupInstance != null
        result.roleList[1] == role
        result.roleMap[role] == true
    }

    void "test edit with instance not null"() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Role role = new Role(authority: 'ROLE_SYSTEM_CONFIGURATION', createdBy: "user", modifiedBy: "user")
        role.save(failOnError: true)
        UserGroupRole userGroupRole = new UserGroupRole(userGroup: userGroup, role: role)
        userGroupRole.save(failOnError: true)
        UserGroupUser userGroupUser = new UserGroupUser(user: adminUser, userGroup: userGroup, manager: true)
        userGroupUser.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 3, label: "test3", dashboardType: DashboardEnum.PVC_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)

        def mockUserService = new MockFor(UserService)
        mockUserService.demand.findAllUsersHavingFullName(0..1) { -> [] }
        mockUserService.demand.getAllEnabledUsers(0..1) { UserGroup userGrp-> [adminUser]}
        controller.userService = mockUserService.proxyInstance()
        Dashboard.metaClass.static.selectPublicByGroupId = { Long groupId ->
            new Object() {
                List list(Object o) {
                    [Dashboard.get(1), Dashboard.get(2), Dashboard.get(3)]
                }
            }
        }
        when:
        params.id = 1
        controller.edit(userGroup)
        then:
        view == '/userGroup/edit'
        model.userGroupInstance != null
        model.roleList[1] == role
        model.roleMap[role] == true
        model['userGroupRoleList'] == [role]
        model['userGroupUserList'] == [adminUser]
        model['managers'] == [adminUser.id]
        model['availableDashboards'] == [[id: 1, name: 'test1()'], [id: 2, name: 'test2()'], [id: 3, name: 'test3()']]
        model['canUpdateDefaultRRAssignedTo'] == true
    }

    void "test show with instance not null"() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Role role = new Role(authority: 'ROLE_SYSTEM_CONFIGURATION', createdBy: "user", modifiedBy: "user")
        role.save(failOnError: true)
        UserGroupRole userGroupRole = new UserGroupRole(userGroup: userGroup, role: role)
        userGroupRole.save(failOnError: true)
        UserGroupUser userGroupUser = new UserGroupUser(user: adminUser, userGroup: userGroup, manager: true)
        userGroupUser.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 3, label: "test3", dashboardType: DashboardEnum.PVC_PUBLIC, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        controller.userService = makeSecurityServiceUser(adminUser)
        Dashboard.metaClass.static.selectPublicByGroupId = { Long groupId ->
            new Object() {
                List list(Object o) {
                    [Dashboard.get(1), Dashboard.get(2), Dashboard.get(3)]
                }
            }
        }
        def userServiceMock = new MockFor(UserService)
        userServiceMock.demand.getAllEnabledUsers(1) { id ->
            return [adminUser]
        }
        controller.userService = userServiceMock.proxyInstance()
        when:
        params.id = 1
        controller.show(userGroup)
        then:
        view == '/userGroup/show'
        model.userGroupAuthority == ['app.role.ROLE_SYSTEM_CONFIGURATION']
        model.userGroupUser[0] == adminUser.username
        model.userGroupInstance != null
        model.dashboardList == ['test1', 'test2','test3']
    }

    void "test show with instance null"() {
        when:
        controller.show(null)
        then:
        response.redirectUrl == '/userGroup/index'
        flash.error != null
    }

    void "test edit with instance null"() {
        when:
        controller.edit(null)
        then:
        response.redirectUrl == '/userGroup/index'
        flash.error != null
    }

    void "test update with instance null"() {
        when:
        request.method = 'POST'
        controller.update(null)
        then:
        response.redirectUrl == '/userGroup/index'
        flash.error != null
    }

    void "test populateAuthority isupdate false"() {
        given:
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Role role = new Role(authority: 'ROLE_SYSTEM_CONFIGURATION', createdBy: "user", modifiedBy: "user")
        role.save(failOnError: true)
        UserGroupRole userGroupRole = new UserGroupRole(userGroup: userGroup, role: role)
        userGroupRole.save(failOnError: true)
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user")
        adminRole.save(flush: true)
        when:
        params['ROLE_SYSTEM_CONFIGURATION'] = 'off'
        params['ROLE_ADMIN'] = 'on'
        controller.populateAuthority(userGroup)
        then:
        Role.count() == 2
        UserGroupRole.count() == 2
    }

    void "test populateAuthority isupdate true"() {
        given:
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Role role = new Role(authority: 'ROLE_SYSTEM_CONFIGURATION', createdBy: "user", modifiedBy: "user")
        role.save(failOnError: true)
        UserGroupRole userGroupRole = new UserGroupRole(userGroup: userGroup, role: role)
        userGroupRole.save(failOnError: true)
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user")
        adminRole.save(flush: true)
        when:
        params['ROLE_SYSTEM_CONFIGURATION'] = 'off'
        params['ROLE_ADMIN'] = 'on'
        controller.populateAuthority(userGroup, true)
        then:
        Role.count() == 2
        UserGroupRole.count() == 1
    }

    void "test populateUsers isupdate false "() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Role role = new Role(authority: 'ROLE_SYSTEM_CONFIGURATION', createdBy: "user", modifiedBy: "user")
        role.save(failOnError: true)
        UserGroupRole userGroupRole = new UserGroupRole(userGroup: userGroup, role: role)
        userGroupRole.save(failOnError: true)
        UserGroupUser userGroupUser = new UserGroupUser(user: adminUser, userGroup: userGroup, manager: true)
        userGroupUser.save(failOnError: true)
        User normalUser = makeNormalUser("user2", [])
        def id1 = normalUser.id as String
        def id2 = adminUser.id as String
        def mockUtilService = Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        Sql.metaClass.static.rows = { String sql ->
            return []
        }
        Sql.metaClass.static.execute = {String sql ->
            return
        }
        Sql.metaClass.static.execute = {String sql, Map params ->
            UserGroupUser.create(userGroup, normalUser, true)
        }
        controller.utilService = mockUtilService
        when:
        params['selectedUsers'] = [normalUser.id]
        params["Manager_${id1}"] = "on"
        params["Manager_${id2}"] = "off"
        controller.populateUsers(userGroup)
        then:
        UserGroupUser.count() == 2
        def list = UserGroupUser.list()
        list[0].user == adminUser
        list[1].user == normalUser
    }

    void "test populateUsers isupdate true "() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        UserGroupUser userGroupUser = new UserGroupUser(user: adminUser, userGroup: userGroup, manager: true)
        userGroupUser.save(failOnError: true)
        User normalUser = makeNormalUser("user2", [])
        def id1 = normalUser.id as String
        def id2 = adminUser.id as String
        def mockUtilService = Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        Sql.metaClass.static.rows = { String sql ->
            return [['user_id': id1]]
        }
        Sql.metaClass.static.execute = {String sql ->
            UserGroupUser.remove(userGroup, adminUser, true)
        }
        Sql.metaClass.static.execute = {String sql, Map params ->
            UserGroupUser.create(userGroup, normalUser, true)
        }
        controller.utilService=mockUtilService
        when:
        params['selectedUsers'] = [normalUser.id]
        params["Manager_${id1}"] = "on"
        params["Manager_${id2}"] = "off"
        controller.populateUsers(userGroup, true)
        then:
        UserGroupUser.count() == 1
        def list = UserGroupUser.list()
        list[0].user == normalUser
    }

    void "test populateModel"() {
        given:
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError: true)
        SourceProfile sourceProfile = new SourceProfile(sourceId: 1, sourceAbbrev: "abv", sourceName: "name", sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                , dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError: true)
        when:
        params.name = "new_group"
        params.createdBy = "new_user"
        params.modifiedBy = "new_user"
        params['sourceProfiles'] = [sourceProfile.id]
        controller.populateModel(userGroup)
        then:
        userGroup.sourceProfiles.size() == 1
        userGroup.name == "new_group"
        userGroup.createdBy == "new_user"
        userGroup.modifiedBy == "new_user"
    }

    void "test save success"() {
        given:
        User normalUser = makeNormalUser("user2", [])
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError: true)
        SourceProfile sourceProfile = new SourceProfile(sourceId: 1, sourceAbbrev: "abv", sourceName: "name", sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                , dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user")
        adminRole.save(flush: true)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save(0..1) { theInstance -> theInstance }
        crudServiceMock.demand.update(2) { def obj -> obj.save(flush: true) }
        controller.CRUDService = crudServiceMock.proxyInstance()
        def userserviceMock = new MockFor(UserService)
        userserviceMock.demand.updateBlindedFlagForUsersAndGroups(0..1) { -> }
        userserviceMock.demand.findAllUsersHavingFullName(0..1){->}
        controller.userService = userserviceMock.proxyInstance()
        def id1 = normalUser.id as String
        def mockUtilService = Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        Sql.metaClass.static.rows = { String sql ->
            return []
        }
        Sql.metaClass.static.execute = {String sql ->
            return
        }
        UserGroupUser userGroupUser
        Sql.metaClass.static.execute = {String sql,  Map params ->
            UserGroup ug = new UserGroup(id: 1, name: "new_group", createdBy: "new_user", modifiedBy: "new_user").save(flush: true)
            userGroupUser = new UserGroupUser(id: 1, user: normalUser, userGroup: ug)
            userGroupUser.save(flush: true)
        }
        controller.utilService=mockUtilService
        def signalIntegrationServiceMock = new MockFor(SignalIntegrationService)
        signalIntegrationServiceMock.demand.updateBlindedDataToSignal(1) { UserGroup userGroupInstance ->
            assert userGroupInstance.name == userGroupUser.userGroup.name
        }
        controller.signalIntegrationService = signalIntegrationServiceMock.proxyInstance()
        when:
        request.method = 'POST'
        params.name = "new_group"
        params.createdBy = "new_user"
        params.modifiedBy = "new_user"
        params.id = 1
        params['ROLE_ADMIN'] = 'on'
        params['selectedUsers'] = [normalUser.id]
        params["Manager_${id1}"] = "on"
        params.dashboardId = [1, 2]
        params['sourceProfiles'] = [sourceProfile.id]
        controller.save()
        then:
        flash.message != null
        response.redirectUrl == '/userGroup/index'
        def userGroup = UserGroup.get(1)
        userGroup.name == "new_group"
        UserGroup.count() == 1
        UserGroupUser.get(1).userGroup.name == "new_group"
        UserGroupRole.get(1).userGroup.name == "new_group"
    }

    void "test save with validation error"() {
        given:
        User normalUser = makeNormalUser("user2", [])
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError: true)
        SourceProfile sourceProfile = new SourceProfile(sourceId: 1, sourceAbbrev: "abv", sourceName: "name", sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                , dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user")
        adminRole.save(flush: true)
        ValidationErrors errors = new ValidationErrors(new Object())
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save(0..1) { theInstance -> throw new ValidationException("message", errors) }
        controller.CRUDService = crudServiceMock.proxyInstance()
        def userserviceMock = new MockFor(UserService)
        userserviceMock.demand.isValidateName(0..1) { String name -> return true }
        userserviceMock.demand.updateBlindedFlagForUsersAndGroups(0..1) {}
        userserviceMock.demand.findAllUsersHavingFullName(0..1){->}
        def mockUtilService = Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        Sql.metaClass.static.rows = { String sql ->
            return []
        }
        Sql.metaClass.static.execute = {String sql ->
            return
        }
        Sql.metaClass.static.execute = {String sql,  Map params ->
            return new javax.xml.bind.ValidationException();
        }
        controller.utilService=mockUtilService
        controller.userService = userserviceMock.proxyInstance()
        def id1 = normalUser.id as String
        when:
        request.method = 'POST'
        params.name = "new_group"
        params.createdBy = "new_user"
        params.modifiedBy = "new_user"
        params.id = 1
        params['ROLE_ADMIN'] = 'on'
        params['selectedUsers'] = [normalUser.id]
        params["Manager_${id1}"] = "on"
        params.dashboardId = [1, 2]
        params['sourceProfiles'] = [sourceProfile.id]
        controller.save()
        then:
        flash.message == null
        view == '/userGroup/create'
        flash.error == null
    }

    void "test save with Exception error"() {
        given:
        User normalUser = makeNormalUser("user2", [])
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError: true)
        SourceProfile sourceProfile = new SourceProfile(sourceId: 1, sourceAbbrev: "abv", sourceName: "name", sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                , dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user")
        adminRole.save(flush: true)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save(0..1) { theInstance -> throw new Exception() }
        controller.CRUDService = crudServiceMock.proxyInstance()
        def userserviceMock = new MockFor(UserService)
        userserviceMock.demand.isValidateName(0..1) { String name -> return true }
        userserviceMock.demand.updateBlindedFlagForUsersAndGroups(0..1) {}
        userserviceMock.demand.findAllUsersHavingFullName(0..1){->}
        def mockUtilService = Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        Sql.metaClass.static.rows = { String sql ->
            return []
        }
        Sql.metaClass.static.execute = {String sql ->
            return
        }
        Sql.metaClass.static.execute = {String sql,  Map params ->
            return new Exception()
        }
        controller.userService = userserviceMock.proxyInstance()
        controller.utilService=mockUtilService
        def id1 = normalUser.id as String
        when:
        request.method = 'POST'
        params.name = "new_group"
        params.createdBy = "new_user"
        params.modifiedBy = "new_user"
        params.id = 1
        params['ROLE_ADMIN'] = 'on'
        params['selectedUsers'] = [normalUser.id]
        params["Manager_${id1}"] = "on"
        params.dashboardId = [1, 2]
        params['sourceProfiles'] = [sourceProfile.id]
        controller.save()
        then:
        flash.message == null
        view == '/userGroup/create'
        flash.error != null
    }

    void "test update success"() {
        given:
        User normalUser = makeNormalUser("user2", [])
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError: true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Role role = new Role(authority: 'ROLE_SYSTEM', createdBy: "user", modifiedBy: "user")
        role.save(failOnError: true)
        UserGroupRole userGroupRole = new UserGroupRole(userGroup: userGroup, role: role)
        userGroupRole.save(failOnError: true)
        UserGroupUser userGroupUser = new UserGroupUser(user: normalUser, userGroup: userGroup, manager: true)
        userGroupUser.save(failOnError: true)
        SourceProfile sourceProfile = new SourceProfile(sourceId: 1, sourceAbbrev: "abv", sourceName: "name", sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                , dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        Dashboard.metaClass.static.selectPublicByGroupId = { Long groupId ->
            new Object() {
                List list(Object o) {
                    [Dashboard.get(1), Dashboard.get(2)]
                }
            }
        }
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user")
        adminRole.save(flush: true)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update(0..1) { theInstance -> theInstance }
        crudServiceMock.demand.update(2) { def obj -> obj.save(flush: true) }
        controller.CRUDService = crudServiceMock.proxyInstance()
        def userServiceMock = new MockFor(UserService)
        userServiceMock.demand.updateBlindedFlagForUsersAndGroups(0..1) {}
        userServiceMock.demand.findAllUsersHavingFullName(0..1){->}
        def mockUtilService = Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        Sql.metaClass.static.rows = { String sql ->
            return []
        }
        Sql.metaClass.static.execute = {String sql ->
            return
        }
        Sql.metaClass.static.execute = {String sql,  Map params ->
            return
        }
        controller.utilService=mockUtilService
        controller.userService = userServiceMock.proxyInstance()
        def id1 = normalUser.id as String
        when:
        request.method = 'POST'
        params.name = "new_group"
        params.createdBy = "new_user"
        params.modifiedBy = "new_user"
        params.id = 1
        params['ROLE_ADMIN'] = 'on'
        params['selectedUsers'] = [normalUser.id]
        params["Manager_${id1}"] = "on"
        params.dashboardId = [1, 2]
        params['sourceProfiles'] = [sourceProfile.id]
        controller.update(userGroup)
        then:
        flash.message != null
        response.redirectUrl == '/userGroup/index'
        def newUserGroup = UserGroup.get(1)
        newUserGroup.name == "new_group"
        UserGroup.count() == 1
        UserGroupRole.count() == 1
        UserGroupUser.count() == 1
        UserGroupUser.get(1).userGroup.name == "new_group"
        UserGroupRole.get(1).userGroup.name == "new_group"
    }

    void "test update with validation error"() {
        given:
        User normalUser = makeNormalUser("user2", [])
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError: true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Role role = new Role(authority: 'ROLE_SYSTEM', createdBy: "user", modifiedBy: "user")
        role.save(failOnError: true)
        UserGroupRole userGroupRole = new UserGroupRole(userGroup: userGroup, role: role)
        userGroupRole.save(failOnError: true)
        UserGroupUser userGroupUser = new UserGroupUser(user: normalUser, userGroup: userGroup, manager: true)
        userGroupUser.save(failOnError: true)
        SourceProfile sourceProfile = new SourceProfile(sourceId: 1, sourceAbbrev: "abv", sourceName: "name", sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                , dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user")
        adminRole.save(flush: true)
        ValidationErrors errors = new ValidationErrors(userGroup)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update(0..1) { theInstance -> throw new ValidationException("message", errors) }
        controller.CRUDService = crudServiceMock.proxyInstance()
        def userServiceMock = new MockFor(UserService)
        userServiceMock.demand.updateBlindedFlagForUsersAndGroups(0..1) {}
        userServiceMock.demand.findAllUsersHavingFullName(0..1){->}
        def mockUtilService = Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        Sql.metaClass.static.rows = { String sql ->
            return []
        }
        Sql.metaClass.static.execute = {String sql ->
            return
        }
        Sql.metaClass.static.execute = {String sql,  Map params ->
            return new javax.xml.bind.ValidationException()
        }
        controller.utilService=mockUtilService
        controller.userService = userServiceMock.proxyInstance()
        def id1 = normalUser.id as String
        when:
        request.method = 'POST'
        params.name = "new_group"
        params.createdBy = "new_user"
        params.modifiedBy = "new_user"
        params.id = 1
        params['ROLE_ADMIN'] = 'on'
        params['selectedUsers'] = [normalUser.id]
        params["Manager_${id1}"] = "on"
        params.dashboardId = [1, 2]
        params['sourceProfiles'] = [sourceProfile.id]
        controller.update(userGroup)
        then:
        flash.message == null
        userGroup.errors != null
        view == '/userGroup/edit'
    }

    void "test update with exception error"() {
        given:
        User normalUser = makeNormalUser("user2", [])
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError: true)
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        Role role = new Role(authority: 'ROLE_SYSTEM', createdBy: "user", modifiedBy: "user")
        role.save(failOnError: true)
        UserGroupRole userGroupRole = new UserGroupRole(userGroup: userGroup, role: role)
        userGroupRole.save(failOnError: true)
        UserGroupUser userGroupUser = new UserGroupUser(user: normalUser, userGroup: userGroup, manager: true)
        userGroupUser.save(failOnError: true)
        SourceProfile sourceProfile = new SourceProfile(sourceId: 1, sourceAbbrev: "abv", sourceName: "name", sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                , dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError: true)
        Dashboard dashboard = new Dashboard(id: 1, label: "test1", dashboardType: DashboardEnum.PVQ_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        dashboard = new Dashboard(id: 2, label: "test2", dashboardType: DashboardEnum.PVR_PUBLIC, owner: normalUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user")
        adminRole.save(flush: true)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update(0..1) { theInstance -> throw new Exception("No Save Call expected") }
        controller.CRUDService = crudServiceMock.proxyInstance()
        def userServiceMock = new MockFor(UserService)
        userServiceMock.demand.updateBlindedFlagForUsersAndGroups(0..1) {}
        userServiceMock.demand.findAllUsersHavingFullName(0..1){->}
        def mockUtilService = Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        Sql.metaClass.static.rows = { String sql ->
            return []
        }
        Sql.metaClass.static.execute = {String sql ->
            return
        }
        Sql.metaClass.static.execute = {String sql,  Map params ->
            return new Exception()
        }
        controller.utilService=mockUtilService
        controller.userService = userServiceMock.proxyInstance()
        def id1 = normalUser.id as String
        when:
        request.method = 'POST'
        params.name = "new_group"
        params.createdBy = "new_user"
        params.modifiedBy = "new_user"
        params.id = 1
        params['ROLE_ADMIN'] = 'on'
        params['selectedUsers'] = [normalUser.id]
        params["Manager_${id1}"] = "on"
        params.dashboardId = [1, 2]
        params['sourceProfiles'] = [sourceProfile.id]
        controller.update(userGroup)
        then:
        flash.message == null
        flash.error != null
        view == '/userGroup/edit'
    }

    void "test delete success"() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete(0..1) { theInstance, name, String justification = null -> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityServiceUser(adminUser)
        def userServiceMock = new MockFor(UserService)
        userServiceMock.demand.getUser(0..1) { -> adminUser }
        userServiceMock.demand.updateBlindedFlagForUsersAndGroups() {}
        def signalIntegrationServiceMock = new MockFor(SignalIntegrationService)
        signalIntegrationServiceMock.demand.updateBlindedDataToSignal(1) { UserGroup userGroupInstance ->
            assert userGroupInstance == userGroup
        }
        controller.signalIntegrationService = signalIntegrationServiceMock.proxyInstance()
        controller.userService = userServiceMock.proxyInstance()
        when:
        request.method = 'POST'
        controller.delete(userGroup)
        then:
        flash.message != null
        response.redirectUrl == '/userGroup/index'
    }

    void "test delete with validation error"() {
        given:
        def adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup(id: 1, createdBy: 'user', modifiedBy: 'user')
        userGroup.save()
        ValidationErrors errors = new ValidationErrors(userGroup)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete(0..1) { theInstance, name, String justification = null -> throw new ValidationException("message", errors) }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityServiceUser(adminUser)
        when:
        request.method = 'POST'
        controller.delete(userGroup)
        then:
        flash.error != null
        response.redirectUrl == '/userGroup/show'
    }

    void "test delete when user is not admin"() {
        given:
        User normalUser = makeNormalUser("user2", [])
        UserGroup userGroup = new UserGroup(id: 1, name: "group", createdBy: 'user', modifiedBy: 'user')
        userGroup.save(failOnError: true)
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete(0..1) { theInstance, name, String justification = null -> theInstance }
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityServiceUser(normalUser)
        when:
        request.method = 'POST'
        controller.delete(userGroup)
        then:
        flash.warn != null
        response.redirectUrl == '/userGroup/index'
    }

    void "test delete with instance null"() {
        when:
        request.method = 'POST'
        controller.delete(null)
        then:
        response.redirectUrl == '/userGroup/index'
        flash.error != null
    }

    void "test ajaxProfileSearch"() {
        given:
        FieldProfile fieldProfile_1 = new FieldProfile(name: "profile1", createdBy: "user", modifiedBy: "user")
        fieldProfile_1.save(failOnError: true)
        FieldProfile fieldProfile_2 = new FieldProfile(name: "profile2", createdBy: "user", modifiedBy: "user")
        fieldProfile_2.save(failOnError: true)
        FieldProfile fieldProfile_3 = new FieldProfile(name: "field", createdBy: "user", modifiedBy: "user")
        fieldProfile_3.save(failOnError: true)
        when:
        params.term = "file"
        controller.ajaxProfileSearch()
        then:
        response.json == [[id: 1, text: 'profile1'], [id: 2, text: 'profile2']]
        response.json.text == ['profile1', 'profile2']
    }
}
