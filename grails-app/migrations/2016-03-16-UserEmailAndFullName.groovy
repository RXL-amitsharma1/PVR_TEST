databaseChangeLog = {

	changeSet(author: "michaelmorett (generated)", id: "1458150062422-1") {
		addColumn(tableName: "PVUSER") {
			column(name: "EMAIL", type: "varchar2(200 char)")
		}
	}

	changeSet(author: "michaelmorett (generated)", id: "1458150062422-2") {
		addColumn(tableName: "PVUSER") {
			column(name: "FULLNAME", type: "varchar2(200 char)")
		}
	}

}
