databaseChangeLog = {

    changeSet(author: "gologuzov (generated)", id: "1478792932290-1") {
        addColumn(tableName: "RPT_FIELD_INFO") {
            column(name: "SUPPRESS_LABEL", type: "number(1,0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE RPT_FIELD_INFO SET SUPPRESS_LABEL = '0'")
                confirm "Successfully set default value for SUPPRESS_LABEL."
            }
        }
        addNotNullConstraint(tableName: "RPT_FIELD_INFO", columnName: "SUPPRESS_LABEL", columnDataType: "number(1,0)")
    }
}
