databaseChangeLog = {

	changeSet(author: "prakriti (generated)", id: "1441133678948-1") {
		createTable(tableName: "EX_STATUS") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_STATUSPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CONFIG_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "END_TIME", type: "number(19,0)")

			column(name: "EX_STATUS", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "FREQUENCY", type: "varchar2(255 char)")

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MESSAGE", type: "clob")

			column(name: "next_run_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "owner_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_ID", type: "number(19,0)")

			column(name: "report_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SECTION_NAME", type: "varchar2(255 char)")

			column(name: "STACK_TRACE", type: "clob")

			column(name: "START_TIME", type: "number(19,0)")

			column(name: "TEMPLATE_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "prakriti (generated)", id: "1441133678948-2") {
        addColumn(tableName: "RCONFIG") {
            column(name: "EXECUTING", type: "number(1,0)")
        }
        grailsChange {
            change {
                sql.execute("UPDATE RCONFIG SET EXECUTING = '0'")
                confirm "Successfully set default value for RCONFIG."
            }
        }
        addNotNullConstraint(tableName: "RCONFIG", columnName: "EXECUTING", columnDataType: "number(1,0)")
	}

	changeSet(author: "prakriti (generated)", id: "1441133678948-3") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "EX_STATUS", constraintName: "FKDFAA18FEF56DC367", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
}
