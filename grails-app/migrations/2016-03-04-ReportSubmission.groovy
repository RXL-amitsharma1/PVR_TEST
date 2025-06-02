databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "1457158353499-1") {
        createTable(tableName: "RPT_SUBMISSION") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_SUBMISSIOPK")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_DATA", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "EX_RCONFIG_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "false")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "RPT_SUBMISSION_STATUS", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "SUBMISSION_DATE", type: "VARCHAR2(4000 char)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1457158353499-2") {
        createTable(tableName: "RPT_SUB_RPT_DESTINATIONS") {
            column(name: "RPT_SUBMISSION_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORTING_DESTINATION", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "sachinverma (generated)", id: "1457158353499-4") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'EX_STATUS')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "EX_STATUS", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }
        }
        sql("update EX_RCONFIG delivery set EX_STATUS = 'GENERATED_FINAL_DRAFT'")
        addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "EX_STATUS")
    }

    changeSet(author: "sachinverma (generated)", id: "1457158353499-10") {
        modifyDataType(columnName: "WORKFLOW_STATE_ID", newDataType: "number(19,0)", tableName: "WORKFLOW_STATE_REPORT_ACTIONS")
    }

    changeSet(author: "sachinverma (generated)", id: "1457158353499-11") {
        addNotNullConstraint(columnDataType: "number(19,0)", columnName: "WORKFLOW_STATE_ID", tableName: "WORKFLOW_STATE_REPORT_ACTIONS")
    }


    changeSet(author: "sachinverma (generated)", id: "1457158353499-13") {
        addForeignKeyConstraint(baseColumnNames: "EX_RCONFIG_ID", baseTableName: "RPT_SUBMISSION", constraintName: "FK4A455D55CE8FBC87", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
    }

    changeSet(author: "sachinverma (generated)", id: "1457158353499-14") {
        addForeignKeyConstraint(baseColumnNames: "RPT_SUBMISSION_ID", baseTableName: "RPT_SUB_RPT_DESTINATIONS", constraintName: "FK47CAB7981E4B1EE3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_SUBMISSION", referencesUniqueColumn: "false")
    }
}
