package com.rxlogix.dynamicReports

import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.enums.ReportFormatEnum
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class BalanceMinusSheetBuilder {

    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")

    private static final String COMPARISON_TYPE = "comparisonType"
    private static final String TABLE_NAME = "tableName"
    private static final String VALIDATION_CHECK = "validationCheck"
    private static final String CASE_NUM = "caseNum"
    private static final String FIELD_NAME = "fieldName"
    private static final String TENANT_ID = "tenantId"

    def createBalanceMinusSheet(def results, String reportName, String locale){
        ReportBuilder reportBuilder = new ReportBuilder()
        JasperReportBuilder criteriaSheet = reportBuilder.initializeNewReport()

        //todo:  get the header from setHeaderAndFooter return value?
        String header = customMessageService.getMessage("jasperReports.mismatchCaseList")
        HeaderBuilder headerBuilder = new HeaderBuilder()
        FooterBuilder footerBuilder = new FooterBuilder()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        List<JasperReportBuilder> jasperReportBuilderList
        Map params = [:]
        params.outputFormat = ReportFormatEnum.XLSX.name()
        params.reportLocale = locale.toString()

        headerBuilder.setHeader(null, params, criteriaSheet, null, header, true)
        buildCriteriaSheet(criteriaSheet, results)
        footerBuilder.setFooter(params, criteriaSheet, null, true)
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        File reportFile = new File(dynamicReportService.getReportsDirectory() + reportName)
        if (reportFile?.exists() && !reportFile.isDirectory()) {
            reportFile.delete()
        }
        JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
        jasperReportBuilderEntry.jasperReportBuilder = criteriaSheet
        jasperReportBuilderEntry.excelSheetName = header
        jasperReportBuilderEntryList.add(jasperReportBuilderEntry)

        jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[])

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.exportXlsx(mainReport, reportName, jasperReportBuilderEntryList, locale, false)
        reportFile.deleteOnExit()

        return reportFile
    }

    private buildCriteriaSheet(JasperReportBuilder report, def results) {
        report.setIgnorePageWidth(true)
        report.setTemplate(Templates.reportTemplate)
        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(Templates.columnTitleStyle)
        report.setColumnStyle(Templates.columnStyle)
        report.addColumn(col.column(customMessageService.getMessage("app.label.tableName"), TABLE_NAME, type.stringType()))
        report.addColumn(col.column(customMessageService.getMessage("app.label.fieldName"), FIELD_NAME, type.stringType()))
        report.addColumn(col.column(customMessageService.getMessage("app.label.comparisonType"), COMPARISON_TYPE, type.stringType()))
        report.addColumn(col.column(customMessageService.getMessage("balanceMinusQuery.validation.checkStatus.label"), VALIDATION_CHECK, type.stringType()))
        report.addColumn(col.column(customMessageService.getMessage("app.label.tenantId"), TENANT_ID, type.stringType()))
        report.addColumn(col.column(customMessageService.getMessage("app.label.caseNum"), CASE_NUM, type.stringType()))
        report.setDataSource(createCriteriaSheetDataSource(results))
    }

    private JRDataSource createCriteriaSheetDataSource(def results) {
        Collection<Map<java.lang.String,?>> result = new LinkedList<>()
        results.eachWithIndex{ entry, int iterator ->
            Map item = [:]
            item.put(TABLE_NAME, results[iterator].TABLE_NAME ?: '')
            item.put(FIELD_NAME, results[iterator].FIELD_NAME ?: '')
            item.put(COMPARISON_TYPE,  results[iterator].COMPARISON_TYPE ?: '')

            if(results[iterator].FLAG_TGT_ROWS_MISSING == 1){
                item.put(VALIDATION_CHECK, 'RECORDS MISSING IN TARGET')
            }else if(results[iterator].MISMATCH_COL_LIST != null){
                item.put(VALIDATION_CHECK, 'COLUMNS '+results[iterator].MISMATCH_COL_LIST+' MISMATCHED')
            }else if(results[iterator].FLAG_TGT_ROWS_ADDITONAL == 1){
                item.put(VALIDATION_CHECK, 'ADDITIONAL RECORDS IN TARGET')
            }else{
                item.put(VALIDATION_CHECK, '')
            }
            item.put(TENANT_ID, results[iterator].TENANT_ID ? String.valueOf(results[iterator].TENANT_ID) : '')
            item.put(CASE_NUM,  results[iterator].CASE_NUM ?: '')
            result.add(item)
        }
        return new JRMapCollectionDataSource(result)
    }

}
