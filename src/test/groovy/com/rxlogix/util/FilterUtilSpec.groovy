package com.rxlogix.util

import com.rxlogix.config.*
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.testing.gorm.DataTest
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ConfineMetaClassChanges([ViewHelper])
class FilterUtilSpec extends Specification implements DataTest {
    def testJsonFilterString = null
    def filterJson
    def user1
    def tag1, tag2, tag3


    def setupSpec() {
        mockDomains Configuration, ReportConfiguration, ExecutedConfiguration, ExecutedReportConfiguration, User, Preference, Tag, ReportTemplate
    }

    def setup() {
        def builder = new groovy.json.JsonBuilder()
        filterJson = builder {
            reportName(
                    type: 'text',
                    value: 'test',
                    name: 'reportName'
            )
        }

        user1 = new User(
                id: 100L,
                username: 'test',
                fullName: "James Bond",
                preference: new Preference(timeZone: "UTC").save(validate: false)
        ).save(validate: false)
        tag1 = new Tag(name: "t1").save(validate: false)
        tag2 = new Tag(name: "t1").save(validate: false)
        tag3 = new Tag(name: "t1").save(validate: false)

        (1..10).each {
            Configuration rc = new Configuration(
                    reportName: "test$it",
                    owner: user1,
                    description: "30 tes",
                    qualityChecked: true,
                    numOfExecutions: 3

            )
            rc.addToTags(tag1)
            rc.addToTags(tag2)
            rc.addToTags(tag3)
            rc.save(validate: false)
        }

        ReportTemplate rt = new ReportTemplate(
                templateType: TemplateTypeEnum.CASE_LINE,
        )
        rt.save(validate: false)

        LocalDateTime ldt = LocalDateTime.now()
        LocalDateTime ten_days_before = ldt.minusDays(10)
        LocalDateTime ten_days_after = ldt.plusDays(10)

        testJsonFilterString = """
{
    "reportName": {
        "type": "text",
        "name": "reportName",
        "value": "3"
    },
    "description": {
        "type": "text",
        "name": "description",
        "value": "3"
    },
    "numOfExecutions": {
        "type": "value",
        "name": "runTimes",
        "value": "3"
    },
    "tags": {
        "type": "multi-value-id",
        "name": "tags",
        "value": ["${tag1.id}", "${tag2.id}"]
    },
    "qualityChecked": {
        "type": "value",
        "name": "qualityChecked",
        "value": true
    },
    "owner": {
        "type": "id",
        "name": "owner",
        "value": "${user1.id}"
    },
    "dateCreated": {
        "type": "range",
        "name": "dateCreated",
        "value1": "${ten_days_before.format(DateTimeFormatter.ofPattern('MM/dd/YYYY'))}",
        "value2": "${ten_days_after.format(DateTimeFormatter.ofPattern('MM/dd/YYYY'))}"
    },
    "lastUpdated": {
        "type": "range",
        "name": "lastUpdated",
        "value1": "${ten_days_before.format(DateTimeFormatter.ofPattern('MM/dd/YYYY'))}",
        "value2": "${ten_days_after.format(DateTimeFormatter.ofPattern('MM/dd/YYYY'))}"
    }
}
"""
    }

    def "test for buildCriteria"() {
        setup:
        def ca = FilterUtil.buildCriteria(filterJson, Configuration, user1.preference)
        expect:
        ca.size() == 1
    }

    def "test compose filter"() {
        setup:
        def jsonFilter = FilterUtil.convertToJsonFilter(testJsonFilterString)
        expect:
        (jsonFilter as Map).size() == 8
    }

    def "test for valueMatching"() {
        expect:
        FilterUtil.valueMatching("> 1") == ['gt', "1"]
        FilterUtil.valueMatching("< 1") == ['lt', "1"]
        FilterUtil.valueMatching(">= 1") == ['ge', "1"]
        FilterUtil.valueMatching("<= 1") == ['le', "1"]
        FilterUtil.valueMatching("between 1 1.5") == ['between', "1", "1.5"]
    }

    def "test for buildEnumOptions"() {
        setup:
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "type" }
        expect:
        def list = JsonSlurper.newInstance().parseText(FilterUtil.buildEnumOptions(TemplateTypeEnum))
        list.size() == 6
    }

}
