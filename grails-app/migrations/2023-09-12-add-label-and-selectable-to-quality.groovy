databaseChangeLog = {

    changeSet(author: "riya", id: "202312091603-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_FIELD', columnName: 'LABEL')
            }
        }
        addColumn(tableName: "QUALITY_FIELD") {
            column(name: "LABEL", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "riya", id: "202312091603-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'QUALITY_FIELD', columnName: 'IS_SELECTABLE')
            }
        }
        addColumn(tableName: "QUALITY_FIELD") {
            column(name: "IS_SELECTABLE", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }
}

