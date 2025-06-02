databaseChangeLog = {

    changeSet(author: "chetansharma (generated)", id: "1511427419488-1") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "signal_configuration", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "chetansharma (generated)", id: "1511427419488-2") {
        addColumn(tableName: "RCONFIG") {
            column(name: "signal_configuration", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }
}
