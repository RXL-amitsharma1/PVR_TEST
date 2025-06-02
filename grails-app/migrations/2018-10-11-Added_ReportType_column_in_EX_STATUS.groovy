databaseChangeLog = {
    changeSet(author: "Akshay Padghan", id: "1960211999018-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'REPORT_TYPE')
            }
        }
        addColumn(tableName: "EX_STATUS") {
            column(name: "REPORT_TYPE", type: "VARCHAR2(255 CHAR)", defaultValue: ''){
            }
        }
        grailsChange {
            change {
                sql.execute("UPDATE (select * from ex_status a INNER JOIN EX_RCONFIG b ON b.ID= a.executed_entity_id) c SET c.report_type = c.pr_type WHERE c.executed_entity_id IS NOT NULL")
                confirm "Successfully Updated REPORT_TYPE values in Execution Status Table."
            }
        }
    }
}