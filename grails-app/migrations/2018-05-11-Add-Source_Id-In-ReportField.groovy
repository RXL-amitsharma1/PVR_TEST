databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1523257222354-2") {
        addColumn(tableName: "RPT_FIELD") {
            column(name: "SOURCE_ID", type: "number(10,0)")
        }
    }
}