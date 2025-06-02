package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.UserService
import com.rxlogix.config.CaseSeries
import com.rxlogix.config.Tag
import com.rxlogix.config.Tenant
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

class CaseSeriesRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<CaseSeriesRestController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains CaseSeries,User, UserGroup, UserGroupUser, Role, UserRole, Tenant,Preference
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

    void "test index"(){
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(description: "description",seriesName: "name",numExecutions: 1,qualityChecked: false,tags: [new Tag(name: "tag")],owner: normalUser)
        caseSeries.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        CaseSeries.metaClass.static.fetchCaseSeriesBySearchString = { LibraryFilter filter, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){
                    return [[caseSeries.id]]
                }
            }
        }
        CaseSeries.metaClass.static.countCaseSeriesBySearchString = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        when:
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.index()
        then:
        response.json.aaData[0].size() == 10
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }
}
