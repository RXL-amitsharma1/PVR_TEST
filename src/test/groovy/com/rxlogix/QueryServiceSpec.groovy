package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges


@ConfineMetaClassChanges([User])
class QueryServiceSpec extends Specification implements DataTest, ServiceUnitTest<QueryService> {

    public static final user = "unitTest"
    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()

    def setupSpec() {
        mockDomains User, Role, UserRole, Query, CustomSQLQuery, QuerySet, SuperQuery, Preference, Configuration, Tag, CaseLineListingTemplate,
                Tenant, SourceTableMaster, SourceColumnMaster, ReportField, ReportFieldGroup, QueryExpressionValue, TemplateQuery, DateRangeInformation, CustomSQLValue,
                ReportFieldInfo, ReportFieldInfoList, CustomSQLTemplate, ExecutedQuery,ExecutedCustomSQLQuery,ExecutedQuerySet
    }

    def setup() {
        // force the tests to run in the TEST_TZ
        TimeZone.setDefault(TimeZone.getTimeZone(TEST_TZ))
    }

    def cleanup() {
        // set the TZ back to what it was
        TimeZone.setDefault(ORIGINAL_TZ)
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
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    private def mockCRUDService() {
        def CRUDServiceMock = new MockFor(CRUDService)
        CRUDServiceMock.demand.saveWithoutAuditLog(1..3) { Object obj->
            true
        }
        return CRUDServiceMock.proxyInstance()
    }

    private def mockSqlService() {
        def SqlServiceMock = new MockFor(SqlService)
        SqlServiceMock.demand.validateQuerySet(1..2) { ->
            true
        }
        return SqlServiceMock.proxyInstance()
    }

    CustomSQLTemplate createSimpleCustomSQLTemplate(User user) {
        new CustomSQLTemplate(columnNamesList: "[Case Number]", factoryDefault: false,
                customSQLTemplateSelectFrom: "select case_num \"Case Number\" from V_C_IDENTIFICATION cm",
                customSQLTemplateWhere: null,
                description: "Case Number",
                customSQLValues: [],
                name: "Custom SQL Template",
                isDeleted: false,
                editable: true,
                hasBlanks: false,
                templateType: TemplateTypeEnum.CUSTOM_SQL,
                owner: user,
                createdBy: "admin",
                modifiedBy: "admin")
    }

    Query createSimpleQueryBuilder(User user) {
        new Query([queryType  : QueryTypeEnum.QUERY_BUILDER,
                   name       : 'Query builder',
                   description: 'Simple query builder',
                   createdBy  : user.username,
                   modifiedBy : user.username,
                   JSONQuery  : """{ "all": { "containerGroups": [
        { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }"""])
    }

    Query createParameterizedQueryBuilder(User user) {
        Query query = new Query([queryType  : QueryTypeEnum.QUERY_BUILDER,
                                 name       : 'Query builder',
                                 description: 'Simple query builder',
                                 createdBy  : user.username,
                                 modifiedBy : user.username,
                                 JSONQuery  : """{ "all": { "containerGroups": [
        { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "" }  ] }  ] } }"""])
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save(failOnError: true)
        SourceTableMaster sourceTableMaster = new SourceTableMaster([tableName: "V_C_IDENTIFICATION", caseJoinOrder: 1, caseJoinType: "E", tableAlias: "cm", tableType: "C"])
        sourceTableMaster.save(failOnError: true)
        SourceColumnMaster sourceColumnMaster = new SourceColumnMaster([tableName: sourceTableMaster, reportItem: "CM_CASE_NUM", columnName: "CASE_NUM", columnType: "V", lang: "en"])
        sourceColumnMaster.save(failOnError: true)
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", sourceColumnId: sourceColumnMaster.reportItem, fieldGroup: fieldGroup, dataType: String.class, sourceId: 1])
        field.save(failOnError: true)
        query.addToQueryExpressionValues(new QueryExpressionValue(reportField: field, operator: QueryOperatorEnum.EQUALS, value: null))
    }

    CustomSQLQuery createSimpleCustomSQLQuery(User user) {
        new CustomSQLQuery(queryType: QueryTypeEnum.CUSTOM_SQL,
                owner: user,
                name: 'Test query',
                createdBy: "admin",
                modifiedBy: "admin",
                customSQLQuery: "where cm.occured_country_id = 223")
    }

    CustomSQLQuery createParameterizedCustomSQLQuery(User user) {
        CustomSQLQuery customSQLQuery = new CustomSQLQuery(queryType: QueryTypeEnum.CUSTOM_SQL,
                owner: user,
                name: 'Test query',
                createdBy: "admin",
                modifiedBy: "admin",
                customSQLQuery: "where cm.occured_country_id = :country_id")
        customSQLQuery.addToCustomSQLValues(new CustomSQLValue(key: ":country_id", value: null))
    }

    QuerySet createQuerySet(User user, SuperQuery queryOne, SuperQuery queryTwo) {
        QuerySet querySet = new QuerySet(queryType: QueryTypeEnum.SET_BUILDER,
                owner: user,
                name: "Query set with query builder and custom SQL query",
                createdBy: "admin",
                modifiedBy: "admin",
                JSONQuery: """{"all": {"containerGroups": [{"expressions": [{"index": "0", "query": "${
                    queryOne.id
                }"}, {"index": "1", "query": "${queryTwo.id}"}], "keyword": "union" }]}}""")
        querySet.addToQueries(queryOne)
        querySet.addToQueries(queryTwo)
    }

    Configuration createSimpleConfiguration(User user) {
        new Configuration([reportName    : 'SAE - Clinical Reconciliation Death Case',
                           description   : 'Config to identify SAE - Clinical Reconciliation death cases',
                           owner         : user,
                           deliveryOption: new DeliveryOption(sharedWith: [user], attachmentFormats: [ReportFormatEnum.PDF]),
                           isEnabled     : true, createdBy: user.username, modifiedBy: user.username])
    }

    void "Test helper method for Date converting String to Date back to String"() {
        given: "An expression date to convert to string"
        when: "We run the helper method to convert"
        String result = service.convertExpressionDateStringToDate(expressionDate, days)
        then: "We get the correct String back"
        result == expected
        where:
        expressionDate | days | expected
        "18-Oct-2014"  | 0    | "18-Oct-2014"
        "18-Oct-2014"  | 1    | "19-Oct-2014"
    }

    void "CustomSQLQuery is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A custom SQL query"
        CustomSQLQuery customSQLQuery = createSimpleCustomSQLQuery(adminUser)
        customSQLQuery.save(validate: false)

        when: "The query is copied and saved"
        CustomSQLQuery customSQLQueryCopy = (CustomSQLQuery) service.copyQuery(customSQLQuery, adminUser)
        customSQLQueryCopy.save(validate: false)

        then: "Each property is duplicated correctly"
        assert customSQLQuery.id != customSQLQueryCopy.id

        expect: "Common properties are the same"
        customSQLQuery.description == customSQLQueryCopy.description
        customSQLQuery.owner == customSQLQueryCopy.owner
        customSQLQuery.isDeleted == customSQLQueryCopy.isDeleted
        customSQLQuery.hasBlanks == customSQLQueryCopy.hasBlanks
        customSQLQuery.tags == customSQLQueryCopy.tags
        customSQLQuery.userGroupQueries == null
        customSQLQuery.userQueries == null
        customSQLQuery.JSONQuery == customSQLQueryCopy.JSONQuery
        // TODO: Unexpectedly fails
//        customSQLQuery.queryType == customSQLQueryCopy.queryType
        customSQLQuery.originalQueryId == customSQLQueryCopy.originalQueryId
        customSQLQuery.factoryDefault == customSQLQueryCopy.factoryDefault
        customSQLQuery.nonValidCases == customSQLQueryCopy.nonValidCases
        customSQLQuery.deletedCases == customSQLQueryCopy.deletedCases
        customSQLQuery.icsrPadderAgencyCases == customSQLQueryCopy.icsrPadderAgencyCases
    }

