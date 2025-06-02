databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1500634054989-1") {
		addColumn(tableName: "APPLICATION_SETTINGS") {
			column(name: "DEFAULT_UI_SETTINGS", type: "clob")
		}
	}
}
