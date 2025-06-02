package com.rxlogix.dynamicReports.charts


import net.sf.jasperreports.engine.JRGenericPrintElement
import net.sf.jasperreports.engine.base.JRBasePrintImage
import net.sf.jasperreports.engine.export.GenericElementPdfHandler
import net.sf.jasperreports.engine.export.JRPdfExporter
import net.sf.jasperreports.engine.export.JRPdfExporterContext

class PdfChartHandler implements GenericElementPdfHandler {

    private JRPdfExporter exporter

    @Override
    boolean toExport(JRGenericPrintElement element) {
        return true
    }

    @Override
    void exportElement(JRPdfExporterContext exporterContext, JRGenericPrintElement element) {
        this.exporter = (JRPdfExporter) exporterContext.getExporterRef()
        JRBasePrintImage image = ImageChartGenerator.getChartImage(exporterContext.exportedReport.getDefaultStyleProvider(),element)
        exporter.exportImage(image)
    }

}