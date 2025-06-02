databaseChangeLog = {
	changeSet(author: "Pomi (generated)", id: "1465936055353-1") {
		modifyDataType(columnName: "DESCRIPTION", newDataType: "varchar2(1000 char)", tableName: "RCONFIG")
	}

	changeSet(author: "Pomi (generated)", id: "1465936055353-2") {
		modifyDataType(columnName: "DESCRIPTION", newDataType: "varchar2(1000 char)", tableName: "EX_RCONFIG")
	}

	changeSet(author: "Pomi (generated)", id: "1465936055353-3") {
		modifyDataType(columnName: "DESCRIPTION", newDataType: "varchar2(1000 char)", tableName: "RPT_TEMPLT")
	}

	changeSet(author: "Pomi (generated)", id: "1465936055353-4") {
		modifyDataType(columnName: "DESCRIPTION", newDataType: "varchar2(1000 char)", tableName: "SUPER_QUERY")
	}

	changeSet(author: "Pomi (generated)", id: "1465936055353-5") {
		modifyDataType(columnName: "DESCRIPTION", newDataType: "varchar2(1000 char)", tableName: "ACCESS_CONTROL_GROUP")
	}
}
