package com.rxlogix.dynamicReports

import com.rxlogix.DynamicReportService
import com.rxlogix.config.ReportResult
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.util.MiscUtil
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder
import net.sf.dynamicreports.jasper.builder.export.JasperCsvExporterBuilder
import net.sf.dynamicreports.jasper.builder.export.JasperDocxExporterBuilder
import net.sf.dynamicreports.jasper.builder.export.JasperHtmlExporterBuilder
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder
import net.sf.dynamicreports.jasper.builder.export.JasperPptxExporterBuilder
import net.sf.dynamicreports.jasper.builder.export.JasperXlsxExporterBuilder
import net.sf.dynamicreports.jasper.constant.JasperProperty
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.DRReport
import net.sf.jasperreports.crosstabs.base.JRBaseCrosstabColumnGroup
import net.sf.jasperreports.engine.JRElementGroup
import net.sf.jasperreports.engine.JRPrintElement
import net.sf.jasperreports.engine.JRPrintPage
import net.sf.jasperreports.engine.JRTextField
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.base.JRBasePrintPage
import net.sf.jasperreports.engine.design.JRDesignTextField
import net.sf.jasperreports.engine.export.JRCsvExporter
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleWriterExporterOutput

import java.nio.charset.StandardCharsets
import java.nio.charset.Charset

import static net.sf.dynamicreports.report.builder.DynamicReports.export


class OutputBuilder {
    // https://support.office.com/en-us/article/Excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3
    public static final int XLSX_MAX_ROWS_PER_SHEET = 1048576
    public static final int PDF_MAX_PAGES = 10000

    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")

