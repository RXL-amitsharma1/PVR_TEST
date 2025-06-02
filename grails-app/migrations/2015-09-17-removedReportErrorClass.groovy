databaseChangeLog = {

	changeSet(author: "prakriti (generated)", id: "1442524942443-7") {
		dropForeignKeyConstraint(baseTableName: "RPT_ERROR", constraintName: "FKF5E4423F2983C79B")
	}

	changeSet(author: "prakriti (generated)", id: "1442524942443-8") {
		dropForeignKeyConstraint(baseTableName: "RPT_ERROR", constraintName: "FKF5E4423F2F961DEA")
	}

	changeSet(author: "prakriti (generated)", id: "1442524942443-9") {
		dropForeignKeyConstraint(baseTableName: "RPT_ERROR", constraintName: "FKF5E4423FA2AF25DE")
	}

	changeSet(author: "prakriti (generated)", id: "1442524942443-10") {
		dropForeignKeyConstraint(baseTableName: "RPT_ERROR", constraintName: "FKF5E4423F8DD35DC3")
	}

	changeSet(author: "prakriti (generated)", id: "1442524942443-12") {
		dropTable(tableName: "RPT_ERROR")
	}
}
