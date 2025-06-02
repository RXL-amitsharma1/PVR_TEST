databaseChangeLog = {

	changeSet(author: "michaelmorett (generated)", id: "1459398771260-1") {
		createTable(tableName: "APPLICATION_SETTINGS") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "APPLICATION_SPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CREATED_BY", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}

			column(name: "SHOW_JAPANESE_REPORT_FIELDS", type: "number(1,0)") {
				constraints(nullable: "false")
			}
		}
	}

}
