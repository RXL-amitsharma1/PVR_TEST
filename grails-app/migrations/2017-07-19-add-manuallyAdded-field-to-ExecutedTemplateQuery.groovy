databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1500470970024-1") {
		addColumn(tableName: "EX_TEMPLT_QUERY") {
			column(name: "MANUALLY_ADDED", type: "number(1,0)")
		}
	}
}
