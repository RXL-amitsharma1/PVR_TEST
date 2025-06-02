package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.Constants
import com.rxlogix.CustomMessageService
import com.rxlogix.SeedDataService
import com.rxlogix.config.ReportResult
import com.rxlogix.config.ReportTemplate
import grails.converters.JSON
import grails.util.Holders
import groovy.util.logging.Slf4j
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.DynamicReports
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.Markup
import net.sf.jasperreports.engine.design.JasperDesign
import org.grails.web.converters.exceptions.ConverterException
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.awt.Color
import java.util.zip.GZIPInputStream

import static net.sf.dynamicreports.report.builder.DynamicReports.*

@Slf4j
class CustomSQLReportBuilder implements SpecificReportTypeBuilder, SpecificTemplateTypeBuilder {
    public static final String CHART_COLUMN_PREFIX = "CHART_COLUMN_"
    public static final String CHART_COLUMN_P_PREFIX = "CHART_COLUMN_P_"

    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    SeedDataService seedDataService = Holders.applicationContext.getBean("seedDataService")

    @Override
    public void createReport(ReportResult reportResult, JasperReportBuilder report, Map params, String lang) {
        ReportTemplate template = reportResult.template ?: reportResult.executedTemplateQuery.executedTemplate
        processReport(template, report, params, lang)
        if (!template.ciomsI && !template.medWatch && !template.columnNamesList && reportResult?.data?.value) {
            JSONArray result = JSON.parse(new GZIPInputStream(new BufferedInputStream(new ByteArrayInputStream(reportResult?.data?.decryptedValue))), "utf-8")
            JSONObject sample = result.first()
            sample.keys().each {
                report.addColumn(createColumn(it, it, type.stringType(), params))
            }
        }
    }

    @Override
    void createSubReport(ReportTemplate executedTemplate, JasperReportBuilder report, Map params, String lang) {

    }

    @Override
    JasperDesign buildTemplate(ReportTemplate template, JasperReportBuilder report, Map params, String lang) {
        processReport(template, report, params, lang)
        return report.toJasperDesign()
    }

    private processReport(ReportTemplate template, JasperReportBuilder report, Map params, String lang) {
        List<String> columnNamesList = []
        try {
            JSONArray columnNamesListJson = JSON.parse(template.columnNamesList.toString())
            columnNamesList = columnNamesListJson?.collect {it.toString()}
        } catch (ConverterException ce) {
            if (template.columnNamesList?.toString()?.length() > 2) {
                String unwrappedColumnNamesString = template.columnNamesList.toString().substring(1, template.columnNamesList.toString().length() - 1)
                columnNamesList = unwrappedColumnNamesString.split(", ").toList()
            }
        } catch (IllegalArgumentException | ClassCastException e) {
            log.error(String.format("Column Names '%s' of Template '%s' parsing error in Custom SQL Report builder processReport: %s", template.columnNamesList != null ? template.columnNamesList.toString() : "[null]", template.name ?: "[null]", e.getMessage()))
        }
        if (template.ciomsI || template.medWatch) {
            InputStream is = null
            try {
                if (template.useFixedTemplate && !template.fixedTemplate) {
//                  no fixed template data but useFixedTemplate flag will be true for CIOMS / Medwatch templates of previous release through IQ
                    String filePath = ((template.ciomsI) ? "CIOMS_OLD.jrxml" : "MEDWATCH_OLD.jrxml")
                    is = seedDataService.getInputStreamForMetadata(filePath)
                }
                else if (template.useFixedTemplate && template.fixedTemplate?.data) {
//                  we will use fixed template driven jrxml for latest and future CIOMS / Medwatch templates
                    is = new ByteArrayInputStream(template.fixedTemplate.data)
                }
                else {
//                  legacy/unsupported templates will default to current jrxml file path
                    String jrxmlPath = ((template.ciomsI) ? Constants.CIOMS_I_JRXML_FILENAME : Constants.MEDWATCH_JRXML_FILENAME)
                    is = CustomSQLReportBuilder.class.getResourceAsStream("/jrxml/" + jrxmlPath)
                }
                report.setTemplateDesign(is)
            } finally {
                if (is != null) {
                    is.close()
                }
            }
        } else if (!columnNamesList.isEmpty()) {
            columnNamesList.each {String columnName ->
                def columnType = type.stringType()
                if (columnName.contains(CHART_COLUMN_PREFIX)) {
                    columnType = type.integerType()
                }
                if (columnName.contains(CHART_COLUMN_P_PREFIX)) {
                    columnType = type.doubleType()
                }
                report.addColumn(createColumn(getColumnLabel(columnName), columnName, columnType, params))
            }
        }
    }

    TextColumnBuilder createColumn(String columnLabel, String columnName, def columnType, Map params) {
        TextColumnBuilder column = col.column(columnLabel, columnName, columnType)
        String updatedColumnLabel = columnLabel.toUpperCase().endsWith("_LINK") ? columnLabel.toUpperCase().replace("_LINK", "") : columnLabel
        column.setTitle(exp.jasperSyntax("\"${updatedColumnLabel?.replaceAll('"','\\\\"')}\"")) //Added replaceAll to handle doublequotes text.
        StyleBuilder columnStyle = getOrCreateColumnStyle(column, params)
        if(columnLabel.toLowerCase().endsWith('_link')){
            columnStyle.setMarkup(Markup.HTML)
            columnStyle.setForegroundColor(new Color(51, 122, 183))
            column.setValueFormatter(new MultipleHyperLinkColumnFormatter())
        }
        return column
    }

    public static String getColumnLabel(String columnName) {
        columnName?.replaceAll(CHART_COLUMN_P_PREFIX, "").replaceAll(CHART_COLUMN_PREFIX, "").trim()
    }
}