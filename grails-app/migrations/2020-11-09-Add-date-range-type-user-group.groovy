databaseChangeLog = {
    changeSet(author: "shikhars", id: "091120201435") {
        createTable(tableName: "USER_GRP_DATE_RANGE_TYPES") {
            column(name: "USER_GRP_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

            column(name: "DATE_RANGE_TYPE_ID", type: "number(19, 0)") {
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "USER_GRP_ID", baseTableName: "USER_GRP_DATE_RANGE_TYPES", constraintName: "FK_MAPPING_USER_GROUP", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_TYPE_ID", baseTableName: "USER_GRP_DATE_RANGE_TYPES", constraintName: "FK_MAPPING_DATE_RANGE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DATE_RANGE_TYPE", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "USER_GRP_ID, DATE_RANGE_TYPE_ID", constraintName: "USER_GRP_DATE_RANGE_PK", tableName: "USER_GRP_DATE_RANGE_TYPES")
    }
}