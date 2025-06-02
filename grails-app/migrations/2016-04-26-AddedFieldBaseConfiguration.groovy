databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1461753744172-1") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "limit_primary_path", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1461753744172-2") {
		addColumn(tableName: "RCONFIG") {
			column(name: "limit_primary_path", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}


	}

}
