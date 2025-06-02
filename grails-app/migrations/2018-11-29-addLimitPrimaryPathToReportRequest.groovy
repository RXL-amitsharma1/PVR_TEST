databaseChangeLog = {
    changeSet(author: "jitin (generated)", id: "2960211999018-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'LIMIT_PRIMARY_PATH')
            }
        }
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "LIMIT_PRIMARY_PATH", type: "NUMBER(1)", defaultValue: 0){
                constraints(nullable: "false")
            }
        }
    }
}