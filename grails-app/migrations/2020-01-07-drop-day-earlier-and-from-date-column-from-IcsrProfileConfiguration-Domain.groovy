databaseChangeLog = {
    changeSet(author: "sargam (generated)", id: "070120200545-1") {
        dropColumn(columnName: "DAY_EARLIER_HOLIDAY", tableName: "RCONFIG")
        dropColumn(columnName: "FROM_DATE", tableName: "RCONFIG")
        dropColumn(columnName: "DAY_EARLIER_HOLIDAY", tableName: "EX_RCONFIG")
        dropColumn(columnName: "FROM_DATE", tableName: "EX_RCONFIG")
    }
}