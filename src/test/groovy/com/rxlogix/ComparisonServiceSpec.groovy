package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([MiscUtil, ViewHelper, IcsrProfileConfiguration, UnitConfiguration, ComparisonQueue, ExecutedReportConfiguration, ExecutionStatus])
class ComparisonServiceSpec extends Specification  implements DataTest, ServiceUnitTest<ComparisonService>{

    def setup() {
        service.queryService = [createExecutedQuery: { Object o -> return o }]
        service.executedConfigurationService = [createReportTemplate: { Object o -> return o }]
    }

    def setupSpec() {
        mockDomains ComparisonQueue, ComparisonResult, ExecutedReportConfiguration,ExecutedXMLTemplate, ExecutedIcsrReportConfiguration, ExecutedCaseLineListingTemplate, IcsrReportConfiguration, ExecutionStatus, UnitConfiguration, IcsrProfileConfiguration, ReportConfiguration, ExecutedConfiguration, Configuration, ExecutedTemplateQuery, TemplateQuery, ExecutedGlobalDateRangeInformation, GlobalDateRangeInformation, ExecutedDateRangeInformation, DateRangeInformation, ExecutedPeriodicReportConfiguration, PeriodicReportConfiguration
    }

    void "test compareCllDtSections"() {
        given:
        service.metaClass.getStringContent = { ReportResult r ->
            return r.sequenceNo == 1 ? content1 : content2
        }
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> code }
        ComparisonService.SectionComparisonResultDTO sectionResultDTO = new ComparisonService.SectionComparisonResultDTO(headerKeys:["a","b","c"])
        ReportResult r1 = new ReportResult(id: 1, sequenceNo: 1)
        r1.drillDownSource = new ExecutedTemplateQuery(executedConfiguration: new ExecutedConfiguration(reportName: "report1"))
        ReportResult r2 = new ReportResult(id: 2, sequenceNo: 2)
        r2.drillDownSource = new ExecutedTemplateQuery(executedConfiguration: new ExecutedConfiguration(reportName: "report1"))
        when:
        service.compareCllDtSections(r1, r2, clazz, sectionResultDTO)
        then:
        sectionResultDTO.sectionsAreEqual == sectionsAreEqual
        where:
        content1 << ["1,2,3\n4,5,6",
                     "1,2,3\n4,5,6",
                     "1,2,3\n4,5,6\n7,8,9",
                     "",
                     '[{"a":1,"b":"2","c":3},{"a":4,"b":"5","c":6}]',
                     '[{"a":1,"b":"2","c":3},{"a":4,"b":"5","c":6}]'
        ]
        content2 << ["1,2,3\n4,5,6",
                     "1,2,0\n4,5,6",
                     "1,2,3\n4,5,6",
                     "1,2,3\n4,5,6",
                     '[{"a":1,"b":"2","c":3},{"b":"5","a":4,"c":6}]',
                     '[{"a":1,"b":"2","c":0},{"a":4,"b":"5","c":6}]'
        ]
        clazz << [ExecutedCaseLineListingTemplate, ExecutedCaseLineListingTemplate, ExecutedCaseLineListingTemplate, ExecutedCaseLineListingTemplate, ExecutedDataTabulationTemplate, ExecutedDataTabulationTemplate]
        sectionsAreEqual << [true, false, false, false, true, false]
    }

    void "test compareIcsrReports"() {
        given:
        service.metaClass.getCasesForIcsrReports = { ExecutedReportConfiguration config1, ExecutedReportConfiguration config2 ->
            return [
                    ["123456": new IcsrReportCase(exIcsrTemplateQueryId: 1, caseNumber: "123456", versionNumber: 1)],
                    ["123456": new IcsrReportCase(exIcsrTemplateQueryId: 2, caseNumber: "123456", versionNumber: 1)]]
        }
        ViewHelper.metaClass.static.getMessage = { String code, Object[] params = null, String defaultLabel = '' -> code }
        ExecutedReportConfiguration config1 = new ExecutedIcsrReportConfiguration()
        ExecutedReportConfiguration config2 = new ExecutedIcsrReportConfiguration()
        ComparisonService.ComparisonResultDTO resultDTO = new ComparisonService.ComparisonResultDTO()

        when:
        int runTime = 0

        service.metaClass.getXmlForIcsr = { ExecutedReportConfiguration config, IcsrReportCase case1 ->
            if (runTime == 0) {
                runTime = 1
                return xml1
            } else {
                runTime = 0
                return xml2
            }
        }
        service.compareIcsrReports(config1, config2, resultDTO)
        then:
        resultDTO.reportsAreEqual == result
        where:
        xml1 << ["<a><b>1</b><c>2</c></a>",
                 "<a><b>1</b><c>2</c></a>",
                 "<a><b>1</b><c>2</c></a>",
        ]
        xml2 << ["<a><b>1</b><c>2</c></a>",
                 "<a><b> 1</b><c>2 </c></a>",
                 "<a><b>3</b><c>4</c></a>",
        ]
        result << [true, true, false]
    }

    void "test compareReports"() {
        given:

        service.metaClass.setType = { def template, ComparisonService.SectionComparisonResultDTO section, ReportResult reportResult -> }
        service.metaClass.compareCllDtSections = { ReportResult r1, ReportResult r2, Class clazz, ComparisonService.SectionComparisonResultDTO sectionResultDTO ->
            sectionResultDTO.sectionsAreEqual = true
        }
        when:
        ComparisonService.ComparisonResultDTO comparisonResultDTO = service.compareReports(cfg1, cfg2)
        then:
        comparisonResultDTO.reportsAreEqual == result
        where:

        cfg1 << [getExecutedConfig("report1", 1, null), getExecutedConfig("report1", 1, null), getExecutedConfig("report1", 1, ReportExecutionStatusEnum.GENERATED_CASES)]
        cfg2 << [getExecutedConfig("report2", 1, null), getExecutedConfig("report1", 2, null), getExecutedConfig("report1", 1, null)]
        result << [true, false, false]


    }

    def getExecutedConfig(String name, int sectionsNumber, ReportExecutionStatusEnum status) {
        def config1 = new ExecutedConfiguration(reportName: name, numOfExecutions: 1, status: status)
        if (status)
            config1 = new ExecutedPeriodicReportConfiguration(reportName: name, numOfExecutions: 1, status: status)
        config1.executedTemplateQueries = []
        for (int i = 0; i < sectionsNumber; i++) {
            config1.executedTemplateQueries << new ExecutedTemplateQuery(executedConfiguration: config1, executedTemplate: new ExecutedDataTabulationTemplate(name: "template" + i))
        }
        return config1
    }

    void "test copyAdhoc"() {
        given:
        String prefix = "check for"
        Date nextRunDate = new Date()
        User user = new User(username: "testuser")
        Date lastRunDate = new Date().minus(3)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
                executedTemplate: new ExecutedCaseLineListingTemplate(name: "template", owner: user))
        ExecutedDateRangeInformation dateRangeInformation = new ExecutedDateRangeInformation(
                executedAsOfVersionDate: new Date(),
                dateRangeEndAbsolute: Date.parse("yyyy-MM-dd", "2022-03-01"),
                dateRangeStartAbsolute: Date.parse("yyyy-MM-dd", "2022-04-01"),
                dateRangeEnum: DateRangeEnum.CUMULATIVE,
                executedTemplateQuery: executedTemplateQuery
        )
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery = dateRangeInformation
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(
                reportName: "report",
                description: "description",
                lastRunDate: lastRunDate,
                numOfExecutions: 2,
                productSelection: "productSelection",
                studySelection: "studySelection",
                eventSelection: "eventSelection",
                configSelectedTimeZone: "configSelectedTimeZone",
                asOfVersionDate: null,
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                emailConfiguration: new EmailConfiguration(),
                qualityChecked: true,
                executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(
                        dateRangeStartAbsolute: Date.parse("yyyy-MM-dd", "2022-01-01"),
                        dateRangeEndAbsolute: Date.parse("yyyy-MM-dd", "2022-02-01"),
                        dateRangeEnum: DateRangeEnum.CUMULATIVE),
                executedTemplateQueries: [executedTemplateQuery]
        )
        service.CRUDService = [
                save: { theInstance, Map saveParams = null ->
                    return theInstance
                }
        ]
        service.configurationService = new ConfigurationService()
        service.configurationService.metaClass.copyBlankValues = { TemplateQuery tqnew, List<QueryValueList> queryValueLists, List<TemplateValueList> templateValueLists -> tqnew }
        when:
        Configuration configuration = service.copyAdhoc(executedConfiguration, user, prefix, nextRunDate, null)
        then:
        configuration.reportName == "check for_report_2"
        configuration.owner.username == "testuser"
        configuration.nextRunDate == nextRunDate
        configuration.scheduleDateJSON == "{\"startDateTime\":\"${nextRunDate.format("yyyy-MM-dd'T'HH:mm")}Z\",\"timeZone\":{\"text\":\"(GMT +00:00) UTC\\n                                \",\"selected\":true,\"offset\":\"+00:00\",\"name\":\"UTC\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1;COUNT=1\"}"
        configuration.isDeleted == false
        configuration.isEnabled == true
        configuration.createdBy == "testuser"
        configuration.modifiedBy == "testuser"
        configuration.emailConfiguration == null
        configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF
        configuration.asOfVersionDate == lastRunDate
        configuration.globalDateRangeInformation.dateRangeStartAbsolute == Date.parse("yyyy-MM-dd", "2022-01-01")
        configuration.globalDateRangeInformation.dateRangeEndAbsolute == Date.parse("yyyy-MM-dd", "2022-02-01")
        configuration.globalDateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM
        configuration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute == Date.parse("yyyy-MM-dd", "2022-03-01")
        configuration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == Date.parse("yyyy-MM-dd", "2022-04-01")
        configuration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUSTOM


    }

    void "test copyAggregate"() {
        given:
        String prefix = "check for"
        Date nextRunDate = new Date()
        User user = new User(username: "testuser")
        Date lastRunDate = new Date().minus(3)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
                executedTemplate: new ExecutedCaseLineListingTemplate(name: "template", owner: user))
        ExecutedDateRangeInformation dateRangeInformation = new ExecutedDateRangeInformation(
                executedAsOfVersionDate: new Date(),
                dateRangeEndAbsolute: Date.parse("yyyy-MM-dd", "2022-03-01"),
                dateRangeStartAbsolute: Date.parse("yyyy-MM-dd", "2022-04-01"),
                dateRangeEnum: DateRangeEnum.CUMULATIVE,
                executedTemplateQuery: executedTemplateQuery
        )
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery = dateRangeInformation
        ExecutedPeriodicReportConfiguration executedConfiguration = new ExecutedPeriodicReportConfiguration(
                reportName: "report",
                description: "description",
                lastRunDate: lastRunDate,
                numOfExecutions: 2,
                productSelection: "productSelection",
                asOfVersionDate: null,
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                emailConfiguration: new EmailConfiguration(),
                qualityChecked: true,
                generateCaseSeries: true,
                reportingDestinations: ["one", "two"],
                executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(
                        dateRangeStartAbsolute: Date.parse("yyyy-MM-dd", "2022-01-01"),
                        dateRangeEndAbsolute: Date.parse("yyyy-MM-dd", "2022-02-01"),
                        dateRangeEnum: DateRangeEnum.CUMULATIVE),
                executedTemplateQueries: [executedTemplateQuery]
        )
        service.CRUDService = [
                save: { theInstance, Map saveParams = null ->
                    return theInstance
                }
        ]
        service.configurationService = new ConfigurationService()
        service.configurationService.metaClass.copyBlankValues = { TemplateQuery tqnew, List<QueryValueList> queryValueLists, List<TemplateValueList> templateValueLists -> tqnew }
        MiscUtil.metaClass.static.getObjectProperties = { Object o, List<String> includeFields = [] ->
            Map result = [:]
            includeFields.each {
                if (o.hasProperty(it))
                    result.put(it, o[it])
            }
            return result

        }
        when:
        PeriodicReportConfiguration configuration = service.copyAggregate(executedConfiguration, user, prefix, nextRunDate, true)
        then:
        configuration.reportName == "check for_report_2"
        configuration.owner.username == "testuser"
        configuration.nextRunDate == nextRunDate
        configuration.scheduleDateJSON == "{\"startDateTime\":\"${nextRunDate.format("yyyy-MM-dd'T'HH:mm")}Z\",\"timeZone\":{\"text\":\"(GMT +00:00) UTC\\n                                \",\"selected\":true,\"offset\":\"+00:00\",\"name\":\"UTC\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1;COUNT=1\"}"
        configuration.isDeleted == false
        configuration.isEnabled == true
        configuration.reportingDestinations.size() == 2
        configuration.createdBy == "testuser"
        configuration.modifiedBy == "testuser"
        configuration.emailConfiguration == null
        configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF
        configuration.asOfVersionDate == lastRunDate
        configuration.globalDateRangeInformation.dateRangeStartAbsolute == Date.parse("yyyy-MM-dd", "2022-01-01")
        configuration.globalDateRangeInformation.dateRangeEndAbsolute == Date.parse("yyyy-MM-dd", "2022-02-01")
        configuration.globalDateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM
        configuration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute == Date.parse("yyyy-MM-dd", "2022-03-01")
        configuration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == Date.parse("yyyy-MM-dd", "2022-04-01")
        configuration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUSTOM


    }

    void "test copyIcsr"() {
        given:
        String prefix = "check for"
        Date nextRunDate = new Date()
        User user = new User(username: "testuser")
        Date lastRunDate = new Date().minus(3)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
                executedTemplate: new ExecutedXMLTemplate(name: "template", owner: user))
        ExecutedDateRangeInformation dateRangeInformation = new ExecutedDateRangeInformation(
                executedAsOfVersionDate: new Date(),
                dateRangeEndAbsolute: Date.parse("yyyy-MM-dd", "2022-03-01"),
                dateRangeStartAbsolute: Date.parse("yyyy-MM-dd", "2022-04-01"),
                dateRangeEnum: DateRangeEnum.CUMULATIVE,
                executedTemplateQuery: executedTemplateQuery
        )
        executedTemplateQuery.executedDateRangeInformationForTemplateQuery = dateRangeInformation
        ExecutedIcsrReportConfiguration executedConfiguration = new ExecutedIcsrReportConfiguration(
                reportName: "report",
                description: "description",
                lastRunDate: lastRunDate,
                numOfExecutions: 2,
                productSelection: "productSelection",
                asOfVersionDate: null,
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                emailConfiguration: new EmailConfiguration(),
                qualityChecked: true,
                generateCaseSeries: true,
                reportingDestinations: ["one", "two"],
                executedGlobalDateRangeInformation: new ExecutedGlobalDateRangeInformation(
                        dateRangeStartAbsolute: Date.parse("yyyy-MM-dd", "2022-01-01"),
                        dateRangeEndAbsolute: Date.parse("yyyy-MM-dd", "2022-02-01"),
                        dateRangeEnum: DateRangeEnum.CUMULATIVE),
                executedTemplateQueries: [executedTemplateQuery]
        )
        service.CRUDService = [
                save: { theInstance, Map saveParams = null ->
                    return theInstance
                }
        ]
        service.configurationService = new ConfigurationService()
        service.configurationService.metaClass.copyBlankValues = { TemplateQuery tqnew, List<QueryValueList> queryValueLists, List<TemplateValueList> templateValueLists -> tqnew }
        MiscUtil.metaClass.static.getObjectProperties = { Object o, List<String> includeFields = [] ->
            Map result = [:]
            includeFields.each {
                if (o.hasProperty(it))
                    result.put(it, o[it])
            }
            return result

        }

        IcsrProfileConfiguration.metaClass.static.findByReportName = { String s ->
            return new IcsrProfileConfiguration()

        }

        UnitConfiguration.metaClass.static.findByUnitNameAndUnitTypeInList = { String s, List l ->
            return new UnitConfiguration()

        }
        when:
        IcsrReportConfiguration configuration = service.copyIcsr(executedConfiguration, user, prefix, nextRunDate)
        then:
        configuration.reportName == "check for_report_2"
        configuration.owner.username == "testuser"
        configuration.nextRunDate == nextRunDate
        configuration.scheduleDateJSON == "{\"startDateTime\":\"${nextRunDate.format("yyyy-MM-dd'T'HH:mm")}Z\",\"timeZone\":{\"text\":\"(GMT +00:00) UTC\\n                                \",\"selected\":true,\"offset\":\"+00:00\",\"name\":\"UTC\"},\"recurrencePattern\":\"FREQ=DAILY;INTERVAL=1;COUNT=1\"}"
        configuration.isDeleted == false
        configuration.isEnabled == true
        configuration.reportingDestinations.size() == 2
        configuration.createdBy == "testuser"
        configuration.modifiedBy == "testuser"
        configuration.emailConfiguration == null
        configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF
        configuration.asOfVersionDate == lastRunDate
        configuration.globalDateRangeInformation.dateRangeStartAbsolute == Date.parse("yyyy-MM-dd", "2022-01-01")
        configuration.globalDateRangeInformation.dateRangeEndAbsolute == Date.parse("yyyy-MM-dd", "2022-02-01")
        configuration.globalDateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM
        configuration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute == Date.parse("yyyy-MM-dd", "2022-03-01")
        configuration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute == Date.parse("yyyy-MM-dd", "2022-04-01")
        configuration.templateQueries[0].dateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUSTOM
    }

    void "test compareJob"() {
        given:
        ExecutedConfiguration excofnig = new ExecutedConfiguration(id: 1L, reportName: "source")
        excofnig.save(failOnError: true, validate: false, flush: true)
        Configuration copyConfig = new Configuration(id: 2L, reportName: "copyConfig")
        copyConfig.save(failOnError: true, validate: false, flush: true)
        ExecutedConfiguration copy = new ExecutedConfiguration(id: 3L, reportName: "copy", status: ReportExecutionStatusEnum.COMPLETED)
        copy.save(failOnError: true, validate: false, flush: true)
        ComparisonQueue comparisonQueue = new ComparisonQueue(id: 1, entityId1: 1, entityId2: 2, entityName1: "entityName1", entityName2: "entityName2", entityType: "entityType", status: ComparisonQueue.Status.WAITING, dateCompared: new Date(), message: "message")
        comparisonQueue.save(failOnError: true, validate: false, flush: true)
        ComparisonQueue.metaClass.static.findAllByStatus = { ComparisonQueue.Status s ->
            return [comparisonQueue]

        }
        ExecutedReportConfiguration.metaClass.static.findByReportNameAndOwnerAndNumOfExecutions = { String name, User u, int n ->
            return copy
        }
        ExecutionStatus.metaClass.static.findByExecutedEntityIdAndExecutionStatusNotInList = { Long id, List l -> true }
        service.metaClass.compareResult = { ExecutedReportConfiguration config1, ExecutedReportConfiguration config2 ->
            ComparisonService.ComparisonResultDTO result = new ComparisonService.ComparisonResultDTO()
            result.reportsAreEqual = true
            result.supported = true
            result.message = "message"
            return result
        }
        when:
        service.compareJob()
        then:
        comparisonQueue.status == ComparisonQueue.Status.COMPLETED
        ComparisonResult.list().size() == 1
    }

    void "test compareAndSave"() {
        given:
        ExecutedConfiguration source = new ExecutedConfiguration(id: 1L, reportName: "source", numOfExecutions: 1)
        source.save(failOnError: true, validate: false, flush: true)
        ExecutedConfiguration copy = new ExecutedConfiguration(id: 2L, reportName: "copy", numOfExecutions: 1)
        copy.save(failOnError: true, validate: false, flush: true)
        service.metaClass.compareReports = { ExecutedReportConfiguration s, ExecutedReportConfiguration c ->
            ComparisonService.ComparisonResultDTO result = new ComparisonService.ComparisonResultDTO()
            result.reportsAreEqual = true
            result.supported = true
            result.message = "message"
            return result
        }
        when:
        service.compareAndSave(source, copy)
        then:
        ComparisonResult.list().size() == 1


    }
}
