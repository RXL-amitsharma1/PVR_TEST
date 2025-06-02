databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1455817197299-1") {
		createTable(tableName: "WORKFLOW_STATE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "WORKFLOW_STATPK")
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

			column(name: "display", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "final_state", type: "number(1,0)") {
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
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456190789317-1") {
		addColumn(tableName: "WORKFLOW_STATE") {
			column(name: "report_actions", type: "varchar2(255 char)")
		}
	}

}
