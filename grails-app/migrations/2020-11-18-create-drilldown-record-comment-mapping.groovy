databaseChangeLog = {
    changeSet(author: "shikhars", id: "201120200313") {
        createTable(tableName: "DRILLDOWN_DATA_COMMENTS") {
            column(name: "CLL_ROW_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "COMMENT_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "CLL_ROW_ID", baseTableName: "DRILLDOWN_DATA_COMMENTS", constraintName: "FK_CLL_COMMENT", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DRILLDOWN_DATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "COMMENT_ID", baseTableName: "DRILLDOWN_DATA_COMMENTS", constraintName: "FK_COMMENT_CLL", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "COMMENT_TABLE", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "CLL_ROW_ID, COMMENT_ID", constraintName: "DRILLDOWN_DATA_COMMENTS_PK", tableName: "DRILLDOWN_DATA_COMMENTS")
    }
}