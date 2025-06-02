package com.rxlogix.config

import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import grails.util.Holders
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User])
class CaseSeriesSpec extends Specification implements DomainUnitTest<CaseSeries> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, Role, UserRole, Tenant
    }

    void "case series is editable by owner"() {
        given:
        User u = makeNormalUser("user",[])
        u.metaClass.isDev = {->
            false
        }
        try {
            Holders.getApplicationContext().getBean("userService")
            Holders.grailsApplication.mainContext.beanFactory.destroySingleton("userService")
        } catch (Exception e) {
            //no bean registered
        }
        Holders.grailsApplication.mainContext.beanFactory.registerSingleton("userService", new Object() {
            public User getCurrentUser() {
                return u
            }
        })
        when:
        CaseSeries caseSeries = new CaseSeries(owner: u, tenantId: 1l)
        then:
        Tenants.withId(1) {
            return caseSeries.isEditable(u)
        }
    }

    void "case series is editable by admin"() {
        given:
        User u = makeNormalUser("user",[])
        User current = makeAdminUser()
        current.metaClass.isDev = {->
            false
        }

        u.metaClass.isDev = {->
            false
        }
        try {
            Holders.getApplicationContext().getBean("userService")
            Holders.grailsApplication.mainContext.beanFactory.destroySingleton("userService")
        } catch (Exception e) {
            //no bean registered
        }
        Holders.grailsApplication.mainContext.beanFactory.registerSingleton("userService", new Object() {
            public User getCurrentUser() {
                return current
            }
        })
        when:
        CaseSeries caseSeries = new CaseSeries(owner: u, tenantId: 1l)
        then:
        Tenants.withId(1){
           return caseSeries.isEditable(current)
        }
    }

    void "case series is editable by group manager"() {
        given:
        User u2 = makeNormalUser("user2", [])
        User u1 = makeNormalUser("user1", [u2.id])
        u2.metaClass.isDev = {->
            false
        }

        u1.metaClass.isDev = {->
            false
        }

        try {
            Holders.getApplicationContext().getBean("userService")
            Holders.grailsApplication.mainContext.beanFactory.destroySingleton("userService")
        } catch (Exception e) {
            //no bean registered
        }
        Holders.grailsApplication.mainContext.beanFactory.registerSingleton("userService", new Object() {
            public User getCurrentUser() {
                return u1
            }
        })
        when:
        CaseSeries caseSeries = new CaseSeries(owner: u2, tenantId: 1L)
        then:
        Tenants.withId(1) {
            caseSeries.isEditable(u1)
        }
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
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user").save(flush: true)
        User adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(flush: true)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isAdmin = { -> return true }
        adminUser.metaClass.getUserTeamIds = { -> [] }
        return adminUser
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
        return normalUser
    }

    void "test tenantId can not be null"() {
        when:
        domain.tenantId = null

        then:
        !domain.validate()
        domain.errors['tenantId'].code == "nullable"
    }
}
