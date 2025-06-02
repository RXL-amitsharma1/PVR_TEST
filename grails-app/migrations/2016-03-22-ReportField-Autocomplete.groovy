databaseChangeLog = {
	changeSet(author: "Pomi (generated)", id: "1458691379292-1") {
		addColumn(tableName: "RPT_FIELD") {
			column(name: "ISAUTOCOMPLETE", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}
}
