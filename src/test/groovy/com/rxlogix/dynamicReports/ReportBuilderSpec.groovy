package com.rxlogix.dynamicReports

import com.rxlogix.*
import com.rxlogix.config.*
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.*
import groovy.mock.interceptor.MockFor
import org.grails.spring.beans.factory.InstanceFactoryBean
import spock.lang.Specification
import grails.testing.gorm.DataTest
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([User])
class ReportBuilderSpec extends Specification implements DataTest {

    def setupSpec() {
        mockDomains User, Role, UserRole, UserGroup, UserGroupUser, Tenant, ExecutedTemplateQuery, ReportTemplate, ReportResult, ExecutedConfiguration, CaseLineListingTemplate
        defineBeans {
            dynamicReportService(InstanceFactoryBean, makeDynamicReportService(), DynamicReportService)
            customMessageService(InstanceFactoryBean, makeCustomMessageService(), CustomMessageService)
            templateService(InstanceFactoryBean, makeTemplateService(), TemplateService)
            userService(InstanceFactoryBean, makeUserService(new User(username: 'user')), UserService)
            reportExecutorService(InstanceFactoryBean, makeReportExecutorService(), ReportExecutorService)
            imageService(InstanceFactoryBean, new MockFor(ImageService).proxyInstance(), ImageService)
        }
    }


    def setup() {
    }

    def cleanup() {
    }

