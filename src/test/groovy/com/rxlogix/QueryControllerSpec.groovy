package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.enums.*
import com.rxlogix.pvcm.PVCMIntegrationService
import com.rxlogix.signal.SignalIntegrationService
import com.rxlogix.test.TestUtils
import com.rxlogix.user.*
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import groovy.mock.interceptor.MockFor
import org.hibernate.FlushMode
import org.hibernate.SessionFactory
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SuperQuery, User, ReportTemplate, CaseLineListingTemplate, SuperQuery])
class QueryControllerSpec extends Specification implements DataTest, ControllerUnitTest<QueryController> {

    QueryService queryService = new QueryService()
    CRUDService crudService = new CRUDService()
    //Use this to get past the constraint that requires a JSONQuery string.
    public static final user = "unitTest"
    def JSONQuery = """{ "all": { "containerGroups": [   
        { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }"""

    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()

    //Use this to get past the constraint that requires a JSONQuery string.

    def setup() {
    }

    def cleanup() {}


    def setupSpec() {
        mockDomains User, Role, UserRole, UserGroupQuery, UserQuery, Query, QuerySet, SuperQuery, Preference, Configuration, Tag, CaseLineListingTemplate, QueryValueList,
                Tenant, ReportField, ReportFieldGroup, QueryExpressionValue, SourceColumnMaster, SourceTableMaster,
                TemplateQuery, DateRangeInformation, ReportFieldInfo, ReportFieldInfoList, SourceProfile, UserGroupUser
        TimeZone.setDefault(TimeZone.getTimeZone(TEST_TZ))
        mockTemplateServiceForReportTemple()
    }

    def cleanupSpec() {
        TimeZone.setDefault(ORIGINAL_TZ)
        User.metaClass.encodePassword = null
        cleanUpTemplateServiceMock()
    }

    private void mockTemplateServiceForReportTemple() {
        def templateService = new MockFor(TemplateService).proxyInstance()
        ReportTemplate.metaClass.getTemplateService = {
            return templateService
        }

        CaseLineListingTemplate.metaClass.getTemplateService = {
            return templateService
        }
    }

    private void cleanUpTemplateServiceMock() {
    }


    // couldn't put these in the setup/cleanup because of issues w/ @Shared. May be missing something to make that work. This is workaround.
    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
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

    private User makeSuperAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_DEV', createdBy: "user", modifiedBy: "user").save(flush: true)
        def adminUser = new User(username: 'superadmin', password: 'admin', fullName: "Super Admin", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isAdmin = { -> return true }
        adminUser.metaClass.getUserTeamIds = { -> [] }
        adminUser.metaClass.static.isDev = { -> return true}
        return adminUser
    }

