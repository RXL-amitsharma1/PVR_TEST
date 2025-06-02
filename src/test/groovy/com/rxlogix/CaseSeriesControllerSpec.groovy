package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.*
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ExecutedCaseSeries, User, SuperQuery])
class CaseSeriesControllerSpec extends Specification implements DataTest, ControllerUnitTest<CaseSeriesController>{

    def setup() {
        ExecutedCaseSeries.metaClass.static.fetchTemporaryCaseSeriesFor = { CaseSeries cs ->
            return ExecutedCaseSeries.where{ seriesName == cs.seriesName}
        }
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference, ReportField, SuperQuery, ParameterValue, CaseSeries, QueryValueList, CaseSeriesDateRangeInformation, Tag, CaseDeliveryOption, EmailConfiguration, ExecutionStatus, ExecutedCaseSeries, ReportResult, ExecutedCaseSeriesDateRangeInformation, ExecutedCaseDeliveryOption, DateRangeType, ExecutedTemplateQuery, ExecutedConfiguration
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

    void "test index"(){
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "test create"(){
        given:
        User adminUser=makeAdminUser()
        def mockUserService=Mock(UserService)
        mockUserService.getUser() >> {adminUser}
        adminUser.metaClass.isConfigurationTemplateCreator = { ->
            return true
        }
        controller.userService=mockUserService
        when:
        controller.create()
        then:
        response.status==200
    }

    void "test preview when query is null"(){
        given:
        SuperQuery.metaClass.static.get = {null}
        when:
        controller.preview(1L)
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test preview when query is not null"(){
        given:
        User adminUser = makeAdminUser()
        def query=new SuperQuery(name: "rx")
        SuperQuery.metaClass.static.get = {Long id -> query}
        def mockUserService=Mock(UserService)
        mockUserService.currentUser >>{adminUser}
        controller.userService=mockUserService
        when:
        controller.preview(1L)
        then:
        response.status==200
    }

    void "test disable not found"(){
        when:
        Tenants.withId(1) {
            controller.disable()
        }
        then:
        response.status== 302
        response.redirectUrl == '/caseSeries/index'
    }

    void "test disable not editable"(){
        given:
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:makeNormalUser("normalUser",[]),seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        Tenants.withId(1) {
            controller.disable()
        }
        then:
        response.status==302
        response.redirectUrl == '/caseSeries/index'
    }

    void "test assignParameterValuesToGlobalQuery"(){
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CaseSeries caseSeries = new CaseSeries(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])])
        caseSeries.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        when:
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        controller.assignParameterValuesToGlobalQuery(caseSeries,false)
        then:
        caseSeries.globalQueryValueLists[0].parameterValues[0].reportField.name == "report"
    }

    void "test setAttributeTags"(){
        CaseSeries caseSeries = new CaseSeries()
        caseSeries.save(failOnError:true,validate:false)
        Tag tag = new Tag(name: "oldTag")
        tag.save(failOnError:true,validate:false,flush:true)
        when:
        params.tags = ["oldTag","newTag"]
        controller.setAttributeTags(caseSeries)
        then:
        caseSeries.tags.size() == 2
    }

    void "test bindAsOfVersionDate"(){
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('bindAsOfVersionDate', [caseSeries, "20-Mar-2016 "] as Object[])
        then:
        caseSeries.includeLockedVersion == true
        caseSeries.asOfVersionDate != null
    }

    void "test bindAsOfVersionDate null"(){
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('bindAsOfVersionDate', [caseSeries, "20-Mar-2016 "] as Object[])
        then:
        caseSeries.includeLockedVersion == false
        caseSeries.asOfVersionDate == null
    }

    void "test bindSharedWith isUpdate false"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption())
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('bindSharedWith', [caseSeries, ["UserGroup_${userGroup.id}","User_${normalUser.id}"], false] as Object[])
        then:
        caseSeries.deliveryOption.sharedWith == [normalUser]
        caseSeries.deliveryOption.sharedWithGroup == [userGroup]
    }

    void "test bindSharedWith isUpdate true"(){
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(sharedWith: [normalUser],sharedWithGroup: [userGroup]))
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> []}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> []}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('bindSharedWith', [caseSeries, ["UserGroup_${userGroup.id}","User_${normalUser.id}"], true] as Object[])
        then:
        caseSeries.deliveryOption.sharedWith == [normalUser]
        caseSeries.deliveryOption.sharedWithGroup == [userGroup]
    }

    void "test bindEmailConfiguration update"(){
        boolean run = false
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        emailConfiguration.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(emailConfiguration: emailConfiguration)
        caseSeries.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.invokeMethod('bindEmailConfiguration', [caseSeries, [subject: "new_email", body: "new_body"]] as Object[])
        then:
        run == true
        caseSeries.emailConfiguration.subject == "new_email"
        caseSeries.emailConfiguration.body == "new_body"
    }

    void "test bindEmailConfiguration save"(){
        boolean run = false
        CaseSeries caseSeries = new CaseSeries()
        caseSeries.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.invokeMethod('bindEmailConfiguration', [caseSeries, [subject: "new_email", body: "new_body"]] as Object[])
        then:
        run == true
        caseSeries.emailConfiguration.subject == "new_email"
        caseSeries.emailConfiguration.body == "new_body"
    }

    void "test bindEmailConfiguration delete"(){
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        emailConfiguration.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(emailConfiguration: emailConfiguration)
        caseSeries.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.delete(0..1){theInstance ->
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.bindEmailConfiguration(caseSeries,[:])
        then:
        caseSeries.emailConfiguration == null
    }

    void "test clearProperties"(){
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]))
        caseSeries.save(failOnError:true,validate:false)
        when:
        controller.clearProperties(caseSeries)
        then:
        caseSeries.deliveryOption.emailToUsers.size() == 0
        caseSeries.deliveryOption.attachmentFormats.size() == 0
    }

    void "test saveAndRun success"(){
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
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> normalUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.save(0..1){caseSeriesInstance, Map saveParams = null ->
            run++
            return caseSeriesInstance
        }
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance ->
            run++
            theInstance.save(failOnError: true,validate:false)
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        params.numExecutions = 0
        params.owner = normalUser
        params.seriesName = "seriesName"
        params.deliveryOption = new CaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser])
        params.tenantId = 1L
        Tenants.withId(1) {
            controller.saveAndRun()
        }
        then:
        run == 2
        flash.message == 'app.caseSeries.generation.progress'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test saveAndRun validation exception"(){
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
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> normalUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.save(0..1){caseSeriesInstance, Map saveParams = null ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        params.numExecutions = 0
        params.owner = normalUser
        params.seriesName = "seriesName"
        params.deliveryOption = new CaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser])
        params.tenantId = 1L
        Tenants.withId(1) {
            controller.saveAndRun()
        }
        then:
        view == '/caseSeries/create'
    }

    void "test saveAndRun exception"(){
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
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> normalUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.save(0..1){caseSeriesInstance, Map saveParams = null ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        params.numExecutions = 0
        params.owner = normalUser
        params.seriesName = "seriesName"
        params.deliveryOption = new CaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser])
        params.tenantId = 1L
        Tenants.withId(1) {
            controller.saveAndRun()
        }
        then:
        flash.error == 'app.label.caseSeries.save.exception'
        view == '/caseSeries/create'
    }

    void "test saveAndRun request method GET"(){
        when:
        request.method = 'GET'
        Tenants.withId(1) {
            controller.saveAndRun()
        }
        then:
        flash.error == 'default.not.saved.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test updateAndRun success"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:normalUser,seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.isAnyGranted(0..1) { String role -> true }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.saveOrUpdate(0..1){caseSeriesInstance, Map saveParams = null ->
            run++
            return caseSeriesInstance
        }
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance ->
            run++
            theInstance.save(failOnError: true,validate:false)
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        Tenants.withId(1) {
            controller.updateAndRun(caseSeries.id)
        }
        then:
        run == 2
        flash.message == 'app.caseSeries.generation.progress'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test updateAndRun validation exception"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:normalUser,seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.isAnyGranted(0..1) { String role -> true }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.saveOrUpdate(0..1){caseSeriesInstance, Map saveParams = null ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        Tenants.withId(1) {
            controller.updateAndRun(caseSeries.id)
        }
        then:
        view == '/caseSeries/edit'
    }

    void "test updateAndRun exception"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:normalUser,seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.isAnyGranted(0..1) { String role -> true }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.saveOrUpdate(0..1){caseSeriesInstance, Map saveParams = null ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        Tenants.withId(1) {
            controller.updateAndRun(caseSeries.id)
        }
        then:
        flash.error == 'app.label.caseSeries.save.exception'
        view == '/caseSeries/edit'
    }

    void "test updateAndRun not found"(){
        when:
        request.method = 'POST'
        Tenants.withId(1) {
            controller.updateAndRun(10)
        }
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test updateAndRun not editable"(){
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:makeNormalUser("normalUser",[]),seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        request.method = 'POST'
        Tenants.withId(1) {
            controller.updateAndRun(caseSeries.id)
        }
        then:
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/caseSeries/index'
    }

    void "test updateAndRun already executing"(){
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:makeNormalUser("normalUser",[]),seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: true)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        request.method = 'POST'
        Tenants.withId(1) {
            controller.updateAndRun(caseSeries.id)
        }
        then:
        flash.warn == 'app.caseseries.run.exists'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test runNow success"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:normalUser,seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(2){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(1){ obj -> new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveOrUpdate(1){caseSeriesInstance, Map saveParams = null ->
            run++
            return caseSeriesInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        Tenants.withId(1) {
            controller.runNow(caseSeries.id)
        }
        then:
        run == 1
        flash.message == 'app.caseSeries.generation.progress'
        response.redirectUrl == '/caseSeries/index'
        !!caseSeries.nextRunDate
    }

    void "test runNow validation exception"(){
        given:
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(seriesName: null)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(2){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(1){ obj -> new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveOrUpdate(1){ caseSeriesInstance ->
            throw new ValidationException("message",caseSeriesInstance.errors)
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        Tenants.withId(1) {
            controller.runNow(caseSeries.id)
        }
        then:
        view == '/caseSeries/edit'
    }

    void "test runNow exception"(){
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:normalUser,seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(2){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(1){ obj -> new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveOrUpdate(1){caseSeriesInstance, Map saveParams = null ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.runNow(caseSeries.id)
        then:
        flash.error == 'app.label.caseSeries.save.exception'
        view == '/caseSeries/edit'
    }

    void "test runNow not found"(){
        when:
        Tenants.withId(1) {
            controller.runNow(10)
        }
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test runNow not editable"(){
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:makeNormalUser("normalUser",[]),seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        Tenants.withId(1) {
            controller.runNow(caseSeries.id)
        }
        then:
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/caseSeries/index'
    }

    void "test runNow already executing"(){
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:makeNormalUser("normalUser",[]),seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: true)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        Tenants.withId(1) {
            controller.runNow(caseSeries.id)
        }
        then:
        flash.warn == 'app.caseseries.run.exists'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test updatePreview success"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "series",numExecutions: 1,owner: normalUser)
        executedCaseSeries.save(failOnError:true,validate:false)
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.getCurrentUser(0..2){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.updateDetailsFrom(0..1){CaseSeries caseSeries, ExecutedCaseSeries executedCaseSeriesInstance ->
            run++
        }
        mockCaseSeriesService.demand.setOwnerAndNameForPreview(0..1){ExecutedCaseSeries caseSeriesInstance ->
            run++
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.saveOrUpdate(0..1){caseSeriesInstance, Map saveParams = null ->
            run++
            return caseSeriesInstance
        }
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance ->
            run++
            theInstance.save(failOnError: true,validate:false)
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        ExecutedCaseSeries.metaClass.static.getByOriginalQueryId = {Long queryId, User owner -> return executedCaseSeries}
        when:
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        params.numExecutions = 0
        params.owner = normalUser
        params.seriesName = "seriesName"
        Tenants.withId(1) {
            controller.updatePreview()
        }
        then:
        run == 5
        if(flash.message) {
            flash.message == 'app.preview.generation'
            response.redirectUrl == '/query/index'
        }
        if(flash.error)
            flash.error == 'app.label.caseSeries.save.exception'
        ExecutionStatus.count() == 1
    }

    void "test updatePreview success new instance"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.getCurrentUser(0..2){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.updateDetailsFrom(0..1){CaseSeries caseSeries, ExecutedCaseSeries executedCaseSeriesInstance ->
            run++
        }
        mockCaseSeriesService.demand.setOwnerAndNameForPreview(0..1){ExecutedCaseSeries caseSeriesInstance ->
            run++
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.saveOrUpdate(0..1){caseSeriesInstance, Map saveParams = null ->
            run++
            return caseSeriesInstance
        }
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance ->
            run++
            theInstance.save(failOnError: true,validate:false)
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        ExecutedCaseSeries.metaClass.static.getByOriginalQueryId = {Long queryId, User owner -> new Object(){
            ExecutedCaseSeries get(){
                return null
            }
        }
        }
        when:
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        params.numExecutions = 0
        params.owner = normalUser
        params.seriesName = "seriesName"
        Tenants.withId(1) {
            controller.updatePreview()
        }
        then:
        run == 5
        if(flash.message) {
            flash.message == 'app.preview.generation'
            response.redirectUrl == '/query/index'
        }
        if(flash.error)
            flash.error == 'app.label.caseSeries.save.exception'
        ExecutionStatus.count() == 1
    }

    void "test updatePreview validation exception"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: null,numExecutions: 1,owner: normalUser)
        executedCaseSeries.save(failOnError:true,validate:false)
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.getCurrentUser(0..2){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.updateDetailsFrom(0..1){CaseSeries caseSeries, ExecutedCaseSeries executedCaseSeriesInstance ->
            run++
        }
        mockCaseSeriesService.demand.setOwnerAndNameForPreview(0..1){ExecutedCaseSeries caseSeriesInstance ->
            run++
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.saveOrUpdate(0..1){caseSeriesInstance, Map saveParams = null ->
            if (!caseSeriesInstance.validate(['seriesName'])) {
                throw new ValidationException("message",(caseSeriesInstance as BaseCaseSeries).errors)
            }
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        ExecutedCaseSeries.metaClass.static.getByOriginalQueryId = {Long queryId, User owner -> return executedCaseSeries}
        when:
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        params.numExecutions = 0
        params.owner = normalUser
        params.seriesName = "seriesName"
        Tenants.withId(1) {
            controller.updatePreview()
        }
        then:
        view == '/caseSeries/preview'
    }

    void "test updatePreview exception"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: null,numExecutions: 1,owner: normalUser)
        executedCaseSeries.save(failOnError:true,validate:false)
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.getCurrentUser(0..2){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.updateDetailsFrom(0..1){CaseSeries caseSeries, ExecutedCaseSeries executedCaseSeriesInstance ->
            run++
        }
        mockCaseSeriesService.demand.setOwnerAndNameForPreview(0..1){ExecutedCaseSeries caseSeriesInstance ->
            run++
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.saveOrUpdate(0..1){caseSeriesInstance, Map saveParams = null ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        ExecutedCaseSeries.metaClass.static.getByOriginalQueryId = {Long queryId, User owner -> return executedCaseSeries}
        when:
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        params.numExecutions = 0
        params.owner = normalUser
        params.seriesName = "seriesName"
        Tenants.withId(1) {
            controller.updatePreview()
        }
        then:
        flash.error == 'app.label.caseSeries.save.exception'
        view == '/caseSeries/preview'
    }

    void "test updatePreview already executing"(){
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "series",numExecutions: 1,owner: normalUser,executing: true)
        executedCaseSeries.save(failOnError:true,validate:false)
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.getCurrentUser(0..2){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        ExecutedCaseSeries.metaClass.static.getByOriginalQueryId = {Long queryId, User owner -> new Object(){
            ExecutedCaseSeries get(){
                return executedCaseSeries
            }
        }
        }
        when:
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        params.numExecutions = 0
        params.owner = normalUser
        params.seriesName = "seriesName"
        Tenants.withId(1) {
            controller.updatePreview()
        }
        then:
        flash.warn == 'app.query.preview.run.exists'
        response.redirectUrl == '/query/index'
    }

    void "test previewCrosstabCases success"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        ReportResult reportResult = new ReportResult(data: new ReportResultData(crossTabHeader: '[{"ROW_1":"test1"},{"column":"test2"}]'),drillDownSource: new ExecutedTemplateQuery(executedConfiguration: new ExecutedConfiguration() ,executedTemplate: new ReportTemplate(name: "test",description: "description")))
        reportResult.save(failOnError:true,validate:false,flush:true)
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "1",numExecutions: 1,owner: normalUser)
        executedCaseSeries.save(failOnError:true,validate:false)
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.getOutputJSONROW(1){ReportResultData resultData, int rowId ->
            run++
            return [ROW_1:'test']
        }
        mockReportService.demand.getCrosstabCaseIds(1){Map<String, ?> row, String columnName ->
            run++
            return [new Tuple2<String,String>("notEmpty","Tuple")]
        }
        controller.reportService = mockReportService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.updateDetailsFrom(1){CaseSeries caseSeries, ExecutedCaseSeries executedCaseSeriesInstance ->
            executedCaseSeriesInstance.description = caseSeries.description
            run++
        }
        mockCaseSeriesService.demand.isDrillDownToCaseList(1){ReportResult reportResultInstance ->
            run++
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveOrUpdate(1){caseSeriesInstance, Map saveParams = null ->
            run++
            return caseSeriesInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.generateCaseSeriesByCaseCommandList(1){ExecutedCaseSeries executedCaseSeriesInstance, List<Tuple2<String, String>> caseIds, int count = -1, boolean refreshCases = false, boolean isDrillDownToCaseList = false->
            run++
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params.rowId = "2"
        params.columnName = "column"
        Tenants.withId(1) {
            controller.previewCrosstabCases(reportResult)
        }
        then:
        run == 6
        response.redirectUrl == '/caseList/index?cid=&detailed=true&filePostfix=2_column&parentId=1'
    }


    void "test previewCrosstabCases validation exception"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        ReportResult reportResult = new ReportResult(data: new ReportResultData(crossTabHeader: '[{"ROW_1":"test1"},{"column":"test2"}]'),drillDownSource: new ExecutedTemplateQuery(executedConfiguration: new ExecutedConfiguration() ,executedTemplate: new ReportTemplate(name: "test",description: "description")))
        reportResult.save(failOnError:true,validate:false,flush:true)
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "1",numExecutions: 1,owner: normalUser)
        executedCaseSeries.save(failOnError:true,validate:false)
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.getOutputJSONROW(1){ReportResultData resultData, int rowId ->
            run++
            return [ROW_1:'test']
        }
        mockReportService.demand.getCrosstabCaseIds(1){Map<String, ?> row, String columnName ->
            run++
            return [new Tuple2<String,String>("notEmpty","Tuple")]
        }
        controller.reportService = mockReportService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.updateDetailsFrom(0..1){CaseSeries caseSeries, ExecutedCaseSeries executedCaseSeriesInstance ->
            throw new ValidationException("message",caseSeries.errors)
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        when:
        params.rowId = "2"
        params.columnName = "column"
        Tenants.withId(1) {
            controller.previewCrosstabCases(reportResult)
        }
        then:
        run == 2
        executedCaseSeries.description == null
        response.redirectUrl == '/caseList/index?cid=&detailed=true&filePostfix=2_column&parentId=1'

    }


    void "test previewCrosstabCases exception"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        ReportResult finalReport = new ReportResult(data: new ReportResultData(crossTabHeader: '[{"ROW_1":"test4"},{"column":"test4"}]'))
        ReportResult draftReport= new ReportResult(data: new ReportResultData(crossTabHeader: '[{"ROW_1":"test5"},{"column":"test5"}]'))
        Configuration config = new Configuration(reportName: "test report")
        ReportResult reportResult = new ReportResult(data: new ReportResultData(crossTabHeader: '[{"ROW_1":"test1"},{"column":"test2"}]'),drillDownSource: new ExecutedTemplateQuery(executedConfiguration: new ExecutedConfiguration(),executedTemplate: new ReportTemplate(name: "test",description: "description")))
        reportResult.save(failOnError:true,validate:false,flush:true)
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "1",numExecutions: 1,owner: normalUser)
        executedCaseSeries.save(failOnError:true,validate:false)
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.getOutputJSONROW(1){ReportResultData resultData, int rowId ->
            run++
            return [ROW_1:'test']
        }
        mockReportService.demand.getCrosstabCaseIds(1){Map<String, ?> row, String columnName ->
            run++
            return [new Tuple2<String,String>("notEmpty","Tuple")]
        }
        controller.reportService = mockReportService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.updateDetailsFrom(0..1){CaseSeries caseSeries, ExecutedCaseSeries executedCaseSeriesInstance ->
            throw new Exception()
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        ExecutedTemplateQuery.metaClass.static.findAllByFinalReportResultOrDraftReportResult = { ReportResult r1, ReportResult r2 ->
            return [new ExecutedTemplateQuery(executedConfiguration: config, executedTemplate: new ReportTemplate(name: "test",description: "description"))]
        }
        when:
        params.rowId = "2"
        params.columnName = "column"
        Tenants.withId(1) {
            controller.previewCrosstabCases(reportResult)
        }
        then:
        run == 2
        response.redirectUrl == '/caseList/index?cid=&detailed=true&filePostfix=2_column&parentId=1'
        flash.error == 'app.label.caseSeries.save.exception'
    }

    void "test delete success"(){
        boolean  run = false
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:makeNormalUser("normalUser",[]),seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
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
        request.method = 'POST'
        controller.delete(caseSeries.id)
        then:
        run == true
        flash.message == 'default.deleted.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test delete validation exception"(){
        boolean  run = false
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:makeNormalUser("normalUser",[]),seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        controller.delete(caseSeries.id)
        then:
        run == false
        flash.error == "Unable to delete case series"
        response.redirectUrl == '/caseSeries/index'
    }

    void "test delete not editable"(){
        boolean  run = false
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:makeNormalUser("normalUser",[]),seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        request.method = 'POST'
        Tenants.withId(1) {
            controller.delete(caseSeries.id)
        }
        then:
        run == false
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/caseSeries/index'
    }

    void "test delete not found"(){
        when:
        request.method = 'POST'
        Tenants.withId(1) {
            controller.delete(10)
        }
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test copy success"(){
        boolean  run = false
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:makeNormalUser("normalUser",[]),seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.copyCaseSeries(0..1){CaseSeries originalCaseSeries ->
            run = true
            return caseSeries
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        when:
        controller.copy(caseSeries)
        then:
        run == true
        flash.message == "app.copy.success"
        response.redirectUrl == '/caseSeries/show/1'
    }

    void "test copy not editable"(){
        boolean  run = false
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:makeNormalUser("normalUser",[]),seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        Tenants.withId(2) {
            controller.copy(caseSeries)
        }
        then:
        run == false
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/caseSeries/index'
    }

    void "test copy not found"(){
        when:
        controller.copy(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test favorite"(){
        boolean run = false
        CaseSeries caseSeries = new CaseSeries()
        caseSeries.save(failOnError:true,validate:false)
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.setFavorite(0..1){ BaseCaseSeries caseSeriesInstance, Boolean isFavorite ->
            run = true
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        when:
        params.id = caseSeries.id
        controller.favorite()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
    }

    void "test favorite null"(){
        when:
        controller.favorite()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == 'default.not.found.message'
    }

    void "test favorite exception"(){
        boolean run = false
        CaseSeries caseSeries = new CaseSeries()
        caseSeries.save(failOnError:true,validate:false)
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.setFavorite(0..1){ BaseCaseSeries caseSeriesInstance, Boolean isFavorite ->
            throw new Exception()
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        when:
        params.id = caseSeries.id
        controller.favorite()
        then:
        run == false
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == 'default.server.error.message'
    }

    void "test save success"(){
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
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> normalUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.save(0..1){caseSeriesInstance, Map saveParams = null ->
            run++
            return caseSeriesInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        params.numExecutions = 0
        params.owner = normalUser
        params.seriesName = "seriesName"
        params.deliveryOption = new CaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser])
        params.tenantId = 1L
        Tenants.withId(1) {
            controller.save()
        }
        then:
        run == 2
        flash.message == 'default.created.message'
        response.redirectUrl == '/caseSeries/show'
    }

    void "test save validation exception"(){
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
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> normalUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.isAnyGranted(0..1) { String role -> false }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.save(0..1){caseSeriesInstance, Map saveParams = null ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        params.numExecutions = 0
        params.owner = normalUser
        params.seriesName = "seriesName"
        params.deliveryOption = new CaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser])
        params.tenantId = 1L
        Tenants.withId(1) {
            controller.save()
        }
        then:
        view == '/caseSeries/create'
    }

    void "test save exception"(){
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
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> normalUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.isAnyGranted(0..1) { String role -> false }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.save(0..1){caseSeriesInstance, Map saveParams = null ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        params.numExecutions = 0
        params.owner = normalUser
        params.seriesName = "seriesName"
        params.deliveryOption = new CaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF],sharedWith: [normalUser])
        params.tenantId = 1L
        Tenants.withId(1) {
            controller.save()
        }
        then:
        flash.error == 'app.label.caseSeries.save.exception'
        view == '/caseSeries/create'
    }

    void "test save request method GET"(){
        when:
        request.method = 'GET'
        controller.save()
        then:
        flash.error == 'default.not.saved.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test update success"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:normalUser,seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.isAnyGranted(0..1) { String role -> true }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..2){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        CaseSeries.metaClass.static.lock = {Serializable serializable -> return CaseSeries.get(serializable)}
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        Tenants.withId(1) {
            controller.update(caseSeries.id)
        }
        then:
        run == 2
        flash.message == 'default.updated.message'
        response.redirectUrl == '/caseSeries/show/1'
    }

    void "test update validation exception"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:normalUser,seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.isAnyGranted(0..1) { String role -> true }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.update(0..1){theInstance ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        CaseSeries.metaClass.static.lock = {Serializable serializable -> return CaseSeries.get(serializable)}
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        Tenants.withId(1) {
            controller.update(caseSeries.id)
        }
        then:
        view == '/caseSeries/edit'
    }

    void "test update exception"(){
        int run = 0
        User normalUser = makeNormalUser("normalUser",[])
        User adminUser = makeAdminUser()
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:normalUser,seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
            Integer getParameterSize(){
                return 1
            }
        }
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..3){-> adminUser}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.isAnyGranted(0..1) { String role -> true }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.update(0..1){theInstance ->
            throw new Exception("Unknown exception")
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        CaseSeries.metaClass.static.lock = {Serializable serializable -> return CaseSeries.get(serializable)}
        when:
        request.method = 'POST'
        params.tags = ["oldTag","newTag"]
        params.caseSeriesDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.asOfVersionDate = "20-Mar-2016 "
        Tenants.withId(1) {
            controller.update(caseSeries.id)
        }
        then:
        flash.error == 'app.label.caseSeries.save.exception'
        view == '/caseSeries/edit'
    }

    void "test update not found"(){
        given:
        CaseSeries.metaClass.static.lock = {Serializable serializable -> return CaseSeries.get(serializable)}
        when:
        request.method = 'POST'
        Tenants.withId(1) {
            controller.update(10)
        }
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test update not editable"(){
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),owner:makeNormalUser("normalUser",[]),seriesName: "seriename",tenantId: 1,numExecutions: 1,executing: false)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.isAnyGranted(0..1) { String role -> false }
        controller.userService = mockUserService.proxyInstance()
        CaseSeries.metaClass.static.lock = {Serializable serializable -> return CaseSeries.get(serializable)}
        when:
        request.method = 'POST'
        Tenants.withId(1) {
            controller.update(caseSeries.id)
        }
        then:
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/caseSeries/index'
    }

//    void "test preview success"(){
//        User normalUser = makeNormalUser("user",[])
//        SuperQuery superQuery = new SuperQuery()
//        superQuery.save(failOnError:true,validate:false,flush:true)
//        def mockUserService = new MockFor(UserService)
//        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
//        controller.userService = mockUserService.proxyInstance()
//        when:
//        controller.preview(superQuery.id)
//        then:
//        view == '/caseSeries/preview'
//    }
//
//    void "test preview null"(){
//        when:
//        controller.preview(10)
//        then:
//        flash.error == 'app.configuration.query.notFound'
//        view == '/query/index'
//    }

    void "test edit success"(){
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(executing: false,seriesName: "series", tenantId: 1L)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        adminUser.metaClass.isConfigurationTemplateCreator = { ->
            return true
        }
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        mockUserService.demand.getUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.correctSchedulerJSONForCurrentDate(1){ json, date->
            return json
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        Tenants.withId(1) {
            controller.edit(caseSeries.id)
        }
        then:
        view == '/caseSeries/edit'
    }

    void "test edit not found"(){
        when:
        Tenants.withId(1) {
            controller.edit(10)
        }
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test edit not editable"(){
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(executing: false,seriesName: "series", tenantId: 1L)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        Tenants.withId(2) {
            controller.edit(caseSeries.id)
        }
        then:
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/caseSeries/index'
    }

    void "test edit already running"(){
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(executing: true,seriesName: "series", tenantId: 1L)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        mockUserService.demand.getUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        Tenants.withId(1) {
            controller.edit(caseSeries.id)
        }
        then:
        flash.warn == "app.caseSeries.running.fail"
        response.redirectUrl == '/caseSeries/index'
    }

    void "test show success"(){
        boolean  run = false
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(executing: false,seriesName: "series")
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.debugGlobalQuerySQL(0..1){def configuration->
            run = true
            return [:]
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params.viewSql = "true"
        controller.show(caseSeries.id)
        then:
        run == true
        view == '/caseSeries/show'
    }

    void "test show not found"(){
        when:
        controller.show(10)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test show not editable"(){
        User normalUser = makeNormalUser("user",[])
        CaseSeries caseSeries = new CaseSeries(executing: false,seriesName: "series",tenantId: 1L)
        caseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        Tenants.withId(2) {
            controller.show(caseSeries.id)
        }
        then:
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/caseSeries/index'
    }

    void "test disable executing"() {
        given:
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"], attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF]), owner: makeNormalUser("normalUser", []), seriesName: "seriename", tenantId: 1, numExecutions: 1, executing: true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = mockUserService.proxyInstance()
        CaseSeries.metaClass.static.get = { Long id -> caseSeries }
        def mockCaseSeriesService = Mock(CaseSeriesService)
        mockCaseSeriesService.getScheduledDateJsonAfterDisable(_) >> {}
        controller.caseSeriesService = mockCaseSeriesService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> { return caseSeries }
        controller.CRUDService = mockCRUDService
        when:
        params.id = 2L
        controller.disable()
        then:
        response.status == 200
    }

    void "test disable not executing success"() {
        given:
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"], attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF]), owner: makeNormalUser("normalUser", []), seriesName: "seriename", tenantId: 1, numExecutions: 1, executing: false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = mockUserService.proxyInstance()
        CaseSeries.metaClass.static.get = { Long id -> caseSeries }
        def mockCaseSeriesService = Mock(CaseSeriesService)
        mockCaseSeriesService.getScheduledDateJsonAfterDisable(_) >> {}
        controller.caseSeriesService = mockCaseSeriesService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> { return caseSeries }
        controller.CRUDService = mockCRUDService
        when:
        params.id = 2L
        controller.disable()
        then:
        response.status == 302
        response.redirectedUrl == "/caseSeries/show"
    }

    void "test disable not executing validation exception"() {
        given:
        User adminUser = makeAdminUser()
        CaseSeries caseSeries = new CaseSeries(deliveryOption: new CaseDeliveryOption(emailToUsers: ["abc@gmail.com"], attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF]), owner: makeNormalUser("normalUser", []), seriesName: "seriename", tenantId: 1, numExecutions: 1, executing: false)
        caseSeries.save(failOnError: true, validate: false)
        CaseSeries.metaClass.static.get = { Long id -> caseSeries }
        def mockCaseSeriesService = Mock(CaseSeriesService)
        mockCaseSeriesService.getScheduledDateJsonAfterDisable(_) >> {}
        controller.caseSeriesService = mockCaseSeriesService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(_) >> { throw new ValidationException("Validation Exception", caseSeries.errors) }
        controller.CRUDService = mockCRUDService
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{adminUser}
        adminUser.preference.locale=new Locale("en")
        controller.userService=mockUserService
        mockUserService.getUser()>>{adminUser}
        adminUser.metaClass.isConfigurationTemplateCreator={-> true}
        when:
        params.id = 2L
        controller.disable()
        then:
        response.status == 200
    }
}
