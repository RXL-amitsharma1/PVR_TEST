package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.CustomMessageService
import com.rxlogix.config.*
import com.rxlogix.dynamicReports.ReportBuilder
import com.rxlogix.dynamicReports.Templates
import com.rxlogix.dynamicReports.reportTypes.common.GroupTextExpression
import com.rxlogix.dynamicReports.reportTypes.crosstab.HeaderTabDTO
import com.rxlogix.dynamicReports.reportTypes.crosstab.TotalOrSubtotalSortExpression
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.SortEnum
import com.rxlogix.reportTemplate.CountTypeEnum
import com.rxlogix.reportTemplate.MeasureTypeEnum
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.util.Holders
import groovy.util.logging.Slf4j
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter
import net.sf.dynamicreports.report.builder.DynamicReports
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.builder.expression.AbstractComplexExpression
import net.sf.dynamicreports.report.builder.expression.JasperExpression
import net.sf.dynamicreports.report.builder.expression.ValueExpression
import net.sf.dynamicreports.report.builder.grid.ColumnGridComponentBuilder
import net.sf.dynamicreports.report.builder.grid.ColumnTitleGroupBuilder
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.builder.style.ConditionalStyleBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.GroupHeaderLayout
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment
import net.sf.dynamicreports.report.constant.HyperLinkTarget
import net.sf.dynamicreports.report.constant.Markup
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.jasperreports.engine.design.JasperDesign
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject

import java.awt.Color

import static net.sf.dynamicreports.report.builder.DynamicReports.*
@Slf4j
class CrosstabReportBuilder implements SpecificReportTypeBuilder, SpecificTemplateTypeBuilder {
    static final String ROW_ID_FIELD_NAME = "ID"

    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    @Override
    void createReport(ReportResult reportResult, JasperReportBuilder report, Map params, String lang) {
        this.templateStyles = stl.templateStyles()
        List<ColumnGroupBuilder> columnGroupList = addGroupingColumns(reportResult, params)
        if (columnGroupList) {
            report.setShowColumnTitle(false)
            report.groupBy(*columnGroupList)
        }
        ExecutedDataTabulationTemplate executedTemplate = reportResult.executedTemplateQuery.usedTemplate
        if (executedTemplate.pageBreakByGroup) {
            for (ColumnGroupBuilder columnGroupBuilder : columnGroupList) {
                columnGroupBuilder.setStartInNewPage(true)
            }
        }
        //Note: this is a manually created crosstab report that is closer in structure to a case line listing report
        List<HeaderTabDTO> headerTabs = addCrosstabColumns(reportResult, report, params)
        if (params.outputFormat != ReportFormatEnum.XLSX.name()) {
            DataTabulationTemplate template = GrailsHibernateUtil.unwrapIfProxy(reportResult.executedTemplateQuery.executedTemplate)
            adjustColumnWidth(template?.rowList?.reportFieldInfoList, headerTabs)
            report.addField(field(ROW_ID_FIELD_NAME, type.integerType()))
        }
    }

    @Override
    void createSubReport(ReportTemplate executedTemplate, JasperReportBuilder report, Map params, String lang) {
        this.templateStyles = stl.templateStyles()
        JSONArray tabHeaders = params.crossTabHeader
        List<HeaderTabDTO> headerTabs = addCrosstabColumns(executedTemplate, tabHeaders, report, params)
        if (params.outputFormat != ReportFormatEnum.XLSX.name()) {
            DataTabulationTemplate template = executedTemplate
            adjustColumnWidth(template.rowList.reportFieldInfoList, headerTabs)
            report.addField(field(ROW_ID_FIELD_NAME, type.integerType()))
        }
    }

    @Override
    JasperDesign buildTemplate(ReportTemplate template, JasperReportBuilder report, Map params, String lang) {
        //TODO: Implementation
        return report.toJasperDesign()
    }

    protected JSONArray getTabHeaders(ReportResult reportResult, JasperReportBuilder report) {
        return (JSONArray) JSON.parse(reportResult.data.crossTabHeader)
    }

    private List<String> addCrosstabColumns(ReportResult reportResult, JasperReportBuilder report, Map params) {
        if (!reportResult.data) {
            return
        }
        JSONArray tabHeaders = ReportBuilder.getHeaderForTopNColumns(reportResult, report.getDataSource())
        params.reportResultId = reportResult.id
        return addCrosstabColumns(reportResult.executedTemplateQuery.usedTemplate, tabHeaders, report, params)
    }

