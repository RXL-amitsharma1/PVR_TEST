package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.reportTemplate.PercentageOptionEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import org.grails.web.json.JSONArray
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([DataTabulationTemplate, User, ReportTemplate, TemplateQuery])
class TemplateServiceSpec extends Specification implements DataTest, ServiceUnitTest<TemplateService> {
    public static final user = "unitTest"
    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()


    def setupSpec() {
        mockDomains User, Role, UserRole, Preference, Configuration, Tag, CaseLineListingTemplate,
                SourceTableMaster, SourceColumnMaster, ReportField, ReportFieldGroup, TemplateQuery, DateRangeInformation,
                ReportFieldInfo, ReportFieldInfoList, Tenant, CustomSQLTemplate, ReportResultData, ReportResult,
                ReportTemplate, DataTabulationTemplate, NonCaseSQLTemplate, TemplateSet, CustomSQLValue, CustomReportField,
                DataTabulationTemplate, DataTabulationColumnMeasure, DataTabulationMeasure,
                ExecutedCustomSQLTemplate, ExecutedNonCaseSQLTemplate, ExecutedCaseLineListingTemplate, ExecutedDataTabulationTemplate, QueryExpressionValue
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
        CRUDServiceMock.demand.saveWithoutAuditLog(1..3) {Object obj->
            true
        }
        return CRUDServiceMock.proxyInstance()
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

    CustomSQLTemplate createParameterizedCustomSQLTemplate(User user) {
        CustomSQLTemplate customSQLTemplate = new CustomSQLTemplate(columnNamesList: null, factoryDefault: false,
                customSQLTemplateSelectFrom: "select :case_num \"Case Number\" from V_C_IDENTIFICATION cm",
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
        customSQLTemplate.addToCustomSQLValues(new CustomSQLValue(key: ":case_num", value: null))
    }

    NonCaseSQLTemplate createSimpleNonCaseSQLTemplate(User user) {
        new NonCaseSQLTemplate(columnNamesList: "[Case Number]", factoryDefault: false,
                nonCaseSql: "select case_num \"Case Number\" from V_C_IDENTIFICATION cm where rownum < 15",
                usePvrDB: false,
                description: "Case Number",
                customSQLValues: [],
                name: "Custom SQL Template",
                isDeleted: false,
                editable: true,
                hasBlanks: false,
                templateType: TemplateTypeEnum.NON_CASE,
                owner: user,
                createdBy: "admin",
                modifiedBy: "admin")
    }

    NonCaseSQLTemplate createParameterizedNonCaseSQLTemplate(User user) {
        NonCaseSQLTemplate nonCaseSQLTemplate = new NonCaseSQLTemplate(columnNamesList: "[Case Number]", factoryDefault: false,
                nonCaseSql: "select :case_num \"Case Number\" from V_C_IDENTIFICATION cm where rownum < 15",
                usePvrDB: false,
                description: "Case Number",
                customSQLValues: [],
                name: "Custom SQL Template",
                isDeleted: false,
                editable: true,
                hasBlanks: false,
                templateType: TemplateTypeEnum.NON_CASE,
                owner: user,
                createdBy: "admin",
                modifiedBy: "admin")
        nonCaseSQLTemplate.addToCustomSQLValues(new CustomSQLValue(key: ":case_num", value: null))
    }

    CaseLineListingTemplate createSimpleCaseLineListingTemplate(User user) {
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save()
        ReportField field = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class,fieldGroup: fieldGroup, sourceId: 1])
        field.save(vailidate: false)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true)
        new CaseLineListingTemplate(columnList: reportFieldInfoList, factoryDefault: false,
                description: "Case Number",
                name: "Case Line Listing Template",
                isDeleted: false,
                editable: true,
                hasBlanks: false,
                templateType: TemplateTypeEnum.CASE_LINE,
                owner: user,
                createdBy: "admin",
                modifiedBy: "admin")
    }

    DataTabulationTemplate createSimpleDataTabulationTemplate(User user, CountTypeEnum countTypeEnum) {
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save()
        ReportField caseNumField = new ReportField([name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class,fieldGroup: fieldGroup, sourceId: 1])
        caseNumField.save(vailidate: false)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: caseNumField, argusName: "fakeName")
        ReportFieldInfoList rowListReportFieldInfoList = new ReportFieldInfoList()
        rowListReportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true)

        ReportField countryField = new ReportField([name: "countryOfIncidence", description: "This is the Country of Incidence", transform: "countryOfIncidence", dataType: String.class,fieldGroup: fieldGroup, sourceId: 1])
        countryField.save()
        reportFieldInfo = new ReportFieldInfo(reportField: countryField, argusName: "fakeName")
        ReportFieldInfoList columnListReportFieldInfoList = new ReportFieldInfoList()
        columnListReportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(failOnError: true)

        DataTabulationMeasure dataTabulationMeasure = new DataTabulationMeasure(
                type: MeasureTypeEnum.CASE_COUNT,
                name: "Case Count",
                dateRangeCount: countTypeEnum,
                customPeriodFrom: null,
                customPeriodTo: null,
                percentageOption: PercentageOptionEnum.NO_PERCENTAGE,
                showTotal: false,
                showSubtotalRowAfterGroups: false,
                showTotalRowOnly: false,
                showTotalAsColumn: false
        )

        DataTabulationColumnMeasure dataTabulationColumnMeasure = new DataTabulationColumnMeasure(
                columnList: columnListReportFieldInfoList,
                showTotalCumulativeCases: false,
                showTotalIntervalCases: false
        )
        dataTabulationColumnMeasure.addToMeasures(dataTabulationMeasure)

        DataTabulationTemplate dataTabulationTemplate = new DataTabulationTemplate(factoryDefault: false,
                rowList: rowListReportFieldInfoList,
                description: "Case Number",
                name: "Data Tabulation Template",
                isDeleted: false,
                editable: true,
                hasBlanks: false,
                templateType: TemplateTypeEnum.DATA_TAB,
                owner: user,
                createdBy: "admin",
                modifiedBy: "admin")
        dataTabulationTemplate.addToColumnMeasureList(dataTabulationColumnMeasure)
    }

    Configuration createSimpleConfiguration(User user) {
        Date dateRangeStartAbsolute = Date.parse("yyyy-MM-dd HH:mm:ss", "2021-01-18 12:22:30").clearTime()
        Date dateRangeEndAbsolute  = Date.parse("yyyy-MM-dd HH:mm:ss", "2021-01-18 15:22:30").clearTime()
        new Configuration([
                           reportName    : 'SAE - Clinical Reconciliation Death Case',
                           description   : 'Config to identify SAE - Clinical Reconciliation death cases',
                           owner         : user,
                           deliveryOption: new DeliveryOption(sharedWith: [user], attachmentFormats: [ReportFormatEnum.PDF]),
                           isEnabled     : true, createdBy: user.username, modifiedBy: user.username,
                           globalDateRangeInformation:
                                   new GlobalDateRangeInformation(dateRangeStartAbsolute:dateRangeStartAbsolute,
                                           dateRangeEndAbsolute: dateRangeEndAbsolute,
                                           dateRangeEnum: DateRangeEnum.CUMULATIVE,
                                           relativeDateRangeValue: 4)])
    }

    TemplateQuery createTemplateQuery (User admin, DateRangeEnum dateRangeEnum) {
        DataTabulationTemplate tabulationTemplate = createSimpleDataTabulationTemplate(admin, CountTypeEnum.PREVIOUS_PERIOD_COUNT)
        Date dateRangeStartAbsolute = Date.parse("yyyy-MM-dd HH:mm:ss", "2021-01-18 12:22:30").clearTime()
        Date dateRangeEndAbsolute  = Date.parse("yyyy-MM-dd HH:mm:ss", "2021-01-18 15:22:30").clearTime()
        Configuration configuration = createSimpleConfiguration(admin)
        configuration.nextRunDate = dateRangeStartAbsolute
        TemplateQuery.metaClass.getUsedConfiguration = {configuration}
        TemplateQuery.metaClass.getStartDate = {dateRangeStartAbsolute}
        TemplateQuery templateQuery = new TemplateQuery(template: tabulationTemplate,
                dateRangeInformationForTemplateQuery:
                        new DateRangeInformation(dateRangeStartAbsolute:dateRangeStartAbsolute,
                                dateRangeEndAbsolute: dateRangeEndAbsolute,
                                dateRangeEnum: dateRangeEnum),
                createdBy: SeedDataService.USERNAME, modifiedBy: SeedDataService.USERNAME)
        configuration.addToTemplateQueries(templateQuery: [templateQuery])
        templateQuery
    }

    def "Create measures from a JSON String"() {
        given: "A valid json string for measures has 2 measures json object"
        def measuresJson = """[{"name":"Case Count","type":"CASE_COUNT","count":"PERIOD_COUNT","percentage":"NO_PERCENTAGE",
                                "showTotal":false},{"name":"Event Count","type":"EVENT_COUNT","count":"PERIOD_COUNT",
                                "percentage":"NO_PERCENTAGE","showTotal":false}]"""
        def measures
        when: "calling createMeasureListFromJson and passing the json string"
        measures = service.createMeasureListFromJson(new DataTabulationTemplate(), measuresJson)
        then: "the list of measure's size should equals to 2"
        measures.size() == 2
    }

    def "Return empty list when measuresJSON is null"() {
        given: "An empty string for measures"
        def measuresJson = ""
        def measures

        when: "calling createMeasureListFromJson and passing the empty string"
        measures = service.createMeasureListFromJson(new DataTabulationTemplate(), measuresJson)

        then: "should return null"
        measures == null
    }

    void "CustomSQLTemplate is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A custom SQL template"
        CustomSQLTemplate customSQLTemplate = createSimpleCustomSQLTemplate(adminUser)
        customSQLTemplate.save(validate: false)

        when: "The template is copied and saved"
        CustomSQLTemplate customSQLTemplateCopy = (CustomSQLTemplate) service.copyTemplate(customSQLTemplate, adminUser)
        customSQLTemplateCopy.save(validate: false)

        then: "Each property is duplicated correctly"
        assert customSQLTemplate.id != customSQLTemplateCopy.id

        expect: "Common properties are the same"
        customSQLTemplate.description == customSQLTemplateCopy.description
        customSQLTemplate.category == customSQLTemplateCopy.category
        customSQLTemplate.owner == customSQLTemplateCopy.owner
        customSQLTemplate.isDeleted == customSQLTemplateCopy.isDeleted
        customSQLTemplate.hasBlanks == customSQLTemplateCopy.hasBlanks
        customSQLTemplate.tags == customSQLTemplateCopy.tags
        customSQLTemplate.templateType == customSQLTemplateCopy.templateType
        customSQLTemplate.originalTemplateId == customSQLTemplateCopy.originalTemplateId
        customSQLTemplate.factoryDefault == customSQLTemplateCopy.factoryDefault
        customSQLTemplate.editable == customSQLTemplateCopy.editable
        customSQLTemplate.ciomsI == customSQLTemplateCopy.ciomsI
        customSQLTemplate.hasBlanks == customSQLTemplateCopy.hasBlanks
        customSQLTemplate.reassessListedness == customSQLTemplateCopy.reassessListedness

        customSQLTemplate.customSQLTemplateSelectFrom == customSQLTemplateCopy.customSQLTemplateSelectFrom
        customSQLTemplate.customSQLTemplateWhere == customSQLTemplateCopy.customSQLTemplateWhere
        customSQLTemplate.columnNamesList == customSQLTemplateCopy.columnNamesList
    }

    void "Parameterized CustomSQLTemplate is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A custom SQL template"
        CustomSQLTemplate customSQLTemplate = createParameterizedCustomSQLTemplate(adminUser)
        customSQLTemplate.save(validate: false)

        when: "The template is copied and saved"
        CustomSQLTemplate customSQLTemplateCopy = (CustomSQLTemplate) service.copyTemplate(customSQLTemplate, adminUser)
        customSQLTemplateCopy.save(validate: false)

        then: "Each property is duplicated correctly"
        assert customSQLTemplate.id != customSQLTemplateCopy.id
        assert customSQLTemplateCopy.customSQLValues.iterator().next().key == ":case_num"
        assert customSQLTemplateCopy.customSQLValues.iterator().next().id != customSQLTemplate.customSQLValues.iterator().next().id

        expect: "Common properties are the same"
        customSQLTemplate.description == customSQLTemplateCopy.description
        customSQLTemplate.category == customSQLTemplateCopy.category
        customSQLTemplate.owner == customSQLTemplateCopy.owner
        customSQLTemplate.isDeleted == customSQLTemplateCopy.isDeleted
        customSQLTemplate.hasBlanks == customSQLTemplateCopy.hasBlanks
        customSQLTemplate.tags == customSQLTemplateCopy.tags
        customSQLTemplate.templateType == customSQLTemplateCopy.templateType
        customSQLTemplate.originalTemplateId == customSQLTemplateCopy.originalTemplateId
        customSQLTemplate.factoryDefault == customSQLTemplateCopy.factoryDefault
        customSQLTemplate.editable == customSQLTemplateCopy.editable
        customSQLTemplate.ciomsI == customSQLTemplateCopy.ciomsI
        customSQLTemplate.hasBlanks == customSQLTemplateCopy.hasBlanks
        customSQLTemplate.reassessListedness == customSQLTemplateCopy.reassessListedness

        customSQLTemplate.customSQLTemplateSelectFrom == customSQLTemplateCopy.customSQLTemplateSelectFrom
        customSQLTemplate.customSQLTemplateWhere == customSQLTemplateCopy.customSQLTemplateWhere
        customSQLTemplate.columnNamesList == customSQLTemplateCopy.columnNamesList
    }

    void "NonCaseSQLTemplate is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A non-case SQL template"
        NonCaseSQLTemplate nonCaseSQLTemplate = createSimpleNonCaseSQLTemplate(adminUser)
        nonCaseSQLTemplate.save(validate: false)

        when: "The template is copied and saved"
        NonCaseSQLTemplate nonCaseSQLTemplateCopy = (NonCaseSQLTemplate) service.copyTemplate(nonCaseSQLTemplate, adminUser)
        nonCaseSQLTemplateCopy.save(validate: false)

        then: "Each property is duplicated correctly"
        assert nonCaseSQLTemplate.id != nonCaseSQLTemplateCopy.id

        expect: "Common properties are the same"
        nonCaseSQLTemplate.description == nonCaseSQLTemplateCopy.description
        nonCaseSQLTemplate.category == nonCaseSQLTemplateCopy.category
        nonCaseSQLTemplate.owner == nonCaseSQLTemplateCopy.owner
        nonCaseSQLTemplate.isDeleted == nonCaseSQLTemplateCopy.isDeleted
        nonCaseSQLTemplate.hasBlanks == nonCaseSQLTemplateCopy.hasBlanks
        nonCaseSQLTemplate.tags == nonCaseSQLTemplateCopy.tags
        nonCaseSQLTemplate.templateType == nonCaseSQLTemplateCopy.templateType
        nonCaseSQLTemplate.originalTemplateId == nonCaseSQLTemplateCopy.originalTemplateId
        nonCaseSQLTemplate.factoryDefault == nonCaseSQLTemplateCopy.factoryDefault
        nonCaseSQLTemplate.editable == nonCaseSQLTemplateCopy.editable
        nonCaseSQLTemplate.ciomsI == nonCaseSQLTemplateCopy.ciomsI
        nonCaseSQLTemplate.hasBlanks == nonCaseSQLTemplateCopy.hasBlanks
        nonCaseSQLTemplate.reassessListedness == nonCaseSQLTemplateCopy.reassessListedness

        nonCaseSQLTemplate.nonCaseSql == nonCaseSQLTemplateCopy.nonCaseSql
        nonCaseSQLTemplate.usePvrDB == nonCaseSQLTemplateCopy.usePvrDB
        nonCaseSQLTemplate.columnNamesList == nonCaseSQLTemplateCopy.columnNamesList
    }

    void "Parameterized NonCaseSQLTemplate is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A non-case SQL template"
        NonCaseSQLTemplate nonCaseSQLTemplate = createParameterizedNonCaseSQLTemplate(adminUser)
        nonCaseSQLTemplate.save(validate: false)

        when: "The template is copied and saved"
        NonCaseSQLTemplate nonCaseSQLTemplateCopy = (NonCaseSQLTemplate) service.copyTemplate(nonCaseSQLTemplate, adminUser)
        nonCaseSQLTemplateCopy.save(validate: false)

        then: "Each property is duplicated correctly"
        assert nonCaseSQLTemplate.id != nonCaseSQLTemplateCopy.id
        assert nonCaseSQLTemplateCopy.customSQLValues.iterator().next().key == ":case_num"
        assert nonCaseSQLTemplateCopy.customSQLValues.iterator().next().id != nonCaseSQLTemplate.customSQLValues.iterator().next().id

        expect: "Common properties are the same"
        nonCaseSQLTemplate.description == nonCaseSQLTemplateCopy.description
        nonCaseSQLTemplate.category == nonCaseSQLTemplateCopy.category
        nonCaseSQLTemplate.owner == nonCaseSQLTemplateCopy.owner
        nonCaseSQLTemplate.isDeleted == nonCaseSQLTemplateCopy.isDeleted
        nonCaseSQLTemplate.hasBlanks == nonCaseSQLTemplateCopy.hasBlanks
        nonCaseSQLTemplate.tags == nonCaseSQLTemplateCopy.tags
        nonCaseSQLTemplate.templateType == nonCaseSQLTemplateCopy.templateType
        nonCaseSQLTemplate.originalTemplateId == nonCaseSQLTemplateCopy.originalTemplateId
        nonCaseSQLTemplate.factoryDefault == nonCaseSQLTemplateCopy.factoryDefault
        nonCaseSQLTemplate.editable == nonCaseSQLTemplateCopy.editable
        nonCaseSQLTemplate.ciomsI == nonCaseSQLTemplateCopy.ciomsI
        nonCaseSQLTemplate.hasBlanks == nonCaseSQLTemplateCopy.hasBlanks
        nonCaseSQLTemplate.reassessListedness == nonCaseSQLTemplateCopy.reassessListedness

        nonCaseSQLTemplate.nonCaseSql == nonCaseSQLTemplateCopy.nonCaseSql
        nonCaseSQLTemplate.usePvrDB == nonCaseSQLTemplateCopy.usePvrDB
        nonCaseSQLTemplate.columnNamesList == nonCaseSQLTemplateCopy.columnNamesList
    }

    void "Case Line Listing Template is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A case line listing template"
        CaseLineListingTemplate caseLineListingTemplate = createSimpleCaseLineListingTemplate(adminUser)
        caseLineListingTemplate.save(failOnError: true)

        when: "The template is copied and saved"
        CaseLineListingTemplate caseLineListingTemplateCopy = (CaseLineListingTemplate) service.copyTemplate(caseLineListingTemplate, adminUser)
        caseLineListingTemplateCopy.save(failOnError: true)

        then: "Each property is duplicated correctly"
        assert caseLineListingTemplate.id != caseLineListingTemplateCopy.id

        expect: "Common properties are the same"
        caseLineListingTemplate.description == caseLineListingTemplateCopy.description
        caseLineListingTemplate.category == caseLineListingTemplateCopy.category
        caseLineListingTemplate.owner == caseLineListingTemplateCopy.owner
        caseLineListingTemplate.isDeleted == caseLineListingTemplateCopy.isDeleted
        caseLineListingTemplate.hasBlanks == caseLineListingTemplateCopy.hasBlanks
        caseLineListingTemplate.tags == caseLineListingTemplateCopy.tags
        caseLineListingTemplate.templateType == caseLineListingTemplateCopy.templateType
        caseLineListingTemplate.originalTemplateId == caseLineListingTemplateCopy.originalTemplateId
        caseLineListingTemplate.factoryDefault == caseLineListingTemplateCopy.factoryDefault
        caseLineListingTemplate.editable == caseLineListingTemplateCopy.editable
        caseLineListingTemplate.ciomsI == caseLineListingTemplateCopy.ciomsI
        caseLineListingTemplate.hasBlanks == caseLineListingTemplateCopy.hasBlanks
        caseLineListingTemplate.reassessListedness == caseLineListingTemplateCopy.reassessListedness

        caseLineListingTemplate.pageBreakByGroup == caseLineListingTemplateCopy.pageBreakByGroup
        caseLineListingTemplate.columnShowTotal == caseLineListingTemplateCopy.columnShowTotal
        caseLineListingTemplate.columnShowSubTotal == caseLineListingTemplateCopy.columnShowSubTotal
        caseLineListingTemplate.columnShowDistinct == caseLineListingTemplateCopy.columnShowDistinct
        caseLineListingTemplate.hideTotalRowCount == caseLineListingTemplateCopy.hideTotalRowCount
        caseLineListingTemplate.suppressRepeatingValuesColumnList == caseLineListingTemplateCopy.suppressRepeatingValuesColumnList
        caseLineListingTemplate.renamedGrouping == caseLineListingTemplateCopy.renamedGrouping
        caseLineListingTemplate.renamedRowCols == caseLineListingTemplateCopy.renamedRowCols

        caseLineListingTemplate.columnList.id != caseLineListingTemplateCopy.columnList.id
        caseLineListingTemplate.groupingList == caseLineListingTemplateCopy.groupingList
        caseLineListingTemplate.rowColumnList == caseLineListingTemplateCopy.rowColumnList
        caseLineListingTemplate.serviceColumnList == caseLineListingTemplateCopy.serviceColumnList
    }

    void "Data Tabulation Template is copied correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()
        def templateService = new MockFor(TemplateService)
        templateService.demand.getJSONRF { ReportFieldInfoList list ->
            return new JSONArray()
        }
        DataTabulationTemplate.metaClass.getTemplateService = {
            return templateService.proxyInstance()
        }

        and: "A case line listing template"
        DataTabulationTemplate dataTabulationTemplate = createSimpleDataTabulationTemplate(adminUser, CountTypeEnum.PERIOD_COUNT)
        dataTabulationTemplate.save(failOnError: true)

        when: "The template is copied and saved"
        DataTabulationTemplate dataTabulationTemplateCopy = (DataTabulationTemplate) service.copyTemplate(dataTabulationTemplate, adminUser)
        dataTabulationTemplateCopy.save(failOnError: true)

        then: "Each property is duplicated correctly"
        assert dataTabulationTemplate.id != dataTabulationTemplateCopy.id

        expect: "Common properties are the same"
        dataTabulationTemplate.description == dataTabulationTemplateCopy.description
        dataTabulationTemplate.category == dataTabulationTemplateCopy.category
        dataTabulationTemplate.owner == dataTabulationTemplateCopy.owner
        dataTabulationTemplate.isDeleted == dataTabulationTemplateCopy.isDeleted
        dataTabulationTemplate.hasBlanks == dataTabulationTemplateCopy.hasBlanks
        dataTabulationTemplate.tags == dataTabulationTemplateCopy.tags
        dataTabulationTemplate.templateType == dataTabulationTemplateCopy.templateType
        dataTabulationTemplate.originalTemplateId == dataTabulationTemplateCopy.originalTemplateId
        dataTabulationTemplate.factoryDefault == dataTabulationTemplateCopy.factoryDefault
        dataTabulationTemplate.editable == dataTabulationTemplateCopy.editable
        dataTabulationTemplate.ciomsI == dataTabulationTemplateCopy.ciomsI
        dataTabulationTemplate.hasBlanks == dataTabulationTemplateCopy.hasBlanks
        dataTabulationTemplate.reassessListedness == dataTabulationTemplateCopy.reassessListedness

        dataTabulationTemplate.showChartSheet == dataTabulationTemplateCopy.showChartSheet
        dataTabulationTemplate.columnMeasureList != dataTabulationTemplateCopy.columnMeasureList
        dataTabulationTemplate.rowList.id != dataTabulationTemplateCopy.rowList.id
    }

    /**
     *
     *
     *
     *
     * Executed
     *
     *
     *
     *
     */

    void "Executed CustomSQLTemplate is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A custom SQL template"
        CustomSQLTemplate customSQLTemplate = createSimpleCustomSQLTemplate(adminUser)
        customSQLTemplate.save(validate: false)

        and: "A Configuration and a TemplateQuery which uses the template"
        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: customSQLTemplate,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed version of the template is created"
        ExecutedCustomSQLTemplate executedCustomSQLTemplate = (ExecutedCustomSQLTemplate) service.createExecutedReportTemplate(templateQuery)

        then: "Each property is duplicated correctly"
        assert customSQLTemplate.id != executedCustomSQLTemplate.id

        expect: "Common properties are the same"
        customSQLTemplate.description == executedCustomSQLTemplate.description
        customSQLTemplate.category == executedCustomSQLTemplate.category
        customSQLTemplate.owner == executedCustomSQLTemplate.owner
        customSQLTemplate.isDeleted == executedCustomSQLTemplate.isDeleted
        customSQLTemplate.hasBlanks == executedCustomSQLTemplate.hasBlanks
        customSQLTemplate.tags == executedCustomSQLTemplate.tags
        customSQLTemplate.templateType == executedCustomSQLTemplate.templateType
        customSQLTemplate.factoryDefault == executedCustomSQLTemplate.factoryDefault
        customSQLTemplate.editable == executedCustomSQLTemplate.editable
        customSQLTemplate.ciomsI == executedCustomSQLTemplate.ciomsI
        customSQLTemplate.hasBlanks == executedCustomSQLTemplate.hasBlanks
        customSQLTemplate.reassessListedness == executedCustomSQLTemplate.reassessListedness

        customSQLTemplate.customSQLTemplateSelectFrom == executedCustomSQLTemplate.customSQLTemplateSelectFrom
        customSQLTemplate.customSQLTemplateWhere == executedCustomSQLTemplate.customSQLTemplateWhere
        customSQLTemplate.columnNamesList == executedCustomSQLTemplate.columnNamesList

        customSQLTemplate.originalTemplateId != executedCustomSQLTemplate.originalTemplateId
    }

    void "Executed Parameterized CustomSQLTemplate is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A custom SQL template"
        CustomSQLTemplate customSQLTemplate = createParameterizedCustomSQLTemplate(adminUser)
        customSQLTemplate.save(validate: false)

        and: "A Configuration and a TemplateQuery which uses the template"
        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: customSQLTemplate,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed version of the template is created"
        ExecutedCustomSQLTemplate executedCustomSQLTemplate = (ExecutedCustomSQLTemplate) service.createExecutedReportTemplate(templateQuery)

        then: "Each property is duplicated correctly"
        assert customSQLTemplate.id != executedCustomSQLTemplate.id
        assert executedCustomSQLTemplate.customSQLValues.iterator().next().key == ":case_num"
        assert executedCustomSQLTemplate.customSQLValues.iterator().next().id != customSQLTemplate.customSQLValues.iterator().next().id

        expect: "Common properties are the same"
        customSQLTemplate.description == executedCustomSQLTemplate.description
        customSQLTemplate.category == executedCustomSQLTemplate.category
        customSQLTemplate.owner == executedCustomSQLTemplate.owner
        customSQLTemplate.isDeleted == executedCustomSQLTemplate.isDeleted
        customSQLTemplate.hasBlanks == executedCustomSQLTemplate.hasBlanks
        customSQLTemplate.tags == executedCustomSQLTemplate.tags
        customSQLTemplate.templateType == executedCustomSQLTemplate.templateType
        customSQLTemplate.factoryDefault == executedCustomSQLTemplate.factoryDefault
        customSQLTemplate.editable == executedCustomSQLTemplate.editable
        customSQLTemplate.ciomsI == executedCustomSQLTemplate.ciomsI
        customSQLTemplate.hasBlanks == executedCustomSQLTemplate.hasBlanks
        customSQLTemplate.reassessListedness == executedCustomSQLTemplate.reassessListedness

        customSQLTemplate.customSQLTemplateSelectFrom == executedCustomSQLTemplate.customSQLTemplateSelectFrom
        customSQLTemplate.customSQLTemplateWhere == executedCustomSQLTemplate.customSQLTemplateWhere
        customSQLTemplate.columnNamesList == executedCustomSQLTemplate.columnNamesList

        customSQLTemplate.originalTemplateId != executedCustomSQLTemplate.originalTemplateId
    }

    void "Executed NonCaseSQLTemplate is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A non-case SQL template"
        NonCaseSQLTemplate nonCaseSQLTemplate = createSimpleNonCaseSQLTemplate(adminUser)
        nonCaseSQLTemplate.save(validate: false)

        and: "A Configuration and a TemplateQuery which uses the template"
        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: nonCaseSQLTemplate,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed version of the template is created"
        ExecutedNonCaseSQLTemplate executedNonCaseSQLTemplate = (ExecutedNonCaseSQLTemplate) service.createExecutedReportTemplate(templateQuery)

        then: "Each property is duplicated correctly"
        assert nonCaseSQLTemplate.id != executedNonCaseSQLTemplate.id

        expect: "Common properties are the same"
        nonCaseSQLTemplate.description == executedNonCaseSQLTemplate.description
        nonCaseSQLTemplate.category == executedNonCaseSQLTemplate.category
        nonCaseSQLTemplate.owner == executedNonCaseSQLTemplate.owner
        nonCaseSQLTemplate.isDeleted == executedNonCaseSQLTemplate.isDeleted
        nonCaseSQLTemplate.hasBlanks == executedNonCaseSQLTemplate.hasBlanks
        nonCaseSQLTemplate.tags == executedNonCaseSQLTemplate.tags
        nonCaseSQLTemplate.templateType == executedNonCaseSQLTemplate.templateType
        nonCaseSQLTemplate.factoryDefault == executedNonCaseSQLTemplate.factoryDefault
        nonCaseSQLTemplate.editable == executedNonCaseSQLTemplate.editable
        nonCaseSQLTemplate.ciomsI == executedNonCaseSQLTemplate.ciomsI
        nonCaseSQLTemplate.hasBlanks == executedNonCaseSQLTemplate.hasBlanks
        nonCaseSQLTemplate.reassessListedness == executedNonCaseSQLTemplate.reassessListedness

        nonCaseSQLTemplate.nonCaseSql == executedNonCaseSQLTemplate.nonCaseSql
        nonCaseSQLTemplate.usePvrDB == executedNonCaseSQLTemplate.usePvrDB
        nonCaseSQLTemplate.columnNamesList == executedNonCaseSQLTemplate.columnNamesList

        nonCaseSQLTemplate.originalTemplateId != executedNonCaseSQLTemplate.originalTemplateId
    }

    void "Executed Parameterized NonCaseSQLTemplate is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A non-case SQL template"
        NonCaseSQLTemplate nonCaseSQLTemplate = createParameterizedNonCaseSQLTemplate(adminUser)
        nonCaseSQLTemplate.save(validate: false)

        and: "A Configuration and a TemplateQuery which uses the template"
        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: nonCaseSQLTemplate,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed version of the template is created"
        ExecutedNonCaseSQLTemplate executedNonCaseSQLTemplate = (ExecutedNonCaseSQLTemplate) service.createExecutedReportTemplate(templateQuery)

        then: "Each property is duplicated correctly"
        assert nonCaseSQLTemplate.id != executedNonCaseSQLTemplate.id
        assert executedNonCaseSQLTemplate.customSQLValues.iterator().next().key == ":case_num"
        assert executedNonCaseSQLTemplate.customSQLValues.iterator().next().id != nonCaseSQLTemplate.customSQLValues.iterator().next().id

        expect: "Common properties are the same"
        nonCaseSQLTemplate.description == executedNonCaseSQLTemplate.description
        nonCaseSQLTemplate.category == executedNonCaseSQLTemplate.category
        nonCaseSQLTemplate.owner == executedNonCaseSQLTemplate.owner
        nonCaseSQLTemplate.isDeleted == executedNonCaseSQLTemplate.isDeleted
        nonCaseSQLTemplate.hasBlanks == executedNonCaseSQLTemplate.hasBlanks
        nonCaseSQLTemplate.tags == executedNonCaseSQLTemplate.tags
        nonCaseSQLTemplate.templateType == executedNonCaseSQLTemplate.templateType
        nonCaseSQLTemplate.factoryDefault == executedNonCaseSQLTemplate.factoryDefault
        nonCaseSQLTemplate.editable == executedNonCaseSQLTemplate.editable
        nonCaseSQLTemplate.ciomsI == executedNonCaseSQLTemplate.ciomsI
        nonCaseSQLTemplate.hasBlanks == executedNonCaseSQLTemplate.hasBlanks
        nonCaseSQLTemplate.reassessListedness == executedNonCaseSQLTemplate.reassessListedness

        nonCaseSQLTemplate.nonCaseSql == executedNonCaseSQLTemplate.nonCaseSql
        nonCaseSQLTemplate.usePvrDB == executedNonCaseSQLTemplate.usePvrDB
        nonCaseSQLTemplate.columnNamesList == executedNonCaseSQLTemplate.columnNamesList

        nonCaseSQLTemplate.originalTemplateId != executedNonCaseSQLTemplate.originalTemplateId
    }

    void "Executed Case Line Listing Template is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A case line listing template"
        CaseLineListingTemplate caseLineListingTemplate = createSimpleCaseLineListingTemplate(adminUser)
        caseLineListingTemplate.save(failOnError: true)

        and: "A Configuration and a TemplateQuery which uses the template"
        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: caseLineListingTemplate,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed version of the template is created"
        ExecutedCaseLineListingTemplate executedCaseLineListingTemplate = (ExecutedCaseLineListingTemplate) service.createExecutedReportTemplate(templateQuery)

        then: "Each property is duplicated correctly"
        assert caseLineListingTemplate.id != executedCaseLineListingTemplate.id

        expect: "Common properties are the same"
        caseLineListingTemplate.description == executedCaseLineListingTemplate.description
        caseLineListingTemplate.category == executedCaseLineListingTemplate.category
        caseLineListingTemplate.owner == executedCaseLineListingTemplate.owner
        caseLineListingTemplate.isDeleted == executedCaseLineListingTemplate.isDeleted
        caseLineListingTemplate.hasBlanks == executedCaseLineListingTemplate.hasBlanks
        caseLineListingTemplate.tags == executedCaseLineListingTemplate.tags
        caseLineListingTemplate.templateType == executedCaseLineListingTemplate.templateType
        caseLineListingTemplate.factoryDefault == executedCaseLineListingTemplate.factoryDefault
        caseLineListingTemplate.editable == executedCaseLineListingTemplate.editable
        caseLineListingTemplate.ciomsI == executedCaseLineListingTemplate.ciomsI
        caseLineListingTemplate.hasBlanks == executedCaseLineListingTemplate.hasBlanks
        caseLineListingTemplate.reassessListedness == executedCaseLineListingTemplate.reassessListedness

        caseLineListingTemplate.pageBreakByGroup == executedCaseLineListingTemplate.pageBreakByGroup
        caseLineListingTemplate.columnShowTotal == executedCaseLineListingTemplate.columnShowTotal
        caseLineListingTemplate.columnShowSubTotal == executedCaseLineListingTemplate.columnShowSubTotal
        caseLineListingTemplate.hideTotalRowCount == executedCaseLineListingTemplate.hideTotalRowCount
        caseLineListingTemplate.columnShowDistinct == executedCaseLineListingTemplate.columnShowDistinct
        caseLineListingTemplate.suppressRepeatingValuesColumnList == executedCaseLineListingTemplate.suppressRepeatingValuesColumnList
        caseLineListingTemplate.renamedGrouping == executedCaseLineListingTemplate.renamedGrouping
        caseLineListingTemplate.renamedRowCols == executedCaseLineListingTemplate.renamedRowCols

        caseLineListingTemplate.columnList.id != executedCaseLineListingTemplate.columnList.id
        caseLineListingTemplate.groupingList == executedCaseLineListingTemplate.groupingList
        caseLineListingTemplate.rowColumnList == executedCaseLineListingTemplate.rowColumnList
        caseLineListingTemplate.serviceColumnList == executedCaseLineListingTemplate.serviceColumnList

        caseLineListingTemplate.originalTemplateId != executedCaseLineListingTemplate.originalTemplateId
    }

    void "Executed  Data Tabulation Template is created correctly"() {
        given: "A user"
        User adminUser = makeAdminUser()
        def templateService = new MockFor(TemplateService)
        templateService.demand.getJSONRF { ReportFieldInfoList list ->
            return new JSONArray()
        }
        DataTabulationTemplate.metaClass.getTemplateService = {
            return templateService.proxyInstance()
        }

        and: "A case line listing template"
        DataTabulationTemplate dataTabulationTemplate = createSimpleDataTabulationTemplate(adminUser, CountTypeEnum.PERIOD_COUNT)
        dataTabulationTemplate.save(failOnError: true)

        and: "A Configuration and a TemplateQuery which uses the template"
        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: dataTabulationTemplate,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed version of the template is created"
        ExecutedDataTabulationTemplate executedDataTabulationTemplate = (ExecutedDataTabulationTemplate) service.createExecutedReportTemplate(templateQuery)

        then: "Each property is duplicated correctly"
        assert dataTabulationTemplate.id != executedDataTabulationTemplate.id

        expect: "Common properties are the same"
        dataTabulationTemplate.description == executedDataTabulationTemplate.description
        dataTabulationTemplate.category == executedDataTabulationTemplate.category
        dataTabulationTemplate.owner == executedDataTabulationTemplate.owner
        dataTabulationTemplate.isDeleted == executedDataTabulationTemplate.isDeleted
        dataTabulationTemplate.hasBlanks == executedDataTabulationTemplate.hasBlanks
        dataTabulationTemplate.tags == executedDataTabulationTemplate.tags
        dataTabulationTemplate.templateType == executedDataTabulationTemplate.templateType
        dataTabulationTemplate.factoryDefault == executedDataTabulationTemplate.factoryDefault
        dataTabulationTemplate.editable == executedDataTabulationTemplate.editable
        dataTabulationTemplate.ciomsI == executedDataTabulationTemplate.ciomsI
        dataTabulationTemplate.hasBlanks == executedDataTabulationTemplate.hasBlanks
        dataTabulationTemplate.reassessListedness == executedDataTabulationTemplate.reassessListedness

        dataTabulationTemplate.showChartSheet == executedDataTabulationTemplate.showChartSheet
        dataTabulationTemplate.columnMeasureList != executedDataTabulationTemplate.columnMeasureList
        dataTabulationTemplate.rowList.id != executedDataTabulationTemplate.rowList.id

        dataTabulationTemplate.originalTemplateId != executedDataTabulationTemplate.originalTemplateId
    }

    void "test getTemplateListForTemplateSet"() {
        given: "A user"
        User adminUser = makeAdminUser()

        def userService = Mock(UserService)
        userService.getUser() >> {
            return adminUser
        }
        service.userService = userService

        and: "A case line listing template"
        CaseLineListingTemplate caseLineListingTemplate = createSimpleCaseLineListingTemplate(adminUser)
        caseLineListingTemplate.qualityChecked = true
        caseLineListingTemplate.save(failOnError: true, validate: false, flush: true)

        ReportTemplate.metaClass.static.ownedByUserWithSearchNoBlank = { User user, String search, TemplateTypeEnum abc, boolean includeCllWithCustomSql -> new Object() {
            List list(Object o) {
                return [[caseLineListingTemplate.id, caseLineListingTemplate.name, caseLineListingTemplate.description, caseLineListingTemplate.qualityChecked, caseLineListingTemplate.hasBlanks, false, adminUser.fullName, caseLineListingTemplate.groupingList]]
            }
            Integer count(Object o) {
                return   1
            }
        }

        }

        when: "Called"
        Map result = service.getTemplateListForTemplateSet(null, null, 0, 20, TemplateTypeEnum.CASE_LINE, false )

        then: result.list[0].qualityChecked == true
    }

    void "Executed Data Tabulation Template with Interactive Output On is created with interactiveOutput set as true"() {
        given: "A user"
        User adminUser = makeAdminUser()
        def templateService = new MockFor(TemplateService)
        templateService.demand.getJSONRF { ReportFieldInfoList list ->
            return new JSONArray()
        }
        DataTabulationTemplate.metaClass.getTemplateService = {
            return templateService.proxyInstance()
        }

        and: "A case line listing template"
        DataTabulationTemplate dataTabulationTemplate = createSimpleDataTabulationTemplate(adminUser, CountTypeEnum.PERIOD_COUNT)
        dataTabulationTemplate.interactiveOutput = true
        dataTabulationTemplate.save(failOnError: true)

        and: "A Configuration and a TemplateQuery which uses the template"
        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: dataTabulationTemplate,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed version of the template is created"
        ExecutedDataTabulationTemplate executedDataTabulationTemplate = (ExecutedDataTabulationTemplate) service.createExecutedReportTemplate(templateQuery)

        then: "interactive output property is copied correctly"
        assert dataTabulationTemplate.id != executedDataTabulationTemplate.id

        expect: "interactiveOutput is true"
        executedDataTabulationTemplate.interactiveOutput == true
        dataTabulationTemplate.originalTemplateId != executedDataTabulationTemplate.originalTemplateId
    }

    void "Executed Case Line Listing Template with Interactive Output On is created with interactiveOutput set as true"() {
        given: "A user"
        User adminUser = makeAdminUser()

        and: "A case line listing template"
        CaseLineListingTemplate caseLineListingTemplate = createSimpleCaseLineListingTemplate(adminUser)
        caseLineListingTemplate.interactiveOutput = true
        caseLineListingTemplate.save(failOnError: true)

        and: "A Configuration and a TemplateQuery which uses the template"
        Configuration config = createSimpleConfiguration(adminUser)
        TemplateQuery templateQuery = new TemplateQuery(template: caseLineListingTemplate,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: adminUser.username,
                modifiedBy: adminUser.username)
        config.addToTemplateQueries(templateQuery)
        config.save(validate: false)

        service.CRUDService = mockCRUDService()

        when: "An executed version of the template is created"
        ExecutedCaseLineListingTemplate executedCaseLineListingTemplate = (ExecutedCaseLineListingTemplate) service.createExecutedReportTemplate(templateQuery)

        then: "Each property is duplicated correctly"
        assert caseLineListingTemplate.id != executedCaseLineListingTemplate.id

        expect: "interactiveOutput is true"
        executedCaseLineListingTemplate.interactiveOutput == true
        caseLineListingTemplate.originalTemplateId != executedCaseLineListingTemplate.originalTemplateId
    }

    void "Data Tabulation Template is copied correctly with interactiveOutput field"() {
        given: "A user"
        User adminUser = makeAdminUser()
        def templateService = new MockFor(TemplateService)
        templateService.demand.getJSONRF { ReportFieldInfoList list ->
            return new JSONArray()
        }
        DataTabulationTemplate.metaClass.getTemplateService = {
            return templateService.proxyInstance()
        }

        and: "A case line listing template"
        DataTabulationTemplate dataTabulationTemplate = createSimpleDataTabulationTemplate(adminUser, CountTypeEnum.PERIOD_COUNT)
        dataTabulationTemplate.interactiveOutput = true
        dataTabulationTemplate.save(failOnError: true)

        when: "The template is copied and saved"
        DataTabulationTemplate dataTabulationTemplateCopy = (DataTabulationTemplate) service.copyTemplate(dataTabulationTemplate, adminUser)
        dataTabulationTemplateCopy.save(failOnError: true)

        then: "Each property is duplicated correctly"
        assert dataTabulationTemplate.id != dataTabulationTemplateCopy.id

        expect: "Common properties are the same"
        dataTabulationTemplate.interactiveOutput == dataTabulationTemplateCopy.interactiveOutput
        dataTabulationTemplateCopy.interactiveOutput == true

        dataTabulationTemplate.rowList.id != dataTabulationTemplateCopy.rowList.id
    }

    void "Test getPrevPeriodStartDateAndEndDate"() {
        given: "Creating all the required stuffs"
        User adminUser = makeAdminUser()
        TemplateQuery templateQuery = createTemplateQuery(adminUser, daterange)

        when:
        def list = service.getPrevPeriodStartDateAndEndDate(templateQuery)

        then:
        list == result

        where:
        daterange                     |   result
        DateRangeEnum.CUMULATIVE      |   ["18-01-2021", new Date().format("dd-MM-yyyy").toString()]
        DateRangeEnum.YESTERDAY       |   ["17-01-2021", "17-01-2021"]
        DateRangeEnum.LAST_WEEK       |   ["10-01-2021", "16-01-2021"]
        DateRangeEnum.LAST_MONTH      |   ["01-12-2020", "31-12-2020"]
        DateRangeEnum.LAST_YEAR       |   ["01-01-2020", "31-12-2020"]
        DateRangeEnum.LAST_X_DAYS     |   ["17-01-2021", "17-01-2021"]
        DateRangeEnum.LAST_X_WEEKS    |   ["10-01-2021", "16-01-2021"]
        DateRangeEnum.LAST_X_MONTHS   |   ["01-12-2020", "31-12-2020"]
        DateRangeEnum.LAST_X_YEARS    |   ["01-01-2020", "31-12-2020"]
        DateRangeEnum.TOMORROW        |   ["17-01-2021", "17-01-2021"]
        DateRangeEnum.NEXT_WEEK       |   ["10-01-2021", "16-01-2021"]
        DateRangeEnum.NEXT_MONTH      |   ["01-12-2020", "31-12-2020"]
        DateRangeEnum.NEXT_X_DAYS     |   ["17-01-2021", "17-01-2021"]
        DateRangeEnum.NEXT_YEAR       |   ["01-01-2020", "31-12-2020"]
        DateRangeEnum.NEXT_X_WEEKS    |   ["10-01-2021", "16-01-2021"]
        DateRangeEnum.NEXT_X_MONTHS   |   ["01-12-2020", "31-12-2020"]
        DateRangeEnum.NEXT_X_YEARS    |   ["01-01-2020", "31-12-2020"]
        DateRangeEnum.PR_DATE_RANGE   |   ["18-01-2021", new Date().format("dd-MM-yyyy").toString()] 

    }

    void "test getDataTabColumnsAndRows"(){
        JSONArray tabHeaders = (JSONArray) JSON.parse("[{\"ROW_1\":\"test\"} ]")
        when:
        def result = service.getDataTabColumnsAndRows(tabHeaders, null)
        then:
        result.rows.size() == 1
    }

    void "test getJSONRF"(){
        given:
        ReportFieldGroup fieldGroup = new ReportFieldGroup(name: "Case Information")
        fieldGroup.save(flush:true,validate:false)
        ReportField field = new ReportField(name: "caseNumber", description: "This is the Case number", transform: "caseNumber", dataType: String.class,fieldGroup: fieldGroup, sourceId: 1)
        field.save(flush:true,vailidate: false)
        CustomReportField customField = new CustomReportField(reportField: field,fieldGroup:fieldGroup,customName: 'report_field_test',defaultExpression: 'abc')
        customField.save(flush:true,validate: false)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field,customField: customField, argusName: "fakeName",renameValue: "test",drillDownTemplate:new CaseLineListingTemplate().save(flush:true, validate:false))
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList(reportFieldInfoList: [reportFieldInfo])
        reportFieldInfo.save(flush:true,validate:false)

        reportFieldInfoList.save(flush:true,validate: false)
        def mockCustomMessageService = new MockFor(CustomMessageService)
        mockCustomMessageService.demand.getMessage(0..2){String code, Object... args = null ->
            return "message"
        }
        service.customMessageService = mockCustomMessageService.proxyInstance()
        service.userService = [currentUser: null]
        when:
        def resultMap = service.getJSONRF(reportFieldInfoList,new Locale("en"))
        then:
        resultMap[0].renameValue=="test"
    }
}
