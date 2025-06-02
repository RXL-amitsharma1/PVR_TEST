databaseChangeLog = {
    changeSet(author: "shikhars", id: "231220200317") {
        addColumn(tableName: "DTAB_TEMPLT") {
            column(name: "QUERY", type: "clob")
        }
    }
}