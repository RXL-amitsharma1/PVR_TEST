package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.customException.CustomJasperException
import com.rxlogix.enums.*
import com.rxlogix.user.*
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import net.sf.dynamicreports.report.exception.DRException
import org.springframework.security.core.GrantedAuthority
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([MiscUtil, AuditLogConfigUtil, ReportResult, ExecutedReportConfiguration, SpringSecurityUtils, Tenants])
class ReportControllerSpec extends Specification implements DataTest, ControllerUnitTest<ReportController> {

    def setupSpec() {
        mockDomains WorkflowRule,User, Role, UserRole,UserGroup, UserGroupUser, Tenant,Preference, WorkflowState,ExecutedReportConfiguration,DateRangeType,ExecutedConfiguration,ExecutedDateRangeInformation,ExecutedDeliveryOption,ExecutedPeriodicReportConfiguration,ExecutedQueryValueList,ExecutedTemplateQuery,ExecutedTemplateValueList,ReportResult,ReportTemplate,SourceProfile,Tenant,WorkflowState,EmailConfiguration,DmsConfiguration, ReportConfiguration,CustomSQLTemplate, ExecutionStatus, IcsrProfileConfiguration, FieldProfile, IcsrCaseTracking
        AuditLogConfigUtil.metaClass.static.logChanges = {domain, Map newMap, Map oldMap, String eventName, String extraValue ="" , String transactionId = "" -> }
    }

    private makeSecurityService(User user) {
        def securityMock = new MockFor(UserService)
        securityMock.demand.getCurrentUser(0..1) { -> user }
        securityMock.demand.getAllowedSharedWithUsersForCurrentUser { -> [user] }
        securityMock.demand.getAllowedSharedWithGroupsForCurrentUser { -> [] }
        securityMock.demand.isCurrentUserDev(0..2) { false }
        return securityMock.proxyInstance()
    }

