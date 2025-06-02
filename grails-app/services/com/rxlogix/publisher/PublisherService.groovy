package com.rxlogix.publisher

import com.plutext.merge.BlockRange
import com.plutext.merge.DocumentBuilder
import com.rxlogix.config.BasicPublisherSource
import com.rxlogix.config.ExecutedPeriodicReportConfiguration
import com.rxlogix.config.ExecutedPublisherSource
import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ReportResult
import com.rxlogix.config.publisher.PublisherCommonParameter
import com.rxlogix.config.publisher.PublisherConfigurationSection
import com.rxlogix.config.publisher.PublisherExecutedTemplate
import com.rxlogix.config.publisher.PublisherReport
import com.rxlogix.config.publisher.PublisherTemplateParameter
import com.rxlogix.enums.*
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.core.GrailsApplication
import groovy.util.slurpersupport.GPathResult
import net.sf.dynamicreports.report.constant.PageOrientation
import org.apache.commons.io.FileUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.docx4j.Docx4J
import org.docx4j.Docx4jProperties
import org.docx4j.TextUtils
import org.docx4j.convert.out.HTMLSettings
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
import org.docx4j.wml.P
import org.grails.web.json.JSONElement

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.util.concurrent.ThreadLocalRandom

class PublisherService {

    def dynamicReportService
    def grailsApplication
    def publisherSourceService
    def CRUDService
    def userService
    def oneDriveRestService

    static final BUILD_IN_PARAMS = [ "author", "reviewer", "approver"]

    static final Map reportParams = [
            pageOrientation     : PageOrientation.LANDSCAPE,
            paperSize           : PageSizeEnum.LETTER,
            sensitivityLabel    : SensitivityLabelEnum.NONE,
            showPageNumbering   : false,
            excludeCriteriaSheet: true,
            excludeAppendix     : true,
            excludeComments     : true,
            excludeLegend       : true,
            showCompanyLogo     : false,
            advancedOptions     : "1"
    ]
    static final Map htmlReportParams = [
            sensitivityLabel    : SensitivityLabelEnum.NONE,
            showPageNumbering   : false,
            excludeCriteriaSheet: true,
            excludeAppendix     : true,
            excludeComments     : true,
            excludeLegend       : true,
            showCompanyLogo     : false,
            advancedOptions     : "1"
    ]

    List<WordTemplateExecutor.Parameter> toParamsMap(PublisherConfigurationSection section, PublisherExecutionLog log) throws PublisherPermissionException {
        log.append("Starting preparing parameters")
        ExecutedReportConfiguration config = section.executedConfiguration
        Set<PublisherTemplateParameter> publisherTemplateParameters = section.publisherTemplate?.parameters?.findAll { it.type == PublisherTemplateParameter.Type.CODE }
        List<WordTemplateExecutor.Parameter> out = []

        if (config instanceof ExecutedPeriodicReportConfiguration) {
            ExecutedPeriodicReportConfiguration c = config
            out.add(WordTemplateExecutor.stringParam("approver", section.approver?.fullName))
            out.add(WordTemplateExecutor.stringParam("reviewer", section.reviewer?.fullName))
            out.add(WordTemplateExecutor.stringParam("author", section.author?.fullName))
            log.append("Preparing Template Executable (Code) Parameters...")

            publisherTemplateParameters?.each {
                if (it.value) {
                    try {
                        out.add(WordTemplateExecutor.stringParam(it.name, Eval.x(config, it.value)?.toString()))
                    } catch (Exception e) {
                        log.logError("Error occured executing code for " + it.value, e)
                    }
                }
            }
            Map<String, String> currentReportValues = [:]
            Map<String, String> previousReportValues = [:]
             PublisherCommonParameter.findAllByIsDeleted(false)?.each{PublisherCommonParameter parameter->
                 if(parameter.value?.indexOf("\$")>-1){
                     currentReportValues.put(parameter.name, parameter.value)
                 }else{
                     out.add(WordTemplateExecutor.stringParam(parameter.name, parameter.value ?: ""))
                 }
             }
            section.parameterValues?.each { k, v ->
                if (v.indexOf("\$report.") > -1) {
                    currentReportValues.put(k, fixSectionNumberFormat(v))
                } else if (v.indexOf("\$previous.") > -1) {
                    previousReportValues.put(k, fixSectionNumberFormat(v))
                } else if(v?.trim())
                    out.add(WordTemplateExecutor.stringParam(k.toString(), (v ?: "").toString()))
            }
            if (currentReportValues?.size() > 0)
                out.addAll(getParameterValues(c, currentReportValues, "report", log))
            if (previousReportValues?.size() > 0)
                out.addAll(getPreviousReportValues(c, previousReportValues, log))
        }
        out
    }

    String fixSectionNumberFormat(String paramName) {
        int start = paramName.indexOf("section[")
        if (start > -1) {
            String result
            int end = paramName.indexOf("]", start);
            result = paramName.substring(0, end) + paramName.substring(end + 1);
            result = result.replace("section[", "section");
            return result;
        } else return paramName
    }

