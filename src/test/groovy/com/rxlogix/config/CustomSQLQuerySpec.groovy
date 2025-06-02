package com.rxlogix.config

import com.rxlogix.UserService
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User])
class CustomSQLQuerySpec extends Specification implements  DomainUnitTest<CustomSQLQuery> {

    public static final user = "Test User"

    def setupSpec() {
        mockDomains Tenant, User, Role, UserRole, CustomSQLValue
    }

    void "test custom validator customSQLValues"() {

        given: "A Custom SQL Query instance with customSQLQuery contains the reserved words."
        CustomSQLQuery query = createMockCustomSqlsQuery()
        query.customSQLQuery = customSQLQuery

        when: "Validation the Custom SQL Query instance"
        Boolean result = query.validate()

        then: "validation fails because SQL may not contain reserved words."
        !result
        query.errors.getFieldError('customSQLQuery').code == 'com.rxlogix.config.query.customSQLQuery.invalid'

        where:
        sno | customSQLQuery
        1   | "where id=20;"
        2   | 'use C_AE_IDENTIFICATION'
        3   | 'alter table table1'
        4   | 'desc case_num'
        5   | 'create tabale tablename'
        6   | 'insert into C_AE_IDENTIFICATION'
        7   | 'drop table table1'
        8   | 'delete from table1'
        9   | 'update tablename'
    }

    void "test getParameterSize(),it returns the size of the customSQLValues list"() {

        given: "A Custom SQL Query instance."
        CustomSQLQuery query = createMockCustomSqlsQuery()
        query.addToCustomSQLValues(new CustomSQLValue(key: "key1", value: "value1"))
        query.save(failOnError: true)

        when: "Call getParameterSize()method on the Custom SQL Query instance"
        Integer result = query.getParameterSize()

        then: "It returns the integer value 1."
        result == 1
    }

    private createMockCustomSqlsQuery() {
        CustomSQLQuery query = new CustomSQLQuery(hasBlanks: true, queryType: QueryTypeEnum.CUSTOM_SQL, owner: makeAdminUser(), name: 'Test query', createdBy: "normalUser", modifiedBy: "normalUser")
        query.userService = makeUserService()
        query.customSQLQuery = "where country_id = 223"
        query.save(flush: true)
        return query
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

    private makeUserService() {
        def userMock = new MockFor(UserService)
        userMock.demand.isCurrentUserAdmin(1..2) { false }
        return userMock.proxyInstance()
    }

}
