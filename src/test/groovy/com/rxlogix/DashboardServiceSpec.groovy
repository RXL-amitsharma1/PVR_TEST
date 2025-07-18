package com.rxlogix

import com.rxlogix.config.Dashboard
import com.rxlogix.config.ReportWidget
import com.rxlogix.config.Tenant
import com.rxlogix.enums.DashboardEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ViewHelper, User])
class DashboardServiceSpec extends Specification implements DataTest, ServiceUnitTest<DashboardService> {

    def setup() {
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' ->
            return code
        }
        ViewHelper.metaClass.static.isPvqModule = { request ->
            if (request.forwardURI == '/quality') {
                return true
            }
            return false
        }

        ViewHelper.metaClass.static.isPvcModule = { request ->
            if (request.forwardURI == '/central') {
                return true
            }
            return false
        }

        
        ViewHelper.metaClass.static.isPvPModule = { request ->
            if (request.forwardURI == '/publisher') {
                return true
            }
            return false
        }

    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains Dashboard, User, Role, UserRole, ReportWidget, Tenant
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



    
    private makeUserService(User user) {
        def serviceMock = new MockFor(UserService)
        serviceMock.demand.getCurrentUser(0..2) { -> user }
        return serviceMock.proxyInstance()
    }

    void "test getDashboard method"() {
        given:
        def adminUser = makeAdminUser()
        String label = "test1"
        service.userService = makeUserService(adminUser)
        Dashboard dashboard = new Dashboard(id: 1, label: label, dashboardType: DashboardEnum.PVQ_MAIN, owner: adminUser, createdBy: "user", modifiedBy: "user")
        dashboard.save(flush: true)
        Dashboard pvrDashboard = new Dashboard(id: 2, label: "test1", dashboardType: DashboardEnum.PVR_MAIN, owner: adminUser, createdBy: "user", modifiedBy: "user")
        pvrDashboard.save(flush: true)
        def mockUserService= new MockFor(UserService)
        mockUserService.demand.getCurrentUser(1..2){-> return adminUser}
        service.userService=mockUserService.proxyInstance()

        ViewHelper.metaClass.static.getMessage = {String code -> code == "app.label.dashboard.main" ? label : code}

        when:
        def res = service.getDashboard(params, new HashMap(forwardURI: forwardURI))

        then:
        res?.id == dashboardId
        where:
        params  | forwardURI   || dashboardId
        [:]     | "/quality"   || 1
        [:]     | "/dashboard" || 2
        [id: 1] | "/dashboard" || 1
        [id: 2] | "/dashboard" || 2

    }
}
