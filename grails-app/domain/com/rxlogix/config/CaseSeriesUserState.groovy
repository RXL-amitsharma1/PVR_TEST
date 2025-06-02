package com.rxlogix.config

import com.rxlogix.user.User
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class CaseSeriesUserState {

    User user
    Boolean isFavorite

    static belongsTo = [caseSeries: CaseSeries]
    static constraints = {
        isFavorite(nullable: true)
    }

    static mapping = {
        table name: "CASE_SERIES_USER_STATE"

        user column: "RPT_USER_ID"
        isFavorite column: "IS_FAVORITE"
        caseSeries column: "ECASE_SERIES_ID"
    }

    public String toString() {
        return "$caseSeries - $user"
    }
}


