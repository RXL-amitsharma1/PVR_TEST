package com.rxlogix.dynamicReports.charts

import org.htmlunit.*
import com.rxlogix.dynamicReports.charts.docx.ContentTypesZipEntry
import groovy.util.logging.Slf4j
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRGenericPrintElement
import net.sf.jasperreports.engine.JRPrintElementIndex
import net.sf.jasperreports.engine.base.JRBasePrintImage
import net.sf.jasperreports.engine.export.JRExporterGridCell
import net.sf.jasperreports.engine.export.ooxml.DocxZip
import net.sf.jasperreports.engine.export.ooxml.GenericElementDocxHandler
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporterContext
import net.sf.jasperreports.engine.export.zip.ExportZipEntry
import org.apache.commons.io.IOUtils
import org.docx4j.XmlUtils
import org.docx4j.vml.CTFill
import org.docx4j.vml.CTRoundRect
import org.docx4j.vml.CTTextbox
import org.docx4j.vml.STTrueFalse
import org.docx4j.wml.*
import org.springframework.http.HttpStatus

import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName

class DocxChartHandler implements GenericElementDocxHandler {
    private JRDocxExporterContext exporterContext
    private JRGenericPrintElement element
    private JRDocxExporter exporter
    private DocxZip docxZip
    private DocxChartHelper chartHelper
    private JRExporterGridCell gridCell
    private String latestComment

    @Override
    boolean toExport(JRGenericPrintElement jrGenericPrintElement) {
        return true
    }

