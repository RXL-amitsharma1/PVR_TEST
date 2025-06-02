databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1505207738395-1") {
		addColumn(tableName: "CASE_SERIES") {
			column(name: "QUALITY_CHECKED", type: "number(1,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1505207738395-2") {
		addColumn(tableName: "EX_CASE_SERIES") {
			column(name: "QUALITY_CHECKED", type: "number(1,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1505207738395-3") {
		sql("update CASE_SERIES set QUALITY_CHECKED = 0;");
		sql("update EX_CASE_SERIES set QUALITY_CHECKED = 0;");
	}

	changeSet(author: "forxsv (generated)", id: "1505207738395-4") {
		addNotNullConstraint(columnDataType: "number(1,0)", columnName: "QUALITY_CHECKED", tableName: "CASE_SERIES")
	}

	changeSet(author: "forxsv (generated)", id: "1505207738395-5") {
		addNotNullConstraint(columnDataType: "number(1,0)", columnName: "QUALITY_CHECKED", tableName: "EX_CASE_SERIES")
	}
}
