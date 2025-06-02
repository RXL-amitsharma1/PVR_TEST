package com.rxlogix.user

import com.rxlogix.UserCommand
import com.rxlogix.UserController
import com.rxlogix.config.*
import com.rxlogix.enums.UserType
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ViewHelper, UserGroup, ReportRequest, ReportTemplate, SuperQuery, CaseSeries, Configuration, ExecutedCaseSeries, ExecutedPeriodicReportConfiguration, ExecutedConfiguration])
class UserControllerSpec extends Specification implements DataTest, ControllerUnitTest<UserController> {
    def setup() {
        ViewHelper.metaClass.static.getMessage = { String code -> return "" }
    }

    def cleanup() {
        ViewHelper.metaClass.static.getMessage = null
    }

    def setupSpec() {
        mockDomains User, Role, UserRole, ExecutedConfiguration, UserGroup, Tenant, UserGroupRole, UserGroupUser, FieldProfile, Configuration, ExecutedPeriodicReportConfiguration, ExecutedCaseSeries, PeriodicReportConfiguration, CaseSeries, SuperQuery, ReportTemplate, ActionItem, ReportRequest
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


    void "test show()"(){
        given:
        User userInstance = makeAdminUser()
        ExecutedConfiguration.metaClass.static.executeQuery = { String q, Map p ->
            [[1, 'reportName1']]
        }
        ExecutedPeriodicReportConfiguration.metaClass.static.executeQuery = { String q, Map p ->
            [[1, 'reportName1']]
        }
        ExecutedCaseSeries.metaClass.static.executeQuery = { String q, Map p ->
            [[1, 'caseSeries1']]
        }
        Configuration.metaClass.static.executeQuery = { String q, Map p ->
            [[1, 'reportName1']]
        }
        CaseSeries.metaClass.static.executeQuery = { String q, Map p ->
            [[1, 'caseSeries1']]
        }
        SuperQuery.metaClass.static.executeQuery = { String q, Map p ->
            [[1, 'superquery1']]
        }
        ReportTemplate.metaClass.static.executeQuery = { String q, Map p ->
            [[1, 'repotemp1']]
        }
        ReportRequest.metaClass.static.executeQuery = { String q, Map p ->
            [[1, 'reportReqest1']]
        }
        UserGroup userGroup = new UserGroup(id: 1, name: "group",createdBy: userInstance.getUsername(), modifiedBy: userInstance.getUsername())
        userGroup.save(failOnError: true, flush: true , validate :false )
        UserGroup.metaClass.static.fetchAllUserGroupByUser={ User user->[]}
        when:
        controller.show(userInstance)
        then:
        view=='/user/show'

    }

    void "test save"(){
        given:
        def user = new UserCommand(username: 'amit' , type: UserType.NON_LDAP, fullName: 'Amity', email:'coolboy@hothunk.mail.com',preference:  new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user"));
        when:
        controller.save(user)
        then:
        flash.error == null
    }

}
