databaseChangeLog = {

	changeSet(author: "sergey (generated)", id: "202306170844-2") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'DTAB_TEMPLT', columnName: 'ALL_TIMEFRAMES')
			}
		}
		addColumn(tableName: "DTAB_TEMPLT") {
			column(name: "ALL_TIMEFRAMES", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}
}
