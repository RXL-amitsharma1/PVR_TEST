databaseChangeLog = {

	changeSet(author: "michaelmorett (generated)", id: "1440463234625-1") {
		addColumn(tableName: "AUDIT_LOG") {
			column(name: "PARENT_OBJECT", type: "varchar2(255 char)")
		}
	}

	changeSet(author: "michaelmorett (generated)", id: "1440463234625-2") {
		addColumn(tableName: "AUDIT_LOG") {
			column(name: "PARENT_OBJECT_ID", type: "number(19,0)")
		}
	}

	changeSet(author: "michaelmorett (generated)", id: "1440463234625-3") {
		modifyDataType(columnName: "DESCRIPTION", newDataType: "varchar2(255 char)", tableName: "AUDIT_LOG")
	}

	changeSet(author: "michaelmorett (generated)", id: "1440463234625-4") {
		dropNotNullConstraint(columnDataType: "varchar2(255 char)", columnName: "DESCRIPTION", tableName: "AUDIT_LOG")
	}
}