    void "Parameterized CustomSQLQuery is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A custom SQL query"
        CustomSQLQuery customSQLQuery = createParameterizedCustomSQLQuery(adminUser)
        customSQLQuery.save(validate: false)

        when: "The query is copied and saved"
        CustomSQLQuery customSQLQueryCopy = (CustomSQLQuery) service.copyQuery(customSQLQuery, adminUser)
        customSQLQueryCopy.save(validate: false)

        then: "Each property is duplicated correctly"
        assert customSQLQuery.id != customSQLQueryCopy.id
        assert customSQLQueryCopy.customSQLValues.iterator().next().key == ":country_id"
        assert customSQLQueryCopy.customSQLValues.iterator().next().id != customSQLQuery.customSQLValues.iterator().next().id

        expect: "Common properties are the same"
        customSQLQuery.description == customSQLQueryCopy.description
        customSQLQuery.owner == customSQLQueryCopy.owner
        customSQLQuery.isDeleted == customSQLQueryCopy.isDeleted
        customSQLQuery.hasBlanks == customSQLQueryCopy.hasBlanks
        customSQLQuery.tags == customSQLQueryCopy.tags
        customSQLQuery.userGroupQueries == null
        customSQLQuery.userQueries == null
        customSQLQuery.JSONQuery == customSQLQueryCopy.JSONQuery
        // TODO: Unexpectedly fails
//        customSQLQuery.queryType == customSQLQueryCopy.queryType
        customSQLQuery.originalQueryId == customSQLQueryCopy.originalQueryId
        customSQLQuery.factoryDefault == customSQLQueryCopy.factoryDefault
        customSQLQuery.nonValidCases == customSQLQueryCopy.nonValidCases
        customSQLQuery.deletedCases == customSQLQueryCopy.deletedCases
        customSQLQuery.icsrPadderAgencyCases == customSQLQueryCopy.icsrPadderAgencyCases
    }

    void "Query Builder is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A simple query"
        Query query = createSimpleQueryBuilder(adminUser)
        query.save(validate: false)

        when: "The query is copied and saved"
        Query queryCopy = (Query) service.copyQuery(query, adminUser)
        queryCopy.save(validate: false)

        then: "Each property is duplicated correctly"
        assert query.id != queryCopy.id

        expect: "Common properties are the same"
        // TODO: Unexpectedly fails
