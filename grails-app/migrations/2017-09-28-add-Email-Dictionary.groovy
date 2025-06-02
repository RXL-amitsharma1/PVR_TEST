databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1506665301984-1") {
		createTable(tableName: "EMAIL") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EMAILPK")
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

			column(name: "DESCRIPTION", type: "varchar2(1000 char)") {
				constraints(nullable: "false")
			}

			column(name: "EMAIL", type: "varchar2(200 char)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

}
