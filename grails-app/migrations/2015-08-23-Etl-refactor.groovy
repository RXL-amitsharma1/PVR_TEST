databaseChangeLog = {

	changeSet(author: "glennsilverman (generated)", id: "1440368505298-1") {
		dropForeignKeyConstraint(baseTableName: "ETL_CASE_TABLE_STATUS", constraintName: "FKD632B410486C89D7")
	}

	changeSet(author: "glennsilverman (generated)", id: "1440368505298-2") {
		dropForeignKeyConstraint(baseTableName: "ETL_MASTER", constraintName: "FK7DA95764486C89D7")
	}

	changeSet(author: "glennsilverman (generated)", id: "1440368505298-3") {
		dropIndex(indexName: "TABLE_NAME_UNIQ_1438716835540", tableName: "ETL_CASE_TABLE_STATUS")
	}

	changeSet(author: "glennsilverman (generated)", id: "1440368505298-4") {
		dropIndex(indexName: "STEP_ID_UNIQ_1438716835541", tableName: "ETL_MASTER")
	}

	changeSet(author: "glennsilverman (generated)", id: "1440368505298-5") {
		dropTable(tableName: "ETL_CASE_TABLE_STATUS")
	}

	changeSet(author: "glennsilverman (generated)", id: "1440368505298-6") {
		dropTable(tableName: "ETL_MASTER")
	}
}
