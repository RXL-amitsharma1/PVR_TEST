package com.rxlogix


import com.rxlogix.config.*
import com.rxlogix.dto.CaseAckSubmissionDTO
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.gateway.AxwayService
import com.rxlogix.gateway.PVGatewayService
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.user.UserRole
import com.rxlogix.UserService
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import org.apache.commons.io.FileUtils
import org.grails.datastore.mapping.simple.SimpleMapDatastore
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges
import com.rxlogix.enums.TimeZoneEnum
import java.sql.SQLException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.sql.DataSource
import groovy.sql.Sql
import com.rxlogix.util.DateUtil
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import com.rxlogix.gateway.PVGatewayService

@ConfineMetaClassChanges([ViewHelper, XMLResultData, ExecutedIcsrTemplateQuery, ExecutedIcsrProfileConfiguration, ExecutedDeliveryOption, IcsrCaseTracking, ExecutedReportConfiguration, ExecutedTemplateQuery, SuperQuery, ReportTemplate])
class IcsrProfileAckServiceSpec extends Specification implements DataTest, ServiceUnitTest<IcsrProfileAckService>  {

    private SimpleDriverDataSource reportDataSourcePVR

    def setupSpec() {
        mockDomains IcsrProfileConfiguration, ExecutedIcsrTemplateQuery, XMLResultData, IcsrCaseTracking,User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference, ExecutedIcsrProfileConfiguration, ExecutedDeliveryOption, ReportTemplate, ExecutedTemplateQuery, SuperQuery, ExecutedIcsrReportConfiguration
        GrailsHibernateUtil.metaClass.static.unwrapIfProxy = { obj ->
            return obj
        }
    }

    def setup() {
        service.targetDatastore = new SimpleMapDatastore(['pva'], IcsrCaseSubmission)
        grailsApplication.config.icr.case.admin.emails = 'change@dummy.net'
        grailsApplication.config.icsr.case.ack.xsd = 'xsd/ICH_ICSR_Schema/multicacheschemas/MCCI_IN200101UV01.xsd'
        grailsApplication.config.icsr.case.ack.xslt = 'xslt/downgrade-ack-single.xsl'
        grailsApplication.config.icsr.case.ack.message.identifier.path = 'acknowledgment.messageacknowledgment.icsrmessagenumb.text()'
        grailsApplication.config.icsr.case.ack.message.status.path = 'acknowledgment.reportacknowledgment.reportacknowledgmentcode.text()'
        grailsApplication.config.icsr.case.ack.xslts.options.DEFAULT = 'xslt/downgrade-ack-single.xsl'
        grailsApplication.config.icsr.case.emdr.ack.local.report.numb.path = 'localReportNumber.text()'
        grailsApplication.config.icsr.case.emdr.ack.error.message.path = 'report.message.text()'
        grailsApplication.config.icsr.emdr.ack.status.identifier.path = 'status.text()'
        grailsApplication.config.icsr.case.emdr.ack.message.identifier.path = 'messageIdentifier.text()'
        grailsApplication.config.icsr.case.ack.local.message.number.path = 'localMessageNumber.text()'
        grailsApplication.config.icsr.case.ack.report.ack.code.path = 'reportAckCode.text()'
        grailsApplication.config.icsr.case.ack.transmission.ack.code.path = 'transmissionAckCode.text()'
        grailsApplication.config.icsr.case.ack.local.report.number.path = 'localReportNumber.text()'
        grailsApplication.config.icsr.case.ack.safety.report.id.path = 'safetyReportId.text()'
        grailsApplication.config.icsr.case.ack.icsr.message.number.path = 'icsrMessageNumber.text()'
        config.icsr.file.validate = true
    }

