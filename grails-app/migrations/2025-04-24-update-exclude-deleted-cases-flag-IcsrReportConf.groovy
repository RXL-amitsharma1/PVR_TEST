databaseChangeLog = {

    changeSet(author: "meenal", id: "202504241733-1") {
        sql("Update RCONFIG set EXCLUDE_DELETED_CASES = 0 where class IN ('com.rxlogix.config.IcsrReportConfiguration', 'com.rxlogix.config.IcsrProfileConfiguration')")
    }
}
