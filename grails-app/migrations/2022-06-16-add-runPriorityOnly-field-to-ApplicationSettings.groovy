databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "160620221109-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'APPLICATION_SETTINGS', columnName: 'RUN_PRIORITY_ONLY')
            }
        }
        addColumn(tableName: "APPLICATION_SETTINGS") {
            column(name: "RUN_PRIORITY_ONLY", type: "number(1,0)")
        }
        sql("update APPLICATION_SETTINGS set RUN_PRIORITY_ONLY = 0;")
        addNotNullConstraint(tableName: "APPLICATION_SETTINGS", columnName: "RUN_PRIORITY_ONLY")
    }
}