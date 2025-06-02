databaseChangeLog = {
    changeSet(author: "sachinverma ", id: "1490344171483-1") {
        sql("update RCONFIG set PRIMARY_DESTINATION= 'FDA' where PRIMARY_DESTINATION is null and class ='com.rxlogix.config.PeriodicReportConfiguration'");
        sql("update EX_RCONFIG set PRIMARY_DESTINATION= 'FDA' where PRIMARY_DESTINATION is null and class ='com.rxlogix.config.ExecutedPeriodicReportConfiguration'");
    }
}