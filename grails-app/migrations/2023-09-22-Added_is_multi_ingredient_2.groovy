databaseChangeLog = {

    changeSet(author: "ShivamRx (generated)", id: "202309221439-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'CASE_SERIES', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "CASE_SERIES") {
            column(name: "IS_MULTI_INGREDIENT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ShivamRx (generated)", id: "202309221439-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "IS_MULTI_INGREDIENT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ShivamRx (generated)", id: "202309221440-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'INBOUND_COMPLIANCE', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "INBOUND_COMPLIANCE") {
            column(name: "IS_MULTI_INGREDIENT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ShivamRx (generated)", id: "202309221440-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_INBOUND_COMPLIANCE', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "EX_INBOUND_COMPLIANCE") {
            column(name: "IS_MULTI_INGREDIENT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ShivamRx (generated)", id: "202309221445") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "IS_MULTI_INGREDIENT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }
}

