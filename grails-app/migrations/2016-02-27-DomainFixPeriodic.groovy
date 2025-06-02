databaseChangeLog = {

	changeSet(author: "sachinverma (generated)", id: "1456694435507-1") {
		createTable(tableName: "EX_GLOBAL_QUERY_VALUES") {
			column(name: "EX_GLOBAL_QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EX_QUERY_VALUE_ID", type: "number(19,0)")

			column(name: "EX_QUERY_VALUE_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-2") {
		createTable(tableName: "GLOBAL_QUERY_VALUES") {
			column(name: "RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_VALUE_ID", type: "number(19,0)")

			column(name: "QUERY_VALUE_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-4") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "EX_GLOBAL_DATE_RANGE_INFO_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-5") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "EX_GLOBAL_QUERY_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-12") {
		preConditions(onFail: 'MARK_RAN') {
			not{
				columnExists(tableName: 'EX_TEMPLT_QUERY', columnName: 'REPORT_RESULT_ID')
			}
		}
		addColumn(tableName: "EX_TEMPLT_QUERY") {
			column(name: "REPORT_RESULT_ID", type: "number(19,0)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-7") {
		addColumn(tableName: "RCONFIG") {
			column(name: "GLOBAL_DATA_RANGE_INFO_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-8") {
		addColumn(tableName: "RCONFIG") {
			column(name: "GLOBAL_QUERY_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-27") {
		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'RPT_RESULT', columnName: 'EX_TEMPLT_QUERY_ID')
		}
		sql("update EX_TEMPLT_QUERY set EX_TEMPLT_QUERY.REPORT_RESULT_ID = (select RPT_RESULT.ID from RPT_RESULT where RPT_RESULT.EX_TEMPLT_QUERY_ID = EX_TEMPLT_QUERY.ID);")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-13") {
		modifyDataType(columnName: "EXC_RCONFIG_ID", newDataType: "number(19,0)", tableName: "EX_RCONFIG_COMMENT_TABLE")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-14") {
		addNotNullConstraint(columnDataType: "number(19,0)", columnName: "EXC_RCONFIG_ID", tableName: "EX_RCONFIG_COMMENT_TABLE")
	}


	changeSet(author: "sachinverma (generated)", id: "1456694435507-16") {
		dropForeignKeyConstraint(baseTableName: "EX_PERIODIC_EX_QUERY_VALUES",  constraintName: "FK7639AD0DB741B4E0")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-17") {
		dropForeignKeyConstraint(baseTableName: "EX_PERIODIC_GLOBAL_QUERY",  constraintName: "FK61CF9DA4DD804A16")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-18") {
		dropForeignKeyConstraint(baseTableName: "EX_PERIODIC_GLOBAL_QUERY",  constraintName: "FK61CF9DA4E54B0969")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-19") {
		dropForeignKeyConstraint(baseTableName: "EX_RCONFIG",  constraintName: "FKC472BE887BACFDB7")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-20") {
		dropForeignKeyConstraint(baseTableName: "EX_TEMPLT_QUERY",  constraintName: "FKAF86CB111C399E1C")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-21") {
		dropForeignKeyConstraint(baseTableName: "PERIODIC_GLOAL_QUERY_VALUES",  constraintName: "FKC4FB830740891065")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-22") {
		dropForeignKeyConstraint(baseTableName: "PERIODIC_GLOBAL_QUERY",  constraintName: "FK8B72C30ADB414AF")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-23") {
		dropForeignKeyConstraint(baseTableName: "PERIODIC_GLOBAL_QUERY",  constraintName: "FK8B72C301CE47A1")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-24") {
		dropForeignKeyConstraint(baseTableName: "RCONFIG",  constraintName: "FK689172145BBDBD3")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-25") {
		dropForeignKeyConstraint(baseTableName: "RPT_RESULT",  constraintName: "FKDC1C5EA62F961DEA")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-26") {
		dropForeignKeyConstraint(baseTableName: "SHARED_WITH",  constraintName: "FKA1C93860E19CFD44")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-36") {
		dropColumn(columnName: "EX_PER_REP_GLOBAL_QUERY_ID", tableName: "EX_RCONFIG")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-37") {
		dropColumn(columnName: "PERIODIC_REP_GLOBAL_QUERY_ID", tableName: "RCONFIG")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-38") {
		dropColumn(columnName: "EX_TEMPLT_QUERY_ID", tableName: "RPT_RESULT")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-39") {
		dropTable(tableName: "EX_PERIODIC_EX_QUERY_VALUES")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-40") {
		dropTable(tableName: "EX_PERIODIC_GLOBAL_QUERY")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-41") {
		dropTable(tableName: "PERIODIC_GLOAL_QUERY_VALUES")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-42") {
		dropTable(tableName: "PERIODIC_GLOBAL_QUERY")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-28") {
		addForeignKeyConstraint(baseColumnNames: "EX_QUERY_VALUE_ID", baseTableName: "EX_GLOBAL_QUERY_VALUES", constraintName: "FKE87F5229B741B4E0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "EX_QUERY_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-29") {
		addForeignKeyConstraint(baseColumnNames: "EX_GLOBAL_DATE_RANGE_INFO_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE88DD804A16", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_GLOBAL_DATE_RANGE_INFO", referencesUniqueColumn: "false")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-30") {
		addForeignKeyConstraint(baseColumnNames: "EX_GLOBAL_QUERY_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE88FE407CED", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-31") {
		addForeignKeyConstraint(baseColumnNames: "REPORT_RESULT_ID", baseTableName: "EX_TEMPLT_QUERY", constraintName: "FKAF86CB11893B8381", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_RESULT", referencesUniqueColumn: "false")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-32") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "GLOBAL_QUERY_VALUES", constraintName: "FK5CD113B540891065", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-33") {
		addForeignKeyConstraint(baseColumnNames: "GLOBAL_DATA_RANGE_INFO_ID", baseTableName: "RCONFIG", constraintName: "FK68917214ADB414AF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "GLOBAL_DATE_RANGE_INFO", referencesUniqueColumn: "false")
	}

	changeSet(author: "sachinverma (generated)", id: "1456694435507-34") {
		addForeignKeyConstraint(baseColumnNames: "GLOBAL_QUERY_ID", baseTableName: "RCONFIG", constraintName: "FK68917214EBF0E079", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}
}
