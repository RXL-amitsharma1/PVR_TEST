databaseChangeLog = {

	changeSet(author: "lei (generated)", id: "1512635093957-1") {
		createTable(tableName: "spotfire_session") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "spotfire_sessPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "deleted", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "email", type: "varchar2(255 char)")

			column(name: "full_name", type: "varchar2(255 char)")

			column(name: "timestamp", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "token", type: "varchar2(255 char)")

			column(name: "username", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "lei (generated)", id: "1512635093957-100") {
		createIndex(indexName: "token_uniq_1512634964660", tableName: "spotfire_session", unique: "true") {
			column(name: "token")
		}
	}
}
