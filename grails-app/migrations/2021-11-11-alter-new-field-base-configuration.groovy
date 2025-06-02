databaseChangeLog = {
    changeSet(author: "anurag (generated)", id: "202111110817-1") {
        addColumn(tableName: "RCONFIG") {
            column(name: "IS_PRIORITY_REPORT", type: "number(1,0)")
        }
        sql("update RCONFIG set IS_PRIORITY_REPORT = 0;")
        addNotNullConstraint(tableName: "RCONFIG", columnName: "IS_PRIORITY_REPORT")
    }

    changeSet(author: "anurag (generated)", id: "202111110817-2") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "IS_PRIORITY_REPORT", type: "number(1,0)")
        }
        sql("update EX_RCONFIG set IS_PRIORITY_REPORT = 0;")
        addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "IS_PRIORITY_REPORT")
    }

    changeSet(author: "anurag (generated)", id: "202111120817-3") {
        addColumn(tableName: "EX_STATUS") {
            column(name: "IS_PRIORITY_REPORT", type: "number(1,0)")
        }
        sql("update EX_STATUS set IS_PRIORITY_REPORT = 0;")
        addNotNullConstraint(tableName: "EX_STATUS", columnName: "IS_PRIORITY_REPORT")
    }
}