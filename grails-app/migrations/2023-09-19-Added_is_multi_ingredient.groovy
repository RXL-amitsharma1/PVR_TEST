databaseChangeLog = {

    changeSet(author: "ShivamRx (generated)", id: "202309191434-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_MULTI_INGREDIENT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ShivamRx (generated)", id: "202309191434-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "IS_MULTI_INGREDIENT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }
}
