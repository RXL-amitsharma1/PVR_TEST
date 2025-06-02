databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1488560216012-1") {
		createTable(tableName: "REPO_FILE_RESOURCE") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "REPO_FILE_RESPK")
			}

			column(name: "DATA", type: "long raw")

			column(name: "FILE_TYPE", type: "varchar2(20 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1488560216012-2") {
		createTable(tableName: "REPO_FOLDER") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "REPO_FOLDERPK")
			}

			column(name: "URI", type: "varchar2(250 char)") {
				constraints(nullable: "false")
			}

			column(name: "PARENT_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1488560216012-3") {
		createTable(tableName: "REPO_RESOURCE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "REPO_RESOURCEPK")
			}

			column(name: "VERSION", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(250 char)")

			column(name: "LABEL", type: "varchar2(200 char)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(200 char)") {
				constraints(nullable: "false")
			}

			column(name: "PARENT_ID", type: "number(19,0)")

			column(name: "RESOURCE_TYPE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1488560216012-4") {
		addColumn(tableName: "RPT_TEMPLT") {
			column(name: "FIXED_TEMPLT_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1488560216012-11") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'RPT_TEMPLT', columnName: 'USE_FIXED_TEMPLATE')
			}
		}
		addColumn(tableName: "RPT_TEMPLT") {
			column(name: "USE_FIXED_TEMPLATE", type: "number(1,0)")
		}
		sql("UPDATE RPT_TEMPLT SET USE_FIXED_TEMPLATE = 0")
		addNotNullConstraint(tableName: "RPT_TEMPLT", columnName: "USE_FIXED_TEMPLATE", columnDataType: "number(1,0)")
	}

	changeSet(author: "gologuzov (generated)", id: "1488560216012-6") {
		addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "REPO_FILE_RESOURCE", constraintName: "FK52FF9964E0996AE9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPO_RESOURCE", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1488560216012-7") {
		addForeignKeyConstraint(baseColumnNames: "id", baseTableName: "REPO_FOLDER", constraintName: "FKA4C55DBE0996AE9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPO_RESOURCE", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1488560216012-8") {
		addForeignKeyConstraint(baseColumnNames: "PARENT_ID", baseTableName: "REPO_FOLDER", constraintName: "FKA4C55DBC2CCD6FE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "REPO_FOLDER", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1488560216012-9") {
		addForeignKeyConstraint(baseColumnNames: "PARENT_ID", baseTableName: "REPO_RESOURCE", constraintName: "FK82B2523BC2CCD6FE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "REPO_FOLDER", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1488560216012-10") {
		addForeignKeyConstraint(baseColumnNames: "FIXED_TEMPLT_ID", baseTableName: "RPT_TEMPLT", constraintName: "FKDF8342E55BC4D43D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "REPO_FILE_RESOURCE", referencesUniqueColumn: "false")
	}
}
