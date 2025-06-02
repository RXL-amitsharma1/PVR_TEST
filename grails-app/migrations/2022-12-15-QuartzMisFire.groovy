databaseChangeLog = {
    changeSet(author: "Sachin Verma", id: "2022121501041-1") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: "QRTZ_TRIGGERS")
        }
        sql("UPDATE QRTZ_TRIGGERS SET MISFIRE_INSTR=4 where JOB_NAME='com.rxlogix.jobs.ComparisonJob'")
        sql("UPDATE QRTZ_TRIGGERS SET MISFIRE_INSTR=2 where JOB_NAME " +
                "in ('com.rxlogix.jobs.ICSRCaseGenerateDataJob'," +
                "'com.rxlogix.jobs.IcsrManualCaseGenerateJob'," +
                "'com.rxlogix.jobs.ICSRProfileAckJob'," +
                "'com.rxlogix.jobs.ICSRScheduleExecutionJob'," +
                "'com.rxlogix.jobs.ICSRScheduleProcessingJob'," +
                "'com.rxlogix.jobs.ReportsExecutorJob'," +
                "'com.rxlogix.jobs.ScheduleCaseSeriesJob'," +
                "'com.rxlogix.jobs.ScheduledReportJob')");
    }
}
