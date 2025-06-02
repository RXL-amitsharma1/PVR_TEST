databaseChangeLog = {

	changeSet(author: "prakriti (generated)", id: "1445905040067-1") {
		addColumn(tableName: "EX_STATUS") {
			column(name: "EXCONFIG_ID", type: "number(19,0)")
		}
	}

}
