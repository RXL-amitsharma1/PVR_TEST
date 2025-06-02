package com.rxlogix.dynamicReports

import com.rxlogix.dynamicReports.reportTypes.TemplateSetCsvDataSource
import net.sf.jasperreports.engine.design.JRDesignField
import spock.lang.Specification

/*@TestMixin(GrailsUnitTestMixin)*/
class TemplateSetCsvDataSourceSpec extends Specification {
    private static final String CASE_NUMBER_COLUMN_NAME = "masterCaseNum_6"
    private static final String TEST_CASE_NUMBER = "19US00012209"
    private static final int CASE_COUNT = 1040

    private JRDesignField caseNumberField

    def setup() {
        caseNumberField = new JRDesignField(name: CASE_NUMBER_COLUMN_NAME)
    }

    def cleanup() {
    }

    void "test for old datasource format (case number groups only)"() {
        given:
        InputStream dataStream = getClass().getResourceAsStream('after_multiple_groups.tar.gz')
        TemplateSetCsvDataSource dataSource = new TemplateSetCsvDataSource(dataStream, CASE_NUMBER_COLUMN_NAME)
        int counter = 0
        Set<String> caseNumbers = new HashSet<>()

        when:
        while (dataSource.next()) {
            counter++
            caseNumbers.add(dataSource.getFieldValue(caseNumberField))
        }

        then:
        counter == CASE_COUNT
        caseNumbers.size() == CASE_COUNT
        caseNumbers.contains(TEST_CASE_NUMBER)
    }

    void "test for new datasource format (multiple groups)"() {
        given:
        InputStream dataStream = getClass().getResourceAsStream('after_multiple_groups.tar.gz')
        TemplateSetCsvDataSource dataSource = new TemplateSetCsvDataSource(dataStream, CASE_NUMBER_COLUMN_NAME)
        int counter = 0
        Set<String> caseNumbers = new HashSet<>()

        when:
        while (dataSource.next()) {
            counter++
            caseNumbers.add(dataSource.getFieldValue(caseNumberField))
        }

        then:
        counter == CASE_COUNT
        caseNumbers.size() == CASE_COUNT
        caseNumbers.contains(TEST_CASE_NUMBER)
    }
}