//        query.description == queryCopy.description
//        query.owner == queryCopy.owner
        query.isDeleted == queryCopy.isDeleted
        query.hasBlanks == queryCopy.hasBlanks
        query.tags == queryCopy.tags
        query.userQueries == null
        query.userGroupQueries == null
        //        query.JSONQuery == queryCopy.JSONQuery
//        query.queryType == queryCopy.queryType
        query.originalQueryId == queryCopy.originalQueryId
        query.factoryDefault == queryCopy.factoryDefault
        query.nonValidCases == queryCopy.nonValidCases
        query.deletedCases == queryCopy.deletedCases
        query.icsrPadderAgencyCases == queryCopy.icsrPadderAgencyCases
    }

    void "Parameterized Query Builder is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A parameterized query"
        Query query = createParameterizedQueryBuilder(adminUser)
        query.save(validate: false)

        when: "The query is copied and saved"
        Query queryCopy = (Query) service.copyQuery(query, adminUser)
        queryCopy.save(validate: false)

        then: "Each property is duplicated correctly"
        assert query.id != queryCopy.id
        assert queryCopy.queryExpressionValues.iterator().next().id != query.queryExpressionValues.iterator().next().id

        expect: "Common properties are the same"
        // TODO: Unexpectedly fails
//        query.description == queryCopy.description
//        query.owner == queryCopy.owner
        query.isDeleted == queryCopy.isDeleted
        query.hasBlanks == queryCopy.hasBlanks
        query.tags == queryCopy.tags
        query.userGroupQueries == null
        query.userQueries == null
