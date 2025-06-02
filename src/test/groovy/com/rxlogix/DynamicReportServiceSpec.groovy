package com.rxlogix

import asset.pipeline.grails.LinkGenerator
import com.rxlogix.config.*
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.enums.XMLNodeElementType
import com.rxlogix.enums.XMLNodeType
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import groovy.mock.interceptor.MockFor
import net.sf.dynamicreports.report.exception.DRException
import net.sf.jasperreports.governors.MaxPagesGovernorException
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class DynamicReportServiceSpec extends Specification implements DataTest, ServiceUnitTest<DynamicReportService> {
    @Shared
    User normalUser
    @Shared
    Preference preference

    void setup() {
        File tempDirectory = new File(grailsApplication.config.tempDirectory)
        tempDirectory.mkdirs()
        def username = "unitTest"
        preference = new Preference(locale: new Locale("en"), createdBy: username, modifiedBy: username)
        normalUser = createUser(username, "ROLE_TEMPLATE_VIEW")
        mockBean("userService", makeUserService(normalUser))
        mockBean("imageService", makeImageService())
        mockBean("grailsLinkGenerator", makeLinkGenerator())
        mockBean("customMessageService", makeCustomMessageService())
    }

    def setupSpec() {
        mockDomains Preference, Role, SharedWith, TemplateQuery, User, UserRole, ExecutedConfiguration, ExecutedTemplateQuery, Tenant, XMLTemplateNode
    }

    @Ignore //small.csv.gz file missing
    void "Create a Jasper report without max pages error"() {
        given: "A report result for small report"
        ReportResult reportResult = createReportResult("./test/data/small.csv.gz")

        when: "Report output is generated"
        File reportFile = service.createReportWithCriteriaSheetCSV(reportResult, false, [outputFormat: ReportFormatEnum.PDF.name()]) // [:] will default HTML output, use [outputFormat:ReportFormatEnum.PDF.name()]

        then: "Report is generated"
        reportFile != null
        reportFile.length() > 0
    }

    @Ignore //small.csv.gz file missing
    void "Create a Jasper report with max pages error"() {
        given: "A report result for large report"
        ReportResult reportResult = createReportResult("./test/data/large.csv.gz")

        when: "Report output is generated"
        service.createReportWithCriteriaSheetCSV(reportResult, false, [outputFormat: ReportFormatEnum.PDF.name()]) // [:] will default HTML output, use [outputFormat:ReportFormatEnum.PDF.name()]

        then: "MaxPagesGovernorException is thrown"
        DRException exception = thrown()
        exception.cause instanceof MaxPagesGovernorException
        int maxPagesCount = grailsApplication.config.pvreports.show.max.jasper.pages
        ((MaxPagesGovernorException) exception.cause).maxPages == maxPagesCount
    }


    void "test createCaseListReport For DRException"(){
        given:
        service.metaClass.executeFileNameSync{String testReportFileName,Closure testC -> throw new DRException("Exception will be thrown when CaseSeries will exceed the set limit.")}

        when:
        ExecutedCaseSeries caseSeries = new ExecutedCaseSeries(flush: true, failOnError: true, validate: false)
        caseSeries.owner = normalUser
        Map params = [:]
        normalUser.preference = new Preference(locale: new Locale("en")).save(flush: true, failOnError: true, validate: false)
        params.outputFormat = ReportFormatEnum.HTML.name()
        params.reportLocale = "en"
        service.createCaseListReport(caseSeries, params)

        then:
        thrown(DRException)
    }

    void "Validate xml transform with no_transform xslt"() {
        given:
        service.icsrXmlService = new IcsrXmlService()
        config.pv.app.e2b.xslts.options.NO_TRANSFORM.'xslt' = 'xslt/NO_TRANSFORM.xsl'
        config.tempDirectory = config.tempDirectory?:"${System.getProperty("java.io.tmpdir")}/pvreports/"
        long currentDateTimeStamp = new Date().time
        String r3ReportFileName = "output_${currentDateTimeStamp}.xml"
        String xsltName = 'NO_TRANSFORM'
        File simpleXMLFilename = new File(config.tempDirectory + "input_${currentDateTimeStamp}.xml")
        simpleXMLFilename.text = '''<?xml version="1.0" ?>
<persons>
<person username="JS1">
<name>John</name>
</person>
<person username="MI1">
<name>Morka</name>
</person>
</persons>'''
        when:
        service.generateR3XMLFromXSLT(simpleXMLFilename, r3ReportFileName, xsltName)
        then:
        new File(config.tempDirectory + r3ReportFileName).text.trim() == '''<?xml version="1.0" encoding="UTF-8"?><persons>
<person username="JS1">
<name>John</name>
</person>
<person username="MI1">
<name>Morka</name>
</person>
</persons>'''.trim()
        cleanup:
        simpleXMLFilename.delete()
        new File(config.tempDirectory + r3ReportFileName).delete()
    }

    void "Validate xml transform with EMDR xslt"() {
        given:
        service.icsrXmlService = new IcsrXmlService()
        config.pv.app.e2b.xslts.options.EMDR.'xslt' = 'xslt/E2B_R3_ICSR_EMDR.xsl'
        config.tempDirectory = config.tempDirectory ?: "${System.getProperty("java.io.tmpdir")}/pvreports/"
        long currentDateTimeStamp = new Date().time
        String r3ReportFileName = "output_${currentDateTimeStamp}.xml"
        String xsltName = 'EMDR'
        File simpleXMLFilename = new File(config.tempDirectory + "input_${currentDateTimeStamp}.xml")
        simpleXMLFilename.text = '''<?xml version="1.0" encoding="UTF-8"?><ichicsr>
          <icsreport>
             <safetyreport>
                <patient>
                    <patientinitial>patientInitial</patientinitial>
                    <patientbirthdater3>2000-05-10</patientbirthdater3>
                </patient>
            </safetyreport>
          </icsreport>
        </ichicsr>'''

        when:
        service.generateR3XMLFromXSLT(simpleXMLFilename, r3ReportFileName, xsltName)
        then:
        File outputR3File = new File(config.tempDirectory + r3ReportFileName)
        outputR3File.text.trim().replaceAll("\r\n", "\n").replaceAll(" ", "") == '''
        <?xml version="1.0" encoding="utf-8"?><PORR_IN040001UV01 xmlns="urn:hl7-org:v3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ITSVersion="XML_1.0" xsi:schemaLocation="urn:hl7-org:v3 ../../../../eMDRHL7/Impl_Files/Con170227.xsd">
        <message>
        <controlActProcess moodCode="EVN">
        <code codeSystemName="HL7 Trigger Event Id" codeSystem="HL7" code="PORR_TE040001UV01"/>
        <!--HL7 Trigger Event ID -->
        <subject>
        <investigationEvent>
        <!--C.1.1: Sender's Safety Report Unique Identifier-->
        <id root="2.16.840.1.113883.3.24" assigningAuthorityName="FDA" extension=""/>
        <statusCode/>
        <activityTime/>
        <availabilityTime/>
        <authorOrPerformer typeCode="AUT">
        <assignedEntity/>
        </authorOrPerformer>
        <trigger>
        <reaction>
        <subject>
        <investigativeSubject>
        <subjectAffectedPerson>
        <!--A1: PATIENT INITIAL-->
        <name>patientInitial</name>
        <!--Birth Time: Date of Birth-->
        <birthTime value="2000-05-10"/>
        </subjectAffectedPerson>
        </investigativeSubject>
        </subject>
        </reaction>
        </trigger>
        </investigationEvent>
        </subject>
        </controlActProcess>
        </message>
        </PORR_IN040001UV01>'''.trim().replaceAll("\r\n", "\n").replaceAll(" ", "")

        cleanup:
        simpleXMLFilename.delete()
        new File(config.tempDirectory + r3ReportFileName).delete()
    }

    void "Validate xml transform when xslt not found then runtime exception"() {
        given:
        service.icsrXmlService = new IcsrXmlService()
        config.pv.app.e2b.xslts.options.NO_TRANSFORM.'xslt' = 'xslt/NO_TRANSFORM.xsl'
        config.tempDirectory = config.tempDirectory?:"${System.getProperty("java.io.tmpdir")}/pvreports/"
        long currentDateTimeStamp = new Date().time
        String r3ReportFileName = "output_${currentDateTimeStamp}.xml"
        String xsltName = 'XYZ_TRANSFORM'
        File simpleXMLFilename = new File(config.tempDirectory + "input_${currentDateTimeStamp}.xml")
        simpleXMLFilename.text = '''<?xml version="1.0" ?>
<persons>
<person username="JS1">
<name>John</name>
</person>
<person username="MI1">
<name>Morka</name>
</person>
</persons>'''
        when:
        service.generateR3XMLFromXSLT(simpleXMLFilename, r3ReportFileName, xsltName)
        then:
        thrown(RuntimeException)
        cleanup:
        simpleXMLFilename.delete()
        new File(config.tempDirectory + r3ReportFileName).delete()
    }

    private UserService makeUserService(User user) {
        UserService userService = new UserService()
        userService.metaClass.getUser = { user }
        userService.metaClass.getCurrentUser = { user }
        return userService
    }

    private makeImageService() {
        def imageServiceMock = new MockFor(ImageService)
        imageServiceMock.demand.getImage(0..10) { String filename ->
            File file = new File("grails-app/assets/images", filename)
            println(file.getAbsolutePath())
            return new FileInputStream(file)
        }
        return imageServiceMock.proxyInstance()
    }

    private makeLinkGenerator() {
        def linkGeneratorMock = new MockFor(LinkGenerator)
        linkGeneratorMock.demand.link(2..2) { LinkedHashMap m -> "" }
        return linkGeneratorMock.proxyInstance()
    }

    private makeCustomMessageService() {
        def customMessageServiceMock = new MockFor(CustomMessageService)
        customMessageServiceMock.demand.getMessage(0..99) { String code -> code }
        customMessageServiceMock.demand.getMessage(0..99) { String code, Object args -> code }
        customMessageServiceMock.demand.getMessage(0..99) { String code, Object[] args, String defaultMessage, Locale locale -> code }
        return customMessageServiceMock.proxyInstance()
    }

    private User createUser(String username, String role) {
        def userRole = new Role(authority: role, createdBy: username, modifiedBy: username).save(flush: true)
        def normalUser = new User(username: 'user', password: 'user', fullName: "Normal User", preference: preference, createdBy: username, modifiedBy: username)
        normalUser.addToTenants(tenant)
        normalUser.save(failOnError: true)
        UserRole.create(normalUser, userRole, true)
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

    private mockBean(String name, def bean) {
        try {
            Holders.getApplicationContext().getBean(name)
            Holders.grailsApplication.mainContext.beanFactory.destroySingleton(name)
        } catch (Exception e) {
            //no bean registered
        }
        Holders.grailsApplication.mainContext.beanFactory.registerSingleton(name, bean)
    }

    private ReportResult createReportResult(String csvDataFilePath) {
        def config = new ExecutedConfiguration(
                executedDeliveryOption: new ExecutedDeliveryOption(attachmentFormats: [ReportFormatEnum.PDF]),
                sourceProfile: new SourceProfile(sourceName: "Test"),
                lastRunDate: new Date(),
                owner: normalUser,
                createdBy: normalUser.username,
                modifiedBy: normalUser.username)
        def templateQuery = new ExecutedTemplateQuery(
                executedTemplate: new ExecutedCaseLineListingTemplate(
                        templateType: TemplateTypeEnum.CASE_LINE,
                        columnList: new ReportFieldInfoList(reportFieldInfoList: [
                                new ReportFieldInfo(argusName: "test1", reportField: new ReportField(name: "CASE_NUMBER"))
                        ])
                ),
                executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(
                        dateRangeStartAbsolute: new Date(),
                        dateRangeEndAbsolute: new Date(),
                        executedAsOfVersionDate: new Date(),
                ),
                executedConfiguration: config,
                createdBy: normalUser.username,
                modifiedBy: normalUser.username)
        config.addToExecutedTemplateQueries(templateQuery)
        File inputFile = new File(csvDataFilePath)

        ReportResult reportResult = new ReportResult(
                data: new ReportResultData(value: inputFile.bytes),
                caseCount: 1000)
        reportResult.metaClass.getExecutedTemplateQuery = {
            return templateQuery
        }
        reportResult.setId(Calendar.getInstance().timeInMillis)
        templateQuery.draftReportResult = reportResult
        templateQuery.finalReportResult = reportResult
        return reportResult
    }

    void "test generateR3XMLFromXMLResultData"() {
        setup:
        String s = "Test Data"
        byte[] data = s.getBytes()
        File file = File.createTempFile("test", "_en.R3XML")

        when:
        service.generateR3XMLFromXMLResultData(data, file)

        then:
        file.text == s
        file.text != data

    }

    void "test processCustomHeadersMap"() {
        setup:
        Map e2bR2localizationMap = [:]
        XMLTemplateNode child1 = new XMLTemplateNode(elementType: XMLNodeElementType.TAG, type: XMLNodeType.SOURCE_FIELD, tagName: "first_child", orderingNumber: 0 , e2bElementName: null, children: [])
        XMLTemplateNode child2 = new XMLTemplateNode(elementType: XMLNodeElementType.TAG, type: XMLNodeType.SOURCE_FIELD, tagName: "second_child", orderingNumber: 1 , e2bElementName: "header for child2", children: [])
        XMLTemplateNode rootNode = new XMLTemplateNode(elementType: XMLNodeElementType.TAG, type: XMLNodeType.TAG_PROPERTIES, tagName: "parent_node", orderingNumber: 0 , e2bElementName: "header for rootNode", children: [child1,child2])

        when:
        service.processCustomHeadersMap(e2bR2localizationMap, rootNode, '')

        then:
        e2bR2localizationMap == ["parent_node":"header for rootNode","second_child":"header for child2"]

    }
}
