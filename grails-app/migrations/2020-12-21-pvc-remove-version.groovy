databaseChangeLog = {
    changeSet(author: "Sergey (generated)", id: "202012211201-1") {
        dropColumn(columnName: "CASE_VERSION", tableName: "DRILLDOWN_METADATA")
    }
}