package com.rxlogix.signal

import com.rxlogix.CRUDService
import com.rxlogix.ExecutedConfigurationService
import com.rxlogix.config.*
import com.rxlogix.dto.caseSeries.integration.ExecutedDateRangeInfoDTO
import com.rxlogix.dto.caseSeries.integration.QueryValueListDTO
import com.rxlogix.dto.reports.integration.ExecutedConfigurationDTO
import com.rxlogix.dto.reports.integration.ExecutedTemplateQueryDTO
import com.rxlogix.enums.*
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupUser
import com.rxlogix.util.DateUtil
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([DateUtil, User, ExecutedConfiguration])
class ReportsBuilderServiceSpec extends Specification implements DataTest, ServiceUnitTest<ReportsBuilderService> {

    User user
    Long templateId = 1
    ReportTemplate reportTemplateInstance
    ExecutedCaseSeries executedCaseSeries
    SourceProfile sourceProfile
    WorkflowState workflowState
    SuperQuery superQuery
    QueryValueListDTO queryValueListDTO
    ExecutedConfiguration executedConfiguration

    @Shared
    ExecutedConfigurationDTO executedConfigurationDTO,executedConfigurationDTOCaseSeries

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
        Holders.config.pvreports.multiTenancy.defaultTenant = 1L
        DateRangeType dateRangeType = new DateRangeType(name: "dcdtDatecol1", sortOrder: 0, isDeleted: false)
        dateRangeType.save(failOnError: true)
        user = new User(username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToTenants(tenant)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.timeZone = "UTC"

        user.preference.locale = new Locale("en")
        user.save(failOnError: true)

        ExecutedCaseSeriesDateRangeInformation executedCaseSeriesDateRangeInformation = new ExecutedCaseSeriesDateRangeInformation(
                dateRangeEndAbsolute: new Date().parse(DateUtil.DATETIME_FMT, "05-05-0001 00:00:01"),
                dateRangeStartAbsolute: new Date().parse(DateUtil.DATETIME_FMT, "01-01-0001 00:00:01"),
                dateRangeEnum: DateRangeEnum.CUMULATIVE,
                relativeDateRangeValue: 1
        )

        ExecutedCaseDeliveryOption executedCaseDeliveryOption = new ExecutedCaseDeliveryOption(sharedWith: [user])

        executedCaseSeries = new ExecutedCaseSeries(
                seriesName: "seriesName",
                description: "description",
                dateRangeType: DateRangeType.findByName("dcdtDatecol1"), numExecutions: 0,
                owner: user, createdBy: "username", dateCreated: new Date(), lastUpdated: new Date(), modifiedBy: "username",
                executedCaseSeriesDateRangeInformation: executedCaseSeriesDateRangeInformation, executedDeliveryOption: executedCaseDeliveryOption, tenantId: 1L
        ).save(validate: false)

        workflowState = new WorkflowState(name: "New", modifiedBy: "username", createdBy: "username").save(failOnError: true)

        reportTemplateInstance = new ReportTemplate(id: templateId, owner: user, templateType: TemplateTypeEnum.CASE_LINE,
                modifiedBy: "user", createdBy: "user", name: "template").save(failOnError: true)

        sourceProfile = new SourceProfile(sourceName: "Argus", sourceAbbrev: "Arg", sourceId: 10).save(failOnError: true)

        executedConfigurationDTO = new ExecutedConfigurationDTO(reportName: "Test Report",
                excludeFollowUp: "false", limitPrimaryPath: false,
                ownerName: "username",
                dateRangeType: DateRangeType.findByName("dcdtDatecol1"),
                sharedWithGroups: [],
                includeLockedVersion: true,
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                sharedWithUsers: ["username"],
                productSelection: '{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}',
                pvrCumulativeCaseSeriesId: 1, includeMedicallyConfirmedCases: false, asOfVersionDate: null, description: null, eventSelection: null, executedETLDate: new Date()
        )

        executedConfigurationDTOCaseSeries = new ExecutedConfigurationDTO(reportName: "Test Report Case Series",
                excludeFollowUp: "false", limitPrimaryPath: false,
                ownerName: "username",
                dateRangeType: DateRangeType.findByName("dcdtDatecol1"),
                sharedWithGroups: [],
                includeLockedVersion: true,
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                sharedWithUsers: ["signaldev"],
                productSelection: '{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}',
                pvrCaseSeriesId: 1, includeMedicallyConfirmedCases: false, asOfVersionDate: null, description: null, eventSelection: null, executedETLDate: new Date()
        )

        ExecutedDateRangeInfoDTO executedDateRangeInfoDTO = new ExecutedDateRangeInfoDTO(
                dateRangeEndAbsolute: new Date().parse(DateUtil.DATETIME_FMT, "05-05-0001 00:00:01"),
                dateRangeStartAbsolute: new Date().parse(DateUtil.DATETIME_FMT, "01-01-0001 00:00:01"),
                dateRangeEnum: DateRangeEnum.CUMULATIVE,
                relativeDateRangeValue: 1
        )

        ExecutedTemplateQueryDTO executedTemplateQueryDTO = new ExecutedTemplateQueryDTO(
                footer: null, headerDateRange: false,
                title: "Test Report", privacyProtected: false, header: "AE By SOC", templateValueLists: [], blindProtected: false,
                templateId: templateId, headerProductSelection: false, executedQueryValueListDTOList: [], queryLevel: QueryLevelEnum.CASE,
                queryId: null, executedTemplateQueryDateRangeInfoDTO: executedDateRangeInfoDTO
        )

        executedConfigurationDTO.executedTemplateQueryDTOList.add(executedTemplateQueryDTO)
        executedConfigurationDTOCaseSeries.executedTemplateQueryDTOList.add(executedTemplateQueryDTO)
        service.CRUDService = [
                save                      : { theInstance ->
                    theInstance.save(failOnError: true)
                },
                instantSaveWithoutAuditLog: { theInstance ->
                    theInstance.save(failOnError: true)
                }]
        service.reportExecutorService = [createReportTemplate: { ReportTemplate template -> reportTemplateInstance }]

        superQuery = new SuperQuery(name: 'Test Query', owner: user, queryType: QueryTypeEnum.QUERY_BUILDER)
        queryValueListDTO = new QueryValueListDTO()

        executedConfiguration = new ExecutedConfiguration(reportName: "Test Report", owner: user,
                dateRangeType: DateRangeType.findByName("dcdtDatecol1"), sharedWith: [user], id: 1L, tenantId: 2L)
    }

