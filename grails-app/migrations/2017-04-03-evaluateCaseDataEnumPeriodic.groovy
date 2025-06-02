databaseChangeLog = {
    changeSet(author: "sachinverma", id: "1490344171494-1") {
        sql("update RCONFIG set EVALUATE_DATE_AS= 'LATEST_VERSION' where EVALUATE_DATE_AS = 'ALL_VERSIONS' and class ='com.rxlogix.config.PeriodicReportConfiguration'");
    }
}