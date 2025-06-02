databaseChangeLog = {

	changeSet(author: "michaelmorett", id: "ParameterValue") {

		addColumn(tableName: "PARAM") {
			column(name: "VALUE_CLOB", type: "clob")
		}

		sql("update PARAM set VALUE_CLOB = VALUE;")
		sql("alter table PARAM drop column VALUE;")
		sql("alter table PARAM rename column VALUE_CLOB to VALUE;")
	}

}
