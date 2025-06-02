databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1518779417987-1") {
		createTable(tableName: "PVR_SETTINGS") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "pvr_settingsPK")
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

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "SKEY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "SVALUE", type: "clob") {
				constraints(nullable: "false")
			}
		}
	}

}