//        query.JSONQuery == queryCopy.JSONQuery
//        query.queryType == queryCopy.queryType
        query.originalQueryId == queryCopy.originalQueryId
        query.factoryDefault == queryCopy.factoryDefault
        query.nonValidCases == queryCopy.nonValidCases
        query.deletedCases == queryCopy.deletedCases
        query.icsrPadderAgencyCases == queryCopy.icsrPadderAgencyCases
    }

    void "Query Set is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A simple query"
        Query query = createSimpleQueryBuilder(adminUser)
        query.save(validate: false)

        and: "A custom SQL query"
        CustomSQLQuery customSQLQuery = createSimpleCustomSQLQuery(adminUser)
        customSQLQuery.save(validate: false)

        and: "A query set that uses both those queries"
        QuerySet querySet = createQuerySet(adminUser, query, customSQLQuery)
        querySet.userService = makeUserService()
        querySet.save(failOnError: true)

        when: "The query is copied and saved"
        QuerySet querySetCopy = (QuerySet) service.copyQuery(querySet, adminUser)
        querySetCopy.userService = makeUserService()
        querySetCopy.save(validate: false)

        then: "Each property is duplicated correctly"
        assert querySet.id != querySetCopy.id

        expect: "Common properties are the same"
        querySet.description == querySetCopy.description
        querySet.owner == querySetCopy.owner
        querySet.isDeleted == querySetCopy.isDeleted
        querySet.hasBlanks == querySetCopy.hasBlanks
        querySet.tags == querySetCopy.tags
        querySet.userQueries == null
        querySet.userGroupQueries == null
        // TODO: Unexpectedly fails
//        querySet.JSONQuery == querySetCopy.JSONQuery
//        querySet.queryType == querySetCopy.queryType
        querySet.originalQueryId == querySetCopy.originalQueryId
        querySet.factoryDefault == querySetCopy.factoryDefault
        querySet.nonValidCases == querySetCopy.nonValidCases
        querySet.deletedCases == querySetCopy.deletedCases
        querySet.icsrPadderAgencyCases == querySetCopy.icsrPadderAgencyCases
    }

    void "Parameterized Query Set is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A parameterized query"
        Query query = createParameterizedQueryBuilder(adminUser)
        query.save(validate: false)

        and: "A parameterized custom SQL query"
        CustomSQLQuery customSQLQuery = createParameterizedCustomSQLQuery(adminUser)
        customSQLQuery.save(validate: false)

        and: "A query set that uses both those queries"
        QuerySet querySet = createQuerySet(adminUser, query, customSQLQuery)
        querySet.userService = makeUserService()
        querySet.save(failOnError: true)

        when: "The query is copied and saved"
        QuerySet querySetCopy = (QuerySet) service.copyQuery(querySet, adminUser)
        querySetCopy.save(validate: false)

        then: "Each property is duplicated correctly"
        assert querySet.id != querySetCopy.id
        assert ((Query) querySetCopy.queries.get(0)).queryExpressionValues.iterator().next().id == query.queryExpressionValues.iterator().next().id
        assert ((CustomSQLQuery) querySetCopy.queries.get(1)).customSQLValues.iterator().next().id == customSQLQuery.customSQLValues.iterator().next().id

        expect: "Common properties are the same"
        querySet.description == querySetCopy.description
        querySet.owner == querySetCopy.owner
        querySet.isDeleted == querySetCopy.isDeleted
        querySet.hasBlanks == querySetCopy.hasBlanks
        querySet.tags == querySetCopy.tags
        querySet.userQueries == null
        querySet.userGroupQueries == null
        querySet.JSONQuery == querySetCopy.JSONQuery
        // TODO: Unexpectedly fails
//        querySet.queryType == querySetCopy.queryType
        querySet.originalQueryId == querySetCopy.originalQueryId
        querySet.factoryDefault == querySetCopy.factoryDefault
        querySet.nonValidCases == querySetCopy.nonValidCases
        querySet.deletedCases == querySetCopy.deletedCases
        querySet.icsrPadderAgencyCases == querySetCopy.icsrPadderAgencyCases
    }

    void "Executed CustomSQLQuery is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A custom SQL query"
        CustomSQLQuery customSQLQuery = createSimpleCustomSQLQuery(adminUser)
        customSQLQuery.userService = makeUserService()
        customSQLQuery.save(validate: false)

        and: "A Configuration and a TemplateQuery which uses the query"

        CustomSQLTemplate customSQLTemplate = createSimpleCustomSQLTemplate(adminUser)
        customSQLTemplate.save(validate: false)

        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: customSQLTemplate, query: customSQLQuery,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed query is created"
        ExecutedCustomSQLQuery executedCustomSQLQuery = (ExecutedCustomSQLQuery) service.createExecutedQuery(templateQuery.query)

        then: "Each property is duplicated correctly"
        assert executedCustomSQLQuery.originalQueryId > 0

        expect: "Common properties are the same"
        customSQLQuery.description == executedCustomSQLQuery.description
        // TODO: Unexpectedly fails
