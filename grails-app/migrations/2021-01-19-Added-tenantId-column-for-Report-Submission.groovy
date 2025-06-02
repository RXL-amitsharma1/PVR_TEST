databaseChangeLog = {
    changeSet(author: "ShubhamRx(generated)", id: "20210119043257-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RPT_SUBMISSION', columnName: 'TENANT_ID')
            }
        }

        addColumn(tableName: "RPT_SUBMISSION") {
            column(name: "TENANT_ID", type: "NUMBER", defaultValue: "1")
        }
    }
}