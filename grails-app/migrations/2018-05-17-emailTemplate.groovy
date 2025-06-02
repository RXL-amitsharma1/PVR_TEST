databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1526546773477-1") {
		createTable(tableName: "EMAIL_TEMPLATE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EMAIL_TEMPLATPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "BODY", type: "clob") {
				constraints(nullable: "false")
			}

			column(name: "CREATED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(1000 char)")

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "OWNER_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "TYPE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "forxsv (generated)", id: "1526546773477-112") {
		addForeignKeyConstraint(baseColumnNames: "OWNER_ID", baseTableName: "EMAIL_TEMPLATE", constraintName: "FK_r6bww21ynhxkpcaldajxwptok", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1526546773477-2") {
		preConditions(onFail: "MARK_RAN", onFailMessage: "Table not exists") {
			tableExists(tableName: "PVR_SETTINGS")
		}
		sql("drop table PVR_SETTINGS cascade constraints")
	}
}
