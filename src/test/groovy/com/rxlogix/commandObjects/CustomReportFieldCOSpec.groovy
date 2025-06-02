package com.rxlogix.commandObjects


import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldGroup
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import grails.testing.gorm.DataTest
import spock.lang.Specification

class CustomReportFieldCOSpec extends Specification implements DataTest {

    def setupSpec() {
        mockDomains ReportField, ReportFieldGroup, SourceColumnMaster, SourceTableMaster
    }

    def "Test to fetch report field"() {
        given:
        CustomReportFieldCO customReportFieldCO = new CustomReportFieldCO()
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save(failOnError: true)
        SourceTableMaster sourceTableMaster = new SourceTableMaster([tableName: "V_C_IDENTIFICATION", caseJoinOrder: 1, caseJoinType: "E", tableAlias: "cm", tableType: "C"])
        sourceTableMaster.save(failOnError: true)
        SourceColumnMaster sourceColumnMaster = new SourceColumnMaster([tableName: sourceTableMaster, reportItem: "CM_CASE_NUM", columnName: "CASE_NUM", columnType: "V", lang: "en"])
        sourceColumnMaster.save(failOnError: true)
        ReportField field = new ReportField([id: 5, name: "caseNumber", description: "This is the Case number", sourceColumn: sourceColumnMaster, fieldGroup: fieldGroup, dataType: String.class, sourceId: 1])
        field.save(failOnError: true)
        customReportFieldCO.reportFieldId = field.id
        when:
        ReportField reportField = customReportFieldCO.getReportField()
        then:
        reportField.name == "caseNumber"
    }

    def "Test to fetch report field group"() {
        given:
        CustomReportFieldCO customReportFieldCO = new CustomReportFieldCO()
        ReportFieldGroup fieldGroup = new ReportFieldGroup([id: 5, name: "Case Information"])
        fieldGroup.save(failOnError: true)
        customReportFieldCO.fieldGroupId = fieldGroup.id
        when:
        ReportFieldGroup reportFieldGroup = customReportFieldCO.getFieldGroup()
        then:
        reportFieldGroup.name == "Case Information"
    }

    void 'test customDescription can have a maximum of 2000 characters'() {
        when: 'for a string of 2000 characters'
        String str = 'a' * 1080
        CustomReportFieldCO customReportFieldCo = new CustomReportFieldCO()
        customReportFieldCo.customDescription = str

        then: 'description validation passes'
        customReportFieldCo.validate(['customDescription'])
    }

    void 'test defaultExpression can have a maximum of 32000 characters'() {
        when: 'for a string of 2000 characters'
        String str = 'a' * 29045
        CustomReportFieldCO customReportFieldCo = new CustomReportFieldCO()
        customReportFieldCo.defaultExpression = str

        then: 'defaultExpression validation passes'
        customReportFieldCo.validate(['defaultExpression'])
    }

    void 'test customDescription can be null'() {
        when:
        CustomReportFieldCO customReportFieldCo = new CustomReportFieldCO()
        customReportFieldCo.customDescription = null
        then:
        customReportFieldCo.validate(['customDescription'])
    }
}
