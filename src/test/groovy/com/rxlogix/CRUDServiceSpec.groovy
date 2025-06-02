package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.plugins.orm.auditable.AuditLogListenerThreadLocal
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([CaseLineListingTemplate, DataTabulationTemplate, CustomSQLTemplate, Query, QuerySet, NonCaseSQLTemplate, CustomSQLQuery, ReportTemplate, User])
class CRUDServiceSpec extends Specification implements DataTest, ServiceUnitTest<CRUDService> {

    public static final user = "unitTest"

    @Shared caseMasterTable
    @Shared lmCountriesTable
    @Shared caseMasterColumnCountry
    @Shared caseInformationRFG
    @Shared countryOfIncidenceRF

    /**
     * Generally speaking the recurrence JSON string ignores the timeZone object; it uses the timezone in the passed in startDateTime
     *
     * Use -Duser.timezone=GMT to force tests to run in a different timezone
     * use --echoOut for force println output into standard out
     */
    def setup() {
        // A sample report field
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save(failOnError: true)
        SourceTableMaster sourceTableMaster = new SourceTableMaster([tableName: "V_C_IDENTIFICATION", caseJoinOrder: 1, caseJoinType: "E", tableAlias: "cm", tableType: "C"])
        sourceTableMaster.save(failOnError: true)
        SourceColumnMaster sourceColumnMaster = new SourceColumnMaster([tableName: sourceTableMaster, reportItem: "CM_CASE_NUM", columnName: "CASE_NUM", columnType: "V", lang: "en"])
        sourceColumnMaster.save(failOnError: true)
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", sourceColumn: sourceColumnMaster, fieldGroup: fieldGroup, dataType: String.class, sourceId: 1])
        field.save(failOnError: true)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: ReportField.get(1), argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true)

        def userServiceMock = new MockFor(UserService)
        userServiceMock.demand.setOwnershipAndModifier { Object object -> return object }
        service.userService = userServiceMock.proxyInstance()

