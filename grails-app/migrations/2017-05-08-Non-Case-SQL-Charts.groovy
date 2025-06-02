databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1494187543777-1") {
		addColumn(tableName: "NONCASE_SQL_TEMPLT") {
			column(name: "CHART_CUSTOM_OPTIONS", type: "clob")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1494187543777-2") {
		addColumn(tableName: "NONCASE_SQL_TEMPLT") {
			column(name: "SHOW_CHART_SHEET", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}
}
