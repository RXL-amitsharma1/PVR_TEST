package com.rxlogix.publisher

import com.rxlogix.config.*
import com.rxlogix.config.publisher.*
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.apache.commons.lang.StringUtils
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([ViewHelper, ExecutedPeriodicReportConfiguration, PublisherCommonParameter])
class PublisherServiceSpec extends Specification implements DataTest, ServiceUnitTest<PublisherService> {
    def setup() {
        MiscUtil.loadFontsRegexSpecificToOs()
    }

    def setupSpec() {
        mockDomains User, ExecutedTemplateQuery, ExecutedPublisherSource, ExecutedPeriodicReportConfiguration, PublisherConfigurationSection, ExecutedGlobalDateRangeInformation, PublisherCommonParameter, PublisherTemplateParameter
    }

    void "temp chart "() {
        given:
        ByteArrayInputStream sampleDoc = new ByteArrayInputStream((new File(getClass().getResource("/publisher/bookmark.docx").toURI())).getBytes())
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(sampleDoc)
        XHTMLImporterImpl importer = new XHTMLImporterImpl(wordMLPackage);
        URL imgPath = getClass().getResource("/publisher/img.png")
        when:
        importer.convert("<html><body><img src=\"${imgPath.toURI()}\"/></body></html>", "file://");
        then:
        1==1
    }

    void "test getBookmarkAsWordObjects"() {
        given:
        ByteArrayInputStream sampleDoc = new ByteArrayInputStream((new File(getClass().getResource("/publisher/bookmark.docx").toURI())).getBytes())
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(sampleDoc)
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart()
        def word = WordTemplateExecutor.parseDocx(documentPart, true).rootNodes
        when:
        List<Object> result1 = WordTemplateExecutor.getBookmarkAsWordObjects(word, "bookmark1", "bookmark2", true, true)
        List<Object> result2 = WordTemplateExecutor.getBookmarkAsWordObjects(word, "bookmark1", "bookmark1", true, true)
        List<Object> result3 = WordTemplateExecutor.getBookmarkAsWordObjects(word, "bookmark2", "bookmark2", true, true)
        List<Object> result4 = WordTemplateExecutor.getBookmarkAsWordObjects(word, "bookmark1", "bookmark2", false, false)
        List<Object> result5 = WordTemplateExecutor.getBookmarkAsWordObjects(word, "bookmark1", "bookmark2", true, false)
        List<Object> result6 = WordTemplateExecutor.getBookmarkAsWordObjects(word, "bookmark1", "bookmark2", false, true)
        List<Object> result7 = WordTemplateExecutor.getBookmarkAsWordObjects(word, "bk1", "bk2", true, true)
        List<Object> result8 = WordTemplateExecutor.getBookmarkAsWordObjects(word, "bk1", "bk2", false, false)
        List<Object> result9 = WordTemplateExecutor.getBookmarkAsWordObjects(word, "bk1", "bk2", true, false)
        List<Object> result10 = WordTemplateExecutor.getBookmarkAsWordObjects(word, "bk1", "bk2", false, true)
        List<Object> result11 = WordTemplateExecutor.getBookmarkAsWordObjects(word, "bookmark1", "fdef", true, true)
        then:
        result1.size() == 6
        result2.size() == 2
        result3.size() == 2
        result4.size() == 2
        result5.size() == 4
        result6.size() == 4
        result7.size() == 1
        result8.size() == 0
        result9.size() == 0
        result10.size() == 0
        result11.size() == 0
    }

