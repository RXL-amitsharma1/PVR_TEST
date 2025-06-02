databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1522082176811-1") {
		createTable(tableName: "EX_XML_TEMPLT") {
			column(name: "id", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "EX_XML_TEMPLTPK")
			}
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-2") {
		createTable(tableName: "XML_TEMPLT") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "XML_TEMPLTPK")
			}

			column(name: "XML_TEMPLT_NODE_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-4") {
		createTable(tableName: "XML_TEMPLT_CLL") {
			column(name: "XML_TEMPLT_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CLL_TEMPLT_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-5") {
		createTable(tableName: "XML_TEMPLT_NODE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "XML_TEMPLT_NOPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "ALLOWED_VALUES", type: "varchar2(255 char)")

			column(name: "CUSTOM_EXPRESSION", type: "varchar2(2000 char)")

			column(name: "DATA_TYPE", type: "varchar2(255 char)")

			column(name: "JSON_QUERY", type: "clob")

			column(name: "MANDATORY", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "MAX_LENGTH", type: "number(10,0)")

			column(name: "ORDERING_NUMBER", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "parent_id", type: "number(19,0)")

			column(name: "RPT_FIELD_ID", type: "number(19,0)")

			column(name: "TAG_NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "CLL_TEMPLT_ID", type: "number(19,0)")

			column(name: "TYPE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "VALUE", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-6") {
		addForeignKeyConstraint(baseColumnNames: "XML_TEMPLT_NODE_ID", baseTableName: "XML_TEMPLT", constraintName: "FK_91cn4rqkc24425vwkgilhly4m", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "XML_TEMPLT_NODE", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-7") {
		addForeignKeyConstraint(baseColumnNames: "CLL_TEMPLT_ID", baseTableName: "XML_TEMPLT_CLL", constraintName: "FK_optmpal6wijw0la7fnpdctflp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CLL_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-8") {
		addForeignKeyConstraint(baseColumnNames: "XML_TEMPLT_ID", baseTableName: "XML_TEMPLT_CLL", constraintName: "FK_7ggil880m44hf33obqskcwho9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "XML_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-9") {
		addForeignKeyConstraint(baseColumnNames: "CLL_TEMPLT_ID", baseTableName: "XML_TEMPLT_NODE", constraintName: "FK_3y8dymnhcbv3rju8xpl73kvnq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CLL_TEMPLT", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-10") {
		addForeignKeyConstraint(baseColumnNames: "parent_id", baseTableName: "XML_TEMPLT_NODE", constraintName: "FK_ear4d88hiqgm92ypoo8q8a069", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "XML_TEMPLT_NODE", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-11") {
		addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_ID", baseTableName: "XML_TEMPLT_NODE", constraintName: "FK_f5h7u0j8ynbva1vdcuqmtfhh9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-12") {
		addColumn(tableName: "XML_TEMPLT_NODE") {
			column(name: "PRIMARY", type: "number(1,0)", defaultValue: 0) {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-13") {
		addColumn(tableName: "XML_TEMPLT_NODE") {
			column(name: "DATE_FORMAT", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-14") {
		addColumn(tableName: "XML_TEMPLT_NODE") {
			column(name: "RPT_FIELD_INFO_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-15") {
		addForeignKeyConstraint(baseColumnNames: "RPT_FIELD_INFO_ID", baseTableName: "XML_TEMPLT_NODE", constraintName: "FK_7b2esdm6f7fiieot0vlqv1xsy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "RPT_FIELD_INFO", referencesUniqueColumn: "false")
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-16") {
		dropForeignKeyConstraint(baseTableName: "XML_TEMPLT_NODE", constraintName: "FK_F5H7U0J8YNBVA1VDCUQMTFHH9")
	}

	changeSet(author: "gologuzov (generated)", id: "1522082176811-17") {
		dropColumn(columnName: "RPT_FIELD_ID", tableName: "XML_TEMPLT_NODE")
	}
}
