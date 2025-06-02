databaseChangeLog = {

	changeSet(author: "Pomi (generated)", id: "1439417089720-1") {
		addColumn(tableName: "QUERY") {
			column(name: "reassess_listedness", type: "varchar2(255 char)")
		}
	}
}
