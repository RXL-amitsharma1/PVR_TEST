package com.rxlogix.config

class ExecutedQueryExpressionValue extends QueryExpressionValue {

    static mapping = {
        table name: "EX_QUERY_EXP"
    }

    static constraints = {
        value(nullable: true)
    }

    @Override
    public String toString() {
        super.toString()
    }

}
