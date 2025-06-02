package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.UserService
import com.rxlogix.config.ExecutedCaseDeliveryOption
import com.rxlogix.config.ExecutedCaseSeries
import com.rxlogix.config.ExecutedCaseSeriesUserState
import com.rxlogix.config.Tenant
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User, ExecutedCaseSeries])
class ExecutedCaseSeriesRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<ExecutedCaseSeriesRestController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference, ExecutedCaseSeries, ExecutedCaseSeriesUserState, ExecutedCaseDeliveryOption
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
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "series",numExecutions: 1,description: "description",owner: normalUser,executedCaseSeriesStates: [new ExecutedCaseSeriesUserState(user: normalUser,isArchived: true)])
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ExecutedCaseSeries.metaClass.static.fetchCaseSeriesBySearchString = { LibraryFilter filter -> new Object(){
                List list(LinkedHashMap H){[max: 10, offset: 0, sort: "", order: ""]
                    return [[executedCaseSeries.id]]
                }
            }
        }
        ExecutedCaseSeries.metaClass.static.countCaseSeriesBySearchString = { LibraryFilter filter ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        when:
        params.sort = "owner"
        params.max = 10
        params.offset = 0
        params.order = ""
        controller.index()
        then:
        response.json.aaData[0].size() == 12
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test getExecutedCaseSeriesList"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "series",numExecutions: 1,description: "description",owner: normalUser,executedCaseSeriesStates: [new ExecutedCaseSeriesUserState(user: normalUser,isArchived: true)])
        executedCaseSeries.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ExecutedCaseSeries.metaClass.static.availableByUser = { User user, String search -> new Object(){
                List list(LinkedHashMap H){[max: 10, offset: 0]
                    return [[id: 1,qualityChecked :executedCaseSeries.qualityChecked,seriesName: executedCaseSeries.seriesName,numExecutions: executedCaseSeries.numExecutions, isFavorite: false]]
                }
            }
        }
        ExecutedCaseSeries.metaClass.static.countAvailableByUser = { User user, String search ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        when:
        controller.getExecutedCaseSeriesList("term",0,0)
        then:
        response.json == [total_count:1, items:[[qced:false, id:1, text:"series - 1", isFavorite:false]]]
    }

    void "test getExecutedCaseSeriesItem"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "series",numExecutions: 1,description: "description",owner: normalUser,executedCaseSeriesStates: [new ExecutedCaseSeriesUserState(user: normalUser,isArchived: true,isFavorite: true)])
        executedCaseSeries.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.id = executedCaseSeries.id
        controller.getExecutedCaseSeriesItem()
        then:
        response.json == [qced:false, id:1, text:"series - 1", isFavorite:true]
    }

    void "test getSharedWithUsers"(){
        given:
        User normalUser = makeNormalUser("testUser", [])
        User user1 = makeNormalUser("user1", [])
        UserGroup userGroup = new UserGroup(id: 1, name: "userGroup", createdBy:'user', modifiedBy:'user')
        userGroup.save(failOnError:true, validate:false)
        ExecutedCaseDeliveryOption executedDeliveryOption = new ExecutedCaseDeliveryOption(sharedWith: [user1], sharedWithGroup: [userGroup])
        executedDeliveryOption.save(failOnError:true,validate : false)
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "testSeries", numExecutions: 1, description: "description",
                owner: normalUser, executedDeliveryOption: executedDeliveryOption)
        executedCaseSeries.save(failOnError:true, validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.id = executedCaseSeries.id
        controller.getSharedWithUsers()
        then:
        response.json.users[0].username == "user1"
        response.json.groups[0].name == "userGroup"
    }
}
