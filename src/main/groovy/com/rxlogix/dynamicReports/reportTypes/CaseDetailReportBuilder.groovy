package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.ImageService
import com.rxlogix.enums.ReportFormatEnum
import grails.util.Holders
import groovy.util.logging.Slf4j
import net.sf.jasperreports.engine.*
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.engine.export.JRPdfExporter
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput

import javax.imageio.ImageIO

@Slf4j
class CaseDetailReportBuilder {

    ImageService imageService = Holders.applicationContext.getBean("imageService")

    ByteArrayOutputStream createReport(List caseDetailList, String outputFormat){
        Map logoMap = [:]
        logoMap.put("Logo_Header", ImageIO.read(imageService.getCompanyImage()))
        logoMap.put("Confidential_Logo", ImageIO.read(imageService.getConfidentialLogo()))
        InputStream sourceFile = CaseDetailReportBuilder.class.getResourceAsStream("/jrxml/CaseDetailForm.jrxml")
        if(sourceFile){
            JasperReport report = JasperCompileManager.compileReport(sourceFile)
            JasperPrint print = fillJasperReportWithData(report,logoMap, caseDetailList)
            ByteArrayOutputStream caseDetailForm
            if(outputFormat == ReportFormatEnum.PDF.name()){
                caseDetailForm = exportJasperReportToPdf(print)
            }
            return caseDetailForm
        }
        return null
    }

    JasperPrint fillJasperReportWithData(JasperReport report, Map logoMap, List caseDetailList){
        JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(caseDetailList)
        JasperPrint print = null
        try{
            print = JasperFillManager.fillReport(report,logoMap,jrBeanCollectionDataSource)
        } catch (JRException e) {
            log.warn("Error while filling fillJasperReportWithData ${e.message}")
        }
    }

    ByteArrayOutputStream exportJasperReportToPdf(JasperPrint print){
        JRPdfExporter pdfExporter = new JRPdfExporter();
        pdfExporter.setExporterInput(new SimpleExporterInput(print));
        ByteArrayOutputStream pdfReportStream = new ByteArrayOutputStream();
        pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfReportStream));
        pdfExporter.exportReport();
        return pdfReportStream
    }
}