package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.enums.*
import com.rxlogix.pvdictionary.config.DictionaryConfig
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.user.*
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.mock.interceptor.MockFor
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ReportConfiguration, Configuration, GrailsHibernateUtil, AuditLogConfigUtil, DateUtil, ExecutedReportConfiguration, Tenants, SpringSecurityUtils])
class ConfigurationControllerSpec extends Specification implements DataTest, ControllerUnitTest<ConfigurationController> {
    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()
    public static final user = "unitTest"

    //Use this to get past the constraint that requires a JSONQuery string.
    def JSONQuery = /{ "all": { "containerGroups": [   { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }/

    def setup() {
    }

    def cleanup() {}

    def setupSpec() {
        mockDomains TemplateQuery,ExecutedConfiguration, Configuration, User, Role,UserRole, Tag, Query, ReportField,ReportFieldGroup, SourceColumnMaster,ReportFieldInfo, ReportConfiguration, ReportFieldInfoList, ReportTask, CaseLineListingTemplate, Tenant,UserGroup, UserGroupUser, Role,Preference, SuperQuery, ParameterValue,PeriodicReportConfiguration, QueryValueList, CaseSeriesDateRangeInformation, Tag, DeliveryOption, EmailConfiguration, ExecutionStatus,DmsConfiguration, ReportResult,ExecutedPeriodicReportConfiguration,ExecutedTemplateQuery,ExecutedReportConfiguration,CustomSQLValue,ExecutedQueryValueList,ExecutedTemplateValueList,TemplateValueList,DateRangeInformation,IcsrTemplateQuery,ReportTask,ReportTemplate, SourceProfile, DataTabulationTemplate
        TimeZone.setDefault(TimeZone.getTimeZone(TEST_TZ))
        mockTemplateServiceForReportTemple()
        GrailsHibernateUtil.metaClass.static.unwrapIfProxy = { obj ->
            return obj
        }
        AuditLogConfigUtil.metaClass.static.logChanges = { domain, Map newMap, Map oldMap, String eventName, String extraValue -> }
    }

    def cleanupSpec() {
        TimeZone.setDefault(ORIGINAL_TZ)
        User.metaClass.encodePassword = null
        cleanUpTemplateServiceMock()
    }

    private void mockTemplateServiceForReportTemple() {
        def templateService = new MockFor(TemplateService).proxyInstance()
        DataTabulationTemplate.metaClass.getTemplateService = {
            return templateService
        }
        ReportTemplate.metaClass.getTemplateService = {
            return templateService
        }

        CaseLineListingTemplate.metaClass.getTemplateService = {
            return templateService
        }
    }

    private void cleanUpTemplateServiceMock() {
        DataTabulationTemplate.metaClass.getTemplateService = null
        ReportTemplate.metaClass.getTemplateService = null
        CaseLineListingTemplate.metaClass.getTemplateService = null
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

    private makeTaskTemplateService() {
        def userMock = new MockFor(TaskTemplateService)
        userMock.demand.fetchReportTasksFromRequest(0..1) { params -> [] }
        return userMock.proxyInstance()
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

    private getEmailList() {
        return ["admin@rxlogix.com", "test@rxlogix.com"]
    }

/*
    Commenting out unit test for the following reasons:
    1.  We're using LDAP now to store email addresses
    2.  LDAP won't be available as a unit test, but this could be moved to an integration test

    Leaving the code in place in the event we choose to implement this as an integration test and/or mock up what would be returned by LDAP. - morett

 */
//    void "To Check if all emails returned are Unique"() {
//        given: "Three users"
//        def normalUser = makeNormalUser()
//        def adminUser = makeAdminUser()
//        def thirdUser = makeThirdUser()
//        and : "A report Template"
//        def fields = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class])
//        fields.save()
//        CaseLineListingTemplate template = new CaseLineListingTemplate(name: 'Test template', createdBy: adminUser, selectedFieldsColumns:fields)
//        template.save(failOnError: true)
//        and : "A query"
//        Query query = new Query([queryType: QueryType.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', createdBy: adminUser, JSONQuery: JSONQuery
//        ])
//        query.save(failOnError: true)
//        def userServiceMock = new MockFor(UserService)
//        userServiceMock.demand.getUser() {
//            return normalUser
//        }
//        controller.userService = userServiceMock.proxyDelegateInstance()
//        and :"a Delivery Option With shared With email list"
//        def email = getEmailList()
//        DeliveryOption deliveryOpt = new DeliveryOption(emailToUsers: email)
//        and : "a configuration object"
//        Configuration config = new Configuration([template: template ,query:query , reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: normalUser,deliveryOption: deliveryOpt, createdBy: normalUser.username, modifiedBy: normalUser.username])
//        config.save()
//        when:" We call method from controller"
//        List<String> result = controller.getAllEmailsUnique(config)
//        then: "the result should not contain duplicate email"
//        result == ["admin@rxlogix.com","third@rxlogix.com","test@rxlogix.com"]
//    }

//        TODO: This test belongs to ReportExecutorServiceSpec
    void "test listTemplates"() {
        given:
        ReportConfiguration.metaClass.static.fetchAllTemplatesForUser = { User user, Class clazz, String search = null ->
            new Object() {
                List list(Object o) {
                    [new Configuration(id: 1, reportName: "test1", description: "description1", dateCreated: new Date()),
                     new Configuration(id: 2, reportName: "test2", description: "description2", dateCreated: new Date())]
                }
                int count(Object o) {
                    return 2
                }
            }
        }
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..3) { new User() }
        controller.userService = userMock.proxyInstance()
        when:
        params.length = 5
        controller.listTemplates()
        then:
        response.json != null
        response.json.size() == 3
        response.json.aaData[0].size() == 4
        response.json.aaData[0].reportName == "test1"
        response.json.aaData[0].description == "description1"


    }

    void "test listPvqCfg"() {
        given:
        controller.qualityService = [listTypes: { ->
            return [[label: "pvq1", value: "pvq1"], [label: "pvq2", value: "pvq2"]]
        }]
        ReportConfiguration.metaClass.static.findAllByIsDeletedAndPvqTypeIsNotNull = {
            return [new Configuration(id: 1L, reportName: "report1"), new Configuration(id: 2L, reportName: "report2")]
        }
        when:
        def model = controller.listPvqCfg()

        then:
        response.status == 200
        model.observations.size() == 2
        model.observations[0].size() == 7
        model.observations[0].reportName == "report1"
        model.observations[1].size() == 7
        model.observations[1].reportName == "report2"

    }

    void "test create pvq - forbidden"() {
        given:
        def mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.fetchConfigurationMapFromSession(_, _) >> { [:] }
        controller.configurationService = mockConfigurationService

        User user = makeAdminUser()
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return user }
        controller.userService = mockUserService
        user.metaClass.isConfigurationTemplateCreator = { return true }
        SourceProfile.metaClass.static.sourceProfilesForUser = { User currentUser -> new SourceProfile() }

        when:
        params.pvqType = "pvq"
        def model = controller.create()

        then:
        response.status == 302
    }

    void "test create pvq - success"() {
        given:
        def mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.fetchConfigurationMapFromSession(_, _) >> { [:] }
        controller.configurationService = mockConfigurationService

        User user = makeAdminUser()
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return user }
        mockUserService.isAnyGranted(_) >> { true }
        controller.userService = mockUserService
        user.metaClass.isConfigurationTemplateCreator = { return true }
        SourceProfile.metaClass.static.sourceProfilesForUser = { User currentUser -> new SourceProfile() }
        when:
        params.pvqType = "pvq"
        controller.create()

        then:
        response.status == 200
    }

    void "test createFromTemplate"() {
        given:
        Configuration originalConfig = new Configuration(id: 1l)
        Configuration.metaClass.static.read = { Long id ->
            return originalConfig
        }
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(1..1) { ->
            User user = new User()
            user.metaClass.isAdmin = { ->
                return true
            }
            return user
        }
        controller.userService = userMock.proxyInstance()
        def configurationServiceMock = new MockFor(ConfigurationService)
        configurationServiceMock.demand.copyConfig(1..1) { cfg, usr, namePrefix, tenantId,isCreateFromTemplate  -> originalConfig }
        controller.configurationService = configurationServiceMock.proxyInstance()
        Configuration.metaClass.hasErrors = { -> return false }
        when:
        Tenants.withId(1){
            controller.createFromTemplate(1L)
        }
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/edit?fromTemplate=true"
    }

    void "Disabled Configuration should not run"() {
        given: " A disable Configuration Object"
        def normalUser = makeNormalUser()
        def adminUser = makeAdminUser()
        controller.springSecurityService = makeSecurityService(normalUser)
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save()
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class, fieldGroup: fieldGroup, sourceId: 1])
        field.save(vailidate: false)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(vailidate: false, flush: true)

        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList, owner: normalUser)
        template.save(failOnError: true)
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)
        DeliveryOption deliveryOpt = new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF])
        Configuration config = new Configuration([template: template, query: query, reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: normalUser, deliveryOption: deliveryOpt, isEnabled: false, createdBy: normalUser.username, modifiedBy: normalUser.username])
        config.save(validate:false)
        when: " We call method from controller and pass disable config object"
