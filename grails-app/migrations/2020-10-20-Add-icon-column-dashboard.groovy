databaseChangeLog = {
    changeSet(author: "shikhars", id: "202010201453") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'DASHBOARD', columnName: 'ICON')
            }
        }

        addColumn(tableName: "DASHBOARD") {
            column(name: "ICON", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
    }
}