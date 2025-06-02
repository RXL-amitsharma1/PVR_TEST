databaseChangeLog = {

	changeSet(author: "Meenakshi (generated)", id: "1472548030566-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'CASE_SERIES', columnName: 'EXECUTION_STATUS')
			}
		}
		addColumn(tableName: "CASE_SERIES") {
			column(name: "EXECUTION_STATUS", type: "varchar2(255 char)") {
				constraints(nullable: "true")
			}
		}
		sql("update CASE_SERIES set EXECUTION_STATUS='COMPLETED';")
		addNotNullConstraint(tableName: "CASE_SERIES", columnName: "EXECUTION_STATUS")
	}
}
