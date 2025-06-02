databaseChangeLog = {
    changeSet(author: "jitin (generated)", id: "1760211799018-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'SUPER_QUERY', columnName: 'QUERY_TARGET')
            }
        }

        addColumn(tableName: "SUPER_QUERY") {
            column(name: "QUERY_TARGET", type: "VARCHAR2(255 CHAR)", defaultValue: "REPORTS")
        }
    }
}
