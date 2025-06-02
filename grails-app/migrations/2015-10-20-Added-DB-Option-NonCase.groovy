databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1443721244395-1") {
        addColumn(tableName: "NONCASE_SQL_TEMPLT") {
            column(name: "USE_PVR_DB", type: "number(1,0)") {
            }
        }
        grailsChange {
            change {
                sql.execute("UPDATE NONCASE_SQL_TEMPLT SET USE_PVR_DB = 0")
                confirm "Successfully set default value for NONCASE_SQL_TEMPLT."
            }
        }
        addNotNullConstraint(tableName: "NONCASE_SQL_TEMPLT", columnName: "USE_PVR_DB", columnDataType: "number(1,0)")
	}
}
