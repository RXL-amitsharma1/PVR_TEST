databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1538572672696-1") {
        addColumn(tableName: "USER_GROUP") {
            column(name: "DEFAULT_ASSIGN_TO", type: "number(1,0)")
        }
        sql("update USER_GROUP set DEFAULT_ASSIGN_TO = 0;")
        addNotNullConstraint(tableName: "USER_GROUP", columnName: "DEFAULT_ASSIGN_TO")
    }
}
