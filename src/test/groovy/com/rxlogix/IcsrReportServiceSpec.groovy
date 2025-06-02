package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.user.UserRole
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import org.apache.commons.io.IOUtils
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges
import com.rxlogix.enums.WorkflowConfigurationTypeEnum

import java.nio.file.Files

@ConfineMetaClassChanges([XMLResultData, ViewHelper])
class IcsrReportServiceSpec extends Specification implements DataTest, ServiceUnitTest<IcsrReportService> {

    def setupSpec() {
        mockDomains XMLResultData, ExecutedIcsrTemplateQuery, IcsrCaseTracking, User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference, ExecutedIcsrReportConfiguration, WorkflowRule, WorkflowState
    }

    static doWithSpring = {
        dynamicReportService(DynamicReportService) {
            grailsApplication = Holders.grailsApplication
        }
        reportService(ReportService)
    }

    def setup() {
        Holders.grailsApplication.config.tempDirectory = System.getProperty("java.io.tmpdir")
    }

    def cleanup() {
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

    void "test get case numbers from icsr report"() {
        given: "A report result instance"
        ReportResult result = createXMLReportResult()
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.getCaseNumberAndVersions(1) { data ->
            return [new Tuple2<String, Integer>('17US00007667', 1233)]
        }
        service.reportService = mockReportService.proxyInstance()
        when: "Call the getCaseNumbers method"
        List<String> caseNumbers = service.getCaseNumbers(result)

        then: "It returns 1040 cases"
        caseNumbers != null
        caseNumbers.size() == 1
        caseNumbers.first() == "17US00007667"
    }

    void "test transfer R3xml file via email for xml"() {
        setup:
        User normalUser = makeNormalUser("user",[])
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{normalUser}
        service.userService=mockUserService
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "type" }
        File file = File.createTempFile('R12_', '_en_R3.xml')
        def userServiceMock = new MockFor(UserService)
        userServiceMock.demand.getCurrentUser(1) { ->
            return null
        }
        service.userService = userServiceMock.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.insertValues(1) { String str, def entity ->
            return str
        }

        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getContentType(1) { String extension ->
            if (extension != 'xml') {
                throw new Exception("Unexpected Extension type")
            }
            return extension
        }
        service.dynamicReportService = mockDynamicReportService.proxyInstance()

        mockEmailService.demand.sendEmailWithFiles(1) { def recipients, def emailCc, String emailSubject, String emailBodyMessage, boolean asyVal, List files ->
            if (!files.find { it.type == 'xml' && it.name == file.name }) {
                throw new Exception("file not found for email")
            }
        }
        service.emailService = mockEmailService.proxyInstance()

        when:
        ExecutedReportConfiguration configuration = new ExecutedIcsrProfileConfiguration(reportName: 'Test Report')
        String caseNumber = "121212"
        int versionNumber = 1
        EmailConfiguration emailConfiguration = new EmailConfiguration(to: 'sachin.verma@rxlogix.com', body: "test email")

        // Mock the missing parameters
        IcsrCaseTracking icsrCaseTracking = new IcsrCaseTracking()  // You may mock this further if needed
        ReportTemplate reportTemplate = new ReportTemplate()  // Mock if necessary
        SuperQuery query = new SuperQuery()  // Mock if necessary

        // Call method with all required parameters
        service.transferFileViaEmail(configuration, caseNumber, versionNumber, emailConfiguration, file, icsrCaseTracking, reportTemplate, query)

        then:
        noExceptionThrown()

        cleanup:
        file.delete()
    }


    void "test transfer R3xml file via email for pdf"() {
        setup:
        User normalUser = makeNormalUser("user",[])
        def mockUserService=Mock(UserService)
        mockUserService.getCurrentUser()>>{normalUser}
        service.userService=mockUserService
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> return "type" }
        File file = File.createTempFile('R12_', '_en.PDF')

        def userServiceMock = new MockFor(UserService)
        userServiceMock.demand.getCurrentUser(1) { ->
            return null
        }
        service.userService = userServiceMock.proxyInstance()

        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.insertValues(2) { String str, def entity ->
            return str
        }

        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getContentType(1) { String extension ->
            if (extension.toLowerCase() != 'pdf') {
                throw new Exception("Unexpected Extension type")
            }
            return extension
        }
        service.dynamicReportService = mockDynamicReportService.proxyInstance()

        mockEmailService.demand.sendEmailWithFiles(1) { def recipients, def emailCc, String emailSubject, String emailBodyMessage, boolean asyVal, List files ->
            if (!files.find { it.type.toLowerCase() == 'pdf' && it.name == file.name }) {
                throw new Exception("file not found for email")
            }
        }
        service.emailService = mockEmailService.proxyInstance()

        when:
        ExecutedReportConfiguration configuration = new ExecutedIcsrProfileConfiguration(reportName: 'Test Report')
        String caseNumber = "1212123"
        int versionNumber = 2
        EmailConfiguration emailConfiguration = new EmailConfiguration(to: 'sachin.verma@rxlogix.com', body: "test email", subject: "TEST Subject")

