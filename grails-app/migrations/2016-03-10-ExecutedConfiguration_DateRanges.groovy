databaseChangeLog = {

	changeSet(author: "michaelmorett (generated)", id: "1457664215940-1") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "LAST_RUN_DATE", type: "timestamp")
		}
	}

	changeSet(author: "michaelmorett (generated)", id: "1457664215940-2") {
		addColumn(tableName: "RCONFIG") {
			column(name: "LAST_RUN_DATE", type: "timestamp")
		}
	}

	changeSet(author: "michaelmorett (generated)", id: "1457664215940-3") {
		dropColumn(columnName: "DATE_RNG_END_DELTA", tableName: "DATE_RANGE")
	}

	changeSet(author: "michaelmorett (generated)", id: "1457664215940-4") {
		dropColumn(columnName: "DATE_RNG_START_DELTA", tableName: "DATE_RANGE")
	}

	changeSet(author: "michaelmorett", id: '1457664215940-5') {
		sql("UPDATE EX_RCONFIG SET LAST_RUN_DATE = LAST_UPDATED where LAST_RUN_DATE is null;")
	}
}
