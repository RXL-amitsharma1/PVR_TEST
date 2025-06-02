databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1538645457704-1") {
		createTable(tableName: "REPORT_REQUEST_PRIORITY") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_REQ_PR_UESPK")
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

			column(name: "DESCRIPTION", type: "clob")

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
		sql("insert into REPORT_REQUEST_PRIORITY(ID, NAME, VERSION, CREATED_BY, MODIFIED_BY, DATE_CREATED, LAST_UPDATED, IS_DELETED) values(1,'High', 0, 'Application', 'Application', SYSDATE, SYSDATE, 0 )")
		sql("insert into REPORT_REQUEST_PRIORITY(ID, NAME, VERSION, CREATED_BY, MODIFIED_BY, DATE_CREATED, LAST_UPDATED, IS_DELETED) values(2,'Medium', 0, 'Application', 'Application', SYSDATE, SYSDATE, 0 )")
		sql("insert into REPORT_REQUEST_PRIORITY(ID, NAME, VERSION, CREATED_BY, MODIFIED_BY, DATE_CREATED, LAST_UPDATED, IS_DELETED) values(3,'Low', 0, 'Application', 'Application', SYSDATE, SYSDATE, 0 )")

	}
	changeSet(author: "forxsv (generated)", id: "1538657245523-1") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "PRIORITY_ID", type: "number(19,0)")
		}
		sql("update REPORT_REQUEST set PRIORITY_ID=1 where PRIORITY like'HIGH'")
		sql("update REPORT_REQUEST set PRIORITY_ID=2 where PRIORITY like'MEDIUM'")
		sql("update REPORT_REQUEST set PRIORITY_ID=3 where PRIORITY like'LOW'")

		dropColumn(columnName: "PRIORITY", tableName: "REPORT_REQUEST")
	}

	changeSet(author: "forxsv (generated)", id: "1538724835667-1") {
		createTable(tableName: "REPORT_REQUEST_LINK") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_REQ_UESPK")
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

			column(name: "DESCRIPTION", type: "varchar2(4000 char)")

			column(name: "FROM_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "LINK_TYPE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "TO_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "forxsv (generated)", id: "1538724835667-2") {
		createTable(tableName: "REPORT_REQUEST_LINK_TYPE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "RPT_REQ_LNK_SPK")
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

			column(name: "DESCRIPTION", type: "clob")

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}
	changeSet(author: "forxsv (generated)", id: "1538724835667-14") {
		sql("insert into REPORT_REQUEST_LINK_TYPE(ID, NAME, VERSION, CREATED_BY, MODIFIED_BY, DATE_CREATED, LAST_UPDATED, IS_DELETED) values(1,'relates to', 0, 'Application', 'Application', SYSDATE, SYSDATE, 0 )")
		sql("insert into REPORT_REQUEST_LINK_TYPE(ID, NAME, VERSION, CREATED_BY, MODIFIED_BY, DATE_CREATED, LAST_UPDATED, IS_DELETED) values(2,'duplicates', 0, 'Application', 'Application', SYSDATE, SYSDATE, 0 )")
	}
	changeSet(author: "forxsv (generated)", id: "1538724835667-143") {
		addForeignKeyConstraint(baseColumnNames: "FROM_ID", baseTableName: "REPORT_REQUEST_LINK", constraintName: "FK_a8hb2qdshl0swxb94or3g5lc8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1538724835667-144") {
		addForeignKeyConstraint(baseColumnNames: "LINK_TYPE_ID", baseTableName: "REPORT_REQUEST_LINK", constraintName: "FK_4pf5fmkbb40oa9q3cvc3kc8ja", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST_LINK_TYPE", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1538724835667-145") {
		addForeignKeyConstraint(baseColumnNames: "TO_ID", baseTableName: "REPORT_REQUEST_LINK", constraintName: "FK_ifsfh7nv6u9owax65vofsmxo3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST", referencesUniqueColumn: "false")
	}
}
