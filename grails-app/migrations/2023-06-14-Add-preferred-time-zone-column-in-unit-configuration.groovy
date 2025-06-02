databaseChangeLog = {

    changeSet(author: "ShubhamRx", id: "202307201733-01") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "UNIT_CONFIGURATION", columnName: "PREFERRED_TIME_ZONE")
            }
        }
        addColumn(tableName: "UNIT_CONFIGURATION") {
            column(name: "PREFERRED_TIME_ZONE", type: "VARCHAR(1024)", defaultValue: 'UTC')
        }
    }

    changeSet(author: "ShubhamRx", id: "202307201734-01") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "PREFERRED_TIME_ZONE")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "PREFERRED_TIME_ZONE", type: "VARCHAR(1024)", defaultValue: 'UTC')
        }
    }

}


