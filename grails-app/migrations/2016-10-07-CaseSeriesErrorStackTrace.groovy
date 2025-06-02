databaseChangeLog = {

	changeSet(author: "gautammalhotra (generated)", id: "1475778030355-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'CASE_SERIES', columnName: 'error_date_created')
			}
		}
		addColumn(tableName: "CASE_SERIES") {
			column(name: "error_date_created", type: "timestamp")
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1475778030355-2") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'CASE_SERIES', columnName: 'MESSAGE')
			}
		}
		addColumn(tableName: "CASE_SERIES") {
			column(name: "MESSAGE", type: "clob")
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1475778030355-3") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'CASE_SERIES', columnName: 'STACK_TRACE')
			}
		}
		addColumn(tableName: "CASE_SERIES") {
			column(name: "STACK_TRACE", type: "clob")
		}
	}
}
