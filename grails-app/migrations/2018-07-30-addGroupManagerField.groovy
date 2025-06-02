databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1532937434742-2") {
		addColumn(tableName: "PVUSERGROUPS_USERS") {
			column(name: "MANAGER", type: "number(1,0)")
		}
	}

}
