package com.rxlogix.api

import com.rxlogix.CustomMessageService
import com.rxlogix.SearchService
import com.rxlogix.UserService
import com.rxlogix.config.*
import com.rxlogix.user.*
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import org.apache.http.HttpStatus
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SpringSecurityUtils, User, UserGroup, ReportRequest, ReportConfiguration, ExecutionStatus])
class UserRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<UserRestController> {

    def setup() {
    }

    def cleanup() {
    }

    private User makeNormalUser(name, team) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(isBlinded: true,username: "user_1", password: 'user', fullName: "peter fletcher", preference: preferenceNormal, createdBy: "user", modifiedBy: "user")
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true,flush:true)
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
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, UserGroupRole
    }

    void "test index"(){
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> return true}
        int run = 0
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        Role role = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: "user", modifiedBy: "user")
        role.save(flush: true)
        User normalUser = new User(username: "user_1", password: 'user', fullName: "peter fletcher", preference: preferenceNormal, createdBy: "user", modifiedBy: "user",lastLogin: new Date())
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true,flush:true)
        UserRole userRole = new UserRole(user: normalUser,role: role)
        userRole.save(flush:true)
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> return true}
        def mockSearchService = new MockFor(SearchService)
        mockSearchService.demand.getUserList(0..1){Map params -> [[normalUser],1]}
        controller.searchService = mockSearchService.proxyInstance()
        def mockCustomMessageService = new MockFor(CustomMessageService)
        mockCustomMessageService.demand.getMessage(0..1){String code, Object... args = null ->
            run++
            return "message"
        }
        userRole.role.customMessageService = mockCustomMessageService.proxyInstance()
        UserGroup.metaClass.static.fetchAllUserGroupByUser = {User user ->
            run++
            return []
        }
        when:
        controller.index()
        then:
        run == 1
        response.json.aaData[0].size() == 8
        response.json.recordsFiltered == 1
        response.json.recordsTotal == 1
    }

    void "test splitResult offset is 0"(){
        User normalUser = makeNormalUser("user",[])
        def items = []
        when:
        controller.splitResult(items,0,10,[normalUser],[normalUser])
        then:
        items.size() == 2
    }

    void "test splitResult offset greater than 0"(){
        User normalUser = makeNormalUser("user",[])
        def items = []
        when:
        controller.splitResult(items,1,10,[normalUser,new User(username: "pranjal")],[normalUser,new User(username: "pranjal")])
        then:
        items.size() == 2
        items[0].children.size() == 1
        items[1].children.size() == 2
    }

    void "test splitResult userOffset greater than 0"(){
        User normalUser = makeNormalUser("user",[])
        def items = []
        when:
        controller.splitResult(items,2,10,[normalUser],[normalUser,new User(username: "pranjal")])
        then:
        items.size() == 1
    }

    void "test sharedWithUserList"(){
        User normalUser = makeNormalUser("user",[])
        ExecutionStatus.metaClass.static.getActiveUsersAndUserGroups = {User user, String term -> return [normalUser] as Set}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.offset = 4
        controller.sharedWithUserList()
        then:
        response.json.total_count == 4
        response.json.items.size() == 3
    }


    void "test sharedWithUserList offset 0"(){
        User normalUser = makeNormalUser("user",[])
        ExecutionStatus.metaClass.static.getActiveUsersAndUserGroups = {User user, String term -> return [normalUser] as Set}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.max = 4
        params.offset = 3
        controller.sharedWithUserList()
        then:
        response.json.total_count == 4
        response.json.items.size() == 3
    }

    void "test sharedWithFilterList when clazz is PeriodicReportConfiguration and offset is 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.getActiveUsersAndUserGroups = { String clazz,User user, String term-> return [users: [normalUser], userGroups: [userGroup]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("",0,0, PeriodicReportConfiguration.name)
        then:
        response.json.total_count == 5
        response.json.items.size() == 5
    }

    void "test sharedWithFilterList when clazz is PeriodicReportConfiguration and offset greater than 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.getActiveUsersAndUserGroups = { String clazz,User user, String term-> return [users: [normalUser,new User(username: "pranjal",isBlinded: false)], userGroups: [userGroup,new UserGroup(name: "group_new",isBlinded: false)]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("",5,1, PeriodicReportConfiguration.name)
        then:
        response.json.total_count == 7
        response.json.items.size() == 1
    }

    void "test sharedWithFilterList when clazz is PeriodicReportConfiguration and useroffset greater than 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.getActiveUsersAndUserGroups = { String clazz,User user, String term-> return [users: [normalUser,new User(username: "pranjal",isBlinded: false)], userGroups: [userGroup]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("",6,1, PeriodicReportConfiguration.name)
        then:
        response.json.total_count == 6
        response.json.items.size() == 1
    }

    void "test sharedWithFilterList when clazz is PeriodicReportConfiguration term is not empty"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.getActiveUsersAndUserGroups = { String clazz,User user, String term-> return [users: [normalUser], userGroups: [userGroup]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("Mine (I'm the owner)",0,0, PeriodicReportConfiguration.name)
        then:
        response.json.total_count == 5
        response.json.items.size() == 2
    }

    void "test sharedWithFilterList when clazz is Configuration and offset is 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.getActiveUsersAndUserGroups = { String clazz,User user, String term-> return [users: [normalUser], userGroups: [userGroup]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("",0,0, Configuration.name)
        then:
        response.json.total_count == 5
        response.json.items.size() == 5
    }

    void "test sharedWithFilterList when clazz is Configuration and offset is greater 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.getActiveUsersAndUserGroups = { String clazz,User user, String term-> return [users: [normalUser,new User(username: "pranjal",isBlinded: false)], userGroups: [userGroup,new UserGroup(name: "group_new",isBlinded: false)]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("",5,1, Configuration.name)
        then:
        response.json.total_count == 7
        response.json.items.size() == 1
    }

    void "test sharedWithFilterList when clazz is Configuration and useroffset is greater 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.getActiveUsersAndUserGroups = { String clazz,User user, String term-> return [users: [normalUser,new User(username: "pranjal",isBlinded: false)], userGroups: [userGroup]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("",6,1, Configuration.name)
        then:
        response.json.total_count == 6
        response.json.items.size() == 1
    }

    void "test sharedWithFilterList when clazz is Configuration term is not empty"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.getActiveUsersAndUserGroups = { String clazz,User user, String term-> return [users: [normalUser], userGroups: [userGroup]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("Mine (I'm the owner)",0,0, Configuration.name)
        then:
        response.json.total_count == 5
        response.json.items.size() == 2
    }

    void "test sharedWithFilterList when clazz is ReportRequest and offset is 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportRequest.metaClass.static.getActiveUsersAndUserGroups = { User user, String term-> return [users: [normalUser], userGroups: [userGroup]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("",0,0, ReportRequest.name)
        then:
        response.json.total_count == 5
        response.json.items.size() == 4
    }

    void "test sharedWithFilterList when clazz is ReportRequest and offset is greater than 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportRequest.metaClass.static.getActiveUsersAndUserGroups = { User user, String term-> return [users: [normalUser,new User(username: "pranjal",isBlinded: false)], userGroups: [userGroup,new UserGroup(name: "group_new",isBlinded: false)]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("",4,1, ReportRequest.name)
        then:
        response.json.total_count == 7
        response.json.items.size() == 1
    }

    void "test sharedWithFilterList when clazz is ReportRequest and useroffset is greater than 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportRequest.metaClass.static.getActiveUsersAndUserGroups = { User user, String term-> return [users: [normalUser,new User(username: "pranjal",isBlinded: false)], userGroups: [userGroup]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("",5,1, ReportRequest.name)
        then:
        response.json.total_count == 6
        response.json.items.size() == 1
    }

    void "test sharedWithFilterList when clazz is ReportRequest term is not empty"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1")
        userGroup.save(failOnError:true,validate:false,flush:true)
        ReportRequest.metaClass.static.getActiveUsersAndUserGroups = { User user, String term-> return [users: [normalUser], userGroups: [userGroup]]}
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithFilterList("Mine (I'm the owner)",0,0, ReportRequest.name)
        then:
        response.json.total_count == 5
        response.json.items.size() == 2
    }

    void "test ownerFilterList when clazz is Configuration"(){
        User normalUser = makeNormalUser("user",[])
        ReportConfiguration.metaClass.static.fetchAllOwners = { User user, clazz, search-> new Object(){
                List list(Object o){
                    return [normalUser]
                }
            }
        }
        ReportConfiguration.metaClass.static.countAllOwners = { User user, clazz, search-> new Object(){
                Integer get(Object o){
                    return 1
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.ownerFilterList("",0,0,Configuration.name)
        then:
        response.json.total_count == 1
        response.json.items.size() == 1
    }

    void "test ownerFilterList when clazz is PeriodicReportConfiguration"(){
        User normalUser = makeNormalUser("user",[])
        ReportConfiguration.metaClass.static.fetchAllOwners = { User user, clazz, search-> new Object(){
                List list(Object o){
                    return [normalUser]
                }
            }
        }
        ReportConfiguration.metaClass.static.countAllOwners = { User user, clazz, search-> new Object(){
                Integer get(Object o){
                    return 1
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.ownerFilterList("",0,0,PeriodicReportConfiguration.name)
        then:
        response.json.total_count == 1
        response.json.items.size() == 1
    }

    void "test ownerFilterList when clazz is IcsrReportConfiguration"(){
        User normalUser = makeNormalUser("user",[])
        ReportConfiguration.metaClass.static.fetchAllOwners = { User user, clazz, search-> new Object(){
                List list(Object o){
                    return [normalUser]
                }
            }
        }
        ReportConfiguration.metaClass.static.countAllOwners = { User user, clazz, search-> new Object(){
                Integer get(Object o){
                    return 1
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.ownerFilterList("",0,0, IcsrReportConfiguration.name)
        then:
        response.json.total_count == 1
        response.json.items.size() == 1
    }

    void "test ownerFilterList when clazz is IcsrProfileConfiguration"(){
        User normalUser = makeNormalUser("user",[])
        ReportConfiguration.metaClass.static.fetchAllOwners = { User user, clazz, search-> new Object(){
                List list(Object o){
                    return [normalUser]
                }
            }
        }
        ReportConfiguration.metaClass.static.countAllOwners = { User user, clazz, search-> new Object(){
                Integer get(Object o){
                    return 1
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.ownerFilterList("",0,0, IcsrProfileConfiguration.name)
        then:
        response.json.total_count == 1
        response.json.items.size() == 1
    }

    void "test ownerFilterList if clazz is ExecutionStatus"(){
        User normalUser = makeNormalUser("user",[])
        ExecutionStatus.metaClass.static.fetchAllOwners = { User user, search-> new Object(){
                List list(Object o){
                    return [normalUser]
                }
            }
        }
        ExecutionStatus.metaClass.static.countAllOwners = { User user, search-> new Object(){
                Integer get(Object o){
                    return 1
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.ownerFilterList("",0,0,ExecutionStatus.name)
        then:
        response.json.total_count == 1
        response.json.items.size() == 1
    }

    void "test sharedWithValues all"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1",isBlinded: false)
        userGroup.save(failOnError:true,validate:false,flush:true)
        def id1=normalUser.id as String
        def id2=userGroup.id as String
        String executors="User_${id1};UserGroup_${id2};owner;sharedWithMe;team"
        when:
        params.ids = executors
        controller.sharedWithValues()
        then:
        response.json.size() == 5
    }

    void "test sharedWithValues none"(){
        when:
        params.ids = ""
        controller.sharedWithValues()
        then:
        response.json == []
    }

    void "test sharedWithList offset is 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1",isBlinded: true)
        userGroup.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){ String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){ String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithList("",0,0)
        then:
        response.json.total_count == 2
        response.json.items.size() == 2
    }

    void "test sharedWithList offset is greater than 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1",isBlinded: true)
        userGroup.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){ String search = null-> [normalUser,new User(username: "pranjal",isBlinded: false)]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){ String search = null-> [userGroup,new UserGroup(name: "group_new",isBlinded: false)]}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithList("",2,1)
        then:
        response.json.total_count == 4
        response.json.items.size() == 1
    }

    void "test sharedWithList useroffset is greater than 0"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup(name: "group_1",isBlinded: true)
        userGroup.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){ String search = null-> [normalUser,new User(username: "pranjal",isBlinded: false)]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){ String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.sharedWithList("",3,1)
        then:
        response.json.total_count == 3
        response.json.items.size() == 1
    }

    void "test generateAPIToken"(){
        User normalUser = makeNormalUser("user",[])
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        Holders.config.rxlogix.pvr.api.token.key= "rxlogix"
        controller.generateAPIToken()
        then:
        response.json.token != null
        response.status == HttpStatus.SC_OK
    }

    void "test userValue"(){
        User normalUser = makeNormalUser("user",[])
        when:
        params.id = normalUser.id
        controller.userValue()
        then:
        response.json == [id: normalUser.id, text:"peter fletcher" ]
    }

    void "test listUsers"(){
        User normalUser = makeNormalUser("user",[])
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.findAllUsersHavingFullName(0..1){ -> [normalUser]}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.listUsers()
        then:
        response.json == [[fullName:"peter fletcher", id:1, username:"user_1"]]
        response.status == HttpStatus.SC_OK
    }
}
