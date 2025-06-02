package reports


import com.rxlogix.DynamicReportService
import com.rxlogix.ReportExecutorService
import com.rxlogix.SeedDataService
import com.rxlogix.config.*
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.User
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import groovy.time.TimeCategory
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification
import com.rxlogix.util.DateUtil

import java.util.zip.GZIPInputStream

@Integration
@Rollback
class DynamicReportServiceSpec extends Specification {
    def setup() {}

    def cleanup() {}

    @Autowired
    ReportExecutorService reportExecutorService

    @Autowired
    DynamicReportService dynamicReportService

    // disable transactions for the test because we're using custom transaction handling in the ReportGeneration code
    // alternative approach : http://beckje01.com/blog/2015/02/14/grails-integration-testing-of-complex-transactions/
    static transactional = false

    /**
     * This test will generate the report output for the small & medium datasets. The large and xlarge result in errors
     * due to memory consumption. Memory for the test can be controlled via BuildConfig.groovy via the
     * grails.project.fork [ test [:] ] block.
     *
     * Ideally we would like to reduce the resource consumption to the point that generating a jasper report output
     * using the medium data set would be possible w/ < 1GB memory allocated. Currently using 2GB default and the G1GC garbage collector
     * which is helps w/ the high CPU utilization if you end up consuming most of the free memory
     *
     * The default ReportResultsData had to be modified to set a maxSize constraint on the value field, otherwise H2 in memory DB would not
     * create the required schema correctly (it would default to 4k size).
     */
    @Ignore
    void "Generate a Jasper report using a JSON dataset as input"() {
        given: "A real template and query to execute a report"
        // generate a sample report to run
        TemplateQuery templateQuery = setupSampleReport("JSON Report")


        when: "The sample report completes, switch out its data field for a pre-generated data set"
        reportExecutorService.runConfigurations()
        reportExecutorService.executeEntities()
        ReportResult result = ReportResult.findByTemplateQuery(templateQuery)

        /**
         * Statistics for JRFileVirtualizer & 12GB RAM
         *
         * use /test/data/small.json.gz for ~ 13k data = 72 page PDF (330k)
         * use /test/data/medium.json.gz for ~ 3MB data = 22,940 page PDF (80MB) @ 106s
         * use /test/data/large.json.gz for ~ 100MB data = ????? page PDF (296MB) @ 429s
         * use /test/data/xlarge.json.gz for ~ 489MB data (cannot check into github)
         */
        File inputFile = new File("./test/data/small.json.gz")
        ReportResultData resultData = new ReportResultData(value: inputFile.bytes)
        result.data = resultData

        result.save(failOnError: true)


        then: "Report output is generated without errors"
        println("Generating the report output")
        File reportFile = dynamicReportService.createReportWithCriteriaSheet(result, [outputFormat: ReportFormatEnum.PDF.name()]) // [:] will default HTML output, use [outputFormat:ReportFormatEnum.PDF.name()]

        // Report output files are designed to be deleted on exit, so make a copy
        File copyOfReport = new File(reportFile.parentFile, FilenameUtils.getBaseName(reportFile.name) + "_copy.${FilenameUtils.getExtension(reportFile.name)}")
        FileUtils.copyFile(reportFile, copyOfReport)
        println("Report output sent to: ${copyOfReport.absolutePath}")
        debugMemoryUsage()
    }

    @Ignore
    void "Generate a Jasper report using a CSV dataset as input"() {
        given: "A real template and query to execute a report"
        // generate a sample report to run
        TemplateQuery templateQuery = setupSampleReport("CSV Report")


        when: "The sample report completes, switch out its data field for a pre-generated data set"
        reportExecutorService.runConfigurations()
        reportExecutorService.executeEntities()
        ReportResult result = ReportResult.findByTemplateQuery(templateQuery)

        /**
         * Also available: medium.csv.gz, large.csv.gz
         */
        File inputFile = new File("./test/data/XXL.csv.gz")
        ReportResultData resultData = new ReportResultData(value: inputFile.bytes)
        result.data = resultData

        result.save(failOnError: true)


        then: "Report output is generated without errors"
        println("Generating the report output")
        File reportFileCSV = dynamicReportService.createReportWithCriteriaSheetCSV(result, false, [outputFormat: ReportFormatEnum.PDF.name()]) // [:] will default HTML output, use [outputFormat:ReportFormatEnum.PDF.name()]

        // Report output files are designed to be deleted on exit, so make a copy
        File copyOfReportCSV = new File(reportFileCSV.parentFile, FilenameUtils.getBaseName(reportFileCSV.name) + "_copy.${FilenameUtils.getExtension(reportFileCSV.name)}")
        FileUtils.copyFile(reportFileCSV, copyOfReportCSV)
        println("CSV Report output sent to: ${copyOfReportCSV.absolutePath}")
        debugMemoryUsage()
    }

