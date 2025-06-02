databaseChangeLog = {

    changeSet(author: "meenal (generated)", id: "202308081510-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AXWAY_MESSAGES', columnName: 'STATUS')
            }
        }
        addColumn(tableName: "AXWAY_MESSAGES") {
            column(name: "STATUS", type: "varchar2(255 char)")
        }
    }
}