databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1447293058717-1") {
		addColumn(tableName: "PARAM") {
			column(name: "IS_FROM_COPY_PASTE", type: "number(1,0)")
		}
        grailsChange {
            change {
                sql.execute("UPDATE PARAM SET IS_FROM_COPY_PASTE = '0'")
                confirm "Successfully set default value for PARAM."
            }
        }
        addNotNullConstraint(tableName: "PARAM", columnName: "IS_FROM_COPY_PASTE", columnDataType: "number(1,0)")
    }
}
