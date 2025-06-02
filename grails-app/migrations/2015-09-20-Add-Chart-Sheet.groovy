databaseChangeLog = {

	changeSet(author: "sgologuzov (generated)", id: "1442439366-1") {
		addColumn(tableName: "DTAB_TEMPLT") {
			column(name: "SHOW_CHART_SHEET", type: "number(1,0)")
		}
		addNotNullConstraint(columnDataType: "number(1,0)", columnName: "SHOW_CHART_SHEET", tableName: "DTAB_TEMPLT", defaultNullValue: 0)
	}
}