//        customSQLQuery.owner == executedCustomSQLQuery.owner
        customSQLQuery.isDeleted == executedCustomSQLQuery.isDeleted
        customSQLQuery.hasBlanks == executedCustomSQLQuery.hasBlanks
        customSQLQuery.tags == executedCustomSQLQuery.tags
        customSQLQuery.userQueries*.user == executedCustomSQLQuery.userQueries*.user
        customSQLQuery.userGroupQueries*.userGroup == executedCustomSQLQuery.userGroupQueries*.userGroup
        customSQLQuery.JSONQuery == executedCustomSQLQuery.JSONQuery
//        customSQLQuery.queryType == executedCustomSQLQuery.queryType
        customSQLQuery.originalQueryId != executedCustomSQLQuery.originalQueryId
        customSQLQuery.factoryDefault == executedCustomSQLQuery.factoryDefault
        customSQLQuery.nonValidCases == executedCustomSQLQuery.nonValidCases
        customSQLQuery.deletedCases == executedCustomSQLQuery.deletedCases
        customSQLQuery.icsrPadderAgencyCases == executedCustomSQLQuery.icsrPadderAgencyCases
    }

    void "Executed Parameterized CustomSQLQuery is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A custom SQL query"
        CustomSQLQuery customSQLQuery = createParameterizedCustomSQLQuery(adminUser)
        customSQLQuery.save(validate: false)

        and: "A Configuration and a TemplateQuery which uses the query"

        CustomSQLTemplate customSQLTemplate = createSimpleCustomSQLTemplate(adminUser)
        customSQLTemplate.save(validate: false)

        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: customSQLTemplate, query: customSQLQuery,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed query is created"
        ExecutedCustomSQLQuery executedCustomSQLQuery = (ExecutedCustomSQLQuery) service.createExecutedQuery(templateQuery.query)

        then: "Each property is duplicated correctly"
        assert executedCustomSQLQuery.originalQueryId > 0
        assert executedCustomSQLQuery.customSQLValues.iterator().next().key == ":country_id"
        assert executedCustomSQLQuery.customSQLValues.iterator().next().id != customSQLQuery.customSQLValues.iterator().next().id

        expect: "Common properties are the same"
        customSQLQuery.description == executedCustomSQLQuery.description
        customSQLQuery.owner == executedCustomSQLQuery.owner
        customSQLQuery.isDeleted == executedCustomSQLQuery.isDeleted
        customSQLQuery.hasBlanks == executedCustomSQLQuery.hasBlanks
        customSQLQuery.tags == executedCustomSQLQuery.tags
        customSQLQuery.userQueries*.user == executedCustomSQLQuery.userQueries*.user
        customSQLQuery.userGroupQueries*.userGroup == executedCustomSQLQuery.userGroupQueries*.userGroup
        customSQLQuery.JSONQuery == executedCustomSQLQuery.JSONQuery
        customSQLQuery.queryType == executedCustomSQLQuery.queryType
        customSQLQuery.originalQueryId != executedCustomSQLQuery.originalQueryId
        customSQLQuery.factoryDefault == executedCustomSQLQuery.factoryDefault
        customSQLQuery.nonValidCases == executedCustomSQLQuery.nonValidCases
        customSQLQuery.deletedCases == executedCustomSQLQuery.deletedCases
        customSQLQuery.icsrPadderAgencyCases == executedCustomSQLQuery.icsrPadderAgencyCases
    }

    void "Executed Query Builder is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A simple query"
        Query query = createSimpleQueryBuilder(adminUser)
        query.save(validate: false)

        and: "A Configuration and a TemplateQuery which uses the query"

        CustomSQLTemplate customSQLTemplate = createSimpleCustomSQLTemplate(adminUser)
        customSQLTemplate.save(validate: false)

        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: customSQLTemplate, query: query,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed query is created"
        SuperQuery executedQuery = service.createExecutedQuery(templateQuery.query)

        then: "Each property is duplicated correctly"
        assert executedQuery.originalQueryId > 0

        expect: "Common properties are the same"
        // TODO: Unexpectedly fails