    def cleanup() {
        new File(System.getProperty("java.io.tmpdir")).eachFileMatch(~/non_existing_.*/) { file ->
            file.deleteDir()
        }
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

    void "check Ack File Handling for invalid"() {
        given:
        User normalUser = makeNormalUser("user",[])
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{normalUser}
        service.userService=mockUserService
        String incomingFolder = Files.createTempDirectory('Rx-Incoming').toString()
        service.grailsApplication = grailsApplication
        def userServiceMock = new MockFor(UserService)
        userServiceMock.demand.getCurrentUser(1) { ->
            return null
        }
        service.userService = userServiceMock.proxyInstance()
        def icsrXmlServiceMock = new MockFor(IcsrXmlService)
        icsrXmlServiceMock.demand.validateXml(1) { File file, String xsdPath ->
            return "Error in file "
        }
        service.icsrXmlService = icsrXmlServiceMock.proxyInstance()
        service.metaClass.getIcsrTrackingRecord = { Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber ->
            return new IcsrCaseTracking(caseNumber: '202302141424001', versionNumber: 1, exIcsrTemplateQueryId: 12345, processedReportId: 100100)
        }
        def emailServiceMock = new MockFor(EmailService)
        emailServiceMock.demand.sendEmailWithFiles(0..1) { def recipients, def emailCc, String emailSubject, String emailBodyMessage, boolean asyVal, List files ->
            return
        }
        service.emailService = emailServiceMock.proxyInstance()
        def e2bAttachmentServiceMock = new MockFor(E2BAttachmentService)
        e2bAttachmentServiceMock.demand.evaluatePathValue(0..1) { String xmlString, String path ->
            return null
        }
        service.e2BAttachmentService = e2bAttachmentServiceMock.proxyInstance()
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' ->
            return code
        }
        IcsrProfileConfiguration profileConfiguration = new IcsrProfileConfiguration(e2bDistributionChannel: new DistributionChannel(incomingFolder: incomingFolder))
        File ackFile = new File("${incomingFolder}/invalidAckFile.xml")
        ackFile.text = getClass().getResourceAsStream('invalidAckFile.xml').text
        when:
        service.readAckFileAndMarkStatus(profileConfiguration.e2bDistributionChannel.incomingFolder, ackFile)
        then:
        new File("${incomingFolder}/error/invalidAckFile.xml").exists()
        cleanup:
        FileUtils.forceDelete(new File(incomingFolder))
    }

