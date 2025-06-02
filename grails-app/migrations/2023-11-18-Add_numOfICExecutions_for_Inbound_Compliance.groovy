databaseChangeLog = {

    changeSet(author: "gunjan", id: "202311181105") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'INBOUND_COMPLIANCE', columnName: 'NUM_OF_IC_EXECUTIONS')
            }
        }
        addColumn(tableName: "INBOUND_COMPLIANCE") {
            column(name: "NUM_OF_IC_EXECUTIONS", type: "NUMBER(10,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "gunjan", id: "202311181130") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_INBOUND_COMPLIANCE', columnName: 'NUM_OF_IC_EXECUTIONS')
            }
        }
        addColumn(tableName: "EX_INBOUND_COMPLIANCE") {
            column(name: "NUM_OF_IC_EXECUTIONS", type: "NUMBER(19,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }
}