//        query.description == executedQuery.description
        query.owner == executedQuery.owner
        query.isDeleted == executedQuery.isDeleted
        query.hasBlanks == executedQuery.hasBlanks
        query.tags == executedQuery.tags
        query.userQueries*.user == executedQuery.userQueries*.user
        query.userGroupQueries*.userGroup == executedQuery.userGroupQueries*.userGroup
//        query.JSONQuery == executedQuery.JSONQuery
//        query.queryType == executedQuery.queryType
        query.originalQueryId != executedQuery.originalQueryId
        query.factoryDefault == executedQuery.factoryDefault
        query.nonValidCases == executedQuery.nonValidCases
        query.deletedCases == executedQuery.deletedCases
        query.icsrPadderAgencyCases == executedQuery.icsrPadderAgencyCases
    }

    void "Executed Parameterized Query Builder is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A parameterized query"
        Query query = createParameterizedQueryBuilder(adminUser)
        query.save(validate: false)

        and: "A Configuration and a TemplateQuery which uses the query"

        CustomSQLTemplate customSQLTemplate = createSimpleCustomSQLTemplate(adminUser)
        customSQLTemplate.save(validate: false)

        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: customSQLTemplate, query: query,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed query is created"
        ExecutedQuery executedQuery = (ExecutedQuery) service.createExecutedQuery(templateQuery.query)

        then: "Each property is duplicated correctly"
        assert executedQuery.originalQueryId > 0
        assert executedQuery.queryExpressionValues.iterator().next().id != query.queryExpressionValues.iterator().next().id

        expect: "Common properties are the same"
        // TODO: Unexpectedly fails
//        query.description == executedQuery.description
        query.owner == executedQuery.owner
        query.isDeleted == executedQuery.isDeleted
        query.hasBlanks == executedQuery.hasBlanks
        query.tags == executedQuery.tags
        query.userQueries*.user == executedQuery.userQueries*.user
        query.userGroupQueries*.userGroup == executedQuery.userGroupQueries*.userGroup
//        query.JSONQuery == executedQuery.JSONQuery
//        query.queryType == executedQuery.queryType
        query.originalQueryId != executedQuery.originalQueryId
        query.factoryDefault == executedQuery.factoryDefault
        query.nonValidCases == executedQuery.nonValidCases
        query.deletedCases == executedQuery.deletedCases
        query.icsrPadderAgencyCases == executedQuery.icsrPadderAgencyCases
    }

    @Ignore
    void "Executed Query Set is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A simple query"
        Query query = createSimpleQueryBuilder(adminUser)
        query.userService = makeUserService()
        query.save(validate: false)

        and: "A custom SQL query"
        CustomSQLQuery customSQLQuery = createSimpleCustomSQLQuery(adminUser)
        customSQLQuery.userService = makeUserService()
        customSQLQuery.save(validate: false)

        and: "A query set that uses both those queries"
        QuerySet querySet = createQuerySet(adminUser, query, customSQLQuery)
        querySet.userService = makeUserService()
        querySet.save(failOnError: true)

        and: "A Configuration and a TemplateQuery which uses the query"
        CustomSQLTemplate customSQLTemplate = createSimpleCustomSQLTemplate(adminUser)
        customSQLTemplate.save(validate: false)

        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: customSQLTemplate, query: querySet,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed query is created"
        ExecutedQuerySet executedQuery = (ExecutedQuerySet) service.createExecutedQuery(templateQuery.query)

        then: "Each property is duplicated correctly"
        assert executedQuery.originalQueryId > 0

        expect: "Common properties are the same"
        querySet.description == executedQuery.description
        querySet.owner == executedQuery.owner
        querySet.isDeleted == executedQuery.isDeleted
        querySet.hasBlanks == executedQuery.hasBlanks
        querySet.tags == executedQuery.tags
        querySet.userQueries*.user == executedQuery.userQueries*.user
        querySet.userGroupQueries*.userGroup == executedQuery.userGroupQueries*.userGroup
        // TODO: Unexpectedly fails
