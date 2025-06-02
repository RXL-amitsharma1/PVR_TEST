package com.rxlogix.config

import com.rxlogix.enums.QueryOperatorEnum

class QueryExpressionValue extends ParameterValue {
    ReportField reportField
    QueryOperatorEnum operator
    String specialKeyValue

    static constraints = {
        specialKeyValue nullable: true
    }

    static mapping = {
        tablePerHierarchy false

        table name: "QUERY_EXP_VALUE"

        reportField column: "REPORT_FIELD_ID"
        operator column: "OPERATOR_ID"
        specialKeyValue column: "SPL_VAL_KEY"
    }

    @Override
    public String toString() {
        return "${reportField} ${operator} ${super.toString()}"
    }
}