        // Mock the missing parameters
        IcsrCaseTracking icsrCaseTracking = new IcsrCaseTracking()
        ReportTemplate reportTemplate = new ReportTemplate()
        SuperQuery query = new SuperQuery()

        service.transferFileViaEmail(configuration, caseNumber, versionNumber, emailConfiguration, file, icsrCaseTracking, reportTemplate, query)

        then:
        noExceptionThrown()

        cleanup:
        file.delete()
    }

    void "test saveR3XMLFile"() {
        setup:
        File file = File.createTempFile('Test', '_en.R3XML')
        file.text = "Test Data"

        ExecutedIcsrTemplateQuery executedTemplateQuery = new ExecutedIcsrTemplateQuery().save(validate: false, failOnError: true, flush: true)

        XMLResultData xmlResultData = new XMLResultData()
        xmlResultData.executedTemplateQueryId = 12345
        xmlResultData.caseNumber = '2022101100'
        xmlResultData.versionNumber = 1
        xmlResultData.save(validate:false, failOnError: true, flush: true)

        XMLResultData.metaClass.static.findByExecutedTemplateQueryIdAndCaseNumberAndVersionNumber = { Long exIcsrTempQueryId, String s, Long v ->
            return xmlResultData
        }

        // Mock icsrProfileAckService and its method getIcsrTrackingRecord
        def icsrProfileAckServiceMock = Mock(IcsrProfileAckService)
        icsrProfileAckServiceMock.getIcsrTrackingRecord(_, _, _) >> {
            def trackingRecord = new IcsrCaseTracking(generationDate: new Date(), flagPmda: true)
            return trackingRecord
        }
        service.icsrProfileAckService = icsrProfileAckServiceMock

        when:
        service.saveR3XMLFile(file, executedTemplateQuery, '2022101100', 1, null)

        then:
        xmlResultData.value == Files.readAllBytes(file.toPath())
        xmlResultData.value != file.text
    }


/*
    void "test generate batch icsr report"() {
        given: "A report result instance"
        ReportResult result = createXMLReportResult()
        ReportResult.metaClass.static.get = {Serializable id -> return result }
        List<String> caseNumbers = service.getCaseNumbers(result)
        List<Tuple2<Long, String>> caseNumberPairs = [new Tuple2<>(0, caseNumbers.get(0)), new Tuple2<>(0, caseNumbers.get(1))]

        when: "Call the createBatchXMLReport method"
        File reportFile = service.createBatchXMLReport(caseNumberPairs)

        then: "It creates a non-empty file"
        reportFile != null
    }
*/
    private ReportResult createXMLReportResult() {
        String rootResourcePath = "icsr_report"
        ExecutedXMLTemplate executedTemplate = new ExecutedXMLTemplate()
        executedTemplate.nestedTemplates = []
        return createReportResult("${rootResourcePath}/data.tar.gz", executedTemplate)
    }

    private createReportResult(String resourcePath, ReportTemplate executedTemplate) {
        ReportResultData data = new ReportResultData()
        data.value = IOUtils.toByteArray(getClass().getResourceAsStream(resourcePath))

        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery()
        executedTemplateQuery.executedTemplate = executedTemplate

        return Stub(ReportResult) {
            getData() >> data
            getExecutedTemplateQuery() >> executedTemplateQuery
            getSourceProfile() >> new SourceProfile()
        }
    }

    void "test targetStatesAndApplications returns correct map"() {
        given:
        WorkflowState draftState = new WorkflowState(name: "Draft", isDeleted: false).save(validate: false)
        WorkflowState submittedState = new WorkflowState(name: "Submitted", isDeleted: false).save(validate: false)
        def reportConfig = new ExecutedIcsrReportConfiguration(id: 1001L).save(validate: false)
        def workflowRule = new WorkflowRule(
                id: 1L,
                name: "Submit Rule",
                configurationTypeEnum: WorkflowConfigurationTypeEnum.PERIODIC_REPORT,
                initialState: draftState,
                targetState: submittedState,
                isDeleted: false,
                needApproval: true
        ).save(validate: false)
        WorkflowState.metaClass.static.findByNameAndIsDeleted = { String name, boolean isDeleted ->
            return name == "Draft" ? draftState : null
        }
        ExecutedReportConfiguration.metaClass.static.findById = { Long id ->
            return id == 1001L ? reportConfig : null
        }
        WorkflowRule.metaClass.static.findAllByConfigurationTypeEnumAndInitialStateAndIsDeleted = {
            WorkflowConfigurationTypeEnum typeEnum, WorkflowState state, boolean isDeleted ->
                return (typeEnum == WorkflowConfigurationTypeEnum.PERIODIC_REPORT &&
                        state == draftState && !isDeleted) ? [workflowRule] : []
        }
        submittedState.metaClass.getReportActionsAsList = { -> ["submit", "review"] }

        when:
        def result = service.targetStatesAndApplications(1001L, "Draft")

        then:
        result.actions == ["Submitted": ["submit", "review"]]
        result.states.size() == 1
        result.states[0].name == "Submitted"
        result.rules == [1L: submittedState]
        result.needApproval == [1L: true]
    }
}
