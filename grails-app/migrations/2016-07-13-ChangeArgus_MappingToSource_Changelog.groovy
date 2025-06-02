databaseChangeLog = {

	//Drop foreign key constraint for TABLE_NAME_ATM_ID column
	changeSet(author: "Prashant (generated)", id: "1468613699103-1") {
		dropForeignKeyConstraint(baseTableName: "ARGUS_COLUMN_MASTER", constraintName: "FKEB1F49C0780789E0")
	}

	//Drop foreign key constraint for LM_TABLE_NAME_ATM_ID column
	changeSet(author: "Prashant (generated)", id: "1468613699103-2") {
		dropForeignKeyConstraint(baseTableName: "ARGUS_COLUMN_MASTER", constraintName: "FKEB1F49C0B3FC82A2")
	}

	//Drop foreign key constraint for ARGUS_COLUMN_MASTER_ID column
	changeSet(author: "Prashant (generated)", id: "1468613699103-3") {
		dropForeignKeyConstraint(baseTableName: "RPT_FIELD", constraintName: "FKF5EE11312A698EE5")
	}

	//Drop foreign key constraint for MAP_TABLE_NAME_ATM_ID column
	changeSet(author: "Prashant (generated)", id: "1468613699103-4") {
		dropForeignKeyConstraint(baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK5D4E9D538627F9BD")
	}

	//Drop foreign key constraint for TABLE_NAME_ATM_ID column
	changeSet(author: "Prashant (generated)", id: "1468613699103-5") {
		dropForeignKeyConstraint(baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK5D4E9D53780789E0")
	}

	changeSet(author: "Prashant (generated)", id: "1468613699103-6") {
		renameTable(oldTableName: "ARGUS_COLUMN_MASTER", newTableName: "SOURCE_COLUMN_MASTER")
	}

	changeSet(author: "Prashant (generated)", id: "1468613699103-7") {
		renameTable(oldTableName: "ARGUS_TABLE_MASTER", newTableName: "SOURCE_TABLE_MASTER")
	}

	changeSet(author: "Prashant (generated)", id: "1468613699103-8") {
		renameColumn(tableName: "RPT_FIELD", oldColumnName: "ARGUS_COLUMN_MASTER_ID", newColumnName: "SOURCE_COLUMN_MASTER_ID")
	}

	changeSet(author: "Prashant (generated)", id: "1468613699103-9") {
		addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FK42A9EA4745116EF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "Prashant (generated)", id: "1468613699103-10") {
		addForeignKeyConstraint(baseColumnNames: "LM_TABLE_NAME_ATM_ID", baseTableName: "SOURCE_COLUMN_MASTER", constraintName: "FK42A9EA47810667B9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "Prashant (generated)", id: "1468613699103-11") {
		addForeignKeyConstraint(baseColumnNames: "SOURCE_COLUMN_MASTER_ID", baseTableName: "RPT_FIELD", constraintName: "FKF5EE113191FE32E7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "REPORT_ITEM", referencedTableName: "SOURCE_COLUMN_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "Prashant (generated)", id: "1468613699103-12") {
		addForeignKeyConstraint(baseColumnNames: "MAP_TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK5D4E9D535331DED4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER", referencesUniqueColumn: "false")
	}

	changeSet(author: "Prashant (generated)", id: "1468613699103-13") {
		addForeignKeyConstraint(baseColumnNames: "TABLE_NAME_ATM_ID", baseTableName: "CASE_COLUMN_JOIN_MAPPING", constraintName: "FK5D4E9D5345116EF7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "TABLE_NAME", referencedTableName: "SOURCE_TABLE_MASTER", referencesUniqueColumn: "false")
	}

}
