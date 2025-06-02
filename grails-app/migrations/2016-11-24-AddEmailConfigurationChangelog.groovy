databaseChangeLog = {

	changeSet(author: "prashantsahi (generated)", id: "1479971939377-1") {
		createTable(tableName: "EMAIL_CONFIGURATION") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EMAIL_CONFIGUPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "EMAIL_BODY", type: "clob") {
				constraints(nullable: "false")
			}

			column(name: "NO_EMAIL_ON_NO_DATA", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "EMAIL_SUBJECT", type: "varchar2(2000 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "prashantsahi (generated)", id: "1479971939377-3") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "EMAIL_CONFIGURATION_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "prashantsahi (generated)", id: "1479971939377-4") {
		addColumn(tableName: "RCONFIG") {
			column(name: "EMAIL_CONFIGURATION_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "prashantsahi (generated)", id: "1479971939377-27") {
		addForeignKeyConstraint(baseColumnNames: "EMAIL_CONFIGURATION_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE889C7C455F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EMAIL_CONFIGURATION", referencesUniqueColumn: "false")
	}

	changeSet(author: "prashantsahi (generated)", id: "1479971939377-28") {
		addForeignKeyConstraint(baseColumnNames: "EMAIL_CONFIGURATION_ID", baseTableName: "RCONFIG", constraintName: "FK689172149C7C455F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EMAIL_CONFIGURATION", referencesUniqueColumn: "false")
	}

}
