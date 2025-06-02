databaseChangeLog = {

    changeSet(author: "anurag", id: "202104290210-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'WORKFLOW_RULE', columnName: 'EXCLUDE_WEEKENDS')
            }
        }
        addColumn(tableName: "WORKFLOW_RULE") {
            column(name: "EXCLUDE_WEEKENDS", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update WORKFLOW_RULE set EXCLUDE_WEEKENDS = 0;")
    }

}

