databaseChangeLog = {
    changeSet(author: "emilmatevosyan", id: "1499661247293-52") {
        addColumn(tableName: "RPT_TEMPLT") {
            column(name: "LAST_EXECUTED", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }
}