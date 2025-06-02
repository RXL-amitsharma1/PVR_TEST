databaseChangeLog = {

	changeSet(author: "glennsilverman (generated)", id: "1438975567501-1") {
		addColumn(tableName: "SUPER_QUERY") {
			column(name: "PVUSER_ID", type: "number(19,0)")

		}

		grailsChange {
			change {
				sql.execute("UPDATE SUPER_QUERY SET PVUSER_ID = CREATED_BY")
				confirm "Successfully set default value for PVUSER_ID."
			}

		}
		addNotNullConstraint(tableName: "SUPER_QUERY", columnName: "PVUSER_ID", columnDataType: "number(19,0)")


	}

	changeSet(author: "glennsilverman (generated)", id: "1438975567501-2") {
		addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "SUPER_QUERY", constraintName: "FK5014CAC449425289", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
}
