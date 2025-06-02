package com.rxlogix.dynamicReports.charts

import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRGenericPrintElement
import net.sf.jasperreports.engine.JRPrintElementIndex
import net.sf.jasperreports.engine.base.JRBasePrintImage
import net.sf.jasperreports.engine.export.ooxml.GenericElementPptxHandler
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporterContext
import net.sf.jasperreports.engine.export.ooxml.PptxZip
import net.sf.jasperreports.engine.export.zip.ExportZipEntry
import org.apache.xmlbeans.XmlException
import org.apache.xmlbeans.XmlOptions
import org.openxmlformats.schemas.drawingml.x2006.main.CTFontReference
import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomGuideList
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTNoFillProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingShapeProps
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun
import org.openxmlformats.schemas.drawingml.x2006.main.CTScRgbColor
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeStyle
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrixReference
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextFont
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextListStyle
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextShapeAutofit
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextSpacing
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextSpacingPercent
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextUnderlineFillGroupWrapper
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D
import org.openxmlformats.schemas.drawingml.x2006.main.STFontCollectionIndex
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAlignType
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType
import org.openxmlformats.schemas.drawingml.x2006.main.STTextStrikeType
import org.openxmlformats.schemas.drawingml.x2006.main.STTextWrappingType
import org.openxmlformats.schemas.presentationml.x2006.main.CTApplicationNonVisualDrawingProps
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape
import org.openxmlformats.schemas.presentationml.x2006.main.CTShapeNonVisual

import javax.xml.namespace.QName

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS

class PptxChartHandler implements GenericElementPptxHandler {
    private JRPptxExporterContext exporterContext
    private JRGenericPrintElement element
    private JRPptxExporter exporter
    private PptxZip pptxZip
    private PptxChartHelper chartHelper
    private String latestComment

    @Override
    boolean toExport(JRGenericPrintElement jrGenericPrintElement) {
        return true
    }

