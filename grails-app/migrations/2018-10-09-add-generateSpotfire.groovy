databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1539100239262-1") {
		addColumn(tableName: "CASE_SERIES") {
			column(name: "GENERATE_SPOTFIRE", type: "varchar2(4000 char)")
		}
	}

}
