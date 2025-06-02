package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.AjaxResponseDTO
import com.rxlogix.enums.PvqTypeEnum
import com.rxlogix.enums.ReasonOfDelayAppEnum
import com.rxlogix.enums.WorkflowConfigurationTypeEnum
import com.rxlogix.user.*
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.async.Promises
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import groovy.sql.Sql
import org.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import javax.sql.DataSource

@ConfineMetaClassChanges([AuditLogConfigUtil, Tenants, ViewHelper, QualitySampling, WorkflowRule, QualityCaseData, QualitySubmission, AjaxResponseDTO ])
class QualityControllerSpec extends Specification implements DataTest, ControllerUnitTest<QualityController> {
    public static final user = "unitTest"

    def setupSpec() {
        mockDomains User, UserGroup, Role, UserRole, UserGroupUser, Tenant, WorkflowRule, WorkflowJustification, WorkflowState, QualityCaseData, QualitySubmission, QualitySampling, Comment, ReportField, ResponsibleParty, PreventativeAction, CorrectiveAction, RootCause, Notification
        AuditLogConfigUtil.metaClass.static.logChanges = { domain, Map newMap, Map oldMap, String eventName, String extraValue -> }

        grailsApplication.config.qualityModule.qualityTagName = "PV Quality: Data Quality"
        grailsApplication.config.qualityModule.qualityLabel = "Case Data Quality"
        grailsApplication.config.qualityModule.submissionTagName = "PV Quality: Submission Quality"
        grailsApplication.config.qualityModule.submissionLabel = "Submission Quality"
        grailsApplication.config.qualityModule.pvqPriorityList = [[name: "Major", value: "PVQ Priority: Major"], [name: "Minor", value: "PVQ Priority: Minor"]]

        grailsApplication.config.qualityModule.dbFieldToQualityFieldMap = [masterCaseNum     : "masterCaseNum",
                                                                           masterVersionNum  : "masterVersionNum",
                                                                           cifTxtDateReceipt : "masterCaseReceiptDate",
                                                                           masterRptTypeId   : "masterRptTypeId",
                                                                           masterCountryId   : "masterCountryId",
                                                                           masterPrimProdName: "masterPrimProdName"]
        grailsApplication.config.qualityModule.qualityColumnList = ["masterCaseNum", "masterVersionNum", "masterCaseReceiptDate", "masterRptTypeId", "masterCountryId", "masterPrimProdName"]
        grailsApplication.config.qualityModule.submissionColumnList = ["masterCaseNum", "masterVersionNum", "masterCaseReceiptDate", "masterRptTypeId", "masterCountryId", "masterPrimProdName"]
        grailsApplication.config.qualityModule.extraColumnList = ["errorType"]
        grailsApplication.config.qualityModule.qualityControllers = ["quality", "issue"]
        grailsApplication.config.qualityModule.dbFieldToSamplingFieldMap = [masterCaseNum          : 'masterCaseNum',
                                                                            masterPrimaryHcpFlag   : 'masterPrimaryHcpFlag',
                                                                            masterCountryId        : 'masterCountryId',
                                                                            cifTxtDateReceipt      : 'cifTxtDateReceipt',
                                                                            masterRptTypeId        : 'masterRptTypeId',
                                                                            patInfoPatientAgeYears : 'patInfoPatientAgeYears',
                                                                            caseParentInfoPvrDob   : 'caseParentInfoPvrDob',
                                                                            patInfoGenderId        : 'patInfoGenderId',
                                                                            productProdNameDrugType: 'productProdNameDrugType',
                                                                            masterPrefTermList     : 'masterPrefTermList',
                                                                            narrativeNarrative     : 'narrativeNarrative',
                                                                            masterVersionNum       : 'masterVersionNum']
        grailsApplication.config.qualityModule.additional = [
                [name: "SAMPLING", label: "Case Sampling", workflow: 1, tag: "PV Quality: Sampling", columnList: ["masterCaseNum", "masterVersionNum", "masterPrimaryHcpFlag", "masterCountryId",
                                                                                                                  "cifTxtDateReceipt", "masterRptTypeId", "patInfoPatientAgeYears",
                                                                                                                  "caseParentInfoPvrDob", "patInfoGenderId", "productProdNameDrugType",
                                                                                                                  "masterPrefTermList"]]
        ]

        grailsApplication.config.argus_attachments = [
                list   : "select ROWID, FILETYPE,TO_CHAR(NOTES) from case_notes_attach where BLOBSIZE>0 and DELETED is NULL and CASE_ID = ?",
                content: "select ROWID, FILETYPE,TO_CHAR(NOTES), DATA from case_notes_attach where ROWID= ?"
        ]
        grailsApplication.config.pvcm_attachments = [
                list: "select REPORT_DATA_ID, FLAG_INCLUDED_DOCUMENTS, TO_CHAR(NOTES), VERSION_NUM from c_lit_reference_fu where flag_included_documents is not null and CASE_ID = ?"
        ]

        grailsApplication.config.qualityModule.qualityColumnUiStackMapping=[["masterCaseNum","masterVersionNum"],["masterCaseReceiptDate","masterCountryId"],[ "masterRptTypeId","masterPrimProdName"]]

        grailsApplication.config.qualityModule.submissionColumnUiStackMapping=[["masterCaseNum","masterVersionNum"],["masterCaseReceiptDate","masterCountryId"],[ "masterRptTypeId","masterPrimProdName"]]
        grailsApplication.config.tempDirectory=System.getProperty("java.io.tmpdir")

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

    def setup() {
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
        securityMock.demand.getCurrentUser(0..3) {  -> user }
        return securityMock.proxyInstance()
    }

    void "test index"(){
        when:
        controller.index()

        then:
        response.status==200
        response.forwardedUrl == "/dashboard/index"
    }

    void "test add Report Widget"(){
        when:
        controller.addReportWidget()

        then:
        response.status==200
        response.forwardedUrl == "/dashboard/addReportWidget"
    }

    void "test remove Report Widget Ajax"(){
        when:
        controller.removeReportWidgetAjax()

        then:
        response.status==200
        response.forwardedUrl == "/dashboard/removeReportWidgetAjax"
    }

    void "test new Dashboard"(){
        when:
        controller.newDashboard()

        then:
        response.status==200
        response.forwardedUrl == "/dashboard/newDashboard"
    }

    void "test remove Dashboard"(){
        when:
        controller.removeDashboard()

        then:
        response.status==200
        response.forwardedUrl == "/dashboard/removeDashboard"
    }

    void "test update Label"(){
        when:
        controller.updateLabel()

        then:
        response.status==200
        response.forwardedUrl == "/dashboard/updateLabel"
    }

    void "test update Report Widgets Ajax"(){
        when:
        controller.updateReportWidgetsAjax()

        then:
        response.status==200
        response.forwardedUrl == "/dashboard/updateReportWidgetsAjax"
    }

    void "test ajaxCaseCount"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getCaseCountByError(_,_,_,_)>>{return val}
        controller.qualityService=qualityServiceMock
        def normalUser = makeNormalUser()
        controller.userService = makeSecurityService(normalUser)
        Tenants.metaClass.static.currentId = { return tenant.id }

        when:
        params.dataType=dataType
        controller.ajaxCaseCount()

        then:
        response.status== 200
        response.json==val

        where:
        dataType<<["CASE", "SUBMISSION"]
        val<<[[errorNameList: [], errorTotalCountList: []], [errorNameList: [], errorTotalCountList: []]]
    }

