package com.rxlogix.config

import com.rxlogix.enums.JobExecutionHistoryStatusEnum
import com.rxlogix.hibernate.EscapedILikeExpression

class JobExecutionHistory {

    String jobTitle
    Date jobStartRunDate
    Date jobEndRunDate
    JobExecutionHistoryStatusEnum jobRunStatus
    String jobRunRemarks

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        jobRunRemarks nullable: true
        jobEndRunDate nullable: true
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
    }

    static mapping = {
        table name: "JOB_EXECUTION_HISTORY"
        jobTitle column: "JOB_TITLE"
        jobStartRunDate column: "JOB_START_RUN_DATE"
        jobEndRunDate column: "JOB_END_RUN_DATE"
        jobRunStatus column: "JOB_RUN_STATUS"
        jobRunRemarks column: "JOB_RUN_REMARKS"
        version false
    }

    static namedQueries = {

        getAllJobExecutionHistoryBySearchString { String jobTitle, String search ->
            if(jobTitle){
                eq('jobTitle', jobTitle)
            }
            if (search) {
                iLikeWithEscape('jobTitle', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }

    }
}
