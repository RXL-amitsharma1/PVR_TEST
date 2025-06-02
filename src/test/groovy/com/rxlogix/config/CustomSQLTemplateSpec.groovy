package com.rxlogix.config

import com.rxlogix.TemplateService
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

//When null pointer exception https://stackoverflow.com/questions/11291495/grails-2-0-1-unit-test-nullpointerexception-on-save
@ConfineMetaClassChanges([User])
class CustomSQLTemplateSpec extends Specification implements DomainUnitTest<CustomSQLTemplate> {

    public static final user = "Test User"

    def setupSpec() {
        mockDomains ReportTemplate, User, Role, UserRole, Tenant
    }

    void "Test custom validator customSQLTemplateWhere, when customSQLTemplateWhere contains reserved words"() {

        given: "A Custom SQL Template instance"
        def templateService = new MockFor(TemplateService)


        templateService.demand.asBoolean(0..20) {
            return true
        }

        templateService.demand.getUsagesCount(0..1) { ReportTemplate template ->
            return 0
        }

        templateService.demand.isTemplateUpdateable(0..20) { ReportTemplate template ->
            return true
        }

        CustomSQLTemplate customSQLTemplate = createMockCustomSQLTemplate(templateService.proxyInstance())

        when: "Validating the Custom SQL Template instance"
        customSQLTemplate.customSQLTemplateWhere = customSQLTemplateWhere
        Boolean result = customSQLTemplate.validate()

        then: "Validation fails because SQL may not contain reserved words."
        !result
        customSQLTemplate.errors.getFieldError('customSQLTemplateWhere').code == 'com.rxlogix.config.query.customSQLQuery.invalid'

        where:
        sno | customSQLTemplateWhere
        1   | 'select case_num'
        2   | 'use C_AE_IDENTIFICATION'
        3   | 'alter table table1'
        4   | 'desc case_num'
        5   | 'create tabale tablename'
        6   | 'insert into C_AE_IDENTIFICATION'
        7   | 'drop table table1'
        8   | 'delete from table1'
        9   | 'update tablename'
        10  | 'where id=20;'
    }

    void "Test custom validator customSQLTemplateSelectFrom, when customSQLTemplateSelectFrom contain reserved words"() {

        given: "A Custom SQL Template instance"
        def templateService = new MockFor(TemplateService)

        templateService.demand.asBoolean(0..20) { ->
            return true
        }

        templateService.demand.getUsagesCount(0..1) { ReportTemplate template ->
            return 0
        }

        templateService.demand.isTemplateUpdateable(0..20) { ReportTemplate template ->
            return true
        }
        CustomSQLTemplate customSQLTemplate = createMockCustomSQLTemplate(templateService.proxyInstance())

        when: "Validating the Custom SQL Template instance"
        customSQLTemplate.customSQLTemplateWhere = customSQLTemplateSelectFrom
        Boolean result = customSQLTemplate.validate()

        then: "Validation fails because SQL may not contain reserved words."
        !result
        customSQLTemplate.errors.getFieldError('customSQLTemplateWhere').code == 'com.rxlogix.config.query.customSQLQuery.invalid'

        where:
        sno | customSQLTemplateSelectFrom
        1   | 'where id=20;'
        2   | 'use C_AE_IDENTIFICATION'
        3   | 'alter table table1'
        4   | 'desc case_num'
        5   | 'create tabale tablename'
        6   | 'insert into C_AE_IDENTIFICATION'
        7   | 'drop table table1'
        8   | 'delete from table1'
        9   | 'update tablename'
    }

    void "Test custom validator hasBlanks,update the template with blank parameter will fails the validation"() {

        given: "A Custom SQL Template instance with hasBlank set to true"
        def templateService = new MockFor(TemplateService)


        templateService.demand.asBoolean(0..2) { ->
            return true
        }

        templateService.demand.getUsagesCount(0..1) { ReportTemplate template ->
            return 1
        }

        templateService.demand.isTemplateUpdateable(0..2) { ReportTemplate template ->
            return false
        }

        CustomSQLTemplate customSQLTemplate = createMockCustomSQLTemplate(templateService.proxyInstance())

        when: "Update the property customSQLTemplateWhere/customSQLTemplateSelectFrom and validating the Custom SQL Template instance"
        customSQLTemplate.customSQLTemplateWhere = 'case_num=5'
        Boolean result = customSQLTemplate.validate()

        then: "Validation fails because we are updating the template with blank parameter."
        !result
        customSQLTemplate.errors.getFieldError('hasBlanks').code == 'app.template.update.fail.blanks'
    }

    private createMockCustomSQLTemplate(def mockedTemplateService) {
        CustomSQLTemplate customSQLTemplate = new CustomSQLTemplate(columnNamesList: ['Case Number', 'Initial Receipt Date'], customSQLTemplateSelectFrom: 'Select case_num "Case Number", init_rept_date "Initial Receipt Date"', customSQLTemplateWhere: "", hasBlanks: true, isDeleted: false, editable: true, templateType: TemplateTypeEnum.CASE_LINE, originalTemplateId: 0, factoryDefault: false, ciomsI: false, owner: makeAdminUser(), name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser")
        customSQLTemplate.templateService = mockedTemplateService
        return customSQLTemplate
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
