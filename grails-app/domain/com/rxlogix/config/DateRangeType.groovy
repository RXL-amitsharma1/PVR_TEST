package com.rxlogix.config

import com.rxlogix.user.UserGroup
import com.rxlogix.util.ViewHelper

class DateRangeType {

    String name
    boolean isDeleted = false
    Integer sortOrder = 0

    static constraints = {
        name unique: true
        sortOrder nullable: true
    }

    static belongsTo = [UserGroup]

    static mapping = {
        table name: "DATE_RANGE_TYPE"
        name column: "NAME"
        isDeleted column: "IS_DELETED"
        sortOrder column: "SORT_ORDER"
    }

    public getI18nKey() {
        return "app.dateDropdown.${name}"
    }

    public getI18nDescriptionKey() {
        return "app.dateDropdown.${name}.label.description"
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        DateRangeType that = (DateRangeType) o

        if (name != that.name) return false
        if (isDeleted != that.isDeleted) return false
        if (sortOrder != that.sortOrder) return false
        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (isDeleted ? 1 : 0)
        result = 31 * result + (sortOrder ? sortOrder.hashCode() : 0)
        return result
    }

    String toString(){
        ViewHelper.getMessage(getI18nKey())
    }

    public static void copyObj(DateRangeType sourceObj, DateRangeType targetObj) {
        targetObj.with {
            name = sourceObj.name
            isDeleted = sourceObj.isDeleted
            sortOrder = sourceObj.sortOrder
        }
    }
}