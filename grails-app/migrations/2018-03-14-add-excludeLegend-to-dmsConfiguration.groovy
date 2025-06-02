databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1521032226068-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DMS_CONFIGURATION', columnName: 'EXCLUDE_LEGEND')
            }
        }
        addColumn(tableName: "DMS_CONFIGURATION") {
            column(name: "EXCLUDE_LEGEND", type: "number(1,0)")
        }
    }
}
