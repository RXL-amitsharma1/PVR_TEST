databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1455851985428-1") {
		createTable(tableName: "WORKFLOW_RULE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "WORKFLOW_RULEPK")
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

			column(name: "description", type: "varchar2(255 char)")

			column(name: "initial_state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "notify", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "target_state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1455851985428-7") {
		addForeignKeyConstraint(baseColumnNames: "initial_state_id", baseTableName: "WORKFLOW_RULE", constraintName: "FKFF917F7CA80C1C18", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "WORKFLOW_STATE", referencesUniqueColumn: "false")
	}

	changeSet(author: "Chetan (generated)", id: "1455851985428-8") {
		addForeignKeyConstraint(baseColumnNames: "target_state_id", baseTableName: "WORKFLOW_RULE", constraintName: "FKFF917F7C38E3486B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "WORKFLOW_STATE", referencesUniqueColumn: "false")
	}
}
