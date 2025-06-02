databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1509089447605-1") {
		addColumn(tableName: "EX_RCONFIG_USER_STATE") {
			column(name: "IS_FAVORITE", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "forxsv (generated)", id: "1509089447605-2") {
		addColumn(tableName: "EX_CASE_SERIES_USER_STATE") {
			column(name: "IS_FAVORITE", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-1") {
		createTable(tableName: "CASE_SERIES_USER_STATE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CASE_SERIES_UPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ECASE_SERIES_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_FAVORITE", type: "number(1,0)")

			column(name: "RPT_USER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-2") {
		createTable(tableName: "QUERY_USER_STATE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "QUERY_USER_STPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_FAVORITE", type: "number(1,0)")

			column(name: "QUERY_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_USER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-3") {
		createTable(tableName: "RCONFIG_USER_STATE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RCONFIG_USER_PK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_FAVORITE", type: "number(1,0)")

			column(name: "RPT_USER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-4") {
		createTable(tableName: "TEMPLATE_USER_STATE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TEMPLATE_USERPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_FAVORITE", type: "number(1,0)")

			column(name: "TEMPLATE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_USER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-106") {
		addForeignKeyConstraint(baseColumnNames: "ECASE_SERIES_ID", baseTableName: "CASE_SERIES_USER_STATE", constraintName: "FK_sy578n4acn9qn07fe4tmofsn0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CASE_SERIES", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-107") {
		addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "CASE_SERIES_USER_STATE", constraintName: "FK_787fpf79mesyf0exxykmj3ccf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-111") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_ID", baseTableName: "QUERY_USER_STATE", constraintName: "FK_o09qghterphddprqhk6f9clex", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-112") {
		addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "QUERY_USER_STATE", constraintName: "FK_gfm529exp0w6w25yjiy1pnako", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-113") {
		addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "RCONFIG_USER_STATE", constraintName: "FK_cr0v5qmtyqhbmnchnhivt0bbr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-114") {
		addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "RCONFIG_USER_STATE", constraintName: "FK_3msbm3sldl2p9wf7sujthpnfn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-116") {
		addForeignKeyConstraint(baseColumnNames: "RPT_USER_ID", baseTableName: "TEMPLATE_USER_STATE", constraintName: "FK_gjjosl4etmuqe1cv0i486bp8r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1509360992349-117") {
		addForeignKeyConstraint(baseColumnNames: "TEMPLATE_ID", baseTableName: "TEMPLATE_USER_STATE", constraintName: "FK_i9pf6c0ca3hgd2fydjh4flwm1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_TEMPLT", referencesUniqueColumn: "false")
	}
}
