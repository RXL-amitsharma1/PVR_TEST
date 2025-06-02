databaseChangeLog = {

    changeSet(author: "forxsv (generated)", id: "1542792854501-1") {
        addColumn(tableName: "WORKFLOW_RULE") {
            column(name: "EXECUTE_IN_DAYS", type: "number(10,0)"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "forxsv (generated)", id: "1542792854501-2") {
        addColumn(tableName: "WORKFLOW_RULE") {
            column(name: "NEED_APPROVAL", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "forxsv (generated)", id: "1542792854501-3") {
        addColumn(tableName: "WORKFLOW_RULE") {
            column(name: "OWNER", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "forxsv (generated)", id: "1542792854501-4") {
        addForeignKeyConstraint(baseColumnNames: "owner", baseTableName: "WORKFLOW_RULE", constraintName: "FKDFAA27FEF32DC777", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
    }

    changeSet(author: "forxsv (generated)", id: "1543175728873-5") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ARCHIVED", type: "number(1,0)")
        }
        sql("update EX_RCONFIG set ARCHIVED = 0;")
        addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "ARCHIVED")
    }

}
