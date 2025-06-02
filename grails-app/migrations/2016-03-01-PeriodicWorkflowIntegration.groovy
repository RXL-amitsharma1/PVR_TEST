databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1456866634530-1") {
        createTable(tableName: "WORKFLOW_STATE_REPORT_ACTIONS") {
            column(name: "WORKFLOW_STATE_ID", type: "number(19,0)")

            column(name: "REPORT_ACTION_ENUM", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1456866634530-3") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "GENERATED_CASES_DATA", type: "number(1,0)") {
                constraints(nullable: "true")
            }
        }
        sql("update EX_RCONFIG set GENERATED_CASES_DATA = 0")
        addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "GENERATED_CASES_DATA")
    }

    changeSet(author: "sachinverma (generated)", id: "1456866634530-4") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "WORKFLOW_STATE_ID", type: "number(19,0)")
        }
    }


    changeSet(author: "sachinverma (generated)", id: "1456866634530-16") {
        dropColumn(columnName: "REPORT_ACTIONS", tableName: "WORKFLOW_STATE")
    }

    changeSet(author: "sachinverma (generated)", id: "1456866634530-11") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_STATE_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE884B2AF5FD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "WORKFLOW_STATE", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1456866634530-12") {
        addForeignKeyConstraint(baseColumnNames: "WORKFLOW_STATE_ID", baseTableName: "WORKFLOW_STATE_REPORT_ACTIONS", constraintName: "FKBF8786E04B2AF5FD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "WORKFLOW_STATE", referencesUniqueColumn: "false")
    }
}
