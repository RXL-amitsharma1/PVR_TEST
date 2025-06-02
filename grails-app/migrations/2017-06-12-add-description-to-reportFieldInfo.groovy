databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1497280862943-1") {
		addColumn(tableName: "RPT_FIELD_INFO") {
			column(name: "RENAME_DESCRIPTION_VALUE", type: "varchar2(255 char)")
		}
	}
}
