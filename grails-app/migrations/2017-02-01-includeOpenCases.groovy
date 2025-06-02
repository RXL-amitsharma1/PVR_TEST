databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1466362752523-02") {
        addColumn(tableName: "RCONFIG") {
            column(name: "INCLUDE_OPEN_CASES_DRAFT", type: "number(1,0)")
        }
        sql("update RCONFIG set INCLUDE_OPEN_CASES_DRAFT = 0 where class='com.rxlogix.config.PeriodicReportConfiguration'")
    }

    changeSet(author: "sachinverma (generated)", id: "1466362752523-01") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "INCLUDE_OPEN_CASES_DRAFT", type: "number(1,0)")
        }
        sql("update EX_RCONFIG set INCLUDE_OPEN_CASES_DRAFT = 0 where class='com.rxlogix.config.ExecutedPeriodicReportConfiguration'")
    }

}