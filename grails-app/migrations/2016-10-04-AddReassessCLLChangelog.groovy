databaseChangeLog = {

	changeSet(author: "prashantsahi (generated)", id: "1475580303202-3") {
		addColumn(tableName: "CLL_TEMPLT") {
			column(name: "CUST_EXP_REASSESS_LISTEDNESS", type: "varchar2(255 char)")
		}
	}

}
