package com.rxlogix.dynamicReports.charts

import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JRGenericPrintElement
import net.sf.jasperreports.engine.JRPrintImage
import net.sf.jasperreports.engine.base.JRBasePrintImage
import net.sf.jasperreports.engine.export.ElementGridCell
import net.sf.jasperreports.engine.export.GridCellSize
import net.sf.jasperreports.engine.export.JRExporterGridCell
import net.sf.jasperreports.engine.export.ooxml.GenericElementXlsxHandler
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporterContext
import net.sf.jasperreports.engine.export.ooxml.XlsxZip
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
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTAnchorClientData
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShapeNonVisual

import javax.xml.namespace.QName

import static org.apache.poi.POIXMLTypeLoader.DEFAULT_XML_OPTIONS

class XlsxChartHandler implements GenericElementXlsxHandler {
    private JRXlsxExporter exporter
    private XlsxZip xlsxZip
    private XlsxChartHelper chartHelper
    private JRExporterGridCell gridCell
    private int colIndex
    private int rowIndex
    private String latestComment

    @Override
    boolean toExport(JRGenericPrintElement jrGenericPrintElement) {
        return true
    }

    @Override
    void exportElement(
            JRXlsxExporterContext exporterContext,
            JRGenericPrintElement element,
            JRExporterGridCell gridCell,
            int colIndex,
            int rowIndex
    ) throws JRException {
        this.gridCell = gridCell
        this.colIndex = colIndex
        this.rowIndex = rowIndex
        this.exporter = (JRXlsxExporter) exporterContext.getExporterRef()
        this.xlsxZip = exporter.xlsxZip
        int pointsNumber = 0
        def chartOptions = element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)
        chartOptions?.series?.each { k, v -> pointsNumber = Math.max(pointsNumber, v?.data?.size() ?: 0) }
        if (element.getParameterValue(ChartGenerator.PARAMETER_EXPORT_AS_IMAGE)) {
            def (rows, cols) = getRowsAndCols(pointsNumber, !!chartOptions?.options?.chart?.inverted, chartOptions?.options?.chart?.type ?: chartOptions?.options?.chart?.map)
            JRBasePrintImage image = ImageChartGenerator.getChartImage(exporterContext.exportedReport.getDefaultStyleProvider(), element, rows, cols)
            ElementGridCell newGridCell = new ElementGridCell(gridCell.getContainer(), gridCell.getParentIndex(), gridCell.getElementIndex(), new GridCellSize(5000, 5000, cols, rows))
            exporter.exportImage(image, newGridCell, 0, 3, 0, 0, null)
        } else {
            ExportZipEntry chartEntry = addChartZipEntry(exporter.sheetIndex + 1)
            addChartRelationship(exporter.sheetIndex + 1)
            injectChart(exporter.sheetIndex + 1, pointsNumber, !!chartOptions?.options?.chart?.inverted, chartOptions?.options?.chart?.type)
            Writer chartWriter = chartEntry.getWriter()
            def options = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).generateChart(false)
            int chartRowsCount = (int) element.getParameterValue(ChartGenerator.PARAMETER_CHART_ROWS_COUNT)
            List totalRowIndicies = element.getParameterValue(ChartGenerator.PARAMETER_TOTAL_ROW_INDICES)
            this.chartHelper = new XlsxChartHelper(exporter, chartWriter, options, chartRowsCount, totalRowIndicies)
            chartHelper.exportChart()
            latestComment = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).getLatestComment()
            if (latestComment != null && latestComment.length() > 0) injectAnnotation()
        }
    }

    @Override
    JRPrintImage getImage(JRXlsxExporterContext exporterContext, JRGenericPrintElement element) throws JRException {
        return null;
    }

    private ExportZipEntry addChartZipEntry(int index) {
        def chartPath = "xl/charts/chart" + index + ".xml"
        ExportZipEntry chartEntry = xlsxZip.createEntry(chartPath);
        xlsxZip.exportZipEntries.add(chartEntry);
        exporter.ctHelper.write("  <Override PartName=\"/${chartPath}\" ContentType=\"application/vnd.openxmlformats-officedocument.drawingml.chart+xml\"/>\n")
        return chartEntry;
    }

    private void addChartRelationship(int index) {
        this.exporter.drawingRelsHelper.write(" <Relationship Id=\"rId" + index + "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart\" Target=\"../charts/chart" + index + ".xml\"/>\n");
    }

    private List getRowsAndCols(int pointsNumber, boolean inverted, String type){
        int rows = 6 // for the first 30 chart rows - 6 excel rows, next 1 excel row will have 1,2 chart row
        int cols = 18 // for the first 100 chart points - 18 excel columns, next 1 excel column will have 1,2 chart point
        if (type in ["line", "spline", "column", "bar", "area", "areaspline", "arearange", "areasplinerange", "columnrange", "scatter"]) {
            if (inverted && pointsNumber > 15) rows = 6 + Math.ceil((pointsNumber - 15) / 1.2)
            if (!inverted && pointsNumber > 100) cols = 18 + Math.ceil((pointsNumber - 100) / 1.2)
        }
        if (type == "custom/world") {
            rows = 18
            cols = 18
        }
        return [rows, cols]
    }

    private void injectChart(int index, int pointsNumber, boolean inverted, String type) throws JRException {
        def(rows,cols)= getRowsAndCols( pointsNumber,  inverted,  type)
        exporter.drawingHelper.write("<xdr:twoCellAnchor editAs=\"oneCell\">\n");
        exporter.drawingHelper.write("    <xdr:from><xdr:col>0</xdr:col><xdr:colOff>0</xdr:colOff><xdr:row>3</xdr:row><xdr:rowOff>11160</xdr:rowOff></xdr:from>\n");
        exporter.drawingHelper.write("    <xdr:to><xdr:col>${cols}</xdr:col><xdr:colOff>590550</xdr:colOff><xdr:row>${rows}</xdr:row><xdr:rowOff>83520</xdr:rowOff></xdr:to>\n");
        exporter.drawingHelper.write("    <xdr:graphicFrame>\n");
        exporter.drawingHelper.write("        <xdr:nvGraphicFramePr>\n");
        exporter.drawingHelper.write("            <xdr:cNvPr id=\"2\" name=\"\"/>\n");
        exporter.drawingHelper.write("            <xdr:cNvGraphicFramePr/>\n");
        exporter.drawingHelper.write("        </xdr:nvGraphicFramePr>\n");
        exporter.drawingHelper.write("        <xdr:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/></xdr:xfrm>\n");
        exporter.drawingHelper.write("        <a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">\n");
        exporter.drawingHelper.write("            <c:chart xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\"\n" +
                " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"\n" +
                " r:id=\"rId" + index +"\"/>\n");
        exporter.drawingHelper.write("        </a:graphicData></a:graphic>\n");
        exporter.drawingHelper.write("    </xdr:graphicFrame>\n");
        exporter.drawingHelper.write("    <xdr:clientData/>\n");
        exporter.drawingHelper.write("</xdr:twoCellAnchor>\n");
    }

    private void injectAnnotation() throws JRException {
        exporter.drawingHelper.write(createShape())
    }

    private String createShape() throws XmlException{
        CTOneCellAnchor ctOneCellAnchor = CTOneCellAnchor.Factory.newInstance()
        CTMarker ctMarker = CTMarker.Factory.newInstance()
        ctMarker.setCol(1)
        ctMarker.setColOff(186840L)
        ctMarker.setRow(3)
        ctMarker.setRowOff(191160L)
        ctOneCellAnchor.setFrom(ctMarker)
        CTPositiveSize2D ctPositiveSize2D = CTPositiveSize2D.Factory.newInstance()
        ctPositiveSize2D.setCx(2940840L)
        ctPositiveSize2D.setCy(1058040L)
        ctOneCellAnchor.setExt(ctPositiveSize2D)
        //Create shape object and add annotation text to it
        CTShape shape = CTShape.Factory.newInstance()

        //Adding shape non visual properties
        CTShapeNonVisual ctShapeNonVisual = CTShapeNonVisual.Factory.newInstance()
        CTNonVisualDrawingProps ctNonVisualDrawingProps = CTNonVisualDrawingProps.Factory.newInstance()
        ctNonVisualDrawingProps.setId(3L)
        ctNonVisualDrawingProps.setName("Chart Annotation")
        ctShapeNonVisual.setCNvPr(ctNonVisualDrawingProps)
        ctShapeNonVisual.setCNvSpPr(CTNonVisualDrawingShapeProps.Factory.newInstance())
        shape.setNvSpPr(ctShapeNonVisual)

        //Adding shape container size and location data
        CTShapeProperties ctShapeProperties = CTShapeProperties.Factory.newInstance()
        CTTransform2D shapeCoordinates = CTTransform2D.Factory.newInstance()
        CTPoint2D locationCoordinates = CTPoint2D.Factory.newInstance()
        locationCoordinates.setX(1539360L)
        locationCoordinates.setY(978480L)
        CTPositiveSize2D sizeCoordinates = CTPositiveSize2D.Factory.newInstance()
        sizeCoordinates.setCx(2940840L)
        sizeCoordinates.setCy(1058040L)
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
        textCharacterProperties.setSz(1600)
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
        ctOneCellAnchor.setSp(shape)
        ctOneCellAnchor.setClientData(CTAnchorClientData.Factory.newInstance())

        //Convert XML fragment to proper XML
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTOneCellAnchor.type.getName().getNamespaceURI(), "oneCellAnchor", "xdr"))
        StringWriter sw = new StringWriter()
        ctOneCellAnchor.save(sw, xmlOptions)
        return sw.toString()
    }
}