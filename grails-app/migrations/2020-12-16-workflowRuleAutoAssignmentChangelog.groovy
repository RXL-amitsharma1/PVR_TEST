databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "161220200121-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'WORKFLOW_RULE', columnName: 'ASSIGNMENT_RULE')
            }
        }
        addColumn(tableName: "WORKFLOW_RULE") {
            column(name: "ASSIGNMENT_RULE", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "ASSIGN_TO_USER_GROUP", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "AUTO_ASSIGN_TO_USERS", type: "number(1,0)") {
                constraints(nullable: "true")
            }

            column(name: "ADVANCED_ASSIGNMENT_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update WORKFLOW_RULE set ASSIGN_TO_USER_GROUP = 0;")
        sql("update WORKFLOW_RULE set AUTO_ASSIGN_TO_USERS = 0;")
        addNotNullConstraint(tableName: "WORKFLOW_RULE", columnName: "ASSIGN_TO_USER_GROUP")
        addNotNullConstraint(tableName: "WORKFLOW_RULE", columnName: "AUTO_ASSIGN_TO_USERS")
    }

    changeSet(author: "anurag (generated)", id: "181220200121-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'WORKFLOW_RULE', columnName: 'DUE_IN_DAYS')
            }
        }
        addColumn(tableName: "WORKFLOW_RULE") {
            column(name: "DUE_IN_DAYS", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update WORKFLOW_RULE set DUE_IN_DAYS = 0;")
    }
}