package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.ActionItemGroupState
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.enums.StatusEnum
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserRole
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import groovy.sql.Sql
import org.grails.datastore.mapping.simple.SimpleMapDatastore
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import java.sql.SQLException

@ConfineMetaClassChanges([CustomReportField,QualitySampling,QualitySubmission,QualityCaseData,WorkflowState,Sql])
class QualityServiceSpec extends Specification implements DataTest, ServiceUnitTest<QualityService> {
    public static final user = "unitTest"
    private SimpleDriverDataSource reportDataSourcePVR


    def setupSpec() {
        mockDomains User, UserGroup, QualityCaseData, QualitySubmission, WorkflowJustification, QualitySampling, ActionItem, QualityField, WorkflowRule, WorkflowState, ReportTemplate, ReportField, Role, UserRole, Tenant, Notification
    }

    def setup() {
        WorkflowState  workflowState = new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test")
        QualityCaseData qualityCaseData = new QualityCaseData(caseNumber: 'caseNumber1', errorType: 'errorType1', tenantId: 1L,assignedToUser: null, assignedToUserGroup: null, workflowState:workflowState )
        qualityCaseData.save(validate: false)
        QualitySubmission qualitySubmission = new QualitySubmission(caseNumber: 'caseNumber2', errorType: 'errorType2', tenantId: 1L,assignedToUser: null, assignedToUserGroup: null, workflowState: workflowState)
        qualitySubmission.save(validate: false)
        QualitySampling qualitySampling = new QualitySampling(caseNumber: 'caseNumber2', errorType: 'errorType3', tenantId: 1L,assignedToUser: null, assignedToUserGroup: null, workflowState: workflowState, type:"SAMPLING")
        qualitySampling.save(validate: false)
        User user=new User(username: 'testUsername',createdBy:'user',modifiedBy:'user')
        user.save(validate: false)
        ActionItem actionItem= new ActionItem(description:'testActionItem',deleted:false,status:StatusEnum.CLOSED)
        actionItem.save(validate: false)
        QualityCaseData qualityCaseData2 = new QualityCaseData(caseNumber: 'caseNumber1', errorType: 'errorType1', actionItems: actionItem, tenantId: 1L, workflowState: workflowState)
        qualityCaseData2.save(validate: false)
        QualitySubmission qualitySubmission2 = new QualitySubmission(caseNumber: 'caseNumber2', errorType: 'errorType2', actionItems: actionItem, tenantId: 1L, workflowState: workflowState)
        qualitySubmission2.save(validate: false)
        QualitySampling qualitySampling2 = new QualitySampling(caseNumber: 'caseNumber2', errorType: 'errorType3', actionItems: actionItem, tenantId: 1L, workflowState: workflowState, type:"SAMPLING")
        qualitySampling2.save(validate: false)
        ActionItem actionItem2= new ActionItem(description:'testActionItem2',deleted:false,status: StatusEnum.OPEN)
        actionItem2.dueDate=new Date() + 10
        actionItem2.save(validate: false)
        QualityCaseData qualityCaseData3 = new QualityCaseData(caseNumber: 'caseNumber1', errorType: 'errorType1', actionItems: actionItem2, tenantId: 1L, workflowState: workflowState)
        qualityCaseData3.save(validate: false)
        QualitySubmission qualitySubmission3 = new QualitySubmission(caseNumber: 'caseNumber2', errorType: 'errorType2', actionItems: actionItem2, tenantId: 1L, workflowState: workflowState)
        qualitySubmission3.save(validate: false)
        QualitySampling qualitySampling3 = new QualitySampling(caseNumber: 'caseNumber2', errorType: 'errorType3', actionItems: actionItem2, tenantId: 1L, workflowState:workflowState, type:"SAMPLING")
        qualitySampling3.save(validate: false)
        Date dueDate = Date.parse("MM/dd/yyyy HH:mm:ss","03/03/2020 10:00:00")
        ActionItemCategory actionItemCategory= new ActionItemCategory(name:'testName',key:'testKey')
        ActionItem actionItem3= new ActionItem( actionCategory:actionItemCategory,newObj:true,description:'testActionItem3',priority:'testHigh',assignedTo:user,deleted:false,status: StatusEnum.OPEN,createdBy:'user',modifiedBy:'user')
        actionItem3.dueDate=new Date() - 1
        actionItem3.save(validate: false)
        QualityCaseData qualityCaseData4 = new QualityCaseData(caseNumber: 'caseNumber1', errorType: 'errorType1', actionItems: [actionItem3], tenantId: 1L, workflowState: workflowState)
        qualityCaseData4.save(validate: false)
        QualitySubmission qualitySubmission4 = new QualitySubmission(caseNumber: 'caseNumber2', errorType: 'errorType2', actionItems: [actionItem3], tenantId: 1L, workflowState: workflowState)
        qualitySubmission4.save(validate: false)
        QualitySampling qualitySampling4 = new QualitySampling(caseNumber: 'caseNumber2', errorType: 'errorType3', actionItems: [actionItem3], tenantId: 1L, workflowState: workflowState, type:"SAMPLING")
        qualitySampling4.save(validate: false)
        QualityField qualityField=new QualityField(qualityModule:PvqTypeEnum.CASE_QUALITY.toString(),fieldName:'testFieldNameCaseQuality',fieldType:'testFieldType', reportIds: 1, execReportId: 1)
        qualityField.save(failOnError:true)
        QualityField qualityField2=new QualityField(qualityModule:PvqTypeEnum.SAMPLING.toString(),fieldName:'testFieldNameSampling',fieldType:'testFieldType', reportIds: 2, execReportId: 2)
        qualityField2.save(failOnError:true)
        QualityField qualityField3=new QualityField(qualityModule:PvqTypeEnum.SUBMISSION_QUALITY.toString(),fieldName:'testFieldNameSubmissionQuality',fieldType:'testFieldType', reportIds: 3, execReportId: 3)
        qualityField3.save(failOnError:true)

        Holders.config.qualityModule.qualityTagName="PV Quality: Data Quality"
        Holders.config.qualityModule.qualityLabel="Case Data Quality"
        Holders.config.qualityModule.submissionTagName="PV Quality: Submission Quality"
        Holders.config.qualityModule.submissionLabel="Submission Quality"
        Holders.config.qualityModule.pvqPriorityList=[[name: "Major", value: "PVQ Priority: Major"], [name: "Minor", value: "PVQ Priority: Minor"]]

        Holders.config.qualityModule.dbFieldToQualityFieldMap = [masterCaseNum     : "masterCaseNum",
                                                                 masterVersionNum  : "masterVersionNum",
                                                                 cifTxtDateReceipt : "masterCaseReceiptDate",
                                                                 masterRptTypeId   : "masterRptTypeId",
                                                                 masterCountryId   : "masterCountryId",
                                                                 masterPrimProdName: "masterPrimProdName"]
        Holders.config.qualityModule.qualityColumnList = ["masterCaseNum", "masterVersionNum", "masterCaseReceiptDate", "masterRptTypeId", "masterCountryId", "masterPrimProdName"]
        Holders.config.qualityModule.submissionColumnList = ["masterCaseNum", "masterVersionNum", "masterCaseReceiptDate", "masterRptTypeId", "masterCountryId", "masterPrimProdName"]
        Holders.config.qualityModule.extraColumnList = ["errorType"]
        Holders.config.qualityModule.qualityControllers = ["quality", "issue"]
        Holders.config.qualityModule.dbFieldToSamplingFieldMap = [masterCaseNum       : 'masterCaseNum',
                                                                  masterPrimaryHcpFlag: 'masterPrimaryHcpFlag',
                                                                  masterCountryId     : 'masterCountryId',
                                                                  cifTxtDateReceipt   : 'cifTxtDateReceipt',
                                                                  masterRptTypeId     : 'masterRptTypeId',
                                                                  patInfoPatientAgeYears : 'patInfoPatientAgeYears',
                                                                  caseParentInfoPvrDob : 'caseParentInfoPvrDob',
                                                                  patInfoGenderId     : 'patInfoGenderId',
                                                                  productProdNameDrugType  : 'productProdNameDrugType',
                                                                  masterPrefTermList  : 'masterPrefTermList',
                                                                  narrativeNarrative  : 'narrativeNarrative',
                                                                  masterVersionNum    : 'masterVersionNum']
        Holders.config.qualityModule.additional = [
                [name:"SAMPLING", label: "Case Sampling",workflow:1, tag: "PV Quality: Sampling",  columnList :["masterCaseNum","masterVersionNum", "masterPrimaryHcpFlag","masterCountryId",
                                                                                                                "cifTxtDateReceipt","masterRptTypeId","patInfoPatientAgeYears",
                                                                                                                "caseParentInfoPvrDob","patInfoGenderId","productProdNameDrugType",
                                                                                                                "masterPrefTermList"]]
        ]

        Holders.config.argus_attachments = [
                list   : "select ROWID, FILETYPE,TO_CHAR(NOTES) from case_notes_attach where BLOBSIZE>0 and DELETED is NULL and CASE_ID = ?",
                content: "select ROWID, FILETYPE,TO_CHAR(NOTES), DATA from case_notes_attach where ROWID= ?"
        ]
        Holders.config.pvcm_attachments = [
                list   : "select REPORT_DATA_ID, FLAG_INCLUDED_DOCUMENTS, TO_CHAR(NOTES), VERSION_NUM from c_lit_reference_fu where flag_included_documents is not null and CASE_ID = ?"
        ]

        Holders.config.pv.app.settings["PVQuality"] = [[
                                                               name    : 'app.label.quality.observations.title',
                                                               icon    : 'md md-widgets',
                                                               link    : '#',
                                                               position: 2,
                                                               role    : 'ROLE_PVQ_VIEW',
                                                               children: [
                                                                       [
                                                                               name    : 'app.label.quality.observations.case.data.quality',
                                                                               icon    : '',
                                                                               link    : '/quality/caseDataQuality',
                                                                               position: 1,
                                                                               role    : 'ROLE_PVQ_VIEW',
                                                                       ],
                                                                       [
                                                                               name    : 'app.label.quality.observations.submissions.quality',
                                                                               icon    : '',
                                                                               link    : '/quality/submissionQuality',
                                                                               position: 2,
                                                                               role    : 'ROLE_PVQ_VIEW',
                                                                       ]
                                                               ]
                                                       ]]
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

    private User makeNormalUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: user, modifiedBy: user)
        def userRole = new Role(authority: 'ROLE_PVQ_VIEW', createdBy: user, modifiedBy: user).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Joe Griffin", preference: preferenceNormal, createdBy: user, modifiedBy: user)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getUser(0..1) { -> user }
        securityMock.demand.isCurrentUserDev(0..2) { false }
        securityMock.demand.getCurrentUser(0..2) {  -> user }
        return securityMock.proxyInstance()
    }


    @Unroll
    void "test update Priority -- Success "() {
        given:
        def result
        int count = 0
        service.CRUDService = [saveOrUpdate: { Object theInstance ->
            result = theInstance.priority
            count++
        }]
        when: "call update priority"
        def params = value
        service.updatePriority(params, 1L)

        then:
        result == 'priorityValue'
        countResult == count
        where:
        value << [[dataType: PvqTypeEnum.CASE_QUALITY.toString(), caseNumber: 'caseNumber1', errorType: 'errorType1', value: 'priorityValue', id: '1'],
                  [dataType: PvqTypeEnum.SUBMISSION_QUALITY.toString(), caseNumber: 'caseNumber2', errorType: 'errorType2', value: 'priorityValue', id: '2'],
                  [dataType: PvqTypeEnum.SAMPLING.toString(), caseNumber: 'caseNumber2', errorType: 'errorType3', value: 'priorityValue', id: '3', selectedIds: "1;2;3"]
        ]
        countResult << [1, 1, 3]

    }

    @Unroll
    void "test update Priority -- Failed "() {
        given:
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveOrUpdate(_) >> { throw new Exception() }
        service.CRUDService = mockCRUDService
        when: "call update priority"
        def params = value
        def result = service.updatePriority(params, 1L)

        then:
        thrown Exception

        where:
        value << [[dataType: PvqTypeEnum.CASE_QUALITY.toString(), caseNumber: 'caseNumber1', errorType: 'errorType1', value: 'priorityValue', id: '1'],
                  [dataType: PvqTypeEnum.SUBMISSION_QUALITY.toString(), caseNumber: 'caseNumber2', errorType: 'errorType2', value: 'priorityValue', id: '2'],
                  [dataType: PvqTypeEnum.SAMPLING.toString(), caseNumber: 'caseNumber2', errorType: 'errorType3', value: 'priorityValue', id: '3']
        ]

    }

    void "test initializeQualityObjByIdAndTenantId"() {
        given:
        QualityCaseData.metaClass.static.findByIdAndTenantId = { Long i, Long t ->
            new QualityCaseData(id: 1L, caseNumber: 'testCaseNum1', errorType: 'testErrorType1', tenantId: 1L, isDeleted: false)
        }
        QualitySubmission.metaClass.static.findByIdAndTenantId = { Long i, Long t ->
            new QualitySubmission(id: 2L, caseNumber: 'testCaseNum2', errorType: 'testErrorType2', tenantId: 1L, isDeleted: false)
        }
        QualitySampling.metaClass.static.findByIdAndTenantId = { Long i, Long t ->
            new QualitySampling(id: 3L, caseNumber: 'testCaseNum3', errorType: 'testErrorType3', tenantId: 1L, isDeleted: false, type: PvqTypeEnum.SAMPLING.toString())
        }
        when:
        def result = service.initializeQualityObjByIdAndTenantId(idString, dataType, 1L)
        then:
        result.toString() == value

        where:
        idString | dataType                                  | value
        '1'      | PvqTypeEnum.CASE_QUALITY.toString()       | 'testCaseNum1 - testErrorType1'
        '2'      | PvqTypeEnum.SUBMISSION_QUALITY.toString() | 'testCaseNum2 - testErrorType2'
        '3'      | PvqTypeEnum.SAMPLING.toString()           | '(Case Sampling) testCaseNum3 - testErrorType3'

    }

    void "test Quality Fields Save"(){

        given:
        Long reportId = testReportId
        Long execReportId = testExecutedReportId
        List<QualityField> rptFields = testReportFields
        String type = typeValue?.name()

        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.saveWithoutAuditLog(_)>>{ }
        mockCRUDService.updateWithoutAuditLog(_)>>{}
        service.CRUDService=mockCRUDService

        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        service.utilService=mockUtilService
        def mock = new MockFor(Sql)
        mock.demand.firstRow(0..6) { String s, Object[] params ->
            return [1: 1]
        }
        mock.demand.close(0..6) { -> }
        when:
        mock.use {
            service.qualityFieldsSave(reportId, execReportId, rptFields, type)
        }
        then:
        noExceptionThrown()

        where:
        testReportId | testExecutedReportId | testReportFields                                                                         | typeValue
        1            | 1                    | [new QualityField(fieldName:'testFieldNameCaseQuality',fieldType:'testFieldType')]       | PvqTypeEnum.CASE_QUALITY
        2            | 2                    | [new QualityField(fieldName:'testFieldNameSampling',fieldType:'testFieldType')]          | PvqTypeEnum.SAMPLING
        3            | 3                    | [new QualityField(fieldName:'testFieldNameSubmissionQuality',fieldType:'testFieldType')] | PvqTypeEnum.SUBMISSION_QUALITY
        1            | 1                    | [new QualityField(fieldName:'testFieldNameCaseQuality1',fieldType:'testFieldType1')]     | PvqTypeEnum.CASE_QUALITY
        1            | 4                    | [new QualityField(fieldName:'testFieldNameCaseQuality2',fieldType:'testFieldType2')]     | PvqTypeEnum.CASE_QUALITY
        5            | 5                    | [new QualityField(fieldName:'testFieldNameCaseQuality',fieldType:'testFieldType')]       | PvqTypeEnum.CASE_QUALITY
    }

    @Unroll
    void "test update Assigned Owner -- Success "(){
        given:
            def mockCRUDService=Mock(CRUDService)
            mockCRUDService.saveOrUpdate(_)>>{}
            service.CRUDService=mockCRUDService
        def mockUserService=Mock(UserService)
        mockUserService.currentUser >> { new User(username: 'normalUser', id: 1L)}
        service.userService=mockUserService
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }

        service.utilService=mockUtilService
        Sql.metaClass.first = true
        Sql.metaClass.rows={String sql->
            if(first){
                first=false
                return [[id: '1']]
            }
            return [['errorType1','1'],['errorType2','2']]
        }
        Sql.metaClass.firstRow={String sql->
            return [count:2,cont:3]
        }
        QualityCaseData qualityCaseData = new QualityCaseData(id:1,caseNumber: 'caseNumber1',metadata:"{}", entryType:"M", errorType: 'errorType1', assignedToUser: null, assignedToUserGroup: null, actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: false), workflowStateUpdatedDate: "01-Feb-2021")
        qualityCaseData.save(validate: false)
        QualitySubmission qualitySubmission = new QualitySubmission(id:1,caseNumber: 'caseNumber2',metadata:"{}", entryType:"M", errorType: 'errorType2', assignedToUser: null, assignedToUserGroup: null, actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: false), workflowStateUpdatedDate: "01-Feb-2021")
        qualitySubmission.save(validate: false)
        QualitySampling qualitySampling = new QualitySampling(id:1,caseNumber: 'caseNumber3',metadata:"{}", entryType:"M", errorType: 'errorType3', assignedToUser: null, assignedToUserGroup: null, actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: false), workflowStateUpdatedDate: "01-Feb-2021", type:PvqTypeEnum.SAMPLING.name())
        qualitySampling.save(validate: false)
        QualityCaseData.metaClass.static.findByIdAndTenantId = { Long i, Long t ->
            qualityCaseData
        }
        QualitySubmission.metaClass.static.findByIdAndTenantId = { Long i, Long t ->
            qualitySubmission
        }
        QualitySampling.metaClass.static.findByIdAndTenantId = { Long i, Long t ->
            qualitySampling
        }
        User user = new User(id: 1).save(validate: false)
        UserGroup userGroup= new UserGroup(id: 1).save(validate: false)
        def result
        int count = 0
        service.CRUDService=[saveWithoutAuditLog:{Object theInstance->
            result = theInstance.assignedToUserGroup?"group":"user"
            count++
        }]
        when:"call update Assigned Owner "
            def params = value
            service.updateAssignedOwner(params, 1L)

        then:
            result==resultValue

        where:
            value<<[[dataType:PvqTypeEnum.CASE_QUALITY.toString(),caseNumber:'caseNumber1',errorType:'errorType1',value:'UserGroup_1'],
                    [dataType:PvqTypeEnum.SUBMISSION_QUALITY.toString(),caseNumber:'caseNumber2',errorType: 'errorType2',value:'User_1'],
                    [dataType:PvqTypeEnum.SAMPLING.toString(),caseNumber:'caseNumber2',errorType: 'errorType3',value:'User_1']
            ]
        resultValue<<["group","user","user"]

    }
    void "test update Assigned Owner -- Failed "(){
        given:
        service.metaClass.updateAssignedToForCase = {
            throw new Exception()
        }
        when:"call update Assigned Owner "
            def params = value
            def result= service.updateAssignedOwner(params, 1L)

        then:
        thrown Exception

        where:
            value<<[[dataType:PvqTypeEnum.CASE_QUALITY.toString(),caseNumber:'caseNumber1',errorType:'errorType1',value:2],
                    [dataType:PvqTypeEnum.SUBMISSION_QUALITY.toString(),caseNumber:'caseNumber2',errorType: 'errorType2',value:1],
                    [dataType:PvqTypeEnum.SAMPLING.toString(),caseNumber:'caseNumber2',errorType: 'errorType3',value:1]
            ]

    }

    void "test get Action Item Status For Quality Record, When quality Action Items is null"(){
        given:
            def id=1
            String qualityRecordType=type
        when:"call getActionItemStatusForQualityRecord"
            def result=service.getActionItemStatusForQualityRecord(id,qualityRecordType)

        then:
            result==null

        where:
            type<<[PvqTypeEnum.CASE_QUALITY.toString(),PvqTypeEnum.SUBMISSION_QUALITY.toString(),PvqTypeEnum.SAMPLING.toString()]
    }
    void "test get Action Item Status For Quality Record,When status CLOSED. "(){
        given:
            def id=2
            String qualityRecordType=type
        when:"call getActionItemStatusForQualityRecord"
            def result=service.getActionItemStatusForQualityRecord(id,qualityRecordType)

        then:
            result== ActionItemGroupState.CLOSED.toString()

        where:
            type<<[PvqTypeEnum.CASE_QUALITY.toString(),PvqTypeEnum.SUBMISSION_QUALITY.toString(),PvqTypeEnum.SAMPLING.toString()]
    }

    void "test get Action Item Status For Quality Record,When status not CLOSED and is not OVERDUE. "(){
        given:
        def id=3
        String qualityRecordType=type
        when:"call getActionItemStatusForQualityRecord"
        def result=service.getActionItemStatusForQualityRecord(id,qualityRecordType)

        then:
        result==ActionItemGroupState.WAITING.toString()

        where:
        type<<[PvqTypeEnum.CASE_QUALITY.toString(),PvqTypeEnum.SUBMISSION_QUALITY.toString(),PvqTypeEnum.SAMPLING.toString()]
    }
    void "test get Action Item Status For Quality Record,When status not CLOSED and is OVERDUE. "(){
        given:
            def id=4
            String qualityRecordType=type
        when:"call getActionItemStatusForQualityRecord"
            def result=service.getActionItemStatusForQualityRecord(id,qualityRecordType)

        then:
            result==ActionItemGroupState.OVERDUE.toString()

        where:
            type<<[PvqTypeEnum.CASE_QUALITY.toString(),PvqTypeEnum.SUBMISSION_QUALITY.toString(),PvqTypeEnum.SAMPLING.toString()]
    }
    void "test save Adhoc Quality Record, When params is null"(){
        given:
            def params=null
        when:
            def model=service.saveAdhocQualityRecord(params, 1L)
        then:
            model.status=='failure'

    }

    @Unroll
    void "test save Adhoc Quality Record -- Success"(){
        given:
        QualityCaseData qualityCaseData = new QualityCaseData(id:1,caseNumber: 'caseNumber1',metadata:"{}", entryType:"M", errorType: 'errorType1', assignedToUser: null, assignedToUserGroup: null, actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: false), workflowStateUpdatedDate: "01-Feb-2021")
        qualityCaseData.save(validate: false)
        QualitySubmission qualitySubmission = new QualitySubmission(id:1,caseNumber: 'caseNumber2',metadata:"{}", entryType:"M", errorType: 'errorType2', assignedToUser: null, assignedToUserGroup: null, actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: false), workflowStateUpdatedDate: "01-Feb-2021")
        qualitySubmission.save(validate: false)
        QualitySampling qualitySampling = new QualitySampling(id:1,caseNumber: 'caseNumber3',metadata:"{}", entryType:"M", errorType: 'errorType3', assignedToUser: null, assignedToUserGroup: null, actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: false), workflowStateUpdatedDate: "01-Feb-2021", type:PvqTypeEnum.SAMPLING.name())
        qualitySampling.save(validate: false)
            def params=value
            def mockCRUDService=Mock(CRUDService)
            (1..2)*mockCRUDService.save(_)>>{}
            service.CRUDService=mockCRUDService

        when:"call saveAdhocQualityRecord "
            def model=service.saveAdhocQualityRecord(params, 1L)

        then:
            model.status=='success'

        where:
        value << [
                [dataType: PvqTypeEnum.CASE_QUALITY.name(), dataType: PvqTypeEnum.CASE_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: '', comment: '',id:'1'],
                [dataType: PvqTypeEnum.CASE_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: '', comment: 'testComment',id:'1'],
                [dataType: PvqTypeEnum.CASE_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: 'testPriority', comment: '',id:'1'],
                [dataType: PvqTypeEnum.CASE_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: 'testPriority', comment: 'testComment',id:'1'],
                [dataType: PvqTypeEnum.SUBMISSION_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: '', comment: '',id:'1'],
                [dataType: PvqTypeEnum.SUBMISSION_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: '', comment: 'testComment',id:'1'],
                [dataType: PvqTypeEnum.SUBMISSION_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: 'testPriority', comment: '',id:'1'],
                [dataType: PvqTypeEnum.SUBMISSION_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: 'testPriority', comment: 'testComment',id:'1']
        ]

    }

    @Unroll
    void "test save Adhoc Quality Record -- Failed"(){
        given:
        QualityCaseData qualityCaseData = new QualityCaseData(id:1,caseNumber: 'caseNumber1',metadata:"{}", entryType:"M", errorType: 'errorType1', assignedToUser: null, assignedToUserGroup: null, actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: false), workflowStateUpdatedDate: "01-Feb-2021")
        qualityCaseData.save(validate: false)
        QualitySubmission qualitySubmission = new QualitySubmission(id:1,caseNumber: 'caseNumber2',metadata:"{}", entryType:"M", errorType: 'errorType2', assignedToUser: null, assignedToUserGroup: null, actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: false), workflowStateUpdatedDate: "01-Feb-2021")
        qualitySubmission.save(validate: false)
        QualitySampling qualitySampling = new QualitySampling(id:1,caseNumber: 'caseNumber3',metadata:"{}", entryType:"M", errorType: 'errorType3', assignedToUser: null, assignedToUserGroup: null, actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: false), workflowStateUpdatedDate: "01-Feb-2021", type:PvqTypeEnum.SAMPLING.name())
        qualitySampling.save(validate: false)
        def params=value
        def mockCRUDService=Mock(CRUDService)
        (1..2)*mockCRUDService.save(_)>>{throw new Exception()}
        service.CRUDService=mockCRUDService

        when:"call saveAdhocQualityRecord "
        def model=service.saveAdhocQualityRecord(params, 1L)

        then:
        model.status=='failure'

        where:
        value << [
                [dataType: PvqTypeEnum.CASE_QUALITY.name(), dataType: PvqTypeEnum.CASE_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: '', comment: '',id:'1'],
                [dataType: PvqTypeEnum.CASE_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: '', comment: 'testComment',id:'1'],
                [dataType: PvqTypeEnum.CASE_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: 'testPriority', comment: '',id:'1'],
                [dataType: PvqTypeEnum.CASE_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: 'testPriority', comment: 'testComment',id:'1'],
                [dataType: PvqTypeEnum.SUBMISSION_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: '', comment: '',id:'1'],
                [dataType: PvqTypeEnum.SUBMISSION_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: '', comment: 'testComment',id:'1'],
                [dataType: PvqTypeEnum.SUBMISSION_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: 'testPriority', comment: '',id:'1'],
                [dataType: PvqTypeEnum.SUBMISSION_QUALITY.name(), masterCaseNum: 'testMasterCaseNum', errorType: 'errorType1', priority: 'testPriority', comment: 'testComment',id:'1']
        ]
    }

    void "test get Quality Report All Fields"(){
        given:
        ReportField.metaClass.static.findByName = { String f ->
            return reportField
        }

        when:"call getQualityReportAllFields"
            def fields=service.getQualityReportAllFields(type)

        then:
            fields[0].fieldName==fieldName
            fields[0].fieldType=='testFieldType'
            fields[0].fieldLabel!=null
            fields[0].selectable==selectable

        where:
            type                                  | fieldName                        | reportField                                                              | selectable
            PvqTypeEnum.CASE_QUALITY.name()       | 'testFieldNameCaseQuality'       | new ReportField(name: 'testFieldNameCaseQuality', lmSQL: 'lmSQL1')       | false
            PvqTypeEnum.SUBMISSION_QUALITY.name() | 'testFieldNameSubmissionQuality' | new ReportField(name: 'testFieldNameSubmissionQuality', lmSQL: 'lmSQL2') | false
            PvqTypeEnum.SAMPLING.name()           | 'testFieldNameSampling'          | new ReportField(name: 'testFieldNameSubmissionQuality', lmSQL: null)     | false
    }

    def newConn(){
        reportDataSourcePVR = new SimpleDriverDataSource()
        Properties properties = new Properties()
        properties.put("defaultRowPrefetch", grailsApplication.config.jdbcProperties.fetch_size ?: 50)
        properties.put("defaultBatchValue", grailsApplication.config.jdbcProperties.batch_size ?: 5)
        properties.put("dbdriver", "com.mysql.jdbc.Driver")
        reportDataSourcePVR.setConnectionProperties(properties)
        reportDataSourcePVR.setDriverClass(org.h2.Driver)
        reportDataSourcePVR.setUsername('sa')
        reportDataSourcePVR.setPassword('sa')
        reportDataSourcePVR.setUrl('jdbc:h2:mem:testDb')

        return reportDataSourcePVR.getConnection('sa','sa')
    }

    void "test fetch Quality Module Errors List"(){
        given:
        String errorType = errorTypeVal
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        service.utilService=mockUtilService
        Sql.metaClass.rows={String sql->
            return dbrecordsValue
        }

        when:
        def model = service.fetchQualityModuleErrorsList(errorType, 1L)

        then:
        model== ['errorType1', 'errorType2', 'errorType3']

        where:
        errorTypeVal                    | dbrecordsValue
        PvqTypeEnum.CASE_QUALITY.name() | [['ERROR_TYPE': 'errorType1'], ['ERROR_TYPE': 'errorType2'], ['ERROR_TYPE': 'errorType3']]
        PvqTypeEnum.CASE_QUALITY.name() | [['ERROR_TYPE': 'errorType1'], ['ERROR_TYPE': 'errorType2'], ['ERROR_TYPE': 'errorType3']]
        ""                              | [['ERROR_TYPE': 'errorType1'], ['ERROR_TYPE': 'errorType2'], ['ERROR_TYPE': 'errorType3']]
    }

    @Unroll
    void "test get Quality Data List,When dbrecords is empty"(){
        given:
        Integer offset=0
        Integer max=2
        String sort=sortValue
        String direction='Asc'
        String type = typeValue?.name()
        boolean isChartDataRequired=false
        Map externalSearch= externalSearchValueMap//[:]
        Map advanceFilter=null
        def mockUserService=Mock(UserService)
        mockUserService.currentUser >> { new User(username: 'normalUser', id: 1L)}
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
         return newConn()
        }

        service.utilService=mockUtilService
        Sql.metaClass.rows={String sql->
            return dbrecordsValue
        }

        service.metaClass.getQualityIssues = {
            [[id: 1, textDesc: "Late1", ownerApp: "Pvq"]]
        }
        service.metaClass.getRootCauses = {
            [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        service.metaClass.getResponsibleParties = {
            [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        User user =new User(username: 'testUsername1',createdBy:'user',modifiedBy:'user')
        def normalUser = makeNormalUser()
        service.userService = makeSecurityService(normalUser)
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> return false}
        when:"call getQualityDataList"
        def model=service.getQualityDataList(offset,max,sort,direction,type,isChartDataRequired,externalSearch,advanceFilter, 1L,"ICV",null,null)

        then:
        model.aaData[0]==null
        model.recordsTotal==0
        model.recordsFiltered==0

        where:
        typeValue                      | sortValue               | dbrecordsValue | externalSearchValueMap
        PvqTypeEnum.CASE_QUALITY       | ''                      | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020", showTriage: 1]
        PvqTypeEnum.CASE_QUALITY       | ''                      | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"]
        PvqTypeEnum.CASE_QUALITY       | ''                      | []             | [:]
        PvqTypeEnum.CASE_QUALITY       | 'masterCaseReceiptDate' | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020", showTriage: 1]
        PvqTypeEnum.CASE_QUALITY       | 'masterCaseReceiptDate' | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"]
        PvqTypeEnum.CASE_QUALITY       | 'masterCaseReceiptDate' | []             | [:]
        PvqTypeEnum.SAMPLING           | ''                      | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020", showTriage: 1]
        PvqTypeEnum.SAMPLING           | ''                      | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"]
        PvqTypeEnum.SAMPLING           | ''                      | []             | [:]
        PvqTypeEnum.SAMPLING           | 'masterCaseReceiptDate' | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020", showTriage: 1]
        PvqTypeEnum.SAMPLING           | 'masterCaseReceiptDate' | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"]
        PvqTypeEnum.SAMPLING           | 'masterCaseReceiptDate' | []             | [:]
        PvqTypeEnum.SUBMISSION_QUALITY | ''                      | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020", showTriage: 1]
        PvqTypeEnum.SUBMISSION_QUALITY | ''                      | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"]
        PvqTypeEnum.SUBMISSION_QUALITY | ''                      | []             | [:]
        PvqTypeEnum.SUBMISSION_QUALITY | 'masterCaseReceiptDate' | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020", showTriage: 1]
        PvqTypeEnum.SUBMISSION_QUALITY | 'masterCaseReceiptDate' | []             | [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"]
        PvqTypeEnum.SUBMISSION_QUALITY | 'masterCaseReceiptDate' | []             | [:]
    }

    @Unroll
    void "test get Quality Data List,When isChartDataRequired is false"(){
        given:
        service.targetDatastore = new SimpleMapDatastore(['pva'], Late)
        service.targetDatastore = new SimpleMapDatastore(['pva'], RootCause)
        service.targetDatastore = new SimpleMapDatastore(['pva'], ResponsibleParty)

        Integer offset=0
        Integer max=2
        String sort=sortValue
        String direction='Asc'
        String type = typeValue?.name()
        boolean isChartDataRequired=false
        Map externalSearch= externalSearchValue
        Map advanceFilter=null
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }

        service.utilService=mockUtilService
        Sql.metaClass.rows={String sql->
            return dbrecordsValue
        }

        QualityCaseData.metaClass.static.getAll =  {List  ids ->
            [
                    new QualityCaseData(id:1, caseNumber: "testCaseNumber",errorType: "testErrorType", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState()),
                    new QualityCaseData(id:2, caseNumber: "testCaseNumber2",errorType: "testErrorType2", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState())
            ]
        }
        QualitySubmission.metaClass.static.getAll = {List  ids ->
            [
                    new QualityCaseData(id:1, caseNumber: "testCaseNumber",errorType: "testErrorType", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState()),
                    new QualityCaseData(id:2, caseNumber: "testCaseNumber2",errorType: "testErrorType2", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState())
            ]
        }
        QualitySampling.metaClass.static.getAll = {List  ids ->
            [
                    new QualityCaseData(id:1, type:"1", caseNumber: "testCaseNumber",errorType: "testErrorType", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState()),
                    new QualityCaseData(id:2, type:"1", caseNumber: "testCaseNumber2",errorType: "testErrorType2", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState())
            ]
        }
        def normalUser = makeNormalUser()
        service.userService = makeSecurityService(normalUser)
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> return false}
        when:"call getQualityDataList"
        def model = service.getQualityDataList(offset, max, sort, direction, type, isChartDataRequired, externalSearch, advanceFilter, 1L)

        then:
        model.aaData[0].caseNumber=='testCaseNumber'
        model.aaData[0].errorType=='testErrorType'
        model.aaData[1].caseNumber=='testCaseNumber2'
        model.aaData[1].errorType=='testErrorType2'
        model.recordsTotal==2
        model.recordsFiltered==2

        where:
        externalSearchValue                                            | typeValue                      | sortValue               | dbrecordsValue
        [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"] | PvqTypeEnum.CASE_QUALITY       | ''                      | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        [:]                                                            | PvqTypeEnum.CASE_QUALITY       | ''                      |[['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"] | PvqTypeEnum.CASE_QUALITY       | 'masterCaseReceiptDate' |[['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        [:]                                                            | PvqTypeEnum.CASE_QUALITY       | ''                      | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"] | PvqTypeEnum.SAMPLING           | ''                      | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        [:]                                                            | PvqTypeEnum.SAMPLING           | ''                      | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"] | PvqTypeEnum.SAMPLING           | 'masterCaseReceiptDate' | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        [:]                                                            | PvqTypeEnum.SAMPLING           | ''                      | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"] | PvqTypeEnum.SUBMISSION_QUALITY | ''                      | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        [:]                                                            | PvqTypeEnum.SUBMISSION_QUALITY | ''                      | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020"] | PvqTypeEnum.SUBMISSION_QUALITY | 'masterCaseReceiptDate' | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        [:]                                                            | PvqTypeEnum.SUBMISSION_QUALITY | ''                      | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]

    }

    void "test get Quality Data List, When isChartDataRequired is true"(){
        given:
        Integer offset=0
        Integer max=2
        String sort=sortValue
        String direction='Asc'
        String type = typeValue?.name()
        boolean isChartDataRequired=true
        Map externalSearch=  [receiptDateFrom: "03-JAN-2020", receiptDateTo: "10-JAN-2020",errorType:'testErrorType4,testErrorType5']
        Map advanceFilter=null
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }

        service.utilService=mockUtilService
        Sql.metaClass.first = true
        Sql.metaClass.rows={String sql->
            if(first){
                first=false
                return dbrecordsValue
            }
            return [['ERROR_TYPE': 'errorType1', 'COUNT': '1'],['ERROR_TYPE': 'errorType2', 'COUNT': '2']]
        }
        service.metaClass.getQualityIssues = {
            [[id: 1, textDesc: "Late1", ownerApp: "Pvq"]]
        }
        service.metaClass.getRootCauses = {
            [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        service.metaClass.getResponsibleParties = {
            [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        QualityCaseData.metaClass.static.getAll =  {List  ids ->
            [
                    new QualityCaseData(id:1, caseNumber: "testCaseNumber",errorType: "testErrorType", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState()),
                    new QualityCaseData(id:2, caseNumber: "testCaseNumber2",errorType: "testErrorType2", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState())
            ]
        }
        QualitySubmission.metaClass.static.getAll = {List  ids ->
            [
                    new QualityCaseData(id:1, caseNumber: "testCaseNumber",errorType: "testErrorType", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState()),
                    new QualityCaseData(id:2, caseNumber: "testCaseNumber2",errorType: "testErrorType2", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState())
            ]
        }
        QualitySampling.metaClass.static.getAll = {List  ids ->
            [
                    new QualityCaseData(id:1, type:"1", caseNumber: "testCaseNumber",errorType: "testErrorType", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState()),
                    new QualityCaseData(id:2, type:"1", caseNumber: "testCaseNumber2",errorType: "testErrorType2", metadata: '{ "testKey": "testValue" }',entryType: 'A', workflowStateUpdatedDate: new Date(),dateCreated: new Date(),dueDate: new Date(), workflowState: new WorkflowState())
            ]
        }
        def normalUser = makeNormalUser()
        service.userService = makeSecurityService(normalUser)
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String roles -> return false}
        when:"call getQualityDataList"
        def model=service.getQualityDataList(offset,max,sort,direction,type,isChartDataRequired,externalSearch,advanceFilter, 1L)

        then:
        model.aaData[0].caseNumber=='testCaseNumber'
        model.aaData[0].errorType=='testErrorType'
        model.recordsTotal==2
        model.recordsFiltered==2
        model.chartData[0].errorType=='errorType2'
        model.chartData[0].count=='2'

        where:
        typeValue                      | sortValue               | dbrecordsValue
        PvqTypeEnum.CASE_QUALITY       | ''                      | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        PvqTypeEnum.CASE_QUALITY       | 'masterCaseReceiptDate' | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        PvqTypeEnum.SAMPLING           | ''                      | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        PvqTypeEnum.SAMPLING           | 'masterCaseReceiptDate' | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        PvqTypeEnum.SUBMISSION_QUALITY | ''                      | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]
        PvqTypeEnum.SUBMISSION_QUALITY | 'masterCaseReceiptDate' | [['ID': 1L, "recordCount":2],['ID': 2L, "recordCount":2]]

    }


    void "test get Quality Data List, when cannot get Report Connection For PVR"(){
        given:
        Integer offset=0
        Integer max=20
        String sort=''
        String direction='Asc'
        String type = PvqTypeEnum.CASE_QUALITY.name()
        boolean isChartDataRequired=false
        Map externalSearch= [:]
        Map advanceFilter=null
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            throw new SQLException()
        }
        service.utilService=mockUtilService
        when:"call getQualityDataList"
        def model=service.getQualityDataList(offset,max,sort,direction,type,isChartDataRequired,externalSearch,advanceFilter, 1L)

        then:
        thrown Exception
    }

    void "test get Quality Data Entity List Search, external search and advanced Filter is null"(){
        given:
        String type = typeValue.name()
        String qualityMonitoringType = null
        if (type == PvqTypeEnum.CASE_QUALITY.name()) {
            qualityMonitoringType = "Case Data Quality"
        } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
            qualityMonitoringType = "Submission Quality"
        } else {
            qualityMonitoringType = "Case Sampling"
        }
        Map externalSearch= [:]
        Map advanceFilter=null
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        service.utilService=mockUtilService
        Sql.metaClass.rows = {String sql->
            return dbrecordsValue
        }
        Sql.metaClass.firstRow={String sql->
            return [count:2,cont:3]
        }
        service.metaClass.getQualityIssues = {
            [[id: 1, textDesc: "Late1", ownerApp: "Pvq"]]
        }
        service.metaClass.getRootCauses = {
            [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        service.metaClass.getResponsibleParties = {
            [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        service.metaClass.getCommentsByQualityIds = { List idsArray, String dataType, Sql sql ->
            [1L: 'testComment']
        }
        service.metaClass.getActionItemByQualityIds = { List idsArray, String dataType, Sql sql ->
            [1L: [1, 'OPEN', new Date()]]
        }
        service.metaClass.getStateByQualityIds = { List idsArray, String dataType, Sql sql ->
            [1L: ['New', false]]
        }
        service.metaClass.getRCADataForQualityRecord = { Long id, String dataType, Map<String,Map<Long,String>> allRcaDataMap ->
            ['ROOT_CAUSE':'(empty)', 'LATE':'(empty)', 'RESPONSIBLE_PARTY':'(empty)']
        }
        User user =new User(username: 'testUsername1',createdBy:'user',modifiedBy:'user')
        WorkflowState.metaClass.static.findById = { Long id ->
            new WorkflowState(id: 2, name: 'NEW', createdBy: user, modifiedBy: user)
        }
        when:"call getQualityDataList"
        def model=service.getQualityDataEntityListSearch(type,externalSearch,advanceFilter, 1L, false)

        then:
        model[0] == ['assignedToUser': null, 'assignedToUserGroup': null, assignedToUserGroupId: '', assignedToUserId: '', 'testKey': 'testValue', 'errorType': 'Blank Outcome', 'priority': 'Minor', 'qualityIssueType': '(empty)', rootCause: '(empty)', 'responsibleParty': '(empty)', 'state': 'NEW', dueIn: '07-Feb-2023', dateCreated: '07-Feb-2023', 'latestComment': null, 'qualityIssueTypeId': null, 'qualityMonitoringType': qualityMonitoringType, executedTemplateId: null]

        where:
        typeValue                      | dbrecordsValue
        PvqTypeEnum.CASE_QUALITY       | [['CASE_NUM':'20US00028404', 'ERROR_TYPE':'Blank Outcome', 'PRIORITY':'Minor','METADATA':[characterStream: [text: '{ "testKey": "testValue" }']], 'REPORT_ID':14,
                                           'WORKFLOW_STATE_UPDATED_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DATE_CREATED':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DUE_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"),'ASSIGNED_TO_USER':null, 'ASSIGNED_TO_USERGROUP':null, ID:10, 'ENTRY_TYPE':'A', 'EXEC_REPORT_ID':43, 'WORKFLOW_STATE_ID':2, 'QUALITY_ISSUE_TYPE_ID':null]]
        PvqTypeEnum.SAMPLING           | [['CASE_NUM':'20US00028404', 'ERROR_TYPE':'Blank Outcome', 'PRIORITY':'Minor','METADATA':[characterStream: [text: '{ "testKey": "testValue" }']], 'REPORT_ID':14,
                                          'WORKFLOW_STATE_UPDATED_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DATE_CREATED':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DUE_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'ASSIGNED_TO_USER':null, 'ASSIGNED_TO_USERGROUP':null, ID:10, 'ENTRY_TYPE':'A', 'EXEC_REPORT_ID':43, 'WORKFLOW_STATE_ID':2, 'QUALITY_ISSUE_TYPE_ID':null]]
    }

    void "test get Quality Data Entity List Search, external search and advanced Filter is not null"(){
        given:
        String type = typeValue.name()
        String qualityMonitoringType = null
        if (type == PvqTypeEnum.CASE_QUALITY.name()) {
            qualityMonitoringType = Holders.config.qualityModule.qualityLabel
        } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
            qualityMonitoringType = Holders.config.qualityModule.submissionLabel
        } else {
            qualityMonitoringType = Holders.config.qualityModule.additional.find { it.name == type }?.label
        }
        Map externalSearch= ['receiptDateFrom':'21-May-2020', 'receiptDateTo':'12-May-2021', showTriage:0]
        Map advanceFilter= ['workflowGroup':['type':'manual', 'name':'workflowGroup', 'value':'final']]
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        service.utilService=mockUtilService
        Sql.metaClass.rows = {String sql->
            return dbrecordsValue
        }
        Sql.metaClass.firstRow={String sql->
            return [count:2,cont:3]
        }
        service.metaClass.getQualityIssues = {
            [[id: 1, textDesc: "Late1", ownerApp: "Pvq"]]
        }
        service.metaClass.getRootCauses = {
            [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        service.metaClass.getResponsibleParties = {
            [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        service.metaClass.getCommentsByQualityIds = { List idsArray, String dataType, Sql sql ->
            [1L: 'testComment']
        }
        service.metaClass.getActionItemByQualityIds = { List idsArray, String dataType, Sql sql ->
            [1L: [1, 'OPEN', new Date()]]
        }
        service.metaClass.getStateByQualityIds = { List idsArray, String dataType, Sql sql ->
            [1L: ['New', false]]
        }
        service.metaClass.getRCADataForQualityRecord = { Long id, String dataType, Map<String,Map<Long,String>> allRcaDataMap ->
            ['ROOT_CAUSE':'(empty)', 'LATE':'(empty)', 'RESPONSIBLE_PARTY':'(empty)']
        }
        User user =new User(username: 'testUsername1',createdBy:'user',modifiedBy:'user')
        WorkflowState.metaClass.static.findById = { Long id ->
            new WorkflowState(id: 2, name: 'NEW', createdBy: user, modifiedBy: user)
        }
        when:"call getQualityDataList"
        def model=service.getQualityDataEntityListSearch(type,externalSearch,advanceFilter, 1L, false)

        then:
        model[0]== ['assignedToUser': null, 'assignedToUserGroup': null, assignedToUserGroupId: '', assignedToUserId: '','testKey':'testValue', 'errorType':'Blank Outcome', 'priority':'Minor', 'qualityIssueType':'(empty)', rootCause:'(empty)', 'responsibleParty':'(empty)', 'state':'NEW', dueIn:"07-Feb-2023", dateCreated:"07-Feb-2023", 'latestComment':null, 'qualityIssueTypeId':null, 'qualityMonitoringType': qualityMonitoringType, executedTemplateId: null]

        where:
        typeValue                      | dbrecordsValue
        PvqTypeEnum.CASE_QUALITY       | [['CASE_NUM':'20US00028404', 'ERROR_TYPE':'Blank Outcome', 'PRIORITY':'Minor','METADATA':[characterStream: [text: '{ "testKey": "testValue" }']], 'REPORT_ID':14,
                                           'WORKFLOW_STATE_UPDATED_DATE': Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DUE_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DATE_CREATED':Date.parse("dd-MMM-yyyy","07-Feb-2023"),'ASSIGNED_TO_USER':null, 'ASSIGNED_TO_USERGROUP':null, ID:10, 'ENTRY_TYPE':'A', 'EXEC_REPORT_ID':43, 'WORKFLOW_STATE_ID':2, 'QUALITY_ISSUE_TYPE_ID':null]]
        PvqTypeEnum.SAMPLING           | [['CASE_NUM':'20US00028404', 'ERROR_TYPE':'Blank Outcome', 'PRIORITY':'Minor','METADATA':[characterStream: [text: '{ "testKey": "testValue" }']], 'REPORT_ID':14,
                                           'WORKFLOW_STATE_UPDATED_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DUE_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DATE_CREATED':Date.parse("dd-MMM-yyyy","07-Feb-2023"),'ASSIGNED_TO_USER':null, 'ASSIGNED_TO_USERGROUP':null, ID:10, 'ENTRY_TYPE':'A', 'EXEC_REPORT_ID':43, 'WORKFLOW_STATE_ID':2, 'QUALITY_ISSUE_TYPE_ID':null]]
        PvqTypeEnum.SUBMISSION_QUALITY | [['CASE_NUM':'20US00028404', 'ERROR_TYPE':'Blank Outcome', 'PRIORITY':'Minor','METADATA':[characterStream: [text: '{ "testKey": "testValue" }']], 'REPORT_ID':14,
                                           'WORKFLOW_STATE_UPDATED_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DUE_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DATE_CREATED':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'ASSIGNED_TO_USER':null, 'ASSIGNED_TO_USERGROUP':null, ID:10, 'ENTRY_TYPE':'A', 'EXEC_REPORT_ID':43, 'WORKFLOW_STATE_ID':2, 'QUALITY_ISSUE_TYPE_ID':null]]

    }

    void "test get Quality Data Entity List Search, when cannot get Report Connection For PVR"(){
        given:
        String type = PvqTypeEnum.CASE_QUALITY.name()
        Map externalSearch= [:]
        Map advanceFilter=null
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            throw new SQLException()
        }
        service.utilService=mockUtilService
        when:"call getQualityDataList"
        def model=service.getQualityDataEntityListSearch(type,externalSearch,advanceFilter, 1L, false)

        then:
        thrown Exception
    }

    @Unroll
    void "test fetch Quality Module Error Types"(){
        given:
            String errorType=errorTypeValue
        when:
            def result=service.fetchQualityModuleErrorTypes(errorType, 1L)
        then:
            result==resultValue
        where:
        errorTypeValue                            | resultValue
        null                                      | []
        ' '                                       | []
        PvqTypeEnum.CASE_QUALITY.toString()       | ['errorType1']
        PvqTypeEnum.SUBMISSION_QUALITY.toString() | ['errorType2']
        PvqTypeEnum.SAMPLING.toString()           | ['errorType3']

    }

    @Unroll
    void "test update Error Type -- Success"() {
        given:

        def result
        int count = 0
        service.CRUDService = [save: { Object theInstance ->
            result = theInstance.errorType
            count++
        }]
        when:
        def params = value
        service.updateErrorType(params, 1L)

        then:
        result == 'testNewErrorTypeValue'
        countResult == count

        where:

        value << [[dataType: PvqTypeEnum.CASE_QUALITY.toString(), caseNumber: 'caseNumber1', errorType: 'errorType1', value: 'testNewErrorTypeValue', id: '1'],
                  [dataType: PvqTypeEnum.SUBMISSION_QUALITY.toString(), caseNumber: 'caseNumber2', errorType: 'errorType2', value: 'testNewErrorTypeValue', id: '2'],
                  [dataType: PvqTypeEnum.SAMPLING.toString(), caseNumber: 'caseNumber2', errorType: 'errorType3', value: 'testNewErrorTypeValue', id: '3', selectedIds: "1;2;3"]
        ]
        countResult << [1, 1, 3]


    }

    @Unroll
    void "test update Error Type -- Failed"() {
        given:
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.save(_) >> {
            throw new Exception()
        }
        service.CRUDService = mockCRUDService
        when:
        def result = service.updateErrorType([dataType: PvqTypeEnum.CASE_QUALITY.toString(), caseNumber: 'caseNumber1', errorType: 'errorType1', value: 'testNewErrorTypeValue', id: '1', value: 'test'], 1L)

        then:
        thrown Exception
    }

    void "test export To Excel"(){
        given:
            def data=[['testErrorType', 'testPriority','' , 'testValue1'], ['testErrorType2', 'testPriority2', '', 'testValue2']]
            def metaData= [sheetName:'CaseDataQuality', columns:[[title:'app.label.errorType', width:20], [title:'app.label.action.item.priority', width:20], [title:'actionItem.assigned.to.label', width:20], [title:'app.reportField.testKey', width:20]]]
            def mockUserService=Mock(UserService)
            service.userService=mockUserService
        when:
            byte[] result=service.exportToExcel(data,metaData)

        then:
            result!=null
    }

    void "test appendQualityTypesToLeftMenu"(){
        when:
           service.appendQualityTypesToLeftMenu()
        then:
        grailsApplication.config.pv.app.settings["PVQuality"][0].children.size()>2
    }

    void "test getTypes"(){
        when:
           List list = service.getTypes(new ExecutedTemplateQuery(executedConfiguration:
                        new ExecutedConfiguration(pvqType: "pvqType")))
        then:
        list.size()==1

    }

    void "test get Case Number By IssueId"(){
        given:
        Long issueId = testIssueId
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        service.utilService=mockUtilService
        Sql.metaClass.first = true
        Sql.metaClass.rows={String sql->
            if(first){
                first=false
                return dbrecordsValue
            }
            return [[['caseNumber']]]
        }
        User user =new User(username: 'testUsername1',createdBy:'user',modifiedBy:'user')

        when:
        def result = service.getCaseNoByIssueId(issueId, 1L)

        then:
        result == resultValue

        where:

        testIssueId | dbrecordsValue    | resultValue
        1           | [['caseNumber1']] | 'caseNumber1'
        2           | [['caseNumber2']] | 'caseNumber2'
        3           | [['caseNumber3']] | 'caseNumber3'
        4           | [['caseNumber']]  | 'caseNumber'
    }

    void "test get Case Number By IssueId, when cannot get Report Connection For PVR"(){
        given:
        Long issueId = testIssueId
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            throw new SQLException()
        }
        service.utilService=mockUtilService
        User user =new User(username: 'testUsername1',createdBy:'user',modifiedBy:'user')

        when:
        def result = service.getCaseNoByIssueId(issueId, 1L)

        then:
        thrown Exception

        where:
        testIssueId << [1, 2, 3]
    }

    void "test get Quality Case Number By IssueId"(){
        given:
        Long issueId = testIssueId

        when:
        def result = service.getQualityCaseNoByIssueId(issueId, 1L)

        then:
        result == resultvalue

        where:
        testIssueId << [1, 2, 3]
        resultvalue << ['select CASE_NUM from quality_case_data quality inner join quality_case_issues qualityItems ON quality.id = qualityItems.quality_case_id   WHERE qualityItems.issue_id = 1 and quality.tenant_id = 1 ',
                        'select CASE_NUM from quality_case_data quality inner join quality_case_issues qualityItems ON quality.id = qualityItems.quality_case_id   WHERE qualityItems.issue_id = 2 and quality.tenant_id = 1 ',
                        'select CASE_NUM from quality_case_data quality inner join quality_case_issues qualityItems ON quality.id = qualityItems.quality_case_id   WHERE qualityItems.issue_id = 3 and quality.tenant_id = 1 '
        ]

    }

    void "test get Submission Case Number By IssueId"(){
        given:
        Long issueId = testIssueId

        when:
        def result = service.getSubmissionCaseNoByIssueId(issueId, 1L)

        then:
        result == resultvalue

        where:
        testIssueId << [1, 2, 3]
        resultvalue << ['select CASE_NUM from quality_submission quality inner join quality_submission_issues qualityItems ON quality.id = qualityItems.quality_submission_id  WHERE qualityItems.issue_id =  1 and quality.tenant_id = 1 ',
                        'select CASE_NUM from quality_submission quality inner join quality_submission_issues qualityItems ON quality.id = qualityItems.quality_submission_id  WHERE qualityItems.issue_id =  2 and quality.tenant_id = 1 ',
                        'select CASE_NUM from quality_submission quality inner join quality_submission_issues qualityItems ON quality.id = qualityItems.quality_submission_id  WHERE qualityItems.issue_id =  3 and quality.tenant_id = 1 '
        ]
    }

    void "test get Sampling Case Number By IssueId"(){
        given:
        Long issueId = testIssueId

        when:
        def result = service.getSamplingCaseNoByIssueId(issueId, 1L)

        then:
        result == resultvalue

        where:
        testIssueId << [1, 2, 3]
        resultvalue << ['select CASE_NUM from quality_sampling quality inner join quality_sampling_issues qualityItems  ON quality.id = qualityItems.quality_sampling_id   WHERE qualityItems.issue_id = 1 and quality.tenant_id = 1 ',
                        'select CASE_NUM from quality_sampling quality inner join quality_sampling_issues qualityItems  ON quality.id = qualityItems.quality_sampling_id   WHERE qualityItems.issue_id = 2 and quality.tenant_id = 1 ',
                        'select CASE_NUM from quality_sampling quality inner join quality_sampling_issues qualityItems  ON quality.id = qualityItems.quality_sampling_id   WHERE qualityItems.issue_id = 3 and quality.tenant_id = 1 '
        ]
    }

    void "test deleteCases"() {
        given:
        service.CRUDService = [softDelete: { theInstance, name, String justification = null -> theInstance.isDeleted = true }]

        List<QualityCaseData> qualityCaseDataList = QualityCaseData.findAll()
        List<QualitySampling> qualitySamplingList = QualitySampling.findAll()
        List<QualitySubmission> qualitySubmissionList = QualitySubmission.findAll()
        when:
        service.deleteCases(qualityCaseDataList*.id, PvqTypeEnum.CASE_QUALITY.name(), 1L, "justification")
        service.deleteCases(qualitySamplingList*.id, PvqTypeEnum.SAMPLING.name(), 1L, "justification")
        service.deleteCases(qualitySubmissionList*.id, PvqTypeEnum.SUBMISSION_QUALITY.name(), 1L, "justification")

        then:

        4 == qualityCaseDataList.findAll { it.isDeleted }?.size()
        4 == qualitySamplingList.findAll { it.isDeleted }?.size()
        4 == qualitySubmissionList.findAll { it.isDeleted }?.size()
    }

    void "test get Quality Data Entity List Search with assigned to filter"(){
        given:
        String type = typeValue.name()
        String qualityMonitoringType = null
        if (type == PvqTypeEnum.CASE_QUALITY.name()) {
            qualityMonitoringType = "Case Data Quality"
        } else if (type == PvqTypeEnum.SUBMISSION_QUALITY.name()) {
            qualityMonitoringType = "Submission Quality"
        } else {
            qualityMonitoringType = "Case Sampling"
        }
        Map externalSearch= [:]
        Map advanceFilter=null
        String assignedTofilter="Admin"
        def mockUtilService=Mock(UtilService)
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        service.utilService=mockUtilService
        Sql.metaClass.rows = {String sql->
            return dbrecordsValue
        }
        Sql.metaClass.firstRow={String sql->
            return [count:2,cont:3]
        }
        service.metaClass.getQualityIssues = {
            [[id: 1, textDesc: "Late1", ownerApp: "Pvq"]]
        }
        service.metaClass.getRootCauses = {
            [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        service.metaClass.getResponsibleParties = {
            [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        service.metaClass.getCommentsByQualityIds = { List idsArray, String dataType, Sql sql ->
            [1L: 'testComment']
        }
        service.metaClass.getActionItemByQualityIds = { List idsArray, String dataType, Sql sql ->
            [1L: [1, 'OPEN', new Date()]]
        }
        service.metaClass.getStateByQualityIds = { List idsArray, String dataType, Sql sql ->
            [1L: ['New', false]]
        }
        service.metaClass.getRCADataForQualityRecord = { Long id, String dataType, Map<String,Map<Long,String>> allRcaDataMap ->
            ['ROOT_CAUSE':'(empty)', 'LATE':'(empty)', 'RESPONSIBLE_PARTY':'(empty)']
        }
        User user =new User(username: 'testUsername1',createdBy:'user',modifiedBy:'user')
        WorkflowState.metaClass.static.findById = { Long id ->
            new WorkflowState(id: 2, name: 'NEW', createdBy: user, modifiedBy: user)
        }
        when:"call getQualityDataList"
        def model=service.getQualityDataEntityListSearch(type,externalSearch,advanceFilter, 1L, false)

        then:
        model[0]== ['assignedToUser': null, 'assignedToUserGroup': null, assignedToUserGroupId: '', assignedToUserId: '','testKey':'testValue', 'errorType':'Blank Outcome', 'priority':'Minor', 'qualityIssueType':'(empty)', rootCause:'(empty)', 'responsibleParty':'(empty)', 'state':'NEW', dueIn:'07-Feb-2023', dateCreated:'07-Feb-2023', 'latestComment':null, 'qualityIssueTypeId':null, 'qualityMonitoringType': qualityMonitoringType, executedTemplateId: null]

        where:
        typeValue                      | dbrecordsValue
        PvqTypeEnum.CASE_QUALITY       | [['CASE_NUM':'20US00028404', 'ERROR_TYPE':'Blank Outcome', 'PRIORITY':'Minor','METADATA':[characterStream: [text: '{ "testKey": "testValue" }']], 'REPORT_ID':14,
                                           'WORKFLOW_STATE_UPDATED_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DATE_CREATED':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DUE_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"),'ASSIGNED_TO_USER':null, 'ASSIGNED_TO_USERGROUP':null, ID:10, 'ENTRY_TYPE':'A', 'EXEC_REPORT_ID':43, 'WORKFLOW_STATE_ID':2, 'QUALITY_ISSUE_TYPE_ID':null]]
        PvqTypeEnum.SAMPLING           | [['CASE_NUM':'20US00028404', 'ERROR_TYPE':'Blank Outcome', 'PRIORITY':'Minor','METADATA':[characterStream: [text: '{ "testKey": "testValue" }']], 'REPORT_ID':14,
                                           'WORKFLOW_STATE_UPDATED_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DATE_CREATED':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DUE_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'ASSIGNED_TO_USER':null, 'ASSIGNED_TO_USERGROUP':null, ID:10, 'ENTRY_TYPE':'A', 'EXEC_REPORT_ID':43, 'WORKFLOW_STATE_ID':2, 'QUALITY_ISSUE_TYPE_ID':null]]
        PvqTypeEnum.SUBMISSION_QUALITY | [['CASE_NUM':'20US00028404', 'ERROR_TYPE':'Blank Outcome', 'PRIORITY':'Minor','METADATA':[characterStream: [text: '{ "testKey": "testValue" }']], 'REPORT_ID':14,
                                           'WORKFLOW_STATE_UPDATED_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DATE_CREATED':Date.parse("dd-MMM-yyyy","07-Feb-2023"), 'DUE_DATE':Date.parse("dd-MMM-yyyy","07-Feb-2023"),'ASSIGNED_TO_USER':null, 'ASSIGNED_TO_USERGROUP':null, ID:10, 'ENTRY_TYPE':'A', 'EXEC_REPORT_ID':43, 'WORKFLOW_STATE_ID':2, 'QUALITY_ISSUE_TYPE_ID':null]]

    }
}