databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1563378439919-1") {
		addColumn(tableName: "DTAB_TEMPLT") {
			column(name: "GROUPING_RF_INFO_LIST_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1563378439919-2") {
		addColumn(tableName: "DTAB_TEMPLT") {
			column(name: "PAGE_BREAK_BY_GROUP", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1563378439919-3") {
		addForeignKeyConstraint(baseColumnNames: "GROUPING_RF_INFO_LIST_ID", baseTableName: "DTAB_TEMPLT", constraintName: "FK_7tmwim1htc0aot0ltyoxw3rhx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1563378439919-4") {
		addColumn(tableName: "DTAB_TEMPLT") {
			column(name: "TRANSPOSE_OUTPUT", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}
}
