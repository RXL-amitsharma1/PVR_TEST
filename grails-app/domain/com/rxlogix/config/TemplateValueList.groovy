package com.rxlogix.config

class TemplateValueList extends ValueList {
    ReportTemplate template

    static mapping = {
        table name: "TEMPLT_VALUE"
        template column: "RPT_TEMPLT_ID"
        tablePerHierarchy false
    }

    @Override
    public String toString() {
       return "${template?.name} - ${super.toString()}"
    }
}
