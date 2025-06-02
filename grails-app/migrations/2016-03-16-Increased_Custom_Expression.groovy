databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1458178322547-6") {
		modifyDataType(columnName: "CUSTOM_EXPRESSION", newDataType: "varchar2(2000 char)", tableName: "RPT_FIELD_INFO")
	}
}
