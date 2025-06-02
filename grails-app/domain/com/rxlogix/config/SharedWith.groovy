package com.rxlogix.config

import com.rxlogix.enums.ReportResultStatusEnum
import com.rxlogix.user.User

class SharedWith {
    User user
    boolean isDeleted = false

    static belongsTo = [executedConfiguration: ExecutedReportConfiguration]
    static constraints = {
    }

    static mapping = {
        table name: "SHARED_WITH"

        user column: "RPT_USER_ID"
        isDeleted column: "IS_DELETED"
        executedConfiguration column: "EX_RCONFIG_ID"
    }

    public String toString() {
        return user.getFullNameAndUserName()
    }
}
