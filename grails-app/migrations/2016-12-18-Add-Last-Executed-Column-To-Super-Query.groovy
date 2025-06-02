databaseChangeLog = {
    changeSet(author: "emilmatevosyan", id: "1499661247293-53") {
        addColumn(tableName: "SUPER_QUERY") {
            column(name: "LAST_EXECUTED", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }
}