    private User makeNormalUser(name, team) {
        User.metaClass.encodePassword = { "password" }
        def preferenceNormal = new Preference(locale: new Locale("en"), createdBy: "user", modifiedBy: "user")
        def userRole = new Role(authority: 'ROLE_DEV', createdBy: "user", modifiedBy: "user").save(flush: true)
        def normalUser = new User(username: name, password: 'user', fullName: name, preference: preferenceNormal, createdBy: "user", modifiedBy: "user", email: "abc@gmail.com")
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


    void "test for filterSubtotalRows"() {
        when:
        String json = """[
{ROW_1:"val", ROW_2:"val",ROW_3:"Subtotal", "GP_1":"1","GP_2":"2"},
{ROW_1:"val", ROW_2:"val",ROW_3:"Subtotal", "GP_1":"1","GP_2":"2"},
{ROW_1:"val", ROW_2:"val",ROW_3:"val", "GP_1":"1","GP_2":"2"},
{ROW_1:"val", ROW_2:"Subtotal",ROW_3:"val", "GP_1":"1","GP_2":"2"},
{ROW_1:"val", ROW_2:"",ROW_3:"", "GP_1":"1","GP_2":"2"}
]
"""
        then:
        ReportBuilder.filterSubtotalRows(template, new ByteArrayInputStream(json.getBytes("UTF-8"))).size() == size
        where:
        size | template
        5    | new ExecutedDataTabulationTemplate(rowList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(hideSubtotal: false), new ReportFieldInfo(hideSubtotal: false), new ReportFieldInfo(hideSubtotal: false)]))
        4    | new ExecutedDataTabulationTemplate(rowList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(hideSubtotal: false), new ReportFieldInfo(hideSubtotal: true), new ReportFieldInfo(hideSubtotal: false)]))
        2    | new ExecutedDataTabulationTemplate(rowList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(hideSubtotal: false), new ReportFieldInfo(hideSubtotal: true), new ReportFieldInfo(hideSubtotal: true)]))
        3    | new ExecutedDataTabulationTemplate(rowList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(hideSubtotal: false), new ReportFieldInfo(hideSubtotal: false), new ReportFieldInfo(hideSubtotal: true)]))
        4    | new ExecutedDataTabulationTemplate(groupingList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(), new ReportFieldInfo()]), rowList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(hideSubtotal: false)]))
        2    | new ExecutedDataTabulationTemplate(groupingList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(), new ReportFieldInfo()]), rowList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(hideSubtotal: true)]))

    }

    void "test for nullValueInFilterSubtotalRows"() {
        when:
        String json = """[
            {ROW_1:"val", ROW_2:"val",ROW_3:"Subtotal", "GP_1":"1","GP_2":"2"},
            {ROW_1:"val", ROW_2:"val",ROW_3:"Subtotal", "GP_1":"1","GP_2":"2"},
            {ROW_1:"val", ROW_2:null ,ROW_3:'val',ROW_4:'val', ROW_5:'val',"GP_1":"1","GP_2":"2"}
            ]
            """
        then:
        ReportBuilder.filterSubtotalRows(template, new ByteArrayInputStream(json.getBytes("UTF-8"))).get(2).get("ROW_2").textValue() == Constants.EMPTY
        where:
        template = new ExecutedDataTabulationTemplate(rowList: new ReportFieldInfoList(reportFieldInfoList: [new ReportFieldInfo(hideSubtotal: false), new ReportFieldInfo(hideSubtotal: false), new ReportFieldInfo(hideSubtotal: false)]))
    }

    void "test createDataSourceCSV when Template Set datasource is handled separately in TemplateSetReportBuilder"() {
        given:
        ReportBuilder reportBuilder = new ReportBuilder()
        User normalUser = makeNormalUser("user", [])
        when:
        ReportResult reportResult = new ReportResult(data: new ReportResultData(crossTabHeader: '[{"ROW_1":"test1"},{"column":"test2"}]'))
        ReportTemplate executedTemplate = new ReportTemplate(name: 'report', owner: normalUser, templateType: TemplateTypeEnum.TEMPLATE_SET)
        reportBuilder.createDataSourceCSV(reportResult, executedTemplate, null)
        then:
        true
    }

    void "test createDataSourceCSV "() {
        given:
        User normalUser = makeNormalUser("user", [])
        ReportBuilder reportBuilder = new ReportBuilder()
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(owner: normalUser, reportName: 'report_1', status: ReportExecutionStatusEnum.GENERATED_DRAFT)
        ReportTemplate reportTemplate = new ReportTemplate(name: 'report', owner: normalUser, templateType: TemplateTypeEnum.CASE_LINE)
        reportTemplate.save(failOnError: true, validate: false)
        ReportResult reportResult = new ReportResult(template: reportTemplate, executionStatus: ReportExecutionStatusEnum.COMPLETED)
        reportResult.save(failOnError: true, validate: false)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: reportTemplate, executedConfiguration: executedConfiguration, draftReportResult: reportResult, reportResult: reportResult, finalReportResult: reportResult)
        executedTemplateQuery.save(failOnError: true, validate: false)
        when:
        def params = [dynamic : true,  rca: true]
        ReportTemplate executedTemplate=new CaseLineListingTemplate(name: 'report',owner: normalUser,templateType: TemplateTypeEnum.CASE_LINE)
        ReportResult reportResultData = new ReportResult(template: executedTemplate, data: new ReportResultData(crossTabHeader: '[{"ROW_1":"test1"},{"column":"test2"}]'),value: 2, dateCreated: new Date(), lastUpdated: new Date(), scheduledBy: normalUser, frequency: "",executionStatus: ReportExecutionStatusEnum.SCHEDULED, executedTemplateQuery: null)
        reportBuilder.createDataSourceCSV(reportResultData, executedTemplate, params)
        then:
        true
    }

    private UserService makeUserService(User user) {
        UserService userService = new UserService()
        userService.metaClass.getUser = { user }
        userService.metaClass.getCurrentUser = { user }
        return userService
    }

    private makeCustomMessageService() {
        def customMessageServiceMock = new MockFor(CustomMessageService)
        customMessageServiceMock.demand.getMessage(0..99) { String code -> code }
        customMessageServiceMock.demand.getMessage(0..99) { String code, Object args -> code }
        customMessageServiceMock.demand.getMessage(0..99) { String code, Object[] args, String defaultMessage, Locale locale -> code }
        return customMessageServiceMock.proxyInstance()
    }

    private makeTemplateService() {
        def MockTemplateService = new MockFor(TemplateService)
        MockTemplateService.demand.getResultTable(0..99) { ReportTemplate template -> [SeriourecordsTotal: 1, recordsFiltered: 1] }
        MockTemplateService.demand.getCllDrilldownDataAjax(0..99) { def filterData, ReportResult reportResult, Long offset, Long max, String sortDir,
                                                                    String sortField, def searchData, String globalSearchData, Map fieldTypeMap, List<String> templateHeader, Map additionalFilterMap = null, String assignedToFilter = null -> [aaData: [], recordsTotal: 1, recordsFiltered: 1]
        }
        return MockTemplateService.proxyInstance()
    }

    private makeReportExecutorService() {
        def mockreportExecutorService = new MockFor(ReportExecutorService)
        mockreportExecutorService.demand.appendReasonOfDelayDataFromMart(0..99) { Map data, String dateFormat, boolean inboundCompliance -> }
        return mockreportExecutorService.proxyInstance()
    }

    private makeDynamicReportService() {
        def dynamicReportService = new MockFor(DynamicReportService)
        dynamicReportService.demand.getSwapVirtualizerMaxSize(1) { -> return 100 }
        dynamicReportService.demand.getReportsDirectory(1) { -> return (System.getProperty("java.io.tmpdir")) }
        dynamicReportService.demand.getBlockSize(1) { -> return 4096 }
        dynamicReportService.demand.getMinGrowCount(1) { -> return 1024 }
        dynamicReportService.demand.getReportsDirectory(1) { -> return (System.getProperty("java.io.tmpdir")) }
        dynamicReportService.demand.getReportFilename(1) { String reportName, String outputFormat, String locale -> return (reportName + '.xlsx') }
        return dynamicReportService.proxyInstance()
    }
}
