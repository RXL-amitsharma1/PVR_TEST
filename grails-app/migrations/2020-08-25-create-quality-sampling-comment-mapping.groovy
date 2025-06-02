databaseChangeLog = {
    changeSet(author: "sachins", id: "250820202358") {
        createTable(tableName: "QUALITY_SAMPLING_COMMENTS") {
            column(name: "QUALITY_SAMPLING_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "QUALITY_SAMPLING_ID", baseTableName: "QUALITY_SAMPLING_COMMENTS", constraintName: "FK_SAMPLING_QUALITY_COMMENTS", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_SAMPLING", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "QUALITY_SAMPLING_COMMENTS", constraintName: "FK_MAPPING_COMMENT_SM", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "COMMENT_TABLE", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "QUALITY_SAMPLING_ID, COMMENT_ID", constraintName: "QLTY_SAMPLING_COMMENTS_PK", tableName: "QUALITY_SAMPLING_COMMENTS")
    }
}