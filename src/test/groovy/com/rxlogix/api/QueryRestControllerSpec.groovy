package com.rxlogix.api

import com.rxlogix.QueryService
import com.rxlogix.LibraryFilter
import com.rxlogix.UserService
import com.rxlogix.config.*
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.mapping.AgencyName
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

/*
@TestFor(QueryRestController)
@Mock([Query, QuerySet, CustomSQLQuery, QueryExpressionValue, Tenant, User, Role, UserRole,ReportField,SuperQuery,Preference,QueryUserState, SourceProfile])
@ConfineMetaClassChanges([SourceProfile])
*/
@ConfineMetaClassChanges([User,SourceProfile,SuperQuery])
class QueryRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<QueryRestController> {
    QueryService queryService = new QueryService()
    //Use this to get past the constraint that requires a JSONQuery string.
    public static final user = "unitTest"
    def JSONQuery = """{ "all": { "containerGroups": [   { "expressions": [  
            { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }"""


    def setup() {
        def normalUser = makeNormalUser()
        def adminUser = makeAdminUser()
        mockNamedSuperQuery()
        controller.queryService = queryService
        controller.queryService.userService = makeUserService(normalUser)
        controller.userService = makeUserService(normalUser)
        Query query1 = new Query(queryType: QueryTypeEnum.QUERY_BUILDER, name: "not deleted", createdBy: normalUser.username,
                modifiedBy: normalUser.username, isDeleted: false, JSONQuery:JSONQuery)
        query1.owner = adminUser
        query1.userService = makeUserServiceForCurrentUserDev()
        query1.save(failOnError: true, flush: true)
        Query query2 = new Query(queryType: QueryTypeEnum.QUERY_BUILDER, name:  "is deleted", createdBy: normalUser.username,
                modifiedBy: normalUser.username, isDeleted:  true, JSONQuery:JSONQuery)
        query2.owner = adminUser
        query2.userService = makeUserServiceForCurrentUserDev()
        query2.save(failOnError: true, flush: true)
        request.addHeader("Accept", "application/json")
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains Query, QuerySet, CustomSQLQuery, QueryExpressionValue, Tenant, User, Role, UserRole, ReportField, SuperQuery, Preference, QueryUserState, SourceProfile
    }

    def cleanupSpec() {
        User.metaClass.encodePassword = null
        User.metaClass.isAdmin = null
        SuperQuery.metaClass.ownedByUser = {}
    }

    private makeUserService(User user) {
        def userMock = new MockFor(UserService)
        userMock.demand.getUser(0..1) { -> user }
        userMock.demand.getCurrentUser(0..1) { -> user }
        return userMock.proxyInstance()
    }

    private makeUserServiceForCurrentUserDev() {
        def userMock = new MockFor(UserService)
        userMock.demand.isCurrentUserAdmin(1..2) { false }
        return userMock.proxyInstance()
    }

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"),createdBy: "user",modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
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

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    void "PVR-419: Query Search should not list deleted queries"() {
        given: "Two queries"
        def queryService = new MockFor(QueryService)
        queryService.demand.getQueryList { String search, int offset, int max, Boolean isQueryTargetReports ->
            return [[ id: 0,text: "queryName",hasBlanks: false,qced: false,isFavorite:false]]
        }

        queryService.demand.getQueryListCount { String search,Boolean isQueryTargetReports ->
            return 1
        }

        controller.queryService = queryService.proxyInstance()

        User.metaClass.isAdmin = { true }
        when: "Call getQueryList method"
        controller.getQueryList("", 1, 30, false, false)

        then: "Should only return the non deleted one"
        response.status == 200
        response.json.items != null
        response.json.total_count != null
    }

    void "With Valid Query Id NameDescription should respond"() {

        when: "Call getQueryNameDescription() method"
        controller.getQueryNameDescription(1L)

        then: "Should only return the non deleted one"
        response.status == 200
        response.json.text
    }




    private mockNamedSuperQuery() {
//        Mocking named query logic. Not a proper test type to test PVR-419. We need to have separate integration test case for query data test case.
        SuperQuery.metaClass.static.ownedByUserWithSearch = { User user, String search ->
            return [list: { Map map ->
                return SuperQuery.findAllByIsDeleted(false)
            }]
        }
    }

    void "test getNonSetQueries only not deleted ones"(){
        User normalUser = new User(fullName: "Joe Griffin")
        normalUser.save(failOnError:true , validate:false)
        SuperQuery superQuery = new SuperQuery(owner: normalUser,name: "superQuery_1",description: "SuperQuery")
        superQuery.save(failOnError:true , validate:false)
        def mockQueryService = new MockFor(QueryService)
        mockQueryService.demand.getNonSetQueriesData(0..1){String search, Long oldSelectedId, int offset, int max ->
            [list:[superQuery,Query.get(1)],totalCount : 2]
        }
        controller.queryService = mockQueryService.proxyInstance()
        when:
        controller.getNonSetQueries(1,"",1,30)
        then:
        response.json == [total_count:2, items:[[id:3, text:"superQuery_1 (SuperQuery) - Owner: Joe Griffin"],[id:1, text:"not deleted  - Owner: Peter Fletcher"]]]
    }

