databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1494437274374-1") {
		createTable(tableName: "CASE_DELIVERIES_SHARED_W_GRPS") {
			column(name: "DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_GROUP_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_GROUP_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-2") {
		createTable(tableName: "CASE_DELIVERIES_SHARED_WITHS") {
			column(name: "DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-3") {
		createTable(tableName: "CASE_DELIVERY") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CASE_DELIVERYPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CASE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-4") {
		createTable(tableName: "EX_CASE_DELIVERIES_EMAIL_USERS") {
			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EMAIL_USER", type: "varchar2(255 char)")

			column(name: "EMAIL_USER_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-5") {
		createTable(tableName: "EX_CASE_DELIVERIES_RPT_FORMATS") {
			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_FORMAT", type: "varchar2(255 char)")

			column(name: "RPT_FORMAT_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-6") {
		createTable(tableName: "EX_CASE_DELIVERIES_SHRD_W_GRPS") {
			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_GROUP_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_GROUP_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-7") {
		createTable(tableName: "EX_CASE_DELIVERIES_SHRD_WTHS") {
			column(name: "EX_DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-8") {
		createTable(tableName: "EX_CASE_DELIVERY") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_CASE_DELIVPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EXECUTED_CASE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1494437274374-9") {
		createTable(tableName: "CASE_DELIVERIES_EMAIL_USERS") {
			column(name: "DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EMAIL_USER", type: "varchar2(255 char)")

			column(name: "EMAIL_USER_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1494437274374-10") {
		createTable(tableName: "CASE_DELIVERIES_RPT_FORMATS") {
			column(name: "DELIVERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_FORMAT", type: "varchar2(255 char)")

			column(name: "RPT_FORMAT_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-69") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_GROUP_ID", baseTableName: "CASE_DELIVERIES_SHARED_W_GRPS", constraintName: "FKDAA1D8725D0D08F9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-70") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "CASE_DELIVERIES_SHARED_WITHS", constraintName: "FKDDB957714F5A069A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-71") {
		addForeignKeyConstraint(baseColumnNames: "CASE_ID", baseTableName: "CASE_DELIVERY", constraintName: "FKE37760232FC103AF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CASE_SERIES", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-74") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_GROUP_ID", baseTableName: "EX_CASE_DELIVERIES_SHRD_W_GRPS", constraintName: "FK15E4B6445D0D08F9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-75") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_CASE_DELIVERIES_SHRD_WTHS", constraintName: "FK3FF49F564F5A069A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1494437274374-76") {
		addForeignKeyConstraint(baseColumnNames: "EXECUTED_CASE_ID", baseTableName: "EX_CASE_DELIVERY", constraintName: "FK648285973B3051EE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_CASE_SERIES", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1494489802273-1") {
		addColumn(tableName: "CASE_SERIES") {
			column(name: "EMAIL_CONFIGURATION_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1494489802273-2") {
		addColumn(tableName: "EX_CASE_SERIES") {
			column(name: "EMAIL_CONFIGURATION_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1494489802273-63") {
		addForeignKeyConstraint(baseColumnNames: "EMAIL_CONFIGURATION_ID", baseTableName: "CASE_SERIES", constraintName: "FK436686669C7C455F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EMAIL_CONFIGURATION", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1494489802273-66") {
		addForeignKeyConstraint(baseColumnNames: "EMAIL_CONFIGURATION_ID", baseTableName: "EX_CASE_SERIES", constraintName: "FKFC2478DA9C7C455F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EMAIL_CONFIGURATION", referencesUniqueColumn: "false")
	}

    changeSet(author: "forxsv (generated)", id: "1494489802273-70") {
        preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN', onFailMessage: 'No admin user found.', onErrorMessage: 'No admin user found.') {
            sqlCheck(expectedResult: '1', 'SELECT COUNT(*) FROM PVUSER u where u.USERNAME=\'admin\';')
        }
        sql("insert into CASE_DELIVERY (id,version,CASE_ID)(select HIBERNATE_SEQUENCE.nextval,1,c.ID from CASE_SERIES c where c.id not in (select CASE_ID from CASE_DELIVERY ) );\n" +
            "insert into CASE_DELIVERIES_SHARED_WITHS (DELIVERY_ID, SHARED_WITH_ID, SHARED_WITH_IDX)  (select d.id, (select u.id from PVUSER u where u.USERNAME='admin'),0 from CASE_DELIVERY d);");
    }

}
