databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1497509320100-1") {
		createTable(tableName: "CASE_SERIES_TAGS") {
			column(name: "CASE_SERIES_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TAG_ID", type: "number(19,0)")

			column(name: "TAG_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1497509320100-2") {
		createTable(tableName: "EX_CASE_SERIES_TAGS") {
			column(name: "EX_CASE_SERIES_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TAG_ID", type: "number(19,0)")

			column(name: "TAG_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1497509320100-56") {
		addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "CASE_SERIES_TAGS", constraintName: "FK492D6E529621E7DC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TAG", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1497509320100-59") {
		addForeignKeyConstraint(baseColumnNames: "TAG_ID", baseTableName: "EX_CASE_SERIES_TAGS", constraintName: "FK3948E45E9621E7DC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TAG", referencesUniqueColumn: "false")
	}

}
