databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1456705108231-71") {
		createTable(tableName: "REPORT_REQUEST") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "REPORT_REQUESPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "assigned_to_id", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "CREATED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_CREATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "description", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "due_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "LAST_UPDATED", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "priority", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "report_name", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "status", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456705108231-73") {
		createTable(tableName: "report_request_pvuser") {
			column(name: "report_request_requesters_id", type: "number(19,0)")
			column(name: "user_id", type: "number(19,0)")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456804016590-2") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "report_type", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456809354258-3") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "date_range_type", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456809354258-4") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "evaluate_date_as", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456809354258-5") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "event_selection", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456809354258-6") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "exclude_follow_up", type: "number(1,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456809354258-7") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "exclude_non_valid_cases", type: "number(1,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456809354258-8") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "include_locked_version", type: "number(1,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456809354258-9") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "product_selection", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456809354258-10") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "study_selection", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456809354258-11") {
		addColumn(tableName: "REPORT_REQUEST") {
			column(name: "suspect_product", type: "number(1,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Chetan (generated)", id: "1456705108231-174") {
		addForeignKeyConstraint(baseColumnNames: "assigned_to_id", baseTableName: "REPORT_REQUEST", constraintName: "FKDB16FA64203910EE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "Chetan (generated)", id: "1456705108231-176") {
		addForeignKeyConstraint(baseColumnNames: "report_request_requesters_id", baseTableName: "report_request_pvuser", constraintName: "FK4455ECEC205ABC05", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "REPORT_REQUEST", referencesUniqueColumn: "false")
	}

	changeSet(author: "Chetan (generated)", id: "1456705108231-177") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "report_request_pvuser", constraintName: "FK4455ECEC8987134F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "Chetan (generated)", id: "1456977464595-1") {
		createTable(tableName: "report_request_delivery") {
			column(name: "report_request_deliveries_id", type: "number(19,0)")
			column(name: "delivery_option_id", type: "number(19,0)")
		}
	}

}
