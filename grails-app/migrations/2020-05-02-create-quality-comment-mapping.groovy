databaseChangeLog = {
    changeSet(author: "shikhars", id: "020520202357") {
        createTable(tableName: "QUALITY_CASE_COMMENTS") {
            column(name: "QUALITY_CASE_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "QUALITY_CASE_ID", baseTableName: "QUALITY_CASE_COMMENTS", constraintName: "FK_CASE_QUALITY_COMMENTS", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_CASE_DATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "QUALITY_CASE_COMMENTS", constraintName: "FK_MAPPING_COMMENT_CQ", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "COMMENT_TABLE", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "QUALITY_CASE_ID, COMMENT_ID", constraintName: "QLTY_CASE_COMMENTS_PK", tableName: "QUALITY_CASE_COMMENTS")
    }

    changeSet(author: "shikhars", id: "030520200327") {
        createTable(tableName: "QUALITY_SUBMISSION_COMMENTS") {
            column(name: "QUALITY_SUBMISSION_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "QUALITY_SUBMISSION_ID", baseTableName: "QUALITY_SUBMISSION_COMMENTS", constraintName: "FK_CASE_SUBMISSION_COMMENTS", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_SUBMISSION", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "QUALITY_SUBMISSION_COMMENTS", constraintName: "FK_MAPPING_COMMENT_CS", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "COMMENT_TABLE", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "QUALITY_SUBMISSION_ID, COMMENT_ID", constraintName: "QLTY_SUBMISSION_COMMENTS_PK", tableName: "QUALITY_SUBMISSION_COMMENTS")
    }
}
