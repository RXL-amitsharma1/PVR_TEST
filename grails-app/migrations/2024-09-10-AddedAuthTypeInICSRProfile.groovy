databaseChangeLog = {

    changeSet(author:"ShubhamRx" , id: "202409101247-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'AUTH_TYPE')
            }
        }

        addColumn(tableName: "RCONFIG") {
            column(name: "AUTH_TYPE", type: "number", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author:"ShubhamRx" , id: "202409101247-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'AUTH_TYPE')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "AUTH_TYPE", type: "number", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "ShubhamRx", id: "202409101247-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'AUTH_NAME')
            }
        }

        addColumn(tableName: "EX_RCONFIG") {
            column(name: "AUTH_NAME", type: "varchar2(255 char)", defaultValue: null) {
                constraints(nullable: "true")
            }
        }
    }
}
