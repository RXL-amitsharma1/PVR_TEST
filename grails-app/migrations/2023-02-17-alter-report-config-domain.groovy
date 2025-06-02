databaseChangeLog = {

    changeSet(author: "anurag", id: "102302171813-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'LOCAL_CP_REQUIRED')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "LOCAL_CP_REQUIRED", type: "NUMBER(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "anurag", id: "102302171813-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'LOCAL_CP_REQUIRED')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "LOCAL_CP_REQUIRED", type: "NUMBER(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }
}