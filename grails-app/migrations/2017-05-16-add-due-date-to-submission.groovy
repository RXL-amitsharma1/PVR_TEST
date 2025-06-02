databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1494939521922-1") {
		addColumn(tableName: "RPT_SUBMISSION") {
			column(name: "DUE_DATE", type: "timestamp")
		}
	}
}
