package reports

import com.rxlogix.SqlGenerationService
import com.rxlogix.config.*
import com.rxlogix.enums.QueryLevelEnum
import com.rxlogix.test.TestUtils
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import spock.lang.Shared
import spock.lang.Specification

import static com.rxlogix.util.Strings.collapseWhitespace

@Integration
@Rollback
class SqlGenerationServiceSpec extends Specification {

    @Shared
    SourceProfile sourceProfile

    static private final Integer DEFAULT_TENANT_ID = 1

    def setup() {
        sourceProfile = TestUtils.createSourceProfile()
    }

    def cleanup() {}


    void "Query Level: generate QuerySQL for Case Level Query"() {
        given: "A configuration with a Case level query and a template"
        def service = new SqlGenerationService()
        String JSONQuery = /{ "all": { "containerGroups": [   { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" }  ] }  ] } }/
        Query query = new Query([queryLevel: QueryLevelEnum.CASE, name: 'Case Level Query: Country = US', JSONQuery: JSONQuery, tenantId: DEFAULT_TENANT_ID.toLong()])

        CaseLineListingTemplate template = new CaseLineListingTemplate(name: 'Test template: Case Number', tenantId: DEFAULT_TENANT_ID.toLong(),
                columnList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(reportField: ReportField.findByNameAndIsDeleted("masterCaseNum",false))]))
        Configuration configuration = new Configuration(sourceProfile: sourceProfile)
        def templateQuery = new TemplateQuery(template: template, tenantId: DEFAULT_TENANT_ID.toLong(), query: query, dateRangeInformationForTemplateQuery: new DateRangeInformation(),
                queryLevel: QueryLevelEnum.CASE)
        configuration.addToTemplateQueries(templateQuery)

        when: "Generating Query SQL"
        String result = service.generateQuerySQL(templateQuery, query, false, false, 0, new Locale("en"))

        then: "Query SQL is Case Level"
        templateQuery.queryLevel == QueryLevelEnum.CASE
        collapseWhitespace(result) == collapseWhitespace("""select cm.TENANT_ID, cm.case_id, cm.version_num from V_C_IDENTIFICATION cm join gtt_versions ver on (cm.case_id = ver.case_id and cm.version_num = ver.version_num) LEFT JOIN VW_COUNTRIES vc1_1 ON (cm.OCCURED_COUNTRY_ID = vc1_1.COUNTRY_ID AND cm.TENANT_ID = vc1_1.TENANT_ID) where ((UPPER(vc1_1.COUNTRY) = UPPER('UNITED STATES')))""");
    }

    void "Query Level: generate QuerySQL for Product & Event Level Query"() {
        given: "A configuration with a Product & Event level query and a template"
        def service = new SqlGenerationService()
        String JSONQuery = /{ "all": { "containerGroups": [   { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" }  ] }  ] } }/
        Query query = new Query([queryLevel: QueryLevelEnum.PRODUCT_EVENT, name: 'Product & Event Level Query: Country = US', JSONQuery: JSONQuery, tenantId: DEFAULT_TENANT_ID.toLong()])

        CaseLineListingTemplate template = new CaseLineListingTemplate(name: 'Test template: Case Number', tenantId: DEFAULT_TENANT_ID.toLong(),
                columnList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(reportField: ReportField.findByNameAndIsDeleted("masterCaseNum",false))]))
        Configuration configuration = new Configuration(sourceProfile: sourceProfile, tenantId: DEFAULT_TENANT_ID.toLong())
        def templateQuery = new TemplateQuery(template: template, query: query, dateRangeInformationForTemplateQuery: new DateRangeInformation(),
                queryLevel: QueryLevelEnum.PRODUCT_EVENT)
        configuration.addToTemplateQueries(templateQuery)

        when: "Generating Query SQL"
        String result = service.generateQuerySQL(templateQuery, query, false, false, 0, new Locale("en"))

        then: "Query SQL contains join C_AE_IDENTIFICATION and C_PROD_IDENTIFICATION"
        templateQuery.queryLevel == QueryLevelEnum.PRODUCT_EVENT
        collapseWhitespace(result) == collapseWhitespace("""select cm.TENANT_ID, cm.case_id, cp.prod_rec_num prod_rec_num, cp.version_num prod_version_num, ce.AE_REC_NUM, cm.version_num from V_C_IDENTIFICATION cm join gtt_versions ver on (cm.case_id = ver.case_id and cm.version_num = ver.version_num) Left join C_AE_IDENTIFICATION ce on (cm.CASE_ID = ce.CASE_ID AND cm.version_num = ce.version_num AND cm.TENANT_ID = ce.TENANT_ID ) Left join C_PROD_IDENTIFICATION cp on (cm.CASE_ID = cp.CASE_ID AND cm.version_num = cp.version_num AND cm.TENANT_ID = cp.TENANT_ID ) LEFT JOIN VW_COUNTRIES vc1_1 ON (cm.OCCURED_COUNTRY_ID = vc1_1.COUNTRY_ID AND cm.TENANT_ID = vc1_1.TENANT_ID) where ((UPPER(vc1_1.COUNTRY) = UPPER('UNITED STATES')))""")
    }

    void "Query Level: generate ReportSQL for Case Level Query"() {
        given: "A configuration with a Case level query and a template"
        def service = new SqlGenerationService()
        String JSONQuery = /{ "all": { "containerGroups": [   { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" }  ] }  ] } }/
        Query query = new Query([queryLevel: QueryLevelEnum.CASE, name: 'Case Level Query: Country = US', JSONQuery: JSONQuery, tenantId: DEFAULT_TENANT_ID.toLong()])
        CaseLineListingTemplate template = new CaseLineListingTemplate(name: 'Test template: Case Number', tenantId: DEFAULT_TENANT_ID.toLong(),
                columnList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(reportField: ReportField.findByNameAndIsDeleted("masterCaseNum",false))]))
        Configuration configuration = new Configuration(sourceProfile: sourceProfile, tenantId: DEFAULT_TENANT_ID.toLong())
        def templateQuery = new TemplateQuery(template: template, query: query, dateRangeInformationForTemplateQuery: new DateRangeInformation(),
                queryLevel: QueryLevelEnum.CASE)
        configuration.addToTemplateQueries(templateQuery)

        when: "Generating Query SQL"
        String result = service.generateReportSQL(templateQuery, true, templateQuery.template, new Locale("en"))

        then: "Report SQL is Case Level"
        templateQuery.queryLevel == QueryLevelEnum.CASE
        collapseWhitespace(result) == collapseWhitespace("""select cm.CASE_NUM AS CASE_NUM0 from V_C_IDENTIFICATION cm where exists (select 1 from gtt_query_case_list caseList where cm.case_id = caseList.case_id and cm.version_num = caseList.version_num and caseList.TENANT_ID = cm.TENANT_ID ) ORDER BY CM.CASE_NUM""")
    }

    void "Query Level: generate ReportSQL for Product & Event Level Query"() {
        given: "A configuration with a Product & Event level query and a template"
        def service = new SqlGenerationService()
        String JSONQuery = /{ "all": { "containerGroups": [   { "expressions": [  { "index": "0", "field": "masterCountryId", "op": "EQUALS", "value": "UNITED STATES" }  ] }  ] } }/
        Query query = new Query([queryLevel: QueryLevelEnum.PRODUCT_EVENT, name: 'Product & Event Level Query: Country = US', JSONQuery: JSONQuery, tenantId: DEFAULT_TENANT_ID.toLong()])
        CaseLineListingTemplate template = new CaseLineListingTemplate(name: 'Test template: Case Number', tenantId: DEFAULT_TENANT_ID.toLong(),
                columnList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(reportField: ReportField.findByNameAndIsDeleted("masterCaseNum",false))]))
        Configuration configuration = new Configuration(sourceProfile: sourceProfile, tenantId: DEFAULT_TENANT_ID.toLong())
        def templateQuery = new TemplateQuery(template: template, query: query, dateRangeInformationForTemplateQuery: new DateRangeInformation(),
                queryLevel: QueryLevelEnum.PRODUCT_EVENT)
        configuration.addToTemplateQueries(templateQuery)

        when: "Generating Query SQL"
        String result = service.generateReportSQL(templateQuery, true, templateQuery.template, new Locale("en"))

        then: "Report SQL is normal"
        templateQuery.queryLevel == QueryLevelEnum.PRODUCT_EVENT
        collapseWhitespace(result) == collapseWhitespace("""select cm.CASE_NUM AS CASE_NUM0 from V_C_IDENTIFICATION cm Left join C_AE_IDENTIFICATION ce on (cm.CASE_ID = ce.CASE_ID and cm.version_num = ce.version_num AND cm.TENANT_ID = ce.TENANT_ID ) Left join C_PROD_IDENTIFICATION cp on (cm.CASE_ID = cp.CASE_ID and cm.version_num = cp.version_num AND cm.TENANT_ID = cp.TENANT_ID ) where exists (select 1 from gtt_query_case_list caseList where cm.case_id = caseList.case_id and cm.version_num = caseList.version_num and caseList.TENANT_ID = cm.TENANT_ID and ce.AE_REC_NUM = caseList.prod_rec_num and cp.prod_rec_num = caseList.prod_rec_num) ORDER BY CM.CASE_NUM""")
    }
}
