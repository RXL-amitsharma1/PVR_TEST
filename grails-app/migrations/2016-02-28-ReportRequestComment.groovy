databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1456705108231-72") {
		createTable(tableName: "REPORT_REQUEST_COMMENT") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "REPORT_COMMENT_REQUESPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "REPORT_COMMENT", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "CREATED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "report_request_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456705108231-175") {
		addForeignKeyConstraint(baseColumnNames: "report_request_id", baseTableName: "REPORT_REQUEST_COMMENT", constraintName: "FK14397764C25FAD53", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST", referencesUniqueColumn: "false")
	}
}
