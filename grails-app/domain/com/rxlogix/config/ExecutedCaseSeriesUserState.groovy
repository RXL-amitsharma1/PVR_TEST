package com.rxlogix.config

import com.rxlogix.user.User

class ExecutedCaseSeriesUserState {

    User user
    boolean isDeleted = false
    boolean isArchived = false
    Boolean isFavorite

    static belongsTo = [executedCaseSeries: ExecutedCaseSeries]
    static constraints = {
        isFavorite(nullable: true)
    }

    static mapping = {
        table name: "EX_CASE_SERIES_USER_STATE"

        user column: "RPT_USER_ID"
        isDeleted column: "IS_DELETED"
        isArchived column: "IS_ARCHIVED"
        isFavorite column: "IS_FAVORITE"
        executedCaseSeries column: "EX_CASE_SERIES_ID"
    }

    public String toString() {
        return "$executedCaseSeries - $user"
    }
}


