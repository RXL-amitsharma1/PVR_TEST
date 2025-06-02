package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.gorm.multitenancy.Tenants
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.springframework.http.HttpMethod
import spock.lang.Ignore

@Integration
@Rollback
class CaseSeriesControllerIntegrationSpec extends BaseControllerIntegrationSpec {

    String controllerName = "caseSeries"

    static private final Integer DEFAULT_TENANT_ID = 1

    CaseSeriesController controller

    void setup() {
        controller = autowire(CaseSeriesController)
    }

    void "Index action should render the index view"() {
        when: "Call the index action"
        controller.index()

        then: "It renders the index view."
        controller.modelAndView.viewName == "/caseSeries/index"
    }

    void "Create action should render create view"() {
        setup:
        def userList = [new User()]
        def emailList = [new HashMap<>()]
        controller.userService = [getAllowedSharedWithUsersForCurrentUser: { userList },
                                  getAllowedSharedWithGroupsForCurrentUser: { userList },
                                  getAllEmails: { emailList }, getUser: {
            [isConfigurationTemplateCreator: { true }]
        }]

        when: "Call the create action"
        controller.create()

        then: "It renders the create view"
        controller.modelAndView.viewName == '/caseSeries/create'
    }

    void "Show action should return not found"() {
        given:
        def id = 1;
        controller.request.contentType = FORM_CONTENT_TYPE

        when:
        controller.show(id)

        then:
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    @Ignore
    void "Show action should render show view"() {
        given:
        def id = 1;
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)

        when:
        controller.show(id)

        then:
        controller.modelAndView.viewName == '/caseSeries/show'
        controller.modelAndView.model.containsValue(caseSeriesInstance)
    }

    void "Edit action should return not found"() {
        given:
        def id = 1;
        controller.request.contentType = FORM_CONTENT_TYPE

        when:
        controller.edit(id)

        then:
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "Edit action should redirect to index"() {
        given:
        CRUDService mockCRUDService = Mock(CRUDService)
        UserService mockUserService = Mock(UserService)
        CaseSeriesService mockCaseSeriesService = Mock(CaseSeriesService)
        controller.CRUDService = mockCRUDService
        controller.userService = mockUserService
        controller.caseSeriesService = mockCaseSeriesService
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        caseSeriesInstance.executing = true
        mockUserService.getUser() >> owner
        mockUserService.getCurrentUser() >> owner

        when:
        Tenants.withId(DEFAULT_TENANT_ID){
            controller.edit(caseSeriesInstance.id)
        }

        then:
        controller.response.redirectUrl == "/caseSeries/index"
        controller.flash.warn.contains("cannot be edited")
    }

    void "Edit action should render show view"() {
        given:

        CRUDService mockCRUDService = Mock(CRUDService)
        UserService mockUserService = Mock(UserService)
        CaseSeriesService mockCaseSeriesService = Mock(CaseSeriesService)
        controller.CRUDService = mockCRUDService
        controller.userService = mockUserService
        controller.caseSeriesService = mockCaseSeriesService
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        mockUserService.getUser() >> owner
        mockUserService.getCurrentUser() >> owner

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.edit(caseSeriesInstance.id)
        }

        then:
        controller.modelAndView.viewName == "/caseSeries/edit"
        controller.modelAndView.model.containsValue(caseSeriesInstance)
    }

    void "Preview action should redirect query index action"() {
        given:
        def selectedQueryId = 100000L
        SuperQuery superQuery = null
        controller.request.contentType = FORM_CONTENT_TYPE


        when:
        controller.preview(selectedQueryId)

        then:
        controller.response.redirectedUrl == "/query/index"
    }

    void "Preview action should render preview view"() {
        given:
        def preference = new Preference(createdBy: 'tester', modifiedBy: 'tester', locale: Locale.US)
        def User owner = new User(username: 'test', createdBy: 'tester', modifiedBy: 'tester', preference: preference)
        owner.addToTenants(tenant)
        owner.save(failOnError: true)
        SuperQuery query = new SuperQuery(name: 'super_query', queryType: QueryTypeEnum.SET_BUILDER,
                createdBy: 'tester', modifiedBy: 'tester', owner: owner)
                .save(failOnError: true)

        when:
        controller.preview(query.id)

        then:
        controller.modelAndView.viewName == "/caseSeries/preview"
    }

