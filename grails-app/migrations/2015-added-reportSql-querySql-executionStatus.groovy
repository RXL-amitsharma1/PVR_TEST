databaseChangeLog = {

	changeSet(author: "prakriti (generated)", id: "1444759529008-1") {
		addColumn(tableName: "EX_STATUS") {
			column(name: "HEADER_SQL", type: "clob")
		}
	}

	changeSet(author: "prakriti (generated)", id: "1444759529008-2") {
		addColumn(tableName: "EX_STATUS") {
			column(name: "QUERY_SQL", type: "clob")
		}
	}

	changeSet(author: "prakriti (generated)", id: "1444759529008-3") {
		addColumn(tableName: "EX_STATUS") {
			column(name: "REPORT_SQL", type: "clob")
		}
	}
}
