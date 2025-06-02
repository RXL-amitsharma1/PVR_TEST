package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.enums.*
import com.rxlogix.publisher.PublisherSourceService
import com.rxlogix.pvdictionary.config.DictionaryConfig
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.user.*
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.mock.interceptor.MockFor
import org.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([AuditLogConfigUtil, User, PeriodicReportConfiguration, SuperQuery, Tenants, ReportTemplate, SourceProfile, ExecutionStatus, Query, ExecutedPublisherSource, ApplicationSettings, ReportResult, SpringSecurityUtils])
class PeriodicReportControllerSpec extends Specification implements DataTest, ControllerUnitTest<PeriodicReportController> {

    def setup() {
    }

    def cleanup() {
    }
    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en") ,createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", email: "user@rxlogix.com", preference: preferenceNormal,  createdBy: "user", modifiedBy: "user")
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    def setupSpec() {
        mockDomains User, UserGroup, Role, UserRole, UserGroupUser, Tenant, Preference, ReportField, ApplicationSettings, SuperQuery, ReportRequest, Query, ParameterValue, PeriodicReportConfiguration, QueryValueList, CaseSeriesDateRangeInformation, Tag, DeliveryOption, EmailConfiguration, ExecutionStatus, DmsConfiguration, ReportResult, ExecutedPeriodicReportConfiguration, ExecutedTemplateQuery, ExecutedReportConfiguration, CustomSQLValue, ExecutedQueryValueList, ExecutedTemplateValueList, TemplateQuery, TemplateValueList, DateRangeInformation, IcsrTemplateQuery, ReportTask, PeriodicReportConfiguration, ReportTemplate, ExecutedIcsrReportConfiguration, WorkflowState, ExecutedPublisherSource, PublisherSource, DataTabulationTemplate, PublisherConfigurationSection
        AuditLogConfigUtil.metaClass.static.logChanges = { domain, Map newMap, Map oldMap, String eventName, String extraValue -> }
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
        adminUser.metaClass.static.isDev = { -> return true}
        return adminUser
    }

    void "test createTemplate"() {
        when:
        controller.userService = makeUserService()
        controller.taskTemplateService = makeTaskTemplateService()
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params.globalDateRangeInformation = [:]
        params.emailConfiguration = [:]
        params.dmsConfiguration = [:]
        controller.createTemplate()
        then:
        response.status == 302
        response.redirectedUrl.startsWith("/template/create")
        session.editingConfiguration.configurationParams != null
        session.editingConfiguration.action == "create"
        session.editingConfiguration.controller == "periodicReport"
    }

    void "test listTemplates"() {
        given:
        ReportConfiguration.metaClass.static.fetchAllTemplatesForUser = { User user, Class clazz, String search = null->
            new Object() {
                List list(Object o) {
                    [new PeriodicReportConfiguration(id: 1, reportName: "test1", description: "description1", dateCreated: new Date()),
                     new PeriodicReportConfiguration(id: 2, reportName: "test2", description: "description2", dateCreated: new Date())]
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

    void "test createFromTemplate"() {
        given:
        PeriodicReportConfiguration originalConfig = new PeriodicReportConfiguration(id: 1l)
        PeriodicReportConfiguration.metaClass.static.read = { Long id ->
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
        configurationServiceMock.demand.copyConfig(1..1) { cfg, usr,prefix, tenantId,isCreateFromTemplate -> originalConfig }
        controller.configurationService = configurationServiceMock.proxyInstance()
        PeriodicReportConfiguration.metaClass.hasErrors = { -> return false }
        when:
        Tenants.withId(1) {
            controller.createFromTemplate(1l)
        }
        then:
        response.status == 302
        response.redirectedUrl == "/periodicReport/edit?fromTemplate=true"
    }

    void "test createQuery"() {
        when:
        controller.taskTemplateService = makeTaskTemplateService()
        controller.userService = makeUserService()
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params.globalDateRangeInformation = [:]
        params.emailConfiguration = [:]
        params.dmsConfiguration = [:]
        controller.createQuery()
        then:
        response.status == 302
        response.redirectedUrl == "/query/create"
        session.editingConfiguration.configurationParams != null
        session.editingConfiguration.action == "create"
        session.editingConfiguration.controller == "periodicReport"

    }


    void "test ajaxDelete"() {
        given:
        PeriodicReportConfiguration p = new PeriodicReportConfiguration(id: 1)
        p.metaClass.isEditableBy = { User currentUser -> true }
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(1..1) { -> return new User() }
        controller.userService = userMock.proxyInstance()
        PeriodicReportConfiguration.metaClass.static.get = { Serializable id -> p }
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.softDelete(1) { o1, o2, o3 -> true }
        controller.CRUDService = crudServiceMock.proxyInstance()
        when:
        params.id = 1
        controller.ajaxDelete()
        then:
        response.status == 200
        response.json.message != null
    }

    void "test ajaxRun"() {
        given:
        PeriodicReportConfiguration p = new PeriodicReportConfiguration(id: 1, scheduleDateJSON: "{\"startDateTime\":\"2017-08-29T13:29Z\",\"timeZone\":{\"name\" :\"EST\",\"offset\" : \"-05:00\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1;COUNT=1;\"}")
        PeriodicReportConfiguration.metaClass.static.get = { Serializable id -> p }
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update(1) { o1 -> true }
        controller.CRUDService = crudServiceMock.proxyInstance()
        def configurationServiceMock = new MockFor(ConfigurationService)
        configurationServiceMock.demand.getNextDate(1) { ReportConfiguration config -> new Date() }
        controller.configurationService = configurationServiceMock.proxyInstance()
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(1..1) { -> return new User() }
        controller.userService = userMock.proxyInstance()
        when:
        params.id = 1
        controller.ajaxRun()
        then:
        response.status == 200
        response.json.data.nextRunDate != null
    }

    void "test editField"() {
        given:
        GlobalDateRangeInformation g = new GlobalDateRangeInformation()
        PeriodicReportConfiguration p = new PeriodicReportConfiguration(id: 1, globalDateRangeInformation: g)
        p.save(flush: true, validate: false)
        //had to add due to Spock no recognizing belongsTo property as property
        g.metaClass.setReportConfiguration = { PeriodicReportConfiguration periodicReportConfiguration ->

        }

        PeriodicReportConfiguration.metaClass.static.get = { Serializable id ->
            return p
        }

        def crudServiceMock = new MockFor(CRUDService)
        def result
        crudServiceMock.demand.update(1) { o1 ->
            if (paramName == "periodicReportType")
                result = p.periodicReportType.name()
            else if (paramName == "dateRangeEnum")
                result = p.globalDateRangeInformation.dateRangeEnum.name()
            else
                result = p."$paramName".toString()
        }

        controller.CRUDService = crudServiceMock.proxyInstance()
        def configurationServiceMock = new MockFor(ConfigurationService)
        configurationServiceMock.demand.fixBindDateRange(1) { GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->  }
        configurationServiceMock.demand.bindParameterValuesToGlobalQuery(1) { ReportConfiguration periodicReportConfiguration, def params ->  }
        configurationServiceMock.demand.getNextDate(1) { ReportConfiguration config -> new Date() }
        controller.configurationService = configurationServiceMock.proxyInstance()
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(1..1) { -> return new User() }
        controller.userService = userMock.proxyInstance()
        controller.periodicReportService = new PeriodicReportService()
        controller.periodicReportService.metaClass.parseScheduler = { String s, locale -> return "" }

        when:
        params.id = 1
        if (paramName == "dateRangeEnum") {
            params.globalDateRangeInformation = [:]
            params.globalDateRangeInformation.dateRangeEnum = paramValue
            params.globalDateRangeInformation.relativeDateRangeValue = "1"
            params.globalDateRangeInformation.dateRangeStartAbsolute = "10-Jan-2018"
            params.globalDateRangeInformation.dateRangeEndAbsolute = "10-Jan-2018"
        } else
            params[paramName] = paramValue
        controller.editField()

        then:
        response.status == res
        (res == 500) || (result == paramValue)
        where:
        paramName                     | paramValue                                                                                                                                                    | res
        "reportName"                  | "reportName"                                                                                                                                                  | 200
        "scheduleDateJSON"            | "{\"startDateTime\":\"2017-08-29T13:29Z\",\"timeZone\":{\"name\" :\"EST\",\"offset\" : \"-05:00\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1;COUNT=1;\"}" | 200
        "productSelection"            | "{}"                                                                                                                                                          | 200
        "periodicReportType"          | "PBRER"                                                                                                                                                       | 200
        "primaryReportingDestination" | "FDA"                                                                                                                                                         | 200
        "dateRangeEnum"               | DateRangeEnum.CUSTOM.name()                                                                                                                                   | 200
    }

    void "test ajaxCopy"() {
        given:
        ViewHelper.getMessage(_) >> { "test" }
        PeriodicReportConfiguration p = new PeriodicReportConfiguration(id: 1, reportName: "test", periodicReportType: PeriodicReportTypeEnum.JPSR,
                globalDateRangeInformation: new GlobalDateRangeInformation(),
                scheduleDateJSON: '{"startDateTime":"2017-08-29T13:29Z","timeZone":{"name" :"EST","offset" : "-05:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}'
        )
        PeriodicReportConfiguration.metaClass.static.get = { Serializable id -> p }
        def configurationServiceMock = new MockFor(ConfigurationService)
        configurationServiceMock.demand.copyConfig(1) { PeriodicReportConfiguration configuration, User user, String namePrefix = null -> p }
        controller.configurationService = configurationServiceMock.proxyInstance()
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(1..3) { -> return new User() }
        controller.userService = userMock.proxyInstance()
        controller.periodicReportService = new PeriodicReportService()
        controller.periodicReportService.userService = controller.userService
        controller.periodicReportService.metaClass.toBulkTableMap = { savedConfig -> return p }
        when:
        params.id = 1
        controller.ajaxCopy()
        then:
        response.status == 200
        response.json.data.reportName == "test"
    }

    void "test exportToExcel"() {
        given:
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(1..3) { -> return new User() }
        User normalUser_1 = makeNormalUser()
        controller.userService = userMock.proxyInstance()
        PeriodicReportConfiguration.metaClass.static.fetchAllIdsForBulkUpdate = { LibraryFilter filter ->
            new Object() {
                List list(Object o) {
                    [0]
                }
            }
        }
        Holders.config.pv.dictionary.group.enabled = true
        PeriodicReportConfiguration.metaClass.static.getAll = { List<Long> idsForUser ->
            [
                    new PeriodicReportConfiguration(id: 1, reportName: "test", periodicReportType: PeriodicReportTypeEnum.JPSR,owner: normalUser_1,
                            globalDateRangeInformation: new GlobalDateRangeInformation(), productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}',
                            scheduleDateJSON: 'scheduleDateJSON', primaryReportingDestination: "dest", dueInDays: 1
                    ),
                    new PeriodicReportConfiguration(id: 2, reportName: "test2", periodicReportType: PeriodicReportTypeEnum.JPSR,owner: normalUser_1,
                            globalDateRangeInformation: new GlobalDateRangeInformation(), productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}',
                            scheduleDateJSON: 'scheduleDateJSON', primaryReportingDestination: "dest", dueInDays: 1
                    )
            ]
        }
        PVDictionaryConfig.setProductConfig(new DictionaryConfig(views: [[index:"1", code:'Ingredient'], [index:"2", code:'Family'], [index:"3", code:'Product Generic Name'], [index:"4", code:'Trade Name']]))

        controller.qualityService = new QualityService()
        def resultData
        controller.qualityService.metaClass.exportToExcel = { data, metadata ->
            resultData = data
            new byte[0]
        }
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
        resultData[1][7] == "JPSR"
        resultData[1][8] == "CUMULATIVE"
        resultData[1][9] == 1
        resultData[1][10] == null
        resultData[1][11] == null
        resultData[1][12] == "dest"
        resultData[1][13] == 1
        resultData[1][14] == "scheduleDateJSON"
    }


    private makeUserService() {
        def userMock = new MockFor(UserService)
        userMock.demand.getAllowedSharedWithUsersForCurrentUser(0..1) { -> new ArrayList<User>() }
        userMock.demand.getAllowedSharedWithGroupsForCurrentUser(0..1) { -> new ArrayList<UserGroup>() }
        return userMock.proxyInstance()
    }

    private makeTaskTemplateService() {
        def userMock = new MockFor(TaskTemplateService)
        userMock.demand.fetchReportTasksFromRequest(0..1) { params -> [] }
        return userMock.proxyInstance()
    }

    void "test bindDmsConfiguration new Instance"(){
        boolean run = false
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration()
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
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(dmsConfiguration: new DmsConfiguration())
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
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(emailConfiguration: emailConfiguration)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        controller.bindEmailConfiguration(icsrReportConfiguration,[subject: "new_email",body: "new_body"])
        then:
        icsrReportConfiguration.emailConfiguration.subject == "new_email"
        icsrReportConfiguration.emailConfiguration.body == "new_body"
    }

    void "test bindEmailConfiguration save"(){
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration()
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        when:
        controller.bindEmailConfiguration(icsrReportConfiguration,[subject: "new_email",body: "new_body"])
        then:
        icsrReportConfiguration.emailConfiguration.subject == "new_email"
        icsrReportConfiguration.emailConfiguration.body == "new_body"
    }

    void "test bindEmailConfiguration delete"(){
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        emailConfiguration.save(failOnError:true,validate:false)
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(emailConfiguration: emailConfiguration)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        controller.bindEmailConfiguration(icsrReportConfiguration,[:])
        then:
        icsrReportConfiguration.emailConfiguration == null
    }

    void "test assignParameterValuesToGlobalQuery"(){
        int run = 0
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(globalDateRangeInformation: new GlobalDateRangeInformation())
        icsrReportConfiguration.save(failOnError:true,validate:false)
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
        controller.invokeMethod('assignParameterValuesToGlobalQuery', [icsrReportConfiguration] as Object[])
        then:
        run == 2
    }

    void "test setExecutedDateRangeInformation"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM),executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        when:
        controller.invokeMethod('setExecutedDateRangeInformation', [executedTemplateQuery] as Object[])
        then:
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute == executedReportConfiguration.executedGlobalDateRangeInformation.dateRangeEndAbsolute
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute == executedReportConfiguration.executedGlobalDateRangeInformation.dateRangeStartAbsolute
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
        controller.assignParameterValuesToTemplateQuery(executedTemplateQuery)
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
        controller.assignParameterValuesToTemplateQuery(executedTemplateQuery)
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
        controller.assignParameterValuesToTemplateQuery(executedTemplateQuery)
        then:
        executedTemplateQuery.executedQueryValueLists.size() == 0
        executedTemplateQuery.executedTemplateValueLists.size() == 0
    }

    void "test assignParameterValuesToTemplateQuery TemplateQuery"(){
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
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
        controller.assignParameterValuesToTemplateQuery(icsrReportConfiguration,templateQuery,0)
        then:
        templateQuery.queryValueLists[0].parameterValues[0].reportField.name == "report"
        templateQuery.templateValueLists[0].parameterValues[0].key == "key"
        templateQuery.templateValueLists[0].parameterValues[0].value == "value"
    }

    void "test assignParameterValuesToTemplateQuery TemplateQuery CustomSQLValue"(){
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
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
        controller.invokeMethod('setDateRangeInformation', [0, dateRangeInformation, new PeriodicReportConfiguration()] as Object[])
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
        controller.invokeMethod('setDateRangeInformation', [0, dateRangeInformation, new PeriodicReportConfiguration()] as Object[])
        then:
        dateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE
        dateRangeInformation.dateRangeStartAbsolute == null
        dateRangeInformation.dateRangeEndAbsolute == null
    }

    void "test bindExistingTemplateQueryEdits"(){
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
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
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        controller.userService = mockUserService.proxyInstance()
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
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(poiInputsParameterValues: [])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        controller.invokeMethod('bindTemplatePOIInputs', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.poiInputsParameterValues.size() == 1
    }

    void "test bindNewTemplateQueries"(){
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        controller.userService = mockUserService.proxyInstance()
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
        controller.invokeMethod('bindNewTemplateQueries', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUMULATIVE
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == null
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == null
        icsrReportConfiguration.templateQueries[0].queryValueLists[0].parameterValues[0].reportField.name == "report"
        icsrReportConfiguration.templateQueries[0].templateValueLists[0].parameterValues[0].key == "key"
        icsrReportConfiguration.templateQueries[0].templateValueLists[0].parameterValues[0].value == "value"
    }

    void "test bindAsOfVersionDate"(){
        User normalUser = makeNormalUser("user",[])
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('bindAsOfVersionDate', [icsrReportConfiguration, "20-Mar-2016 "] as Object[])
        then:
        icsrReportConfiguration.includeLockedVersion == true
        icsrReportConfiguration.asOfVersionDate != null
    }

    void "test bindAsOfVersionDate null"(){
        User normalUser = makeNormalUser("user",[])
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('bindAsOfVersionDate', [icsrReportConfiguration, "20-Mar-2016 "] as Object[])
        then:
        icsrReportConfiguration.includeLockedVersion == false
        icsrReportConfiguration.asOfVersionDate == null
    }

    void "test setAttributeTags"(){
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration()
        icsrReportConfiguration.save(failOnError:true,validate:false)
        Tag tag = new Tag(name: "oldTag")
        tag.save(failOnError:true,validate:false,flush:true)
        when:
        params.tags = ["oldTag","newTag"]
        controller.invokeMethod('setAttributeTags', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.tags.size() == 2
    }

    void "test bindReportTasks"(){
        ReportTask reportTask = new ReportTask(description: "oldTask")
        reportTask.save(failOnError:true,validate:false)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(reportTasks: [reportTask] as Set)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        when:
        controller.invokeMethod('bindReportTasks', [icsrReportConfiguration, [:]] as Object[])
        then:
        icsrReportConfiguration.reportTasks.size() == 1
        icsrReportConfiguration.reportTasks[0].description == "newTask"
    }

    void "test clearListFromConfiguration"(){
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"])
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        controller.invokeMethod('clearListFromConfiguration', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.deliveryOption.emailToUsers.size() == 0
        icsrReportConfiguration.deliveryOption.attachmentFormats.size() == 0
        icsrReportConfiguration.poiInputsParameterValues.size() == 0
        icsrReportConfiguration.tags.size() == 0
        icsrReportConfiguration.reportingDestinations.size() == 0
    }

    void "test setNextRunDateAndScheduleDateJSON"(){
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(scheduleDateJSON: """{"startDateTime":"2021-02-18T01:00Z","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1"}
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
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(scheduleDateJSON: "true",isEnabled: false)
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
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(scheduleDateJSON: "",isEnabled: false)
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
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(scheduleDateJSON: "FREQ=WEEKLY",isEnabled: false)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.invokeMethod('setNextRunDateAndScheduleDateJSON', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.nextRunDate == null
    }

    void "test setReportingDestinations"(){
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(primaryReportingDestination: "primary")
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        params.reportingDestinations = "primary@!reporting@!destination"
        controller.invokeMethod('setReportingDestinations', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.reportingDestinations.size() == 1
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
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),primaryReportingDestination: "primary",deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"],evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF,scheduleDateJSON: "true",isEnabled: true)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1){ GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            run++
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            run++
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration periodicReportConfiguration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
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
        params.deliveryOption = [oneDriveSiteId: 'oneDrive@rxlogix.com']
        controller.invokeMethod('populateModel', [icsrReportConfiguration] as Object[])
        then:
        run == 3
        icsrReportConfiguration.templateQueries.size() == 1
        icsrReportConfiguration.globalQueryValueLists.size() == 1
    }

    void "test createFromTemplate success"(){
        User adminUser = makeAdminUser()
        PeriodicReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(reportName: "icsr")
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.copyConfig(0..1){ PeriodicReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false-> return icsrReportConfiguration}
        controller.configurationService = mockConfigurationService.proxyInstance()
        Tenants.metaClass.static.currentId = { -> return 10}
        when:
        controller.createFromTemplate(icsrReportConfiguration.id)
        then:
        flash.message == "app.copy.success"
        response.redirectUrl == '/periodicReport/edit/1?fromTemplate=true'
    }


    void "test createFromTemplate not found"(){
        when:
        controller.createFromTemplate(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/periodicReport/index'
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

    void "test removeSection success"() {
        int run = 0
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError: true, validate: false, flush: true)

        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError: true, validate: false, flush: true)

        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]

        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1) { theInstance ->
            run++
        }
        controller.CRUDService = mockCRUDService.proxyInstance()

        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.deleteReportsCachedFilesIfAny(0..1) { ExecutedReportConfiguration conf, boolean isDraft ->
            run++
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        controller.removeSection(executedTemplateQuery.id)
        then:
        then:
        run == 2
        response.json.success == true
        response.json.alerts[0].message == "executedReportConfiguration.remove.section.success"
        executedReportConfiguration.executedTemplateQueries.isEmpty()
    }

//    ve.message and ex.message in Controller code
//    void "test removeSection validation exception"(){
//        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(createdBy: null)
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedConfiguration: executedReportConfiguration)
//        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
//        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance ->
//            if (!theInstance.validate()) {
//                throw new ValidationException("message",theInstance.errors)
//            }
//        }
//        controller.CRUDService = mockCRUDService.proxyInstance()
//        when:
//        controller.removeSection(executedTemplateQuery.id)
//        then:
//        response.status == 500
//        response.json == [message:"executedReportConfiguration.remove.section.error"]
//    }
//
//    void "test removeSection exception"(){
//        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(createdBy: null)
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedConfiguration: executedReportConfiguration)
//        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
//        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance ->
//                throw new Exception("message")
//        }
//        controller.CRUDService = mockCRUDService.proxyInstance()
//        when:
//        controller.removeSection(executedTemplateQuery.id)
//        then:
//        response.status == 500
//        response.json == [message:"default.server.error.message"]
//    }

    void "test removeSection not found"(){
        when:
        controller.removeSection(10)
        then:
        response.status == 500
        response.json == [error: "Not Found"]
    }

    void "test saveSection success"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(executedDeliveryOption: new ExecutedDeliveryOption(), tenantId: 1L, sourceProfile: new SourceProfile(id: 1L), reportName: "test", owner: normalUser, workflowState: new WorkflowState(name: "test", createdBy: "user", modifiedBy: "user"), modifiedBy: "test", createdBy: "test", periodicReportType: PeriodicReportTypeEnum.IND, poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10))
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue(key: "test")
        parameterValue.save(failOnError:true,validate:true,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue(key: "test")
        customSQLValue.save(failOnError:true,validate:true,flush:true)
        def mockuserService=Mock(UserService)
        mockuserService.isCurrentUserAdmin() >> { return true }
        SuperQuery superQuery = new SuperQuery(queryType: QueryTypeEnum.QUERY_BUILDER, owner: normalUser, modifiedBy: "user", name: "test", createdBy: "user")
        superQuery.userService=mockuserService
        superQuery.save(failOnError:true,validate:true,flush:true)
        ReportTemplate reportTemplate = new ReportTemplate(name: "test", owner: normalUser, templateType: TemplateTypeEnum.CASE_LINE, createdBy: "test", modifiedBy: "test")
        reportTemplate.save(failOnError:true,validate:true,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate, modifiedBy: "normalUser", createdBy: "user", executedQueryValueLists: [new ExecutedQueryValueList(query: superQuery, parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(template: reportTemplate, parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration,executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM))
        executedTemplateQuery.save(failOnError:true,validate:true,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(failOnError:true,validate:true,flush:true)
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveExecutedTemplateQuery(0..1){ ExecutedTemplateQuery executedTemplateQueryInstance, ReportTemplate template, SuperQuery superQueryInstance, boolean isExecuteRptFromCount ->
            run = true
        }
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
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
        run == true
        response.json.success == true
        response.json.alerts[0].message == "executedReportConfiguration.add.section.success"
        response.json.alerts[0].type.name == "SUCCESS"
    }

    void "test saveSection validation exception"(){
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
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate, executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration,executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM))
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveExecutedTemplateQuery(0..1){ ExecutedTemplateQuery executedTemplateQueryInstance, ReportTemplate template, SuperQuery superQueryInstance, boolean isExecuteRptFromCount->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        SuperQuery.metaClass.static.get = { Serializable serializable ->
            new Object() {
                Integer getParameterSize() {
                    return 1
                }
            }
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
        response.json == [error:true, message:"default.system.error.message", errors:[]]
    }

    void "test saveSection exception"(){
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
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate, executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration,executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM))
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveExecutedTemplateQuery(0..1){ ExecutedTemplateQuery executedTemplateQueryInstance, ReportTemplate template, SuperQuery superQueryInstance->
            throw new Exception("message")
        }
        SuperQuery.metaClass.static.get = { Serializable serializable ->
            new Object() {
                Integer getParameterSize() {
                    return 1
                }
            }
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

    void "test editField success globalDateRangeInformation"(){
        int run = 0
        ReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration(globalDateRangeInformation: new GlobalDateRangeInformation())
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.fixBindDateRange(0..1){ GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            run++
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            run++
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params.id = executedReportConfiguration.id
        controller.editField()
        then:
        run == 3
        response.json.httpCode == 200
        response.json.status == true
    }

//    validate giving error
//    void "test editField reportName"(){
//        boolean run = false
//        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.update(0..1){theInstance ->
//            run = true
//            return theInstance
//        }
//        controller.CRUDService = mockCRUDService.proxyInstance()
//        executedReportConfiguration.metaClass.validate = {java.util.List fields -> return true}
//        executedReportConfiguration.metaClass.hasErrors = {-> return false}
//        when:
//        params.id = executedReportConfiguration.id
//        params.reportName = "report_new"
//        controller.editField()
//        then:
//        run == true
//        response.json.httpCode == 200
//        response.json.status == true
//    }
//
//    void "test editField reportName validation false"(){
//        User normalUser = makeNormalUser("user",[])
//        boolean run = false
//        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName: "report",owner: normalUser)
//        periodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration(owner: normalUser)
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
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
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
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
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
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
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
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.editField()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

    void "test ajaxDelete no instance"(){
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.ajaxDelete()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

    void "test ajaxDelete not editable"(){
        User normalUser = makeNormalUser("user",[])
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration(tenantId: 1)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        UserGroup.metaClass.static.fetchAllUserGroupByUser={User user->[]}
        Tenants.metaClass.static.currentId = { -> return 10}
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxDelete()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "app.configuration.delete.permission"
    }

    void "test ajaxDelete success"(){
        boolean run = false
        User adminUser = makeAdminUser()
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
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
        PeriodicReportConfiguration.metaClass.static.isEditableBy = {User currentUser -> true}
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
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        PeriodicReportConfiguration.metaClass.static.isEditableBy = {User currentUser -> true}
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxDelete()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test ajaxDelete exception"(){
        User adminUser = makeAdminUser()
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        PeriodicReportConfiguration.metaClass.static.isEditableBy = {User currentUser -> true}
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxDelete()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test ajaxRun no instance"(){
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.ajaxRun()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

//    void "test ajaxRun run exists"(){
//        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration(isEnabled: true,nextRunDate: new Date())
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
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration(scheduleDateJSON: "true",isEnabled: false)
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
        PeriodicReportConfiguration.metaClass.static.isEditableBy = {User u -> true}
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxRun()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
    }

    void "test ajaxRun validation exception"(){
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
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
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
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
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.ajaxCopy()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

    void "test ajaxCopy success"(){
        boolean run = false
        User adminUser = makeAdminUser()
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.copyConfig(0..1){PeriodicReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false->
            return new PeriodicReportConfiguration()
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockPeriodicReportService = new MockFor(PeriodicReportService)
        mockPeriodicReportService.demand.toBulkTableMap(0..1){PeriodicReportConfiguration conf ->
            run = true
            return [:]
        }
        controller.periodicReportService = mockPeriodicReportService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxCopy()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
    }

    void "test ajaxCopy validation error"(){
        User adminUser = makeAdminUser()
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
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
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
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

    void "test importExcel"(){
        int run = 0
        def multipartFile = new GrailsMockMultipartFile('file', 'reportFile.pdf', '', new byte[0])
        request.addFile(multipartFile)
        def mockPeriodicReportService = new MockFor(PeriodicReportService)
        mockPeriodicReportService.demand.importFromExcel(0..1){ workbook->
            run++
            return  [errors:[],added:[],updated :[]]
        }
        mockPeriodicReportService.demand.getDisplayMessage(0..2){ String code, List reportNames->
            run++
            return  "message generated!"
        }
        controller.periodicReportService = mockPeriodicReportService.proxyInstance()
        when:
        controller.importExcel()
        then:
        run == 3
        response.redirectUrl == '/periodicReport/bulkUpdate'
        flash.message != null
    }

    void "test importExcel errors"(){
        int run = 0
        def multipartFile = new GrailsMockMultipartFile('file', 'reportFile.pdf', '', new byte[0])
        request.addFile(multipartFile)
        def mockPeriodicReportService = new MockFor(PeriodicReportService)
        mockPeriodicReportService.demand.importFromExcel(0..1){ workbook->
            run++
            return  [errors:["error"],added:[],updated :[]]
        }
        mockPeriodicReportService.demand.getDisplayMessage(0..2){ String code, List reportNames->
            run++
            return  "message generated!"
        }
        controller.periodicReportService = mockPeriodicReportService.proxyInstance()
        when:
        controller.importExcel()
        then:
        run == 3
        response.redirectUrl == '/periodicReport/bulkUpdate'
        flash.error != null
    }

    void "test addSection"(){
        ExecutedPeriodicReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        views['/periodicReport/includes/_addSectionForm.gsp'] = 'template content'
        when:
        controller.addSection(executedReportConfiguration.id)
        then:
        response.text == 'template content'
    }

    void "test addSection not found"(){
        when:
        controller.addSection(10)
        then:
        response.text == 'Not Found'
    }

    void "test runOnce success"(){
        boolean run = false
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration(nextRunDate: new Date(),isEnabled: false)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockPeriodicReportService = new MockFor(PeriodicReportService)
        mockPeriodicReportService.demand.scheduleToRunOnce(0..1){ PeriodicReportConfiguration periodicReportConfiguration->
            run = true
        }
        controller.periodicReportService = mockPeriodicReportService.proxyInstance()
        when:
        controller.runOnce(executedReportConfiguration)
        then:
        run == true
        flash.message == 'app.Configuration.RunningMessage'
        response.redirectUrl == '/executionStatus/list'
    }

    void "test runOnce not found"(){
        when:
        controller.runOnce(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/periodicReport/index'
    }

    void "test runOnce exists"(){
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration(nextRunDate: new Date(),isEnabled: true)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.runOnce(executedReportConfiguration)
        then:
        flash.warn == 'app.configuration.run.exists'
        response.redirectUrl == '/periodicReport/index'
    }

    void "test runOnce validation exception"(){
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration(nextRunDate: new Date(),isEnabled: false)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockPeriodicReportService = new MockFor(PeriodicReportService)
        mockPeriodicReportService.demand.scheduleToRunOnce(0..1){ PeriodicReportConfiguration periodicReportConfiguration->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.periodicReportService = mockPeriodicReportService.proxyInstance()
        when:
        controller.runOnce(executedReportConfiguration)
        then:
        view == '/periodicReport/create'
    }

    void "test run request method GET"(){
        when:
        request.method = 'GET'
        controller.run(10)
        then:
        flash.error == 'default.not.saved.message'
        response.redirectUrl == '/periodicReport/index'
    }

    def "test bulkUpdate"(){
        when:
        controller.bulkUpdate()

        then:
        response.status == 200
    }

    def "test targetStatesAndApplications"(){
        given:
        def mockPeriodicReportService = Mock( PeriodicReportService )
        mockPeriodicReportService.targetStatesAndApplications(_, _) >> {return [:]}
        controller.periodicReportService = mockPeriodicReportService

        when:
        params.initialState = 'initialState'
        params.executedReportConfiguration = 1
        controller.targetStatesAndApplications()

        then:
        response.status == 200
    }

    def "test updateReportState"(){
        given:
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(workflowState: new WorkflowState(name: "old_state"))
        executedPeriodicReportConfiguration.save(failOnError:true,validate:false)
        WorkflowState workflowState = new WorkflowState(name: "new_state")
        workflowState.save(failOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithoutAuditLog(_) >> {true}
        controller.CRUDService = mockCRUDService
        def mockUserService = Mock( UserService )
        controller.userService = mockUserService

        when:
        params.newState = "new_state"
        params.oldState = "old_state"
        controller.updateReportState(1L)

        then:
        response.status == 200
        response.json == [success:true, message:"app.periodicReportConfiguration.state.update.success"]
    }

    def "test updateReportState exception"(){
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration = new ExecutedPeriodicReportConfiguration(workflowState: new WorkflowState(name: "old_state"))
        executedPeriodicReportConfiguration.save(failOnError:true,validate:false)
        WorkflowState workflowState = new WorkflowState(name: "new_state")
        workflowState.save(failOnError:true,validate:false)
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithoutAuditLog(_) >> {throw new ValidationException("Validation Exception", executedPeriodicReportConfiguration.errors)}
        controller.CRUDService = mockCRUDService
        def mockUserService = Mock( UserService )
        controller.userService = mockUserService

        when:
        params.newState = "new_state"
        params.oldState = "old_state"
        controller.updateReportState(1L)

        then:
        response.status == 500
    }

    def "test create"(){
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
        SourceProfile.metaClass.static.sourceProfilesForUser = { User currentUser -> new SourceProfile()}
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

    def "test edit -- success"(){
        given:
        User user = makeAdminUser()
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return user}
        controller.userService = mockUserService
        user.metaClass.isConfigurationTemplateCreator = {return true}
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(id: 1, reportName: "test", productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}', globalDateRangeInformation: new GlobalDateRangeInformation(), scheduleDateJSON: 'scheduleDateJSON', isEnabled: true)
        PeriodicReportConfiguration.metaClass.static.read = {Long id -> periodicReportConfiguration}
        PeriodicReportConfiguration.metaClass.running = { -> false}
        PeriodicReportConfiguration.metaClass.isEditableBy = {User currentUser -> true}
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

    def "test view"(){
        given:
        PeriodicReportConfiguration.metaClass.static.read = {Long id -> configuration}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> ifAnyGranted}
        def mockConfigurationService = Mock(ConfigurationService)
        def configurationMap = ["testKey1": "testMap1", "testKey2": "testMap2"]
        mockConfigurationService.getConfigurationAsJSON(_) >> { return configurationMap as JSON }
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
        configuration                                                                                                 | viewConfigJSON | ifAnyGranted | statusVal | urlVal
        new PeriodicReportConfiguration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | true           | true         | 200       | null
        new PeriodicReportConfiguration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | true           | false        | 200       | null
        new PeriodicReportConfiguration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | false          | true         | 200       | null
        new PeriodicReportConfiguration(templateQueries: [new TemplateQuery()], deliveryOption: new DeliveryOption()) | false          | false        | 200       | null
        null                                                                                                          | true           | true         | 302       | '/periodicReport/index'
    }

    def "test ajaxPeriodicSaveAsAndRun -- failed if reportName is null"() {
        given:
        ViewHelper.getMessage(_) >> { "test1" }
        PeriodicReportConfiguration c = new PeriodicReportConfiguration(
                scheduleDateJSON: '{"startDateTime":"2017-08-29T13:29Z","timeZone":{"name" :"EST","offset" : "-05:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}'
        )
        PeriodicReportConfiguration.metaClass.static.get = { Serializable id -> c }

        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User()
        }

        when:
        controller.ajaxPeriodicSaveAsAndRun()
        then:
        response.status != 200
    }

    def "test ajaxPeriodicSaveAsAndRun -- failed if reportName is not unique"() {
        given:
        ViewHelper.getMessage(_) >> { "test1" }
        PeriodicReportConfiguration c = new PeriodicReportConfiguration( reportName:'test2',
                scheduleDateJSON: '{"startDateTime":"2017-08-29T13:29Z","timeZone":{"name" :"EST","offset" : "-05:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}'
        )
        PeriodicReportConfiguration.metaClass.static.get = { Serializable id -> c }

        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User()
        }

        controller.configurationService = Stub(ConfigurationService) {
            isUniqueName(c.getReportName(),controller.userService.getCurrentUser()) >> false
        }

        when:
        controller.ajaxPeriodicSaveAsAndRun()
        then:
        response.status != 200
    }

    def "test copy"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeNormalUser('user', [])}
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
        config                                  | urlVal
        new PeriodicReportConfiguration(id: 1L) | '/periodicReport/view'
        null                                    | '/periodicReport/index'
    }

    def "test delete --Success"(){
        given:
        PeriodicReportConfiguration periodicReportConfiguration=new PeriodicReportConfiguration()
        def user = makeNormalUser('user', [])
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return user}
        controller.userService = mockUserService
        PeriodicReportConfiguration.metaClass.isEditableBy = {User currentUser -> true}
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.softDelete(_, _, _) >> {true}
        controller.CRUDService = mockCRUDService

        when:
        params.deleteJustification = 'Test Delete'
        request.method = 'DELETE'
        controller.delete(periodicReportConfiguration)

        then:
        response.status == 302
        response.redirectedUrl == '/periodicReport/index'

    }

    def "test delete -- failure"(){
        given:
        PeriodicReportConfiguration configurationInstance = new PeriodicReportConfiguration(id: 1L)
        def mockUserService = Mock(UserService)
        mockUserService.getCurrentUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        PeriodicReportConfiguration.metaClass.isEditableBy = {User currentUser -> isEditableBy}
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
        true         | '/periodicReport/view'
        false        | '/periodicReport/index'
    }

    def "test save request method GET"(){
        when:
        request.method = 'GET'
        Tenants.withId(1) {
            controller.save()
        }
        then:
        flash.error == 'default.not.saved.message'
        response.redirectUrl == '/periodicReport/index'
    }

    def "test save - invalid tenant"(){
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
        response.redirectedUrl == '/periodicReport/create'
    }

    def "test save --try block"(){
        given:
        PeriodicReportConfiguration periodicReportConfigurationInstance = new PeriodicReportConfiguration(id: 1L, reportName: 'reportName')
        def mockConfigurationService = Mock( ConfigurationService )
        mockConfigurationService.getNextDate(_) >> { PeriodicReportConfiguration config-> return new Date()}
        mockConfigurationService.fixBindDateRange(_, _, _) >> { GlobalDateRangeInformation globalDateRangeInformation, PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)

        }
        mockConfigurationService.bindParameterValuesToGlobalQuery(_, _) >> { PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
        }
        mockConfigurationService.bindSharedWith(_, _, _, _) >> {PeriodicReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> new PeriodicReportConfiguration()}
        mockConfigurationService.checkProductCheckboxes(_) >> { PeriodicReportConfiguration configurationInstance-> false}
        controller.configurationService = mockConfigurationService
        def mockTaskTemplateService = Mock( TaskTemplateService )
        mockTaskTemplateService.fetchReportTasksFromRequest(_) >> {}
        controller.taskTemplateService = mockTaskTemplateService
        User normalUser = makeNormalUser("user",[])
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.save(_) >> {return periodicReportConfigurationInstance}
        controller.CRUDService = mockCRUDService
        ReportRequest reportRequest = new ReportRequest(reportName: "TestingReport")
        ReportRequest.metaClass.static.get = {Long requestId -> reportRequest}

        when:
        request.method = 'POST'
        params.deliveryOption = [oneDriveSiteId: 'oneDrive@rxlogix.com']
        params.requestId = 1L
        controller.save()
        then:
        response.status == 200
    }

    def "test save --ValidationException"(){
        given:
        PeriodicReportConfiguration periodicReportConfigurationInstance = new PeriodicReportConfiguration(id: 1L, reportName: 'reportName')
        def mockConfigurationService = Mock( ConfigurationService )
        mockConfigurationService.getNextDate(_) >> { PeriodicReportConfiguration config-> return new Date()}
        mockConfigurationService.fixBindDateRange(_, _, _) >> { GlobalDateRangeInformation globalDateRangeInformation, PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)

        }
        mockConfigurationService.bindParameterValuesToGlobalQuery(_, _) >> { PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
        }
        mockConfigurationService.bindSharedWith(_, _, _, _) >> {PeriodicReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> new PeriodicReportConfiguration()}
        mockConfigurationService.checkProductCheckboxes(_) >> { PeriodicReportConfiguration configurationInstance-> false}
        controller.configurationService = mockConfigurationService
        def mockUserService = new MockFor(UserService)
        User normalUser = makeNormalUser("user",[])
        mockUserService.demand.getCurrentUser(0..2){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockTaskTemplateService = Mock( TaskTemplateService )
        mockTaskTemplateService.fetchReportTasksFromRequest(_) >> {}
        controller.taskTemplateService = mockTaskTemplateService
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception", periodicReportConfigurationInstance.errors)}
        controller.CRUDService = mockCRUDService
        PublisherSource publisherSource = new PublisherSource()
        periodicReportConfigurationInstance.addToAttachments(publisherSource)
        PublisherConfigurationSection publisherConfigurationSection =new PublisherConfigurationSection()
        periodicReportConfigurationInstance.addToPublisherConfigurationSections(publisherConfigurationSection)
        when:
        request.method = 'POST'
        params.deliveryOption = [oneDriveSiteId: 'oneDrive@rxlogix.com']
        params.requestId = 1L
        controller.save()
        then:
        response.status == 200
    }


    def "test sources"(){
        given:
        ExecutedPublisherSource executedPublisherSource=new ExecutedPublisherSource()
        when:
        controller.sources()
        then:
        response.status == 200
    }

    def "test updatePeriodicAjaxCall request method GET"(){
        when:
        request.method = 'GET'
        controller.updatePeriodicAjaxCall()

        then:
        flash.error == 'default.not.saved.message'
        response.status == 302
        response.redirectUrl == '/periodicReport/index'
    }

    def "test updatePeriodicAjaxCall"(){
        given:
        PeriodicReportConfiguration.metaClass.static.lock = {Long id -> new PeriodicReportConfiguration()}
        def mockConfigurationService = Mock( ConfigurationService )
        mockConfigurationService.getNextDate(_) >> { PeriodicReportConfiguration config-> return new Date()}
        mockConfigurationService.fixBindDateRange(_, _, _) >> { GlobalDateRangeInformation globalDateRangeInformation, PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)

        }
        mockConfigurationService.bindParameterValuesToGlobalQuery(_, _) >> { PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
        }
        mockConfigurationService.bindSharedWith(_, _, _, _) >> {PeriodicReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> new PeriodicReportConfiguration()}
        mockConfigurationService.checkProductCheckboxes(_) >> { PeriodicReportConfiguration configurationInstance-> false}
        controller.configurationService = mockConfigurationService
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.update(_) >> {true}
        controller.CRUDService = mockCRUDService
        def mockUserService = Mock( UserService )
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> true}

        when:
        params.id = 1L
        controller.updatePeriodicAjaxCall()
        then:
        response.status == 302
    }

    def "test downloadExecutedAttachment"(){
        given:
        ExecutedPublisherSource.metaClass.static.get = {Long id -> new ExecutedPublisherSource()}

        when:
        params.id = 2L
        controller.downloadExecutedAttachment()

        then:
        response.status == 200
    }

    def "test downloadExecutedAttachment not found"(){
        given:
        ExecutedPublisherSource.metaClass.static.get = {Long id -> null}
        when:
        params.id = 2L
        controller.downloadExecutedAttachment()
        then:
        response.status == 302
        response.redirectedUrl == '/periodicReport/index'
    }

    def "test index"(){
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test disable --try block"() {
        given:
        User normalUser = makeNormalUser("user", [])
        PeriodicReportConfiguration periodicReportConfiguration =  new PeriodicReportConfiguration(id: 1L, reportName: "test", periodicReportType: PeriodicReportTypeEnum.JPSR,
                globalDateRangeInformation: new GlobalDateRangeInformation(), executing : false,
                scheduleDateJSON: '{"startDateTime":"2017-08-29T13:29Z","timeZone":{"name" :"EST","offset" : "-05:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}'
        )
        PeriodicReportConfiguration.metaClass.static.get = {Long id -> periodicReportConfiguration}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return normalUser }
        controller.userService = mockUserService
        periodicReportConfiguration.metaClass.isEditableBy = {User currentUser -> true}
        controller.CRUDService = [update:{d-> return periodicReportConfiguration}]
        when:
        params.id = 1L
        controller.disable()
        then:
        response.status == 302
    }

    void "test disable --validation exception"() {
        given:
        User normalUser = makeNormalUser("user", [])
        PeriodicReportConfiguration periodicReportConfigurationInstance =  new PeriodicReportConfiguration(id: 1L, reportName: "test", periodicReportType: PeriodicReportTypeEnum.JPSR,
                globalDateRangeInformation: new GlobalDateRangeInformation(), executing : false,
                scheduleDateJSON: '{"startDateTime":"2017-08-29T13:29Z","timeZone":{"name" :"EST","offset" : "-05:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}'
        )
        PeriodicReportConfiguration.metaClass.static.get = {Long id -> periodicReportConfigurationInstance}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return normalUser }
        controller.userService = mockUserService
        periodicReportConfigurationInstance.metaClass.isEditableBy = {User currentUser -> true}
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.update(_) >> { throw new ValidationException("Validation Exception", periodicReportConfigurationInstance.errors) }
        controller.CRUDService = mockCRUDService
        PeriodicReportConfiguration.metaClass.static.read = {return periodicReportConfigurationInstance}
        def mockConfigurationService = Mock( ConfigurationService )
        mockConfigurationService.getNextDate(_) >> { PeriodicReportConfiguration config-> return new Date()}
        mockConfigurationService.fixBindDateRange(_, _, _) >> { GlobalDateRangeInformation globalDateRangeInformation, PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)

        }
        mockConfigurationService.bindParameterValuesToGlobalQuery(_, _) >> { PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
        }
        mockConfigurationService.bindSharedWith(_, _, _, _) >> {PeriodicReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> new PeriodicReportConfiguration()}
        mockConfigurationService.checkProductCheckboxes(_) >> { PeriodicReportConfiguration configurationInstance-> false}
        controller.configurationService = mockConfigurationService
        def mockTaskTemplateService = Mock( TaskTemplateService )
        mockTaskTemplateService.fetchReportTasksFromRequest(_) >> {}
        controller.taskTemplateService = mockTaskTemplateService
        when:
        params.id = 1L
        params.deliveryOption = [oneDriveSiteId: 'oneDrive@rxlogix.com']
        controller.disable()
        then:
        response.status == 200
    }

    void "test disable not found"(){
        given:
        PeriodicReportConfiguration.metaClass.static.get = {Long id -> null}
        when:
        params.id = 1L
        controller.disable()
        then:
        flash.error == 'default.not.found.message'
        response.status == 302
        response.redirectUrl == '/periodicReport/index'
    }

    def "test viewExecutedConfig when instance does not exist"(){
        given:
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(isDeleted : true)
        when:
        controller.viewExecutedConfig(executedReportConfiguration)
        then:
        response.status == 302
        response.redirectedUrl == '/periodicReport/index'
    }

    def "test viewExecutedConfig when instance exists"(){
        given:
        ExecutedPeriodicReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(executedTemplateQueries: [new ExecutedTemplateQuery()], executedDeliveryOption: new ExecutedDeliveryOption())
        ExecutionStatus.metaClass.static.getExecutionStatusByExectutedEntity = { Long i, String s ->
            [new ExecutionStatus(entityId: 2L)]
        }
        when:
        controller.viewExecutedConfig(executedReportConfiguration)
        then:
        response.status == 200
    }

    def "test queryDataTemplate"(){
        given:
        Query query = new Query(id : 1L, queryExpressionValues: [new QueryExpressionValue(value: "value",key: "key",reportField: new ReportField(name: "report_field"),operator: QueryOperatorEnum.LAST_WEEK)])
        Query.metaClass.static.get = {Long id -> query}
        when:
        params.queryId = 1L
        def result = controller.queryDataTemplate()
        then:
        result.size() == 1
    }

    def "test reports"(){
        when:
        controller.reports()
        then:
        response.status == 200
    }

    def "test updatePublisherAttachment when executedConfigId exists and try block executes"(){
        given:
        ExecutedPeriodicReportConfiguration.metaClass.static.lock = {Long executedConfigId -> new ExecutedPeriodicReportConfiguration(id: 1L)}
        def mockCRUDService = Mock( CRUDService )
        CRUDService.update(_) >> {true}
        controller.CRUDService = mockCRUDService
        when:
        params.executedConfigId = 1L
        controller.updatePublisherAttachment()
        then:
        response.status == 302
        response.redirectedUrl == '/pvp/sections?attachmentesUpdated=true'
    }

    def "test updatePublisherAttachment when executedConfigId exists and Validation Exception occurs"(){
        given:
        ExecutedPeriodicReportConfiguration executedPeriodicReportConfiguration= new ExecutedPeriodicReportConfiguration(id: 1L)
        ExecutedPeriodicReportConfiguration.metaClass.static.lock = {Long executedConfigId -> executedPeriodicReportConfiguration}
        def mockCRUDService = Mock( CRUDService )
        CRUDService.update(_) >> {throw new ValidationException("Validation Exception", executedPeriodicReportConfiguration.errors)}
        controller.CRUDService = mockCRUDService
        when:
        params.executedConfigId = 1L
        controller.updatePublisherAttachment()
        then:
        response.status == 302
        response.redirectedUrl == '/pvp/sections?attachmentesUpdated=true'
    }

    def "test downloadAttachment --Try Block"(){
        given:
        ExecutedPublisherSource executedPublisherSource = new ExecutedPublisherSource(id: 1L, name: 'executedPublisherSource')
        ExecutedPublisherSource.metaClass.static.get = {Long id -> executedPublisherSource}
        def mockPublisherSourceService = Mock( PublisherSourceService )
        mockPublisherSourceService.getDataMap(_) >> {return [data: ['data1','data2'], contntType: "contntType", name: "name"]}
        controller.publisherSourceService = mockPublisherSourceService
        when:
        params.executedAttachment = true
        params.id = 1L
        controller.downloadAttachment()
        then:
        response.status == 200
    }

    def "test downloadAttachment --Exception"(){
        given:
        ExecutedPublisherSource executedPublisherSource = new ExecutedPublisherSource(id: 1L, name: 'executedPublisherSource')
        ExecutedPublisherSource.metaClass.static.get = {Long id -> executedPublisherSource}
        def mockPublisherSourceService = Mock( PublisherSourceService )
        mockPublisherSourceService.getDataMap(_) >> {throw new Exception()}
        controller.publisherSourceService = mockPublisherSourceService
        when:
        params.executedAttachment = true
        params.id = 1L
        controller.downloadAttachment()
        then:
        response.status == 200
    }

    def "test downloadAttachment when instance does not found"(){
        given:
        ExecutedPublisherSource.metaClass.static.get = {Long id -> null}
        when:
        controller.downloadAttachment()
        then:
        response.status == 302
        response.redirectedUrl == '/periodicReport/index'
    }

    def "test getDmsFolders"(){
        given:
        ApplicationSettings applicationSettings=new ApplicationSettings(dateCreated: new Date(), dmsIntegration: 'old_Value')
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        ExecutedReportConfiguration.metaClass.static.read = {Long id -> executedReportConfiguration}
        def mockdmsService = Mock( DmsService )
        mockdmsService.getFolderList(_, _) >> {['folder1']}
        controller.dmsService = mockdmsService
        ApplicationSettings.metaClass.static.first = {-> applicationSettings}
        when:
        params.id = 1L
        controller.getDmsFolders()
        then:
        response.status == 200
    }

    def "test saveOnDemandSection --Success"(){
        given:
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
        ReportResult reportResult=new ReportResult()
        ExecutedDateRangeInformation executedDateRangeInformationForTemplateQuery = new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(), dateRangeEnum: DateRangeEnum.CUSTOM, dateRangeStartAbsolute: new Date(), dateRangeEndAbsolute: new Date())
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQuery: new SuperQuery(name: 'executedTemplateQuery.executedQuery') ,manuallyAdded : false, onDemandSectionParams: 'onDemandSectionParams', executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],
                executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration, executedDateRangeInformationForTemplateQuery: executedDateRangeInformationForTemplateQuery)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        ReportTemplate reportTemplate = new ReportTemplate(name: 'reportTemplate')
        reportTemplate.save(failOnError:true,validate:false,flush:true)
        ReportResult.metaClass.static.read = {Long id -> reportResult}
        ExecutedReportConfiguration.addToExecutedQueryValueLists(_) >> {true}
        def mockReportExecutorService = Mock( ReportExecutorService )
        mockReportExecutorService.saveExecutedTemplateQuery(_, _, _, _) >> {true}
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.executeRptFromCount = 1L
        params.rowId = 2L
        params.columnName = 'columnName'
        params.count = 3
        params.reportResultId = 4L
        params["template.id"] = reportTemplate.id
        controller.saveOnDemandSection(executedTemplateQuery)
        then:
        then:
        response.status == 200
        response.json.success == true
        response.json.alerts[0].message == 'executedReportConfiguration.report.template.section.success'
        response.json.alerts[0].type.name == 'SUCCESS'

    }

    def "test saveOnDemandSection --Validation Error during periodic configuration"(){
        given:
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
        ReportResult reportResult=new ReportResult()
        ExecutedDateRangeInformation executedDateRangeInformationForTemplateQuery = new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(), dateRangeEnum: DateRangeEnum.CUSTOM, dateRangeStartAbsolute: new Date(), dateRangeEndAbsolute: new Date())
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQuery: new SuperQuery(name: 'executedTemplateQuery.executedQuery') ,manuallyAdded : false, onDemandSectionParams: 'onDemandSectionParams', executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],
                executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration, executedDateRangeInformationForTemplateQuery: executedDateRangeInformationForTemplateQuery)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        ReportTemplate reportTemplate = new ReportTemplate(name: 'reportTemplate')
        reportTemplate.save(failOnError:true,validate:false,flush:true)
        ReportResult.metaClass.static.read = {Long id -> reportResult}
        ExecutedReportConfiguration.addToExecutedQueryValueLists(_) >> {true}
        def mockReportExecutorService = Mock( ReportExecutorService )
        mockReportExecutorService.saveExecutedTemplateQuery(_, _, _, _) >> {throw new ValidationException("Validation Exception", executedTemplateQuery.errors)}
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.executeRptFromCount = 1L
        params.rowId = 2L
        params.columnName = 'columnName'
        params.count = 3
        params.reportResultId = 4L
        params["template.id"] = reportTemplate.id
        controller.saveOnDemandSection(executedTemplateQuery)
        then:
        response.status == 500
    }

    def "test saveOnDemandSection --Unexpected Error in periodic configuration"(){
        given:
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
        ReportResult reportResult=new ReportResult()
        ExecutedDateRangeInformation executedDateRangeInformationForTemplateQuery = new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(), dateRangeEnum: DateRangeEnum.CUMULATIVE, dateRangeStartAbsolute: new Date(), dateRangeEndAbsolute: new Date())
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQuery: new SuperQuery(name: 'executedTemplateQuery.executedQuery') ,manuallyAdded : false, onDemandSectionParams: 'onDemandSectionParams', executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],
                executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration, executedDateRangeInformationForTemplateQuery: executedDateRangeInformationForTemplateQuery)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        ReportTemplate reportTemplate = new ReportTemplate(name: 'reportTemplate')
        reportTemplate.save(failOnError:true,validate:false,flush:true)
        ReportResult.metaClass.static.read = {Long id -> reportResult}
        ExecutedReportConfiguration.addToExecutedQueryValueLists(_) >> {true}
        def mockReportExecutorService = Mock( ReportExecutorService )
        mockReportExecutorService.saveExecutedTemplateQuery(_, _, _, _) >> {throw new ValidationException("Validation Exception", executedTemplateQuery.errors)}
        controller.reportExecutorService = mockReportExecutorService
        when:
        params.executeRptFromCount = 1L
        params.rowId = 2L
        params.columnName = 'columnName'
        params.count = 3
        params.reportResultId = 4L
        params["template.id"] = reportTemplate.id
        controller.saveOnDemandSection(executedTemplateQuery)
        then:
        response.status == 500
    }

    void "test Update -- GET"(){
        when:
        request.method = 'GET'
        controller.update()

        then:
        response.status == 302
        response.redirectedUrl == '/periodicReport/index'
    }

    void "test Update -- not Found"(){
        given:
        PeriodicReportConfiguration.metaClass.static.lock = {Serializable serializable -> null}
        when:
        request.method = 'POST'
        controller.update()
        then:
        response.status == 302
        response.redirectedUrl == '/periodicReport/index'

        }

    void "test update -- invalid tenant"(){
        given:
        PeriodicReportConfiguration.metaClass.static.lock = {Serializable serializable -> return new PeriodicReportConfiguration(id: 1L)}
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
        response.redirectedUrl == '/periodicReport/edit'
    }

    def "test update -- success"(){
        given:
        PeriodicReportConfiguration periodicReportConfigurationInstance =  new PeriodicReportConfiguration(id: 1L, reportName: "test", periodicReportType: PeriodicReportTypeEnum.JPSR,
                globalDateRangeInformation: new GlobalDateRangeInformation(), executing : false,
                scheduleDateJSON: '{"startDateTime":"2017-08-29T13:29Z","timeZone":{"name" :"EST","offset" : "-05:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}'
        )
        PeriodicReportConfiguration.metaClass.static.lock = {Serializable serializable -> return periodicReportConfigurationInstance}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        mockUserService.isAnyGranted(_) >> {false}
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        Tenants.metaClass.static.currentId = { return 1L}
        def mockConfigurationService = Mock( ConfigurationService )
        mockConfigurationService.getNextDate(_) >> { PeriodicReportConfiguration config-> return new Date()}
        mockConfigurationService.fixBindDateRange(_, _, _) >> { GlobalDateRangeInformation globalDateRangeInformation, PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)

        }
        mockConfigurationService.bindParameterValuesToGlobalQuery(_, _) >> { PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
        }
        mockConfigurationService.bindSharedWith(_, _, _, _) >> {PeriodicReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> new PeriodicReportConfiguration()}
        mockConfigurationService.checkProductCheckboxes(_) >> { PeriodicReportConfiguration configurationInstance-> false}
        controller.configurationService = mockConfigurationService
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.update(_) >> {return new PeriodicReportConfiguration(id: 1L)}
        controller.CRUDService = mockCRUDService
        def mockTaskTemplateService = Mock( TaskTemplateService )
        mockTaskTemplateService.fetchReportTasksFromRequest(_) >> {}
        controller.taskTemplateService = mockTaskTemplateService

        when:
        request.method = 'POST'
        params.tenantId = '1'
        params.reportId = reportId
        params.deliveryOption = [oneDriveSiteId: 'oneDrive@rxlogix.com']
        controller.update()

        then:
        response.status == 302
        response.redirectedUrl == urlVal

        where:
        reportId  | urlVal
        1L        |'/report/showFirstSection/1'
        null      |'/periodicReport/view'
    }

    void "test update -- failure"(){
        given:
        PeriodicReportConfiguration periodicReportConfigurationInstance =  new PeriodicReportConfiguration(id: 1L, reportName: "test", periodicReportType: PeriodicReportTypeEnum.JPSR,
                globalDateRangeInformation: new GlobalDateRangeInformation(), executing : false,
                scheduleDateJSON: '{"startDateTime":"2017-08-29T13:29Z","timeZone":{"name" :"EST","offset" : "-05:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}'
        )
        PeriodicReportConfiguration.metaClass.static.lock = {Serializable serializable -> return periodicReportConfigurationInstance}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        mockUserService.isAnyGranted(_) >> {false}
        controller.userService = mockUserService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
        Tenants.metaClass.static.currentId = { return 1L}
        def mockConfigurationService = Mock( ConfigurationService )
        mockConfigurationService.getNextDate(_) >> { PeriodicReportConfiguration config-> return new Date()}
        mockConfigurationService.fixBindDateRange(_, _, _) >> { GlobalDateRangeInformation globalDateRangeInformation, PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)

        }
        mockConfigurationService.bindParameterValuesToGlobalQuery(_, _) >> { PeriodicReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
        }
        mockConfigurationService.bindSharedWith(_, _, _, _) >> {PeriodicReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> new PeriodicReportConfiguration()}
        mockConfigurationService.checkProductCheckboxes(_) >> { PeriodicReportConfiguration configurationInstance-> false}
        controller.configurationService = mockConfigurationService
        def mockCRUDService = Mock( CRUDService )
        mockCRUDService.update(_) >> {throw new ValidationException("Validation Exception", periodicReportConfigurationInstance.errors)}
        controller.CRUDService = mockCRUDService
        def mockTaskTemplateService = Mock( TaskTemplateService )
        mockTaskTemplateService.fetchReportTasksFromRequest(_) >> {}
        controller.taskTemplateService = mockTaskTemplateService
        PublisherSource p1 = new PublisherSource(name: 'p1', userGroup: new UserGroup())
        PublisherSource p2 = new PublisherSource(name: 'p2', userGroup: new UserGroup())
        periodicReportConfigurationInstance.addToAttachments(p1)
        periodicReportConfigurationInstance.addToAttachments(p2)
        PublisherConfigurationSection publisherConfigurationSection = new PublisherConfigurationSection(name: 'PublisherConfigurationSection', filename: 'filename')
        periodicReportConfigurationInstance.addToPublisherConfigurationSections(publisherConfigurationSection)

        when:
        request.method = 'POST'
        params.tenantId = '1'
        params.deliveryOption = [oneDriveSiteId: 'oneDrive@rxlogix.com']
        controller.update()

        then:
        response.status == 200
    }

    private void prepareExecutedTemplateForQuery(ExecutedTemplateQuery executedTemplateQuery, ReportTemplate reportTemplate) {
        executedTemplateQuery.executedTemplate = reportTemplate
        SuperQuery.metaClass.static.get = {
            Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
    }
}
