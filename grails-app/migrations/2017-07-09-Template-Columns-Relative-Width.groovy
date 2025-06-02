databaseChangeLog = {

	changeSet(author: "gologuzov (generated)", id: "1499588267406-1") {
		addColumn(tableName: "RPT_FIELD_INFO") {
			column(defaultValue: "0", name: "COLUMN_WIDTH", type: "number(10,0)") {
				constraints(nullable: "false")
			}
		}
	}
}
