databaseChangeLog = {

	changeSet(author: "Prashant (generated)", id: "1472051298753-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'WORKFLOW_STATE', columnName: 'IS_DELETED')
			}
		}
		addColumn(tableName: "WORKFLOW_STATE") {
			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update WORKFLOW_STATE set IS_DELETED = 0;")
		addNotNullConstraint(tableName: "WORKFLOW_STATE", columnName: "IS_DELETED")
	}
}
