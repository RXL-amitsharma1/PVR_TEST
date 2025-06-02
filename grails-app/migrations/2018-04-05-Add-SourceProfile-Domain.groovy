databaseChangeLog = {


	changeSet(author: "prashantsahi (generated)", id: "1522864529814-2") {
		createTable(tableName: "SOURCE_PROFILE") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "SOURCE_PROFILPK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_CENTRAL", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "false")
			}

			column(name: "SOURCE_ABBREVIATION", type: "varchar2(5 char)") {
				constraints(nullable: "false")
			}

			column(name: "SOURCE_ID", type: "number(10,0)") {
				constraints(nullable: "false")
			}

			column(name: "SOURCE_NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "SOURCE_TYPE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "prashantsahi (generated)", id: "1522864529814-129") {
		createIndex(indexName: "SRC_ABBR_uniq_1522864515471", tableName: "SOURCE_PROFILE", unique: "true") {
			column(name: "SOURCE_ABBREVIATION")
		}
	}

	changeSet(author: "prashantsahi (generated)", id: "1522864529814-130") {
		createIndex(indexName: "SRC_ID_uniq_1522864515471", tableName: "SOURCE_PROFILE", unique: "true") {
			column(name: "SOURCE_ID")
		}
	}

	changeSet(author: "prashantsahi (generated)", id: "1522864529814-131") {
		createIndex(indexName: "SRC_NAME_uniq_1522864515471", tableName: "SOURCE_PROFILE", unique: "true") {
			column(name: "SOURCE_NAME")
		}
	}

	changeSet(author: "jitin (generated)", id: "1960211999018-6") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SOURCE_PROFILE', columnName: 'CASE_NUMBER_FIELD_NAME')
			}
		}

		addColumn(tableName: "SOURCE_PROFILE") {
			column(name: "CASE_NUMBER_FIELD_NAME", type: "VARCHAR2(255 CHAR)", defaultValue: 'masterCaseNum')
		}
	}
}