    File produceReportOutput(Map params, String reportName, JasperConcatenatedReportBuilder report, List<JasperReportBuilderEntry> jasperReportBuilderEntryList, Boolean templateType) {
        def reportFile = null
        String locale = params.reportLocale
        if (!params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) {
            reportFile = exportHtml(report, reportName, locale)
        } else if (params.outputFormat == ReportFormatEnum.PDF.name()) {
            reportFile = exportPdf(report, reportName, locale)
        } else if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
            reportFile = exportXlsx(report, reportName, jasperReportBuilderEntryList, locale, templateType)
        } else if (params.outputFormat == ReportFormatEnum.DOCX.name()) {
            reportFile = exportDocx(report, reportName, locale)
        } else if (params.outputFormat == ReportFormatEnum.PPTX.name()) {
            reportFile = exportPptx(report, reportName, locale)
        } else if (params.outputFormat == ReportFormatEnum.CSV.name()) {
            reportFile = exportCsv(report, reportName, jasperReportBuilderEntryList, locale, templateType)
        }
        return reportFile
    }

    private File exportHtml(report, String reportName, String locale) {
        File reportFile = new File(dynamicReportService.getReportsDirectory() + dynamicReportService.getReportFilename(reportName, ReportFormatEnum.HTML.name(), locale))
        JasperHtmlExporterBuilder htmlExporter = export.htmlExporter(new FileOutputStream(reportFile))
        htmlExporter.setCharacterEncoding(StandardCharsets.UTF_8.name())
        htmlExporter.setOutputImagesToDir(true);
        htmlExporter.setImagesDirName(dynamicReportService.getReportsDirectory() + reportName);
        htmlExporter.setImagesURI("/reports/report/image?reportName=${reportName}&image=");
        report.toHtml(htmlExporter)
        return reportFile
    }

    private File exportPdf(JasperConcatenatedReportBuilder report, String reportName, String locale) {
        File reportFile = new File(dynamicReportService.getReportsDirectory() + dynamicReportService.getReportFilename(reportName, ReportFormatEnum.PDF.name(), locale))
        JasperPdfExporterBuilder pdfExporter = export.pdfExporter(new FileOutputStream(reportFile))
        pdfExporter.setCharacterEncoding(StandardCharsets.UTF_8.name())
        pdfExporter.setForceLineBreakPolicy(true)
        pdfExporter.setCompressed(true)
        report.toPdf(pdfExporter)
        return reportFile
    }

    private File exportXlsx(report, String reportName, List<JasperReportBuilderEntry> jasperReportBuilderEntryList, String locale, Boolean templateType) {
        File reportFile = new File(dynamicReportService.getReportsDirectory() + dynamicReportService.getReportFilename(reportName, ReportFormatEnum.XLSX.name(), locale))
        JasperXlsxExporterBuilder xlsxExporter = export.xlsxExporter(new FileOutputStream(reportFile))
        int numOfPages = 0
        String sheetNnames = ""
        // This code is added to modify the sheet's Name from page1, page2, page3....etc to meaning full name such as 'Criteria Sheet' in excel file
        JasperPrint reportToJasperPrint
        ArrayList<String> sheetNamesLst=[]
        jasperReportBuilderEntryList.each {
            reportToJasperPrint = it.jasperReportBuilder.toJasperPrint()
            numOfPages = reportToJasperPrint.getPages().size()
               for(int i=1;i<=numOfPages;i++) {
                   if(numOfPages == 1)
                       sheetNnames = sheetNnames + "${it.excelSheetName}"
                   else
                       sheetNnames = sheetNnames + "${it.excelSheetName}_${i}/"
               }
            sheetNamesLst.add(sheetNnames.substring(0, Math.min(31, sheetNnames.length())))
            if(sheetNamesLst.size()>1){
                for(def itr :sheetNamesLst){
                    sheetNnames=sheetNnames.substring(0, Math.min(31, sheetNnames.length()))
                    if(!(itr.equals(sheetNnames)) && itr.toLowerCase().equals(sheetNnames.toLowerCase())){
                        sheetNamesLst.pop()
                        sheetNnames=itr
                        break
                    }
                }
            }
            reportToJasperPrint.setProperty(JasperProperty.EXPORT_XLS_SHEET_NAMES_PREFIX, MiscUtil.escapeExcelSheetName(sheetNnames).trim())
            sheetNnames = ""
        }
        xlsxExporter.removeEmptySpaceBetweenColumns = true
        xlsxExporter.removeEmptySpaceBetweenRows = true
        xlsxExporter.detectCellType = true
        xlsxExporter.setCharacterEncoding(StandardCharsets.UTF_8.name())
        xlsxExporter.setMaxRowsPerSheet(XLSX_MAX_ROWS_PER_SHEET)
        xlsxExporter.onePagePerSheet = !templateType
        xlsxExporter.collapseRowSpan = !templateType
        report.toXlsx(xlsxExporter)
        return reportFile
    }

    private File exportDocx(report, String reportName, String locale) {
        File reportFile = new File(dynamicReportService.getReportsDirectory() + dynamicReportService.getReportFilename(reportName, ReportFormatEnum.DOCX.name(), locale))
        JasperDocxExporterBuilder docxExporter = export.docxExporter(new FileOutputStream(reportFile))
        docxExporter.setCharacterEncoding(StandardCharsets.UTF_8.name())
        docxExporter.setFramesAsNestedTables(false)
        report.toDocx(docxExporter)
        return reportFile
    }

    private File exportPptx(report, String reportName, String locale) {
        File reportFile = new File(dynamicReportService.getReportsDirectory() + dynamicReportService.getReportFilename(reportName, ReportFormatEnum.PPTX.name(), locale))
        JasperPptxExporterBuilder pptxExporter = export.pptxExporter(new FileOutputStream(reportFile))
        pptxExporter.setCharacterEncoding(StandardCharsets.UTF_8.name())
        report.toPptx(pptxExporter)
        return reportFile
    }

    private File exportCsv(JasperConcatenatedReportBuilder report, String reportName,
                           List<JasperReportBuilderEntry> jasperReportBuilderEntryList,
                           String locale, Boolean templateType) {
        File reportFile = new File(dynamicReportService.getReportsDirectory() +
                dynamicReportService.getReportFilename(reportName, ReportFormatEnum.CSV.name()))

        try (FileOutputStream fos = new FileOutputStream(reportFile, false);
             OutputStreamWriter writer = new OutputStreamWriter(fos, Charset.forName("MS932"))) {

            JRCsvExporter csvExporter = new JRCsvExporter()
            JasperPrint combinedJasperPrint = new JasperPrint()

            jasperReportBuilderEntryList.each { entry ->
                if (entry.excelSheetName in ["Criteria Sheet", "Appendix"]) {
                    return
                }
                JasperPrint reportToJasperPrint = entry.jasperReportBuilder.toJasperPrint()
                reportToJasperPrint.getPages().each { page ->
                    combinedJasperPrint.addPage(page)
                }
            }

            csvExporter.setExporterInput(new SimpleExporterInput(combinedJasperPrint))
            csvExporter.setExporterOutput(new SimpleWriterExporterOutput(writer))

            SimpleCsvExporterConfiguration configuration = new SimpleCsvExporterConfiguration()
            configuration.setFieldDelimiter(",")
            configuration.setRecordDelimiter("\n")
            csvExporter.setConfiguration(configuration)

            csvExporter.exportReport()

        } catch (IOException e) {
            log.error("Error exporting CSV: ", e)
        }

        return reportFile
    }

}
