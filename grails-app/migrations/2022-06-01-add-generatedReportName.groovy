databaseChangeLog = {


	changeSet(author: "forxsv (generated)", id: "010620221000-1") {
		addColumn(tableName: "RCONFIG") {
			column(name: "GENERATED_RPT_NAME", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "010620221000-2") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "GENERATED_RPT_NAME", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "010620221000-3") {
		addColumn(tableName: "REPORT_REQUEST_TYPE") {
			column(name: "RCONFIG_ID", type: "number(19,0)")
		}
	}
	changeSet(author: "sachinverma (generated)", id: "010620221000-4") {
		addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "REPORT_REQUEST_TYPE", constraintName: "RPT_RQST_TYPE_CFG_FK", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "sergey (generated)", id: "010620221000-6") {
		addColumn(tableName: "REPORT_REQUEST_FIELD") {
			column(name: "SECTION", type: "varchar2(255 char)"){
				constraints(nullable: "true")
			}
			column(name: "WIDTH", type: "number(19,0)"){
				constraints(nullable: "true")
			}
			column(name: "SEC_ALLOWED_VALUES", type: "varchar2(4000 char)"){
				constraints(nullable: "true")
			}
			column(name: "SEC_LABEL", type: "varchar2(255 char)"){
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "sergey (generated)", id: "010620221000-8") {
		addColumn(tableName: "REPORT_REQUEST_FIELD") {
			column(name: "JSCRIPT", type: "varchar2(4000 char)"){
				constraints(nullable: "true")
			}
			column(name: "DISABLED", type: "number(19,0)"){
				constraints(nullable: "true")
			}

		}
	}
	changeSet(author: "sergey (generated)", id: "010620221000-9") {
		addColumn(tableName: "TASK") {
			column(name: "BASE_DATE", type: "varchar2(255 char)"){
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "sergey (generated)", id: "010620221000-10") {
		addColumn(tableName: "PUBLISHER_CFG_SECT") {
			column(name: "TASK_TEMPLATE_ID", type: "number(19,0)"){
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "sachinverma (generated)", id: "010620221000-11") {
		addForeignKeyConstraint(baseColumnNames: "TASK_TEMPLATE_ID", baseTableName: "PUBLISHER_CFG_SECT", constraintName: "TASK_TPL_ID_PUB_CFG_SEC_FK", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TASK_TEMPLATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "sergey (generated)", id: "010620221000-12") {
		addColumn(tableName: "REPORT_TASK") {
			column(name: "BASE_DATE", type: "varchar2(255 char)"){
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "forxsv (generated)", id: "010620221000-14") {
		createTable(tableName: "PUB_SECTION_TASK") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "PUB_SECTION_TASK_PK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ACTION_CATEGORY", type: "varchar2(255 char)")

			column(name: "APP_TYPE", type: "varchar2(255 char)")

			column(name: "ASSIGNED_GROUP_ID", type: "number(19,0)")

			column(name: "ASSIGNED_USER_ID", type: "number(19,0)")

			column(name: "CREATED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(4000 char)") {
				constraints(nullable: "false")
			}

			column(name: "DUE", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
			column(name: "PRIORITY", type: "varchar2(255 char)")
			column(name: "TASK_ID", type: "number(19,0)")
			column(name: "action_category_id", type: "number(19,0)")
			column(name: "BASE_DATE", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "010620221000-15") {
		addForeignKeyConstraint(baseColumnNames: "ASSIGNED_GROUP_ID", baseTableName: "PUB_SECTION_TASK", constraintName: "FK_PUB_SECTION_TASK_1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "010620221000-16") {
		addForeignKeyConstraint(baseColumnNames: "ASSIGNED_USER_ID", baseTableName: "PUB_SECTION_TASK", constraintName: "FK_PUB_SECTION_TASK_2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "010620221000-18") {
		addForeignKeyConstraint(baseColumnNames: "TASK_ID", baseTableName: "PUB_SECTION_TASK", constraintName: "FK_PUB_SECTION_TASK_4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TASK_TEMPLATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv", id:'010620221000-19') {
		sql("UPDATE REPORT_TASK set BASE_DATE='CREATION_DATE';")
		sql("UPDATE REPORT_TASK set DUE=((-1*CREATE_SHIFT) - (-1*DUE)) where CREATE_SHIFT is not null;")
	}

	changeSet(author: "sergey (generated)", id: "010620221000-20") {
		addColumn(tableName: "PUB_SECTION_TASK") {
			column(name: "ASSIGN_TO_TYPE", type: "varchar2(255 char)"){
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "sergey (generated)", id: "010620221000-21") {
		createTable(tableName: "REPORT_REQ_P_C_USERS") {
			column(name: "REPORT_REQ_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "USER_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}
	changeSet(author: "sergey (generated)", id: "010620221000-22") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "PRIMARY_P_CONTRIBUTOR", type: "number(19,0)")
		}
	}

	changeSet(author: "sergey (generated)", id: "010620221000-23") {
		addForeignKeyConstraint(baseColumnNames: "PRIMARY_P_CONTRIBUTOR", baseTableName: "REPORT_REQUEST", constraintName: "RR_PRIMARY_P_CONT_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
	changeSet(author: "sergey (generated)", id: "010620221000-24") {
		addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "REPORT_REQ_P_C_USERS", constraintName: "RR_P_C_USERS_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
	changeSet(author: "sergey (generated)", id: "010620221000-25") {
		addForeignKeyConstraint(baseColumnNames: "REPORT_REQ_ID", baseTableName: "REPORT_REQ_P_C_USERS", constraintName: "REPORT_REQ_ID_RR", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST", referencesUniqueColumn: "false")
	}
	changeSet(author: "sergey khovrachev (generated)", id: "010620221000-26") {
		addColumn(tableName: "PUBLISHER_REPORT") {
			column(name: "qc_workflow_state_id", type: "number(19,0)")
		}
	}
	changeSet(author: "sergey khovrachev  (generated)", id: "010620221000-27") {
		addColumn(tableName: "WORKFLOW_JUSTIFICATION") {
			column(name: "PUBLISHER_REPORT_QC_ID", type: "number(19,0)")
		}
	}
	changeSet(author: "sergey khovrachev  (generated)", id: "20200522100601-28") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				foreignKeyConstraintExists(foreignKeyTableName: 'WORKFLOW_JUSTIFICATION', foreignKeyName: 'FK_QC_PUBLISHER_REPORT_ID')
			}
		}
		addForeignKeyConstraint(baseColumnNames: "PUBLISHER_REPORT_QC_ID", baseTableName: "WORKFLOW_JUSTIFICATION", constraintName: "FK_QC_PUBLISHER_REPORT_ID", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PUBLISHER_REPORT", referencesUniqueColumn: "false")
	}

	changeSet(author: "sergey khovrachev (generated)", id: "20200522100602-28") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'RCONFIG', columnName: 'GENERATE_SPOTFIRE')
			}
		}
		addColumn(tableName: "RCONFIG") {
			column(name: "GENERATE_SPOTFIRE", type: "varchar2(4000 char)")
		}
	}

	changeSet(author: "ShubhamRx (generated)", id: "20200522100600-29") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "ASSOCIATED_SPOTFIRE_FILE", type: "varchar2(1024 char)")
		}
	}
	changeSet(author: "sergey khovrachev", id: "20200522100600-30") {
		addColumn(tableName: "REPORT_REQUEST_FIELD") {
			column(name: "SHOW_IN_PLAN", type: "number(1,0)")
		}
	}
	changeSet(author: "sergey (generated)", id: "20200522100600-31") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "GENERATED_RPT_NAME", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "sergey (generated)", id: "20200522100600-32") {
		addColumn(tableName: "UNIT_CONFIGURATION") {
			column(name: "EMAIL_TEMPLATE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "sergey (generated)", id: "20200522100600-38") {
		addColumn(tableName: "RCONFIG") {
			column(name: "REMOVE_OLD_VERSION", type: "number(19,0)")
		}
	}

	changeSet(author: "sergey (generated)", id: "20200522100600-39") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "REMOVE_OLD_VERSION", type: "number(19,0)")
		}
	}
	changeSet(author: "sergey (generated)", id: "20200522100600-40") {
		createTable(tableName: "COMPARISON_RESULT") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "COMPARISON_RESULT_PK")
			}
			column(name: "VERSION", type: "number(19,0)") {constraints(nullable: "false")}
			column(name: "ENTITY_ID_1", type: "number(19,0)"){constraints(nullable: "false")}
			column(name: "ENTITY_NAME_1", type: "varchar2(300 char)"){constraints(nullable: "false")}
			column(name: "ENTITY_ID_2", type: "number(19,0)"){constraints(nullable: "false")}
			column(name: "ENTITY_NAME_2", type: "varchar2(300 char)"){constraints(nullable: "false")}
			column(name: "ENTITY_TYPE", type: "varchar2(255 char)"){constraints(nullable: "false")}
			column(name: "RESULT", type: "number(1,0)"){constraints(nullable: "false")}
			column(name: "DATA", type: "clob"){constraints(nullable: "false")}
		}
	}

	changeSet(author: "sergey (generated)", id: "20200522100600-41") {
		createTable(tableName: "COMPARISON_QUEUE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "COMPARISON_QUEUE_PK")
			}
			column(name: "VERSION", type: "number(19,0)") {constraints(nullable: "false")}
			column(name: "ENTITY_ID_1", type: "number(19,0)"){constraints(nullable: "false")}
			column(name: "ENTITY_NAME_1", type: "varchar2(300 char)"){constraints(nullable: "false")}
			column(name: "ENTITY_ID_2", type: "number(19,0)"){constraints(nullable: "false")}
			column(name: "ENTITY_NAME_2", type: "varchar2(300 char)"){constraints(nullable: "false")}
			column(name: "ENTITY_TYPE", type: "varchar2(255 char)"){constraints(nullable: "false")}
			column(name: "STATUS", type: "varchar2(255 char)"){constraints(nullable: "false")}
			column(name: "MESSAGE", type: "clob")
			column(name: "DATE_CREATED", type: "timestamp"){constraints(nullable: "false")}
			column(name: "DATE_COMPARED", type: "timestamp")
		}
	}
	changeSet(author: "sergey (generated)", id: "20200522100600-42") {
		addColumn(tableName: "COMPARISON_RESULT") {
			column(name: "DATE_CREATED", type: "timestamp")
		}
	}
}
