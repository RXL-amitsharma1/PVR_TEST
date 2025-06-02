databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1516953353813-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'USER_GROUP', columnName: 'DATA_PROTECTION_QUERY_ID')
            }
        }
        addColumn(tableName: "USER_GROUP") {
            column(name: "DATA_PROTECTION_QUERY_ID", type: "number(19,0)")
        }
    }

}
