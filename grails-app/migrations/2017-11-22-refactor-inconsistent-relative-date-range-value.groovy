databaseChangeLog = {

	changeSet(author: "aditya", id: "17112200000-1") {
		List<String> tableNames = ['CASE_SERIRES_DATE_RANGE_INFO', 'GLOBAL_DATE_RANGE_INFO', 'EX_CS_DATE_RANGE_INFO', 'EX_GLOBAL_DATE_RANGE_INFO']
        tableNames.each {
            sql("UPDATE ${it} SET RELATIVE_DATE_RNG_VALUE='1' WHERE RELATIVE_DATE_RNG_VALUE <> 1 AND DATE_RNG_ENUM NOT IN ('LAST_X_DAYS', 'LAST_X_WEEKS', 'LAST_X_MONTHS', 'LAST_X_YEARS')")
        }
	}


}
