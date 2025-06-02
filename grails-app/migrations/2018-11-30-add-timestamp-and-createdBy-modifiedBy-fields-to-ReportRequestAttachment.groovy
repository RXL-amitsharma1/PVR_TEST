databaseChangeLog = {

    changeSet(author: "sargam (generated)", id: "1542792854517-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST_ATTACH', columnName: 'CREATED_BY')
            }
        }
        addColumn(tableName: "REPORT_REQUEST_ATTACH") {
            column(name: "CREATED_BY", type: "varchar2(255 char)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sargam (generated)", id: "1542792854517-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST_ATTACH', columnName: 'MODIFIED_BY')
            }
        }
        addColumn(tableName: "REPORT_REQUEST_ATTACH") {
            column(name: "MODIFIED_BY", type: "varchar2(255 char)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sargam (generated)", id: "1542792854517-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST_ATTACH', columnName: 'DATE_CREATED')
            }
        }
        addColumn(tableName: "REPORT_REQUEST_ATTACH") {
            column(name: "DATE_CREATED", type: "timestamp"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sargam (generated)", id: "1542792854517-5") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'REPORT_REQUEST_ATTACH', columnName: 'LAST_UPDATED')
            }
        }
        addColumn(tableName: "REPORT_REQUEST_ATTACH") {
            column(name: "LAST_UPDATED", type: "timestamp"){
                constraints(nullable: "true")
            }
        }
    }
}