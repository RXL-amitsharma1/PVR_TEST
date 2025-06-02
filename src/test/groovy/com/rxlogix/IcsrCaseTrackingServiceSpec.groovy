package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import groovy.sql.Sql
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges
import com.rxlogix.config.ExecutedIcsrProfileConfiguration
import com.rxlogix.util.ViewHelper
import com.rxlogix.util.DateUtil
import com.rxlogix.UserService

import java.time.LocalDateTime

@ConfineMetaClassChanges([User, ExecutedTemplateQuery, ReportConfiguration])
class IcsrCaseTrackingServiceSpec extends Specification implements DataTest, ServiceUnitTest<IcsrCaseTrackingService> {

    private SimpleDriverDataSource reportDataSourcePVR

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, ExecutedIcsrProfileConfiguration, UserRole, BulkDownloadIcsrReports, ExecutedTemplateQuery, ReportTemplate, ExecutedDateRangeInformation, ExecutedReportConfiguration, ExecutedDeliveryOption, ExecutedPeriodicReportConfiguration, ReportConfiguration, UnitConfiguration, IcsrOrganizationType, IcsrProfileConfiguration, Preference, Role, Tenant, IcsrCaseTracking
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
        normalUser.metaClass.isICSRAdmin = { -> return true }
        normalUser.metaClass.hasICSRActionRoles = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
        normalUser.metaClass.static.isDev = { -> return false }
        normalUser.preference.timeZone = "Asia/Kolkata"
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


    void "test prepareBulkDownload when download is succesful"() {
        given:
        def mockUtilService = Mock(UtilService)
        service.utilService = mockUtilService
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }

        Sql.metaClass.call = true
        int run = 0

        def downloadData = """{
        "downloadData": [
            {
                "caseNumber": "20240400352",
                "exIcsrTemplateQueryId": 144626,
                "versionNumber": 1
            }
        ]
    }"""

        User user = makeNormalUser("user", [])

        BulkDownloadIcsrReports bulkDownloadIcsrReports = new BulkDownloadIcsrReports(id: 1, downloadBy: user, downloadData: downloadData)
        bulkDownloadIcsrReports.save(validate: false, failOnError: true)

        ReportTemplate executedXMLTemplate = new ReportTemplate(
                name: "Test Template",
                templateType: TemplateTypeEnum.ICSR_XML,
                createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                owner: user
        )
        executedXMLTemplate.save(validate: false, failOnError: true)

        ExecutedDeliveryOption deliveryOption = new ExecutedDeliveryOption(
                attachmentFormats: [ReportFormatEnum.HTML]
        )
        deliveryOption.save(validate: false, failOnError: true)

        ExecutedIcsrProfileConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(
                executedDeliveryOption: deliveryOption,
                executedGlobalQueryValueLists: [],
                clazz: "class",
                reportName: "report_1",
                owner: user,
                createdBy: "user",
                modifiedBy: "user",
                signalConfiguration: false,
                tenantId: 1,
                recipientOrganizationName: "test"
        )
        executedReportConfiguration.save(validate: false, failOnError: true)

