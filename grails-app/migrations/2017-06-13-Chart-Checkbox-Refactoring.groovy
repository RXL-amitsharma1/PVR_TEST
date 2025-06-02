databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1497369113778-1") {
		addColumn(tableName: "RPT_TEMPLT") {
			column(name: "SHOW_CHART_SHEET", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1497369113778-2") {
		sql("merge into RPT_TEMPLT " +
				"using DTAB_TEMPLT " +
				"on (RPT_TEMPLT.ID=DTAB_TEMPLT.ID) " +
				"when matched then update set RPT_TEMPLT.SHOW_CHART_SHEET=DTAB_TEMPLT.SHOW_CHART_SHEET")
	}

	changeSet(author: "gologuzov (generated)", id: "1497369113778-3") {
		sql("merge into RPT_TEMPLT " +
				"using NONCASE_SQL_TEMPLT " +
				"on (RPT_TEMPLT.ID=NONCASE_SQL_TEMPLT.ID) " +
				"when matched then update set RPT_TEMPLT.SHOW_CHART_SHEET=NONCASE_SQL_TEMPLT.SHOW_CHART_SHEET")
	}

	changeSet(author: "gologuzov (generated)", id: "1497369113778-4") {
		dropColumn(columnName: "SHOW_CHART_SHEET", tableName: "DTAB_TEMPLT")
	}

	changeSet(author: "gologuzov (generated)", id: "1497369113778-5") {
		dropColumn(columnName: "SHOW_CHART_SHEET", tableName: "NONCASE_SQL_TEMPLT")
	}
}
