package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.customException.CustomJasperException
import com.rxlogix.enums.*
import com.rxlogix.user.*
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import net.sf.dynamicreports.report.exception.DRException
import org.springframework.security.core.GrantedAuthority
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([MiscUtil, AuditLogConfigUtil, ReportResult, ExecutedReportConfiguration, SpringSecurityUtils, Tenants])
class ReportControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportController> {

    def setupSpec() {
        mockDomains WorkflowRule,User, Role, UserRole,UserGroup, UserGroupUser, Tenant,Preference, WorkflowState,ExecutedReportConfiguration,DateRangeType,ExecutedConfiguration,ExecutedDateRangeInformation,ExecutedDeliveryOption,ExecutedPeriodicReportConfiguration,ExecutedQueryValueList,ExecutedTemplateQuery,ExecutedTemplateValueList,ReportResult,ReportTemplate,SourceProfile,Tenant,WorkflowState,EmailConfiguration,DmsConfiguration, ReportConfiguration,CustomSQLTemplate, ExecutionStatus, IcsrProfileConfiguration, FieldProfile, IcsrCaseTracking
        AuditLogConfigUtil.metaClass.static.logChanges = {domain, Map newMap, Map oldMap, String eventName, String extraValue ="" , String transactionId = "" -> }
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser(0..1) { -> user }
        securityMock.demand.getAllowedSharedWithUsersForCurrentUser { -> [user] }
        securityMock.demand.getAllowedSharedWithGroupsForCurrentUser { -> [] }
        securityMock.demand.isCurrentUserDev(0..2) { false }
        return securityMock.proxyInstance()
    }

    private User makeNormalUser(name, team, String email = null) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_DEV', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user",email: email?:"abc@gmail.com")
        normalUser.addToTenants(tenant)
        normalUser.save(validate: false)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
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
        return adminUser
    }

    def setup() {
        ViewHelper.metaClass.static.getMessage = { String messageKey ->
            if (messageKey == 'default.date.format.long.tz') {
                return 'dd-MMM-yyyy hh:mm:ss a z'
            }
            return messageKey
        }
        Holders.config.pvreports.show.max.html=1_000 // Used to prevent HTML generation for large reports
    }

    def cleanup() {
    }

   
}
