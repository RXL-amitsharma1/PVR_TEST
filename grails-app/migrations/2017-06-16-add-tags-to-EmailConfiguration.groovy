databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1497605707917-1") {
		addColumn(tableName: "EMAIL_CONFIGURATION") {
			column(name: "cc", type: "CLOB")
		}
	}
}
