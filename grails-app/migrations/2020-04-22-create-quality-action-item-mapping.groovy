databaseChangeLog = {
    changeSet(author: "shikhars", id: "220420201521") {
        createTable(tableName: "QUALITY_CASE_ACTION_ITEMS") {
            column(name: "QUALITY_CASE_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "ACTION_ITEM_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "QUALITY_CASE_ID", baseTableName: "QUALITY_CASE_ACTION_ITEMS", constraintName: "FK_MAPPING_CASE_QUALITY", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_CASE_DATA", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ACTION_ITEM_ID", baseTableName: "QUALITY_CASE_ACTION_ITEMS", constraintName: "FK_MAPPING_ACTION_ITEM", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "QUALITY_CASE_ID, ACTION_ITEM_ID", constraintName: "QUALITY_CASE_ACTION_ITEMS_PK", tableName: "QUALITY_CASE_ACTION_ITEMS")
    }

    changeSet(author: "shikhars", id: "220420201522") {
        createTable(tableName: "QUALITY_SUBMISSION_ACTION_ITEM") {
            column(name: "QUALITY_SUBMISSION_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "ACTION_ITEM_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "QUALITY_SUBMISSION_ID", baseTableName: "QUALITY_SUBMISSION_ACTION_ITEM", constraintName: "FK_MAPPING_CASE_SUBMISSION", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_SUBMISSION", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ACTION_ITEM_ID", baseTableName: "QUALITY_SUBMISSION_ACTION_ITEM", constraintName: "FK_MAPPING_ACTION_ITEM_SUB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "QUALITY_SUBMISSION_ID, ACTION_ITEM_ID", constraintName: "QLTY_SUBMISSION_ACTN_ITEMS_PK", tableName: "QUALITY_SUBMISSION_ACTION_ITEM")
    }

    changeSet(author: "shikhars", id: "220420201523") {
        createTable(tableName: "QUALITY_SAMPLING_ACTION_ITEMS") {
            column(name: "QUALITY_SAMPLING_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

            column(name: "ACTION_ITEM_ID", type: "number(19, 0)"){
                constraints(nullable: "false")
            }

        }
        addForeignKeyConstraint(baseColumnNames: "QUALITY_SAMPLING_ID", baseTableName: "QUALITY_SAMPLING_ACTION_ITEMS", constraintName: "FK_MAPPING_CASE_SAMPLING", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "QUALITY_SAMPLING", referencesUniqueColumn: "false")
        addForeignKeyConstraint(baseColumnNames: "ACTION_ITEM_ID", baseTableName: "QUALITY_SAMPLING_ACTION_ITEMS", constraintName: "FK_MAPPING_ACTION_ITEM_SAM", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM", referencesUniqueColumn: "false")
        addPrimaryKey(columnNames: "QUALITY_SAMPLING_ID, ACTION_ITEM_ID", constraintName: "QLTY_SAMPLING_ACTN_ITEMS_PK", tableName: "QUALITY_SAMPLING_ACTION_ITEMS")
    }
}