    void "test preparing parameters values"() {
        when:
        service.dynamicReportService = new Object() {
            File createMultiTemplateReport(ExecutedReportConfiguration executedConfigurationInstance, Map params) {
                URL resource
                if ((params.sectionsToExport[0] == 1) && (params.outputFormat == ReportFormatEnum.HTML.name())) resource = getClass().getResource("/publisher/cll1.html")
                if ((params.sectionsToExport[0] == 1) && (params.outputFormat == ReportFormatEnum.XLSX.name())) resource = getClass().getResource("/publisher/cll1.xlsx")
                if ((params.sectionsToExport[0] == 2) && (params.outputFormat == ReportFormatEnum.HTML.name())) resource = getClass().getResource("/publisher/dt.html")
                if ((params.sectionsToExport[0] == 2) && (params.outputFormat == ReportFormatEnum.PDF.name())) resource = getClass().getResource("/publisher/dt.pdf")
                return new File(resource.toURI())
            }

            File createReportWithCriteriaSheetCSV(ReportResult reportResult, boolean isInDraftMode, Map params) {
                URL resource
                resource = getClass().getResource("/publisher/cll1.xlsx")
                return new File(resource.toURI())
            }

            String getReportsDirectory() {
                return grailsApplication.config.tempDirectory as String
            }
        }
        service.publisherSourceService = new Object() {
            byte[] getData(BasicPublisherSource source) {
                URL resource
                if (source.name == "excel") resource = getClass().getResource("/publisher/excel.xlsx")
                if (source.name == "pdf") resource = getClass().getResource("/publisher/dt.pdf")
                if (source.name == "word") resource = getClass().getResource("/publisher/Simple.docx")
                if (source.name == "img") resource = getClass().getResource("/publisher/img.png")
                if (source.name == "json") resource = getClass().getResource("/publisher/json.json")
                if (source.name == "xml") resource = getClass().getResource("/publisher/xml.xml")
                return (new File(resource.toURI())).getBytes()
            }
        }

        ViewHelper.metaClass.static.getDictionaryValues = { String jsonString, DictionaryTypeEnum dictionaryType -> return jsonString }
        PublisherCommonParameter.metaClass.static.findAllByIsDeleted = { a ->
            return [
                    new PublisherCommonParameter(name: "common1", value: "commontext"),
                    new PublisherCommonParameter(name: "common2", value: "\$report.source[excel].content"),
                    new PublisherCommonParameter(name: "common3", value: "\$eval \$report.configuration.reportName")
            ]
        }
        Date testDate = new Date()
        Date testDate2 = new Date().minus(1)
        ExecutedPeriodicReportConfiguration config = new ExecutedPeriodicReportConfiguration()
        config.reportName = "name"
        config.productSelection = "productSelection"
        config.studySelection = "studySelection"
        config.primaryReportingDestination = "primaryReportingDestination"
        config.reportingDestinations = ["destination1", "destination2"]
        config.numOfExecutions = 5
        config.lastRunDate = testDate
        config.executedGlobalDateRangeInformation = new ExecutedGlobalDateRangeInformation(dateRangeStartAbsolute: testDate, dateRangeEndAbsolute: testDate2)
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery()
        executedTemplateQuery.metaClass.getId = { -> 1 }
        executedTemplateQuery.metaClass.isVisible = { -> true }
        executedTemplateQuery.metaClass.getUsedTemplate = { -> new ExecutedCaseLineListingTemplate() }
        executedTemplateQuery.metaClass.getReportResult = { -> new ReportResult(sequenceNo: 1) }
        config.executedTemplateQueries.add(executedTemplateQuery)

        ExecutedTemplateQuery executedTemplateQuery2 = new ExecutedTemplateQuery()
        executedTemplateQuery2.metaClass.getId = { -> 2 }
        executedTemplateQuery2.metaClass.isVisible = { -> true }
        executedTemplateQuery2.metaClass.getUsedTemplate = { ->
            new ExecutedDataTabulationTemplate(showChartSheet: true)
        }
        executedTemplateQuery2.metaClass.getReportResult = { -> new ReportResult(sequenceNo: 2) }
        config.executedTemplateQueries.add(executedTemplateQuery2)

        ExecutedPublisherSource source1 = new ExecutedPublisherSource(name: "excel", fileType: BasicPublisherSource.FileType.EXCEL)
        source1.metaClass.isVisible = { -> true }

        ExecutedPublisherSource source2 = new ExecutedPublisherSource(name: "word", fileType: BasicPublisherSource.FileType.WORD)
        source2.metaClass.isVisible = { -> true }
        ExecutedPublisherSource source3 = new ExecutedPublisherSource(name: "pdf", fileType: BasicPublisherSource.FileType.PDF)
        source3.metaClass.isVisible = { -> true }
        ExecutedPublisherSource source4 = new ExecutedPublisherSource(name: "img", fileType: BasicPublisherSource.FileType.IMAGE)
        source4.metaClass.isVisible = { -> true }
        ExecutedPublisherSource source5 = new ExecutedPublisherSource(name: "json", fileType: BasicPublisherSource.FileType.JSON)
        source5.metaClass.isVisible = { -> true }
        ExecutedPublisherSource source6 = new ExecutedPublisherSource(name: "xml", fileType: BasicPublisherSource.FileType.XML)
        source6.metaClass.isVisible = { -> true }
        config.attachments = [source1, source2, source3, source4, source5, source6]
        PublisherConfigurationSection section = new PublisherConfigurationSection(executedConfiguration: config)
        section.approver = new User(fullName: "approver")
        section.author = new User(fullName: "author")
        section.reviewer = new User(fullName: "reviewer")
        config.publisherReports = [new PublisherReport(published: true, data: (new File(getClass().getResource("/publisher/text.docx").toURI())).getBytes())] as Set
        ExecutedPeriodicReportConfiguration.metaClass.static.findAllByReportNameAndIsDeletedAndIsPublisherReportAndNumOfExecutionsLessThan = { o1, o2, o3, o4 ->
            return [config]
        }

        section.metaClass.getParameterValues { return  [
                simpleString               : "simple String",
                section                    : "\$report.section1.table",
                data                       : "\$report.section1.data",
                sectionCell                : "\$report.section1.cell[3;5]",
                sectionCellLast            : "\$report.section1.cell[last;last]",
                sectionCellLast1           : "\$report.section1.cell[last-1;last-1]",
                sectionChart               : "\$report.section2.chart",
                sectionRange               : "\$report.section1.range[3;3][5;5]",
                sourceContentExcel         : "\$report.source[excel].content",
                sourceContentPdf           : "\$report.source[pdf].content",
                sourceContentWord          : "\$report.source[word].content",
                sourceContentImg           : "\$report.source[img].content",
                sourceExcelData            : "\$report.source[excel].data",
                sourceJsonData             : "\$report.source[json].data.root.id",
                sourceXmlData              : "\$report.source[xml].data.root.sub.text()",
                sourceCell                 : "\$report.source[excel].cell[3;5]",
                sourceRange                : "\$report.source[excel].range[3;3][5;5]",
                sourcePdfAsImg             : "\$report.source[pdf].img",
                sourcePdfAsImgRange        : "\$report.source[pdf].img[1-2]",
                previousSection            : "\$previous.section1.table",
                previousData               : "\$previous.section1.data",
                previousSectionCell        : "\$previous.section1.cell[3;5]",
                previousSectionChart       : "\$previous.section2.chart",
                previousSectionRange       : "\$previous.section1.range[3;3][5;5]",
                previousSourceContentExcel : "\$previous.source[excel].content",
                previousSourceContentPdf   : "\$previous.source[pdf].content",
                previousSourceContentWord  : "\$previous.source[word].content",
                previousSourceContentImg   : "\$previous.source[img].content",
                previousSourceExcelData    : "\$previous.source[excel].data",
                previousSourceJsonData     : "\$previous.source[json].data",
                previousSourceXmlData      : "\$previous.source[xml].data",
                previousSourceCell         : "\$previous.source[excel].cell[3;5]",
                previousSourceRange        : "\$previous.source[excel].range[3;3][5;5]",
                previousSourcePdfAsImg     : "\$previous.source[pdf].img",
                previousSourcePdfAsImgRange: "\$previous.source[pdf].img[1-2]",
                previousDocText            : "\$previous.doc.text[\"rt\";\"en\"]",
                previousDocText2           : "\$previous.doc.text(\"rt\";\"en\")",
                previousDocPar             : "\$previous.doc.paragraph[\"Par1\";\"Par2\"]",
                previousDocPar2            : "\$previous.doc.paragraph(\"Par1\";\"en\")",
                eval                       : "\$eval \$report.source[excel].data[3][3] + \$report.section1.data[3][4].toUpperCase()",
                sheetCell                  : "\$report.source[excel].cell[1][1;1]",
                sheetRange                 : "\$report.source[excel].range[1][1;1][2;2]",
                sheetSectionCell         : "\$report.section1.cell[0][1;1]",
                sheetSectionRange          : "\$report.section1.range[0][1;1][2;2]",
                //todo:implement test for $previous.doc.section[\"begin str\";\"end str\")
        ]}

        section.publisherTemplate = new PublisherTemplate()
        section.publisherTemplate.parameters = [new PublisherTemplateParameter(name: "code", value: "x.reportName.toUpperCase()", type: PublisherTemplateParameter.Type.CODE)]


        PublisherExecutionLog log = new PublisherExecutionLog()
        List<WordTemplateExecutor.Parameter> result = service.toParamsMap(section, log)
        int i = 0
        then:
        //standard parameters from report
        checkParam(result[i++], "approver", "approver", WordTemplateExecutor.ParameterType.STRING)
        checkParam(result[i++], "reviewer", "reviewer", WordTemplateExecutor.ParameterType.STRING)
        checkParam(result[i++], "author", "author", WordTemplateExecutor.ParameterType.STRING)

        //param from template with type CODE x.reportName.toUpperCase()
        checkParam(result[i++], "code", "NAME", WordTemplateExecutor.ParameterType.STRING)

        //common param commontext
        checkParam(result[i++], "common1", "commontext", WordTemplateExecutor.ParameterType.STRING)

        //params from report
        checkParam(result[i++], "simpleString", "simple String", WordTemplateExecutor.ParameterType.STRING)

        //common param
        result[i].name == "common2"
        (result[i].value.indexOf("Dose") > -1) && (result[i].value.indexOf("Formulation") > -1) && (result[i].value.indexOf("Test Product AJ") > -1) && (result[i].value.indexOf("Standard Pharmaceutical Co AJ") > -1)
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //common param
        result[i].name == "common3"
        result[i].value == "name"
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++

        //$report.section1.table
        result[i].name == "section"
        result[i].value.indexOf("html") > -1
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++

        //$report.section1.data
        result[i].name == "data"
        result[i].value.size() > 0
        result[i].value[3][5] == "Dose"
        result[i].type == WordTemplateExecutor.ParameterType.DATA
        i++
        //$report.section1.cell[3;5]
        result[i].name == "sectionCell"
        result[i].value == "Dose"
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++
        //$report.section1.cell[last;last]
        result[i].name == "sectionCellLast"
        result[i].value == "Capsule"
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++
        //$report.section1.cell[last-1;last-1]
        result[i].name == "sectionCellLast1"
        result[i].value == "100 millimole"
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++
        //$report.section2.chart
        result[i].name == "sectionChart"
        result[i].value.startsWith("<html><body><img src=")
        new File((new XmlSlurper().parseText(result[i].value)).body.img.@src.text().substring(7)).exists()
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$report.section1.range[3;3][5;5]
        result[i].name == "sectionRange"
        result[i].value == "<html><head><style>table, th, td { border: 1px solid black;border-collapse: collapse;}</style></head><body><table><tr><td>International Birth Date</td><td>MAH Address</td><td>Dose</td></tr><tr><td>01-AUG-99</td><td>9060 Irvine, Ctr Dr</td><td>100 millimole</td></tr><tr><td>01-AUG-99</td><td>9060 Irvine, Ctr Dr</td><td>100 millimole</td></tr></table></body></html>"
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$report.source[excel].content
        result[i].name == "sourceContentExcel"
        (result[i].value.indexOf("Dose") > -1) && (result[i].value.indexOf("Formulation") > -1) && (result[i].value.indexOf("Test Product AJ") > -1) && (result[i].value.indexOf("Standard Pharmaceutical Co AJ") > -1)
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$report.source[pdf].content
        result[i].name == "sourceContentPdf"
        result[i].value.indexOf("91UA00010850") > -1
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$report.source[word].content
        result[i].name == "sourceContentWord"
        result[i].value.indexOf("start") > -1
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$report.source[img].content
        result[i].name == "sourceContentImg"
        result[i].value.indexOf("<html><body><img src='file://") > -1
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$report.source[excel].data
        result[i].name == "sourceExcelData"
        result[i].value.size() > 0
        result[i].value[3][5] == "Dose"
        result[i].type == WordTemplateExecutor.ParameterType.DATA
        i++
        //$report.source[json].data
        result[i].name == "sourceJsonData"
        result[i].value == "1"
        result[i].type == WordTemplateExecutor.ParameterType.DATA
        i++
        //$report.source[xml].data
        result[i].name == "sourceXmlData"
        result[i].value == "test"
        result[i].type == WordTemplateExecutor.ParameterType.DATA
        i++
        //$report.source[excel].cell[3;5]
        result[i].name == "sourceCell"
        result[i].value == "Dose"
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++
        //$report.source[excel].range[3;3][5;5]
        result[i].name == "sourceRange"
        result[i].value == "<html><head><style>table, th, td { border: 1px solid black;border-collapse: collapse;}</style></head><body><table><tr><td>International Birth Date</td><td>MAH Address</td><td>Dose</td></tr><tr><td>01-AUG-99</td><td>9060 Irvine, Ctr Dr</td><td>100 millimole</td></tr><tr><td>01-AUG-99</td><td>9060 Irvine, Ctr Dr</td><td>100 millimole</td></tr></table></body></html>"
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$report.source[pdf].img
        result[i].name == "sourcePdfAsImg"
        StringUtils.countMatches(result[i].value, "img") == 20
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$report.source[pdf].img[1-2]
        result[i].name == "sourcePdfAsImgRange"
        StringUtils.countMatches(result[i].value, "<img") == 2
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$eval $report.source[excel].data[3][3] + $report.section1.data[3][4]
        result[i].name == "eval"
        result[i].value == "International Birth DateMAH ADDRESS"
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++
        //report.source[excel].cell[1][1;1]
        result[i].name == "sheetCell"
        result[i].value == 1
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++
        //report.source[excel].range[1][1;1][2;2]
        result[i].name == "sheetRange"
        result[i].value == "<html><head><style>table, th, td { border: 1px solid black;border-collapse: collapse;}</style></head><body><table><tr><td>1</td><td>2</td></tr><tr><td>4</td><td>5</td></tr></table></body></html>"
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //report.section1.cell[0][1;1]
        result[i].name == "sheetSectionCell"
        result[i].value == "r 1"
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++
        //report.section1.range[0][1;1][2;2]
        result[i].name == "sheetSectionRange"
        result[i].value == "<html><head><style>table, th, td { border: 1px solid black;border-collapse: collapse;}</style></head><body><table><tr><td>r 1</td><td>r 2</td></tr><tr><td>r 2</td><td>r 3</td></tr></table></body></html>"
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$previous.doc.text["begin str";"end str"]
        result[i].name == "previousDocText"
        result[i].value == "rt\nPar1:text\nPar2:text2\nen"
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++
        //$previous.doc.text("begin str";"end str")
        result[i].name == "previousDocText2"
        result[i].value == "\nPar1:text\nPar2:text2\n"
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++
        //$previous.doc.paragraph["Par1";"Par2"]
        result[i].name == "previousDocPar"
        result[i].value.size() == 2 && result[i].value[1].toString() == "Par2:text2"
        result[i].type == WordTemplateExecutor.ParameterType.WORD
        i++
        //$previous.doc.paragraph("Par1";"en")
        result[i].name == "previousDocPar2"
        result[i].value.size() == 1 && result[i].value[0].toString() == "Par2:text2"
        result[i].type == WordTemplateExecutor.ParameterType.WORD
        i++
        //$previous.section1.table
        result[i].name == "previousSection"
        result[i].value.indexOf("html") > -1
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$previous.section1.data
        result[i].name == "previousData"
        result[i].value.size() > 0
        result[i].value[3][5] == "Dose"
        result[i].type == WordTemplateExecutor.ParameterType.DATA
        i++
        //$previous.section1.cell[3;5]
        result[i].name == "previousSectionCell"
        result[i].value == "Dose"
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++
        //$previous.section2.chart
        result[i].name == "previousSectionChart"
        result[i].value.startsWith("<html><body><img src=")
        new File((new XmlSlurper().parseText(result[i].value)).body.img.@src.text().substring(7)).exists()
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$previous.section1.range[3;3][5;5]
        result[i].name == "previousSectionRange"
        result[i].value == "<html><head><style>table, th, td { border: 1px solid black;border-collapse: collapse;}</style></head><body><table><tr><td>International Birth Date</td><td>MAH Address</td><td>Dose</td></tr><tr><td>01-AUG-99</td><td>9060 Irvine, Ctr Dr</td><td>100 millimole</td></tr><tr><td>01-AUG-99</td><td>9060 Irvine, Ctr Dr</td><td>100 millimole</td></tr></table></body></html>"
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$previous.source[excel].content
        result[i].name == "previousSourceContentExcel"
        (result[i].value.indexOf("Dose") > -1) && (result[i].value.indexOf("Formulation") > -1) && (result[i].value.indexOf("Test Product AJ") > -1) && (result[i].value.indexOf("Standard Pharmaceutical Co AJ") > -1)
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$previous.source[excel].content
        result[i].name == "previousSourceContentPdf"
        result[i].value.indexOf("91UA00010850") > -1
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$previous.source[word].content
        result[i].name == "previousSourceContentWord"
        result[i].value.indexOf("start") > -1
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$previous.source[img].content
        result[i].name == "previousSourceContentImg"
        result[i].value.indexOf("<html><body><img src='file://") > -1
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$previous.source[excel].data
        result[i].name == "previousSourceExcelData"
        result[i].value.size() > 0
        result[i].value[3][5] == "Dose"
        result[i].type == WordTemplateExecutor.ParameterType.DATA
        i++
        //$previous.source[json].data
        result[i].name == "previousSourceJsonData"
        result[i].value.size() > 0
        result[i].value.root.id == "1"
        result[i].type == WordTemplateExecutor.ParameterType.DATA
        i++
        //$previous.source[xml].data
        result[i].name == "previousSourceXmlData"
        result[i].value.size() > 0
        result[i].value.root.sub.text() == "test"
        result[i].type == WordTemplateExecutor.ParameterType.DATA
        i++
        //$previous.source[excel].cell[3;5]
        result[i].name == "previousSourceCell"
        result[i].value == "Dose"
        result[i].type == WordTemplateExecutor.ParameterType.STRING
        i++
        //$previous.source[excel].range[3;3][5;5]
        result[i].name == "previousSourceRange"
        result[i].value == "<html><head><style>table, th, td { border: 1px solid black;border-collapse: collapse;}</style></head><body><table><tr><td>International Birth Date</td><td>MAH Address</td><td>Dose</td></tr><tr><td>01-AUG-99</td><td>9060 Irvine, Ctr Dr</td><td>100 millimole</td></tr><tr><td>01-AUG-99</td><td>9060 Irvine, Ctr Dr</td><td>100 millimole</td></tr></table></body></html>"
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$previous.source[pdf].img
        result[i].name == "previousSourcePdfAsImg"
        StringUtils.countMatches(result[i].value, "img") == 20
        result[i].type == WordTemplateExecutor.ParameterType.HTML
        i++
        //$previous.source[pdf].img[1-2]
        result[i].name == "previousSourcePdfAsImgRange"
        StringUtils.countMatches(result[i].value, "<img") == 2
        result[i].type == WordTemplateExecutor.ParameterType.HTML
    }

