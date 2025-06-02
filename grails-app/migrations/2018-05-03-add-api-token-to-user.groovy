databaseChangeLog = {
	changeSet(author: "lei (generated)", id: "1525651865997-3") {
		addColumn(tableName: "PVUSER") {
			column(name: "api_token", type: "varchar2(4000)") {
				constraints(nullable: "true")
			}
		}
	}
}
