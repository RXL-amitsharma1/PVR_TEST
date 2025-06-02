package com.rxlogix.dynamicReports.reportTypes


import com.rxlogix.CustomMessageService
import com.rxlogix.config.*
import com.rxlogix.dynamicReports.ReportBuilder
import com.rxlogix.dynamicReports.Templates
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.builder.DynamicReports
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.ComponentBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.builder.expression.JasperExpression
import net.sf.dynamicreports.report.builder.expression.ValueExpression
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.constant.GroupHeaderLayout
import net.sf.dynamicreports.report.constant.Markup
import net.sf.dynamicreports.report.constant.SplitType
import net.sf.dynamicreports.report.constant.WhenNoDataType
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.dynamicreports.report.definition.datatype.DRIDataType
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.design.JasperDesign
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import grails.converters.JSON

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class TemplateSetReportBuilder implements SpecificReportTypeBuilder, SpecificTemplateTypeBuilder {

    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    @Override
    void createReport(ReportResult reportResult, JasperReportBuilder report, Map params, String lang) {
        ExecutedTemplateSet executedTemplate = (ExecutedTemplateSet) reportResult?.executedTemplateQuery?.executedTemplate
        ReportTemplate mainExecutedTemplate = executedTemplate.nestedTemplates.first()
        mainExecutedTemplate = GrailsHibernateUtil.unwrapIfProxy(mainExecutedTemplate) // When exporting entire report -> template set templates are coming as proxy obj
        TemplateSetCsvDataSource mainDataSource = createDataSource(reportResult, executedTemplate.linkSectionsByGrouping?getGroupingColumnName(mainExecutedTemplate):null)
        def subreports = []
        JSONObject crossTabHeaderMap = (JSONObject) JSON.parse(reportResult.data.crossTabHeader?:"{}")
        for (ReportTemplate executedNestedTemplate : executedTemplate.nestedTemplates) {
            executedNestedTemplate = GrailsHibernateUtil.unwrapIfProxy(executedNestedTemplate)
            List<String> columnNames
            JSONArray crossTabHeaders
            if (executedNestedTemplate instanceof CaseLineListingTemplate) {
                columnNames = executedNestedTemplate.fieldNameWithIndex
            } else if (executedNestedTemplate instanceof DataTabulationTemplate) {
                crossTabHeaders = crossTabHeaderMap.getJSONArray(String.valueOf(executedNestedTemplate.id))
                columnNames = crossTabHeaders.collect { JSONObject crossTabHeader ->
                    crossTabHeader.keySet()[0]
                }
                columnNames.add("ID")
            }

            JasperReportBuilder subreport = DynamicReports.report()
            subreport.setTemplate(Templates.reportTemplate)
            subreport.setWhenNoDataType(executedTemplate.excludeEmptySections ? WhenNoDataType.NO_PAGES : WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
            setSubReportHeader(executedNestedTemplate.name, params, subreport)
            params.outputFormat=="XLSX" ? subreport.setIgnorePageWidth(true) : subreport.setIgnorePageWidth(false)
            params.caseNumberFieldName = reportResult?.sourceProfile.caseNumberFieldName
            params.crossTabHeader = crossTabHeaders
            params.linkSectionsByGrouping = executedTemplate.linkSectionsByGrouping
            SpecificReportTypeBuilder reportTypeBuilder = ReportBuilder.getSpecificReportBuilder(executedNestedTemplate)
            reportTypeBuilder.createSubReport(executedNestedTemplate, subreport, params, lang)

            subreports.add(cmp.subreport(subreport)
                    .setMinHeight(1)
                    .setDataSource(new SubreportDataSource(executedNestedTemplate, mainDataSource, columnNames)))
        }
        if (executedTemplate.linkSectionsByGrouping) {
            List<TextColumnBuilder> groupingColumns = getGroupingColumns(mainExecutedTemplate, params, lang)
            processColumnGrouping(groupingColumns, report, params.outputFormat == "XLSX")
        }
        report.setDataSource(mainDataSource)
        report.detail(*subreports)
    }

    @Override
    void createSubReport(ReportTemplate executedTemplate, JasperReportBuilder report, Map params, String lang) {

    }

    @Override
    JasperDesign buildTemplate(ReportTemplate template, JasperReportBuilder report, Map params, String lang) {
        //TODO: Implementation
        return report.toJasperDesign()
    }

    protected void processColumnGrouping(List<TextColumnBuilder> columns, JasperReportBuilder report, Boolean isExcel = false) {
        List<ColumnGroupBuilder> columnGroups = []
        List<ComponentBuilder> groupHeader = []
        columns.eachWithIndex {it,i->
            ColumnGroupBuilder singleColumnGroup = grp.group(it)
            singleColumnGroup.showColumnHeaderAndFooter();
            singleColumnGroup.setHeaderLayout(GroupHeaderLayout.EMPTY)
            TextFieldBuilder<String> textHeaderBuilder = cmp.text(new GroupTextExpression(singleColumnGroup)).setMarkup(Markup.HTML)
            groupHeader.add(textHeaderBuilder)
            if (!isExcel) {
                TextFieldBuilder<String> bookmarkHeader = cmp.text(" ").setWidth(1)
                bookmarkHeader.setAnchorName(new GroupBookmarkExpression(singleColumnGroup))
                bookmarkHeader.setBookmarkLevel(2)
                bookmarkHeader.setPrintWhenExpression(new SuppressRepeatingBookmarkPrintWhenExpression(singleColumnGroup))
                groupHeader.add(bookmarkHeader)
            }
            singleColumnGroup.addHeaderComponent(cmp.horizontalFlowList(textHeaderBuilder))
            singleColumnGroup.reprintHeaderOnEachPage()
            singleColumnGroup.setStartInNewPage(true)
            singleColumnGroup.setFooterSplitType(SplitType.IMMEDIATE)
            //singleColumnGroup.setKeepTogether(true)
            columnGroups.add(singleColumnGroup)
        }
        report.setShowColumnTitle(false)
        report.groupBy(*columnGroups)
    }

    private List<TextColumnBuilder> getGroupingColumns(ReportTemplate executedTemplate, Map params, String lang) {
        List<TextColumnBuilder> columns = []
        if (executedTemplate instanceof CaseLineListingTemplate) {
            List<ReportFieldInfo> groupingColumns = executedTemplate?.groupingList?.reportFieldInfoList
            List<String> fieldNameWithIndex = executedTemplate?.getFieldNameWithIndex()
            int columnLength = executedTemplate.columnList.reportFieldInfoList.size()

            for (int i = 0; i < groupingColumns?.size(); i++) {
                def reportFieldInfo = groupingColumns.get(i)
                ReportField reportField = reportFieldInfo.reportField
                String columnLabel = customMessageService.getMessage("app.reportField." + reportField.name)
                if (reportFieldInfo.renameValue) {
                    columnLabel = reportFieldInfo.renameValue
                }
                TextColumnBuilder column = createColumn(columnLabel, fieldNameWithIndex[i + columnLength], reportFieldInfo, params)
                if (!reportFieldInfo.customExpression &&
                        !reportFieldInfo.commaSeparatedValue &&
                        !reportFieldInfo.blindedValue &&
                        reportField.isDate()) {
                    column.setPattern(reportField.getDateFormat(lang))
                }
                columns.add(column)
            }
        }
        return columns
    }

    void setSubReportHeader(String header, Map params, JasperReportBuilder subreport) {
        if (!params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) {
            setSubReportPageHeader(header, subreport)
        } else {
            setSubReportPrintablePageHeader(header, subreport, params.outputFormat=="XLSX")
        }
    }


    private void setSubReportPageHeader(String header, JasperReportBuilder subreport) {
        TextFieldBuilder textFieldBuilder = cmp.text(header)

        textFieldBuilder.setStyle(Templates.subReportPageHeaderStyle)
        subreport.pageHeader(cmp.horizontalFlowList(textFieldBuilder).newFlowRow().add(cmp.gap(5, 5)))
        subreport.setDefaultFont(Templates.defaultFontStyleHTML)
        subreport.setColumnTitleStyle(Templates.columnTitleStyle)
        subreport.setColumnStyle(Templates.columnStyle)

        def reportTemplate = subreport.getReport().getTemplate()

        reportTemplate.setGroupTitleStyle(Templates.groupTitleStyle.style)
        reportTemplate.setGroupStyle(Templates.groupStyle.style)
    }

    void setSubReportPrintablePageHeader(String header, JasperReportBuilder subreport, Boolean isExcel = false) {
        List<ComponentBuilder> subReportHeader = []
        subReportHeader.add(cmp.text(header)
            .setStyle(Templates.subReportPageHeaderStyle)
        )
        if (!isExcel) {
            TextFieldBuilder<String> bookmarkHeader = cmp.text(" ").setWidth(1)
            bookmarkHeader.setAnchorName(header)
            bookmarkHeader.setBookmarkLevel(3)
            //bookmarkHeader.setPrintWhenExpression(exp.printInFirstPage())
            bookmarkHeader.setPrintRepeatedValues(false)
            subReportHeader.add(bookmarkHeader)
        }
        subreport.pageHeader(cmp.horizontalFlowList(*subReportHeader.reverse()).newFlowRow().add(cmp.gap(5, 5)))
        subreport.setDefaultFont(Templates.defaultFontStyle)
        subreport.setColumnTitleStyle(Templates.columnTitleStyle)
        subreport.setColumnHeaderStyle(Templates.columnHeaderStyle)
        subreport.setColumnStyle(Templates.columnStyle)

        def reportTemplate = subreport.getReport().getTemplate()
        reportTemplate.setGroupTitleStyle(Templates.groupTitleStyle.style)
        reportTemplate.setGroupStyle(Templates.groupStyle.style)
    }

    private static DRIDataType detectColumnType(ReportFieldInfo reportFieldInfo) {
        try {
            if (!reportFieldInfo.customExpression &&
                    !reportFieldInfo.commaSeparatedValue &&
                    !reportFieldInfo.blindedValue) {
                return type.detectType(reportFieldInfo.reportField.dataType)
            }
        } catch (Exception e) {
            // Using string for unknown field types
            return type.stringType()
        }
        return type.stringType()
    }

    private class NoDataExpression extends AbstractSimpleExpression<Boolean> {
        public Boolean evaluate(ReportParameters reportParameters) {
            return reportParameters.reportRowNumber == 0
        }
    }

    private class GroupTextExpression extends AbstractSimpleExpression<String> {
        private ColumnGroupBuilder columnGroup

        GroupTextExpression(ColumnGroupBuilder columnGroup) {
            this.columnGroup = columnGroup
        }

        String evaluate(ReportParameters reportParameters) {
            StringBuilder sb = new StringBuilder()
            sb.append("<b>")
            if (columnGroup.group.titleExpression instanceof  ValueExpression) {
                sb.append(((ValueExpression) columnGroup.group.titleExpression).value)
            } else if (columnGroup.group.titleExpression instanceof  JasperExpression) {
                sb.append(((JasperExpression) columnGroup.group.titleExpression).expression.replace("\"", ""))
            }
            sb.append(": ")
            sb.append("</b>")
            sb.append(reportParameters.getFieldValue(columnGroup.group.valueField.valueExpression.name))
            return sb.toString();
        }
    }

    private class GroupBookmarkExpression extends AbstractSimpleExpression<String> {
        private ColumnGroupBuilder columnGroup

        GroupBookmarkExpression(ColumnGroupBuilder columnGroup) {
            this.columnGroup = columnGroup
        }

        String evaluate(ReportParameters reportParameters) {
            StringBuilder sb = new StringBuilder()
            if (columnGroup.group.titleExpression instanceof  ValueExpression) {
                sb.append(((ValueExpression) columnGroup.group.titleExpression).value)
            } else if (columnGroup.group.titleExpression instanceof  JasperExpression) {
                sb.append(((JasperExpression) columnGroup.group.titleExpression).expression.replace("\"", ""))
            }
            sb.append(": ")
            sb.append(reportParameters.getFieldValue(columnGroup.group.valueField.valueExpression.name))
            return sb.toString();
        }
    }

    private static TemplateSetCsvDataSource createDataSource(ReportResult reportResult, String caseNumberColumnName) {
        if (reportResult?.data?.value) {
            return new TemplateSetCsvDataSource(new BufferedInputStream(new ByteArrayInputStream(reportResult?.data?.decryptedValue)), caseNumberColumnName)
        }
        return new TemplateSetCsvDataSource(null, caseNumberColumnName)
    }

    private static getGroupingColumnName(ReportTemplate executedTemplate) {
        if (executedTemplate instanceof CaseLineListingTemplate) {
            List<String> fieldNameWithIndex = executedTemplate.getFieldNameWithIndex()
            int columnLength = executedTemplate.columnList.reportFieldInfoList.size()
            return fieldNameWithIndex[columnLength]
        }
        return null
    }

    private static class SubreportDataSource extends AbstractSimpleExpression<JRDataSource> {
        private ReportTemplate executedTemplate
        private TemplateSetCsvDataSource mainDataSource
        private List<String> columnNames

        SubreportDataSource(ReportTemplate executedTemplate, TemplateSetCsvDataSource mainDataSource,
                            List<String> columnNames) {
            this.executedTemplate = executedTemplate
            this.mainDataSource = mainDataSource
            this.columnNames = columnNames
        }

        JRDataSource evaluate(ReportParameters reportParameters) {
            return mainDataSource.getSubreportDataSource(executedTemplate.id, columnNames)
        }
    }
}