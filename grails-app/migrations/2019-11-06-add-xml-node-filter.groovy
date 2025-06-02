databaseChangeLog = {
	changeSet(author: "sgologuzov", id: "1573075855986-1") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "XML_TEMPLT_NODE", columnName: "FILTER_FIELD_INFO_ID")
			}
		}
		addColumn(tableName: "XML_TEMPLT_NODE") {
			column(name: "FILTER_FIELD_INFO_ID", type: "number(19,0)")
		}
	}
}