        CaseLineListingTemplate.metaClass.detectChangesForAuditLog = { Object object, Map params, AuditLogCategoryEnum auditLogCategoryEnum -> return [] }
        DataTabulationTemplate.metaClass.detectChangesForAuditLog = { Object object, Map params, AuditLogCategoryEnum auditLogCategoryEnum -> return [] }
        CustomSQLTemplate.metaClass.detectChangesForAuditLog = { Object object, Map params, AuditLogCategoryEnum auditLogCategoryEnum -> return [] }
        NonCaseSQLTemplate.metaClass.detectChangesForAuditLog = { Object object, Map params, AuditLogCategoryEnum auditLogCategoryEnum -> return [] }
        Query.metaClass.detectChangesForAuditLog = { Object object, Map params, AuditLogCategoryEnum auditLogCategoryEnum -> return [] }
        QuerySet.metaClass.detectChangesForAuditLog = { Object object, Map params, AuditLogCategoryEnum auditLogCategoryEnum -> return [] }
        CustomSQLQuery.metaClass.detectChangesForAuditLog = { Object object, Map params, AuditLogCategoryEnum auditLogCategoryEnum -> return [] }
    }

    void setupSpec() {
        mockDomains Configuration, User, Role, UserRole, Query, ReportField, ReportFieldGroup, SourceTableMaster, SourceColumnMaster, QueryExpressionValue, Tenant, CaseLineListingTemplate, ReportFieldInfo, ReportFieldInfoList, DataTabulationTemplate, DataTabulationMeasure, DataTabulationColumnMeasure, CustomSQLTemplate, NonCaseSQLTemplate, QuerySet, CustomSQLQuery, ReportTemplate, SuperQuery
        buildReportFields()
        mockTemplateServiceForReportTemple()
    }

    def cleanupSpec() {
        cleanUpTemplateServiceMock()
    }

    private void mockTemplateServiceForReportTemple() {
        def mockTemplateService = new MockFor(TemplateService)
        mockTemplateService.demand.isTemplateUpdateable { ReportTemplate template ->
            return true
        }
        def templateService = mockTemplateService.proxyInstance()

        ReportTemplate.metaClass.getTemplateService = {
            return templateService
        }

        CaseLineListingTemplate.metaClass.getTemplateService = {
            return templateService
        }
    }

    private void cleanUpTemplateServiceMock() {

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
        def preferenceAdmin = new Preference(locale: new Locale("en"))
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    void buildReportFields() {
        caseMasterTable = new SourceTableMaster(tableName: "V_C_IDENTIFICATION", tableAlias: "cm", tableType: "C", caseJoinOrder: 1)
        lmCountriesTable = new SourceTableMaster(tableName: "VW_LCO_COUNTRY", tableAlias: "lco", tableType: "L", caseJoinOrder: null)
        caseMasterColumnCountry = new SourceColumnMaster(tableName: caseMasterTable, columnName: "COUNTRY_ID",
                primaryKey: null, lmTableName: lmCountriesTable, lmJoinColumn: "COUNTRY_ID",
                lmDecodeColumn: "COUNTRY", columnType: "N", reportItem: "CM_COUNTRY_ID",lang: 'en')
        caseInformationRFG = new ReportFieldGroup(name: "Case Information")

        //Purposely leaving out listDomainClass
        countryOfIncidenceRF = new ReportField(name: "masterCountryId",
                fieldGroup: caseInformationRFG, sourceColumnId: caseMasterColumnCountry.reportItem,
                dataType: String.class, sourceId: 1)
    }

    void saveReportFields() {
        caseMasterTable.save(failOnError: true)
        lmCountriesTable.save(failOnError: true)
        caseMasterColumnCountry.save(failOnError: true)
        caseInformationRFG.save(failOnError: true)
        countryOfIncidenceRF.save(failOnError: true)
    }

    void "Save Case Line Listing template through CRUDService -- Success"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A Case Line Listing template"
        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, owner: adminUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: ReportFieldInfoList.get(1))

        when: "Save through CRUDService"
        template = (CaseLineListingTemplate) service.save(template)

        then: "Success saved to database"
        template.id != null
    }

    void "Save Case Line Listing template through CRUDService -- Failed"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A Case Line Listing template"
        CaseLineListingTemplate template = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE, owner: adminUser, createdBy: "normalUser", modifiedBy: "normalUser", columnList: ReportFieldInfoList.get(1))

        when: "Update through CRUDService"
        try {
            template = (CaseLineListingTemplate) service.save(template)
        } catch (ValidationException ve) {
            // Validation Exception: Name cannot be null
        }

        then: "Failed saved to database"
        template.id == null
    }

    void "Save Data Tabulation template through CRUDService -- Success"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A Data Tabulation template"
        DataTabulationTemplate template = new DataTabulationTemplate(templateType: TemplateTypeEnum.DATA_TAB, owner: adminUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", rowList: ReportFieldInfoList.get(1))
        DataTabulationMeasure measure = new DataTabulationMeasure(type: MeasureTypeEnum.CASE_COUNT, name: "measure1", dateRangeCount: CountTypeEnum.CUMULATIVE_COUNT, percentageOption: PercentageOptionEnum.BY_SUBTOTAL).save(failOnError: true)
        DataTabulationColumnMeasure columnMeasure = new DataTabulationColumnMeasure(measures: [measure]).save(failOnError: true)
        template.columnMeasureList = [columnMeasure]

        when: "Save through CRUDService"
        template = (DataTabulationTemplate) service.save(template)

        then: "Success saved to database"
        template.id != null
    }

    void "Save Data Tabulation template through CRUDService -- Failed"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A Data Tabulation template"
        DataTabulationTemplate template = new DataTabulationTemplate(templateType: TemplateTypeEnum.DATA_TAB, owner: adminUser, createdBy: "normalUser", modifiedBy: "normalUser")
        DataTabulationMeasure measure = new DataTabulationMeasure(type: MeasureTypeEnum.CASE_COUNT, name: "measure1", dateRangeCount: CountTypeEnum.CUMULATIVE_COUNT, percentageOption: PercentageOptionEnum.BY_SUBTOTAL).save(failOnError: true)
        DataTabulationColumnMeasure columnMeasure = new DataTabulationColumnMeasure(measures: [measure]).save(failOnError: true)
        template.columnMeasureList = [columnMeasure]

        when: "Save through CRUDService"
        try {
            template = (DataTabulationTemplate) service.save(template)
        } catch (Exception ve) {
            // Validation Exception: RowList cannot be null
        }

        then: "Failed saved to database"
        template.id == null
    }

    void "Save Custom SQL template through CRUDService -- Success"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        def sqlServiceMock = new MockFor(SqlService)
        // Mock twice because they are called twice
        sqlServiceMock.demand.validateCustomSQL { String str, boolean usePvrDB -> return true }
        sqlServiceMock.demand.validateColumnName { String str, boolean usePvrDB -> return true }
        sqlServiceMock.demand.validateCustomSQL { String str, boolean usePvrDB -> return true }
        sqlServiceMock.demand.validateColumnName { String str, boolean usePvrDB -> return true }
        CustomSQLTemplate.metaClass.sqlService = sqlServiceMock.proxyInstance()

        and: "A Custom SQL template"
        CustomSQLTemplate template = new CustomSQLTemplate(templateType: TemplateTypeEnum.CUSTOM_SQL, owner: adminUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser")
        template.customSQLTemplateSelectFrom = """select case_num "Case Number" from V_C_IDENTIFICATION cm"""
        template.columnNamesList = ""

        when: "Save through CRUDService"
        template = (CustomSQLTemplate) service.save(template)

        then: "Success saved to database"
        template.id != null
    }

    void "Save Custom SQL template through CRUDService -- Failed"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A Custom SQL template"
        CustomSQLTemplate template = new CustomSQLTemplate(templateType: TemplateTypeEnum.CUSTOM_SQL, owner: adminUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser")
        template.customSQLTemplateSelectFrom = """delete case_num "Case Number" from V_C_IDENTIFICATION cm"""
        template.columnNamesList = ""

        when: "Save through CRUDService"
        try {
            template = (CustomSQLTemplate) service.save(template)
        } catch (ValidationException ve) {
            // Validation Exception: invalid CustomSQL
        }

        then: "Failed saved to database"
        template.id == null
    }

    void "Save Non Case SQL template through CRUDService -- Success"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A Non Case SQL template"
        NonCaseSQLTemplate template = new NonCaseSQLTemplate(templateType: TemplateTypeEnum.CUSTOM_SQL, owner: adminUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser")
        template.nonCaseSql = """select case_num "Case Number" from V_C_IDENTIFICATION cm where rownum < 15"""
        template.columnNamesList = ""

        when: "Save through CRUDService"
        template = (NonCaseSQLTemplate) service.save(template)

        then: "Success saved to database"
        template.id != null
    }

    void "Save Non Case SQL template through CRUDService -- Failed"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A Non Case SQL template"
        NonCaseSQLTemplate template = new NonCaseSQLTemplate(templateType: TemplateTypeEnum.CUSTOM_SQL, owner: adminUser, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser")
        template.nonCaseSql = """delete case_num "Case Number" from V_C_IDENTIFICATION cm where rownum < 15"""
        template.columnNamesList = ""

        when: "Save through CRUDService"
        try {
            template = (NonCaseSQLTemplate) service.save(template)
        } catch (ValidationException ve) {
            // Validation Exception: invalid ColumnName
        }

        then: "Failed saved to database"
        template.id == null
    }

    void "Save Query through CRUDService -- Success"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A Case Line Listing template"
        Query query = new Query(queryType: QueryTypeEnum.QUERY_BUILDER, owner: adminUser, name: 'Test query', createdBy: "normalUser", modifiedBy: "normalUser")
        query.JSONQuery = ""
        query.userService = makeUserService()

        when: "Save through CRUDService"
        query = (Query) service.save(query)

        then: "Success saved to database"
        query.id != null
    }

    void "Save Query through CRUDService -- Failed"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A Case Line Listing template"
        Query query = new Query(queryType: QueryTypeEnum.QUERY_BUILDER, owner: adminUser, createdBy: "normalUser", modifiedBy: "normalUser")
        query.JSONQuery = ""

        when: "Save through CRUDService"
        try {
            query = (Query) service.save(query)
        } catch (Exception ve) {
            // Validation Exception: Name cannot be null
        }

        then: "Failed saved to database"
        query.id == null
    }

    void "Save Query Set through CRUDService -- Success"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        def sqlServiceMock = new MockFor(SqlService)
        // Mock twice because they are called twice
        sqlServiceMock.demand.validateQuerySet(2..2) { obj -> return true }
        QuerySet.metaClass.sqlService = sqlServiceMock.proxyInstance()

        and: "A Query Set"
        Query query = new Query(queryType: QueryTypeEnum.QUERY_BUILDER, owner: adminUser, name: 'Test query', createdBy: "normalUser", modifiedBy: "normalUser")
        query.userService = makeUserService()
        query.save(failOnError: true)
        QuerySet querySet = new QuerySet(queryType: QueryTypeEnum.SET_BUILDER, owner: adminUser, name: 'Test query set', createdBy: "normalUser", modifiedBy: "normalUser")
        querySet.addToQueries(query)
        querySet.JSONQuery = null
        querySet.userService = makeUserService()

        when: "Save through CRUDService"
        querySet = (QuerySet) service.save(querySet)

        then: "Success saved to database"
        querySet.id != null
    }

    void "Save Query Set through CRUDService -- Failed"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        def sqlServiceMock = new MockFor(SqlService)
        // Mock twice because they are called twice
        sqlServiceMock.demand.validateQuerySet(2..2) { obj -> return false }
        QuerySet.metaClass.sqlService = sqlServiceMock.proxyInstance()

        and: "A Query Set"
        Query query = new Query(queryType: QueryTypeEnum.QUERY_BUILDER, owner: adminUser, name: 'Test query', createdBy: "normalUser", modifiedBy: "normalUser")
        query.userService = makeUserService()
        query.save(failOnError: true)
        QuerySet querySet = new QuerySet(queryType: QueryTypeEnum.SET_BUILDER, owner: adminUser, name: 'Test query set', createdBy: "normalUser", modifiedBy: "normalUser")
        querySet.addToQueries(query)
        querySet.userService = makeUserService()
        querySet.JSONQuery = ""

        when: "Save through CRUDService"
        try {
            querySet = (QuerySet) service.save(querySet)
        } catch (Exception ve) {
            // Validation Exception: invalid query in set
        }

        then: "Failed saved to database"
        querySet.id == null
    }

    void "Save Custom SQL Query through CRUDService -- Success"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A Custom SQL Query"
        CustomSQLQuery query = new CustomSQLQuery(queryType: QueryTypeEnum.CUSTOM_SQL, owner: adminUser, name: 'Test query', createdBy: "normalUser", modifiedBy: "normalUser")
        query.customSQLQuery = "where country_id = 223"
        query.userService = makeUserService()

        when: "Save through CRUDService"
        query = (CustomSQLQuery) service.save(query)

        then: "Success saved to database"
        query.id != null
    }

    void "Save Custom SQL Query through CRUDService -- Failed"() {
        given: "An admin user"
        def adminUser = makeAdminUser()

        and: "A Custom SQL Query"
        CustomSQLQuery query = new CustomSQLQuery(queryType: QueryTypeEnum.CUSTOM_SQL, owner: adminUser, name: 'Test query', createdBy: "normalUser", modifiedBy: "normalUser")
        query.customSQLQuery = "where delete country_id = 223"
        query.userService = makeUserService()

        when: "Save through CRUDService"
        try {
            query = (CustomSQLQuery) service.save(query)
        } catch (ValidationException ve) {
            // Validation Exception: invalid custom SQL
        }

        then: "Failed saved to database"
        query.id == null
    }

    void "Validate disabling of Audit thread"() {
        given:
        boolean disabled = false
        def closure = {
            disabled = AuditLogListenerThreadLocal.getAuditLogDisabled()
        }
        when:
        service.disableAuditLog(closure)
        then:
        !!disabled
        !AuditLogListenerThreadLocal.getAuditLogDisabled()
    }

    private makeUserService() {
        def userMock = new MockFor(UserService)
        userMock.demand.isCurrentUserAdmin(1..2) { false }
        return userMock.proxyInstance()
    }
}
