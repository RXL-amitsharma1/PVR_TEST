databaseChangeLog = {

	changeSet(author: "sergey (generated)", id: "20200727100600-1") {
		addColumn(tableName: "COMPARISON_RESULT") {
			column(name: "MESSAGE", type: "varchar2(1000 char)")
			column(name: "SUPPORTED", type: "number(1,0)")
		}
	}
	changeSet(author: "sergey", id: "20200727100600-2") {
		sql("update COMPARISON_RESULT set SUPPORTED=1;")
	}

}
