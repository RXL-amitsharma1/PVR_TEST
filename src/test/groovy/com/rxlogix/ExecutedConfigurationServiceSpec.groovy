package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.enums.*
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import com.rxlogix.util.DateUtil
import com.rxlogix.util.RelativeDateConverter
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SuperQuery, ReportTemplate])
class ExecutedConfigurationServiceSpec extends Specification implements DataTest, ServiceUnitTest<ExecutedConfigurationService> {

    @Shared User normalUser
    @Shared Preference preference
    @Shared Configuration configuration

    def setupSpec() {
        mockDomains CaseLineListingTemplate, Tag, SourceColumnMaster ,Configuration, ReportResult, ReportFieldInfoList, ReportFieldInfo, User, Role, UserRole, Preference, Tenant, TemplateQuery, SharedWith, ExecutedConfiguration, ExecutedPeriodicReportConfiguration, ExecutedTemplateQuery, DeliveryOption, ExecutedDeliveryOption,ReportTemplate, ReportField, TemplateQuery, Query, TemplateSet, ExecutedIcsrProfileConfiguration
    }

    def setup() {
        def username = "unitTest"
        preference = new Preference(locale: new Locale("en"), createdBy: username, modifiedBy: username)
        normalUser = createUser(username, "ROLE_TEMPLATE_VIEW")
        configuration = createConfiguration(normalUser)
    }

