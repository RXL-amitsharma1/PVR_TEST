databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1457562816579-1") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "completion_date", type: "timestamp")
		}
	}
}
