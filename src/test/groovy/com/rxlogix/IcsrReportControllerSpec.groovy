package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.*
import com.rxlogix.file.MultipartFileSender
import com.rxlogix.user.*
import grails.gorm.multitenancy.Tenants
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationErrors
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import org.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ConfineMetaClassChanges([MultipartFileSender, User, Tenants, ExecutedTemplateQuery, MultipartFileSender, UserGroup, IcsrReportConfiguration, SpringSecurityUtils, SuperQuery])
class IcsrReportControllerSpec extends Specification implements DataTest, ControllerUnitTest<IcsrReportController> {

    def setup() {
    }

    def cleanup() {
    }

    def setupSpec() {
        mockDomains User, UserGroup, UserGroupUser, Role, UserRole, Tenant, Preference, ReportField, SuperQuery, ParameterValue, IcsrReportConfiguration, QueryValueList, CaseSeriesDateRangeInformation, Tag, DeliveryOption, EmailConfiguration, ExecutionStatus, DmsConfiguration, ReportResult,ExecutedTemplateQuery,ExecutedReportConfiguration,CustomSQLValue,ExecutedQueryValueList,ExecutedTemplateValueList,TemplateQuery,TemplateValueList,DateRangeInformation,IcsrTemplateQuery,ReportTask,PeriodicReportConfiguration,ReportTemplate,Query,ExecutedIcsrReportConfiguration,WorkflowState, QueryExpressionValue, SourceProfile
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
        normalUser.metaClass.static.isDev = { -> return false}
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
        adminUser.metaClass.isAdmin = { -> return true }
        adminUser.metaClass.getUserTeamIds = { -> [] }
        adminUser.metaClass.static.isDev = { -> return true}
        return adminUser
    }

    void "test bindDmsConfiguration new Instance"(){
        boolean run = false
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration()
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockCrudService = new MockFor(CRUDService)
        mockCrudService.demand.save(0..1){ theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCrudService.proxyInstance()
        Map emailData = ['format': ReportFormatEnum.PDF]
        when:
        controller.invokeMethod('bindDmsConfiguration', [icsrReportConfiguration, emailData] as Object[])
        then:
        run == true
        icsrReportConfiguration.dmsConfiguration.format == ReportFormatEnum.PDF
    }

    void "test bindDmsConfiguration"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(dmsConfiguration: new DmsConfiguration())
        icsrReportConfiguration.save(failOnError:true,validate:false)
        Map emailData = ['format': ReportFormatEnum.PDF]
        when:
        controller.invokeMethod('bindDmsConfiguration', [icsrReportConfiguration, emailData] as Object[])
        then:
        DmsConfiguration.count() == 1
    }

    void "test bindEmailConfiguration update"(){
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        emailConfiguration.save(failOnError:true,validate:false)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(emailConfiguration: emailConfiguration)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        controller.invokeMethod('bindEmailConfiguration', [icsrReportConfiguration, [subject: "new_email",body: "new_body"]] as Object[])
        then:
        icsrReportConfiguration.emailConfiguration.subject == "new_email"
        icsrReportConfiguration.emailConfiguration.body == "new_body"
    }

    void "test reports"(){
        when:
        controller.reports(1L)
        then:
        response.status==200
    }

    void "test showResult,When Instance exists "(){
        given:
        ReportResult reportResult= new ReportResult(id: 1L)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(id: 2L, finalReportResult: reportResult)
        ExecutedIcsrReportConfiguration executedConfiguration=new ExecutedIcsrReportConfiguration(id: 1L, executedTemplateQueries: [executedTemplateQuery])
        ExecutedIcsrReportConfiguration.metaClass.static.read ={Long id -> executedConfiguration}
        ExecutedTemplateQuery.metaClass.static.getReportResult = {return reportResult }
        when:
        controller.showResult(1L)
        then:
        response.status==200
        response.forwardedUrl == "/icsrReport/show"
    }
    void "test showResult,When Instance does not exist "(){
        given:
        ReportResult reportResult= new ReportResult(id: 1L)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(id: 2L, finalReportResult: reportResult)
        ExecutedIcsrReportConfiguration executedConfiguration=new ExecutedIcsrReportConfiguration(id: 1L, executedTemplateQueries: [executedTemplateQuery])
        ExecutedIcsrReportConfiguration.metaClass.static.read ={Long id -> null}
        ExecutedTemplateQuery.metaClass.static.getReportResult = {return reportResult }
        when:
        controller.showResult(1L)
        then:
        response.status==302
        response.redirectUrl == "/icsrReport/index"
    }

