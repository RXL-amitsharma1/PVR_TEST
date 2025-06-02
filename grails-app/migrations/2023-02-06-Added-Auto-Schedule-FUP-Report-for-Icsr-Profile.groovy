databaseChangeLog = {

    changeSet(author: "Meenal", id: "202302061132-01") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RCONFIG", columnName: "AUTO_SCHEDULE_FUP_REPORT")
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "AUTO_SCHEDULE_FUP_REPORT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Meenal", id: "202302061132-02") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "EX_RCONFIG", columnName: "AUTO_SCHEDULE_FUP_REPORT")
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "AUTO_SCHEDULE_FUP_REPORT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }
    }

}