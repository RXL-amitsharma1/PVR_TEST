databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1503658305846-1") {
		createTable(tableName: "DMS_CONFIGURATION") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DMS_CONFIGURAPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "DESCRIPTION", type: "varchar2(1000 char)")

			column(name: "EXCLUDE_APPENDIX", type: "number(1,0)")

			column(name: "EXCLUDE_COMMENTS", type: "number(1,0)")

			column(name: "EXCLUDE_CRITERIA_SHEET", type: "number(1,0)")

			column(name: "FORMAT", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(200 char)")

			column(name: "FOLDER", type: "varchar2(200 char)")

			column(name: "NO_DOCUMENT_ON_NO_DATA", type: "number(1,0)")

			column(name: "PAGE_ORIENTATION", type: "varchar2(255 char)")

			column(name: "PAPER_SIZE", type: "varchar2(255 char)")

			column(name: "SENSITIVITY_LABEL", type: "varchar2(255 char)")

			column(name: "SHOW_COMPANY_LOGO", type: "number(1,0)")

			column(name: "SHOW_PAGE_NUMBERING", type: "number(1,0)")

			column(name: "TAG", type: "varchar2(200 char)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1503658305846-2") {
		addColumn(tableName: "APPLICATION_SETTINGS") {
			column(name: "DMS_INTEGRATION", type: "varchar2(4000 char)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1503658305846-3") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "DMS_CONFIGURATION_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1503658305846-4") {
		addColumn(tableName: "RCONFIG") {
			column(name: "DMS_CONFIGURATION_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1503658305846-64") {
		addForeignKeyConstraint(baseColumnNames: "DMS_CONFIGURATION_ID", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE8828429E83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DMS_CONFIGURATION", referencesUniqueColumn: "false")
	}

	changeSet(author: "forxsv (generated)", id: "1503658305846-65") {
		addForeignKeyConstraint(baseColumnNames: "DMS_CONFIGURATION_ID", baseTableName: "RCONFIG", constraintName: "FK6891721428429E83", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DMS_CONFIGURATION", referencesUniqueColumn: "false")
	}
}