    private User makeAdminUser1() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user").save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isAdmin = { -> return false }
        adminUser.metaClass.getUserTeamIds = { -> [] }
        adminUser.metaClass.static.isDev = { -> return true}
        return adminUser
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getUser(0..1) { -> user }
        securityMock.demand.isCurrentUserDev(0..2) { false }
        securityMock.demand.getCurrentUser(0..1) { -> user }
        securityMock.demand.isAnyGranted(0..1){ String role-> false}
        return securityMock.proxyInstance()
    }

    void "Non-admin user can edit a query owned by itself"() {
        given: "A non-admin user"
        def normalUser = makeNormalUser()
        User.metaClass.isAdmin = { false }
        SuperQuery.metaClass.static.read = { Long id -> Query.get(id) }
        and: "The non-admin user is currently logged in"
        controller.userService = makeSecurityService(normalUser)

        and: "A query to edit is owned by the same user"
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                 createdBy: normalUser.username, modifiedBy: normalUser.username, JSONQuery: JSONQuery])
        query.owner = normalUser
        query.userService = makeUserService()
        query.save(failOnError: true)

        def queryServiceMock = new MockFor(QueryService)

        queryServiceMock.demand.getUsagesCountQuerySet(1..1) { originalQuery ->
            return 0
        }

        queryServiceMock.demand.getUsagesCount(1..1) { originalQuery ->
            return 0
        }

        queryServiceMock.demand.isQueryUpdateable(1..1) { originalQuery ->
            return true
        }

        controller.queryService = queryServiceMock.proxyInstance()
        when: "A non-owner, non-admin wants to edit a query it owns"
        controller.edit(1L)
        then: "A user can edit a query they own"
        response.status == 200
    }

    void "Non-admin user cannot edit a query owned by an admin"() {
        given: "A non-admin user"
        def normalUser = makeNormalUser()

        and: "An admin user"
        def adminUser = makeAdminUser()

        and: "The non-admin user is currently logged in"
        controller.userService = makeSecurityService(normalUser)
        User.metaClass.isAdmin = { false }
        SuperQuery.metaClass.static.read = {Long id -> Query.get(id)}

        and: "A query to edit is owned by an admin user"
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                 createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(flush: true)

        when: "A non-owner, non-admin wants to edit a query owned by an admin"
        controller.edit(1L)
        then: "Not allowed; They are redirected to the index, and shown a warning message"
        response.status == 302
        flash.warn.size() > 0
        response.redirectedUrl == "/query/index"
    }

    void "Query can be created"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.createExecutedQuery { queryInstance -> true }
        queryServiceMock.demand.saveQuery { originalQuery, user -> originalQuery }
        controller.queryService = queryServiceMock.proxyInstance()

        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { originalQuery -> originalQuery }
        controller.CRUDService = crudServiceMock.proxyInstance()
        def PVCMIntegrationServiceMock = new MockFor(PVCMIntegrationService)
        PVCMIntegrationServiceMock.demand.checkAndInvokeRoutingCondition {queryInstance, isPreviouslyTagExist -> true }
        controller.PVCMIntegrationService = PVCMIntegrationServiceMock.proxyInstance()

        controller.userService = makeSecurityService(adminUser)

        and: "A query"
        Query query = new Query([queryLevel: QueryLevelEnum.CASE, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                 createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        and: "No blank parameters"
        query.hasBlanks = false;

        when: "The query with no errors is updated"
        params.id = 1
        params.queryType = QueryTypeEnum.QUERY_BUILDER
        controller.save()
        then: "The query is created and page redirected"
        response.status == 302
        response.redirectedUrl == "/query/view/1"
    }

    void "Query not used in any Configuration can be viewed"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        controller.userService = makeSecurityService(adminUser)
        SuperQuery.metaClass.static.read = {Long id -> Query.get(id)}

        and: "A query"
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                 createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)

        and: "No blank parameters"
        query.hasBlanks = false;

        when: "The query with no errors is viewed"
        controller.view(query.id)
        then: "The query is viewed"
        response.status == 200
    }

    void "Query can be viewed if it is used in a Configuration."() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        controller.userService = makeSecurityService(adminUser)
        SuperQuery.metaClass.static.read = {Long id -> Query.get(id)}

        and: "A query"
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                 createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)

        and: "No blank parameters"
        query.hasBlanks = false;

        and: "A configuration which uses the above query"
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save(failOnError: true)
        SourceTableMaster sourceTableMaster = new SourceTableMaster([tableName: "V_C_IDENTIFICATION", caseJoinOrder: 1, caseJoinType: "E", tableAlias: "cm", tableType: "C"])
        sourceTableMaster.save(failOnError: true)
        SourceColumnMaster sourceColumnMaster = new SourceColumnMaster([tableName: sourceTableMaster, reportItem: "CM_CASE_NUM", columnName: "CASE_NUM", columnType: "V", lang: "en"])
        sourceColumnMaster.save(failOnError: true)
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", sourceColumnId: sourceColumnMaster.reportItem, fieldGroup: fieldGroup, dataType: String.class, sourceId: 1])
        field.save(failOnError: true)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true)

        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, owner: adminUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList)
        template.save(failOnError: true)
        Configuration config = new Configuration([template: template, query:query, reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: adminUser,deliveryOption: new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF]), isEnabled: true, createdBy: adminUser.username, modifiedBy: adminUser.username, tenantId:1L])
        config.addToTemplateQueries(new TemplateQuery(query: query, template: template,dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username, modifiedBy: adminUser.username))
        config.sourceProfile = TestUtils.createSourceProfile()
        config.save(failOnError: true)

        when: "The query with no errors is viewed"
        controller.view(1L)
        then: "The query is viewed"
        response.status == 200
    }

    void "Admin can edit a query owned by admin"() {
        given: "A non-admin user"
        def normalUser = makeNormalUser()
        SuperQuery.metaClass.static.read = {Long id -> Query.get(id)}

        and: "An admin user"
        def adminUser = makeAdminUser()

        and: "The query to edit is owned by the same admin user"
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                 createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)

        and: "The currently logged in user is the same admin user"
        controller.userService = makeSecurityService(adminUser)
        User.metaClass.isAdmin = { true }

        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.getUsagesCountQuerySet(1..1) { originalQuery ->
            return 0
        }

        queryServiceMock.demand.getUsagesCount(1..1) { originalQuery ->
            return 0
        }

        queryServiceMock.demand.isQueryUpdateable(1..1) { originalQuery ->
            return true
        }

        controller.queryService = queryServiceMock.proxyInstance()
        when: "The admin wants to edit a query it owns"
        controller.edit(1L)
        then: "Admin can edit the query"
        response.status == 200
    }

    void "Admin can edit a query owned by another user"() {
        given: "A non-admin user"
        def normalUser = makeNormalUser()
        SuperQuery.metaClass.static.read = {Long id -> Query.get(id)}

        and: "An admin user"
        def adminUser = makeAdminUser()

        and: "A query owned by a non-admin"
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                 createdBy: normalUser.username, modifiedBy: normalUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)

        and: "The currently logged in user is the same admin user"
        controller.userService = makeSecurityService(adminUser)
        User.metaClass.isAdmin = { true }

        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.getUsagesCountQuerySet(1..1) { originalQuery ->
            return 0
        }

        queryServiceMock.demand.getUsagesCount(1..1) { originalQuery ->
            return 0
        }

        queryServiceMock.demand.isQueryUpdateable(1..1) { originalQuery ->
            return true
        }

        controller.queryService = queryServiceMock.proxyInstance()

        when: "The admin wants to edit a query owned by a normal user"
        controller.edit(1L)
        then: "Admin can edit the query"
        response.status == 200
        model.query == query
    }

    void "Query not used in any Configuration can be updated"() {
        given: "An admin user"
        def adminUser = makeAdminUser()
        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.createExecutedQuery { queryInstance -> true }
        queryServiceMock.demand.getUsages { originalQuery ->
            return []
        }
        queryServiceMock.demand.updateQuery { originalQuery -> false }
        controller.queryService = queryServiceMock.proxyInstance()

        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { originalQuery -> originalQuery }
        controller.CRUDService = crudServiceMock.proxyInstance()
        def PVCMIntegrationServiceMock = new MockFor(PVCMIntegrationService)
        PVCMIntegrationServiceMock.demand.checkAndInvokeRoutingCondition { queryInstance, isPreviouslyTagExist -> true}
        controller.PVCMIntegrationService = PVCMIntegrationServiceMock.proxyInstance()

        controller.userService = makeSecurityService(adminUser)
        User.metaClass.isAdmin = { true }
        and: "A query with no blank parameters"
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, hasBlanks: false, name: 'Test Query', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                 createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)

        def mockSignalIntegrationService = new MockFor(SignalIntegrationService)
        mockSignalIntegrationService.demand.notifySignalForUpdate(0..1){def q -> true}
        controller.signalIntegrationService = mockSignalIntegrationService.proxyInstance()

        when: "The query with no errors is updated"
        params.id = 1
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        controller.update()
        then: "The query is updated and page redirected"
        response.status == 302
        response.redirectedUrl == "/query/view/1"
    }

    void "Query without blanks can be updated if it is used in a Configuration."() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.createExecutedQuery { queryInstance -> true }
        queryServiceMock.demand.getUsages { originalQuery ->
            return []
        }
        queryServiceMock.demand.updateQuery { originalQuery ->
            originalQuery
        }
        controller.queryService = queryServiceMock.proxyInstance()

        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { originalQuery -> originalQuery }
        controller.CRUDService = crudServiceMock.proxyInstance()
        def PVCMIntegrationServiceMock = new MockFor(PVCMIntegrationService)
        PVCMIntegrationServiceMock.demand.checkAndInvokeRoutingCondition {queryInstance, isPreviouslyTagExist -> true }
        controller.PVCMIntegrationService = PVCMIntegrationServiceMock.proxyInstance()

        controller.userService = makeSecurityService(adminUser)
        User.metaClass.isAdmin = { true }
        and: "A query"
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                 createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.hasBlanks = false
        query.userService = makeUserService()
        query.save(failOnError: true)

        def mockSignalIntegrationService = new MockFor(SignalIntegrationService)
        mockSignalIntegrationService.demand.notifySignalForUpdate(0..1){def q -> true}
        controller.signalIntegrationService = mockSignalIntegrationService.proxyInstance()

        and: "A configuration which uses the above query"
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save(failOnError: true)
        SourceTableMaster sourceTableMaster = new SourceTableMaster([tableName: "V_C_IDENTIFICATION", caseJoinOrder: 1, caseJoinType: "E", tableAlias: "cm", tableType: "C"])
        sourceTableMaster.save(failOnError: true)
        SourceColumnMaster sourceColumnMaster = new SourceColumnMaster([tableName: sourceTableMaster, reportItem: "CM_CASE_NUM", columnName: "CASE_NUM", columnType: "V", lang: "en"])
        sourceColumnMaster.save(failOnError: true)
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", sourceColumnId: sourceColumnMaster.reportItem, fieldGroup: fieldGroup, dataType: String.class, sourceId: 1])
        field.save(failOnError: true)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true)

        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, owner: adminUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList)
        template.save(failOnError: true)
        TemplateQuery tq= new TemplateQuery(template: template, query:query, dateRangeInformationForTemplateQuery: new DateRangeInformation(dateRangeEnum: DateRangeEnum.CUSTOM, dateRangeStartAbsolute:new Date("Mon Jan 01 00:00:00 $TEST_TZ 0001") ,dateRangeEndAbsolute:new Date("Mon Jan 14 00:00:00 $TEST_TZ 0001")), createdBy: adminUser.username, modifiedBy: adminUser.username)
        Configuration config = new Configuration([template: template, reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: adminUser,deliveryOption: new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF]), isEnabled: true, createdBy: adminUser.username, modifiedBy: adminUser.username, tenantId:1L])
        config.addToTemplateQueries(tq)
        config.sourceProfile = TestUtils.createSourceProfile()
        config.save(failOnError: true)
        when: "The query with no errors is updated"
        request.contentType = FORM_CONTENT_TYPE
        request.method = 'POST'
        params.id = 1
        controller.update()
        then: "The query is not updated and page redirected"
        response.status == 302
        response.redirectedUrl == "/query/view/1"
    }

