databaseChangeLog = {
    changeSet(author: "mudasir", id: "202007205997-1") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "DASHBOARD", columnName: "PARENT_ID")
            }
        }
        addColumn(tableName: "DASHBOARD") {
            column(name: "PARENT_ID", type: "NUMBER(10)") {
                constraints(nullable: "true")
            }
        }
    }
}