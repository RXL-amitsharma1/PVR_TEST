databaseChangeLog = {

    changeSet(author: "ShubhamRx", id: "202304101046-01") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RCONFIG", columnName: "DEVICE_REPORTABLE")
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "DEVICE_REPORTABLE", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ShubhamRx", id: "202304101046-02") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "DEVICE_REPORTABLE")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "DEVICE_REPORTABLE", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
    }

}