    void "Delete action returns not found"() {
        setup:
        def caseSeriesId = 10000L
        def caseSeriesInstance = null
        controller.request.contentType = FORM_CONTENT_TYPE
        controller.request.method = HttpMethod.POST

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.delete(caseSeriesId)
        }

        then:
        controller.flash.error
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "Delete action should render show view"() {
        given:
        CRUDService mockCRUDService = Mock(CRUDService)
        UserService mockUserService = Mock(UserService)
        CaseSeriesService mockCaseSeriesService = Mock(CaseSeriesService)
        controller.CRUDService = mockCRUDService
        controller.userService = mockUserService
        controller.caseSeriesService = mockCaseSeriesService
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        mockUserService.getUser() >> owner
        mockUserService.getCurrentUser() >> owner
        controller.request.method = HttpMethod.POST
        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.delete(caseSeriesInstance.id)
        }

        then:
        controller.flash.message
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "Copy action returns not found"() {
        setup:
        def originalCaseSeries = null
        controller.request.contentType = FORM_CONTENT_TYPE

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.copy(originalCaseSeries)
        }

        then:
        controller.flash.error
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "Copy action returns copied CaseSeries"() {
        setup:
        CaseSeriesService mockCaseSeriesService = Mock(CaseSeriesService)
        UserService mockUserService = Mock(UserService)
        controller.caseSeriesService = mockCaseSeriesService
        controller.userService = mockUserService
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        mockCaseSeriesService.copyCaseSeries(caseSeriesInstance) >> caseSeriesInstance
        mockUserService.getCurrentUser() >> owner

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.copy(caseSeriesInstance)
        }

