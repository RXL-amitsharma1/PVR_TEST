databaseChangeLog = {

	changeSet(author: "gautammalhotra (generated)", id: "1468483815995-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'REPORT_REQUEST', columnName: 'AS_OF_VERSION_DATE')
			}
		}
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "AS_OF_VERSION_DATE", type: "timestamp") {
				constraints(nullable: "true")
			}
		}
	}
}
