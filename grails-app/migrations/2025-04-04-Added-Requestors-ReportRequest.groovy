databaseChangeLog = {

    changeSet(author: "sergey", id: "202504101220-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'REQUESTORS_NAMES')
            }
        }
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "REQUESTORS_NAMES", type: "varchar2(32000)") {
                constraints(nullable: "true")
            }
        }
    }

}