databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1555927032014-1") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "DTAB_MEASURE", columnName: "SORT")
			}
		}
		addColumn(tableName: "DTAB_MEASURE") {
			column(name: "SORT", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1555927032014-2") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "DTAB_MEASURE", columnName: "SORT_LEVEL")
			}
		}
		addColumn(tableName: "DTAB_MEASURE") {
			column(name: "SORT_LEVEL", type: "number(10,0)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1555927032014-3") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "DTAB_MEASURE", columnName: "TOP_X_COUNT")
			}
		}
		addColumn(tableName: "DTAB_MEASURE") {
			column(name: "TOP_X_COUNT", type: "number(10,0)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1555927032014-4") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "DTAB_MEASURE", columnName: "SHOW_TOP_X")
			}
		}
		addColumn(tableName: "DTAB_MEASURE") {
			column(name: "SHOW_TOP_X", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}
}
