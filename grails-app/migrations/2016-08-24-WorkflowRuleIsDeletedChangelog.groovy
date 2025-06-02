databaseChangeLog = {

    changeSet(author: "Prashant (generated)", id: "1472043480121-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'WORKFLOW_RULE', columnName: 'IS_DELETED')
            }
        }
        addColumn(tableName: "WORKFLOW_RULE") {
            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update WORKFLOW_RULE set IS_DELETED = 0;")
        addNotNullConstraint(tableName: "WORKFLOW_RULE", columnName: "IS_DELETED")
    }

}