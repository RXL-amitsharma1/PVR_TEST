databaseChangeLog = {
    changeSet(author: "sahil", id: "202502181221-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'CASE_ID')
            }
        }
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "CASE_ID", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sahil", id: "202502181221-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SUBMISSION', columnName: 'CASE_ID')
            }
        }
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "CASE_ID", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sahil", id: "202502181221-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_SAMPLING', columnName: 'CASE_ID')
            }
        }
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "CASE_ID", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }
}