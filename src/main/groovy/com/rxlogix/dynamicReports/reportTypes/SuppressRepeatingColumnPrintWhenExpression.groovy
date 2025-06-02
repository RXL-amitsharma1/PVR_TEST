package com.rxlogix.dynamicReports.reportTypes

import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.definition.ReportParameters

import static net.sf.dynamicreports.report.builder.DynamicReports.exp


class SuppressRepeatingColumnPrintWhenExpression extends AbstractSimpleExpression<Boolean> {

    ColumnGroupBuilder group
    String columnName
    Boolean repeatOnNewPage
    def previousValue
    int previousPageNumber


    SuppressRepeatingColumnPrintWhenExpression(ColumnGroupBuilder group, String columnName, boolean repeatOnNewPage = false) {
        this.group = group
        this.columnName = columnName
        this.repeatOnNewPage = repeatOnNewPage
    }

    Boolean evaluate(ReportParameters reportParameters) {
        if (group) {
            int groupNumber = exp.groupRowNumber(group)?.evaluate(reportParameters)
            int pageNumber = reportParameters.pageNumber
            def currentValue = reportParameters.getFieldValue(columnName)
            boolean suppress = groupNumber != 1 && currentValue == this.previousValue && (!repeatOnNewPage || pageNumber == this.previousPageNumber)
            this.previousValue = currentValue
            this.previousPageNumber = pageNumber
            return suppress
        } else {
            return false
        }
    }
}
