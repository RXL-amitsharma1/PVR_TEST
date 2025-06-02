databaseChangeLog = {

	changeSet(author: "Meenakshi (generated)", id: "1473849808394-1") {
		createTable(tableName: "WORKFLOW_JUSTIFICATION") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "WORKFLOW_JUSTPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "EX_REPORT", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "from_state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "routed_by_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "to_state_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}
}
