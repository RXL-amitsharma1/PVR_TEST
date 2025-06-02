databaseChangeLog = {
	changeSet(author: "gologuzov (generated)", id: "1559017018925-1") {
		addColumn(tableName: "XML_TEMPLT_NODE") {
			column(name: "ELEMENT_TYPE", type: "varchar2(255 char)")
		}
		sql("update XML_TEMPLT_NODE set ELEMENT_TYPE = 'TAG';")
		addNotNullConstraint(tableName: "XML_TEMPLT_NODE", columnName: "ELEMENT_TYPE")
	}
}
