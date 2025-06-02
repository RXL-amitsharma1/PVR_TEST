package com.rxlogix.api

import com.rxlogix.*
import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.enums.*
import com.rxlogix.mapping.*
import com.rxlogix.user.*
import com.rxlogix.util.FilterUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import groovy.mock.interceptor.MockFor
import groovy.transform.CompileStatic
import org.grails.web.json.JSONArray
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

//@TestFor(ConfigurationRestController)
//@Mock([ReportResult, User, Role, UserRole, Preference, Configuration, Query, TemplateQuery, CaseLineListingTemplate, ExecutedConfiguration, ExecutedDeliveryOption,SourceColumnMaster, Tenant,ReportConfiguration,PeriodicReportConfiguration,ExecutionStatus,ExecutedReportConfiguration,ExecutedPeriodicReportConfiguration,ParameterValue,ExecutedIcsrProfileConfiguration,BaseConfiguration,SourceProfile,DateRangeType,ExecutedCaseSeries])
//@Build([Query,CaseLineListingTemplate])
@ConfineMetaClassChanges([LmDeviceType, LmProductSector, LmProductSectorType, LmDrugWithStudy, LmCompanyUnit, LmCompound, User, Tenants, ViewHelper, UserGroup, PeriodicReportConfiguration, ExecutionStatus, Configuration, FilterUtil])
class ConfigurationRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<ConfigurationRestController> {


    public static final user = "unitTest"

    def setup() {

//        def normalUser = makeNormalUser()
//        def adminUser = makeAdminUser()
//        controller.springSecurityService = makeSecurityService(adminUser)
//        def query = new Query()
//        def template = new CaseLineListingTemplate()
//        def config1deliveryOption =new DeliveryOption()
//        def config2deliveryOption =new DeliveryOption()
//
//        ExecutedDeliveryOption executedDeliveryOption = new ExecutedDeliveryOption(config1deliveryOption.properties)
//        def templateQuery =  new TemplateQuery(template: template, query: new Query(),
//                dateRangeInformationForTemplateQuery:new DateRangeInformation(), createdBy: normalUser.username, modifiedBy: normalUser.username)
//        def config1 = new Configuration(nextRunDate: new Date(), reportName: "configuration 1", owner: normalUser, createdBy: normalUser.username, modifiedBy: normalUser.username)
//        config1.addToTemplateQueries(templateQuery)
//        config1.save(failOnError: true, flush: true)
//
//        def ec1 = new ExecutedConfiguration (config1.properties)
//        ec1.executedDeliveryOption = executedDeliveryOption
//        def executedTemplateQuery = new ExecutedTemplateQuery(executedConfiguration: ec1, createdBy: normalUser.username, modifiedBy: normalUser.username)
//        def inProgressResult1 = new ReportResult(executionStatus: ReportExecutionStatusEnum.GENERATING, statusUser: new SharedWith(), scheduledBy: normalUser,templateQuery: templateQuery,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)
//
//        def ec2 = new ExecutedConfiguration (config1.properties)
//        ec2.executedDeliveryOption = new ExecutedDeliveryOption(config1deliveryOption.properties)
//        def executedTemplateQuery2= new ExecutedTemplateQuery(executedConfiguration: ec2, createdBy: normalUser.username, modifiedBy: normalUser.username)
//        def generatedResult1 = new ReportResult(templateQuery: templateQuery,  report: config1, executionStatus: ReportExecutionStatusEnum.DELIVERING, statusUser: new SharedWith(), scheduledBy: normalUser,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)
//
//        def ec3 = new ExecutedConfiguration (config1.properties)
//        ec3.executedDeliveryOption = new ExecutedDeliveryOption(config1deliveryOption.properties)
//        def deliveredResult1 = new ReportResult(executedConfiguration: ec3, report: config1, executionStatus: ReportExecutionStatusEnum.COMPLETED, statusUser: new SharedWith(), scheduledBy: normalUser,templateQuery: templateQuery,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)
//
//        def config2 = new Configuration(nextRunDate: null, reportName: "configuration 2", owner: normalUser,deliveryOption: config2deliveryOption, createdBy: normalUser.username, modifiedBy: normalUser.username)
//        def templateQuery1 = new TemplateQuery(template: template, query: query, dateRangeInformationForTemplateQuery:new DateRangeInformation(), createdBy: normalUser.username, modifiedBy: normalUser.username)
//        config2.addToTemplateQueries(templateQuery1).save(failOnError: true, flush: true)
//        def ec4 = new ExecutedConfiguration (config2.properties)
//        ec4.executedDeliveryOption = new ExecutedDeliveryOption(config2deliveryOption.properties)
//        def inProgressResult2 = new ReportResult(executedConfiguration: ec4, report: config2, executionStatus: ReportExecutionStatusEnum.GENERATING, statusUser: new SharedWith(), scheduledBy: normalUser,templateQuery: templateQuery1,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)
//
//        def ec5 = new ExecutedConfiguration (config2.properties)
//        ec5.executedDeliveryOption = new ExecutedDeliveryOption(config2deliveryOption.properties)
//        def generatedResult2 = new ReportResult(executedConfiguration: ec5, report: config2, executionStatus: ReportExecutionStatusEnum.DELIVERING, statusUser: new SharedWith(), scheduledBy: normalUser, templateQuery: templateQuery1,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)
//
//        def ec6 = new ExecutedConfiguration (config2.properties)
//        ec6.executedDeliveryOption = new ExecutedDeliveryOption(config2deliveryOption.properties)
//        def deliveredResult2 = new ReportResult(executedConfiguration: ec6, report: config2, executionStatus: ReportExecutionStatusEnum.COMPLETED, statusUser: new SharedWith(), scheduledBy: normalUser, templateQuery: templateQuery1,executedTemplateQuery:executedTemplateQuery).save(failOnError: true, flush: true)
//
//        request.addHeader("Accept", "application/json")
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains ReportResult, User, Role, UserRole, Preference, Configuration, Query, TemplateQuery, CaseLineListingTemplate, ExecutedConfiguration, ExecutedDeliveryOption, SourceColumnMaster, Tenant, ReportConfiguration, PeriodicReportConfiguration, ExecutionStatus, ExecutedReportConfiguration, ExecutedPeriodicReportConfiguration, ParameterValue, ExecutedIcsrProfileConfiguration, BaseConfiguration, SourceProfile, DateRangeType,
                LmDeviceType, LmProductSector, LmProductSectorType, LmDrugWithStudy, LmCompanyUnit, LmCompound
    }

    def cleanupSpec() {
        User.metaClass.encodePassword = null
    }

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"),modifiedBy: "user",createdBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user,tenants: [getTenant()])
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false}
        normalUser.metaClass.static.isDev = { -> return false}
        return normalUser
    }

    private mockCurrUser(User normalUser = makeNormalUser()){
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
    }

    private Tenant getTenant() {
        def tenant = Tenant.get(1L)
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = 1L
        return tenant.save(flush:true)
    }


    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"),createdBy: "user",modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isAdmin = { -> return true}
        return adminUser
    }

    private User makeDevUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceDev = new Preference(locale: new Locale("en"),createdBy: "user",modifiedBy: "user")
        def devRole = new Role(authority: 'ROLE_DEV', createdBy: user, modifiedBy: user).save(flush: true)
        def devUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceDev, createdBy: user, modifiedBy: user)
        devUser.addToTenants(tenant)
        devUser.save(failOnError: true)
        UserRole.create(devUser, devRole, true)
        devUser.metaClass.isAdmin = { -> return false}
        devUser.metaClass.static.isDev = { -> return true}
        return devUser
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(SpringSecurityService)
        securityMock.demand.getCurrentUser { -> user }
        return securityMock.proxyInstance()
    }
//TODO: execution status is not fixed yet
//    void "PVR-226: By default do not show Delivered Reports"() {
//        given: "Two configurations"
//
//        when: "Call executionStatus method"
//        controller.executionStatus()
//
//        then: "Report execution status should not be delivered"
//        response.status == 200
//        if (response.json.executionStatus) {
//            response.json.executionStatus.each {
//                it != [ReportExecutionStatus.COMPLETED.value()]
//            }
//        } else {
//            response.json.nextRunDate != null
//        }
//    }