    void "check Ack File Handling for valid for PVGateway"() {
        given:
        String incomingFolder = Files.createTempDirectory('Rx-Incoming').toString()
        Files.createDirectories(Paths.get("${incomingFolder}/archive"))
        service.grailsApplication = grailsApplication
        service.icsrXmlService = new IcsrXmlService()
        def reportExecutorServiceMock = Mock(ReportExecutorService)
        reportExecutorServiceMock.checkIfTransmitted(_,_) >> { return 1 }
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getTransmittedR3XmlFileName(0..1){ ReportResult reportResult, String caseNumber -> return ""}
        service.dynamicReportService = mockDynamicReportService.proxyInstance()
        def e2bAttachmentServiceMock = new MockFor(E2BAttachmentService)
        e2bAttachmentServiceMock.demand.evaluatePathValue(0..1) { String xmlString, String path ->
            return null
        }
        service.e2BAttachmentService = e2bAttachmentServiceMock.proxyInstance()

        def PVGatewayServiceMock = new MockFor(PVGatewayService)
        PVGatewayServiceMock.demand.getAckReceiveDateForFile(1){ String fileName ->
            return new Date()
        }
        service.PVGatewayService = (PVGatewayService) PVGatewayServiceMock.proxyInstance()
        reportExecutorServiceMock.changeIcsrCaseStatus(_) >> { return true}
        service.metaClass.getIcsrTrackingRecord = { Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber ->
            return new IcsrCaseTracking(caseNumber: '202302141424001', versionNumber: 1, exIcsrTemplateQueryId: 12345, processedReportId: 100100)
        }
        service.reportExecutorService = reportExecutorServiceMock
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' ->
            return code
        }
        ExecutedIcsrTemplateQuery.metaClass.static.read = { Long id -> new ExecutedIcsrTemplateQuery(executedConfiguration: new ExecutedIcsrProfileConfiguration(status: ReportExecutionStatusEnum.GENERATED_DRAFT),finalReportResult: new ReportResult(),draftReportResult: new ReportResult()) }
        IcsrProfileConfiguration profileConfiguration = new IcsrProfileConfiguration(e2bDistributionChannel: new DistributionChannel(incomingFolder: incomingFolder))
        def mockSqlGenerationService = new MockFor(SqlGenerationService)
        mockSqlGenerationService.demand.insertAckDetail(0..1) {Long tenantId, Long processedReportId, File acknowledgementFile -> return true}
        mockSqlGenerationService.demand.insertIntoGTTforAck(0..1) { CaseAckSubmissionDTO caseAckSubmissionDTO ->  return true}
        service.sqlGenerationService = mockSqlGenerationService.proxyInstance()
        File ackFile = new File("${incomingFolder}/validAckFile.xml")
        ackFile.text = getClass().getResourceAsStream('validAckFile.xml').text
        XMLResultData.metaClass.static.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber = {Long executedTemplateQueryId, String caseNumber, Long versionNumber ->
            return new XMLResultData(caseNumber: '202302141424001', versionNumber: 1, executedTemplateQueryId: 12345, isAttachmentExist: true)
        }
        service.targetDatastore = new SimpleMapDatastore(['pva'], IcsrCaseSubmission)
        IcsrCaseSubmission.metaClass.static.withNewSession = {Closure closure -> return true}
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.excludeAttachment(0..1) {IcsrCaseTracking icsrCaseTracking,  ExecutedIcsrTemplateQuery executedIcsrTemplateQuery, String status -> return true}
        service.icsrReportService = mockIcsrReportService.proxyInstance()
        when:
        Holders.config.icsr.case.ack.xslts.options=[FDA: 'xslt/NO_TRANSFORM.xsl', DEFAULT:'xslt/downgrade-ack-single.xsl']
        Holders.config.icsr.case.ack.xsd='xsd/ICH_ICSR_Schema/multicacheschemas/MCCI_IN200101UV01.xsd'
        service.readAckFileAndMarkStatus(profileConfiguration.e2bDistributionChannel.incomingFolder, ackFile)
        then:
        new File("${incomingFolder}/archive/validAckFile.xml").exists()
        cleanup:
        FileUtils.forceDelete(new File(incomingFolder))
    }

    void "check Ack File Handling for valid for Axway"() {
        given:
        String incomingFolder = Files.createTempDirectory('Rx-Incoming').toString()
        Files.createDirectories(Paths.get("${incomingFolder}/archive"))
        service.grailsApplication = grailsApplication
        service.icsrXmlService = new IcsrXmlService()
        def reportExecutorServiceMock = Mock(ReportExecutorService)
        reportExecutorServiceMock.checkIfTransmitted(_,_) >> { return 1 }
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getTransmittedR3XmlFileName(0..1){ ReportResult reportResult, String caseNumber -> return ""}
        service.dynamicReportService = mockDynamicReportService.proxyInstance()
        def e2bAttachmentServiceMock = new MockFor(E2BAttachmentService)
        e2bAttachmentServiceMock.demand.evaluatePathValue(0..1) { String xmlString, String path ->
            return null
        }
        service.e2BAttachmentService = e2bAttachmentServiceMock.proxyInstance()

        def AxwayServiceMock = new MockFor(AxwayService)
        AxwayServiceMock.demand.getAckReceiveDateForFile(1){ String fileName ->
            return new Date()
        }
        service.AxwayService = (AxwayService) AxwayServiceMock.proxyInstance()
        reportExecutorServiceMock.changeIcsrCaseStatus(_) >> { return true}
        service.metaClass.getIcsrTrackingRecord = { Long exIcsrTemplateQueryId, String caseNumber, Long versionNumber ->
            return new IcsrCaseTracking(caseNumber: '202302141424001', versionNumber: 1, exIcsrTemplateQueryId: 12345, processedReportId: 100100)
        }
        service.reportExecutorService = reportExecutorServiceMock
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' ->
            return code
        }
        ExecutedIcsrTemplateQuery.metaClass.static.read = { Long id -> new ExecutedIcsrTemplateQuery(executedConfiguration: new ExecutedIcsrProfileConfiguration(status: ReportExecutionStatusEnum.GENERATED_DRAFT),finalReportResult: new ReportResult(),draftReportResult: new ReportResult()) }
        IcsrProfileConfiguration profileConfiguration = new IcsrProfileConfiguration(e2bDistributionChannel: new DistributionChannel(incomingFolder: incomingFolder))
        def mockSqlGenerationService = new MockFor(SqlGenerationService)
        mockSqlGenerationService.demand.insertAckDetail(0..1) {Long tenantId, Long processedReportId, File acknowledgementFile -> return true}
        mockSqlGenerationService.demand.insertIntoGTTforAck(0..1) { CaseAckSubmissionDTO caseAckSubmissionDTO ->  return true}
        service.sqlGenerationService = mockSqlGenerationService.proxyInstance()
        File ackFile = new File("${incomingFolder}/validAckFile.xml")
        ackFile.text = getClass().getResourceAsStream('validAckFile.xml').text
        service.targetDatastore = new SimpleMapDatastore(['pva'], IcsrCaseSubmission)
        IcsrCaseSubmission.metaClass.static.withNewSession = {Closure closure -> return true}
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.excludeAttachment(0..1) {IcsrCaseTracking icsrCaseTracking,  ExecutedIcsrTemplateQuery executedIcsrTemplateQuery, String status -> return true}
        service.icsrReportService = mockIcsrReportService.proxyInstance()
        when:
        Holders.config.icsr.case.ack.xslts.options=[FDA: 'xslt/NO_TRANSFORM.xsl', DEFAULT:'xslt/downgrade-ack-single.xsl']
        Holders.config.icsr.case.ack.xsd='xsd/ICH_ICSR_Schema/multicacheschemas/MCCI_IN200101UV01.xsd'
        service.readAckFileAndMarkStatus(profileConfiguration.e2bDistributionChannel.incomingFolder, ackFile)
        then:
        new File("${incomingFolder}/archive/validAckFile.xml").exists()
        cleanup:
        FileUtils.forceDelete(new File(incomingFolder))
    }


    void "check ack execution for profile for finding valid files"() {
        given:
        String incomingFolder = Files.createTempDirectory('Rx-Incoming').toString()
        service.grailsApplication = grailsApplication
        def icsrXmlServiceMock = new MockFor(IcsrXmlService)
        icsrXmlServiceMock.demand.validateXml(1) { File file, String xsdPath ->
            return "Error in file "
        }
        service.icsrXmlService = icsrXmlServiceMock.proxyInstance()
        def emailServiceMock = new MockFor(EmailService)
        emailServiceMock.demand.sendEmailWithFiles(0..1) { def recipients, def emailCc, String emailSubject, String emailBodyMessage, boolean asyVal, List files ->
            return
        }
        service.emailService = emailServiceMock.proxyInstance()
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' ->
            return code
        }
        IcsrProfileConfiguration profileConfiguration = new IcsrProfileConfiguration(e2bDistributionChannel: new DistributionChannel(incomingFolder: incomingFolder))
        File ackFile = new File("${incomingFolder}/invalidAckFile.xml")
        ackFile.text = getClass().getResourceAsStream('invalidAckFile.xml').text
        Boolean modifiedFile = ackFile.setLastModified(ackFile.lastModified()-120000) //Added to make past date file
        when:
        service.executeAckFiles(profileConfiguration.e2bDistributionChannel.incomingFolder)
        then:
        (new File("${incomingFolder}/error/invalidAckFile.xml").exists() || !modifiedFile) //TODO made this to make sure date modified then applied.
        cleanup:
        FileUtils.forceDelete(new File(incomingFolder))
    }

    void 'check identifier and ack from downgrade ack file'(){
        when:
            String downgradeXml = """<?xml version="1.0" encoding="UTF-8"?>
<ichicsrack lang="en">
   <ichicsrmessageheader>
      <messagetype>ichicsrack</messagetype>
      <messageformatversion>2.1</messageformatversion>
      <messageformatrelease>1.0</messageformatrelease>
      <messagenumb>89JP00013606</messagenumb>
      <messagesenderidentifier>ZZ_FDA_OCP</messagesenderidentifier>
      <messagereceiveridentifier>Rx_Logix_FDA_OCP</messagereceiveridentifier>
      <messagedateformat>204</messagedateformat>
      <messagedate>20200123082058</messagedate>
   </ichicsrmessageheader>
   <acknowledgment>
      <messageacknowledgment>
         <icsrmessagenumb>1234-89JP00013606-1</icsrmessagenumb>
         <localmessagenumb>89US00013606</localmessagenumb>
         <icsrmessagesenderidentifier>1</icsrmessagesenderidentifier>
         <icsrmessagereceiveridentifier>1</icsrmessagereceiveridentifier>
         <icsrmessagedateformat>204</icsrmessagedateformat>
         <icsrmessagedate>20200123082058</icsrmessagedate>
         <transmissionacknowledgmentcode>01</transmissionacknowledgmentcode>
         <parsingerrormessage>None</parsingerrormessage>
      </messageacknowledgment>
      <reportacknowledgment>
         <safetyreportid>-Rx_Logix_FDA_OCP-89JP00013606</safetyreportid>
         <localreportnumb>13US000192</localreportnumb>
         <receiptdateformat>102</receiptdateformat>
         <receiptdate>20200123</receiptdate>
         <reportacknowledgmentcode>01</reportacknowledgmentcode>
         <errormessagecomment>Failure loaded the case into the receiver Database and the Case Number created is 89US00013606
                </errormessagecomment>
      </reportacknowledgment>
   </acknowledgment>
</ichicsrack>"""
            def rootNode = new XmlParser().parseText(downgradeXml)
        then:
           '1234-89JP00013606-1' == service.getMessageIdentifier(rootNode)
           '01' == service.getAckStatus(rootNode)
    }

    def "test moveExistingFile retries and eventually succeeds"() {
        given:
        Path dummyPath = Paths.get("some/path/to/file.xml")
        def callCount = 0
        GroovyMock(Files, global: true)
        Files.move(_, _) >> { Path src, Path dest ->
            callCount++
            if (callCount == 1) {
                throw new FileAlreadyExistsException("File already exists")
            }
            return dest
        }

        when:
        service.moveExistingFile(dummyPath)

        then:
        callCount == 2
    }

    def "test moveFile - moves file to destination folder"() {
        given:
        File tempSourceFile = File.createTempFile("testfile", ".txt")
        tempSourceFile.text = "sample content"
        String tempDestFolder = Files.createTempDirectory("destination").toString()

        and: "Mock moveExistingFile to avoid actual rename logic during test"
        service.metaClass.moveExistingFile = { Path p ->
        }

        when:
        service.moveFile(tempDestFolder, tempSourceFile)

        then:
        def movedFile = Paths.get(tempDestFolder, tempSourceFile.name).toFile()
        movedFile.exists()
        movedFile.text == "sample content"
    }

    def "test moveFile - creates destination folder if not exists"() {
        given:
        File tempSourceFile = File.createTempFile("testfile2", ".txt")
        tempSourceFile.text = "folder creation test"
        String tempDestFolder = new File(System.getProperty("java.io.tmpdir"), "non_existing_" + System.currentTimeMillis()).absolutePath

        and:
        service.metaClass.moveExistingFile = { Path p -> }

        when:
        service.moveFile(tempDestFolder, tempSourceFile)

        then:
        new File(tempDestFolder).exists()
        new File(tempDestFolder, tempSourceFile.name).text == "folder creation test"
    }

    void "test getDateWithTimeZone method with and without timezone"() {
        given:
        def date = Date.parse("yyyy-MM-dd HH:mm:ss", "2025-05-13 10:30:00")

        when: "timezone is explicitly provided"
        def resultWithTz = service.getDateWithTimeZone(date, "Asia/Kolkata")

        then:
        resultWithTz == "05/13/2025 10:30:00 (Asia/Kolkata)"

        when: "timezone is null (should default to TZ_330)"
        def resultWithDefaultTz = service.getDateWithTimeZone(date, null)

        then:
        resultWithDefaultTz == "05/13/2025 10:30:00 (${TimeZoneEnum.TZ_330.getTimezoneId()})"

        when: "date is null"
        def resultNullDate = service.getDateWithTimeZone(null, "Asia/Kolkata")

        then:
        resultNullDate == null
    }

    def "test getLicenseIdApprovalNumberApprovalTypeId with valid authId using MockFor Sql"() {
        given:
        service.dataSource_pva = Mock(DataSource)
        def mockSql = new MockFor(Sql)
        mockSql.demand.rows(1..1) { String query, List params ->
            return [[
                            LICENSE_ID: "LIC001",
                            APPROVAL_NUMBER: "APP123",
                            APPROVAL_TYPE_ID: "TYPE456"
                    ]]
        }
        mockSql.demand.close(0..1) { -> }

        when:
        Map result
        mockSql.use {
            result = service.getLicenseIdApprovalNumberApprovalTypeId(100L)
        }

        then:
        result.licenseId == "LIC001"
        result.approvalNumber == "APP123"
        result.approvalTypeId == "TYPE456"
    }

    def "test getLicenseIdApprovalNumberApprovalTypeId with null authId"() {
        when:
        def result = service.getLicenseIdApprovalNumberApprovalTypeId(null)

        then:
        result.licenseId == null
        result.approvalNumber == null
        result.approvalTypeId == null
    }

    def "test getLicenseIdApprovalNumberApprovalTypeId with SQL exception using MockFor"() {
        given:
        service.dataSource_pva = Mock(DataSource)
        def mockSql = new MockFor(Sql)
        mockSql.demand.rows(1..1) { String query, List params ->
            throw new SQLException("Simulated DB failure")
        }
        mockSql.demand.close(0..1) { -> }

        when:
        Map result
        mockSql.use {
            result = service.getLicenseIdApprovalNumberApprovalTypeId(101L)
        }

        then:
        result.licenseId == null
        result.approvalNumber == null
        result.approvalTypeId == null
    }

    void "validateSubmissionDate returns false for invalid date or null"() {
        given:
        Date invalidDate = null
        Date generationDate = new Date()
        String timeZoneId = "Asia/Kolkata"
        GroovyMock(DateUtil, global: true)
        DateUtil.covertToDateWithTimeZone(_, _, _) >> { Date date, String format, String tz -> return date }

        when:
        boolean result = service.validateSubmissionDate(invalidDate, generationDate, timeZoneId)

        then:
        !result
    }

    void "validateSubmissionDate returns false for SUBMISSION_NOT_REQUIRED state"() {
        given:
        Date submissionDate = new Date()
        Date generationDate = new Date() - 1
        String timeZoneId = "Asia/Kolkata"
        GroovyMock(DateUtil, global: true)
        DateUtil.covertToDateWithTimeZone(_, _, _) >> { Date date, String format, String tz -> return date }

        when:
        boolean result = service.validateSubmissionDate(submissionDate, generationDate, timeZoneId, "SUBMISSION_NOT_REQUIRED")

        then:
        !result
    }

    void "test getIcsrTrackingRecord returns correct tracking record"() {
        given:
        Long exIcsrTemplateQueryId = 1001L
        String caseNumber = "CASE-789"
        Long versionNumber = 2L
        IcsrCaseTracking expectedTracking = new IcsrCaseTracking(
                exIcsrTemplateQueryId: exIcsrTemplateQueryId,
                caseNumber: caseNumber,
                versionNumber: versionNumber
        )
        GroovyMock(IcsrCaseTracking, global: true)
        IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(exIcsrTemplateQueryId, caseNumber, versionNumber) >> expectedTracking

        when:
        def result = service.getIcsrTrackingRecord(exIcsrTemplateQueryId, caseNumber, versionNumber)

        then:
        result == expectedTracking
    }

    void "test getIcsrTrackingRecordByProcessedReportId returns correct tracking record"() {
        given:
        Long processedReportId = 999L
        String caseNumber = "CASE-001"
        Long versionNumber = 1L
        GroovyMock(IcsrCaseTracking, global: true)
        def expectedRecord = new IcsrCaseTracking(processedReportId: processedReportId, caseNumber: caseNumber, versionNumber: versionNumber)
        IcsrCaseTracking.findByProcessedReportIdAndCaseNumberAndVersionNumber(processedReportId, caseNumber, versionNumber) >> expectedRecord

        when:
        def result = service.getIcsrTrackingRecordByProcessedReportId(processedReportId, caseNumber, versionNumber)

        then:
        result == expectedRecord
    }

    void "test getIcsrTrackingRecordWithoutPva returns correct tracking record"() {
        given:
        def mockTrackingRecord = new IcsrCaseTracking(
                exIcsrTemplateQueryId: 1001L,
                caseNumber: "CASE-789",
                versionNumber: 1
        )
        GroovyMock(IcsrCaseTracking, global: true)
        IcsrCaseTracking.findByExIcsrTemplateQueryIdAndCaseNumberAndVersionNumber(1001L, "CASE-789", 1) >> mockTrackingRecord

        when:
        def result = service.getIcsrTrackingRecordWithoutPva(1001L, "CASE-789", 1)

        then:
        result == mockTrackingRecord
    }

    void "test icsrCaseTrackingMapForAuditLog with valid IcsrCaseTracking data"() {
        given:
        def icsr = new IcsrCaseTracking(
                id: 1L,
                caseNumber: "CASE001",
                versionNumber: "1.0",
                caseReceiptDate: Date.parse("yyyy-MM-dd", "2024-01-01"),
                safetyReceiptDate: Date.parse("yyyy-MM-dd", "2024-01-02"),
                productName: "ProductX",
                eventPreferredTerm: "Headache",
                susar: true,
                recipient: "RecipientX",
                profileName: "ProfileA",
                exIcsrProfileId: 12L,
                exIcsrTemplateQueryId: 10L,
                queryId: 11L,
                dueDate: Date.parse("yyyy-MM-dd", "2024-01-15"),
                scheduledDate: Date.parse("yyyy-MM-dd", "2024-01-05"),
                generationDate: Date.parse("yyyy-MM-dd", "2024-01-10"),
                submissionDate: Date.parse("yyyy-MM-dd", "2024-01-12"),
                e2BStatus: "SUBMITTED",
                dueInDays: 14,
                followupNumber: 2,
                localReportMessage: "Message",
                transmissionDate: Date.parse("yyyy-MM-dd", "2024-01-16"),
                modifiedDate: Date.parse("yyyy-MM-dd", "2024-01-17"),
                followupInfo: "Additional",
                awareDate: Date.parse("yyyy-MM-dd", "2024-01-03"),
                ackFileName: "ack.xml",
                isGenerated: true,
                caseId: 99L,
                flagLocalCpRequired: true,
                flagAutoGenerate: true,
                submissionFormDesc: "FormDesc",
                processedReportId: 123456L,
                prodHashCode: "ABC123",
                preferredDateTime: Date.parse("yyyy-MM-dd", "2024-01-18"),
                timeZoneOffset: "Asia/Kolkata",
                profileId: 42L
        ).save(validate: false)
        ExecutedReportConfiguration.metaClass.static.read = {
            Long id -> new ExecutedIcsrProfileConfiguration(
                    recipientOrganizationName: "OrgX",
                    preferredTimeZone: "Asia/Kolkata"
            )
        }
        SuperQuery.metaClass.static.read = {
            Long id -> new SuperQuery(name: "QueryTitle")
        }
        def reportTemplate = new ReportTemplate(name: "CIOMS", id: 123L, originalTemplateId: 999L)
        reportTemplate.metaClass.isCiomsITemplate = { -> true }
        reportTemplate.metaClass.isMedWatchTemplate = { -> false }
        ExecutedTemplateQuery.metaClass.static.read = {
            Long id -> new ExecutedTemplateQuery(executedTemplate: reportTemplate)
        }

        when:
        def result = service.icsrCaseTrackingMapForAuditLog(icsr)

        then:
        result['caseNumber'] == "CASE001"
        result['queryName'] == "QueryTitle"
        result['reportForm'] == "CIOMS"
        result['report'] == true
        result['recipient'] == "RecipientX"
        result['preferredTimeZone'] == "Asia/Kolkata"
        result['isGenerated'] == true
        result['templateId'] == 999L
    }

    void "test moveFileToErrorAndNotify moves file to destination folder"() {
        given:
        def service = new IcsrProfileAckService()
        def tempDir = System.getProperty("java.io.tmpdir")
        def sourceFile = new File(tempDir, "ackFile_test.txt")
        sourceFile.text = "Dummy ACK file content"
        def destinationFolder = tempDir + "/ackError"
        def expectedDestinationFile = new File(destinationFolder, sourceFile.name)
        service.metaClass.sendIcsrAckFileFailureEmailTo = { File file ->
            assert file.exists()
            assert file.name == sourceFile.name
        }
        if (expectedDestinationFile.exists()) expectedDestinationFile.delete()

        when:
        service.moveFileToErrorAndNotify(destinationFolder, sourceFile)

        then:
        expectedDestinationFile.exists()

        cleanup:
        expectedDestinationFile?.delete()
        new File(destinationFolder)?.deleteDir()
    }

    void "should downgrade ACK file using default XSLT path"() {
        given:
        def testDir = new File("test/unit")
        testDir.mkdirs()
        def testFile = new File(testDir, "test-ack.xml")
        testFile.text = "<xml></xml>"
        def expectedResultFile = new File(testDir, "test-ack_r2.xml")
        service.icsrXmlService = Mock(IcsrXmlService) {
            1 * transform(testFile, 'xslt/downgrade-ack-single.xsl', _) >> expectedResultFile
        }

        when:
        def result = service.downgradeAckFile(testFile, testDir.absolutePath)

        then:
        result == expectedResultFile

        cleanup:
        testFile.delete()
        expectedResultFile.delete()
    }

    void "should return local report number using configured xpath"() {
        given:
        def xmlText = '''
        <report>
            <localReportNumber>ABC123</localReportNumber>
        </report>
    '''
        def rootNode = new XmlParser().parseText(xmlText)

        when:
        String result = service.getEmdrLocalReportNumber(rootNode)

        then:
        result == 'ABC123'
    }

    void "should return error message using configured xpath"() {
        given:
        def xml = '''
        <ack>
            <report>
                <message>Error: Invalid report format</message>
            </report>
        </ack>
    '''
        def rootNode = new XmlParser().parseText(xml)

        when:
        def result = service.getEmdrAckErrorMssg(rootNode)

        then:
        result == 'Error: Invalid report format'
    }

    void "should return ACK status using configured xpath"() {
        given:
        def xmlString = '''
        <acknowledgement>
            <status>ACCEPTED</status>
        </acknowledgement>
    '''
        def rootNode = new XmlParser().parseText(xmlString)

        when:
        String result = service.getEmdrAckStatus(rootNode)

        then:
        result == 'ACCEPTED'
    }

    void "should return message identifier using configured xpath"() {
        given:
        def xml = '''
        <report>
            <messageIdentifier>MSG-789</messageIdentifier>
        </report>
    '''
        Node rootXmlNode = new XmlParser().parseText(xml)

        when:
        String result = service.getEmdrMessageIdentifier(rootXmlNode)

        then:
        result == 'MSG-789'
    }

    void "should return local message number using configured xpath"() {
        given:
        def xmlString = '''
        <root>
            <localMessageNumber>MSG456</localMessageNumber>
        </root>
    '''
        def rootNode = new XmlParser().parseText(xmlString)

        when:
        def result = service.getLocalMessageNumber(rootNode)

        then:
        result == 'MSG456'
    }

    void "should return report ACK code using configured xpath"() {
        given:
        def xmlString = '''
        <root>
            <reportAckCode>RACK001</reportAckCode>
        </root>
    '''
        def rootNode = new XmlParser().parseText(xmlString)

        when:
        def result = service.getReportAckCode(rootNode)

        then:
        result == 'RACK001'
    }

    void "should return transmission ACK code using configured xpath"() {
        given:
        def xmlString = '''
        <root>
            <transmissionAckCode>TACK002</transmissionAckCode>
        </root>
    '''
        def rootNode = new XmlParser().parseText(xmlString)

        when:
        def result = service.getTransmissionAckCode(rootNode)

        then:
        result == 'TACK002'
    }

    void "should return local report number using configured xpath"() {
        given:
        def xmlString = '''
        <root>
            <localReportNumber>LRN123</localReportNumber>
        </root>
    '''
        def rootNode = new XmlParser().parseText(xmlString)

        when:
        def result = service.getLocalReportNumber(rootNode)

        then:
        result == 'LRN123'
    }

    void "should return safety report ID using configured xpath"() {
        given:
        def xmlString = '''
        <root>
            <safetyReportId>SRID789</safetyReportId>
        </root>
    '''
        def rootNode = new XmlParser().parseText(xmlString)

        when:
        def result = service.getSafetyReportId(rootNode)

        then:
        result == 'SRID789'
    }

    void "should return ICSR message number using configured xpath"() {
        given:
        def xmlString = '''
        <root>
            <icsrMessageNumber>IMN555</icsrMessageNumber>
        </root>
    '''
        def rootNode = new XmlParser().parseText(xmlString)

        when:
        def result = service.getIcsrMessageNumber(rootNode)

        then:
        result == 'IMN555'
    }

    void "should return error message from reportacknowledgment if present"() {
        given:
        def xmlContent = '''
        <root>
            <acknowledgment>
                <reportacknowledgment>
                    <errormessagecomment>Error from report acknowledgment</errormessagecomment>
                </reportacknowledgment>
                <messageacknowledgment>
                    <parsingerrormessage>Error from message acknowledgment</parsingerrormessage>
                </messageacknowledgment>
            </acknowledgment>
        </root>
    '''
        def rootNode = new XmlParser().parseText(xmlContent)

        when:
        def result = service.getErrorMsgForApplicationRejected(rootNode)

        then:
        result == "Error from report acknowledgment"
    }

    void "should return ACK status using configured xpath"() {
        given:
        grailsApplication.config.icsr.case.ack.message.status.path = 'messageacknowledgment.acknowledgmentcode.text()'

        def xml = """
        <acknowledgment>
            <messageacknowledgment>
                <acknowledgmentcode>ACCEPTED</acknowledgmentcode>
            </messageacknowledgment>
        </acknowledgment>
    """
        def node = new XmlParser().parseText(xml)

        when:
        def result = service.getAckStatus(node)

        then:
        result == "ACCEPTED"
    }

    void "should return message identifier using configured xpath"() {
        given:
        grailsApplication.config.icsr.case.ack.message.identifier.path = 'messageacknowledgment.messagenumb.text()'

        def xml = """
        <acknowledgment>
            <messageacknowledgment>
                <messagenumb>MSG123</messagenumb>
            </messageacknowledgment>
        </acknowledgment>
    """
        def node = new XmlParser().parseText(xml)

        when:
        def result = service.getMessageIdentifier(node)

        then:
        result == "MSG123"
    }


    void "should return case number from reportacknowledgment"() {
        given:
        def xml = """
        <root>
            <acknowledgment>
                <reportacknowledgment>
                    <localreportnumb>CASE456</localreportnumb>
                </reportacknowledgment>
            </acknowledgment>
        </root>
    """
        def node = new XmlParser().parseText(xml)

        when:
        def result = service.getCaseNumber(node)

        then:
        result == "CASE456"
    }

    void "should return error message from reportacknowledgment"() {
        given:
        def xml = """
        <root>
            <acknowledgment>
                <reportacknowledgment>
                    <errormessagecomment>Some error occurred</errormessagecomment>
                </reportacknowledgment>
            </acknowledgment>
        </root>
    """
        def node = new XmlParser().parseText(xml)

        when:
        def result = service.getErrorMsg(node)

        then:
        result == "Some error occurred"
    }

    void "should return only files older than 1 minute from folder"() {
        given:
        File testDir = new File("test/unit/tmpFolder")
        testDir.mkdirs()
        File oldFile = new File(testDir, "oldFile.xml")
        oldFile.text = "<test>old</test>"
        oldFile.setLastModified(System.currentTimeMillis() - (2 * 60 * 1000)) // 2 minutes ago
        File newFile = new File(testDir, "newFile.xml")
        newFile.text = "<test>new</test>"
        newFile.setLastModified(System.currentTimeMillis()) // now

        when:
        List<File> result = service.getFilesFromFolder(testDir.path)

        then:
        result.size() == 1
        result[0].name == "oldFile.xml"

        cleanup:
        oldFile.delete()
        newFile.delete()
        testDir.delete()
    }

    void "should fetch intake case id based on caseId, versionNumber, and tenantId"() {
        given:
        def mockUtilService = Mock(UtilService)
        mockUtilService.getReportConnection() >> {
            return newConn() // your helper method
        }
        service.utilService = mockUtilService
        Sql.metaClass.call = true
        Sql.metaClass.firstRow = { String query, List params ->
            return ["INTAKE_CASE_ID": 12345L]
        }
        Sql.metaClass.close = { -> }

        when:
        Long result = service.fetchIntakeCaseId(1001L, 1L, 101L)

        then:
        result == 12345L
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

}
