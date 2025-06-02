databaseChangeLog = {

    changeSet(author: "Aman Deep (generated)", id: "202105122306001-1") {

        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                columnExists(tableName: 'WORKFLOW_JUSTIFICATION', columnName: 'ASSIGNED_TO_USER_ID')
            }
        }
        addColumn(tableName: "WORKFLOW_JUSTIFICATION") {
            column(name: "ASSIGNED_TO_USER_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "Aman Deep (generated)", id: "202105122306001-2") {

        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                columnExists(tableName: 'WORKFLOW_JUSTIFICATION', columnName: 'ASSIGNED_TO_USERGROUP_ID')
            }
        }
        addColumn(tableName: "WORKFLOW_JUSTIFICATION") {
            column(name: "ASSIGNED_TO_USERGROUP_ID", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }
}