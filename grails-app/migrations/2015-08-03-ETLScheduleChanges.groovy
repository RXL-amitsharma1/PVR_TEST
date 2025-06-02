databaseChangeLog = {

	changeSet(author: "glennsilverman (generated)", id: "1438716847383-1") {
		createTable(tableName: "ETL_CASE_TABLE_STATUS") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ETL_CASE_TABLPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ETL_SCHEDULE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "STAGE_END_TIME", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "STAGE_START_TIME", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "table_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "TRANSFORMATION_END_TIME", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "TRANSFORMATION_START_TIME", type: "timestamp") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "glennsilverman (generated)", id: "1438716847383-2") {
		createTable(tableName: "ETL_MASTER") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ETL_MASTERPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ETL_SCHEDULE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "FINISH_TIME", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "PASS_STATUS", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "STAGE_KEY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "START_TIME", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "STEP_ID", type: "number(10,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "glennsilverman (generated)", id: "1438716847383-3") {
		createTable(tableName: "ETL_SCHEDULE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ETL_SCHEDULEPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CREATED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DISABLED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "REPEAT_INTERVAL", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "SCHEDULE_NAME", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "START_DATETIME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "glennsilverman (generated)", id: "1438716847383-10") {
		createIndex(indexName: "table_name_uniq_1438716835540", tableName: "ETL_CASE_TABLE_STATUS", unique: "true") {
			column(name: "table_name")
		}
	}

	changeSet(author: "glennsilverman (generated)", id: "1438716847383-11") {
		createIndex(indexName: "STEP_ID_uniq_1438716835541", tableName: "ETL_MASTER", unique: "true") {
			column(name: "STEP_ID")
		}
	}

	changeSet(author: "glennsilverman (generated)", id: "1438716847383-12") {
		createIndex(indexName: "SCHEDULE_NAME_uniq_1438716835", tableName: "ETL_SCHEDULE", unique: "true") {
			column(name: "SCHEDULE_NAME")
		}
	}

	changeSet(author: "glennsilverman (generated)", id: "1438716847383-5") {
		addForeignKeyConstraint(baseColumnNames: "ETL_SCHEDULE_ID", baseTableName: "ETL_CASE_TABLE_STATUS", constraintName: "FKD632B410486C89D7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ETL_SCHEDULE", referencesUniqueColumn: "false")
	}

	changeSet(author: "glennsilverman (generated)", id: "1438716847383-6") {
		addForeignKeyConstraint(baseColumnNames: "ETL_SCHEDULE_ID", baseTableName: "ETL_MASTER", constraintName: "FK7DA95764486C89D7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ETL_SCHEDULE", referencesUniqueColumn: "false")
	}
}
