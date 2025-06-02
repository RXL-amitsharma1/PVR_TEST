package com.rxlogix.dynamicReports.reportTypes.common

import com.rxlogix.CustomMessageService
import com.rxlogix.config.ReportFieldInfo
import grails.util.Holders
import net.sf.dynamicreports.report.builder.expression.AbstractComplexExpression
import net.sf.dynamicreports.report.builder.expression.JasperExpression
import net.sf.dynamicreports.report.builder.expression.ValueExpression
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.definition.ReportParameters

class GroupTextExpression extends AbstractComplexExpression<String> {
    private CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    private List<ColumnGroupBuilder> columnGroups
    private List<ReportFieldInfo> groupingColumns
    private Boolean hideTotalRowCount = false

    GroupTextExpression(List<ColumnGroupBuilder> columnGroups, List<ReportFieldInfo> groupingColumns, Boolean hideTotalRowCount = false) {
        this.hideTotalRowCount = hideTotalRowCount
        this.columnGroups = columnGroups
        this.groupingColumns = groupingColumns
        columnGroups.each {
            addExpression(it.group.valueField.valueExpression)
        }
    }

    String evaluate(List<?> values, ReportParameters reportParameters) {
        if (values == null || !values.findAll { it != null }) return ""
        StringBuilder sb = new StringBuilder()
        def previousSupressed = false
        boolean totalRow = false
        Iterator<?> valuesIterator = values.iterator()
        columnGroups.eachWithIndex { ColumnGroupBuilder group, int i ->
            if (valuesIterator.hasNext()) {
                if (sb.size() > 0) {
                    if (!(groupingColumns[i].suppressLabel && previousSupressed)) {
                        sb.append(",")
                    }
                    sb.append(" ")
                }
                if (!groupingColumns[i].suppressLabel) {
                    if (group.group.titleExpression instanceof ValueExpression) {
                        sb.append(((ValueExpression) group.group.titleExpression).value)
                    } else if (group.group.titleExpression instanceof JasperExpression) {
                        sb.append(((JasperExpression) group.group.titleExpression).expression.replace("\"", ""))
                    }
                    sb.append(": ")
                    previousSupressed = false
                } else {
                    previousSupressed = true
                }
                String val = valuesIterator.next()
                if (val in ["Total", "総計"]) totalRow = true
                sb.append(val)
            }
        }
        if (totalRow && groupingColumns?.size() > 0) return customMessageService.getMessage("app.grand.total")
        if (valuesIterator.hasNext()) {
            String subTotalRowsNumber = valuesIterator.next()
            sb.append(" (")
            if (!hideTotalRowCount) {
                sb.append(customMessageService.getMessage("app.label.subTotalRowsNumber"))
                sb.append(": ")
                sb.append(subTotalRowsNumber)
            }
            if (valuesIterator.hasNext()) {
                if (!hideTotalRowCount) sb.append(", ")
                sb.append(customMessageService.getMessage("app.label.subTotalCaseNumber"))
                sb.append(": ")
                sb.append(valuesIterator.next())
            }
            sb.append(")")
        }
        return sb.toString()
    }
}