    private List<String> addCrosstabColumns(DataTabulationTemplate executedTemplate, JSONArray tabHeaders,
                                            JasperReportBuilder report, Map params) {
        ColumnGroupBuilder singleColumnGroup
        int groupingListSize = executedTemplate.groupingList?.reportFieldInfoList?.size() ?: 0
        List<String> fields = []
        List<HeaderTabDTO> headerTabs = []
        for (JSONObject header : tabHeaders) {
            HeaderTabDTO headerTabDTO = new HeaderTabDTO(header)
            fields.add(headerTabDTO.columnName)
            TextColumnBuilder column
            if (!headerTabDTO.isRowColumn() && !headerTabDTO.isCaseListColumn()) {
                def columnType = type.integerType()
                if (headerTabDTO.isPercentageColumn()) {
                        columnType = type.stringType()
                }
                column = createColumn(headerTabDTO.columnLabel, headerTabDTO.columnName, columnType, params)
                column.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
                        .setTitleStyle(stl.style(Templates.columnTitleStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER))
                if (executedTemplate.drillDownToCaseList
                        && !headerTabDTO.isPercentageColumn() && !headerTabDTO.isTotalCaseCount()
                        && !headerTabDTO.isIntervalCaseCount() && !headerTabDTO.isMeasureTotal()
                        && (!params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name())) {
                    CaseCountHyperlinkExpression hyperLinkExpression = new CaseCountHyperlinkExpression(headerTabDTO.columnName, params.reportResultId)
                    hyperLinkExpression.addExpression(new TotalOrSubtotalRow(fields))
                    column.setHyperLink(DynamicReports.hyperLink(hyperLinkExpression).setTarget(HyperLinkTarget.BLANK))
                }
            } else if (isGroupedRow(headerTabDTO.columnName, tabHeaders, executedTemplate)) {
                continue
            } else {
                column = createColumn(headerTabDTO.columnLabel, headerTabDTO.columnName, type.stringType(), params)
            }
            def columnStyle = getOrCreateColumnStyle(column, params)
            ConditionalStyleBuilder totalOrSubtotalStyle = stl.conditionalStyle(new TotalOrSubtotalRow(fields)).bold().setBackgroundColor(Templates.subTotalBackgroundColor)
            List<ConditionalStyleBuilder> styles = [totalOrSubtotalStyle]
            List colorConditionsForField = getColorConditionsForField(executedTemplate, headerTabDTO.columnName)
            styles.addAll(getConditionsStylesList(fields, headerTabDTO.columnName, colorConditionsForField))
            ConditionalStyleBuilder[] stylesArray = new ConditionalStyleBuilder[styles.size()]
            styles.toArray(stylesArray)
            StyleBuilder totalOrSubtotalStyleColumnStyle = stl.style(columnStyle).addConditionalStyle(stylesArray)
            totalOrSubtotalStyleColumnStyle.setMarkup(headerTabDTO.isRowColumn() ? Markup.NONE : Markup.HTML)

            column.setStyle(totalOrSubtotalStyleColumnStyle)
            column.setValueFormatter(new ConditionalColumnFormatter(fields, headerTabDTO.columnName, colorConditionsForField, params.outputFormat, groupingListSize))
            if (headerTabDTO.isRowColumn()
                    && (params.outputFormat != ReportFormatEnum.XLSX.name()
                    || executedTemplate.supressRepeatingExcel)) {
                singleColumnGroup = grp.group(createColumn(headerTabDTO.columnLabel, headerTabDTO.columnName, type.stringType(), params))
                        .setHeaderLayout(GroupHeaderLayout.EMPTY).setPadding(0)
                report.groupBy(singleColumnGroup)
                column.setValueFormatter(new SuppressRepeatingColumnFormatter(singleColumnGroup, headerTabDTO.columnName, true, groupingListSize, headerTabDTO.columnName))
            } else if(headerTabDTO.isRowColumn()){
                column.setValueFormatter(new TotalSubtotalColumnFormatter(headerTabDTO.columnName, groupingListSize))
            }
            if (headerTabDTO.isRowColumn()) {
                report.addSort(asc(new TotalOrSubtotalSortExpression(headerTabDTO.columnName)))
            }
            // We do use "/n" as joiner while saving into DB
            headerTabDTO.columnBuilder = column
            headerTabs.add(headerTabDTO)
        }
        //Once used always Supress Tabulation
        if (executedTemplate.instanceOf(ExecutedDataTabulationTemplate) && executedTemplate.supressHeaders) {

            List headersList = getSupressColumnTree(headerTabs)
            // Transform the map to column grouping
            List<ColumnGridComponentBuilder> columnGrid = headersList.collect { headersMap ->
                groupHeaders(headersMap, Templates.columnTitleStyle)
            }.flatten()

            report.columnGrid(*columnGrid)
        }
        headerTabs.each {
            report.addColumn(it.columnBuilder)
        }
        Map<DataTabulationMeasure, List<HeaderTabDTO>> measureListMap = getMeasureMapping(headerTabs,
                executedTemplate.columnMeasureList)
        processReportLimiting(report, measureListMap)
        processReportSorting(report, measureListMap)
        return headerTabs
    }

