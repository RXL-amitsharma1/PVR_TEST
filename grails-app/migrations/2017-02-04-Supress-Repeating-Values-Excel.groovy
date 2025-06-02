databaseChangeLog = {

    changeSet(author: "gologuzov (generated)", id: "1486242117417-1") {
        addColumn(tableName: "DTAB_TEMPLT") {
            column(name: "SUPRESS_REPEATING_EXCEL", type: "number(1,0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE DTAB_TEMPLT SET SUPRESS_REPEATING_EXCEL = '0'")
                confirm "Successfully set default value for SUPRESS_REPEATING_EXCEL."
            }
        }
        addNotNullConstraint(tableName: "DTAB_TEMPLT", columnName: "SUPRESS_REPEATING_EXCEL", columnDataType: "number(1,0)")
    }
}
