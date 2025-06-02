databaseChangeLog = {

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-1") {
		createTable(tableName: "FIELD_PROFILE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "FIELD_PROFILEPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CREATED_BY", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(1000 char)")

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(30 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-2") {
		createTable(tableName: "PVUSERGROUPS_ROLES") {
			column(name: "role_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "user_group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-3") {
		createTable(tableName: "PVUSERGROUPS_USERS") {
			column(name: "user_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "user_group_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-4") {
		createTable(tableName: "USER_GROUP") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "USER_GROUPPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CREATED_BY", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(1000 char)")

			column(name: "FIELD_PROFILE_ID", type: "number(19,0)")

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(30 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-22") {
		addPrimaryKey(columnNames: "role_id, user_group_id", constraintName: "PVUSERGROUPS_PK", tableName: "PVUSERGROUPS_ROLES")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-23") {
		addPrimaryKey(columnNames: "user_id, user_group_id", constraintName: "PVUSERGROUPS_USER_PK", tableName: "PVUSERGROUPS_USERS")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-30") {
		createIndex(indexName: "NAME_uniq_1472731486229", tableName: "FIELD_PROFILE", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-31") {
		createIndex(indexName: "NAME_uniq_1472731486259", tableName: "USER_GROUP", unique: "true") {
			column(name: "NAME")
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-25") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "PVUSERGROUPS_ROLES", constraintName: "FK89A9E1C3E45C4F6F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ROLE", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-26") {
		addForeignKeyConstraint(baseColumnNames: "user_group_id", baseTableName: "PVUSERGROUPS_ROLES", constraintName: "FK89A9E1C3B308F9EE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-27") {
		addForeignKeyConstraint(baseColumnNames: "user_group_id", baseTableName: "PVUSERGROUPS_USERS", constraintName: "FK89D5E10EB308F9EE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-28") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "PVUSERGROUPS_USERS", constraintName: "FK89D5E10E8987134F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1472731845057-29") {
		addForeignKeyConstraint(baseColumnNames: "FIELD_PROFILE_ID", baseTableName: "USER_GROUP", constraintName: "FKC62E00EB19AB979A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "FIELD_PROFILE", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1474311225077-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'FIELD_PROFILE', columnName: 'IS_DELETED')
			}
		}
		addColumn(tableName: "FIELD_PROFILE") {
			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update FIELD_PROFILE set IS_DELETED = 0;")
		addNotNullConstraint(tableName: "FIELD_PROFILE", columnName: "IS_DELETED")
	}


	changeSet(author: "gautammalhotra (generated)", id: "1474311225077-2") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'USER_GROUP', columnName: 'IS_DELETED')
			}
		}
		addColumn(tableName: "USER_GROUP") {
			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update USER_GROUP set IS_DELETED = 0;")
		addNotNullConstraint(tableName: "USER_GROUP", columnName: "IS_DELETED")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1474746769406-1") {
		createTable(tableName: "field_profile_rpt_field") {
			column(name: "field_profile_report_fields_id", type: "number(19,0)")
			column(name: "report_field_id", type: "number(19,0)")
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1474746769406-19") {
		addForeignKeyConstraint(baseColumnNames: "field_profile_report_fields_id", baseTableName: "field_profile_rpt_field", constraintName: "FKF91A3256AFA95255", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "FIELD_PROFILE", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1474746769406-20") {
		addForeignKeyConstraint(baseColumnNames: "report_field_id", baseTableName: "field_profile_rpt_field", constraintName: "FKF91A3256BF61BBB3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1475123562626-1") {
		createTable(tableName: "report_request_user_group") {
			column(name: "REQUESTOR_GROUPS", type: "number(19,0)")

			column(name: "user_group_id", type: "number(19,0)")
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1475123562626-2") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'REPORT_REQUEST', columnName: 'assigned_group_to_id')
			}
		}
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "assigned_group_to_id", type: "number(19,0)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1475123562626-20") {
		addForeignKeyConstraint(baseColumnNames: "assigned_group_to_id", baseTableName: "REPORT_REQUEST", constraintName: "FKDB16FA64DF361BCD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1475123562626-21") {
		addForeignKeyConstraint(baseColumnNames: "REQUESTOR_GROUPS", baseTableName: "report_request_user_group", constraintName: "FK6783E1A6BF20669E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1475123562626-22") {
		addForeignKeyConstraint(baseColumnNames: "user_group_id", baseTableName: "report_request_user_group", constraintName: "FK6783E1A6B308F9EE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1475390976697-7") {
		modifyDataType(columnName: "ASSIGNED_TO_ID", newDataType: "number(19,0)", tableName: "REPORT_REQUEST")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1475390976697-8") {
		dropNotNullConstraint(columnDataType: "number(19,0)", columnName: "ASSIGNED_TO_ID", tableName: "REPORT_REQUEST")
	}
}
