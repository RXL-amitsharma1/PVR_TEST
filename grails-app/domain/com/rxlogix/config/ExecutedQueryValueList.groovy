package com.rxlogix.config

import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ExecutedQueryValueList extends QueryValueList {

    static mapping = {
        table name: "EX_QUERY_VALUE"
    }

    @Override
    public String toString() {
        super.toString()
    }

}
