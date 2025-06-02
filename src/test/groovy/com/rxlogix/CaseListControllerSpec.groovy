package com.rxlogix

import com.rxlogix.commandObjects.CaseCommand
import com.rxlogix.config.*
import com.rxlogix.enums.NotificationApp
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.file.MultipartFileSender
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.user.UserRole
import com.rxlogix.util.AuditLogConfigUtil
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.mock.interceptor.MockFor
import net.sf.dynamicreports.report.exception.DRException
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ConfineMetaClassChanges([AuditLogConfigUtil, Tenants, MultipartFileSender])
class CaseListControllerSpec extends Specification implements DataTest,  ControllerUnitTest<CaseListController> {

    public static final user = "unitTest"

    def setup() {
    }

    def cleanup() {

    }

    def setupSpec() {
        mockDomains ExecutedCaseSeries, Tenant, User, Role, UserRole, Preference, ApplicationSettings, ExecutedReportConfiguration, ExecutedPeriodicReportConfiguration
        AuditLogConfigUtil.metaClass.static.logChanges  = {domain, Map newMap, Map oldMap, String eventName, String extraValue = "", String transactionId = ("" + System.currentTimeMillis()) ->
        }
    }

    void "test renderReportOutputType"() {
        given:
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(id: 1L, seriesName: "temp temp", tenantId: 1L, createdBy: user, modifiedBy: user)
        executedCaseSeries.save(flush: true, validate: false)
        controller.dynamicReportService = new Object() {
            public File createCaseListReport(ExecutedCaseSeries caseSeries, Map params) {
                File reportFile = File.createTempFile("temp", "")
                reportFile.write("hello world!")
                return reportFile
            }

            public String getContentType(String type) {
                return "text/plain"
            }

            public String truncateFileName(String fileName, int maxBytes) {
                return fileName
            }
        }
        controller.userService = makeUserService(makeAdminUser())
        when:
        params.id = executedCaseSeries.id
        params.outputFormat = "PDF"
        controller.list(executedCaseSeries.id)
        then:
        response.getContentType()
        response.getContentAsString() == "hello world!"
        response.getHeaderValue("Content-Disposition") == 'attachment;filename="temp_temp.PDF"'
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

    private makeUserService(User user) {
        def userMock = new MockFor(UserService)
        userMock.demand.getUser(0..1) { -> user }
        userMock.demand.getCurrentUser(0..1) { -> user }
        return userMock.proxyInstance()
    }

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        Preference preferenceAdmin = new Preference(locale: new Locale("en"))
        Role adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: user, modifiedBy: user).save(flush: true)
        User adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: user, modifiedBy: user)
        adminUser.addToTenants(tenant)
        adminUser.save(flush: true)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isDev = { true }
        return adminUser
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
        normalUser.metaClass.isDev = { false }
        return normalUser
    }

    void "test index"(){
        boolean run = false
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName")
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(caseSeries: executedCaseSeries,cumulativeCaseSeries: executedCaseSeries,status: ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT)
        executedReportConfiguration.save(failOnError: true,validate:false,flush:true)
        ApplicationSettings applicationSettings = new ApplicationSettings(defaultUiSettings: "default")
        applicationSettings.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..2) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteNotificationByExecutionStatusId(0..1){User userObj, Long executionStatusIdVal, NotificationApp appNameVal->
            run = true
        }
        controller.notificationService = mockNotificationService.proxyInstance()
        when:
        params.cid = executedCaseSeries.id
        def result = controller.index(false)
        then:
        run == true
        result.size() == 9
        result.cumulativeType == true
    }

    void "test index no params.cid"(){
        boolean run = false
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName")
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(caseSeries: executedCaseSeries,cumulativeCaseSeries: executedCaseSeries,status: ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT)
        executedReportConfiguration.save(failOnError: true,validate:false,flush:true)
        ApplicationSettings applicationSettings = new ApplicationSettings(defaultUiSettings: "default")
        applicationSettings.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..2) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteNotificationByExecutionStatusId(0..1){User userObj, Long executionStatusIdVal, NotificationApp appNameVal->
            run = true
        }
        controller.notificationService = mockNotificationService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        def result = controller.index(false)
        then:
        run == true
        result.size() == 9
        result.cumulativeType == true
    }

    void "test index case series not found"(){
        when:
        controller.index(false)
        then:
        flash.error == 'app.caseList.not.available'
        response.redirectUrl == '/executedCaseSeries/index'
    }

    void "test index not viewable"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",tenantId: 10)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(caseSeries: executedCaseSeries,cumulativeCaseSeries: executedCaseSeries,status: ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT)
        executedReportConfiguration.save(failOnError: true,validate:false,flush:true)
        ApplicationSettings applicationSettings = new ApplicationSettings(defaultUiSettings: "default")
        applicationSettings.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> normalUser }
        controller.userService = userMock.proxyInstance()
        Tenants.metaClass.static.currentId = { -> return 1}
        when:
        params.cid = executedCaseSeries.id
        controller.index(false)
        then:
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/executedCaseSeries/index'
    }

    void "test index executing"(){
        boolean run = false
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: true,seriesName: "seriesName")
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(caseSeries: executedCaseSeries,cumulativeCaseSeries: executedCaseSeries,status: ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT)
        executedReportConfiguration.save(failOnError: true,validate:false,flush:true)
        ApplicationSettings applicationSettings = new ApplicationSettings(defaultUiSettings: "default")
        applicationSettings.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..2) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteNotificationByExecutionStatusId(0..1){User userObj, Long executionStatusIdVal, NotificationApp appNameVal->
            run = true
        }
        controller.notificationService = mockNotificationService.proxyInstance()
        when:
        params.cid = executedCaseSeries.id
        controller.index(false)
        then:
        run == true
        response.redirectUrl == '/executedCaseSeries/show/1'
    }

    void "test list outputFormat not equal to HTML"(){
        int run = 0
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.truncateFileName(0..1){String fileName, int maxBytes ->
            return fileName
        }
        mockDynamicReportService.demand.createCaseListReport(0..1){ExecutedCaseSeries caseSeries, Map params ->
            run++
            return new File("pathname")
        }
        mockDynamicReportService.demand.getContentType(0..1){String extension ->
            run++
            return "content"
        }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        MultipartFileSender.metaClass.static.renderFile = { File file, String reportFileName, String ext, String contentType, HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean inline ->
            run++
        }
        when:
        params.outputFormat = "PDF"
        params.caseListType = "openCaseList"
        controller.list(executedCaseSeries.id)
        then:
        run == 3
    }

    void "test list outputFormat equal to HTML"(){
        boolean run = false
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner")
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.getCaseOfSeries(0..1){Long caseSeriesId, Integer offset, Integer max, String sort, String direction, String searchString, String caseSeriesOwner->
            run = true
            return [1,1,[new CaseDTO(caseUniqueId:'caseUniqueId', caseNumber:'caseNumber',versionNumber:'versionNumber', type:'type', productFamily:'productFamily', eventPI:'eventPI', seriousness:'seriousness', outcome:'outcome', listedness:'listedness', causality:'causality', comments:'comments',justification: 'justification', lockedDate:new Date(),eventSequenceNumber:'eventSequenceNumber',eventReceiptDate:new Date(),eventPreferredTerm:'eventPreferredTerm',eventSeriousness:'eventSeriousness',caseId:1,caseSeriesTags:['caseSeriesTags'],globalTags:['globalTags'])]]
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params.outputFormat = ""
        params.sort = "dateCreated"
        params.offset = 0
        params.max = 10
        params.order = ""
        params.searchString = ""
        controller.list(executedCaseSeries.id)
        then:
        run == true
        response.json.aaData[0].size() == 24
        response.json.recordsTotal == 1
        response.json.recordsFiltered == 1
    }

    void "test list case series not found"(){
        when:
        controller.list(10)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test list not viewable"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> normalUser }
        controller.userService = userMock.proxyInstance()
        when:
        params.outputFormat = "PDF"
        params.caseListType = "openCaseList"
        controller.list(executedCaseSeries.id)
        then:
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/executedCaseSeries/index'
    }

    void "test list exception in reportFile"(){
        given:
        ExecutedCaseSeries caseSeries = new ExecutedCaseSeries().save(flush: true, failOnError: true, validate: false)
        Map params = [:]
        def dynamicReportServiceMock= new MockFor(DynamicReportService)
        dynamicReportServiceMock.demand.createCaseListReport(0..1) { ExecutedCaseSeries testCaseSeries, Map testParams->
            throw new DRException("Exception thrown while creating caseList report")
        }
        controller.dynamicReportService = dynamicReportServiceMock.proxyInstance()

        when:
        controller.list(caseSeries, params)

        then:
        thrown(Exception)

    }

    void "test setExecutionStatus"(){
        int run = 0
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance ->
            run++
        }
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            run++
            theInstance.save(failOnError: true,validate:false)
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        messageSource.addMessage("auditLog.refresh.caseseries", Locale.default, "Case Line Listing Template")
        when:
        controller.invokeMethod('setExecutionStatus', [executedCaseSeries] as Object[])
        then:
        run == 1
        flash.message == 'app.refreshCaseSeries.progress'
    }

    void "test refreshCaseList"(){
        int run = 0
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(caseSeries: executedCaseSeries,status: ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT)
        executedReportConfiguration.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..2) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance ->
            run++
        }
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            run++
            theInstance.save(failOnError: true,validate:false)
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        messageSource.addMessage("auditLog.refresh.caseseries", Locale.default, "Case Line Listing Template")
        when:
        controller.refreshCaseList(executedCaseSeries)
        then:
        response.redirectUrl == '/executionStatus/list'
        run == 1
        flash.message == 'app.refreshCaseSeries.progress'
    }

    void "test refreshCaseList with cumulativeCaseSeries"(){
        int run = 0
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(caseSeries: executedCaseSeries,cumulativeCaseSeries: executedCaseSeries,status: ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT)
        executedReportConfiguration.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..3) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance ->
            run++
        }
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            run++
            theInstance.save(failOnError: true,validate:false)
            return theInstance
        }
        mockCRUDService.demand.instantSaveWithoutAuditLog(0..1){theInstance ->
            run++
        }
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            run++
            theInstance.save(failOnError: true,validate:false)
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        messageSource.addMessage("auditLog.refresh.caseseries", Locale.default, "Case Line Listing Template")
        when:
        controller.refreshCaseList(executedCaseSeries)
        then:
        response.redirectUrl == '/executionStatus/list'
        run == 2
        flash.message == 'app.refreshCaseSeries.progress'
    }

    void "test refreshCaseList not viewable"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> normalUser }
        controller.userService = userMock.proxyInstance()
        when:
        controller.refreshCaseList(executedCaseSeries)
        then:
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/executedCaseSeries/index'
    }

    void "test refreshCaseList executing"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: true,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(caseSeries: executedCaseSeries,status: ReportExecutionStatusEnum.GENERATED_FINAL_DRAFT)
        executedReportConfiguration.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        when:
        controller.refreshCaseList(executedCaseSeries)
        then:
        response.redirectUrl == '/executedCaseSeries/show/1'
    }

    void "test refreshCaseList case series not found"(){
        when:
        controller.refreshCaseList(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/executedCaseSeries/index'
    }

    void "test addCaseToList caseNumberAndVersion true"(){
        int run = 0
        User adminUser = makeAdminUser()
        File reportFile = File.createTempFile("temp", "")
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def mockImportService = new MockFor(ImportService)
        mockImportService.demand.readFromExcel(0..1){ MultipartFile file ->
            run++
            return ["stringnotempty"]
        }
        controller.importService = mockImportService.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveCaseSeriesInDB(0..1){Set<String> caseNumberAndVersion, ExecutedCaseSeries executedCaseSeriesInstance, String justification = null ->
            run++
            return [] as Set
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.deleteAllCaseSeriesCachedFile(0..1){ExecutedCaseSeries executedCaseSeriesInstance, Boolean refresh = false ->
            run++
        }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        messageSource.addMessage("auditLog.add.cases", Locale.default, "Case Line Listing Template")
        when:
        Holders.config.caseSeries.bulk.addCase.delimiter= ":"
        controller.addCaseToList(new CaseCommand(executedCaseSeries: executedCaseSeries,caseNumber: "1",versionNumber: "1",justification: "jsutification",file: reportFile as MultipartFile))
        then:
        run == 3
        response.json == [message:"caseCommand.import.caseNumber.success", status:200]
    }

    void "test addCaseToList caseNumberAndVersion false"(){
        int run = 0
        User adminUser = makeAdminUser()
        File reportFile = File.createTempFile("temp", "")
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def mockImportService = new MockFor(ImportService)
        mockImportService.demand.readFromExcel(0..1){ MultipartFile file ->
            run++
            return []
        }
        controller.importService = mockImportService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.deleteAllCaseSeriesCachedFile(0..1){ExecutedCaseSeries executedCaseSeriesInstance, Boolean refresh = false ->
            run++
        }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        messageSource.addMessage("auditLog.add.cases", Locale.default, "Case Line Listing Template")
        when:
        controller.addCaseToList(new CaseCommand(executedCaseSeries: executedCaseSeries,caseNumber: "1",versionNumber: "1",justification: "jsutification",file: reportFile as MultipartFile))
        then:
        run == 2
        response.json == [message:"app.label.no.data.excel.error", status:500]
    }

    void "test addCaseToList exception"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        when:
        controller.addCaseToList(new CaseCommand(executedCaseSeries: executedCaseSeries,caseNumber: "1",versionNumber: "1",justification: "jsutification"))
        then:
        response.json == [message:"default.server.error.message", status:500]
    }

    void "test addCaseToList no versionNumber and warnings"(){
        int run = 0
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.addCaseToGeneratedList(0..1){CaseCommand caseCommand, User user ->
            run++
            return ["string not empty"] as Set
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        controller.addCaseToList(new CaseCommand(executedCaseSeries: executedCaseSeries,caseNumber: "1",justification: "jsutification"))
        then:
        run == 1
        response.json == [message:"caseCommand.string not empty", status:500]
    }

    void "test addCaseToList no versionNumber and no warnings"(){
        int run = 0
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..2) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.addCaseToGeneratedList(0..1){CaseCommand caseCommand, User user ->
            run++
            return [] as Set
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.deleteAllCaseSeriesCachedFile(0..1){ExecutedCaseSeries executedCaseSeriesInstance, Boolean refresh = false ->
            run++
        }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        messageSource.addMessage("auditLog.add.cases", Locale.default, "Case Line Listing Template")
        when:
        controller.addCaseToList(new CaseCommand(executedCaseSeries: executedCaseSeries,caseNumber: "1",justification: "jsutification"))
        then:
        run == 2
        response.json == [message:"caseCommand.add.caseNumber.success", status:200]
    }

    void "test addCaseToList caseCommand validation fail"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        when:
        controller.addCaseToList(new CaseCommand(executedCaseSeries: executedCaseSeries,justification: "jsutification"))
        then:
        response.json == [message:"app.error.fill.all.required", status:500]
    }

    void "test removeCaseFromList success"(){
        int run = 0
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..2) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.removeCaseFromGeneratedList(0..1){List<CaseCommand> caseCommandList, User user, ExecutedCaseSeries caseSeries ->
            run++
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.deleteAllCaseSeriesCachedFile(0..1){ExecutedCaseSeries executedCaseSeriesInstance, Boolean refresh = false ->
            run++
        }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        messageSource.addMessage("auditLog.remove.cases", Locale.default, "Case Line Listing Template")
        request.json = JsonOutput.toJson([[caseNumber: "caseNumber", versionNumber: "versionNumber",justification:"justification"]])
        when:
        controller.removeCaseFromList(executedCaseSeries.id)
        then:
        run == 2
        flash.message == 'caseCommand.remove.caseNumbers.success'
        response.json == [success:true, message:"caseCommand.remove.caseNumbers.success"]
    }

    void "test removeCaseFromList failed validation"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        request.json = JsonOutput.toJson([[versionNumber: "versionNumber"]])
        when:
        controller.removeCaseFromList(executedCaseSeries.id)
        then:
        response.json == [message:"app.error.fill.all.required", status:500]
    }

    void "test removeCaseFromList exception"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.removeCaseFromGeneratedList(0..1){List<CaseCommand> caseCommandList, User user, ExecutedCaseSeries caseSeries ->
            throw new Exception()
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        request.json = JsonOutput.toJson([[caseNumber: "caseNumber", versionNumber: "versionNumber",justification:"justification"]])
        when:
        controller.removeCaseFromList(executedCaseSeries.id)
        then:
        response.json == [message:"default.server.error.message", status:500]
    }

    void "test moveCasesToList success"(){
        int run = 0
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.addCaseToGeneratedList(0..1){CaseCommand caseCommand, User user ->
            run++
            return [] as Set
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.deleteAllCaseSeriesCachedFile(0..1){ExecutedCaseSeries executedCaseSeriesInstance, Boolean refresh = false ->
            run++
        }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        messageSource.addMessage("auditLog.remove.cases", Locale.default, "Case Line Listing Template")
        request.json = JsonOutput.toJson([[caseNumber: "caseNumber", versionNumber: "versionNumber",justification:"justification"]])
        when:
        controller.moveCasesToList(executedCaseSeries.id)
        then:
        run == 2
        flash.message == 'caseCommand.removedToAdd.caseNumbers.success'
        response.json == [success:true, message:"caseCommand.removedToAdd.caseNumbers.success"]
    }

    void "test moveCasesToList failed validation"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        request.json = JsonOutput.toJson([[versionNumber: "versionNumber"]])
        when:
        controller.moveCasesToList(executedCaseSeries.id)
        then:
        response.json == [message:"app.error.fill.all.required", status:500]
    }

    void "test moveCasesToList exception"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(0..1) { -> adminUser }
        controller.userService = userMock.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.addCaseToGeneratedList(0..1){CaseCommand caseCommand, User user ->
            throw new Exception()
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        request.json = JsonOutput.toJson([[caseNumber: "caseNumber", versionNumber: "versionNumber",justification:"justification"]])
        when:
        controller.moveCasesToList(executedCaseSeries.id)
        then:
        response.json == [message:"default.server.error.message", status:500]
    }

    void "test updateTags success"(){
        int run = 0
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveTags(0..1){String caseLevelTags,String globalTags,Long caseNumber,Long cid, String owner ->
            run++
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.deleteAllCaseSeriesCachedFile(0..1){ExecutedCaseSeries executedCaseSeriesInstance, Boolean refresh = false ->
            run++
        }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        controller.auditLogTagsForCase(executedCaseSeries, "Updated Tags for case: 20240100034", "caseLevelTags", "globalTags", "", "")
        when:
        params.caseNumber = "123"
        params.caseLevelTags = ""
        params.globalTags = ""
        controller.updateTags(executedCaseSeries.id)
        then:
        run == 2
        flash.message == 'caseCommand.add.tags.success'
        response.json == [success:true, message:'caseCommand.add.tags.success']
    }

    void "test updateTags exception"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveTags(0..1){String caseLevelTags,String globalTags,Long caseNumber,Long cid, String owner ->
            throw new Exception()
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params.caseNumber = "123"
        params.caseLevelTags = ""
        params.globalTags = ""
        controller.updateTags(executedCaseSeries.id)
        then:
        response.json == [message:"default.server.error.message", status:500]
    }

    void "test updateCommentToCaseNumber success"(){
        int run = 0
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveCaseComment(0..1){Long caseUniqueId, String comments ->
            run++
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.deleteAllCaseSeriesCachedFile(0..1){ExecutedCaseSeries executedCaseSeriesInstance, Boolean refresh = false ->
            run++
        }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        controller.auditLogTagsForCase(executedCaseSeries, "Updated Tags for case: 20240100034", "caseLevelTags", "globalTags", "", "")
        when:
        params.executedCaseSeries = "${executedCaseSeries.id}"
        params.caseNumberUniqueId = "1"
        params.comments = "comments"
        params.caseNumber = "20240100034"
        controller.updateCommentToCaseNumber()
        then:
        run == 2
        flash.message == 'caseCommand.add.comment.success'
        response.json == [success:true, message:'caseCommand.add.comment.success']
    }

    void "test updateCommentToCaseNumber exception"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveCaseComment(0..1){Long caseUniqueId, String comments ->
            throw new Exception()
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params.executedCaseSeries = "${executedCaseSeries.id}"
        params.caseNumberUniqueId = "1"
        params.comments = "comments"
        params.caseNumber = "20240100034"
        controller.updateCommentToCaseNumber()
        then:
        response.json == [message:"default.server.error.message", status:500]
    }

    void "test updateCommentToCaseNumber caseNumberUniqueId 0"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveCaseComment(0..1){Long caseUniqueId, String comments ->
            throw new Exception()
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params.executedCaseSeries = "${executedCaseSeries.id}"
        params.caseNumberUniqueId = "0"
        params.comments = "comments"
        params.caseNumber = "20240100034"
        controller.updateCommentToCaseNumber()
        then:
        response.json == [message:"app.error.fill.all.required", status:500]
    }

    void "test updateCommentToCaseNumber comments null"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.saveCaseComment(0..1){Long caseUniqueId, String comments ->
            throw new Exception()
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params.executedCaseSeries = "${executedCaseSeries.id}"
        params.caseNumberUniqueId = "1"
        params.comments = ""
        params.caseNumber = "20240100034"
        controller.updateCommentToCaseNumber()
        then:
        response.json == [message:"app.error.fill.all.required", status:500]
    }

    void "test fetchAllTags"(){
        boolean run = false
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.getAllTags(0..1){Map params -> [new PvrTags(name: "name",type: "type")]}
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        PvrTags.metaClass.static.withNewSession = {Closure closure -> run = true}
        when:
        params.type = "type"
        controller.fetchAllTags()
        then:
        run == true
    }

    void "test openCasesList"(){
        boolean run = false
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.getOpenCaseOfSeries(0..1){Long caseSeriesId, String caseSeriesOwner->
            run = true
            return [new CaseDTO(caseUniqueId:'caseUniqueId', caseNumber:'caseNumber',versionNumber:'versionNumber', type:'type', productFamily:'productFamily', eventPI:'eventPI', seriousness:'seriousness', outcome:'outcome', listedness:'listedness', causality:'causality', comments:'comments',justification: 'justification', lockedDate:new Date(),eventSequenceNumber:'eventSequenceNumber',eventReceiptDate:new Date(),eventPreferredTerm:'eventPreferredTerm',eventSeriousness:'eventSeriousness',caseId:1,caseSeriesTags:['caseSeriesTags'],globalTags:['globalTags'])]
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        controller.openCasesList(executedCaseSeries)
        then:
        run == true
        response.json[0].size() == 24
    }

    void "test openCasesList not found"(){
        when:
        controller.openCasesList(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/caseSeries/index'
    }

    void "test removedCasesList"(){
        boolean run = false
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(executing: false,seriesName: "seriesName",isTemporary: false,caseSeriesOwner: "owner",numExecutions: 1,owner: adminUser,executedDeliveryOption: new ExecutedCaseDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF,ReportFormatEnum.HTML],sharedWith: [makeNormalUser("user",[])]),tenantId: 1)
        executedCaseSeries.save(failOnError: true,validate:false,flush:true)
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.getRemovedCaseOfSeries(0..1){Long caseSeriesId, String caseSeriesOwner->
            run = true
            return [new CaseDTO(caseUniqueId:'caseUniqueId', caseNumber:'caseNumber',versionNumber:'versionNumber', type:'type', productFamily:'productFamily', eventPI:'eventPI', seriousness:'seriousness', outcome:'outcome', listedness:'listedness', causality:'causality', comments:'comments',justification: 'justification', lockedDate:new Date(),eventSequenceNumber:'eventSequenceNumber',eventReceiptDate:new Date(),eventPreferredTerm:'eventPreferredTerm',eventSeriousness:'eventSeriousness',caseId:1,caseSeriesTags:['caseSeriesTags'],globalTags:['globalTags'])]
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        controller.removedCasesList(executedCaseSeries)
        then:
        run == true
        response.json[0].size() == 24
    }

    void "test removedCasesList not found"(){
        when:
        controller.removedCasesList(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/caseSeries/index'
    }
}
