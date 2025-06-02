databaseChangeLog = {

	changeSet(author: "leigao (generated)", id: "1438305072079-1") {
		createTable(tableName: "MISC_CONFIG") {
			column(name: "KEY", type: "varchar2(255 char)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "MISC_CONFIGPK")
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

			column(name: "VALUE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}
}
