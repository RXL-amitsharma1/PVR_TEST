databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1497425726247-1") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "DUE_DATE", type: "timestamp")
		}
	}

	changeSet(author: "forxsv (generated)", id: "1497425726247-2") {
		sql("merge into EX_RCONFIG " +
				"using EX_GLOBAL_DATE_RANGE_INFO " +
				"on (EX_RCONFIG.EX_GLOBAL_DATE_RANGE_INFO_ID=EX_GLOBAL_DATE_RANGE_INFO.ID) " +
				"when matched then " +
				"update set EX_RCONFIG.DUE_DATE=(EX_GLOBAL_DATE_RANGE_INFO.DATE_RNG_END_ABSOLUTE + NVL(EX_RCONFIG.DUE_IN_DAYS, 0))")
	}
}
