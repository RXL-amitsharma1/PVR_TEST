databaseChangeLog = {

	changeSet(author: "prashantsahi (generated)", id: "1476698780160-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'RPT_FIELD_INFO', columnName: 'ADVANCED_SORTING')
			}
		}

		addColumn(tableName: "RPT_FIELD_INFO") {
			column(name: "ADVANCED_SORTING", type: "varchar2(2000 char)")
		}
	}

}