    void "test WordTemplateExecutor"() {
        when:
        WordTemplateExecutor executor = new WordTemplateExecutor(new PublisherExecutionLog())
        ByteArrayInputStream template = new ByteArrayInputStream((new File(getClass().getResource("/publisher/template.docx").toURI())).getBytes())
        ByteArrayInputStream sampleDoc = new ByteArrayInputStream((new File(getClass().getResource("/publisher/text.docx").toURI())).getBytes())
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(sampleDoc)
        MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart()
        def word = WordTemplateExecutor.parseDocx(documentPart, false).rootNodes


        List<WordTemplateExecutor.Parameter> paramsList = [
                WordTemplateExecutor.stringParam("headerVariable", "simpleString"),
                WordTemplateExecutor.stringParam("simpleVariableInText", "simpleStringInText"),
                WordTemplateExecutor.htmlParam("sectionVariable1", "<html><head><style>table, th, td { border: 1px solid black;border-collapse: collapse;}</style></head><body><table><tr><td>International Birth Date</td><td>MAH Address</td><td>Dose</td></tr><tr><td>01-AUG-99</td><td>9060 Irvine, Ctr Dr</td><td>100 millimole</td></tr><tr><td>01-AUG-99</td><td>9060 Irvine, Ctr Dr</td><td>100 millimole</td></tr></table></body></html>"),
                WordTemplateExecutor.wordParam("sectionVariable2", word),
                WordTemplateExecutor.dataParam("conditionVariable1", [1, 3, 5]),
                WordTemplateExecutor.stringParam("conditionVariable2", "true"),
                WordTemplateExecutor.stringParam("conditionVariable3", "true"),
                WordTemplateExecutor.stringParam("conditionVariable4", "false"),
                WordTemplateExecutor.stringParam("conditionVariable7", "true"),
                WordTemplateExecutor.stringParam("conditionVariable8", "false"),
                WordTemplateExecutor.dataParam("loopData", [["1", "2"], ["3", "4"]]),
                WordTemplateExecutor.dataParam("changesData", [["1", "2"], ["3", "4"]]),
        ]


        byte[] out = executor.generateFromTemplate(template, paramsList)


        wordMLPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(out))
        documentPart = wordMLPackage.getMainDocumentPart()
        String text = WordTemplateExecutor.parseDocx(documentPart, false).textNodes.collect { it.getValue() }.join("")
        then:
        text.indexOf("Header simpleString header") > -1
        text.indexOf("simpleStringInText Start simpleStringInText end simpleStringInText") > -1
        text.indexOf("International Birth Date") > -1
        text.indexOf("Par1:text") > -1
        text.indexOf("condition1") > -1
        text.indexOf("condition2") > -1
        text.indexOf("condition3") > -1
        text.indexOf("condition4") == -1
        text.indexOf("condition7") > -1
        text.indexOf("condition8") == -1
        text.indexOf("condition5") > -1
        text.indexOf("condition6") == -1
        text.indexOf("item1") > -1
        text.indexOf("item2") > -1
        text.indexOf("item3") > -1
        text.indexOf("item4") > -1
        text.indexOf("itemcond1") > -1
        text.indexOf("itemcond2") == -1
        text.indexOf("itemcond3") > -1
        text.indexOf("itemcond4") == -1
        text.indexOf("joinresult2,4") > -1
        text.indexOf("#VAR BOOLEAN wasUpdated = ctx.changesData.size()>0") == -1
    }

    boolean checkParam(WordTemplateExecutor.Parameter result, String name, Object value, type) {
        return (result.name == name) &&
                (result.value == value) &&
                (result.type == type)
    }

    void "test fetch Parameter"() {
        given:
        ByteArrayInputStream templateDoc = new ByteArrayInputStream((new File(getClass().getResource("/publisher/Sample_DSUR_Body_Text.docx").toURI())).getBytes())
        PublisherCommonParameter.metaClass.static.findAllByIsDeleted = {boolean b -> []}
        List validParameters = ['12345678', 'BOLD_UPPER_CASE_PARAMETER', 'CompanyName', 'conditionVariable1', 'conditionVariable2',
                                'conditionVariable3', 'conditionVariable4', 'conditionVariable7', 'conditionVariable8', 'CurrentDate',
                                'dateRangeEndAbsolute', 'dateRangeStartAbsolute', 'DD', 'EventCount', 'Indication', 'loopData', 'MMM', 'Number',
                                'PLAIN_PARAMETER', 'Products', 'StudyNum',
                                'TheInvestigatorsBrochureIBprovidesasummaryofclinicalandnonclinicaldatafortheproductrelevanttoitsstudyinhumansubjectsSection7oftheIBSummaryofDataandGuidancefortheInvestigatorprovidesinvestigatorswithaclearunderstandingofthepossiblerisksadversereactionssp',
                                'UPPER_CASE_PARAMETER', 'YYYY']
        List invalidParameters = ['!Inappropriateparameter', '@Inappropriate_parameter', 'Inappropriate parameter', 'Inappropriate${parameter',
                                  'Inappropriate-parameter', 'Inappropriate/number_parameter', 'Inappropriate_parameter',
                                  'TheInvestigatorsBrochureIBprovidesasummaryofclinicalandnonclinicaldatafortheproductrelevanttoitsstudyinhumansubjectsSection7oftheIBSummaryofDataandGuidancefortheInvestigatorprovidesinvestigatorswithaclearunderstandingofthepossiblerisksaDversereactionsspecifictestsobservationsandprecautionsrelevanttoandactsasthereferencesafetyinformationforthepurposesofthisreport']

        when:
        def result = service.fetchParameters(templateDoc)

        then:
        result.validParam.collect {it.name} == validParameters
        result.invalidParam.collect {it.name} == invalidParameters
    }
}
