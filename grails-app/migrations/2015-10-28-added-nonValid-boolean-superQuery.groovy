databaseChangeLog = {

	changeSet(author: "prakriti (generated)", id: "1446057018699-1") {
		addColumn(tableName: "SUPER_QUERY") {
			column(name: "NON_VALID_CASES", type: "number(1,0)") {
			}
		}
        grailsChange {
            change {
                sql.execute("UPDATE SUPER_QUERY SET NON_VALID_CASES = (CASE WHEN NAME = 'Non-Valid Cases' THEN '1' ELSE '0' END )")
                confirm "Successfully set default value for SUPER_QUERY NON_VALID_CASES."
            }
        }
        addNotNullConstraint(tableName: "SUPER_QUERY", columnName: "NON_VALID_CASES", columnDataType: "number(1,0)")
	}
}
