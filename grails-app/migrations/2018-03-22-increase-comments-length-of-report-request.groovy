databaseChangeLog = {
	changeSet(author: "jitin (generated)", id: "1521718465586-1") {
		modifyDataType(columnName: "NOTE", newDataType: "VARCHAR(4000)", tableName: "COMMENT_TABLE")
	}
}
