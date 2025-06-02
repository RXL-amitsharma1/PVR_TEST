package com.rxlogix.config

import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.enums.XMLNodeElementType
import com.rxlogix.enums.XMLNodeType
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import grails.validation.ValidationException
import spock.lang.Specification

// https://stackoverflow.com/questions/49528379/how-to-disable-deepvalidate-in-grails-globally
class XMLTemplateNodeSpec extends Specification implements DomainUnitTest<XMLTemplateNode> {
    CaseLineListingTemplate reportTemplateToTest
    ReportFieldInfo reportFieldInfoToTest

    def setup() {
        reportTemplateToTest = new CaseLineListingTemplate(
                templateType: TemplateTypeEnum.CASE_LINE
        )
        reportTemplateToTest.save(validate: false, flush: true)

        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoToTest = new ReportFieldInfo(
                reportField: new ReportField(name: "masterCaseNum").save(validate: false, flush: true),
                argusName: "masterCaseNum")
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfoToTest)
        reportFieldInfoList.save(validate: false, flush: true)
        reportFieldInfoToTest.save(validate: false, flush: true)
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains CaseLineListingTemplate, ReportFieldInfo, ReportField, ReportTemplate,ReportFieldInfoList
    }

    void "test the TAG_PROPERTIES node saving"() {
        setup:
        def xmlTemplateNode = new XMLTemplateNode(
                elementType: XMLNodeElementType.ATTRIBUTE,
                type: XMLNodeType.TAG_PROPERTIES,
                tagName: "icsr",
                template: reportTemplateToTest
        )
        when:
        xmlTemplateNode.save(failOnError: true)
        then:
        xmlTemplateNode.id != null
    }

    void "test the sourceFieldLabel and e2bElement and e2bElementName saving"() {
        setup:
        def xmlTemplateNode = new XMLTemplateNode(
                elementType: XMLNodeElementType.ATTRIBUTE,
                type: XMLNodeType.TAG_PROPERTIES,
                tagName: "icsr",
                template: reportTemplateToTest,
                e2bElement: "e2bElement",
                e2bElementName: "e2bElementName",
                sourceFieldLabel: "sourceFieldLabel"
        )
        when:
        xmlTemplateNode.save(failOnError: true)
        then:
        xmlTemplateNode.e2bElement != null
        xmlTemplateNode.e2bElementName != null
        xmlTemplateNode.sourceFieldLabel != null
    }

    void "test the SOURCE_FIELD node saving"() {
        setup:
        def xmlTemplateNode = new XMLTemplateNode(
                elementType: XMLNodeElementType.TAG,
                type: XMLNodeType.SOURCE_FIELD,
                tagName: "icsr",
                reportFieldInfo: reportFieldInfoToTest,
                template: reportTemplateToTest,
                dateFormat: null,
        )
        when:
        xmlTemplateNode.save(failOnError: true)
        then:
        xmlTemplateNode.id != null
    }

    void "test the SOURCE_FIELD node save fail due to template missing"() {
        setup:
        def xmlTemplateNode = new XMLTemplateNode(
                elementType: XMLNodeElementType.TAG,
                type: XMLNodeType.SOURCE_FIELD,
                tagName: "icsr",
                reportFieldInfo: reportFieldInfoToTest,
                template: null,
                dateFormat: null,
        )
        when:
        xmlTemplateNode.save(failOnError: true)
        then:
        thrown(ValidationException)
    }

    void "test the STATIC_VALUE node saving"() {
        setup:
        def xmlTemplateNode = new XMLTemplateNode(
                elementType: XMLNodeElementType.TAG,
                type: XMLNodeType.SOURCE_FIELD,
                tagName: "icsr",
                dateFormat: null,
                value: "Hello World!"
        )
        when:
        xmlTemplateNode.save(failOnError: true)
        then:
        xmlTemplateNode.id != null
    }

    void "test node with child node saving"() {
        setup:
        def childNode = new XMLTemplateNode(
                elementType: XMLNodeElementType.TAG,
                type: XMLNodeType.SOURCE_FIELD,
                tagName: "icsr",
                dateFormat: null,
                value: "Hello World!"
        )
        childNode.save()
        def xmlTemplateNode = new XMLTemplateNode(
                elementType: XMLNodeElementType.TAG,
                type: XMLNodeType.SOURCE_FIELD,
                tagName: "icsr",
                reportFieldInfo: reportFieldInfoToTest,
                template: reportTemplateToTest,
                dateFormat: null,
                children: [childNode]
        )
        when:
        xmlTemplateNode.save(failOnError: true)
        then:
        xmlTemplateNode.id != null
        xmlTemplateNode.children.size() > 0
        xmlTemplateNode.children.first().id != null
    }
}
