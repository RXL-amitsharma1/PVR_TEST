databaseChangeLog = {

    changeSet(author: "Prashant (generated)", id: "1471953707334-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'IS_DELETED')
            }
        }
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update REPORT_REQUEST set IS_DELETED = 0;")
        addNotNullConstraint(tableName: "REPORT_REQUEST", columnName: "IS_DELETED")
    }

}