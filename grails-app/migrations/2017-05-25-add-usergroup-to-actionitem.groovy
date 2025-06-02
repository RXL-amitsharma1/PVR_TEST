databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1495715047483-1") {
		addColumn(tableName: "ACTION_ITEM") {
			column(name: "assigned_group_to_id", type: "number(19,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1495715047483-3") {
		dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "ASSIGNED_TO_ID", tableName: "ACTION_ITEM")
	}

	changeSet(author: "forxsv (generated)", id: "1495715047483-69") {
		addForeignKeyConstraint(baseColumnNames: "assigned_group_to_id", baseTableName: "ACTION_ITEM", constraintName: "FKE077AB7CDF361BCD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1495715047483-70") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "ACTION_ITEM", constraintName: "FKE077AB7C203910EE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
}
