package com.rxlogix.config

import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.enums.XMLNodeType
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class XMLTemplateSpec extends Specification implements DomainUnitTest<XMLTemplate> {
    public static final user = "Test User"
    CaseLineListingTemplate reportTemplateToTest

    def setup() {
        reportTemplateToTest = new CaseLineListingTemplate(
                templateType: TemplateTypeEnum.CASE_LINE
        )
        reportTemplateToTest.save(validate: false)
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains ReportTemplate, CaseLineListingTemplate, ReportFieldInfoList, QueryExpressionValue, User, Role, UserRole, Tenant
    }

    void "test for saving"() {
        setup:
        def xmlTemplateNode = new XMLTemplateNode(
                type: XMLNodeType.SOURCE_FIELD,
                tagName: "icsr",
                value: "Hello World!",
                e2bElement: "e2bElement",
                sourceFieldLabel: "sourceFieldLabel"
        )
        def adminUser = makeAdminUser()
        XMLTemplate xmlTemplate = new XMLTemplate(
                templateType: TemplateTypeEnum.ICSR_XML,
                owner: adminUser,
                createdBy: "Unit Test",
                modifiedBy: "Unit Test",
                name: "Test Template",
                rootNode: xmlTemplateNode,
                nestedTemplates: [reportTemplateToTest]
        )
        when:
        xmlTemplate.save()
        then:
        xmlTemplate.id != null
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
        Preference preferenceAdmin = new Preference(locale: new Locale("en"))
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        User adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(flush: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }
}