//        controller.executeConfiguration(config) // This method is removed.
        then: "the result should redirect to index page "
//        response.redirectedUrl == '/configuration/index'
    }
    //TODO: Some of the below test needs to be modified since the functionality are changed and method are rearranged Commented for now.
//    void "Run configuration doesn't render Run page if Configuration is Disabled"() {
//        given: " A disabled Configuration Object"
//        def normalUser = makeNormalUser()
//        def adminUser = makeAdminUser()
//        controller.springSecurityService = makeSecurityService(normalUser)
//        controller.metaClass.setNextRunDateAndScheduleDateJSON = {Configuration configuration1 -> return [configuration1] }
//        controller.metaClass.CRUDService.save = {Configuration configuration1 -> return [configuration1] }
//        def fields = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class])
//        fields.save()
//        CaseLineListingTemplate template = new CaseLineListingTemplate(name: 'Test template', createdBy: adminUser, selectedFieldsColumns:fields)
//        template.save(failOnError: true)
//        Query query = new Query([queryLevel: QueryLevel.CASE, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', createdBy: adminUser, JSONQuery: JSONQuery])
//        query.save(failOnError: true)
//        DeliveryOption deliveryOpt = new DeliveryOption()
//
//        def userServiceMock = new MockFor(UserService)
//        userServiceMock.demand.getUser() {
//            return normalUser
//        }
//        controller.userService = userServiceMock.proxyDelegateInstance()
//        Configuration configuration = new Configuration([template: template ,query:query , reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: normalUser,deliveryOption: deliveryOpt,isEnabled: false, createdBy: normalUser.username, modifiedBy: normalUser.username])
//
//        configuration.save()
//        params.id = 1
//        when:" We call method from controller and pass disable config object"
//        controller.run()
//        then: "the result should redirect to index page "
//        response.redirectedUrl == '/report/index'
//    }

    void "test index"(){
        when:
        controller.index()

        then:
        response.status == 200
    }

    void "test view"(){
        given:
        ReportConfiguration.metaClass.static.read = {Long id -> configuration}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> ifAnyGranted}
        def mockConfigurationService = Mock(ConfigurationService)
        def configurationMap = ["testKey1": "testMap1", "testKey2": "testMap2"] as JSON
        mockConfigurationService.getConfigurationAsJSON(_) >> {return configurationMap}
        controller.configurationService = mockConfigurationService
        def mockReportExecutorService = Mock(ReportExecutorService)
        def sqlList = [["testKey1": "testMap1", "testKey2": "testMap2"], ["testKey3": "testMap3", "testKey4": "testMap4"]]
        mockReportExecutorService.debugReportSQL(_) >> {return sqlList}
        controller.reportExecutorService = mockReportExecutorService

        when:
        params.viewConfigJSON = viewConfigJSON
        params.viewSql = "true"
        controller.view(1L)

        then:
        response.status == statusVal
        response.redirectedUrl == urlVal

        where:
        configuration                                                                                   | viewConfigJSON | ifAnyGranted | statusVal | urlVal
        new Configuration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | true           | true         | 200       | null
        new Configuration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | true           | false        | 200       | null
        new Configuration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | false          | true         | 200       | null
        new Configuration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | false          | false        | 200       | null
        null                                                                                            | true           | true         | 302       | '/configuration/index'
    }

    void "test viewExecutedConfig"(){
        given:
        ExecutedReportConfiguration.metaClass.static.read = { Long id -> executedReportConfiguration}
        ExecutedReportConfiguration.metaClass.isViewableBy = { ->
            return true
        }
        def mockUserService = Mock(UserService)
        mockUserService.getCurrentUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        ExecutionStatus.metaClass.static.getExecutionStatusByExectutedEntity = { Long i, String s ->
            [new ExecutionStatus(entityId: 2L)]
        }
        ReportConfiguration.metaClass.static.get = {Long id -> new Configuration(id: 3L)}
        ReportConfiguration.metaClass.static.findByReportNameAndOwner = {String s, User u ->
            new Configuration(id: 4L)
        }

        when:
        Long id = 1L
        controller.viewExecutedConfig(id)

        then:
        response.status == statusVal
        response.redirectedUrl == urlVal

        where:
        executedReportConfiguration                                                                                                                           | statusVal | urlVal
        new ExecutedIcsrProfileConfiguration()                                                                                                                | 302       | "/icsrProfileConfiguration/viewExecutedConfig/1"
        new ExecutedIcsrReportConfiguration()                                                                                                                 | 302       | "/icsrReport/viewExecutedConfig/1"
        new ExecutedConfiguration(executedTemplateQueries: [new ExecutedTemplateQuery()], executedDeliveryOption: new ExecutedDeliveryOption())               | 200       | null
        new ExecutedPeriodicReportConfiguration(executedTemplateQueries: [new ExecutedTemplateQuery()], executedDeliveryOption: new ExecutedDeliveryOption()) | 200       | null
        null                                                                                                                                                  | 302       | '/configuration/index'
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
        config                    | urlVal
        new Configuration(id: 1L) | '/configuration/view'
        null                      | '/configuration/index'
    }

    void "test delete -- success"(){
        given:
        Configuration configurationInstance = new Configuration(id: 1L)
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        Configuration.metaClass.isEditableBy = {User currentUser -> true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_) >> {theInstance,name, String justification -> true}
        controller.CRUDService = mockCRUDService

        when:
        params.deleteJustification = 'Test Delete'
        request.method = 'DELETE'
        controller.delete(configurationInstance)

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/index'
    }

    void "test delete -- failure"(){
        given:
        Configuration configurationInstance = new Configuration(id: 1L)
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        Configuration.metaClass.isEditableBy = {User currentUser -> isEditableBy}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_) >> {theInstance,name, String justification -> throw new ValidationException("Validation Exception", new ValidationErrors(configurationInstance))}
        controller.CRUDService = mockCRUDService

        when:
        params.deleteJustification = 'Test Delete'
        request.method = 'DELETE'
        controller.delete(configurationInstance)

        then:
        response.status == 302
        response.redirectedUrl == urlVal

        where:
        isEditableBy | urlVal
        true         | '/configuration/view'
        false        | '/configuration/index'
    }

    void "if one of the emailToUsers or shared with is not null, then validate should be true"() {
        given: "params for Delivery Options"
        def testConf = new Configuration()
        def deliveryOptionInst = new DeliveryOption(
                emailToUsers: ["abc@test.com"],
                sharedWith: null,
                attachmentFormats: [ReportFormatEnum.PDF],
                report: testConf
        )
        when: "we call check For delivery option Selected"
        then: "the result should return false"
        deliveryOptionInst.validate(['attachmentFormats'])
    }


    void "if none of the emailToUsers or shared with is not null, then validate should be false"() {
        given: "params for Delivery Options"
        def testConf = new Configuration()
        def deliveryOptionInst = new DeliveryOption(
                emailToUsers: null,
                sharedWith: null,
                attachmentFormats: [ReportFormatEnum.PDF],
                report: testConf
        )
        when: "we call check For delivery option Selected"
        then: "the result should return false"
        !deliveryOptionInst.validate(['attachmentFormats'])
    }

//    void "check For the error page redirect to runNow in case of runNow and id both in params"() {
//        given: " A Configuration Object with validation error"
//        def normalUser = makeNormalUser()
//        def adminUser = makeAdminUser()
//        def thirdUser = makeThirdUser()
//        def fields = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class])
//        fields.save()
//        CaseLineListingTemplate template = new CaseLineListingTemplate(name: 'Test template', createdBy: adminUser, selectedFieldsColumns:fields)
//        template.save(failOnError: true)
//        Query query = new Query([queryLevel: QueryLevel.CASE, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', createdBy: adminUser, JSONQuery: JSONQuery])
//        query.save(failOnError: true)
//        params.runNow = true
//        DeliveryOption deliveryOpt = new DeliveryOption()
//        Configuration configuration = new Configuration([template: template ,query:query , reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: normalUser,deliveryOption: deliveryOpt,isEnabled: false, createdBy: normalUser.username, modifiedBy: normalUser.username])
//
//        configuration.save()
//        params.id = 1
//        and:"normal user is logged in"
//        controller.springSecurityService = makeSecurityService(normalUser)
//        and: " mocked two private methods used by the function"
//        def userServiceMock = new MockFor(UserService)
//        userServiceMock.demand.getUser() {
//            return normalUser
//        }
//        controller.userService = userServiceMock.proxyDelegateInstance()
//        ConfigurationController.metaClass.getAllEmailsUnique = {Configuration config -> return ["admin@rxlogix.com"]}
//        ConfigurationController.metaClass.getAllUsers = { -> return [adminUser,thirdUser] }
//        ConfigurationController.metaClass.getTimezone = { -> return ["America/Los_Angeles"] }
//        ConfigurationController.metaClass.setNextRunDateAndScheduleDateJSON = {Configuration configuration1  -> return [configuration] }
//        when: "validation error method is called"
//        controller.update()
//        then: "the result should redirect to run page"
//        view == '/configuration/edit'
//    }

