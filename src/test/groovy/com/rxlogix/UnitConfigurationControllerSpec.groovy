package com.rxlogix

import com.rxlogix.config.Email
import com.rxlogix.config.IcsrOrganizationType
import com.rxlogix.config.UnitConfiguration
import com.rxlogix.enums.UnitTypeEnum
import com.rxlogix.mapping.AllowedAttachment
import com.rxlogix.mapping.OrganizationCountry
import com.rxlogix.user.*
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import org.grails.datastore.mapping.simple.SimpleMapDatastore
import spock.lang.Specification
import com.rxlogix.config.Tenant
import com.rxlogix.mapping.*
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([UnitConfiguration])
class UnitConfigurationControllerSpec extends Specification implements DataTest, ControllerUnitTest<UnitConfigurationController> {

    def setupSpec() {
        mockDomains IcsrOrganizationType, UnitConfiguration, Email , User, Preference, Role, UserRole, Tenant
    }

    def setup() {
        controller.metaClass.bindEmail = { UnitConfiguration configurationInstance -> }
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

    private Tenant getTenant() {
        def tenant = Tenant.get(1L)
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = 1L
        return tenant.save()
    }

    void "test index"() {
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test create"(){
        given:
        controller.targetDatastore = new SimpleMapDatastore(['pva'], OrganizationCountry)
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: "unitName",unitType: UnitTypeEnum.BOTH,organizationType: new IcsrOrganizationType(name: "organisation"),unitRegisteredId: "1",unitRetired: false,createdBy: "user", modifiedBy: "user")
        unitConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockOrganizationTypeService = Mock( OrganizationTypeService )
        mockOrganizationTypeService.getAllIcsrOrganizationType() >> {new IcsrOrganizationType(name: "organisation")}
        controller.organizationTypeService=mockOrganizationTypeService
        UserService userServiceMock = Mock( UserService )
        controller.userService=userServiceMock
        when:
        controller.create()
        then:
        response.status == 200
    }

    void "test save when request.method == 'GET'"(){
        given:
        UserService userServiceMock = Mock( UserService )
        controller.userService=userServiceMock
        when:
        controller.save()
        then:
        flash.error == 'default.not.saved.message'
        response.status == 302
        response.redirectedUrl == '/unitConfiguration/index'

    }

