package com.rxlogix.util

import com.rxlogix.*
import com.rxlogix.config.*
import com.rxlogix.enums.IcsrProfileSubmissionDateOptionEnum
import com.rxlogix.mapping.SafetyCalendar
import com.rxlogix.user.*
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.mock.interceptor.MockFor
import org.hibernate.FlushMode
import org.hibernate.SessionFactory
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Tenants, User, Configuration, SafetyCalendar, SourceProfile, IcsrProfileConfiguration, SpringSecurityUtils])
class IcsrProfileConfigurationControllerSpec extends Specification implements DataTest, ControllerUnitTest<IcsrProfileConfigurationController> {

    public static final user = "unitTest"

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference,ReportField,SuperQuery,ParameterValue, CaseSeries, DataTabulationTemplate, SourceProfile, QueryValueList, CaseSeriesDateRangeInformation, Tag,CaseDeliveryOption, EmailConfiguration, ExecutionStatus, ExecutedCaseSeries, ReportResult, ExecutedCaseSeriesDateRangeInformation, ExecutedCaseDeliveryOption, DateRangeType,IcsrProfileConfiguration, FieldProfile
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


    // couldn't put these in the setup/cleanup because of issues w/ @Shared. May be missing something to make that work. This is workaround.
    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en") ,createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", email: "user@rxlogix.com", preference: preferenceNormal, createdBy: user, modifiedBy: user)
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
        normalUser.metaClass.static.isDev = { -> return false}
        return normalUser
    }

    private User makeThirdUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"))
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def thirdUser = new User(username: 'third', password: 'third', fullName: "Joe Doe", email: "third@rxlogix.com", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        thirdUser.addToTenants(tenant)
        thirdUser.save(failOnError: true)
        UserRole.create(thirdUser, userRole, true)
        return thirdUser
    }

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", email: "admin@rxlogix.com", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isAdmin = { -> return true }
        adminUser.metaClass.getUserTeamIds = { -> [] }
        adminUser.metaClass.static.isDev = { -> return true}
        return adminUser
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(SpringSecurityService)
        securityMock.demand.getCurrentUser { -> user }
        return securityMock.proxyInstance()
    }

    void "test index"(){
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test copy"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        def mockConfigurationService = Mock(ConfigurationService)
        Configuration copyConfig = new Configuration(id: 2L)
        mockConfigurationService.copyConfig(_,_) >> {return copyConfig}
        controller.configurationService = mockConfigurationService
        Configuration.metaClass.hasErrors = { -> false }
        when:
        controller.copy(config)
        then:
        response.status == 302
        response.redirectedUrl == urlVal
        where:
        config                               | urlVal
        new IcsrProfileConfiguration(id: 1L) | '/icsrProfileConfiguration/view'
        null                                 | '/icsrProfileConfiguration/index'
    }

    void "test edit when instance does not exist"(){
        given:
        IcsrProfileConfiguration.metaClass.static.read = {Long id -> null}
        when:
        params.id = 1L
        controller.edit()
        then:
        response.status == 302
        response.redirectedUrl == '/icsrProfileConfiguration/index'
    }

    void "test edit"(){
        given:
        boolean run = false
        SafetyCalendar.metaClass.static.withNewSession = {Closure closure -> run = true}
        SafetyCalendar.metaClass.static.read = {Serializable serializable -> new SafetyCalendar(id:1,name:"xyz",isDeleted:false)}
        IcsrProfileConfiguration icsrProfileConInstance = new IcsrProfileConfiguration(id: 1L,recipientOrganization: new UnitConfiguration(), senderOrganization: new UnitConfiguration(),
                e2bDistributionChannel: new DistributionChannel(), fieldProfile: new FieldProfile(), submissionDateFrom: IcsrProfileSubmissionDateOptionEnum.ACK)
        def mockUserService = Mock(UserService)
        mockUserService.currentUser >> { return makeAdminUser()}
        controller.userService = mockUserService
        IcsrProfileConfiguration.metaClass.static.read = {Long id -> icsrProfileConInstance}
        User.metaClass.isConfigurationTemplateCreator = {return true}
        IcsrProfileConfiguration.metaClass.isRunning = { -> isRunning}
        IcsrProfileConfiguration.metaClass.isEditableBy = {User currentUser -> isEditableBy}
        def mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.fetchConfigurationMapFromSession(_,_) >> {[configurationParams: [id: '2'], templateQueryIndex: [index: 1L, type: "template"]]}
        controller.configurationService = mockConfigurationService
        Tenants.metaClass.static.currentId = { -> return 1L}
        UserGroup.metaClass.static.fetchAllFieldProfileByUser = {User currentUser -> new FieldProfile()}
        SourceProfile.metaClass.static.sourceProfilesForUser = {User currentUser -> new SourceProfile()}
        when:
        params.id = 1L
        controller.edit()
        then:
        response.status == statusVal
        where:
        isRunning | isEditableBy | statusVal
        false     | true         | 200
        true      | true         | 302
        false     | false        | 302
    }

    void "test delete -- not Found"(){
        when:
        request.method = 'POST'
        controller.delete(null)
        then:
        response.status == 302
        response.redirectedUrl == '/icsrProfileConfiguration/index'
    }

    void "test delete -- success"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.currentUser >> { return makeAdminUser()}
        controller.userService = mockUserService
        IcsrProfileConfiguration.metaClass.isEditableBy = {User currentUser -> true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_) >> {theInstance,name, String justification -> true}
        controller.CRUDService = mockCRUDService

        when:
        params.deleteJustification = 'Test Delete'
        request.method = 'DELETE'
        IcsrProfileConfiguration icsrProfileConfiguration = new IcsrProfileConfiguration(id: 1L,recipientOrganization: new UnitConfiguration(), senderOrganization: new UnitConfiguration(),
                e2bDistributionChannel: new DistributionChannel(), fieldProfile: new FieldProfile(), submissionDateFrom: IcsrProfileSubmissionDateOptionEnum.ACK)
        controller.delete(icsrProfileConfiguration)

        then:
        flash.message == 'default.deleted.message'
        response.status == 302
        response.redirectedUrl == '/icsrProfileConfiguration/index'
    }

    void "test delete --failure"(){
        given:
        IcsrProfileConfiguration icsrProfileConInstance = new IcsrProfileConfiguration(id: 1L,recipientOrganization: new UnitConfiguration(), senderOrganization: new UnitConfiguration(),
                e2bDistributionChannel: new DistributionChannel(), fieldProfile: new FieldProfile(), submissionDateFrom: IcsrProfileSubmissionDateOptionEnum.ACK)
        def mockUserService = Mock(UserService)
        mockUserService.currentUser >> { return makeAdminUser()}
        controller.userService = mockUserService
        IcsrProfileConfiguration.metaClass.isEditableBy = {User currentUser -> isEditableBy}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_) >> {theInstance,name, String justification -> throw new ValidationException("Validation Exception", new ValidationErrors(icsrProfileConInstance))}
        controller.CRUDService = mockCRUDService

        when:
        params.deleteJustification = 'Test Delete'
        request.method = 'DELETE'
        IcsrProfileConfiguration icsrProfileConfiguration = new IcsrProfileConfiguration(id: 1L,recipientOrganization: new UnitConfiguration(), senderOrganization: new UnitConfiguration(),
                e2bDistributionChannel: new DistributionChannel(), fieldProfile: new FieldProfile(), submissionDateFrom: IcsrProfileSubmissionDateOptionEnum.ACK)
        controller.delete(icsrProfileConfiguration)
        then:
        response.status == 302
        response.redirectedUrl == urlVal
        where:
        isEditableBy | urlVal
        true         | '/icsrProfileConfiguration/view'
        false        | '/icsrProfileConfiguration/index'
    }

    void "test save request method GET"(){
        when:
        request.method = 'GET'
        Tenants.withId(1) {
            controller.save()
        }
        then:
        flash.error == 'default.not.saved.message'
        response.redirectUrl == '/icsrProfileConfiguration/index'
    }

    void "test save - invalid tenant"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return false}
        Tenants.metaClass.static.currentId = { return 1L}

        when:
        request.method = 'POST'
        params.tenantId = '10'
        controller.save()

        then:
        flash.error == "invalid.tenant"
        response.status == 302
        response.redirectedUrl == '/icsrProfileConfiguration/create'
    }

    void "test save -- success"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        Tenants.metaClass.static.currentId = { return 1L}
        def mockIcsrProfileConfigurationService =new MockFor( IcsrProfileConfigurationService )
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ IcsrProfileConfiguration configurationInstance, def params ->
            true
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){IcsrProfileConfiguration configurationInstance-> }
        mockIcsrProfileConfigurationService.demand.bindSharedWith(0..1){IcsrProfileConfiguration configurationInstance, List<String> sharedWith, Boolean isUpdate ->
            new IcsrProfileConfiguration()
        }
        mockIcsrProfileConfigurationService.demand.saveUpdate(0..1){IcsrProfileConfiguration icsrProfileConfInstance-> true}
        controller.icsrProfileConfigurationService = mockIcsrProfileConfigurationService.proxyInstance()
        controller.configurationService = mockConfigurationService.proxyInstance()
        IcsrProfileConfiguration.metaClass.static.preValidateTemplate = {return new Configuration()}
        when:
        request.method = 'POST'
        controller.save()

        then:
        response.status == 302
        response.redirectedUrl == '/icsrProfileConfiguration/create'
    }

    void "test save -- Failure"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        def mockSession = Mock(org.hibernate.Session)
        mockSession.setFlushMode(_ as FlushMode) >> {true}
        def mockSessionFactory = Mock(SessionFactory)
        mockSessionFactory.getCurrentSession() >> {return mockSession}
        controller.sessionFactory = mockSessionFactory
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        Tenants.metaClass.static.currentId = { return 1L}
        def mockIcsrProfileConfigurationService =new MockFor( IcsrProfileConfigurationService )
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ IcsrProfileConfiguration configurationInstance, def params ->
            true
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){IcsrProfileConfiguration configurationInstance-> }
        mockIcsrProfileConfigurationService.demand.bindSharedWith(0..1){IcsrProfileConfiguration configurationInstance, List<String> sharedWith, Boolean isUpdate ->
            new IcsrProfileConfiguration()
        }
        mockIcsrProfileConfigurationService.demand.saveUpdate(0..1){IcsrProfileConfiguration icsrProfileConfInstance-> throw error}
        controller.icsrProfileConfigurationService = mockIcsrProfileConfigurationService.proxyInstance()
        controller.configurationService = mockConfigurationService.proxyInstance()
        IcsrProfileConfiguration.metaClass.static.preValidateTemplate = {return new Configuration()}
        when:
        request.method = 'POST'
        controller.save()
        then:
        response.status == statusVal
        response.redirectedUrl == urlVal

        where:
        error                                                                                      | statusVal | urlVal
        new ValidationException("Validation Exception", new ValidationErrors(new Configuration())) | 302       | '/icsrProfileConfiguration/create'
        new Exception()                                                                            | 302       | '/icsrProfileConfiguration/create'
    }

    void "test Update -- GET"(){
        when:
        request.method = 'GET'
        controller.update()

        then:
        response.status == 302
        response.redirectedUrl == '/icsrProfileConfiguration/index'
    }

    void "test Update -- not Found"(){
        given:
        if(icsrProfileConfiguration )
            icsrProfileConfiguration .setVersion(2L)
        IcsrProfileConfiguration.metaClass.static.lock = { Serializable serializable -> return icsrProfileConfiguration}

        when:
        request.method = 'POST'
        params.version = '1'
        controller.update()

        then:
        response.status == statusVal
        response.redirectedUrl == urlVal

        where:
        icsrProfileConfiguration             | statusVal | urlVal
        null                                 | 302       | '/icsrProfileConfiguration/index'
        new IcsrProfileConfiguration(id: 1L) | 302       | '/icsrProfileConfiguration/edit'
    }

    void "test update -- invalid tenant"(){
        given:
        IcsrProfileConfiguration.metaClass.static.lock = {Serializable serializable -> return new IcsrProfileConfiguration(id: 1L)}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return false}
        Tenants.metaClass.static.currentId = { return 1L}

        when:
        request.method = 'POST'
        params.tenantId = '10'
        controller.update()

        then:
        response.status == 302
        response.redirectedUrl == '/icsrProfileConfiguration/edit'
    }

    void "test update -- success"(){
        given:
        IcsrProfileConfiguration.metaClass.static.lock = {Serializable serializable -> return new IcsrProfileConfiguration(id: 1L)}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        Tenants.metaClass.static.currentId = { return 1L}
        def mockIcsrProfileConfigurationService =new MockFor( IcsrProfileConfigurationService )
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ IcsrProfileConfiguration configurationInstance, def params ->
            true
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){IcsrProfileConfiguration configurationInstance-> }
        mockIcsrProfileConfigurationService.demand.bindSharedWith(0..1){IcsrProfileConfiguration configurationInstance, List<String> sharedWith, Boolean isUpdate ->
            new IcsrProfileConfiguration()
        }
        mockIcsrProfileConfigurationService.demand.saveUpdate(0..1){IcsrProfileConfiguration icsrProfileConfInstance-> true}
        controller.icsrProfileConfigurationService = mockIcsrProfileConfigurationService.proxyInstance()
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        request.method = 'POST'
        params.reportId = reportId
        controller.update()
        then:
        response.status == 302
        response.redirectedUrl == urlVal
        where:
        reportId | urlVal
        1L       | '/icsrProfileConfiguration/edit'
        null     | '/icsrProfileConfiguration/edit'
    }

    void "test update -- failure"(){
        given:
        IcsrProfileConfiguration.metaClass.static.lock = {Serializable serializable -> return new IcsrProfileConfiguration(id: 1L)}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        def mockSession = Mock(org.hibernate.Session)
        mockSession.setFlushMode(_ as FlushMode) >> {true}
        def mockSessionFactory = Mock(SessionFactory)
        mockSessionFactory.getCurrentSession() >> {return mockSession}
        controller.sessionFactory = mockSessionFactory
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        Tenants.metaClass.static.currentId = { return 1L}
        def mockIcsrProfileConfigurationService =new MockFor( IcsrProfileConfigurationService )
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ IcsrProfileConfiguration configurationInstance, def params ->
            true
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){IcsrProfileConfiguration configurationInstance-> }
        mockIcsrProfileConfigurationService.demand.bindSharedWith(0..1){IcsrProfileConfiguration configurationInstance, List<String> sharedWith, Boolean isUpdate ->
            new IcsrProfileConfiguration()
        }
        mockIcsrProfileConfigurationService.demand.saveUpdate(0..1){IcsrProfileConfiguration icsrProfileConfInstance-> throw error}
        controller.icsrProfileConfigurationService = mockIcsrProfileConfigurationService.proxyInstance()
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        request.method = 'POST'
        controller.update()

        then:
        response.status == statusVal
        response.redirectedUrl == urlVal

        where:
        error                                                                                                               | statusVal | urlVal
        new ValidationException("Validation Exception", new ValidationErrors(new IcsrProfileConfigurationControllerSpec())) | 302       | '/icsrProfileConfiguration/edit'
        new Exception()                                                                                                     | 302       | '/icsrProfileConfiguration/edit'
    }

    void "test view"(){
        given:
        IcsrProfileConfiguration.metaClass.static.read = {-> icsrProfileConfInstance }
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> ifAnyGranted}
        def mockConfigurationService = Mock(ConfigurationService)
        def icsrProfileConfigurationMap = ["testKey1": "testMap1", "testKey2": "testMap2"] as JSON
        mockConfigurationService.getConfigurationAsJSON(_) >> {return icsrProfileConfigurationMap}
        controller.configurationService = mockConfigurationService
        def mockReportExecutorService = Mock(ReportExecutorService)
        def sqlList = [["testKey1": "testMap1", "testKey2": "testMap2"], ["testKey3": "testMap3", "testKey4": "testMap4"]]
        mockReportExecutorService.debugReportSQL(_) >> {return sqlList}
        controller.reportExecutorService = mockReportExecutorService

        when:
        params.viewConfigJSON = viewConfigJSON
        params.viewSql = "true"
        params.id = 1L
        controller.view()

        then:
        response.status == statusVal
        response.redirectedUrl == urlVal

        where:
        icsrProfileConfInstance                                                                                    | viewConfigJSON | ifAnyGranted | statusVal | urlVal
        new IcsrProfileConfiguration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | true           | true         | 200       | null
        new IcsrProfileConfiguration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | true           | false        | 200       | null
        new IcsrProfileConfiguration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | false          | true         | 200       | null
        new IcsrProfileConfiguration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | false          | false        | 200       | null
    }

}
