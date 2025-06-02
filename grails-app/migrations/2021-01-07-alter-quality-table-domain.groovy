databaseChangeLog = {
    changeSet(author: "anurag", id: "21010720202358-1") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'QUALITY_CASE_DATA', columnName: 'ASSIGNED_USER')
        }
        dropColumn(columnName: "ASSIGNED_USER", tableName: "QUALITY_CASE_DATA")
        addColumn(tableName: "QUALITY_CASE_DATA") {
            column(name: "ASSIGNED_TO_USER", type: "number(19, 0)"){
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_TO_USERGROUP", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag", id: "21010720202358-2") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'QUALITY_SUBMISSION', columnName: 'ASSIGNED_USER')
        }
        dropColumn(columnName: "ASSIGNED_USER", tableName: "QUALITY_SUBMISSION")
        addColumn(tableName: "QUALITY_SUBMISSION") {
            column(name: "ASSIGNED_TO_USER", type: "number(19, 0)"){
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_TO_USERGROUP", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "anurag", id: "21010720202358-3") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'QUALITY_SAMPLING', columnName: 'ASSIGNED_USER')
        }
        dropColumn(columnName: "ASSIGNED_USER", tableName: "QUALITY_SAMPLING")
        addColumn(tableName: "QUALITY_SAMPLING") {
            column(name: "ASSIGNED_TO_USER", type: "number(19, 0)"){
                constraints(nullable: "true")
            }

            column(name: "ASSIGNED_TO_USERGROUP", type: "number(19, 0)"){
                constraints(nullable: "true")
            }
        }
    }
}