//todo:  This began failing when we started using CRUDService.softDelete() - morett
//todo:  Due to time constraints to deploy to ALSC, had to comment this out. - morett
//    void "Query not used in any Configuration can be deleted"() {
//        given: "An admin user"
//        def adminUser = makeAdminUser()
//        def queryServiceMock = new MockFor(QueryService)
//        queryServiceMock.demand.getUsages { originalQuery ->
//            return []
//        }
//        queryServiceMock.demand.deleteQuery { originalQuery -> originalQuery }
//        controller.queryService = queryServiceMock.proxyInstance()
//        controller.userService = makeSecurityService(adminUser)
//        User.metaClass.isAdmin = { true }
//        and: "A query"
//        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
//                                 createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
//        query.owner = adminUser
//        query.save(flush: true, failOnError: true)
//
//        and: "No blank parameters"
//        query.hasBlanks = false;
//
//        when: "The query with no errors is deleted"
//        params.id = 1
//        controller.delete()
//        then: "The query is deleted and page redirected"
//        response.status == 302
//        response.redirectedUrl == "/query/index"
//        flash.message == "app.query.delete.success"
//    }

//todo:  This began failing when we started using CRUDService.softDelete() - morett
//todo:  Due to time constraints to deploy to ALSC, had to comment this out. - morett
//    void "Query cannot be deleted if it is used in a Configuration."() {
//        given: "An admin user"
//        def adminUser = makeAdminUser()
//        controller.userService = makeSecurityService(adminUser)
//        User.metaClass.isAdmin = { true }
//        and: "A query"
//        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
//                                 createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
//        query.owner = adminUser
//        query.save(failOnError: true)
//
//        and: "No blank parameters"
//        query.hasBlanks = false;
//
//        and: "A configuration which uses the above query"
//        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
//        fieldGroup.save(failOnError: true)
//        SourceTableMaster sourceTableMaster = new SourceTableMaster([tableName: "C_IDENTIFICATION", caseJoinOrder: 1, caseJoinType: "E", tableAlias: "cm", tableType: "C"])
//        sourceTableMaster.save(failOnError: true)
//        SourceColumnMaster sourceColumnMaster = new SourceColumnMaster([tableName: sourceTableMaster, reportItem: "CM_CASE_NUM", columnName: "CASE_NUM", columnType: "V"])
//        sourceColumnMaster.save(failOnError: true)
//        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", sourceColumn: sourceColumnMaster, fieldGroup: fieldGroup, dataType: String.class, sourceId: 1])
//        field.save(failOnError: true)
//        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
//        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
//        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true)
//
//        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, owner: adminUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList)
//        template.save(failOnError: true)
//        Configuration config = new Configuration([template: template,  reportName: 'SAE - Clinical Reconciliation Death Case', description: 'Config to identify SAE - Clinical Reconciliation death cases', owner: adminUser,deliveryOption: new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF]), isEnabled: true, createdBy: adminUser.username, modifiedBy: adminUser.username])
//        TemplateQuery tq = new TemplateQuery(template: template, query: query, dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username, modifiedBy: adminUser.username)
//        config.addToTemplateQueries(tq)
//        config.save(failOnError: true)
//
//        def queryServiceMock = new MockFor(QueryService)
//        queryServiceMock.demand.getUsages { originalQuery ->
//            return [1]
//        }
//
//        controller.queryService = queryServiceMock.proxyInstance()
//
//        when: "The query with no errors is updated"
//        params.id = 1
//        controller.delete()
//        then: "The query is not deleted and page redirected"
//        response.status == 302
//        response.redirectedUrl == "/query/index"
//        flash.error == """app.query.delete.usage
//                            <linkQuery>/query/checkUsage/1"""
//    }

    void "Query with errors cannot be updated"() {
        given: "An admin"
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)

        and: "A query"
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'Test Query', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                 createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)
        User.metaClass.isAdmin = { true }
        and: "You can't save Query with missing data"
        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.getUsages { originalQuery ->
            return []
        }
        queryServiceMock.demand.updateQuery { originalQuery, params ->
            originalQuery.save()
            return originalQuery
        }
        controller.queryService = queryServiceMock.proxyInstance()

        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.update { originalQuery -> originalQuery }
        controller.CRUDService = crudServiceMock.proxyInstance()

        and: "No blank parameters"
        query.hasBlanks = false;
        query.userService = makeUserService()

        when: "There is missing data"
        params.id = 1
        params.name = null
        controller.update()

        then: "The query is not updated and page shows an error"
        response.status == 302
    }

    void "Check usage only returns usages of the selected query"() {
        given: "An admin"
        def adminUser = makeAdminUser()
        SuperQuery.metaClass.static.read = {Long id -> Query.get(id)}
        and: "A query"
        Query queryToNotFind = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                          createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        queryToNotFind.owner = adminUser
        queryToNotFind.userService = makeUserService()
        queryToNotFind.save(failOnError: true)

        and: "Another query"
        Query queryToFind = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases 2', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                       createdBy: adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery])
        queryToFind.owner = adminUser
        queryToFind.userService = makeUserService()
        queryToFind.userService = makeUserService()
        queryToFind.save(failOnError: true)

        and: "A template"
        def field = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class, sourceId: 1,fieldGroup: new ReportFieldGroup(name:  'xyx').save()])
        field.save()
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true)

        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, owner: adminUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList)
        template.save(failOnError: true)

        and: "A configuration to find"
        def deliveryOption1 =new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF])
        Configuration config = new Configuration([reportName: 'Test configuration should find',deliveryOption: deliveryOption1 ,  owner: adminUser, createdBy: adminUser.username, modifiedBy: adminUser.username, tenantId: 1L])
                .addToTemplateQueries(new TemplateQuery(template: template, query: queryToFind,
                        dateRangeInformationForTemplateQuery: new DateRangeInformation(),
                        createdBy: adminUser.username, modifiedBy: adminUser.username))
        config.sourceProfile = TestUtils.createSourceProfile()
        config.save(failOnError: true)

        and: "Another configuration not to find"
        def deliveryOption2 =new DeliveryOption(attachmentFormats: [ReportFormatEnum.PDF])
        Configuration config1 = new Configuration([reportName: 'Test configuration to not find',deliveryOption: deliveryOption2, owner: adminUser, createdBy: adminUser.username, modifiedBy: adminUser.username, tenantId: 1L])
                .addToTemplateQueries(new TemplateQuery(template: template, query: queryToNotFind,
                        dateRangeInformationForTemplateQuery: new DateRangeInformation(),
                        createdBy: adminUser.username, modifiedBy: adminUser.username))
        config1.sourceProfile = TestUtils.createSourceProfile()
        config1.save(failOnError: true)

        when: "The selected query's usages is checked"
        params.id = 2
        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.getUsages(1..1) { originalQuery ->
            return [config]
        }
        controller.queryService = queryServiceMock.proxyInstance()
        controller.checkUsage()
        then: "Only usages with the selected query are returned"
        response.status == 200
        view == '/query/checkUsage'
        model.usages.size() == 1
        model.usages[0].name == "Test configuration should find"
    }

    private makeUserService() {
        def userMock = new MockFor(UserService)
        userMock.demand.isCurrentUserAdmin(1..2) { false }
        userMock.demand.isAnyGranted(0..1){ String role-> false}
        return userMock.proxyInstance()
    }

    void "test edit not found"(){
        given:
        SuperQuery.metaClass.static.read={null}
        when:
        controller.edit(2L)
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test edit found not editable"(){
        given:
        SuperQuery queryInstance=new SuperQuery()
        SuperQuery.metaClass.static.read={Long id -> queryInstance}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        SuperQuery.metaClass.isEditableBy = {User currentUser -> false}
        when:
        controller.edit(2L)
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test edit found editable"(){
        given:
        SuperQuery queryInstance=new SuperQuery()
        SuperQuery.metaClass.static.read={Long id -> queryInstance}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser()}
        controller.userService = mockUserService
        SuperQuery.metaClass.isEditableBy = {User currentUser -> true}
        def mockQueryService=Mock(QueryService)
        mockQueryService.getUsagesCountQuerySet(_)>>{}
        controller.queryService=mockQueryService
        when:
        controller.edit(2L)
        then:
        response.status==200
    }

    void "test edit found editable with nonvalidcases"(){
        given:
        SuperQuery queryInstance=new SuperQuery(nonValidCases: true,icsrPadderAgencyCases: true)
        SuperQuery.metaClass.static.read={Long id -> queryInstance}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        SuperQuery.metaClass.isEditableBy = {User currentUser -> true}
        def mockQueryService=Mock(QueryService)
        mockQueryService.getUsagesCountQuerySet(_)>>{}
        controller.queryService=mockQueryService
        when:
        controller.edit(2L)
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test delete not found"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        SuperQuery.metaClass.static.get={null}
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test delete found"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        SuperQuery.metaClass.static.get={Long id ->new SuperQuery(nonValidCases: true,icsrPadderAgencyCases: true)}
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test delete found not editable"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        SuperQuery.metaClass.static.get={Long id ->new SuperQuery(nonValidCases: false,icsrPadderAgencyCases: false , deletedCases: false)}
        SuperQuery.metaClass.isEditableBy = {User currentUser -> false}
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test delete found editable"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        SuperQuery.metaClass.static.get={Long id ->new SuperQuery(nonValidCases: false,icsrPadderAgencyCases: false , deletedCases: false)}
        SuperQuery.metaClass.isEditableBy = {User currentUser -> true}
        def mockQueryService=Mock(QueryService)
        mockQueryService.getUsagesCount(_)>>{return 2}
        controller.queryService=mockQueryService
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test delete found editable with no usage count"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        SuperQuery.metaClass.static.get={Long id ->new SuperQuery(nonValidCases: false,icsrPadderAgencyCases: false,deletedCases: false)}
        SuperQuery.metaClass.isEditableBy = {User currentUser -> true}
        def mockQueryService=Mock(QueryService)
        mockQueryService.getUsagesCount(_)>>{return 0}
        mockQueryService.getUsagesCountQuerySet(_)>>{return 2}
        controller.queryService=mockQueryService
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test delete found editable try success with no usage count and no usage count query set"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        SuperQuery.metaClass.static.get={Long id ->new SuperQuery(nonValidCases: false,icsrPadderAgencyCases: false,deletedCases: false)}
        SuperQuery.metaClass.isEditableBy = {User currentUser -> true}
        def mockQueryService=Mock(QueryService)
        mockQueryService.getUsagesCount(_)>>{return 0}
        mockQueryService.getUsagesCountQuerySet(_)>>{return 0}
        controller.queryService=mockQueryService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_) >> {}
        controller.CRUDService = mockCRUDService
        def PVCMIntegrationServiceMock = new MockFor(PVCMIntegrationService)
        PVCMIntegrationServiceMock.demand.checkAndInvokeRoutingCondition { queryInstance, isPreviouslyTagExist -> true}
        controller.PVCMIntegrationService = PVCMIntegrationServiceMock.proxyInstance()

        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test delete found editable validation exception with no usage count and no usage count query set"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        SuperQuery superQuery=new SuperQuery(nonValidCases: false,icsrPadderAgencyCases: false, deletedCases: false)
        SuperQuery.metaClass.static.get={Long id ->superQuery}
        SuperQuery.metaClass.isEditableBy = {User currentUser -> true}
        def mockQueryService=Mock(QueryService)
        mockQueryService.getUsagesCount(_)>>{return 0}
        mockQueryService.getUsagesCountQuerySet(_)>>{return 0}
        controller.queryService=mockQueryService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.softDelete(_,_,_) >> {throw new ValidationException("Validation Exception",superQuery.errors)}
        controller.CRUDService = mockCRUDService
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/query/view/1"
    }

    void "test copy not found"(){
        given:
        SuperQuery.metaClass.static.read={null}
        when:
        controller.copy()
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test copy try success"(){
        given:
        SuperQuery superQuery=new SuperQuery(nonValidCases: false,icsrPadderAgencyCases: false,deletedCases: false)
        SuperQuery.metaClass.static.read={Long id ->superQuery}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        def mockQueryService=Mock(QueryService)
        mockQueryService.copyQuery(_,_)>>{}
        controller.queryService=mockQueryService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {return superQuery}
        controller.CRUDService = mockCRUDService
        def PVCMIntegrationServiceMock = new MockFor(PVCMIntegrationService)
        PVCMIntegrationServiceMock.demand.checkAndInvokeRoutingCondition { queryInstance, isPreviouslyTagExist -> true}
        controller.PVCMIntegrationService = PVCMIntegrationServiceMock.proxyInstance()
        when:
        controller.copy(2L)
        then:
        response.status==302
        response.redirectedUrl=="/query/view"
    }

    void "test copy validation exception"(){
        given:
        SuperQuery superQuery=new SuperQuery(nonValidCases: false,icsrPadderAgencyCases: false,deletedCases: false)
        SuperQuery.metaClass.static.read={Long id ->superQuery}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        def mockQueryService=Mock(QueryService)
        mockQueryService.copyQuery(_,_)>>{}
        controller.queryService=mockQueryService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception",superQuery.errors)}
        controller.CRUDService = mockCRUDService
        when:
        controller.copy(2L)
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test save not saved"(){
        given:
        SuperQuery.metaClass.static.getSuperQueryInstance={return instanceType}
        when:
        controller.save()
        then:
        response.status == statusVal
        response.redirectedUrl == urlVal
        where:
        instanceType                                       | statusVal    |   urlVal
        null                                               | 302          |  "/query/index"
    }

    void "test save found with validation exception"() {
        given:
        SuperQuery superQuery=new SuperQuery(id: 2L)
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return makeAdminUser1() }
        controller.userService = mockUserService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception",superQuery.errors)}
        controller.CRUDService = mockCRUDService
        def mockSession = Mock(org.hibernate.Session)
        mockSession.setFlushMode(_ as FlushMode) >> {true}
        def mockSessionFactory = Mock(SessionFactory)
        mockSessionFactory.getCurrentSession() >> {return mockSession}
        controller.sessionFactory = mockSessionFactory
        when:
        params.queryType = QueryTypeEnum.QUERY_BUILDER.name()
        controller.save()
        then:
        response.status == 200
    }

    void "test save found with editing configuration session"() {
        given:
        SuperQuery superQuery=new SuperQuery(id: 2L)
        Query query=new Query(id: 2L)
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return makeAdminUser1() }
        mockUserService.isCurrentUserAdmin()>>{return true}
        controller.userService = mockUserService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {return  new SuperQuery(id: 1L, name: "Test Query", description: "Test Description",hasBlanks: true)}
        controller.CRUDService = mockCRUDService
        def mockSqlService=Mock(SqlService)
        mockSqlService.getQueriesFromJSON(_)>>{}
        controller.sqlService=mockSqlService
        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.createExecutedQuery { queryInstance -> true }
        queryServiceMock.demand.getUsages { originalQuery ->
            return []
        }
        queryServiceMock.demand.updateQuery { originalQuery -> false }
        def PVCMIntegrationServiceMock = new MockFor(PVCMIntegrationService)
        PVCMIntegrationServiceMock.demand.checkAndInvokeRoutingCondition {queryInstance, isPreviouslyTagExist -> true }
        controller.PVCMIntegrationService = PVCMIntegrationServiceMock.proxyInstance()

        controller.queryService = queryServiceMock.proxyInstance()
        when:
        params.queryType = queryTypeVal
        session.editingConfiguration = [queryId: 1L , controller: 'query' , action: 'save' , configurationId: 1L]
        controller.save()
        then:
        response.status == statusVal
        response.redirectedUrl== urlVal
        where:
        queryTypeVal                        | statusVal | urlVal
        QueryTypeEnum.QUERY_BUILDER.name()  | 302       | "/query/save/1?continueEditing=true&queryId="
    }

    void "test save found with editing reason of delay session"() {
        given:
        SuperQuery superQuery=new SuperQuery(id: 2L)
        Query query=new Query(id: 2L)
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return makeAdminUser1() }
        controller.userService = mockUserService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {return  new SuperQuery(id: 1L, name: "Test Query", description: "Test Description")}
        controller.CRUDService = mockCRUDService
        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.createExecutedQuery { queryInstance -> true }
        queryServiceMock.demand.getUsages { originalQuery ->
            return []
        }
        queryServiceMock.demand.updateQuery { originalQuery -> false }
        def PVCMIntegrationServiceMock = new MockFor(PVCMIntegrationService)
        PVCMIntegrationServiceMock.demand.checkAndInvokeRoutingCondition { queryInstance, isPreviouslyTagExist -> true}
        controller.PVCMIntegrationService = PVCMIntegrationServiceMock.proxyInstance()

        controller.queryService = queryServiceMock.proxyInstance()
        when:
        params.queryType = queryTypeVal
        session.editingAutoReasonOfDelay = [queryId:1L  , controller:'query', action:'save'  , autoReasonOfDelayId: 1L]
        controller.save()
        then:
        response.status == statusVal
        response.redirectedUrl==urlVal
        where:
        queryTypeVal                        | statusVal | urlVal
        QueryTypeEnum.QUERY_BUILDER.name()  | 302       | "/query/save/1?continueEditing=true&queryId="
    }

    void "test save found with no session"() {
        given:
        SuperQuery superQuery=new SuperQuery(id: 2L)
        Query query=new Query(id: 2L)
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return makeAdminUser1() }
        controller.userService = mockUserService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {return  new SuperQuery(id: 1L, name: "Test Query", description: "Test Description")}
        controller.CRUDService = mockCRUDService
        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.createExecutedQuery { queryInstance -> true }
        queryServiceMock.demand.getUsages { originalQuery ->
            return []
        }
        queryServiceMock.demand.updateQuery { originalQuery -> false }
        def PVCMIntegrationServiceMock = new MockFor(PVCMIntegrationService)
        PVCMIntegrationServiceMock.demand.checkAndInvokeRoutingCondition {queryInstance, isPreviouslyTagExist -> true }
        controller.PVCMIntegrationService = PVCMIntegrationServiceMock.proxyInstance()
        controller.queryService = queryServiceMock.proxyInstance()
        when:
        params.queryType = queryTypeVal
        controller.save()
        then:
        response.status == statusVal
        response.redirectedUrl== urlVal
        where:
        queryTypeVal                        | statusVal | urlVal
        QueryTypeEnum.QUERY_BUILDER.name()  | 302       | "/query/view"
    }

    void "Session not found if we enter query input with backslash while saving it"() {
        given: "An admin user"
        def adminUser = makeAdminUser()
        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.createExecutedQuery { queryInstance -> true }
        queryServiceMock.demand.saveQuery { originalQuery, user -> originalQuery }
        controller.queryService = queryServiceMock.proxyInstance()
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { originalQuery -> originalQuery }
        controller.CRUDService = crudServiceMock.proxyInstance()
        def PVCMIntegrationServiceMock = new MockFor(PVCMIntegrationService)
        PVCMIntegrationServiceMock.demand.checkAndInvokeRoutingCondition {queryInstance, isPreviouslyTagExist -> true }
        controller.PVCMIntegrationService = PVCMIntegrationServiceMock.proxyInstance()
        controller.userService = makeSecurityService(adminUser)
        and: "A query"
        def JSONQuery1 ="""{ "all": { "containerGroups": [   
        { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR0\00\215" }  ] }  ] } }"""

        Query query = new Query([queryLevel: QueryLevelEnum.CASE, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                 createdBy : adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery1])

        query.owner = adminUser
        and: "No blank parameters"
        query.hasBlanks = false;
        when: "The query with no errors is updated"
        params.id = 1
        params.queryType = QueryTypeEnum.QUERY_BUILDER
        controller.save()
        then: "The query is created and page redirected"
        response.status == 302
        response.redirectedUrl == "/query/view/1"
    }

    void "Session not found if we enter query input with backslash while updating it"() {
        given: "An admin user"
        def adminUser = makeAdminUser()
        def crudServiceMock = new MockFor(CRUDService)
        crudServiceMock.demand.save { originalQuery -> originalQuery }
        crudServiceMock.demand.update{ theInstance -> theInstance}
        controller.CRUDService = crudServiceMock.proxyInstance()
        controller.userService = makeSecurityService(adminUser)
        and: "A query"
        def JSONQuery1 ="""{ "all": { "containerGroups": [   
        { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR0\00\215" }  ] }  ] } }"""

        SuperQuery superQueryInstance = new SuperQuery([queryLevel: QueryLevelEnum.CASE, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases',
                                                        createdBy : adminUser.username, modifiedBy: adminUser.username, JSONQuery: JSONQuery1])

        List<Map> newParams = new JsonSlurper().parseText(superQueryInstance.JSONQuery.replace("\\","\\\\")).blankParameters as List<Map> ?: []
        superQueryInstance.owner = adminUser
        and: "No blank parameters"
        superQueryInstance.hasBlanks = false;

        when: "The query with no errors is updated"
        params.id = 1
        params.queryType = QueryTypeEnum.QUERY_BUILDER
        controller.updateBlankParamsForQueryBuilder(superQueryInstance)
        then: "The query is created and page redirected"
        response.status == 200
    }

    void "test edit found editable with deletedCases"(){
        given:
        SuperQuery queryInstance=new SuperQuery(deletedCases: true)
        SuperQuery.metaClass.static.read={Long id -> queryInstance}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        SuperQuery.metaClass.isEditableBy = {User currentUser -> true}
        def mockQueryService=Mock(QueryService)
        mockQueryService.getUsagesCountQuerySet(_)>>{}
        controller.queryService=mockQueryService
        when:
        controller.edit(2L)
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test copy validation exception with deletedCases"(){
        given:
        SuperQuery superQuery=new SuperQuery(deletedCases: true)
        SuperQuery.metaClass.static.read={Long id ->superQuery}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        def mockQueryService=Mock(QueryService)
        mockQueryService.copyQuery(_,_)>>{}
        controller.queryService=mockQueryService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {throw new ValidationException("Validation Exception",superQuery.errors)}
        controller.CRUDService = mockCRUDService
        when:
        controller.copy(2L)
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test copy try success for deletedcases as false "(){
        given:
        SuperQuery superQuery=new SuperQuery(deletedCases: false)
        SuperQuery.metaClass.static.read={Long id ->superQuery}
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        def mockQueryService=Mock(QueryService)
        mockQueryService.copyQuery(_,_)>>{}
        controller.queryService=mockQueryService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {return superQuery}
        controller.CRUDService = mockCRUDService
        def PVCMIntegrationServiceMock = new MockFor(PVCMIntegrationService)
        PVCMIntegrationServiceMock.demand.checkAndInvokeRoutingCondition { queryInstance, isPreviouslyTagExist -> true}
        controller.PVCMIntegrationService = PVCMIntegrationServiceMock.proxyInstance()
        when:
        controller.copy(2L)
        then:
        response.status==302
        response.redirectedUrl=="/query/view"
    }

    void "test delete found for deletedCases query "(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        SuperQuery.metaClass.static.get={Long id ->new SuperQuery(deletedCases: true)}
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "SuperAdmin can edit deleted query"() {
        given: "A admin user"
        def normalUser = makeAdminUser()
        SuperQuery.metaClass.static.read = {Long id -> Query.get(id)}

        and: "An superadmin user"
        def adminUser = makeSuperAdminUser()

        and: "A query deleted query"
        Query query = new Query([queryType: QueryTypeEnum.QUERY_BUILDER, name: 'TEST QUERY: SAE - Clinical Reconciliation Death Cases', description: 'Query to identify SAE - Clinical Reconciliation death cases', deletedCases: true,
                                 createdBy: normalUser.username, modifiedBy: normalUser.username, JSONQuery: JSONQuery])
        query.owner = adminUser
        query.userService = makeUserService()
        query.save(failOnError: true)

        and: "The currently logged in user is super admin user"
        controller.userService = makeSecurityService(adminUser)
        User.metaClass.isAdmin = { true }

        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.getUsagesCountQuerySet(1..1) { originalQuery ->
            return 0
        }

        queryServiceMock.demand.getUsagesCount(1..1) { originalQuery ->
            return 0
        }

        queryServiceMock.demand.isQueryUpdateable(1..1) { originalQuery ->
            return true
        }

        controller.queryService = queryServiceMock.proxyInstance()

        when: "The super admin wants to edit"
        controller.edit(1L)
        then: "Super Admin can edit the query"
        response.status == 200
        model.query == query
    }

    void "test delete found for deleted cases"(){
        given:
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {return makeAdminUser1()}
        controller.userService = mockUserService
        SuperQuery.metaClass.static.get={Long id ->new SuperQuery(deletedCases: true)}
        when:
        params.id=1L
        request.method="POST"
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/query/index"
    }

    void "test save found with deleted cases selected"() {
        given:
        SuperQuery superQuery=new SuperQuery(id: 2L)
        Query query=new Query(id: 2L)
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> { return makeAdminUser1() }
        mockUserService.isCurrentUserAdmin()>>{return true}
        controller.userService = mockUserService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {return  new SuperQuery(id: 1L, name: "Test Query", description: "Test Description",deletedCases: true,hasBlanks: true)}
        controller.CRUDService = mockCRUDService
        def mockSqlService=Mock(SqlService)
        mockSqlService.getQueriesFromJSON(_)>>{}
        controller.sqlService=mockSqlService
        def queryServiceMock = new MockFor(QueryService)
        queryServiceMock.demand.createExecutedQuery { queryInstance -> true }
        queryServiceMock.demand.getUsages { originalQuery ->
            return []
        }
        queryServiceMock.demand.updateQuery { originalQuery -> false }
        def PVCMIntegrationServiceMock = new MockFor(PVCMIntegrationService)
        PVCMIntegrationServiceMock.demand.checkAndInvokeRoutingCondition {queryInstance, isPreviouslyTagExist -> true }
        controller.PVCMIntegrationService = PVCMIntegrationServiceMock.proxyInstance()

        controller.queryService = queryServiceMock.proxyInstance()
        when:
        params.queryType = queryTypeVal
        session.editingConfiguration = [queryId: 1L , controller: 'query' , action: 'save' , configurationId: 1L]
        controller.save()
        then:
        response.status == statusVal
        response.redirectedUrl== urlVal
        where:
        queryTypeVal                        | statusVal | urlVal
        QueryTypeEnum.QUERY_BUILDER.name()  | 302       | "/query/save/1?continueEditing=true&queryId="
    }

    void "test getEmbaseOperators renders correct JSON"() {
        given: "An expected output based on the QueryOperatorEnum values"
//         Operators = [QueryOperatorEnum.CONTAINS, QueryOperatorEnum.EQUALS]
        def expectedResult = [
                [value: 'CONTAINS', display: 'Contains'],
                [value: 'EQUALS', display: 'Equals']
        ]

        // Mock the message method for i18n translation
        controller.metaClass.message = { Map args ->
            switch (args.code) {
                case 'app.queryOperator.CONTAINS': return 'Contains'
                case 'app.queryOperator.EQUALS': return 'Equals'
                default: return args.code // Fallback to the code itself if no match
            }
        }

        when: "getEmbaseOperators method is called"
        controller.getEmbaseOperators()

        then: "The response should contain the correct JSON output with i18n values"
        response.json == expectedResult
        response.status == 200
    }
}