    void "test ajaxCaseDataCount"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getCaseDataCountByError(1L)>>{return val}
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = { return tenant.id }
        when:
        controller.ajaxCaseDataCount()
        then:
        response.status==200
        response.json==val
        where:
        val<<[[errorNames: ["testErrorType1", "testErrorType2"], caseCountValues: [10, 20]]]
    }

    void "test ajaxSubmissionCount"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getSubmissionCountByError(1L)>>{return val}
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = { return tenant.id }
        when:
        controller.ajaxSubmissionCount()
        then:
        response.status==200
        response.json==val
        where:
        val<<[[errorNames: ["testErrorType1", "testErrorType2"], caseCountValues: [10, 20]]]
    }

    void "test ajaxProductsCount"(){
        given:
        List fieldsNames=["testField1", "testField2"]
        List list = [['count':1,'errorType':'testErrorType1'],['count':2,'errorType':'testErrorType2']]
        Map dataMap =["fieldNamesList":fieldsNames, "errorsList": list]
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getErrorsCountByFieldName(_,_,_,_,_) >> { return dataMap}
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = { return tenant.id }

        when:
        params.dataType=dataType
        params.from=from
        controller.ajaxProductsCount()

        then:
        response.status==200
        response.json.fieldNameList==fieldsNames

        where:
        dataType<<["CASE", "SUBMISSION"]
        from<<["2020-12-22", null]
    }

    void "test ajaxCaseReportTypeCount"(){
        given:
        List fieldsNames=["testField1", "testField2"]
        List list = [['count':1,'errorType':'testErrorType1'],['count':2,'errorType':'testErrorType2']]
        Map dataMap =["fieldNamesList":fieldsNames, "errorsList": list]
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getErrorsCountByFieldName(_,_,_,_,_) >> { return dataMap}
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = { return tenant.id }

        when:
        params.dataType=dataType
        params.from=from
        controller.ajaxCaseReportTypeCount()

        then:
        response.status==200
        response.json.fieldNameList==fieldsNames

        where:
        dataType<<["CASE", "SUBMISSION"]
        from<<["2020-12-22", null]
    }

    void "test ajaxEntrySiteCount"(){
        given:
        List fieldsNames=["testField1", "testField2"]
        List list = [['count':1,'errorType':'testErrorType1'],['count':2,'errorType':'testErrorType2']]
        Map dataMap =["fieldNamesList":fieldsNames, "errorsList": list]
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getErrorsCountByFieldName(_,_,_,_,_) >> { return dataMap}
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = { return tenant.id }

        when:
        params.dataType=dataType
        params.from=from
        controller.ajaxEntrySiteCount()

        then:
        response.status==200
        response.json.fieldNameList==fieldsNames

        where:
        dataType<<["CASE", "SUBMISSION"]
        from<<["2020-12-22", null]
    }

    void "test ajaxTop20ErrorsCount"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getCaseCountByError(_,_,_,_)>>{return val}
        controller.qualityService=qualityServiceMock
        def normalUser = makeNormalUser()
        controller.userService = makeSecurityService(normalUser)
        Tenants.metaClass.static.currentId = { return tenant.id }

        when:
        params.dataType=dataType
        params.from="2020-12-22"
        controller.ajaxTop20ErrorsCount()

        then:
        response.status== 200
        response.json==val

        where:
        dataType<<["CASE", "SUBMISSION"]
        val<<[[errorNameList: [], errorTotalCountList: []], [errorNameList: [], errorTotalCountList: []]]
    }

    void "Test quality case data columns when user is not allowed"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.isPermitted(PvqTypeEnum.CASE_QUALITY.name()) >> {
            return false
        }
        controller.qualityService=qualityServiceMock
        when:
        controller.caseDataQuality()
        then:
        response.status==302
        response.redirectedUrl=='/errors/forbidden'
    }

    void "Test quality case data columns"() {
        given:
            def qualityServiceMock=Mock(QualityService)
            qualityServiceMock.getQualityReportAllFields(_) >> {
                return value
            }
            qualityServiceMock.isPermitted(_) >> {
               return true
            }
        qualityServiceMock.getAdditional(_) >> {
            return [name: "SAMPLING", label: "Case Sampling", workflow: 1, tag: "PV Quality: Sampling"]
        }
        qualityServiceMock.getColumnList(_) >> {
            return grailsApplication.config.qualityModule.qualityColumnList
        }
        def reportExecutorServiceMock=Mock(ReportExecutorService)
        reportExecutorServiceMock.getCorrectiveActionList(_) >>  {
           return  [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        reportExecutorServiceMock.getPreventativeActionList(_) >> {
           return  [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        controller.qualityService=qualityServiceMock
        controller.reportExecutorService=reportExecutorServiceMock
        when: "call caseDataQuality api"
            def model = controller.caseDataQuality()
        then:
            response.status == 200
            model.reportOtherColumnList[0].fieldName==fieldNameValue
            model.reportOtherColumnList[0].fieldType=='testFieldType'
            model.reportOtherColumnList[0].fieldLabel==fieldLabelValue
            model.moduleColumnList[0].fieldName=='masterCaseNum'
            model.moduleColumnList[0].fieldType==fieldTypeValue
            model.moduleColumnList[0].fieldLabel!=null


        where:
            fieldNameValue   | fieldLabelValue   | fieldTypeValue     | value
            'testFieldName'  | 'testFieldLabel'  | 'VARCHAR2'         | [[fieldName: 'testFieldName', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]
            'testFieldName'  | 'testFieldLabel'  | 'testFieldTypeMCN' | [[fieldName: 'testFieldName', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel'],
                                                                         [fieldName: 'masterCaseNum', fieldType: 'testFieldTypeMCN', fieldLabel: 'testFieldLabel']]
            'testFieldName1' | 'testFieldLabel1' | 'VARCHAR2'         | [[fieldName: 'testFieldName2', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel2'],
                                                                         [fieldName: 'testFieldName1', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel1']]
            'testFieldName1' | 'testFieldLabel1' | 'VARCHAR2'         | [[fieldName: 'testFieldName2', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel2'],
                                                                         [fieldName: 'testFieldName1', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel1'],
                                                                         [fieldName: 'masterCountryId', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]


    }

    void "Test quality case data columns, When reportsField is Empty. "() {
        given:
            def qualityServiceMock=Mock(QualityService)
            qualityServiceMock.getQualityReportAllFields(_) >> {
                return value
            }
        qualityServiceMock.isPermitted(_) >> {
            return true
        }
        qualityServiceMock.getColumnList(_) >> {
            return grailsApplication.config.qualityModule.qualityColumnList
        }
            controller.qualityService=qualityServiceMock
        def reportExecutorServiceMock=Mock(ReportExecutorService)
        reportExecutorServiceMock.getCorrectiveActionList(_) >>  {
            return  [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        reportExecutorServiceMock.getPreventativeActionList(_) >> {
            return  [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        controller.reportExecutorService=reportExecutorServiceMock
            Tenants.metaClass.static.currentId = { return tenant.id }
        when: "call caseDataQuality api"
            def model = controller.caseDataQuality()
        then:
            response.status == 200
            model.reportOtherColumnList[0]==result1
            model.moduleColumnList[0].fieldName=='masterCaseNum'
            model.moduleColumnList[0].fieldType==fieldTypeValue
            model.moduleColumnList[0].fieldLabel!=null

        where:
            result1 |fieldTypeValue| value
            null | 'VARCHAR2'      | []
            null | 'testFieldType' | [[fieldName: 'masterCaseNum', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]
            null | 'VARCHAR2'      | [[fieldName: 'masterCountryId', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]

    }

    void "Test quality submission columns when user is not allowed"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.isPermitted(PvqTypeEnum.SUBMISSION_QUALITY.name()) >> {
            return false
        }
        controller.qualityService=qualityServiceMock
        when:
        controller.submissionQuality()
        then:
        response.status==302
        response.redirectedUrl=='/errors/forbidden'
    }

    void "test quality submission "(){
        given:
            def qualityServiceMock=Mock(QualityService)
            qualityServiceMock.getQualityReportAllFields(_) >> {
                return value
            }
        qualityServiceMock.isPermitted(_) >> {
            return true
        }
            controller.qualityService=qualityServiceMock
        def reportExecutorServiceMock=Mock(ReportExecutorService)
        reportExecutorServiceMock.getCorrectiveActionList(_) >>  {
            return  [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        reportExecutorServiceMock.getPreventativeActionList(_) >> {
            return  [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        controller.reportExecutorService=reportExecutorServiceMock
            Tenants.metaClass.static.currentId = { return tenant.id }
        when: "call submissionQuality api"
            def model = controller.submissionQuality()
        then:
            response.status == 200
            model.reportOtherColumnList[0].fieldName==fieldNameValue
            model.reportOtherColumnList[0].fieldType=='testFieldType'
            model.reportOtherColumnList[0].fieldLabel==fieldLabelValue
            model.moduleColumnList[0].fieldName=='masterCaseNum'
            model.moduleColumnList[0].fieldType==fieldTypeValue
            model.moduleColumnList[0].fieldLabel!=null

        where:
            fieldNameValue   | fieldLabelValue   | fieldTypeValue     | value
            'testFieldName'  | 'testFieldLabel'  | 'VARCHAR2'         | [[fieldName: 'testFieldName', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]
            'testFieldName'  | 'testFieldLabel'  | 'testFieldTypeMCN' | [[fieldName: 'testFieldName', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel'],
                                                                         [fieldName: 'masterCaseNum', fieldType: 'testFieldTypeMCN', fieldLabel: 'testFieldLabel']]
            'testFieldName1' | 'testFieldLabel1' | 'VARCHAR2'         | [[fieldName: 'testFieldName2', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel2'],
                                                                         [fieldName: 'testFieldName1', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel1']]
            'testFieldName1' | 'testFieldLabel1' | 'VARCHAR2'         | [[fieldName: 'testFieldName2', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel2'],
                                                                         [fieldName: 'testFieldName1', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel1'],
                                                                         [fieldName: 'masterCountryId', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]


    }

    void "test quality submission, When reportsField is Empty. "(){
        given:
            def qualityServiceMock=Mock(QualityService)
            qualityServiceMock.getQualityReportAllFields(_) >> {
                return value
            }
        qualityServiceMock.isPermitted(_) >> {
            return true
        }
            controller.qualityService=qualityServiceMock
        def reportExecutorServiceMock=Mock(ReportExecutorService)
        reportExecutorServiceMock.getCorrectiveActionList(_) >>  {
            return  [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        reportExecutorServiceMock.getPreventativeActionList(_) >> {
            return  [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        controller.reportExecutorService=reportExecutorServiceMock
            Tenants.metaClass.static.currentId = { return tenant.id }
        when: "call submissionQuality api"
            def model = controller.submissionQuality()
        then:
            response.status == 200
            model.reportOtherColumnList[0]==result1
            model.moduleColumnList[0].fieldName=='masterCaseNum'
            model.moduleColumnList[0].fieldType==fieldTypeValue
            model.moduleColumnList[0].fieldLabel!=null

        where:
            result1 |fieldTypeValue| value
            null | 'VARCHAR2'      | []
            null | 'testFieldType' | [[fieldName: 'masterCaseNum', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]
            null | 'VARCHAR2'      | [[fieldName: 'masterCountryId', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]

    }

    void "Test case sampling columns when user is not allowed"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.isPermitted(PvqTypeEnum.SAMPLING.name()) >> {
            return false
        }
        controller.qualityService=qualityServiceMock
        when:
        controller.caseSampling()
        then:
        response.status==302
        response.redirectedUrl=='/errors/forbidden'
    }

    void "test case sampling "(){
        given:
            def qualityServiceMock=Mock(QualityService)
            qualityServiceMock.getQualityReportAllFields(_) >> {
                return value
            }
        qualityServiceMock.isPermitted(_) >> {
            return true
        }
        qualityServiceMock.getColumnList(_) >> {
            return ["masterCaseNum","masterVersionNum", "masterPrimaryHcpFlag","masterCountryId","masterSiteId",
                    "cifTxtDateReceipt","masterRptTypeId","patInfoPatientAgeYears",
                    "caseParentInfoPvrDob","patInfoGenderId","productProdNameDrugType",
                    "masterPrefTermList"]
        }
        qualityServiceMock.getAdditional(_) >> {
            return [name: "SAMPLING", label: "Case Sampling", workflow: 1, tag: "PV Quality: Sampling"]
        }
        Tenants.metaClass.static.currentId = { return tenant.id }
        controller.qualityService=qualityServiceMock
        def reportExecutorServiceMock=Mock(ReportExecutorService)
        reportExecutorServiceMock.getCorrectiveActionList(_) >>  {
            return  [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        reportExecutorServiceMock.getPreventativeActionList(_) >> {
            return  [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        controller.reportExecutorService=reportExecutorServiceMock
        when: "call caseSampling api"
            def model = controller.caseSampling()
        then:
            response.status == 200
            model.reportOtherColumnList[0].fieldName==fieldNameValue
            model.reportOtherColumnList[0].fieldType=='testFieldType'
            model.reportOtherColumnList[0].fieldLabel==fieldLabelValue
            model.moduleColumnList[0].fieldName=='masterCaseNum'
            model.moduleColumnList[0].fieldType==fieldTypeValue
            model.moduleColumnList[0].fieldLabel!=null

        where:
            fieldNameValue   | fieldLabelValue   | fieldTypeValue     | value
            'testFieldName'  | 'testFieldLabel'  | 'VARCHAR2'         | [[fieldName: 'testFieldName', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]
            'testFieldName'  | 'testFieldLabel'  | 'testFieldTypeMCN' | [[fieldName: 'testFieldName', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel'],
                                                                         [fieldName: 'masterCaseNum', fieldType: 'testFieldTypeMCN', fieldLabel: 'testFieldLabel']]
            'testFieldName1' | 'testFieldLabel1' | 'VARCHAR2'         | [[fieldName: 'testFieldName2', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel2'],
                                                                         [fieldName: 'testFieldName1', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel1']]
            'testFieldName1' | 'testFieldLabel1' | 'VARCHAR2'         | [[fieldName: 'testFieldName2', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel2'],
                                                                         [fieldName: 'testFieldName1', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel1'],
                                                                         [fieldName: 'masterCountryId', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]


    }

    void "test case sampling, When reportFields is empty "(){
        given:
            def qualityServiceMock=Mock(QualityService)
            qualityServiceMock.getQualityReportAllFields(_) >> {
                return value
            }
        qualityServiceMock.isPermitted(_) >> {
            return true
        }
        qualityServiceMock.getColumnList(_) >> {
            return ["masterCaseNum","masterVersionNum", "masterPrimaryHcpFlag","masterCountryId","masterSiteId",
                    "cifTxtDateReceipt","masterRptTypeId","patInfoPatientAgeYears",
                    "caseParentInfoPvrDob","patInfoGenderId","productProdNameDrugType",
                    "masterPrefTermList"]
        }
        qualityServiceMock.getAdditional(_) >> {
            return [name: "SAMPLING", label: "Case Sampling", workflow: 1, tag: "PV Quality: Sampling"]
        }
        Tenants.metaClass.static.currentId = { return tenant.id }
        controller.qualityService=qualityServiceMock
        def reportExecutorServiceMock=Mock(ReportExecutorService)
        reportExecutorServiceMock.getCorrectiveActionList(_) >>  {
            return  [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        reportExecutorServiceMock.getPreventativeActionList(_) >> {
            return  [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        controller.reportExecutorService=reportExecutorServiceMock
        when: "call caseSampling api"
            def model = controller.caseSampling()
        then:
            response.status == 200
            model.reportOtherColumnList[0]==result1
            model.moduleColumnList[0].fieldName=='masterCaseNum'
            model.moduleColumnList[0].fieldType==fieldTypeValue
            model.moduleColumnList[0].fieldLabel!=null

        where:
            result1 |fieldTypeValue| value
            null | 'VARCHAR2'      | []
            null | 'testFieldType' | [[fieldName: 'masterCaseNum', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]
            null | 'VARCHAR2'      | [[fieldName: 'masterCountryId', fieldType: 'testFieldType', fieldLabel: 'testFieldLabel']]

    }

    void "Test quality case data ajax api for datatable"() {
        given:
            def normalUser = makeNormalUser()
            controller.userService = makeSecurityService(normalUser)
            def data = [[caseNumber : "testcasenum1",caseReceiptNumber : "01-Apr-2020"],[caseNumber : 'testcasenum2',caseReceiptNumber : "01-Apr-2020"]]
            def valout = [aaData : data,filteredCount : 2, totalCount : 2]
            def qualityServiceMock=Mock(QualityService)
            qualityServiceMock.getQualityDataList(_,_,_,_,_,_,_,_,_,_,_,_) >> valout
            controller.qualityService=qualityServiceMock
            Tenants.metaClass.static.currentId = { return tenant.id }
        when: "call qualityDataAjax api"
            params.sort = 'masterCaseReceiptDate'
            params.max = 20
            params.offset = 0
            params.refreshChart="true"
            request.method="GET"
            controller.qualityDataAjax()
        then:
            response.status == 200
            response.json.aaData==data
    }


    void "Test quality Submission ajax api"() {
        given:
            def normalUser = makeNormalUser()
            controller.userService = makeSecurityService(normalUser)
            def data = [[caseNumber : "testcasenum1",caseReceiptNumber : "01-Apr-2020"],[caseNumber : 'testcasenum2',caseReceiptNumber : "01-Apr-2020"]]
            def valout = [aaData : data,filteredCount : 2, totalCount : 2]
            def qualityServiceMock=Mock(QualityService)
            qualityServiceMock.getQualityDataList(_,_,_,_,_,_,_,_,_,_,_,_) >> valout
            controller.qualityService=qualityServiceMock
            Tenants.metaClass.static.currentId = { return tenant.id }
        when: "call qualityDataAjax api"
            params.sort = 'masterCaseReceiptDate'
            params.max = 20
            params.offset = 0
            params.refreshChart="true"
            request.method="GET"
            controller.qualitySubmissionAjax()
        then:
            response.status == 200
            response.json.aaData==data
    }

    void "Test quality Sampling ajax api"() {
        given:
            def data = [[caseNumber : "testcasenum1",caseReceiptNumber : "01-Apr-2020"],[caseNumber : 'testcasenum2',caseReceiptNumber : "01-Apr-2020"]]
            def valout = [aaData : data,filteredCount : 2, totalCount : 2]
            def qualityServiceMock=Mock(QualityService)
            qualityServiceMock.getQualityDataList(_,_,_,_,_,_,_,_,_,_,_,_) >> valout
            controller.qualityService=qualityServiceMock
            Tenants.metaClass.static.currentId = {  return tenant.id  }
        when: "call qualityDataAjax api"
            params.sort = 'masterCaseReceiptDate'
            params.max = 20
            params.offset = 0
            params.refreshChart="true"
            request.method="GET"
            controller.qualitySamplingAjax()
        then:
        response.status == 200
        response.json.aaData==data
    }

    void "test get Errors Count By FieldName when dataType is not null"(){
        given:
        String type=typeValue
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getErrorsCountByFieldName(_,_,_,_,_) >> { return [:]}
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = {  return tenant.id  }
        when:
        controller.invokeMethod('getErrorsCountByFieldName', ['testFieldName', type, [:], 1L, 1] as Object[])
        then:
        response.status==200
        where:
        typeValue<<["CASE", "SUBMISSION"]
    }

    void "test get Errors Count By FieldName when dataType is null"(){
        given:
        List fieldsNames=["testField1", "testField2"]
        List list = [['count':1,'errorType':'testErrorType1'],['count':2,'errorType':'testErrorType2']]
        Map dataMap =["fieldNamesList":fieldsNames, "errorsList": list]
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getErrorsCountByFieldName(_,_,_,_,_) >> { return dataMap}
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = {  return tenant.id  }
        when:
        controller.invokeMethod('getErrorsCountByFieldName', ['testFieldName', null, [:], 1L, 1] as Object[])
        then:
        response.status==200
    }

    void "test ajax Latest Quality IssuesUrl"(){
        given:
        params.length=20
        params.start=0
        Map dataMap = testdataMap
        String type = typeValue
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getLatestQualityIssues(_, 1L, _, _) >> { return dataMap }
        controller.qualityService = qualityServiceMock
        Tenants.metaClass.static.currentId = {  return tenant.id  }
        when:
        params.dataType=dataType
        controller.ajaxLatestQualityIssuesUrl(dataType)

        then:
        response.status==200
        response.json==testdataMap

        where:
        dataType     | typeValue                             | testdataMap
        "CASE"       | PvqTypeEnum.CASE_QUALITY.name()       | [aaData: [['masterCaseNum':'testCaseNum2', 'masterVersionNum':3, 'masterCaseReceiptDate':'24-OCT-2001', 'masterCountryId':'UNITED KINGDOM', 'masterPrimProdName':'WonderUSA', 'errorType':'Blank Causality - Reporter', 'priority':-1, 'executedReportId':1, 'dataType':'CASE_QUALITY']], recordsTotal : 1, recordsFiltered : 1]
        "SUBMISSION" | PvqTypeEnum.SUBMISSION_QUALITY.name() | [aaData: [['masterCaseNum':'testCaseNum1', 'masterVersionNum':1, 'masterCaseReceiptDate':'06-APR-1991', 'masterCountryId':'UNITED STATES', 'masterPrimProdName':'Placebo License', 'errorType':'Negative Ack', 'priority':-1, 'executedReportId':1, 'dataType':'SUBMISSION_QUALITY']], recordsTotal : 1, recordsFiltered : 1]
    }

    void "test ajax Latest Quality IssuesUrl when dataType is null"(){
        params.length=20
        params.start=0
        Map dataMap1 = testdataMap1
        Map dataMap2 = testdataMap2
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getLatestQualityIssues(PvqTypeEnum.CASE_QUALITY.name(),1L,_,_) >> { return dataMap1}
        qualityServiceMock.getLatestQualityIssues(PvqTypeEnum.SUBMISSION_QUALITY.name(),1L,_,_) >> { return dataMap2}
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = {  return tenant.id  }

        when:
        params.dataType="ALL"
        controller.ajaxLatestQualityIssuesUrl()

        then:
        response.status==200
        response.json.aaData==[['masterCaseNum':'testCaseNum2', 'masterVersionNum':3, 'masterCaseReceiptDate':'24-OCT-2001', 'masterCountryId':'UNITED KINGDOM', 'masterPrimProdName':'WonderUSA', 'errorType':'Blank Causality - Reporter', 'priority':-1, 'executedReportId':1, 'dataType':'CASE_QUALITY'],
                                 ['masterCaseNum':'testCaseNum1', 'masterVersionNum':1, 'masterCaseReceiptDate':'06-APR-1991', 'masterCountryId':'UNITED STATES', 'masterPrimProdName':'Placebo License', 'errorType':'Negative Ack', 'priority':-1, 'executedReportId':1, 'dataType':'SUBMISSION_QUALITY']]
        response.json.recordsTotal==2
        response.json.recordsFiltered==2
        where:
        testdataMap1<<[[aaData: [['masterCaseNum':'testCaseNum2', 'masterVersionNum':3, 'masterCaseReceiptDate':'24-OCT-2001', 'masterCountryId':'UNITED KINGDOM', 'masterPrimProdName':'WonderUSA', 'errorType':'Blank Causality - Reporter', 'priority':-1, 'executedReportId':1, 'dataType':'CASE_QUALITY']], recordsTotal : 1, recordsFiltered : 1]]
        testdataMap2<<[[aaData: [['masterCaseNum':'testCaseNum1', 'masterVersionNum':1, 'masterCaseReceiptDate':'06-APR-1991', 'masterCountryId':'UNITED STATES', 'masterPrimProdName':'Placebo License', 'errorType':'Negative Ack', 'priority':-1, 'executedReportId':1, 'dataType':'SUBMISSION_QUALITY']], recordsTotal : 1, recordsFiltered : 1]]
    }

    void "test update Assigned Owner"() {
        given:
        def qualityServiceMock = Mock(QualityService)
        qualityServiceMock.updateAssignedOwner(_, _) >> {}
        controller.qualityService = qualityServiceMock
        Tenants.metaClass.static.currentId = { return tenant.id }
        when: "call updateAssignedOwner"
        controller.updateAssignedOwner()
        then:
        response.status == 200
        response.text == "Ok"
    }

    void "test  update Priority"() {
        given:
        def qualityServiceMock = Mock(QualityService)
        qualityServiceMock.updatePriority(_, _) >> {}
        controller.qualityService = qualityServiceMock
        Tenants.metaClass.static.currentId = { return tenant.id }
        when: "call updatePriority"
        controller.updatePriority()

        then:
        response.status == 200
        response.text == "Ok"
    }

    void "test  update QualityIssueType"() {
        given:
        def qualityServiceMock = Mock(QualityService)
        qualityServiceMock.updateQualityIssueType(_, _) >> {}
        controller.qualityService = qualityServiceMock
        Tenants.metaClass.static.currentId = { return tenant.id }
        when: "call updateQualityIssueType"
        controller.updateQualityIssueType()
        then:
        response.status == 200
        response.text == "Ok"
    }

    void "test update RootCause"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.updateRootCauses(_,_) >> val
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = {  return tenant.id  }
        when:"call updateRootCause"
        controller.updateRootCause()
        then:
        response.status==200
        response.text==val
        where:
        val<<['Ok','Error occurred in updating root cause']
    }

    void "test update ResponsibleParty"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.updateResponsibleParty(_,_) >> val
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = {  return tenant.id  }
        when:"call updateResponsibleParty"
        controller.updateResponsibleParty()
        then:
        response.status==200
        response.text==val
        where:
        val<<['Ok','Error occurred in updating responsible party']
    }

    void "test  getQualityPriorityList "(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getQualityPriorityList() >> val
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = { return tenant.id }
        when:"call getQualityPriorityList"
        controller.getQualityPriorityList()
        then:
        response.status==200
        response.json[0]==result
        where:
        result  |val
        'name1' |['name1','name2','name3']
    }

    void "test checkCaseNum "(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.checkCaseNumber(_) >> { return value}
        controller.qualityService=qualityServiceMock
        when:"call checkCaseNum"
        controller.checkCaseNum()
        then:
        response.status==200
        response.json==value

        where:
        value<<[['caseNumber': '', 'masterVersionNum': '', 'country': '', 'reportType': '', 'caseReceiptDate': '', 'masterSiteId': '', 'masterPrimProdName': '']]
    }

    void "test fetch criteria for Manual Errors"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.fetchPriorityAndComments(_,_) >>{return val}
        controller.qualityService=qualityServiceMock
        Tenants.metaClass.static.currentId = {  return tenant.id  }
        when: "call fetchCriteriaForManualError"
        controller.fetchCriteriaForManualError()
        then:
        response.status==200
        response.json==val
        where:
        val<<[["priority": "" , "comment": ""]]
    }

    void "test fetch Users "(){
        given:
            def userServiceMock=Mock(UserService)
            userServiceMock.getActiveUsers()>>{return [[id:1,fullName:'testName'],[id:2,fullName:'testName2']]}
            controller.userService=userServiceMock
            Tenants.metaClass.static.currentId = { return tenant.id }
        when:"call fetchUsers"
           controller.fetchUsers()
        then:
            response.status==200
            response.json[0].fullName=='testName'
    }

    void "test fetch Users and Groups "(){
        given:
        def userServiceMock=Mock(UserService)
        userServiceMock.getActiveUsers()>>{return [[id:1,fullName:'testName'],[id:2,fullName:'testName2']]}
        userServiceMock.getActiveGroups()>>{return [[id:1,name:'testGroupName'],[id:2,name:'testName2']]}
        controller.userService=userServiceMock
        Tenants.metaClass.static.currentId = { return tenant.id }
        when:"call fetchUsers"
        controller.fetchUsersAndGroups()
        then:
        response.status==200
        response.json[0].fullName=='testGroupName'
    }

    void "test getQualityIssues"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getQualityIssues() >> { return [[id: 1, textDesc: "Late1", ownerApp: "Pvq"]] }
        controller.qualityService=qualityServiceMock
        when:
        controller.getQualityIssues()
        then:
        response.status==200
    }

    void "test getRootCauses"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getRootCauses() >> { return [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]] }
        controller.qualityService=qualityServiceMock
        when:
        controller.getRootCauses()
        then:
        response.status==200
    }

    void "test getResponsibleParties"(){
        given:
        def qualityServiceMock=Mock(QualityService)
        qualityServiceMock.getResponsibleParties() >> { return [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]] }
        controller.qualityService=qualityServiceMock
        when:
        controller.getResponsibleParties()
        then:
        response.status==200
    }

    void "test Save AdHoc Alert"(){
        given:
            def mockQualityService=Mock(QualityService)
            mockQualityService.saveAdhocQualityRecord(_, _)>>{ return [status:'success']}
            controller.qualityService=mockQualityService
            Tenants.metaClass.static.currentId = {  return tenant.id  }
        when:"call saveAdHocAlert"
            params.dataType=type
            controller.saveAdHocAlert()

        then:
            println(type)
            response.status==302
            response.redirectedUrl==result

        where:
        type                                    |result
        PvqTypeEnum.CASE_QUALITY.name()         |'/quality/caseDataQuality'
        PvqTypeEnum.SUBMISSION_QUALITY.name()   |'/quality/submissionQuality'
    }

    void "test add to Manual --Success"() {
        given:
        def mockAjaxResponseDTO = Mock(AjaxResponseDTO)
        controller.qualityService = mockAjaxResponseDTO
        def mockQualityService=Mock(QualityService)
        mockQualityService.saveAdhocQualityRecord(_, _)>>{ return [status:'success']}
        controller.qualityService=mockQualityService
        Tenants.metaClass.static.currentId = { return tenant.id }

        when: "call addToManual"
        controller.addToManual()

        then:
        response.status==200
    }

    void "test add to Manual --Failure"() {
        given:
        def mockQualityService = Mock(QualityService)
        controller.qualityService = mockQualityService
        Boolean failure=false
        mockQualityService.saveAdhocQualityRecord(_,_)>>{ throw new Exception()}
        mockQualityService.saveAdhocQualityRecord(_,_,_)>>{ throw new Exception()}
        AjaxResponseDTO.metaClass.setFailureResponse={Exception ex, String message, int _httpCode ->failure=true}
        AjaxResponseDTO.metaClass.setFailureResponse={Exception ex, String message ->failure=true}

        when: "call addToManual"
        controller.addToManual()

        then:
        failure==true
    }

    void "test action items"(){
        when:"call the Action Items"
        controller.actionItems()

        then:"It renders index view"
        view == '/quality/actionItems'
        model.qualityModule != null
    }

    void "test fetch Quality Module ErrorTypes"(){
        given:
            params.dataType=dataTypeValue
            def mockQualityService=Mock(QualityService)
            mockQualityService.fetchQualityModuleErrorTypes(_,_) >>{
                return ['errorType1','errorType2']
            }
            controller.qualityService=mockQualityService
            Tenants.metaClass.static.currentId = { return tenant.id }
        when:
            controller.fetchQualityModuleErrorTypes()

        then:
            response.status==200
            response.json[0]=='errorType1'
            response.json[1]=='errorType2'
        where:
            dataTypeValue << [null, PvqTypeEnum.CASE_QUALITY.name(),
                              PvqTypeEnum.SUBMISSION_QUALITY.name(),
                              PvqTypeEnum.SAMPLING.name()]

    }

    void "test fetch Quality Module Errors List"(){
        given:
        params.dataType=dataTypeValue
        def mockQualityService=Mock(QualityService)
        mockQualityService.fetchQualityModuleErrorsList(_,_) >>{
            return ['errorType1','errorType2']
        }
        controller.qualityService=mockQualityService
        Tenants.metaClass.static.currentId = { return tenant.id }
        when:
        controller.fetchQualityModuleErrorsList()

        then:
        response.status==200
        response.json[0]=='errorType1'
        response.json[1]=='errorType2'
        where:
        dataTypeValue << [null, PvqTypeEnum.CASE_QUALITY.name(),
                          PvqTypeEnum.SUBMISSION_QUALITY.name(),
                          PvqTypeEnum.SAMPLING.name()]

    }

    @Unroll
    void "test  update ErrorType "() {
        given:
        def mockQualityService = Mock(QualityService)
        mockQualityService.updateErrorType(_, _) >> {}
        controller.qualityService = mockQualityService
        Tenants.metaClass.static.currentId = { return tenant.id }
        when:
        controller.updateErrorType()
        then:
        response.status == 200
        response.text == "Ok"
    }

    void "test downloadSourceDocuments"(){
        given:
        def mockQualityService=Mock(QualityService)
        mockQualityService.fetchAttachmentContent(_, _, _) >>{
            return [id: "1", fileName: "sourceDoc.xlsx", data: new byte[0]]
        }
        controller.qualityService=mockQualityService
        Tenants.metaClass.static.currentId = {  return tenant.id  }

        when:
        controller.downloadSourceDocuments("1", "19CY00010841", "1")

        then:
        response.status==200
    }

    void "test viewSourceDocument"(){
        given:
        def mockQualityService=Mock(QualityService)
        mockQualityService.fetchAttachmentContent(_,_,_) >>{
            return [id: "1", fileName: "sourceDoc.xlsx", data: new byte[0]]
        }
        controller.qualityService=mockQualityService
        Tenants.metaClass.static.currentId = {  return tenant.id  }

        when:
        params.id=1
        controller.viewSourceDocument()

        then:
        response.status==200
    }

    void "test fetchSourceDocuments"(){
        given:
        def mockQualityService=Mock(QualityService)
        mockQualityService.fetchAttachments(_) >>{
            return val
        }
        controller.qualityService=mockQualityService
        Tenants.metaClass.static.currentId = {  return tenant.id  }

        when:
        controller.fetchSourceDocuments("ABCXYZ123")

        then:
        response.status==200
        response.json==val

        where:
        val<<[[[id: "1", fileName: "argusAttachment1.xlsx", notes: ""],
               [id: "2", fileName: "argusAttachment2.xlsx", notes: ""]]]
    }

    @Unroll
    void "test export To Excel Quality Submission "(){
        given:
            params.data=dataValue
            def tenantId = 1L
            def mockQualityService=Mock(QualityService)
        Tenants.metaClass.static.currentId = { return tenant.id }
            mockQualityService.getQualityDataByIds(_,_,_)>>{
                [[testKey:'testValue1', errorType:'testErrorType', priority:'testPriority', assignedTo:''],
                 [testKey:'testValue2', errorType:'testErrorType', priority:'testPriority', assignedTo:'']]

            }
        controller.metaClass.getQualityDataByFilter = { Map paramData, String type, Boolean idsOnly ->
            return [[testKey: 'testValue1', errorType: 'testErrorType', priority: 'testPriority', assignedTo: ''],
                    [testKey: 'testValue2', errorType: 'testErrorType', priority: 'testPriority', assignedTo: '']]
        }
        mockQualityService.exportToExcel(_, _, _) >> {
            return []
        }
            mockQualityService.getColumnList(_) >> {
                return ['masterCaseNum']
            }
            mockQualityService.getRenameValueOfReportField(_,_) >> { ['testValue1','testValue2'] }
            controller.qualityService=mockQualityService
        when:
            controller.exportToExcelQualitySubmission()

        then:
            response.status==200

        where:
        dataValue<<['{"selectAll":"false","selectedIds":["1","2"]}','{"selectAll":"false","selectedIds":[]}']
    }

    @Unroll
    void "test export To Excel Quality Sampling "(){
        given:
        params.data=dataValue
        def tenantId = 1L
        Tenants.metaClass.static.currentId = { return tenant.id }
        def mockQualityService=Mock(QualityService)
        mockQualityService.getQualityDataByIds(_,_,_)>>{
            [[testKey:'testValue1', errorType:'testErrorType', priority:'testPriority', assignedTo:''],
             [testKey:'testValue2', errorType:'testErrorType', priority:'testPriority', assignedTo:'']]

        }
        controller.metaClass.getQualityDataByFilter = { Map paramData, String type, Boolean idsOnly ->
            return [[testKey: 'testValue1', errorType: 'testErrorType', priority: 'testPriority', assignedTo: ''],
                    [testKey: 'testValue2', errorType: 'testErrorType', priority: 'testPriority', assignedTo: '']]
        }

        mockQualityService.exportToExcel(_, _) >> {
            return []
        }
        mockQualityService.getColumnList(_) >> {
            return ['masterCaseNum']
        }
        mockQualityService.getRenameValueOfReportField(_,_) >> { ['testValue1','testValue2'] }
        controller.qualityService=mockQualityService
        when:
        controller.exportToExcelQualitySampling()

        then:
        response.status==200

        where:
        dataValue<<['{"selectAll":"false","selectedIds":["1","2"]}','{"selectAll":"false","selectedIds":[]}']
    }

    @Unroll
    void "test export To Excel Case Data Quality "(){
        given:
        params.data=dataValue
        def tenantId = 1L
        Tenants.metaClass.static.currentId = { return tenant.id }
        def mockQualityService=Mock(QualityService)
        mockQualityService.getQualityDataByIds(_,_,_)>>{
            [[testKey:'testValue1', errorType:'testErrorType', priority:'testPriority', assignedTo:''],
             [testKey:'testValue2', errorType:'testErrorType', priority:'testPriority', assignedTo:'']]

        }
        controller.metaClass.getQualityDataByFilter = { Map paramData, String type, Boolean idsOnly ->
            return [[testKey: 'testValue1', errorType: 'testErrorType', priority: 'testPriority', assignedTo: ''],
                    [testKey: 'testValue2', errorType: 'testErrorType', priority: 'testPriority', assignedTo: '']]
        }

        mockQualityService.exportToExcel(_, _) >> {
            return []
        }
        mockQualityService.getColumnList(_) >> {
            return ['masterCaseNum']
        }
        mockQualityService.getRenameValueOfReportField(_,_) >> { ['testValue1','testValue2'] }
        controller.qualityService=mockQualityService
        when:
        controller.exportToExcelCaseDatQuality()

        then:
        response.status==200
        response.contentType==null

        where:
        dataValue<<['{"selectAll":"false","selectedIds":["1","2"]}','{"selectAll":"false","selectedIds":[]}']
    }

    @Unroll
    void "test send email api"(){
        given:
            params.act = value
            params.data = dataValue
            params.emailToUsers=userEmailValue
            params.body=emailBodyValue
            params.subject=emailSubjectValue
            params.tenantId = 1L
            params.dataType = type
            def tenantId = 1L
            Tenants.metaClass.static.currentId = { return tenant.id }
            def userServiceMock = new MockFor(UserService)
            userServiceMock.demand.getCurrentUser(1) { ->
                return new User(preference: new Preference(locale: Locale.ENGLISH))
            }
            controller.userService = userServiceMock.proxyInstance()
            def mockQualityService = Mock(QualityService)
            mockQualityService.getQualityDataByIds(_, _,_) >> {
                [[testKey: 'testValue1', errorType: 'testErrorType', priority: 'testPriority', assignedTo: ''],
                 [testKey: 'testValue2', errorType: 'testErrorType', priority: 'testPriority', assignedTo: '']]
                 }
            mockQualityService.getColumnList(_) >> {
                return ['masterCaseNum']
            }
            mockQualityService.getRenameValueOfReportField(_,_) >> { ['testValue1','testValue2'] }
            controller.qualityService = mockQualityService
            def mockEmailService=Mock(EmailService)
            mockEmailService.sendEmailWithFiles(_,_,_,_,_,_)>>{

            }
            controller.emailService=mockEmailService
        when:
            controller.sendEmail()
        then:
            response.status==302
            response.redirectedUrl=='/quality/'+ value
            flash.message!=null
        where:
        /*
        TODO: to add cases for null or empty text and email body
         */
        value               | dataValue                                       | userEmailValue                 | emailBodyValue  | emailSubjectValue|type
        'caseDataQuality'   | '{"selectAll":"false","selectedIds":["1","2"]}' | 'user1@g.com'                  | 'testEmailBody' | 'testSubject'|PvqTypeEnum.CASE_QUALITY.name()
        'caseDataQuality'   | '{"selectAll":"false","selectedIds":["1","2"]}' | ['user1@g.com', 'user2@g.com'] | 'testEmailBody' | 'testSubject'|PvqTypeEnum.CASE_QUALITY.name()
        'caseDataQuality'   | '{"selectAll":"false","selectedIds":["1","2"]}' | 'user1@g.com'                  | null          | 'testSubject'|PvqTypeEnum.CASE_QUALITY.name()
        'caseDataQuality'   | '{"selectAll":"false","selectedIds":["1","2"]}' | ['user1@g.com', 'user2@g.com'] | null          | 'testSubject'|PvqTypeEnum.CASE_QUALITY.name()
        'caseDataQuality'   | '{"selectAll":"false","selectedIds":["1","2"]}' | 'user1@g.com'                  | 'testEmailBody' | null|PvqTypeEnum.CASE_QUALITY.name()
        'caseDataQuality'   | '{"selectAll":"false","selectedIds":["1","2"]}' | ['user1@g.com', 'user2@g.com'] | 'testEmailBody' | null|PvqTypeEnum.CASE_QUALITY.name()

        'caseSampling?dataType=SAMPLING'      | '{"selectAll":"false","selectedIds":["1","2"]}' | 'user1@g.com'                  | 'testEmailBody' | 'testSubject'|PvqTypeEnum.SAMPLING.name()
        'caseSampling?dataType=SAMPLING'      | '{"selectAll":"false","selectedIds":["1","2"]}' | ['user1@g.com', 'user2@g.com'] | 'testEmailBody' | 'testSubject'|PvqTypeEnum.SAMPLING.name()
        'caseSampling?dataType=SAMPLING'      | '{"selectAll":"false","selectedIds":["1","2"]}' | 'user1@g.com'                  | null          | 'testSubject'|PvqTypeEnum.SAMPLING.name()
        'caseSampling?dataType=SAMPLING'      | '{"selectAll":"false","selectedIds":["1","2"]}' | ['user1@g.com', 'user2@g.com'] | null          | 'testSubject'|PvqTypeEnum.SAMPLING.name()
        'caseSampling?dataType=SAMPLING'      | '{"selectAll":"false","selectedIds":["1","2"]}' | 'user1@g.com'                  | 'testEmailBody' | null|PvqTypeEnum.SAMPLING.name()
        'caseSampling?dataType=SAMPLING'      | '{"selectAll":"false","selectedIds":["1","2"]}' | ['user1@g.com', 'user2@g.com'] | 'testEmailBody' | null|PvqTypeEnum.SAMPLING.name()

        'submissionQuality' | '{"selectAll":"false","selectedIds":["1","2"]}' | 'user1@g.com'                  | 'testEmailBody' | 'testSubject'|PvqTypeEnum.SUBMISSION_QUALITY.name()
        'submissionQuality' | '{"selectAll":"false","selectedIds":["1","2"]}' | ['user1@g.com', 'user2@g.com'] | 'testEmailBody' | 'testSubject'|PvqTypeEnum.SUBMISSION_QUALITY.name()
        'submissionQuality' | '{"selectAll":"false","selectedIds":["1","2"]}' | 'user1@g.com'                  | null          | 'testSubject'|PvqTypeEnum.SUBMISSION_QUALITY.name()
        'submissionQuality' | '{"selectAll":"false","selectedIds":["1","2"]}' | ['user1@g.com', 'user2@g.com'] | null          | 'testSubject'|PvqTypeEnum.SUBMISSION_QUALITY.name()
        'submissionQuality' | '{"selectAll":"false","selectedIds":["1","2"]}' | 'user1@g.com'                  | 'testEmailBody' | null|PvqTypeEnum.SUBMISSION_QUALITY.name()
        'submissionQuality' | '{"selectAll":"false","selectedIds":["1","2"]}' | ['user1@g.com', 'user2@g.com'] | 'testEmailBody' | null|PvqTypeEnum.SUBMISSION_QUALITY.name()


    }

    @Unroll
    void "test caseForm"() {
        given:
        Tenants.metaClass.static.currentId = { return tenant.id }
        QualityCaseData.metaClass.static.findById = { Long i ->
                new QualityCaseData(id:0,caseNumber: 'caseNumber1',metadata:"{}", errorType: 'errorType1', actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: false), workflowStateUpdatedDate: "01-Feb-2021")
        }
        QualitySubmission.metaClass.static.findById = { Long i ->
                new QualitySubmission(id:1,caseNumber: 'caseNumber3',metadata:"{}", errorType: 'errorType2', actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: true), workflowStateUpdatedDate: "01-Feb-2021")
        }
        QualitySampling.metaClass.static.findById = { Long i ->
                new QualitySampling(id:2,caseNumber: 'caseNumber5',metadata:"{}", errorType: 'errorType3', actionItems: [], fieldName: null, tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test", finalState: false), workflowStateUpdatedDate: "01-Feb-2021", type:PvqTypeEnum.SAMPLING.name())
        }
        controller.sqlGenerationService = [isCaseNumberExistsForTenant: { String caseNumber, Integer versionNumber -> true },
                                           getCaseMetadataDetails             : { String caseNumber, Integer versionNumber -> [case_info: [case_number: "caseNumber1"]] }
        ]
        def mockQualityService = new MockFor(QualityService)
        mockQualityService.demand.getQualityIssues(0..4) {
            return []
        }
        mockQualityService.demand.getRootCauses(0..4) {
            return []
        }
        mockQualityService.demand.getResponsibleParties(0..4) {
            return []
        }
       controller.qualityService = mockQualityService.proxyInstance()
        def reportExecutorServiceMock=Mock(ReportExecutorService)
        reportExecutorServiceMock.getCorrectiveActionList(_) >>  {
            return  [[id: 1, textDesc: "Root1", ownerApp: "Pvq"]]
        }
        reportExecutorServiceMock.getPreventativeActionList(_) >> {
            return  [[id: 1, textDesc: "Responsible1", ownerApp: "Pvq"]]
        }
        controller.reportExecutorService=reportExecutorServiceMock
        params.versionNumber = 2
        params.caseNumber = "caseNumber1"
        params.type = type

        when:
        def model = controller.caseForm()

        then:
        response.status == 200
        model.caseInfo.case_info.case_number == "caseNumber1"
        model.caseNumber == "caseNumber1"
        model.versionNumber == 2
        model.attachmentsList.size() == 0

        where:
        type << [PvqTypeEnum.CASE_QUALITY.name(), PvqTypeEnum.SUBMISSION_QUALITY.name(), PvqTypeEnum.SAMPLING.name()]
    }

    @Unroll
    void "test updateFieldError"() {
        given:
        controller.qualityService = [
                saveAdhocQualityRecord: { Map params, Long tenantId, Long selectedId -> [:] },
                checkCaseNumber       : { String s -> [caseNumber: "CASENUMBER"] }
        ]

        params.caseNumber = "CASENUMBER"
        Tenants.metaClass.static.currentId = { return tenant.id }
        when:
        controller.updateFieldError()
        then:
        response.status == 200
    }

    @Unroll
    void "test issueList"() {
        given:
        Tenants.metaClass.static.currentId = { return tenant.id }
        Map<String,Map<Long,String>> allRcaDataMap = [:]
        WorkflowState.metaClass.static.findByIdAndIsDeleted = {Long a, Boolean b->  new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test")}
        QualityCaseData.metaClass.static.findAllByIsDeletedAndCaseNumberAndTenantId = { Boolean b, String s, Long i ->
            if(s=="caseNumber1")
            [       new QualityCaseData(id:0,caseNumber: 'caseNumber1', masterVersionNum: 1L, metadata:"{}", errorType: 'errorType1', actionItems: [], tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test"), workflowStateUpdatedDate: "01-Feb-2021"),
                    new QualityCaseData(id:1,caseNumber: 'caseNumber1', masterVersionNum: 1L,metadata:"{}", errorType: 'errorType1', actionItems: [], tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test"), workflowStateUpdatedDate: "01-Feb-2021")]
            .collect{it.metaClass.getId = {->0}; it}
            else []
        }
        QualitySubmission.metaClass.static.findAllByIsDeletedAndCaseNumberAndTenantIdAndSubmissionIdentifier = { Boolean b, String s, Long i, String s1  ->
            if(s=="caseNumber3")
            [new QualitySubmission(id:2,caseNumber: 'caseNumber3', masterVersionNum: 2L, metadata:"{}", errorType: 'errorType2', actionItems: [], tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test"), workflowStateUpdatedDate: "01-Feb-2021"),
             new QualitySubmission(id:3,caseNumber: 'caseNumber3', masterVersionNum: 2L, metadata:"{}", errorType: 'errorType2', actionItems: [], tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test"), workflowStateUpdatedDate: "01-Feb-2021")]
                    .collect{it.metaClass.getId = {->0}; it}
            else []
        }
        QualitySampling.metaClass.static.findAllByIsDeletedAndCaseNumberAndTenantIdAndType = { Boolean b, String s, Long i, String t ->
            if(s=="caseNumber5")
            [new QualitySampling(id:4,caseNumber: 'caseNumber5', masterVersionNum: 3L, metadata:"{}", errorType: 'errorType3', actionItems: [], tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test"), workflowStateUpdatedDate: "01-Feb-2021", type:PvqTypeEnum.SAMPLING.name()),
             new QualitySampling(id:5,caseNumber: 'caseNumber5', masterVersionNum: 3L, metadata:"{}", errorType: 'errorType3', actionItems: [], tenantId: 1L, workflowState: new WorkflowState(name: "new", modifiedBy: "test", createdBy: "test"), workflowStateUpdatedDate: "01-Feb-2021", type:PvqTypeEnum.SAMPLING.name())]
                    .collect{it.metaClass.getId = {->0}; it}
            else []
        }
        def normalUser = makeNormalUser()
        controller.userService = makeSecurityService(normalUser)
        def mockQualityService=Mock(QualityService)
        mockQualityService.formQualityRecordRow(_,_,_,_,_,_) >> {row["state"]="state"; return row}
        mockQualityService.getAllRcaDataMap() >> {return allRcaDataMap}
        mockQualityService.getQualityReportAllFields(_) >> [
                [fieldName: 'fieldA', fieldType: 'String', fieldLabel: 'Field A', selectable: true],
                [fieldName: 'fieldB', fieldType: 'Date', fieldLabel: 'Field B', selectable: false]
        ]
        controller.qualityService=mockQualityService
        when:
        params.type = type
        params.caseNumber = caseNumber
        params.versionNumber = versionNumber
        controller.issueList()
        then:
        response.status == 200
        response.json.size() == size
        response.json[0].errorType == errorType
        where:
        type                                  | caseNumber    | versionNumber | size | errorType    | row
        PvqTypeEnum.CASE_QUALITY.name()       | 'caseNumber1' | '1'           | 2    | "errorType1" | [caseNumber: 'caseNumber1', masterVersionNum: 1L, errorType: 'errorType1', state: 'testState1', qualityIssueNotError: true]
        PvqTypeEnum.SUBMISSION_QUALITY.name() | 'caseNumber3' | '2'           | 2    | "errorType2" | [caseNumber: 'caseNumber3', masterVersionNum: 2L, errorType: 'errorType2', state: 'testState3', qualityIssueNotError: false]
        PvqTypeEnum.SAMPLING.name()           | 'caseNumber5' | '3'           | 2    | "errorType3" | [caseNumber: 'caseNumber5', masterVersionNum: 3L, errorType: 'errorType3', state: 'testState5', qualityIssueNotError: true]
    }

    void "test importExcel"() {
        given:
        def multipartFile = new GrailsMockMultipartFile('file', 'reportFile.xlsx', '', new byte[0])
        request.addFile(multipartFile)

        def mockImportService = new MockFor(ImportService)
        mockImportService.demand.readFromExcel(0..1){ file->
            return list
        }
        controller.importService = mockImportService.proxyInstance()

        when:
        controller.importExcel()

        then:
        response.json == json

        where:
        list                            | json
        ['00US0002641', '00US00039396'] | ['success':true, 'uploadedValues':'00US0002641;00US00039396', 'message':'']
        []                              | ['success':false, 'uploadedValues':'', 'message':'app.label.no.data.excel.error']
    }



    void "test exportActionPlanToExcel"() {
        given:
        def result
        def normalUser = makeNormalUser()
        controller.userService = makeSecurityService(normalUser)
        Tenants.metaClass.static.currentId = { return tenant.id }
        controller.qualityService = [
                getRanges    : { def m -> return ["from0": new Date(), "to0": new Date()] },
                exportToExcel: { List sheets ->
                    result = sheets; return null },
                getActionPlanData      : { Map params, Map<String, Date> ranges -> return [] },
                fetchActonPlanCasesData: { Date from, Date to, List responsiblePartyFilter, List<String> errorTypeFilter, List<String> observationFilter, String workflowFilter, List<String> issueTypeFilter, String priorityFilter, Boolean primaryOnly, String timeZone, Locale locale ->
                    return []
                }

        ]

        AuditLogConfigUtil.metaClass.static.logChanges = {domain, Map newMap, Map oldMap, String eventName, String extraValue = "", String transactionId = ("" + System.currentTimeMillis()), String username = null, String fullname = null ->

        }
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> code }
        controller.metaClass.getActionPlanData = { params, Map<String, Date> ranges ->
            [[responsibleParty  : "responsibleParty",
              responsiblePartyId: "responsiblePartyId",
              observation       : "observation",
              observationCode   : "observationCode",
              errorType         : "errorType",
              priority          : "priority",
              lastNumber0       : 1,
              lastVendor0       : 1,
              lastIssue0        : 1,
              lastObservation0  : 1,
              lastPriority0     : 1,
              completed0        : 1,
              overdue0          : 1,
              total0            : 1
             ]]
        }
        controller.metaClass.fetchActonPlanCasesData = { Date from, Date to, List responsiblePartyFilter, List<String> errorTypeFilter, List<String> observationFilter, String workflowFilter, List<String> issueTypeFilter, String priorityFilter ->
            [[id                : "id",
              caseNumber        : "caseNumber",
              caseVersion       : "caseVersion",
              observation       : "observation",
              assignedTo        : "assignedTo",
              rootCause         : "rootCause",
              correctiveAction  : "correctiveAction",
              responsibleParty  : "responsibleParty",
              preventativeAction: "preventativeAction",
              correctiveDate    : "correctiveDate",
              preventativeDate  : "preventativeDate",
              investigation     : "investigation",
              summary           : "summary",
              actions           : "actions",
              primary           : "primary",
              errorType         : "errorType",
              workFlowState     : "workFlowState",


             ]]
        }
        when:
        params.data = '{"someKey":"someVal"}'
        controller.exportActionPlanToExcel()
        then:
        response.text == 'ok'
        response.status == 200
    }

    void "test fieldPossibleValues"() {
        given:
        String lang= 'en'
        String field = fieldValue
        String term = searchTerm
        Integer max = 30
        Integer page = 1
        def mockReportFieldService = Mock(ReportFieldService)
        mockReportFieldService.getSelectableValuesForFields(lang) >> {
            return [
                    'deathAutopsy': ['<NULL>', 'Autopsy Done', 'Autopsy Done, Results Available', 'Autopsy Done, Results Not Available', 'Autopsy Not Done', 'Unknown'],
                    'assessOutcome': ['Abortion due to AE/Infection', 'Congenital Anomaly', 'Death due to AE/infection', 'Death not due to AE/infection', 'Fatal', 'Improved', 'Lasting Damage', 'Not Recovered', 'Not Recovered/Not Resolved', 'Not Reported', 'Recovered', 'Recovered Sequelae', 'Recovered with Treatment', 'Resolved', 'Unchanged', 'Unknown', 'Worsened'],
                    'deathDetailsTermType': ['Autopsy Results', 'Cause of Death'],
                    'patInfoGenderId': ['Female', 'Male', 'UNK'],
                    'reportersCountryId': ['INDIA', 'US', 'UK', 'JAPAN', 'CHINA', 'AUSTRALIA'],
                    'casePatInfoPvrEthnicityId': ['*Multiracial or Other', 'African American', 'American Indian or Alaska Native', 'Asian', 'Black', 'Caucasian', 'HINDU', 'Native Hawaiian or Other Pacific Islander', 'Other', 'White'],
                    'cptpTrimOfExp': ['FIRST', 'FIRST', 'SECOND', 'FIRST', 'SECOND', 'THIRD', 'FIRST', 'THIRD', 'UNKNOWN']
            ]
        }
        controller.reportFieldService = mockReportFieldService

        when:
        controller.fieldPossibleValues(lang, field, term, max, page)

        then:
        response.status == 200
        response.json == json

        where:
        fieldValue        | searchTerm   | json
        "assessOutcome"   | ""           | [[id:'Abortion due to AE/Infection', text:'Abortion due to AE/Infection'], [id:'Congenital Anomaly', text:'Congenital Anomaly'], [id:'Death due to AE/infection', text:'Death due to AE/infection'], [id:'Death not due to AE/infection', text:'Death not due to AE/infection'], [id:'Fatal', text:'Fatal'], [id:'Improved', text:'Improved'], [id:'Lasting Damage', text:'Lasting Damage'], [id:'Not Recovered', text:'Not Recovered'], [id:'Not Recovered/Not Resolved', text:'Not Recovered/Not Resolved'], [id:'Not Reported', text:'Not Reported'], [id:'Recovered', text:'Recovered'], [id:'Recovered Sequelae', text:'Recovered Sequelae'], [id:'Recovered with Treatment', text:'Recovered with Treatment'], [id:'Resolved', text:'Resolved'], [id:'Unchanged', text:'Unchanged'], [id:'Unknown', text:'Unknown'], [id:'Worsened', text:'Worsened']]
        "assessOutcome"   | "Congenital" | [[id:'Congenital Anomaly', text:'Congenital Anomaly']]
        "deathAutopsy"    | ""           | [[id:'<NULL>', text:'<NULL>'], [id:'Autopsy Done', text:'Autopsy Done'], [id:'Autopsy Done, Results Available', text:'Autopsy Done, Results Available'], [id:'Autopsy Done, Results Not Available', text:'Autopsy Done, Results Not Available'], [id:'Autopsy Not Done', text:'Autopsy Not Done'], [id:'Unknown', text:'Unknown']]
        "deathAutopsy"    | "Unknown"    | [[id:'Unknown', text:'Unknown']]
        "patInfoGenderId" | ""           | [[id:'Female', text:'Female'], [id:'Male', text:'Male'], [id:'UNK', text:'UNK']]
    }

}
