package com.rxlogix.api

import com.rxlogix.*
import com.rxlogix.config.*
import com.rxlogix.enums.IcsrCaseStateEnum
import com.rxlogix.enums.IcsrReportSpecEnum
import com.rxlogix.enums.MessageTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.file.MultipartFileSender
import com.rxlogix.user.*
import com.rxlogix.util.AuditLogConfigUtil
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import org.grails.datastore.mapping.simple.SimpleMapDatastore
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import grails.gorm.multitenancy.Tenants

@ConfineMetaClassChanges([GrailsHibernateUtil, AuditLogConfigUtil, MultipartFileSender, User, IcsrCaseTracking, IcsrProfileConfiguration, IcsrCaseSubmission, ExecutedIcsrTemplateQuery])
class IcsrCaseTrackingRestControllerSpec extends Specification implements DataTest, ControllerUnitTest<IcsrCaseTrackingRestController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, SuperQuery, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference, ExecutedIcsrTemplateQuery, IcsrProfileConfiguration, ExecutedIcsrProfileConfiguration, ExecutedTemplateQuery, ExecutedReportConfiguration, BulkDownloadIcsrReports, IcsrCaseTracking
        GrailsHibernateUtil.metaClass.static.unwrapIfProxy = { obj ->
            return obj
        }
        AuditLogConfigUtil.metaClass.static.logChanges = { domain, Map newMap, Map oldMap, String eventName, String extraValue ="", String transactionId = "" -> }
    }

    private User makeNormalUser(name, team) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_TEMPLATE_VIEW', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user")
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
        normalUser.metaClass.isICSRAdmin = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
        return normalUser
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

    void "test parseCaseTrackingList"(){
        ExecutedIcsrTemplateQuery executedTemplateQuery = new ExecutedIcsrTemplateQuery(msgType: MessageTypeEnum.MASTER,createdBy:"user",modifiedBy: "user",executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"),draftReportResult: new ReportResult(),finalReportResult: new ReportResult() )
        ExecutedIcsrProfileConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(senderId: "1",receiverId: "1",status: ReportExecutionStatusEnum.GENERATED_DRAFT,reportName: "report",numOfExecutions: 1)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        executedTemplateQuery.executedConfiguration = executedReportConfiguration
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        when:
        def result = controller.parseCaseTrackingList(["1;10"])
        then:
        result.size() == 1
    }

    void "test parseCaseTrackingList currentSenderId != senderId"(){
        ExecutedIcsrTemplateQuery executedTemplateQuery = new ExecutedIcsrTemplateQuery(msgType: MessageTypeEnum.MASTER,createdBy:"user",modifiedBy: "user",executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"),draftReportResult: new ReportResult(),finalReportResult: new ReportResult() )
        ExecutedIcsrProfileConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(senderId: "1",receiverId: "1",status: ReportExecutionStatusEnum.GENERATED_DRAFT,reportName: "report",numOfExecutions: 1)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        executedTemplateQuery.executedConfiguration = executedReportConfiguration
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        ExecutedIcsrTemplateQuery executedTemplateQueryInstance = new ExecutedIcsrTemplateQuery(msgType: MessageTypeEnum.MASTER,createdBy:"user",modifiedBy: "user",executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"),draftReportResult: new ReportResult(),finalReportResult: new ReportResult() )
        ExecutedIcsrProfileConfiguration executedReportConfigurationInstance = new ExecutedIcsrProfileConfiguration(senderId: "2",receiverId: "2",status: ReportExecutionStatusEnum.GENERATED_DRAFT,reportName: "report",numOfExecutions: 1)
        executedReportConfigurationInstance.save(failOnError:true,validate:false,flush:true)
        executedTemplateQueryInstance.executedConfiguration = executedReportConfigurationInstance
        executedTemplateQueryInstance.save(failOnError:true,validate:false,flush:true)
        when:
        controller.parseCaseTrackingList(["1;10","2;10"])
        then:
        RuntimeException runtimeException = thrown()
        runtimeException.message == "icsr.case.tracking.error.differentSenderReceiverPair"
    }

    void "test parseCaseTrackingList currentReceiverId != receiverId"(){
        ExecutedIcsrTemplateQuery executedTemplateQuery = new ExecutedIcsrTemplateQuery(msgType: MessageTypeEnum.MASTER,createdBy:"user",modifiedBy: "user",executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"),draftReportResult: new ReportResult(),finalReportResult: new ReportResult() )
        ExecutedIcsrProfileConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(senderId: "1",receiverId: "1",status: ReportExecutionStatusEnum.GENERATED_DRAFT,reportName: "report",numOfExecutions: 1)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        executedTemplateQuery.executedConfiguration = executedReportConfiguration
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        ExecutedIcsrTemplateQuery executedTemplateQueryInstance = new ExecutedIcsrTemplateQuery(msgType: MessageTypeEnum.MASTER,createdBy:"user",modifiedBy: "user",executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"),draftReportResult: new ReportResult(),finalReportResult: new ReportResult() )
        ExecutedIcsrProfileConfiguration executedReportConfigurationInstance = new ExecutedIcsrProfileConfiguration(senderId: "1",receiverId: "2",status: ReportExecutionStatusEnum.GENERATED_DRAFT,reportName: "report",numOfExecutions: 1)
        executedReportConfigurationInstance.save(failOnError:true,validate:false,flush:true)
        executedTemplateQueryInstance.executedConfiguration = executedReportConfigurationInstance
        executedTemplateQueryInstance.save(failOnError:true,validate:false,flush:true)
        when:
        controller.parseCaseTrackingList(["1;10","2;10"])
        then:
        RuntimeException runtimeException = thrown()
        runtimeException.message == "icsr.case.tracking.error.differentSenderReceiverPair"
    }

    void "test parseCaseTrackingList existingCase"(){
        ExecutedIcsrTemplateQuery executedTemplateQuery = new ExecutedIcsrTemplateQuery(msgType: MessageTypeEnum.MASTER,createdBy:"user",modifiedBy: "user",executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"),draftReportResult: new ReportResult(),finalReportResult: new ReportResult() )
        ExecutedIcsrProfileConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(senderId: "1",receiverId: "1",status: ReportExecutionStatusEnum.GENERATED_DRAFT,reportName: "report",numOfExecutions: 1)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        executedTemplateQuery.executedConfiguration = executedReportConfiguration
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        ExecutedIcsrTemplateQuery executedTemplateQueryInstance = new ExecutedIcsrTemplateQuery(msgType: MessageTypeEnum.BACKLOG,createdBy:"user",modifiedBy: "user",executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"),draftReportResult: new ReportResult(),finalReportResult: new ReportResult() )
        ExecutedIcsrProfileConfiguration executedReportConfigurationInstance = new ExecutedIcsrProfileConfiguration(senderId: "1",receiverId: "1",status: ReportExecutionStatusEnum.GENERATED_DRAFT,reportName: "report",numOfExecutions: 1)
        executedReportConfigurationInstance.save(failOnError:true,validate:false,flush:true)
        executedTemplateQueryInstance.executedConfiguration = executedReportConfigurationInstance
        executedTemplateQueryInstance.save(failOnError:true,validate:false,flush:true)
        when:
        controller.parseCaseTrackingList(["1;10","2;10"])
        then:
        RuntimeException runtimeException = thrown()
        runtimeException.message == "icsr.case.tracking.error.duplicateCaseNumber"
    }

    void "test parseCaseTrackingList Executed template not found"(){
        when:
        controller.parseCaseTrackingList(["1"])
        then:
        RuntimeException runtimeException = thrown()
        runtimeException.message == "Executed template not found"
    }

    void "test downloadBulkXML"(){
        int run = 0
        ExecutedIcsrTemplateQuery executedTemplateQuery = new ExecutedIcsrTemplateQuery(createdBy:"user",modifiedBy: "user",executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"),draftReportResult: new ReportResult(),finalReportResult: new ReportResult() )
        ExecutedIcsrProfileConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(senderId: "1",receiverId: "1",status: ReportExecutionStatusEnum.GENERATED_DRAFT,reportName: "report",numOfExecutions: 1)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        executedTemplateQuery.executedConfiguration = executedReportConfiguration
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.createBulkXMLReport(0..1){ List<Tuple2<String, ReportResult>> requestData, IcsrReportSpecEnum reportSpec->
            run++
            return new File("path")
        }
        controller.icsrReportService = mockIcsrReportService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getContentType(0..1){ String extension -> ""}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        MultipartFileSender.metaClass.static.renderFile = { File file, String reportFileName, String ext, String contentType, HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean inline->
            run++
        }
        when:
        params["caseNumber[]"] = ["1;10"]
        params.reportSpec = "E2B_R3"
        controller.downloadBulkXML()
        then:
        run == 2
    }

    void "test downloadBatchXML"(){
        int run = 0
        ExecutedIcsrTemplateQuery executedTemplateQuery = new ExecutedIcsrTemplateQuery(createdBy:"user",modifiedBy: "user",executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"),draftReportResult: new ReportResult(),finalReportResult: new ReportResult() )
        ExecutedIcsrProfileConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(senderId: "1",receiverId: "1",status: ReportExecutionStatusEnum.GENERATED_DRAFT,reportName: "report",numOfExecutions: 1)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        executedTemplateQuery.executedConfiguration = executedReportConfiguration
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.createBatchXMLReport(0..1){ List<Tuple2<String, ReportResult>> requestData, IcsrReportSpecEnum reportSpec->
            run++
            return new File("path")
        }
        controller.icsrReportService = mockIcsrReportService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getContentType(0..1){ String extension -> ""}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        MultipartFileSender.metaClass.static.renderFile = { File file, String reportFileName, String ext, String contentType, HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean inline->
            run++
        }
        when:
        params["caseNumber[]"] = ["1;10"]
        params.reportSpec = "E2B_R3"
        controller.downloadBatchXML()
        then:
        run == 2
    }

    void "test downloadBatchXML runtime exception"(){
        int run = 0
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.createBatchXMLReport(0..1){ List<Tuple2<String, ReportResult>> requestData, IcsrReportSpecEnum reportSpec->
            run++
            return new File("path")
        }
        controller.icsrReportService = mockIcsrReportService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getContentType(0..1){ String extension -> ""}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        MultipartFileSender.metaClass.static.renderFile = { File file, String reportFileName, String ext, String contentType, HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean inline->
            run++
        }
        when:
        params["caseNumber[]"] = ""
        params.reportSpec = "E2B_R3"
        controller.downloadBatchXML()
        then:
        run == 0
        flash.error != null
        response.redirectUrl == '/icsrProfileConfiguration/viewCases'
    }

    void "test getIndicator yellow"(){
        Date now = new Date();
        when:
        def result = controller.getIndicator(new IcsrCaseTracking(),now+1)
        then:
        result == "yellow"
    }

    void "test getIndicator red"(){
        Date now = new Date();
        when:
        def result = controller.getIndicator(new IcsrCaseTracking(),now-1)
        then:
        result == "red"
    }

    void "test getIndicator null"(){
        Date now = new Date();
        when:
        def result = controller.getIndicator(new IcsrCaseTracking(),now+4)
        then:
        result == ""
    }

    void "test toMap"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(recipientOrganizationName: "organisation")
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQuery: new SuperQuery(name: "super"),executedTemplate: new ReportTemplate(name: "template"))
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        when:
        def result = controller.toMap(new IcsrCaseTracking(exIcsrProfileId: executedReportConfiguration.id,exIcsrTemplateQueryId: executedTemplateQuery.id,safetyReceiptDate: new Date(),caseNumber: "1",versionNumber: 1,caseReceiptDate: new Date(),productName: "product",eventPreferredTerm: "event",susar: "susar",profileName: "profile",generationDate: new Date(),submissionDate: new Date(),e2BStatus: "status"))
        then:
        result.size() == 57
    }



    void "test index with indicator red"(){
        given:
        User normalUser = makeNormalUser("user",[])
        Date now = new Date()
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(recipientOrganizationName: "organisation")
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQuery: new SuperQuery(name: "super"),executedTemplate: new ReportTemplate(name: "template"))
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..6){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockIcsrCaseTrackingService = new MockFor(IcsrCaseTrackingService)
        mockIcsrCaseTrackingService.demand.preloadPrequalifiedCases(1) { List<IcsrCaseTracking> icsrCaseTrackingList ->
            return [:]
        }
        controller.icsrCaseTrackingService = mockIcsrCaseTrackingService.proxyInstance()

        IcsrProfileConfiguration.metaClass.static.fetchAllProfileIds = { User currentUser , Boolean isAdmin, Boolean includeArchived ->
            new Object() {
                List list(Object o) {
                    return [executedReportConfiguration.getId()]
                }
            }
        }
        IcsrCaseTracking.metaClass.static.getAllByFilter = { LibraryFilter filter, String caseNumber = null, Long versionNumber = null, Long exIcsrProfileId = null, Long exIcsrTemplateQueryId = null, String icsrCaseStateEnum = null, List<Long> profileIds, def searchData, boolean isAdmin, String sort, String direction = "asc" -> new Object(){
                List list(Object o){ [max:10, offset:0, sort:"", order:""]
                    return [new IcsrCaseTracking(dueDate: now -3,exIcsrProfileId: executedReportConfiguration.id,exIcsrTemplateQueryId: executedTemplateQuery.id,safetyReceiptDate: now-1,caseNumber: "1",versionNumber: 1,caseReceiptDate: new Date(),productName: "product",eventPreferredTerm: "event",susar: "susar",profileName: "profile",generationDate: new Date(),e2BStatus: "status")]
                }

                int count() {
                    return 1
                }
            }
        }
        when:
        params.max = 10
        params.offset = 0
        params.sort = ""
        params.order = ""
        controller.index()
        then:
        response.json.recordsTotal == 1
        response.json.aaData[0].size() == 57
        response.json.recordsFiltered == 1
        response.json.aaData[0].indicator == "red"
    }


    void "test notFound"(){
        when:
        params.id = 1
        controller.notFound()
        then:
        response.redirectUrl == '/icsrCaseTrackingRest/index'
        flash.message == 'default.not.found.message'
    }

    void "test getErrorDetails"(){
        given:
        controller.targetDatastore = new SimpleMapDatastore(['pva'], IcsrCaseSubmission)
        IcsrCaseSubmission.metaClass.static.fetchIcsrErrorDetailsByCaseNoAndVersionNo = {String profileName, Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber, String status -> new Object(){
                Object get(){
                    return new IcsrCaseSubmission(parsingErrorTxt: "Parsing Error File")
                }
            }
        }
        when:
        controller.getErrorDetails("xyz profile",121,'1',1,'COMMIT_REJECTED')
        then:
        response.text == "Parsing Error File"
    }

    void "test nullifyReport"() {
        given:
        controller.targetDatastore = new SimpleMapDatastore(['pva'], IcsrCaseTracking)
        ExecutedIcsrTemplateQuery executedTemplateQuery = new ExecutedIcsrTemplateQuery(msgType: MessageTypeEnum.MASTER, createdBy: "user", modifiedBy: "user", executedTemplate: new ReportTemplate(name: "template"), executedQuery: new SuperQuery(name: "query"), draftReportResult: new ReportResult(), finalReportResult: new ReportResult())
        ExecutedIcsrProfileConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(senderId: "1", receiverId: "1", status: ReportExecutionStatusEnum.GENERATED_DRAFT, reportName: "report", numOfExecutions: 1)
        executedReportConfiguration.save(failOnError: true, validate: false, flush: true)
        executedTemplateQuery.executedConfiguration = executedReportConfiguration
        executedTemplateQuery.save(failOnError: true, validate: false, flush: true)
        ExecutedIcsrTemplateQuery.metaClass.static.read = { Long id -> executedTemplateQuery }
        ExecutedIcsrTemplateQuery targetTemplateQuery = new ExecutedIcsrTemplateQuery(msgType: MessageTypeEnum.MASTER, createdBy: "user", modifiedBy: "user", executedTemplate: new ReportTemplate(name: "template"), executedQuery: new SuperQuery(name: "query"), draftReportResult: new ReportResult(), finalReportResult: new ReportResult())
        ExecutedIcsrProfileConfiguration targetReportConfiguration = new ExecutedIcsrProfileConfiguration(senderId: "1", receiverId: "1", status: ReportExecutionStatusEnum.GENERATED_DRAFT, reportName: "report", numOfExecutions: 1)
        targetReportConfiguration.save(failOnError: true, validate: false, flush: true)
        targetTemplateQuery.executedConfiguration = executedReportConfiguration
        targetTemplateQuery.save(failOnError: true, validate: false, flush: true)
        IcsrCaseTracking newIcsrCaseTrackingInstance = new IcsrCaseTracking(exIcsrTemplateQueryId: 100L, caseNumber: "123456", versionNumber: 1, profileName: executedReportConfiguration.reportName, recipient: "user")
        newIcsrCaseTrackingInstance.save(failOnError: true, validate: false, flush: true)
        Map newValues = [caseNumber: "123456", version: 1, exIcsrTemplateQueryId: 100L, icsrState: IcsrCaseStateEnum.SCHEDULED]
        targetReportConfiguration.metaClass.getExecutedTemplateQueriesForProcessing = {
            -> return targetTemplateQuery
        }
        def mockExecutedIcsrConfigurationService = Mock(ExecutedIcsrConfigurationService)
        mockExecutedIcsrConfigurationService.createFromExecutedIcsrConfiguration(_, _) >> {
            return targetReportConfiguration
        }
        controller.executedIcsrConfigurationService = mockExecutedIcsrConfigurationService
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithoutAuditLog(_) >> {
            return targetReportConfiguration
        }
        controller.CRUDService = mockCRUDService
        IcsrProfileConfiguration.metaClass.static.findByReportName = { String s -> new IcsrProfileConfiguration(id: 1L, reportName: 'test profile', tenantId: 1L, isDeleted: false) }
        def mockIcsrScheduleService = Mock(IcsrScheduleService)
        mockIcsrScheduleService.logIcsrCaseToScheduleTrackingForNullification(_, _, _, _, _, _, _, _) >> {
            return true
        }
        controller.icsrScheduleService = mockIcsrScheduleService
        def mockIcsrProfileAckService = Mock(IcsrProfileAckService)
        mockIcsrProfileAckService.getIcsrTrackingRecord(_, _, _) >> {
            return newIcsrCaseTrackingInstance
        }
        mockIcsrProfileAckService.icsrCaseTrackingMapForAuditLog(_) >> {
            return newValues
        }
        controller.icsrProfileAckService = mockIcsrProfileAckService
        when:
        params.icsrTempQueryId = 100L
        params.versionNumber = 1
        params.prodHashCode = "1"
        params.justification = "Test Justification"
        controller.nullifyReport("123456", 5)
        then:
        response.status == 200
    }

    void "test checkPreviousVersionIsTransmittedForTrue"() {
        given:
        Holders.config.pvr.icsr.enforce.transmission.in.version.sequence = true

        def mockIcsrScheduleService = Mock(IcsrScheduleService)
        mockIcsrScheduleService.checkPreviousVersionIsTransmitted(_,_,_,_,_) >> true
        controller.icsrScheduleService = mockIcsrScheduleService

        controller.params.profileName = "Test Profile 1"
        controller.params.recipient = "Test Recipient 1"
        controller.params.caseNumber = "202302210001"
        controller.params.templateId = 12345L
        controller.params.versionNumber = 1

        when:
        controller.checkPreviousVersionIsTransmitted("Test Profile 1", "Test Recipient 1", "202302210001")

        then:
        response.status == 200
    }

    void "test checkPreviousVersionIsTransmittedForFalse"() {
        given:
        Holders.config.pvr.icsr.enforce.transmission.in.version.sequence = true

        controller.params.profileName = "Test Profile 1"
        controller.params.recipient = "Test Recipient 1"
        controller.params.caseNumber = "202302210001"
        controller.params.templateId = "12345"
        controller.params.versionNumber = "1"

        def mockIcsrScheduleService = Mock(IcsrScheduleService)
        mockIcsrScheduleService.checkPreviousVersionIsTransmitted(_,_,_,_,_) >> false
        controller.icsrScheduleService = mockIcsrScheduleService

        when:
        controller.checkPreviousVersionIsTransmitted()

        then:
        response.status == 500
    }
    def "should transmit previous version when flag is true"() {
        given:
        Holders.config.pvr.icsr.enforce.transmission.in.version.sequence = true
        def mockIcsrScheduleService = Mock(IcsrScheduleService)
        mockIcsrScheduleService.checkPreviousVersionIsTransmitted(_, _, _, _, _) >> true
        controller.icsrScheduleService = mockIcsrScheduleService

        controller.params.templateId = 12345L
        controller.params.profileName = "Test Profile 1"
        controller.params.recipient = "Test Recipient 1"
        controller.params.caseNumber = "202302210001"
        controller.params.versionNumber = 1

        when:
        controller.checkPreviousVersionIsTransmittedForAll()

        then:
        response.status == 200
    }

    void "test checkAllPreviousVersionIsTransmittedForFalse"() {
        given:
        Holders.config.pvr.icsr.enforce.transmission.in.version.sequence= true
        def mockIcsrScheduleService = Mock(IcsrScheduleService)
        mockIcsrScheduleService.checkPreviousVersionIsTransmitted(_,_,_,_,_) >> {
            return false
        }
        controller.icsrScheduleService = mockIcsrScheduleService
        params.checkIds = """[{"id":"51752_20230200154_2","status":"GENERATED","recipient":"Test Unit","templateId":29458,"profileName":"Test Profile 2","caseNumber":20230200154,"versionNumber":2}]"""

        when:
        controller.checkPreviousVersionIsTransmittedForAll()

        then:
        response.status == 500
    }

    void "test checkAvailableDevice"() {
        given:
        IcsrProfileConfiguration.metaClass.static.read = { Long id ->
            new IcsrProfileConfiguration(
                    id: 1L,
                    reportName: 'Test Device Reporting Profile',
                    tenantId: 1L,
                    isDeleted: false,
                    deviceReportable: true
            )
        }

        def mockSqlGenerationService = Mock(SqlGenerationService)
        mockSqlGenerationService.fetchCaseIdFromSource(_,_) >> { "1234" }
        mockSqlGenerationService.checkCaseProductList(_,_,_) >> {
            return [
                    [PROD_HASH_CODE: "123", PRODUCT_NAME: 'Device Product 1'],
                    [PROD_HASH_CODE: "456", PRODUCT_NAME: 'Device Product 2']
            ]
        }

        controller.sqlGenerationService = mockSqlGenerationService
        controller.params.caseVersionNumber = "20230530001##1"
        controller.params.profileId = "1"
        Tenants.metaClass.static.currentId = { return tenant.id }

        when:
        controller.listDevices()

        then:
        response.status == 200
    }

    void "test checkAvailableDeviceForNoDevice"() {
        given:
        IcsrProfileConfiguration.metaClass.static.read = { Long id -> new IcsrProfileConfiguration(id: 1L, reportName: 'Test Device Reporting Profile', tenantId: 1L, isDeleted: false, deviceReportable: true)}
        def mockSqlGenerationService = Mock(SqlGenerationService)
        mockSqlGenerationService.fetchCaseIdFromSource(_,_) >> {
            return "1234"
        }
        mockSqlGenerationService.checkCaseProductList(_,_) >> {
            return [[PROD_REC_NUM: -1, PRODUCT_NAME: null]]
        }
        controller.sqlGenerationService = mockSqlGenerationService
        controller.params.caseVersionNumber = "20230530001##1"
        controller.params.profileId = "1"

        when:
        controller.listDevices()

        then:
        response.status == 200
    }

    void "test checkAvailableDeviceForNoDeviceProfile"() {
        given:
        IcsrProfileConfiguration.metaClass.static.read = { Long id -> new IcsrProfileConfiguration(id: 1L, reportName: 'Test Device Reporting Profile', tenantId: 1L, isDeleted: false, deviceReportable: false)}
        def mockSqlGenerationService = Mock(SqlGenerationService)
        mockSqlGenerationService.fetchCaseIdFromSource(_,_) >> {
            return "1234"
        }
        mockSqlGenerationService.checkCaseProductList(_,_) >> {
            return [[PROD_REC_NUM: 1, PRODUCT_NAME: 'Device Product 1'], [PROD_REC_NUM: 2, PRODUCT_NAME: 'Device Product 2']]
        }
        controller.sqlGenerationService = mockSqlGenerationService
        controller.params.caseVersionNumber = "20230530001##1"
        controller.params.profileId = "1"
        when:
        controller.listDevices()

        then:
        response.status == 200
    }

    void "test listDevicesForMultipleDevice"() {
        given:
        def mockSqlGenerationService = Mock(SqlGenerationService)
        mockSqlGenerationService.fetchCaseIdFromSource(_,_) >> {
            return "1234"
        }
        mockSqlGenerationService.checkCaseProductList(_,_) >> {
            return [[PROD_REC_NUM: 1, PRODUCT_NAME: 'Device Product 1'], [PROD_REC_NUM: 2, PRODUCT_NAME: 'Device Product 2']]
        }
        controller.sqlGenerationService = mockSqlGenerationService

        when:
        params.caseNumber = "20230530001"
        params.version = 1
        controller.listDevices()

        then:
        response.status == 200
    }

    void "test prepareBulkDownloadIcsrReports when queue is not full and data is less than configured value" (){
        given:
        params.data =  '["144626_20240400352_1", "144627_20240400353_1"]'
        def mockUserService=Mock(UserService)
        controller.userService=mockUserService
        User currentUser = new User(username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        int currentSize=5;
        mockUserService.getUser() >> currentUser
        for(int i=1; i<=currentSize; i++){
            BulkDownloadIcsrReports bulkDownloadIcsrReports = new BulkDownloadIcsrReports(id: i, downloadBy: currentUser, downloadData: 'abc')
            bulkDownloadIcsrReports.save(validate: false)
        }
        def mockCRUDService = Mock(CRUDService)
        controller.CRUDService = mockCRUDService
        def mockUtilService = Mock(UtilService)
        controller.utilService = mockUtilService
        mockCRUDService.save(_) >> {
            BulkDownloadIcsrReports bulkDownloadIcsrReports =  new BulkDownloadIcsrReports(id: currentSize+1, downloadBy: currentUser, downloadData: 'abc')
            bulkDownloadIcsrReports.save(validate: false)
            return bulkDownloadIcsrReports
        }
        when:
        controller.prepareBulkDownloadIcsrReports()

        then:
        BulkDownloadIcsrReports.findAll().size() == currentSize+1
    }

    void "test prepareBulkDownloadIcsrReports when queue is not full but data is more than configured value" (){
        given:
        params.data =  """['1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3',
                           '1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3','1_2_3',]"""
        def mockUserService=Mock(UserService)
        controller.userService=mockUserService
        User currentUser = new User(username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        mockUserService.getUser() >> currentUser
        Holders.config.icsr.profile.bulk.export.maxCount=10
        int currentSize=5;
        for(int i=1; i<=currentSize; i++){
            BulkDownloadIcsrReports bulkDownloadIcsrReports = new BulkDownloadIcsrReports(id: i, downloadBy: currentUser, downloadData: 'abc')
            bulkDownloadIcsrReports.save(validate: false)
        }
        def mockCRUDService = Mock(CRUDService)
        controller.CRUDService = mockCRUDService
        def mockUtilService = Mock(UtilService)
        controller.utilService = mockUtilService
        mockCRUDService.save(_) >> {
            BulkDownloadIcsrReports bulkDownloadIcsrReports =  new BulkDownloadIcsrReports(id: currentSize+1, downloadBy: currentUser, downloadData: 'abc')
            bulkDownloadIcsrReports.save(validate: false)
            return bulkDownloadIcsrReports
        }
        when:
        controller.prepareBulkDownloadIcsrReports()

        then:
        BulkDownloadIcsrReports.findAll().size() == currentSize+1
    }

    void "test prepareBulkDownloadIcsrReports when queue is full and data is less than configured value" (){
        given:
        params.data =  '["144626_20240400352_1", "144626_20240400352_1"]'
        def mockUserService=Mock(UserService)
        controller.userService=mockUserService
        User currentUser = new User(username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        mockUserService.getUser() >> currentUser
        int currentSize=30;
        for(int i=1; i<=currentSize; i++){
            BulkDownloadIcsrReports bulkDownloadIcsrReports = new BulkDownloadIcsrReports(id: i, downloadBy: currentUser, downloadData: 'abc')
            bulkDownloadIcsrReports.save(validate: false)
        }
        def mockCRUDService = Mock(CRUDService)
        controller.CRUDService = mockCRUDService
        def mockUtilService = Mock(UtilService)
        controller.utilService = mockUtilService
        mockCRUDService.save(_) >> {
            BulkDownloadIcsrReports bulkDownloadIcsrReports =  new BulkDownloadIcsrReports(id: currentSize+1, downloadBy: currentUser, downloadData: 'abc')
            bulkDownloadIcsrReports.save(validate: false)
            return bulkDownloadIcsrReports
        }
        when:
        controller.prepareBulkDownloadIcsrReports()

        then:
        BulkDownloadIcsrReports.findAll().size() == currentSize
    }
}
