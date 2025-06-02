databaseChangeLog = {

	changeSet(author: "Meenakshi (generated)", id: "1470910537349-18") {
		dropNotNullConstraint(tableName: "TASK", columnName: "DUE_DATE")
	}
	changeSet(author: "Meenakshi (generated)", id: "1470910537349-17") {
		sql("UPDATE TASK SET DUE_DATE=NULL;")
	}

	changeSet(author: "Meenakshi (generated)", id: "1470910537349-15") {
		modifyDataType(columnName: "DUE_DATE", newDataType: "number(10,0)", tableName: "TASK")
	}

	changeSet(author: "Meenakshi (generated)", id: "1470910537349-16") {
		sql("UPDATE TASK SET DUE_DATE=0;")
	}

}
