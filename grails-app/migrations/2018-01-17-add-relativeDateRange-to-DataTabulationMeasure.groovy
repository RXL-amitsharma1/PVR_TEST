databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1516173924833-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'DTAB_MEASURE', columnName: 'RELATIVE_DATE_RNG_VALUE')
			}
		}
		addColumn(tableName: "DTAB_MEASURE") {
			column(name: "RELATIVE_DATE_RNG_VALUE", type: "number(10,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update DTAB_MEASURE set RELATIVE_DATE_RNG_VALUE = 1;")
		addNotNullConstraint(tableName: "DTAB_MEASURE", columnName: "RELATIVE_DATE_RNG_VALUE")
	}

}