databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1512846532130-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'DTAB_TEMPLT', columnName: 'DRILL_DOWN_TO_CASE_LIST')
			}
		}
		addColumn(tableName: "DTAB_TEMPLT") {
			column(name: "DRILL_DOWN_TO_CASE_LIST", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}
}
