databaseChangeLog = {

    changeSet(author: "riya", id: "202310101255-1") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'FINAL_EX_ETL_DATE')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "FINAL_EX_ETL_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
        sql("update EX_RCONFIG set FINAL_EX_ETL_DATE = EX_ETL_DATE;")
    }

    changeSet(author: "riya", id: "202310101255-2") {

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'FINAL_LAST_RUN_DATE')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "FINAL_LAST_RUN_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
        sql("update EX_RCONFIG set FINAL_LAST_RUN_DATE = LAST_RUN_DATE;")
    }
}