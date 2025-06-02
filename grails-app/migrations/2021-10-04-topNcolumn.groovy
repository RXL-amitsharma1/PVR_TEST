databaseChangeLog = {

    changeSet(author: "sergey", id: "202110040001-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DTAB_MEASURE', columnName: 'TOP_TYPE')
            }
        }
        addColumn(tableName: "DTAB_MEASURE") {
            column(name: "TOP_TYPE", type: "VARCHAR2(255)") {
                constraints(nullable: "true")
            }
        }
        addColumn(tableName: "DTAB_MEASURE") {
            column(name: "TOP_N", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
}

