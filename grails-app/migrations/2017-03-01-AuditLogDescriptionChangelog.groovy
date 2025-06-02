databaseChangeLog = {

	changeSet(author: "prashantsahi (generated)", id: "1488352497869-1") {
		addColumn(tableName: "AUDIT_LOG") {
			column(name: "DESCRIPTION_COPY", type: "clob") {
				constraints(nullable: "true")
			}
		}
		sql("update AUDIT_LOG set DESCRIPTION_COPY = DESCRIPTION;")

		dropColumn(tableName: "AUDIT_LOG", columnName: "DESCRIPTION")

		renameColumn(tableName: "AUDIT_LOG", oldColumnName: "DESCRIPTION_COPY", newColumnName: "DESCRIPTION")
	}
	
}