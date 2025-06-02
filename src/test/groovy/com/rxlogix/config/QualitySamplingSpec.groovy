package com.rxlogix.config

import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.user.User
import grails.testing.gorm.DataTest
import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class QualitySamplingSpec extends Specification implements DomainUnitTest<QualitySampling> {

    def setupSpec() {
        mockDomains User,Tenant, WorkflowRule, WorkflowState
    }

    QualitySampling qualitySampling
    def createNewQualitySampling(){
        qualitySampling = new QualitySampling(id: 1L, reportId: 11L, caseNumber: 'testCaseNumber1',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType', metadata: 'metadata', triageAction: 21L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, type:"SAMPLING", versionNumber: 31L,executedTemplateId: 1L)

    }

    def setup() {
        QualitySampling qualitySampling1=new QualitySampling(id: 2L, reportId: 12L, caseNumber: 'testCaseNumber1',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType3', metadata: 'metadata', triageAction: 22L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, type:"SAMPLING", versionNumber: 32L,executedTemplateId: 1L)
        qualitySampling1.save(failOnError:true)
        QualitySampling qualitySampling2=new QualitySampling(id: 3L, reportId: 13L, caseNumber: 'testCaseNumber2',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType1', metadata: 'metadata', triageAction: 23L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, type:"SAMPLING", versionNumber: 33L,executedTemplateId: 1L)
        qualitySampling2.save(failOnError:true)
        QualitySampling qualitySampling3=new QualitySampling(id: 4L, reportId: 14L, caseNumber: 'testCaseNumber3',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType2', metadata: 'metadata', triageAction: 24L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, type:"SAMPLING", versionNumber: 34L,executedTemplateId: 1L)
        qualitySampling3.save(failOnError:true)
        QualitySampling qualitySampling4=new QualitySampling(id: 5L, reportId: 15L, caseNumber: 'testCaseNumber4',workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                errorType: 'testErrorType3', metadata: 'metadata', triageAction: 25L, priority: 'testPriority', workflowStateUpdatedDate: new Date(),
                justification: 'testJustification', entryType: 'testEntryType', createdBy: 'user', modifiedBy: 'user', tenantId: 1L, type:"SAMPLING", versionNumber: 35L,executedTemplateId: 1L)
        qualitySampling4.save(failOnError:true)

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

    void "test reportId "(){
        given:
            createNewQualitySampling()
        when:"reportId equals value"
            qualitySampling.reportId=value
        then:
            qualitySampling.validate()==result
        where:
            value | result
            null  | true
            12L   | true
    }
    void "test caseNumber cannot be null "(){
        given:
            createNewQualitySampling()
        when:"caseNumber equals value"
            qualitySampling.caseNumber=value
        then:
            qualitySampling.validate()==result
        where:
            value            | result
            null             | false
            ''               | true
            'testCaseNumber' | true
    }
    void "test errorType cannot be null "(){
        given:
            createNewQualitySampling()
        when:"errorType equals value"
            qualitySampling.errorType=value
        then:
            qualitySampling.validate()==result
        where:
            value           | result
            null            | false
            ''              | true
            'testErrorType' | true
    }
    void "test metadata cannot be null "(){
        given:
            createNewQualitySampling()
        when:"metadata equals value"
            qualitySampling.metadata=value
        then:
            qualitySampling.validate()==result
        where:
            value          | result
            null           | false
            ''             | true
            'testMetadata' | true
    }
    void "test justification cannot be > 4000"() {
        given:
            createNewQualitySampling()
        when:"justification equals value"
            qualitySampling.justification=value
        then:
            qualitySampling.validate()==result
        where:
            value                      | result
            null                       | true
            ''                         | true
            'testJustification'        | true
            'testJustification' * 400  | false
    }
    void "test entryType cannot be null "(){
        given:
            createNewQualitySampling()
        when:"entryType equals value"
            qualitySampling.entryType=value
        then:
            qualitySampling.validate()==result
        where:
            value          | result
            null           | false
            ''             | true
            'testEntryType' | true
    }

    void "test assignedToUser"() {
        given:
            createNewQualitySampling()
        when:"assignedUser equals value"
            qualitySampling.assignedToUser=value
        then:
            qualitySampling.validate()==result
        where:
            value      | result
            null       | true
            new User() | false
    }
    void "test createdBy cannot be null"() {
        given:
            createNewQualitySampling()
        when:"createdBy equals value"
            qualitySampling.createdBy=value
        then:
            qualitySampling.validate()==result
        where:
            value  | result
            null   | false
            ''     | true
            'user' | true
            ' '    | true

    }
    void "test modifiedBy cannot be null"() {
        given:
            createNewQualitySampling()
        when:"modifiedBy equals value"
            qualitySampling.modifiedBy=value
        then:
            qualitySampling.validate()==result
        where:
            value  | result
            null   | false
            ''     | true
            'user' | true
            ' '    | true

    }
    void "test toString "(){
        given:
            QualitySampling qualitySampling1 = new QualitySampling(
                    id: 8L,
                    reportId: 14L,
                    caseNumber: 'testCaseNumber2',
                    errorType: 'testErrorType2',
                    metadata: 'metadata2',
                    triageAction: 21L,
                    priority: 'testPriority2',
                    justification: 'testJustification2',
                    entryType: 'testEntryType2',
                    createdBy: 'user2',
                    modifiedBy: 'user2',
                    workflowState: new WorkflowState(name:"new", modifiedBy: "test", createdBy:"test"),
                    tenantId: 1L,
                    type: PvqTypeEnum.SAMPLING.name())
        config.qualityModule.additional = [['name':'SAMPLING', 'label':'Case Sampling', 'workflow':1, 'tag':'PV Quality: Sampling', 'columnList':['masterCaseNum', 'masterVersionNum', 'errorType', 'masterPrimaryHcpFlag', 'masterCountryId', 'masterRptTypeId', 'patInfoPatientAgeYears', 'caseParentInfoPvrDob', 'patInfoGenderId', 'productProdNameDrugType', 'masterPrefTermList']], ['name':'OTHER_SAMPLING_1', 'label':'Other Sampling 1', 'workflow':2, 'tag':'PV Quality: Other Sampling 1', 'columnList':['masterCaseNum', 'masterVersionNum', 'masterPrimaryHcpFlag', 'masterCountryId']], ['name':'OTHER_SAMPLING_2', 'label':'Other Sampling 2', 'workflow':3, 'tag':'PV Quality: Other Sampling 2', 'columnList':['masterCaseNum', 'masterVersionNum', 'masterPrimaryHcpFlag', 'masterCountryId']], ['name':'OTHER_SAMPLING_3', 'label':'Other Sampling 3', 'workflow':4, 'tag':'PV Quality: Other Sampling 3', 'columnList':['masterCaseNum', 'masterVersionNum', 'masterPrimaryHcpFlag', 'masterCountryId']], ['name':'OTHER_SAMPLING_4', 'label':'Other Sampling 4', 'workflow':5, 'tag':'PV Quality: Other Sampling 4', 'columnList':['masterCaseNum', 'masterVersionNum', 'masterPrimaryHcpFlag', 'masterCountryId']], ['name':'OTHER_SAMPLING_5', 'label':'Other Sampling 5', 'workflow':6, 'tag':'PV Quality: Other Sampling 5', 'columnList':['masterCaseNum', 'masterVersionNum', 'masterPrimaryHcpFlag', 'masterCountryId']], ['name':'OTHER_SAMPLING_6', 'label':'Other Sampling 6', 'workflow':7, 'tag':'PV Quality: Other Sampling 6', 'columnList':['masterCaseNum', 'masterVersionNum', 'masterPrimaryHcpFlag', 'masterCountryId']]]
        when:
            def result=qualitySampling1.toString()
        then:
        println(result)
            result == "(Case Sampling)" +
                    " testCaseNumber2" +
                    " - testErrorType2"
    }

    void "test getErrorTypes "(){
        when:
            def result = QualitySampling.getErrorTypes(1L, PvqTypeEnum.SAMPLING.toString())
        then:
            result[0]=="testErrorType1"
            result[1]=="testErrorType2"
            result[2]=="testErrorType3"
    }
}
