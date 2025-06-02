databaseChangeLog = {
    changeSet(author: "meenal (generated)", id: "202403151305-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'SENDER_HOLDER_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "SENDER_HOLDER_ID", type: "varchar2(200 char)") {
                constraints(nullable: "true")
            }
        }
    }
}