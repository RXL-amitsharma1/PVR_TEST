databaseChangeLog = {

	changeSet(author: "Amrit (generated)", id: "1479375782310-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'RPT_FIELD', columnName: 'PRE_QUERY_PROCEDURE')
			}
		}

		addColumn(tableName: "RPT_FIELD") {
			column(name: "PRE_QUERY_PROCEDURE", type: "varchar2(1000 char)")
		}
	}

	changeSet(author: "Amrit (generated)", id: "1479375782310-2") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'RPT_FIELD', columnName: 'POST_QUERY_PROCEDURE')
			}
		}

		addColumn(tableName: "RPT_FIELD") {
			column(name: "POST_QUERY_PROCEDURE", type: "varchar2(1000 char)")
		}
	}

	changeSet(author: "Amrit (generated)", id: "1479375782310-3") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'RPT_FIELD', columnName: 'PRE_REPORT_PROCEDURE')
			}
		}

		addColumn(tableName: "RPT_FIELD") {
			column(name: "PRE_REPORT_PROCEDURE", type: "varchar2(1000 char)")
		}
	}
}
