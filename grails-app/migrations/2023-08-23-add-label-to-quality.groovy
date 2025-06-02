databaseChangeLog = {

	changeSet(author: "sergey (generated)", id: "202308230844") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'QUALITY_FIELD', columnName: 'LABEL')
			}
		}
		addColumn(tableName: "QUALITY_FIELD") {
			column(name: "LABEL", type: "varchar2(255 char)") {
				constraints(nullable: "true")
			}
		}
	}
}
