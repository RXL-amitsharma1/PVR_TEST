package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.validation.ValidationException
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class ReportExecutorServiceSpec extends Specification implements DataTest, ServiceUnitTest<ReportExecutorService> {

    @Shared User normalUser
    @Shared Preference preference
    @Shared Configuration configuration

    def setupSpec() {
        mockDomains CaseLineListingTemplate, Tag, SourceColumnMaster, Configuration, ReportResult, ReportFieldInfoList, ReportFieldInfo, User, Role, UserRole, Preference, Tenant, TemplateQuery, SharedWith, ExecutedConfiguration, ExecutedPeriodicReportConfiguration, ExecutedTemplateQuery, DeliveryOption, ExecutedDeliveryOption, ReportTemplate, ReportField, TemplateQuery, Query, TemplateSet, ExecutedIcsrProfileConfiguration
    }

    def setup() {
        def username = "unitTest"
        preference = new Preference(locale: new Locale("en"), createdBy: username, modifiedBy: username)
        normalUser = createUser(username, "ROLE_TEMPLATE_VIEW")
        configuration = createConfiguration(normalUser)
    }

    void "Test that error occurs when creating ExecutedConfiguration"() {
        given:
        def resultData = new ReportResultData(value: "unit test".bytes, versionSQL: "", querySQL: "", reportSQL: "")
        def result = new ReportResult(data: resultData)

        when: "Creating ExecutedConfiguration"
        result.save(flush: true,failOnError: true)

        then: "ValidationException is thrown while trying to save the report result because ReportResult has missing fields"
        thrown ValidationException
    }

    void "For execute report: catch Exception of any type "() {
        given: "An empty result"
        def config = new ExecutedPeriodicReportConfiguration(executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF]), owner: normalUser, createdBy: normalUser.username, modifiedBy: normalUser.username)
        def templateQuery  = new ExecutedTemplateQuery(executedTemplate: new CaseLineListingTemplate(),executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(), executedConfiguration: config, createdBy: normalUser.username, modifiedBy: normalUser.username)
        config.addToExecutedTemplateQueries(templateQuery)
        def result = new ReportResult(executedTemplateQuery: templateQuery)
        when: "Executing this report"
        service.invokeMethod('generateReportResult', [result, false] as Object[])

        then: "Exception is thrown while trying generate a report result because ReportResult has missing fields"
        thrown Exception
    }

    void "Test that error occurs during execution of report throws ExecutionStatusException"() {
        given:
        service.metaClass.executeReportJob = {
            throw new ExecutionStatusException()
        }

        when: "Executing a report"
        service.invokeMethod('executeReportsForConfiguration', [new Configuration()] as Object[])

        then: "It catches an Execution Status exception"
        thrown Exception
    }

    void "Test that error occurs during delivery of report throws ExecutionStatusException"() {
        given:
        service.metaClass.setDeliveringStatus = {
            true
        }
        service.metaClass.deliverReport = {ExecutedConfiguration executedConfiguration ->
            throw new ExecutionStatusException()
        }

        when: "Delivering a report"
        service.deliverExecutedReport(new ExecutedConfiguration(), null)

        then: "It catches an Execution Status exception"
        thrown Exception
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


    def "test createImportSubmissionGtt" () {
        given:
        List list = [["16US00000000001423","10-Apr-2020","EMA","10-Apr-2020","10-Apr-2020",	"Periodic",	"0","CIOMS"],
                     ["16US00000000001423",null,"EMA","10-Apr-2020","10-Apr-2020",	null,	"",null]]
        when:
        def result = service.createImportSubmissionGtt(list)
        then:
        result.query == """begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_CLL_REPORT_DATA_TEMP''); end;';
INSERT INTO GTT_CLL_REPORT_DATA_TEMP (CASE_NUM, DATE_1, TEXT_1,DATE_2,DATE_3,NUMBER_1,NUMBER_2,TEXT_2,SORT_COL)values('16US00000000001423', TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),'EMA',TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),0,0,'CIOMS',1);
INSERT INTO GTT_CLL_REPORT_DATA_TEMP (CASE_NUM, DATE_1, TEXT_1,DATE_2,DATE_3,NUMBER_1,NUMBER_2,TEXT_2,SORT_COL)values('16US00000000001423', null,'EMA',TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),null,null,null,2);

 END;
"""
    }

    private User createUser(String username, String role) {
        def userRole = new Role(authority: role, createdBy: username, modifiedBy: username).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Normal User", preference: preference, createdBy: username, modifiedBy: username)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    /**
     * Perform a deep comparison of the contents of two collections.
     * @param collection1
     * @param collection2
     * @return
     */

    private Configuration createConfiguration(User normalUser) {

        CaseLineListingTemplate template1 = createTemplate("Test template 1: Case Number")
        CaseLineListingTemplate template2 = createTemplate("Test template 2: Case Number")

        Query query = createQuery()

        TemplateQuery templateQuery1 = createTemplateQuery(template1)
        templateQuery1.header = "Custom Header"
        templateQuery1.title = "Custom Title"
        templateQuery1.footer = "Custom Footer"
        DateRangeInformation dateRangeInformation = new DateRangeInformation(dateRangeEnum: DateRangeEnum.CUSTOM,
                                                                             dateRangeStartAbsolute: new DateTime(2016, 3, 10, 0, 0, 0, 0).toDate(),
                                                                             dateRangeEndAbsolute:new DateTime(2016, 3, 15, 0, 0, 0, 0).toDate())
        templateQuery1.dateRangeInformationForTemplateQuery = dateRangeInformation

        TemplateQuery templateQuery2 = createTemplateQuery(template2)

        configuration = new Configuration(reportName: "Test Configuration",
                description: "Configuration for Unit Test",
                nextRunDate: new DateTime(2016, 4, 20, 0, 0, 0, 0).toDate(),
                asOfVersionDate: new DateTime(2016, 3, 20, 0, 0, 0, 0).toDate(),
                /*dateRangeType: DateRangeTypeCaseEnum.CASE_RECEIPT_DATE,*/ //TODO: Need to check here
                deliveryOption: new DeliveryOption(sharedWith: [normalUser],
                attachmentFormats: [ReportFormatEnum.PDF]),
                productSelection: '{"1":[{"name":"DEXIBUPROFEN","id":2685}],"2":[],"3":[],"4":[]}',
                studySelection: '{"1":[],"2":[{"name":"MPS001","id":100012}],"3":[]}',
                eventSelection: '{"1":[],"2":[],"3":[],"4":[{"name":"Anaemia","id":10002034}],"5":[],"6":[]}',
                owner: normalUser,
                createdBy: normalUser.username,
                modifiedBy: normalUser.username)

        configuration.addToTags(new Tag(name: "Tag 1"))
        configuration.addToTags(new Tag(name: "Tag 2"))
        configuration.addToTemplateQueries(templateQuery: templateQuery1, query: query)
        configuration.addToTemplateQueries(templateQuery: templateQuery2, query: query)
        return configuration
    }

    private Query createQuery() {

        def JSONQuery = """{ "all": { "containerGroups": [
        { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }"""

        Query query = new Query(name: "Test Query",
                queryType: QueryTypeEnum.QUERY_BUILDER,
                JSONQuery: JSONQuery,
                owner: normalUser,
                createdBy: normalUser.username,
                modifiedBy: normalUser.username
        )

        return query
    }

    private CaseLineListingTemplate createTemplate(String name) {

        ReportField reportField = new ReportField(name: "masterCaseNum")
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: reportField, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo)

        CaseLineListingTemplate template = new CaseLineListingTemplate(name: name, columnList: reportFieldInfoList)

        return template
    }

    private TemplateQuery createTemplateQuery(template) {

        TemplateQuery templateQuery  = new TemplateQuery(template: template,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(),
                report: configuration, createdBy: normalUser.username, modifiedBy: normalUser.username)

        return templateQuery
    }

    @Ignore
    def "test createImportRcaGtt-allValuesPopulated" () {
        given:
        List list = [["16US00000000001423", "RK RX Partner E2B R3", "10-Apr-2020", "10-Apr-2020", "10-Apr-2020", "Exclude", "Exclude", "Exclude",
                      "Exclude", "Exclude", "Case Correction", "Communication Awareness", "10-Apr-2020", "10-Apr-2020", "yes", 1.0, "Investigation",
                      "Summary", "Action"]]
        when:
        def result = service.createImportRcaGtt(list)
        then:
        result.query.trim() == """begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_CLL_REPORT_DATA_TEMP''); end;'; execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_submission_late_case_proc''); end;';
INSERT INTO GTT_CLL_REPORT_DATA_TEMP (Case_num, TEXT_1, DATE_1, DATE_2, DATE_3,TEXT_2,TEXT_3,TEXT_10,TEXT_12,TEXT_4,TEXT_5,TEXT_6,DATE_4,DATE_5,TEXT_7,TEXT_8,VERSION_NUM,TEXT_21,TEXT_22,TEXT_23) values('16US00000000001423', 'RK RX Partner E2B R3', TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),'Exclude','Exclude','Exclude','Exclude','Exclude','Case Correction','Communication Awareness',TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),null,'yes',1.0,'Investigation','Summary','Action');

 END;
 """.trim()
    }

    @Ignore
    def "test createImportRcaGtt-nullValues" () {
        given:
        List list = [["16US00000000001423", "RK RX Partner E2B R3", "10-Apr-2020", "10-Apr-2020", "10-Apr-2020", "Exclude", "Exclude", "Exclude",
                      "Exclude", "Exclude", "Case Correction", "Communication Awareness", "10-Apr-2020", "10-Apr-2020", "yes", null, null,
                      null, null]]
        when:
        def result = service.createImportRcaGtt(list)
        then:
        result.query.trim() == """begin execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''GTT_CLL_REPORT_DATA_TEMP''); end;'; execute immediate ' begin   pkg_pvr_app_util.p_truncate_table(''gtt_submission_late_case_proc''); end;';
INSERT INTO GTT_CLL_REPORT_DATA_TEMP (Case_num, TEXT_1, DATE_1, DATE_2, DATE_3,TEXT_2,TEXT_3,TEXT_10,TEXT_12,TEXT_4,TEXT_5,TEXT_6,DATE_4,DATE_5,TEXT_7,TEXT_8,VERSION_NUM,TEXT_21,TEXT_22,TEXT_23) values('16US00000000001423', 'RK RX Partner E2B R3', TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),'Exclude','Exclude','Exclude','Exclude','Exclude','Case Correction','Communication Awareness',TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),TO_DATE('10-Apr-2020', 'DD-MON-YYYY'),null,'yes',null,null,null,null);

 END;
        """.trim()
    }

}