    void "test getReportingDestinations"(){
        def mockQueryService = new MockFor(QueryService)
        mockQueryService.demand.getAgenciesNames(0..1){String search, int offset, int max ->
            [list:[new AgencyName(name: "NewAgency")],totalCount : 1]
        }
        controller.queryService = mockQueryService.proxyInstance()
        when:
        controller.getReportingDestinations("ewA",1,30)
        then:
        response.json == [total_count:1, items:[[id:"NewAgency", text:"NewAgency"]]]
    }

    void "test reportFieldsForQueryValue"(){
        ReportField reportField = new ReportField(name:"field",dictionaryType: DictionaryTypeEnum.EVENT,dictionaryLevel: 10,isAutocomplete: false,dataType: Integer,isText: false,listDomainClass: Integer)
        reportField.save(failOnError:true,validate:false)
        SourceProfile.metaClass.static.fetchAllCaseNumberFieldNames = {["caseNum"]}
        when:
        params.name = "field"
        controller.reportFieldsForQueryValue()
        then:
        response.json.size() == 10
    }

    void "test getQueryNameDescription"(){
        User normalUser = makeNormalUser("normalUser",[])
        QueryUserState queryUserState = new QueryUserState(isFavorite: true,user: normalUser)
        queryUserState.save(failOnError:true,flush:true,validate:false)
        SuperQuery superQuery = new SuperQuery(name: "query",description: "description",createdBy: "user",modifiedBy: "user",owner: normalUser,lastExecuted: new Date(),queryType: QueryTypeEnum.QUERY_BUILDER)
        superQuery.save(failOnError:true,flush:true,validate:false)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> normalUser }
        controller.userService = userMock.proxyInstance()
        when:
        controller.getQueryNameDescription(superQuery.id)
        then:
        response.json == [qced:false, text:"query (description) - Owner: normalUser", isFavorite:false]
    }

    void "test index"(){
        User normalUser = makeNormalUser("normalUser",[])
        int run = 0
        QueryUserState queryUserState = new QueryUserState(isFavorite: true,user: normalUser)
        queryUserState.save(failOnError:true,flush:true,validate:false)
        SuperQuery superQuery = new SuperQuery(name: "query",description: "description",createdBy: "user",modifiedBy: "user",owner: normalUser,lastExecuted: new Date(),queryType: QueryTypeEnum.QUERY_BUILDER,qualityChecked: false,tags: [new Tag(name: "tag")])
        superQuery.save(failOnError:true,flush:true,validate:false)
        def userMock = new MockFor(UserService)
        userMock.demand.getUser(0..2) { -> normalUser }
        userMock.demand.getCurrentUser(0..1) { -> normalUser }
        controller.userService = userMock.proxyInstance()
        SuperQuery.metaClass.static.fetchAllIdsBySearchString = { LibraryFilter filter, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0, sort: "", order: ""]
                    run++
                    return [[superQuery.id]]
                }
            }
        }
        SuperQuery.metaClass.static.countRecordsBySearchString = { LibraryFilter filter -> new Object(){
                int get(LinkedHashMap H){
                    run++
                    return 1
                }
            }
        }
        def mockQueryService = new MockFor(QueryService)
        mockQueryService.demand.getUsagesCount(0..1){SuperQuery query ->
            run++
            return 1
        }
        superQuery.queryService = mockQueryService.proxyInstance()
        when:
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.index()
        then:
        run == 4
        response.json.aaData[0].size() == 15
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
    }

    void "test toMap"(){
        User normalUser = makeNormalUser("normalUser",[])
        QueryUserState queryUserState = new QueryUserState(isFavorite: true,user: normalUser)
        queryUserState.save(failOnError:true,flush:true,validate:false)
        SuperQuery superQuery = new SuperQuery(name: "query",description: "description",createdBy: "user",modifiedBy: "user",owner: normalUser,lastExecuted: new Date(),queryType: QueryTypeEnum.QUERY_BUILDER,qualityChecked: false,tags: [new Tag(name: "tag")])
        superQuery.save(failOnError:true,flush:true,validate:false)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> normalUser }
        controller.userService = userMock.proxyInstance()
        def mockQueryService = new MockFor(QueryService)
        mockQueryService.demand.getUsagesCount(0..1){SuperQuery query ->
            return 1
        }
        superQuery.queryService = mockQueryService.proxyInstance()
        when:
        def result = controller.invokeMethod('toMap', [superQuery] as Object[])
        then:
        result.size() == 15
    }
}