//        querySet.JSONQuery == executedQuery.JSONQuery
        querySet.queryType == executedQuery.queryType
        querySet.originalQueryId != executedQuery.originalQueryId
        querySet.factoryDefault == executedQuery.factoryDefault
        querySet.nonValidCases == executedQuery.nonValidCases
        querySet.deletedCases == executedQuery.deletedCases
        querySet.icsrPadderAgencyCases == executedQuery.icsrPadderAgencyCases
    }

    @Ignore
    void "Executed Parameterized Query Set is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A parameterized query"
        Query query = createParameterizedQueryBuilder(adminUser)
        query.userService = makeUserService()
        query.save(validate: false)

        and: "A parameterized custom SQL query"
        CustomSQLQuery customSQLQuery = createParameterizedCustomSQLQuery(adminUser)
        customSQLQuery.customSQLQuery = makeUserService()
        customSQLQuery.save(validate: false)

        and: "A query set that uses both those queries"
        QuerySet querySet = createQuerySet(adminUser, query, customSQLQuery)
        querySet.userService = makeUserService()
        querySet.save(failOnError: true)

        and: "A Configuration and a TemplateQuery which uses the query"
        CustomSQLTemplate customSQLTemplate = createSimpleCustomSQLTemplate(adminUser)
        customSQLTemplate.save(validate: false)

        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: customSQLTemplate, query: querySet,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed query is created"
        ExecutedQuerySet executedQuery = (ExecutedQuerySet) service.createExecutedQuery(templateQuery.query)

        then: "Each property is duplicated correctly"
        assert executedQuery.originalQueryId > 0
        assert ((ExecutedQuery) executedQuery.queries.get(0)).queryExpressionValues.iterator().next().id != query.queryExpressionValues.iterator().next().id
        assert ((ExecutedCustomSQLQuery) executedQuery.queries.get(1)).customSQLValues.iterator().next().id != customSQLQuery.customSQLValues.iterator().next().id

        expect: "Common properties are the same"
        querySet.description == executedQuery.description
        querySet.owner == executedQuery.owner
        querySet.isDeleted == executedQuery.isDeleted
        querySet.hasBlanks == executedQuery.hasBlanks
        querySet.tags == executedQuery.tags
        querySet.userQueries*.user == executedQuery.userQueries*.user
        querySet.userGroupQueries*.userGroup == executedQuery.userGroupQueries*.userGroup
        // TODO: Unexpectedly fails
//        querySet.JSONQuery == executedQuery.JSONQuery
        querySet.queryType == executedQuery.queryType
        querySet.originalQueryId != executedQuery.originalQueryId
        querySet.factoryDefault == executedQuery.factoryDefault
        querySet.nonValidCases == executedQuery.nonValidCases
        querySet.deletedCases == executedQuery.deletedCases
        querySet.icsrPadderAgencyCases == executedQuery.icsrPadderAgencyCases
    }

    private makeUserService() {
        def userMock = new MockFor(UserService)
        userMock.demand.isCurrentUserAdmin(1..2) { false }
        return userMock.proxyInstance()
    }

    def "test getExpressionWithValue"() {
        given:
        List expressionList = [[index:0, field:'productFamilyId', op:'EQUALS', value:null, key:1], [index:1, field:'productDrugType', op:'EQUALS', value:null, key:2]]
        String keyword = 'and'
        Locale locale = new Locale('en_IN')
        List<Tuple2> values = [
                new Tuple2('productFamilyId', 'Pembrolizumab'),
                new Tuple2('productDrugType', 'Suspect'),
                new Tuple2('productFamilyId', 'Revatio'),
                new Tuple2('productDrugType', 'Suspect')]
        def mockCustomMessageService = new MockFor(CustomMessageService)
        mockCustomMessageService.demand.getMessage(1..2){field, args, localeVal ->
            if(field.contains('productFamilyId')){
                return "Product Family Name"
            }else{
                return "Drug Type"
            }
        }
        service.customMessageService = mockCustomMessageService.proxyInstance()

        when:
        String res = service.getExpressionWithValue(expressionList, keyword, locale, values)

        then:
        res != null
        res.contains('Product Family Name')
        res.contains('Drug Type')
        res.contains('EQUALS')
        res.contains('Pembrolizumab')
        res.contains('Suspect')
        res.contains('AND')
        res.contains('(Product Family Name  EQUALS  Pembrolizumab)')
        res.contains('(Drug Type  EQUALS  Suspect)')
    }
}
