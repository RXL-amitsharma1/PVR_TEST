databaseChangeLog = {

	changeSet(author: "Chetan (generated)", id: "1458158796957-1") {
		modifyDataType(columnName: "CUSTOM_EXPRESSION", newDataType: "varchar2(2000 char)", tableName: "RPT_FIELD_INFO")
	}
}
