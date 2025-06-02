databaseChangeLog = {

	changeSet(author: "prakriti (generated)", id: "1441752822656-1") {
		createTable(tableName: "EX_STATUSES_RPT_FORMATS") {
			column(name: "EX_STATUS_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "RPT_FORMAT", type: "varchar2(255 char)")

			column(name: "RPT_FORMAT_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "prakriti (generated)", id: "1441752822656-2") {
		createTable(tableName: "EX_STATUSES_SHARED_WITHS") {
			column(name: "EX_STATUS_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "SHARED_WITH_ID", type: "number(19,0)")

			column(name: "SHARED_WITH_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "prakriti (generated)", id: "1441752822656-7") {
		addForeignKeyConstraint(baseColumnNames: "SHARED_WITH_ID", baseTableName: "EX_STATUSES_SHARED_WITHS", constraintName: "FK9FA64B264F5A069A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}
}
