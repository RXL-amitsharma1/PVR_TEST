databaseChangeLog = {

	changeSet(author: "Prashant (generated)", id: "1471980115278-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'TASK_TEMPLATE', columnName: 'IS_DELETED')
			}
		}
		addColumn(tableName: "TASK_TEMPLATE") {
			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update TASK_TEMPLATE set IS_DELETED = 0;")
		addNotNullConstraint(tableName: "TASK_TEMPLATE", columnName: "IS_DELETED")
	}
}