databaseChangeLog = {

    changeSet(author: "Rishabh Jain", id: "202203281441-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "EX_ETL_DATE")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "EX_ETL_DATE", type: "timestamp", defaultValue: null)
        }
    }
}