        then:
        controller.flash.message
        controller.response.redirectedUrl == '/caseSeries/show/' + caseSeriesInstance.id
    }

    void "Copy action returns invalid CaseSeries"() {
        setup:
        CaseSeriesService mockCaseSeriesService = Mock(CaseSeriesService)
        UserService mockUserService = Mock(UserService)
        controller.caseSeriesService = mockCaseSeriesService
        controller.userService = mockUserService
        CaseDeliveryOption caseDeliveryOption = new CaseDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF], emailToUsers: ['test@rxlogix.com'])
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        CaseSeries invalidCaseSeriesInstance = new CaseSeries(seriesName: "seriesName1",
                caseSeriesDateRangeInformation: caseSeriesDateRangeInformation,
                createdBy: 'tester', deliveryOption: caseDeliveryOption, modifiedBy: 'tester', owner: owner, tenantId: DEFAULT_TENANT_ID.toLong())
        invalidCaseSeriesInstance.save(failOnError: false)
        invalidCaseSeriesInstance.errors.reject("error.code")
        mockCaseSeriesService.copyCaseSeries(caseSeriesInstance) >> invalidCaseSeriesInstance
        mockUserService.getCurrentUser() >> owner

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.copy(caseSeriesInstance)
        }

        then:
        controller.chainModel.containsValue(invalidCaseSeriesInstance)
        controller.chainModel.containsKey("theInstance")
    }

    void "Save action redirects index when http request method is GET"() {
        setup:
        controller.request.contentType = FORM_CONTENT_TYPE
        controller.request.method = HttpMethod.GET

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.save()
        }

        then:
        controller.flash.error
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "Save action should save and redirect to index action"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        UserService mockUserService = Mock(UserService)
        controller.CRUDService = mockCRUDService
        controller.userService = mockUserService
        controller.request.contentType = FORM_CONTENT_TYPE
        controller.request.method = HttpMethod.POST
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        mockCRUDService.save(caseSeriesInstance) >> caseSeriesInstance
        mockUserService.getCurrentUser() >> owner

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.save()
        }

        then:
        controller.flash.message
        controller.response.redirectedUrl == '/caseSeries/show'
    }

    void "Update action should redirect to index action"() {
        setup:
        Long caseSeriesId = 10011L
        controller.request.contentType = FORM_CONTENT_TYPE
        controller.request.method = HttpMethod.POST

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.update(caseSeriesId)
        }

        then:
        controller.flash.error
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "Update action should update and redirect to index action"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        UserService mockUserService = Mock(UserService)
        controller.CRUDService = mockCRUDService
        controller.userService = mockUserService
        controller.request.method = HttpMethod.POST
        controller.request.contentType = FORM_CONTENT_TYPE
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        mockCRUDService.update(caseSeriesInstance) >> caseSeriesInstance
        mockUserService.getCurrentUser() >> owner

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.update(caseSeriesInstance.id)
        }

        then:
        controller.flash.message
        controller.response.redirectedUrl == '/caseSeries/show/' + caseSeriesInstance.id
    }

    void "SaveAndRun action should redirect to index action"() {
        setup:
        controller.request.method = HttpMethod.GET
        controller.request.contentType = FORM_CONTENT_TYPE

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.saveAndRun()
        }

        then:
        controller.flash.error
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "SaveAndRun action should render to create view"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        UserService mockUserService = Mock(UserService)
        controller.CRUDService = mockCRUDService
        controller.userService = mockUserService
        controller.request.method = HttpMethod.POST
        controller.request.contentType = FORM_CONTENT_TYPE
        controller.params.tenantId = DEFAULT_TENANT_ID.toLong()
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        ExecutionStatus executionStatus = new ExecutionStatus(entityType: ExecutingEntityTypeEnum.EXCECUTED_CASESERIES,
                nextRunDate: new Date(), entityId: 1L, owner: owner, reportName: "reportName", reportVersion: 1L,
                sharedWith: caseSeriesInstance.shareWithUsers, tenantId: DEFAULT_TENANT_ID.toLong()).save(failOnError: true)
        mockCRUDService.save(caseSeriesInstance) >> caseSeriesInstance
        mockCRUDService.instantSaveWithoutAuditLog(executionStatus) >> null
        mockUserService.getCurrentUser() >> owner
        def userList = [new User()]
        mockUserService.getAllowedSharedWithUsersForCurrentUser() >> { userList }
        mockUserService.getAllowedSharedWithGroupsForCurrentUser() >> { userList }


        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.saveAndRun()
        }

        then:
        controller.flash.message
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "UpdateAndRun action should redirect to index action"() {
        setup:
        Long caseSeriesId = 100100L
        controller.request.contentType = FORM_CONTENT_TYPE
        controller.request.method = HttpMethod.POST

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.updateAndRun(caseSeriesId)
        }

        then:
        controller.flash.error
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "UpdateAndRun action should redirect to index action when execution is in progress"() {
        setup:

        CRUDService mockCRUDService = Mock(CRUDService)
        UserService mockUserService = Mock(UserService)
        CaseSeriesService mockCaseSeriesService = Mock(CaseSeriesService)
        controller.CRUDService = mockCRUDService
        controller.userService = mockUserService
        controller.caseSeriesService = mockCaseSeriesService
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        caseSeriesInstance.executing = true
        mockUserService.getUser() >> owner
        mockUserService.getCurrentUser() >> owner
        controller.request.method = HttpMethod.POST

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.updateAndRun(caseSeriesInstance.id)
        }

        then:
        controller.flash.warn
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "UpdateAndRun action should update and redirect to executionStatus list action"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        UserService mockUserService = Mock(UserService)
        controller.CRUDService = mockCRUDService
        controller.userService = mockUserService
        controller.request.method = HttpMethod.POST
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        ExecutionStatus executionStatus = createExecutionStatus(owner).save(failOnError: true)
        executionStatus.templateId = DEFAULT_TENANT_ID.toLong()
        mockCRUDService.saveOrUpdate(caseSeriesInstance) >> caseSeriesInstance
        mockCRUDService.instantSaveWithoutAuditLog(executionStatus) >> null
        mockUserService.getCurrentUser() >> owner

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.updateAndRun(caseSeriesInstance.id)
        }

        then:
        controller.flash.message
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "RunNow action should redirect to index action"() {
        setup:
        Long caseSeriesId = 100101L
        controller.request.contentType = FORM_CONTENT_TYPE

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.runNow(caseSeriesId)
        }

        then:
        controller.flash.error
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    void "RunNow action should redirect to index action when caseSeries executing is in progress"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        UserService mockUserService = Mock(UserService)
        CaseSeriesService mockCaseSeriesService = Mock(CaseSeriesService)
        controller.CRUDService = mockCRUDService
        controller.userService = mockUserService
        controller.caseSeriesService = mockCaseSeriesService
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        caseSeriesInstance.executing = true
        mockUserService.getCurrentUser() >> owner
        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.runNow(caseSeriesInstance.id)
        }

        then:
        controller.flash.warn
        controller.response.redirectedUrl == '/caseSeries/index'
    }

    @Ignore
    void "RunNow action should update and redirect to executionStatus list action"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        UserService mockUserService = Mock(UserService)
        CaseSeriesService mockCaseSeriesService = Mock(CaseSeriesService)
        controller.CRUDService = mockCRUDService
        controller.userService = mockUserService
        controller.caseSeriesService = mockCaseSeriesService
        controller.request.method = HttpMethod.POST
        User owner = createUser()
        CaseSeries caseSeriesInstance = setupCaseSeries(owner)
        ExecutionStatus executionStatus = createExecutionStatus(owner).save(failOnError: true)
        ExecutedCaseSeriesDateRangeInformation executedCaseSeriesDateRangeInformation = new ExecutedCaseSeriesDateRangeInformation()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(caseSeriesInstance)
        executedCaseSeries.executedCaseSeriesDateRangeInformation = executedCaseSeriesDateRangeInformation
        executedCaseSeries.save(failOnError: true)
        mockCRUDService.saveOrUpdate(caseSeriesInstance) >> caseSeriesInstance
        mockCRUDService.instantSaveWithoutAuditLog(executionStatus) >> null
        mockUserService.getCurrentUser() >> owner
        mockCaseSeriesService.updateDetailsFrom(caseSeriesInstance, executedCaseSeries) >> executedCaseSeries
        mockCaseSeriesService.setOwnerAndNameForPreview(executedCaseSeries) >> caseSeriesInstance

        when:
        Tenants.withId(DEFAULT_TENANT_ID) {
            controller.updateAndRun(caseSeriesInstance.id)
        }

        then:
        controller.flash.message
        controller.response.redirectedUrl == '/executionStatus/list'
    }

    private CaseDeliveryOption createCaseDeliveryOption() {
        new CaseDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF], emailToUsers: ['test@rxlogix.com'])
    }

    private ExecutionStatus createExecutionStatus(User owner) {
        new ExecutionStatus(reportVersion: 1L, reportName: 'execRepName', owner: owner, nextRunDate: new Date(), entityType: ExecutingEntityTypeEnum.CASESERIES, entityId: 1L, tenantId: DEFAULT_TENANT_ID.toLong())
    }

    private CaseSeriesDateRangeInformation getCaseSeriesDateRangeInformation() {
        new CaseSeriesDateRangeInformation()
    }

    private CaseSeries createValidCaseSeries(CaseSeriesDateRangeInformation caseSeriesDateRangeInformation, CaseDeliveryOption caseDeliveryOption, User owner) {
        new CaseSeries(seriesName: "seriesName",
                caseSeriesDateRangeInformation: caseSeriesDateRangeInformation,
                createdBy: 'tester', deliveryOption: caseDeliveryOption, modifiedBy: 'tester', owner: owner, tenantId: DEFAULT_TENANT_ID.toLong())
    }

    private User createUser() {
        Preference preference = new Preference(createdBy: 'tester', modifiedBy: 'tester', locale: Locale.US)
        User normalUser = new User(username: 'test', createdBy: 'tester', modifiedBy: 'tester', preference: preference)
        normalUser.addToTenants(tenant)
        return normalUser.save(failOnError: true)
    }

    private CaseSeries setupCaseSeries(User owner) {
        def CaseSeriesDateRangeInformation caseSeriesDateRangeInformation = new CaseSeriesDateRangeInformation()
        def CaseDeliveryOption caseDeliveryOption = new CaseDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF], emailToUsers: ['test@rxlogix.com'])
        def caseSeriesInstance = createValidCaseSeries(caseSeriesDateRangeInformation, caseDeliveryOption, owner).save(failOnError: true)
        caseSeriesInstance
    }


    private Tenant getTenant() {
        def tenant = Tenant.get(DEFAULT_TENANT_ID.toLong())
        if (tenant) {
            return tenant
        }
        tenant = new Tenant(name: 'Default', active: true)
        tenant.id = DEFAULT_TENANT_ID.toLong()
        return tenant.save()
    }

}
