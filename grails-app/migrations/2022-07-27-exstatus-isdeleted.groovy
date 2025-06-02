databaseChangeLog = {
	changeSet(author: "sergey (generated)", id: "20200727100600-3") {
		addColumn(tableName: "EX_STATUS") {
			column(name: "IS_DELETED", type: "number(1,0)")
		}
		sql("update EX_STATUS set IS_DELETED=0;")
		addNotNullConstraint(tableName: "EX_STATUS", columnName: "IS_DELETED")
	}
}
