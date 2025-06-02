databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1510743521094-2") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'RPT_FIELD_INFO', columnName: 'NEW_LEGEND_VALUE')
			}
		}
		addColumn(tableName: "RPT_FIELD_INFO") {
			column(name: "NEW_LEGEND_VALUE", type: "varchar2(2000 char)")
		}
	}
	changeSet(author: "gologuzov (generated)", id: "1511219186043-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EMAIL_CONFIGURATION', columnName: 'exclude_legend')
			}
		}
		addColumn(tableName: "EMAIL_CONFIGURATION") {
			column(name: "exclude_legend", type: "number(1,0)")
		}
	}
}
