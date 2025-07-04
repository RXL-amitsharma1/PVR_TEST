package com.rxlogix.dynamicReports.reportTypes

import net.sf.dynamicreports.report.builder.expression.AbstractComplexExpression
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.definition.ReportParameters

class SuppressRepeatingBookmarkPrintWhenExpression extends AbstractComplexExpression<Boolean> {
    def previousValue

    SuppressRepeatingBookmarkPrintWhenExpression(ColumnGroupBuilder columnGroup) {
        addExpression(columnGroup.group.valueField.valueExpression)
    }

    SuppressRepeatingBookmarkPrintWhenExpression(List<ColumnGroupBuilder> columnGroups) {
        columnGroups.each {
            addExpression(it.group.valueField.valueExpression)
        }
    }

    Boolean evaluate(List<?> values, ReportParameters reportParameters) {
        Iterator<?> valuesIterator = values.iterator()
        def currentValue = []
        while (valuesIterator.hasNext()) {
            currentValue += valuesIterator.next()
        }
        def doPrint = currentValue != this.previousValue
        this.previousValue = currentValue
        return doPrint
    }
}