    List<WordTemplateExecutor.Parameter> getPreviousReportValues(ExecutedPeriodicReportConfiguration config, Map<String, String> cellValues, PublisherExecutionLog log) {
        log.append("Preparing data for variables from previous report...")
        List<WordTemplateExecutor.Parameter> result = []
        List<ExecutedPeriodicReportConfiguration> reports = ExecutedPeriodicReportConfiguration.
                findAllByReportNameAndIsDeletedAndIsPublisherReportAndNumOfExecutionsLessThan(config.reportName, false, true, config.numOfExecutions)
        ExecutedPeriodicReportConfiguration latestPublishedExecutedReport = reports.findAll { r -> r.publisherReports.find { p -> p.published } }?.max { it.numOfExecutions }
        if (!latestPublishedExecutedReport) return []
        PublisherReport latestPublishedDoc = latestPublishedExecutedReport?.publisherReports?.find { p -> p.published }
        if (latestPublishedDoc && cellValues.keySet().find { cellValues.get(it).startsWith("\$previous.doc") }) {
            WordprocessingMLPackage targetWordMLPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(latestPublishedDoc.data));
            MainDocumentPart mainDocumentPart = targetWordMLPackage.getMainDocumentPart();
            Map<String, Object> parseResult = WordTemplateExecutor.parseDocx(mainDocumentPart, false);
            String text = (String) parseResult.get("text");
            List<Object> textNodes = (List<Object>) parseResult.get("textNodes");
            List<Object> rootNodes = (List<Object>) parseResult.get("rootNodes");
            cellValues.each { String k, String v ->
                try {
                    Map<String, Object> cfg = fetchParamConfig(v);
                    if (cfg.type == "text") {
                        result.add(WordTemplateExecutor.stringParam(k, substring(text, cfg.begin, cfg.end, cfg.includeLeft, cfg.includeRight)))
                    } else if (cfg.type == "title") {
                        result.add(WordTemplateExecutor.wordParam(k, WordTemplateExecutor.getSectionAsWordObjects(textNodes, rootNodes, cfg.begin, cfg.end)))
                    } else if (cfg.type == "paragraph") {
                        result.add(WordTemplateExecutor.wordParam(k, WordTemplateExecutor.getParagraphsAsWordObjects(rootNodes, cfg.begin, cfg.end, cfg.includeLeft, cfg.includeRight)))
                    } else if (cfg.type == "bookmark") {
                        result.add(WordTemplateExecutor.wordParam(k, WordTemplateExecutor.getBookmarkAsWordObjects(rootNodes, cfg.begin, cfg.end, cfg.includeLeft, cfg.includeRight)))
                    }
                } catch (Exception e) {
                    log.logError("Error fetching value from report ", e)
                }
            }
        }
        Map<String, String> previousSections = cellValues.findAll { k, v -> (v.startsWith("\$previous.section") || v.startsWith("\$previous.source")) }
        if (previousSections) {
            result.addAll(getParameterValues(latestPublishedExecutedReport, previousSections, "previous", log))
        }
        return result
    }

    private static String substring(String text, String begin, String end, Boolean includeLeft, Boolean includeRight) {
        int beginInd = text.indexOf(begin) + (includeLeft ? 0 : begin.length())
        int endInd = text.indexOf(end, beginInd) + (includeRight ? end.length() : 0)
        if (beginInd > -1 && endInd > -1 && beginInd < endInd) return text.substring(beginInd, endInd)
        return null
    }

    private static Map<String, Object> fetchParamConfig(String text) {

        if (text.matches(".*\\.text[\\[(]\".*\";\".*\"[\\])]"))
            return setParamConfig(text, "text");
        if (text.matches(".*\\.title[\\[(]\".*\";\".*\"[\\])]"))
            return setParamConfig(text, "title");
        if (text.matches(".*\\.paragraph[\\[(]\".*\";\".*\"[\\])]"))
            return setParamConfig(text, "paragraph")
        if (text.matches(".*\\.bookmark[\\[(]\".*\";\".*\"[\\])]"))
            return setParamConfig(text, "bookmark");

        return new HashMap<String, Object>();

    }

    private static Map<String, Object> setParamConfig(String text, String type) {
        int index = text.lastIndexOf("."+type)+type.length()+1
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("type", type);
        result.put("includeLeft", text.charAt(index) == '[');
        result.put("includeRight", text.charAt(text.length() - 1) == ']');
        String[] textBeginEnd = text.substring(index + 2, text.length() - 2).split("\";\"");
        result.put("begin", textBeginEnd[0]);
        result.put("end", textBeginEnd[1]);
        return result;
    }

    List<WordTemplateExecutor.Parameter> getParameterValues(ExecutedPeriodicReportConfiguration config, Map<String, String> parameterValues, String prefix, PublisherExecutionLog log) {
        int pl = prefix.length()
        List<WordTemplateExecutor.Parameter> result = []
        Map<String, List<Object[]>> sectionExcelData = [:]
        Map<String, JSONElement> sectionJsonData = [:]
        Map<String, GPathResult> sectionXmlData = [:]
        Map<String, String> sectionHtmlData = [:]
        Map<String, String> wordData = [:]
        Map<String, PDDocument> pdflData = [:]
        Set<Integer> requiredParameterValuesSections = []
        Set<Integer> requiredFullDataSections = []
        Set<String> requiredFullDataFiles = []
        Set<String> requiredCellValueFiles = []
        parameterValues.each { String k, String v ->
            try {
                if ((v.indexOf(".cell") > -1) || (v.indexOf(".data") > -1) || (v.indexOf(".range") > -1)) {
                    if (v.indexOf("\$${prefix}.section") > -1)
                        requiredParameterValuesSections << (v.substring(v.indexOf("\$${prefix}.section") + 9 + pl, [v.indexOf(".cell"), v.indexOf(".data"), v.indexOf(".range")].max()) as Integer)
                    if (v.indexOf("\$${prefix}.source") > -1)
                        requiredCellValueFiles << (v.substring(v.indexOf("\$${prefix}.source") + 8 + pl, [v.indexOf(".cell"), v.indexOf(".data"), v.indexOf(".range")].max()))

                } else {
                    if (v.indexOf("\$${prefix}.section") > -1) {
                        int start = v.indexOf("\$${prefix}.section") + 9 + pl
                        requiredFullDataSections << (v.substring(start, v.indexOf(".", start + 1)) as Integer)
                    }
                    if (v.indexOf("\$${prefix}.source") > -1) {
                        int start = v.indexOf("\$${prefix}.source") + 8 + pl
                        requiredFullDataFiles << (v.substring(start, v.indexOf(".", start + 1)))
                    }
                }
            } catch (Exception e) {
                log.logError("Error parsing section number: ", e)
            }
        }
        config.executedTemplateQueries?.eachWithIndex { ExecutedTemplateQuery executedTemplateQuery, int i ->
            if (!executedTemplateQuery.isVisible()) throw new PublisherPermissionException("\$${prefix}.section" + (i + 1))
            if (i + 1 in requiredParameterValuesSections) {
                if (!executedTemplateQuery.usedTemplate.isNotExportable(ReportFormatEnum.XLSX)) {
                    File file
                    //= dynamicReportService.createMultiTemplateReport(config, [outputFormat: format] << reportParams)
                    TemplateTypeEnum executeTemplateType = executedTemplateQuery.usedTemplate.templateType
                    ReportResult reportResult = executedTemplateQuery.reportResult
                    if (executeTemplateType == TemplateTypeEnum.DATA_TAB) {
                        file = dynamicReportService.createReportWithCriteriaSheet(reportResult, false, [outputFormat: "XLSX"] << htmlReportParams)
                    } else if (executeTemplateType != TemplateTypeEnum.ICSR_XML) {
                        file = dynamicReportService.createReportWithCriteriaSheetCSV(reportResult, false, [outputFormat: "XLSX"] << htmlReportParams)
                    }
                    if (file) {
                        Workbook workbook = new XSSFWorkbook(file)
                        List allTabs = []
                        for (int k = 0; k < workbook.getNumberOfSheets(); k++) {
                            allTabs << extractExcelData(workbook, k)
                        }
                        sectionExcelData.put("section" + (i + 1), allTabs[workbook?.getNumberOfSheets() - 1])
                        sectionExcelData.put("section" + (i + 1) + "allTabs", allTabs)
                        workbook.close()
                    }
                }
            }
            if (i + 1 in requiredFullDataSections) {
                Map data = getQueryTemplateData(executedTemplateQuery);
                sectionHtmlData.put("section" + (i + 1) + ".table", data.table)
                sectionHtmlData.put("section" + (i + 1) + ".chart", data.chart)
            }
        }
        List<ExecutedPublisherSource> common = ExecutedPublisherSource.findAllByConfigurationIsNull()?.findAll { !(it.name in config.attachments.name) }
        (common + config.attachments)?.each { attachment ->

            try {
                String key = requiredCellValueFiles.find { (it == "" + attachment.sortNumber) || (it == "[" + attachment.name + "]") }
                byte[] data
                if (key) {
                    if (!attachment.isVisible()) throw new PublisherPermissionException("\$${prefix}.source[" + attachment.name + "]")
                    data = publisherSourceService.getData(attachment)
                    if (attachment.fileType == BasicPublisherSource.FileType.EXCEL) {
                        Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(data))
                        List allTabs=[]
                        for(int i=0;i<workbook.getNumberOfSheets();i++){
                            allTabs<<extractExcelData(workbook, i)
                        }
                        sectionExcelData.put("source" + key, allTabs[0])
                        sectionExcelData.put("source" + key+"allTabs", allTabs)

                        workbook.close()
                    }
                    if (attachment.fileType == BasicPublisherSource.FileType.JSON) {
                        sectionJsonData.put("source" + key, JSON.parse(new String(data, "UTF-8")))
                    }
                    if (attachment.fileType == BasicPublisherSource.FileType.XML) {
                        def xml = new XmlSlurper().parseText(new String(data, "UTF-8"))
                        String rootName = xml.name()
                        sectionXmlData.put("source" + key, [(rootName): xml])
                    }
                }
                key = requiredFullDataFiles.find { (it == "" + attachment.sortNumber) || (it == "[" + attachment.name + "]") }
                if (key) {
                    data = data ?: publisherSourceService.getData(attachment)
                    if (!attachment.isVisible()) throw new PublisherPermissionException("\$${prefix}.source[" + attachment.name + "]")
                    String htmlContent = ""
                    StringBuilder html = new StringBuilder()
                    if (attachment.fileType == BasicPublisherSource.FileType.EXCEL) {
                        ExcelToHtml toHtml = ExcelToHtml.create(new ByteArrayInputStream(data), html)
                        toHtml.setCompleteHTML(true)
                        toHtml.printPage()
                        htmlContent = html.toString()
                    }
                    if (attachment.fileType == BasicPublisherSource.FileType.WORD) {
                        htmlContent = wordToHtml(new ByteArrayInputStream(data))
                        WordprocessingMLPackage targetWordMLPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(data));
                        MainDocumentPart mainDocumentPart = targetWordMLPackage.getMainDocumentPart();
                        Map<String, Object> parseResult = WordTemplateExecutor.parseDocx(mainDocumentPart, true);
                        wordData.put("source" + key, parseResult);
                    }
                    if (attachment.fileType == BasicPublisherSource.FileType.IMAGE) {
                        File tempFile = File.createTempFile("img_" + System.currentTimeMillis(), ".jpg", new File(grailsApplication.config.tempDirectory as String))
                        tempFile.withOutputStream { it.write data }
                        htmlContent = "<html><body><img src='file://${tempFile.getAbsolutePath()}'/></body></html>";
                    }
                    if (attachment.fileType == BasicPublisherSource.FileType.PDF) {
                        PDDocument pdf = PDDocument.load(data)
                        PDFTextStripper pdfStripper = new PDFTextStripper();
                        htmlContent = "<html><body>" + pdfStripper.getText(pdf)?.replaceAll("\n", "<br/>") + "</body></html>";
                        pdflData.put("source" + key, pdf)
                    }
                    if (attachment.fileType == BasicPublisherSource.FileType.JSON) {
                        htmlContent = "<html><body><pre>${new String(data, "UTF-8")}</pre></body></html>";
                    }
                    if (attachment.fileType == BasicPublisherSource.FileType.XML) {
                        htmlContent = "<html><body><pre>${new String(data, "UTF-8").replaceAll("<", "&lt;").replaceAll(">", "&gt;")}</pre></body></html>";
                    }
                    sectionHtmlData.put("source" + key, htmlContent)
                }
            } catch (Exception e) {
                log.logError("Error fetching attachment ${attachment.name} content!", e)
            }
        }

        parameterValues.each { String k, String v ->
            try {
                int startIndex = v.indexOf("\$${prefix}.") + 2 + pl
                String sectionName = v.substring(startIndex, v.indexOf(".", startIndex + 1))
                if ((v.indexOf("source") > -1) &&(v.indexOf("\$eval")==-1)) {
                    v = v.substring(v.indexOf("]") + 1)
                }
                if (v.indexOf(".chart") > -1) {
                    result.add(WordTemplateExecutor.htmlParam(k, sectionHtmlData.get(sectionName + ".chart")))
                } else if (v.indexOf(".table") > -1) {
                    result.add(WordTemplateExecutor.htmlParam(k, sectionHtmlData.get(sectionName + ".table")))
                } else if (v.indexOf(".content") > -1) {
                    result.add(WordTemplateExecutor.htmlParam(k, sectionHtmlData.get(sectionName)))
                } else if (v.indexOf(".img") > -1) {

                    PDDocument document = pdflData.get(sectionName)
                    PDFRenderer pdfRenderer = new PDFRenderer(document)
                    int start = 0;
                    int finish = document.getNumberOfPages();
                    if (v.indexOf("[") > -1) {
                        String[] ind = v.split(/\[/)[1].split(/\]/)[0].split("-")
                        start = Math.max(0, (ind[0].trim() as Integer) - 1)
                        finish = Math.min(document.getNumberOfPages(), (ind[1].trim() as Integer))
                    }
                    List<File> pages = []
                    String name = "" + System.currentTimeMillis()
                    for (int i = start; i < finish; i++) {
                        BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 300, ImageType.RGB);
                        File img = new File((grailsApplication.config.tempDirectory as String) + name + i + "_temp_img.png")
                        ImageIO.write(bim, "png", img);
                        pages.add(img)
                    }
                    StringBuffer html = new StringBuffer()
                    html.append("<html><body>")
                    pages.each {
                        html.append("<img src=\"file://" + it.getAbsolutePath() + "\"/>")
                    }
                    html.append("</body></html>")

                    result.add(WordTemplateExecutor.htmlParam(k, html.toString()))
                } else if (v.trim().startsWith("\$eval")) {
                    try {
                        String expression = v.substring(5).replaceAll(/\.data/, "").replaceAll('\\\$report\\.', "x.")
                        //.replaceAll('\\\$nlg\\.', "y.")//natural language generation demo stub
                        //Object value = Eval.xy(sectionExcelData, getNLG(), expression); //natural language generation demo stub
                        Map allData=sectionExcelData + sectionJsonData + [configuration:config]
                        if(expression.indexOf("source[")){
                            allData.each {paramName,paramValue->
                                String p='x\\.'+paramName.replaceAll(/\]/,"\\\\]").replaceAll(/\[/,"\\\\[")
                                expression = expression.replaceAll(p, "x['"+paramName+"']")
                            }
                        }
                        Object value = Eval.x(allData, expression);
                        result.add(WordTemplateExecutor.stringParam(k, value))
                    } catch (Exception e) {
                        log.logError("Error evaluating expression " + v, e)
                    }

                } else if (v.indexOf(".range") > -1) {
                    try {
                        List<Object[]> o = sectionExcelData.get(sectionName)
                        StringBuilder htmlTable = new StringBuilder()
                        htmlTable.append("<html><head><style>table, th, td { border: 1px solid black;border-collapse: collapse;}</style></head><body><table>")
                        String[] part = v.split("[\\[\\];]").findAll()
                        int rowStart = part[1] as Integer
                        int columnStart = part[2] as Integer
                        int rowEnd = getCellNumber(part[3], o.size())
                        int columnEnd = getCellNumber(part[4], o[0].size())
                        if (part.length == 6) {
                            rowStart = part[2] as Integer
                            columnStart = part[3] as Integer
                            o = sectionExcelData.get(sectionName + "allTabs")[part[1] as Integer]
                            rowEnd = getCellNumber(part[4], o.size())
                            columnEnd = getCellNumber(part[5], o[0].size())
                        }
                        for (int i = rowStart; i <= rowEnd; i++) {
                            htmlTable.append("<tr>")
                            for (int j = columnStart; j <= columnEnd; j++) {
                                if (i < o.size() && j < o[i].length)
                                    htmlTable.append("<td>" + (o[i][j] ?: "") + "</td>");
                                else
                                    htmlTable.append("<td> </td>")
                            }
                            htmlTable.append("</tr>")
                        }
                        htmlTable.append("</table></body></html>")
                        result.add(WordTemplateExecutor.htmlParam(k, htmlTable.toString()))

                    } catch (Exception e) {
                        log.expression("Unable to parse " + v, e)
                    }
                } else if (v.indexOf(".data") > -1) {
                    if (sectionJsonData.get(sectionName) || sectionXmlData.get(sectionName)) {
                        String expression = "x" + v.substring(v.indexOf(".data") + 5)
                        def obj = sectionJsonData.get(sectionName) ?: sectionXmlData.get(sectionName)
                        Object value = "[Error occurred evaluating expression]"
                        try {
                            value = Eval.x(obj, expression)
                        } catch (Exception e) {
                            log.logWarning("Error occurred evaluating  expression: " + k + " Value: " + v, e)
                        }
                        result.add(WordTemplateExecutor.dataParam(k, value))
                    } else
                        result.add(WordTemplateExecutor.dataParam(k, sectionExcelData.get(sectionName)))
                } else if (v.contains(".text") || v.contains(".paragraph") || v.contains(".title") || v.contains(".bookmark")) {
                    Map<String, Object> parseResult = wordData.get(sectionName)
                    if (!parseResult) {
                        log.logWarning("Source/section with name ${sectionName} was not found", null)
                    } else {
                        String text = (String) parseResult.get("text");
                        List<Object> textNodes = (List<Object>) parseResult.get("textNodes");
                        List<Object> rootNodes = (List<Object>) parseResult.get("rootNodes");

                        Map<String, Object> cfg = fetchParamConfig(v);
                        if (cfg.type == "text") {
                            result.add(WordTemplateExecutor.stringParam(k, substring(text, cfg.begin, cfg.end, cfg.includeLeft, cfg.includeRight)))
                        } else if (cfg.type == "title") {
                            result.add(WordTemplateExecutor.wordParam(k, WordTemplateExecutor.getSectionAsWordObjects(textNodes, rootNodes, cfg.begin, cfg.end)))
                        } else if (cfg.type == "paragraph") {
                            result.add(WordTemplateExecutor.wordParam(k, WordTemplateExecutor.getParagraphsAsWordObjects(rootNodes, cfg.begin, cfg.end, cfg.includeLeft, cfg.includeRight)))
                        } else if (cfg.type == "bookmark") {
                            result.add(WordTemplateExecutor.wordParam(k, WordTemplateExecutor.getBookmarkAsWordObjects(rootNodes, cfg.begin, cfg.end, cfg.includeLeft, cfg.includeRight)))
                        }
                    }
                } else {
                    List<Object[]> o = sectionExcelData.get(sectionName)
                    int beginIndex = v.lastIndexOf("[")
                    int endIndex = v.lastIndexOf("]")
                    if(v.indexOf("[")!=beginIndex){
                        int sheet = v.substring(v.indexOf("[") + 1, v.indexOf("]")) as Integer
                        o = sectionExcelData.get(sectionName + "allTabs")[sheet]
                    }
                    String[] val = v.substring(beginIndex + 1, endIndex).split(";");
                    int row = getCellNumber(val[0], o.size())
                    int column = getCellNumber(val[1], o[row].size())
                    result.add(WordTemplateExecutor.stringParam(k, o[row][column]))
                }
            } catch (Exception e) {
                log.logError("Error fetching value from report. Parameter: " + k + " Value: " + v, e)
            }
        }
        return result
    }

    private Integer getCellNumber(String val, Integer size) {
        if (val.indexOf("last") == -1) {
            return Integer.parseInt(val)
        } else {
            def r = val.split("-")
            Integer num = size - 1
            if (r.size() > 1 && r[1]) {
                num = num - Integer.parseInt(r[1].trim())
            }
            return num
        }
    }

    static List<Object[]> extractExcelData(Workbook workbook, sheetNum) {

        Sheet sheet = workbook?.getSheetAt(sheetNum)
        int h = sheet?.getLastRowNum() ?: 0
        int w = (0..h).collect { sheet?.getRow(it)?.getLastCellNum() ?: 0 }.max()
        Object[][] o = new Object[h + 2][w + 1];
        DataFormatter df = new DataFormatter();
        for (int i = 0; i <= h; i++) {
            XSSFRow row = sheet?.getRow(i);
            for (int j = 0; j < row?.getLastCellNum(); j++) {
                Cell cell = row.getCell(j)
                String val = df.formatCellValue(cell);
                o[i + 1][j + 1] = val?.isNumber() ? val as BigDecimal : val
            }
        }

        return o as List<Object[]>
    }

    String wordToHtml(InputStream wordFile) throws Exception {

        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(wordFile)
        HTMLSettings htmlSettings = Docx4J.createHTMLSettings()
        String tempDir = dynamicReportService.getReportsDirectory()
        htmlSettings.setImageDirPath(tempDir)
        htmlSettings.setImageTargetUri(tempDir)
        htmlSettings.setWmlPackage(wordMLPackage)


        /* CSS reset, see http://itumbcom.blogspot.com.au/2013/06/css-reset-how-complex-it-should-be.html  */
//        String userCSS = "html, body, div, span, h1, h2, h3, h4, h5, h6, p, a, img,  ol, ul, li, table, caption, tbody, tfoot, thead, tr, th, td " +
//                "{ margin: 0; padding: 0; border: 0;}" +
//                "body {line-height: 1;} ";
//        htmlSettings.setUserCSS(userCSS);


        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true);
        Docx4J.toHTML(htmlSettings, os, Docx4J.FLAG_EXPORT_PREFER_NONXSL);
        return os.toString("UTF-8");
    }

    Map<String, String> getQueryTemplateData(ExecutedTemplateQuery executedTemplateQuery) {
        ExecutedReportConfiguration executedReportConfiguration = executedTemplateQuery.executedConfiguration
        Map<String, Object> htmlParams = [:]

        File file = dynamicReportService.createMultiTemplateReport(executedReportConfiguration, [sectionsToExport: [executedTemplateQuery.id], outputFormat: ReportFormatEnum.HTML.name()] << htmlReportParams)
        if (executedTemplateQuery.usedTemplate.templateType != TemplateTypeEnum.ICSR_XML) {
            if (executedTemplateQuery.usedTemplate.showChartSheet) {
                File chartFile = dynamicReportService.createMultiTemplateReport(executedReportConfiguration, [sectionsToExport: [executedTemplateQuery.id], outputFormat: ReportFormatEnum.PDF.name()] << reportParams)
                File img = new File((grailsApplication.config.tempDirectory as String) + chartFile.getName() + ThreadLocalRandom.current().nextInt() + "_temp_img.png")
                WordTemplateExecutor.convertPageToImage(chartFile, 1, img)
                htmlParams.put("chart".toString(), "<html><body><img src=\"file://" + img.getAbsolutePath() + "\"/></body></html>");
                htmlParams.put("chartFile".toString(), img.getAbsolutePath());
                htmlParams.put("chartFileSize".toString(), img.length());
            }
            String html = FileUtils.readFileToString(file, "UTF-8")
            String begin = html.substring(0, html.indexOf("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"));
            String startString1 = "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"empty-cells: show; width: 100%; border-collapse: collapse;\">"
            String startString2 = "<table class=\"jrPage\""
            String endString = "</table>"
            int start = Math.max(html.lastIndexOf(startString1), html.lastIndexOf(startString2))
            int end = html.indexOf(endString, start);
            html = html.substring(start, end + endString.length());
            html = begin + html + '</body></html>'

            html = html.replaceAll("&versionNumber", "&amp;versionNumber")
            html = html.replaceAll("&image", "&amp;image")

            html = html.replaceAll("font-size: 14px;", "font-size: 9px;")
            html = html.replaceAll(/height:[0-9]*px/, "height:1px");
            html = html.replaceAll("line-height: 1.3398438;", "")
            html = html.replaceAll("href=\"/reports", "href=\"" + grailsApplication.config.grails.appBaseURL)
            htmlParams.put("table".toString(), html)
        }
        htmlParams
    }

    byte[] generate(PublisherConfigurationSection section, PublisherExecutionLog log, Boolean cntinue=false) throws PublisherPermissionException {

        List<WordTemplateExecutor.Parameter> paramsList = toParamsMap(section, log)
        ByteArrayInputStream template
        byte[] out = []
        WordTemplateExecutor executor = new WordTemplateExecutor(log)
        try {
            if (cntinue) {
                byte[] data = section.getDraftPublisherExecutedTemplates()?.data
                if (data) template = new ByteArrayInputStream(data)
            }
            if (!template) {
                if (section.filename) {
                    template = new ByteArrayInputStream(section.templateFileData)
                } else {
                    template = new ByteArrayInputStream(section.publisherTemplate.template)
                }
            }

            out = executor.generateFromTemplate(template, paramsList)
            log.append("Complete.")
        } catch (Exception e) {
            log.fatal = "Unknown exception occurred executing section" + e.getMessage()
            e.printStackTrace()
        }
        return out


    }

    Map updatePendingParameters(PublisherConfigurationSection section) {
        byte[] data = section.getDraftPublisherExecutedTemplates()?.data
        Map parameters = [:]
        if (data) {
            parameters = WordTemplateExecutor.fetchParameters(new ByteArrayInputStream(data))
            section.pendingComment = parameters.comment?.size() ?: 0
            section.pendingVariable = parameters.variable?.size() ?: 0
            section.pendingManual = parameters.manual?.size() ?: 0
        }
        return parameters
    }

    Map pullTheLastSectionChanges(PublisherConfigurationSection section) {
        if (section.lockCode) {
            Map response = oneDriveRestService.pullChanges(section.lockCode)
            if (response.error) return response
            PublisherExecutedTemplate executedTemplates = new PublisherExecutedTemplate()
            executedTemplates.name = section.name
            executedTemplates.numOfExecution = (section.publisherExecutedTemplates?.size() ?: 0) + 1
            executedTemplates.status = PublisherExecutedTemplate.Status.DRAFT
            executedTemplates.createdBy = userService.currentUser?.fullName
            executedTemplates.modifiedBy = userService.currentUser?.fullName
            executedTemplates.data = response.data
            section.getDraftPublisherExecutedTemplates()?.status = PublisherExecutedTemplate.Status.ARCHIVE
            section.addToPublisherExecutedTemplates(executedTemplates)
            CRUDService.save(section)
        }
        return [error: false]
    }

    Map pullTheLastFullDocumentChanges(PublisherReport document) {
        if (document.lockCode) {
            Map response = oneDriveRestService.pullChanges(document.lockCode)
            if (response.error) return response
            document.data = response.data
            CRUDService.save(document)
        }
        return [error: false]
    }

    Map pushTheLastFullDocumentChanges(PublisherReport document) {
        if (document.lockCode) {
            if (document.data) {
                Map response = oneDriveRestService.pushChanges(document.lockCode, document.data)
                if (response.code != 404) return response //if 404 will create it again
            } else {
                return [error: false]
            }
        }
        try {
            createFullDocumentFileOnOneDrive(document)
            updateFullDocumentAccessRights(document)
        } catch (Exception e) {
            log.error("Error pushTheLastFullDocumentChanges!", e);
            return [error: true, message: e.getMessage()]
        }
        return [error: false]
    }

    Map pushTheLastSectionChanges(PublisherConfigurationSection section) {
        if (section.lockCode) {
            if (section.getDraftPublisherExecutedTemplates()) {
                Map response = oneDriveRestService.pushChanges(section.lockCode, section.getDraftPublisherExecutedTemplates().data)
                if (response.code != 404) return response //if 404 will create it again
            } else {
                return [error: false]
            }
        }
        try {
            createSectionFileOnOneDrive(section)
            updateSectionAccessRights(section)
        } catch (Exception e) {
            log.error("Error pushTheLastSectionChanges!", e);
            return [error: true, message: e.getMessage()]
        }
        return [error: false]
    }

    void updateFullDocumentAccessRights(PublisherReport document) {
        if (document.lockCode) {
            Set<User> usersToShare = [document.executedReportConfiguration.owner, document.author, document.approver, document.reviewer, userService.currentUser]
            if (document.assignedToGroup) {
                usersToShare.addAll(document.assignedToGroup.users)
            } else {
                usersToShare.addAll(document.executedReportConfiguration.allSharedUsers)
            }
            oneDriveRestService.updateAccessRights(usersToShare, document.lockCode)
        }
    }

    void updateSectionAccessRights(PublisherConfigurationSection section) {
        if (section.lockCode) {
            Set<User> usersToShare = [section.author, section.approver, section.reviewer, section.executedConfiguration.owner, userService.currentUser]
            if (section.assignedToGroup) {
                usersToShare.addAll(section.assignedToGroup.users)
            } else {
                usersToShare.addAll(section.executedConfiguration.allSharedUsers)
            }
            oneDriveRestService.updateAccessRights(usersToShare, section.lockCode)
        }
    }

    void createFullDocumentFileOnOneDrive(PublisherReport document) {
        if (document.data) {
            def result = oneDriveRestService.uploadOneDriveFile(document.name, document.data,  getFolderName(document))
            document.lockedBy = userService.currentUser
            document.lockCode = result.id
            document.modifiedBy = userService.currentUser.fullName
            document.lastUpdated = new Date();
            document.save(flush: true)
        }
    }

    String getFolderName(entity) {
        ExecutedReportConfiguration configuration = (entity instanceof PublisherReport ? entity.executedReportConfiguration : entity.executedConfiguration)
        return configuration.reportName + "_" + configuration.numOfExecutions
    }

    void createSectionFileOnOneDrive(PublisherConfigurationSection section) {
        if (section.getDraftPublisherExecutedTemplates()) {
            def result = oneDriveRestService.uploadOneDriveFile(section.name, section.getDraftPublisherExecutedTemplates().data, getFolderName(section))
            section.lockedBy = userService.currentUser
            section.lockCode = result.id
            section.modifiedBy = userService.currentUser.fullName
            section.lastUpdated = new Date();
            section.save(flush: true)
        }
    }

    void processSection(PublisherConfigurationSection section, Boolean cntinue = false) throws PublisherNoTemplateException {
        if (section.publisherTemplate || section.filename || cntinue) {
            PublisherExecutionLog log = new PublisherExecutionLog()
            PublisherExecutedTemplate executedTemplates = new PublisherExecutedTemplate()
            executedTemplates.name = section.name
            executedTemplates.numOfExecution = (section.publisherExecutedTemplates?.size() ?: 0) + 1
            executedTemplates.status = PublisherExecutedTemplate.Status.DRAFT
            executedTemplates.createdBy = userService.currentUser?.fullName
            executedTemplates.modifiedBy = userService.currentUser?.fullName
            executedTemplates.data = generate(section, log, cntinue)
            if (executedTemplates.data) {
                Map parameters = WordTemplateExecutor.fetchParameters(new ByteArrayInputStream(executedTemplates.data))
                section.pendingComment = parameters.comment?.size() ?: 0
                section.pendingVariable = parameters.variable?.size() ?: 0
                section.pendingManual = parameters.manual?.size() ?: 0
            }
            section.getDraftPublisherExecutedTemplates()?.status = PublisherExecutedTemplate.Status.ARCHIVE
            executedTemplates.setExecutionLog(log)
            section.addToPublisherExecutedTemplates(executedTemplates)
            CRUDService.save(section)
        } else
            throw new PublisherNoTemplateException("No template found!")
    }

    Map<String,List<PublisherTemplateParameter>> fetchParameters(InputStream inputStream) {
        Map<String,List<PublisherTemplateParameter>> publisherTemplateParametersMap = new HashMap<>()
        Map<String, List<String>> parametersMap = WordTemplateExecutor.fetchParameters(inputStream)
        List<String> paramString = parametersMap?.variable
        List<PublisherTemplateParameter> result = new LinkedList<>()
        List existingParams = BUILD_IN_PARAMS + PublisherCommonParameter.findAllByIsDeleted(false)?.collect{it.name}
        paramString?.each {
            String[] parts = it.split("::")
            if (!(parts[0] in existingParams)) {
                PublisherTemplateParameter ptp = new PublisherTemplateParameter(name: parts[0], title: parts[0], description: parts[2], type: PublisherTemplateParameter.Type.TEXT)
                ptp.discard()
                result.add(ptp)
            }
        }
        publisherTemplateParametersMap.put("validParam", result.sort{it.name?.toLowerCase()})
        publisherTemplateParametersMap.put("invalidParam", getInvalidParams(parametersMap?.invalidVariable))
        return publisherTemplateParametersMap
    }

    List<PublisherTemplateParameter> getInvalidParams(List<String> invalidParam){
        List<PublisherTemplateParameter> invalidParamsList = new LinkedList<>()
        invalidParam?.each {
            String[] parts = it.split("::")
            PublisherTemplateParameter ptp = new PublisherTemplateParameter(name: parts[0], title: parts[0], description: parts[2], type: PublisherTemplateParameter.Type.TEXT)
            ptp.discard()
            invalidParamsList.add(ptp)
        }
        return invalidParamsList.sort{it.name?.toLowerCase()}
    }

    byte[] mergeDocx(List<byte[]> section) {
        List<BlockRange> blockRanges = new ArrayList<BlockRange>();
        if(section.size()==1){
            return section[0]
        }
        section.eachWithIndex { byte[] data, int inx ->
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(data))
            BlockRange block = new BlockRange(wordMLPackage)
            blockRanges.add(block)
            block.setStyleHandler(BlockRange.StyleHandler.USE_EARLIER);
            block.setNumberingHandler(BlockRange.NumberingHandler.USE_EARLIER);
            block.setRestartPageNumbering(false);
            block.setHeaderBehaviour(BlockRange.HfBehaviour.DEFAULT);
            block.setFooterBehaviour(BlockRange.HfBehaviour.DEFAULT);
            block.setSectionBreakBefore(BlockRange.SectionBreakBefore.CONTINUOUS);
        }
        DocumentBuilder documentBuilder = new DocumentBuilder();
        WordprocessingMLPackage output = documentBuilder.buildOpenDocument(blockRanges)
        MainDocumentPart documentPart = output.getMainDocumentPart();
        List<P> toRemove = []
        List<Object> content = documentPart.getContent();
        for(Object obj: content) {
            if (obj instanceof P) {
                P p = (P) obj;
                StringWriter txt = new StringWriter();
                TextUtils.extractText(p, txt);
                if (txt.toString().contains("Docx4j Enterprise ed")) {
                    toRemove<<p;
                }
            }

        }
        toRemove.each {
            documentPart.getContent().remove(it)
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        output.save(outputStream);
        return outputStream.toByteArray()
    }

}
