databaseChangeLog = {

    changeSet(author: "ShubhamRx (generated)", id: "202309281553-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'INCLUDE_OPEN_CASES')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "INCLUDE_OPEN_CASES", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ShubhamRx (generated)", id: "202309281553-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'INCLUDE_OPEN_CASES')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "INCLUDE_OPEN_CASES", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }
}