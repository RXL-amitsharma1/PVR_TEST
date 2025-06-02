databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1457915882538-1") {
		addColumn(tableName: "PREFERENCE") {
			column(name: "THEME", type: "varchar2(255 char)")
		}
	}

}