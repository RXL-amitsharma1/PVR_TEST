databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1455814450958-2") {
		createTable(tableName: "ACTION_ITEM") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "ACTION_ITEMPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "action_category", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "cioms_forms", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "completion_date", type: "timestamp")

			column(name: "CREATED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "due_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "notify_by_email", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "notify_by_text", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "priority", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "report_annotations", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "report_output", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "status", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456846340202-1") {
		addColumn(tableName: "ACTION_ITEM") {
			column(name: "app_type", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456934390598-1") {
		createTable(tableName: "report_request_action_item") {
			column(name: "report_request_action_items_id", type: "number(19,0)")
			column(name: "action_item_id", type: "number(19,0)")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456934390598-14") {
		addForeignKeyConstraint(baseColumnNames: "action_item_id", baseTableName: "report_request_action_item", constraintName: "FK4E25A41C6458485", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "ACTION_ITEM", referencesUniqueColumn: "false")
	}

	changeSet(author: "Chetan (generated)", id: "1456934390598-15") {
		addForeignKeyConstraint(baseColumnNames: "report_request_action_items_id", baseTableName: "report_request_action_item", constraintName: "FK4E25A415F07FEC5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST", referencesUniqueColumn: "false")
	}

	changeSet(author: "Chetan (generated)", id: "1456857852752-13") {
		dropColumn(columnName: "CIOMS_FORMS", tableName: "ACTION_ITEM")
	}

	changeSet(author: "Chetan (generated)", id: "1456857852752-14") {
		dropColumn(columnName: "NOTIFY_BY_EMAIL", tableName: "ACTION_ITEM")
	}

	changeSet(author: "Chetan (generated)", id: "1456857852752-15") {
		dropColumn(columnName: "NOTIFY_BY_TEXT", tableName: "ACTION_ITEM")
	}

	changeSet(author: "Chetan (generated)", id: "1456857852752-16") {
		dropColumn(columnName: "REPORT_ANNOTATIONS", tableName: "ACTION_ITEM")
	}

	changeSet(author: "Chetan (generated)", id: "1456857852752-17") {
		dropColumn(columnName: "REPORT_OUTPUT", tableName: "ACTION_ITEM")
	}

}
