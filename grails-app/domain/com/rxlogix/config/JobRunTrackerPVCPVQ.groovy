package com.rxlogix.config

class JobRunTrackerPVCPVQ {

    String jobName
    Date lastRunDate
    Date dateCreated
    Date lastUpdated

    static mapping = {
        table name: "JOB_RUN_TRACKER_PVC_PVQ"
        id name: 'jobName', column: "JOB_NAME", generator: 'assigned'
        jobName column: "JOB_NAME"
        lastRunDate column: "LAST_RUN_DATE"
        version false
    }

    static constraints = {
        jobName nullable: false, unique: true
        lastRunDate nullable: true
    }

    public String toString() {
        return "$jobName - $lastRunDate"
    }

}
