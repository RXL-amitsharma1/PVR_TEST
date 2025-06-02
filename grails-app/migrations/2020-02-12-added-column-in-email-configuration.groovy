databaseChangeLog = {
    changeSet(author: "anurag", id: "202002125997-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EMAIL_CONFIGURATION", columnName: "EMAIL_TO")
            }
        }
        addColumn(tableName: "EMAIL_CONFIGURATION") {
            column(name: "EMAIL_TO", type: "CLOB")
        }
    }
}