    void exportElement(
            JRPptxExporterContext exporterContext,
            JRGenericPrintElement element
    ) throws JRException {
        this.exporterContext = exporterContext
        this.element = element
        this.exporter = (JRPptxExporter) exporterContext.getExporterRef()
        this.pptxZip = exporter.pptxZip

        if(element.getParameterValue(ChartGenerator.PARAMETER_EXPORT_AS_IMAGE)){
            JRBasePrintImage image = ImageChartGenerator.getChartImage(exporterContext.exportedReport.getDefaultStyleProvider(),element)
            exporter.exportImage(image)
        }else {
            JRPrintElementIndex index = exporter.getElementIndex()
            ExportZipEntry chartEntry = addChartZipEntry(index.toString())
            addChartRelationship(index.toString())
            injectChart(index.toString())
            Writer chartWriter = chartEntry.getWriter()
            def options = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).generateChart(false)
            int chartRowsCount = (int) element.getParameterValue(ChartGenerator.PARAMETER_CHART_ROWS_COUNT)
            this.chartHelper = new PptxChartHelper(exporter, chartWriter, options, chartRowsCount)
            chartHelper.exportChart()
            latestComment = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).getLatestComment()
            if (latestComment != null && latestComment.length() > 0) injectAnnotation()
        }
    }

    private ExportZipEntry addChartZipEntry(String index) {
        def chartPath = "ppt/charts/chart" + index + ".xml"
        ExportZipEntry chartEntry = pptxZip.createEntry(chartPath)
        pptxZip.exportZipEntries.add(chartEntry)
        exporter.ctHelper.write("  <Override PartName=\"/${chartPath}\" ContentType=\"application/vnd.openxmlformats-officedocument.drawingml.chart+xml\"/>\n")
        return chartEntry;
    }

    private void addChartRelationship(String index) {
        this.exporter.slideRelsHelper.write(" <Relationship Id=\"rId" + index + "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart\" Target=\"../charts/chart" + index + ".xml\"/>\n");
    }

    private void injectChart(String index) throws JRException {
        exporter.slideHelper.write(
                        "            <p:graphicFrame>\n" +
                        "                <p:nvGraphicFramePr>\n" +
                        "                    <p:cNvPr id=\"3\" name=\"Chart 2\"/>\n" +
                        "                    <p:cNvGraphicFramePr>\n" +
                        "                        <a:graphicFrameLocks noGrp=\"1\"/>\n" +
                        "                    </p:cNvGraphicFramePr>\n" +
                        "                    <p:nvPr/>\n" +
                        "                </p:nvGraphicFramePr>\n" +
                        "                <p:xfrm>\n" +
                        "                    <a:off x=\"342900\" y=\"1365920\"/>\n" +
                        "                    <a:ext cx=\"9372600\" cy=\"5472608\"/>\n" +
                        "                </p:xfrm>\n" +
                        "                <a:graphic>\n" +
                        "                    <a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">\n" +
                        "                        <c:chart\n" +
                        "                            xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\"\n" +
                        "                            xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"\n" +
                        "                                         r:id=\"rId${index}\"/>\n" +
                        "                        </a:graphicData>\n" +
                        "                    </a:graphic>\n" +
                        "                </p:graphicFrame>")
    }

    private void injectAnnotation() throws JRException {
        exporter.slideHelper.write(createShape())
    }

    private String createShape() throws XmlException {
        //Create shape object and add annotation text to it
        CTShape shape = CTShape.Factory.newInstance()

        //Adding shape non visual properties
        CTShapeNonVisual ctShapeNonVisual = CTShapeNonVisual.Factory.newInstance()
        CTNonVisualDrawingProps ctNonVisualDrawingProps = CTNonVisualDrawingProps.Factory.newInstance()
        ctNonVisualDrawingProps.setId(1000L)
        ctNonVisualDrawingProps.setName("Chart Annotation")
        ctShapeNonVisual.setCNvPr(ctNonVisualDrawingProps)
        CTNonVisualDrawingShapeProps ctNonVisualDrawingShapeProps = CTNonVisualDrawingShapeProps.Factory.newInstance()
        ctShapeNonVisual.setCNvSpPr(ctNonVisualDrawingShapeProps)
        CTApplicationNonVisualDrawingProps ctApplicationNonVisualDrawingProps = CTApplicationNonVisualDrawingProps.Factory.newInstance()
        ctShapeNonVisual.setNvPr(ctApplicationNonVisualDrawingProps)
        shape.setNvSpPr(ctShapeNonVisual)

        //Adding shape container size and location data
        CTShapeProperties ctShapeProperties = CTShapeProperties.Factory.newInstance()
        CTTransform2D shapeCoordinates = CTTransform2D.Factory.newInstance()
        CTPoint2D locationCoordinates = CTPoint2D.Factory.newInstance()
        locationCoordinates.setX(1644120L)
        locationCoordinates.setY(2112120L)
        CTPositiveSize2D sizeCoordinates = CTPositiveSize2D.Factory.newInstance()
        sizeCoordinates.setCx(2543820L)
        sizeCoordinates.setCy(512820L)
        shapeCoordinates.setOff(locationCoordinates)
        shapeCoordinates.setExt(sizeCoordinates)
        ctShapeProperties.setXfrm(shapeCoordinates)

        //Adding shape geometry
        CTPresetGeometry2D ctPresetGeometry2D = CTPresetGeometry2D.Factory.newInstance()
        ctPresetGeometry2D.setPrst(STShapeType.Enum.forString("roundRect"))
        CTGeomGuideList avLstObj = CTGeomGuideList.Factory.newInstance()
        ctPresetGeometry2D.setAvLst(avLstObj)
        ctShapeProperties.setPrstGeom(ctPresetGeometry2D)

        //Adding shape color properties
        CTLineProperties outlineProps = CTLineProperties.Factory.newInstance()
        CTSolidColorFillProperties lineSolidFill = CTSolidColorFillProperties.Factory.newInstance()
        CTScRgbColor lineFillColor = CTScRgbColor.Factory.newInstance()
        lineFillColor.setR(52)
        lineFillColor.setG(101)
        lineFillColor.setB(164)
        lineSolidFill.setScrgbClr(lineFillColor)
        outlineProps.setSolidFill(lineSolidFill)
        ctShapeProperties.setLn(outlineProps)
        ctShapeProperties.setNoFill(CTNoFillProperties.Factory.newInstance())

        shape.setSpPr(ctShapeProperties)

        //Add shape style
        CTShapeStyle ctShapeStyle = CTShapeStyle.Factory.newInstance()
        CTStyleMatrixReference lnRef = CTStyleMatrixReference.Factory.newInstance()
        lnRef.setIdx(0L)
        ctShapeStyle.setLnRef(lnRef)
        CTStyleMatrixReference fillRef = CTStyleMatrixReference.Factory.newInstance()
        fillRef.setIdx(0L)
        ctShapeStyle.setFillRef(fillRef)
        CTStyleMatrixReference effectRef = CTStyleMatrixReference.Factory.newInstance()
        effectRef.setIdx(0L)
        ctShapeStyle.setEffectRef(effectRef)
        CTFontReference fontRef = CTFontReference.Factory.newInstance()
        fontRef.setIdx(STFontCollectionIndex.Enum.forInt(2))
        ctShapeStyle.setFontRef(fontRef)
        shape.setStyle(ctShapeStyle)

        //Add text body
        CTTextBody ctTextBody = CTTextBody.Factory.newInstance()

        //Add text body properties
        CTTextBodyProperties ctTxtBdyProps = CTTextBodyProperties.Factory.newInstance()
        ctTxtBdyProps.setLIns(0)
        ctTxtBdyProps.setRIns(0)
        ctTxtBdyProps.setTIns(0)
        ctTxtBdyProps.setBIns(0)
        ctTxtBdyProps.setAnchor(STTextAnchoringType.Enum.forInt(2))
        ctTxtBdyProps.setSpAutoFit(CTTextShapeAutofit.Factory.newInstance())
        ctTxtBdyProps.setWrap(STTextWrappingType.Enum.forInt(2))
        ctTextBody.setBodyPr(ctTxtBdyProps)
        //Add text properties
        CTTextParagraph ctTextParagraph = CTTextParagraph.Factory.newInstance()
        CTTextParagraphProperties ctPpr = CTTextParagraphProperties.Factory.newInstance()
        ctPpr.setAlgn(STTextAlignType.Enum.forInt(2))
        CTTextSpacing ctTxtSpacing = CTTextSpacing.Factory.newInstance()
        CTTextSpacingPercent ctTextSpacingPercent = CTTextSpacingPercent.Factory.newInstance()
        ctTextSpacingPercent.setVal(100000)
        ctTxtSpacing.setSpcPct(ctTextSpacingPercent)
        ctPpr.setLnSpc(ctTxtSpacing)
        ctTextParagraph.setPPr(ctPpr)

        CTRegularTextRun ctRegularTextRun = CTRegularTextRun.Factory.newInstance()
        CTTextCharacterProperties textCharacterProperties = CTTextCharacterProperties.Factory.newInstance()
        textCharacterProperties.setB(false)
        textCharacterProperties.setLang("en-US")
        textCharacterProperties.setSz(1200)
        textCharacterProperties.setSpc(-1)
        textCharacterProperties.setStrike(STTextStrikeType.Enum.forInt(1))

        CTSolidColorFillProperties textSolidFill = CTSolidColorFillProperties.Factory.newInstance()
        CTScRgbColor textSolidFillColor = CTScRgbColor.Factory.newInstance()
        textSolidFillColor.setR(0)
        textSolidFillColor.setG(0)
        textSolidFillColor.setB(0)
        textSolidFill.setScrgbClr(textSolidFillColor)
        textCharacterProperties.setSolidFill(textSolidFill)

        CTTextUnderlineFillGroupWrapper uFill = CTTextUnderlineFillGroupWrapper.Factory.newInstance()
        CTSolidColorFillProperties uFillSolid = CTSolidColorFillProperties.Factory.newInstance()
        CTScRgbColor uFillSolidColor = CTScRgbColor.Factory.newInstance()
        uFillSolidColor.setR(255)
        uFillSolidColor.setG(255)
        uFillSolidColor.setB(255)
        uFillSolid.setScrgbClr(uFillSolidColor)
        uFill.setSolidFill(uFillSolid)
        textCharacterProperties.setUFill(uFill)

        CTTextFont textFont = CTTextFont.Factory.newInstance()
        textFont.setTypeface("Times New Roman")
        textCharacterProperties.setLatin(textFont)
        ctRegularTextRun.setRPr(textCharacterProperties)
        ctRegularTextRun.setT(latestComment)
        CTRegularTextRun[] rArray = [ctRegularTextRun]
        ctTextParagraph.setRArray(rArray)

        CTTextCharacterProperties endParaRpr = CTTextCharacterProperties.Factory.newInstance()
        endParaRpr.setB(false)
        endParaRpr.setLang("en-US")
        endParaRpr.setSz(1200)
        endParaRpr.setSpc(-1)
        endParaRpr.setStrike(STTextStrikeType.Enum.forInt(1))

        CTSolidColorFillProperties endParaSolidFill = CTSolidColorFillProperties.Factory.newInstance()
        CTScRgbColor endParaSolidFillColor = CTScRgbColor.Factory.newInstance()
        endParaSolidFillColor.setR(0)
        endParaSolidFillColor.setG(0)
        endParaSolidFillColor.setB(0)
        endParaSolidFill.setScrgbClr(endParaSolidFillColor)
        endParaRpr.setSolidFill(endParaSolidFill)

        CTTextUnderlineFillGroupWrapper endParaUFill = CTTextUnderlineFillGroupWrapper.Factory.newInstance()
        CTSolidColorFillProperties endParaUFillSolid = CTSolidColorFillProperties.Factory.newInstance()
        CTScRgbColor endParaUFillSolidColor = CTScRgbColor.Factory.newInstance()
        endParaUFillSolidColor.setR(255)
        endParaUFillSolidColor.setG(255)
        endParaUFillSolidColor.setB(255)
        endParaUFillSolid.setScrgbClr(endParaUFillSolidColor)
        endParaUFill.setSolidFill(endParaUFillSolid)
        endParaRpr.setUFill(uFill)

        CTTextFont endParaTextFont = CTTextFont.Factory.newInstance()
        endParaTextFont.setTypeface("Times New Roman")
        endParaRpr.setLatin(endParaTextFont)
        ctTextParagraph.setEndParaRPr(endParaRpr)

        CTTextParagraph[] pArray = [ctTextParagraph]
        ctTextBody.setPArray(pArray)

        shape.setTxBody(ctTextBody)

        //Convert XML fragment to proper XML
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTShape.type.getName().getNamespaceURI(), "sp", "p"))
        StringWriter sw = new StringWriter()
        shape.save(sw, xmlOptions)
        return sw.toString()

    }
}