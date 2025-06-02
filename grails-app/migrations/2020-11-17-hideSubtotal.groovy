databaseChangeLog = {
    changeSet(author: "sergey", id: "202011171234") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RPT_FIELD_INFO', columnName: 'HIDE_SUBTOTAL')
            }
        }

        addColumn(tableName: "RPT_FIELD_INFO") {
            column(name: "HIDE_SUBTOTAL", type: "number(1,0)")
        }
    }
}