    @Ignore
    //TODO: Return to this on template set
    void "Generate a Jasper report that has a template set using a CSV dataset as input"() {
        given: "A real template and query to execute a report"
        // generate a sample report to run
        TemplateQuery templateQuery = setupSampleTemplateSetReport("CSV Template Set Report")


        when: "The sample report completes, switch out its data field for a pre-generated data set"
        reportExecutorService.runConfigurations()
        reportExecutorService.executeEntities()
        ReportResult result = ReportResult.findByTemplateQuery(templateQuery)

        /**
         * Also available: medium.csv.gz, large.csv.gz
         */
//        File inputFile = new File("./test/data/XXL.csv.gz")
//        ReportResultData resultData = new ReportResultData(value: inputFile.bytes)
//        result.data = resultData
//
//        result.save(failOnError: true)


        then: "Report output is generated without errors"
        println("Generating the report output")
        File reportFileCSV = dynamicReportService.createReportWithCriteriaSheetCSV(result, false, [outputFormat: ReportFormatEnum.PDF.name()]) // [:] will default HTML output, use [outputFormat:ReportFormatEnum.PDF.name()]

        // Report output files are designed to be deleted on exit, so make a copy
        File copyOfReportCSV = new File(reportFileCSV.parentFile, FilenameUtils.getBaseName(reportFileCSV.name) + "_copy.${FilenameUtils.getExtension(reportFileCSV.name)}")
        FileUtils.copyFile(reportFileCSV, copyOfReportCSV)
        println("CSV Report output sent to: ${copyOfReportCSV.absolutePath}")
        debugMemoryUsage()
    }

    @Ignore
    void "Convert JSON ReportResultData to JSON"() {
        given: "JSON data from reportResultData"
        File inputFile = new File("./test/data/XXL.json.gz")
        GZIPInputStream gzis = new GZIPInputStream(new BufferedInputStream(new FileInputStream(inputFile)))

        when: "JSON data is converted to CSV"
        File resultFile = dynamicReportService.convertJSONToCSV(gzis)
        gzis.close()

        then: "Correct CSV is generated"
        File outputFile = new File("./test/data/XXL.csv.gz")
        IOUtils.copy(new BufferedInputStream(new FileInputStream(resultFile)), new BufferedOutputStream(new FileOutputStream(outputFile)))
        resultFile.delete()

        println("CSV.GZ output sent to: ${outputFile.absolutePath}")

        debugMemoryUsage()
    }

    /**
     * Create a sample report we will use to graft our large report data set to once the report is scheduled and completed.
     * This will create all the intermediate objects we require to generate the report output using Jasper
     *
     * @return TemplateQuery for the scheduled configuration
     */
    private TemplateQuery setupSampleReport(String reportName) {
        def adminUser = User.findByUsername("admin")
        def startupTime = (new Date()).format(DateUtil.JSON_DATE)

        // get us last years date
        def last30Days
        use(TimeCategory) {
            last30Days = new Date() - 30.days
        }

        def runNow = """{"startDateTime":"${
            startupTime
        }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""

        def template = CaseLineListingTemplate.findByName("CIOMS I")
        def configuration = new Configuration([reportName: reportName, isEnabled: true, isDeleted: false, description: 'This is a sample CIOMS I report to test huge report output', scheduleDateJSON: runNow, owner: adminUser, reportDateRange: last30Days, runNow: true, dateCreated: new Date(), nextRunDate: new Date(), scheduledBy: adminUser, tags: [], deliveryOption: new DeliveryOption(sharedWith: [adminUser], attachmentFormats: [ReportFormatEnum.PDF])])

        configuration.deliveryOption.save()
        configuration.createdBy = SeedDataService.USERNAME
        configuration.modifiedBy = SeedDataService.USERNAME
        configuration.addToTemplateQueries(new TemplateQuery(template: template, dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: SeedDataService.USERNAME, modifiedBy: SeedDataService.USERNAME))
        configuration.save(failOnError: true)

        configuration.save()

        return configuration.templateQueries.first()
    }

    /**
     * Create a sample report we will use to graft our large report data set to once the report is scheduled and completed.
     * This will create all the intermediate objects we require to generate the report output using Jasper
     *
     * @return TemplateQuery for the scheduled configuration
     */
    private TemplateQuery setupSampleTemplateSetReport(String reportName) {
        def adminUser = User.findByUsername("admin")
        def startupTime = (new Date()).format(DateUtil.JSON_DATE)

        // get us last years date
        def last30Days
        use(TimeCategory) {
            last30Days = new Date() - 30.days
        }

        def runNow = """{"startDateTime":"${
            startupTime
        }","timeZone":{"name":"UTC","offset":"+00:00"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""

        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        ReportFieldInfo countryInfo = new ReportFieldInfo(reportField: ReportField.findByNameAndIsDeleted("masterCountryId",false), argusName: "cm.OCCURED_COUNTRY_ID", sortLevel: -1, stackId: -1, suppressRepeatingValues: 0)
        reportFieldInfoList.addToReportFieldInfoList(countryInfo)
        ReportFieldInfoList groupingList = new ReportFieldInfoList()
        ReportFieldInfo caseNumInfo = new ReportFieldInfo(reportField: ReportField.findByNameAndIsDeleted("masterCaseNum",false), argusName: "cm.CASE_NUM", sortLevel: -1, stackId: -1, suppressRepeatingValues: 0)
        groupingList.addToReportFieldInfoList(caseNumInfo)
        CaseLineListingTemplate template1 = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE,
                description: "CLL1", name: "Template 1", isDeleted: false, createdBy: "adminUser", owner: adminUser,
                modifiedBy: "adminUser", columnList: reportFieldInfoList, groupingList: groupingList).save(flush: true, failOnError: true)