    private User makeNormalUser(name, team, String email = null) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_DEV', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user",email: email?:"abc@gmail.com")
        normalUser.addToTenants(tenant)
        normalUser.save(validate: false)
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

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user").save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        return adminUser
    }

    def setup() {
        ViewHelper.metaClass.static.getMessage = { String messageKey ->
            if (messageKey == 'default.date.format.long.tz') {
                return 'dd-MMM-yyyy hh:mm:ss a z'
            }
            return messageKey
        }
        Holders.config.pvreports.show.max.html=1_000 // Used to prevent HTML generation for large reports
    }

    def cleanup() {
    }

    void "Test Index"(){
        when:
        controller.index()
        then:
        response.status == 200
    }

    void "Test icsr"(){
        when:
        controller.icsr()
        then:
        response.status == 200
    }

    void "Test showIcsrReport"(){
        when:
        params.id == 1L
        controller.showIcsrReport()
        then:
        response.status == 200
    }

    void "test renderReportOutputType"() {
        given:
        ExecutedReportConfiguration executedConfiguration = new ExecutedConfiguration()
        File reportFile = File.createTempFile("temp", "")
        reportFile.write("hello world!")
        def mockDynamicReportService =new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getReportNameAsFileName { ExecutedReportConfiguration configuration, ExecutedTemplateQuery templtQuery ->
                return "temp temp"
            }

        mockDynamicReportService.demand.getContentType { String type ->
                return "text/plain"
            }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()

        when:
        params.outputFormat = "pdf"
        controller.renderReportOutputType(reportFile, executedConfiguration)
        then:
        response.getContentType()
        response.getContentAsString() == "hello world!"
        response.getHeaderValue("Content-Disposition") == 'attachment;filename="temp_temp.pdf"'
    }

    void "Test copyCaseNumbersResult"(){
        when:
        ReportResult reportResult = new ReportResult(executionStatus: ReportExecutionStatusEnum.COMPLETED)
        controller.copyCaseNumbersResult(reportResult)
        then:
        response.json.size() == 0
    }

    void "test delete"() {
        given:
        Map<String, Object> param = [:]
        param.put('id', 12234)
        param.put('deleteForAll', false)
        param.put('deleteJustification', null)
        def adminUser = makeAdminUser()
        controller.userService = makeSecurityService(adminUser)

        when:
        request.method = 'POST'
        params.id=12234
        String redirectURL = 'http://localhost:8080/reports/report/index'
        request.addHeader('referer',redirectURL)
        controller.delete()

        then:
        flash.message || flash.error
        response.status == 302
        response.redirectUrl == redirectURL
    }

    void "test drill down"() {
        given:
        Holders.config.casedata.drill.down.uri = uri
        controller.sqlGenerationService = [isCaseNumberExistsForTenant: { String caseNumber, Integer versionNumber ->
            return flag
        }]
        controller.notificationService = [deleteNotificationByNotificationParameters: { User userVal, def appNameVal, def caseNumberVal, def versionNumberVal, def executedTemplateQueryIdVal, def isInDraftModeVal -> }]
        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User()
        }
        when:
        params.caseNumber = 'xyz'
        params.versionNumber = 1
        controller.drillDown()

        then:
        response.status == status

        where:
        uri                         | flag  | status
        '/report/exportSingleCIOMS' | false | 403
        null                        | true  | 302
        '/report/exportSingleCIOMS' | true  | 302
    }

    void "test exportSingleCIOMS"() {
        given:
        controller.sqlGenerationService = [isCaseNumberExistsForTenant: { String caseNumber, Integer versionNumber ->
            return false
        }]

        when:
        params.caseNumber = 'xyz'
        params.versionNumber = 1
        controller.exportSingleCIOMS()

        then:
        response.status == 403
    }

    void "test report export xlsx"() {
        given:
        ExecutedReportConfiguration executedConfiguration = new ExecutedConfiguration()
        File reportFile = File.createTempFile("temp", "")
        reportFile.write("hello world!")
        def mockDynamicReportService =new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getReportNameAsFileName{ ExecutedReportConfiguration configuration, ExecutedTemplateQuery templtQuery ->
                return "temp temp"
            }

        mockDynamicReportService.demand.getContentType{ String type ->
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()

        when:
        params.outputFormat = 'XLSX'
        controller.renderReportOutputType(reportFile, executedConfiguration)

        then:
        response.status == 200
        response.getContentType()
        response.getContentAsString() == "hello world!"
        response.getHeaderValue("Content-Disposition") == 'attachment;filename="temp_temp.xlsx"'

    }

    void "test report export pptx"() {
        given:
        ExecutedReportConfiguration executedConfiguration = new ExecutedConfiguration()
        File reportFile = File.createTempFile("temp", "")
        reportFile.write("hello world!")
        def mockDynamicReportService =new MockFor(DynamicReportService)
            mockDynamicReportService.demand.getReportNameAsFileName{ ExecutedReportConfiguration configuration, ExecutedTemplateQuery templtQuery ->
                return "temp temp"
            }

            mockDynamicReportService.demand.getContentType{ String type ->
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()

        when:
        params.outputFormat = 'PPTX'
        controller.renderReportOutputType(reportFile, executedConfiguration)

        then:
        response.status == 200
        response.getContentType()
        response.getContentAsString() == "hello world!"
        response.getHeaderValue("Content-Disposition") == 'attachment;filename="temp_temp.pptx"'

    }

    void "test report export docx"() {
        given:
        ExecutedReportConfiguration executedConfiguration = new ExecutedConfiguration()
        File reportFile = File.createTempFile("temp", "")
        reportFile.write("hello world!")
        def mockDynamicReportService =new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getReportNameAsFileName{ ExecutedReportConfiguration configuration, ExecutedTemplateQuery templtQuery ->
            return "temp temp"
        }

        mockDynamicReportService.demand.getContentType{ String type ->
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()

        when:
        params.outputFormat = 'DOCX'
        controller.renderReportOutputType(reportFile, executedConfiguration)

        then:
        response.status == 200
        response.getContentType()
        response.getContentAsString() == "hello world!"
        response.getHeaderValue("Content-Disposition") == 'attachment;filename="temp_temp.docx"'

    }
  
    void "test show"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE)
        reportTemplate.save(failOnError:true,validate:false)
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true , validate:false)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,executedConfiguration: executedReportConfiguration,finalReportResult: reportResult )
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(failOnError:true,validate:false)
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.topXRowsInReport(1){ReportTemplate executedTemplate -> return 0}
        mockDynamicReportService.demand.isLargeReportResult(1){ReportResult reportResultInstance, ReportFormatEnum outputFormat -> false}
        mockDynamicReportService.demand.getReportName(1){ReportResult reportResultInstance, boolean isInDraftMode, Map params -> "report_1"}
        mockDynamicReportService.demand.getReportNameAsFileName(0..1){ExecutedReportConfiguration executedConfiguration, ExecutedTemplateQuery Instance = null -> "filename"}
        mockDynamicReportService.demand.createReportWithCriteriaSheetCSV(1){ReportResult reportResultObj, boolean isInDraftMode, Map params -> null }
        mockDynamicReportService.demand.isLargeReportResult(1){ReportResult reportResultInstance, ReportFormatEnum outputFormat -> false}
        mockDynamicReportService.demand.getReportNameWithLocale(0..1){String reportName, String locale -> "report_1_locale"}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        User.metaClass.static.isDev = {-> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {Collection<? extends GrantedAuthority> roles -> true}
        ReportResult.metaClass.isViewableBy = {User currentUser -> true}
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.getRemovedCaseOfSeries(0..1){Long caseSeriesId, String caseSeriesOwner -> []}
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        def mockCommentService = new MockFor(CommentService)
        mockCommentService.demand.getReportResultChartAnnotation(0..1){Long id -> null}
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        controller.commentService = mockCommentService.proxyInstance()
        controller.notificationService = [deleteNotification:{Long l, NotificationApp t->}]
        when:
        params.id = reportResult.id
        controller.show()
        then:
        view == '/report/show.gsp'
    }

    @SuppressWarnings("JUnitPublicNonTestMethod")
    def "shouldDownloadPdfReportSuccessfully"() {
        given:
        // Mocked user
        def normalUser = new User(username: 'testUser', preference: new Preference(locale: "en"))

        // Mock services
        controller.userService = Mock(UserService) {
            getUser() >> normalUser
            getCurrentUser() >> normalUser
        }

        controller.dynamicReportService = Mock(DynamicReportService) {
            createPDFReport(_, _, _, _, _) >> new File("/tmp/dummy.pdf")
            getContentType(_) >> "application/pdf"
        }

        controller.icsrProfileAckService = Mock(IcsrProfileAckService) {
            getIcsrTrackingRecord(_, _, _) >> new IcsrCaseTracking(generationDate: new Date())
        }

        // Create ExecutedTemplateQuery and mock its static read method
        def executedTemplateQuery = new ExecutedTemplateQuery(id: 1L)
        def config = new ExecutedConfiguration(reportName: "testReport", owner: normalUser)
        config.metaClass.isViewableBy = { User u -> true }
        executedTemplateQuery.metaClass.getUsedConfiguration = { -> config }

        ExecutedTemplateQuery.metaClass.static.read = { Long id -> executedTemplateQuery }

        ReportConfiguration.metaClass.static.findByReportNameAndOwner = { String name, User owner ->
            def cfg = new IcsrProfileConfiguration()
            cfg.metaClass.isViewableBy = { User u -> true }
            return cfg
        }

        // Simulate params and request
        controller.params.exIcsrTemplateQueryId = 1L
        controller.params.caseNumber = "CASE123"
        controller.params.versionNumber = 1L
        controller.params.outputFormat = "PDF"
        controller.params.isInDraftMode = false

        when:
        controller.downloadPdf(1L, "CASE123", 1L, "PDF", false)

        then:
        noExceptionThrown()
    }

    void "test criteria"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(includeAllStudyDrugsCases: true,studyDrugs: "study", executedETLDate: new Date(), hasGeneratedCasesData: false, status: ReportExecutionStatusEnum.COMPLETED) as ExecutedReportConfiguration
        executedReportConfiguration.save(failOnError:true,validate:false,flush :true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){User user -> normalUser}
        mockUserService.demand.getUser(0..1){User user -> makeAdminUser()}
        controller.userService = mockUserService.proxyInstance()
        User.metaClass.static.isDev = {-> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {Collection<? extends GrantedAuthority> roles -> true}
        def etlJobServiceMock=Mock(EtlJobService)
        etlJobServiceMock.getEtlStatus() >> {return [lastRunDateTime: new Date("Mon Oct 18 11:21:22 UTC 2021")]}
        MiscUtil.metaClass.static.getBean = { String s -> etlJobServiceMock}
        controller.userService = mockUserService.proxyInstance()
        ExecutedReportConfiguration.metaClass.isViewableBy = {User currentUser -> true}
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteExecutedReportNotification(0..1){User user, ExecutedReportConfiguration executedReportConfigurationInstance, NotificationApp appName -> null}
        controller.notificationService = mockNotificationService.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = Stub(UserService) {
            getCurrentUser() >> new User()
        }
        mockReportExecutorService.demand.debugExecutedReportSQL(0..1){ExecutedReportConfiguration executedReportConfigurationInstance, Boolean basicSqlFlag = true -> []}
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params['viewBasicSql'] = "true"
        params['viewAdvanceSql'] = "true"
        params['isInDraftMode'] = "false"
        controller.criteria(executedReportConfiguration.id)
        then:
        view == '/report/periodicReportCriteria'
        flash.warn == null
        flash.message == null
        model.size() == 8
    }

    void "test showFirstSection status equal to generated_cases"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(includeAllStudyDrugsCases: true,studyDrugs: "study",status: ReportExecutionStatusEnum.GENERATED_CASES)
        executedReportConfiguration.save(failOnError:true,validate:false,flush :true)
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true , validate:false)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedConfiguration: executedReportConfiguration,draftReportResult: reportResult)
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        User.metaClass.static.isDev = {-> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {Collection<? extends GrantedAuthority> roles -> true}
        ExecutedReportConfiguration.metaClass.isViewableBy = {User currentUser -> true}
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteExecutedReportNotification(0..1){User user, ExecutedReportConfiguration executedReportConfigurationInstance, NotificationApp appName -> null}
        controller.notificationService = mockNotificationService.proxyInstance()
        when:
        controller.showFirstSection(executedReportConfiguration.id)
        then:
        response.redirectUrl == '/caseList/index/1'
    }

    void "test showFirstSection status not equal to generated_cases"(){
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError:true)
        SourceProfile sourceProfile= new SourceProfile(sourceId: 1,sourceAbbrev: "abv" ,sourceName: "name",sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                ,dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(periodicReportType: PeriodicReportTypeEnum.ACO
                ,executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML]),executionStatus: ReportExecutionStatusEnum.GENERATED_NEW_SECTION,
                executedGlobalQueryValueLists: [new ExecutedQueryValueList()],workflowState: workflowStateInstance,
                clazz: "class",reportName: "report_1",owner: normalUser,createdBy: "user",modifiedBy: "user",
                signalConfiguration: false,tenantId: 1,sourceProfile: sourceProfile)
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE,
                showTemplateFooter: true,createdBy: "user",modifiedBy: "user")
        reportTemplate.save(failOnError:true)
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true , validate:false)
        ExecutedDateRangeInformation executedDateRangeInformation =new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date())
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,executedDateRangeInformationForTemplateQuery: executedDateRangeInformation,
                executedTemplateValueLists: [new ExecutedTemplateValueList(template: reportTemplate)],executedConfiguration: executedReportConfiguration,createdBy:"user",modifiedBy: "user",finalReportResult: reportResult,draftReportResult: reportResult )
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedDateRangeInformation.executedTemplateQuery = executedTemplateQuery
        executedDateRangeInformation.save(failOnError:true,validate:false)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        User.metaClass.static.isDev = {-> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {Collection<? extends GrantedAuthority> roles -> true}
        ExecutedReportConfiguration.metaClass.isViewableBy = {User currentUser -> true}
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteExecutedReportNotification(0..1){User user, ExecutedReportConfiguration executedReportConfigurationInstance, NotificationApp appName -> null}
        controller.notificationService = mockNotificationService.proxyInstance()
        try {
            Holders.getApplicationContext().getBean("userService")
            Holders.grailsApplication.mainContext.beanFactory.destroySingleton("userService")
        } catch (Exception e) {
            //no bean registered
        }
        Holders.grailsApplication.mainContext.beanFactory.registerSingleton("userService", new Object() {
            public User getCurrentUser() {
                return normalUser
            }
        })
        when:
        params['isInDraftMode'] = "true"
        controller.showFirstSection(executedReportConfiguration.id)
        then:
        response.redirectUrl == '/report/show/1?isInDraftMode=true'
    }

    void "test showCaseXml"(){
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError:true)
        SourceProfile sourceProfile= new SourceProfile(sourceId: 1,sourceAbbrev: "abv" ,sourceName: "name",sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                ,dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(periodicReportType: PeriodicReportTypeEnum.ACO
                ,executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML]),executionStatus: ReportExecutionStatusEnum.GENERATED_NEW_SECTION,
                executedGlobalQueryValueLists: [new ExecutedQueryValueList()],workflowState: workflowStateInstance,
                clazz: "class",reportName: "report_1",owner: normalUser,createdBy: "user",modifiedBy: "user",
                signalConfiguration: false,tenantId: 1,sourceProfile: sourceProfile)
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE,
                showTemplateFooter: true,createdBy: "user",modifiedBy: "user")
        reportTemplate.save(failOnError:true)
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true , validate:false)
        ExecutedDateRangeInformation executedDateRangeInformation =new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date())
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,executedDateRangeInformationForTemplateQuery: executedDateRangeInformation,
                executedTemplateValueLists: [new ExecutedTemplateValueList(template: reportTemplate)],executedConfiguration: executedReportConfiguration,createdBy:"user",modifiedBy: "user",finalReportResult: reportResult,draftReportResult: reportResult )
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedDateRangeInformation.executedTemplateQuery = executedTemplateQuery
        executedDateRangeInformation.save(failOnError:true,validate:false)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.metaClass.isViewableBy = { User u -> true }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        User.metaClass.static.isDev = {-> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {Collection<? extends GrantedAuthority> roles -> true}
        ExecutedReportConfiguration.metaClass.isViewableBy = {User currentUser -> true}
        controller.metaClass.show = {ReportResult reportResultInstance ->
            flash.warn = message(code: "app.show.run")
        }
        when:
        params['isInDraftMode'] = "true"
        controller.showCaseXml(executedTemplateQuery.id,true)
        then:
        flash.warn == "app.show.run"
    }

    void "test showXml"(){
        User normalUser = makeNormalUser("user",[])
        WorkflowState workflowStateInstance=new WorkflowState(name:'New',createdBy:'user',modifiedBy:'user')
        workflowStateInstance.save(failOnError:true)
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError:true)
        SourceProfile sourceProfile= new SourceProfile(sourceId: 1,sourceAbbrev: "abv" ,sourceName: "name",sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE
                ,dateRangeTypes: [dateRangeType])
        sourceProfile.save(failOnError:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(periodicReportType: PeriodicReportTypeEnum.ACO
                ,executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML]),executionStatus: ReportExecutionStatusEnum.GENERATED_NEW_SECTION,
                executedGlobalQueryValueLists: [new ExecutedQueryValueList()],workflowState: workflowStateInstance,
                clazz: "class",reportName: "report_1",owner: normalUser,createdBy: "user",modifiedBy: "user",
                signalConfiguration: false,tenantId: 1,sourceProfile: sourceProfile)
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE,
                showTemplateFooter: true,createdBy: "user",modifiedBy: "user")
        reportTemplate.save(failOnError:true)
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true , validate:false)
        ExecutedDateRangeInformation executedDateRangeInformation =new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date())
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,executedDateRangeInformationForTemplateQuery: executedDateRangeInformation,
                executedTemplateValueLists: [new ExecutedTemplateValueList(template: reportTemplate)],executedConfiguration: executedReportConfiguration,createdBy:"user",modifiedBy: "user",finalReportResult: reportResult,draftReportResult: reportResult )
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedDateRangeInformation.executedTemplateQuery = executedTemplateQuery
        executedDateRangeInformation.save(failOnError:true,validate:false)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.metaClass.isViewableBy = { User u -> true }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        User.metaClass.static.isDev = {-> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {Collection<? extends GrantedAuthority> roles -> true}
        ExecutedReportConfiguration.metaClass.isViewableBy = {User currentUser -> true}
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteExecutedReportNotification(0..1){User user, ExecutedReportConfiguration executedReportConfigurationInstance, NotificationApp appName -> null}
        controller.notificationService = mockNotificationService.proxyInstance()
        controller.metaClass.show = {ReportResult reportResultInstance ->
            flash.warn = message(code: "app.show.run")
        }
        when:
        params['isInDraftMode'] = "true"
        controller.showXml(executedReportConfiguration.id, true)
        then:
        flash.warn == "app.show.run"
    }

    void "test share"(){
        User normalUser_1 = makeNormalUser("user2", [], "abc1@gmail.com")
        User normalUser_2 = makeNormalUser("user3", [], "abc2@gmail.com")
        UserGroup userGroup_1 = new UserGroup(id: 1, name: "group",createdBy:'user',modifiedBy:'user')
        UserGroup userGroup_2 = new UserGroup(id: 2, name: "group2",createdBy:'user',modifiedBy:'user')
        userGroup_1.save(failOnError:true)
        userGroup_2.save(failOnError:true)
        ExecutedDeliveryOption executedDeliveryOption = new ExecutedDeliveryOption()
        executedDeliveryOption.save(failOnError:true,validate : false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(executedDeliveryOption: executedDeliveryOption)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser_1}
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null -> [normalUser_1,normalUser_2]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null -> [userGroup_1,userGroup_2]}
        controller.userService = mockUserService.proxyInstance()
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.save(0..1){ theInstance ->
            flash.warn = 'app.notify.shared.successful'
            return theInstance
        }
        mockCrudService.demand.saveWithoutAuditLog(0..1) { theInstance ->
            return true
        }
        def mockReportService = Mock(ReportService)
        mockReportService.updateAuditLogShareWith(_, _ as int, _, _, false) >> {return true}
        controller.reportService = mockReportService

        controller.CRUDService = mockCrudService.proxyInstance()
        def id1=normalUser_2.id as String
        def id2=userGroup_2.id as String
        def id3=normalUser_1.id as String
        def id4=userGroup_1.id as String
        String executors="User_${id1};UserGroup_${id2};User_${id3};UserGroup_${id4}"
        request.addHeader('referer',"report/index")
        request.setRequestURI("/reports/report/showFirstSection/")
        when:
        params.executedConfigId = executedReportConfiguration.id
        params.sharedWith = ""
        controller.share()
        then:
        flash.message == 'app.configuration.shared.successful'
        view == '/report/share.gsp'
    }

    void "test email"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.isLargeReportResult(0..1){ExecutedReportConfiguration executedConfiguration, Boolean draftMode = false, Boolean hasPPTXFormat -> false}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.emailReportTo(0..1){def configuration, List<String> recipients, List<ReportFormatEnum> outputs
            ->         flash.message =  'app.function.run'
        }
        controller.emailService = mockEmailService.proxyInstance()
        request.addHeader('referer',"report/index")
        when:
        params.executedConfigId = executedReportConfiguration.id
        params.attachmentFormats = ["PPTX"]
        params.emailToUsers = []
        params.emailConfiguration = [:]
        controller.email()
        then:
        flash.message == 'app.function.run'
        response.redirectUrl == 'report/index'
    }
    void "test exception for components reaches outside available width or height"(){
        given :
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE)
        reportTemplate.save(failOnError:true,validate:false)
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true , validate:false)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,executedConfiguration: executedReportConfiguration,finalReportResult: reportResult )
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(failOnError:true,validate:false)
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.topXRowsInReport(1){ReportTemplate executedTemplate -> return 0}
        mockDynamicReportService.demand.isLargeReportResult(1){ReportResult reportResultInstance, ReportFormatEnum outputFormat -> false}
        mockDynamicReportService.demand.getReportName(1){ReportResult reportResultInstance, boolean isInDraftMode, Map params -> "report_1"}
        mockDynamicReportService.demand.getReportNameAsFileName(0..1){ExecutedReportConfiguration executedConfiguration, ExecutedTemplateQuery Instance = null -> "filename"}
        mockDynamicReportService.demand.createReportWithCriteriaSheetCSV(1){ReportResult reportResultObj, boolean isInDraftMode, Map params -> throw new DRException("components reaches outside available width") }
        mockDynamicReportService.demand.isLargeReportResult(1){ReportResult reportResultInstance, ReportFormatEnum outputFormat -> false}
        mockDynamicReportService.demand.getReportNameWithLocale(0..1){String reportName, String locale -> "report_1_locale"}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        User.metaClass.static.isDev = {-> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {Collection<? extends GrantedAuthority> roles -> true}
        ReportResult.metaClass.isViewableBy = {User currentUser -> true}
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.getRemovedCaseOfSeries(0..1){Long caseSeriesId, String caseSeriesOwner -> []}
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        def mockCommentService = new MockFor(CommentService)
        mockCommentService.demand.getReportResultChartAnnotation(0..1){Long id -> null}
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        controller.commentService = mockCommentService.proxyInstance()
        controller.notificationService = [deleteNotification:{Long l, NotificationApp t->}]
        when:
        controller.show(reportResult)
        then:
        response.status==302
        response.redirectedUrl=='/report/show/1'
    }

    void "test email for Exception"(){
        given:
        ExecutedReportConfiguration reportConfiguration = new ExecutedPeriodicReportConfiguration().save(flush: true, failOnError: true, validate: false)
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.emailReportTo(0..1){ExecutedReportConfiguration testReportConfiguration, List<String> testEmailList, List<ReportFormatEnum> testFormats ->
            throw new CustomJasperException("Exception thrown during emailing Report")
        }

        controller.emailService = mockEmailService.proxyInstance()

        when:
        params.executedConfigId = reportConfiguration.id
        params.attachmentFormats = ["PDF","XML"]
        params.emailToUsers = ["abc@gmail.com"]
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        controller.email()

        then:
        thrown(Exception)
    }

    void "test bindEmailConfiguration update"(){
        EmailConfiguration emailConfiguration = new EmailConfiguration()
        emailConfiguration.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(emailConfiguration: emailConfiguration)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.update(0..1){ theInstance ->
            flash.message = 'app.update.successful'
            return theInstance
        }
        controller.CRUDService = mockCrudService.proxyInstance()
        Map<String,String> emailData = ['subject':"subject_1",'body':"body_1"]
        when:
        controller.invokeMethod('bindEmailConfiguration', [executedReportConfiguration, emailData] as Object[])
        then:
        flash.message == 'app.update.successful'
    }

    void "test bindEmailConfiguration save"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.save(0..1){ theInstance ->
            flash.message = 'app.save.successful'
            return theInstance
        }
        controller.CRUDService = mockCrudService.proxyInstance()
        Map<String,String> emailData = ['subject':"subject_1",'body':"body_1"]
        when:
        controller.invokeMethod('bindEmailConfiguration', [executedReportConfiguration, emailData] as Object[])
        then:
        flash.message == 'app.save.successful'
    }

    void "test bindEmailConfiguration delete"(){
        EmailConfiguration emailConfiguration = new EmailConfiguration()
        emailConfiguration.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(emailConfiguration: emailConfiguration)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.softDelete(0..1){ theInstance, name, String justification = null, Map saveParams = null ->
            flash.message = 'app.delete.successful'
            return theInstance
        }
        controller.CRUDService = mockCrudService.proxyInstance()
        Map<String,String> emailData = [:]
        when:
        controller.invokeMethod('bindEmailConfiguration', [executedReportConfiguration, emailData] as Object[])
        then:
        flash.message == 'app.delete.successful'
    }

    void "test bindDmsConfiguration update"(){
        DmsConfiguration dmsConfiguration = new DmsConfiguration()
        dmsConfiguration.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(dmsConfiguration: dmsConfiguration)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.update(0..1){ theInstance ->
            flash.message = 'app.update.successful'
            return theInstance
        }
        controller.CRUDService = mockCrudService.proxyInstance()
        Map<String,String> emailData = ['format':"format"]
        when:
        controller.invokeMethod('bindDmsConfiguration', [executedReportConfiguration, emailData] as Object[])
        then:
        flash.message == 'app.update.successful'
    }

    void "test bindDmsConfiguration save"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.save(0..1){ theInstance ->
            flash.message = 'app.save.successful'
            return theInstance
        }
        controller.CRUDService = mockCrudService.proxyInstance()
        Map<String,String> emailData = ['format':"format"]
        when:
        controller.invokeMethod('bindDmsConfiguration', [executedReportConfiguration, emailData] as Object[])
        then:
        flash.message == 'app.save.successful'
    }

    void "test bindDmsConfiguration delete"(){
        DmsConfiguration dmsConfiguration = new DmsConfiguration()
        dmsConfiguration.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(dmsConfiguration: dmsConfiguration)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.softDelete(0..1){ theInstance, name, String justification = null, Map saveParams = null ->
            flash.message = 'app.delete.successful'
            return theInstance
        }
        controller.CRUDService = mockCrudService.proxyInstance()
        Map<String,String> emailData = [:]
        when:
        controller.invokeMethod('bindDmsConfiguration', [executedReportConfiguration, emailData] as Object[])
        then:
        flash.message == 'app.delete.successful'
    }

    void "test sendToDms"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.save(0..1){ theInstance -> theInstance}
        controller.CRUDService = mockCrudService.proxyInstance()
        def mockDmsService = new MockFor(DmsService)
        mockDmsService.demand.uploadReport(0..1){def configuration, String format = null, List sections = null -> null}
        controller.dmsService = mockDmsService.proxyInstance()
        request.addHeader('referer',"report/index")
        when:
        params.executedConfigId = executedReportConfiguration.id
        params.dmsConfiguration = ['format':"format"]
        controller.sendToDms()
        then:
        response.redirectUrl == 'report/index'
        flash.message == 'app.dms.successfully.upload'
    }

    void "test sendToDms exception"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockDmsService = new MockFor(DmsService)
        mockDmsService.demand.uploadReport(0..1){def configuration, String format = null, List sections = null -> null}
        controller.dmsService = mockDmsService.proxyInstance()
        request.addHeader('referer',"report/index")
        when:
        params.executedConfigId = executedReportConfiguration.id
        params.dmsConfiguration = ['format':"format"]
        controller.sendToDms()
        then:
        response.redirectUrl == 'report/index'
        flash.error == "app.dms.failure.upload"
    }

    void "test viewMultiTemplateReport"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(owner: normalUser,reportName: 'report_1')
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE)
        reportTemplate.save(failOnError:true,validate:false)
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true , validate:false)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,executedConfiguration: executedReportConfiguration,finalReportResult: reportResult )
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.topXRowsInReport(0..1){ExecutedReportConfiguration executedConfigurationInstance -> return 0}
        mockDynamicReportService.demand.getReportName(0..1){ExecutedReportConfiguration executedConfigurationInstance, boolean isInDraftMode, Map params -> "report_1"}
        mockDynamicReportService.demand.isLargeReportResult(0..1){ExecutedReportConfiguration executedConfiguration, Boolean draftMode = false, Boolean hasPPTXFormat = false-> false}
        mockDynamicReportService.demand.getReportNameWithLocale(0..1){String reportName, String locale -> "report_1_locale"}
        mockDynamicReportService.demand.getReportNameAsFileName(0..1){ExecutedReportConfiguration executedConfiguration -> "report_1_locale"}
        def mockQualityService=Mock(QualityService)
        mockQualityService.getQualityDataByIds(_,_,_)>>{
            [[testKey:'testValue1', errorType:'testErrorType', priority:'testPriority', assignedTo:''],
             [testKey:'testValue2', errorType:'testErrorType', priority:'testPriority', assignedTo:'']]

        }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        User.metaClass.static.isDev = {-> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> true}
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {Collection<? extends GrantedAuthority> roles -> true}
        ExecutedReportConfiguration.metaClass.isViewableBy = {User currentUser -> true}
        ReportResult.metaClass.static.findById = {Long id -> new ReportResult()}
        AuditLogConfigUtil.metaClass.static.logChanges = { def domain, Map newMap, Map oldMap, String eventName, String extraValue ->
            // do nothing
        }
        when:
        params.outputFormat = "XML"
        params['isInDraftMode'] = "true"
        params.id = executedReportConfiguration.id
        controller.viewMultiTemplateReport()
        then:
        view == '/report/viewMultiTemplateReport.gsp'
    }

    void "test exportReportFromInbox"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(owner: normalUser,reportName: 'report_1',status: ReportExecutionStatusEnum.GENERATED_DRAFT)
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE)
        reportTemplate.save(failOnError:true,validate:false)
        ReportResult reportResult = new ReportResult(executionStatus: ReportExecutionStatusEnum.COMPLETED)
        reportResult.save(failOnError:true , validate:false)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,executedConfiguration: executedConfiguration,draftReportResult: reportResult,reportResult: reportResult,finalReportResult:reportResult )
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.isLargeReportResult(0..1){ExecutedReportConfiguration executedConfigurationInstance, Boolean draftMode = false, Boolean hasPPTXFormat = false-> false}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        controller.metaClass.show = {ReportResult reportResultInstance ->
            flash.message = message(code: "app.show.run")
        }
        when:
        params.id = executedConfiguration.id
        controller.exportReportFromInbox()
        then:
        flash.message == "app.show.run"
    }

    void "test exportReportFromInbox no executed template queries"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(owner: normalUser,reportName: 'report_1',status: ReportExecutionStatusEnum.GENERATED_DRAFT)
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE)
        reportTemplate.save(failOnError:true,validate:false)
        ReportResult reportResult = new ReportResult(executionStatus: ReportExecutionStatusEnum.COMPLETED)
        reportResult.save(failOnError:true , validate:false)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,executedConfiguration: executedConfiguration,draftReportResult: reportResult,reportResult: reportResult,finalReportResult:reportResult )
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.isLargeReportResult(0..1){ExecutedReportConfiguration executedConfigurationInstance, Boolean draftMode = false, Boolean hasPPTXFormat = false-> false}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        controller.metaClass.show = {ReportResult reportResultInstance ->
            flash.message = message(code: "app.show.run")
        }
        when:
        params.id = executedConfiguration.id
        controller.exportReportFromInbox()
        then:
        flash.warn =="app.warn.completed.template.queries.not.found"
        view == '/report/index'
    }

    void "test updateStatus"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.changeReportResultStatus(0..1){def reportStatus, User user, ExecutedReportConfiguration report ->
            flash.message = "function.run"
        }
        controller.reportService = mockReportService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.updateStatus()
        then:
        flash.message == "function.run"
        response.redirectUrl == '/report/index'
    }

    void "test delete with report"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(status: ReportExecutionStatusEnum.SUBMITTED)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        request.method = 'POST'
        params.id=executedReportConfiguration.id
        String redirectURL = 'http://localhost:8080/reports/report/index'
        request.addHeader('referer',redirectURL)
        controller.delete()
        then:
        flash.error == 'default.report.delete.not.rights'
        response.redirectUrl == redirectURL
    }

    void "test delete with report success delete for all true"(){
        User adminUser = makeAdminUser()
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(status: ReportExecutionStatusEnum.SUBMITTED)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.softDelete(0..1){ theInstance, name, String justification = null, Map saveParams = null -> theInstance}
        controller.CRUDService = mockCrudService.proxyInstance()
        when:
        request.method = 'POST'
        params.id=executedReportConfiguration.id
        params["deleteForAll"] = "true"
        String redirectURL = 'http://localhost:8080/reports/report/index'
        request.addHeader('referer',redirectURL)
        controller.delete()
        then:
        flash.message == 'default.deleted.message'
        response.redirectUrl == redirectURL
    }

    void "test delete with report success delete for all false"(){
        User adminUser = makeAdminUser()
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(status: ReportExecutionStatusEnum.SUBMITTED, executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF]), owner: adminUser)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        mockUserService.demand.getUser(0..1){ -> adminUser}
        mockUserService.demand.removeUserFromDeliveryOptionSharedWith(0..1){ User user, BaseDeliveryOption deliveryOption, Long ownerId -> }
        controller.userService = mockUserService.proxyInstance()
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.softDeleteForUser(0..1){User user, theInstance, name, String justification = null-> theInstance}
        controller.CRUDService = mockCrudService.proxyInstance()
        adminUser.metaClass.static.isAdmin = {
            return true
        }
        when:
        request.method = 'POST'
        params.id=executedReportConfiguration.id
        params["deleteForAll"] = "false"
        String redirectURL = 'http://localhost:8080/reports/report/index'
        request.addHeader('referer',redirectURL)
        controller.delete()
        then:
        flash.message == 'default.deleted.message'
        response.redirectUrl == redirectURL
    }

    void "test archive"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.toggleIsArchived(0..1){User user, ExecutedReportConfiguration executedConfiguration ->
            run = true
        }
        controller.reportService = mockReportService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        String redirectURL = 'http://localhost:8080/reports/report/index'
        request.addHeader('referer',redirectURL)
        params.id = executedReportConfiguration.id
        controller.archive()
        then:
        run == true
        response.redirectUrl == redirectURL
    }

    void "test favorite"(){
        boolean run = false
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.setFavorite(0..1){ExecutedReportConfiguration executedConfiguration, Boolean isFavorite ->
            run = true
        }
        controller.reportService = mockReportService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.favorite()
        then:
        run == true
        response.json.status == true
        response.json.httpCode == 200
        response.json.message == ""
    }

    void "test favorite throw exception"(){
        boolean run = false
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.setFavorite(1){ExecutedReportConfiguration executedConfiguration, Boolean isFavorite ->
            run = true
            throw new Exception()
        }
        controller.reportService = mockReportService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.favorite()
        then:
        run == true
        response.json.status == false
        response.json.httpCode == 500
        response.json.message == "default.server.error.message"
    }

    void "test favorite report null"(){
        when:
        params.id = 10
        controller.favorite()
        then:
        response.json.status == false
        response.json.httpCode == 500
        response.json.message == "default.not.found.message"
    }

    void "test showReport executedReportConfigurationInstance"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.findExecutedReportConfigurationById(0..1){Long id -> executedReportConfiguration}
        controller.reportService = mockReportService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.topXRowsInReport(0..1){ExecutedReportConfiguration executedConfigurationInstance -> return 0}
        mockDynamicReportService.demand.isLargeReportResult(0..1){ExecutedReportConfiguration executedConfiguration, Boolean draftMode = false, Boolean hasPPTXFormat = false-> false}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        when:
        params["isInDraftMode"] = "true"

        def responseDTO = controller.invokeMethod('showReport', [executedReportConfiguration.id] as Object[])
        then:
        responseDTO.status == true
    }

    void "test showReport report result"(){
        User normalUser = makeNormalUser("user",[])
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE)
        reportTemplate.save(failOnError:true,validate:false)
        ReportResult reportResult = new ReportResult(executionStatus: ReportExecutionStatusEnum.COMPLETED)
        reportResult.save(failOnError:true , validate:false)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,draftReportResult: reportResult,reportResult: reportResult,finalReportResult:reportResult )
        executedTemplateQuery.save(failOnError:true,validate:false)
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.findExecutedReportConfigurationById(0..1){Long id -> null}
        controller.reportService = mockReportService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.topXRowsInReport(0..1){ReportTemplate executedTemplate -> return 0}
        mockDynamicReportService.demand.isLargeReportResult(0..1){ReportResult reportResultInstance, ReportFormatEnum outputFormat = ReportFormatEnum.HTML-> false}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        when:
        params["isInDraftMode"] = "true"
        def responseDTO = controller.invokeMethod('showReport', [reportResult.id] as Object[])
        then:
        responseDTO.status == true
    }

    void "test getReportAsHTMLString"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.findExecutedReportConfigurationById(0..1){Long id -> executedReportConfiguration}
        controller.reportService = mockReportService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.topXRowsInReport(0..1){ExecutedReportConfiguration executedConfigurationInstance -> return 0}
        mockDynamicReportService.demand.isLargeReportResult(0..1){ExecutedReportConfiguration executedConfiguration, Boolean draftMode = false, Boolean hasPPTXFormat = false-> false}
        mockDynamicReportService.demand.getReportsDirectory(0..1){ -> "/report/needs/to/be/present/on/your/system"}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        when:
        params["isInDraftMode"] = "true"
        controller.getReportAsHTMLString("report",executedReportConfiguration.id)
        then:
        response.text == "pvreports.file.html.not.found.error"
    }

    void "test exportSingleCIOMS isCaseNumberExistsForTenant=true"(){
        int run = 0
        User adminUser = makeAdminUser()
        CustomSQLTemplate customSQLTemplate = new CustomSQLTemplate(name: "CIOMS I Template",ciomsI: true,originalTemplateId: 0)
        customSQLTemplate.save(failOnError:true,validate:false)
        SourceProfile sourceProfile = new SourceProfile(isCentral: true)
        sourceProfile.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        Tenants.metaClass.static.currentId = { -> return 1}
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getReportName(0..1){ReportResult reportResult, boolean isInDraftMode, Map params -> "report"}
        mockDynamicReportService.demand.getReportFilename(0..1){String reportName, String outputFormat, String locale-> "filename"}
        mockDynamicReportService.demand.getReportsDirectory(0..1){ -> "/report/needs/to/be/present/on/your/system"}
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.generateSingleCIOMSReport(0..1){ReportResult result, String caseNumber, Integer versionNumber,boolean blind, boolean privacy, Long processReportId, String prodHashCode ->
            run++
        }
        def mockSqlGenerationService = new MockFor(DynamicReportService)
        mockSqlGenerationService.demand.isCaseNumberExistsForTenant(1){String caseNumber, Integer versionNumber ->
            return true
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        controller.sqlGenerationService = mockSqlGenerationService.proxyInstance()
        controller.metaClass.show = {ReportResult reportResultInstance ->
            run++
        }
        when:
        params.caseNumber = 'xyz'
        params.versionNumber = 1
        controller.exportSingleCIOMS()
        then:
        run == 2
    }

    void "test copyCaseNumbersConfiguration"(){
        User normalUser = makeNormalUser("user",[])
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE)
        reportTemplate.save(failOnError:true,validate:false)
        ReportResult reportResult = new ReportResult(executionStatus: ReportExecutionStatusEnum.COMPLETED)
        reportResult.save(failOnError:true , validate:false)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,draftReportResult: reportResult,reportResult: reportResult,finalReportResult:reportResult )
        executedTemplateQuery.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries.add(executedTemplateQuery)
        when:
        controller.copyCaseNumbersConfiguration(executedReportConfiguration.id)
        then:
        response.json.size() == 0
    }

    void "test addDmsConfiguration"(){
        views['/configuration/includes/_dmsConfiguration.gsp'] = 'mock template contents'
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        params.id = executedReportConfiguration.id
        controller.addDmsConfiguration()
        then:
        response.text == 'mock template contents'
    }

    void "test addEmailConfiguration"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration( emailConfiguration: new EmailConfiguration(subject: "subject",body: "body"))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        params.id = executedReportConfiguration.id
        controller.addEmailConfiguration()
        then:
        response.json.subject == "subject"
        response.json.body == "body"
    }

    void "test checkDeleteForAllAllowed"(){
        User adminUser = makeAdminUser()
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.checkDeleteForAllAllowed()
        then:
        response.text == "true"
    }

    void "test getTotalReportRows"(){
        User normalUser = makeNormalUser("user",[])
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE)
        reportTemplate.save(failOnError:true,validate:false)
        ReportResult reportResult = new ReportResult(executionStatus: ReportExecutionStatusEnum.COMPLETED,reportRows: 5)
        reportResult.save(failOnError:true , validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,draftReportResult: reportResult,reportResult: reportResult,finalReportResult:reportResult,executedConfiguration: executedReportConfiguration )
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedReportConfiguration.executedTemplateQueries.add(executedTemplateQuery)
        when:
        def result = controller.getTotalReportRows(executedReportConfiguration,0,false)
        then:
        result == 10
    }
}
