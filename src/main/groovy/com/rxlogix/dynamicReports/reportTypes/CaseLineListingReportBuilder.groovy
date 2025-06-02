package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.Constants
import com.rxlogix.CustomMessageService
import com.rxlogix.config.*
import com.rxlogix.dynamicReports.Templates
import com.rxlogix.dynamicReports.customElements.formula.FormulaElementBuilder
import com.rxlogix.dynamicReports.reportTypes.common.GroupTextExpression
import com.rxlogix.dynamicReports.reportTypes.xlsx.ExcelFilteredCasesFormulaExpression
import com.rxlogix.dynamicReports.reportTypes.xlsx.ExcelFilteredRowsFormulaExpression
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.SortEnum
import com.rxlogix.enums.TemplateFieldTypeEnum
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.jasper.constant.JasperProperty
import net.sf.dynamicreports.report.base.column.DRColumn
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter
import net.sf.dynamicreports.report.builder.DynamicReports
import net.sf.dynamicreports.report.builder.FieldBuilder
import net.sf.dynamicreports.report.builder.SortBuilder
import net.sf.dynamicreports.report.builder.VariableBuilder
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.builder.expression.Expressions
import net.sf.dynamicreports.report.builder.expression.JasperExpression
import net.sf.dynamicreports.report.builder.grid.HorizontalColumnGridListBuilder
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.builder.style.ConditionalStyleBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.Calculation
import net.sf.dynamicreports.report.constant.Evaluation
import net.sf.dynamicreports.report.constant.GroupHeaderLayout
import net.sf.dynamicreports.report.constant.HyperLinkTarget
import net.sf.dynamicreports.report.constant.Markup
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.dynamicreports.report.definition.expression.DRIExpression
import net.sf.jasperreports.engine.JRVariable
import net.sf.jasperreports.engine.design.JasperDesign
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import java.awt.Color

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class CaseLineListingReportBuilder implements SpecificReportTypeBuilder, SpecificTemplateTypeBuilder {
    private static final String LAST_CASE_NUMBER = "lastCaseNumber"
    private static final String CASE_NUMBER_COUNT_VAR = "caseNumberCount"

    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    private FieldBuilder caseNumberField
    private FieldBuilder versionNumberField
    private List<ColumnGroupBuilder> pendingGroups

    @Override
    void createReport(ReportResult reportResult, JasperReportBuilder report, Map params, String lang) {
        ReportTemplate rptTemplateObj = (ReportTemplate) GrailsHibernateUtil.unwrapIfProxy(reportResult.template)
        ReportTemplate executedTemplateObj = (ReportTemplate) GrailsHibernateUtil.unwrapIfProxy(reportResult?.executedTemplateQuery?.executedTemplate)
        ExecutedCaseLineListingTemplate executedTemplate = (ExecutedCaseLineListingTemplate) (rptTemplateObj ?: executedTemplateObj)
        String caseNumberFieldName = reportResult.sourceProfile.caseNumberFieldName
        initReportFields(executedTemplate, report, caseNumberFieldName)
        processColumnGrouping(executedTemplate, report, params, lang)
        processReport(executedTemplate, report, params, lang)
    }

    @Override
    JasperDesign buildTemplate(ReportTemplate template, JasperReportBuilder report, Map params, String lang) {
        params.buildTemplate = true
        initReportFields(template, report)
        processColumnGrouping(template, report, params, lang)
        processReport(template, report, params, lang)
        return report.toJasperDesign()
    }

    @Override
    void createSubReport(ReportTemplate executedTemplate, JasperReportBuilder report, Map params, String lang) {
        initReportFields(executedTemplate, report, params.caseNumberFieldName)
        if(!params.linkSectionsByGrouping){
            processColumnGrouping(executedTemplate, report, params, lang)
        }
        processReport(executedTemplate, report, params, lang)
    }

    private void initReportFields(CaseLineListingTemplate executedTemplate, JasperReportBuilder report, String caseNumberFieldName = "masterCaseNum") {
        this.caseNumberField = createField(executedTemplate, report, caseNumberFieldName)
        this.versionNumberField = createField(executedTemplate, report, "masterVersionNum")
        this.pendingGroups = []
    }

    private FieldBuilder createField(CaseLineListingTemplate executedTemplate, JasperReportBuilder report, String fieldName) {
        List<ReportFieldInfo> allColumns = executedTemplate.allSelectedFieldsInfo
        int index = allColumns.findIndexOf { fieldName.equals(it.reportField.name) }

        //To check if CaseNumber(J) field is present
        if (index == -1) {
            fieldName = "masterCaseNumJ"
            index = allColumns.findIndexOf { fieldName.equals(it.reportField.name) }
        }

        if (index > -1) {
            ReportFieldInfo column = allColumns[index]
            List<String> fieldNameWithIndex = executedTemplate?.getFieldNameWithIndex()
            FieldBuilder field = field(fieldNameWithIndex[index], detectColumnType(column))
            report.addField(field)
            return field
        }
        return null
    }

    private void processReport(CaseLineListingTemplate executedTemplate, JasperReportBuilder report, Map params, String lang) {
        this.templateStyles = stl.templateStyles()
        def selectedColumns = executedTemplate.selectedFieldsFullInfo?.findAll {
            it.type == TemplateFieldTypeEnum.COLUMN_FIELD
        }
        if (params.rca) {
            selectedColumns.addAll([
                    [nameWithIndex: "dueInDays", type: TemplateFieldTypeEnum.SERVICE_COLUMN_FIELD, reportFieldInfo: new ReportFieldInfo(reportField: new ReportField([name: "dueInDays"]), renameValue: ViewHelper.getMessage("app.pcv.dueIn"))],
                    [nameWithIndex: "assignedToGroup", type: TemplateFieldTypeEnum.SERVICE_COLUMN_FIELD, reportFieldInfo: new ReportFieldInfo(reportField: new ReportField([name: "assignedToGroup"]), renameValue: ViewHelper.getMessage("app.label.assignedToGroup"))],
                    [nameWithIndex: "assignedToUser", type: TemplateFieldTypeEnum.SERVICE_COLUMN_FIELD, reportFieldInfo: new ReportFieldInfo(reportField: new ReportField([name: "assignedToUser"]), renameValue: ViewHelper.getMessage("app.label.assignedToUser"))],
                    [nameWithIndex: "workFlowState", type: TemplateFieldTypeEnum.SERVICE_COLUMN_FIELD, reportFieldInfo: new ReportFieldInfo(reportField: new ReportField([name: "workFlowState"]), renameValue: ViewHelper.getMessage("app.pcv.workflowState"))],
                    [nameWithIndex: "hasAttachments", type: TemplateFieldTypeEnum.SERVICE_COLUMN_FIELD, reportFieldInfo: new ReportFieldInfo(reportField: new ReportField([name: "attachment"]), renameValue: ViewHelper.getMessage("app.pcv.attachment"))]
            ])
        }
        if (executedTemplate?.hasStackedColumns()) {
            addStackedColumns(executedTemplate, selectedColumns, report, params, lang)
        } else {
            addCaseLineListingColumns(executedTemplate, selectedColumns, report, params, lang)
        }
        processColspans(executedTemplate, report, params, lang)
        if (params.outputFormat != ReportFormatEnum.XLSX.name()) {
            processSummary(executedTemplate, report, params)
        }
        pendingGroups.each {
            report.groupBy(it)
        }
    }

    List<TextColumnBuilder> addCaseLineListingColumns(CaseLineListingTemplate executedCaseLineListingTemplate, List<?> selectedColumns, JasperReportBuilder report, Map params, String lang) {
        List<TextColumnBuilder> columns = makeSelectedColumnsList(executedCaseLineListingTemplate, selectedColumns, report, params, lang)
        if (params.outputFormat != ReportFormatEnum.XLSX.name()) {
            adjustColumnWidth(selectedColumns*.reportFieldInfo, columns)
        }
        report.columns(*columns)
        return columns
    }

    private addStackedColumns(CaseLineListingTemplate executedCaseLineListingTemplate, List<?> selectedColumns, JasperReportBuilder report, Map params, String lang) {
        List<TextColumnBuilder> allColumns = makeSelectedColumnsList(executedCaseLineListingTemplate, selectedColumns, report, params, lang)

        if (params.outputFormat != ReportFormatEnum.XLSX.name()) {
            Map<Integer, Map<Integer, TextColumnBuilder>> columnGrids = new HashMap<>()
            Map<Integer, Map<Integer, ReportFieldInfo>> fieldInfoGrids = new HashMap<>()
            Map<Integer, Integer> minColumnsMap = new HashMap<>()
            def currentStackId = -1
            def rowIndex = -1
            def columnIndex = -1
            for (int i = 0; i < selectedColumns.size(); i++) {
                ReportFieldInfo fieldInfo = selectedColumns.get(i).reportFieldInfo
                TextColumnBuilder column = allColumns.get(i)
                if (fieldInfo.stackId > 0 && fieldInfo.stackId == currentStackId) {
                    // Stacked columns
                    rowIndex++
                } else {
                    // Non-stacked column or another stacked column
                    columnIndex++
                    rowIndex = 0
                }
                Map<Integer, TextColumnBuilder> columnRow = columnGrids.get(rowIndex)
                if (columnRow == null) {
                    columnRow = new HashMap<>()
                    columnGrids.put(rowIndex, columnRow)
                }
                columnRow.put(columnIndex, column)

                Map<Integer, ReportFieldInfo> fieldInfoRow = fieldInfoGrids.get(rowIndex)
                if (fieldInfoRow == null) {
                    fieldInfoRow = new HashMap<>()
                    fieldInfoGrids.put(rowIndex, fieldInfoRow)
                }
                fieldInfoRow.put(columnIndex, fieldInfo)

                Integer minColumns = fieldInfo.reportField.fixedWidth
                if (minColumns) {
                    if (minColumnsMap.get(columnIndex) != null) {
                        minColumns = Math.max(minColumns, minColumnsMap.get(columnIndex))
                    }
                    minColumnsMap.put(columnIndex, minColumns)
                }
                currentStackId = fieldInfo.stackId
            }
            HorizontalColumnGridListBuilder horizontalColumnGridListBuilder = grid.horizontalColumnGridList()
            for (int i = 0; i < columnGrids.size(); i++) {
                Map<Integer, TextColumnBuilder> row = columnGrids.get(i)
                if (i > 0) {
                    horizontalColumnGridListBuilder.newRow()
                }
                for (int j = 0; j < columnGrids.get(0).size(); j++) {
                    TextColumnBuilder column = row.get(j)
                    if (column == null) {
                        column = col.column(exp.jasperSyntax("\"\""))
                        column.setStyle(stl.style(Templates.columnStyle).setTopBorder(Templates.emptyBorderLine).setBottomBorder(Templates.emptyBorderLine))
                        row.put(j, column)
                        allColumns.add(column)
                    } else {
                        StyleBuilder columnStyle = getOrCreateColumnStyle(column, params)
                        if (i > 0) {
                            columnStyle.conditionalStyles(stl.conditionalStyle(exp.jasperSyntax("\$F{${column.name}} == null || \"\".equals(\$F{${column.name}})")).setTopBorder(Templates.emptyBorderLine))
                        } else {
                            columnStyle.setBottomBorder(Templates.emptyBorderLine)
                        }

                    }
                    Integer minColumns = minColumnsMap.get(j)
                    if (minColumns != null) {
                        column.setMinColumns(minColumns)
                    }
                    horizontalColumnGridListBuilder.add(column)
                }
            }
            report.columnGrid(horizontalColumnGridListBuilder)
            adjustColumnWidth(fieldInfoGrids, columnGrids)
        }
        report.columns(*allColumns)
    }

    /**
     * The method adds selected columns to list. Also it adds suppressing for selected columns
     * @param executedCaseLineListingTemplate Report template
     * @param report Report builder
     * @param params Report params
     * @param columnIndexOffset offset for adding columns
     * @return List of
     */
    protected List makeSelectedColumnsList(CaseLineListingTemplate executedCaseLineListingTemplate, List selectedColumns, JasperReportBuilder report, Map params, String lang) {
        Boolean firstSuppressionColumnGrouped = false
        List columns = []
        List allFieldsFullInfo = executedCaseLineListingTemplate.getSelectedFieldsFullInfo()
        SuppressEmptyRowPrintWhenExpression emptyRowPrintExp = new SuppressEmptyRowPrintWhenExpression()
        for (int i = 0; i < selectedColumns?.size(); i++) {
            def reportFieldInfo = selectedColumns.get(i).reportFieldInfo
            ReportField reportField = reportFieldInfo.reportField
            String columnLabel = customMessageService.getMessage("app.reportField." + reportField.name, null, "", Locale.ENGLISH)
            if (reportFieldInfo.renameValue) {
                columnLabel = reportFieldInfo.renameValue
            }

            String columnValue = selectedColumns.get(i).nameWithIndex

            TextColumnBuilder<String> column = createColumn(columnLabel, columnValue, reportFieldInfo, params)
            String dateFormat
            Boolean isFollowBodyField = (reportField?.transform?.equals(Constants.FOLLOWUP_QUERY_BODY_PVCM) ? true : false)
            if(isFollowBodyField) {
                getOrCreateColumnStyle(column, params).setMarkup(Markup.HTML)
            }
            if (!reportFieldInfo.customExpression &&
                    !reportFieldInfo.commaSeparatedValue &&
                    !reportFieldInfo.blindedValue &&
                    reportField.isDate()) {
                dateFormat = reportField.getDateFormat(lang)
                if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
                    column.setValueFormatter(new DateValueColumnFormatter(dateFormat))
                }
                column.setPattern(dateFormat)
            }
            if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
                if(isFollowBodyField) {
                    column.setStyle(Templates.columnStyleMarkupHTML)
                }else {
                    column.setStyle(Templates.columnStyleXLSX)
                    column.addProperty("net.sf.jasperreports.export.xls.auto.fit.row", "true")
                }
                if (reportFieldInfo.reportField.isUrlField) {
                    column.setHyperLink(DynamicReports.hyperLink(createFieldHyperlink(column.name)))
                }
            } else {
                if (!params.buildTemplate && reportFieldInfo.suppressRepeatingValues) {
                    ColumnGroupBuilder columnGroup = grp.group(createColumn(columnLabel, columnValue, reportFieldInfo, params))
                            .setHeaderLayout(GroupHeaderLayout.EMPTY)
                            .setHeaderStyle(Templates.emptyPaddingStyle)
                            .setFooterStyle(Templates.emptyPaddingStyle)
                            .setPadding(0)
                    def columnStyle = getOrCreateColumnStyle(column, params)
                    columnStyle = getSuppressRepeatingColumnStyle(columnStyle, columnGroup, column.name)
                    column.setValueFormatter(new SuppressRepeatingColumnFormatter(new SuppressRepeatingColumnPrintWhenExpression(columnGroup, column.name)))
                            .setStyle(columnStyle)
                    emptyRowPrintExp.addSuppressExpression(new SuppressRepeatingColumnPrintWhenExpression(columnGroup, column.name))
                    if (!firstSuppressionColumnGrouped) {
                        DRIExpression<Boolean> skipFirstGroupExp = exp.jasperSyntax("\$V{REPORT_COUNT} > 0")
                        columnGroup.setHeaderPrintWhenExpression(skipFirstGroupExp)
                        columnGroup.setHeaderStyle(stl.style(Templates.groupHeaderStyle).setTopPadding(0))
                        report.groupBy(columnGroup)
                        firstSuppressionColumnGrouped = true
                    } else {
                        pendingGroups.add(columnGroup)
                    }
                } else {
                    emptyRowPrintExp.addSuppressExpression(new SuppressEmptyValuePrintWhenExpression(column.name))
                }
                // Add Case number hyperlink for CIOMS I Form
                if ((!params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) &&
                        column.name.equals(caseNumberField?.name) &&
                        !reportFieldInfo.customExpression) {
                    column.setHyperLink(DynamicReports.hyperLink(createCaseNumberHyperlinkExpression(column.name, versionNumberField?.name)).setTarget(HyperLinkTarget.BLANK))
                }

                if (reportFieldInfo.reportField.isUrlField) {
                    StyleBuilder columnStyle = getOrCreateColumnStyle(column, params)
                    columnStyle.setMarkup(Markup.HTML)
                    if(!executedCaseLineListingTemplate.interactiveOutput) {
                        column.setValueFormatter(new MultipleHyperLinkColumnFormatter())
                    }
                    columnStyle.setForegroundColor(new Color(51, 122, 183))
                }
            }
            if (reportFieldInfo.colorConditions) {
                appendConditionalFormatting(reportFieldInfo.colorConditions, column, columnValue, allFieldsFullInfo, dateFormat, params, null)
            }
            Integer minColumns = reportFieldInfo.reportField.fixedWidth
            if (minColumns) {
                column.setMinColumns(minColumns)
            }
            columns.add(column)
        }
        if (!params.buildTemplate) {
            report.setDetailPrintWhenExpression(emptyRowPrintExp)
        }
        return columns
    }

    private ConditionalStyleBuilder[] getConditionsStylesList(String currentField, colorConditionsForField, List fields) {
        List<ConditionalStyleBuilder> conditions = colorConditionsForField?.collect {
            if (it.color) {
                stl.conditionalStyle(new ColorConditionCell(currentField, it.conditions, fields))
                        .setBackgroundColor(new Color(Integer.valueOf(it.color.substring(1, 3), 16),
                                Integer.valueOf(it.color.substring(3, 5), 16),
                                Integer.valueOf(it.color.substring(5, 7), 16)))
            } else null
        }
        return conditions.findAll { it }
    }

    class ColorConditionCell extends AbstractSimpleExpression<Boolean> {
        String field
        List conditions
        List fields

        ColorConditionCell(String field, List conditions, List fields) {
            this.field = field
            this.fields = fields
            this.conditions = conditions
        }

        @Override
        Boolean evaluate(ReportParameters reportParameters) {
            Boolean result = true
            for (int i = 0; i < conditions.size(); i++) {
                String fieldPattern = fieldNameToNameWitIndex(fields, conditions[i].field)

                result = CrosstabReportBuilder.evaluateColorSubCondition(reportParameters.getValue(fieldPattern), conditions[i].operator as QueryOperatorEnum, conditions[i].value)
                if (!result) return false
            }
            return result
        }
    }

    String fieldNameToNameWitIndex(List fields, field) {
        fields.find {
            String label = it.reportFieldInfo.renameValue ?: it.reportFieldInfo.reportField.getDisplayName()
            label.trim() == field.trim()
        }.nameWithIndex
    }


    class ConditionalColumnFormatter extends AbstractValueFormatter<Object, Object> {
        String field
        List fieldConditions
        List fields
        String dateFormat
        Boolean supportIcon

        ConditionalColumnFormatter(String field, List fieldConditions, List fields, String dateFormat, String format) {
            this.field = field
            this.fieldConditions = fieldConditions
            this.fields = fields
            this.dateFormat = dateFormat
            supportIcon = !format || (format in [ReportFormatEnum.HTML.name(), ReportFormatEnum.PDF.name()])
        }

        @Override
        Object format(Object obj, ReportParameters reportParameters) {
            if (!obj) {
                return obj
            }
            if (fieldConditions) {
                String text = obj.toString()
                if(dateFormat && (obj instanceof Date)) text = ((Date)obj).format(dateFormat)
                for (int j = 0; j < fieldConditions.size(); j++) {
                    Map oneFieldCondition = fieldConditions[j];
                    Boolean conditionResult = false
                    for (int i = 0; i < oneFieldCondition.conditions.size(); i++) {
                        String fieldPattern = fieldNameToNameWitIndex(fields, oneFieldCondition.conditions[i].field)
                        conditionResult = CrosstabReportBuilder.evaluateColorSubCondition(reportParameters.getValue(fieldPattern), oneFieldCondition.conditions[i].operator as QueryOperatorEnum, oneFieldCondition.conditions[i].value)
                        if (!conditionResult) break
                    }
                    if (conditionResult && oneFieldCondition.icon) {
                        return CrosstabReportBuilder.formatConditionalCell(oneFieldCondition.icon, text, supportIcon)
                    }
                }
            }
            return obj
        }
    }

    private addGroupingColumns(CaseLineListingTemplate executedCaseLineListingTemplate,
                               groupingColumns, List<SortBuilder> sortColumnBuilderList,
                               List<ColumnGroupBuilder> columnGroupList, boolean addHyperLinks, String lang, Map params) {
        List<ColumnGroupBuilder> columnGroups = []
        List<ComponentBuilder> groupHeader = []
        boolean groupedByCaseNumber = false
        for (int i = 0; i < groupingColumns?.size(); i++) {
            ReportFieldInfo reportFieldInfo = groupingColumns.get(i).reportFieldInfo
            ReportField reportField = reportFieldInfo.reportField
            String columnLabel = customMessageService.getMessage("app.reportField." + reportField.name)
            if (reportFieldInfo.renameValue) {
                columnLabel = reportFieldInfo.renameValue
            }
            TextColumnBuilder column = createColumn(columnLabel, groupingColumns.get(i).nameWithIndex, reportFieldInfo, params)
            if (!reportFieldInfo.customExpression &&
                    !reportFieldInfo.commaSeparatedValue &&
                    !reportFieldInfo.blindedValue &&
                    reportField.isDate()) {
                column.setPattern(reportField.getDateFormat(lang))
            }
            if (!reportFieldInfo.advancedSorting) {
                sortColumnBuilderList.add(reportFieldInfo.sort == SortEnum.DESCENDING ? desc(column) : asc(column))
            }
            ColumnGroupBuilder singleColumnGroup = grp.group(column)
            columnGroups.add(singleColumnGroup)

            if (!params.buildTemplate) {
                singleColumnGroup.setHeaderLayout(GroupHeaderLayout.EMPTY)
                if (column.name.equals(caseNumberField?.name)) {
                    groupedByCaseNumber = true
                }

                if (i == groupingColumns?.size() - 1) {
                    singleColumnGroup.showColumnHeaderAndFooter()
                    // A hack for tree like bookmarks
                    columnGroups.reverse().eachWithIndex { ColumnGroupBuilder group, int groupIndex ->
                        TextFieldBuilder<String> bookmarkHeader = cmp.text(" ").setWidth(1)
                        bookmarkHeader.setAnchorName(new GroupAnchorNameExpression(group))
                        bookmarkHeader.setBookmarkLevel(columnGroups.size() - groupIndex + 1)
                        bookmarkHeader.setPrintWhenExpression(new SuppressRepeatingBookmarkPrintWhenExpression(columnGroups.subList(0, columnGroups.size() - groupIndex)))
                        groupHeader.add(bookmarkHeader)
                    }
                    VariableBuilder<Long> caseNumberCount
                    def groupHeaderExpression = new GroupTextExpression(columnGroups, groupingColumns*.reportFieldInfo, executedCaseLineListingTemplate.hideTotalRowCount)
                    if (executedCaseLineListingTemplate.columnShowSubTotal) {
                        groupHeaderExpression.addExpression(Expressions.groupRowNumber(singleColumnGroup))
                        if (caseNumberField) {
                            groupHeaderExpression.addExpression(variable(caseNumberField, Calculation.DISTINCT_COUNT)
                                    .setInitialValueExpression(exp.jasperSyntax("Long.valueOf(0);", Long.class))
                                    .setResetGroup(singleColumnGroup))
                        }
                    }
                    StyleBuilder groupHeaderStyle = stl.style(Templates.boldStyle).bold()
                    def groupHeaderComponent = cmp.text(groupHeaderExpression)
                            .setStyle(groupHeaderStyle)
                    // Add Case number hyperlink for CIOMS I Form
                    if (addHyperLinks &&
                            !reportFieldInfo.customExpression &&
                            groupedByCaseNumber) {
                        groupHeaderComponent.setHyperLink(DynamicReports.hyperLink(createCaseNumberHyperlinkExpression(caseNumberField?.name, versionNumberField?.name)))
                    }
                    // A hack for fixing the https://rxlogixdev.atlassian.net/browse/PVR-4747 bug, It's Jasper Reports library bug
                    if (!dynamicReportService.isInPrintMode(params)) {
                        groupHeaderComponent.setMinRows(5)
                    }
                    groupHeader.add(groupHeaderComponent)
                    singleColumnGroup.addHeaderComponent(cmp.horizontalFlowList(*groupHeader.reverse()))
                    singleColumnGroup.reprintHeaderOnEachPage()
                    singleColumnGroup.setMinHeightToStartNewPage(60)
                }
                columnGroupList.add(singleColumnGroup)
            } else {
                singleColumnGroup.setHeaderLayout(GroupHeaderLayout.TITLE_AND_VALUE)
            }
        }

        [sortColumnBuilderList, columnGroupList]
    }

    /**
     * The method adds colspan columns to list
     * @param executedCaseLineListingTemplate Report template
     * @param columnSpanColumns Colspan columns list from report template
     * @param colspanList Colspan column list
     * @return
     */
    private addColspanColumns(CaseLineListingTemplate executedCaseLineListingTemplate,
                              columnSpanColumns, JasperReportBuilder report, String lang,
                              Map params) {
        VerticalListBuilder colspanList = cmp.verticalList().setStyle(Templates.colspanStyle)
        List<ColumnGroupBuilder> singleColumnGroups = []
        List allFieldsFullInfo = executedCaseLineListingTemplate.getSelectedFieldsFullInfo()
        columnSpanColumns.eachWithIndex { reportFieldFullInfo, int i ->
            ReportFieldInfo reportFieldInfo = reportFieldFullInfo.reportFieldInfo
            ReportField reportField = reportFieldInfo.reportField
            String columnLabel = customMessageService.getMessage("app.reportField." + reportField.name)
            String columnValue = reportFieldFullInfo.nameWithIndex
            if (columnLabel.contains('(J)') || (columnValue.contains('J_'))) {
                columnLabel = customMessageService.getMessage("app.reportField." + reportFieldInfo.reportField.name, null, "app.reportField." + reportFieldInfo.reportField.name, Locale.JAPANESE)
            } else {
                columnLabel = customMessageService.getMessage("app.reportField." + reportFieldInfo.reportField.name, null, "app.reportField." + reportFieldInfo.reportField.name, Locale.ENGLISH)
            }
            if (reportFieldInfo.renameValue) {
                columnLabel = reportFieldInfo.renameValue
            }
            TextColumnBuilder column = createColumn(columnLabel, columnValue, reportFieldInfo, params)

            HorizontalListBuilder singleColspan = cmp.horizontalFlowList()
            singleColspan.add(cmp.text(exp.jasperSyntax("\"${columnLabel}\"")).setStyle(Templates.boldStyle).setMinDimension(20, 10))
            StyleBuilder columnStyle = Templates.columnStyle
            TextFieldBuilder comp = cmp.text(column.getColumn())
            String dateFormat = reportField.getDateFormat(lang)
            if (!reportFieldInfo.customExpression &&
                    !reportFieldInfo.commaSeparatedValue &&
                    !reportFieldInfo.blindedValue &&
                    reportField.isDate()) {
                comp.setValueFormatter(new DateValueColumnFormatter(dateFormat))
                comp.setPattern(dateFormat)
            }
            if (reportFieldInfo.reportField.isUrlField) {
                columnStyle = getOrCreateColumnStyle(column, params)
                columnStyle.setMarkup(Markup.HTML)
                if (!executedCaseLineListingTemplate.interactiveOutput) {
                    comp.setValueFormatter(new MultipleHyperLinkColumnFormatter())
                }
                columnStyle.setForegroundColor(new Color(51, 122, 183))
            }
            comp.setStyle(columnStyle)
            singleColspan.add(comp)

            if (!params.buildTemplate && reportFieldInfo.suppressRepeatingValues) {
                singleColspan.setStyle(Templates.colspanStyle)

                ColumnGroupBuilder singleColumnGroup = grp.group(createColumn(columnLabel, columnValue, reportFieldInfo, params))
                        .setHeaderLayout(GroupHeaderLayout.EMPTY)
                        .setHeaderStyle(Templates.emptyPaddingStyle)
                        .setFooterStyle(Templates.emptyPaddingStyle)
                        .setPadding(0)
                singleColumnGroup.addFooterComponent(singleColspan)
                singleColumnGroups.add(singleColumnGroup)
            } else {
                colspanList.add(singleColspan)
            }
            if (reportFieldInfo.colorConditions) {
                appendConditionalFormatting(reportFieldInfo.colorConditions, column, columnValue, allFieldsFullInfo, dateFormat, params, comp)
            }
        }
        singleColumnGroups.reverse().each {
            report.groupBy(it)
        }
        return colspanList
    }

    private appendConditionalFormatting(String colorConditions, column, columnValue, allFieldsFullInfo, dateFormat, params, comp) {
        def columnStyle = getOrCreateColumnStyle(column, params)
        List colorConditionsForField = JSON.parse(colorConditions)
        ConditionalStyleBuilder[] stylesArray = getConditionsStylesList(columnValue, colorConditionsForField, allFieldsFullInfo)
        StyleBuilder columnStyleBulder = stl.style(columnStyle)
        columnStyleBulder.addConditionalStyle(stylesArray)
        columnStyleBulder.setMarkup(Markup.HTML)
        def obj = comp ?: column
        obj.setStyle(columnStyleBulder)
        obj.setValueFormatter(new ConditionalColumnFormatter(columnValue, colorConditionsForField, allFieldsFullInfo, dateFormat, params.outputFormat))
    }

    protected void processColumnGrouping(CaseLineListingTemplate executedCaseLineListingTemplate, JasperReportBuilder report, Map params, String lang) {
        def allColumns = executedCaseLineListingTemplate?.selectedFieldsFullInfo
        def selectedColumns = allColumns?.findAll { it.type == TemplateFieldTypeEnum.COLUMN_FIELD }
        def groupingColumns = allColumns?.findAll { it.type == TemplateFieldTypeEnum.GROUPING_FIELD }
        int columnLength = selectedColumns.size()

        if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
            //Flatten for Excel
            addCaseLineListingColumns(executedCaseLineListingTemplate, groupingColumns, report, params, lang)
        } else {
            List columnGroupList = new ArrayList<ColumnGroupBuilder>()
            List sortColumnBuilderList = new ArrayList<SortBuilder>()
            boolean addHypeLinks = !params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()

            (sortColumnBuilderList, columnGroupList) = addGroupingColumns(executedCaseLineListingTemplate, groupingColumns,
                    sortColumnBuilderList, columnGroupList, addHypeLinks, lang, params)

            if (columnGroupList) {
                report.setShowColumnTitle(false)
                report.groupBy(*columnGroupList)
                report.sortBy(*sortColumnBuilderList)
            }

            if (executedCaseLineListingTemplate.pageBreakByGroup) {
                for (ColumnGroupBuilder columnGroupBuilder : columnGroupList) {
                    columnGroupBuilder.setStartInNewPage(true)
                }
            }
        }
    }

    protected void processColspans(CaseLineListingTemplate executedCaseLineListingTemplate, JasperReportBuilder report, Map params, String lang) {

        def columnSpanColumns = executedCaseLineListingTemplate?.selectedFieldsFullInfo?.findAll {
            it.type == TemplateFieldTypeEnum.ROW_COLUMN_FIELD
        }
        if (columnSpanColumns) {
            if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
                //Flatten for Excel
                List<TextColumnBuilder> columns = addCaseLineListingColumns(executedCaseLineListingTemplate, columnSpanColumns, report, params, lang)
                // Extending columns width to avoiding performance problems
                columns*.setMinColumns(MIN_COLSPAN_COLUMNS_WIDTH_XLSX)
            } else {
                VerticalListBuilder colspanList = addColspanColumns(executedCaseLineListingTemplate, columnSpanColumns, report, lang, params)
                report.addDetailFooter(colspanList)
            }
        }
    }

    protected void processSummary(CaseLineListingTemplate executedCaseLineListingTemplate, JasperReportBuilder report, Map params) {
        VerticalListBuilder reportSummaryList = getSummaryList(executedCaseLineListingTemplate, report, params)
        report.setSummaryWithPageHeaderAndFooter(true)
        report.addSummary(reportSummaryList)
    }

    VerticalListBuilder getSummaryList(CaseLineListingTemplate executedCaseLineListingTemplate, JasperReportBuilder report, Map params) {
        VerticalListBuilder reportSummaryList = cmp.verticalList()
        def summaryStyle = Templates.boldStyle
        if (executedCaseLineListingTemplate.columnShowTotal) {
            // Total record count
            if (!executedCaseLineListingTemplate.hideTotalRowCount) {
                String totalRowsNumberLabel = customMessageService.getMessage("app.label.totalRowsNumber")
                reportSummaryList.add(cmp.text(createTotalRowNumberExpression(totalRowsNumberLabel)).setStyle(summaryStyle).setEvaluationTime(Evaluation.REPORT))
            }
            // Total unique case numbers count
            if (caseNumberField) {
                JasperExpression<String> lasCaseNumberExpression = new JasperExpression<>("\$F{${caseNumberField.name}} != null &&  \$F{${caseNumberField.name}}.trim().length() > 0 ?  \$F{${caseNumberField.name}} : \$V{${LAST_CASE_NUMBER}}", String.class)
                VariableBuilder<Long> lastCaseNumber = variable(LAST_CASE_NUMBER, lasCaseNumberExpression, Calculation.NOTHING)
                report.addVariable(lastCaseNumber)
                VariableBuilder<Long> caseNumberCount = variable(CASE_NUMBER_COUNT_VAR, new JasperExpression<String>("\$V{${LAST_CASE_NUMBER}}", String.class), Calculation.DISTINCT_COUNT)
                caseNumberCount.setInitialValueExpression(exp.jasperSyntax("Long.valueOf(0);", Long.class))
                report.addVariable(caseNumberCount)
                String totalCaseNumberLabel = ViewHelper.getMessage("app.label.totalCaseNumber")
                reportSummaryList.add(cmp.text(createTotalCaseNumberExpression(totalCaseNumberLabel)).setStyle(summaryStyle).setEvaluationTime(Evaluation.REPORT))
            }
        }
        return reportSummaryList
    }

    private static StyleBuilder getSuppressRepeatingColumnStyle(StyleBuilder columnTemplateStyle, ColumnGroupBuilder singleColumnGroup, String columnName) {
        stl.style(columnTemplateStyle)
                .setName("SuppressRepeating_${columnName}")
                .setBottomBorder(Templates.emptyBorderLine)
                .conditionalStyles(stl.conditionalStyle(new SuppressRepeatingColumnPrintWhenExpression(singleColumnGroup, columnName)).setTopBorder(Templates.emptyBorderLine))
    }

    /**
     * Adjustment of column width for plain columns
     */
    private def adjustColumnWidth(List<ReportFieldInfo> fieldsInfo, List<TextColumnBuilder> columns) {
        def columnWidthList = fieldsInfo.collect { it.columnWidth }
        def predefinedWidthCount = columnWidthList.findAll({ it != ReportFieldInfo.AUTO_COLUMN_WIDTH }).size()
        if (predefinedWidthCount > 0) {
            def normalizationFactor = getNormalizationFactor(columnWidthList)
            def autoColumnWidth = getAutoColumnWidth(columnWidthList)
            fieldsInfo.eachWithIndex { ReportFieldInfo entry, int i ->
                TextColumnBuilder column = columns.get(i)
                BigDecimal columnWidth = entry.columnWidth == ReportFieldInfo.AUTO_COLUMN_WIDTH ? autoColumnWidth : entry.columnWidth / normalizationFactor
                column.setWidth(columnWidth.intValue())
            }
        }
    }

    /**
     * Adjustment of column width for stacked columns
     */
    private def adjustColumnWidth(Map<Integer, Map<Integer, ReportFieldInfo>> fieldInfoGrids, Map<Integer, Map<Integer, TextColumnBuilder>> columnGrids) {
        def columnWidthMap = [:]
        fieldInfoGrids.entrySet().each { rowEntry ->
            Map<Integer, ReportFieldInfo> fieldInfoRow = rowEntry.value
            fieldInfoRow.entrySet().each { fieldInfoEntry ->
                ReportFieldInfo fieldInfo = fieldInfoEntry.value
                def columnWidth = columnWidthMap.get(fieldInfoEntry.key)
                if (!columnWidth || fieldInfo.columnWidth > columnWidth) {
                    columnWidthMap.put(fieldInfoEntry.key, fieldInfo.columnWidth)
                }
            }
        }
        def predefinedWidthCount = columnWidthMap.findAll({ it.getValue() != ReportFieldInfo.AUTO_COLUMN_WIDTH }).size()
        if (predefinedWidthCount > 0) {
            def autoColumnWidth = getAutoColumnWidth(columnWidthMap.values())
            def normalizationFactor = getNormalizationFactor(columnWidthMap.values())
            columnWidthMap.entrySet().each { columnWidthEntry ->
                BigDecimal columnWidth = columnWidthEntry.value == ReportFieldInfo.AUTO_COLUMN_WIDTH ? autoColumnWidth : columnWidthEntry.value / normalizationFactor
                columnGrids.values().each {
                    it.get(columnWidthEntry.key)?.setWidth(columnWidth.intValue())
                }
            }
        }
    }

    private DRIExpression<String> createTotalRowNumberExpression(String label) {
        return exp.jasperSyntax("\"${label}: \" + \$V{${JRVariable.REPORT_COUNT}}", String.class)
    }

    private DRIExpression<String> createTotalCaseNumberExpression(String label) {
        return exp.jasperSyntax("\"${label}: \" + \$V{${CASE_NUMBER_COUNT_VAR}}", String.class)
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

    DRIExpression<String> createCaseNumberHyperlinkExpression(String caseNumberFieldName, String versionNumberFieldName) {
        def caseNumber = caseNumberFieldName ? "\$F{${caseNumberFieldName}}" : "\"\""
        def versionNumber = versionNumberFieldName ? "\$F{${versionNumberFieldName}}" : "\"\""
        def hyperlinkExpression = grailsLinkGenerator.link(
                controller: 'report',
                action: 'drillDown')
        return exp.jasperSyntax("\"${hyperlinkExpression}?caseNumber=\" + ${caseNumber} + \"&versionNumber=\" + ${versionNumber}", String.class)
    }

    DRIExpression<String> createFieldHyperlink(String fieldName) {
        def field = fieldName ? "\$F{${fieldName}}" : "\"\""
        return exp.jasperSyntax(field, String.class)
    }

    void processReportFilter(CaseLineListingTemplate executedTemplate, JasperReportBuilder report, Map params, boolean hasFooter) {
        if (executedTemplate.columnShowTotal && params.outputFormat == ReportFormatEnum.XLSX.name()) {
            VerticalListBuilder reportSummaryList = getSummaryList(executedTemplate, report, params)
            report.addPageHeader(reportSummaryList)
            List<DRColumn> columns = report.getReport().getColumns()
            if (columns.size() > 0) {
                columns.first().addTitlePropertyExpression(Expressions.property("net.sf.jasperreports.export.xls.auto.filter", "Start"))
                columns.last().addTitlePropertyExpression(Expressions.property("net.sf.jasperreports.export.xls.auto.filter", "End"))
            }
            def summaryStyle = Templates.boldStyle
            String filteredRowsLabel = customMessageService.getMessage("app.label.filteredRowsNumber")
            if (!executedTemplate.hideTotalRowCount) {
                FormulaElementBuilder filteredRowsField = new FormulaElementBuilder()
                        .setText(createTotalRowNumberExpression(filteredRowsLabel))
                        .setStyle(summaryStyle)
                        .setHeight(15)
                        .addProperty(JasperProperty.EXPORT_XLS_CELL_FORMULA, new ExcelFilteredRowsFormulaExpression(filteredRowsLabel, executedTemplate.hideTotalRowCount, hasFooter))
                report.addPageHeader(filteredRowsField)
            }
            if (caseNumberField) {
                String filteredCasesLabel = customMessageService.getMessage("app.label.filteredCaseNumber")

                int caseNumberFieldIndex = executedTemplate.fieldNameWithIndex.indexOf(caseNumberField.name)
                if (executedTemplate.groupingList) {
                    ReportFieldInfo reportFieldInfo = executedTemplate.allSelectedFieldsInfo[caseNumberFieldIndex]
                    caseNumberFieldIndex = executedTemplate.groupingList.reportFieldInfoList.indexOf(reportFieldInfo)
                    if (caseNumberFieldIndex == -1) {
                        caseNumberFieldIndex = executedTemplate.groupingList.reportFieldInfoList.size() + executedTemplate.columnList.reportFieldInfoList.indexOf(reportFieldInfo)
                    }
                }

                FormulaElementBuilder filteredCasesField = new FormulaElementBuilder()
                        .setText(createTotalCaseNumberExpression(filteredCasesLabel))
                        .setStyle(summaryStyle)
                        .setHeight(15)
                        .addProperty(JasperProperty.EXPORT_XLS_CELL_FORMULA, new ExcelFilteredCasesFormulaExpression(filteredCasesLabel, caseNumberFieldIndex, executedTemplate.hideTotalRowCount, hasFooter))
                        .addProperty("net.sf.jasperreports.export.xls.formula.isArray", "true")
                report.addPageHeader(filteredCasesField)
            }
        }
    }
}
