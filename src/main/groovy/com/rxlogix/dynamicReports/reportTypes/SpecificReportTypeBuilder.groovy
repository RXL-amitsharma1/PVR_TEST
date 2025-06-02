package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.config.ReportFieldInfo
import com.rxlogix.config.ReportResult
import com.rxlogix.config.ReportTemplate
import com.rxlogix.dynamicReports.Templates
import grails.util.Holders
import grails.web.mapping.LinkGenerator
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.jasper.constant.JasperProperty
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.builder.style.TemplateStylesBuilder
import net.sf.dynamicreports.report.definition.datatype.DRIDataType

import static net.sf.dynamicreports.report.builder.DynamicReports.*

/**
 * Created by gologuzov on 07.11.15.
 */
trait SpecificReportTypeBuilder {
    static final int MIN_COLSPAN_COLUMNS_WIDTH_XLSX = 50
    static final String COLUMN_TITLE_CSS_CLASS = "column-title"

    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    LinkGenerator grailsLinkGenerator = Holders.applicationContext.getBean("grailsLinkGenerator")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    TemplateStylesBuilder templateStyles = stl.templateStyles()

    abstract void createReport(ReportResult reportResult, JasperReportBuilder report, Map params, String lang)

    abstract void createSubReport(ReportTemplate executedTemplate, JasperReportBuilder report, Map params, String lang)

    def getOrCreateColumnStyle(TextColumnBuilder column, Map params) {
        StyleBuilder columnStyle = templateStyles.getStyle(column.name)
        if (!columnStyle) {
            columnStyle = stl.style(Templates.columnStyle)
            columnStyle.setName(column.name)
            column.setStyle(columnStyle)
            templateStyles.addStyle(columnStyle)
        }
        return columnStyle
    }

    TextColumnBuilder createColumn(String columnLabel, String columnName, ReportFieldInfo reportFieldInfo, Map params) {

        if(!reportFieldInfo.renameValue) {
            if (columnLabel.contains('(J)') || (columnName.contains('J_')) ) {
                columnLabel = customMessageService.getMessage("app.reportField." + reportFieldInfo.reportField.name, null, "app.reportField." + reportFieldInfo.reportField.name, Locale.JAPANESE)
            } else {
                columnLabel = customMessageService.getMessage("app.reportField." + reportFieldInfo.reportField.name, null, "app.reportField." + reportFieldInfo.reportField.name, Locale.ENGLISH)
            }
        }

        TextColumnBuilder column = col.column(columnLabel, columnName, detectColumnType(reportFieldInfo))
        column.setTitle(exp.jasperSyntax("\"${columnLabel?.replaceAll('"','\\\\"')}\"")) //Added replaceAll to handle doublequotes text.
        column.addTitleProperty(JasperProperty.EXPORT_HTML_ID, exp.jasperSyntax("\"${reportFieldInfo.reportField.name}\""))
        column.addTitleProperty(JasperProperty.EXPORT_HTML_CLASS, exp.jasperSyntax("\"${COLUMN_TITLE_CSS_CLASS}\""))
        StyleBuilder columnStyle = getOrCreateColumnStyle(column, params)
        return column
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

    def getAutoColumnWidth(Collection<Integer> columnWidthList) {
        def predefinedWidthCount = columnWidthList.findAll({ it != ReportFieldInfo.AUTO_COLUMN_WIDTH }).size()
        def autoColumnCount = columnWidthList.size() - predefinedWidthCount
        def autoColumnWidth = 0
        if (autoColumnCount > 0) {
            int totalWidth = columnWidthList.sum()
            if (totalWidth < 100) {
                autoColumnWidth = (100 - totalWidth) / autoColumnCount
            }
        }
        return autoColumnWidth
    }

    def getNormalizationFactor(Collection<Integer> columnWidthList) {
        def factor = 1.0
        int totalWidth = columnWidthList.sum()
        if (totalWidth > 100) {
            factor = (totalWidth - 100) / totalWidth
        }
        return factor
    }
}