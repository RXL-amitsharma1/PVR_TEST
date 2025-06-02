databaseChangeLog = {
    changeSet(author: "shikhars", id: "181120200302") {
        createTable(tableName: "DRILLDOWN_DATA_ACTION_ITEMS") {
            column(name: "CLL_ROW_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "ACTION_ITEM_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "CLL_ROW_ID", baseTableName: "DRILLDOWN_DATA_ACTION_ITEMS", constraintName: "FK_MAPPING_DRILLDOWN_DATA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DRILLDOWN_DATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ACTION_ITEM_ID", baseTableName: "DRILLDOWN_DATA_ACTION_ITEMS", constraintName: "FK_MAPPING_ACTION_ITEM_CLL", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "CLL_ROW_ID, ACTION_ITEM_ID", constraintName: "DRILLDOWN_DATA_ACTION_ITEMS_PK", tableName: "DRILLDOWN_DATA_ACTION_ITEMS")
    }

    changeSet(author: "shikhars", id: "181120200320") {
        sql("INSERT INTO ACTION_ITEM_CATEGORY (ID, VERSION, NAME, KEY, DESCRIPTION, FOR_PVQ) VALUES (11,1,'Drilldown Record','DRILLDOWN_RECORD','Action Item related to a CLL drilldown record',0)")
    }
}