package com.rxlogix.dynamicReports

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import com.rxlogix.Constants
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.config.*
import com.rxlogix.dynamicReports.charts.ChartBuilder
import com.rxlogix.dynamicReports.charts.CrossTabChartBuilder
import com.rxlogix.dynamicReports.charts.NonCaseSQLChartBuilder
import com.rxlogix.dynamicReports.reportTypes.*
import com.rxlogix.enums.DateRangeValueEnum
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.SortEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.enums.TopColumnTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.util.Holders
import groovy.json.JsonSlurper
import net.minidev.json.parser.JSONParser
import net.sf.dynamicreports.jasper.base.templatedesign.JasperTemplateDesign
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.FieldBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.expression.JasperExpression
import net.sf.dynamicreports.report.constant.WhenNoDataType
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JREmptyDataSource
import net.sf.jasperreports.engine.JRExpression
import net.sf.jasperreports.engine.data.JRCsvDataSource
import net.sf.jasperreports.engine.data.JsonDataSource
import net.sf.jasperreports.engine.design.JasperDesign
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer
import net.sf.jasperreports.engine.util.JRSwapFile
import net.sf.jasperreports.engine.util.JsonUtil
import net.sf.jasperreports.engine.xml.JRXmlLoader
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringEscapeUtils
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.converters.exceptions.ConverterException
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.i18n.LocaleContextHolder

