package com.rxlogix.config

import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ParameterValue {
    String value
    String key
    boolean isFromCopyPaste = false // Used when rendering QEVs in edit config page

    static mapping = {
        tablePerHierarchy false
        table name: "PARAM"
        id column: "ID"

        key column : "LOOKUP" // "KEY" is a reserved word DB's
        value column: "VALUE", sqlType: DbUtil.longStringType
        isFromCopyPaste column: "IS_FROM_COPY_PASTE"
    }

    static constraints = {
        value(nullable: true, maxSize: 1000 * 1024) // 1 M
    }

    @Override
    public String toString() {
        return "${value ? key + ' - ' + value: ''}"
    }
}
