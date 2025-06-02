package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.CustomMessageService
import grails.util.Holders
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.dynamicreports.report.definition.expression.DRISimpleExpression

/**
 * Created by gologuzov on 04.05.16.
 */
class SuppressRepeatingColumnFormatter extends AbstractValueFormatter<Object, Object> {

    DRISimpleExpression<Boolean> expression
    Integer groupingListSize
    String columnName
    String field
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    SuppressRepeatingColumnFormatter(ColumnGroupBuilder group, String columnName, boolean repeatOnNewPage = false, Integer groupingListSize = 0, String field = null) {
        this.expression = new SuppressRepeatingColumnPrintWhenExpression(group, columnName, repeatOnNewPage)
        this.groupingListSize = groupingListSize
        this.columnName = columnName
        this.field = field
    }

    SuppressRepeatingColumnFormatter(DRISimpleExpression<Boolean> expression) {
        this.expression = expression
    }

    @Override
    Object format(Object currentValue, ReportParameters reportParameters) {
        if (expression.evaluate(reportParameters)) {
            return ""
        }
        String translated = CrosstabReportBuilder.translateTotalSubtotal(currentValue.toString(), field, groupingListSize)
        if (translated) return translated
        return currentValue
    }
}
