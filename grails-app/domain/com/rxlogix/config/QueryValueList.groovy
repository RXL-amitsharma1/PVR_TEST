package com.rxlogix.config

import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class QueryValueList extends ValueList {
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
