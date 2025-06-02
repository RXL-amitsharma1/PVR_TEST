databaseChangeLog = {

	changeSet(author: "glennsilverman (generated)", id: "1441221733237-1") {
		addColumn(tableName: "ETL_SCHEDULE") {
			column(name: "IS_INITIAL", type: "number(1,0)")
		}
		grailsChange {
			change {
				sql.execute("UPDATE ETL_SCHEDULE SET IS_INITIAL = '0'")
				confirm "Successfully set default value for IS_INITIAL."
			}
		}
		addNotNullConstraint(tableName: "ETL_SCHEDULE", columnName: "IS_INITIAL", columnDataType: "number(1,0)")
	}

}
