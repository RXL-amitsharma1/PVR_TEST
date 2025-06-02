package com.rxlogix

import com.rxlogix.commandObjects.SpotfireCommand
import com.rxlogix.config.Tenant
import com.rxlogix.mapping.LmProductFamily
import com.rxlogix.spotfire.SpotfireService
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import groovy.json.JsonBuilder
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User, SpotfireCommand])
class DataAnalysisControllerSpec extends Specification implements DataTest, ControllerUnitTest<DataAnalysisController> {

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
        normalUser.metaClass.static.isDev = { -> return false }
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
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user").save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isAdmin = { -> return true }
        adminUser.metaClass.getUserTeamIds = { -> [] }
        adminUser.metaClass.static.isDev = { -> return true }
        return adminUser
    }

    def setup() {
        Holders.config.spotfire.token_secret = "rxlogix"
        Holders.config.spotfire.libraryRoot = "/Reports"
        Holders.config.spotfire.callbackUrl = "http://10.100.6.8:8080/reports/spotfire/rx_validate\""
    }

    def setupSpec() {
        mockDomains User,Tenant,Role,UserRole
    }

    void "test index"(){
        given:
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {adminUser}
        controller.userService=mockUserService
        when:
        controller.index()
        then:
        response.status==200
    }

    void "test create"(){
        given:
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {adminUser}
        controller.userService=mockUserService
        def mockSpotfireService=Mock(SpotfireService)
        mockSpotfireService.getHashedValue(_)>>{}
        controller.spotfireService=mockSpotfireService
        when:
        controller.create()
        then:
        response.status==200
    }

    void "test view not user"(){
        given:
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {adminUser}
        mockUserService.getUserByUsername()>>{}
        controller.userService=mockUserService
        when:
        controller.view("test")
        then:
        response.status==403
    }

    void "test view with user"() {
        given:
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {adminUser}
        mockUserService.getUserByUsername(_)>>{adminUser}
        controller.userService=mockUserService
        def mockSpotfireService = Mock(SpotfireService)
        mockSpotfireService.addAuthToken(_, _, _, _) >> {}
        mockSpotfireService.decodeFileName(_) >> "U_test"  // mock decodeFileName to return a valid prefix
        controller.spotfireService = mockSpotfireService

        when:
        controller.view("folder/test")

        then:
        response.status == 200
    }


    void "test list"(){
        given:
        def mockSpotfireService=Mock(SpotfireService)
        mockSpotfireService.getReportFilesMapData()>>{[]}
        controller.spotfireService=mockSpotfireService
        when:
        controller.list()
        then:
        response.status==200
    }

    void "test generate request method get"(){
        given:
        SpotfireCommand spotfireCommand=new SpotfireCommand()
        when:
        request.method="GET"
        controller.generate(spotfireCommand)
        then:
        response.status==302
        response.redirectedUrl=="/dataAnalysis/index"
    }


    void "test generate saved and validate"() {
        given:
        SpotfireCommand spotfireCommand = new SpotfireCommand(productFamilyIds: "test", fromDate: new Date(), endDate: new Date(), asOfDate: new Date(), type: "test1", caseSeriesId: 2L)
        SpotfireCommand.metaClass.static.validate = { -> true }
        def mockSpotfireService = Mock(SpotfireService)
        mockSpotfireService.reserveFileName(_) >> {}
        mockSpotfireService.generateReportParams(_, _, _, _, _, _, _, _) >> {
            return [:]
        }
        mockSpotfireService.invokeReportGenerationAPI(_) >> {
            return new JsonBuilder([JobId: '123', StatusCode: '200']).toString()
        }
        controller.spotfireService = mockSpotfireService
        when:
        request.method = "POST"
        controller.generate(spotfireCommand)
        then:
        response.status == 302
        response.redirectedUrl == "/dataAnalysis/index"
    }

    void "test accessDenied"(){
        when:
        controller.accessDenied()
        then:
        response.status==200
    }

    void "test keepAlive"(){
        when:
        controller.keepAlive()
        then:
        response.status==200
    }

    void "test getProductFamilyList"(){
        given:
        LmProductFamily lmProductFamily= new LmProductFamily(name: "rxm",lang: "test")
        User adminUser = makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        adminUser.preference.locale=new Locale("en")
        controller.userService=mockUserService
        def mockSpotfireService=Mock(SpotfireService)
        mockSpotfireService.appendLingualSuffix(_,_)>>{}
        controller.spotfireService=mockSpotfireService
        when:
        controller.getProductFamilyList()
        then:
        response.status==200
    }
}