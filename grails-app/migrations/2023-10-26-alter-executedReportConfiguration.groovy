databaseChangeLog = {

    changeSet(author: "meenal (generated)", id: "202309291501-6") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "RECIPIENT_TYPE_ID")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "RECIPIENT_TYPE_ID", type: "number(19,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "meenal (generated)", id: "202309291501-7") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "SENDER_TYPE_ID")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "SENDER_TYPE_ID", type: "number(19,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

}
