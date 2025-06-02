package com.rxlogix.config

import com.rxlogix.enums.KillStatusEnum

class ReportExecutionKillRequest {

    Long executionStatusId
    KillStatusEnum killStatus = KillStatusEnum.NEW
    Date dateCreated
    Date lastUpdated

    static constraints = {
    }

    static mapping = {
        table name:"REPORT_EXECUTION_KILL_REQUEST"
        executionStatusId column: "EXECUTION_STATUS_ID"
        killStatus column:  "KILL_STATUS"
    }
}
