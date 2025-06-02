databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1457829974856-1") {
		createTable(tableName: "case_series") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "case_seriesPK")
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

			column(name: "date_range_type", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)")

			column(name: "evaluate_date_as", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "event_selection", type: "varchar2(255 char)")

			column(name: "exclude_follow_up", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "exclude_non_valid_cases", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "global_query_id", type: "number(19,0)")

			column(name: "include_locked_version", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "product_selection", type: "varchar2(255 char)")

			column(name: "series_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "study_selection", type: "varchar2(255 char)")

			column(name: "suspect_product", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "use_case_series_id", type: "number(19,0)")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1457829974856-9") {
		addForeignKeyConstraint(baseColumnNames: "global_query_id", baseTableName: "case_series", constraintName: "FK264B8A66EBF0E079", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SUPER_QUERY", referencesUniqueColumn: "false")
	}

	changeSet(author: "Chetan (generated)", id: "1457829974856-10") {
		addForeignKeyConstraint(baseColumnNames: "use_case_series_id", baseTableName: "case_series", constraintName: "FK264B8A6619EAC26", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "EX_RCONFIG", referencesUniqueColumn: "false")
	}
}
