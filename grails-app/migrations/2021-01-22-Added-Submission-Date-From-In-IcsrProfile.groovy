databaseChangeLog = {
    changeSet(author: "ShubhamRx (generated)", id: "20210122014440-1") {
        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'RCONFIG', columnName: 'SUBMISSION_DATE_FROM')
            }
        }

        addColumn(tableName: "RCONFIG") {
            column(name: "SUBMISSION_DATE_FROM", type: "varchar2(255 char)")
        }
    }
}