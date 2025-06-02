databaseChangeLog = {
	changeSet(author: "Pomi (generated)", id: "1461195164380-6") {
		dropForeignKeyConstraint(baseTableName: "RPT_RESULT", constraintName: "FKDC1C5EA68C474AC5")
	}

	changeSet(author: "Pomi (generated)", id: "1461195164380-7") {
		dropColumn(columnName: "TEMPLT_QUERY_ID", tableName: "RPT_RESULT")
	}
}
