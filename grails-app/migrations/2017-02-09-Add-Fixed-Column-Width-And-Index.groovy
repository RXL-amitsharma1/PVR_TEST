databaseChangeLog = {

    changeSet(author: "gologuzov (generated)", id: "1486653279243-1") {
        addColumn(tableName: "RPT_FIELD") {
            column(name: "FIXED_WIDTH", type: "number(10,0)")
        }
    }

    changeSet(author: "gologuzov (generated)", id: "1486653279243-2") {
        addColumn(tableName: "RPT_FIELD") {
            column(name: "WIDTH_PROPORTION_INDEX", type: "number(10,0)")
        }
    }

    changeSet(author: "gologuzov (generated)", id: "1486653279243-53") {
        dropColumn(columnName: "MIN_COLUMNS", tableName: "SOURCE_COLUMN_MASTER")
    }
}
