databaseChangeLog = {

    changeSet(author: "sachins", id: "2021090120009-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUTO_ASSIGNMENT', columnName: 'VERSION_NUM')
            }
        }
        addColumn(tableName: "AUTO_ASSIGNMENT") {
            column(name: "VERSION_NUM", type: "NUMBER(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "sachins", id: "2021090120009-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'AUTO_ASSIGNMENT', columnName: 'TYPE')
            }
        }
        addColumn(tableName: "AUTO_ASSIGNMENT") {
            column(name: "TYPE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
}

