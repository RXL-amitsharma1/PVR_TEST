package com.rxlogix.config

import com.rxlogix.user.User

class ConfigurationUserState {
    User user
    Boolean isFavorite

    static belongsTo = [configuration: ReportConfiguration]
    static constraints = {
        isFavorite(nullable: true)
    }

    static mapping = {
        table name: "RCONFIG_USER_STATE"

        user column: "RPT_USER_ID"
        isFavorite column: "IS_FAVORITE"
        configuration column: "RCONFIG_ID"
    }

    public String toString() {
        return "$configuration: $user: isFavorite:${isFavorite}"
    }
}
