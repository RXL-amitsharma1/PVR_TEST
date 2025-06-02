databaseChangeLog = {
    changeSet(author: "shikhars", id: "301120200343") {
        createTable(tableName: "DRILLDOWN_DATA_ISSUES") {
            column(name: "CLL_ROW_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "ISSUE_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "CLL_ROW_ID", baseTableName: "DRILLDOWN_DATA_ISSUES", constraintName: "FK_CLL_ISSUE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DRILLDOWN_DATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ISSUE_ID", baseTableName: "DRILLDOWN_DATA_ISSUES", constraintName: "FK_ISSUE_CLL", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CAPA_8D", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "CLL_ROW_ID, ISSUE_ID", constraintName: "DRILLDOWN_DATA_ISSUES_PK", tableName: "DRILLDOWN_DATA_ISSUES")
    }
}