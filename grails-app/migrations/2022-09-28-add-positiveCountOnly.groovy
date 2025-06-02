databaseChangeLog = {

	changeSet(author: "sergey (generated)", id: "202209280844-4") {
		addColumn(tableName: "DTAB_TEMPLT") {
			column(name: "POSITIVE_COUNT_ONLY", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}
}