import javax.imageio.ImageIO
import java.nio.charset.StandardCharsets
import java.util.regex.Matcher
import java.util.zip.GZIPInputStream

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class ReportBuilder {
    private static final String MAX_PAGES_PROPERTY = "net.sf.jasperreports.governor.max.pages"

    def customMessageService = Holders.applicationContext.getBean("customMessageService")
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")
    static GrailsApplication grailsApplication = Holders.applicationContext.getBean("grailsApplication")
    protected static Logger log = LoggerFactory.getLogger(getClass())

    void createSingleReport(ExecutedReportConfiguration executedConfigurationInstance, ReportResult reportResult, Map params, ExecutedTemplateQuery executedTemplateQuery,
                                   ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        ReportBuilder reportBuilder = new ReportBuilder()
        JasperReportBuilder report = reportBuilder.initializeNewReport(reportResult)
        ReportTemplate executedTemplate = GrailsHibernateUtil.unwrapIfProxy(reportResult.executedTemplateQuery.executedTemplate)
        HeaderBuilder headerBuilder = new HeaderBuilder()
        FooterBuilder footerBuilder = new FooterBuilder()
        if (executedTemplate.useFixedTemplate && executedTemplate.fixedTemplate && executedTemplate.fixedTemplate.data && !executedTemplate.ciomsI && !executedTemplate.medWatch) {
            JasperDesign jasperDesign = JRXmlLoader.load(new ByteArrayInputStream(executedTemplate.fixedTemplate.data))
            jasperDesign.setProperty("net.sf.jasperreports.data.adapter", null)
            report.setTemplateDesign(new JasperTemplateDesign(jasperDesign))
            headerBuilder.setHeaderParameters(executedConfigurationInstance, params, report, executedTemplateQuery, null, false)
            footerBuilder.setFooterParameters(params, report, executedTemplateQuery, false)

            JRExpression designFilterExpression = jasperDesign.getFilterExpression()
            if (designFilterExpression) {
                JasperExpression<Boolean> filterExpression = new JasperExpression<>(designFilterExpression.text, Boolean.class)
                report.setFilterExpression(filterExpression)
            }
            report.setParameter(CustomReportParameters.CLINICAL_COMPOUND_NUMBER.jasperName, reportResult.clinicalCompoundNumber)

            String templateFooter= executedTemplateQuery.executedTemplate.templateFooter
            report.setParameter(CustomReportParameters.MEDDRA_VERSION.jasperName, reportResult.medDraVersion)
            report.setParameter(CustomReportParameters.TEMPLATE_FOOTER.jasperName, templateFooter)
            if(executedConfigurationInstance.instanceOf(ExecutedPeriodicReportConfiguration) && executedConfigurationInstance.periodicReportType == PeriodicReportTypeEnum.NUPR){
                report.setParameter(CustomReportParameters.INCLUDE_CASE_NUM_NUPR.jasperName, !(params.includeCaseNumber == 'false'))
            }
        } else {
            if (executedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
                ExecutedDataTabulationTemplate executedCaseLineListingTemplate = (ExecutedDataTabulationTemplate) GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery?.executedTemplate)
                if (executedCaseLineListingTemplate.showChartSheet) {
                    ChartBuilder chartBuilder = new CrossTabChartBuilder()
                    chartBuilder.createChart(executedConfigurationInstance, reportResult, params, executedTemplateQuery, jasperReportBuilderEntryList)
                }
            }
            reportBuilder.buildReport(report, reportResult, params, executedConfigurationInstance.locale?.toString())
            //???
            if (!executedTemplate.ciomsI) {
                headerBuilder.setHeader(executedConfigurationInstance, params, report, executedTemplateQuery, null, false)

                //Set the showTemplateFooter as true as we need to show it in reports only.
                executedTemplateQuery?.executedTemplate?.showTemplateFooter = true
                footerBuilder.setFooter(params, report, executedTemplateQuery, false)
            } else {
                report.addParameter(CustomReportParameters.REPORT_TITLE.jasperName, "CIOMS I Form")
                report.addParameter(CustomReportParameters.REPORT_DATE.jasperName, CustomReportParameters.REPORT_DATE.jasperType)
                headerBuilder.setHeaderParameters(executedConfigurationInstance, params, report, executedTemplateQuery, null, false)
                report.setParameter(CustomReportParameters.REPORT_DATE.jasperName, executedConfigurationInstance.nextRunDate)
            }
        }
        setDateRangeParameters(executedTemplateQuery, report)

        JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
        jasperReportBuilderEntry.jasperReportBuilder = report
        jasperReportBuilderEntry.excelSheetName = ViewHelper.getReportTitle(executedConfigurationInstance, executedTemplateQuery)
        jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        if (!Boolean.valueOf(params.isInDraftMode ?: false) && executedTemplateQuery.draftOnly) {
            jasperReportBuilderEntry.jasperReportBuilder.background(new WatermarkComponentBuilder(customMessageService.getMessage("app.label.notSubmittableWatermark"), jasperReportBuilderEntry.jasperReportBuilder.report.template))
        }
    }

    ReportResult createTempReport(executedNestedTemplate, compressedReportData, crossTabHeaders, executedConfigurationInstance, executedTemplateQuery) {
        ExecutedTemplateQuery tempExecutedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: executedNestedTemplate, header: executedNestedTemplate.name,
                executedDateRangeInformationForTemplateQuery: new ExecutedDateRangeInformation(executedTemplateQuery.executedDateRangeInformationForTemplateQuery.properties))
        tempExecutedTemplateQuery.discard()
        ReportResult tempReportResult = new ReportResult(template: executedNestedTemplate, drillDownSource: tempExecutedTemplateQuery, data: new ReportResultData(value: compressedReportData, crossTabHeader: crossTabHeaders?.toString()))
        tempReportResult.discard()
        tempExecutedTemplateQuery.executedConfiguration = executedConfigurationInstance
        return tempReportResult
    }

    void createSingleReportCSV(ExecutedReportConfiguration executedConfigurationInstance, ReportResult reportResult, Map params, ExecutedTemplateQuery executedTemplateQuery,
                               ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {

        ReportTemplate executedTemplate = GrailsHibernateUtil.unwrapIfProxy(reportResult.template ?: reportResult.executedTemplateQuery.executedTemplate)
        if ((executedTemplate.templateType == TemplateTypeEnum.TEMPLATE_SET) && (!executedTemplate.linkSectionsByGrouping) ) {
            ExecutedTemplateSet executedTemplateSet = (ExecutedTemplateSet) GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery?.executedTemplate)
            JSONObject crossTabHeaderMap = (JSONObject) JSON.parse(reportResult.data.crossTabHeader ?: "{}")
            Map subreportsData = getTemplateSetData(reportResult?.data?.decryptedValue)
            List<String> columnNames
            JSONArray crossTabHeaders
            executedTemplateSet.nestedTemplates.each {
                ReportTemplate executedNestedTemplate = GrailsHibernateUtil.unwrapIfProxy(it)
                byte[] compressedReportData
                if (executedNestedTemplate instanceof DataTabulationTemplate) {
                    crossTabHeaders = crossTabHeaderMap.getJSONArray(String.valueOf(executedNestedTemplate.id))
                    columnNames = crossTabHeaders.collect { JSONObject crossTabHeader ->
                        crossTabHeader.keySet()[0]
                    }
                    compressedReportData = convertCsvToJsonValue(subreportsData.get(executedNestedTemplate.id as String), columnNames)
                    if (!executedTemplateSet.excludeEmptySections || compressedReportData) {
                        ReportResult tempReportResult = createTempReport(executedNestedTemplate, compressedReportData, crossTabHeaders, executedConfigurationInstance, executedTemplateQuery)
                        createSingleReport(executedConfigurationInstance, tempReportResult, params, tempReportResult.executedTemplateQuery, jasperReportBuilderEntryList)
                    }
                } else {
                    compressedReportData = FileUtil.compressData(subreportsData.get(String.valueOf(executedNestedTemplate.id)))
                    if (!executedTemplateSet.excludeEmptySections || compressedReportData) {
                        ReportResult tempReportResult = createTempReport(executedNestedTemplate, compressedReportData, crossTabHeaders, executedConfigurationInstance, executedTemplateQuery)
                        createSingleReportCSV(executedConfigurationInstance, tempReportResult, params, tempReportResult.executedTemplateQuery, jasperReportBuilderEntryList)
                    }
                }
            }
        } else {
            ReportBuilder reportBuilder = new ReportBuilder()
            ReportTemplate rptTemplateObj = (ReportTemplate) GrailsHibernateUtil.unwrapIfProxy(reportResult.template)
            ReportTemplate executedTemplateObj = (ReportTemplate) GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery.executedTemplate)
            JasperReportBuilder report = reportBuilder.initializeNewReportCSV(reportResult, rptTemplateObj ?: executedTemplateObj, params)
            HeaderBuilder headerBuilder = new HeaderBuilder()
            FooterBuilder footerBuilder = new FooterBuilder()
            if (executedTemplate.useFixedTemplate && executedTemplate.fixedTemplate && executedTemplate.fixedTemplate.data && !executedTemplate.ciomsI && !executedTemplate.medWatch) {
                JasperDesign jasperDesign = JRXmlLoader.load(new ByteArrayInputStream(executedTemplate.fixedTemplate.data))
                jasperDesign.setProperty("net.sf.jasperreports.data.adapter", null)
                report.setTemplateDesign(new JasperTemplateDesign(jasperDesign))
                headerBuilder.setHeaderParameters(executedConfigurationInstance, params, report, executedTemplateQuery, null, false)
                footerBuilder.setFooterParameters(params, report, executedTemplateQuery, false)

                JRExpression designFilterExpression = jasperDesign.getFilterExpression()
                if (designFilterExpression) {
                    JasperExpression<Boolean> filterExpression = new JasperExpression<>(designFilterExpression.text, Boolean.class)
                    report.setFilterExpression(filterExpression)
                }
                report.setParameter(CustomReportParameters.MEDDRA_VERSION.jasperName, reportResult.medDraVersion)
                if(executedConfigurationInstance.instanceOf(ExecutedPeriodicReportConfiguration) && executedConfigurationInstance.periodicReportType == PeriodicReportTypeEnum.NUPR){
                    report.setParameter(CustomReportParameters.INCLUDE_CASE_NUM_NUPR.jasperName, !(params.includeCaseNumber == 'false'))
                }
            } else {
                if (executedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
                    ExecutedDataTabulationTemplate executedCaseLineListingTemplate = (ExecutedDataTabulationTemplate) GrailsHibernateUtil.unwrapIfProxy(executedTemplateQuery?.executedTemplate)
                    if (executedCaseLineListingTemplate.showChartSheet) {
                        ChartBuilder chartBuilder = new CrossTabChartBuilder()
                        chartBuilder.createChart(executedConfigurationInstance, reportResult, params, executedTemplateQuery, jasperReportBuilderEntryList)
                    }
                } else if (executedTemplate.templateType == TemplateTypeEnum.NON_CASE) {
                   if (executedTemplate.showChartSheet) {
                        ChartBuilder chartBuilder = new NonCaseSQLChartBuilder()
                        chartBuilder.createChart(executedConfigurationInstance, reportResult, params, executedTemplateQuery, jasperReportBuilderEntryList)
                    }
                }
                SpecificReportTypeBuilder specificReportTypeBuilder = reportBuilder.buildReport(report, reportResult, params, executedConfigurationInstance.locale?.toString())
                if (!executedTemplate.ciomsI && !executedTemplate.medWatch) {
                    if (specificReportTypeBuilder instanceof CaseLineListingReportBuilder && params.outputFormat == ReportFormatEnum.XLSX.name()) {
                        headerBuilder.setHeader(executedConfigurationInstance, params, report, executedTemplateQuery, null, false)
                        boolean footer = !!executedTemplateQuery.footer || !!executedTemplateQuery.executedTemplate.templateFooter||executedTemplateQuery.displayMedDraVersionNumber
                        ((CaseLineListingReportBuilder) specificReportTypeBuilder).processReportFilter(GrailsHibernateUtil.unwrapIfProxy(reportResult.template ?: reportResult.executedTemplateQuery.executedTemplate), report, params, footer)
                    } else {
                        headerBuilder.setHeader(executedConfigurationInstance, params, report, executedTemplateQuery, null, false)
                    }
                    footerBuilder.setFooter(params, report, executedTemplateQuery, false)

                } else {
                    report.addParameter(CustomReportParameters.REPORT_TITLE.jasperName, "CIOMS I Form")
                    report.addParameter(CustomReportParameters.REPORT_DATE.jasperName, CustomReportParameters.REPORT_DATE.jasperType)
                    headerBuilder.setHeaderParameters(executedConfigurationInstance, params, report, executedTemplateQuery, null, false)
                    report.setParameter(CustomReportParameters.REPORT_DATE.jasperName, executedConfigurationInstance.nextRunDate)
                    report.setParameter(CustomReportParameters.FDA_LOGO.jasperName, ImageIO.read(imageService.getFDALogo()))
                    report.setParameter(CustomReportParameters.MEDWATCH_LOGO.jasperName, ImageIO.read(imageService.getMedwatchLogo()))
                }
            }
            setDateRangeParameters(executedTemplateQuery, report)

            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = report
            jasperReportBuilderEntry.excelSheetName = ViewHelper.getReportTitle(executedConfigurationInstance, executedTemplateQuery)
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
            if (!Boolean.valueOf(params.isInDraftMode ?: false) && executedTemplateQuery.draftOnly) {
                jasperReportBuilderEntry.jasperReportBuilder.background(new WatermarkComponentBuilder(customMessageService.getMessage("app.label.notSubmittableWatermark"), jasperReportBuilderEntry.jasperReportBuilder.report.template))
            }
        }
    }


    Map getTemplateSetData(byte[] data) {
        Map result = [:]
        if (data && data.size() > 0) {
            TarArchiveInputStream subreportsInputStream = new TarArchiveInputStream(new GZIPInputStream(new ByteArrayInputStream(data)))
            TarArchiveEntry entry = subreportsInputStream.getNextTarEntry();
            if (entry) {
                subreportsInputStream = new TarArchiveInputStream(new GZIPInputStream(new ByteArrayInputStream(IOUtils.toByteArray(subreportsInputStream))))
                entry = subreportsInputStream.getNextTarEntry();
                while (entry != null) {
                    String fileName = entry.name
                    String id = fileName.tokenize(".")[0].substring(1);
                    result.put(id, IOUtils.toByteArray(subreportsInputStream))
                    entry = subreportsInputStream.getNextTarEntry();
                }
            }
        }
        return result
    }

    byte[] convertCsvToJsonValue(byte[] data, List<String> names) {
        if (!data || data.size() == 0) return new byte[0]
        byte[] toZip = data
        if (data[0] != ((byte) '[')) { //legacy reports
            List result = []
            Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(new StringReader(new String(data, "UTF-8")));
            records.each { row ->
                Map jsonRow = [:]
                row.eachWithIndex { it, i ->
                    jsonRow[names[i]] = it
                }
                result.add(jsonRow)
            }
            String content = (result as JSON).toString()
            toZip = content.getBytes("UTF-8")
        }
        return FileUtil.compressData(toZip)
    }

    SpecificReportTypeBuilder buildReport(JasperReportBuilder report, ReportResult reportResult, Map params, String lang) {
        SpecificReportTypeBuilder specificReportTypeBuilder = getSpecificReportBuilder(reportResult.template ?: reportResult.executedTemplateQuery.executedTemplate)
        params.caseNumberFieldName = reportResult?.sourceProfile?.caseNumberFieldName
        Integer maxPages = grailsApplication.config.pvreports.show.max.jasper.pages
        if (maxPages) {
            report.addProperty(MAX_PAGES_PROPERTY, String.valueOf(maxPages))
        }
        specificReportTypeBuilder.createReport(reportResult, report, params, lang)
        dynamicReportService.checkIfNoData(reportResult, report)
        return specificReportTypeBuilder
    }

    static ArrayNode filterSubtotalRows(ExecutedDataTabulationTemplate template, InputStream is, boolean chart = false) {
        ArrayNode array = (ArrayNode) JsonUtil.parseJson(is)
        int groupingListSize = template.groupingList?.reportFieldInfoList?.size() ?: 0
        // In data tabulation, Rows can have value from ROW_1 to ROW_5.
        int MAX_ROW_COUNT = 6
        for (def row in array) {
            for (int rowCount = 1; rowCount < MAX_ROW_COUNT; rowCount++) {
                if (row.has("ROW_${rowCount}")) {
                    if (row.get("ROW_${rowCount}").textValue() == null) {
                        row["ROW_${rowCount}"] = new TextNode(Constants.EMPTY)
                    }
                }
            }
        }
        List hideIndexes = []
        template.groupingList?.reportFieldInfoList?.
                eachWithIndex { it, i -> hideIndexes.add(i) }
        template.rowList?.reportFieldInfoList?.
                eachWithIndex { it, i -> if (it.hideSubtotal || chart) hideIndexes.add(i + groupingListSize) }
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).has("ROW_1")) {
                if (chart && array.get(i).get("ROW_1").asText() in ["Total","総計"]) {
                    array.remove(i);
                    i--;
                    continue;
                }
                for (int j = 0; j < hideIndexes.size(); j++)
                    if (array.get(i).get("ROW_${hideIndexes[j] + 1}").asText() in ["Subtotal", "小計"]) {
                        array.remove(i);
                        i--;
                    }
            }
        }
        return array
    }

    public static JSONArray getHeaderForTopNColumns(ReportResult reportResult, JRDataSource jsonDataSource) {
        JSONArray tabHeaders = (JSONArray) JSON.parse(reportResult.data.crossTabHeader)
        List topValues = getTopColumnsValues(reportResult)
        if (topValues) {
            ObjectMapper mapper = new ObjectMapper()
            def jsonNodeItrList = jsonDataSource.getAt("jsonNodesIterator")
            if (jsonNodeItrList) {
                List<JSONObject> jsonObjectList = new ArrayList<>()
                jsonNodeItrList.eachWithIndex { jsonNodeItr, m ->
                    String objectNode = mapper.writeValueAsString(jsonNodeItr)
                    JSONParser parser = new JSONParser()
                    JSONObject json = (JSONObject) parser.parse(objectNode)
                    jsonObjectList.add(json)
                }
                tabHeaders = filterHeader(tabHeaders, topValues, jsonObjectList)
                ArrayNode arrayNode = mapper.valueToTree(jsonObjectList)
                JsonNode jsonNode = mapper.createArrayNode().addAll(arrayNode)
                jsonDataSource.putAt("jsonNodesIterator", jsonNode.iterator())
            }
        }
        tabHeaders
    }

    static List getTopColumnsValues(ReportResult reportResult) {
        List topValues = []
        ((DataTabulationTemplate) reportResult.executedTemplateQuery.usedTemplate).columnMeasureList.eachWithIndex { DataTabulationColumnMeasure columnMeasure, int i ->
            columnMeasure.measures.each { DataTabulationMeasure measure ->
                if (measure.topColumnX > 0) {
                    topValues << [measureIndex: i, topN: measure.topColumnX, type: measure.topColumnType, measureCode: measure.type.code]
                }
            }
        }
        return topValues
    }

    static JSONArray filterHeader(JSONArray tabHeaders, List topValues, List<JSONObject> jsonObjectList) {
        if (!topValues || !jsonObjectList) return tabHeaders
        topValues.each {
            int measureIndex = it.measureIndex + 1
            int topN = it.topN
            TopColumnTypeEnum type = it.type
            String measureCode = it.measureCode
            List columnCodesJsonObjects = tabHeaders.findAll {
                String code = it.collect { it.key }[0]
                return code.endsWith(measureIndex.toString()) && code.indexOf("_" + measureCode) > -1
            }
            List columnCodes = columnCodesJsonObjects.collect { it.collect { it.key }[0] }
            List sortedCodes
            if (type == TopColumnTypeEnum.FIRST) {// top N for first row
                sortedCodes = jsonObjectList[0].findAll { k, v -> k in columnCodes }.sort { o -> -o.value }.collect { it.key }
            } else if (type == TopColumnTypeEnum.LAST) {// top N for the last row
                sortedCodes = jsonObjectList[jsonObjectList.size() - 1].findAll { k, v -> k in columnCodes }.sort { o -> -o.value }.collect { it.key }
            } else { //for total
                Map totalRow = jsonObjectList.find { it.ROW_1 in ["Total", "総計"] }
                if (totalRow) {
                    sortedCodes = totalRow.findAll { k, v -> k in columnCodes }.sort { o -> -o.value }.collect { it.key }
                } else {
                    List total = jsonObjectList[0].findAll { k, v -> k in columnCodes }.collect { [code: it.key, value: 0] }
                    jsonObjectList.each { row ->
                        total.each {
                            it.value += row[it.code]
                        }
                    }
                    sortedCodes = total.sort { -it.value }.collect { it.code }
                }
            }
            List codesToBeShown = sortedCodes.subList(0, Math.min(topN, sortedCodes.size()))
            List columnsToRemove = tabHeaders.findAll { it.collect { it.key }[0].endsWith(measureIndex.toString()) }.findAll {
                String code = it.collect { it.key }[0]
                !code.startsWith("ROW") && !(isCodeFromGroup(it.collect { it.key }[0], codesToBeShown))
            }
            tabHeaders.removeAll(columnsToRemove)

        }
        return tabHeaders
    }

    static boolean isCodeFromGroup(String code, List codes) {
        List partCode = code.split("_")
        String codeGroupIndex = code.charAt(code.length() - 1).toString()
        return codes.find { String it ->
            List partIt = it.split("_")
            if ((partCode[0] == partIt[0]) && (partCode[1] == partIt[1]) && it.endsWith(codeGroupIndex)) return true
            return false
        }
    }

    public static JRDataSource createDataSource(ReportResult reportResult, boolean chart = false) {
        InputStream is
        if (reportResult?.data?.value) {
            is = new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(reportResult?.data?.decryptedValue)))
        } else {
            is = new ByteArrayInputStream("[]".getBytes())
        }
        ArrayNode array = filterSubtotalRows((ExecutedDataTabulationTemplate) GrailsHibernateUtil.unwrapIfProxy(reportResult.executedTemplateQuery.executedTemplate), is, chart )
        if (chart) {
            JSONArray tabHeaders = (JSONArray) JSON.parse(reportResult.data.crossTabHeader)
            List complianceIndex = []
            for (JSONObject header : tabHeaders) {
                String name = header.entrySet().getAt(0).key
                if (name.trim() && name.trim()!="" && name.toLowerCase().startsWith("gp") && name.split("_").size() > 2 && name.split("_")[2].startsWith(Constants.PERCENTAGE_COLUMN)) {
                    complianceIndex.add(name)
                }
            }
            for (int i = 0; i < array.size(); i++) {
                complianceIndex.each {
                    if (array[i].get(it).toString() == "\"$Constants.NA\"") {
                        array[i].putAt(it, null)
                    }
                }
            }
        }
        JsonDataSource jsonDataSource = new JsonDataSource(array, null)
        jsonDataSource.setDatePattern("yyyy-MM-dd'T'HH:mm:ssZ")
        Map specialSettings = [:]
        if (reportResult.executedTemplateQuery.executedTemplate instanceof ExecutedDataTabulationTemplate) {
            specialSettings = CrossTabChartBuilder.setSpecialChartSettings(reportResult.executedTemplateQuery.executedTemplate)
        }
        ObjectMapper mapper = new ObjectMapper()
        def jsonNodeItrList = jsonDataSource.getAt("jsonNodesIterator")
        if (jsonNodeItrList) {
            JSONArray tabHeaders = (JSONArray) JSON.parse(reportResult.data.crossTabHeader)
            List<FieldBuilder> columnFields = []
            List<TextColumnBuilder> columns = []
            for (JSONObject header : tabHeaders) {
                String name = header.entrySet().getAt(0).key
                String label = header.entrySet().getAt(0).value
                label = label.replaceAll(Constants.JSON_HEADER_REGEX, "")
                boolean isCaseListColumn = name.startsWith(Constants.CASE_LIST_COLUMN)
                boolean isTotalCaseCount = name.startsWith(Constants.CASE_COUNT_COLUMN)
                boolean isIntervalCaseCount = name.startsWith(Constants.INTERVAL_CASE_COUNT_COLUMN)
                Matcher matcher = (name =~ Constants.JSON_ITERATOR_REGEX)
                String specialSetting = matcher.matches()?(specialSettings?.get(name.split("_")[2])):null
                //identify percentage values to link them to percentage axis
                boolean isPercentageColumn = matcher.matches() && matcher.groupCount() > 2 && matcher.group(3).startsWith(Constants.PERCENTAGE_COLUMN)&& !(matcher.group(3).startsWith("PA") && !specialSetting)
                if (!isCaseListColumn && !isTotalCaseCount && !isIntervalCaseCount && !isPercentageColumn) {
                    FieldBuilder field = field(name, type.integerType())
                    columnFields.add(field)
                    columns.add(Columns.column(label, field))
                }
            }
            Map result = [:]
            Integer topX
            ExecutedDataTabulationTemplate template = GrailsHibernateUtil.unwrapIfProxy(reportResult.executedTemplateQuery.executedTemplate)
            Integer maxChartPoints = template.maxChartPoints
            template.columnMeasureList.each { columnMeasure ->
                columnMeasure.measures.eachWithIndex { measure, i ->
                    if (measure.topXCount) topX = measure.topXCount
                    if (measure.sort) {
                        for (int x = 0; x < columns.size(); x++) {
                            if (hasMeasureNameInTitle(columns[x].object.titleExpression.value,measure.name.toString() )){
                                result = [sort: measure.sort, name: columnFields[x].name]
                                break
                            }
                        }
                    } else if (measure.topXCount) {
                        for (int x = 0; x < columns.size(); x++) {
                            if (columns[x].object.titleExpression.value.contains(measure.name.toString())) {
                                result = [sort: SortEnum.DESCENDING, name: columnFields[x].name]
                                break;
                            }
                        }
                    }
                }
            }

            if (chart && maxChartPoints && (maxChartPoints < (array.size()))) {
                if (!result) {
                    result = [:]
                }
                if (!topX) topX = maxChartPoints
                topX = topX < maxChartPoints ? topX : maxChartPoints
            }
            if (result!=null) {
                List<JSONObject> jsonObjectList = new ArrayList<>()
                jsonNodeItrList.eachWithIndex { jsonNodeItr, m ->
                    String objectNode = mapper.writeValueAsString(jsonNodeItr)
                    JSONParser parser = new JSONParser()
                    JSONObject json = (JSONObject) parser.parse(objectNode)
                    jsonObjectList.add(json)
                }
                if (result) {
                    int orderSign = (result.sort == SortEnum.DESCENDING ? -1 : 1)
                    jsonObjectList = jsonObjectList.sort { JSONObject jsonObjectA, JSONObject jsonObjectB ->
                        orderSign * (getValueFromJson(jsonObjectA, result.name) <=> getValueFromJson(jsonObjectB, result.name))
                    }
                }

                JSONObject lastJObject = new JSONObject()
                int j
                int topElements

                List<JSONObject> newJsonObjectList = new ArrayList<>()
                JSONObject totalJsonObj = new JSONObject()

                if (topX) {
                    if (chart) {
                        lastJObject.putAll(jsonObjectList.get(0))
                        lastJObject.keySet().each { k ->
                            if (k.startsWith("GP")) lastJObject[k] = (k.contains("_" + Constants.PERCENTAGE_COLUMN) ? null : 0)
                            if (k.startsWith("ROW")) lastJObject[k] = "[...]"
                            if (k.startsWith("ID")) lastJObject[k] = ""
                        }
                    }
                    int count = 0;
                    if (!chart) {
                        for (j = 0; j < jsonObjectList.size(); j++) {
                            if (!(jsonObjectList.get(j).get("ROW_1") in ["Total","総計"]) && (count < topX)) {
                                newJsonObjectList.add(jsonObjectList.get(j))
                                count++
                            }
                            if ((count >= topX) && totalJsonObj) break;
                        }
                    } else {
                        for (j = 0; j < jsonObjectList.size(); j++) {
                            if (count < topX) {
                                newJsonObjectList.add(jsonObjectList.get(j))
                                count++
                            } else {
                                jsonObjectList.get(j).keySet().each { String k ->
                                    if (k.startsWith("GP") && !k.contains("_" + Constants.PERCENTAGE_COLUMN)) lastJObject[k] += jsonObjectList.get(j).get(k) ?: 0;
                                    if (k.startsWith("ID")) lastJObject[k] += jsonObjectList.get(j).get(k) + ",";
                                }
                            }
                        }
                        newJsonObjectList.add(lastJObject)
                    }

                } else {
                    for (j = 0; j < jsonObjectList.size(); j++) {
                        if (jsonObjectList.get(j).has("ROW_1") && (jsonObjectList.get(j).get("ROW_1") in ["Total", "総計" ])) {
                            lastJObject.putAll(jsonObjectList.get(j))
                        } else {
                            newJsonObjectList.add(jsonObjectList.get(j))
                        }
                    }
                    if(lastJObject) newJsonObjectList.add(lastJObject)
                }
                ArrayNode arrayNode = mapper.valueToTree(newJsonObjectList)
                JsonNode jsonNode = mapper.createArrayNode().addAll(arrayNode)
                jsonDataSource.putAt("jsonNodesIterator", jsonNode.iterator())
            }
        }
        return jsonDataSource
    }


    static boolean hasMeasureNameInTitle(String title, String measureName) {
        if (title.contains(measureName)) {
            String ending = title.substring(title.indexOf(measureName) + measureName.length())
            if (!ending) return true
            if (ending.charAt(0).isLetterOrDigit()) return false
            return true
        }
        return false
    }

    static def getValueFromJson(JSONObject jsonObject, String name) {
        if (!jsonObject.get(name) || (jsonObject.get(name).toString() == Constants.NA)) return Integer.MIN_VALUE
        if (name.contains("_P")) return jsonObject.getDouble(name)
        if (jsonObject.get(name) instanceof String) return jsonObject.getString(name)
        return jsonObject.getInt(name)
    }

    public static JRDataSource createDataSourceCSV(ReportResult reportResult, ReportTemplate executedTemplate, Map params = null) {
        // Template Set datasource is handled separately in TemplateSetReportBuilder
        if (executedTemplate.templateType == TemplateTypeEnum.TEMPLATE_SET
                || executedTemplate.templateType == TemplateTypeEnum.ICSR_XML
                || !reportResult?.data?.value) {
            return new JREmptyDataSource(0)
        }
        String inbound_processing_template = grailsApplication.config.pvcModule.inbound_processing_template
        String late_processing_template = grailsApplication.config.pvcModule.late_processing_template
        InputStream is
        ReportTemplate template = GrailsHibernateUtil.unwrapIfProxy(reportResult.template ?: reportResult.getExecutedTemplateQuery().executedTemplate)
        if (params?.dynamic || params?.chart || (params?.appendDrillDown && (template.name in [late_processing_template, inbound_processing_template]))) {
            def templateService = Holders.applicationContext.getBean("templateService")
            def searchData = templateService.getSearchDataForInteractiveOutput(params.searchData)
            Map data, tableData
            if (template.instanceOf(CaseLineListingTemplate)) {
                data = templateService.getResultTable(template)
                if (template.name in [late_processing_template, inbound_processing_template] && !params.widget) {
                    tableData = templateService.getCllDrilldownDataAjax(params.filter, reportResult, null, null, params.direction, params.sort, searchData,
                            params.globalSearch?.trim(), data.fieldTypeMap, data.templateHeader, (params.rca ? [pvcLcpFlagPrimary: "1"] : null), params.assignedToFilter, params.rowIdFilter)
                }else{
                    tableData = templateService.getDataFromReportResultData(reportResult, data.header, data.fieldTypeMap, params.filter,null, null, params.direction, params.sort, searchData, params.globalSearch?.trim(), params.rowIdFilter, false)
                }
            }
            if (template.instanceOf(CustomSQLTemplate) || template.instanceOf(NonCaseSQLTemplate)) {
                tableData = templateService.getDataFromReportResultData(reportResult, templateService.getColumnNamesList(template.columnNamesList), null, params.filter,null, null, params.direction, params.sort, searchData, params.globalSearch?.trim(), params.rowIdFilter, false)
            }
            def csvList = tableData.aaData
            if (params.rca) {
                def fieldNameIndexTupleList = template.getFieldNameIndexTuple()
                List<String> headerList = fieldNameIndexTupleList.collect { it.getFirst() } ?: []
                Holders.applicationContext.getBean("reportExecutorService").
                        appendReasonOfDelayDataFromMart([header: headerList, data: csvList], "YYYY-MM-dd'T'HH:mm:ssZ", template.name == inbound_processing_template)
            }
            StringBuilder sb = new StringBuilder()
            int viewableColumnsCount = Integer.MAX_VALUE
            if (executedTemplate instanceof CaseLineListingTemplate) viewableColumnsCount = ((CaseLineListingTemplate) executedTemplate).getFieldNameWithIndex().size()
            if (params.chart && template.maxChartPoints && (template.maxChartPoints < csvList.size())) {
                int count = 0
                List newCsvList = []
                List lastRow = []
                List columnList = JSON.parse(executedTemplate.columnNamesList)
                for (int j = 0; j < columnList.size(); j++) {
                    if (columnList[j].contains(CustomSQLReportBuilder.CHART_COLUMN_P_PREFIX)) continue
                    else if (columnList[j].contains(CustomSQLReportBuilder.CHART_COLUMN_PREFIX)) lastRow[j] = 0
                    else lastRow[j] = "Other"
                }
                for (int j = 0; j < csvList.size(); j++) {
                    if (count < template.maxChartPoints) {
                        newCsvList.add(csvList[j])
                        count++
                    } else {
                        for (int k = 0; k < columnList.size(); k++) {
                            if (columnList[k].contains(CustomSQLReportBuilder.CHART_COLUMN_P_PREFIX)) continue
                            else if (columnList[k].contains(CustomSQLReportBuilder.CHART_COLUMN_PREFIX)) {
                                try {
                                    lastRow[k] += Double.parseDouble(csvList[j][k].toString())
                                } catch (e) {
                                    //just ignoring
                                }
                            }
                        }
                    }
                }
                newCsvList.add(lastRow)
                csvList = newCsvList
            }
            for (int i = 0; i < csvList.size(); i++) {
                if (i > 0) sb.append("\n")
                for (int j = 0; j < Math.min(csvList[i].size(), viewableColumnsCount); j++) {
                    if (j > 0) sb.append(",")
                    String content = csvList[i][j].toString()
                    content = escape(content)
                    sb.append(content)
                }
                if (params.rca) {
                    sb.append("," + (csvList[i][data.header.indexOf("dueInDays")]?: " "))
                    sb.append(",")
                    String assignedToValue = csvList[i][data.header.indexOf("assignedToUser")]
                    sb.append(escape(assignedToValue))
                    sb.append(",")
                    assignedToValue = csvList[i][data.header.indexOf("assignedToGroup")]
                    sb.append(escape(assignedToValue))
                    sb.append("," + (csvList[i][data.header.indexOf("workFlowState")] ?: " "))
                    String attachment = csvList[i][data.header.indexOf("hasAttachments")] == "true" ? ViewHelper.getMessage("app.label.yes") : ViewHelper.getMessage("app.label.no")
                    sb.append("," + attachment ?: "")
                }
            }
            is = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
        } else {
            is = new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(reportResult?.data?.decryptedValue)))
        }
        JRCsvDataSource csvDataSource = new JRCsvDataSource(is, StandardCharsets.UTF_8.name())
        csvDataSource.setDatePattern(Constants.DateFormat.CSV_JASPER)

        String[] columnNames

        switch (executedTemplate.templateType) {
            case TemplateTypeEnum.CASE_LINE:
                List<String> columnNamesList = ((CaseLineListingTemplate) executedTemplate).getFieldNameWithIndex()
                if (params.rca) {
                    columnNamesList.addAll(["dueInDays", "assignedToUser", "assignedToGroup", "workFlowState", "hasAttachments"])
                }
                columnNames = columnNamesList
                break;
            case TemplateTypeEnum.DATA_TAB:
                // Data tab should use JSON datasource, not CSV
                break;
            case TemplateTypeEnum.NON_CASE:
            case TemplateTypeEnum.CUSTOM_SQL:
                try {
                    columnNames = JSON.parse(executedTemplate.columnNamesList)
                } catch (ConverterException ce) {
                    if (executedTemplate.columnNamesList?.toString()?.length() > 2) {
                        String unwrappedColumnNamesString = executedTemplate.columnNamesList.toString().substring(1, executedTemplate.columnNamesList.toString().length() - 1)
                        columnNames = unwrappedColumnNamesString.split(", ")
                    }
                }
                break;
        }
        csvDataSource.setColumnNames(columnNames)
        return csvDataSource
    }

    static String escape(String content){
        if (content == null || content.isEmpty()) return " "

        if (content.startsWith("\"") && content.endsWith("\"")){ // already in form :  "string"
            content = content.substring(1, content.length() - 1).replace("\"", "\"\"") // replace internal double quotes
        } else {
            content = content.replace("\"", "\"\"")
        }
        content = "\"" + content + "\"" // add quotes around element, to handle special characters
        return content
    }

    JasperReportBuilder initializeNewReport(ReportResult reportResult, boolean chart = false) {
        JasperReportBuilder report = report()
        report.setDataSource(createDataSource(reportResult, chart))
                .setTemplate(Templates.reportTemplate)
                .setVirtualizer(new JRSwapFileVirtualizer(dynamicReportService.getSwapVirtualizerMaxSize(), new JRSwapFile(dynamicReportService.getReportsDirectory(), dynamicReportService.getBlockSize(), dynamicReportService.getMinGrowCount())))
        report
    }

    JasperReportBuilder initializeNewReportCSV(ReportResult reportResult, ReportTemplate executedTemplate, Map params = null) {
        JasperReportBuilder report = report()
        report.setDataSource(createDataSourceCSV(reportResult, executedTemplate, params))
                .setTemplate(Templates.reportTemplate)
                .setVirtualizer(new JRSwapFileVirtualizer(dynamicReportService.getSwapVirtualizerMaxSize(), new JRSwapFile(dynamicReportService.getReportsDirectory(), dynamicReportService.getBlockSize(), dynamicReportService.getMinGrowCount())))
        report
    }

    JasperReportBuilder initializeNewReport() {
        JasperReportBuilder report = report()
        report.setTemplate(Templates.reportTemplate)
        report.setLocale(LocaleContextHolder.getLocale())
        report.setVirtualizer(new JRSwapFileVirtualizer(dynamicReportService.getSwapVirtualizerMaxSize(), new JRSwapFile(dynamicReportService.getReportsDirectory(), dynamicReportService.getBlockSize(), dynamicReportService.getMinGrowCount())))
        report
    }

    static getSpecificReportBuilder(ReportTemplate executedTemplate) {
        if (executedTemplate.templateType == TemplateTypeEnum.CUSTOM_SQL ||
                executedTemplate.templateType == TemplateTypeEnum.NON_CASE) {
            //todo: refactor:  custom SQL is not an output format, it is a process that describes how the columns were obtained; - morett
            //todo: refactor:  this presumes a case line listing but that is not correct; custom sql should work for any output type - morett
            return new CustomSQLReportBuilder()
        } else if (executedTemplate.templateType == TemplateTypeEnum.DATA_TAB) {
            if (executedTemplate.transposeOutput) {
                return new CrosstabTransposedReportBuilder()
            } else {
                return new CrosstabReportBuilder()
            }
        } else if (executedTemplate.templateType == TemplateTypeEnum.TEMPLATE_SET) {
            return new TemplateSetReportBuilder()
        } else if (executedTemplate.templateType == TemplateTypeEnum.CASE_LINE) {
            return new CaseLineListingReportBuilder()
        }
        return new UnknownReportBuilder()
    }

    private void setDateRangeParameters(ExecutedTemplateQuery templateQuery, JasperReportBuilder report) {
        //report.addParameter(CustomReportParameters.REPORT_START_DATE.jasperName, CustomReportParameters.REPORT_START_DATE.jasperType)
        //report.addParameter(CustomReportParameters.REPORT_END_DATE.jasperName, CustomReportParameters.REPORT_END_DATE.jasperType)
        if (templateQuery) {
            if (templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEnum.name() == DateRangeValueEnum.CUMULATIVE.name()) {
                Date endDate = (templateQuery.executedConfiguration instanceof ExecutedPeriodicReportConfiguration) ?
                        templateQuery.getGlobalEndDate() :
                        templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute
                report.setParameter(CustomReportParameters.REPORT_END_DATE.jasperName, endDate)
            } else {
                Date startDate = templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeStartAbsolute
                report.setParameter(CustomReportParameters.REPORT_START_DATE.jasperName, startDate)
                Date endDate = templateQuery.executedDateRangeInformationForTemplateQuery.dateRangeEndAbsolute
                report.setParameter(CustomReportParameters.REPORT_END_DATE.jasperName, endDate)
            }
        }
    }
}
