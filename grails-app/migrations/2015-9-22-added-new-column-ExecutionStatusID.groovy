databaseChangeLog = {

	changeSet(author: "prakriti (generated)", id: "1442962172788-1") {
		addColumn(tableName: "NOTIFICATION") {
			column(name: "EXS_ID", type: "number(19,0)")
			}
                grailsChange {
                    change {
                        sql.execute("UPDATE NOTIFICATION SET EXS_ID = '0'")
                        confirm "Successfully set default value for EXS_ID."
                    }
                }
                addNotNullConstraint(tableName: "NOTIFICATION", columnName: "EXS_ID", columnDataType: "number(19,0)")
            }
}
