databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1457493773485-96") {
		createTable(tableName: "TASK") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TASKPK")
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

			column(name: "due_date", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "task_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "task_template_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "tasks_idx", type: "number(10,0)")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1457493773485-97") {
		createTable(tableName: "TASK_TEMPLATE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "TASK_TEMPLATEPK")
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

	changeSet(author: "Chetan (generated)", id: "1457493773485-214") {
		addForeignKeyConstraint(baseColumnNames: "task_template_id", baseTableName: "TASK", constraintName: "FK272D8525C11A43", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "TASK_TEMPLATE", referencesUniqueColumn: "false")
	}

}
