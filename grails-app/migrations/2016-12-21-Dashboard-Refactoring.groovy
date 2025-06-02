databaseChangeLog = {
	changeSet(author: "gologuzov (generated)", id: "1481811822279-3") {
		addColumn(tableName: "RWIDGET") {
			column(name: "WIDGET_TYPE", type: "varchar2(255 char)")
		}
		sql("update RWIDGET set WIDGET_TYPE = 'CHART';")
		addNotNullConstraint(tableName: "RWIDGET", columnName: "WIDGET_TYPE")
		dropNotNullConstraint(tableName: "RWIDGET", columnName: "REPORT_CONFIGURATION_ID")
	}
}
