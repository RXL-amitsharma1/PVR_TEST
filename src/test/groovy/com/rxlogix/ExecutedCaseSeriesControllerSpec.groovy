package com.rxlogix

import com.rxlogix.co.SaveCaseSeriesFromSpotfireCO
import com.rxlogix.config.*
import com.rxlogix.customException.CustomJasperException
import com.rxlogix.enums.*
import com.rxlogix.user.*
import com.rxlogix.util.AuditLogConfigUtil
import grails.gorm.multitenancy.Tenants
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([AuditLogConfigUtil, Tenants, User])
class ExecutedCaseSeriesControllerSpec extends Specification implements DataTest, ControllerUnitTest<ExecutedCaseSeriesController> {

    def setup(){
    }

    def cleanup(){
    }

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant,ExecutedCaseSeries,CaseSeries,EmailConfiguration, ExecutedCaseSeriesUserState, ExecutedTemplateQuery, DateRangeType, ExecutedReportConfiguration, ExecutedPeriodicReportConfiguration, ExecutedDateRangeInformation, ReportTemplate
        AuditLogConfigUtil.metaClass.static.logChanges = {domain, Map newMap, Map oldMap, String eventName, String extraValue ="" , String transactionId = "" -> }
    }

    private User makeNormalUser(name, team, String email = null) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_DEV', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user",email: email?:"abc@gmail.com",enabled: true)
        normalUser.addToTenants(tenant)
        normalUser.save(validate: false)
        UserRole.create(normalUser, userRole, true)
        normalUser.metaClass.isAdmin = { -> return false }
        normalUser.metaClass.getUserTeamIds = { -> team }
        normalUser.metaClass.static.isDev = { -> return false}
        return normalUser
    }

    private User makeAdminUser() {
        User.metaClass.encodePassword = { "password" }
        def preferenceAdmin = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def adminRole = new Role(authority: 'ROLE_ADMIN', createdBy: "user", modifiedBy: "user").save(flush: true)
        def adminUser = new User(username: 'admin', password: 'admin', fullName: "Peter Fletcher", preference: preferenceAdmin, createdBy: "user", modifiedBy: "user")
        adminUser.addToTenants(tenant)
        adminUser.save(failOnError: true)
        UserRole.create(adminUser, adminRole, true)
        adminUser.metaClass.isAdmin = { -> return true }
        adminUser.metaClass.getUserTeamIds = { -> [] }
        adminUser.metaClass.static.isDev = { -> return true}
        return adminUser
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

    void "test show"(){
        int run = 0
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "serie_1",owner: adminUser)
        executedCaseSeries.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(seriesName:"serie_1",owner: adminUser )
        caseSeries.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..2){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockNotificationService = new MockFor(NotificationService)
        mockNotificationService.demand.deleteNotificationByExecutionStatusId(0..1){User user, Long executionStatusId, NotificationApp appName ->
            run++
        }
        controller.notificationService = mockNotificationService.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.debugCaseSeriesSql(0..1){ExecutedCaseSeries executedCaseSeriesInstance, Boolean basicSqlFlag = true ->
            run++
            return [:]
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        params.viewBasicSql = "true"
        params.viewAdvanceSql = "true"
        controller.show(executedCaseSeries)
        then:
        view == '/executedCaseSeries/show'
        run == 2
        model.size() == 3
        model == [seriesInstance:executedCaseSeries, viewSql:[:], caseSeriesInstanceId:2]
    }

    void "test show not found"(){
        when:
        controller.show(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/executedCaseSeries/index'
    }

    void "test show not viewable"(){
        User normalUser = makeNormalUser("user",[], "xyz1@gmail.com")
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "serie_1",owner: makeNormalUser("normalUser",[]),tenantId: 10)
        executedCaseSeries.save(failOnError:true,validate:false)
        CaseSeries caseSeries = new CaseSeries(seriesName:"serie_1",owner: normalUser )
        caseSeries.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        Tenants.metaClass.static.currentId = { -> return 1}
        when:
        controller.show(executedCaseSeries)
        then:
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/executedCaseSeries/index'
    }

    void "test delete"(){
        boolean run = false
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "serie_1",owner: adminUser)
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> adminUser}
        mockUserService.demand.getUser(0..1){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.deleteForAll = "true"
        controller.delete(executedCaseSeries)
        then:
        response.redirectUrl == '/executedCaseSeries/index'
        run == true
        flash.message == 'default.deleted.message'
    }

    void "test delete deleteForAll false"(){
        boolean run = false
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "serie_1",owner: adminUser)
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> adminUser}
        mockUserService.demand.getUser(0..1){ -> adminUser}
        mockUserService.demand.removeUserFromDeliveryOptionSharedWith(0..1){User user, ExecutedCaseDeliveryOption deliveryOption, Long ownerId -> }
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDeleteForUser(0..1){User user, theInstance, name, String justification = null ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        params.deleteForAll = "false"
        controller.delete(executedCaseSeries)
        then:
        response.redirectUrl == '/executedCaseSeries/index'
        run == true
        flash.message == 'default.deleted.message'
    }

    void "test delete not found"(){
        when:
        request.method = 'POST'
        controller.delete(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/executedCaseSeries/index'
    }

    void "test delete not editable"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "serie_1",owner: adminUser)
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> makeNormalUser("user",[], "xyz2@gmail.com")}
        controller.userService = mockUserService.proxyInstance()
        when:
        request.method = 'POST'
        controller.delete(executedCaseSeries)
        then:
        flash.warn == "app.warn.noPermission"
        response.redirectUrl == '/executedCaseSeries/index'
    }

    void "test bindEmailConfiguration update"(){
        boolean run = false
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        emailConfiguration.save(failOnError:true,validate:false)
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(emailConfiguration: emailConfiguration)
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.invokeMethod('bindEmailConfiguration', [executedCaseSeries, [subject: "new_email", body: "new_body"]] as Object[])
        then:
        run == true
        executedCaseSeries.emailConfiguration.subject == "new_email"
        executedCaseSeries.emailConfiguration.body == "new_body"
    }

    void "test bindEmailConfiguration save"(){
        boolean run = false
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.invokeMethod('bindEmailConfiguration', [executedCaseSeries, [subject: "new_email", body: "new_body"]] as Object[])
        then:
        run == true
        executedCaseSeries.emailConfiguration.subject == "new_email"
        executedCaseSeries.emailConfiguration.body == "new_body"
    }

    void "test bindEmailConfiguration delete"(){
        boolean run = false
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        emailConfiguration.save(failOnError:true,validate:false)
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(emailConfiguration: emailConfiguration)
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        controller.invokeMethod('bindEmailConfiguration', [executedCaseSeries, [:]] as Object[])
        then:
        run == true
        executedCaseSeries.emailConfiguration == null
    }

    void "test email"(){
        int run = 0
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.emailReportTo(0..1){def configuration, List<String> recipients, List<ReportFormatEnum> outputs ->
            run++
        }
        controller.emailService = mockEmailService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.executedConfigId = executedCaseSeries.id
        params.attachmentFormats = ["PDF","XML"]
        params.emailToUsers = ["abc@gmail.com"]
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        controller.email()
        then:
        run == 2
        executedCaseSeries.emailConfiguration.subject == "new_email"
        executedCaseSeries.emailConfiguration.body == "new_body"
    }

    void "test email for Exception"(){
        given:
        ExecutedCaseSeries caseSeries = new ExecutedCaseSeries().save(flush: true, failOnError: true, validate: false)
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.emailReportTo(0..1){ExecutedCaseSeries testCaseSeries, List<String> testEmailList, List<ReportFormatEnum> testFormats ->
           throw new CustomJasperException("Exception thrown during emailing Report")
        }

        controller.emailService = mockEmailService.proxyInstance()

        when:
        params.executedConfigId = caseSeries.id
        params.attachmentFormats = ["PDF","XML"]
        params.emailToUsers = ["abc@gmail.com"]
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        controller.email()

        then:
        thrown(Exception)
    }

    void "test checkDeleteForAllAllowed current user admin"(){
        User adminUser = makeAdminUser()
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> adminUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.id = executedCaseSeries.id
        controller.checkDeleteForAllAllowed()
        then:
        response.text == "true"
    }

    void "test checkDeleteForAllAllowed owner equal to current user"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(owner: normalUser)
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.id = executedCaseSeries.id
        controller.checkDeleteForAllAllowed()
        then:
        response.text == "true"
    }

    void "test checkDeleteForAllAllowed false"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(owner: makeNormalUser("normalUser",[], "xyz3@gmail.com"))
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.id = executedCaseSeries.id
        controller.checkDeleteForAllAllowed()
        then:
        response.text == "false"
    }

    void "test sendShareNotification"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        request.setRequestURI("/reports/caseList/index?")
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..1){def recipients, def messageBody, boolean asyVal, String emailSubject ->
            run = true
        }
        controller.emailService = mockEmailService.proxyInstance()
        when:
        controller.invokeMethod('sendShareNotification', [[normalUser] as Set<User>, [] as Set<UserGroup>, executedCaseSeries] as Object[])
        then:
        run == true
    }

    void "test share"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.shareExecutedCaseSeries(0..1){def params, ExecutedCaseSeries executedCaseSeriesInstance ->
            [newUsers : [normalUser] as Set<User>,newGroups : [] as Set<UserGroup> , executedCaseSeries : executedCaseSeries]
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        request.setRequestURI("/reports/caseList/index?")
        def mockEmailService = new MockFor(EmailService)
        mockEmailService.demand.sendNotificationEmail(0..1){def recipients, def messageBody, boolean asyVal, String emailSubject ->
            run = true
        }
        controller.emailService = mockEmailService.proxyInstance()
        when:
        params.executedConfigId = executedCaseSeries.id
        controller.share()
        then:
        run == true
        flash.message == 'app.configuration.shared.successful'
        response.redirectUrl == '/executedCaseSeries/index'
    }

    void "test addEmailConfiguration"(){
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        emailConfiguration.save(failOnError:true,validate:false)
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(emailConfiguration: emailConfiguration)
        executedCaseSeries.save(failOnError:true,validate:false)
        when:
        params.id = executedCaseSeries.id
        controller.addEmailConfiguration()
        then:
        response.json.subject == "email"
        response.json.body == "body"
    }

    void "test addEmailConfiguration null"(){
        when:
        params.id = null
        controller.addEmailConfiguration()
        then:
        response.text == "Not Found"
    }

    void "test archive"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
        executedCaseSeries.save(failOnError:true,validate:false)
        ExecutedCaseSeriesUserState executedCaseSeriesUserState = new ExecutedCaseSeriesUserState(user: normalUser,executedCaseSeries: executedCaseSeries)
        executedCaseSeriesUserState.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.archive(executedCaseSeries)
        then:
        response.redirectUrl == '/executedCaseSeries/index'
        executedCaseSeriesUserState.isArchived == true
    }

    void "test archive when ExecutedCaseSeriesUserState null"(){
        User normalUser = makeNormalUser("user",[])
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.archive(executedCaseSeries)
        then:
        response.redirectUrl == '/executedCaseSeries/index'
        ExecutedCaseSeriesUserState.count() == 1
    }

    void "test archive not found"(){
        when:
        controller.archive(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/executedCaseSeries/index'
    }

    void "test favorite"(){
        boolean run = false
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.setFavorite(0..1){ BaseCaseSeries caseSeries, Boolean isFavorite ->
            run = true
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        when:
        params.id = executedCaseSeries.id
        controller.favorite()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
    }

    void "test favorite null"(){
        boolean run = false
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.setFavorite(0..1){ BaseCaseSeries caseSeries, Boolean isFavorite ->
            run = true
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        when:
        controller.favorite()
        then:
        run == false
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == 'default.not.found.message'
    }

    void "test favorite exception"(){
        boolean run = false
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries()
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.setFavorite(0..1){ BaseCaseSeries caseSeries, Boolean isFavorite ->
            throw new Exception()
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        when:
        params.id = executedCaseSeries.id
        controller.favorite()
        then:
        run == false
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == 'default.server.error.message'
    }

    void "test saveExecutedCaseSeries cid exists where seriesName does not match params seriesName"(){
        boolean run = false
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "series")
        executedCaseSeries.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.cid = "${executedCaseSeries.id}"
        params.seriesName = "name"
        controller.saveExecutedCaseSeries()
        then:
        run == true
        response.json.success == true
    }

    void "test saveExecutedCaseSeries cid exists where seriesName matches params seriesName"(){
        ExecutedCaseSeries executedCaseSeries = new ExecutedCaseSeries(seriesName: "series")
        executedCaseSeries.save(failOnError:true,validate:false)
        when:
        params.cid = "${executedCaseSeries.id}"
        params.seriesName = "series"
        controller.saveExecutedCaseSeries()
        then:
        response.json == [success:false, message:"com.rxlogix.config.executed.caseSeries.seriesName.unique.per.user"]
    }

    void "test saveExecutedCaseSeries cid doesn't exist"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(periodicReportType: PeriodicReportTypeEnum.ACO
                ,executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML]),executionStatus: ReportExecutionStatusEnum.GENERATED_NEW_SECTION,
                executedGlobalQueryValueLists: [],
                clazz: "class",reportName: "report_1",owner: normalUser,createdBy: "user",modifiedBy: "user",
                signalConfiguration: false,tenantId: 1)
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE,
                showTemplateFooter: true,createdBy: "user",modifiedBy: "user")
        reportTemplate.save(failOnError:true)
        ExecutedDateRangeInformation executedDateRangeInformation =new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUMULATIVE)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,executedDateRangeInformationForTemplateQuery: executedDateRangeInformation,
                executedTemplateValueLists: [new ExecutedTemplateValueList(template: reportTemplate)],executedConfiguration: executedReportConfiguration,createdBy:"user",modifiedBy: "user" )
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedDateRangeInformation.executedTemplateQuery = executedTemplateQuery
        executedDateRangeInformation.save(failOnError:true,validate:false)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.getCaseNumberAndVersions(0..1){ ReportResult reportResult ->
            return [new Tuple2('two', 2)]
        }
        controller.reportService = mockReportService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.save(0..1){ExecutedCaseSeries executedCaseSeries, Set caseNumberAndVersion ->
            run = true
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        when:
        params.executedTemplateQueryId = "${executedTemplateQuery.id}"
        params.seriesName = "seriesName"
        controller.saveExecutedCaseSeries()
        then:
        run == true
        response.json.success == true
    }

    void "test saveExecutedCaseSeries exception"(){
        User normalUser = makeNormalUser("user",[])
        DateRangeType dateRangeType = new DateRangeType(name: "daterange")
        dateRangeType.save(failOnError:true)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedPeriodicReportConfiguration(periodicReportType: PeriodicReportTypeEnum.ACO
                ,executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.HTML]),executionStatus: ReportExecutionStatusEnum.GENERATED_NEW_SECTION,
                executedGlobalQueryValueLists: [],
                clazz: "class",reportName: "report_1",owner: normalUser,createdBy: "user",modifiedBy: "user",
                signalConfiguration: false,tenantId: 1)
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE,
                showTemplateFooter: true,createdBy: "user",modifiedBy: "user")
        reportTemplate.save(failOnError:true)
        ExecutedDateRangeInformation executedDateRangeInformation =new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUMULATIVE)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate,executedDateRangeInformationForTemplateQuery: executedDateRangeInformation,
                executedTemplateValueLists: [new ExecutedTemplateValueList(template: reportTemplate)],executedConfiguration: executedReportConfiguration,createdBy:"user",modifiedBy: "user" )
        executedTemplateQuery.save(failOnError:true,validate:false)
        executedDateRangeInformation.executedTemplateQuery = executedTemplateQuery
        executedDateRangeInformation.save(failOnError:true,validate:false)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        executedReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){ -> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockReportService = new MockFor(ReportService)
        mockReportService.demand.getCaseNumberAndVersions(0..1){ ReportResult reportResult ->
            return [new Tuple2('two', 2)]
        }
        controller.reportService = mockReportService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.save(0..1){ExecutedCaseSeries executedCaseSeries, Set caseNumberAndVersion ->
            if (!executedCaseSeries.validate(['seriesName'])) {
                throw new ValidationException("message",executedCaseSeries.errors)
            }
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        when:
        params.executedTemplateQueryId = "${executedTemplateQuery.id}"
        params.seriesName = null
        controller.saveExecutedCaseSeries()
        then:
        response.json.success == false
    }

    void "test saveCaseSeriesForSpotfire successful validation for co and user present"(){
        boolean run = false
        User normalUser = makeNormalUser("user", [])
        def mockUtilService = new MockFor(UtilService)
        mockUtilService.demand.getUserForPVS(1) { String username, boolean isEnabled ->
            normalUser
        }
        controller.utilService = mockUtilService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.save(0..1){ExecutedCaseSeries executedCaseSeries, Set caseNumberAndVersion ->
            run = true
            executedCaseSeries.save(validate:false)
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        when:
        params.seriesName = "seriesName"
        controller.saveCaseSeriesForSpotfire(new SaveCaseSeriesFromSpotfireCO(user: "user",seriesName: "series",caseNumbers: "caseNumbers",tenantId: 1))
        then:
        run == true
        response.json == [data:[id:1], message:"default.created.message", status:true]
    }

    void "test saveCaseSeriesForSpotfire successful validation for co and user present validation exception"(){
        User normalUser = makeNormalUser("user",[])
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.save(0..1){ExecutedCaseSeries executedCaseSeries, Set caseNumberAndVersion ->
            if (!executedCaseSeries.validate(['seriesName'])) {
                throw new ValidationException("message",executedCaseSeries.errors)
            }
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        def mockUtilService = new MockFor(UtilService)
        mockUtilService.demand.getUserForPVS(0..1){String username, boolean isEnabled ->
             return normalUser
        }
        controller.utilService = mockUtilService.proxyInstance()
        when:
        params.seriesName = null
        controller.saveCaseSeriesForSpotfire(new SaveCaseSeriesFromSpotfireCO(user: "user",seriesName: "series",caseNumbers: "caseNumbers",tenantId: 1))
        then:
        response.json.status == false
        response.json.data == null
    }

    void "test saveCaseSeriesForSpotfire unsuccessful validation"(){
        when:
        controller.saveCaseSeriesForSpotfire(new SaveCaseSeriesFromSpotfireCO(seriesName: "series",caseNumbers: "caseNumbers",tenantId: 1))
        then:
        response.json == [data:null, message:"app.error.fill.all.required", status:false]
    }

    void "test saveCaseSeriesForSpotfire successful validation for co and user not present"(){
        given:
        User normalUser = makeNormalUser("user",[])
        def mockUtilService = new MockFor(UtilService)
        mockUtilService.demand.getUserForPVS(0..1){String username, boolean isEnabled ->
            return null
        }
        controller.utilService = mockUtilService.proxyInstance()
        when:
        controller.saveCaseSeriesForSpotfire(new SaveCaseSeriesFromSpotfireCO(user: "user",seriesName: "series",caseNumbers: "caseNumbers",tenantId: 1))
        then:
        response.json == [data:null, message:"case.series.spotfire.user.not.exist", status:false]
    }

    void "test saveCaseSeriesForSpotfire successful validation for co and user present exception"(){
        User normalUser = makeNormalUser("user",[])
        def mockUtilService = new MockFor(UtilService)
        mockUtilService.demand.getUserForPVS(1) { String username, boolean isEnabled ->
            normalUser
        }
        controller.utilService = mockUtilService.proxyInstance()
        def mockCaseSeriesService = new MockFor(CaseSeriesService)
        mockCaseSeriesService.demand.save(0..1){ExecutedCaseSeries executedCaseSeries, Set caseNumberAndVersion ->
            throw new Exception()
        }
        controller.caseSeriesService = mockCaseSeriesService.proxyInstance()
        when:
        params.seriesName = "seriesName"
        controller.saveCaseSeriesForSpotfire(new SaveCaseSeriesFromSpotfireCO(user: "user",seriesName: "series",caseNumbers: "caseNumbers",tenantId: 1))
        then:
        response.json == [data:null, message:null, status:false]
    }
}