//    void "check For the error page redirect to edit in case if params has ID "() {
//        given: " A Configuration Object with validation error"
//        def normalUser = makeNormalUser()
//        def adminUser = makeAdminUser()
//        def thirdUser = makeThirdUser()
//        def fields = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class])
//        fields.save()
//        CaseLineListingTemplate template = new CaseLineListingTemplate(name: 'Test template', createdBy: adminUser, selectedFieldsColumns:fields)
//        template.save(failOnError: true)
//        Query query = new Query([queryLevel: QueryLevel.CASE, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', createdBy: adminUser, JSONQuery: JSONQuery])
//        query.save(failOnError: true)
//        DeliveryOption deliveryOpt = new DeliveryOption()
//        Configuration configuration = new Configuration([template: template ,query:query , reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: normalUser,deliveryOption: deliveryOpt,isEnabled: false, createdBy: normalUser.username, modifiedBy: normalUser.username])
//        configuration.save()
//        params.id = 1
//        and:"normal user is logged in"
//        controller.springSecurityService = makeSecurityService(normalUser)
//        controller.metaClass.setNextRunDateAndScheduleDateJSON = {Configuration configuration1  -> return [configuration] }
//        and: " mocked two private methods used by the function"
//        def userServiceMock = new MockFor(UserService)
//        userServiceMock.demand.getUser() {
//            return normalUser
//        }
//        controller.userService = userServiceMock.proxyDelegateInstance()
//        ConfigurationController.metaClass.getAllEmailsUnique = {Configuration config -> return ["admin@rxlogix.com"]}
//        ConfigurationController.metaClass.getAllUsers = { -> return [adminUser,thirdUser] }
//        ConfigurationController.metaClass.getTimezone = { -> return ["America/Los_Angeles"] }
//        when: "validation error method is called"
//        controller.update()
//        then: "the result should redirect to edit page"
//        view == '/configuration/edit'
//    }

