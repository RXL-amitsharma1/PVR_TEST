databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1457373013177-1") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "end_date", type: "timestamp")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1457373013177-2") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "start_date", type: "timestamp")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1457373013177-17") {
		dropTable(tableName: "REPORT_REQUEST_DELIVERY")
	}

}
