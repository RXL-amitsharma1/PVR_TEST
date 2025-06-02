databaseChangeLog = {

	changeSet(author: "Meenakshi (generated)", id: "1472014394552-1") {
		addColumn(tableName: "CASE_SERIES") {
			column(name: "IS_TEMPORARY", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update CASE_SERIES set IS_TEMPORARY = 0;")
		addNotNullConstraint(tableName: "CASE_SERIES", columnName: "IS_TEMPORARY")
	}
}
