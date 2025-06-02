databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1456272331063-6") {
		dropForeignKeyConstraint(baseTableName: "EX_TEMPLT_SET_CLL", constraintName: "FK385837AF3997143")
	}

	changeSet(author: "Sherry (generated)", id: "1456272331063-7") {
		dropTable(tableName: "EX_TEMPLT_SET_CLL")
	}
}
