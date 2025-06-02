databaseChangeLog = {
    changeSet(author: "sergey", id: "20231024111111-1") {
        addColumn(tableName: "CLL_TEMPLT") {
            column(name: "COL_HIDE_ROW_COUNT", type: "number(1,0)")
        }
    }
}
