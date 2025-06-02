package com.rxlogix

import com.rxlogix.config.SuperQuery
import com.rxlogix.config.ValueList
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class QueryRCAValueList extends ValueList {
    SuperQuery query

    static mapping = {
        tablePerHierarchy false

        table name: "QUERY_VALUE"
        query column: "SUPER_QUERY_ID"
    }

    @Override
    public String toString() {
        return "${query?.name} - ${super.toString()}"
    }

}
