databaseChangeLog = {

    changeSet(author: "sergey", id: "202301213210-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'WORKFLOW_RULE', columnName: 'AE_EXCLUDE_WEEKENDS')
            }
        }
        addColumn(tableName: "WORKFLOW_RULE") {
            column(name: "AE_EXCLUDE_WEEKENDS", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update WORKFLOW_RULE set AE_EXCLUDE_WEEKENDS = 0;")
    }

}

