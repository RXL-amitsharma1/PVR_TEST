databaseChangeLog = {

	changeSet(author: "gautammalhotra (generated)", id: "1466191757593-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'CLL_TEMPLT', columnName: 'COL_SHOW_DISTINCT')
			}
		}
		addColumn(tableName: "CLL_TEMPLT") {
			column(name: "COL_SHOW_DISTINCT", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update CLL_TEMPLT set COL_SHOW_DISTINCT = 0;")
		addNotNullConstraint(tableName: "CLL_TEMPLT", columnName: "COL_SHOW_DISTINCT")
	}
}
