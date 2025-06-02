databaseChangeLog = {

	changeSet(author: "prashantsahi (generated)", id: "1476949451163-7") {
		addColumn(tableName: "CASE_SERIES") {
			column(name: "EVENT_SELECTION_COPY", type: "clob") {
				constraints(nullable: "true")
			}
		}
		sql("update CASE_SERIES set EVENT_SELECTION_COPY = EVENT_SELECTION;")

		dropColumn(tableName: "CASE_SERIES", columnName: "EVENT_SELECTION")

		renameColumn(tableName: "CASE_SERIES", oldColumnName: "EVENT_SELECTION_COPY", newColumnName: "EVENT_SELECTION")
	}

	changeSet(author: "prashantsahi (generated)", id: "1476949451163-10") {
		addColumn(tableName: "CASE_SERIES") {
			column(name: "PRODUCT_SELECTION_COPY", type: "clob") {
				constraints(nullable: "true")
			}
		}
		sql("update CASE_SERIES set PRODUCT_SELECTION_COPY = PRODUCT_SELECTION;")

		dropColumn(tableName: "CASE_SERIES", columnName: "PRODUCT_SELECTION")

		renameColumn(tableName: "CASE_SERIES", oldColumnName: "PRODUCT_SELECTION_COPY", newColumnName: "PRODUCT_SELECTION")
	}

	changeSet(author: "prashantsahi (generated)", id: "1476949451163-13") {
		addColumn(tableName: "CASE_SERIES") {
			column(name: "STUDY_SELECTION_COPY", type: "clob") {
				constraints(nullable: "true")
			}
		}
		sql("update CASE_SERIES set STUDY_SELECTION_COPY = STUDY_SELECTION;")

		dropColumn(tableName: "CASE_SERIES", columnName: "STUDY_SELECTION")

		renameColumn(tableName: "CASE_SERIES", oldColumnName: "STUDY_SELECTION_COPY", newColumnName: "STUDY_SELECTION")
	}

}
