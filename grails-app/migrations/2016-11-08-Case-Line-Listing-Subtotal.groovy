databaseChangeLog = {
    changeSet(author: "gologuzov (generated)", id: "1478612438706-1") {
        addColumn(tableName: "CLL_TEMPLT") {
            column(name: "COL_SHOW_SUBTOTAL", type: "number(1,0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE CLL_TEMPLT SET COL_SHOW_SUBTOTAL = '0'")
                confirm "Successfully set default value for COL_SHOW_SUBTOTAL."
            }
        }
        addNotNullConstraint(tableName: "CLL_TEMPLT", columnName: "COL_SHOW_SUBTOTAL", columnDataType: "number(1,0)")
    }
}
