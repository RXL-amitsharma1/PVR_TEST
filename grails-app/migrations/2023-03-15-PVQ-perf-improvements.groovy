databaseChangeLog = {

    changeSet(author: "sergey", id: "202303152000-4") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "QUALITY_CASE_DATA", columnName: "DUE_DATE")
            }
        }
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "DUE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "sergey", id: "202303152000-5") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "QUALITY_SUBMISSION", columnName: "DUE_DATE")
            }
        }
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "DUE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "sergey", id: "202303152000-6") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "QUALITY_SAMPLING", columnName: "DUE_DATE")
            }
        }
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "DUE_DATE", type: "timestamp") {
                constraints(nullable: "true")
            }
        }
    }
}
