package com.rxlogix


import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User])
class RoleControllerSpec extends Specification implements DataTest, ControllerUnitTest<RoleController> {

    public static final adminUser = "Admin User"

    def setupSpec() {
        mockDomains Role, User, Role, UserRole
    }


    def setup() {
        mockRoles()
        makeAdminUser()
    }

    void "test index action, it renders the index page."() {
        when: "Call index action."
        controller.index()

        then: "Renders the view page."
        view == '/role/index'
        model.roleInstanceList == Role.list()
        model.roleInstanceTotal == Role.count()
    }


    private static mockRoles() {
        3.times {
            new Role(authority: "Role_${it}", modifiedBy: "Application", createdBy: "Application", dateCreated: new Date(), lastUpdated: new Date()).save(failOnError: true)
        }
    }

    private static makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        Preference preferenceAdmin = new Preference(locale: new Locale("en"))
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: adminUser, modifiedBy: adminUser).save(flush: true)
        3.times {
            User adminUser = new User(username: "Test User-${it}", password: "Test User-${it}", fullName: "User-${it}", preference: preferenceAdmin, createdBy: adminUser, modifiedBy: adminUser)
            adminUser.save(flush: true)
            UserRole.create(adminUser, adminRole, true)
        }
    }

    void "test show"(){
        Role role=new Role(authority: "Role", modifiedBy: "Application", createdBy: "Application", dateCreated: new Date(), lastUpdated: new Date()).save(failOnError: true)
        when:
        controller.show(role)
        then:
        response.status == 200
    }

    void "test show when instance does not exist"(){
        when:
        controller.show(null)
        then:
        response.status == 302
        response.redirectedUrl == '/role/index'
    }
}