    void "test getPersistedReportResult"(){
        given:
        ReportTemplate templ1 = new ReportTemplate(id: 1L, name: "Template 1", description: 'Nested template 1', templateType: TemplateTypeEnum.CASE_LINE, owner: normalUser)
        templ1.setProperty('id', 1L)
        ReportTemplate templ2 = new ReportTemplate(id: 2L, name: "Template 2", description: 'Nested template 2', templateType: TemplateTypeEnum.CASE_LINE, owner: normalUser)
        templ2.setProperty('id', 2L)
        TemplateSet templateSet = new TemplateSet(id: 3L, name: "testTemplate", description: "Test Description", owner: normalUser, nestedTemplates: [],linkSectionsByGrouping: linkSectionsByGrouping, sectionBreakByEachTemplate: sectionBreakByEachTemplate)
        templateSet.setProperty('id', 3L)
        templateSet.addToNestedTemplates(templ1)
        templateSet.addToNestedTemplates(templ2)
        TemplateQuery templateQuery = new TemplateQuery(id: 1L, template: templateSet, report: new Configuration())
        DateRangeInformation dateRangeInformation = new DateRangeInformation(dateRangeEnum: DateRangeEnum.CUSTOM, templateQuery: templateQuery,
                dateRangeStartAbsolute: new DateTime(2016, 3, 10, 0, 0, 0, 0).toDate(),
                dateRangeEndAbsolute:new DateTime(2016, 3, 15, 0, 0, 0, 0).toDate())
        templateQuery.dateRangeInformationForTemplateQuery = dateRangeInformation
        ExecutedIcsrProfileConfiguration configuration = new ExecutedIcsrProfileConfiguration()
        service.metaClass.createReportTemplate = { ReportTemplate template ->
            return new ReportTemplate()
        }
        service.metaClass.addExecutedTemplateQueryToExecutedConfiguration = { TemplateQuery templateQuery1, ReportTemplate template ->
            return new ExecutedTemplateQuery()
        }
        def mockQueryService = Mock(QueryService)
        mockQueryService.createExecutedQuery(_) >> {return new SuperQuery()}
        service.queryService = mockQueryService
        def mockUserService = Mock(UserService)
        mockUserService.setOwnershipAndModifier(_) >> {return true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.userService >> mockUserService
        service.CRUDService = mockCRUDService
        ReportTemplate.metaClass.static.getLatestExRptTempltByOrigTempltId = { templateId -> new Object(){
            def get(Object o){
                return templateSet.id
            }
        }
        }

        when:
        service.getPersistedReportResult(templateQuery, configuration, normalUser, false)

        then:
        configuration.executedTemplateQueries.size() == sizeVal

        where:
        linkSectionsByGrouping | sectionBreakByEachTemplate | sizeVal
        true                   | false                      | 1
        false                  | true                       | 2
        false                  | false                      | 1
    }

    def "Test createExecutedConfiguration()" () {
        given:
        ReportTemplate templ1 = new ReportTemplate(id: 1L, name: "Template 1", description: 'Nested template 1', templateType: TemplateTypeEnum.CASE_LINE, owner: normalUser)
        templ1.setProperty('id', 1L)
        ReportTemplate templ2 = new ReportTemplate(id: 2L, name: "Template 2", description: 'Nested template 2', templateType: TemplateTypeEnum.CASE_LINE, owner: normalUser)
        templ2.setProperty('id', 2L)
        TemplateSet templateSet = new TemplateSet(id: 3L, name: "testTemplate", description: "Test Description", owner: normalUser, nestedTemplates: [],linkSectionsByGrouping: true, sectionBreakByEachTemplate: false)
        templateSet.setProperty('id', 3L)
        templateSet.addToNestedTemplates(templ1)
        templateSet.addToNestedTemplates(templ2)
        def mockUserService = Mock(UserService)
        mockUserService.setOwnershipAndModifier(_) >> {return true}
        def mockCRUDService = Mock(CRUDService)
        mockCRUDService.userService >> mockUserService
        service.CRUDService = mockCRUDService
        service.sqlGenerationService = Mock(SqlGenerationService)
        service.configurationService = Mock(ConfigurationService)
        service.templateService = Mock(TemplateService)
        service.queryService = Mock(QueryService)
        def mockEtlJobService = Mock(EtlJobService)
        mockEtlJobService.lastSuccessfulEtlStartTime() >> {return new Date("Mon Oct 18 07:21:22 PM UTC 2021")}
        mockEtlJobService.getSchedule() >> {return ['startDateTime':  '2015-03-31T03:23+02:00']}
        service.etlJobService = mockEtlJobService
        ReportTemplate.metaClass.static.getLatestExRptTempltByOrigTempltId = { templateId -> new Object(){
            def get(Object o){
                return 10L
            }
        }
        }
        SuperQuery.metaClass.static.getLatestExQueryByOrigQueryId = { queryId -> new Object(){
            def get(Object o){
                return 11L
            }
        }
        }

        when:
        Configuration cfg = createConfiguration(normalUser)
        cfg.pvqType="pvq"
        cfg.templateQueries[0].rootCause=1L
        cfg.templateQueries[0].responsibleParty=2L
        cfg.templateQueries[0].issueType=3L
        cfg.templateQueries[0].assignedToUser=normalUser
        cfg.templateQueries[0].priority="priority"
        def executedConfiguration = service.createExecutedConfiguration(configuration, configuration.nextRunDate)

        then:
        assert executedConfiguration.reportName == configuration.reportName
        assert executedConfiguration.owner == configuration.owner
        assert executedConfiguration.scheduleDateJSON == configuration.scheduleDateJSON
        assert executedConfiguration.nextRunDate == configuration.nextRunDate
        assert executedConfiguration.description == configuration.description
        assert executedConfiguration.dateCreated == configuration.dateCreated
        assert executedConfiguration.lastUpdated == configuration.lastUpdated
        assert executedConfiguration.isDeleted == configuration.isDeleted
        assert executedConfiguration.isEnabled == configuration.isEnabled
        assert compareCollections(executedConfiguration.tags, configuration.tags)
        assert executedConfiguration.dateRangeType == configuration.dateRangeType
        assert executedConfiguration.productSelection == configuration.productSelection
        assert executedConfiguration.eventSelection == configuration.eventSelection
        assert executedConfiguration.studySelection == configuration.studySelection
        assert executedConfiguration.configSelectedTimeZone == configuration.configSelectedTimeZone
        assert executedConfiguration.evaluateDateAs == configuration.evaluateDateAs
        assert executedConfiguration.excludeFollowUp == configuration.excludeFollowUp
        assert executedConfiguration.includeLockedVersion == configuration.includeLockedVersion
        assert executedConfiguration.includeAllStudyDrugsCases == configuration.includeAllStudyDrugsCases
        assert executedConfiguration.excludeNonValidCases == configuration.excludeNonValidCases
        assert executedConfiguration.excludeDeletedCases == configuration.excludeDeletedCases
        assert executedConfiguration.suspectProduct == configuration.suspectProduct
        assert executedConfiguration.adjustPerScheduleFrequency == configuration.adjustPerScheduleFrequency

        assert compareCollections(executedConfiguration.executedDeliveryOption.sharedWith, configuration.deliveryOption.sharedWith)
        assert compareCollections(executedConfiguration.executedDeliveryOption.attachmentFormats, configuration.deliveryOption.attachmentFormats)
        assert compareCollections(executedConfiguration.executedDeliveryOption.emailToUsers, configuration.deliveryOption.emailToUsers)

        assert executedConfiguration.createdBy == configuration.getOwner().username
        assert executedConfiguration.modifiedBy == configuration.modifiedBy
        assert executedConfiguration.executionStatus == ReportExecutionStatusEnum.SCHEDULED.value()
        assert executedConfiguration.numOfExecutions == 1
        assert executedConfiguration.lastRunDate.clearTime() == new Date().clearTime()
        assert executedConfiguration.limitPrimaryPath == configuration?.limitPrimaryPath
        assert executedConfiguration.blankValuesJSON == configuration?.blankValuesJSON
        assert executedConfiguration.includeMedicallyConfirmedCases == configuration?.includeMedicallyConfirmedCases
        assert executedConfiguration.asOfVersionDate == configuration.asOfVersionDate
        assert executedConfiguration.pvqType == "pvq"
        assert executedConfiguration.executedTemplateQueries[0].rootCause == 1L
        assert executedConfiguration.executedTemplateQueries[0].responsibleParty == 2L
        assert executedConfiguration.executedTemplateQueries[0].issueType == 3L
        assert executedConfiguration.executedTemplateQueries[0].assignedToUser == normalUser
        assert executedConfiguration.executedTemplateQueries[0].priority == "priority"
        //todo:  this field needs to be refactored away (deleted); ExecutedDeliveryOption and DeliveryOption both have SharedWith
        assert executedConfiguration.executedDeliveryOption?.sharedWith?.toSet()[0] == configuration.deliveryOption?.sharedWith?.toSet()[0]

        /*
            Note regarding ExecutedTemplateQueries:

            When an ExecutedConfiguration is created, the TemplateQueries belonging to the Configuration are known and should be copied to ExecutedConfiguration.executedTemplateQueries.

            That does not happen.  Instead, the TemplateQueries are only moved to ExecutedConfiguration during executeReportJob().getPersistedReportResult().

            What we really needed was a reference from ExecutedTemplateQuery to TemplateQuery.  With that, we could store the ExecutedTemplateQuery independently and later retrieve the
            ExecutedTemplateQuery to set it on the ReportResult.

            The net result of all of this is that we cannot test or verify the contents of executedTemplateQueries while
            creating an ExecutedConfiguration.

         */

    }

    private boolean compareCollections(Collection collection1, Collection collection2) {
        if (collection1 && collection2) {
            def elementsInCommon = collection1.intersect(collection2)
            def difference = collection1.plus(collection2)
            difference.removeAll(elementsInCommon)
            return difference.size() == 0
        }

        if (!collection1 && !collection2) { return true }

        return false
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

    private User createUser(String username, String role) {
        def userRole = new Role(authority: role, createdBy: username, modifiedBy: username).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Normal User", preference: preference, createdBy: username, modifiedBy: username)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
        return normalUser
    }

    private String getRunOnceScheduledDateJson() {
        User user = normalUser
        def startupTime = (RelativeDateConverter.getFormattedCurrentDateTimeForTimeZone(user, DateUtil.JSON_DATE))
        def timeZone = DateUtil.getTimezoneForRunOnce(user)
        return """{"startDateTime":"${
            startupTime
        }","timeZone":{"${timeZone}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""

    }


    private Configuration createConfiguration(User normalUser) {

        CaseLineListingTemplate template1 = createTemplate("Test template 1: Case Number")
        template1.setProperty('id', 10L)
        CaseLineListingTemplate template2 = createTemplate("Test template 2: Case Number")
        template2.setProperty('id', 20L)

        Query query = createQuery()
        query.setId(11L)

        TemplateQuery templateQuery1 = createTemplateQuery(template1,query)
        templateQuery1.header = "Custom Header"
        templateQuery1.title = "Custom Title"
        templateQuery1.footer = "Custom Footer"
        DateRangeInformation dateRangeInformation = new DateRangeInformation(dateRangeEnum: DateRangeEnum.CUSTOM,
                dateRangeStartAbsolute: new DateTime(2016, 3, 10, 0, 0, 0, 0).toDate(),
                dateRangeEndAbsolute:new DateTime(2016, 3, 15, 0, 0, 0, 0).toDate(),templateQuery: templateQuery1)
        templateQuery1.dateRangeInformationForTemplateQuery = dateRangeInformation
        GlobalDateRangeInformation globalDateRangeInformation = new GlobalDateRangeInformation(dateRangeEnum: DateRangeEnum.CUSTOM,
                dateRangeStartAbsolute: new DateTime(2016, 3, 10, 0, 0, 0, 0).toDate(),
                dateRangeEndAbsolute:new DateTime(2016, 3, 15, 0, 0, 0, 0).toDate())
        TemplateQuery templateQuery2 = createTemplateQuery(template2,query)
        DateRangeInformation dateRangeInformation2 = new DateRangeInformation(dateRangeEnum: DateRangeEnum.CUMULATIVE,templateQuery: templateQuery2)
        templateQuery2.dateRangeInformationForTemplateQuery = dateRangeInformation2

        configuration = new Configuration(reportName: "Test Configuration",
                description: "Configuration for Unit Test",
                nextRunDate: new DateTime(2016, 4, 20, 0, 0, 0, 0).toDate(),
                asOfVersionDate: new DateTime(2016, 3, 20, 0, 0, 0, 0).toDate(),
                /*dateRangeType: DateRangeTypeCaseEnum.CASE_RECEIPT_DATE,*/ //TODO: Need to check here
                deliveryOption: new DeliveryOption(sharedWith: [normalUser],
                        attachmentFormats: [ReportFormatEnum.PDF]),
                productSelection: '{"1":[{"name":"DEXIBUPROFEN","id":2685}],"2":[],"3":[],"4":[]}',
                studySelection: '{"1":[],"2":[{"name":"MPS001","id":100012}],"3":[]}',
                eventSelection: '{"1":[],"2":[],"3":[],"4":[{"name":"Anaemia","id":10002034}],"5":[],"6":[]}',
                owner: normalUser,
                createdBy: normalUser.username,
                modifiedBy: normalUser.username, globalDateRangeInformation: globalDateRangeInformation)

        configuration.addToTags(new Tag(name: "Tag 1"))
        configuration.addToTags(new Tag(name: "Tag 2"))
        configuration.addToTemplateQueries(templateQuery1)
        configuration.addToTemplateQueries(templateQuery2)
        configuration.scheduleDateJSON = runOnceScheduledDateJson
        return configuration
    }

    private Query createQuery() {

        def JSONQuery = """{ "all": { "containerGroups": [
        { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }"""

        Query query = new Query(name: "Test Query",
                queryType: QueryTypeEnum.QUERY_BUILDER,
                JSONQuery: JSONQuery,
                owner: normalUser,
                createdBy: normalUser.username,
                modifiedBy: normalUser.username
        )

        return query
    }

    private CaseLineListingTemplate createTemplate(String name) {

        ReportField reportField = new ReportField(name: "masterCaseNum")
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: reportField, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo)

        CaseLineListingTemplate template = new CaseLineListingTemplate(name: name, columnList: reportFieldInfoList, templateType: TemplateTypeEnum.CASE_LINE, owner: normalUser)

        return template
    }

    private TemplateQuery createTemplateQuery(template, query) {

        TemplateQuery templateQuery  = new TemplateQuery(template: template,
                dateRangeInformationForTemplateQuery: new DateRangeInformation(),
                report: configuration, createdBy: normalUser.username, modifiedBy: normalUser.username, query: query)

        return templateQuery
    }

}
