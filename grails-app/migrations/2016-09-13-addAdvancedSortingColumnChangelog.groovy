databaseChangeLog = {

	changeSet(author: "Prashant (generated)", id: "1473760871768-1") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'CLL_TEMPLT', columnName: 'ADVANCED_SORTING')
			}
		}
		addColumn(tableName: "CLL_TEMPLT") {
			column(name: "ADVANCED_SORTING", type: "clob")
		}
	}

}