package com.rxlogix.config

import com.rxlogix.user.User

class TemplateUserState {
    User user
    Boolean isFavorite

    static belongsTo = [template: ReportTemplate]
    static constraints = {
        isFavorite(nullable: true)
    }

    static mapping = {
        table name: "TEMPLATE_USER_STATE"

        user column: "RPT_USER_ID"
        isFavorite column: "IS_FAVORITE"
        template column: "TEMPLATE_ID"
        user cascade: 'none'
    }

    public String toString() {
        return "$template - $user"
    }
}
