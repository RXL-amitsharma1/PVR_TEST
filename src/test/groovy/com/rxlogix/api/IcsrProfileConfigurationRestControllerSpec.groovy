package com.rxlogix.api

import com.rxlogix.LibraryFilter
import com.rxlogix.SqlGenerationService
import com.rxlogix.UserService
import com.rxlogix.config.*
import com.rxlogix.mapping.SafetyCalendar
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User, ReportConfiguration, SafetyCalendar, ExecutedIcsrProfileConfiguration])
class IcsrProfileConfigurationRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<IcsrProfileConfigurationRestController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference, IcsrProfileConfiguration, ExecutedIcsrProfileConfiguration
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

    void "test list"(){
        User normalUser = makeNormalUser("user",[])
        IcsrProfileConfiguration icsrProfileConfiguration = new IcsrProfileConfiguration(reportName: "report",senderOrganization: new UnitConfiguration(unitName: "sender",organizationType: new IcsrOrganizationType(name: "sender_type",langId: "1")),recipientOrganization: new UnitConfiguration(unitName: "recipient",organizationType: new IcsrOrganizationType(name: "recipient_type",langId: "1")),owner: normalUser)
        icsrProfileConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = Mock(UserService)
        mockUserService.currentUser >> { return normalUser}
        controller.userService = mockUserService
        def mockSqlService = new MockFor(SqlGenerationService)
        mockSqlService.demand.getPVALanguageId(0..10) { String locale -> return 1 }
        controller.sqlGenerationService = mockSqlService.proxyInstance()
        IcsrProfileConfiguration.metaClass.static.getAllIdsByFilter = { LibraryFilter filter, boolean showXMLOption = false, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){
                    return [[icsrProfileConfiguration.id]]
                }
            }
        }
        IcsrProfileConfiguration.metaClass.static.countRecordsByFilter = {  LibraryFilter filter, boolean showXMLOption = false  -> new Object(){
                int get(Object o){
                    return 1
                }
            }
        }
        when:
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.list()
        then:
        response.json.aaData[0].size() == 12
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test profileList name matches search"(){
        User normalUser = makeNormalUser("user",[])
        def mockUserService = Mock(UserService)
        mockUserService.currentUser >> { return normalUser}
        controller.userService = mockUserService
        ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = new ExecutedIcsrProfileConfiguration(numOfExecutions:1, reportName:"reportName", senderOrganizationName: "senderOrganizationName", senderTypeName: "senderTypeName", recipientOrganizationName: "recipient_organization", recipientTypeName: "recipient_type", owner:normalUser)
        executedIcsrProfileConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedIcsrProfileConfiguration.metaClass.static.getAllIcsrProfileConfBySearchString = { String search, User user  ->
            new Object() {
                List list(Object o) {
                    return [[1, 1, "reportName", "senderOrganizationName", "senderTypeName", "recipient_organization", "recipient_type", "22-Aug", normalUser, "22-Aug"]]
                }
            }
        }
         ExecutedIcsrProfileConfiguration.metaClass.static.countAllIcsrProfileConfBySearchString = { String search, User user  ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        when:
        params.searchString = "reportName"
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.profileList()
        then:
        response.json.aaData[0].size() == 11
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test profileList with no search"(){
        User normalUser = makeNormalUser("user",[])
        def mockUserService = Mock(UserService)
        mockUserService.currentUser >> { return normalUser}
        controller.userService = mockUserService
        ExecutedIcsrProfileConfiguration executedIcsrProfileConfiguration = new ExecutedIcsrProfileConfiguration(numOfExecutions:1, reportName:"reportName", senderOrganizationName: "senderOrganizationName", senderTypeName: "senderTypeName", recipientOrganizationName: "recipient_organization", recipientTypeName: "recipient_type", owner:normalUser)
        executedIcsrProfileConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedIcsrProfileConfiguration.metaClass.static.getAllIcsrProfileConfBySearchString = { String search, User user ->
            new Object() {
                List list(Object o) {
                    return [[1, 1, "reportName", "senderOrganizationName", "senderTypeName", "recipient_organization", "recipient_type", "22-Aug", normalUser, "22-Aug"]]
                }
            }
        }
        ExecutedIcsrProfileConfiguration.metaClass.static.countAllIcsrProfileConfBySearchString = { String search, User user ->
            new Object() {
                Long get() {
                    return 1
                }
            }
        }
        when:
        params.searchString = ""
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.profileList()
        then:
        response.json.aaData[0].size() == 11
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }
   
    void "test getIcsrCalendarNames empty"(){
        boolean run = false
        SafetyCalendar.metaClass.static.withNewSession = {Closure closure -> run = true}
        SafetyCalendar.metaClass.static.findAllByIsDeleted = {Boolean isDeleted  -> return []}
        when:
        controller.getIcsrCalendarNames("", 1, 30)
        then:
        run == true
        response.json.items != null
        response.json.total_count == 0
    }
}
