databaseChangeLog = {
	changeSet(author: "Sherry (generated)", id: "1442604068591-5") {
		dropColumn(columnName: "SHOW_TOTAL_CUMULATIVE_CASES", tableName: "DTAB_TEMPLT")
	}

	changeSet(author: "Sherry (generated)", id: "1442604068591-6") {
		dropColumn(columnName: "SHOW_TOTAL_INTERVAL_CASES", tableName: "DTAB_TEMPLT")
	}
}