    static List getSupressColumnTree(List<HeaderTabDTO> headerTabs) {
        def tree
        tree = { -> [:].withDefault { tree() } }

        List headersList = []
        Map previousMap
        // Create a map to group labels
        headerTabs.each { HeaderTabDTO headerTab ->
            Map currentMap
            if (!previousMap || headerTab.labels.first() != previousMap.keySet().first()) {
                currentMap = tree()
                headersList.add(currentMap)
                previousMap = currentMap
            } else {
                currentMap = previousMap
            }
            headerTab.labels.eachWithIndex { String label, int i ->
                if (i == headerTab.labels.size() - 1) {
                    currentMap[label] = headerTab
                } else {
                    currentMap = currentMap[label]
                }
            }
        }
        return headersList
    }

    private static List<ColumnGridComponentBuilder> groupHeaders(Map headers, def columnTitleTemplateStyle, List<String> groupKeys = []) {
        return headers.collect { key, value ->
            if (value instanceof Map) {
                if (value.size() > 0) {
                    ColumnTitleGroupBuilder group = grid.titleGroup(key)
                    group.add(*groupHeaders(value, columnTitleTemplateStyle, groupKeys.plus(key)))
                    if(alignByColumnNameClass(value.values().first())){
                        group.setTitleStyle(stl.style(columnTitleTemplateStyle).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))
                    } else {
                        group.setTitleStyle(stl.style(columnTitleTemplateStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER))
                    }
                    return group.setTitleWidth(1)
                }
                return groupHeaders(value, columnTitleTemplateStyle, groupKeys).first()
            } else {
                HeaderTabDTO headerTab = ((HeaderTabDTO) value)
                headerTab.labels.removeAll(groupKeys)
                headerTab.columnBuilder.setTitle(headerTab.labels.join("\n").trim())
                return ((HeaderTabDTO) value).columnBuilder
            }
        }
    }

    static Boolean alignByColumnNameClass(def values){
        def headerTabDTO = (values?.columnName) ? values : values[" "]
        while(headerTabDTO instanceof Map && headerTabDTO.size()>0){
            headerTabDTO = headerTabDTO[" "]
        }
        headerTabDTO instanceof HeaderTabDTO && headerTabDTO.columnName?.contains("ROW_")
    }

    TextColumnBuilder createColumn(String columnLabel, String columnName, def columnType, Map params) {
        TextColumnBuilder column = col.column(columnLabel, columnName, columnType)
        StyleBuilder columnStyle = getOrCreateColumnStyle(column, params)
        return column
    }

    /**
     * Adjustment of column width for data tabulations
     */
    private def adjustColumnWidth(List<ReportFieldInfo> fieldsInfo, List<HeaderTabDTO> headerTabs) {
        def index = 0
        def columnWidthList = headerTabs.collect {
            def width = ReportFieldInfo.AUTO_COLUMN_WIDTH
            if (fieldsInfo && index < fieldsInfo.size()) {
                width = fieldsInfo[index].columnWidth
            }
            index++
            return width
        }
        def predefinedWidthCount = columnWidthList.findAll({ it != ReportFieldInfo.AUTO_COLUMN_WIDTH }).size()
        if (predefinedWidthCount > 0) {
            def normalizationFactor = getNormalizationFactor(columnWidthList)
            def autoColumnWidth = getAutoColumnWidth(columnWidthList)
            headerTabs.eachWithIndex { HeaderTabDTO entry, int i ->
                BigDecimal columnWidth = columnWidthList[i] == ReportFieldInfo.AUTO_COLUMN_WIDTH ? autoColumnWidth : columnWidthList[i] / normalizationFactor
                entry.columnBuilder.setWidth(columnWidth.intValue())
            }
        }
    }


    private List<ColumnGroupBuilder> addGroupingColumns(ReportResult reportResult, Map params) {
        ExecutedDataTabulationTemplate executedTemplate = reportResult.executedTemplateQuery.usedTemplate
        List<ReportFieldInfo> groupingColumns = executedTemplate.groupingList?.reportFieldInfoList
        if (groupingColumns) {
            List<ColumnGroupBuilder> columnGroupList = []
            List<ColumnGroupBuilder> columnGroups = []
            List<ComponentBuilder> groupHeader = []
            for (int i = 0; i < groupingColumns?.size(); i++) {
                ReportFieldInfo reportFieldInfo = groupingColumns.get(i)
                ReportField reportField = reportFieldInfo.reportField
                String columnLabel = customMessageService.getMessage("app.reportField." + reportField.name)
                if (reportFieldInfo.renameValue) {
                    columnLabel = reportFieldInfo.renameValue
                }
                TextColumnBuilder column = createColumn(columnLabel, "ROW_${i + 1}", reportFieldInfo, params)
                ColumnGroupBuilder singleColumnGroup = grp.group(column)
                columnGroups.add(singleColumnGroup)
                singleColumnGroup.setHeaderLayout(GroupHeaderLayout.EMPTY)
                if (i == groupingColumns?.size() - 1) {
                    singleColumnGroup.showColumnHeaderAndFooter()
                    def groupHeaderExpression = new GroupTextExpression(columnGroups, groupingColumns)
                    StyleBuilder groupHeaderStyle = stl.style(Templates.boldStyle).bold()
                    def groupHeaderComponent = cmp.text(groupHeaderExpression)
                            .setStyle(groupHeaderStyle)
                            .setEvaluationGroup(singleColumnGroup)
                    // A hack for fixing the https://rxlogixdev.atlassian.net/browse/PVR-4747 bug, It's Jasper Reports library bug
                    if (!dynamicReportService.isInPrintMode(params)) {
                        groupHeaderComponent.setMinRows(5)
                    }
                    columnGroups.reverse().eachWithIndex { ColumnGroupBuilder group, int groupIndex ->
                        TextFieldBuilder<String> bookmarkHeader = cmp.text(" ").setWidth(1)
                        bookmarkHeader.setAnchorName(new GroupAnchorNameExpression(group))
                        bookmarkHeader.setBookmarkLevel(columnGroups.size() - groupIndex + 1)
                        bookmarkHeader.setPrintWhenExpression(new SuppressRepeatingBookmarkPrintWhenExpression(columnGroups.subList(0, columnGroups.size() - groupIndex)))
                        groupHeader.add(bookmarkHeader)
                    }
                    groupHeader.add(groupHeaderComponent)
                    singleColumnGroup.addHeaderComponent(cmp.horizontalFlowList(*groupHeader.reverse()))
                    singleColumnGroup.reprintHeaderOnEachPage()
                    singleColumnGroup.setMinHeightToStartNewPage(60)
                    singleColumnGroup.setPadding(0)
                }
                columnGroupList.add(singleColumnGroup)
            }
            return columnGroupList
        }
        return null
    }

    private class GroupAnchorNameExpression extends AbstractSimpleExpression<String> {

        private ColumnGroupBuilder columnGroup

        GroupAnchorNameExpression(ColumnGroupBuilder columnGroup) {
            this.columnGroup = columnGroup
        }

        String evaluate(ReportParameters reportParameters) {
            String value = reportParameters.getFieldValue(columnGroup.group.valueField.valueExpression.name)
            if (value == null || value.trim().length() == 0) {
                value = ViewHelper.getEmptyLabel()
            }
            return value
        }
    }

    private class TotalOrSubtotalRow extends AbstractSimpleExpression<Boolean> {
        List<String> fields

        TotalOrSubtotalRow(List<String> fields) {
            this.fields = fields
        }

        @Override
        Boolean evaluate(ReportParameters reportParameters) {
            for (String field : fields) {
                Object value = reportParameters.getValue(field)
                if (value instanceof String && ("Total".equals(value) || "Subtotal".equals(value) || "Sub Total".equals(value) || "総計".equals(value) || "小計".equals(value))) {
                    return true
                }
            }
            return false
        }
    }

    private class CaseCountHyperlinkExpression extends AbstractComplexExpression<String> {
        private String fieldName
        private Long reportResultId

        CaseCountHyperlinkExpression(String fieldName, Long reportResultId) {
            this.fieldName = fieldName
            this.reportResultId = reportResultId
        }

        @Override
        String evaluate(List<?> values, ReportParameters reportParameters) {
            Boolean isTotalorSubtotal = (Boolean) values.get(0)
            def rowId = reportParameters.getFieldValue(ROW_ID_FIELD_NAME)
            def value = reportParameters.getFieldValue(fieldName)
            List<String> totalColumnList = ['GP_1_IC41', 'GP_1_EC41', 'GP_1_PC41', 'GP_1_VC41', 'GP_1_IR41']
            if ( rowId != null && value && !(fieldName in totalColumnList)) {
                return grailsLinkGenerator.link(
                        controller: 'caseSeries',
                        action: 'previewCrosstabCases',
                        id: reportResultId,
                        params: [rowId: rowId, columnName: fieldName, count: value])
            }
            return null
        }
    }

    private Map<DataTabulationMeasure, List<HeaderTabDTO>> getMeasureMapping(List<HeaderTabDTO> headerTabs, List<DataTabulationColumnMeasure> columnMeasureList) {
        Map<DataTabulationMeasure, List<HeaderTabDTO>> measureListMap = new LinkedHashMap<>()
        columnMeasureList.eachWithIndex { DataTabulationColumnMeasure columnMeasure, int i ->
            columnMeasure.measures.each { DataTabulationMeasure measure ->
                measureListMap.put(measure, headerTabs.findAll {
                    it.columnIndex == (i + 1) && it.isRelatedWith(measure.type)
                })
            }
        }
        return measureListMap
    }

    private void processReportLimiting(JasperReportBuilder report, Map<DataTabulationMeasure, List<HeaderTabDTO>> measureListMap) {
        Map.Entry<DataTabulationMeasure, List<HeaderTabDTO>> topXEntry = measureListMap.entrySet()
                .find { it.key.showTopX }
        if (topXEntry && topXEntry.value && topXEntry.value.size() > 0) {
            DataTabulationMeasure measure = topXEntry.key
            report.setParameter("REPORT_MAX_COUNT", measure.topXCount + 1)
            TextColumnBuilder columnBuilder = topXEntry.value.first().columnBuilder
            if (!measure.sort) {
                report.addSort(desc(columnBuilder))
            }
        }
    }

    private void processReportSorting(JasperReportBuilder report, Map<DataTabulationMeasure, List<HeaderTabDTO>> measureListMap) {
        List<Map.Entry<DataTabulationMeasure, List<HeaderTabDTO>>> sortEntries = measureListMap.entrySet()
                .findAll { it.key.sort }
                .sort { it.key.sortLevel }
        sortEntries.each {
            DataTabulationMeasure measure = it.key
            if (it.value && it.value.size() > 0) {
                TextColumnBuilder columnBuilder = it.value.first().columnBuilder
                report.addSort(measure.sort == SortEnum.DESCENDING ? desc(columnBuilder) : asc(columnBuilder))
            }
        }
    }

    private boolean isGroupedRow(String columnName, JSONArray tabHeaders, DataTabulationTemplate executedTemplate) {
        int index = tabHeaders.findIndexOf {
            it.entrySet().getAt(0).key == columnName
        }
        return index > -1 && executedTemplate.groupingList && index < executedTemplate.groupingList.reportFieldInfoList.size()
    }

    static List getColorConditionsForField(DataTabulationTemplate executedTemplate, String currentField) {
        List colorConditions = []
        if (currentField.startsWith("GP")) {
            String setAndMeasure = currentField.split("_")[2]
            String measureCode = setAndMeasure.substring(0, setAndMeasure.length() - 2)
            String dateRangeCountType = CountTypeEnum.getTypeByCode(setAndMeasure.charAt(2))
            MeasureTypeEnum measureType = MeasureTypeEnum.values().find { it.getCode() == measureCode }
            if (measureType) {
                int setNumber = Integer.parseInt(setAndMeasure.substring(setAndMeasure.length() - 1, setAndMeasure.length()))
                if ((setNumber > 0) && (setNumber <= executedTemplate.columnMeasureList.size())) {
                    DataTabulationColumnMeasure set = executedTemplate.columnMeasureList[setNumber - 1]

                    DataTabulationMeasure measure = set.measures.find { ((it.type == measureType) && (it.dateRangeCount.type == dateRangeCountType)) }
                    if (measure?.colorConditions) {
                        JSONElement conditionsJson = JSON.parse(measure.colorConditions)
                        conditionsJson.each {
                            it.conditions.each {
                                if (it.field.startsWith("ROW_")) {
                                    String fieldName = it.field.split("_")[1]
                                    int index = executedTemplate.rowList?.reportFieldInfoList?.findIndexOf { ReportFieldInfo r -> r.reportField.name == fieldName } + 1 + (executedTemplate.groupingList?.reportFieldInfoList?.size()?:0)
                                    it.field = "ROW_" + index
                                }
                            }
                            colorConditions << it
                        }
                    }
                }

            }
        }
        return colorConditions
    }

    private List<ConditionalStyleBuilder> getConditionsStylesList(List<String> fields, String currentField, colorConditionsForField) {
        List<ConditionalStyleBuilder> conditions = colorConditionsForField?.collect {
            if (it.color) {
                stl.conditionalStyle(new ColorConditionCell(fields, currentField, it.conditions))
                        .setBackgroundColor(new Color(Integer.valueOf(it.color.substring(1, 3), 16),
                                Integer.valueOf(it.color.substring(3, 5), 16),
                                Integer.valueOf(it.color.substring(5, 7), 16)))
            } else null
        }
        return conditions.findAll { it }
    }

    static String translateTotalSubtotal(String currentValue, String field, Integer groupingListSize) {
        if (currentValue && field) {
            CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
            if (groupingListSize > 0) {
                if ((field == "ROW_1") && (currentValue.toString() == "Total")) return customMessageService.getMessage("app.grand.total")
                int rowCount = groupingListSize + 1
                if ((field == "ROW_${rowCount}") && (currentValue.toString() in ["Subtotal", "小計"])) return customMessageService.getMessage("app.total")
            }
            if (currentValue.toString() in ["Total", "合計", "総計"]) return customMessageService.getMessage("app.total")
            if (currentValue.toString() in ["Subtotal", "小計"]) return customMessageService.getMessage("app.subtotal")
        }
        return null
    }

    class ConditionalColumnFormatter extends AbstractValueFormatter<Object, Object> {

        List<String> fields
        String field
        List fieldConditions
        Boolean supportIcon
        Integer groupingListSize

        ConditionalColumnFormatter(List<String> fields, String field, List fieldConditions, String format, Integer groupingListSize) {
            this.fields = fields
            this.field = field
            this.fieldConditions = fieldConditions
            supportIcon = !format || (format in [ReportFormatEnum.HTML.name(), ReportFormatEnum.PDF.name()])
            this.groupingListSize = groupingListSize
        }

        @Override
        Object format(Object o, ReportParameters reportParameters) {
            if (!o) {
                return o
            }
            String translated = translateTotalSubtotal(o.toString(), field, groupingListSize)
            if (translated) return translated
            if (fieldConditions) {
                String text = o.toString()
                for (int j = 0; j < fieldConditions.size(); j++) {
                    Map oneFieldCondition = fieldConditions[j];
                    Boolean conditionResult = false
                    for (int i = 0; i < oneFieldCondition.conditions.size(); i++) {
                        String fieldPattern = CrosstabReportBuilder.formFiledNameForColorCondition(oneFieldCondition.conditions[i].field, field)
                        conditionResult = CrosstabReportBuilder.evaluateColorSubCondition(reportParameters.getValue(fieldPattern), oneFieldCondition.conditions[i].operator as QueryOperatorEnum, oneFieldCondition.conditions[i].value)
                        if (!conditionResult) break
                    }
                    if (conditionResult && oneFieldCondition.icon) {
                        return formatConditionalCell(oneFieldCondition.icon, text, supportIcon)
                    }
                }
            }
            return o
        }
    }

    class TotalSubtotalColumnFormatter extends AbstractValueFormatter<Object, Object> {

        String field
        Integer groupingListSize

        TotalSubtotalColumnFormatter(String field, Integer groupingListSize) {
            this.field = field
            this.groupingListSize = groupingListSize
        }

        @Override
        Object format(Object o, ReportParameters reportParameters) {
            if (!o) {
                return o
            }
            String translated = translateTotalSubtotal(o.toString(), field, groupingListSize)
            if (translated) return translated
            return o
        }
    }

    static formatConditionalCell(String format, String text, Boolean supportIcon = true) {
        if (!format) return text
        StringBuilder sb = new StringBuilder();
        boolean tagOpen = false
        String highlighttext = null
        String highlightcolor = null
        for (int i = 0; i < format.length(); i++) {

            if (format[i] == "\\") {
                if (supportIcon) {
                    sb.append("<font face=\"FontAwesome\">&#x").append("" + format[i + 1] + format[i + 2] + format[i + 3] + format[i + 4]).append(";</font>")
                }
                i = i + 4
                continue
            }
            if (format[i] == "<") {
                if (format[i + 1] == "/") {
                    highlighttext = null
                    highlightcolor = null
                } else tagOpen = true
            }
            if (format[i] == ">") {
                tagOpen = false

            }
            if (tagOpen && (format[i] == "h")) {
                String t = fetchParameterValue("highlighttext", format, i)
                if (t) highlighttext = t
                t = fetchParameterValue("highlightcolor", format, i)
                if (t) highlightcolor = t
            }
            if (format[i] == "\$") {
                if (format.substring(i, format.length()).startsWith("\$value")) {
                    if (highlighttext && highlightcolor) {
                        sb.append(highlight(text, highlighttext, highlightcolor))
                    } else
                        sb.append(text)
                    i = i + 5
                    continue
                }
            }
            sb.append(format[i])
        }
        return sb.replaceAll("<s>", "<style isStrikeThrough=\"true\">").replaceAll("</s>","</style>").toString()
    }

    static String fetchParameterValue(String name, String format, int index) {
        String sub = format.substring(index, format.length())
        if (sub.startsWith(name)) {
            int start = sub.indexOf("\"")
            if (start > -1) {
                int end = sub.indexOf("\"", start + 1)
                if (end > -1) {
                    return sub.substring(start + 1, end)
                }
            }
        }
        return null
    }

    static String highlight(String text, String part, String color) {
        String upText = text.toUpperCase()
        String upPart = part.toUpperCase()
        int start = upText.indexOf(upPart)
        String result = text
        while (start > -1) {
            String replacement = "<font color=\"${color}\">" + result.substring(start, start + part.length()) + "</font>"
            result = result.substring(0, start) + replacement + result.substring(start + part.length(), result.length())

            start = result.toUpperCase().indexOf(upPart, start + replacement.length())
        }
        return result
    }

    class ColorConditionCell extends AbstractSimpleExpression<Boolean> {
        List<String> fields
        String field
        List conditions

        ColorConditionCell(List<String> fields, String field, List conditions) {
            this.fields = fields
            this.field = field
            this.conditions = conditions
        }

        @Override
        Boolean evaluate(ReportParameters reportParameters) {
            Boolean result = true
            for (int i = 0; i < conditions.size(); i++) {
                String fieldPattern = formFiledNameForColorCondition(conditions[i].field, field)
                result = evaluateColorSubCondition(reportParameters.getValue(fieldPattern), conditions[i].operator as QueryOperatorEnum, conditions[i].value)
                if (!result) return false
            }
            return result
        }
    }

    static String formFiledNameForColorCondition(String conditionField, String currentField) {
        String fieldPattern = conditionField
        if (fieldPattern.startsWith("GP")) {
            String[] parts = fieldPattern.split("-")
            Integer set = Integer.parseInt(parts[3]) + 1
            String countType = (parts.length>4?parts[4]:"1")
            String measure = HeaderTabDTO.MEASURE_TYPE_TO_ALIASES[parts[1] as MeasureTypeEnum][Integer.parseInt(parts[2])]
            String[] filedParts = currentField.split("_")
            if (parts[2] == "2") {
                fieldPattern = "GP_1_" + measure + "4" + set
            } else {
                fieldPattern = "GP_" + filedParts[1] + "_" + measure + countType  + set
            }
        }
        return fieldPattern
    }

    static boolean evaluateColorSubCondition(Object columnValue, QueryOperatorEnum operator, value) {
        def toNumber = { Object o ->
            if (o instanceof Number) return o;
            String s = o.toString().replace("%", "")
            if (s.startsWith(".")) s = "0" + s;
            return Double.parseDouble(s)
        }
        def contains = { String cv, String v ->
            String lowerValue = cv?.toLowerCase()?.trim();
            if (!lowerValue) return false;
            if (value.contains(";")) {
                return value.toLowerCase().split(";").find { lowerValue.contains(it.trim()) }
            } else
                return lowerValue?.contains(value.toLowerCase())
        }

        def dateCompare = { Object cv, String v, comparator ->
            if (cv instanceof Date) {
                try {
                    Date dcv = cv
                    Date dv = Date.parse("dd-MMM-yyyy", value.toString())
                    return comparator(dcv, dv)
                } catch (e) {
                    //can not convert to date, thats ok
                    return false
                }
            }
            String sValue = v.toString()
            String sColumnValue = cv.toString()
            if (sValue.contains("-") && sValue.charAt(0).isDigit()) {
                try {
                    Date dcv
                    try {
                        dcv = Date.parse("dd-MMM-yyyy", sColumnValue)
                    } catch (e1) {
                        try {
                            String[] parts = sColumnValue.split("-Q")
                            if (parts.length != 2) throw new Exception();
                            int month = 3 * (Integer.parseInt(parts[1]) - 1)
                            if (month > 11) throw new Exception();
                            dcv = new Date(Integer.parseInt(parts[0]) - 1900, month, 1, 0, 0, 0)
                        } catch (e2) {
                            try {
                                dcv = Date.parse("yyyy", sColumnValue)
                            } catch (e3) {
                                try {
                                    dcv = Date.parse("'Week'ww-yyyy", sColumnValue)
                                } catch (e4) {
                                    try {
                                        dcv = Date.parse("yyyy-MM", sColumnValue)
                                    } catch (e5) {

                                    }
                                }
                            }
                        }
                    }
                    Date dv = Date.parse("dd-MMM-yyyy", sValue)
                    if (dcv)
                        return comparator(dcv, dv)
                } catch (e) {
                    //can not convert to date, thats ok
                }
            }
            return null
        }

        def eq = { Object cv, String v ->
            if (dateCompare(columnValue, value, { Date cv1, Date v1 -> return cv1.equals(v1) })) return true
            String lowerValue = cv?.toString().toLowerCase()?.trim()
            if (!lowerValue) return false;
            if (value.contains(";")) {
                return value.toLowerCase().split(";").find { lowerValue == it.trim() }
            } else
                return lowerValue == value.toLowerCase()
        }
        try {

            switch (operator) {
                case QueryOperatorEnum.CONTAINS:
                    return contains(columnValue.toString(), value.toString())
                case QueryOperatorEnum.DOES_NOT_CONTAIN:
                    return !contains(columnValue.toString(), value.toString())
                case QueryOperatorEnum.EQUALS:
                    return eq(columnValue, value.toString())
                case QueryOperatorEnum.NOT_EQUAL:
                    return !eq(columnValue, value.toString())
                case QueryOperatorEnum.LESS_THAN:
                    if (((columnValue instanceof Number) || NumberUtils.isParsable(columnValue.toString())) && ((value instanceof Number) || NumberUtils.isParsable(value.toString())))
                        return toNumber(columnValue) < toNumber(value)
                    return dateCompare (columnValue, value,{Date cv, Date v-> return cv.before(v)})
                case QueryOperatorEnum.LESS_THAN_OR_EQUAL:
                    if (((columnValue instanceof Number) || NumberUtils.isParsable(columnValue.toString())) && ((value instanceof Number) || NumberUtils.isParsable(value.toString())))
                        return toNumber(columnValue) <= toNumber(value)
                    return dateCompare (columnValue, value,{Date cv, Date v-> return cv.before(v)|| cv.equals(v)})
                case QueryOperatorEnum.GREATER_THAN:
                    if (((columnValue instanceof Number) || NumberUtils.isParsable(columnValue.toString())) && ((value instanceof Number) || NumberUtils.isParsable(value.toString())))
                        return toNumber(columnValue) > toNumber(value)
                    return dateCompare (columnValue, value,{Date cv, Date v-> return cv.after(v)})
                case QueryOperatorEnum.GREATER_THAN_OR_EQUAL:
                    if (((columnValue instanceof Number) || NumberUtils.isParsable(columnValue.toString())) && ((value instanceof Number) || NumberUtils.isParsable(value.toString())))
                        return toNumber(columnValue) >= toNumber(value)
                    return dateCompare (columnValue, value,{Date cv, Date v-> return cv.after(v)|| cv.equals(v)})
                case QueryOperatorEnum.IS_EMPTY:
                    return !columnValue
                case QueryOperatorEnum.IS_NOT_EMPTY:
                    return !!columnValue
                case QueryOperatorEnum.START_WITH:
                    return columnValue?.toString()?.startsWith(value.toString())
                case QueryOperatorEnum.DOES_NOT_START:
                    return !columnValue?.toString()?.startsWith(value.toString())
                case QueryOperatorEnum.ENDS_WITH:
                    return columnValue?.toString()?.endsWith(value.toString())
                case QueryOperatorEnum.DOES_NOT_END:
                    return !columnValue?.toString()?.endsWith(value.toString())
            }
        } catch (Exception e) {
            log.error("Error occured in CrosstabReportBuilder trying to evaluate condition to color cell" + e.getMessage())

        }
        return false;
    }
}
