databaseChangeLog = {
    changeSet(author: "shikhars", id: "301120200348") {
        createTable(tableName: "QUALITY_CASE_ISSUES") {
            column(name: "QUALITY_CASE_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "ISSUE_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "QUALITY_CASE_ID", baseTableName: "QUALITY_CASE_ISSUES", constraintName: "FK_QCD_ISSUE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_CASE_DATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ISSUE_ID", baseTableName: "QUALITY_CASE_ISSUES", constraintName: "FK_ISSUE_QCD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CAPA_8D", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "QUALITY_CASE_ID, ISSUE_ID", constraintName: "QUALITY_CASE_ISSUES_PK", tableName: "QUALITY_CASE_ISSUES")
    }

    changeSet(author: "shikhars", id: "301120200351") {
        createTable(tableName: "QUALITY_SUBMISSION_ISSUES") {
            column(name: "QUALITY_SUBMISSION_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "ISSUE_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "QUALITY_SUBMISSION_ID", baseTableName: "QUALITY_SUBMISSION_ISSUES", constraintName: "FK_QSUB_ISSUE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_SUBMISSION", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ISSUE_ID", baseTableName: "QUALITY_SUBMISSION_ISSUES", constraintName: "FK_ISSUE_QSUB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CAPA_8D", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "QUALITY_SUBMISSION_ID, ISSUE_ID", constraintName: "QUALITY_SUBMISSION_ISSUES_PK", tableName: "QUALITY_SUBMISSION_ISSUES")
    }

    changeSet(author: "shikhars", id: "301120200353") {
        createTable(tableName: "QUALITY_SAMPLING_ISSUES") {
            column(name: "QUALITY_SAMPLING_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "ISSUE_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "QUALITY_SAMPLING_ID", baseTableName: "QUALITY_SAMPLING_ISSUES", constraintName: "FK_QSAM_ISSUE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_SAMPLING", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ISSUE_ID", baseTableName: "QUALITY_SAMPLING_ISSUES", constraintName: "FK_ISSUE_QSAM", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CAPA_8D", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "QUALITY_SAMPLING_ID, ISSUE_ID", constraintName: "QUALITY_SAMPLING_ISSUES_PK", tableName: "QUALITY_SAMPLING_ISSUES")
    }
}