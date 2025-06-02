databaseChangeLog = {

    changeSet(author: "shubhamRx", id: "20201207053301-07") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RCONFIG", columnName: "AUTO_SUBMIT")
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "AUTO_SUBMIT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "shubhamRx", id: "20201207053301-08") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "AUTO_SUBMIT")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "AUTO_SUBMIT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

}