databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1522919698068-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'IS_TEMPLATE')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_TEMPLATE", type: "number(1,0)")
        }
        sql("update RCONFIG set IS_TEMPLATE=0");
        addNotNullConstraint(columnDataType: "number(1,0)", columnName: "IS_TEMPLATE", tableName: "RCONFIG")
    }
}
