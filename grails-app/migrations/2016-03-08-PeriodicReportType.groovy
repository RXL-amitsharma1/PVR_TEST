databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1457473100214-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'PR_TYPE')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "PR_TYPE", type: "varchar2(255 char)")
        }
        sql("update EX_RCONFIG exrconfig set exrconfig.PR_TYPE='OTHER' where class='com.rxlogix.config.ExecutedPeriodicReportConfiguration'");
    }

    changeSet(author: "sachinverma (generated)", id: "1457473100214-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'PR_TYPE')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "PR_TYPE", type: "varchar2(255 char)")
        }
        sql("update RCONFIG rconfig set rconfig.PR_TYPE='OTHER' where class='com.rxlogix.config.PeriodicReportConfiguration'")
    }
}
