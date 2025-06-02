databaseChangeLog = {
    changeSet(author: "sachins", id: "2020-09-28-1003") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RPT_TEMPLT', columnName: 'INTERACTIVE_OUTPUT')
            }
        }

        addColumn(tableName: "RPT_TEMPLT") {
            column(name: "INTERACTIVE_OUTPUT", type: "number(1,0)", defaultValue: 0) {
                constraints(nullable: "true")
            }
        }
        addNotNullConstraint(tableName: "RPT_TEMPLT", columnName: "INTERACTIVE_OUTPUT")
    }
}