    def setupSpec() {
        mockDomains User, ReportTemplate, DateRangeInformation, CustomSQLQuery, Configuration, SignalReportInfo, UserGroupUser, ExecutedCaseSeriesDateRangeInformation, ExecutedCaseDeliveryOption,
                DateRangeType, ExecutedConfiguration, SourceProfile, ReportResult, ExecutedTemplateQuery, ExecutedCaseSeries, WorkflowState, ExecutedCaseSeriesUserState, Tenant, SuperQuery,
                ExecutedConfiguration, TemplateSet, ExecutedTemplateSet, ExecutedGlobalDateRangeInformation
    }


    void "test createExecutedConfiguration"() {
        given:
        def mockExecutedConfigurationService = Mock(ExecutedConfigurationService)
        mockExecutedConfigurationService.createReportTemplate(_) >> {return new ReportTemplate()}
        service.executedConfigurationService = mockExecutedConfigurationService
        when:
        ExecutedGlobalDateRangeInformation executedGlobalDateRangeInformation = new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date())
        executedConfigurationDTO.executedGlobalDateRangeInformation = executedGlobalDateRangeInformation
        executedConfigurationDTO.asOfVersionDate = new Date()
        executedConfigurationDTO.evaluateDateAs = EvaluateCaseDateEnum.LATEST_VERSION
        ExecutedConfiguration executedConfiguration = service.createExecutedConfiguration(executedConfigurationDTO)
        then:
        noExceptionThrown()
        executedConfiguration.reportName == 'Test Report'
        executedConfiguration.cumulativeCaseSeriesId != null
    }

    void "test createExecutedConfiguration for usedCaseSeries"() {
        given:
        def mockExecutedConfigurationService = Mock(ExecutedConfigurationService)
        mockExecutedConfigurationService.createReportTemplate(_) >> {return new ReportTemplate()}
        service.executedConfigurationService = mockExecutedConfigurationService
        when:
        ExecutedGlobalDateRangeInformation executedGlobalDateRangeInformation = new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date())
        executedConfigurationDTOCaseSeries.executedGlobalDateRangeInformation = executedGlobalDateRangeInformation
        executedConfigurationDTOCaseSeries.asOfVersionDate = new Date()
        executedConfigurationDTOCaseSeries.evaluateDateAs = EvaluateCaseDateEnum.LATEST_VERSION
        ExecutedConfiguration executedConfiguration = service.createExecutedConfiguration(executedConfigurationDTOCaseSeries)
        then:
        noExceptionThrown()
        executedConfiguration.reportName == 'Test Report Case Series'
        executedConfiguration.usedCaseSeriesId != null
    }

    void "test createExecutedConfiguration for nestedTemplates check"(){
        given:
        TemplateSet templateSet = new TemplateSet()
        templateSet.id = templateId
        templateSet.owner = user
        templateSet.name = 'test_set'
        templateSet.templateType = TemplateTypeEnum.TEMPLATE_SET
        templateSet.modifiedBy = user.username
        templateSet.createdBy = user.username
        templateSet.nestedTemplates = [reportTemplateInstance]
        templateSet.save(flush: true, failOnError: true)
        ReportTemplate executedTemplate = new ExecutedTemplateSet(templateSet.properties)
        executedTemplate.name = 'test'
        executedTemplate.originalTemplateId = templateId
        executedTemplate.save(flush: true, failOnError: true)

        def mockExecutedConfigurationService = new MockFor(ExecutedConfigurationService)
        mockExecutedConfigurationService.demand.createReportTemplate(0..1) {ReportTemplate template ->
            return executedTemplate
        }

        service.executedConfigurationService = mockExecutedConfigurationService.proxyInstance()

        when:
        ExecutedGlobalDateRangeInformation executedGlobalDateRangeInformation = new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date())
        executedConfigurationDTO.executedGlobalDateRangeInformation = executedGlobalDateRangeInformation
        executedConfigurationDTO.asOfVersionDate = new Date()
        executedConfigurationDTO.evaluateDateAs = EvaluateCaseDateEnum.LATEST_VERSION
        ExecutedConfiguration executedConfiguration = service.createExecutedConfiguration(executedConfigurationDTO)
        then:
        executedConfiguration.executedTemplateQueries.executedTemplate.size() > 0

    }

    void "test createExecutedConfiguration method"() {
        given:
        DateUtil.metaClass.static.getOffsetString = { String timeZoneId ->
            return '+00:00'
        }

        User.metaClass.findByUsername = { name ->
            user
        }
        def mockExecutedConfigurationService = Mock(ExecutedConfigurationService)
        mockExecutedConfigurationService.createReportTemplate(_) >> {return new ReportTemplate()}
        service.executedConfigurationService = mockExecutedConfigurationService

        when:
        ExecutedGlobalDateRangeInformation executedGlobalDateRangeInformation = new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date())
        executedConfigurationDTO.executedGlobalDateRangeInformation = executedGlobalDateRangeInformation
        executedConfigurationDTO.asOfVersionDate = new Date()
        executedConfigurationDTO.evaluateDateAs = EvaluateCaseDateEnum.LATEST_VERSION
        ExecutedConfiguration executedConfiguration  = service.createExecutedConfiguration(executedConfigurationDTO)

        then:
        executedConfiguration?.reportName == executedConfigurationDTO.reportName
        executedConfiguration?.description == executedConfigurationDTO.description
        executedConfiguration?.owner ==  user
        executedConfiguration?.excludeFollowUp == executedConfigurationDTO.excludeFollowUp
        executedConfiguration?.excludeNonValidCases == executedConfigurationDTO.excludeNonValidCases
        executedConfiguration?.includeMedicallyConfirmedCases == executedConfigurationDTO.includeMedicallyConfirmedCases
    }

    void "Test createDateRangeInfo method" () {
        given:
        Date asOfVersionDate = new Date().clearTime()
        EvaluateCaseDateEnum evaluateDateAs =  EvaluateCaseDateEnum.VERSION_PER_REPORTING_PERIOD

        when:
        def result = service.getAsOfVersionDateCustom(asOfVersionDate, evaluateDateAs)

        then:
        result == asOfVersionDate
    }

    void "Test createExecutedDeliveryOption method" () {
        given:
        User.metaClass.findAllByUsernameInList = { users ->
            user
        }

        when:
        ExecutedDeliveryOption executedDeliveryOption = service.createExecutedDeliveryOption(executedConfigurationDTO, user)

        then:
        executedDeliveryOption.sharedWith == [user]
        executedDeliveryOption.sharedWithGroup == executedConfigurationDTO.sharedWithGroups
        executedDeliveryOption.emailToUsers == []
        executedDeliveryOption.attachmentFormats == null
    }

    void "Test getRunOnceScheduledDateJson method" () {
        given:
        Date date = new Date()
        String startupTime = (date).format(DateUtil.JSON_DATE)
        String result = "{\"startDateTime\":\"${startupTime}\",\"timeZone\":{\"name\" :\"UTC\",\"offset\" : \"+00:00\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1;COUNT=1;\"}"

        when:
        String scheduledDateJson = service.getRunOnceScheduledDateJson(user, date)

        then:
        scheduledDateJson == result
    }

    void "Test createExecutionStatus method"() {
        given:
        def CRUDService = new MockFor(CRUDService)
        CRUDService.demand.instantSaveWithoutAuditLog(1) { object -> return object }
        service.CRUDService = CRUDService.proxyInstance()
        ExecutedConfiguration.metaClass.getAllSharedUsers = { -> [user] }

        when:
        ExecutionStatus executionStatus = service.createExecutionStatus(executedConfiguration, ExecutingEntityTypeEnum.CONFIGURATION, null)

        then:
        executionStatus.entityType == ExecutingEntityTypeEnum.CONFIGURATION
        executionStatus.reportName == executedConfiguration.reportName
        executionStatus.entityId == executedConfiguration.id
        executionStatus.tenantId == executedConfiguration.tenantId
    }

}