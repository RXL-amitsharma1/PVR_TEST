databaseChangeLog = {

	changeSet(author: "prakriti (generated)", id: "1444326078946-1") {
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "SUSPECT_PRODUCT", type: "number(1,0)")
			}
            grailsChange {
                change {
                    sql.execute("UPDATE EX_RCONFIG SET SUSPECT_PRODUCT = '0'")
                    confirm "Successfully set default value for SUSPECT_PRODUCT."
                }
            }
            addNotNullConstraint(tableName: "EX_RCONFIG", columnName: "SUSPECT_PRODUCT", columnDataType: "number(1,0)")

		}

	changeSet(author: "prakriti (generated)", id: "1444326078946-2") {
		addColumn(tableName: "RCONFIG") {
			column(name: "SUSPECT_PRODUCT", type: "number(1,0)") }
        grailsChange {
            change {
                sql.execute("UPDATE RCONFIG SET SUSPECT_PRODUCT = '0'")
                confirm "Successfully set default value for SUSPECT_PRODUCT."
            }
        }
        addNotNullConstraint(tableName: "RCONFIG", columnName: "SUSPECT_PRODUCT", columnDataType: "number(1,0)")

    }
}