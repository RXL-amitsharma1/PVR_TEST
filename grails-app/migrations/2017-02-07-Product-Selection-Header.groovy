databaseChangeLog = {

    changeSet(author: "gologuzov (generated)", id: "1486464286483-1") {
        addColumn(tableName: "EX_TEMPLT_QUERY") {
            column(name: "HEADER_PRODUCT_SELECTION", type: "number(1,0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE EX_TEMPLT_QUERY SET HEADER_PRODUCT_SELECTION = '0'")
                confirm "Successfully set default value for HEADER_PRODUCT_SELECTION."
            }
        }
        addNotNullConstraint(tableName: "EX_TEMPLT_QUERY", columnName: "HEADER_PRODUCT_SELECTION", columnDataType: "number(1,0)")
    }

    changeSet(author: "gologuzov (generated)", id: "1486464286483-2") {
        addColumn(tableName: "TEMPLT_QUERY") {
            column(name: "HEADER_PRODUCT_SELECTION", type: "number(1,0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE TEMPLT_QUERY SET HEADER_PRODUCT_SELECTION = '0'")
                confirm "Successfully set default value for HEADER_PRODUCT_SELECTION."
            }
        }
    }
}
