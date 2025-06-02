package com.rxlogix

import com.rxlogix.config.Tenant
import com.rxlogix.config.publisher.PublisherCommonParameter
import com.rxlogix.publisher.PublisherCommonParameterController
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import spock.lang.Specification

class PublisherCommonParameterControllerSpec extends Specification implements DataTest, ControllerUnitTest<PublisherCommonParameterController> {

    public static final user = "unitTest"

    def setupSpec() {
        mockDomains User, PublisherCommonParameter, Role, UserRole, Preference, Tenant
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

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def userRole = new Role(authority: 'ROLE_PVQ_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    void "test index"(){
        when:
        controller.index()

        then:
        response.status == 200
    }

    void "test list"(){
        given:
        PublisherCommonParameter template = new PublisherCommonParameter(name: 'newPublisherCommonParameter', description: 'testDescription', value: "test", isDeleted: false)
        PublisherCommonParameter.metaClass.static.findAllByIsDeleted = { boolean b ->
            return template
        }

        when:
        controller.list()

        then:
        response.status == 200
        response.json[0].name == 'newPublisherCommonParameter'
        response.json[0].description == 'testDescription'
    }

    void "test create"(){
        given:
        PublisherCommonParameter template = new PublisherCommonParameter(name: 'newPublisherCommonParameter', description: 'testDescription', value: "test", isDeleted: false)

        when:
        controller.create(template)

        then:
        response.status==200
    }

    void "test save try success"(){
        given:
        def normalUser = makeNormalUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {normalUser}
        controller.userService=mockUserService
        PublisherCommonParameter template = new PublisherCommonParameter(name: 'newPublisherCommonParameter', description: 'testDescription', value: "test", isDeleted: false)
        template.save(failOnError:true,validate:false)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.save(_) >> {return true}
        controller.CRUDService = mockCRUDService
        when:
        controller.save()
        then:
        response.status==302
        response.redirectedUrl=='/publisherCommonParameter/index'
    }

    void "test save try failure"(){
        given:
        def normalUser = makeNormalUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {normalUser}
        controller.userService=mockUserService
        PublisherCommonParameter template = new PublisherCommonParameter(name: 'newPublisherCommonParameter', description: 'testDescription', value: "test", isDeleted: false)
        template.save(failOnError:true,validate:false)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception",template.errors)}
        controller.CRUDService = mockCRUDService
        when:
        controller.save()
        then:
        response.status==200
    }

    void "test edit not found"(){
        given:
        PublisherCommonParameter.metaClass.static.read={null}
        when:
        controller.edit()
        then:
        response.status==302
        response.redirectedUrl=='/publisherCommonParameter/index'
    }

    void "test edit found"(){
        given:
        PublisherCommonParameter template = new PublisherCommonParameter(name: 'newPublisherCommonParameter', description: 'testDescription', value: "test", isDeleted: false)
        PublisherCommonParameter.metaClass.static.read={Long id->template }
        when:
        controller.edit(1L)
        then:
        response.status==200
    }

    void "test update try success"(){
        given:
        PublisherCommonParameter template = new PublisherCommonParameter(name: 'newPublisherCommonParameter', description: 'testDescription', value: "test", isDeleted: false)
        PublisherCommonParameter.metaClass.static.get={Long id->template }
        //template.save(failOnError:true,validate:false)
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_) >> {return true}
        controller.CRUDService = mockCRUDService
        when:
        params.id=1L
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=='/publisherCommonParameter/index'
    }

    void "test update try failure"(){
        given:
        PublisherCommonParameter template = new PublisherCommonParameter(name: 'newPublisherCommonParameter', description: 'testDescription', value: "test", isDeleted: false)
        PublisherCommonParameter.metaClass.static.get={Long id->template }
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception",template.errors)}
        controller.CRUDService = mockCRUDService
        when:
        params.id=1L
        controller.update()
        then:
        response.status==200
    }

    void "test show not found"(){
        given:
        PublisherCommonParameter.metaClass.static.read={null}
        when:
        controller.show()
        then:
        response.status==302
        response.redirectedUrl=='/publisherCommonParameter/index'
    }

    void "test show found"(){
        given:
        PublisherCommonParameter template = new PublisherCommonParameter(name: 'newPublisherCommonParameter', description: 'testDescription', value: "test", isDeleted: false)
        PublisherCommonParameter.metaClass.static.read={Long id->template }
        when:
        controller.show(1L)
        then:
        response.status==200
    }

    void "test delete not found"(){
        given:
        PublisherCommonParameter.metaClass.static.read={null}
        when:
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=='/publisherCommonParameter/index'
    }

    void "test delete found with try"(){
        given:
        PublisherCommonParameter template = new PublisherCommonParameter(name: 'newPublisherCommonParameter', description: 'testDescription', value: "test", isDeleted: false)
        PublisherCommonParameter.metaClass.static.read={Long id->template }
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.softDelete(_) >> {return true}
        controller.CRUDService = mockCRUDService
        when:
        controller.delete(2L)
        then:
        response.status==302
        response.redirectedUrl=='/publisherCommonParameter/index'
    }

    void "test delete found with validation exception"(){
        given:
        PublisherCommonParameter template = new PublisherCommonParameter(name: 'newPublisherCommonParameter', description: 'testDescription', value: "test", isDeleted: false)
        PublisherCommonParameter.metaClass.static.read={Long id->template }
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.softDelete(_) >> {throw new ValidationException("Validation Exception",template.errors)}
        controller.CRUDService = mockCRUDService
        when:
        controller.delete(2L)
        then:
        response.status==302
        response.redirectedUrl=='/publisherCommonParameter/index'
    }
}