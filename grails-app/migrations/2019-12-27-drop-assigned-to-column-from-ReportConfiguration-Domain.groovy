databaseChangeLog = {
    changeSet(author: "sargam (generated)", id: "271220190517-1") {
        dropColumn(columnName: "ASSIGNED_TO", tableName: "RCONFIG")
        dropColumn(columnName: "ASSIGNED_GROUP_TO", tableName: "RCONFIG")
        dropColumn(columnName: "ASSIGNED_TO", tableName: "EX_RCONFIG")
        dropColumn(columnName: "ASSIGNED_GROUP_TO", tableName: "EX_RCONFIG")
    }
}