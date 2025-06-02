package com.rxlogix.config.metadata

import com.rxlogix.config.WorkflowRule
import com.rxlogix.config.WorkflowState
import com.rxlogix.user.User
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import com.rxlogix.config.QualityCaseData
import com.rxlogix.config.Tenant


class QualityCaseDataSpec extends Specification implements DataTest, DomainUnitTest<QualityCaseData>  {

    def setupSpec() {
        mockDomain User
        mockDomain Tenant
        mockDomain WorkflowRule
        mockDomain WorkflowState
    }


    QualityCaseData qualityCaseData
    def createNewQualityCaseData(){
        qualityCaseData=new QualityCaseData()
        qualityCaseData.id=8L
        qualityCaseData.reportId=11L
        qualityCaseData.caseNumber='testCaseNumber1'
        qualityCaseData.versionNumber = 1L
        qualityCaseData.errorType='testErrorType'
        qualityCaseData.metadata='metadata'
        qualityCaseData.triageAction=21L
        qualityCaseData.priority='testPriority'
        qualityCaseData.justification='testJustification'
        qualityCaseData.entryType='testEntryType'
        qualityCaseData.createdBy='user'
        qualityCaseData.modifiedBy='user'
        qualityCaseData.tenantId=1L
        qualityCaseData.workflowState = new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test")
        qualityCaseData.workflowStateUpdatedDate = new Date()
        qualityCaseData.executedTemplateId=1L
    }
    def setup() {
        QualityCaseData qualityCaseData1 = new QualityCaseData(id: 1L, reportId: 11L, caseNumber: 'testCaseNumber1',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType3', metadata: 'metadata', triageAction: 21L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, versionNumber: 31L, executedTemplateId: 1L)
        qualityCaseData1.save(failOnError: true)
        QualityCaseData qualityCaseData2 = new QualityCaseData(id: 2L, reportId: 12L, caseNumber: 'testCaseNumber1',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType1', metadata: 'metadata', triageAction: 22L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, versionNumber: 32L,executedTemplateId: 1L)
        qualityCaseData2.save(failOnError: true)
        QualityCaseData qualityCaseData3 = new QualityCaseData(id: 3L, reportId: 13L, caseNumber: 'testCaseNumber1',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType2', metadata: 'metadata', triageAction: 23L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, versionNumber: 33L, executedTemplateId: 1L)
        qualityCaseData3.save(failOnError: true)
        QualityCaseData qualityCaseData4 = new QualityCaseData(id: 4L, reportId: 14L, caseNumber: 'testCaseNumber1',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType2', metadata: 'metadata', triageAction: 24L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, versionNumber: 34L, executedTemplateId: 1L)
        qualityCaseData4.save(failOnError: true)

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
            createNewQualityCaseData()
        when:"reportId equals id "
            qualityCaseData.reportId=id
        then:
            qualityCaseData.validate()==true
        where:
            id<<[null,13L]
    }
    void "test caseNumber cannot be null"() {
        given:
            createNewQualityCaseData()
        when:"caseNumber equals value"
            qualityCaseData.caseNumber=value
        then:
            qualityCaseData.validate()==result
        where:
            value        | result
            null         | false
            ''           | true
            'caseNumber' | true
            ' '          | true

    }
    void "test errorType cannot be null"() {
        given:
            createNewQualityCaseData()
        when:"errorType equals value"
            qualityCaseData.errorType=value
        then:
            qualityCaseData.validate()==result
        where:
            value       | result
            null        | false
            ''          | true
            'errorType' | true
            ' '         | true

    }
    void "test metadata cannot be null"() {
        given:
            createNewQualityCaseData()
        when:"metadata equals value"
            qualityCaseData.metadata=value
        then:
            qualityCaseData.validate()==result
        where:
            value      | result
            null       | false
            ''         | true
            'metadata' | true
            ' '        | true

    }

    void "test justification cannot be > 4000"() {
        given:
            createNewQualityCaseData()
        when:"justification equals value"
            qualityCaseData.justification=value
        then:
            qualityCaseData.validate()==result
        where:
            value                      | result
            null                       | true
            ''                         | true
            'testJustification'        | true
            'testJustification' * 400  | false
    }

    void "test entryType cannot be null"() {
        given:
            createNewQualityCaseData()
        when:"entryType equals value"
            qualityCaseData.entryType=value
        then:
            qualityCaseData.validate()==result
        where:
            value       | result
            null        | false
            ''          | true
            'entryType' | true
            ' '         | true

    }
    void "test assignedToUser"() {
        given:
            createNewQualityCaseData()
        when:"assignedUser equals value"
            qualityCaseData.assignedToUser=value
        then:
            qualityCaseData.validate()==result
        where:
            value      | result
            null       | true
            new User() | false
    }

    void "test createdBy cannot be null"() {
        given:
            createNewQualityCaseData()
        when:"createdBy equals value"
            qualityCaseData.createdBy=value
        then:
            qualityCaseData.validate()==result
        where:
            value  | result
            null   | false
            ''     | true
            'user' | true
            ' '    | true

    }
    void "test modifiedBy cannot be null"() {
        given:
            createNewQualityCaseData()
        when:"modifiedBy equals value"
            qualityCaseData.modifiedBy=value
        then:
            qualityCaseData.validate()==result
        where:
            value  | result
            null   | false
            ''     | true
            'user' | true
            ' '    | true

    }

    void "test toString "(){
        given:
            QualityCaseData qualityCaseData1 = new QualityCaseData(
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
            def result=qualityCaseData1.toString()
        then:
            result == "testCaseNumber1 -" +
                    " testErrorType"
    }

    void "test getErrorTypes"(){
        given:
            def tenantId = 1L
        when:
            def result=QualityCaseData.getErrorTypes()
        then:
            result[0]==null
            result[1]==null
            result[2]==null
            result[3]==null
    }
}
