databaseChangeLog = {
	changeSet(author: "sverma", id: "1573075855997-1") {
		preConditions(onFail: "MARK_RAN") {
			not {
				columnExists(tableName: "XML_TEMPLT_NODE", columnName: "TAG_COLOR")
			}
		}
		addColumn(tableName: "XML_TEMPLT_NODE") {
			column(name: "TAG_COLOR", type: "VARCHAR(256)")
		}
	}

	changeSet(author: "sverma", id: "1573075855997-2") {
		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'XML_TEMPLT_NODE', columnName: 'DATA_TYPE')
		}
		dropColumn(columnName: "DATA_TYPE", tableName: "XML_TEMPLT_NODE")
		dropColumn(columnName: "ALLOWED_VALUES", tableName: "XML_TEMPLT_NODE")
		dropColumn(columnName: "MAX_LENGTH", tableName: "XML_TEMPLT_NODE")
		dropColumn(columnName: "CUSTOM_EXPRESSION", tableName: "XML_TEMPLT_NODE")
		dropColumn(columnName: "MANDATORY", tableName: "XML_TEMPLT_NODE")
		dropColumn(columnName: "PRIMARY", tableName: "XML_TEMPLT_NODE")
		dropColumn(columnName: "JSON_QUERY", tableName: "XML_TEMPLT_NODE")
		sql("update XML_TEMPLT_NODE set TYPE= 'SOURCE_FIELD', RPT_FIELD_INFO_ID=null where TYPE='STATIC_VALUE'");
	}
}