    void "test bindEmailConfiguration save"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration()
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        controller.bindEmailConfiguration(icsrReportConfiguration,[subject: "new_email",body: "new_body"])
        then:
        icsrReportConfiguration.emailConfiguration.subject == "new_email"
        icsrReportConfiguration.emailConfiguration.body == "new_body"
    }

    void "test bindEmailConfiguration delete"(){
        EmailConfiguration emailConfiguration = new EmailConfiguration(subject: "email",body: "body")
        emailConfiguration.save(failOnError:true,validate:false)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(emailConfiguration: emailConfiguration)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        controller.bindEmailConfiguration(icsrReportConfiguration,[:])
        then:
        icsrReportConfiguration.emailConfiguration == null
    }

    void "test setExecutedDateRangeInformation"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrReportConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM),executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        when:
        controller.setExecutedDateRangeInformation(executedTemplateQuery)
        then:
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute == executedReportConfiguration.executedGlobalDateRangeInformation.dateRangeEndAbsolute
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute == executedReportConfiguration.executedGlobalDateRangeInformation.dateRangeStartAbsolute
    }

    void "test assignParameterValuesToTemplateQuery"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new Query(name: 'Test Query',queryExpressionValues:[ new QueryExpressionValue(reportField:reportField,operator:QueryOperatorEnum.EQUALS,key:'key').save(failOnError:true,validate:false)])
        superQuery.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        when:
        params["qev[0].key"] = "true"
        params["tv[0].key"] = "key"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["tv[0].value"] = "value"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].operator"] = "TOMORROW"
        params["qev[0].field"] = "report"
        params["template"] = new ReportTemplate()
        controller.assignParameterValuesToTemplateQuery(executedTemplateQuery)
        then:
        executedTemplateQuery.executedQueryValueLists[0].parameterValues[0].reportField.name == "report"
        executedTemplateQuery.executedTemplateValueLists[0].parameterValues[0].key == "key"
        executedTemplateQuery.executedTemplateValueLists[0].parameterValues[0].value == "value"
    }

    void "test assignParameterValuesToTemplateQuery CustomSQLValue"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new Query(name: 'Test Query',queryExpressionValues:[ new QueryExpressionValue(reportField:reportField,operator:QueryOperatorEnum.EQUALS,key:'key').save(failOnError:true,validate:false)])
        superQuery.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        when:
        params["qev[0].key"] = "true"
        params["tv[0].key"] = "key"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["tv[0].value"] = "value"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].operator"] = "TOMORROW"
        params["template"] = new ReportTemplate()
        controller.assignParameterValuesToTemplateQuery(executedTemplateQuery)
        then:
        executedTemplateQuery.executedQueryValueLists[0].parameterValues[0].key == "true"
        executedTemplateQuery.executedQueryValueLists[0].parameterValues[0].value == "newValue"
        executedTemplateQuery.executedTemplateValueLists[0].parameterValues[0].key == "key"
        executedTemplateQuery.executedTemplateValueLists[0].parameterValues[0].value == "value"
    }

    void "test assignParameterValuesToTemplateQuery null"(){
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new Query(name: 'Test Query',queryExpressionValues:[ new QueryExpressionValue(reportField:reportField,operator:QueryOperatorEnum.EQUALS,key:'key').save(failOnError:true,validate:false)])
        superQuery.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedQueryValueLists: [new ExecutedQueryValueList(parameterValues: [parameterValue])],executedTemplateValueLists: [new ExecutedTemplateValueList(parameterValues: [customSQLValue])],executedConfiguration: executedReportConfiguration)
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        when:
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["tv[0].value"] = "value"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].operator"] = "TOMORROW"
        params["template"] = new ReportTemplate()
        controller.assignParameterValuesToTemplateQuery(executedTemplateQuery)
        then:
        executedTemplateQuery.executedQueryValueLists.size() == 0
        executedTemplateQuery.executedTemplateValueLists.size() == 0
    }

    void "test assignParameterValuesToTemplateQuery TemplateQuery"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new Query(name: 'Test Query',queryExpressionValues:[ new QueryExpressionValue(reportField:reportField,operator:QueryOperatorEnum.EQUALS,key:'key').save(failOnError:true,validate:false)])
        superQuery.save(failOnError:true,validate:false,flush:true)
        TemplateQuery templateQuery = new TemplateQuery(queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])])
        templateQuery.save(failOnError:true,validate:false,flush:true)
        when:
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        controller.assignParameterValuesToTemplateQuery(icsrReportConfiguration,templateQuery,0)
        then:
        templateQuery.queryValueLists[0].parameterValues[0].reportField.name == "report"
        templateQuery.templateValueLists[0].parameterValues[0].key == "key"
        templateQuery.templateValueLists[0].parameterValues[0].value == "value"
    }

    void "test assignParameterValuesToTemplateQuery TemplateQuery CustomSQLValue"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new Query(name: 'Test Query',queryExpressionValues:[ new QueryExpressionValue(reportField:reportField,operator:QueryOperatorEnum.EQUALS,key:'key').save(failOnError:true,validate:false)])
        superQuery.save(failOnError:true,validate:false,flush:true)
        TemplateQuery templateQuery = new TemplateQuery(queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])])
        templateQuery.save(failOnError:true,validate:false,flush:true)
        when:
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQueries[0].template"] = new ReportTemplate()
        controller.assignParameterValuesToTemplateQuery(icsrReportConfiguration,templateQuery,0)
        then:
        templateQuery.queryValueLists[0].parameterValues[0].key == "true"
        templateQuery.templateValueLists[0].parameterValues[0].key == "key"
        templateQuery.templateValueLists[0].parameterValues[0].value == "value"
    }

    void "test setDateRangeInformation"(){
        User normalUser = makeNormalUser("user",[])
        DateRangeInformation dateRangeInformation = new DateRangeInformation()
        dateRangeInformation.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUSTOM"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        controller.invokeMethod('setDateRangeInformation', [0, dateRangeInformation, new IcsrReportConfiguration()] as Object[])
        then:
        dateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM
        dateRangeInformation.dateRangeStartAbsolute != null
        dateRangeInformation.dateRangeEndAbsolute != null
    }

    void "test setDateRangeInformation null"(){
        DateRangeInformation dateRangeInformation = new DateRangeInformation()
        dateRangeInformation.save(failOnError:true,validate:false,flush:true)
        when:
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        controller.invokeMethod('setDateRangeInformation', [0, dateRangeInformation, new IcsrReportConfiguration()] as Object[])
        then:
        dateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE
        dateRangeInformation.dateRangeStartAbsolute == null
        dateRangeInformation.dateRangeEndAbsolute == null
    }

    void "test bindExistingTemplateQueryEdits"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new Query(name: 'Test Query',queryExpressionValues:[ new QueryExpressionValue(reportField:reportField,operator:QueryOperatorEnum.EQUALS,key:'key').save(failOnError:true,validate:false)])
        superQuery.save(failOnError:true,validate:false,flush:true)
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        controller.userService = mockUserService.proxyInstance()
        when:
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        controller.invokeMethod('bindExistingTemplateQueryEdits', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUMULATIVE
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == null
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == null
        icsrReportConfiguration.templateQueries[0].queryValueLists[0].parameterValues[0].reportField.name == "report"
        icsrReportConfiguration.templateQueries[0].templateValueLists[0].parameterValues[0].key == "key"
        icsrReportConfiguration.templateQueries[0].templateValueLists[0].parameterValues[0].value == "value"
    }

    void "test bindTemplatePOIInputs"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(poiInputsParameterValues: [])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        controller.invokeMethod('bindTemplatePOIInputs', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.poiInputsParameterValues.size() == 1
    }

    void "test bindNewTemplateQueries"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
        icsrReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new Query(name: 'Test Query',queryExpressionValues:[ new QueryExpressionValue(reportField:reportField,operator:QueryOperatorEnum.EQUALS,key:'key').save(failOnError:true,validate:false)])
        superQuery.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        controller.userService = mockUserService.proxyInstance()
        when:
        params["templateQueries[0].id"] = 1
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.relativeDateRangeValue"] = "20"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params["templateQueries[0].new"] = "true"
        params["templateQueries[0].dynamicFormEntryDeleted"] = "false"
        controller.invokeMethod('bindNewTemplateQueries', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUMULATIVE
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == null
        icsrReportConfiguration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == null
        icsrReportConfiguration.templateQueries[0].queryValueLists[0].parameterValues[0].reportField.name == "report"
        icsrReportConfiguration.templateQueries[0].templateValueLists[0].parameterValues[0].key == "key"
        icsrReportConfiguration.templateQueries[0].templateValueLists[0].parameterValues[0].value == "value"
    }

    void "test bindAsOfVersionDate"(){
        User normalUser = makeNormalUser("user",[])
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('bindAsOfVersionDate', [icsrReportConfiguration, "20-Mar-2016 "] as Object[])
        then:
        icsrReportConfiguration.includeLockedVersion == true
        icsrReportConfiguration.asOfVersionDate != null
    }

    void "test bindAsOfVersionDate null"(){
        User normalUser = makeNormalUser("user",[])
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.invokeMethod('bindAsOfVersionDate', [icsrReportConfiguration, "20-Mar-2016 "] as Object[])
        then:
        icsrReportConfiguration.includeLockedVersion == false
        icsrReportConfiguration.asOfVersionDate == null
    }

    void "test setAttributeTags"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration()
        icsrReportConfiguration.save(failOnError:true,validate:false)
        Tag tag = new Tag(name: "oldTag")
        tag.save(failOnError:true,validate:false,flush:true)
        when:
        params.tags = ["oldTag","newTag"]
        controller.invokeMethod('setAttributeTags', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.tags.size() == 2
    }

    void "test bindReportTasks"(){
        ReportTask reportTask = new ReportTask(description: "oldTask")
        reportTask.save(failOnError:true,validate:false)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(reportTasks: [reportTask] as Set)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        when:
        controller.invokeMethod('bindReportTasks', [icsrReportConfiguration,[:]] as Object[])
        then:
        icsrReportConfiguration.reportTasks.size() == 1
        icsrReportConfiguration.reportTasks[0].description == "newTask"
    }

    void "test clearListFromConfiguration"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"])
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        controller.invokeMethod('clearListFromConfiguration', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.deliveryOption.emailToUsers.size() == 0
        icsrReportConfiguration.deliveryOption.attachmentFormats.size() == 0
        icsrReportConfiguration.poiInputsParameterValues.size() == 0
        icsrReportConfiguration.tags.size() == 0
        icsrReportConfiguration.reportingDestinations.size() == 0
    }

    void "test setNextRunDateAndScheduleDateJSON"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(scheduleDateJSON: """{"startDateTime":"2021-02-18T01:00Z","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1"}
""",isEnabled: true)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.invokeMethod('setNextRunDateAndScheduleDateJSON', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.nextRunDate != null
    }


    void "test setNextRunDateAndScheduleDateJSON null"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(scheduleDateJSON: "true",isEnabled: false)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.invokeMethod('setNextRunDateAndScheduleDateJSON', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.nextRunDate == null
    }

    void "test setNextRunDateAndScheduleDateJSON scheduleDateJSON"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(scheduleDateJSON: "",isEnabled: false)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.invokeMethod('setNextRunDateAndScheduleDateJSON', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.nextRunDate == null
    }

    void "test setNextRunDateAndScheduleDateJSON MiscUtil"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(scheduleDateJSON: "FREQ=WEEKLY",isEnabled: false)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.invokeMethod('setNextRunDateAndScheduleDateJSON', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.nextRunDate == null
    }

    void "test setReportingDestinations"(){
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(primaryReportingDestination: "primary")
        icsrReportConfiguration.save(failOnError:true,validate:false)
        when:
        params.reportingDestinations = "primary@!reporting@!destination"
        controller.invokeMethod('setReportingDestinations', [icsrReportConfiguration] as Object[])
        then:
        icsrReportConfiguration.reportingDestinations.size() == 2
    }

    void "test populateModel"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new Query(name: 'Test Query',queryExpressionValues:[ new QueryExpressionValue(reportField:reportField,operator:QueryOperatorEnum.EQUALS,key:'key').save(failOnError:true,validate:false)])
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),primaryReportingDestination: "primary",deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"],evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF,scheduleDateJSON: "true",isEnabled: true)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1) { GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration periodicReportConfiguration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        when:
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.tags = ["oldTag","newTag"]
        params.reportingDestinations = "primary@!reporting@!destination"
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
        params.asOfVersionDate = "20-Mar-2016 "
        controller.invokeMethod('populateModel', [icsrReportConfiguration] as Object[])
        then:
        run == 1
        icsrReportConfiguration.templateQueries.size() == 1
        icsrReportConfiguration.globalQueryValueLists.size() == 1
    }

    void "test downloadBulkXML"(){
        int run = 0
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.createBulkXMLReport(0..1){ ReportResult reportResult, List<String> caseNumbers, IcsrReportSpecEnum reportSpec->
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
        controller.downloadBulkXML(new ReportResult())
        then:
        run == 2
    }

    void "test downloadBatchXML"(){
        int run = 0
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.createBatchXMLReport(0..1){ ReportResult reportResult, List<String> caseNumbers, IcsrReportSpecEnum reportSpec->
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
        controller.downloadBatchXML(new ReportResult())
        then:
        run == 2
    }

    void "test createFromTemplate"(){
        User normalUser = makeNormalUser("user",[])
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(reportName: "icsr")
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.copyConfig(0..1){ IcsrReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false-> return icsrReportConfiguration}
        controller.configurationService = mockConfigurationService.proxyInstance()
        Tenants.metaClass.static.currentId = { -> return 10}
        when:
        controller.createFromTemplate(icsrReportConfiguration)
        then:
        flash.message == "app.copy.success"
        response.redirectUrl == '/icsrReport/edit/1?fromTemplate=true'
    }


    void "test createFromTemplate not found"(){
        when:
        controller.createFromTemplate(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/icsrReport/index'
    }

//    instance do not match with function call
//    void "test editField success globalDateRangeInformation"(){
//        boolean run = false
//        ReportField reportField = new ReportField(name: "report")
//        reportField.save(failOnError:true,validate:false,flush:true)
//        SuperQuery superQuery = new Query(name: 'Test Query',queryExpressionValues:[ new QueryExpressionValue(reportField:reportField,operator:QueryOperatorEnum.EQUALS,key:'key').save(failOnError:true,validate:false)])
//        superQuery.save(failOnError:true,validate:false,flush:true)
//        ParameterValue parameterValue = new ParameterValue()
//        parameterValue.save(failOnError:true,validate:false,flush:true)
//        ReportConfiguration executedReportConfiguration = new IcsrReportConfiguration(description: "description",reportName: "report_1",dateCreated: new Date(),globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)])
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
//                Integer getParameterSize(){
//                    return 1
//                }
//            }
//        }
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.update(0..1){theInstance ->
//            run = true
//            return theInstance
//        }
//        controller.CRUDService = mockCRUDService.proxyInstance()
//        when:
//        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
//        params["qev[0].key"] = "true"
//        params["validQueries"] = "${superQuery.id}"
//        params["qev[0].value"] = "true"
//        params["qev[0].specialKeyValue"] = "specialKeyValue"
//        params["qev[0].copyPasteValue"] = "ParameterValue"
//        params["qev[0].isFromCopyPaste"] = "true"
//        params["qev[0].field"] = "report"
//        params["qev[0].operator"] = "TOMORROW"
//        params.id = executedReportConfiguration.id
//        controller.editField()
//        then:
//        run == true
//        executedReportConfiguration.globalQueryValueLists[0].parameterValues[0].reportField.name == "report"
//        response.json == []
//    }

    void "test editField reportName"(){
        boolean run = false
        IcsrReportConfiguration periodicReportConfiguration = new IcsrReportConfiguration(reportName: "report")
        periodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        params.reportName = "report_new"
        controller.editField()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
    }

//    void "test editField reportName validation false"(){
//        User normalUser = makeNormalUser("user",[])
//        boolean run = false
//        PeriodicReportConfiguration periodicReportConfiguration = new PeriodicReportConfiguration(reportName: "report",owner: normalUser)
//        periodicReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration(owner: normalUser)
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.update(0..1){theInstance ->
//            run = true
//            return theInstance
//        }
//        controller.CRUDService = mockCRUDService.proxyInstance()
//        when:
//        params.id = executedReportConfiguration.id
//        params.reportName = "report"
//        controller.editField()
//        then:
//        run == false
//        response.json == []
//    }

    void "test editField scheduleDateJSON"(){
        boolean run = false
        User normalUser = makeNormalUser("user",[])
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.parseScheduler(0..1){String s, locale ->
            return "label"
        }
        controller.icsrReportService = mockIcsrReportService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        params.scheduleDateJSON = "scheduleDateJSON"
        controller.editField()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
        response.json.data == [json:"scheduleDateJSON", label:"label"]
    }

    void "test editField"(){
        boolean run = false
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.editField()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
    }

    void "test editField exception"(){
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.editField()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test editField no instance"(){
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.editField()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

    void "test ajaxDelete no instance"(){
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.ajaxDelete()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

    void "test ajaxDelete not editable"(){
        User normalUser = makeNormalUser("user",[])
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration(tenantId: 1)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        Tenants.metaClass.static.currentId = { -> return 10}
        UserGroup.metaClass.static.fetchAllUserGroupByUser={User user->[]}
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxDelete()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "app.configuration.delete.permission"
    }

    void "test ajaxDelete success"(){
        boolean run = false
        User adminUser = makeAdminUser()
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxDelete()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
        response.json.message == 'default.deleted.message'
    }

    void "test ajaxDelete validation exception"(){
        User adminUser = makeAdminUser()
        UserGroup.metaClass.static.fetchAllUserGroupByUser={User user->[]}
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxDelete()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test ajaxDelete exception"(){
        User adminUser = makeAdminUser()
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxDelete()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test ajaxRun no instance"(){
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.ajaxRun()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

    void "test ajaxRun run exists"(){
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration(isEnabled: true,nextRunDate: new Date())
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxRun()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == 'app.configuration.run.exists'
    }
//    instance not matching in setNextRunDateAndScheduleDateJSON
//    void "test ajaxRun success"(){
//        boolean run = false
//        User adminUser = makeAdminUser()
//        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration(scheduleDateJSON: "true",isEnabled: false)
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        def mockConfigurationService = new MockFor(ConfigurationService)
//        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
//        controller.configurationService = mockConfigurationService.proxyInstance()
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.update(0..1){theInstance ->
//            run = true
//            return theInstance
//        }
//        controller.CRUDService = mockCRUDService.proxyInstance()
//        def mockUserService = new MockFor(UserService)
//        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
//        controller.userService = mockUserService.proxyInstance()
//        when:
//        params.id = executedReportConfiguration.id
//        controller.ajaxRun()
//        then:
//        run == true
//        response.json == []
//    }
//
//    void "test ajaxRun validation exception"(){
//        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        def mockConfigurationService = new MockFor(ConfigurationService)
//        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
//        controller.configurationService = mockConfigurationService.proxyInstance()
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.update(0..1){theInstance ->
//            throw new ValidationException("message",new ValidationErrors(new Object()))
//        }
//        controller.CRUDService = mockCRUDService.proxyInstance()
//        when:
//        params.id = executedReportConfiguration.id
//        controller.ajaxRun()
//        then:
//        response.json.httpCode == 500
//        response.json.status == false
//    }
//
//    void "test ajaxRun exception"(){
//        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
//        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
//        def mockConfigurationService = new MockFor(ConfigurationService)
//        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
//        controller.configurationService = mockConfigurationService.proxyInstance()
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.update(0..1){theInstance ->
//            throw new Exception()
//        }
//        controller.CRUDService = mockCRUDService.proxyInstance()
//        when:
//        params.id = executedReportConfiguration.id
//        controller.ajaxRun()
//        then:
//        response.json.httpCode == 500
//        response.json.status == false
//    }

    void "test ajaxCopy no instance"(){
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.ajaxCopy()
        then:
        response.json.httpCode == 500
        response.json.status == false
        response.json.message == "default.not.found.message"
    }

    void "test ajaxCopy success"(){
        boolean run = false
        User adminUser = makeAdminUser()
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.copyConfig(0..1){IcsrReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false->
            return new IcsrReportConfiguration()
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.toBulkTableMap(0..1){IcsrReportConfiguration conf ->
            run = true
            return [:]
        }
        controller.icsrReportService = mockIcsrReportService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxCopy()
        then:
        run == true
        response.json.httpCode == 200
        response.json.status == true
    }

    void "test ajaxCopy validation error"(){
        User adminUser = makeAdminUser()
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.copyConfig(0..1){PeriodicReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxCopy()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test ajaxCopy exception"(){
        User adminUser = makeAdminUser()
        PeriodicReportConfiguration executedReportConfiguration = new PeriodicReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.copyConfig(0..1){PeriodicReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false->
            throw new Exception()
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        params.id = executedReportConfiguration.id
        controller.ajaxCopy()
        then:
        response.json.httpCode == 500
        response.json.status == false
    }

    void "test importExcel"(){
        int run = 0
        def multipartFile = new GrailsMockMultipartFile('file', 'reportFile.pdf', '', new byte[0])
        request.addFile(multipartFile)
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.importFromExcel(0..1){ workbook->
            run++
            return  [errors:[],added:[],updated :[]]
        }
        mockIcsrReportService.demand.getDisplayMessage(0..2){ String code, List reportNames->
            run++
            return  "message generated!"
        }
        controller.icsrReportService = mockIcsrReportService.proxyInstance()
        when:
        controller.importExcel()
        then:
        run == 3
        response.redirectUrl == '/icsrReport/bulkUpdate'
        flash.message != null
    }

    void "test importExcel errors"(){
        int run = 0
        def multipartFile = new GrailsMockMultipartFile('file', 'reportFile.pdf', '', new byte[0])
        request.addFile(multipartFile)
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.importFromExcel(0..1){ workbook->
            run++
            return  [errors:["error"],added:[],updated :[]]
        }
        mockIcsrReportService.demand.getDisplayMessage(0..2){ String code, List reportNames->
            run++
            return  "message generated!"
        }
        controller.icsrReportService = mockIcsrReportService.proxyInstance()
        when:
        controller.importExcel()
        then:
        run == 3
        response.redirectUrl == '/icsrReport/bulkUpdate'
        flash.error != null
    }

    void "test exportToExcel"() {
        given:
        def userMock = new MockFor(UserService)
        userMock.demand.getCurrentUser(1..3) { -> return new User() }
        controller.userService = userMock.proxyInstance()
        IcsrReportConfiguration.metaClass.static.fetchAllIdsForBulkUpdate = { LibraryFilter filter ->
            new Object() {
                List list(Object o) {
                    [0]
                }
            }
        }
        IcsrReportConfiguration.metaClass.static.getAll = { List<Long> idsForUser ->
            [
                    new IcsrReportConfiguration(id: 1, reportName: "test", periodicReportType: PeriodicReportTypeEnum.JPSR,
                            globalDateRangeInformation: new GlobalDateRangeInformation(), productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}',
                            scheduleDateJSON: 'scheduleDateJSON', primaryReportingDestination: "dest", dueInDays: 1
                    ),
                    new IcsrReportConfiguration(id: 2, reportName: "test2", periodicReportType: PeriodicReportTypeEnum.JPSR,
                            globalDateRangeInformation: new GlobalDateRangeInformation(), productSelection: '{"1":[{"name":"AZASPIRIUM CHLORIDE","id":2886},{"name":"ASPIRIN ALUMINIUM","id":6001}],"2":[],"3":[],"4":[]}',
                            scheduleDateJSON: 'scheduleDateJSON', primaryReportingDestination: "dest", dueInDays: 1
                    )
            ]
        }

        controller.qualityService = new QualityService()
        def resultData
        controller.qualityService.metaClass.exportToExcel = { data, metadata ->
            resultData = data
            new byte[0]
        }
        when:
        controller.exportToExcel()
        then:
        resultData.size() == 2
        resultData[1][0] == "test2"
        resultData[1][1] == ""
        resultData[1][2] == "AZASPIRIUM CHLORIDE,ASPIRIN ALUMINIUM"
        resultData[1][3] == ""
        resultData[1][4] == ""
        resultData[1][5] == ""
        resultData[1][6] == "JPSR"
        resultData[1][7] == "CUMULATIVE"
        resultData[1][8] == 1
        resultData[1][9] == null
        resultData[1][10] == null
        resultData[1][11] == "dest"
        resultData[1][12] == 1
        resultData[1][13] == "scheduleDateJSON"
    }

    void "test runOnce success"(){
        boolean run = false
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration(nextRunDate: new Date(),isEnabled: false)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.scheduleToRunOnce(0..1){ IcsrReportConfiguration periodicReportConfiguration->
            run = true
        }
        controller.icsrReportService = mockIcsrReportService.proxyInstance()
        when:
        controller.runOnce(executedReportConfiguration)
        then:
        run == true
        flash.message == 'app.Configuration.RunningMessage'
        response.redirectUrl == '/executionStatus/list'
    }

    void "test runOnce not found"(){
        when:
        controller.runOnce(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/icsrReport/index'
    }

    void "test runOnce exists"(){
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration(nextRunDate: new Date(),isEnabled: true)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        when:
        controller.runOnce(executedReportConfiguration)
        then:
        flash.warn == 'app.configuration.run.exists'
        response.redirectUrl == '/icsrReport/index'
    }

    void "test runOnce validation exception"(){
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration(nextRunDate: new Date(),isEnabled: false)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.scheduleToRunOnce(0..1){ IcsrReportConfiguration periodicReportConfiguration->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> makeAdminUser()}
        controller.icsrReportService = mockIcsrReportService.proxyInstance()
        controller.userService = mockUserService.proxyInstance()
        when:
        controller.runOnce(executedReportConfiguration)
        then:
        view == '/icsrReport/create'
    }

    void "test run success update"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),primaryReportingDestination: "primary",deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"],evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF,scheduleDateJSON: "true",isEnabled: true)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1) { GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration periodicReportConfiguration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            return theInstance
        }
        mockCRUDService.demand.update(0..1){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        when:
        request.method = 'POST'
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.tags = ["oldTag","newTag"]
        params.reportingDestinations = "primary@!reporting@!destination"
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
        params.asOfVersionDate = "20-Mar-2016 "
        controller.run(icsrReportConfiguration.id)
        then:
        run == 2
        flash.message == 'app.Configuration.RunningMessage'
        response.redirectUrl == '/executionStatus/list'
    }

    void "test run success save"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1) { GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration periodicReportConfiguration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        when:
        request.method = 'POST'
        params.globalQueryValueLists = [new QueryValueList(parameterValues: [parameterValue])]
        params.globalDateRangeInformation = new GlobalDateRangeInformation()
        params.primaryReportingDestination = "primary"
        params.deliveryOption = new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF])
        params.tags = [new Tag()]
        params.poiInputsParameterValues = [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)]
        params.reportingDestinations = ["reportingDestination"]
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.scheduleDateJSON = "true"
        params.isEnabled =  true
        params.templateQueries = [templateQuery]
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.tags = ["oldTag","newTag"]
        params.reportingDestinations = "primary@!reporting@!destination"
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
        params.asOfVersionDate = "20-Mar-2016 "
        controller.run(10)
        then:
        run == 3
        flash.message == 'app.Configuration.RunningMessage'
        response.redirectUrl == '/executionStatus/list'
    }

    void "test run success validation exception update"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),primaryReportingDestination: "primary",deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"],evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF,scheduleDateJSON: "true",isEnabled: true)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}

        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1) { GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration periodicReportConfiguration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            return theInstance
        }
        mockCRUDService.demand.update(0..1){theInstance ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        when:
        request.method = 'POST'
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.tags = ["oldTag","newTag"]
        params.reportingDestinations = "primary@!reporting@!destination"
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
        params.asOfVersionDate = "20-Mar-2016 "
        controller.run(icsrReportConfiguration.id)
        then:
        run == 1
        view == '/icsrReport/edit'
    }

//    void "test run validation exception save"(){
//        int run = 0
//        User normalUser = makeNormalUser("user",[])
//        UserGroup userGroup = new UserGroup()
//        userGroup.save(failOnError:true,validate:false)
//        ReportField reportField = new ReportField(name: "report")
//        reportField.save(failOnError:true,validate:false,flush:true)
//        SuperQuery superQuery = new SuperQuery()
//        superQuery.save(failOnError:true,validate:false,flush:true)
//        ParameterValue parameterValue = new ParameterValue()
//        parameterValue.save(failOnError:true,validate:false,flush:true)
//        CustomSQLValue customSQLValue = new CustomSQLValue()
//        customSQLValue.save(failOnError:true,validate:false,flush:true)
//        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
//        reportTaskInstance.save(failOnError:true,validate:false)
//        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
//                Integer getParameterSize(){
//                    return 1
//                }
//            }
//        }
//        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
//        templateQuery.save(failOnError:true,validate:false,flush:true)
//        def mockUserService = new MockFor(UserService)
//        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
//        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
//        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
//        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
//        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
//        controller.userService = mockUserService.proxyInstance()
//        def mockConfigurationService = new MockFor(ConfigurationService)
//        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
//        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
//        controller.configurationService = mockConfigurationService.proxyInstance()
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.save(0..2){theInstance ->
//            run++
//            return theInstance
//        }
//        mockCRUDService.demand.save(0..1){theInstance ->
//            throw new ValidationException("message",new ValidationErrors(new Object()))
//        }
//        controller.CRUDService = mockCRUDService.proxyInstance()
//        def mockTaskTemplateService = new MockFor(TaskTemplateService)
//        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
//        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
//        when:
//        request.method = 'POST'
//        params.globalQueryValueLists = [new QueryValueList(parameterValues: [parameterValue])]
//        params.globalDateRangeInformation = new GlobalDateRangeInformation()
//        params.primaryReportingDestination = "primary"
//        params.deliveryOption = new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF])
//        params.tags = [new Tag()]
//        params.poiInputsParameterValues = [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)]
//        params.reportingDestinations = ["reportingDestination"]
//        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
//        params.scheduleDateJSON = "true"
//        params.isEnabled =  true
//        params.templateQueries = [templateQuery]
//        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
//        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
//        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
//        params["templateQuery0.qev[0].key"] = "true"
//        params["templateQuery0.tv[0].key"] = "key"
//        params["templateQueries[0].validQueries"] = "${superQuery.id}"
//        params["templateQuery0.qev[0].value"] = "true"
//        params["templateQuery0.tv[0].value"] = "value"
//        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
//        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
//        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
//        params["templateQuery0.qev[0].operator"] = "TOMORROW"
//        params["templateQuery0.qev[0].field"] = "report"
//        params["templateQueries[0].template"] = new ReportTemplate()
//        params["templateQueries[0].query"] = superQuery
//        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
//        params.tags = ["oldTag","newTag"]
//        params.reportingDestinations = "primary@!reporting@!destination"
//        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
//        params["qev[0].key"] = "true"
//        params["validQueries"] = "${superQuery.id}"
//        params["qev[0].value"] = "true"
//        params["qev[0].specialKeyValue"] = "specialKeyValue"
//        params["qev[0].copyPasteValue"] = "ParameterValue"
//        params["qev[0].isFromCopyPaste"] = "true"
//        params["qev[0].field"] = "report"
//        params["qev[0].operator"] = "TOMORROW"
//        params["poiInput[0].key"] = "key"
//        params["poiInput[0].value"] = "value"
//        params.emailConfiguration = [subject: "new_email",body: "new_body"]
//        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
//        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
//        params.asOfVersionDate = "20-Mar-2016 "
//        controller.run(10)
//        then:
//        run == 3
//        view == '/icsrReport/create'
//    }

    void "test run request method GET"(){
        when:
        request.method = 'GET'
        controller.run(10)
        then:
        flash.error == 'default.not.saved.message'
        response.redirectUrl == '/icsrReport/index'
    }

    void "test delete success"(){
        User normalUser = makeNormalUser("user",[])
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration(tenantId: 1)
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        when:
        request.method = 'POST'
        controller.delete(executedReportConfiguration)
        then:
        flash.warn == "app.configuration.delete.permission"
        response.redirectUrl == '/icsrReport/index'
    }

    void "test delete not editable"(){
        boolean run = false
        User adminUser = makeAdminUser()
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        Tenants.metaClass.static.currentId = { -> return 10}
        when:
        request.method = 'POST'
        controller.delete(executedReportConfiguration)
        then:
        run == true
        flash.message == 'default.deleted.message'
        response.redirectUrl == '/icsrReport/index'
    }

    void "test delete validation exception"(){
        User adminUser = makeAdminUser()
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration()
        UserGroup.metaClass.static.fetchAllUserGroupByUser={User user->[]}
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.softDelete(0..1){theInstance, name, String justification = null, Map saveParams = null ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        request.method = 'POST'
        controller.delete(executedReportConfiguration)
        then:
        flash.error == 'default.not.deleted.message'
        response.redirectUrl == '/icsrReport/view'
    }

    void "test delete not found"(){
        when:
        request.method = 'POST'
        controller.delete(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/icsrReport/index'
    }

    void "test copy success"(){
        boolean run = false
        User adminUser = makeAdminUser()
        IcsrReportConfiguration executedReportConfiguration = new IcsrReportConfiguration()
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.copyConfig(0..1){ IcsrReportConfiguration configuration, User user, String namePrefix = null, Long tenantId = null, boolean isCreateFromTemplate = false->
            run = true
            return executedReportConfiguration
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.copy(executedReportConfiguration)
        then:
        run == true
        flash.message == "app.copy.success"
        response.redirectUrl == '/icsrReport/view/1'
    }

    void "test copy not found"(){
        when:
        controller.copy(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/icsrReport/index'
    }

    void "test update success"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),primaryReportingDestination: "primary",deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"],evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF,scheduleDateJSON: "true",isEnabled: true)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1) { GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration periodicReportConfiguration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(1){ ReportConfiguration configurationInstance-> run++}
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            return theInstance
        }
        mockCRUDService.demand.update(1){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        IcsrReportConfiguration.metaClass.static.lock = {Serializable serializable -> return icsrReportConfiguration}
        when:
        request.method = 'POST'
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.tags = ["oldTag","newTag"]
        params.reportingDestinations = "primary@!reporting@!destination"
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
        params.asOfVersionDate = "20-Mar-2016 "
        params.id = icsrReportConfiguration.id
        controller.update()
        then:
        run == 2
        flash.message == 'default.updated.message'
        response.redirectUrl == '/icsrReport/view/1'
    }

    void "test update validation exception"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),primaryReportingDestination: "primary",deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"],evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF,scheduleDateJSON: "true",isEnabled: true)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1) { GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration periodicReportConfiguration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            return theInstance
        }
        mockCRUDService.demand.update(0..1){theInstance ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        IcsrReportConfiguration.metaClass.static.lock = {Serializable serializable -> return icsrReportConfiguration}
        when:
        request.method = 'POST'
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.tags = ["oldTag","newTag"]
        params.reportingDestinations = "primary@!reporting@!destination"
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
        params.asOfVersionDate = "20-Mar-2016 "
        params.id = icsrReportConfiguration.id
        controller.update()
        then:
        run == 1
        view == '/icsrReport/edit'
    }

    void "test update not found"(){
        IcsrReportConfiguration.metaClass.static.lock = {Serializable serializable -> return null}
        when:
        controller.update()
        then:
        response.status == 405
    }

    void "test save success"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1) { GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration periodicReportConfiguration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        when:
        request.method = 'POST'
        params.globalQueryValueLists = [new QueryValueList(parameterValues: [parameterValue])]
        params.globalDateRangeInformation = new GlobalDateRangeInformation()
        params.primaryReportingDestination = "primary"
        params.deliveryOption = new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF])
        params.tags = [new Tag()]
        params.poiInputsParameterValues = [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)]
        params.reportingDestinations = ["reportingDestination"]
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.scheduleDateJSON = "true"
        params.isEnabled =  true
        params.templateQueries = [templateQuery]
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.tags = ["oldTag","newTag"]
        params.reportingDestinations = "primary@!reporting@!destination"
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
        params.asOfVersionDate = "20-Mar-2016 "
        controller.save()
        then:
        run == 3
        flash.message == 'default.created.message'
        response.redirectUrl == '/icsrReport/view'
    }

    void "test save validation error"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1) { GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration periodicReportConfiguration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..1){theInstance ->
            run++
            return theInstance
        }
        mockCRUDService.demand.save(0..1){theInstance ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        when:
        request.method = 'POST'
        params.globalQueryValueLists = [new QueryValueList(parameterValues: [parameterValue])]
        params.globalDateRangeInformation = new GlobalDateRangeInformation()
        params.primaryReportingDestination = "primary"
        params.deliveryOption = new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF])
        params.tags = [new Tag()]
        params.poiInputsParameterValues = [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)]
        params.reportingDestinations = ["reportingDestination"]
        params.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
        params.scheduleDateJSON = "true"
        params.isEnabled =  true
        params.templateQueries = [templateQuery]
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.tags = ["oldTag","newTag"]
        params.reportingDestinations = "primary@!reporting@!destination"
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
        params.asOfVersionDate = "20-Mar-2016 "
        controller.save()
        then:
        run == 2
        view == '/icsrReport/create'
    }

    void "test save request method GET"(){
        when:
        request.method = 'GET'
        controller.save()
        then:
        response.status == 405
    }

    void "test queryDataTemplate"(){
        Query query = new Query(queryExpressionValues: [new QueryExpressionValue(value: "value",key: "key",reportField: new ReportField(name: "report_field"),operator: QueryOperatorEnum.LAST_WEEK)])
        query.save(failOnError:true,validate:false,flush:true)
        when:
        params.queryId = query.id
        def result = controller.queryDataTemplate()
        then:
        result.size() == 1
    }

    void "test viewExecutedConfig"(){
        ExecutedIcsrReportConfiguration icsrReportConfiguration = new ExecutedIcsrReportConfiguration(deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF]))
        icsrReportConfiguration.save(failOnError:true,validate:false)
        ExecutedTemplateQuery templateQuery = new ExecutedTemplateQuery()
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.executedTemplateQueries = [templateQuery]
        when:
        controller.viewExecutedConfig(icsrReportConfiguration)
        then:
        view == '/icsrReport/view'
        model.size() == 4
    }

    void "test viewExecutedConfig not found"(){
        when:
        controller.viewExecutedConfig()
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/icsrReport/index'
    }

    void "test view configurationJson null"(){
        int run = 0
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF]))
        icsrReportConfiguration.save(failOnError:true,validate:false)
        TemplateQuery templateQuery = new TemplateQuery()
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        mockReportExecutorService.demand.debugReportSQL(0..1){ ReportConfiguration configuration->
            run++
            return []
        }
        mockReportExecutorService.demand.debugGlobalQuerySQL(0..1){ def configuration->
            run++
            return [:]
        }
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return false}
        when:
        params.viewConfigJSON = false
        params.viewSql = true
        controller.view(icsrReportConfiguration.id)
        then:
        run == 2
        view == '/icsrReport/view'
        model.configurationJson == null
    }

