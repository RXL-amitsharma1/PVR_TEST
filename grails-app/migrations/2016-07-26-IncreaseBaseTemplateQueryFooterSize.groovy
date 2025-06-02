databaseChangeLog = {

	changeSet(author: "Michael Morett", id: "1469581501461-1") {
		modifyDataType(columnName: "FOOTER", newDataType: "varchar2(1000 char)", tableName: "TEMPLT_QUERY")
		modifyDataType(columnName: "FOOTER", newDataType: "varchar2(1000 char)", tableName: "EX_TEMPLT_QUERY")
	}

}
