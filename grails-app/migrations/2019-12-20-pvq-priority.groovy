databaseChangeLog = {
	changeSet(author: "skhovrachev", id: "20121210-1") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "QUALITY_CASE_META_DATA", columnName: "priority")
			}
		}
		addColumn(tableName: "QUALITY_CASE_META_DATA") {
			column(name: "priority", type: "varchar2(255 char)")
		}
	}
}