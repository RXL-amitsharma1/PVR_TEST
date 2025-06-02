package com.rxlogix.config

class CustomSQLTemplateValue extends ParameterValue {
    String field

    static mapping = {
        tablePerHierarchy false
        table name: "SQL_TEMPLT_VALUE"
        field column: "FIELD"
    }

    static constraints = {
        field nullable: true
    }

    @Override
    public String toString() {
        return "${field} - ${super.toString()}"
    }
}
