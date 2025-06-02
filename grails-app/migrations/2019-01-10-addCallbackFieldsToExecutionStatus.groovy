databaseChangeLog = {
    changeSet(author: "jitin (generated)", id: "1960211799018-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'CALLBACK_URL')
            }
        }

        addColumn(tableName: "EX_STATUS") {
            column(name: "CALLBACK_URL", type: "VARCHAR2(255 CHAR)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "jitin (generated)", id: "1960211799018-7") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_STATUS', columnName: 'CALLBACK_STATUS')
            }
        }

        addColumn(tableName: "EX_STATUS") {
            column(name: "CALLBACK_STATUS", type: "VARCHAR2(255 CHAR)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "jitin (generated)", id: "1960211799018-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_CASE_SERIES', columnName: 'CASE_SERIES_OWNER')
            }
        }

        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "CASE_SERIES_OWNER", type: "VARCHAR2(255 CHAR)", defaultValue: "PVR"){
                constraints(nullable: "false")
            }
        }
    }
}