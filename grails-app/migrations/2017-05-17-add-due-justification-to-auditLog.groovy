databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1495024288194-1") {
		addColumn(tableName: "AUDIT_LOG") {
			column(name: "JUSTIFICATION", type: "clob")
		}
	}
}
