package com.rxlogix.config

import com.rxlogix.user.User

class ExecutedReportUserState {
    User user
    boolean isDeleted = false
    boolean isArchived = false
    Boolean isFavorite

    static belongsTo = [executedConfiguration: ExecutedReportConfiguration]
    static constraints = {
        isFavorite(nullable: true)
    }

    static mapping = {
        table name: "EX_RCONFIG_USER_STATE"

        user column: "RPT_USER_ID"
        isDeleted column: "IS_DELETED"
        isArchived column: "IS_ARCHIVED"
        isFavorite column: "IS_FAVORITE"
        executedConfiguration column: "EX_RCONFIG_ID"
    }

    public String toString() {
        return "$executedConfiguration - $user"
    }
}
