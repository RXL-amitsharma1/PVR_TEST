databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1506760702415-1") {
		addColumn(tableName: "RPT_TEMPLT") {
			column(name: "REASSESS_FOR_PRODUCT", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1506760702415-2") {
		addColumn(tableName: "QUERY") {
			column(name: "reassess_for_product", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}
}
