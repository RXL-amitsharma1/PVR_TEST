databaseChangeLog = {
    changeSet(author: "ShubhamRx (generated)", id: "20210211045040-1") {
        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'EX_RCONFIG', columnName: 'SUBMISSION_DATE_FROM')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "SUBMISSION_DATE_FROM", type: "varchar2(255 char)")
        }
    }
}