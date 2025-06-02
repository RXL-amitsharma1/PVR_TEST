databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "202210061022-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'SIMPLE_FORM')
            }
        }
        addColumn(tableName: "RCONFIG") {
            column(name: "SIMPLE_FORM", type: "number(1,0)")
        }
        sql("update RCONFIG set SIMPLE_FORM=0");
        addNotNullConstraint(columnDataType: "number(1,0)", columnName: "SIMPLE_FORM", tableName: "RCONFIG")
    }
}