//    void "PVR-226: Show Delivered Reports"() {
//        given: "Two configurations"
//
//        when: "Call delivered method"
//        controller.delivered()
//
//        then: "Report execution status should be delivered"
//        response.status == 200
//        response.json.executionStaus.each {
//            it == [ReportExecutionStatus.COMPLETED.value()]
//        }
//    }

//    void "PVR-226: Show All execution status"() {
//        given: "Two configurations"
//        def configs = Configuration.where { nextRunDate != null }.list()
//
//        when: "Call listAllResults method"
//        controller.listAllResults()
//
//        then: "Should show all results and the configs with next run date"
//        response.status == 200
//        response.json.size() == ReportResult.list().size() + configs.size()
//    }

    void "test briefProperties"(){
        User normalUser = makeNormalUser()
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(reportName: "report",description: "description",owner: normalUser)
        reportConfiguration.dateCreated = new Date()
        reportConfiguration.lastUpdated = new Date()
        reportConfiguration.save(failOnError:true,validate:false)
        def mockCustomMessageService = new MockFor(CustomMessageService)
        mockCustomMessageService.demand.getMessage(0..10){String code, Object... args = null -> "test ran successfully"}
        controller.customMessageService = mockCustomMessageService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}

        controller.userService = mockUserService.proxyInstance()
        when:
        def result = controller.invokeMethod('briefProperties', [[reportConfiguration], false] as Object[])
        then:
        result[0].size() == 12
    }

    void "test index"(){
        User normalUser = makeNormalUser()
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(reportName: "report",description: "description",owner: normalUser)
        reportConfiguration.dateCreated = new Date()
        reportConfiguration.lastUpdated = new Date()
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCustomMessageService = new MockFor(CustomMessageService)
        mockCustomMessageService.demand.getMessage(0..1){String code, Object... args = null -> "test ran successfully"}
        controller.customMessageService = mockCustomMessageService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ReportConfiguration.metaClass.static.getAllIdsByFilter = {LibraryFilter filter, Class clazz, boolean showXMLOption = false, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){
                    return [reportConfiguration.id]
                }
            }
        }
        ReportConfiguration.metaClass.static.countRecordsByFilter = {LibraryFilter filter, boolean showXMLOption = false -> new Object(){
                int get(Object o){
                    return 1
                }
            }
        }
        when:
        params["mixedTypes"] = "false"
        controller.index()
        then:
        response.json.aaData[0].size() == 12
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test getDataMap COMPLETED"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.COMPLETED,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>,isDeleted: false)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.invokeMethod("getDataMap", [executionStatus, [:]])
        then:
        result.size() == 18
    }

    void "test getDataMap COMPLETED for rundate"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(lastRunDate: new Date())
        executedConfiguration.save(flush:true,validate:false,failOnError:true)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,executedEntityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.COMPLETED,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        Map data =[runDate : executedConfiguration?.lastRunDate]
        when:
        def result = controller.invokeMethod("getDataMap", [executionStatus,[(executedConfiguration.id):executedConfiguration]])
        then:
        result.runDate == data.runDate
    }

    void "test getDataMap WARN"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.WARN,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.invokeMethod("getDataMap", [executionStatus, [:]])
        then:
        result.size() == 18
    }

    void "test getDataMap GENERATING"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.GENERATING,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.invokeMethod("getDataMap", [executionStatus, [:]])
        then:
        result.size() == 18
    }

    void "test getDataMap DELIVERING"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.DELIVERING,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.invokeMethod("getDataMap", [executionStatus, [:]])
        then:
        result.size() == 18
    }

    void "test getDataMap GENERATING_DRAFT"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.GENERATING_DRAFT,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.invokeMethod("getDataMap", [executionStatus, [:]])
        then:
        result.size() == 18
    }

    void "test getDataMap GENERATING_FINAL_DRAFT"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.GENERATING_FINAL_DRAFT,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.invokeMethod("getDataMap", [executionStatus, [:]])
        then:
        result.size() == 18
    }

    void "test getDataMap GENERATING_NEW_SECTION"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.GENERATING_NEW_SECTION,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.invokeMethod("getDataMap", [executionStatus, [:]])
        then:
        result.size() == 18
    }

    void "test getDataMap ERROR"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.ERROR,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.invokeMethod("getDataMap", [executionStatus, [:]])
        then:
        result.size() == 18
    }

    void "test getDataMap BACKLOG"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.BACKLOG,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.invokeMethod("getDataMap", [executionStatus, [:]])
        then:
        result.size() == 18
    }

    void "test reportConfigurationMapForError"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.COMPLETED,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.invokeMethod("reportConfigurationMapForError", [executionStatus])
        then:
        result.size() == 1
        result[0].size() == 18
    }

    void "test getPeriodicReportType"(){
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(periodicReportType: PeriodicReportTypeEnum.ADDENDUM)
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        String result = controller.getPeriodicReportType(reportConfiguration)
        then:
        result == "ADDENDUM"
    }

    void "test reportConfigurationMap"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(reportName: "report",owner: normalUser,nextRunDate: new Date(),numOfExecutions: 1,periodicReportType: PeriodicReportTypeEnum.ADDENDUM,deliveryOption: new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser]))
        reportConfiguration.dateCreated = new Date()
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.calculateFrequency(0..1){BaseConfiguration configuration -> return FrequencyEnum.RUN_ONCE}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        def result = controller.reportConfigurationMap([reportConfiguration])
        then:
        result.size() == 1
        result[0].size() == 17
    }

    void "test showExecutionsScheduled sort is version"(){
        given:
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(reportName: "report",owner: normalUser,nextRunDate: new Date(),numOfExecutions: 1,periodicReportType: PeriodicReportTypeEnum.ADDENDUM,deliveryOption: new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser]))
        reportConfiguration.dateCreated = new Date()
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.getAlreadyRunningConfigurationIds = { -> []}
        ReportConfiguration.metaClass.static.fetchAllScheduledForUser = {String search, List<Long> alreadyRunningConfigurationIds, List<Long> backLogConfigurationIds, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy=null,
                                                                         String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[reportConfiguration.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.calculateFrequency(0..1){BaseConfiguration configuration -> return FrequencyEnum.RUN_ONCE}
        controller.configurationService = mockConfigurationService.proxyInstance()
        ExecutionStatus.metaClass.static.findAllByExecutionStatus= { ReportExecutionStatusEnum e -> []}
        when:
        controller.showExecutionsScheduled("",10,0,"version","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 17
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsScheduled sort is owner"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(reportName: "report",owner: normalUser,nextRunDate: new Date(),numOfExecutions: 1,periodicReportType: PeriodicReportTypeEnum.ADDENDUM,deliveryOption: new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser]))
        reportConfiguration.dateCreated = new Date()
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.getAlreadyRunningConfigurationIds = { -> []}
        ReportConfiguration.metaClass.static.fetchAllScheduledForUser = {String search, List<Long> alreadyRunningConfigurationIds, List<Long> backLogConfigurationIds, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy="owner", String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[reportConfiguration.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.calculateFrequency(0..1){BaseConfiguration configuration -> return FrequencyEnum.RUN_ONCE}
        controller.configurationService = mockConfigurationService.proxyInstance()
        ExecutionStatus.metaClass.static.findAllByExecutionStatus= { ReportExecutionStatusEnum e -> []}
        when:
        controller.showExecutionsScheduled("",10,0,"owner","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 17
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsScheduled sort is runDate"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(reportName: "report",owner: normalUser,nextRunDate: new Date(),numOfExecutions: 1,periodicReportType: PeriodicReportTypeEnum.ADDENDUM,deliveryOption: new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser]))
        reportConfiguration.dateCreated = new Date()
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportConfiguration.metaClass.static.getAlreadyRunningConfigurationIds = { -> []}
        ReportConfiguration.metaClass.static.fetchAllScheduledForUser = {String search, List<Long> alreadyRunningConfigurationIds, List<Long> backLogConfigurationIds, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[reportConfiguration.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.calculateFrequency(0..1){BaseConfiguration configuration -> return FrequencyEnum.RUN_ONCE}
        controller.configurationService = mockConfigurationService.proxyInstance()
        ExecutionStatus.metaClass.static.findAllByExecutionStatus= { ReportExecutionStatusEnum e -> []}
        when:
        controller.showExecutionsScheduled("",10,0,"runDate","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 17
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test executedReportConfigurationMap"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.COMPLETED,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        when:
        def result = controller.invokeMethod("executedReportConfigurationMap", [executionStatus])
        then:
        result.size() == 1
        result[0].size() == 18
    }

    void "test showExecutionsCompleted sort is runDate"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.COMPLETED,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndCompletedStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = null, String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsCompleted("",10,0,"runDate","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsCompleted sort is version"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.COMPLETED,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndCompletedStatus = { String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "version", String sortDirection = "asc" ->
            new Object() {
                List list(Object o) {
                    [max: 10, offset: 0]
                    return [[executionStatus.id]]
                }

                int count(Object o) {
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsCompleted("",10,0,"version","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsCompleted sort is owner"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.COMPLETED,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndCompletedStatus = { String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "owner", String sortDirection = "asc" ->
            new Object() {
                List list(Object o) {
                    [max: 10, offset: 0]
                    return [[executionStatus.id]]
                }

                int count(Object o) {
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsCompleted("",10,0,"owner","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsError sort is version"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.ERROR,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndErrorStatus = { String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "version", String sortDirection = "asc" ->
            new Object() {
                List list(Object o) {
                    [max: 10, offset: 0]
                    return [[executionStatus.id]]
                }

                int count(Object o) {
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsError("",10,0,"version","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsError sort is owner"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.ERROR,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndErrorStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "owner", String sortDirection = "asc"  -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsError("",10,0,"owner","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsError sort is runDate"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.ERROR,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndErrorStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "runDate", String sortDirection = "asc"-> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsError("",10,0,"runDate","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsInProgress sort is runDate"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.GENERATING,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndInProgressStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "runDate", String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsInProgress("",10,0,"runDate","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsInProgress sort is version"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.GENERATING,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndInProgressStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "version", String sortDirection = "asc"  -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsInProgress("",10,0,"version","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsInProgress sort is owner"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.GENERATING,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndInProgressStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "owner", String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsInProgress("",10,0,"owner","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsBacklog sort is owner"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.BACKLOG,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndBackLogStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "owner", String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsBacklog("",10,0,"owner","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsBacklog sort is runDate"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.BACKLOG,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndBackLogStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "runDate", String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsBacklog("",10,0,"runDate","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test showExecutionsBacklog sort is version"(){
        User normalUser = makeNormalUser()
        mockCurrUser(normalUser)
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.BACKLOG,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndBackLogStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "version", String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        controller.showExecutionsBacklog("",10,0,"version","",normalUser,null,[:], false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test executionStatus status SCHEDULED"(){
        User normalUser = makeNormalUser()
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(reportName: "report",owner: normalUser,nextRunDate: new Date(),numOfExecutions: 1,periodicReportType: PeriodicReportTypeEnum.ADDENDUM,deliveryOption: new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser]))
        reportConfiguration.dateCreated = new Date()
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ParamsUtils.metaClass.static.parseSharedWithParam = {String sharedWithParam, Long userId -> [:]}
        FilterUtil.metaClass.static.buildCriteria = {Object filterJson, Class clz, Preference userPreference -> null}
        ReportConfiguration.metaClass.static.getAlreadyRunningConfigurationIds = { -> []}
        ReportConfiguration.metaClass.static.fetchAllScheduledForUser = {String search, List<Long> alreadyRunningConfigurationIds, List<Long> backLogConfigurationIds, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sort = null, String order = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[reportConfiguration.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.calculateFrequency(0..1){BaseConfiguration configuration -> return FrequencyEnum.RUN_ONCE}
        controller.configurationService = mockConfigurationService.proxyInstance()
        ExecutionStatus.metaClass.static.findAllByExecutionStatus= { ReportExecutionStatusEnum e -> []}
        when:
        request.method = 'POST'
        params.searchString =""
        params.max = 10
        params.offset = 0
        params.sort = "version"
        params.order = ""
        controller.executionStatus("SCHEDULED", false)
        then:
        response.json.aaData[0].size() == 17
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test executionStatus status COMPLETED"(){
        User normalUser = makeNormalUser()
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.COMPLETED,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ParamsUtils.metaClass.static.parseSharedWithParam = {String sharedWithParam, Long userId -> [:]}
        FilterUtil.metaClass.static.buildCriteria = {Object filterJson, Class clz, Preference userPreference -> null}
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndCompletedStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "runDate", String sortDirection = "asc"-> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        request.method = 'POST'
        params.searchString =""
        params.max = 10
        params.offset = 0
        params.sort = "version"
        params.order = ""
        controller.executionStatus("COMPLETED", false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test executionStatus status ERROR"(){
        User normalUser = makeNormalUser()
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.ERROR,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ParamsUtils.metaClass.static.parseSharedWithParam = {String sharedWithParam, Long userId -> [:]}
        FilterUtil.metaClass.static.buildCriteria = {Object filterJson, Class clz, Preference userPreference -> null}
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndErrorStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "version", String sortDirection = "asc"  -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        request.method = 'POST'
        params.searchString =""
        params.max = 10
        params.offset = 0
        params.sort = "version"
        params.order = ""
        controller.executionStatus("ERROR", false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test executionStatus status BACKLOG"(){
        User normalUser = makeNormalUser()
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.BACKLOG,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ParamsUtils.metaClass.static.parseSharedWithParam = {String sharedWithParam, Long userId -> [:]}
        FilterUtil.metaClass.static.buildCriteria = {Object filterJson, Class clz, Preference userPreference -> null}
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndBackLogStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "owner", String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0, sort: "owner", order: ""]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        request.method = 'POST'
        params.searchString =""
        params.max = 10
        params.offset = 0
        params.sort = "version"
        params.order = ""
        controller.executionStatus("BACKLOG", false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test executionStatus status GENERATING"(){
        User normalUser = makeNormalUser()
        ExecutionStatus executionStatus = new ExecutionStatus(reportName: "report",periodicReportType: "type",reportVersion: 1,frequency: FrequencyEnum.HOURLY,owner: normalUser,entityId: 1L,message: "message",sectionName: "section",nextRunDate: new Date(),executionStatus: ReportExecutionStatusEnum.GENERATING,attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser] as Set<User>)
        executionStatus.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        ParamsUtils.metaClass.static.parseSharedWithParam = {String sharedWithParam, Long userId -> [:]}
        FilterUtil.metaClass.static.buildCriteria = {Object filterJson, Class clz, Preference userPreference -> null}
        ExecutionStatus.metaClass.static.fetchAllBySearchStringAndInProgressStatus = {String search, User user, List<Closure> advancedFilterCriteria, Map shareWith, boolean isICSRProfile, String sortBy = "runDate", String sortDirection = "asc" -> new Object(){
                List list(Object o){[max: 10, offset: 0]
                    return [[executionStatus.id]]
                }
                int count(Object o){
                    return 1
                }
            }
        }
        when:
        request.method = 'POST'
        params.searchString =""
        params.max = 10
        params.offset = 0
        params.sort = "version"
        params.order = ""
        controller.executionStatus("GENERATING", false)
        then:
        response.json.aaData[0].size() == 18
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test getChildProducts level is 1"(){
        LmIngredient.metaClass.static.fetchProductsByIngredient = {BigDecimal ingredientId, String currentLang -> [new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")]}
        LmProductFamily.metaClass.static.findByProductFamilyIdAndLang = {BigDecimal productFamilyId,String lang -> new LmProductFamily(productFamilyId: 1,name: "family",lang: "en")}
        when:
        def result = controller.getChildProducts(new LmIngredient(ingredient: "ingredient",ingredientId: 1,lang: "en"),1,"en")
        then:
        result == [[name:"family", id:1, lang:"en", level:2]]
    }

    void "test getChildProducts level is 2"(){
        LmProduct.metaClass.static.findAllByProductFamilyIdAndLang = {BigDecimal productFamilyId,String lang -> [new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")]}
        when:
        def result = controller.getChildProducts(new LmProductFamily(productFamilyId: 1,name: "family",lang: "en"),2,"en")
        then:
        result == [[name:"product", id:1, lang:"en", level:3]]
    }

    void "test getChildProducts level is 3"(){
        LmProduct.metaClass.static.fetchLicensesByProduct = {BigDecimal productId, String currentLang -> [new LmLicense(licenseId: 1,tradeName: "license",lang: "en")]}
        when:
        def result = controller.getChildProducts(new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en"),3,"en")
        then:
        result == [[name:"license", id:1, lang:"en", level:4]]
    }

    void "test getProductInstance level is 1"(){
        LmIngredient.metaClass.static.fetchByProductDictionaryFilters = { String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> [1] }
        LmIngredient.metaClass.static.createCriteria = { ->
            new Object() {
                List list(Object o) {
                    return [new LmIngredient(ingredient: "ingredient", ingredientId: 1, lang: "en")]
                }
            }
        }

        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        def result = controller.getProductInstance("1","","search,term","en","false",",")
        then:
        result.size() == 1
    }

    void "test getProductInstance level is 1 but lmIngredientIdsList null"(){
        LmIngredient.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> []}
        controller.metaClass.getProductInstanceList = {searchItems, searchTerm, exactSearch, filterList, list, Class clz, fieldName, propertyName -> [new LmIngredient(ingredient: "ingredient",ingredientId: 1,lang: "en")]}
        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        def result = controller.getProductInstance("1","","search,term","en","false",",")
        then:
        result == []
    }

    void "test getProductInstance level is 1 but lmIngredientIdsList null but output not null"(){
        LmIngredient.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> []}
        controller.metaClass.getProductInstanceList = {searchItems, searchTerm, exactSearch, filterList, list, Class clz, fieldName, propertyName -> [new LmIngredient(ingredient: "ingredient",ingredientId: 1,lang: "en")]}
        when:
        def result = controller.getProductInstance("1","","search,term","en","false",",")
        then:
        result.size() == 1
    }

    void "test getProductInstance level is 1 when productId not null"(){
        LmIngredient.metaClass.static.findByIngredientIdAndLang = { BigDecimal ingredientId,String lang -> new LmIngredient(ingredient: "ingredient",ingredientId: 1,lang: "en")}
        when:
        def result = controller.getProductInstance("1","1","search,term","en","false",",")
        then:
        result.class == LmIngredient
    }

    void "test getProductInstance level is 2"(){
        LmProductFamily.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> [1]}
        LmProductFamily.metaClass.static.createCriteria = { ->
            new Object() {
                List list(Object o) {
                    return [new LmProductFamily(productFamilyId: 1,name: "family",lang: "en")]
                }
            }
        }
        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        def result = controller.getProductInstance("2","","search,term","en","false",",")
        then:
        result.size() == 1
    }

    void "test getProductInstance level is 2 but lmProductFamilyIdsList null"(){
        LmProductFamily.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> []}
        controller.metaClass.getProductInstanceList = {searchItems, searchTerm, exactSearch, filterList, list, Class clz, fieldName, propertyName -> [new LmProductFamily(productFamilyId: 1,name: "family",lang: "en")]}
        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        def result = controller.getProductInstance("2","","search,term","en","false",",")
        then:
        result == []
    }

    void "test getProductInstance level is 2 but lmProductFamilyIdsList null but output not null"(){
        LmProductFamily.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> []}
        controller.metaClass.getProductInstanceList = {searchItems, searchTerm, exactSearch, filterList, list, Class clz, fieldName, propertyName -> [new LmProductFamily(productFamilyId: 1,name: "family",lang: "en")]}
        when:
        def result = controller.getProductInstance("2","","search,term","en","false",",")
        then:
        result.size() == 1
    }

    void "test getProductInstance level is 2 when productId not null"(){
        LmProductFamily.metaClass.static.findByProductFamilyIdAndLang = { BigDecimal productFamilyId,String lang -> new LmProductFamily(productFamilyId: 1,name: "family",lang: "en")}
        when:
        def result = controller.getProductInstance("2","1","search,term","en","false",",")
        then:
        result.class == LmProductFamily
    }

    void "test getProductInstance level is 3"(){
        LmProduct.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> [1]}
        LmProduct.metaClass.static.createCriteria = { ->
            new Object() {
                List list(Object o) {
                    return [new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")]
                }
            }
        }
        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        def result = controller.getProductInstance("3","","search,term","en","false",",")
        then:
        result.size() == 1
    }

    void "test getProductInstance level is 3 but lmProductIdsList null"(){
        LmProduct.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> []}
        controller.metaClass.getProductInstanceList = {searchItems, searchTerm, exactSearch, filterList, list, Class clz, fieldName, propertyName -> [new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")]}
        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        def result = controller.getProductInstance("3","","search,term","en","false",",")
        then:
        result == []
    }

    void "test getProductInstance level is 3 but lmProductIdsList null but output not null"(){
        LmProduct.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> []}
        LmProduct.metaClass.static.createCriteria = { ->
            new Object() {
                List list(Object o) {
                    return [new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")]
                }
            }
        }
        when:
        def result = controller.getProductInstance("3","","search,term","en","false",",")
        then:
        result.size() == 1
    }

    void "test getProductInstance level is 3 when productId not null"(){
        LmProduct.metaClass.static.findByProductIdAndLang = { BigDecimal ingredientId,String lang -> new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")}
        when:
        def result = controller.getProductInstance("3","1","search,term","en","false",",")
        then:
        result.class == LmProduct
    }

    void "test getProductInstance level is 4"(){
        LmLicense.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> [1]}
        LmLicense.metaClass.static.createCriteria = { ->
            new Object() {
                List list(Object o) {
                    return [new LmLicense(licenseId: 1,tradeName: "license",lang: "en")]
                }
            }
        }
        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        def result = controller.getProductInstance("4","","search,term","en","false",",")
        then:
        result.size() == 1
    }

    void "test getProductInstance level is 4 but lmLicenseIdsList null"(){
        LmLicense.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> []}
        controller.metaClass.getProductInstanceList = {searchItems, searchTerm, exactSearch, filterList, list, Class clz, fieldName, propertyName -> [new LmLicense(licenseId: 1,tradeName: "license",lang: "en")]}
        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        def result = controller.getProductInstance("4","","search,term","en","false",",")
        then:
        result == []
    }

    void "test getProductInstance level is 4 but lmLicenseIdsList null but output not null"(){
        LmLicense.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> []}
        LmLicense.metaClass.static.createCriteria = { ->
            new Object() {
                List list(Object o) {
                    return [new LmLicense(licenseId: 1,tradeName: "license",lang: "en")]
                }
            }
        }
        when:
        def result = controller.getProductInstance("4","","search,term","en","false",",")
        then:
        result.size() == 1
    }

    void "test getProductInstance level is 4 when productId not null"(){
        LmLicense.metaClass.static.findByLicenseIdAndLang = { BigDecimal ingredientId,String lang -> new LmLicense(licenseId: 1,tradeName: "license",lang: "en")}
        when:
        def result = controller.getProductInstance("4","1","search,term","en","false",",")
        then:
        result.class == LmLicense
    }

    void "test getSelectedProduct level 1"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmIngredient.metaClass.static.findByIngredientIdAndLang = { BigDecimal ingredientId,String lang -> new LmIngredient(ingredient: "ingredient",ingredientId: 1,lang: "en")}
        LmIngredient.metaClass.static.fetchProductsByIngredient = {BigDecimal ingredientId, String currentLang -> [new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")]}
        LmProductFamily.metaClass.static.findByProductFamilyIdAndLang = {BigDecimal productFamilyId,String lang -> new LmProductFamily(productFamilyId: 1,name: "family",lang: "en")}
        when:
        params.dictionaryLevel = "1"
        params.productId = "1"
        controller.getSelectedProduct()
        then:
        response.json == [nextLevelItems:[[level:2, name:"family", id:1, lang:"en"]], name:"ingredient", id:1, lang:"en"]
    }

    void "test getSelectedProduct level 2"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmProductFamily.metaClass.static.findByProductFamilyIdAndLang = { BigDecimal ingredientId,String lang -> new LmProductFamily(productFamilyId: 1,name: "family",lang: "en")}
        LmProduct.metaClass.static.findAllByProductFamilyIdAndLang = {BigDecimal productFamilyId,String lang -> [new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")]}
        when:
        params.dictionaryLevel = "2"
        params.productId = "1"
        controller.getSelectedProduct()
        then:
        response.json == [nextLevelItems:[[level:3, name:"product", id:1, lang:"en"]], name:"family", id:1, lang:"en"]
    }

    void "test getSelectedProduct level 3"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmProduct.metaClass.static.findByProductIdAndLang = { BigDecimal ingredientId,String lang -> new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")}
        LmProduct.metaClass.static.fetchLicensesByProduct = {BigDecimal productId, String currentLang -> [new LmLicense(licenseId: 1,tradeName: "license",lang: "en")]}
        when:
        params.dictionaryLevel = "3"
        params.productId = "1"
        controller.getSelectedProduct()
        then:
        response.json == [nextLevelItems:[[level:4, name:"license", id:1, lang:"en"]], name:"product", id:1, lang:"en"]
    }

    void "test getPreLevelProductParents level 2"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmProductFamily.metaClass.static.findByProductFamilyIdAndLang = { BigDecimal productFamilyId,String lang -> new LmProductFamily(productFamilyId: 1,name: "family",lang: "en")}
        LmProduct.metaClass.static.findAllByProductFamilyIdAndLang = { BigDecimal productFamilyId,String lang -> [new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")]}
        LmProduct.metaClass.static.fetchIngredientsByProduct = { BigDecimal productId, String currentLang -> [new LmIngredient(ingredient: "ingredient",ingredientId: 1,lang: "en")]}
        when:
        params.productIds = "1"
        params.dictionaryLevel = "2"
        controller.getPreLevelProductParents()
        then:
        response.json == [[level:1, name:"ingredient", id:1, lang:"en"]]
    }

    void "test getPreLevelProductParents level 3"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmProduct.metaClass.static.findByProductIdAndLang = { BigDecimal productFamilyId,String lang  -> new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")}
        LmProductFamily.metaClass.static.findByProductFamilyIdAndLang = { BigDecimal productFamilyId,String lang -> new LmProductFamily(productFamilyId: 1,name: "family",lang: "en")}
        when:
        params.productIds = "1"
        params.dictionaryLevel = "3"
        controller.getPreLevelProductParents()
        then:
        response.json == [[level:2, name:"family", id:1, lang:"en"]]
    }

    void "test getPreLevelProductParents level 4"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmLicense.metaClass.static.findByLicenseIdAndLang = { BigDecimal ingredientId,String lang -> new LmLicense(licenseId: 1,tradeName: "license",lang: "en")}
        LmLicense.metaClass.static.fetchProductsByLicense = { BigDecimal licenseId,String currentLang -> [new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")]}
        when:
        params.productIds = "1"
        params.dictionaryLevel = "4"
        controller.getPreLevelProductParents()
        then:
        response.json == [[level:3, name:"product", id:1, lang:"en"]]
    }

    void "test searchProducts level 1"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmIngredient.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> [1]}
        controller.metaClass.getProductInstanceList = {searchItems, searchTerm, exactSearch, filterList, list, Class clz, fieldName, propertyName -> [new LmIngredient(ingredient: "ingredient",ingredientId: 1,lang: "en")]}
        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        params.contains = "search,term"
        params.delimiter = ","
        params.dictionaryLevel = "1"
        controller.searchProducts()
        then:
        response.json == [[level:"1", name:"ingredient", id:1, lang:"en"]]
    }

    void "test searchProducts level 2"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmProductFamily.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> [1]}
        controller.metaClass.getProductInstanceList = {searchItems, searchTerm, exactSearch, filterList, list, Class clz, fieldName, propertyName -> [new LmProductFamily(productFamilyId: 1,name: "family",lang: "en")]}
        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        params.contains = "search,term"
        params.delimiter = ","
        params.dictionaryLevel = "2"
        controller.searchProducts()
        then:
        response.json == [[level:"2", name:"family", id:1, lang:"en"]]
    }

    void "test searchProducts level 3"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmProduct.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> [1]}
        LmProduct.metaClass.static.createCriteria = { ->
            new Object() {
                List list(Object o) {
                    return [new LmProduct(productFamilyId: 1,productId: 1,name: "product",lang: "en")]
                }
            }
        }
        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        params.contains = "search,term"
        params.delimiter = ","
        params.dictionaryLevel = "3"
        controller.searchProducts()
        then:
        response.json == [[level:"3", name:"product", id:1, lang:"en"]]
    }

    void "test searchProducts level 4"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmLicense.metaClass.static.fetchByProductDictionaryFilters = {String productSectorId, String productSectorTypeId, String deviceTypeId, String companyUnitId -> [1]}
        LmLicense.metaClass.static.createCriteria = { ->
            new Object() {
                List list(Object o) {
                    return [new LmLicense(licenseId: 1,tradeName: "license",lang: "en")]
                }
            }
        }
        when:
        params.productSector = "1"
        params.productSectorType = "1"
        params.deviceType = "1"
        params.companyUnit = "1"
        params.contains = "search,term"
        params.delimiter = ","
        params.dictionaryLevel = "4"
        controller.searchProducts()
        then:
        response.json == [[level:"4", name:"license", id:1, lang:"en"]]
    }

    void "test getStudyInstance level is 1 when exactsearch and searchterm true "(){
        LmProtocols.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmProtocols(id: 1,protocolId: 1,description: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("1","","search","en","true",null,false)
        then:
        result.size() == 1
    }

    void "test getStudyInstance level is 1 when exactsearch and searchitems true "(){
        LmProtocols.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmProtocols(id: 1,protocolId: 1,description: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("1","","search,term","en","true",",",false)
        then:
        result.size() == 1
    }

    void "test getStudyInstance level is 1 when exactsearch false and searchterm true "(){
        LmProtocols.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmProtocols(id: 1,protocolId: 1,description: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("1","","search","en","false",null,false)
        then:
        result.size() == 1
    }

    void "test getStudyInstance level is 1 when exactsearch false and searchitems true "(){
        LmProtocols.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmProtocols(id: 1,protocolId: 1,description: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("1","","search,term","en","false",",",false)
        then:
        result.size() == 1
    }

    void "test getStudyInstance level is 1 when studyId true "(){
        LmProtocols.metaClass.static.findByProtocolIdAndLang = {BigDecimal protocolId,String lang -> new LmProtocols(id: 1,protocolId: 1,description: 'search',lang: 'en')}
        when:
        def result = controller.getStudyInstance("1","1","search,term","en","false",",",false)
        then:
        result.class == LmProtocols
    }

    void "test getStudyInstance level is 2 when exactsearch and searchterm true "(){
        LmStudies.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("2","","search","en","true",null,false)
        then:
        result.size() == 1
    }

    void "test getStudyInstance level is 2 when exactsearch and searchitems true "(){
        LmStudies.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("2","","search,term","en","true",",",false)
        then:
        result.size() == 1
    }

    void "test getStudyInstance level is 2 when exactsearch false and searchterm true "(){
        LmStudies.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("2","","search","en","false",null,false)
        then:
        result.size() == 1
    }

    void "test getStudyInstance level is 2 when exactsearch false and searchitems true "(){
        LmStudies.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("2","","search,term","en","false",",",false)
        then:
        result.size() == 1
    }

    void "test getStudyInstance level is 2 when studyId true "(){
        LmStudies.metaClass.static.findByStudyIdAndLang = { BigDecimal studyId, String lang -> new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')}
        when:
        def result = controller.getStudyInstance("2","1","search,term","en","false",",",false)
        then:
        result.class == LmStudies
    }

    void "test getStudyInstance level 3 when searchitems is null"(){
        LmDrugWithStudy.metaClass.static.findAllByNameLike = { String name-> [new LmDrugWithStudy(productFamilyId: 1,productId: 1,name: "DrugStudy",lang: "en")]}
        LmStudyDrugs.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudyDrugs(productId: 1,studyId: 1,isImp: false)]
                }
            }
        }
        LmStudies.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("3","","DrugStudy","en","false","",false)
        then:
        result.size() == 1
    }

    void "test getStudyInstance level 3 when searchitems is not null"(){
        LmDrugWithStudy.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [1]
                }
            }
        }
        LmStudyDrugs.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudyDrugs(productId: 1,studyId: 1,isImp: false)]
                }
            }
        }
        LmStudies.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("3","","DrugStudy,search","en","false",",",false)
        then:
        result.size() == 1
    }

    void "test getStudyInstance level 4 when searchitems is null"(){
        LmCompound.metaClass.static.findAllByNumberLike = { String number-> [new LmCompound(number: "1",lang: "en")]}
        LmStudyCompound.metaClass.static.findAllByCompoundIdInList = { List list -> [new LmStudyCompound(compoundId: "1",lang: "en",studyId: 1)] }
        LmStudies.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("4","","1","en","false","",false)
        then:
        result.size() == 1
    }

    void "test getStudyInstance level 4 when searchitems is not null"(){
        LmCompound.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return ["1"]
                }
            }
        }
        LmStudyCompound.metaClass.static.findAllByCompoundIdInList = { List list -> [new LmStudyCompound(compoundId: "1",lang: "en",studyId: 1)] }
        LmStudies.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')]
                }
            }
        }
        when:
        def result = controller.getStudyInstance("4","","1,search","en","false",",",false)
        then:
        result.size() == 1
    }

    void "test getChildStudies"(){
        LmProtocols.metaClass.static.fetchStudiesByProtocols = {BigDecimal protocolId, String currentLang -> [new LmStudies(id: 1,studyId: 1,studyNum: 'study',lang: 'en')] }
        when:
        def result = controller.getChildStudies(new LmProtocols(id: 1,protocolId: 1,description: 'search',lang: 'en'),1,"en")
        then:
        result == [[name:"study", id:1, lang:"en", level:2]]
    }

    void "test getParentStudies"(){
        LmStudies.metaClass.static.fetchProtocolsByStudy = {BigDecimal studyId,String currentLang -> [new LmProtocols(id: 1,protocolId: 1,description: 'protocol',lang: 'en')] }
        when:
        def result = controller.getParentStudies(new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en'),2,new JSONArray(),"en")
        then:
        result == [[name:"protocol", id:1, lang:"en", level:1]]
    }

    void "test getSelectedStudy level is 1"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmProtocols.metaClass.static.findByProtocolIdAndLang = {BigDecimal protocolId,String lang -> new LmProtocols(id: 1,protocolId: 1,description: 'search',lang: 'en')}
        LmProtocols.metaClass.static.fetchStudiesByProtocols = {BigDecimal protocolId, String currentLang -> [new LmStudies(id: 1,studyId: 1,studyNum: 'study',lang: 'en')] }
        when:
        params.dictionaryLevel = "1"
        params.studyId = "1"
        controller.getSelectedStudy()
        then:
        response.json == [nextLevelItems:[[level:2, name:"study", id:1, lang:"en"]], name:"search", id:1, lang:"en"]
    }

    void "test getSelectedStudy level is 2"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmStudies.metaClass.static.findByStudyIdAndLang = { BigDecimal studyId, String lang -> new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')}
        when:
        params.dictionaryLevel = "2"
        params.studyId = "1"
        controller.getSelectedStudy()
        then:
        response.json == [nextLevelItems:[], name:"search", id:1, lang:"en"]
    }

    void "test getPreLevelStudyParents level is 1"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmProtocols.metaClass.static.findByProtocolIdAndLang = {BigDecimal protocolId,String lang -> new LmProtocols(id: 1,protocolId: 1,description: 'search',lang: 'en')}
        when:
        params.dictionaryLevel = "1"
        params.studyIds = "1"
        controller.getPreLevelStudyParents()
        then:
        response.json == []
    }

    void "test getPreLevelStudyParents level is 2"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmStudies.metaClass.static.findByStudyIdAndLang = { BigDecimal studyId, String lang -> new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')}
        LmStudies.metaClass.static.fetchProtocolsByStudy = {BigDecimal studyId,String currentLang -> [new LmProtocols(id: 1,protocolId: 1,description: 'protocol',lang: 'en')] }
        when:
        params.dictionaryLevel = "2"
        params.studyIds = "1"
        controller.getPreLevelStudyParents()
        then:
        response.json == [[level:1, name:"protocol", id:1, lang:"en"]]
    }

    void "test searchStudies level is 1"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmProtocols.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmProtocols(id: 1,protocolId: 1,description: 'search',lang: 'en')]
                }
            }
        }
        when:
        params.dictionaryLevel = 1
        params.contains = "search"
        params.exact_search = "true"
        params.delimiter = ""
        params.imp = "false"
        controller.searchStudies()
        then:
        response.json == [[level:1, name:"search", id:1, lang:"en"]]
    }

    void "test searchStudies level is 2"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmStudies.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')]
                }
            }
        }
        when:
        params.dictionaryLevel = 2
        params.contains = "search"
        params.exact_search = "true"
        params.delimiter = ""
        params.imp = "false"
        controller.searchStudies()
        then:
        response.json == [[level:2, name:"search", id:1, lang:"en"]]
    }

    void "test searchStudies level is 3"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmDrugWithStudy.metaClass.static.findAllByNameLike = { String name-> [new LmDrugWithStudy(productFamilyId: 1,productId: 1,name: "DrugStudy",lang: "en")]}
        LmStudyDrugs.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudyDrugs(productId: 1,studyId: 1,isImp: false)]
                }
            }
        }
        LmStudies.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')]
                }
            }
        }
        when:
        params.dictionaryLevel = 3
        params.contains = "DrugStudy"
        params.exact_search = "false"
        params.delimiter = ""
        params.imp = "false"
        controller.searchStudies()
        then:
        response.json == [[level:3, name:"search", id:1, lang:"en"]]
    }

    void "test searchStudies level is 4"(){
        User normalUser = makeNormalUser()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        LmCompound.metaClass.static.findAllByNumberLike = { String number-> [new LmCompound(number: "1",lang: "en")]}
        LmStudyCompound.metaClass.static.findAllByCompoundIdInList = { List list -> [new LmStudyCompound(compoundId: "1",lang: "en",studyId: 1)] }
        LmStudies.metaClass.static.createCriteria = { -> new Object(){
                List list(Object o){
                    return [new LmStudies(id: 1,studyId: 1,studyNum: 'search',lang: 'en')]
                }
            }
        }
        when:
        params.dictionaryLevel = 4
        params.contains = "1"
        params.exact_search = "false"
        params.delimiter = ""
        params.imp = "false"
        controller.searchStudies()
        then:
        response.json == [[level:4, name:"search", id:1, lang:"en"]]
    }

    void"test killExecution success"(){
        boolean run = false
        User normalUser = makeNormalUser()
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName: "report",owner: normalUser)
        periodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutionStatus executionStatus = new ExecutionStatus(entityType: ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION,entityId: periodicReportConfiguration.id)
        executionStatus.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(2..2) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executionStatus.id
        controller.killExecution(executionStatus.id)
        then:
        run == true
        response.json == [success : true]
    }

    void"test killExecution owner id does not match"(){
        boolean run = false
        User normalUser = makeNormalUser()
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName: "report")
        periodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutionStatus executionStatus = new ExecutionStatus(entityType: ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION,entityId: periodicReportConfiguration.id)
        executionStatus.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(2..2) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executionStatus.id
        controller.killExecution(executionStatus.id)
        then:
        run == false
        response.status == 401
        response.json == [message:"app.configuration.edit.permission", status:401]
    }

    void"test killExecution executing object does not exist"(){
        boolean run = false
        User normalUser = makeNormalUser()
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName: "report")
        periodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutionStatus executionStatus = new ExecutionStatus()
        executionStatus.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(2..2) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executionStatus.id
        controller.killExecution(executionStatus.id)
        then:
        run == false
        response.status == 404
        response.json == [message:"default.not.found.message", status:404]
    }

    void"test killExecution execption"(){
        boolean run = false
        User normalUser = makeNormalUser()
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName: "report",owner: normalUser)
        periodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutionStatus executionStatus = new ExecutionStatus(entityType: ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION,entityId: periodicReportConfiguration.id)
        executionStatus.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(2..2) { -> normalUser }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        PeriodicReportConfiguration.metaClass.static.isEditableBy = {User currentUser -> true}
        when:
        params.id = executionStatus.id
        controller.killExecution(executionStatus.id)
        then:
        run == false
        response.status == 500
        response.json == [message:"default.server.error.message", status:500]
    }

    void "test getConfigurationPOIInputsParams"(){
        ParameterValue parameterValue = new ParameterValue(value: "value",key:"key_1")
        parameterValue.save(failOnError:true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(poiInputsParameterValues: [parameterValue])
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.getConfigurationPOIInputsParams(executedReportConfiguration.id)
        then:
        response.json == [data:["key_1"]]
    }

    void "test unschedule success"(){
        boolean run = false
        User devUser = makeDevUser()
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(reportName: "report",description: "description",owner: devUser)
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> devUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        PeriodicReportConfiguration.metaClass.static.isEditableBy = {User currentUser -> true}
        when:
        params.id = reportConfiguration.id
        controller.unschedule()
        then:
        run == true
        response.json == [success : true]
    }

    void "test unschedule already executing"(){
        boolean run = false
        User devUser = makeDevUser()
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(reportName: "report",description: "description",owner:devUser)
        reportConfiguration.executing = true
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> devUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        PeriodicReportConfiguration.metaClass.static.isEditableBy = {User currentUser -> true}
        when:
        params.id = reportConfiguration.id
        controller.unschedule()
        then:
        run == false
        response.status == 406
        response.json == [message:"app.configuration.unscheduled.fail", status:406]
    }

    void "test unschedule does not have permission to edit"(){
        boolean run = false
        User normalUser = makeNormalUser()
        UserGroup.metaClass.static.fetchAllUserGroupByUser={ User user->[]}
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(reportName: "report",description: "description",owner: makeDevUser(),tenantId: 100)
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        Tenants.metaClass.static.currentId = { -> return 0}
        PeriodicReportConfiguration.metaClass.static.isEditableBy = {User currentUser -> false}
        when:
        params.id = reportConfiguration.id
        controller.unschedule()
        then:
        run == false
        response.status == 401
        response.json == [message:"app.configuration.edit.permission", status:401]
    }

    void "test unschedule exception"(){
        boolean run = false
        User devUser = makeDevUser()
        ReportConfiguration reportConfiguration = new PeriodicReportConfiguration(reportName: "report",description: "description",owner: devUser)
        reportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> devUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        PeriodicReportConfiguration.metaClass.static.isEditableBy = {User currentUser -> true}
        when:
        params.id = reportConfiguration.id
        controller.unschedule()
        then:
        run == false
        response.status == 500
        response.json == [message:"default.server.error.message", status:500]
    }

    void "test getProductSectorList"(){
        def lmProductSector = new LmProductSector()
        lmProductSector.name = "ProductSector"
        lmProductSector.id = 1
        lmProductSector.lang = "en"
        lmProductSector.save(failOnError:true,flush:true,validate:false)
        LmProductSector.metaClass.static.createCriteria = { ->
            new Object() {
                List list(LinkedHashMap M, Closure cl) {
                    List l = [lmProductSector]
                    l.metaClass.getTotalCount = {
                        return 1L
                    }
                    return l
                }
            }
        }
        when:
        controller.getProductSectorList("ProductSector",0,0)
        then:
        response.json == [total_count: 1, items: [[id: lmProductSector.id, text: lmProductSector.name]]]
    }

    void "test getProductSectorTypeList"(){
        def lmProductSectorType = new LmProductSectorType()
        lmProductSectorType.name = "ProductSector"
        lmProductSectorType.id = 1
        lmProductSectorType.lang = "en"
        lmProductSectorType.save(failOnError:true,flush:true,validate:false)
        LmProductSectorType.metaClass.static.createCriteria = { ->
            new Object() {
                List list(LinkedHashMap M, Closure cl) {
                    List l = [lmProductSectorType]
                    l.metaClass.getTotalCount = {
                        return 1L
                    }
                    return l
                }
            }
        }
        when:
        controller.getProductSectorList("ProductSector",0,0)
        then:
        response.json == [total_count: 1, items: [[id: lmProductSectorType.id, text: lmProductSectorType.name]]]
    }

    void "test getProductsList"(){
        User normalUser = makeNormalUser()
        def lmDrugWithStudy = new LmDrugWithStudy()
        lmDrugWithStudy.name = "ProductSector"
        lmDrugWithStudy.productFamilyId = 1
        lmDrugWithStudy.productId = 1
        lmDrugWithStudy.lang = "en"
        lmDrugWithStudy.save(failOnError:true,flush:true,validate:false)
        LmDrugWithStudy.metaClass.static.createCriteria = { ->
            new Object() {
                List list(LinkedHashMap M, Closure cl) {
                    List l = [lmDrugWithStudy]
                    l.metaClass.getTotalCount = {
                        return 1L
                    }
                    return l
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.getProductsList("ProductSector",0,0)
        then:
        response.json == [total_count:1, items:[[id:lmDrugWithStudy.name, text:lmDrugWithStudy.name]]]
    }

    void "test getDeviceTypeList"(){
        def lmDeviceType = new LmDeviceType()
        lmDeviceType.name = "ProductSector"
        lmDeviceType.id = 1
        lmDeviceType.lang = "en"
        lmDeviceType.save(failOnError:true,flush:true,validate:false)
        LmDeviceType.metaClass.static.createCriteria = { ->
            new Object() {
                List list(LinkedHashMap M, Closure cl) {
                    List l = [lmDeviceType]
                    l.metaClass.getTotalCount = {
                        return 1L
                    }
                    return l
                }
            }
        }
        when:
        controller.getDeviceTypeList("ProductSector",0,0)
        then:
        response.json == [total_count: 1, items: [[id: lmDeviceType.id, text: lmDeviceType.name]]]
    }

    void "test getCompanyUnitList"(){
        def lmCompanyUnit = new LmCompanyUnit()
        lmCompanyUnit.name = "ProductSector"
        lmCompanyUnit.id = 1
        lmCompanyUnit.lang = "en"
        lmCompanyUnit.save(failOnError:true,flush:true,validate:false)
        LmCompanyUnit.metaClass.static.createCriteria = { ->
            new Object() {
                List list(LinkedHashMap M, Closure cl) {
                    List l = [lmCompanyUnit]
                    l.metaClass.getTotalCount = {
                        return 1L
                    }
                    return l
                }
            }
        }
        when:
        controller.getCompanyUnitList("ProductSector",0,0)
        then:
        response.json == [total_count: 1, items: [[id: lmCompanyUnit.id, text: lmCompanyUnit.name]]]
    }

    void "test getCompoundsList"(){
        User normalUser = makeNormalUser()
        def lmCompound = new LmCompound()
        lmCompound.number = '1'
        lmCompound.lang = "en"
        lmCompound.save(failOnError:true,flush:true,validate:false)
        LmCompound.metaClass.static.createCriteria = { ->
            new Object() {
                List list(LinkedHashMap M, Closure cl) {
                    List l = [lmCompound]
                    l.metaClass.getTotalCount = {
                        return 1L
                    }
                    return l
                }
            }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.getCompoundsList("ProductSector",0,0)
        then:
        response.json == [total_count: 1, items: [[id: lmCompound.number, text: lmCompound.number]]]
    }

    void "test removeFromBacklog success"(){
        boolean run = false
        User normalUser = makeNormalUser()
        BaseConfiguration baseConfiguration = new ExecutedIcsrProfileConfiguration(reportName: "report",owner: normalUser)
        baseConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutionStatus executionStatus = new ExecutionStatus(entityType: ExecutingEntityTypeEnum.EXECUTED_ICSR_PROFILE_CONFIGURATION,entityId: baseConfiguration.id)
        executionStatus.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        ExecutionStatus.metaClass.static.removeFromBacklog = {Long id -> return true}
        when:
        params.id = executionStatus.id
        controller.removeFromBacklog()
        then:
        run == true
        response.json == [success : true]
    }

    void"test removeFromBacklog owner id does not match"(){
        boolean run = false
        User normalUser = makeNormalUser()
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName: "report")
        periodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutionStatus executionStatus = new ExecutionStatus(entityType: ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION,entityId: periodicReportConfiguration.id)
        executionStatus.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executionStatus.id
        controller.removeFromBacklog()
        then:
        run == false
        response.status == 401
        response.json == [message:"app.configuration.edit.permission", status:401]
    }

    void"test removeFromBacklog executing object does not exist"(){
        boolean run = false
        User normalUser = makeNormalUser()
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName: "report")
        periodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutionStatus executionStatus = new ExecutionStatus()
        executionStatus.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executionStatus.id
        controller.removeFromBacklog()
        then:
        run == false
        response.status == 404
        response.json == [message:"default.not.found.message", status:404]
    }

    void"test removeFromBacklog execption"(){
        boolean run = false
        User normalUser = makeNormalUser()
        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName: "report",owner: normalUser)
        periodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutionStatus executionStatus = new ExecutionStatus(entityType: ExecutingEntityTypeEnum.PERIODIC_CONFIGURATION,entityId: periodicReportConfiguration.id)
        executionStatus.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executionStatus.id
        controller.removeFromBacklog()
        then:
        run == false
        response.status == 500
        response.json == [message:"default.server.error.message", status:500]
    }

    void "test bulkSchedulingList"(){
        boolean run = false
        params.sort=sort
        params.oder=order
        User normalUser = makeNormalUser()
        Configuration configuration = new Configuration()
        configuration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        Configuration.metaClass.static.ownedByUser = { User user ->
            new Object() {
                List list(Object o) {
                    [0]
                }
            }
        }
        Configuration.metaClass.static.fetchAllIdsForBulkUpdate = {LibraryFilter filter, List<Long> sharedWithIds -> new Object(){
                List list(Object o){[max: 10, offset: 0, sort: params.sort, order: params.order]
                    return [configuration.id]
                }
            }
        }
        Configuration.metaClass.static.countAllForBulkUpdate = {LibraryFilter filter, List<Long> sharedWithIds -> new Object(){
                int get(Object o){
                    return 1
                }
            }
        }
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.toBulkTableMap(0..1){Configuration conf ->
            run = true
            return [:]
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.bulkSchedulingList()
        then:
        run == true
        response.json == [recordsFiltered:1, aaData:[[:]], recordsTotal:1]
        where:
        sort<<["", "DeliveryOption.sharedWith", "DeliveryOption.emailToUsers"]
        order<<["", "asc", "desc"]
    }

    void "test fetchDateRangeTypesForDatasource source profile has dateRangeType"(){
        DateRangeType dateRangeType = new DateRangeType(name: "date")
        dateRangeType.save(failOnError:true,validate:false,flush:true)
        SourceProfile sourceProfile = new SourceProfile(dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError:true,validate:false,flush:true)
        ViewHelper.metaClass.static.getMessage = {String code, Object[] params = null, String defaultLabel='' -> return "app.dateDropdown.date"}
        when:
        controller.fetchDateRangeTypesForDatasource(sourceProfile.id)
        then:
        response.json == [[display:"app.dateDropdown.date", name:1]]
    }

    void "test fetchDateRangeTypesForDatasource source profile do not have dateRangeType"(){
        DateRangeType dateRangeType = new DateRangeType(name: "date")
        dateRangeType.save(failOnError:true,validate:false,flush:true)
        SourceProfile sourceProfile = new SourceProfile()
        sourceProfile.save(failOnError:true,validate:false,flush:true)
        ViewHelper.metaClass.static.getMessage = {String code, Object[] params = null, String defaultLabel='' -> return "app.dateDropdown.date"}
        when:
        controller.fetchDateRangeTypesForDatasource(sourceProfile.id)
        then:
        response.json == [[display:"app.dateDropdown.date", name:1]]
    }

    void "test fetchEvaluateCaseDatesForDatasource source profile includes latest version date"(){
        SourceProfile sourceProfile = new SourceProfile(includeLatestVersionOnly: true)
        sourceProfile.save(failOnError:true,validate:false,flush:true)
        ViewHelper.metaClass.static.getMessage = {String code, Object[] params = null, String defaultLabel='' -> return "app.dateRangeType"}
        when:
        controller.fetchEvaluateCaseDatesForDatasource(sourceProfile.id,true)
        then:
        response.json == [[display:"app.dateRangeType", name:"LATEST_VERSION"]]
    }

    void "test fetchEvaluateCaseDatesForDatasource source profile does not includes latest version date"(){
        SourceProfile sourceProfile = new SourceProfile()
        sourceProfile.save(failOnError:true,validate:false,flush:true)
        ViewHelper.metaClass.static.getMessage = {String code, Object[] params = null, String defaultLabel='' -> return "app.dateRangeType"}
        when:
        controller.fetchEvaluateCaseDatesForDatasource(sourceProfile.id,true)
        then:
        response.json == [[display:"app.dateRangeType", name:"LATEST_VERSION"], [display:"app.dateRangeType", name:"ALL_VERSIONS"], [display:"app.dateRangeType", name:"VERSION_PER_REPORTING_PERIOD"], [display:"app.dateRangeType", name:"VERSION_ASOF"]]
    }

    void "test fetchEvaluateCaseDatesForDatasource source profile does not includes latest version date showAllVersions false"(){
        SourceProfile sourceProfile = new SourceProfile()
        sourceProfile.save(failOnError:true,validate:false,flush:true)
        ViewHelper.metaClass.static.getMessage = {String code, Object[] params = null, String defaultLabel='' -> return "app.dateRangeType"}
        when:
        controller.fetchEvaluateCaseDatesForDatasource(sourceProfile.id,false)
        then:
        response.json == [[display:"app.dateRangeType", name:"LATEST_VERSION"], [display:"app.dateRangeType", name:"VERSION_PER_REPORTING_PERIOD"], [display:"app.dateRangeType", name:"VERSION_ASOF"]]
    }

    void "test fetchEvaluateCaseDateSubmissionForDatasource source profile includes latest version date"(){
        SourceProfile sourceProfile = new SourceProfile(includeLatestVersionOnly: true)
        sourceProfile.save(failOnError:true,validate:false,flush:true)
        ViewHelper.metaClass.static.getMessage = {String code, Object[] params = null, String defaultLabel='' -> return "app.dateRangeType"}
        when:
        controller.fetchEvaluateCaseDateSubmissionForDatasource(sourceProfile.id)
        then:
        response.json == [[display:"app.dateRangeType", name:"LATEST_VERSION"]]
    }

    void "test fetchEvaluateCaseDateSubmissionForDatasource source profile does not includes latest version date"(){
        SourceProfile sourceProfile = new SourceProfile()
        sourceProfile.save(failOnError:true,validate:false,flush:true)
        ViewHelper.metaClass.static.getMessage = {String code, Object[] params = null, String defaultLabel='' -> return "app.dateRangeType"}
        when:
        controller.fetchEvaluateCaseDateSubmissionForDatasource(sourceProfile.id)
        then:
        response.json == [[display:"app.dateRangeType", name:"LATEST_VERSION"], [display:"app.dateRangeType", name:"ALL_VERSIONS"], [display:"app.dateRangeType", name:"VERSION_PER_REPORTING_PERIOD"],[display:"app.dateRangeType", name:"VERSION_ASOF_GENERATION_DATE"]]
    }
}
