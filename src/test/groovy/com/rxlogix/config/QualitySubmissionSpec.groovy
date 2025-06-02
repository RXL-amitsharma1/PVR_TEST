package com.rxlogix.config

import com.rxlogix.user.User
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class QualitySubmissionSpec extends Specification implements DomainUnitTest<QualitySubmission> {

    def setupSpec() {
        mockDomains User,Tenant
    }

    QualitySubmission qualitySubmission
    def createNewQualitySubmission(){
        qualitySubmission=new QualitySubmission()
        qualitySubmission.id=8L
        qualitySubmission.reportId=11L
        qualitySubmission.caseNumber='testCaseNumber1'
        qualitySubmission.versionNumber = 1L
        qualitySubmission.errorType='testErrorType'
        qualitySubmission.metadata='metadata'
        qualitySubmission.triageAction=21L
        qualitySubmission.priority='testPriority'
        qualitySubmission.justification='testJustification'
        qualitySubmission.entryType='testEntryType'
        qualitySubmission.createdBy='user'
        qualitySubmission.modifiedBy='user'
        qualitySubmission.tenantId=1L
        qualitySubmission.workflowState = new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test")
        qualitySubmission.workflowStateUpdatedDate = new Date()
        qualitySubmission.executedTemplateId = 1l
        qualitySubmission.submissionIdentifier="-"
    }
    def setup() {
        QualitySubmission qualitySubmission1 = new QualitySubmission(id: 1L, reportId: 11L, caseNumber: 'testCaseNumber1',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType3', metadata: 'metadata', triageAction: 21L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, versionNumber: 31L, executedTemplateId: 1L, submissionIdentifier:"-")
        qualitySubmission1.save(failOnError: true)
        QualitySubmission qualitySubmission2 = new QualitySubmission(id: 2L, reportId: 12L, caseNumber: 'testCaseNumber1',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType1', metadata: 'metadata', triageAction: 22L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, versionNumber: 32L, executedTemplateId: 1L,submissionIdentifier:"-")
        qualitySubmission2.save(failOnError: true)
        QualitySubmission qualitySubmission3 = new QualitySubmission(id: 3L, reportId: 13L, caseNumber: 'testCaseNumber1',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType2', metadata: 'metadata', triageAction: 23L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, versionNumber: 33L, executedTemplateId: 1L,submissionIdentifier:"-")
        qualitySubmission3.save(failOnError: true)
        QualitySubmission qualitySubmission4 = new QualitySubmission(id: 4L, reportId: 14L, caseNumber: 'testCaseNumber1',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType2', metadata: 'metadata', triageAction: 24L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, versionNumber: 34L, executedTemplateId: 1L,submissionIdentifier:"-")
        qualitySubmission4.save(failOnError: true)
    }

    def cleanup() {
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

    void "test reportId"() {
        given:
            createNewQualitySubmission()
        when:"reportId equals id "
            qualitySubmission.reportId=id
        then:
            qualitySubmission.validate()==true
        where:
            id<<[null,13L]
    }
    void "test caseNumber cannot be null"() {
        given:
            createNewQualitySubmission()
        when:"caseNumber equals value"
            qualitySubmission.caseNumber=value
        then:
            qualitySubmission.validate()==result
        where:
            value        | result
            null         | false
            ''           | true
            'caseNumber' | true
            ' '          | true

    }
    void "test errorType cannot be null"() {
        given:
            createNewQualitySubmission()
        when:"errorType equals value"
            qualitySubmission.errorType=value
        then:
            qualitySubmission.validate()==result
        where:
            value       | result
            null        | false
            ''          | true
            'errorType' | true
            ' '         | true

    }

    void "test metadata cannot be null"() {
        given:
            createNewQualitySubmission()
        when:"metadata equals value"
            qualitySubmission.metadata=value
        then:
            qualitySubmission.validate()==result
        where:
            value      | result
            null       | false
            ''         | true
            'metadata' | true
            ' '        | true

    }
    void "test justification cannot be > 4000"() {
        given:
            createNewQualitySubmission()
        when:"justification equals value"
            qualitySubmission.justification=value
        then:
            qualitySubmission.validate()==result
        where:
            value                      | result
            null                       | true
            ''                         | true
            'testJustification'        | true
            'testJustification' * 400  | false
    }
    void "test entryType cannot be null"() {
        given:
            createNewQualitySubmission()
        when:"entryType equals value"
            qualitySubmission.entryType=value
        then:
            qualitySubmission.validate()==result
        where:
            value       | result
            null        | false
            ''          | true
            'entryType' | true
            ' '         | true

    }
    void "test assignedToUser"() {
        given:
            createNewQualitySubmission()
        when:"assignedUser equals value"
            qualitySubmission.assignedToUser=value
        then:
            result == qualitySubmission.validate()
        where:
            value      | result
            null       | true
            new User() | false
    }

    void "test createdBy cannot be null"() {
        given:
            createNewQualitySubmission()
        when:"createdBy equals value"
            qualitySubmission.createdBy=value
        then:
            result == qualitySubmission.validate()
        where:
            value  | result
            null   | false
            ''     | true
            'user' | true
            ' '    | true

    }
    void "test modifiedBy cannot be null"() {
        given:
            createNewQualitySubmission()
        when:"modifiedBy equals value"
            qualitySubmission.modifiedBy=value
        then:
            qualitySubmission.validate()==result
        where:
            value  | result
            null   | false
            ''     | true
            'user' | true
            ' '    | true

    }
    void "test toString "(){
        given:
            QualitySubmission qualitySubmission1 = new QualitySubmission(
                    id: 4L,
                    reportId: 14L,
                    caseNumber: 'testCaseNumber1',
                    errorType: 'testErrorType',
                    metadata: 'metadata',
                    triageAction: 21L,
                    priority: 'testPriority',
                    justification: 'testJustification',
                    entryType: 'testEntryType',
                    createdBy: 'user',
                    modifiedBy: 'user',
                    tenantId: 1L)
        when:
            def result=qualitySubmission1.toString()
        then:
            result == "testCaseNumber1 -" +
                    " testErrorType"
    }

    void "test getErrorTypes"(){
        given:
             def tenantId =  1L
        when:
            def result=QualitySubmission.getErrorTypes()
        then:
            result[0]==null
            result[1]==null
            result[2]==null
            result[3]==null
    }
}
