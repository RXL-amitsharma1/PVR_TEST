databaseChangeLog = {

	changeSet(author: "sachinverma (generated)", id: "1457733912018-7") {
		preConditions(onFail: 'MARK_RAN') {
			foreignKeyConstraintExists(foreignKeyTableName: 'RPT_RESULT', foreignKeyName: 'FKDC1C5EA68C474AC5')
		}
		dropForeignKeyConstraint(baseTableName: "RPT_RESULT", constraintName: "FKDC1C5EA68C474AC5")
	}

	changeSet(author: "sachinverma (generated)", id: "1457733912018-9") {
		preConditions(onFail: 'MARK_RAN') {
				columnExists(tableName: 'RPT_RESULT', columnName: 'TEMPLT_QUERY_ID')
		}
		dropColumn(columnName: "TEMPLT_QUERY_ID", tableName: "RPT_RESULT")
	}

}