        reportFieldInfoList = new ReportFieldInfoList()
        countryInfo = new ReportFieldInfo(reportField: ReportField.findByNameAndIsDeleted("masterCountryId",false), argusName: "cm.OCCURED_COUNTRY_ID", sortLevel: -1, stackId: -1, suppressRepeatingValues: 0)
        reportFieldInfoList.addToReportFieldInfoList(countryInfo)
        groupingList = new ReportFieldInfoList()
        caseNumInfo = new ReportFieldInfo(reportField: ReportField.findByNameAndIsDeleted("masterCaseNum",false), argusName: "cm.CASE_NUM", sortLevel: -1, stackId: -1, suppressRepeatingValues: 0)
        groupingList.addToReportFieldInfoList(caseNumInfo)
        CaseLineListingTemplate template2 = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE,
                description: "CLL2", name: "Template 2", isDeleted: false, createdBy: "adminUser", owner: adminUser,
                modifiedBy: "adminUser", columnList: reportFieldInfoList, groupingList: groupingList).save(flush: true, failOnError: true)

        TemplateSet templateSet = new TemplateSet(templateType: TemplateTypeEnum.TEMPLATE_SET,
                description: "TEST - Template set", name: "Template Set Test", isDeleted: false, createdBy: "adminUser",
                owner: adminUser, modifiedBy: "adminUser")
        templateSet.addToNestedTemplates(template1)
        templateSet.addToNestedTemplates(template2)
        templateSet.save(flush: true, failOnError: true)


        def configuration = new Configuration([reportName: reportName, isEnabled: true, isDeleted: false, description: 'This is a sample CIOMS I report to test huge report output', scheduleDateJSON: runNow, owner: adminUser, reportDateRange: last30Days, runNow: true, dateCreated: new Date(), nextRunDate: new Date(), scheduledBy: adminUser, tags: [], deliveryOption: new DeliveryOption(sharedWith: [adminUser], attachmentFormats: [ReportFormatEnum.PDF])])

        configuration.deliveryOption.save()
        configuration.createdBy = SeedDataService.USERNAME
        configuration.modifiedBy = SeedDataService.USERNAME
        configuration.addToTemplateQueries(new TemplateQuery(template: templateSet, dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: SeedDataService.USERNAME, modifiedBy: SeedDataService.USERNAME))
        configuration.save(failOnError: true)

        configuration.save()

        return configuration.templateQueries.first()
    }

    private void debugMemoryUsage() {
        int availableProcessors = Runtime.getRuntime().availableProcessors()
        Double freeMemory = ((Double) Runtime.getRuntime().freeMemory() / (1024 * 1024))?.round(2)
        Double maxMemory = ((Double) Runtime.getRuntime().maxMemory() / (1024 * 1024))?.round(2)
        Double totalMemory = ((Double) Runtime.getRuntime().totalMemory() / (1024 * 1024))?.round(2)

        println("${availableProcessors} processors and ${totalMemory}MB total memory, ${maxMemory - freeMemory}MB free out of ${maxMemory}MB")
    }
}
