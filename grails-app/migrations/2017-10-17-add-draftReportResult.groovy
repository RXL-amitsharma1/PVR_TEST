databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1508237332546-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_TEMPLT_QUERY', columnName: 'DRAFT_REPORT_RESULT_ID')
			}
		}
		addColumn(tableName: "EX_TEMPLT_QUERY") {
			column(name: "DRAFT_REPORT_RESULT_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "forxsv  (generated)", id: "1508237332546-2") {
		preConditions(onFail: 'MARK_RAN', onError: "CONTINUE") {
			sqlCheck(expectedResult: 'N', "SELECT Nullable FROM user_tab_columns WHERE table_name = 'EX_TEMPLT_QUERY' AND column_name = 'REPORT_RESULT_ID';")
		}
		dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "REPORT_RESULT_ID", tableName: "EX_TEMPLT_QUERY")
	}

	changeSet(author: "forxsv  (generated)", id: "1508237332546-3") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				foreignKeyConstraintExists(foreignKeyTableName: 'EX_TEMPLT_QUERY', foreignKeyName: 'FK_6tjvt98fmp4mnfjvka1htstug')
			}
		}
		addForeignKeyConstraint(baseColumnNames: "DRAFT_REPORT_RESULT_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FK_6tjvt98fmp4mnfjvka1htstug", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_RESULT", referencesUniqueColumn: "false")
	}

}
