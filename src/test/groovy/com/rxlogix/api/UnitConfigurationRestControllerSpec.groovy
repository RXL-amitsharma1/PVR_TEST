package com.rxlogix.api

import com.rxlogix.SqlGenerationService
import com.rxlogix.util.ViewHelper
import com.rxlogix.UserService
import com.rxlogix.config.IcsrOrganizationType
import com.rxlogix.config.Tenant
import com.rxlogix.config.UnitConfiguration
import com.rxlogix.enums.UnitTypeEnum
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User, UnitConfiguration])
class UnitConfigurationRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<UnitConfigurationRestController>  {

    def setup() {
    }

    def cleanup() {
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

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant,Preference, UnitConfiguration,IcsrOrganizationType
    }

    void "test list with search string"(){
        User normalUser = makeNormalUser("user",[])
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: "unitName",unitType: UnitTypeEnum.BOTH,organizationType: new IcsrOrganizationType(name: "organisation", langId: "1"),unitRegisteredId: "1",e2bValidation: true,unitRetired: false,createdBy: "user")
        unitConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockSqlService = new MockFor(SqlGenerationService)
        mockSqlService.demand.getPVALanguageId { String locale -> return 1 }
        controller.sqlGenerationService = mockSqlService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1) { return normalUser }
        mockUserService.demand.getUserByUsername(0..1) { String username -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        UnitConfiguration.metaClass.static.getAllUnitConfigurationBySearchString = { String search, String sortBy = null, String sortDirection = "asc" -> return unitConfiguration}
        when:
        params.searchString = "unitName"
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.list()
        then:
        response.json.aaData[0].size() == 9
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test list with no search string"(){
        User normalUser = makeNormalUser("user",[])
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: "unitName",unitType: UnitTypeEnum.BOTH,organizationType: new IcsrOrganizationType(name: "organisation",langId: "1"),unitRegisteredId: "1",e2bValidation: true,unitRetired: false,createdBy: "user")
        unitConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockSqlService = new MockFor(SqlGenerationService)
        mockSqlService.demand.getPVALanguageId { String locale -> return 1 }
        controller.sqlGenerationService = mockSqlService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1) { return normalUser }
        mockUserService.demand.getUserByUsername(0..1){ String username -> return normalUser}
        controller.userService = mockUserService.proxyInstance()
        UnitConfiguration.metaClass.static.getAllUnitConfigurationBySearchString = { String search, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){
                    return []
                }
                int count(){
                    return 0
                }
            }
        }
        when:
        params.searchString = ""
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.list()
        then:
        response.json.aaData == []
        response.json.recordsTotal == 0
        response.json.recordsFiltered == 0
    }

    void "test searchDataBasedOnParam"() {
        User normalUser = makeNormalUser("user",[])
        IcsrOrganizationType icsrOrganizationType = new IcsrOrganizationType(name: "organisation", langId: "1")
        icsrOrganizationType.save(failOnError: true, validate: false, flush: true)

        UnitConfiguration unitConfiguration = new UnitConfiguration(
                organizationType: icsrOrganizationType,
                organizationCountry: "ind"
        )
        unitConfiguration.save(failOnError: true, validate: false, flush: true)

        def mockSqlService = new MockFor(SqlGenerationService)
        mockSqlService.demand.getPVALanguageId { String locale -> return 1 }
        controller.sqlGenerationService = mockSqlService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(2) { return normalUser }
        controller.userService = mockUserService.proxyInstance()
        ViewHelper.metaClass.static.getOrganizationCountryNameByPreference = { String countryName ->
            return countryName
        }
        when: "Calling the controller action"
        Map params = [id: unitConfiguration.id]
        controller.searchDataBasedOnParam(params)

        then: "Verify the response"
        response.json != null
        response.json.organizationType == [name: 'organisation', id: icsrOrganizationType.id]
        response.json.organizationCountry == "ind"
    }
}
