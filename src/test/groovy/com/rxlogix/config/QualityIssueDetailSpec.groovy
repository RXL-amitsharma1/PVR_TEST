package com.rxlogix.config


import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class QualityIssueDetailSpec extends Specification implements DomainUnitTest<QualityIssueDetail> {
    QualityIssueDetail qualityIssueDetail

    def createNewQualityIssueDetail(){
        qualityIssueDetail=new QualityIssueDetail()
        qualityIssueDetail.rootCauseId=8L
        qualityIssueDetail.responsiblePartyId=11L
        qualityIssueDetail.correctiveActionId=8L
        qualityIssueDetail.preventativeActionId=11L
        qualityIssueDetail.correctiveDate=new Date()
        qualityIssueDetail.preventativeDate=new Date()
        qualityIssueDetail.investigation='testInvestigation'
        qualityIssueDetail.summary='testSummary'
        qualityIssueDetail.actions='testActions'
        qualityIssueDetail.isPrimary = false
        qualityIssueDetail.createdBy='user'
        qualityIssueDetail.modifiedBy='user'
        qualityIssueDetail.dateCreated =new Date()
        qualityIssueDetail.lastUpdated = new Date()
    }

    def setup() {
    }

    def cleanup() {
    }

    void "test rootCauseId"() {
        given:
        createNewQualityIssueDetail()
        when:"rootCauseId is given"
        qualityIssueDetail.rootCauseId=value
        then:
        qualityIssueDetail.validate()==result
        where:
        value | result
        null  | true
        0     | true
        1L    | true
    }

    void "test responsiblePartyId"() {
        given:
        createNewQualityIssueDetail()
        when:"responsiblePartyId is given"
        qualityIssueDetail.responsiblePartyId=value
        then:
        qualityIssueDetail.validate()==result
        where:
        value | result
        null  | true
        0     | true
        1L    | true
    }

    void "test correctiveActionId"() {
        given:
        createNewQualityIssueDetail()
        when:"correctiveActionId is given"
        qualityIssueDetail.correctiveActionId=value
        then:
        qualityIssueDetail.validate()==result
        where:
        value | result
        null  | true
        0     | true
        1L    | true
    }

    void "test preventativeActionId"() {
        given:
        createNewQualityIssueDetail()
        when:"preventativeActionId is given"
        qualityIssueDetail.preventativeActionId=value
        then:
        qualityIssueDetail.validate()==result
        where:
        value | result
        null  | true
        0     | true
        1L    | true
    }

    void "test correctiveDate"() {
        given:
        createNewQualityIssueDetail()
        when:"correctiveDate is given"
        qualityIssueDetail.correctiveDate=value
        then:
        qualityIssueDetail.validate()==result
        where:
        value      | result
        null       | true
        new Date() | true
    }

    void "test preventativeDate"() {
        given:
        createNewQualityIssueDetail()
        when:"preventativeDate is given"
        qualityIssueDetail.preventativeDate=value
        then:
        qualityIssueDetail.validate()==result
        where:
        value      | result
        null       | true
        new Date() | true
    }

    void "test investigation"() {
        given:
        createNewQualityIssueDetail()
        when:"investigation is given"
        qualityIssueDetail.investigation=value
        then:
        qualityIssueDetail.validate()==result
        where:
        value               | result
        null                | true
        ''                  | true
        'testInvestigation' | true
        ' '                 | true
    }

    void "test summary"() {
        given:
        createNewQualityIssueDetail()
        when:"summary is given"
        qualityIssueDetail.summary=value
        then:
        qualityIssueDetail.validate()==result
        where:
        value         | result
        null          | true
        ''            | true
        'testSummary' | true
        ' '           | true
    }

    void "test actions"() {
        given:
        createNewQualityIssueDetail()
        when:"actions is given"
        qualityIssueDetail.actions=value
        then:
        qualityIssueDetail.validate()==result
        where:
        value         | result
        null          | true
        ''            | true
        'testActions' | true
        ' '           | true
    }
}
