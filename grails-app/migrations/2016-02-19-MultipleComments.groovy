databaseChangeLog = {

	changeSet(author: "sachinverma (generated)", id: "1455847294990-1") {
		createTable(tableName: "ex_rconfig_comment_table") {
			column(name: "EXC_RCONFIG_ID", type: "number(19,0)")

			column(name: "comment_id", type: "number(19,0)")
		}
	}

	changeSet(author: "sachinverma (generated)", id: "1455847294990-2") {
		createTable(tableName: "rpt_result_comment_table") {
			column(name: "report_result_comments_id", type: "number(19,0)")

			column(name: "comment_id", type: "number(19,0)")
		}
	}

	changeSet(author: "sachinverma (generated)", id: "1455847294990-8") {
		dropForeignKeyConstraint(baseTableName: "EX_RCONFIG", constraintName: "FKC472BE8826BFB4BC")
	}

	changeSet(author: "sachinverma (generated)", id: "1455847294990-9") {
		dropForeignKeyConstraint(baseTableName: "RPT_RESULT", constraintName: "FKDC1C5EA626BFB4BC")
	}

	changeSet(author: "sachinverma (generated)", id: "1455847294990-14") {
		dropColumn(columnName: "COMMENT_TABLE_ID", tableName: "EX_RCONFIG")
	}

	changeSet(author: "sachinverma (generated)", id: "1455847294990-15") {
		dropColumn(columnName: "COMMENT_TABLE_ID", tableName: "RPT_RESULT")
	}

	changeSet(author: "sachinverma (generated)", id: "1455847294990-10") {
		addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "ex_rconfig_comment_table", constraintName: "FK51F04C9726BFB4BC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "COMMENT_TABLE", referencesUniqueColumn: "false")
	}

	changeSet(author: "sachinverma (generated)", id: "1455847294990-11") {
		addForeignKeyConstraint(baseColumnNames: "EXC_RCONFIG_ID", baseTableName: "ex_rconfig_comment_table", constraintName: "FK51F04C97CB73CA56", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "sachinverma (generated)", id: "1455847294990-12") {
		addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "rpt_result_comment_table", constraintName: "FK359103F526BFB4BC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "COMMENT_TABLE", referencesUniqueColumn: "false")
	}

	changeSet(author: "sachinverma (generated)", id: "1455847294990-13") {
		addForeignKeyConstraint(baseColumnNames: "report_result_comments_id", baseTableName: "rpt_result_comment_table", constraintName: "FK359103F5B5E4EB5E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_RESULT", referencesUniqueColumn: "false")
	}
}
