databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1439847320690-1") {
		createTable(tableName: "DTAB_COL_MEAS_MEASURES") {
			column(name: "DTAB_COL_MEAS_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "MEASURE_ID", type: "number(19,0)")

			column(name: "MEASURES_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "Sherry (generated)", id: "1439847320690-2") {
		createTable(tableName: "DTAB_COLUMN_MEASURE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DTAB_COLUMN_MPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "COLUMNS_RFI_LIST_ID", type: "number(19,0)")

			column(name: "SHOW_TOTAL_CUMULATIVE_CASES", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHOW_TOTAL_INTERVAL_CASES", type: "number(1,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Sherry (generated)", id: "1439847320690-3") {
		createTable(tableName: "DTAB_TEMPLTS_COL_MEAS") {
			column(name: "DTAB_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "COLUMN_MEASURE_ID", type: "number(19,0)")

			column(name: "COLUMN_MEASURE_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "Sherry (generated)", id: "1439847320690-4") {
		createTable(tableName: "DTAB_TEMPLTS_MEASURES") {
			column(name: "DTAB_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "MEASURE_ID", type: "number(19,0)")

			column(name: "MEASURE_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "Sherry (generated)", id: "1439847320690-5") {
		dropForeignKeyConstraint(baseTableName: "DTAB_MEASURE", constraintName: "FK15CDC3B03CA2389B")
	}

	changeSet(author: "Sherry (generated)", id: "1439847320690-10") {
		dropColumn(columnName: "DTAB_TEMPLT_ID", tableName: "DTAB_MEASURE")
	}

	changeSet(author: "Sherry (generated)", id: "1439847320690-11") {
		dropColumn(columnName: "MEASURES_IDX", tableName: "DTAB_MEASURE")
	}

	changeSet(author: "Sherry (generated)", id: "1439847320690-6") {
		addForeignKeyConstraint(baseColumnNames: "MEASURE_ID", baseTableName: "DTAB_COL_MEAS_MEASURES", constraintName: "FKA8C55AFD9948F089", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DTAB_MEASURE", referencesUniqueColumn: "false")
	}

	changeSet(author: "Sherry (generated)", id: "1439847320690-7") {
		addForeignKeyConstraint(baseColumnNames: "COLUMNS_RFI_LIST_ID", baseTableName: "DTAB_COLUMN_MEASURE", constraintName: "FK22AA12A3475B2E84", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD_INFO_LIST", referencesUniqueColumn: "false")
	}

	changeSet(author: "Sherry (generated)", id: "1439847320690-8") {
		addForeignKeyConstraint(baseColumnNames: "COLUMN_MEASURE_ID", baseTableName: "DTAB_TEMPLTS_COL_MEAS", constraintName: "FK4A52E4DF68A4719C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DTAB_COLUMN_MEASURE", referencesUniqueColumn: "false")
	}

	changeSet(author: "Sherry (generated)", id: "1439847320690-9") {
		addForeignKeyConstraint(baseColumnNames: "MEASURE_ID", baseTableName: "DTAB_TEMPLTS_MEASURES", constraintName: "FK34B5C06B9948F089", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DTAB_MEASURE", referencesUniqueColumn: "false")
	}
}
