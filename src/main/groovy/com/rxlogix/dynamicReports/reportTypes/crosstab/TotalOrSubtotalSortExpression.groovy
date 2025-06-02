package com.rxlogix.dynamicReports.reportTypes.crosstab

import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.definition.ReportParameters

class TotalOrSubtotalSortExpression extends AbstractSimpleExpression<Integer> {
    private int currentIndex
    private boolean isTotalSubtotal
    private String fieldName

    TotalOrSubtotalSortExpression(String fieldName) {
        this.fieldName = fieldName
    }

    @Override
    Integer evaluate(ReportParameters reportParameters) {
        Object value = reportParameters.getValue(fieldName)
        if (value instanceof String && ("Total".equals(value) || "Subtotal".equals(value) || "Sub Total".equals(value) || "総計".equals(value) || "小計".equals(value))) {
            currentIndex++
            isTotalSubtotal = true
        } else if(isTotalSubtotal) {
            currentIndex++
            isTotalSubtotal = false
        }
        return currentIndex
    }
}
