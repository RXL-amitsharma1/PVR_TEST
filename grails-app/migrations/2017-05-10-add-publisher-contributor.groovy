databaseChangeLog = {



	changeSet(author: "forxsv (generated)", id: "060420221000-1") {
		createTable(tableName: "RCONFIG_P_C_USERS") {
			column(name: "RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "USER_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}
	changeSet(author: "forxsv (generated)", id: "060420221000-2") {
		createTable(tableName: "EX_RCONFIG_P_C_USERS") {
			column(name: "RCONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "USER_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}
	changeSet(author: "forxsv (generated)", id: "060420221000-3") {
		addColumn(tableName: "RCONFIG") {
			column(name: "PRIMARY_P_CONTRIBUTOR", type: "number(19,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "060420221000-4") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "PRIMARY_P_CONTRIBUTOR", type: "number(19,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "060420221000-15") {
		sql("update RCONFIG set PRIMARY_P_CONTRIBUTOR = PVUSER_ID where PRIMARY_P_CONTRIBUTOR is null;")
		sql("update EX_RCONFIG set PRIMARY_P_CONTRIBUTOR = PVUSER_ID where PRIMARY_P_CONTRIBUTOR is null;")
	}

	changeSet(author: "forxsv (generated)", id: "060420221000-5") {
		addForeignKeyConstraint(baseColumnNames: "PRIMARY_P_CONTRIBUTOR", baseTableName: "RCONFIG", constraintName: "PRIMARY_P_CONT_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
	changeSet(author: "forxsv (generated)", id: "060420221000-6") {
		addForeignKeyConstraint(baseColumnNames: "PRIMARY_P_CONTRIBUTOR", baseTableName: "EX_RCONFIG", constraintName: "EX_PRIMARY_P_CONT_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
	changeSet(author: "forxsv (generated)", id: "060420221000-7") {
		addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "RCONFIG_P_C_USERS", constraintName: "RCONFIG_P_C_USERS_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
	changeSet(author: "forxsv (generated)", id: "060420221000-8") {
		addForeignKeyConstraint(baseColumnNames: "USER_ID", baseTableName: "EX_RCONFIG_P_C_USERS", constraintName: "EX_RCONFIG_P_C_USERS_USER", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
	changeSet(author: "forxsv (generated)", id: "060420221000-9") {
		addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "RCONFIG_P_C_USERS", constraintName: "RCONFIG_P_C_USERS_RCONFIG", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}
	changeSet(author: "forxsv (generated)", id: "060420221000-10") {
		addForeignKeyConstraint(baseColumnNames: "RCONFIG_ID", baseTableName: "EX_RCONFIG_P_C_USERS", constraintName: "EX_RCONFIG_P_C_USERS_RCONFIG", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}

}
