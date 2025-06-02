databaseChangeLog = {

    changeSet(author: "meenal (generated)", id: "202311081451-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'EX_ICSR_TEMPLT_QUERY', columnName: 'MSG_TYPE')
        }
        dropColumn(columnName: "MSG_TYPE", tableName: "EX_ICSR_TEMPLT_QUERY")
    }

    changeSet(author: "meenal (generated)", id: "202311081451-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ICSR_TEMPLT_QUERY', columnName: 'MSG_TYPE')
        }
        dropColumn(columnName: "MSG_TYPE", tableName: "ICSR_TEMPLT_QUERY")
    }
}
