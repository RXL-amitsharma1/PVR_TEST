databaseChangeLog = {
    changeSet(author: "rishabhJ", id: "202204211936") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'PREFERENCE', columnName: 'SIM_CASES')
            }
        }
        addColumn(tableName: "PREFERENCE") {
            column(name: "SIM_CASES", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }
}