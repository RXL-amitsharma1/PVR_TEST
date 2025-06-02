databaseChangeLog = {

	changeSet(author: "chetansharma (generated)", id: "1522229896902-1") {
		createTable(tableName: "SIGNAL_REPORT") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SIGNAL_REPORTPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "configuration_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CREATED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "is_generating", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "link_url", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "report_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "user_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "chetansharma (generated)", id: "1522229896902-96") {
		addForeignKeyConstraint(baseColumnNames: "configuration_id", baseTableName: "SIGNAL_REPORT", constraintName: "FK_ft63w46pqc381kqjidcotxojh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RCONFIG", referencesUniqueColumn: "false")
	}

}