//    void "In case of no params id and run now error page should redirect to create page "() {
//        given: " A Configuration Object with validation error"
//        def normalUser = makeNormalUser()
//        def adminUser = makeAdminUser()
//        def thirdUser = makeThirdUser()
//        def fields = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class])
//        fields.save()
//        CaseLineListingTemplate template = new CaseLineListingTemplate(name: 'Test template', createdBy: adminUser, selectedFieldsColumns:fields)
//        template.save(failOnError: true)
//        Query query = new Query([queryLevel: QueryLevel.CASE, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', createdBy: adminUser, JSONQuery: JSONQuery])
//        query.save(failOnError: true)
//        DeliveryOption deliveryOpt = new DeliveryOption()
//        Configuration configuration = new Configuration([template: template ,query:query , reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: normalUser,deliveryOption: deliveryOpt,isEnabled: false, createdBy: normalUser.username, modifiedBy: normalUser.username])
//        and:"normal user is logged in"
//        controller.springSecurityService = makeSecurityService(normalUser)
//        and: " mocked two private methods used by the function"
//        def userServiceMock = new MockFor(UserService)
//        userServiceMock.demand.getUser() {
//            return normalUser
//        }
//        controller.userService = userServiceMock.proxyDelegateInstance()
//        ConfigurationController.metaClass.getAllEmailsUnique = {Configuration config -> return ["admin@rxlogix.com"]}
//        ConfigurationController.metaClass.getAllUsers = { -> return [adminUser,thirdUser] }
//        ConfigurationController.metaClass.getTimezone = { -> return ["America/Los_Angeles"] }
//        when: "validation error method is called"
//        CRUDService.save(configuration)
//        then: "the result should redirect to create page"
//        view == '/configuration/create'
//    }

    void "Date Range type Cumulative"() {
        given: "A Configuration Object with relativeAbsoluteCumulativeDateRange value as  cumulative"
        def adminUser = makeAdminUser()
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save()
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class, fieldGroup: fieldGroup, sourceId: 1])
        field.save(vailidate: false)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true, flush: true)

        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList, owner: adminUser)
        template.save(failOnError: true)
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)
        DeliveryOption deliveryOpt = new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF])
        TemplateQuery tq = new TemplateQuery(dateRangeInformationForTemplateQuery: new DateRangeInformation(relativeAbsoluteCumulativeDateRange: DateRangeValueEnum.CUMULATIVE,dateRangeEnum: DateRangeEnum.CUMULATIVE), createdBy: adminUser.username, modifiedBy: adminUser.username)
        def configuration = new Configuration([reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: adminUser, deliveryOption: deliveryOpt, isEnabled: false, createdBy: adminUser.username, modifiedBy: adminUser.username])
        configuration.addToTemplateQueries(tq)
        configuration.save(validate: false)
        and: "cumulative/ default option is selected"
        controller.springSecurityService = makeSecurityService(adminUser)
        when: "getReportStartAndEndDate() is called"
        def result = tq.dateRangeInformationForTemplateQuery.getReportStartAndEndDate()
        then: "the result should be "
        assert ((new Date() - result[1]) / 1000 < 1)
    }

    void "Date Range type Custom"() {
        given: " A Configuration Object with relativeAbsoluteCumulativeDateRange value as  absolute"
        def adminUser = makeAdminUser()
        def normalUser = makeNormalUser()
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save()
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class, fieldGroup: fieldGroup, sourceId: 1])
        field.save(vailidate: false)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true, flush: true)

        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, owner: normalUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList)
        template.save(failOnError: true)
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)
        DeliveryOption deliveryOpt = new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF])
        TemplateQuery tq = new TemplateQuery(dateRangeInformationForTemplateQuery:
                new DateRangeInformation(dateRangeEnum: DateRangeEnum.CUSTOM, dateRangeStartAbsolute: new Date("Mon Jan 01 00:00:00 $TEST_TZ 0001"),
                        dateRangeEndAbsolute: new Date("Sun Jan 14 00:00:00 $TEST_TZ 0001")), createdBy: normalUser.username, modifiedBy: normalUser.username)
        Configuration configuration = new Configuration([template   : template, query: query, reportName: 'SAE - Clinical Reconciliation Death Case',
                                                         description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: normalUser, deliveryOption: deliveryOpt, isEnabled: false,
                                                         createdBy  : normalUser.username, modifiedBy: normalUser.username
        ])
        configuration.addToTemplateQueries(tq)
        configuration.save(validate: false)
        and: "from and to date is picked"
        controller.springSecurityService = makeSecurityService(normalUser)
        when: "getReportStartAndEndDate() is called"
        def result = tq.dateRangeInformationForTemplateQuery.getReportStartAndEndDate()
        then: "the result should be "
        result == [new Date("Mon Jan 01 00:00:00 $TEST_TZ 0001"), new Date("Sun Jan 14 00:00:00 $TEST_TZ 0001")]
    }

    void "Check for as of version date"() {
        given: " A Configuration Object with evaluateDateAs value as  as Of Version"
        def adminUser = makeAdminUser()
        def normalUser = makeNormalUser()
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save()
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class, fieldGroup: fieldGroup, sourceId: 1])
        field.save(vailidate: false)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true, flush: true)

        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, owner: normalUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList)
        template.save(failOnError: true)
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)
        DeliveryOption deliveryOpt = new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF])
        Configuration configuration = new Configuration([template: template, query: query, reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: normalUser, deliveryOption: deliveryOpt, isEnabled: false, relativeAbsoluteCumulativeDateRange: DateRangeValueEnum.CUMULATIVE, asOfVersionDate: new Date("Mon Jan 01 00:00:00 $TEST_TZ 0001"), evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF, createdBy: normalUser.username, modifiedBy: normalUser.username])
        configuration.save(validate: false)
        and: "a date is picked from date picker"
        controller.springSecurityService = makeSecurityService(normalUser)
        when: "getAsOfVersionDateCustom() is called"
        def result = configuration.getAsOfVersionDateCustom()
        then: "the result should be "
        result == new Date("Mon Jan 01 00:00:00 $TEST_TZ 0001")
    }

    void "Check for as of date as latest version in Configuration"() {
        given: " A Configuration Object with evaluateDateAs value as  as Of Version"
        def adminUser = makeAdminUser()
        def normalUser = makeNormalUser()
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save()
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class, fieldGroup: fieldGroup, sourceId: 1])
        field.save(vailidate: false)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true, flush: true)

        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, owner: normalUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList)
        template.save(failOnError: true)
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)
        DeliveryOption deliveryOpt = new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF])
        Configuration configuration = new Configuration([template: template, query: query, reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: normalUser, deliveryOption: deliveryOpt, isEnabled: false, relativeAbsoluteCumulativeDateRange: DateRangeValueEnum.CUMULATIVE, evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION, createdBy: normalUser.username, modifiedBy: normalUser.username])
        configuration.save(validate:false)
        and: "case evaluate on selects Latest Version"
        controller.springSecurityService = makeSecurityService(normalUser)
        when: "getAsOfVersionDateCustom() is called"
        def result = configuration.getAsOfVersionDateCustom()
        then: "the result should be "
        assert ((new Date() - result) / 1000 < 1)
    }

    void "Check for as of date as of end of reporting period in Configuration"() {
        given: " A Configuration Object with evaluateDateAs value as  as Of Version"
        def adminUser = makeAdminUser()
        def normalUser = makeNormalUser()
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save()
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class, fieldGroup: fieldGroup, sourceId: 1])
        field.save(vailidate: false)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true, flush: true)

        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, owner: normalUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList)
        template.save(failOnError: true)
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)
        DeliveryOption deliveryOpt = new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF])
        Configuration configuration = new Configuration([template: template, query: query, reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: normalUser, deliveryOption: deliveryOpt, isEnabled: false, relativeAbsoluteDateRange: DateRangeValueEnum.CUMULATIVE, evaluateDateAs: EvaluateCaseDateEnum.VERSION_PER_REPORTING_PERIOD, createdBy: normalUser.username, modifiedBy: normalUser.username])
        configuration.save(validate:false)
        and: "case evaluate on selects Latest Version"
        controller.springSecurityService = makeSecurityService(normalUser)
        when: "getAsOfVersionDateCustom() is called"
        def result = configuration.getAsOfVersionDateCustom()
        then: "the result should be "
        result == null
    }

    void "test exportToExcel"() {
        given:
        User normalUser_1 = makeNormalUser()
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user')
        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User()
        }
        Configuration.metaClass.static.ownedByUser = { User user ->
            new Object() {
                List list(Object o) {
                    [0]
                }
            }
        }
        Configuration.metaClass.static.fetchAllIdsForBulkUpdate = { LibraryFilter filter, List<Long> sharedIds ->
            new Object() {
                List list(Object o) {
                    [0]
                }
            }
        }
        def deliveryOption = new DeliveryOption(
                emailToUsers: ["pvreports@rxlogix.com"],
                sharedWith: [normalUser_1],
                sharedWithGroup: [userGroup_1,userGroup_2],
                attachmentFormats: [ReportFormatEnum.PDF]
        )
        Configuration.metaClass.static.getAll = { List<Long> idsForUser ->
            [
                    new Configuration(id: 1, reportName: "test", productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}', globalDateRangeInformation: new GlobalDateRangeInformation(), scheduleDateJSON: 'scheduleDateJSON', deliveryOption: deliveryOption,owner:normalUser_1),
                    new Configuration(id: 1, reportName: "test2", productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}', globalDateRangeInformation: new GlobalDateRangeInformation(), scheduleDateJSON: 'scheduleDateJSON', deliveryOption: deliveryOption,owner:normalUser_1)
            ]
        }
        Holders.config.pv.dictionary.group.enabled = true
        controller.qualityService = new QualityService()
        def resultData
        controller.qualityService.metaClass.exportToExcel = { data, metadata ->
            resultData = data
            new byte[0]
        }
        PVDictionaryConfig.setProductConfig(new DictionaryConfig(views: [[index: "1", code: 'Ingredient'], [index: "2", code: 'Family'], [index: "3", code: 'Product Generic Name'], [index: "4", code: 'Trade Name']]))
        when:
        controller.exportToExcel()
        then:
        resultData.size() == 2
        resultData[1][0] == "test2"
        resultData[1][1] == ""
        resultData[1][2] == "AZASPIRIUM CHLORIDE,ASPIRIN ALUMINIUM"
        resultData[1][3] == ""
        resultData[1][4] == ""
        resultData[1][5] == ""
        resultData[1][6] == ""
        resultData[1][7] == "scheduleDateJSON"
        resultData[1][8] == "user"
        resultData[1][9] == "group, group2"
        resultData[1][10] == "pvreports@rxlogix.com"
        resultData[1][11] == "PDF"
    }

    void "test editField"() {
        given:
        Configuration c = new Configuration(id: 1)
        Configuration.metaClass.static.get = { Serializable id -> c }
        c.metaClass.validate = { List field -> true }

        def result
        controller.CRUDService = Stub(CRUDService) {
            update(_) >> result >> c."$Value".toString()
        }

        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User()
        }
        controller.periodicReportService = Stub(PeriodicReportService) {
            parseScheduler(_, _) >> new String("")
        }

        when:
        params.id = 1
        params[paramName] = paramValue
        controller.editField()

        then:
        response.status == res

        where:
        Value                       | paramName          | paramValue                                                                                                                                                    | res
        "reportName"                | "reportName"       | "reportName"                                                                                                                                                  | 200
        "scheduleDateJSON"          | "scheduleDateJSON" | "{\"startDateTime\":\"2017-08-29T13:29Z\",\"timeZone\":{\"name\" :\"EST\",\"offset\" : \"-05:00\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1;COUNT=1;\"}" | 200
        "productSelection"          | "productSelection" | "{}"                                                                                                                                                          | 200
        "deliveryOption"            | "emailToUsers"     | ""                                                                                                                                                            | 200
        "deliveryOption"            | "SharedUsers"      | ""                                                                                                                                                            | 200
    }

    void "test ajaxCopy"() {
        given:
        ViewHelper.getMessage(_) >> { "test" }
        Configuration c = new Configuration(id: 1, reportName: "test",
                scheduleDateJSON: '{"startDateTime":"2017-08-29T13:29Z","timeZone":{"name" :"EST","offset" : "-05:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}'
        )
        Configuration.metaClass.static.get = { Serializable id -> c }

        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User()
        }

        controller.configurationService = Stub(ConfigurationService) {
            copyConfig(_, _) >> c
            toBulkTableMap(_) >> ['id': c.id, 'reportName': c.reportName]
        }

        when:
        params.id = 1
        controller.ajaxCopy()
        then:
        response.status == 200
        response.json.data.reportName == "test"

    }

    void "test ajaxRun"() {
        given:
        Configuration c = new Configuration(id: 1, scheduleDateJSON: "{\"startDateTime\":\"2017-08-29T13:29Z\",\"timeZone\":{\"name\" :\"EST\",\"offset\" : \"-05:00\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1;COUNT=1;\"}")
        Configuration.metaClass.static.get = { Serializable id -> c }

        controller.CRUDService = Stub(CRUDService) {
            update(_) >> c
        }

        controller.configurationService = Stub(ConfigurationService) {
            getNextDate(_) >> new Date()
        }

        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User()
        }

        when:
        params.id = 1
        controller.ajaxRun()
        then:
        response.status == 200
        response.json.data.nextRunDate != null
    }

    void "test ajaxDelete"() {
        given:
        Configuration c = new Configuration(id: 1)
        c.metaClass.isEditableBy = { User currentUser -> true }

        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User()
        }

        Configuration.metaClass.static.get = { Serializable id -> c }

        controller.CRUDService = Stub(CRUDService) {
            softDelete(_, _, _) >> true
        }
        when:
        params.id = 1
        controller.ajaxDelete()
        then:
        response.status == 200
        response.json.message != null
    }

    private makeUserService() {
        def userMock = new MockFor(UserService)
        userMock.demand.isCurrentUserAdmin(1..2) { false }
        return userMock.proxyInstance()
    }

    void "test assignParameterValuesToGlobalQuery"(){
        int run = 0
        Configuration configuration = new Configuration(globalDateRangeInformation: new GlobalDateRangeInformation())
        configuration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.fixBindDateRange(0..1){ GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            run++
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            run++
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        controller.invokeMethod('assignParameterValuesToGlobalQuery', [configuration] as Object[])
        then:
        run == 2
    }

    void "test bindAsOfVersionDate"(){
        User normalUser = makeNormalUser("user",[])
        Configuration configuration = new Configuration(evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF)
        configuration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('bindAsOfVersionDate', [configuration, "20-Mar-2016 "] as Object[])
        then:
        configuration.includeLockedVersion
        configuration.asOfVersionDate != null
    }

    void "test bindAsOfVersionDate null"(){
        User normalUser = makeNormalUser("user",[])
        Configuration configuration = new Configuration(evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION)
        configuration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('bindAsOfVersionDate', [configuration, "20-Mar-2016 "] as Object[])
        then:
        !configuration.includeLockedVersion
        configuration.asOfVersionDate == null
    }

    void "test bindDmsConfiguration new Instance"(){
        boolean run = false
        Configuration icsrReportConfiguration = new Configuration()
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.save(0..1){ theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCrudService.proxyInstance()
        Map emailData = ['format': ReportFormatEnum.PDF]
        when:
        controller.invokeMethod('bindDmsConfiguration', [icsrReportConfiguration, emailData] as Object[])
        then:
        run == true
        icsrReportConfiguration.dmsConfiguration.format == ReportFormatEnum.PDF
    }

    void "test bindDmsConfiguration update"(){
        boolean run = false
        Configuration icsrReportConfiguration = new Configuration(dmsConfiguration: new DmsConfiguration())
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.update(0..1){ theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCrudService.proxyInstance()
        Map emailData = ['format': ReportFormatEnum.PDF]
        when:
        controller.invokeMethod('bindDmsConfiguration', [icsrReportConfiguration, emailData] as Object[])
        then:
        run == true
        icsrReportConfiguration.dmsConfiguration.format == ReportFormatEnum.PDF
    }

    void "test bindEmailConfiguration update"(){
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        emailConfiguration.save(failOnError:true,validate:false)
        Configuration icsrReportConfiguration = new Configuration(emailConfiguration: emailConfiguration)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        controller.invokeMethod('bindEmailConfiguration', [icsrReportConfiguration, [subject: "new_email", body: "new_body"]] as Object[])
        then:
        icsrReportConfiguration.emailConfiguration.subject == "new_email"
        icsrReportConfiguration.emailConfiguration.body == "new_body"
    }

    void "test bindEmailConfiguration save"(){
        Configuration icsrReportConfiguration = new Configuration()
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        controller.invokeMethod('bindEmailConfiguration', [icsrReportConfiguration, [subject: "new_email",body: "new_body"]] as Object[])
        then:
        icsrReportConfiguration.emailConfiguration.subject == "new_email"
        icsrReportConfiguration.emailConfiguration.body == "new_body"
    }

    void "test bindEmailConfiguration delete"(){
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        emailConfiguration.save(failOnError:true,validate:false)
        Configuration icsrReportConfiguration = new Configuration(emailConfiguration: emailConfiguration)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        controller.invokeMethod('bindEmailConfiguration', [icsrReportConfiguration, [:]] as Object[])
        then:
        icsrReportConfiguration.emailConfiguration==null
    }

    void "test setExecutedDateRangeInformation CUSTOM"(){
        User normalUser = makeNormalUser("normalUser",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM),executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params["executedDateRangeInformationForTemplateQuery.relativeDateRangeValue"] = 2
        params["executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "30-Mar-2016"
        controller.invokeMethod('setExecutedDateRangeInformation', [executedTemplateQuery] as Object[])
        then:
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute != null
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute != null
    }

    void "test setExecutedDateRangeInformation for save on demand section with globaldaterange as custom"(){
        User normalUser = makeNormalUser("normalUser",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeEnum:DateRangeEnum.CUSTOM, dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.PR_DATE_RANGE),executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('setExecutedDateRangeInformation', [executedTemplateQuery] as Object[])
        then:
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute != null
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute != null
    }


    void "test setExecutedDateRangeInformation CUMULATIVE"(){
        User normalUser = makeNormalUser("normalUser",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUMULATIVE),executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params["executedDateRangeInformationForTemplateQuery.relativeDateRangeValue"] = 2
        controller.invokeMethod('setExecutedDateRangeInformation', [executedTemplateQuery] as Object[])
        then:
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute != null
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute != null
    }

    void "test assignParameterValuesToTemplateQuery"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        when:
        params["qev[0].key"] = "true"
        params["tv[0].key"] = "key"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["tv[0].value"] = "value"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].operator"] = "TOMORROW"
        params["qev[0].field"] = "report"
        params["template"] = new ReportTemplate()
        controller.invokeMethod('assignParameterValuesToTemplateQuery', [executedTemplateQuery] as Object[])
        then:
        executedTemplateQuery.executedQueryValueLists[0].parameterValues[0].reportField.name == "report"
        executedTemplateQuery.executedTemplateValueLists[0].parameterValues[0].key == "key"
        executedTemplateQuery.executedTemplateValueLists[0].parameterValues[0].value == "value"
    }

    void "test assignParameterValuesToTemplateQuery CustomSQLValue"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        when:
        params["qev[0].key"] = "true"
        params["tv[0].key"] = "key"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["tv[0].value"] = "value"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].operator"] = "TOMORROW"
        params["template"] = new ReportTemplate()
        controller.invokeMethod('assignParameterValuesToTemplateQuery', [executedTemplateQuery] as Object[])
        then:
        executedTemplateQuery.executedQueryValueLists[0].parameterValues[0].key == "true"
        executedTemplateQuery.executedQueryValueLists[0].parameterValues[0].value == "newValue"
        executedTemplateQuery.executedTemplateValueLists[0].parameterValues[0].key == "key"
        executedTemplateQuery.executedTemplateValueLists[0].parameterValues[0].value == "value"
    }

    void "test assignParameterValuesToTemplateQuery null"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        when:
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["tv[0].value"] = "value"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].operator"] = "TOMORROW"
        params["template"] = new ReportTemplate()
        controller.invokeMethod('assignParameterValuesToTemplateQuery', [executedTemplateQuery] as Object[])
        then:
        executedTemplateQuery.executedQueryValueLists.size() == 0
        executedTemplateQuery.executedTemplateValueLists.size() == 0
    }

    void "test assignParameterValuesToTemplateQuery TemplateQuery"(){
        Configuration icsrReportConfiguration = new Configuration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        TemplateQuery templateQuery = new TemplateQuery(queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])])
        templateQuery.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        when:
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        controller.invokeMethod('assignParameterValuesToTemplateQuery', [icsrReportConfiguration, templateQuery, 0] as Object[])
        then:
        templateQuery.queryValueLists[0].parameterValues[0].reportField.name == "report"
        templateQuery.templateValueLists[0].parameterValues[0].key == "key"
        templateQuery.templateValueLists[0].parameterValues[0].value == "value"
    }

    void "test assignParameterValuesToTemplateQuery TemplateQuery CustomSQLValue"(){
        Configuration icsrReportConfiguration = new Configuration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        TemplateQuery templateQuery = new TemplateQuery(queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])])
        templateQuery.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        when:
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQueries[0].template"] = new ReportTemplate()
        controller.assignParameterValuesToTemplateQuery(icsrReportConfiguration,templateQuery,0)
        then:
        templateQuery.queryValueLists[0].parameterValues[0].key == "true"
        templateQuery.templateValueLists[0].parameterValues[0].key == "key"
        templateQuery.templateValueLists[0].parameterValues[0].value == "value"
    }

    void "test setDateRangeInformation"(){
        User normalUser = makeNormalUser("user",[])
        DateRangeInformation dateRangeInformation = new DateRangeInformation()
        dateRangeInformation.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUSTOM"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        controller.invokeMethod('setDateRangeInformation', [0, dateRangeInformation, new Configuration()] as Object[])
        then:
        dateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM
        dateRangeInformation.dateRangeStartAbsolute != null
        dateRangeInformation.dateRangeEndAbsolute != null
    }

    void "test setDateRangeInformation null"(){
        DateRangeInformation dateRangeInformation = new DateRangeInformation()
        dateRangeInformation.save(failOnError:true,validate:false,flush:true)
        when:
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        controller.invokeMethod('setDateRangeInformation', [0,dateRangeInformation,new Configuration()] as Object[])
        then:
        dateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE
        dateRangeInformation.dateRangeStartAbsolute == null
        dateRangeInformation.dateRangeEndAbsolute == null
    }

    void "test bindExistingTemplateQueryEdits"(){
        User normalUser = makeNormalUser("user",[])
        Configuration icsrReportConfiguration = new Configuration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockUserService = Mock(UserService)
        mockUserService.getCurrentUser() >> {return normalUser}
        mockUserService.setOwnershipAndModifier(_) >> {Object object-> return templateQuery }
        controller.userService = mockUserService
        when:
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.reassessListednessDate = ["20-Mar-2016"]
        controller.invokeMethod('bindExistingTemplateQueryEdits', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUMULATIVE
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == null
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == null
        icsrReportConfiguration.templateQueries[0].queryValueLists[0].parameterValues[0].reportField.name == "report"
        icsrReportConfiguration.templateQueries[0].templateValueLists[0].parameterValues[0].key == "key"
        icsrReportConfiguration.templateQueries[0].templateValueLists[0].parameterValues[0].value == "value"
    }

    void "test bindTemplatePOIInputs"(){
        Configuration icsrReportConfiguration = new Configuration(poiInputsParameterValues: [])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        controller.invokeMethod('bindTemplatePOIInputs', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.poiInputsParameterValues.size() == 1
    }

    void "test bindNewTemplateQueries"(){
        User normalUser = makeNormalUser("user",[])
        Configuration icsrReportConfiguration = new Configuration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        def mockUserService = Mock(UserService)
        mockUserService.getCurrentUser() >> {return normalUser}
        mockUserService.setOwnershipAndModifier(_) >> {Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        controller.userService = mockUserService
        when:
        params["templateQueries[0].id"] = 1
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.relativeDateRangeValue"] = "20"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params["templateQueries[0].new"] = "true"
        params["templateQueries[0].dynamicFormEntryDeleted"] = "false"
        params.reassessListednessDate = ["20-Mar-2016"]
        controller.invokeMethod('bindNewTemplateQueries', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUMULATIVE
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == null
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == null
        icsrReportConfiguration.templateQueries[0].queryValueLists[0].parameterValues[0].reportField.name == "report"
        icsrReportConfiguration.templateQueries[0].templateValueLists[0].parameterValues[0].key == "key"
        icsrReportConfiguration.templateQueries[0].templateValueLists[0].parameterValues[0].value == "value"
    }

    void "test setAttributeTags"(){
        Configuration icsrReportConfiguration = new Configuration()
        icsrReportConfiguration.save(failOnError:true,validate:false)
        Tag tag = new Tag(name: "oldTag")
        tag.save(failOnError:true,validate:false,flush:true)
        when:
        params.tags = ["oldTag","newTag"]
        controller.invokeMethod('setAttributeTags', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.tags.size() == 2
    }

    void "test clearListFromConfiguration"(){
        Configuration icsrReportConfiguration = new Configuration(deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"])
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        controller.invokeMethod('clearListFromConfiguration', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.deliveryOption.emailToUsers.size() == 0
        icsrReportConfiguration.deliveryOption.attachmentFormats.size() == 0
        icsrReportConfiguration.poiInputsParameterValues.size() == 0
        icsrReportConfiguration.tags.size() == 0
    }

    void "test setNextRunDateAndScheduleDateJSON"(){
        Configuration icsrReportConfiguration = new Configuration(scheduleDateJSON: """{"startDateTime":"2021-02-18T01:00Z","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1"}
""",isEnabled: true)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.invokeMethod('setNextRunDateAndScheduleDateJSON', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.nextRunDate != null
    }


    void "test setNextRunDateAndScheduleDateJSON null"(){
        Configuration icsrReportConfiguration = new Configuration(scheduleDateJSON: "true",isEnabled: false)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.invokeMethod('setNextRunDateAndScheduleDateJSON', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.nextRunDate == null
    }

    void "test setNextRunDateAndScheduleDateJSON scheduleDateJSON"(){
        Configuration icsrReportConfiguration = new Configuration(scheduleDateJSON: "",isEnabled: false)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.invokeMethod('setNextRunDateAndScheduleDateJSON', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.nextRunDate == null
    }

    void "test setNextRunDateAndScheduleDateJSON MiscUtil"(){
        Configuration icsrReportConfiguration = new Configuration(scheduleDateJSON: "FREQ=WEEKLY",isEnabled: false)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.invokeMethod('setNextRunDateAndScheduleDateJSON', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.nextRunDate == null
    }

    void "test populateModel"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        Configuration icsrReportConfiguration = new Configuration(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),primaryReportingDestination: "primary",deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"],evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF,scheduleDateJSON: "true",isEnabled: true)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockUserService = Mock(UserService)
        mockUserService.getCurrentUser() >> {return normalUser}
        mockUserService.setOwnershipAndModifier(_) >> {Object object-> return templateQuery }
        mockUserService.setOwnershipAndModifier(_) >> {Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.getAllowedSharedWithUsersForCurrentUser(_) >> {String search = null-> [normalUser]}
        mockUserService.getAllowedSharedWithGroupsForCurrentUser(_) >> {String search = null-> [userGroup]}
        controller.userService = mockUserService
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1){ GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            run++
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            run++
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration configurationInstance-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.tags = ["oldTag","newTag"]
        params.reportingDestinations = "primary@!reporting@!destination"
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
        params.asOfVersionDate = "20-Mar-2016 "
        params.reassessListednessDate = ["20-Mar-2016"]
        controller.taskTemplateService = makeTaskTemplateService()
        controller.invokeMethod('populateModel', [icsrReportConfiguration] as Object[])
        then:
        run == 3
        icsrReportConfiguration.templateQueries.size() == 1
        icsrReportConfiguration.globalQueryValueLists.size() == 1
    }

    void "test listTemplates success"(){
        User normalUser = makeNormalUser("user",[])
        ReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration(description: "description",reportName: "report_1",dateCreated: new Date())
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ReportConfiguration.metaClass.static.fetchAllTemplatesForUser = {User user, Class clazz, String search = null -> new Object(){
                List list(Object o){
                    return [executedReportConfiguration]
                }
                int count(Object o) {
                    return 2
                }
            }
        }
        when:
        params.length = 5
        controller.listTemplates()
        then:
        response.json.size() == 3
    }

    void "test saveSection success"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(id: 4L, poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ReportTemplate reportTemplate = new ReportTemplate()
        reportTemplate.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration,executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM))
        executedTemplateQuery.executedTemplate = reportTemplate
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveExecutedTemplateQuery(0..1){ ExecutedTemplateQuery executedTemplateQueryInstance, ReportTemplate template, SuperQuery superQueryInstance, boolean isExecuteRptFromCount->
            run = true
        }
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User(preference:  new Preference(locale: new Locale("en")))
        }
        DateUtil.metaClass.static.getEndDate = { String dateToDatePicker, Locale locale -> new Date()}
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params["qev[0].key"] = "true"
        params["tv[0].key"] = "key"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["tv[0].value"] = "value"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].operator"] = "TOMORROW"
        params["qev[0].field"] = "report"
        params["template"] = new ReportTemplate()
        params["query.id"] = superQuery.id
        params["template.id"] = reportTemplate.id
        params["executedDateRangeInformationForTemplateQuery.relativeDateRangeValue"] = 2
        params["executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "30-Mar-2016"
        params.templtReassessDate = "20-Apr-2022"
        params.reassessListednessDate = "20-Apr-2022"
        controller.saveSection(executedTemplateQuery)
        then:
        run == true
        response.json.success == true
        response.json.alerts[0].message == "executedReportConfiguration.add.section.success"
    }

    void "test saveSection validation exception"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ReportTemplate reportTemplate = new ReportTemplate()
        reportTemplate.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration,executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM))
        executedTemplateQuery.executedTemplate = reportTemplate
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveExecutedTemplateQuery(0..1){ ExecutedTemplateQuery executedTemplateQueryInstance, ReportTemplate template, SuperQuery superQueryInstance, boolean isExecuteRptFromCount->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User(preference:  new Preference(locale: new Locale("en")))
        }
        DateUtil.metaClass.static.getEndDate = { String dateToDatePicker, Locale locale -> new Date()}
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params["qev[0].key"] = "true"
        params["tv[0].key"] = "key"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["tv[0].value"] = "value"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].operator"] = "TOMORROW"
        params["qev[0].field"] = "report"
        params["template"] = new ReportTemplate()
        params["query.id"] = superQuery.id
        params["template.id"] = reportTemplate.id
        params["executedDateRangeInformationForTemplateQuery.relativeDateRangeValue"] = 2
        params["executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "30-Mar-2016"
        params.templtReassessDate = "20-Apr-2022"
        params.reassessListednessDate = "20-Apr-2022"
        controller.saveSection(executedTemplateQuery)
        then:
        response.status == 500
        response.json == [error:true, message:"default.system.error.message", errors:[]]
    }

    void "test saveSection exception"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ReportTemplate reportTemplate = new ReportTemplate()
        reportTemplate.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration,executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM))
        executedTemplateQuery.executedTemplate = reportTemplate
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveExecutedTemplateQuery(0..1){ ExecutedTemplateQuery executedTemplateQueryInstance, ReportTemplate template, SuperQuery superQueryInstance, boolean isExecuteRptFromCount->
            throw new Exception("message")
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params["qev[0].key"] = "true"
        params["tv[0].key"] = "key"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["tv[0].value"] = "value"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].operator"] = "TOMORROW"
        params["qev[0].field"] = "report"
        params["template"] = new ReportTemplate()
        params["query.id"] = superQuery.id
        params["template.id"] = reportTemplate.id
        controller.saveSection(executedTemplateQuery)
        then:
        response.status == 500
        response.json == [message:"default.server.error.message"]
    }

    void "test editField reportName"(){
        boolean run = false
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        executedReportConfiguration.metaClass.validate = {java.util.List fields -> return true}
        executedReportConfiguration.metaClass.hasErrors = {-> return false}
        when:
        params.id = executedReportConfiguration.id
        params.reportName = "report_new"
        controller.editField()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
    }

//    validation not giving error
//    void "test editField reportName validation false"(){
//        User normalUser = makeNormalUser("user",[])
//        boolean run = false
//        Configuration periodicReportConfiguration = new Configuration(reportName: "report",owner: normalUser)
//        periodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        Configuration executedReportConfiguration = new Configuration(owner: normalUser)
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.update(0..1){theInstance ->
//            run = true
//            return theInstance
//        }
//        controller.CRUDService = mockCRUDService.proxyInstance()
//        when:
//        params.id = executedReportConfiguration.id
//        params.reportName = "report"
//        controller.editField()
//        then:
//        run == false
//        response.json.httpCode == 500
//        response.json.status == false
//        response.json == []
//    }

    void "test editField scheduleDateJSON"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockPeriodicReportService = new MockFor(PeriodicReportService)
        mockPeriodicReportService.demand.parseScheduler(0..1){String s, locale ->
            return "label"
        }
        controller.periodicReportService = mockPeriodicReportService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        params.scheduleDateJSON = "scheduleDateJSON"
        controller.editField()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
        response.json.data == [json:"scheduleDateJSON", label:"label"]
    }

    void "test editField success"(){
        boolean run = false
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.editField()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
    }

    void "test editField exception"(){
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.editField()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test editField no instance"(){
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.editField()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }


    void "test ajaxDelete no instance"(){
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.ajaxDelete()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

//    void "test ajaxDelete not editable"(){
//        User normalUser = makeNormalUser("user",[])
//        Configuration executedReportConfiguration = new Configuration(tenantId: 1)
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        def mockUserService = new MockFor(UserService)
//        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
//        controller.userService = mockUserService.proxyInstance()
//        Tenants.metaClass.static.currentId = { -> return 10}
//        when:
//        params.id = executedReportConfiguration.id
//        controller.ajaxDelete()
//        then:
//        response.json.httpCode == 500
//        response.json.status == false
//        response.json.message == "app.configuration.delete.permission"
//    }

    void "test ajaxDelete success"(){
        boolean run = false
        User adminUser = makeAdminUser()
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxDelete()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
        response.json.message == 'default.deleted.message'
    }

    void "test ajaxDelete validation exception"(){
        User adminUser = makeAdminUser()
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxDelete()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test ajaxDelete exception"(){
        User adminUser = makeAdminUser()
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxDelete()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }


    void "test ajaxRun no instance"(){
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.ajaxRun()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

//    void "test ajaxRun run exists"(){
//        Configuration executedReportConfiguration = new Configuration(isEnabled: true,nextRunDate: new Date())
//        executedReportConfiguration.nextRunDate = new Date()
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        when:
//        params.id = executedReportConfiguration.id
//        controller.ajaxRun()
//        then:
//        response.json.httpCode == 500
//        response.json.status == false
//        response.json.message == 'app.configuration.run.exists'
//    }

    void "test ajaxRun success"(){
        boolean run = false
        User adminUser = makeAdminUser()
        Configuration executedReportConfiguration = new Configuration(scheduleDateJSON: "true",isEnabled: false)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxRun()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
    }

    void "test ajaxRun validation exception"(){
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxRun()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test ajaxRun exception"(){
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxRun()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }


    void "test ajaxCopy no instance"(){
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.ajaxCopy()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }
//    toBulkTableMap parameter not matching arguments
//    void "test ajaxCopy success"(){
//        boolean run = false
//        User adminUser = makeAdminUser()
//        Configuration executedReportConfiguration = new Configuration()
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        def mockUserService = new MockFor(UserService)
//        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
//        controller.userService = mockUserService.proxyInstance()
//        def mockConfigurationService = new MockFor(ConfigurationService)
//        mockConfigurationService.demand.copyConfig(0..1){PeriodicReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false->
//            return new PeriodicReportConfiguration()
//        }
//        mockConfigurationService.demand.toBulkTableMap(0..1){Configuration conf->
//            run = true
//            return [:]
//        }
//        controller.configurationService = mockConfigurationService.proxyInstance()
//        when:
//        params.id = executedReportConfiguration.id
//        controller.ajaxCopy()
//        then:
//        run == true
//        response.json.httpCode == 200
//        response.json.status == true
//    }

    void "test ajaxCopy validation error"(){
        User adminUser = makeAdminUser()
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.copyConfig(0..1){PeriodicReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxCopy()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test ajaxCopy exception"(){
        User adminUser = makeAdminUser()
        Configuration executedReportConfiguration = new Configuration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.copyConfig(0..1){PeriodicReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false->
            throw new Exception()
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxCopy()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test addSection"(){
        User adminUser = makeAdminUser()
        ExecutedConfiguration executedReportConfiguration = new ExecutedConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        views['/configuration/includes/_addSectionForm.gsp'] = 'template content'
        when:
        controller.addSection(executedReportConfiguration.id)
        then:
        response.text == 'template content'
    }

    void "test addSection not editable"(){
        User normalUser = makeNormalUser("user_normal",[])
        ExecutedConfiguration executedReportConfiguration = new ExecutedConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        Tenants.metaClass.static.currentId = { -> return 10}
        views['/configuration/includes/_addSectionForm.gsp'] = 'template content'
        when:
        controller.addSection(executedReportConfiguration.id)
        then:
        response.text == 'No permission issue'
    }

    void "test addSection not found"(){
        when:
        controller.addSection(10)
        then:
        response.text == 'Not Found'
    }

    void "test ajaxSaveAsAndRun -- failed if reportName is null"() {
        given:
        ViewHelper.getMessage(_) >> { "test1" }
        Configuration c = new Configuration(
                scheduleDateJSON: '{"startDateTime":"2017-08-29T13:29Z","timeZone":{"name" :"EST","offset" : "-05:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}'
        )
        Configuration.metaClass.static.get = { Serializable id -> c }

        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User()
        }

        when:
        controller.ajaxSaveAsAndRun()
        then:
        response.status != 200
    }

    void "test ajaxSaveAsAndRun -- failed if reportName is not unique"() {
        given:
        ViewHelper.getMessage(_) >> { "test1" }
        Configuration c = new Configuration( reportName:'test2',
                scheduleDateJSON: '{"startDateTime":"2017-08-29T13:29Z","timeZone":{"name" :"EST","offset" : "-05:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}'
        )
        Configuration.metaClass.static.get = { Serializable id -> c }

        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User()
        }

        controller.configurationService = Stub(ConfigurationService) {
            isUniqueName(c.getReportName(),controller.userService.getCurrentUser()) >> false
        }

        when:
        controller.ajaxSaveAsAndRun()
        then:
        response.status != 200
    }

    void "test edit -- success"(){
        given:
        User user = makeAdminUser()
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return user}
        controller.userService = mockUserService
        user.metaClass.isConfigurationTemplateCreator = {return true}
        Configuration configuration = new Configuration(id: 1, reportName: "test", productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}', globalDateRangeInformation: new GlobalDateRangeInformation(), scheduleDateJSON: 'scheduleDateJSON', isEnabled: true)
        Configuration.metaClass.static.read = {Long id -> configuration}
        Configuration.metaClass.running = { -> false}
        Configuration.metaClass.isEditableBy = {User currentUser -> true}
        def mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.fetchConfigurationMapFromSession(_,_) >> {[configurationParams: [id: '2'], templateQueryIndex: [index: 1L, type: "template"]]}
        controller.configurationService = mockConfigurationService
        Tenants.metaClass.static.currentId = { -> return 1L}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithoutAuditLog(_) >> {}
        controller.CRUDService = mockCRUDService
        SourceProfile.metaClass.static.sourceProfilesForUser = {User currentUser -> new SourceProfile()}

        when:
        controller.edit(1L)

        then:
        response.status == 200
    }

    void "test edit -- failure"(){
        given:
        User user = makeAdminUser()
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return user}
        controller.userService = mockUserService
        user.metaClass.isConfigurationTemplateCreator = {return true}
        Configuration configuration = new Configuration(id: 1, reportName: "test", productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}', globalDateRangeInformation: new GlobalDateRangeInformation(), scheduleDateJSON: 'scheduleDateJSON', isEnabled: true)
        Configuration.metaClass.static.read = {Long id -> configuration}
        Configuration.metaClass.isRunning = { -> isRunning}
        Configuration.metaClass.isEditableBy = {User currentUser -> isEditableBy}
        def mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.fetchConfigurationMapFromSession(_,_) >> {[configurationParams: [id: '2'], templateQueryIndex: [index: 1L, type: "template"]]}
        controller.configurationService = mockConfigurationService
        Tenants.metaClass.static.currentId = { -> return 1L}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithoutAuditLog(_) >> {throw new ValidationException("Validation Exception", new ValidationErrors(configuration))}
        controller.CRUDService = mockCRUDService
        SourceProfile.metaClass.static.sourceProfilesForUser = {User currentUser -> new SourceProfile()}

        when:
        controller.edit(1L)

        then:
        response.status == statusVal

        where:
        isRunning | isEditableBy | statusVal
        true      | true         | 302
        false     | false        | 302
        false     | true         | 200
    }

    void "test getAllEmailsUnique"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getAllEmails(_) >> []
        controller.userService = mockUserService

        when:
        controller.getAllEmailsUnique(new Configuration())

        then:
        response.status == 200
    }

    void "test create"(){
        given:
        def mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.fetchConfigurationMapFromSession(_,_) >> {[templateQueryIndex: [index: 1L, type: "template"]]}
        controller.configurationService = mockConfigurationService
        DataTabulationTemplate reportTemplate = new DataTabulationTemplate(name: 'testTemplate', description: 'test Description', isDeleted: false, templateType: TemplateTypeEnum.DATA_TAB, hasBlanks: true)
        ReportTemplate.metaClass.static.get = {String templateName -> reportTemplate}
        DataTabulationTemplate.metaClass.static.isGranularity = {return true}
        User user = makeAdminUser()
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return user}
        controller.userService = mockUserService
        user.metaClass.isConfigurationTemplateCreator = {return true}
        SourceProfile.metaClass.static.sourceProfilesForUser = {User currentUser -> new SourceProfile()}
        SuperQuery query = new SuperQuery(name: 'testQuery', description: 'test description', hasBlanks: true)
        SuperQuery.metaClass.static.get = {String queryName -> query}
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: 'testCaseSeries', description: 'test description', isDeleted: false, isEnabled: true)
        ExecutedCaseSeries.metaClass.static.get = {String caseSeriesName -> executedCaseSeries}

        when:
        params.selectedTemplate = 'testTemplate'
        params.selectedQuery = 'testQuery'
        params.selectedCaseSeries = 'testCaseSeries'
        controller.create()

        then:
        response.status == 200
    }

    void "test save - not saved"(){
        when:
        request.method = 'GET'
        controller.save()

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/index'
    }

    void "test save - invalid tenant"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> return false}
        Tenants.metaClass.static.currentId = { return 1L}

        when:
        request.method = 'POST'
        params.tenantId = '10'
        controller.save()

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/create'
    }

    void "test save -- success"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        mockUserService.setOwnershipAndModifier(_) >> {User user-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        Tenants.metaClass.static.currentId = { return 1L}
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1){ GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            true
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            true
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration configuration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ ReportConfiguration configuration, List<String> sharedWith, List<String> sharedBy, boolean b ->
            new Configuration()
        }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ReportConfiguration configurationInstance ->
            true
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        Tag tag = new Tag(name: "oldTag")
        //tag.save(failOnError:true,validate:false,flush:true)
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        //emailConfiguration.save(failOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {return new Configuration(id: 3L)}
        controller.CRUDService = mockCRUDService
        controller.taskTemplateService = makeTaskTemplateService()

        when:
        request.method = 'POST'
        params.tenantId = '1'
        params.tags = ["oldTag","newTag"]
        params.reportName = 'testReport'
        params.asOfVersionDate = '20-Mar-2016'
        controller.save()

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/view'
    }

    void "test save -- failure"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        mockUserService.setOwnershipAndModifier(_) >> {User user-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        Tenants.metaClass.static.currentId = { return 1L}
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1){ GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            true
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            true
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ ReportConfiguration configuration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ ReportConfiguration configuration, List<String> sharedWith, List<String> sharedBy, boolean b ->
            new Configuration()
        }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ReportConfiguration configurationInstance ->
            true
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        Tag tag = new Tag(name: "oldTag")
        //tag.save(failOnError:true,validate:false,flush:true)
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        //emailConfiguration.save(failOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw error}
        controller.CRUDService = mockCRUDService
        SourceProfile.metaClass.static.sourceProfilesForUser = {User currentUser -> new SourceProfile()}
        controller.taskTemplateService = makeTaskTemplateService()

        when:
        request.method = 'POST'
        params.tenantId = '1'
        params.tags = ["oldTag","newTag"]
        params.reportName = 'testReport'
        params.asOfVersionDate = '20-Mar-2016'
        controller.save()

        then:
        response.status == statusVal
        response.redirectedUrl == urlVal

        where:
        error                                                                                      | statusVal | urlVal
        new ValidationException("Validation Exception", new ValidationErrors(new Configuration())) | 200       | null
        new Exception()                                                                            | 302       | '/configuration/create'
    }

    void "test create template"(){
        when:
        params.templateType = TemplateTypeEnum.DATA_TAB
        request.method = method
        controller.createTemplate()

        then:
        response.status == 302
        response.redirectedUrl == urlVal

        where:
        method | urlVal
        'GET'  | '/configuration/index'
        'POST' | '/template/create?templateType=Data+Tabulation'
    }

    void "test create query"(){
        when:
        request.method = method
        controller.createQuery()

        then:
        response.status == 302
        response.redirectedUrl == urlVal

        where:
        method | urlVal
        'GET'  | '/configuration/index'
        'POST' | '/query/create'
    }

    void "test Update -- GET"(){
        when:
        request.method = 'GET'
        controller.update()

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/index'
    }

    void "test Update -- not Found"(){
        given:
        if(configuration)
            configuration.setVersion(2L)
        Configuration.metaClass.static.lock = {Serializable serializable -> return configuration}

        when:
        request.method = 'POST'
        params.version = '1'
        controller.update()

        then:
        response.status == statusVal
        response.redirectedUrl == urlVal

        where:
        configuration             | statusVal | urlVal
        null                      | 302       | '/configuration/index'
        new Configuration(id: 1L) | 302       | '/configuration/edit'
    }

    void "test update -- invalid tenant"(){
        given:
        Configuration.metaClass.static.lock = {Serializable serializable -> return new Configuration(id: 1L)}
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
        response.redirectedUrl == '/configuration/edit'
    }

    void "test update -- success"(){
        given:
        Configuration.metaClass.static.lock = {Serializable serializable -> return new Configuration(id: 1L)}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        mockUserService.isAnyGranted(_) >> {false}
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        Tenants.metaClass.static.currentId = { return 1L}
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1){ GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            true
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            true
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ ReportConfiguration configuration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ ReportConfiguration configuration, List<String> sharedWith, List<String> sharedBy, boolean b ->
            new Configuration()
        }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ReportConfiguration configurationInstance ->
            true
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        Tag tag = new Tag(name: "oldTag")
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {return new Configuration(id: 1L)}
        controller.CRUDService = mockCRUDService
        controller.taskTemplateService = makeTaskTemplateService()

        when:
        request.method = 'POST'
        params.tenantId = '1'
        params.reportId = reportId
        controller.update()

        then:
        response.status == 302
        response.redirectedUrl == urlVal

        where:
        reportId | urlVal
        1L       | '/report/showFirstSection/1'
        null     | '/configuration/view'
    }

    void "test update -- failure"(){
        given:
        Configuration.metaClass.static.lock = {Serializable serializable -> return new Configuration(id: 1L)}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        mockUserService.isAnyGranted(_) >> {false}
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        Tenants.metaClass.static.currentId = { return 1L}
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1){ GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            true
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            true
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ ReportConfiguration configuration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ ReportConfiguration configuration, List<String> sharedWith, List<String> sharedBy, boolean b ->
            new Configuration()
        }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ReportConfiguration configurationInstance ->
            true
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        Tag tag = new Tag(name: "oldTag")
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> {throw exception}
        controller.CRUDService = mockCRUDService
        controller.taskTemplateService = makeTaskTemplateService()

        when:
        request.method = 'POST'
        params.tenantId = '1'
        params.reportId = 1L
        controller.update()

        then:
        response.status == statusVal
        response.redirectedUrl == urlVal

        where:
        exception                                                                                  | statusVal | urlVal
        new ValidationException("Validation Exception", new ValidationErrors(new Configuration())) | 200       | null
        new Exception()                                                                            | 302       | '/configuration/edit'
    }

    void "test bindReportTasks"() {
        ReportTask reportTask = new ReportTask(description: "oldTask")
        reportTask.save(failOnError: true, validate: false)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError: true, validate: false)
        Configuration reportConfiguration = new Configuration(reportTasks: [reportTask] as Set)
        reportConfiguration.save(failOnError: true, validate: false)
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1) { params -> [reportTaskInstance] }
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        when:
        controller.invokeMethod('bindReportTasks', [reportConfiguration, [:]] as Object[])
        then:
        reportConfiguration.reportTasks.size() == 1
        reportConfiguration.reportTasks[0].description == "newTask"
    }

    void "test save -- success with deleted and non-valid checkbox combination"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        mockUserService.setOwnershipAndModifier(_) >> {User user-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        Tenants.metaClass.static.currentId = { return 1L}
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..100){ GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            true
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            true
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration configuration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ ReportConfiguration configuration, List<String> sharedWith, List<String> sharedBy, boolean b ->
            new Configuration()
        }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ReportConfiguration configurationInstance ->
            true
        }


        controller.configurationService = mockConfigurationService.proxyInstance()
        Tag tag = new Tag(name: "oldTag")
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {return new Configuration(id: 3L)}
        controller.CRUDService = mockCRUDService
        controller.taskTemplateService = makeTaskTemplateService()

        when:
        request.method = 'POST'
        params.tenantId = '1'
        params.tags = ["oldTag","newTag"]
        params.reportName = 'testReport'
        params.asOfVersionDate = '20-Mar-2016'
        params.excludeNonValidCases = 'on'
        params.excludeDeletedCases = 'on'
        controller.save()

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/view'

    }

    void "test edit -- success for deleted checkbox"(){
        given:
        User user = makeAdminUser()
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return user}
        controller.userService = mockUserService
        user.metaClass.isConfigurationTemplateCreator = {return true}
        Configuration configuration = new Configuration(id: 1, reportName: "test",excludeDeletedCases: true,excludeNonValidCases: true ,productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}', globalDateRangeInformation: new GlobalDateRangeInformation(), scheduleDateJSON: 'scheduleDateJSON', isEnabled: true)
        Configuration.metaClass.static.read = {Long id -> configuration}
        Configuration.metaClass.running = { -> false}
        Configuration.metaClass.isEditableBy = {User currentUser -> true}
        def mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.fetchConfigurationMapFromSession(_,_) >> {[configurationParams: [id: '2'], templateQueryIndex: [index: 1L, type: "template"]]}
        controller.configurationService = mockConfigurationService
        Tenants.metaClass.static.currentId = { -> return 1L}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithoutAuditLog(_) >> {}
        controller.CRUDService = mockCRUDService
        SourceProfile.metaClass.static.sourceProfilesForUser = {User currentUser -> new SourceProfile()}

        when:
        controller.edit(1L)

        then:
        response.status == 200
    }
}
