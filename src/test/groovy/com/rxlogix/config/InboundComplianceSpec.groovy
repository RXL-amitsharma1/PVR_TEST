package com.rxlogix.config

import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.test.TestUtils
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User])
class InboundComplianceSpec extends Specification implements DomainUnitTest<InboundCompliance> {

    InboundCompliance inboundCompliance

    def setupSpec() {
        mockDomains Preference, Tenant, User, Role, UserRole, SourceProfile, SuperQuery, QueryCompliance
    }

    def createNewInboundCompliance(){
        inboundCompliance=new InboundCompliance()
        inboundCompliance.id=8L
        inboundCompliance.senderName='Sender Name 1'
        def preference = new Preference(createdBy: 'tester', modifiedBy: 'tester', locale: Locale.US)
        def User owner = makeNormalUser("user",[])
        inboundCompliance.owner= owner
        inboundCompliance.description = "testDescription"
        inboundCompliance.createdBy='user'
        inboundCompliance.modifiedBy='user'
        Date now = new Date()
        inboundCompliance.dateCreated=now
        inboundCompliance.lastUpdated=now
        inboundCompliance.tenantId=getTenant().getId()
        inboundCompliance.sourceProfile = SourceProfile.findAll()[0] ?: TestUtils.createSourceProfile()
        SuperQuery query = new SuperQuery(name: 'super_query', queryType: QueryTypeEnum.SET_BUILDER,
                createdBy: 'tester', modifiedBy: 'tester', owner: owner)
                .save(failOnError: true, validate : false)
        QueryCompliance qc = new QueryCompliance(criteriaName : "Criteria Test 1", query : query,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: owner.username, modifiedBy: owner.username, dateCreated : now,
                lastUpdated : now)
        inboundCompliance.addToQueriesCompliance(qc)
    }

    private User makeNormalUser(name, team) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user")
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
        normalUser.metaClass.static.isDev = { -> return false}
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


    void "test inboundCompliance Id"() {
        given:
        createNewInboundCompliance()
        when:"inboundCompliance Id equals id"
        inboundCompliance.id=id
        then:
        inboundCompliance.validate()==true
        where:
        id<<[null,13L]
    }

    void "test senderName cannot be null"() {
        given:
        createNewInboundCompliance()
        when:"senderName equals value"
        inboundCompliance.senderName=value
        then:
        inboundCompliance.validate()==result
        where:
        value           | result
        null            | false
        ''              | false
        'Sender Name 1' | true
    }

    void "test description cannot be > 4000"() {
        given:
        createNewInboundCompliance()
        when:"description equals value"
        inboundCompliance.description=value
        then:
        inboundCompliance.validate()==result
        where:
        value                      | result
        null                       | true
        ''                         | true
        'testDescription'          | true
        'testDescription' * 400    | false
    }

    void "test createdBy cannot be null"() {
        given:
        createNewInboundCompliance()
        when:"createdBy equals value"
        inboundCompliance.createdBy=value
        then:
        inboundCompliance.validate()==result
        where:
        value  | result
        null   | false
        ''     | true
        'user' | true
        ' '    | true
    }

    void "test modifiedBy cannot be null"() {
        given:
        createNewInboundCompliance()
        when:"modifiedBy equals value"
        inboundCompliance.modifiedBy=value
        then:
        inboundCompliance.validate()==result
        where:
        value  | result
        null   | false
        ''     | true
        'user' | true
        ' '    | true
    }

}
