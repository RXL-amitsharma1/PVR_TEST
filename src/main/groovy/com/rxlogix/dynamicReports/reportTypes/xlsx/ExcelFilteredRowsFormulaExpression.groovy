package com.rxlogix.dynamicReports.reportTypes.xlsx

import com.rxlogix.DynamicReportService
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter

class ExcelFilteredRowsFormulaExpression extends AbstractSimpleExpression<String> {
    private static final int FIRST_DATA_ROW_INDEX = 8
    private static final int MAX_COLUMN_INDEX = 16383
    private String label
    private Boolean hideTotalRowCount
    private Boolean hasFooter = false

    ExcelFilteredRowsFormulaExpression(String label, Boolean hideTotalRowCount, Boolean hasFooter) {
        this.label = label
        this.hideTotalRowCount = hideTotalRowCount
        this.hasFooter = hasFooter
    }

    @Override
    String evaluate(ReportParameters reportParameters) {
        int firstRowIndex = FIRST_DATA_ROW_INDEX - (hideTotalRowCount ? 2 : 0)
        int lastDataRowIndex = firstRowIndex + reportParameters.reportRowNumber - (hasFooter ? 1 : 0)
        String columnIndexName = JRXlsxExporter.getColumIndexName(0, MAX_COLUMN_INDEX)
        String firstCell = "${columnIndexName}${firstRowIndex}"
        String lastCell = "${columnIndexName}${lastDataRowIndex}"
        String range = "${firstCell}:${lastCell}"
        String subTotal = "SUBTOTAL(${ExcelSubtotalFunction.COUNTA.ignoresHidden}, ${range})"
        String finalTotal = "IF(EXACT(${columnIndexName}${FIRST_DATA_ROW_INDEX}, \"${DynamicReportService.REPORT_NO_DATA_MESSAGE}\"), 0 , ${subTotal})"
        return "CONCATENATE(&quot;${label}: &quot;, ${finalTotal} )"
    }
}