        ExecutedDateRangeInformation executedDateRangeInformation = new ExecutedDateRangeInformation(
                executedAsOfVersionDate: LocalDateTime.now(),
                dateRangeEnum: DateRangeEnum.CUMULATIVE
        )
        executedDateRangeInformation.save(validate: false, failOnError: true)

        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
                id: 144626L,
                executedTemplate: executedXMLTemplate,
                executedDateRangeInformationForTemplateQuery: executedDateRangeInformation,
                executedConfiguration: executedReportConfiguration,
                createdBy: "user",
                modifiedBy: "user"
        )
        executedTemplateQuery.save(validate: false, failOnError: true)

        // Link back-reference and save again
        executedDateRangeInformation.executedTemplateQuery = executedTemplateQuery
        executedDateRangeInformation.save(validate: false, failOnError: true)

        // Link template query to config and save
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(validate: false, failOnError: true)

        ExecutedTemplateQuery.metaClass.static.read = { Long id -> executedTemplateQuery }

        UnitConfiguration unitConfiguration = new UnitConfiguration(
                unitName: 'abc',
                unitType: UnitTypeEnum.BOTH,
                unitRegisteredId: '1',
                unitRetired: false,
                organizationType: new IcsrOrganizationType(org_name_id: 1, name: 'abc')
        )
        unitConfiguration.save(validate: false, failOnError: true)

        IcsrProfileConfiguration icsrProfileConfiguration = new IcsrProfileConfiguration(
                recipientOrganization: unitConfiguration,
                reportName: "report",
                senderOrganization: unitConfiguration
        )
        icsrProfileConfiguration.save(validate: false, failOnError: true)

        ReportConfiguration.metaClass.static.findByReportNameAndOwner = { String reportName, User owner ->
            return icsrProfileConfiguration
        }

        icsrProfileConfiguration.metaClass.isViewableBy = { User u ->
            return true
        }

        File xmlFile = new File("abc")
        xmlFile.createNewFile()

        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.createR3XMLReport(0..1) { ExecutedTemplateQuery etq, Boolean b, Map params ->
            return xmlFile
        }
        service.dynamicReportService = mockDynamicReportService.proxyInstance()

        def mockNotificationService = Mock(NotificationService)
        service.notificationService = mockNotificationService
        mockNotificationService.addNotification(_, _, _, _, _, _) >> { User user1, String message, String messageArgs, String notificationParameters, NotificationLevelEnum level, NotificationApp appName ->
            run++
            return true
        }

        when:
        service.prepareBulkDownload()

        then:
        run == 1
    }

    void "test prepareBulkDownload when download is failed"() {
        given:
        def mockUtilService = Mock(UtilService)
        service.utilService = mockUtilService
        mockUtilService.getReportConnectionForPVR() >> {
            return newConn()
        }
        int run = 0
        def downloadData = """{
        "downloadData": [
                {
                    "caseNumber": "20240400352",
                    "exIcsrTemplateQueryId": 144626,
                    "versionNumber": 1
                }
        ]
    }"""
        User user = makeNormalUser("user", [])
        BulkDownloadIcsrReports bulkDownloadIcsrReports = new BulkDownloadIcsrReports(id: 1, downloadBy: user, downloadData: downloadData)
        bulkDownloadIcsrReports.save(validate: false)
        ReportTemplate executedXMLTemplate = new ReportTemplate(name: "Test Template", templateType: TemplateTypeEnum.ICSR_XML, createdBy: 'createdBy', modifiedBy: 'modifiedBy', owner: user)
        executedXMLTemplate.save(validate: false, failOnEror: true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML]), executedGlobalQueryValueLists: [],
                clazz: "class", reportName: "report_1", owner: user, createdBy: "user", modifiedBy: "user",
                signalConfiguration: false, tenantId: 1, recipientOrganizationName: "test")
        ExecutedDateRangeInformation executedDateRangeInformation = new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(), dateRangeEnum: DateRangeEnum.CUMULATIVE)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(id: 144626L, executedTemplate: executedXMLTemplate, executedDateRangeInformationForTemplateQuery: executedDateRangeInformation,
                executedConfiguration: executedReportConfiguration, createdBy: "user", modifiedBy: "user")
        executedTemplateQuery.save(validate: false, failOnError: true)
        executedDateRangeInformation.executedTemplateQuery = executedTemplateQuery
        executedDateRangeInformation.save(failOnError: true, validate: false)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(failOnError: true, validate: false)
        ExecutedTemplateQuery.metaClass.static.read = { Long id -> executedTemplateQuery }
        UnitConfiguration unitConfiguration = new UnitConfiguration(unitName: 'abc', unitType: UnitTypeEnum.BOTH, unitRegisteredId: '1', unitRetired: false, organizationType: new IcsrOrganizationType(org_name_id: 1, name: 'abc'))
        unitConfiguration.save(validate: false)
        IcsrProfileConfiguration icsrProfileConfiguration = new IcsrProfileConfiguration(recipientOrganization: unitConfiguration, reportName: "report", senderOrganization: unitConfiguration)
        icsrProfileConfiguration.save(validate: false)
        ReportConfiguration.metaClass.static.findByReportNameAndOwner = { String reportName, User owner ->
            return icsrProfileConfiguration
        }
        icsrProfileConfiguration.metaClass.isViewableBy = { User u ->
            return true
        }
        File invalidXmlFile = new File("abc")
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.createR3XMLReport(0..1) { ExecutedTemplateQuery executedTemplateQuery1, Boolean b, Map params ->
            return invalidXmlFile.mkdir()
        }
        service.dynamicReportService = mockDynamicReportService.proxyInstance()
        def mockNotificationService = Mock(NotificationService)
        service.notificationService = mockNotificationService
        mockNotificationService.addNotification(_, _, _, _, _, _) >> { User user1, String message, String messageArgs, String notificationParameters, NotificationLevelEnum level, NotificationApp appName ->
            return true
        }
        when:
        service.prepareBulkDownload()

        then:
        run == 0
    }

    void "test createZipFile creates valid zip from directory"() {
        given:
        def tempDir = File.createTempDir()
        def file1 = new File(tempDir, "sample1.txt")
        file1.text = "Hello"
        def file2 = new File(tempDir, "sample2.txt")
        file2.text = "World"
        def zipFilePath = File.createTempFile("test-", ".zip")
        zipFilePath.delete()

        when:
        service.createZipFile(zipFilePath.absolutePath, tempDir)

        then:
        def zipFile = new java.util.zip.ZipFile(zipFilePath)
        def entries = zipFile.entries().collect { it.name }
        entries.containsAll(["sample1.txt", "sample2.txt"])
        zipFile.getInputStream(zipFile.getEntry("sample1.txt")).text == "Hello"
        zipFile.getInputStream(zipFile.getEntry("sample2.txt")).text == "World"

        cleanup:
        zipFilePath?.delete()
        file1?.delete()
        file2?.delete()
        tempDir?.deleteDir()
    }

    void "test toMap"() {
        User normalUser = makeNormalUser("user", [])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrProfileConfiguration(recipientOrganizationName: "organisation")
        executedReportConfiguration.save(failOnError: true, validate: false, flush: true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQuery: new SuperQuery(name: "super"), executedTemplate: new ReportTemplate(name: "template"))
        executedTemplateQuery.save(failOnError: true, validate: false, flush: true)
        when:
        def result = service.toMap(new IcsrCaseTracking(exIcsrProfileId: executedReportConfiguration.id, exIcsrTemplateQueryId: executedTemplateQuery.id, safetyReceiptDate: new Date(), caseNumber: "1", versionNumber: 1, caseReceiptDate: new Date(), productName: "product", eventPreferredTerm: "event", susar: "susar", profileName: "profile", generationDate: new Date(), submissionDate: new Date(), e2BStatus: "status"), 'en', normalUser)
        then:
        result.size() == 58
    }

    void "test generateActionList returns download only for non-admin"() {
        given:
        def icsrCaseTracking = new IcsrCaseTracking(
                e2BStatus: 'SUBMISSION_NOT_REQUIRED',
                flagLocalCpRequired: false,
                flagAutoGenerate: false,
                followupInfo: null
        )
        User normalUser = makeNormalUser("user", [])
        Locale locale = Locale.ENGLISH
        ViewHelper.getMessage(_, _, _, _) >> { args -> "Label for ${args[0]}" }

        when:
        def result = service.generateActionList(icsrCaseTracking, locale, normalUser)

        then:
        result.size() == 1
        result[0].id == "download"

    }

    void "getIndicator returns yellow when due date is within next 2 days and no submission date"() {
        given:
        def caseTracking = new IcsrCaseTracking(submissionDate: null)
        Date now = new Date()

        when:
        def result = service.getIndicator(caseTracking, now + 1)

        then:
        result == "yellow"
    }

    void "getIndicator returns red when due date is in past and no submission date"() {
        given:
        def caseTracking = new IcsrCaseTracking(submissionDate: null)
        Date now = new Date()

        when:
        def result = service.getIndicator(caseTracking, now - 1)

        then:
        result == "red"
    }

    void "getIndicator returns empty string when submission date exists regardless of due date"() {
        given:
        def caseTracking = new IcsrCaseTracking(submissionDate: new Date())
        Date now = new Date()

        when:
        def result = service.getIndicator(caseTracking, now + 1)

        then:
        result == ""
    }

    void "test toCaseSubmissionHistoryMap returns expected map"() {
        given:
        User normalUser = makeNormalUser("user", [])
        def mockUserService = Mock(UserService)
        mockUserService.getCurrentUser() >> {return normalUser}
        service.userService = mockUserService
        def now = new Date()
        def formattedDate = now.format("yyyy-MM-dd'T'HH:mm:ss")
        def icsrCaseSubmission = new IcsrCaseSubmission(
                caseNumber: "CASE123",
                versionNumber: 1,
                e2bStatus: "TRANSMITTED",
                lastUpdateDate: now,
                reportDestination: "FDA",
                exIcsrTemplateQueryId: 1001L,
                lastUpdatedBy: "TestUser",
                ackFileName: "ack.xml",
                comments: "Some comment",
                submissionDocument: new byte[2048],
                e2bProcessId: "PROC123",
                attachmentAckFileName: "ack_attach.xml",
                transmittedDate: now
        )
        DateUtil.metaClass.static.covertToDateWithTimeZone(_, _, _) >> now
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' ->
            return "Translated Status"
        }

        when:
        def result = service.toCaseSubmissionHistoryMap(icsrCaseSubmission, "Asia/Kolkata", "en")

        then:
        result.caseNumber == "CASE123"
        result.versionNumber == 1
        result.e2BStatus == "TRANSMITTED"
        result.reportDestination == "FDA"
        result.exIcsrTemplateQueryId == 1001L
        result.lastUpdatedBy == "TestUser"
        result.ackFileName == "ack.xml"
        result.comments == "Some comment"
        result.submissionDocument == true
        result.e2bProcessId == null
        result.attachmentAckFileName == "ack_attach.xml"
    }

    void "test fetchTimeZoneMessage returns correct label"() {
        given:
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null ->
            return "${code} (${params?.join(', ')})"
        }
        def inputTimeZoneId = "Asia/Kolkata"  // Replace with real enum value if different
        def expectedKey = TimeZoneEnum.values().find { it.timezoneId == inputTimeZoneId }?.getI18nKey()
        def expectedOffset = TimeZoneEnum.values().find { it.timezoneId == inputTimeZoneId }?.getGmtOffset()

        when:
        def result = service.fetchTimeZoneMessage(inputTimeZoneId)

        then:
        result == "${expectedKey} (${expectedOffset})"
    }

    void "test getTimezone returns expected timezone list"() {
        given:
        ViewHelper.metaClass.static.getMessage = { String i18nKey, Object gmtOffset = null ->
            return "${i18nKey} ${gmtOffset ?: ''}".trim()
        }
        List<Map> expected = TimeZoneEnum.values().collect {
            [id: it.name(), text: "${it.i18nKey} ${it.gmtOffset}".trim()]
        }
        when:
        def result = service.getTimezone()

        then:
        result == expected

        cleanup:
        GroovySystem.metaClassRegistry.removeMetaClass(ViewHelper)
    }

    void "test validateCaseDataForPreview returns false when isGenerated is true"() {
        given:
        def mockAckService = Mock(IcsrProfileAckService)
        service.icsrProfileAckService = mockAckService
        def mockTracking = Mock(IcsrCaseTracking)
        mockTracking.isGenerated >> true
        mockAckService.getIcsrTrackingRecord(1001L, "CASE123", 1) >> mockTracking

        when:
        def result = service.validateCaseDataForPreview(1001L, "CASE123", 1)

        then:
        result == false
    }

    void "test validateCaseDataForPreview returns true when isGenerated is false"() {
        given:
        def mockAckService = Mock(IcsrProfileAckService)
        service.icsrProfileAckService = mockAckService
        def mockTracking = Mock(IcsrCaseTracking)
        IcsrCaseTracking.isGenerated >> false
        mockAckService.getIcsrTrackingRecord(1002L, "CASE456", 2) >> mockTracking

        when:
        def result = service.validateCaseDataForPreview(1002L, "CASE456", 2)

        then:
        result == true
    }

}