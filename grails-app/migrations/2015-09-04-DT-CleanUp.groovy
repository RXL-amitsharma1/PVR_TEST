databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1441391365783-2") {
		addNotNullConstraint(columnDataType: "clob", columnName: "NON_CASE_SQL", tableName: "NONCASE_SQL_TEMPLT")
	}

	changeSet(author: "Sherry (generated)", id: "1441391365783-4") {
		addNotNullConstraint(columnDataType: "clob", columnName: "SELECT_FROM_STMT", tableName: "SQL_TEMPLT")
	}

	changeSet(author: "Sherry (generated)", id: "1441391365783-5") {
		dropForeignKeyConstraint(baseTableName: "DTAB_TEMPLT", constraintName: "FK56FDF7AA134C62A8")
	}

	changeSet(author: "Sherry (generated)", id: "1441391365783-6") {
		dropForeignKeyConstraint(baseTableName: "DTAB_TEMPLTS_MEASURES", constraintName: "FK34B5C06B9948F089")
	}

	changeSet(author: "Sherry (generated)", id: "1441391365783-7") {
		dropColumn(columnName: "COLUMNS_RF_INFO_LIST_ID", tableName: "DTAB_TEMPLT")
	}

	changeSet(author: "Sherry (generated)", id: "1441391365783-8") {
		dropTable(tableName: "DTAB_TEMPLTS_MEASURES")
	}
}
