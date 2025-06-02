databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "21012020202358-1") {
        createTable(tableName: "WORKFlOW_ASSIGNED_TO_USER") {
            column(name: "WORKFLOW_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ASSIGNED_TO_USER_ID", type: "number(19,0)")

            column(name: "ASSIGNED_TO_USER_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "21012020202358-2") {
        createTable(tableName: "WORKFlOW_ASSIGNED_TO_USERGROUP") {
            column(name: "WORKFLOW_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "ASSIGNED_TO_USERGROUP_ID", type: "number(19,0)")

            column(name: "ASSIGNED_TO_USERGROUP_IDX", type: "number(10,0)")
        }
    }

    changeSet(author: "forxsv (generated)", id: "21012020202358-3") {
        addForeignKeyConstraint(baseColumnNames: "ASSIGNED_TO_USER_ID", baseTableName: "WORKFlOW_ASSIGNED_TO_USER", constraintName: "FK_lfidoka6mqiikyavqgsqgvk3q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "21012020202358-4") {
        addForeignKeyConstraint(baseColumnNames: "ASSIGNED_TO_USERGROUP_ID", baseTableName: "WORKFlOW_ASSIGNED_TO_USERGROUP", constraintName: "FK_4ovqiwmqkflm34m62iqcq87h5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }

}