    void "test save"(){
        given:
        User normalUser = makeNormalUser("user",[])
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1) { return normalUser }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = Mock( CRUDService )
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: "unitName",unitType: UnitTypeEnum.BOTH,organizationType: new IcsrOrganizationType(name: "organisation"),unitRegisteredId: "1",unitRetired: false,createdBy: "user", modifiedBy: "user")
        unitConfiguration.save(failOnError:true,validate:false,flush:true)
        mockCRUDService.save(_) >> {return unitConfiguration}
        controller.CRUDService=mockCRUDService
        when:
        request.method = 'POST'
        controller.save()
        then:
        flash.message == 'default.created.message'
        response.status == 302
    }

    void "test save validation exception"(){
        given:
        def mockCRUDService = Mock( CRUDService )
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: "unitName",unitType: UnitTypeEnum.BOTH,organizationType: new IcsrOrganizationType(name: "ICSR_UNIT_CONF_REGULATORY_AUTHORITY"),unitRegisteredId: "1",unitRetired: false,createdBy: "user", modifiedBy: "user")
        unitConfiguration.save(failOnError:true,validate:false,flush:true)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception", unitConfiguration.errors)}
        def mockOrganizationTypeService = Mock( OrganizationTypeService )
        mockOrganizationTypeService.getAllIcsrOrganizationType() >> {new IcsrOrganizationType(name: "organisation")}
        controller.organizationTypeService=mockOrganizationTypeService
        controller.CRUDService=mockCRUDService
        UserService userServiceMock = Mock( UserService )
        controller.userService=userServiceMock
        when:
        request.method = 'POST'
        controller.save()
        then:
        response.status == 200
    }

    void "test save when Exception"(){
        given:
        def mockCRUDService = Mock( CRUDService )
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: "unitName",unitType: UnitTypeEnum.BOTH,organizationType: new IcsrOrganizationType(name: "ICSR_UNIT_CONF_REGULATORY_AUTHORITY"),unitRegisteredId: "1",unitRetired: false,createdBy: "user", modifiedBy: "user")
        unitConfiguration.save(failOnError:true,validate:false,flush:true)
        mockCRUDService.save(_) >> {throw new Exception()}
        controller.CRUDService=mockCRUDService
        UserService userServiceMock = Mock( UserService )
        controller.userService=userServiceMock
        when:
        request.method = 'POST'
        controller.save()
        then:
        flash.error == "app.error.500"
        response.status == 302
        response.redirectedUrl == '/unitConfiguration/create'
    }

    void "test show"(){
        given:
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: "unitName",unitType: UnitTypeEnum.BOTH,organizationType: new IcsrOrganizationType(name: "ICSR_UNIT_CONF_REGULATORY_AUTHORITY"),unitRegisteredId: "1",unitRetired: false,createdBy: "user", modifiedBy: "user", preferredLanguage: "en")
        unitConfiguration.save(failOnError:true,validate:false,flush:true)
        UnitConfiguration.metaClass.static.read = {unitConfiguration}
        controller.targetDatastore = new SimpleMapDatastore(['pva'], PreferredLanguage)
        def mockOrganizationTypeService = Mock( OrganizationTypeService )
        mockOrganizationTypeService.getAllIcsrOrganizationType() >> {new IcsrOrganizationType(name: "organisation")}
        controller.organizationTypeService=mockOrganizationTypeService
        UserService userServiceMock = Mock( UserService )
        controller.userService=userServiceMock
        ViewHelper.metaClass.static.getCorrectPreferredLanguage = { String preferredLanguage ->
            return preferredLanguage
        }
        when:
        controller.show(1L)
        then:
        response.status == 200
    }

    void "test show when instance is null"(){
        given:
        UnitConfiguration.metaClass.static.read = {null}
        def mockOrganizationTypeService = Mock( OrganizationTypeService )
        mockOrganizationTypeService.getAllIcsrOrganizationType() >> {new IcsrOrganizationType(name: "organisation")}
        controller.organizationTypeService=mockOrganizationTypeService
        when:
        controller.show(1L)
        then:
        response.status == 302
        response.redirectedUrl == '/unitConfiguration/index'
    }

    void "test edit"(){
        given:
        controller.targetDatastore = new SimpleMapDatastore(['pva'], OrganizationCountry)
        controller.targetDatastore = new SimpleMapDatastore(['pva'], AllowedAttachment)
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: "unitName",unitType: UnitTypeEnum.BOTH,organizationType: new IcsrOrganizationType(name: "ICSR_UNIT_CONF_REGULATORY_AUTHORITY"),unitRegisteredId: "1",unitRetired: false,createdBy: "user", modifiedBy: "user",registeredWith: new UnitConfiguration(id: 1L))
        unitConfiguration.save(failOnError:true,validate:false,flush:true)
        UnitConfiguration.metaClass.static.read = {unitConfiguration}
        OrganizationTypeService mockOrganizationTypeService = Mock( OrganizationTypeService )
        mockOrganizationTypeService.getAllIcsrOrganizationType() >> {new IcsrOrganizationType(name: "organisation",langId:"1")}
        controller.organizationTypeService=mockOrganizationTypeService
        UserService userServiceMock = Mock( UserService )
        controller.userService=userServiceMock
        def mockSqlService = new MockFor(SqlGenerationService)
        mockSqlService.demand.getPVALanguageId { String locale -> return 1 }
        controller.sqlGenerationService = mockSqlService.proxyInstance()
        when:
        controller.edit(1L)
        then:
        response.status == 200
    }

    void "test update when instance is null"(){
        User normalUser = makeNormalUser("user",[])
        when:
        request.method = 'POST'
        request.method = 'PUT'
        def mockSqlService = new MockFor(SqlGenerationService)
        mockSqlService.demand.getPVALanguageId { String locale -> return 1 }
        controller.sqlGenerationService = mockSqlService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1) { return normalUser }
        controller.userService = mockUserService.proxyInstance()
        controller.update()
        then:
        flash.message == 'default.not.found.message'
        response.status == 302
        response.redirectedUrl == '/unitConfiguration/index'

    }

    void "test update"(){
        given:
        def mockCRUDService = Mock( CRUDService )
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: "unitName",unitType: UnitTypeEnum.BOTH,organizationType: new IcsrOrganizationType(name: "organisation"),unitRegisteredId: "1",unitRetired: false,createdBy: "user", modifiedBy: "user")
        unitConfiguration.save(failOnError:true,validate:false,flush:true)
        mockCRUDService.update(_) >> {return unitConfiguration}
        controller.CRUDService=mockCRUDService
        User normalUser = makeNormalUser("user",[])
        def mockSqlService = new MockFor(SqlGenerationService)
        mockSqlService.demand.getPVALanguageId { String locale -> return 1 }
        controller.sqlGenerationService = mockSqlService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1) { return normalUser }
        controller.userService = mockUserService.proxyInstance()
        when:
        request.method = 'POST'
        request.method = 'PUT'
        params.id = 1L
        controller.update()
        then:
        flash.message == 'default.updated.message'
        response.status == 302
        response.redirectedUrl == '/unitConfiguration/index'
    }

    void "test update validation exception"(){
        given:
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: "unitName",unitType: UnitTypeEnum.BOTH,organizationType: new IcsrOrganizationType(name: "ICSR_UNIT_CONF_REGULATORY_AUTHORITY"),unitRegisteredId: "1",unitRetired: false,createdBy: "user", modifiedBy: "user")
        unitConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception", unitConfiguration.errors)}
        def mockOrganizationTypeService = Mock( OrganizationTypeService )
        mockOrganizationTypeService.getAllIcsrOrganizationType() >> {new IcsrOrganizationType(name: "organisation")}
        controller.organizationTypeService=mockOrganizationTypeService
        controller.CRUDService=mockCRUDService
        User normalUser = makeNormalUser("user",[])
        def mockSqlService = new MockFor(SqlGenerationService)
        mockSqlService.demand.getPVALanguageId { String locale -> return 1 }
        controller.sqlGenerationService = mockSqlService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1) { return normalUser }
        controller.userService = mockUserService.proxyInstance()
        when:
        request.method = 'POST'
        request.method = 'PUT'
        params.id = 1L
        controller.update()
        then:
        response.status == 200
    }

    void "test update when Exception"(){
        given:
        def mockCRUDService = Mock( CRUDService )
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: "unitName",unitType: UnitTypeEnum.BOTH,organizationType: new IcsrOrganizationType(name: "ICSR_UNIT_CONF_REGULATORY_AUTHORITY"),unitRegisteredId: "1",unitRetired: false,createdBy: "user", modifiedBy: "user")
        unitConfiguration.save(failOnError:true,validate:false,flush:true)
        mockCRUDService.update(_) >> {throw new Exception()}
        controller.CRUDService=mockCRUDService
        User normalUser = makeNormalUser("user",[])
        def mockSqlService = new MockFor(SqlGenerationService)
        mockSqlService.demand.getPVALanguageId { String locale -> return 1 }
        controller.sqlGenerationService = mockSqlService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1) { return normalUser }
        controller.userService = mockUserService.proxyInstance()
        when:
        request.method = 'POST'
        request.method = 'PUT'
        params.id = 1L
        controller.update()
        then:
        flash.error == "app.error.500"
        response.status == 302
        response.redirectedUrl == '/unitConfiguration/edit'
    }

}
