databaseChangeLog = {
    changeSet(author: "ShubhamRx (generated)", id: "20210222094606-1") {
        preConditions(onFail: 'MARK_RAN') {
            not{
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'ASSOCIATED_SPOTFIRE_FILE')
            }
        }

        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "ASSOCIATED_SPOTFIRE_FILE", type: "varchar2(1024 char)")
        }
    }
}