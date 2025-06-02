databaseChangeLog = {
    changeSet(author: "anurag (generated)", id: "202310031321-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'UNIT_CONFIGURATION', columnName: 'HOLDER_ID')
            }
        }
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "HOLDER_ID", type: "varchar2(200 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag (generated)", id: "202310031321-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'HOLDER_ID')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "HOLDER_ID", type: "varchar2(200 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag (generated)", id: "202310031321-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'HOLDER_ID')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "HOLDER_ID", type: "varchar2(200 char)") {
                constraints(nullable: "true")
            }
        }
    }
}