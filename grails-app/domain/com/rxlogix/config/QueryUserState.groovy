package com.rxlogix.config

import com.rxlogix.user.User

class QueryUserState {
    User user
    Boolean isFavorite

    static belongsTo = [query: SuperQuery]
    static constraints = {
        isFavorite(nullable: true)
    }

    static mapping = {
        table name: "QUERY_USER_STATE"

        user column: "RPT_USER_ID"
        isFavorite column: "IS_FAVORITE"
        query column: "QUERY_ID"
    }

    public String toString() {
        return "$query - $user"
    }
}
