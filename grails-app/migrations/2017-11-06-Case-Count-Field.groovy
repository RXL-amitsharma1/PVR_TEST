databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1509792032332-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'RPT_RESULT', columnName: 'CASE_COUNT')
			}
		}
		addColumn(tableName: "RPT_RESULT") {
			column(name: "CASE_COUNT", type: "number(19,0)")
		}
	}
}
