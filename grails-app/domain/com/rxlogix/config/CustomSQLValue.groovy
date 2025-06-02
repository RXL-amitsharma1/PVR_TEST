package com.rxlogix.config

import com.rxlogix.Constants

class CustomSQLValue extends ParameterValue {

    static mapping = {
        tablePerHierarchy false
        table name: "SQL_VALUE"
    }

    static constraints = {
        value(validator: { value ->
            if (value && value.toLowerCase() ==~ Constants.SQL_DML_PATTERN_REGEX) {
                return "com.rxlogix.config.query.customSQLQuery.invalid"
            }
        })
    }

    @Override
    public String toString() {
        super.toString()
    }
}