//    getNConfigurationAsJSON : it should be getConfigurationAsJSON
//    void "test view configurationJson not null"(){
//        boolean run = false
//        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML, ReportFormatEnum.PDF]))
//        icsrReportConfiguration.save(failOnError:true,validate:false)
//        TemplateQuery templateQuery = new TemplateQuery()
//        templateQuery.save(failOnError:true,validate:false,flush:true)
//        icsrReportConfiguration.templateQueries = [templateQuery]
//        SpringSecurityUtils.metaClass.static.ifAnyGranted = {String roles -> return true}
//        def mockConfigurationService = new MockFor(ConfigurationService)
//        mockConfigurationService.demand.getNConfigurationAsJSON(0..1){ ReportConfiguration config->
//            run = true
//            return [:]
//        }
//        controller.configurationService = mockConfigurationService.proxyInstance()
//        when:
//        params.viewConfigJSON = true
//        params.viewSql = false
//        controller.view(icsrReportConfiguration.id)
//        then:
//        run == true
//        view == '/icsrReport/view'
//        model.configurationJson == null
//    }

    void "test view not found"(){
        when:
        controller.view(10)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/icsrReport/index'
    }

    void "test disable"(){
        boolean run =false
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration()
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.update(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.id = icsrReportConfiguration.id
        controller.disable()
        then:
        run == true
        flash.message == 'default.disabled.message'
        response.redirectUrl == '/icsrReport/view/1'
    }

    void "test disable not found"(){
        when:
        controller.disable()
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/icsrReport/index'
    }

//    populate model instance not matching argument
//    void "test disable validation exception"(){
//        int run = 0
//        User normalUser = makeNormalUser("user",[])
//        UserGroup userGroup = new UserGroup()
//        userGroup.save(failOnError:true,validate:false)
//        ReportField reportField = new ReportField(name: "report")
//        reportField.save(failOnError:true,validate:false,flush:true)
//        SuperQuery superQuery = new SuperQuery()
//        superQuery.save(failOnError:true,validate:false,flush:true)
//        ParameterValue parameterValue = new ParameterValue()
//        parameterValue.save(failOnError:true,validate:false,flush:true)
//        CustomSQLValue customSQLValue = new CustomSQLValue()
//        customSQLValue.save(failOnError:true,validate:false,flush:true)
//        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
//        reportTaskInstance.save(failOnError:true,validate:false)
//        ReportConfiguration icsrReportConfiguration = new PeriodicReportConfiguration(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),primaryReportingDestination: "primary",deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"],evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF,scheduleDateJSON: "true",isEnabled: true)
//        icsrReportConfiguration.save(failOnError:true,validate:false)
//        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
//                Integer getParameterSize(){
//                    return 1
//                }
//            }
//        }
//        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
//        templateQuery.save(failOnError:true,validate:false,flush:true)
//        icsrReportConfiguration.templateQueries = [templateQuery]
//        def mockUserService = new MockFor(UserService)
//        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
//        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
//        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
//        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
//        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
//        controller.userService = mockUserService.proxyInstance()
//        def mockConfigurationService = new MockFor(ConfigurationService)
//        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
//        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
//        controller.configurationService = mockConfigurationService.proxyInstance()
//        def mockCRUDService = new MockFor(CRUDService)
//        mockCRUDService.demand.save(0..2){theInstance ->
//            return theInstance
//        }
//        mockCRUDService.demand.update(0..1){theInstance ->
//            throw new ValidationException("message",new ValidationErrors(new Object()))
//        }
//        controller.CRUDService = mockCRUDService.proxyInstance()
//        def mockTaskTemplateService = new MockFor(TaskTemplateService)
//        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
//        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
//        IcsrReportConfiguration.metaClass.static.lock = {Serializable serializable -> return icsrReportConfiguration}
//        when:
//        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
//        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
//        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
//        params["templateQuery0.qev[0].key"] = "true"
//        params["templateQuery0.tv[0].key"] = "key"
//        params["templateQueries[0].validQueries"] = "${superQuery.id}"
//        params["templateQuery0.qev[0].value"] = "true"
//        params["templateQuery0.tv[0].value"] = "value"
//        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
//        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
//        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
//        params["templateQuery0.qev[0].operator"] = "TOMORROW"
//        params["templateQuery0.qev[0].field"] = "report"
//        params["templateQueries[0].template"] = new ReportTemplate()
//        params["templateQueries[0].query"] = superQuery
//        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
//        params.tags = ["oldTag","newTag"]
//        params.reportingDestinations = "primary@!reporting@!destination"
//        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
//        params["qev[0].key"] = "true"
//        params["validQueries"] = "${superQuery.id}"
//        params["qev[0].value"] = "true"
//        params["qev[0].specialKeyValue"] = "specialKeyValue"
//        params["qev[0].copyPasteValue"] = "ParameterValue"
//        params["qev[0].isFromCopyPaste"] = "true"
//        params["qev[0].field"] = "report"
//        params["qev[0].operator"] = "TOMORROW"
//        params["poiInput[0].key"] = "key"
//        params["poiInput[0].value"] = "value"
//        params.emailConfiguration = [subject: "new_email",body: "new_body"]
//        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
//        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
//        params.asOfVersionDate = "20-Mar-2016 "
//        params.id = icsrReportConfiguration.id
//        controller.disable()
//        then:
//        run == 1
//        view == '/icsrReport/edit'
//    }

    void "test initConfigurationFromMap"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),primaryReportingDestination: "primary",deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"],evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF,scheduleDateJSON: "true",isEnabled: true)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1) { GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration periodicReportConfiguration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
        mockConfigurationService.demand.initConfigurationTemplatesFromSession(0..1){ session, ReportConfiguration configurationInstance-> run++}
        mockConfigurationService.demand.initConfigurationQueriesFromSession(0..1){ session, ReportConfiguration configurationInstance-> run++}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        when:
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.tags = ["oldTag","newTag"]
        params.reportingDestinations = "primary@!reporting@!destination"
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
        params.asOfVersionDate = "20-Mar-2016 "
        controller.invokeMethod('initConfigurationFromMap', [icsrReportConfiguration, [:]] as Object[])
        then:
        run == 3
    }

    void "test edit success"(){
        int run = 0
        User adminUser = makeAdminUser()
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(executing: false)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.fetchConfigurationMapFromSession(0..1){ params, session->
            run++
            return [configurationParams:[:],templateQueryIndex:[:]]
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            run++
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        mockConfigurationService.demand.correctSchedulerJSONForCurrentDate(1){ json, date->
            return json
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.edit(icsrReportConfiguration.id)
        then:
        run == 2
        view == '/icsrReport/edit'
    }

    void "test edit validation error"(){
        int run = 0
        User adminUser = makeAdminUser()
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(executing: false)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.fetchConfigurationMapFromSession(0..1){ params, session->
            run++
            return [configurationParams:[:],templateQueryIndex:[:]]
        }
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            throw new ValidationException("message",new ValidationErrors(new Object()))
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        mockConfigurationService.demand.correctSchedulerJSONForCurrentDate(1){ json, date->
            return json
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.edit(icsrReportConfiguration.id)
        then:
        run == 1
        view == '/icsrReport/edit'
    }

    void "test edit not found"(){
        when:
        controller.edit(10)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/icsrReport/index'
    }

    void "test edit not editable"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(executing: false,tenantId: 1)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.fetchConfigurationMapFromSession(0..1){ params, session->
            run++
            return [configurationParams:[:],templateQueryIndex:[:]]
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        Tenants.metaClass.static.currentId = { -> return 10}
        when:
        controller.edit(icsrReportConfiguration.id)
        then:
        run == 1
        flash.warn == "app.configuration.edit.permission"
        response.redirectUrl == "/icsrReport/index"
    }

    void "test edit running"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        UserGroup userGroup = new UserGroup()
        userGroup.save(failOnError:true,validate:false)
        ReportField reportField = new ReportField(name: "report")
        reportField.save(failOnError:true,validate:false,flush:true)
        SuperQuery superQuery = new SuperQuery()
        superQuery.save(failOnError:true,validate:false,flush:true)
        ParameterValue parameterValue = new ParameterValue()
        parameterValue.save(failOnError:true,validate:false,flush:true)
        CustomSQLValue customSQLValue = new CustomSQLValue()
        customSQLValue.save(failOnError:true,validate:false,flush:true)
        ReportTask reportTaskInstance = new ReportTask(description: "newTask")
        reportTaskInstance.save(failOnError:true,validate:false)
        IcsrReportConfiguration icsrReportConfiguration = new IcsrReportConfiguration(executing: true,tenantId: 1,globalQueryValueLists: [new QueryValueList(parameterValues: [parameterValue])],globalDateRangeInformation: new GlobalDateRangeInformation(),primaryReportingDestination: "primary",deliveryOption: new DeliveryOption(emailToUsers: ["abc@gmail.com"],attachmentFormats: [ReportFormatEnum.HTML,ReportFormatEnum.PDF]),tags: [new Tag()],poiInputsParameterValues: [new ParameterValue(key: "specialKeyValue",value: "newValue",isFromCopyPaste: false)],reportingDestinations: ["reportingDestination"],evaluateDateAs: EvaluateCaseDateEnum.VERSION_ASOF,scheduleDateJSON: "true",isEnabled: true)
        icsrReportConfiguration.save(failOnError:true,validate:false)
        SuperQuery.metaClass.static.get = {Serializable serializable -> new Object(){
                Integer getParameterSize(){
                    return 1
                }
            }
        }
        IcsrTemplateQuery templateQuery = new IcsrTemplateQuery(dmsConfiguration: new DmsConfiguration(),queryValueLists: [new QueryValueList(parameterValues: [parameterValue])],templateValueLists: [new TemplateValueList(parameterValues: [customSQLValue])],dateRangeInformationForTemplateQuery: new DateRangeInformation())
        UserGroup.metaClass.static.fetchAllUserGroupByUser={User user->[]}
        templateQuery.save(failOnError:true,validate:false,flush:true)
        icsrReportConfiguration.templateQueries = [templateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getCurrentUser(0..1){-> normalUser}
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return templateQuery }
        mockUserService.demand.setOwnershipAndModifier(0..1){Object object-> return new IcsrTemplateQuery(template: new ReportTemplate(name: "report"),query: new SuperQuery(name: "superQuery")) }
        mockUserService.demand.getAllowedSharedWithUsersForCurrentUser(0..1){String search = null-> [normalUser]}
        mockUserService.demand.getAllowedSharedWithGroupsForCurrentUser(0..1){String search = null-> [userGroup]}
        mockUserService.demand.getUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.fetchConfigurationMapFromSession(0..1){ params, session->
            run++
            return [configurationParams:[id:"${icsrReportConfiguration.id}"],templateQueryIndex:[:]]
        }
        mockConfigurationService.demand.getNextDate(0..1){ ReportConfiguration config-> return new Date()}
        mockConfigurationService.demand.fixBindDateRange(0..1) { GlobalDateRangeInformation globalDateRangeInformation, ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().fixBindDateRange(globalDateRangeInformation, periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.bindParameterValuesToGlobalQuery(0..1){ ReportConfiguration periodicReportConfiguration, def params ->
            new ConfigurationService().bindParameterValuesToGlobalQuery(periodicReportConfiguration, params)
            return
        }
        mockConfigurationService.demand.removeRemovedTemplateQueries(0..1){ReportConfiguration periodicReportConfiguration-> }
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.checkProductCheckboxes(0..1){ ReportConfiguration configurationInstance-> run++}
        mockConfigurationService.demand.initConfigurationTemplatesFromSession(0..1){ session, ReportConfiguration configurationInstance-> run++}
        mockConfigurationService.demand.initConfigurationQueriesFromSession(0..1){ session, ReportConfiguration configurationInstance-> run++}
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.save(0..2){theInstance ->
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        def mockTaskTemplateService = new MockFor(TaskTemplateService)
        mockTaskTemplateService.demand.fetchReportTasksFromRequest(0..1){params-> [reportTaskInstance]}
        controller.taskTemplateService = mockTaskTemplateService.proxyInstance()
        when:
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum"] = "CUMULATIVE"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"] = "10-Mar-2016"
        params["templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"] = "20-Mar-2016"
        params["templateQuery0.qev[0].key"] = "true"
        params["templateQuery0.tv[0].key"] = "key"
        params["templateQueries[0].validQueries"] = "${superQuery.id}"
        params["templateQuery0.qev[0].value"] = "true"
        params["templateQuery0.tv[0].value"] = "value"
        params["templateQuery0.qev[0].specialKeyValue"] = "specialKeyValue"
        params["templateQuery0.qev[0].copyPasteValue"] = "ParameterValue"
        params["templateQuery0.qev[0].isFromCopyPaste"] = "true"
        params["templateQuery0.qev[0].operator"] = "TOMORROW"
        params["templateQuery0.qev[0].field"] = "report"
        params["templateQueries[0].template"] = new ReportTemplate()
        params["templateQueries[0].query"] = superQuery
        params["templateQueries[0].msgType"] = MessageTypeEnum.RECODED
        params.tags = ["oldTag","newTag"]
        params.reportingDestinations = "primary@!reporting@!destination"
        params.globalDateRangeInformation = ["dateRangeEnum": DateRangeEnum.TOMORROW]
        params["qev[0].key"] = "true"
        params["validQueries"] = "${superQuery.id}"
        params["qev[0].value"] = "true"
        params["qev[0].specialKeyValue"] = "specialKeyValue"
        params["qev[0].copyPasteValue"] = "ParameterValue"
        params["qev[0].isFromCopyPaste"] = "true"
        params["qev[0].field"] = "report"
        params["qev[0].operator"] = "TOMORROW"
        params["poiInput[0].key"] = "key"
        params["poiInput[0].value"] = "value"
        params.emailConfiguration = [subject: "new_email",body: "new_body"]
        params.sharedWith = "UserGroup_${userGroup.id};User_${normalUser.id}"
        params.dmsConfiguration = ['format': ReportFormatEnum.PDF]
        params.asOfVersionDate = "20-Mar-2016 "
        controller.edit(icsrReportConfiguration.id)
        then:
        run == 4
        flash.warn == "app.configuration.running.fail"
        response.redirectUrl == "/icsrReport/index"
    }

    void "test createQuery"(){
        when:
        params.id = 1
        controller.createQuery()
        then:
        response.redirectUrl == '/query/create'
    }

    void "test createTemplate"(){
        when:
        params.id = 1
        params.templateType = "PDF"
        controller.createTemplate()
        then:
        response.redirectUrl == '/template/create?templateType=PDF'
    }

    void "test create query not found"(){
        boolean run =false
        User normalUser = makeNormalUser("user",[])
        ReportTemplate reportTemplate = new ReportTemplate(hasBlanks: true)
        reportTemplate.save(failOnError:true,validate:false,flush :true)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.bindSharedWith(0..1){ReportConfiguration configurationInstance, List<String> sharedWith, List<String> executableBy, Boolean isUpdate-> }
        mockConfigurationService.demand.fetchConfigurationMapFromSession(0..1){ params, session->
            run = true
            return [configurationParams:[:],templateQueryIndex:[:]]
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        SuperQuery.metaClass.static.get = {Serializable serializable -> return null}
        when:
        params.selectedTemplate = reportTemplate.id
        params.selectedQuery = 10
        controller.create()
        then:
        run == true
        flash.error == 'app.configuration.query.notFound'
        view == '/icsrReport/create'
        model.configurationInstance.templateQueries.size() == 1
    }

    void "test create template not found"(){
        boolean run =false
        User normalUser = makeNormalUser("user",[])
        SuperQuery superQuery = new SuperQuery(hasBlanks: true)
        superQuery.save(failOnError:true,validate:false,flush :true)
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.fetchConfigurationMapFromSession(0..1){ params, session->
            run = true
            return [configurationParams:[:],templateQueryIndex:[:]]
        }
        controller.configurationService = mockConfigurationService.proxyInstance()
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..2){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        SuperQuery.metaClass.static.get = {Serializable serializable -> return superQuery}
        when:
        params.selectedTemplate = 10
        params.selectedQuery = superQuery.id
        controller.create()
        then:
        run == true
        flash.error == 'app.configuration.template.notFound'
        view == '/icsrReport/create'
        model.configurationInstance.templateQueries.size() == 1
    }

    void "test updateReportState"(){
        boolean run = false
        ExecutedIcsrReportConfiguration icsrReportConfiguration = new ExecutedIcsrReportConfiguration(workflowState: new WorkflowState(name: "old_state"))
        icsrReportConfiguration.save(failOnError:true,validate:false)
        WorkflowState workflowState = new WorkflowState(name: "new_state")
        workflowState.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            run = true
            return theInstance
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.newState = "new_state"
        params.oldState = "old_state"
        controller.updateReportState(icsrReportConfiguration.id)
        then:
        run == true
        response.json == [success:true, message:"app.periodicReportConfiguration.state.update.success"]
    }

    void "test updateReportState exception"(){
        ExecutedIcsrReportConfiguration icsrReportConfiguration = new ExecutedIcsrReportConfiguration(workflowState: new WorkflowState(name: "old_state"))
        icsrReportConfiguration.save(failOnError:true,validate:false)
        WorkflowState workflowState = new WorkflowState(name: "new_state")
        workflowState.save(failOnError:true,validate:false)
        def mockCRUDService = new MockFor(CRUDService)
        mockCRUDService.demand.saveWithoutAuditLog(0..1){theInstance ->
            throw new Exception()
        }
        controller.CRUDService = mockCRUDService.proxyInstance()
        when:
        params.newState = "new_state"
        params.oldState = "old_state"
        controller.updateReportState(10)
        then:
        response.json == [message: "Server Error"]
        response.status == 500
    }

    void "test targetStatesAndApplications"(){
        boolean run = false
        def mockIcsrReportService = new MockFor(IcsrReportService)
        mockIcsrReportService.demand.targetStatesAndApplications(0..1){ Long executedReportConfiguration, String initialState->
            run = true
            return [:]
        }
        controller.icsrReportService = mockIcsrReportService.proxyInstance()
        when:
        params.initialState = "initialState"
        params.executedReportConfiguration = 1
        controller.targetStatesAndApplications()
        then:
        run == true
    }

    void "test show"(){
        int run = 0
        User adminUser = makeAdminUser()
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrReportConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10),tenantId: 1,caseSeries: new ExecutedCaseSeries(caseSeriesOwner: "owner"))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM),executedConfiguration: executedReportConfiguration,finalReportResult: reportResult,draftReportResult: reportResult,executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"))
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> adminUser}
        mockUserService.demand.getCurrentUser(0..1){-> adminUser}
        controller.userService = mockUserService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getReportName(0..1){ ReportResult reportResultInstance, boolean isInDraftMode, Map params ->
            run++
            return "report_1"
        }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        def mockReportExecutorService = new MockFor(ReportExecutorService)
        controller.reportExecutorService = mockReportExecutorService.proxyInstance()
        when:
        controller.show(reportResult)
        then:
        run == 1
    }

//  multipartFile run not working
    void "test show report file XML E2B_R3"(){
        int run = 0
        File reportFile = File.createTempFile("hello","world")
        User adminUser = makeAdminUser()
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrReportConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10),tenantId: 1,caseSeries: new ExecutedCaseSeries(caseSeriesOwner: "owner"))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM),executedConfiguration: executedReportConfiguration,finalReportResult: reportResult,draftReportResult: reportResult,executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"))
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> adminUser }
        mockUserService.demand.getCurrentUser(0..1) { -> adminUser } // ← this fixes the issue
        controller.userService = mockUserService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getReportName(0..1) { ReportResult reportResultInstance, boolean isInDraftMode, Map params ->
            run++
            return "report_1"
        }
        mockDynamicReportService.demand.getReportNameAsFileName(0..1){ExecutedReportConfiguration executedConfiguration, ReportResult reportResultInstance = null-> return "name " }
        mockDynamicReportService.demand.getContentType(0..1){ReportFormatEnum reportFormat-> return "runs " }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        when:
        params.icsrReportSpec = IcsrReportSpecEnum.E2B_R3
        params.outputFormat = "XML"
        controller.show(reportResult)
        then:
        run == 1
    }

    void "test show report file XML E2B_R2"(){
        int run = 0
        File reportFile = File.createTempFile("hello","world")
        User adminUser = makeAdminUser()
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true,validate:false)
        ExecutedIcsrReportConfiguration executedReportConfiguration = new ExecutedIcsrReportConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10),tenantId: 1,caseSeries: new ExecutedCaseSeries(caseSeriesOwner: "owner"))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM),executedConfiguration: executedReportConfiguration,finalReportResult: reportResult,draftReportResult: reportResult,executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"))
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> adminUser }
        mockUserService.demand.getCurrentUser(0..1) { -> adminUser } // ← REQUIRED to prevent failure
        controller.userService = mockUserService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getReportName(0..1) { ReportResult reportResultInstance, boolean isInDraftMode, Map params ->
            run++
            return "report_1"
        }
        mockDynamicReportService.demand.getReportNameAsFileName(0..1){ExecutedReportConfiguration executedConfiguration, ReportResult reportResultInstance = null-> return "name " }
        mockDynamicReportService.demand.getContentType(0..1){ReportFormatEnum reportFormat-> return "runs " }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        when:
        params.icsrReportSpec = IcsrReportSpecEnum.E2B_R2
        params.outputFormat = "XML"
        controller.show(reportResult)
        then:
        run == 1
    }

    void "test show report file PDF"(){
        int run = 0
        File reportFile = File.createTempFile("hello","world")
        User adminUser = makeAdminUser()
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrReportConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10),tenantId: 1,caseSeries: new ExecutedCaseSeries(caseSeriesOwner: "owner"))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM),executedConfiguration: executedReportConfiguration,finalReportResult: reportResult,draftReportResult: reportResult,executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"))
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> adminUser }
        mockUserService.demand.getCurrentUser(0..1) { -> adminUser } // ← REQUIRED to prevent failure
        controller.userService = mockUserService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getReportName(0..1) { ReportResult reportResultInstance, boolean isInDraftMode, Map params ->
            run++
            return "report_1"
        }
        mockDynamicReportService.demand.getReportNameAsFileName(0..1){ExecutedReportConfiguration executedConfiguration, ReportResult reportResultInstance = null-> return "name " }
        mockDynamicReportService.demand.getContentType(0..1){ReportFormatEnum reportFormat-> return "runs " }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        when:
        params.icsrReportSpec = IcsrReportSpecEnum.E2B_R2
        params.outputFormat = "PDF"
        controller.show(reportResult)
        then:
        run == 1
    }

    void "test show report file default"(){
        int run = 0
        File reportFile = File.createTempFile("hello","world")
        User adminUser = makeAdminUser()
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrReportConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10),tenantId: 1,caseSeries: new ExecutedCaseSeries(caseSeriesOwner: "owner"))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM),executedConfiguration: executedReportConfiguration,finalReportResult: reportResult,draftReportResult: reportResult,executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"))
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1) { -> adminUser }
        mockUserService.demand.getCurrentUser(0..1) { -> adminUser } // ← REQUIRED to prevent failure
        controller.userService = mockUserService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getReportName(0..1) { ReportResult reportResultInstance, boolean isInDraftMode, Map params ->
            run++
            return "report_1"
        }
        mockDynamicReportService.demand.getReportNameAsFileName(0..1){ExecutedReportConfiguration executedConfiguration, ReportResult reportResultInstance = null-> return "name " }
        mockDynamicReportService.demand.getContentType(0..1){ReportFormatEnum reportFormat-> return "runs " }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        when:
        params.icsrReportSpec = IcsrReportSpecEnum.E2B_R2
        params.outputFormat = "XLSX"
        controller.show(reportResult)
        then:
        run == 1
    }

    void "test show not editable"(){
        int run = 0
        User normalUser = makeNormalUser("user",[])
        ReportResult reportResult = new ReportResult()
        reportResult.save(failOnError:true,validate:false)
        ExecutedReportConfiguration executedReportConfiguration = new ExecutedIcsrReportConfiguration(executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+10),tenantId: 1,caseSeries: new ExecutedCaseSeries(caseSeriesOwner: "owner"))
        executedReportConfiguration.save(failOnError:true,validate:false,flush:true)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedAsOfVersionDate: new Date(),dateRangeEnum: DateRangeEnum.CUSTOM),executedConfiguration: executedReportConfiguration,finalReportResult: reportResult,draftReportResult: reportResult,executedTemplate: new ReportTemplate(name: "template"),executedQuery: new SuperQuery(name: "query"))
        executedTemplateQuery.save(failOnError:true,validate:false,flush:true)
        executedReportConfiguration.executedTemplateQueries = [executedTemplateQuery]
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> normalUser}
        controller.userService = mockUserService.proxyInstance()
        def mockDynamicReportService = new MockFor(DynamicReportService)
        mockDynamicReportService.demand.getReportName(0..1){ ReportResult reportResultInstance, boolean isInDraftMode, Map params ->
            run++
            return "report_1"
        }
        controller.dynamicReportService = mockDynamicReportService.proxyInstance()
        Tenants.metaClass.static.currentId = { -> return 10}
        when:
        params.icsrReportSpec = IcsrReportSpecEnum.E2B_R3
        controller.show(reportResult)
        then:
        run == 1
        flash.warn == "app.userPermission.message"
        view == '/icsrReport/index'
    }

    void "test show not found"(){
        when:
        controller.show(null)
        then:
        flash.error == 'default.not.found.message'
        response.redirectUrl == '/icsrReport/index'
    }
}
