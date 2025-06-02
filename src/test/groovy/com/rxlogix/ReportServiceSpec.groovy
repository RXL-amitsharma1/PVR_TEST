package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.user.AIEmailPreference
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.apache.commons.io.IOUtils
import spock.lang.Specification

class ReportServiceSpec extends Specification implements DataTest, ServiceUnitTest<ReportService> {

    def setup() {
    }

    def cleanup() {
    }

    void "test get case numbers from icsr report"() {
        given: "A report result instance"
        ReportResult result = createXMLReportResult("icsr_report_data.tar.gz")

        when: "Call the getCaseNumbers method"
        List<Tuple2<String, Integer>> caseNumberAndVersions = service.getCaseNumberAndVersions(result)

        then: "It returns 1040 cases"
        caseNumberAndVersions != null
        caseNumberAndVersions.size() == 1040
        caseNumberAndVersions.first().first == "19FR00010871"
        caseNumberAndVersions.first().second == null //version number will come as null.
    }

    void "test get case numbers from cll report"() {
        given: "A report result instance"
        ReportResult result = createCLLReportResult("cll_report_data.csv.gzip")

        when: "Call the getCaseNumbers method"
        List<Tuple2<String, Integer>> caseNumberAndVersions = service.getCaseNumberAndVersions(result)

        then: "It returns 25 rows"
        caseNumberAndVersions != null
        caseNumberAndVersions.size() == 25
        caseNumberAndVersions.first().first == "19US00011265"
        caseNumberAndVersions.first().second == 8
    }

    void "test get case numbers from blank report"() {
        given: "A blank report result instance"
        ReportResult result = createCLLReportResult("blank_report_data.csv.gzip")

        when: "Call the getCaseNumbers method"
        List<Tuple2<String, Integer>> caseNumberAndVersions = service.getCaseNumberAndVersions(result)

        then: "It returns null"
        caseNumberAndVersions != null
        caseNumberAndVersions.size() == 0
    }

    void "test get case numbers from CIOMS I report"() {
        given: "A report result instance"
        ReportResult result = createCiomsIReportResult("cioms_i_report_data.csv.gzip")

        when: "Call the getCaseAndVersionNumbers method"
        List<Tuple2<String, Integer>> caseNumberAndVersions = service.getCaseNumberAndVersions(result)

        then: "It returns 25 cases"
        caseNumberAndVersions != null
        caseNumberAndVersions.size() == 25
        caseNumberAndVersions.first().first == "19US00011263"
        caseNumberAndVersions.first().second == null
    }

    private ReportResult createXMLReportResult(String resourcePath) {
        ExecutedXMLTemplate executedTemplate = new ExecutedXMLTemplate()
        return createReportResult(resourcePath, executedTemplate)
    }

    private ReportResult createCLLReportResult(String resourcePath) {
        def executedTemplate = Stub(ExecutedCaseLineListingTemplate) {
            getFieldNameWithIndex() >> ["masterCaseNum_0", "masterCountryId_1", "masterVersionNum_2"]
        }
        return createReportResult(resourcePath, executedTemplate)
    }

    private ReportResult createCiomsIReportResult(String resourcePath) {
        ExecutedCustomSQLTemplate executedTemplate = new ExecutedCustomSQLTemplate()
        executedTemplate.ciomsI = true
        executedTemplate.columnNamesList = "[" +
                "PATIENT_INITIALS_1," +
                "COUNTRY_1A," +
                "PAT_DOB_DAY_2," +
                "PAT_DOB_MONTH_2," +
                "PAT_DOB_YEAR_2," +
                "PAT_AGE_YEARS_2A," +
                "PAT_GENDER_3," +
                "PAT_WEIGHT_3A," +
                "REACT_ONSET_DAY_4," +
                "REACT_ONSET_MONTH_5," +
                "REACT_ONSET_YEAR_6," +
                "CASE_DESCRIPTION_7," +
                "CASE_COMMENT_7," +
                "OTHER_SER_CRITERIA_7," +
                "DESCRIBE_REACTION_7," +
                "PAT_DIED_8," +
                "DATE_OF_DEATH_9," +
                "PAT_HOSPITALISED_10," +
                "PAT_DISABILITY_11," +
                "LIFE_THREATENING_12," +
                "REL_TESTS_13," +
                "LAB_DATA_13," +
                "SUS_PRODUCT_NAME_ALL_14," +
                "ADDITIONAL_14," +
                "DAILY_DOSES_ALL_15," +
                "ROUTE_OF_ADMIN_ALL_16," +
                "INDICATION_ALL_17," +
                "THERAPY_DATES_ALL_18," +
                "THERAPY_DURATION_19," +
                "REACT_DECHAL_OUTCOME_20," +
                "REACT_RECHAL_OUTCOME_21," +
                "CONMEDS_PRODUCTS_DATES_22," +
                "OTHER_RELEVANT_HISTORY_23," +
                "MANU_NAME_ADDRESS_24A," +
                "MFR_CONTROL_NO_24B," +
                "MANU_DATE_RECIEVED_24C," +
                "REPORT_SOURCE_STUDY_24D," +
                "REPORT_SOURCE_LIT_24D," +
                "REPORT_SOURCE_HP_24D," +
                "LITERATURE_24D," +
                "REPORT_SOURCE_OTHER_24D," +
                "REPORT_SOURCE_OTHER_DESC_24D," +
                "REPORT_TYPE_INIT_25A," +
                "REPORT_TYPE_FOLLOWUP_25A," +
                "REPORTER_NAME_ADDRESS_25B," +
                "REPORT_DATE," +
                "REMARKS_26," +
                "PATIENT_CONFIDENTIALITY," +
                "EFFECTIVE_START_DATE]"
        return createReportResult(resourcePath, executedTemplate)
    }

    private createReportResult(String resourcePath, ReportTemplate executedTemplate) {
        ReportResultData data = new ReportResultData()
        data.value = IOUtils.toByteArray(getClass().getResourceAsStream(resourcePath))

        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery()
        executedTemplateQuery.executedTemplate = executedTemplate

        return Stub(ReportResult) {
            getData() >> data
            getExecutedTemplateQuery() >> executedTemplateQuery
            getSourceProfile() >> new SourceProfile()
        }
    }
}
