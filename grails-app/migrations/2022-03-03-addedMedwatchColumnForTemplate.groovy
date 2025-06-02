databaseChangeLog = {

    changeSet(author: "Shubham Sharma", id: "20220303034334-7") {
        preConditions(onFail: "MARK_RAN") {
            not {
                columnExists(tableName: "RPT_TEMPLT", columnName: "MEDWATCH_TEMPLATE")
            }
        }

        addColumn(tableName: "RPT_TEMPLT") {
            column(name: "MEDWATCH_TEMPLATE", type: "number(1,0)")
        }

        grailsChange {
            change {
                sql.execute("UPDATE RPT_TEMPLT SET MEDWATCH_TEMPLATE = '0'")
                confirm "Successfully set default value for RPT_TEMPLT."
            }
        }

        addNotNullConstraint(tableName: "RPT_TEMPLT", columnName: "MEDWATCH_TEMPLATE", columnDataType: "number(1,0)")

    }

    changeSet(author: "Shubham Sharma", id: "20220303034334-8") {
        sql("""UPDATE RPT_TEMPLT SET MEDWATCH_TEMPLATE = '1' WHERE name ='Medwatch Template';""")
    }
}