    @Override
    void exportElement(
            JRDocxExporterContext exporterContext,
            JRGenericPrintElement element,
            JRExporterGridCell gridCell
    ) throws JRException {
        this.exporterContext = exporterContext
        this.element = element
        this.gridCell = gridCell
        this.exporter = (JRDocxExporter) exporterContext.getExporterRef()
        this.docxZip = exporter.docxZip

        if (element.getParameterValue(ChartGenerator.PARAMETER_EXPORT_AS_IMAGE)) {
            JRBasePrintImage image = ImageChartGenerator.getChartImage(exporterContext.exportedReport.getDefaultStyleProvider(), element)
            exporter.exportImage(exporterContext.getTableHelper(), image, gridCell)
        } else {

            // Very durty hack: removing existing content types entry and adding own implementation
            ContentTypesZipEntry contentTypesEntry = docxZip.exportZipEntries.find {
                it.name == "[Content_Types].xml" && it instanceof ContentTypesZipEntry
            }
            if (!contentTypesEntry) {
                docxZip.exportZipEntries.removeAll {
                    it.name == "[Content_Types].xml"
                }
                contentTypesEntry = new ContentTypesZipEntry("[Content_Types].xml")
                docxZip.addEntry(contentTypesEntry)
            }
            // End of the very durty hack
            latestComment = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).getLatestComment()
            JRPrintElementIndex index = exporter.getElementIndex(gridCell)
            ExportZipEntry chartEntry = addChartZipEntry(index.toString())
            contentTypesEntry.addContentType("<Override PartName=\"/${chartEntry.name}\" ContentType=\"application/vnd.openxmlformats-officedocument.drawingml.chart+xml\"/>")
            addChartRelationship(index.toString())
            injectChart(index.toString())
            Writer chartWriter = chartEntry.getWriter()
            def options = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).generateChart(false)
            int chartRowsCount = (int) element.getParameterValue(ChartGenerator.PARAMETER_CHART_ROWS_COUNT)
            this.chartHelper = new DocxChartHelper(exporter, chartWriter, options, chartRowsCount)
            chartHelper.exportChart()
        }
    }

    private ExportZipEntry addChartZipEntry(String index) {
        def chartPath = "word/charts/chart" + index + ".xml"
        ExportZipEntry chartEntry = docxZip.createEntry(chartPath)
        docxZip.exportZipEntries.add(chartEntry)
        return chartEntry;
    }

    private void addChartRelationship(String index) {
        this.exporter.relsHelper.write(" <Relationship Id=\"rId" + index + "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart\" Target=\"charts/chart" + index + ".xml\"/>\n");
    }

    private void injectChart(String index) throws JRException {
        exporterContext.tableHelper.cellHelper.exportHeader(element, gridCell)
        exporter.docHelper.write(
                "        <w:p>\n" +
                "            <w:pPr>\n" +
                "                <w:spacing w:lineRule=\"auto\" w:line=\"240\" w:after=\"0\" w:before=\"0\"/>\n" +
                "            </w:pPr>\n" +
                        injectAnnotation() +
                "            <w:r>\n" +
                "                <w:rPr/>\n" +
                "                <w:drawing>\n" +
                "                    <wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">\n" +
                "                        <wp:extent cx=\"9372600\" cy=\"5270500\" />\n" +
                "                        <wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\" />\n" +
                "                        <wp:docPr id=\"1\" name=\"chart\" />\n" +
                "                        <a:graphic>\n" +
                "                            <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">\n" +
                "                                <c:chart xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\"\n" +
                "                                         xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"\n" +
                "                                         r:id=\"rId${index}\"/>\n" +
                "                            </a:graphicData>\n" +
                "                        </a:graphic>\n" +
                "                    </wp:inline>\n" +
                "                </w:drawing>\n" +
                "            </w:r>\n" +
                "        </w:p>");
        exporterContext.tableHelper.cellHelper.exportFooter()
    }

    private String injectAnnotation(){
        if(latestComment !=null && latestComment.length() > 0) {
            ObjectFactory wmlObjectFactory = new ObjectFactory()
            org.docx4j.vml.ObjectFactory vmlObjectFactory = new org.docx4j.vml.ObjectFactory()
            P annotationParagraph = wmlObjectFactory.createP()
            R annotationRun = wmlObjectFactory.createR()
            RPr rPrObject = wmlObjectFactory.createRPr()
            rPrObject.setNoProof(wmlObjectFactory.createBooleanDefaultTrue().setVal(true))
            Pict pictObj = wmlObjectFactory.createPict()
            CTRoundRect roundRect = vmlObjectFactory.createCTRoundRect()
            roundRect.setStyle("position:absolute;margin-left:77.75pt;margin-top:70.2pt;width:183.75pt;height:41.25pt;z-index:251658240;mso-position-horizontal-relative:text;mso-position-vertical-relative:text")
            roundRect.setArcsize("10923f")
            roundRect.setFilled(STTrueFalse.F)
            CTFill fill = vmlObjectFactory.createCTFill()
            fill.setOpacity("0")
            CTTextbox textbox = vmlObjectFactory.createCTTextbox()
            textbox.setStyle("mso-fit-shape-to-text:t")
            CTTxbxContent txbxContent = wmlObjectFactory.createCTTxbxContent()
            P textParagraph = wmlObjectFactory.createP()
            R textParagraphRun = wmlObjectFactory.createR()
            Text annotationText = wmlObjectFactory.createText()
            annotationText.setValue(latestComment)
            textParagraphRun.getContent().add(annotationText)
            textParagraph.getContent().add(textParagraphRun)
            txbxContent.getContent().add(textParagraph)
            textbox.setTxbxContent(txbxContent)
            QName fillQname = new QName("urn:schemas-microsoft-com:vml", "fill")
            JAXBElement<CTFill> fillXml = new JAXBElement<CTFill>(fillQname, CTFill.class, fill)
            roundRect.getEGShapeElements().add(fillXml)
            QName textBoxQname = new QName("urn:schemas-microsoft-com:vml", "textbox")
            JAXBElement<CTTextbox> textBoxXml = new JAXBElement<CTTextbox>(textBoxQname, CTTextbox.class, textbox)
            roundRect.getEGShapeElements().add(textBoxXml)
            QName roundRectQname = new QName("urn:schemas-microsoft-com:vml", "roundrect")
            JAXBElement<CTRoundRect> roundRectXml = new JAXBElement<CTRoundRect>(roundRectQname, CTRoundRect.class, roundRect)
            pictObj.getAnyAndAny().add(roundRectXml)
            annotationRun.setRPr(rPrObject)
            annotationRun.getContent().add(pictObj)
            return XmlUtils.marshaltoString(annotationRun)
        }else{
            return ""
        }
    }
}