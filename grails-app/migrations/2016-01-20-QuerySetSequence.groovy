databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1453313958902-1") {
		addColumn(tableName: "QUERY_SETS_SUPER_QRS") {
			column(name: "SUPER_QUERY_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "Sherry (generated)", id: "1453313958902-7") {
		dropForeignKeyConstraint(baseTableName: "QUERY_SETS_SUPER_QRS", constraintName: "FKB3E3F577707264C7")
	}
}
