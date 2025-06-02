databaseChangeLog = {


	changeSet(author: "forxsv (generated)", id: "1532251400379-2") {
		addColumn(tableName: "RCONFIG") {
			column(name: "GENERATE_DRAFT", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update RCONFIG set GENERATE_DRAFT = 0;")
		addNotNullConstraint(tableName: "RCONFIG", columnName: "GENERATE_DRAFT")
	}
}
