package com.rxlogix.dynamicReports.customElements.formula

import com.rxlogix.dynamicReports.Templates
import net.sf.jasperreports.engine.*
import net.sf.jasperreports.engine.base.JRBasePrintText
import net.sf.jasperreports.engine.export.GridCellSize
import net.sf.jasperreports.engine.export.JRExporterGridCell
import net.sf.jasperreports.engine.export.JRXlsAbstractExporter
import net.sf.jasperreports.engine.export.ooxml.GenericElementXlsxHandler
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporterContext
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporterNature
import net.sf.jasperreports.engine.type.RotationEnum

class FormulaXlsxHandler implements GenericElementXlsxHandler {

    private JRXlsxExporter exporter

    @Override
    void exportElement(JRXlsxExporterContext exporterContext,
                       JRGenericPrintElement element,
                       JRExporterGridCell gridCell,
                       int colIndex,
                       int rowIndex) throws JRException {

        String textValue = element.getParameterValue(FormulaElementBuilder.TEXT_PARAMETER_NAME)
        JRPrintText text = new JRBasePrintText(null)
        text.setText(textValue)
        text.setFontName(Templates.DEFAULT_FONT_NAME)
        text.setStyle(element.getStyle())

        exporter = (JRXlsxExporter) exporterContext.getExporterRef()

        exportHeader(new JRExporterGridCellProxy(gridCell, text), rowIndex, colIndex, exporter.maxColumnIndex,
                null, exporter.getTextLocale(text),
                exporter.isWrapText(gridCell.getElement()) || Boolean.TRUE.equals(((JRXlsxExporterNature)exporter.nature).getColumnAutoFit(gridCell.getElement())),
                exporter.isCellHidden(gridCell.getElement()),
                exporter.isCellLocked(gridCell.getElement()),
                exporter.isShrinkToFit(gridCell.getElement()),
                exporter.isIgnoreTextFormatting(text),
                text.getRotationValue(),
                exporter.sheetInfo)
        exporter.sheetHelper.exportMergedCells(rowIndex, colIndex, exporter.maxColumnIndex, gridCell.getRowSpan(),
                gridCell.getColSpan())
        FormulaInfo formulaInfo = getFormulaInfo(element)
        if (formulaInfo != null) {
            if (formulaInfo.isArray) {
                String columnIndexName = JRXlsxExporter.getColumIndexName(colIndex, exporter.maxColumnIndex)
                exporter.sheetHelper.write("    <f aca=\"true\" t=\"array\" ref=\"${columnIndexName}${rowIndex + 1}:${columnIndexName}${rowIndex + 1}\">${formulaInfo.formula}</f>\n")
            } else {
                exporter.sheetHelper.write("    <f>${formulaInfo.formula}</f>\n")
            }
        }
        exportValue(textValue)
        exportFooter()
    }

    @Override
    JRPrintImage getImage(JRXlsxExporterContext jrXlsxExporterContext, JRGenericPrintElement jrGenericPrintElement) throws JRException {
        return null
    }

    @Override
    boolean toExport(JRGenericPrintElement jrGenericPrintElement) {
        return true
    }

    private void exportHeader(JRExporterGridCell gridCell, int rowIndex, int colIndex, int maxColIndex, String pattern, Locale locale, boolean isWrapText, boolean isHidden, boolean isLocked, boolean isShrinkToFit, boolean isIgnoreTextFormatting, RotationEnum rotation, JRXlsAbstractExporter.SheetInfo sheetInfo) {
        String columIndexName = JRXlsAbstractExporter.getColumIndexName(colIndex, maxColIndex) + (rowIndex + 1)
        String cellStyle = exporter.styleHelper.getCellStyle(gridCell, pattern, locale, isWrapText, isHidden, isLocked, isShrinkToFit, isIgnoreTextFormatting, rotation, sheetInfo)
        String type = "str"
        exporter.cellHelper.write("  <c r=\"${columIndexName}\" s=\"${cellStyle}\" t=\"${type}\">\n")
    }

    private void exportValue(String value) {
        exporter.cellHelper.write("    <v>${value}</v>\n")
    }

    private void exportFooter() {
        exporter.cellHelper.write("  </c>\n")
    }

    private FormulaInfo getFormulaInfo(JRGenericPrintElement element) {
        String formula = JRPropertiesUtil.getOwnProperty(element, "net.sf.jasperreports.export.xls.formula")
        if (formula != null) {
            formula = formula.trim()
            if (formula.startsWith("=")) {
                formula = formula.substring(1)
            }
            boolean isArray = Boolean.valueOf(JRPropertiesUtil.getOwnProperty(element, "net.sf.jasperreports.export.xls.formula.isArray"))
            return new FormulaInfo(formula: formula, isArray: isArray)
        }
        return null
    }

    private static class FormulaInfo {
        boolean isArray
        String formula
    }

    private static class JRExporterGridCellProxy extends JRExporterGridCell {
        private JRExporterGridCell gridCell
        private JRPrintElement element

        JRExporterGridCellProxy(JRExporterGridCell gridCell, JRPrintElement element) {
            this.gridCell = gridCell
            this.element = element
        }

        @Override
        GridCellSize getSize() {
            return gridCell.getSize()
        }

        @Override
        byte getType() {
            return gridCell.getType()
        }

        JRPrintElement getElement() {
            return element
        }

        @Override
        String getElementAddress() {
            return gridCell.getElementAddress()
        }

        @Override
        String getProperty(String key) {
            return gridCell.getProperty(key)
        }
    }
}
