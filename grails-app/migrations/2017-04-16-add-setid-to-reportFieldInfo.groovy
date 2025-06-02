databaseChangeLog = {

	changeSet(author: "foxsv (generated)", id: "1492350930194-1") {
		addColumn(tableName: "RPT_FIELD_INFO") {
			column(name: "SET_ID", type: "number(10,0)", defaultValue: 0){
				constraints(nullable: "false")
			}
		}
	}
}
