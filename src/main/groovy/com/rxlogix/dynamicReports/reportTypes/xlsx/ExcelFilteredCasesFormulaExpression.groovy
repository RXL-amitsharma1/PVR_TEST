package com.rxlogix.dynamicReports.reportTypes.xlsx

import com.rxlogix.DynamicReportService
import com.rxlogix.config.CaseLineListingTemplate
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter

class ExcelFilteredCasesFormulaExpression extends AbstractSimpleExpression<String> {
    private static final int FIRST_DATA_ROW_INDEX = 8
    private static final int MAX_COLUMN_INDEX = 16383
    private String label
    private int caseNumberColumnIndex
    private Boolean hideTotalRowCount
    private Boolean hasFooter = false

    ExcelFilteredCasesFormulaExpression(String label, int caseNumberColumnIndex, Boolean hideTotalRowCount, Boolean hasFooter) {
        this.label = label
        this.caseNumberColumnIndex = caseNumberColumnIndex
        this.hideTotalRowCount = hideTotalRowCount
        this.hasFooter = hasFooter
    }

    @Override
    String evaluate(ReportParameters reportParameters) {
        int firstRowIndex = FIRST_DATA_ROW_INDEX - (hideTotalRowCount ? 2 : 0)
        int lastDataRowIndex = firstRowIndex + reportParameters.reportRowNumber - (hasFooter ? 1 : 0)
        String columnIndexName = JRXlsxExporter.getColumIndexName(caseNumberColumnIndex, MAX_COLUMN_INDEX)
        String firstCell = "${columnIndexName}${firstRowIndex}"
        String lastCell = "${columnIndexName}${lastDataRowIndex}"
        String range = "${firstCell}:${lastCell}"
        String sumFormula = "SUM(IF(FREQUENCY(IF(SUBTOTAL(${ExcelSubtotalFunction.COUNTA.includesHidden},OFFSET(${firstCell},ROW(${range})-ROW(${firstCell}),0,1)),MATCH(${range},${range},0)),ROW(${range})-ROW(${firstCell})+1)>0,1))"
        String finalFormula = "IF(EXACT(${columnIndexName}${firstRowIndex}, \"${DynamicReportService.REPORT_NO_DATA_MESSAGE}\"), 0 , ${sumFormula})"
        return "CONCATENATE(&quot;${label}: &quot;, ${finalFormula})"
    }
}
